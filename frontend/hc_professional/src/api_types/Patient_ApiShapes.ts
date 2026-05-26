// ─── API request / response envelope types ──────────────────────────────────
// These mirror the Django Ninja schema shapes that will be returned by the
// backend. Pages never import these directly — the service layer uses them.

import type { Patient } from './Patient';
import type { BaseResponse } from './BaseResponse';

export interface PatientListResponse extends BaseResponse {
  patients: Patient[];
}

export interface PatientDetailResponse extends BaseResponse {
  patient: Patient;
}

export interface ReconcilePayload {
  patientId: string;
  entryIds: string[];
  verificationMethod: string;
  reason: string;
}

export interface ReconcileResponse extends BaseResponse {
  updatedStreak: number;
  reconciledCount: number;
}
