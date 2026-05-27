import { client } from "../api";

export async function logout(): Promise<void> {
    await client.post('/logout/');
}