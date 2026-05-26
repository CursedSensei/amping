import type { WebLogoutResponse } from "../api_types/Web_LogoutResponse";
import api from "./axios_client";

export async function logout(): Promise<WebLogoutResponse> {
    const response = await api.post('/logout/');
    return response.data;
}