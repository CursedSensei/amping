

import React, { createContext, useContext, useEffect, useState } from 'react';
import { client } from '../services/api';
import { login as apiLogin, logout as apiLogout } from '../services/auth/login';
import { getUserProfile } from '../services/getUserProfile';
import type { UserProfile } from '../types';

type AuthContextType = {
  user: UserProfile | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [user, setUser] = useState<UserProfile | null>(() => {
        try {
            const raw = localStorage.getItem('auth_user');
            return raw ? (JSON.parse(raw) as UserProfile) : null;
        } catch {
            return null;
        }
    });
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(!!user);



    useEffect(() => {
        if (user) localStorage.setItem('auth_user', JSON.stringify(user));
        else localStorage.removeItem('auth_user');
    }, [user]);

    useEffect(() => {
        if (user) {
            fetchUserProfile().catch(() => {
                setUser(null);
            });
        }

        client.interceptors.response.use(response => response, error => {
            if (error.response && error.response.status === 401) {
                setUser(null);
                setIsAuthenticated(false);
            }
            return Promise.reject(error);
        });
    }, []);



    async function fetchUserProfile() {
        const userProfile = await getUserProfile();
        setUser(userProfile);
        setIsAuthenticated(true);
    }



    const login = async (email: string, password: string) => {
        try {
            await apiLogin(email, password);
            await fetchUserProfile();
        } catch (error) {
            logout();
            throw error;
        }
    };

    const logout = async () => {
        await apiLogout();
        setUser(null);
        setIsAuthenticated(false);
    };



    return <AuthContext.Provider value={{ user, isAuthenticated, login, logout }}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
};

export default AuthContext;
