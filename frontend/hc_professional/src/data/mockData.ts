// ─── Types ─────────────────────────────────────────────────────────────────

export type RiskTier = 'tier1' | 'tier2' | 'tier3' | 'safe';
export type AgeProfile = 'Child' | 'Adult' | 'Senior';
export type DayStatus =
  | 'app-recorded'
  | 'provider-reconciled'
  | 'technical-miss'
  | 'unverified-absence'
  | 'future';

export interface WeekDay {
  day: 'Mon' | 'Tue' | 'Wed' | 'Thu' | 'Fri' | 'Sat' | 'Sun';
  status: 'done' | 'missed' | 'pending';
}

export interface AnomalousEntry {
  id: string;
  date: string;
  statusBadge: 'unverified-miss' | 'tech-failure' | 'app-miss';
  detectedCause: string;
}

export interface PenaltyEvent {
  date: string;
  tier: 1 | 2;
  label: string;
}

export interface PDCPoint {
  week: string;
  pdc: number;
}

export interface HeatmapDay {
  date: number | null; // null = padding
  status: DayStatus;
  note?: string;
  symptoms?: string[];
  videoLink?: string | null;
}

export interface Patient {
  id: string;
  name: string;
  age: number;
  ageProfile: AgeProfile;
  clinic: string;
  provider: string;
  bhw: string;
  patientId: string;
  regimentStart: string;
  currentDay: number;
  totalDays: number;
  currentStreak: number;
  bestStreak: number;
  heartQuota: number; // out of 3
  riskTier: RiskTier;
  lastActive: string;
  triggerReason: string;
  lastSyncLabel: string;
  symptomReported?: string[];
  weeklyCompliance: WeekDay[];
  anomalousEntries: AnomalousEntry[];
  penaltyHistory: PenaltyEvent[];
  pdcTrend: PDCPoint[];
  heatmapMonth: string; // e.g. "MAY 2026"
  heatmapStartDay: number; // 0=Mon, 1=Tue...
  heatmapDays: HeatmapDay[];
  monthPDC: number;
  pdcTarget: number;
  month3Protected: boolean;
}

// ─── Mock Patients ──────────────────────────────────────────────────────────

