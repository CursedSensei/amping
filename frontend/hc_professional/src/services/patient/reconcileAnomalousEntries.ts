import type { WebReconcileAnomalyPayload } from "../../api_types/Web_ReconcileAnomalyPayload";
import type { WebReconcileAnomalyResponse } from "../../api_types/Web_ReconcileAnomalyResponse";
import { client } from "../api";


export async function getAnomalousEntries({patient_id, payload}: { patient_id: number, payload: WebReconcileAnomalyPayload }): Promise<WebReconcileAnomalyResponse> {
    const response = await client.post(`/patient/${patient_id}/reconcile_anomalies/`, payload);
    return response.data;
}