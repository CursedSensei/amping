// ─── Auth service stubs ───────────────────────────────────────────────────────
// Currently simulates network latency and writes to sessionStorage directly.
// When the backend is ready, swap each stub body for the commented apiClient call.

// import apiClient from './axiosInstance';

export async function login(email: string, password: string): Promise<void> {
  // TODO: const res = await apiClient.post('/auth/login/', { email, password });
  //       sessionStorage.setItem('hc_token', res.data.access_token);
  void email; void password; // consumed by real call above
  await new Promise<void>((r) => setTimeout(r, 900));
  sessionStorage.setItem('hc_auth', 'true');
}

export async function logout(): Promise<void> {
  // TODO: await apiClient.post('/auth/logout/');
  sessionStorage.removeItem('hc_auth');
  sessionStorage.removeItem('hc_token');
}

export interface SignupForm {
  fullName: string;
  email: string;
  role: string;
  employeeId: string;
  password: string;
}

export async function signup(form: SignupForm): Promise<void> {
  // TODO: await apiClient.post('/auth/register/', form);
  void form; // consumed by real call above
  await new Promise<void>((r) => setTimeout(r, 1100));
  sessionStorage.setItem('hc_auth', 'true');
}
