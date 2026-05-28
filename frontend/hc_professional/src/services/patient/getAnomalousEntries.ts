import type { WebAnomalousEntriesResponse } from "../../api_types/Web_AnomalousEntriesResponse";
import { client } from "../api";


export async function getAnomalousEntries({patient_id}: { patient_id: number }): Promise<WebAnomalousEntriesResponse> {
    const response = await client.get(`/patient/${patient_id}/anomalous_entries/`);
    return response.data;
}