import type { WebLoginHealthProviderPayload } from "../api_types/Web_LoginHealthProviderPayload";
import type { WebLoginHealthProviderResponse } from "../api_types/Web_LoginHealthProviderResponse";
import type { WebLogoutResponse } from "../api_types/Web_LogoutResponse";
import apiClient from "./axiosInstance";

export async function login(email: string, password: string): Promise<WebLoginHealthProviderResponse> {
    const data: WebLoginHealthProviderPayload = {
        email,
        password,
    }

    const response = await apiClient.post('/login/', data);
    return response.data;
}

export async function logout(): Promise<WebLogoutResponse> {
    const response = await apiClient.post('/logout/');
    return response.data;
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
