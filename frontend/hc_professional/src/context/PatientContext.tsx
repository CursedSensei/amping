import { createContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { Patient } from '../api_types/Patient';
import { fetchPatients } from '../service/patientService';

// ─── Shape ────────────────────────────────────────────────────────────────────

interface PatientContextValue {
  patients: Patient[];
  isLoading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

// ─── Context ──────────────────────────────────────────────────────────────────

export const PatientContext = createContext<PatientContextValue | null>(null);

// ─── Provider ─────────────────────────────────────────────────────────────────

export function PatientProvider({ children }: { children: ReactNode }) {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchPatients();
      setPatients(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load patients');
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Fetch on mount
  useEffect(() => { void load(); }, [load]);

  return (
    <PatientContext.Provider value={{ patients, isLoading, error, refresh: load }}>
      {children}
    </PatientContext.Provider>
  );
}
