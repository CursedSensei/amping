import type { WebAdherenceMonthRequest } from "../../api_types/Web_AdherenceMonthRequest";
import type { WebAdherenceMonthResponse } from "../../api_types/Web_AdherenceMonthResponse";
import { client } from "../api";


export async function getPatientAdherenceMonth({patient_id, payload}: { patient_id: number, payload: WebAdherenceMonthRequest }): Promise<WebAdherenceMonthResponse> {
    const response = await client.get(`/patient/${patient_id}/adherence_month/`, { params: payload });
    return response.data;
}