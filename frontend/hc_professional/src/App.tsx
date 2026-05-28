import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { PatientProvider } from './context/PatientContext';
import DoseReconciliation from './pages/DoseReconciliation';
import Login from './pages/Login';
import PatientRoster from './pages/PatientRoster';
import RiskStratification from './pages/RiskStratification';
import Signup from './pages/Signup';
import UnifiedAdherenceRecord from './pages/UnifiedAdherenceRecord';

// ─── Page transition wrapper ────────────────────────────────────────────────

function AnimatedRoutes() {
  const location = useLocation();
  const { isAuthenticated} = useAuth();

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
          element={isAuthenticated ? <Navigate to="/" replace /> : <Login />}
        />
        <Route
          path="/signup"
          element={isAuthenticated ? <Navigate to="/" replace /> : <Signup />}
        />

        {/* Protected routes */}
        <Route
          path="/"
          element={isAuthenticated ? <PatientRoster /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/patient/:id"
          element={isAuthenticated ? <UnifiedAdherenceRecord /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/patient/:id/reconcile"
          element={isAuthenticated ? <DoseReconciliation /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/risk"
          element={isAuthenticated ? <RiskStratification /> : <Navigate to="/login" replace />}
        />

        {/* Fallback */}
        <Route path="*" element={<Navigate to={isAuthenticated ? '/' : '/login'} replace />} />
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
            <AnimatedRoutes/>
          </BrowserRouter>
        </PatientProvider>
      </AuthProvider>
    </>
  );
}
