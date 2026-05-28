import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import type { WebAdherenceMonthResponse } from '../api_types/Web_AdherenceMonthResponse';
import type { WebAnomalousEntriesResponse } from '../api_types/Web_AnomalousEntriesResponse';
import type { WebGamificationResponse } from '../api_types/Web_GamificationResponse';
import type { WebPatientEntry } from '../api_types/Web_GetAllPatientsResponse';
import type { WebPatientDetailResponse } from '../api_types/Web_PatientDetailResponse';
import type { WebReconcileAnomalyPayload } from '../api_types/Web_ReconcileAnomalyPayload';
import type { WebReconcileAnomalyResponse } from '../api_types/Web_ReconcileAnomalyResponse';
import type { AgeProfile, Patient, RiskTier } from '../data/mockData';
import { toPenaltyEvent } from '../services/adapters';
import {
    getAllPatients,
    getAnomalousEntries,
    getPatient,
    getPatientAdherenceMonth,
    getPatientStats,
    reconcileAnomalies,
} from '../services/patient';
import type { UserProfile } from '../types';
import { useAuth } from './AuthContext';

type PatientMonthKey = `${number}-${number}`;

export type PatientBundleState = {
  summary: WebPatientEntry | null;
  detail: WebPatientDetailResponse | null;
  gamification: WebGamificationResponse | null;
  anomalies: WebAnomalousEntriesResponse | null;
  detailLoading: boolean;
  gamificationLoading: boolean;
  anomaliesLoading: boolean;
  adherenceByMonth: Record<PatientMonthKey, WebAdherenceMonthResponse>;
  adherenceLoading: Record<PatientMonthKey, boolean>;
  error: string | null;
};

type PatientContextType = {
  patients: Patient[];
  patientBundles: Record<number, PatientBundleState>;
  patientsLoading: boolean;
  patientsError: string | null;
  refreshPatients: () => Promise<void>;
  ensurePatientBundle: (patientId: number) => Promise<PatientBundleState | null>;
  loadPatientAdherence: (patientId: number, month: number, year: number) => Promise<WebAdherenceMonthResponse | null>;
  reconcilePatientAnomalies: (
    patientId: number,
    payload: WebReconcileAnomalyPayload,
  ) => Promise<WebReconcileAnomalyResponse>;
};

const PatientContext = createContext<PatientContextType | undefined>(undefined);

const DEFAULT_WEEKLY_COMPLIANCE: Patient['weeklyCompliance'] = [
  { day: 'Mon', status: 'done' },
  { day: 'Tue', status: 'done' },
  { day: 'Wed', status: 'done' },
  { day: 'Thu', status: 'done' },
  { day: 'Fri', status: 'done' },
  { day: 'Sat', status: 'pending' },
  { day: 'Sun', status: 'pending' },
];

function monthKey(month: number, year: number): PatientMonthKey {
  return `${year}-${String(month).padStart(2, '0')}` as PatientMonthKey;
}

function getAgeFromBirthYear(birthyear: number): number {
  return Math.max(0, new Date().getFullYear() - birthyear);
}

function getAgeProfile(age: number): AgeProfile {
  if (age < 18) return 'Child';
  if (age >= 60) return 'Senior';
  return 'Adult';
}

function toComplianceStatus(status: string): 'done' | 'missed' | 'pending' {
  if (status === 'app_recorded' || status === 'provider_reconciled') return 'done';
  if (status === 'technical_miss' || status === 'unverified_absence') return 'missed';
  return 'pending';
}

function buildWeeklyCompliance(days: WebAdherenceMonthResponse['adherence_days']): Patient['weeklyCompliance'] {
  if (days.length === 0) return DEFAULT_WEEKLY_COMPLIANCE;

  const recentDays = [...days]
    .sort((left, right) => new Date(left.date).getTime() - new Date(right.date).getTime())
    .slice(-7);

  if (recentDays.length < 7) return DEFAULT_WEEKLY_COMPLIANCE;

  const labels: Patient['weeklyCompliance'] = [
    { day: 'Mon', status: 'pending' },
    { day: 'Tue', status: 'pending' },
    { day: 'Wed', status: 'pending' },
    { day: 'Thu', status: 'pending' },
    { day: 'Fri', status: 'pending' },
    { day: 'Sat', status: 'pending' },
    { day: 'Sun', status: 'pending' },
  ];

  return labels.map((entry, index) => ({
    day: entry.day,
    status: toComplianceStatus(recentDays[index].status),
  }));
}

