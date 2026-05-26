export interface WebAdherenceMonthResponse {
    adherence_days: WebAdherenceDayEntry[];
    month:          number;
    month_pdc:      number;
    pdc_target:     number;
    year:           number;
}

export interface WebAdherenceDayEntry {
    date:       Date;
    id:         number;
    status:     AdherenceStatusEnum;
    symptoms:   string[];
    video_link: null | string;
}

export enum AdherenceStatusEnum {
    AppRecorded = "app_recorded",
    ProviderReconciled = "provider_reconciled",
    TechnicalMiss = "technical_miss",
    UnverifiedAbsence = "unverified_absence",
}
