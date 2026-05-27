export interface WebCreatePatientPayload {
    birthyear:     number;
    contact:       string;
    email:         string;
    firstname:     string;
    guardians:     WebPatientGuardianEntry[];
    id:            number;
    lastname:      string;
    regimen_start: Date;
    total_days:    number;
}

export interface WebPatientGuardianEntry {
    contact:   string;
    email:     string;
    firstname: string;
    id:        number;
    lastname:  string;
}
