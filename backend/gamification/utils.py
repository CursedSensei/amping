"""HUMANE PENALTY MATRIX — gamification gating logic.

Implements the three-gate funnel from the source PDF:
    Gate 1 — Grace Window (forgive if dose is late within tolerance)
    Gate 2 — Auto-Forgive Roll (probabilistic forgiveness, consumes monthly quota)
    Gate 3 — Penalty Tier (deterministic streak/XP penalty, tiered by recent miss count)

Inputs are parameterized by age category and regimen stage:
    age_category: derived from PatientUser.birthyear (Child/Adolescent <18, Adult 18-59, Senior 60+)
    stage:        derived from regimen day count (Early M1-2, Mid M3, Advanced M4-6)

The sex modifier from the original PDF (+2h grace window for female patients across all
stages and ages) has been intentionally abolished — do NOT reintroduce it.

The orchestrator `evaluate()` returns a `Decision` dataclass; it does not write to the DB.
Callers (e.g. adherence upload handlers) are responsible for persisting the resulting
streak/XP/penalty effects and incrementing the quota counter when `quota_consumed=True`.

Randomness is seeded by (patient_id, dose_date) by default so re-evaluation of the same
miss is idempotent.
"""

from __future__ import annotations

import random
from dataclasses import dataclass
from datetime import date, datetime, timedelta
from enum import Enum


class AgeCategory(str, Enum):
    CHILD_ADOLESCENT = "CHILD_ADOLESCENT"
    ADULT = "ADULT"
    SENIOR = "SENIOR"


class Stage(str, Enum):
    EARLY = "EARLY"
    MID = "MID"
    ADVANCED = "ADVANCED"


class Gate(str, Enum):
    GATE_1_GRACE = "GATE_1_GRACE"
    GATE_2_AUTO_FORGIVE = "GATE_2_AUTO_FORGIVE"
    GATE_3_PENALTY = "GATE_3_PENALTY"


@dataclass(frozen=True)
class Decision:
    gate_reached: Gate
    forgiven: bool
    forgive_probability_pct: float
    penalty_tier: int | None
    streak_effect: str
    streak_multiplier: float
    xp_delta_pct: int
    system_action: str
    rationale: str
    quota_consumed: bool


AGE_THRESHOLDS = {"child_adolescent_max": 17, "adult_max": 59}

STAGE_BOUNDS = {
    Stage.EARLY: (1, 60),
    Stage.MID: (61, 90),
    Stage.ADVANCED: (91, 180),
}

GRACE_WINDOW_HOURS = {
    AgeCategory.CHILD_ADOLESCENT: {Stage.EARLY: 8,  Stage.MID: 12, Stage.ADVANCED: 8},
    AgeCategory.ADULT:            {Stage.EARLY: 6,  Stage.MID: 8,  Stage.ADVANCED: 4},
    AgeCategory.SENIOR:           {Stage.EARLY: 12, Stage.MID: 16, Stage.ADVANCED: 8},
}

AUTO_FORGIVE_BASE_PCT = {
    AgeCategory.CHILD_ADOLESCENT: {Stage.EARLY: 75, Stage.MID: 85, Stage.ADVANCED: 65},
    AgeCategory.ADULT:            {Stage.EARLY: 60, Stage.MID: 80, Stage.ADVANCED: 70},
    AgeCategory.SENIOR:           {Stage.EARLY: 80, Stage.MID: 90, Stage.ADVANCED: 85},
}

FREQUENCY_MODIFIER = {0: 0, 1: -15, 2: -35}  # 3+ -> LOCKED (None)
RECENCY_BUCKETS = [(3, -20), (7, -10), (14, -5)]  # 15+ -> 0

MONTHLY_QUOTA = {
    AgeCategory.CHILD_ADOLESCENT: {"0": 3, "1_2": 2, "3_plus": 1},
    AgeCategory.ADULT:            {"0": 2, "1_2": 1, "3_plus": 0},
    AgeCategory.SENIOR:           {"0": 3, "1_2": 2, "3_plus": 1},
}

TIER_EFFECTS = {
    1: {"streak_effect": "preserve",      "streak_multiplier": 1.0,  "xp_delta_pct": -10, "action": "ai_check_in"},
    2: {"streak_effect": "multiply_0.75", "streak_multiplier": 0.75, "xp_delta_pct": -25, "action": "provider_notification"},
    3: {"streak_effect": "multiply_0.50", "streak_multiplier": 0.50, "xp_delta_pct": -40, "action": "provider_flagged_bhw_visit"},
    4: {"streak_effect": "reset",         "streak_multiplier": 0.0,  "xp_delta_pct": -50, "action": "bhw_visit_in_person_dot"},
}


