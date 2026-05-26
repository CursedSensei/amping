import { useContext } from 'react';
import { PatientContext } from '../context/PatientContext';

export function usePatients() {
  const ctx = useContext(PatientContext);
  if (!ctx) throw new Error('usePatients must be used inside <PatientProvider>');
  return ctx;
}
