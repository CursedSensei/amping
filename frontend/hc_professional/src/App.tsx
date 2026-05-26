import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import PatientRoster from './pages/PatientRoster';
import UnifiedAdherenceRecord from './pages/UnifiedAdherenceRecord';
import DoseReconciliation from './pages/DoseReconciliation';
import RiskStratification from './pages/RiskStratification';
import PatientRegistration from './pages/PatientRegistration';

// ─── Page transition wrapper ────────────────────────────────────────────────

function AnimatedRoutes({ isAuthed, onLogin, onLogout }: {
  isAuthed: boolean;
  onLogin: () => void;
  onLogout: () => void;
}) {
  const location = useLocation();

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
          element={isAuthed ? <Navigate to="/" replace /> : <Login onLogin={onLogin} />}
        />
        <Route
          path="/signup"
          element={isAuthed ? <Navigate to="/" replace /> : <Signup onLogin={onLogin} />}
        />

        {/* Protected routes */}
        <Route
          path="/"
          element={isAuthed ? <PatientRoster onLogout={onLogout} /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/patient/:id"
          element={isAuthed ? <UnifiedAdherenceRecord /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/patient/:id/reconcile"
          element={isAuthed ? <DoseReconciliation /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/risk"
          element={isAuthed ? <RiskStratification /> : <Navigate to="/login" replace />}
        />
        <Route
          path="/patientregister"
          element={isAuthed ? <PatientRegistration /> : <Navigate to="/login" replace />}
        />

        {/* Fallback */}
        <Route path="*" element={<Navigate to={isAuthed ? '/' : '/login'} replace />} />
      </Routes>
    </div>
  );
}

// ─── Root ────────────────────────────────────────────────────────────────────

export default function App() {
  const [isAuthed, setIsAuthed] = useState(() => sessionStorage.getItem('hc_auth') === 'true');

  useEffect(() => {
    // Sync auth state on storage events (multi-tab support)
    const onStorage = () => setIsAuthed(sessionStorage.getItem('hc_auth') === 'true');
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const handleLogin = () => setIsAuthed(true);
  const handleLogout = () => {
    sessionStorage.removeItem('hc_auth');
    setIsAuthed(false);
  };

  return (
    <>
      <style>{`
        @keyframes fadein {
          from { opacity: 0; transform: translateY(6px); }
          to   { opacity: 1; transform: translateY(0); }
        }
      `}</style>
      <BrowserRouter>
        <AnimatedRoutes isAuthed={isAuthed} onLogin={handleLogin} onLogout={handleLogout} />
      </BrowserRouter>
    </>
  );
}
