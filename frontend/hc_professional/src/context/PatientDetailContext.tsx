import { createContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { Patient } from '../api_types/Patient';
import { fetchPatientById, reconcileEntries } from '../service/patientService';

// ─── Shape ────────────────────────────────────────────────────────────────────

interface PatientDetailContextValue {
  patient: Patient | null;
  isLoading: boolean;
  error: string | null;
  /** Reconcile anomalous entries and update currentStreak in local state. */
  applyReconciliation: (
    entryIds: string[],
    verificationMethod: string,
    reason: string,
  ) => Promise<{ updatedStreak: number }>;
}

// ─── Context ──────────────────────────────────────────────────────────────────

export const PatientDetailContext = createContext<PatientDetailContextValue | null>(null);

// ─── Provider ─────────────────────────────────────────────────────────────────

interface PatientDetailProviderProps {
  patientId: string;
  children: ReactNode;
}

export function PatientDetailProvider({ patientId, children }: PatientDetailProviderProps) {
  const [patient, setPatient] = useState<Patient | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await fetchPatientById(patientId);
        if (!cancelled) setPatient(data);
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load patient');
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };

    void load();
    return () => { cancelled = true; };
  }, [patientId]);

  const applyReconciliation = useCallback(
    async (entryIds: string[], verificationMethod: string, reason: string) => {
      const result = await reconcileEntries(patientId, entryIds, verificationMethod, reason);
      // Optimistically update the streak in local state
      setPatient((prev) =>
        prev ? { ...prev, currentStreak: result.updatedStreak } : prev,
      );
      return { updatedStreak: result.updatedStreak };
    },
    [patientId],
  );

  return (
    <PatientDetailContext.Provider value={{ patient, isLoading, error, applyReconciliation }}>
      {children}
    </PatientDetailContext.Provider>
  );
}