export const MOCK_PATIENTS: Patient[] = [
  {
    id: 'p1',
    name: 'Grandma Carmen',
    age: 72,
    ageProfile: 'Senior',
    clinic: 'Lapu-Lapu City Health Office',
    provider: 'Dr. Alicia Tan',
    bhw: 'Maria Reyes',
    patientId: 'PH-TB-2024-0031',
    regimentStart: 'November 8, 2025',
    currentDay: 92,
    totalDays: 180,
    currentStreak: 65,
    bestStreak: 65,
    heartQuota: 3,
    riskTier: 'tier2',
    lastActive: '4 days ago',
    triggerReason: '3 unverified misses in 7 days; quota exhausted',
    lastSyncLabel: '1 day ago',
    symptomReported: ['Dizziness reported'],
    month3Protected: true,
    weeklyCompliance: [
      { day: 'Mon', status: 'done' },
      { day: 'Tue', status: 'done' },
      { day: 'Wed', status: 'done' },
      { day: 'Thu', status: 'done' },
      { day: 'Fri', status: 'done' },
      { day: 'Sat', status: 'pending' },
      { day: 'Sun', status: 'pending' },
    ],
    anomalousEntries: [
      { id: 'a1', date: 'May 18, 2026', statusBadge: 'unverified-miss', detectedCause: 'Device offline flag detected' },
      { id: 'a2', date: 'May 19, 2026', statusBadge: 'unverified-miss', detectedCause: 'No entry, no technical flag' },
      { id: 'a3', date: 'May 20, 2026', statusBadge: 'tech-failure', detectedCause: 'App crash log detected' },
    ],
    penaltyHistory: [
      { date: 'May 6', tier: 1, label: 'Tier 1 Applied' },
      { date: 'May 14', tier: 2, label: 'Tier 2 (Downgraded)' },
    ],
    pdcTrend: [
      { week: 'Week 1', pdc: 92 },
      { week: 'Week 2', pdc: 88 },
      { week: 'Week 3', pdc: 71 },
      { week: 'Week 4', pdc: 57 },
    ],
    heatmapMonth: 'MAY 2026',
    heatmapStartDay: 3, // Thursday
    heatmapDays: buildHeatmap(31, 3, [
      { date: 6, status: 'unverified-absence', note: 'No dose record, no reconciliation. Penalty applied.' },
      { date: 14, status: 'unverified-absence', note: 'No dose record, no reconciliation. Penalty applied.' },
      { date: 19, status: 'technical-miss', note: 'App crash log detected.' },
      { date: 24, status: 'future' },
      { date: 25, status: 'future' },
      { date: 26, status: 'future' },
      { date: 27, status: 'future' },
      { date: 28, status: 'future' },
      { date: 29, status: 'future' },
      { date: 30, status: 'future' },
      { date: 31, status: 'future' },
    ]),
    monthPDC: 87,
    pdcTarget: 85,
  },
  {
    id: 'p2',
    name: 'Leo Santos',
    age: 12,
    ageProfile: 'Child',
    clinic: 'Lapu-Lapu City Health Office',
    provider: 'Dr. Alicia Tan',
    bhw: 'Maria Reyes',
    patientId: 'PH-TB-2024-0047',
    regimentStart: 'March 19, 2026',
    currentDay: 65,
    totalDays: 180,
    currentStreak: 4,
    bestStreak: 61,
    heartQuota: 3,
    riskTier: 'tier1',
    lastActive: '2 days ago',
    triggerReason: '2 unverified misses in past 7 days',
    lastSyncLabel: '2 hours ago',
    symptomReported: ['Mild stomach pain reported'],
    month3Protected: true,
    weeklyCompliance: [
      { day: 'Mon', status: 'done' },
      { day: 'Tue', status: 'done' },
      { day: 'Wed', status: 'done' },
      { day: 'Thu', status: 'missed' },
      { day: 'Fri', status: 'done' },
      { day: 'Sat', status: 'pending' },
      { day: 'Sun', status: 'pending' },
    ],
    anomalousEntries: [
      { id: 'b1', date: 'May 18, 2026', statusBadge: 'unverified-miss', detectedCause: 'Device offline flag detected' },
      { id: 'b2', date: 'May 20, 2026', statusBadge: 'tech-failure', detectedCause: 'App crash log detected' },
    ],
    penaltyHistory: [
      { date: 'May 6', tier: 1, label: 'Tier 1 Applied' },
      { date: 'May 14', tier: 2, label: 'Tier 2 (Downgraded)' },
    ],
    pdcTrend: [
      { week: 'Week 1', pdc: 100 },
      { week: 'Week 2', pdc: 95 },
      { week: 'Week 3', pdc: 85 },
      { week: 'Week 4', pdc: 72 },
    ],
    heatmapMonth: 'MAY 2026',
    heatmapStartDay: 3,
    heatmapDays: buildHeatmap(31, 3, [
      { date: 6, status: 'unverified-absence', note: 'No dose record. Penalty applied.' },
      { date: 14, status: 'provider-reconciled', note: 'Home visit confirmed. Dose counted.' },
      { date: 19, status: 'technical-miss', note: 'App crash log detected.' },
      { date: 24, status: 'future' },
      { date: 25, status: 'future' },
      { date: 26, status: 'future' },
      { date: 27, status: 'future' },
      { date: 28, status: 'future' },
      { date: 29, status: 'future' },
      { date: 30, status: 'future' },
      { date: 31, status: 'future' },
    ]),
    monthPDC: 87,
    pdcTarget: 85,
  },
  {
    id: 'p3',
    name: 'Maria Cruz',
    age: 28,
    ageProfile: 'Adult',
    clinic: 'Mandaue Health Center',
    provider: 'Dr. Benito Cruz',
    bhw: 'Liza Santos',
    patientId: 'PH-TB-2024-0022',
    regimentStart: 'January 5, 2026',
    currentDay: 138,
    totalDays: 180,
    currentStreak: 12,
    bestStreak: 42,
    heartQuota: 2,
    riskTier: 'safe',
    lastActive: 'Today',
    triggerReason: '0 misses in past 30 days, streak active',
    lastSyncLabel: '10 mins ago',
    month3Protected: false,
    weeklyCompliance: [
      { day: 'Mon', status: 'done' },
      { day: 'Tue', status: 'done' },
      { day: 'Wed', status: 'done' },
      { day: 'Thu', status: 'done' },
      { day: 'Fri', status: 'done' },
      { day: 'Sat', status: 'pending' },
      { day: 'Sun', status: 'pending' },
    ],
    anomalousEntries: [],
    penaltyHistory: [],
    pdcTrend: [
      { week: 'Week 1', pdc: 100 },
      { week: 'Week 2', pdc: 100 },
      { week: 'Week 3', pdc: 93 },
      { week: 'Week 4', pdc: 96 },
    ],
    heatmapMonth: 'MAY 2026',
    heatmapStartDay: 3,
    heatmapDays: buildHeatmap(31, 3, [
      { date: 24, status: 'future' },
      { date: 25, status: 'future' },
      { date: 26, status: 'future' },
      { date: 27, status: 'future' },
      { date: 28, status: 'future' },
      { date: 29, status: 'future' },
      { date: 30, status: 'future' },
      { date: 31, status: 'future' },
    ]),
    monthPDC: 96,
    pdcTarget: 85,
  },
  {
    id: 'p4',
    name: 'Andres Bautista',
    age: 45,
    ageProfile: 'Adult',
    clinic: 'Cebu City Health Office',
    provider: 'Dr. Ramon Flores',
    bhw: 'Ana Reyes',
    patientId: 'PH-TB-2024-0019',
    regimentStart: 'December 2, 2025',
    currentDay: 143,
    totalDays: 180,
    currentStreak: 2,
    bestStreak: 78,
    heartQuota: 0,
    riskTier: 'tier3',
    lastActive: '9 days ago',
    triggerReason: '4+ unverified misses; BHW visit attempted, no contact',
    lastSyncLabel: '9 days ago',
    month3Protected: false,
    weeklyCompliance: [
      { day: 'Mon', status: 'missed' },
      { day: 'Tue', status: 'missed' },
      { day: 'Wed', status: 'missed' },
      { day: 'Thu', status: 'missed' },
      { day: 'Fri', status: 'missed' },
      { day: 'Sat', status: 'pending' },
      { day: 'Sun', status: 'pending' },
    ],
    anomalousEntries: [
      { id: 'c1', date: 'May 14, 2026', statusBadge: 'unverified-miss', detectedCause: 'No entry, no technical flag' },
      { id: 'c2', date: 'May 15, 2026', statusBadge: 'unverified-miss', detectedCause: 'No entry, no technical flag' },
      { id: 'c3', date: 'May 16, 2026', statusBadge: 'unverified-miss', detectedCause: 'No entry, no technical flag' },
      { id: 'c4', date: 'May 17, 2026', statusBadge: 'unverified-miss', detectedCause: 'No entry, no technical flag' },
    ],
    penaltyHistory: [
      { date: 'May 10', tier: 1, label: 'Tier 1 Applied' },
      { date: 'May 14', tier: 2, label: 'Tier 2 (Downgraded)' },
    ],
    pdcTrend: [
      { week: 'Week 1', pdc: 85 },
      { week: 'Week 2', pdc: 71 },
      { week: 'Week 3', pdc: 42 },
      { week: 'Week 4', pdc: 28 },
    ],
    heatmapMonth: 'MAY 2026',
    heatmapStartDay: 3,
    heatmapDays: buildHeatmap(31, 3, [
      { date: 13, status: 'unverified-absence', note: 'No contact.' },
      { date: 14, status: 'unverified-absence', note: 'No contact.' },
      { date: 15, status: 'unverified-absence', note: 'No contact.' },
      { date: 16, status: 'unverified-absence', note: 'No contact.' },
      { date: 17, status: 'unverified-absence', note: 'No contact.' },
      { date: 18, status: 'unverified-absence', note: 'No contact.' },
      { date: 19, status: 'unverified-absence', note: 'No contact.' },
      { date: 24, status: 'future' },
      { date: 25, status: 'future' },
      { date: 26, status: 'future' },
      { date: 27, status: 'future' },
      { date: 28, status: 'future' },
      { date: 29, status: 'future' },
      { date: 30, status: 'future' },
      { date: 31, status: 'future' },
    ]),
    monthPDC: 48,
    pdcTarget: 85,
  },
  {
    id: 'p5',
    name: 'Sofia Uy',
    age: 8,
    ageProfile: 'Child',
    clinic: 'Talisay Health Center',
    provider: 'Dr. Mei Santos',
    bhw: 'Carlo Dela Vega',
    patientId: 'PH-TB-2024-0055',
    regimentStart: 'April 25, 2026',
    currentDay: 28,
    totalDays: 180,
    currentStreak: 3,
    bestStreak: 24,
    heartQuota: 3,
    riskTier: 'tier1',
    lastActive: 'Yesterday',
    triggerReason: '1 unverified miss in past 7 days',
    lastSyncLabel: 'Yesterday',
    month3Protected: false,
    weeklyCompliance: [
      { day: 'Mon', status: 'done' },
      { day: 'Tue', status: 'done' },
      { day: 'Wed', status: 'missed' },
      { day: 'Thu', status: 'done' },
      { day: 'Fri', status: 'done' },
      { day: 'Sat', status: 'pending' },
      { day: 'Sun', status: 'pending' },
    ],
    anomalousEntries: [
      { id: 'd1', date: 'May 21, 2026', statusBadge: 'unverified-miss', detectedCause: 'No entry, no technical flag' },
    ],
    penaltyHistory: [],
    pdcTrend: [
      { week: 'Week 1', pdc: 100 },
      { week: 'Week 2', pdc: 93 },
      { week: 'Week 3', pdc: 85 },
      { week: 'Week 4', pdc: 82 },
    ],
    heatmapMonth: 'MAY 2026',
    heatmapStartDay: 3,
    heatmapDays: buildHeatmap(31, 3, [
      { date: 21, status: 'unverified-absence', note: 'No dose record.' },
      { date: 24, status: 'future' },
      { date: 25, status: 'future' },
      { date: 26, status: 'future' },
      { date: 27, status: 'future' },
      { date: 28, status: 'future' },
      { date: 29, status: 'future' },
      { date: 30, status: 'future' },
      { date: 31, status: 'future' },
    ]),
    monthPDC: 82,
    pdcTarget: 85,
  },
];

// ─── Helper ────────────────────────────────────────────────────────────────

function buildHeatmap(
  totalDays: number,
  startDay: number, // 0=Mon
  overrides: { date: number; status: DayStatus; note?: string }[]
): HeatmapDay[] {
  const overrideMap = new Map(overrides.map((o) => [o.date, o]));
  const result: HeatmapDay[] = [];

  // leading padding
  for (let i = 0; i < startDay; i++) {
    result.push({ date: null, status: 'future' });
  }

  for (let d = 1; d <= totalDays; d++) {
    const override = overrideMap.get(d);
    if (override) {
      result.push({ date: d, status: override.status, note: override.note });
    } else {
      result.push({ date: d, status: 'app-recorded' });
    }
  }
  return result;
}
