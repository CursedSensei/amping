import type { WebLoginHealthProviderPayload } from "../api_types/Web_LoginHealthProviderPayload";
import type { WebLoginHealthProviderResponse } from "../api_types/Web_LoginHealthProviderResponse";
import api from "./axios_client";

export async function login(email: string, password: string): Promise<WebLoginHealthProviderResponse> {
    const data: WebLoginHealthProviderPayload = {
        email,
        password,
    }

    const response = await api.post('/login/', data);
    return response.data;
}