import type { WebHealthCareProviderProfileResponse } from "../api_types/Web_HealthCareProviderProfileResponse";
import { client } from "./api";


export async function getUserProfile(): Promise<WebHealthCareProviderProfileResponse> {
    const response = await client.get('/profile/');
    return response.data;
}