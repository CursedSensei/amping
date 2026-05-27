export interface WebCreatePatientPayload {
    birthyear:     number;
    contact:       string;
    email:         string;
    firstname:     string;
    guardians:     WebPatientGuardianEntry[];
    lastname:      string;
    regimen_start: Date;
    total_days:    number;
}

export interface WebPatientGuardianEntry {
    contact:   string;
    email:     string;
    firstname: string;
    lastname:  string;
}
