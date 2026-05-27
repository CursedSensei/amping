/**
 * Sent by a healthcare provider or BHW to reconcile one or more anomalous
 * entries (e.g. mark a technical-miss as provider-verified).
 */
export interface WebReconcileAnomalyPayload {
    entry_ids:           number[];
    reason:              string;
    verification_method: AdherenceStatusEnum;
}

export enum AdherenceStatusEnum {
    AppRecorded = "app_recorded",
    ProviderReconciled = "provider_reconciled",
    TechnicalMiss = "technical_miss",
    UnverifiedAbsence = "unverified_absence",
}
