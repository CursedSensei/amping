// ─── Configured Axios client ─────────────────────────────────────────────────
// Base URL is read from the VITE_API_BASE_URL environment variable.
// Falls back to the local Django dev server if the variable is not set.

import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
});

// Attach bearer token on every request when one is present in session storage.
apiClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('hc_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default apiClient;
