# Patient Entity Attributes for ERD

## Overview
Comprehensive categorization of all patient attributes needed upon onboarding, derived from the current implementation and TB adherence tracking requirements.

---

## KEY ATTRIBUTES
Attributes that uniquely identify a patient record.

| Attribute | Type | Description |
|-----------|------|-------------|
| `id` | String (UUID) | System-generated unique patient identifier |
| `patientId` | String | External patient ID (e.g., PH-TB-2024-0031) for clinic/registry reference |

---
a
## SIMPLE ATTRIBUTES
Single-valued, atomic attributes with no substructure.

### Demographics
| Attribute | Type | Domain | Description |
|-----------|------|--------|-------------|
| `name` | String | Any name | Patient's full name |
| `age` | Integer | 0–120 | Patient's age in years |
| `ageProfile` | Enum | {'Child', 'Adult', 'Senior'} | Age bracket category |

### Clinic & Provider Assignment
| Attribute | Type | Domain | Description |
|-----------|------|--------|-------------|
| `clinic` | String | Clinic name | Health facility where patient is registered |
| `provider` | String | Provider name | Healthcare provider/doctor name |
| `bhw` | String | BHW name | Barangay Health Worker assigned to patient |

### Treatment Progress
| Attribute | Type | Domain | Description |
|-----------|------|--------|-------------|
| `regimentStart` | Date/String | ISO 8601 date | Start date of TB treatment regimen |
| `currentDay` | Integer | 0–180+ | Current day in treatment course |
| `totalDays` | Integer | 180+ | Total days in prescribed regimen |
| `monthPDC` | Integer | 0–100 | Proportion of Days Covered (%) this month |
| `pdcTarget` | Integer | 0–100 | Target PDC threshold (typically 85%) |

### Adherence Metrics
| Attribute | Type | Domain | Description |
|-----------|------|--------|-------------|
| `currentStreak` | Integer | 0+ | Current consecutive compliant days |
| `bestStreak` | Integer | 0+ | Best (highest) consecutive compliant days to date |
| `heartQuota` | Integer | 0–3 | Remaining heart lives/chances before escalation |

### Risk Management
| Attribute | Type | Domain | Description |
|-----------|------|--------|-------------|
| `riskTier` | Enum | {'tier1', 'tier2', 'tier3', 'safe'} | Current risk classification tier |
| `month3Protected` | Boolean | true/false | Month 3 adherence protection flag (Days 61–90) |

### Activity & Status
| Attribute | Type | Domain | Description |
|-----------|------|--------|-------------|
| `lastActive` | String | Relative time | Last activity timestamp (e.g., "4 days ago", "Today") |
| `lastSyncLabel` | String | Relative time | Last sync/data update (e.g., "1 day ago", "2 hours ago") |
| `triggerReason` | String | Reason text | Why patient triggered escalation (e.g., "3 unverified misses in 7 days") |
| `symptomReported` | String (Optional) | Symptom text | Any reported symptoms (e.g., "Dizziness reported") |

### Calendar Metadata
| Attribute | Type | Domain | Description |
|-----------|------|--------|-------------|
| `heatmapMonth` | String | "MONTH YEAR" format | Month being displayed (e.g., "MAY 2026") |
| `heatmapStartDay` | Integer | 0–6 | Day of week month starts on (0=Mon, 6=Sun) |

---

## COMPOSITE ATTRIBUTES
Attributes composed of multiple related fields; can be simple (non-decomposable in UI) or stored as objects.

| Attribute | Sub-Attributes | Type | Description |
|-----------|----------------|------|-------------|
| `regimentMetadata` | `regimentStart`, `currentDay`, `totalDays` | Composite | Treatment timeline information grouped logically |
| `streakMetrics` | `currentStreak`, `bestStreak` | Composite | Adherence streak tracking (current vs. best) |
| `pdcMetrics` | `monthPDC`, `pdcTarget` | Composite | PDC performance with target threshold |
| `contactInfo` | `clinic`, `provider`, `bhw` | Composite | Care team assignment and facility details |

---

## SINGLE-VALUED vs. MULTI-VALUED ATTRIBUTES

### SINGLE-VALUED (One value per patient)
- `id` — unique identifier
- `name` — patient name
- `age` — single age value
- `ageProfile` — one category
- `clinic` — one facility assignment
- `provider` — one provider assigned
- `bhw` — one BHW assigned
- `patientId` — one registry ID
- `regimentStart` — one start date
- `currentDay` — one current position
- `totalDays` — one regimen duration
- `currentStreak` — single current streak
- `bestStreak` — single best streak value
- `heartQuota` — single quota count
- `riskTier` — one tier classification
- `lastActive` — most recent activity timestamp
- `triggerReason` — current escalation reason
- `lastSyncLabel` — most recent sync time
- `symptomReported` — reported symptom (optional, single)
- `monthPDC` — this month's PDC
- `pdcTarget` — target threshold
- `month3Protected` — protection flag
- `heatmapMonth` — display month
- `heatmapStartDay` — calendar start day

