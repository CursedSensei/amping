import type { WebLoginHealthProviderPayload } from "../../api_types/Web_LoginHealthProviderPayload";
import type { WebLoginHealthProviderResponse } from "../../api_types/Web_LoginHealthProviderResponse";
import { client } from "../api";


export async function login(email: string, password: string): Promise<WebLoginHealthProviderResponse> {
    const payload: WebLoginHealthProviderPayload = {
        email,
        password,
    };

    const response = await client.post('/login/', payload);
    if (response.status !== 200) {
        throw new Error("Login failed: " + response.statusText);
    }
    
    return response.data;
}

export async function logout(): Promise<void> {
    await client.post('/logout/');
}