export interface WebGetAllPatientsResponse {
    patients: WebPatientEntry[];
}

export interface WebPatientEntry {
    birthyear: number;
    contact:   string;
    email:     string;
    firstname: string;
    id:        number;
    lastname:  string;
}