def age_category_from_birthyear(birthyear: int, now: datetime) -> AgeCategory:
    """Derive age bucket from birthyear using `now.year - birthyear`."""
    age = now.year - birthyear
    if age <= AGE_THRESHOLDS["child_adolescent_max"]:
        return AgeCategory.CHILD_ADOLESCENT
    if age <= AGE_THRESHOLDS["adult_max"]:
        return AgeCategory.ADULT
    return AgeCategory.SENIOR


def stage_from_day(current_day: int) -> Stage:
    """Map 1-based regimen day to Stage. Out-of-range days clamp to nearest stage."""
    if current_day < 1:
        return Stage.EARLY
    if current_day <= 60:
        return Stage.EARLY
    if current_day <= 90:
        return Stage.MID
    return Stage.ADVANCED


def grace_window_hours(age: AgeCategory, stage: Stage) -> int:
    return GRACE_WINDOW_HOURS[age][stage]


def auto_forgive_base_pct(age: AgeCategory, stage: Stage) -> int:
    return AUTO_FORGIVE_BASE_PCT[age][stage]


def frequency_modifier_pct(miss_count_7d: int) -> int | None:
    """Return modifier in pct, or None to signal LOCKED (3+ misses)."""
    if miss_count_7d >= 3:
        return None
    return FREQUENCY_MODIFIER.get(miss_count_7d, 0)


def recency_modifier_pct(last_relapse_date: date | None, today: date) -> int:
    if last_relapse_date is None:
        return 0
    days_since = (today - last_relapse_date).days
    if days_since < 0:
        return 0
    for max_days, modifier in RECENCY_BUCKETS:
        if days_since <= max_days:
            return modifier
    return 0


def monthly_quota_slots(age: AgeCategory, prior_relapses_30d: int) -> int:
    if prior_relapses_30d <= 0:
        key = "0"
    elif prior_relapses_30d <= 2:
        key = "1_2"
    else:
        key = "3_plus"
    return MONTHLY_QUOTA[age][key]


def penalty_tier_from_miss_count(miss_count_7d: int) -> int:
    """1st miss -> Tier 1, 2nd -> Tier 2, 3rd -> Tier 3, 4th+ -> Tier 4."""
    if miss_count_7d <= 1:
        return 1
    if miss_count_7d == 2:
        return 2
    if miss_count_7d == 3:
        return 3
    return 4


def tier_effects(tier: int) -> dict:
    return TIER_EFFECTS[tier]


def make_seeded_rng(patient_id: int, dose_date: date) -> random.Random:
    """RNG seeded by (patient_id, dose_date) so re-evaluation is deterministic."""
    seed = hash((patient_id, dose_date.isoformat()))
    return random.Random(seed)


def get_miss_dates_past_7d(patient, today: date) -> list[date]:
    """Dose-dates of UNVERIFIED_ABSENCE or TECHNICAL_MISS records in (today-7, today]."""
    from adherence.models import AdherenceDayRecord, AdherenceStatusEnum

    start = today - timedelta(days=7)
    qs = (
        AdherenceDayRecord.objects
        .filter(
            patient=patient,
            dose_date__gt=start,
            dose_date__lte=today,
            status__in=[
                AdherenceStatusEnum.UNVERIFIED_ABSENCE,
                AdherenceStatusEnum.TECHNICAL_MISS,
            ],
        )
        .order_by("dose_date")
        .values_list("dose_date", flat=True)
    )
    return list(qs)


def get_last_relapse_date(patient, today: date) -> date | None:
    """Most recent UNVERIFIED_ABSENCE dose_date on or before `today`.

    Technical misses are excluded — they reflect device/network failures, not
    behavioral relapse, so they should not drive the recency penalty.
    """
    from adherence.models import AdherenceDayRecord, AdherenceStatusEnum

    record = (
        AdherenceDayRecord.objects
        .filter(
            patient=patient,
            dose_date__lte=today,
            status=AdherenceStatusEnum.UNVERIFIED_ABSENCE,
        )
        .order_by("-dose_date")
        .values_list("dose_date", flat=True)
        .first()
    )
    return record


