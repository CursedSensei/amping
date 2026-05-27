export interface WebPatientDetailResponse {
    birthyear:        number;
    contact:          string;
    current_day:      number;
    email:            string;
    firstname:        string;
    guardians:        WebPatientGuardianDetailEntry[];
    id:               number;
    lastname:         string;
    month3_protected: boolean;
    month_pdc:        number;
    pdc_target:       number;
    regimen_start:    Date;
    total_days:       number;
}

export interface WebPatientGuardianDetailEntry {
    contact:   string;
    email:     string;
    firstname: string;
    id:        number;
    lastname:  string;
}
