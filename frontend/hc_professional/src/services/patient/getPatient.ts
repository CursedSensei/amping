import type { WebPatientDetailResponse } from "../../api_types/Web_PatientDetailResponse";
import { client } from "../api";


export async function getPatient({patient_id}: { patient_id: number }): Promise<WebPatientDetailResponse> {
    const response = await client.get(`/patient/${patient_id}`);
    return response.data;
}