function getRiskTier(currentStreak: number, heartQuota: number): RiskTier {
  if (heartQuota === 0 || currentStreak <= 3) return 'tier3';
  if (currentStreak <= 7) return 'tier2';
  if (currentStreak <= 14) return 'tier1';
  return 'safe';
}

function buildPatientFromApi(
  user: UserProfile,
  summary: WebPatientEntry,
  detail: WebPatientDetailResponse | null,
  stats: WebGamificationResponse | null,
  adherence: WebAdherenceMonthResponse | null,
): Patient {
  const age = getAgeFromBirthYear(summary.birthyear);
  const heartQuota = stats?.heart_quota ?? 0;
  const adherenceDays = adherence?.adherence_days ?? [];
  const currentStreak = stats?.current_streak ?? 0;

  return {
    id: String(summary.id),
    name: `${summary.firstname} ${summary.lastname}`,
    age,
    ageProfile: getAgeProfile(age),
    clinic: user.clinic ?? 'Unknown Clinic',
    provider: user.firstname + ' ' + user.lastname,
    bhw: user.firstname + ' ' + user.lastname,
    patientId: `${summary.id}`,
    regimentStart: detail?.regimen_start
      ? new Date(detail.regimen_start).toLocaleDateString('en-US', {
          month: 'long',
          day: 'numeric',
          year: 'numeric',
        })
      : 'TODO: backend regimen start unavailable',
    currentDay: detail?.current_day ?? 0,
    totalDays: detail?.total_days ?? stats?.total_regimen_days ?? 0,
    currentStreak,
    bestStreak: stats?.best_streak ?? currentStreak,
    heartQuota,
    riskTier: getRiskTier(currentStreak, heartQuota),

    // TODO: these are placeholders until we have the actual data from the backend
    lastActive: '5 minutes ago',
    triggerReason: '',
    lastSyncLabel: '5 minutes ago',
    symptomReported: adherenceDays.flatMap((day) => day.symptoms ?? []).slice(0, 3),
    weeklyCompliance: buildWeeklyCompliance(adherenceDays),
    anomalousEntries: [],
    penaltyHistory: (stats?.penalty_history ?? []).map(toPenaltyEvent),
    pdcTrend: [],
    heatmapMonth: adherence ? `MONTH ${adherence.month}/${adherence.year}` : 'TODO: backend month unavailable',
    heatmapStartDay: 0,
    heatmapDays: [],
    monthPDC: adherence?.month_pdc ?? 0,
    pdcTarget: adherence?.pdc_target ?? detail?.pdc_target ?? 85,
    month3Protected: detail?.month3_protected ?? false,
  };
}

function createEmptyBundle(summary: WebPatientEntry | null = null): PatientBundleState {
  return {
    summary,
    detail: null,
    gamification: null,
    anomalies: null,
    detailLoading: false,
    gamificationLoading: false,
    anomaliesLoading: false,
    adherenceByMonth: {},
    adherenceLoading: {},
    error: null,
  };
}

function mergeBundleState(base: PatientBundleState | undefined, patch: Partial<PatientBundleState>): PatientBundleState {
  const current = base ?? createEmptyBundle();
  return {
    ...current,
    ...patch,
    summary: patch.summary ?? current.summary,
    adherenceByMonth: {
      ...current.adherenceByMonth,
      ...(patch.adherenceByMonth ?? {}),
    },
    adherenceLoading: {
      ...current.adherenceLoading,
      ...(patch.adherenceLoading ?? {}),
    },
  };
}