### MULTI-VALUED (Collections of records)
- `weeklyCompliance[]` — 7 weekly compliance entries
- `anomalousEntries[]` — variable number of anomalous dose records
- `penaltyHistory[]` — variable number of penalty events
- `pdcTrend[]` — variable number of weekly PDC data points
- `heatmapDays[]` — 35–42 heatmap day entries (one calendar month)

---

## STORED vs. DERIVED ATTRIBUTES

### STORED (Stored directly in database)
Core onboarding and user-provided information:
- `id` (system-generated)
- `patientId` (external registry ID)
- `name`
- `age`
- `ageProfile`
- `clinic`
- `provider`
- `bhw`
- `regimentStart`
- `totalDays` (prescribed regimen length)
- `pdcTarget` (organizational/protocol-based threshold)
- `symptomReported` (optional, user-entered)

### DERIVED (Computed from stored data)
Calculated from dose records, sync data, or business logic:
- `currentDay` — `floor((now - regimentStart) / 86400)` or from dose log end date
- `currentStreak` — computed from consecutive compliant days
- `bestStreak` — maximum streak to date from historical records
- `heartQuota` — computed from penalty events or escalation threshold
- `riskTier` — derived from penalty history, streaks, and compliance rules
- `monthPDC` — calculated as `(compliant_days_this_month / days_in_month) * 100`
- `month3Protected` — derived from `currentDay` (true if 61–90)
- `lastActive` — timestamp of most recent sync or app interaction
- `lastSyncLabel` — human-readable relative time
- `triggerReason` — derived from compliance anomalies and risk rules
- `weeklyCompliance[]` — aggregated from dose records for each day
- `anomalousEntries[]` — detected from inconsistencies in dose logs
- `penaltyHistory[]` — generated from risk escalation events
- `pdcTrend[]` — computed from weekly compliance aggregates
- `heatmapMonth` — display parameter (not patient data)
- `heatmapStartDay` — calendar calculation for month start day
- `heatmapDays[]` — generated from anomalousEntries and day-by-day compliance

---

## COMPLEX ATTRIBUTES
Multi-level composite or collection attributes requiring detailed data structures.

### 1. `weeklyCompliance: WeekDay[]`
**Type:** Multi-valued, derived composite  
**Structure:**
```typescript
WeekDay {
  day: 'Mon' | 'Tue' | 'Wed' | 'Thu' | 'Fri' | 'Sat' | 'Sun'
  status: 'done' | 'missed' | 'pending'
}
```
**Description:** 7-element array representing last 7 days of compliance status per day of week.  
**Derivation:** Aggregated from dose logs for the past week.

---

### 2. `anomalousEntries: AnomalousEntry[]`
**Type:** Multi-valued, derived collection  
**Structure:**
```typescript
AnomalousEntry {
  id: string              // unique anomaly record ID
  date: string            // date of anomaly (e.g., "May 18, 2026")
  statusBadge: 'unverified-miss' | 'tech-failure' | 'app-miss'
  detectedCause: string   // description of detected issue
}
```
**Description:** Variable-length list of detected inconsistencies or technical issues in dose records.  
**Derivation:** Derived from dose sync log analysis and technical error detection.

---

### 3. `penaltyHistory: PenaltyEvent[]`
**Type:** Multi-valued, stored/derived collection  
**Structure:**
```typescript
PenaltyEvent {
  date: string           // date of penalty event (e.g., "May 6")
  tier: 1 | 2            // penalty tier applied (Tier 1 or Tier 2)
  label: string          // description (e.g., "Tier 1 Applied", "Tier 2 (Downgraded)")
}
```
**Description:** Historical log of all risk escalation events applied to this patient.  
**Derivation:** Generated by risk escalation business logic based on compliance failures.

---

### 4. `pdcTrend: PDCPoint[]`
**Type:** Multi-valued, derived collection  
**Structure:**
```typescript
PDCPoint {
  week: string           // week label (e.g., "Week 1", "Week 2")
  pdc: number            // PDC percentage for that week (0–100)
}
```
**Description:** Weekly PDC performance trend (typically 4 weeks for a month view).  
**Derivation:** Computed from daily compliance aggregates per week.

---

