export interface CreatePatientPayload {
    age:      number;
    ageGroup: string;
    contact:  string;
    email:    string;
    name:     string;
    [property: string]: any;
}
