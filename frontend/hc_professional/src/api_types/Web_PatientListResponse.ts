/**
 * Inferred schema for GET /api/v1/web/patients/
 * Fields derived from PatientRoster.tsx display requirements.
 */

export interface WebPatientListItem {
  id: number;
  firstname: string;
  lastname: string;
  age: number;
  age_profile: 'Child' | 'Adult' | 'Senior';
  risk_tier: 'tier1' | 'tier2' | 'tier3' | 'safe';
  trigger_reason: string;
  current_streak: number;
  heart_quota: number;
  last_sync_label: string;
  last_active: string;
  month3_protected: boolean;
  current_day: number;
  total_days: number;
  month_pdc: number;
  pdc_target: number;
  symptom_reported: string[];
  weekly_compliance: WebWeeklyComplianceEntry[];
}

export interface WebWeeklyComplianceEntry {
  day: string;
  status: 'done' | 'missed' | 'pending';
}

export interface WebPatientListResponse {
  patients: WebPatientListItem[];
}
