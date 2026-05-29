/**
 * Adapter utilities — normalize snake_case API responses to the camelCase
 * local types used by pages and components.
 */

import type { WebAdherenceDayEntry } from '../api_types/Web_AdherenceMonthResponse';
import { AdherenceStatusEnum } from '../api_types/Web_AdherenceMonthResponse';
import type { WebAnomalousEntry } from '../api_types/Web_AnomalousEntriesResponse';
import type { WebPenaltyEvent } from '../api_types/Web_GamificationResponse';
import type { WebPatientListItem } from '../api_types/Web_PatientListResponse';
import type {
    AnomalousEntry,
    DayStatus,
    HeatmapDay,
    Patient,
    PDCPoint,
    PenaltyEvent,
    RiskTier,
    WeekDay,
} from '../data/mockData';

// ─── Status maps ─────────────────────────────────────────────────────────────

const STATUS_MAP: Record<AdherenceStatusEnum, DayStatus> = {
  [AdherenceStatusEnum.AppRecorded]:       'app-recorded',
  [AdherenceStatusEnum.ProviderReconciled]: 'provider-reconciled',
  [AdherenceStatusEnum.TechnicalMiss]:     'technical-miss',
  [AdherenceStatusEnum.UnverifiedAbsence]: 'unverified-absence',
};

export function toAdherenceStatus(api: AdherenceStatusEnum): DayStatus {
  return STATUS_MAP[api] ?? 'app-recorded';
}

export function toRiskTier(api: string): RiskTier {
  if (api === 'tier1' || api === 'tier2' || api === 'tier3' || api === 'safe') return api;
  return 'safe';
}

// ─── Heatmap builder ─────────────────────────────────────────────────────────

/**
 * Converts WebAdherenceDayEntry[] into the HeatmapDay[] grid (with Mon-first
 * leading-null padding) expected by UnifiedAdherenceRecord.
 */
export function buildHeatmapFromApi(
  days: WebAdherenceDayEntry[],
  year: number,
  month: number, // 1-indexed
): { heatmapDays: HeatmapDay[]; heatmapStartDay: number } {
  // Weekday of the 1st (0 = Mon … 6 = Sun, Mon-first)
  const jsDay   = new Date(year, month - 1, 1).getDay(); // Sun=0
  const startDay = (jsDay + 6) % 7;                      // Mon=0

  const byDay = new Map<number, WebAdherenceDayEntry>();
  for (const entry of days) {
    byDay.set(new Date(entry.date).getDate(), entry);
  }

  const daysInMonth = new Date(year, month, 0).getDate();
  const today       = new Date();
  const todayY      = today.getFullYear();
  const todayM      = today.getMonth() + 1;
  const todayD      = today.getDate();

  const result: HeatmapDay[] = [];

  for (let i = 0; i < startDay; i++) {
    result.push({ date: null, status: 'future' });
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const isFuture =
      year > todayY ||
      (year === todayY && month > todayM) ||
      (year === todayY && month === todayM && d > todayD);

    if (isFuture) {
      result.push({ date: d, status: 'future' });
      continue;
    }

    const entry = byDay.get(d);
    if (entry) {
      result.push({
        date: d,
        status: toAdherenceStatus(entry.status),
        symptoms: entry.symptoms ?? [],
        videoLink: entry.video_link ?? null,
      });
    } else {
      result.push({ date: d, status: 'app-recorded', symptoms: [], videoLink: null });
    }
  }

  return { heatmapDays: result, heatmapStartDay: startDay };
}

// ─── Anomalous entry ─────────────────────────────────────────────────────────

export function toAnomalousEntry(api: WebAnomalousEntry): AnomalousEntry {
  const lower = api.reason.toLowerCase();
  let statusBadge: AnomalousEntry['statusBadge'] = 'unverified-miss';
  if (lower.includes('crash') || lower.includes('technical') || lower.includes('connectivity')) {
    statusBadge = 'tech-failure';
  } else if (lower.includes('app') && lower.includes('miss')) {
    statusBadge = 'app-miss';
  }

  return {
    id: String(api.id),
    date: new Date(api.date).toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    }),
    statusBadge,
    detectedCause: api.reason,
  };
}

// ─── Penalty event ───────────────────────────────────────────────────────────

export function toPenaltyEvent(api: WebPenaltyEvent): PenaltyEvent {
  const dateLabel = new Date(api.date).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  });
  return {
    date: dateLabel,
    tier: (api.tier === 1 || api.tier === 2 ? api.tier : 1) as 1 | 2,
    label: api.label,
  };
}

// ─── PDC point ───────────────────────────────────────────────────────────────

export function toPDCPoint(week: number, pdc: number): PDCPoint {
  return { week: `Week ${week}`, pdc };
}

// ─── Patient list item → partial Patient ─────────────────────────────────────

/**
 * Maps WebPatientListItem to the subset of the local Patient type used by
 * PatientRoster. Only includes fields available in the list response.
 */
export function toPatientListEntry(
  api: WebPatientListItem,
): Omit<Patient, 'anomalousEntries' | 'penaltyHistory' | 'pdcTrend' | 'heatmapMonth' | 'heatmapStartDay' | 'heatmapDays'> {
  return {
    id: String(api.id),
    name: `${api.firstname} ${api.lastname}`,
    age: api.age,
    ageProfile: api.age_profile,
    clinic: '',
    provider: '',
    bhw: '',
    patientId: api.patient_id,
    regimentStart: '',
    currentDay: api.current_day,
    totalDays: api.total_days,
    currentStreak: api.current_streak,
    bestStreak: api.current_streak,
    heartQuota: api.heart_quota,
    riskTier: toRiskTier(api.risk_tier),
    lastActive: api.last_active,
    triggerReason: api.trigger_reason,
    lastSyncLabel: api.last_sync_label,
    symptomReported: api.symptom_reported,
    monthPDC: api.month_pdc,
    pdcTarget: api.pdc_target,
    month3Protected: api.month3_protected,
    weeklyCompliance: api.weekly_compliance.map((w) => ({
      day: w.day as WeekDay['day'],
      status: w.status,
    })),
  };
}
