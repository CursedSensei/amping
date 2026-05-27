export interface MobileWeeklyAdherenceResponse {
    adherence_days: MobileAdherenceDayEntry[];
    week_end:       Date;
    week_start:     Date;
}

export interface MobileAdherenceDayEntry {
    date:   Date;
    status: AdherenceStatusEnum;
}

export enum AdherenceStatusEnum {
    AppRecorded = "app_recorded",
    ProviderReconciled = "provider_reconciled",
    TechnicalMiss = "technical_miss",
    UnverifiedAbsence = "unverified_absence",
}