def get_prior_relapses_30d(patient, today: date) -> int:
    """Count of UNVERIFIED_ABSENCE records in (today-30, today]."""
    from adherence.models import AdherenceDayRecord, AdherenceStatusEnum

    start = today - timedelta(days=30)
    return (
        AdherenceDayRecord.objects
        .filter(
            patient=patient,
            dose_date__gt=start,
            dose_date__lte=today,
            status=AdherenceStatusEnum.UNVERIFIED_ABSENCE,
        )
        .count()
    )


def sync_month3_protected(patient_stats) -> None:
    """Set month3_protected = True iff current_day is in the Mid stage (61..90).

    Idempotent. Safe to call from any flow that advances or recomputes current_day.
    Persists only if the value actually changes.
    """
    desired = 61 <= patient_stats.current_day <= 90
    if patient_stats.month3_protected != desired:
        patient_stats.month3_protected = desired
        patient_stats.save(update_fields=["month3_protected"])


def current_day_of_regimen(patient_stats, today: date) -> int:
    """1-based day index in the regimen given today's date."""
    return max(1, (today - patient_stats.regimen_start_date).days + 1)


def reset_monthly_quota_if_new_period(patient_stats, today: date) -> None:
    """If the stored period_start is in a different calendar month than `today`,
    reset the used counter to 0 and set period_start to the first of `today`'s month.
    Saves `patient_stats`.
    """
    period_start = patient_stats.forgiveness_quota_period_start
    current_period = date(today.year, today.month, 1)
    if period_start is None or (period_start.year, period_start.month) != (today.year, today.month):
        patient_stats.forgiveness_quota_used_this_month = 0
        patient_stats.forgiveness_quota_period_start = current_period
        patient_stats.save(update_fields=[
            "forgiveness_quota_used_this_month",
            "forgiveness_quota_period_start",
        ])


def _clamp(value: float, lo: float, hi: float) -> float:
    return max(lo, min(hi, value))


def evaluate(
    *,
    patient_id: int,
    birthyear: int,
    now: datetime,
    scheduled_dose_time: datetime,
    dose_date: date,
    current_day_of_regimen: int,
    miss_dates_past_7d: list[date],
    last_relapse_date: date | None,
    prior_relapses_30d: int,
    quota_used_this_month: int,
    rng: random.Random | None = None,
) -> Decision:
    """Run Gate 1 → Gate 2 → Gate 3 and return a Decision. No DB writes.

    The current miss is assumed to be already included in `miss_dates_past_7d`,
    so its length drives both the frequency modifier and the Gate 3 tier.
    """
    age = age_category_from_birthyear(birthyear, now)
    stage = stage_from_day(current_day_of_regimen)
    today = now.date()
    if rng is None:
        rng = make_seeded_rng(patient_id, dose_date)

    # Gate 1 — Grace Window
    lateness_hours = (now - scheduled_dose_time).total_seconds() / 3600.0
    window_h = grace_window_hours(age, stage)
    if 0 <= lateness_hours <= window_h:
        return Decision(
            gate_reached=Gate.GATE_1_GRACE,
            forgiven=True,
            forgive_probability_pct=100.0,
            penalty_tier=None,
            streak_effect="preserve",
            streak_multiplier=1.0,
            xp_delta_pct=0,
            system_action="none",
            rationale=(
                f"Gate 1 grace: {lateness_hours:.1f}h late ≤ {window_h}h window "
                f"(age={age.value}, stage={stage.value})."
            ),
            quota_consumed=False,
        )

    # Gate 2 — Auto-Forgive Roll
    miss_count_7d = len(miss_dates_past_7d)
    freq_mod = frequency_modifier_pct(miss_count_7d)
    base = auto_forgive_base_pct(age, stage)
    recency_mod = recency_modifier_pct(last_relapse_date, today)
    slots = monthly_quota_slots(age, prior_relapses_30d)

    if freq_mod is None:
        prob = 0.0
        prob_explanation = (
            f"LOCKED (3+ misses in past 7d); base {base} ignored, recency {recency_mod:+d} ignored"
        )
    else:
        prob = _clamp(base + freq_mod + recency_mod, 0.0, 100.0)
        prob_explanation = (
            f"base {base} + freq {freq_mod:+d} + recency {recency_mod:+d} = {prob:.0f}%"
        )

    quota_available = quota_used_this_month < slots

    if quota_available and prob > 0:
        roll = rng.uniform(0, 100)
        if roll < prob:
            return Decision(
                gate_reached=Gate.GATE_2_AUTO_FORGIVE,
                forgiven=True,
                forgive_probability_pct=prob,
                penalty_tier=None,
                streak_effect="preserve",
                streak_multiplier=1.0,
                xp_delta_pct=0,
                system_action="none",
                rationale=(
                    f"Gate 2 forgiven: rolled {roll:.1f} < probability {prob:.0f}% "
                    f"({prob_explanation}); quota {quota_used_this_month + 1}/{slots} used."
                ),
                quota_consumed=True,
            )
        gate2_outcome = (
            f"rolled {roll:.1f} ≥ probability {prob:.0f}% ({prob_explanation}); "
            f"quota {quota_used_this_month}/{slots} used"
        )
    else:
        if not quota_available:
            gate2_outcome = (
                f"quota exhausted ({quota_used_this_month}/{slots}); skipped roll "
                f"(probability would have been {prob:.0f}%, {prob_explanation})"
            )
        else:
            gate2_outcome = f"probability 0% ({prob_explanation}); skipped roll"

    # Gate 3 — Penalty Tier
    tier = penalty_tier_from_miss_count(miss_count_7d)
    effects = tier_effects(tier)
    return Decision(
        gate_reached=Gate.GATE_3_PENALTY,
        forgiven=False,
        forgive_probability_pct=prob,
        penalty_tier=tier,
        streak_effect=effects["streak_effect"],
        streak_multiplier=effects["streak_multiplier"],
        xp_delta_pct=effects["xp_delta_pct"],
        system_action=effects["action"],
        rationale=(
            f"Gate 3 Tier {tier}: {miss_count_7d} miss(es) in past 7d. "
            f"Gate 2 outcome — {gate2_outcome}."
        ),
        quota_consumed=False,
    )