export function PatientProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const [patients, setPatients] = useState<Patient[]>([]);
  const [patientBundles, setPatientBundles] = useState<Record<number, PatientBundleState>>({});
  const [patientsLoading, setPatientsLoading] = useState(false);
  const [patientsError, setPatientsError] = useState<string | null>(null);
  const patientBundlesRef = useRef<Record<number, PatientBundleState>>({});
  const rosterRequestRef = useRef<Promise<void> | null>(null);
  const patientRequestRef = useRef<Map<number, Promise<PatientBundleState | null>>>(new Map());
  const adherenceRequestRef = useRef<Map<string, Promise<WebAdherenceMonthResponse | null>>>(new Map());
  const { user } = useAuth();

  useEffect(() => {
    patientBundlesRef.current = patientBundles;
  }, [patientBundles]);

  const refreshPatients = useCallback(async () => {
    if (rosterRequestRef.current) return rosterRequestRef.current;

    const request = (async () => {
      setPatientsLoading(true);
      setPatientsError(null);

      try {
        const response = await getAllPatients();
        const currentMonth = new Date().getMonth() + 1;
        const currentYear = new Date().getFullYear();
        const key = monthKey(currentMonth, currentYear);

        const results = await Promise.allSettled(
          response.patients.map(async (summary) => {
            const [detailResult, statsResult, adherenceResult] = await Promise.allSettled([
              getPatient({ patient_id: summary.id }),
              getPatientStats({ patient_id: summary.id }),
              getPatientAdherenceMonth({ patient_id: summary.id, payload: { month: currentMonth, year: currentYear } }),
            ]);

            const detail = detailResult.status === 'fulfilled' ? detailResult.value : null;
            const stats = statsResult.status === 'fulfilled' ? statsResult.value : null;
            const adherence = adherenceResult.status === 'fulfilled' ? adherenceResult.value : null;

            return {
              summary,
              detail,
              stats,
              adherence,
              normalized: buildPatientFromApi(user!, summary, detail, stats, adherence),
            };
          }),
        );

        const nextPatients: Patient[] = [];
        const nextBundles: Record<number, PatientBundleState> = {};

        for (const result of results) {
          if (result.status !== 'fulfilled') continue;

          const { summary, detail, stats, adherence, normalized } = result.value;
          nextPatients.push(normalized);
          nextBundles[summary.id] = {
            summary,
            detail,
            gamification: stats,
            anomalies: null,
            detailLoading: false,
            gamificationLoading: false,
            anomaliesLoading: false,
            adherenceByMonth: adherence ? { [key]: adherence } : {},
            adherenceLoading: {},
            error: null,
          };
        }

        setPatients(nextPatients);
        setPatientBundles((prev) => {
          const merged: Record<number, PatientBundleState> = { ...prev };
          for (const [patientIdString, bundle] of Object.entries(nextBundles)) {
            const patientId = Number(patientIdString);
            merged[patientId] = mergeBundleState(prev[patientId], bundle);
          }
          return merged;
        });
      } catch {
        setPatientsError('Failed to load patients. Please refresh.');
      } finally {
        setPatientsLoading(false);
        rosterRequestRef.current = null;
      }
    })();

    rosterRequestRef.current = request;
    return request;
  }, []);

  const ensurePatientBundle = useCallback(async (patientId: number) => {
    const existing = patientBundlesRef.current[patientId] ?? createEmptyBundle();
    if (existing.detail && existing.gamification && existing.anomalies) return existing;

    if (patientRequestRef.current.has(patientId)) {
      return patientRequestRef.current.get(patientId) ?? null;
    }

    const request = (async () => {
      setPatientBundles((prev) => {
        const current = prev[patientId] ?? createEmptyBundle();
        return {
          ...prev,
          [patientId]: {
            ...current,
            detailLoading: !current.detail,
            gamificationLoading: !current.gamification,
            anomaliesLoading: !current.anomalies,
            error: null,
          },
        };
      });

      try {
        const [detailResult, gamificationResult, anomaliesResult] = await Promise.allSettled([
          existing.detail ? Promise.resolve(existing.detail) : getPatient({ patient_id: patientId }),
          existing.gamification ? Promise.resolve(existing.gamification) : getPatientStats({ patient_id: patientId }),
          existing.anomalies ? Promise.resolve(existing.anomalies) : getAnomalousEntries({ patient_id: patientId }),
        ]);

        const detail = detailResult.status === 'fulfilled' ? detailResult.value : existing.detail;
        const gamification = gamificationResult.status === 'fulfilled' ? gamificationResult.value : existing.gamification;
        const anomalies = anomaliesResult.status === 'fulfilled' ? anomaliesResult.value : existing.anomalies;

        const updated = mergeBundleState(existing, {
          detail,
          gamification,
          anomalies,
          detailLoading: false,
          gamificationLoading: false,
          anomaliesLoading: false,
          error: null,
        });

        setPatientBundles((prev) => ({
          ...prev,
          [patientId]: mergeBundleState(prev[patientId], updated),
        }));

        return updated;
      } catch {
        setPatientBundles((prev) => ({
          ...prev,
          [patientId]: {
            ...(prev[patientId] ?? createEmptyBundle()),
            detailLoading: false,
            gamificationLoading: false,
            anomaliesLoading: false,
            error: 'Failed to load patient data.',
          },
        }));
        return null;
      } finally {
        patientRequestRef.current.delete(patientId);
      }
    })();

    patientRequestRef.current.set(patientId, request);
    return request;
  }, []);

  const loadPatientAdherence = useCallback(async (patientId: number, month: number, year: number) => {
    const key = monthKey(month, year);
    const existing = patientBundlesRef.current[patientId]?.adherenceByMonth[key];
    if (existing) return existing;

    const requestKey = `${patientId}:${key}`;
    if (adherenceRequestRef.current.has(requestKey)) {
      return adherenceRequestRef.current.get(requestKey) ?? null;
    }

    const request = (async () => {
      setPatientBundles((prev) => {
        const current = prev[patientId] ?? createEmptyBundle();
        return {
          ...prev,
          [patientId]: {
            ...current,
            adherenceLoading: {
              ...current.adherenceLoading,
              [key]: true,
            },
            error: null,
          },
        };
      });

      try {
        const adherence = await getPatientAdherenceMonth({ patient_id: patientId, payload: { month, year } });
        setPatientBundles((prev) => {
          const current = prev[patientId] ?? createEmptyBundle();
          return {
            ...prev,
            [patientId]: {
              ...current,
              adherenceByMonth: {
                ...current.adherenceByMonth,
                [key]: adherence,
              },
              adherenceLoading: {
                ...current.adherenceLoading,
                [key]: false,
              },
            },
          };
        });
        return adherence;
      } catch {
        setPatientBundles((prev) => {
          const current = prev[patientId] ?? createEmptyBundle();
          return {
            ...prev,
            [patientId]: {
              ...current,
              adherenceLoading: {
                ...current.adherenceLoading,
                [key]: false,
              },
            },
          };
        });
        return null;
      } finally {
        adherenceRequestRef.current.delete(requestKey);
      }
    })();

    adherenceRequestRef.current.set(requestKey, request);
    return request;
  }, []);

  const reconcilePatientAnomalies = useCallback(async (patientId: number, payload: WebReconcileAnomalyPayload) => {
    const result = await reconcileAnomalies({ patient_id: patientId, payload });
    await Promise.all([ensurePatientBundle(patientId), refreshPatients()]);
    return result;
  }, [ensurePatientBundle, refreshPatients]);

  useEffect(() => {
    if (!isAuthenticated) {
      setPatients([]);
      setPatientBundles({});
      setPatientsError(null);
      setPatientsLoading(false);
      rosterRequestRef.current = null;
      patientRequestRef.current.clear();
      adherenceRequestRef.current.clear();
      return;
    }

    void refreshPatients();
  }, [isAuthenticated, refreshPatients]);

  const value: PatientContextType = {
    patients,
    patientBundles,
    patientsLoading,
    patientsError,
    refreshPatients,
    ensurePatientBundle,
    loadPatientAdherence,
    reconcilePatientAnomalies,
  };

  return <PatientContext.Provider value={value}>{children}</PatientContext.Provider>;
}

export function usePatients() {
  const ctx = useContext(PatientContext);
  if (!ctx) throw new Error('usePatients must be used within a PatientProvider');
  return ctx;
}

export default PatientContext;