### 5. `heatmapDays: HeatmapDay[]`
**Type:** Multi-valued, derived collection  
**Structure:**
```typescript
HeatmapDay {
  date: number | null           // date of month (1–31) or null for padding
  status: DayStatus             // one of: 'app-recorded', 'provider-reconciled', 
                                // 'technical-miss', 'unverified-absence', 'future'
  note?: string                 // optional detail about the day
}
```
**Description:** Calendar-grid representation of the entire month (35–42 elements including padding for week alignment).  
**Derivation:** Built from `anomalousEntries`, daily dose logs, provider reconciliation records, and the heatmap metadata (`heatmapMonth`, `heatmapStartDay`).

---

## ATTRIBUTE DEPENDENCY GRAPH

### Onboarding (Required at enrollment):
```
Demographics: name, age, ageProfile
Clinic Setup: clinic, provider, bhw, patientId
Treatment: regimentStart, totalDays, pdcTarget
```

### Derived on First Sync:
```
currentDay ← (now - regimentStart)
month3Protected ← (currentDay >= 61 && currentDay <= 90)
lastSyncLabel ← system timestamp
```

### Updated on Each Dose Event:
```
weeklyCompliance[] ← dose records (past 7 days)
currentStreak ← consecutive compliant days from dose logs
monthPDC ← (compliant_days_this_month / days_in_month) × 100
pdcTrend[] ← weekly PDC aggregates
heatmapDays[] ← day-by-day status from dose logs + anomalies
```

### Updated on Risk Escalation:
```
riskTier ← escalation business logic (based on penalties & compliance)
heartQuota ← remaining lives (from escalation thresholds)
triggerReason ← escalation trigger description
penaltyHistory[] ← new penalty event appended
lastActive ← timestamp of escalation event
```

### Updated on Anomaly Detection:
```
anomalousEntries[] ← new anomaly records discovered in sync
triggerReason ← may be updated with new anomaly details
```

---

## SUMMARY TABLE

| Category | Count | Attributes |
|----------|-------|------------|
| **Key** | 2 | id, patientId |
| **Simple** | 20 | name, age, ageProfile, clinic, provider, bhw, regimentStart, currentDay, totalDays, monthPDC, pdcTarget, currentStreak, bestStreak, heartQuota, riskTier, lastActive, triggerReason, lastSyncLabel, symptomReported, month3Protected |
| **Composite** | 4 | regimentMetadata, streakMetrics, pdcMetrics, contactInfo |
| **Single-Valued** | 23 | All attributes except collections |
| **Multi-Valued** | 5 | weeklyCompliance[], anomalousEntries[], penaltyHistory[], pdcTrend[], heatmapDays[] |
| **Stored** | 12 | id, patientId, name, age, ageProfile, clinic, provider, bhw, regimentStart, totalDays, pdcTarget, symptomReported |
| **Derived** | 18 | currentDay, currentStreak, bestStreak, heartQuota, riskTier, monthPDC, month3Protected, lastActive, lastSyncLabel, triggerReason, weeklyCompliance[], anomalousEntries[], penaltyHistory[], pdcTrend[], heatmapDays[], heatmapMonth, heatmapStartDay |
| **Complex** | 5 | weeklyCompliance[], anomalousEntries[], penaltyHistory[], pdcTrend[], heatmapDays[] |

---

## NOTES FOR ERD DEVELOPMENT

1. **Primary Key:** `Patient.id` (UUID)
2. **External Key:** `Patient.patientId` (registry reference, should be unique within clinic)
3. **Key Relationships to Model:**
   - `Patient` ← → `DoseRecord` (one-to-many: one patient has many dose records)
   - `Patient` ← → `AnomalyRecord` (one-to-many: anomalies derived from dose logs)
   - `Patient` ← → `PenaltyEvent` (one-to-many: escalation events)
   - `Patient` ← → `Clinic` (many-to-one: patient assigned to one clinic)
   - `Patient` ← → `Provider` (many-to-one: patient assigned to one provider)
   - `Patient` ← → `BHW` (many-to-one: patient assigned to one BHW)

4. **Calculated Fields to Handle in Queries/Views:**
   - `currentDay`, `currentStreak`, `monthPDC`, `riskTier` should be computed views or cached with refresh triggers
   - `heatmapDays[]` is a UI-specific transformation; store the underlying `DoseRecord` and `AnomalyRecord` instead
   - `weeklyCompliance[]` can be a view aggregating recent `DoseRecord` entries

5. **Temporal Considerations:**
   - Store `regimentStart` and `lastActive` as precise timestamps (ISO 8601)
   - `lastSyncLabel` is a UI-computed relative time; don't store as raw text
   - `penaltyHistory[]` should have timestamps for each event for audit compliance

6. **Optional vs. Required:**
   - `symptomReported` is optional (nullable)
   - All other attributes are required for patient record creation