def apply_decision(
    *,
    decision: Decision,
    patient_stats,
    adherence_record,
    today: date,
) -> None:
    """Apply a Decision to PatientStats and create a PenaltyEvent if Gate 3 fired.

    Mutations:
      - Gate 1: no DB change (caller already created the AdherenceDayRecord).
      - Gate 2 forgiven: increment forgiveness_quota_used_this_month.
      - Gate 3: apply streak_multiplier to current_streak (min 1 for partial resets,
                0 for full reset), create a PenaltyEvent with the tier.

    Callers are expected to be inside a transaction.atomic() block. This function
    saves patient_stats but does not flag the AdherenceDayRecord status — that is
    the caller's responsibility.
    """
    from .models import PenaltyEvent

    if decision.gate_reached == Gate.GATE_1_GRACE:
        return

    if decision.gate_reached == Gate.GATE_2_AUTO_FORGIVE and decision.quota_consumed:
        patient_stats.forgiveness_quota_used_this_month += 1
        patient_stats.save(update_fields=["forgiveness_quota_used_this_month"])
        return

    if decision.gate_reached == Gate.GATE_3_PENALTY:
        if decision.streak_effect == "preserve":
            pass
        elif decision.streak_effect == "reset":
            patient_stats.current_streak = 0
        else:
            reduced = int(patient_stats.current_streak * decision.streak_multiplier)
            patient_stats.current_streak = max(1, reduced) if patient_stats.current_streak > 0 else 0
        patient_stats.save(update_fields=["current_streak"])

        PenaltyEvent.objects.create(
            patient_stats=patient_stats,
            adherence_record=adherence_record,
            date=today,
            tier=decision.penalty_tier,
            penalty_given=abs(decision.xp_delta_pct),
            reverted=False,
        )


def revert_penalty_for_record(adherence_record) -> bool:
    """Mark any PenaltyEvent linked to this record as reverted and refund quota
    if it was a Gate 2 consumption. Returns True if anything was reverted.

    Streak recompute is intentionally deferred — restoring the exact pre-miss
    streak requires re-running evaluate() over the patient's full history, which
    is out of scope for this hook.
    """
    from .models import PenaltyEvent

    events = PenaltyEvent.objects.filter(adherence_record=adherence_record, reverted=False)
    if not events.exists():
        return False
    for event in events:
        event.reverted = True
        event.save(update_fields=["reverted"])
    return True


def refund_quota_for_forgiven_record(patient_stats, adherence_record) -> None:
    """If a Gate 2 forgiveness was previously recorded against this dose (no
    PenaltyEvent, but quota was consumed), decrement quota_used. Currently a
    no-op stub — Gate 2 consumptions don't yet persist a discriminator
    distinguishing them from other quota uses. Documented for future expansion.
    """
    # No-op: Gate 2 quota consumption is tracked only as a counter increment;
    # there is no per-record link to undo. A future schema change could add a
    # GraceConsumption row keyed to AdherenceDayRecord to make this reversible.
    return
