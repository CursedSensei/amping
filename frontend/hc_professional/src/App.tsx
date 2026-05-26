import { BrowserRouter, Routes, Route, Navigate, useLocation, useParams, Outlet } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import PatientRoster from './pages/PatientRoster';
import UnifiedAdherenceRecord from './pages/UnifiedAdherenceRecord';
import DoseReconciliation from './pages/DoseReconciliation';
import RiskStratification from './pages/RiskStratification';
import { AuthProvider } from './context/AuthContext';
import { PatientProvider } from './context/PatientContext';
import { PatientDetailProvider } from './context/PatientDetailContext';
import { useAuth } from './hooks/useAuth';

// ─── Layout: single-patient subtree ─────────────────────────────────────────
// Mounts PatientDetailProvider once for /patient/:id and /patient/:id/reconcile
// so both child pages share the same fetched patient without duplicate calls.

function PatientDetailLayout() {
  const { id } = useParams<{ id: string }>();
  return (
    <PatientDetailProvider patientId={id!}>
      <Outlet />
    </PatientDetailProvider>
  );
}

// ─── Page transition wrapper ─────────────────────────────────────────────────

function AnimatedRoutes() {
  const location = useLocation();
  const { isAuthed } = useAuth();

  return (
    <div
      key={location.pathname}
      className="animate-fadein"
      style={{ animation: 'fadein 0.18s ease' }}
    >
      <Routes location={location}>
        {/* Public routes */}
        <Route
          path="/login"
          element={isAuthed ? <Navigate to="/" replace /> : <Login />}
        />
        <Route
          path="/signup"
          element={isAuthed ? <Navigate to="/" replace /> : <Signup />}
        />

        {/* Protected routes */}
        <Route
          path="/"
          element={isAuthed ? <PatientRoster /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/patient/:id"
          element={isAuthed ? <PatientDetailLayout /> : <Navigate to="/login" replace />}
        >
          <Route index element={<UnifiedAdherenceRecord />} />
          <Route path="reconcile" element={<DoseReconciliation />} />
        </Route>
        <Route
          path="/risk"
          element={isAuthed ? <RiskStratification /> : <Navigate to="/login" replace />}
        />

        {/* Fallback */}
        <Route path="*" element={<Navigate to={isAuthed ? '/' : '/login'} replace />} />
      </Routes>
    </div>
  );
}

// ─── Root ────────────────────────────────────────────────────────────────────

export default function App() {
  return (
    <>
      <style>{`
        @keyframes fadein {
          from { opacity: 0; transform: translateY(6px); }
          to   { opacity: 1; transform: translateY(0); }
        }
      `}</style>
      <AuthProvider>
        <PatientProvider>
          <BrowserRouter>
            <AnimatedRoutes />
          </BrowserRouter>
        </PatientProvider>
      </AuthProvider>
    </>
  );
}
