import type { WebCreatePatientPayload } from "../../api_types/Web_CreatePatientPayload";
import type { WebCreatePatientResponse } from "../../api_types/Web_CreatePatientResponse";
import { client } from "../api";


export async function getAllPatients({payload}: { payload: WebCreatePatientPayload }): Promise<WebCreatePatientResponse> {
    const response = await client.post('/patient/', payload);
    return response.data;
}