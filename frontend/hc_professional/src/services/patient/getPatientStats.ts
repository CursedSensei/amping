import type { WebGamificationResponse } from "../../api_types/Web_GamificationResponse";
import { client } from "../api";


export async function getPatientStats({patient_id}: { patient_id: number }): Promise<WebGamificationResponse> {
    const response = await client.get(`/patient/${patient_id}/gamification`);
    return response.data;
}