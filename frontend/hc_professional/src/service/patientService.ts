// ─── Patient service stubs ────────────────────────────────────────────────────
// All functions currently resolve with mock data so the app works before the
// backend is ready.  When a real endpoint is available, uncomment the apiClient
// line and remove the Promise.resolve line — no context or page code changes needed.

import type { Patient } from '../api_types/Patient';
import { MOCK_PATIENTS } from '../data/mockData';
// import apiClient from './axiosInstance';
// import type { PatientListResponse, PatientDetailResponse, ReconcileResponse } from '../api_types/Patient_ApiShapes';

export async function fetchPatients(): Promise<Patient[]> {
  // TODO: return (await apiClient.get<PatientListResponse>('/patients/')).data.patients;
  return Promise.resolve(MOCK_PATIENTS);
}

export async function fetchPatientById(id: string): Promise<Patient | null> {
  // TODO: return (await apiClient.get<PatientDetailResponse>(`/patients/${id}/`)).data.patient;
  return Promise.resolve(MOCK_PATIENTS.find((p) => p.id === id) ?? null);
}

export async function reconcileEntries(
  patientId: string,
  entryIds: string[],
  _verificationMethod: string,
  _reason: string,
): Promise<{ updatedStreak: number; reconciledCount: number }> {
  // TODO: return (await apiClient.post<ReconcileResponse>('/patients/reconcile/', {
  //   patientId, entryIds, verificationMethod: _verificationMethod, reason: _reason,
  // })).data;
  const patient = MOCK_PATIENTS.find((p) => p.id === patientId);
  return Promise.resolve({
    updatedStreak: (patient?.currentStreak ?? 0) + entryIds.length,
    reconciledCount: entryIds.length,
  });
}
