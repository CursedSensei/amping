import { useContext } from 'react';
import { PatientDetailContext } from '../context/PatientDetailContext';

export function usePatientDetail() {
  const ctx = useContext(PatientDetailContext);
  if (!ctx) throw new Error('usePatientDetail must be used inside <PatientDetailProvider>');
  return ctx;
}
