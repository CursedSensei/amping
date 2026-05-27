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
import type { WebAdherenceMonthRequest } from '../api_types/Web_AdherenceMonthRequest';
import type { WebAdherenceMonthResponse } from '../api_types/Web_AdherenceMonthResponse';
import type { WebAnomalousEntriesResponse } from '../api_types/Web_AnomalousEntriesResponse';
import type { WebGamificationResponse } from '../api_types/Web_GamificationResponse';
import type { WebPatientDetailResponse } from '../api_types/Web_PatientDetailResponse';
import type { WebPatientListResponse } from '../api_types/Web_PatientListResponse';
import type { WebReconcileAnomalyPayload } from '../api_types/Web_ReconcileAnomalyPayload';
import type { WebReconcileAnomalyResponse } from '../api_types/Web_ReconcileAnomalyResponse';

// ─── Axios instance ──────────────────────────────────────────────────────────

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api/v1/web';

export const client = axios.create({
  withCredentials: true, // send/receive Django session cookie
  headers: { 'Content-Type': 'application/json' },
  baseURL: baseURL,
});

client.defaults.xsrfCookieName = 'csrftoken';
client.defaults.xsrfHeaderName = 'X-CSRFToken';
client.defaults.withXSRFToken = true


// TODO: CSRF handing for cross-site with Render deployment.
//
// axios.defaults.xsrfCookieName = 'csrftoken';
// axios.defaults.xsrfHeaderName = 'X-CSRFToken';
// axios.defaults.withCredentials = true;

// let csrfToken: string | null = null;
// let csrfPromise: Promise<string> | null = null;

// async function fetchCsrfToken() {
//   if (csrfToken) return csrfToken;
//   if (csrfPromise) return csrfPromise;

//   csrfPromise = axios.get<{ csrfToken: string }>(baseURL + '/csrf/')
//     .then((res) => {
//       csrfToken = res.data.csrfToken;
//       return csrfToken;
//     })
//     .finally(() => {
//       csrfPromise = null;
//     });

//   return csrfPromise;
// }

// client.interceptors.request.use(config => {
//   config.headers = config.headers || {};
//   config.headers["X-CSRFToken"] = fetchCsrfToken();

//   return config;
// });

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
