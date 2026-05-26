// ─── Domain types (canonical source — previously in mockData.ts) ────────────

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
