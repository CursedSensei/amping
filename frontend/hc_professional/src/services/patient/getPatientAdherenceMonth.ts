import type { WebAdherenceMonthResponse } from "../../api_types/Web_AdherenceMonthResponse";
import { client } from "../api";


export async function getPatientAdherenceMonth({patient_id}: { patient_id: number }): Promise<WebAdherenceMonthResponse> {
    const response = await client.get(`/patient/${patient_id}/adherence_month/`);
    return response.data;
}