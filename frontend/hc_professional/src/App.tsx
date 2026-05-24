import { BrowserRouter, Routes, Route } from 'react-router-dom';
import PatientRoster from './pages/PatientRoster';
import UnifiedAdherenceRecord from './pages/UnifiedAdherenceRecord';
import DoseReconciliation from './pages/DoseReconciliation';
import RiskStratification from './pages/RiskStratification';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<PatientRoster />} />
        <Route path="/patient/:id" element={<UnifiedAdherenceRecord />} />
        <Route path="/patient/:id/reconcile" element={<DoseReconciliation />} />
        <Route path="/risk" element={<RiskStratification />} />
      </Routes>
    </BrowserRouter>
  );
}
