import type { WebGetAllPatientsResponse } from "../../api_types/Web_GetAllPatientsResponse";
import { client } from "../api";


export async function getAllPatients(): Promise<WebGetAllPatientsResponse> {
    const response = await client.get('/patient/');
    return response.data;
}