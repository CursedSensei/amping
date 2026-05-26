import { createContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { login as svcLogin, logout as svcLogout, signup as svcSignup, type SignupForm } from '../service/authService';

// ─── Shape ────────────────────────────────────────────────────────────────────

interface AuthContextValue {
  isAuthed: boolean;
  isLoading: boolean;
  authError: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  signup: (form: SignupForm) => Promise<void>;
}

// ─── Context ──────────────────────────────────────────────────────────────────

export const AuthContext = createContext<AuthContextValue | null>(null);

// ─── Provider ─────────────────────────────────────────────────────────────────

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthed, setIsAuthed] = useState<boolean>(
    () => sessionStorage.getItem('hc_auth') === 'true',
  );
  const [isLoading, setIsLoading] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  // Multi-tab sync — mirrors the storage listener that was in App.tsx
  useEffect(() => {
    const onStorage = () => setIsAuthed(sessionStorage.getItem('hc_auth') === 'true');
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    setAuthError(null);
    try {
      await svcLogin(email, password);
      setIsAuthed(true);
    } catch (err) {
      setAuthError(err instanceof Error ? err.message : 'Login failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    await svcLogout();
    setIsAuthed(false);
  }, []);

  const signup = useCallback(async (form: SignupForm) => {
    setIsLoading(true);
    setAuthError(null);
    try {
      await svcSignup(form);
      setIsAuthed(true);
    } catch (err) {
      setAuthError(err instanceof Error ? err.message : 'Signup failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ isAuthed, isLoading, authError, login, logout, signup }}>
      {children}
    </AuthContext.Provider>
  );
}
