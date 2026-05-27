/**
 * Axios client and typed API call functions for the AMPING HC Professional portal.
 *
 * All paths are relative (no baseURL) — the Vite dev server proxies /api/* to
 * http://localhost:8000.  In production Django serves the built frontend directly.
 *
 * Auth: Django session cookies (withCredentials: true).
 * 401 responses trigger an automatic redirect to /login.
 */

import axios from 'axios';
import type { WebLoginHealthProviderPayload } from '../api_types/Web_LoginHealthProviderPayload';
import type { WebLoginHealthProviderResponse } from '../api_types/Web_LoginHealthProviderResponse';
import type { WebLogoutResponse } from '../api_types/Web_LogoutResponse';
import type { WebPatientListResponse } from '../api_types/Web_PatientListResponse';
import type { WebPatientDetailResponse } from '../api_types/Web_PatientDetailResponse';
import type { WebAdherenceMonthResponse } from '../api_types/Web_AdherenceMonthResponse';
import type { WebAdherenceMonthRequest } from '../api_types/Web_AdherenceMonthRequest';
import type { WebGamificationResponse } from '../api_types/Web_GamificationResponse';
import type { WebAnomalousEntriesResponse } from '../api_types/Web_AnomalousEntriesResponse';
import type { WebReconcileAnomalyPayload } from '../api_types/Web_ReconcileAnomalyPayload';
import type { WebReconcileAnomalyResponse } from '../api_types/Web_ReconcileAnomalyResponse';

// ─── Axios instance ──────────────────────────────────────────────────────────

const client = axios.create({
  withCredentials: true, // send/receive Django session cookie
  headers: { 'Content-Type': 'application/json' },
});

// Redirect to /login on any 401
client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (axios.isAxiosError(err) && err.response?.status === 401) {
      window.location.replace('/login');
    }
    return Promise.reject(err);
  },
);

// ─── Auth ────────────────────────────────────────────────────────────────────

/** POST /api/v1/web/login/ */
export async function login(
  email: string,
  password: string,
): Promise<WebLoginHealthProviderResponse> {
  const payload: WebLoginHealthProviderPayload = { email, password };
  const res = await client.post<WebLoginHealthProviderResponse>(
    '/api/v1/web/login/',
    payload,
  );
  return res.data;
}

/** POST /api/v1/web/logout/ */
export async function logout(): Promise<WebLogoutResponse> {
  const res = await client.post<WebLogoutResponse>('/api/v1/web/logout/');
  return res.data;
}

// ─── Patients ────────────────────────────────────────────────────────────────

/** GET /api/v1/web/patients/ */
export async function getPatients(): Promise<WebPatientListResponse> {
  const res = await client.get<WebPatientListResponse>('/api/v1/web/patients/');
  return res.data;
}

/** GET /api/v1/web/patient/{id}/ */
export async function getPatient(id: number): Promise<WebPatientDetailResponse> {
  const res = await client.get<WebPatientDetailResponse>(`/api/v1/web/patient/${id}/`);
  return res.data;
}

// ─── Adherence ───────────────────────────────────────────────────────────────

/** GET /api/v1/web/patient/{id}/adherence_month/?month=&year= */
export async function getAdherenceMonth(
  patientId: number,
  month: number,
  year: number,
): Promise<WebAdherenceMonthResponse> {
  const params: WebAdherenceMonthRequest = { month, year };
  const res = await client.get<WebAdherenceMonthResponse>(
    `/api/v1/web/patient/${patientId}/adherence_month/`,
    { params },
  );
  return res.data;
}

// ─── Gamification ────────────────────────────────────────────────────────────

/** GET /api/v1/web/patient/{id}/gamification/ */
export async function getGamification(patientId: number): Promise<WebGamificationResponse> {
  const res = await client.get<WebGamificationResponse>(
    `/api/v1/web/patient/${patientId}/gamification/`,
  );
  return res.data;
}

// ─── Anomalous Entries ───────────────────────────────────────────────────────

/** GET /api/v1/web/patient/{id}/anomalous_entries/ */
export async function getAnomalousEntries(
  patientId: number,
): Promise<WebAnomalousEntriesResponse> {
  const res = await client.get<WebAnomalousEntriesResponse>(
    `/api/v1/web/patient/${patientId}/anomalous_entries/`,
  );
  return res.data;
}

/** POST /api/v1/web/patient/{id}/reconcile_anomalies/ */
export async function reconcileAnomalies(
  patientId: number,
  payload: WebReconcileAnomalyPayload,
): Promise<WebReconcileAnomalyResponse> {
  const res = await client.post<WebReconcileAnomalyResponse>(
    `/api/v1/web/patient/${patientId}/reconcile_anomalies/`,
    payload,
  );
  return res.data;
}
