import { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import PatientRoster from './pages/PatientRoster';
import UnifiedAdherenceRecord from './pages/UnifiedAdherenceRecord';
import DoseReconciliation from './pages/DoseReconciliation';
import RiskStratification from './pages/RiskStratification';
import { logout as apiLogout } from './services/api';

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

        {/* Fallback */}
        <Route path="*" element={<Navigate to={isAuthed ? '/' : '/login'} replace />} />
      </Routes>
    </div>
  );
}

// ─── Root ────────────────────────────────────────────────────────────────────

export default function App() {
  // Django session cookie is the source of truth — start unauthenticated.
  // The 401 interceptor in api.ts will redirect to /login if the cookie expires.
  const [isAuthed, setIsAuthed] = useState(false);

  const handleLogin = () => setIsAuthed(true);

  const handleLogout = async () => {
    try {
      await apiLogout();
    } catch {
      // Ignore errors — clear local state regardless so UI reflects logged-out
    }
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
