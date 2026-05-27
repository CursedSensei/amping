/**
 * Sent by a healthcare provider or BHW to reconcile one or more anomalous
 * entries (e.g. mark a technical-miss as provider-verified).
 */
export interface WebReconcileAnomalyPayload {
    entry_ids:           number[];
    reason:              string;
    verification_method: ReconciliationMethodEnum;
}

export enum ReconciliationMethodEnum {
    DotOrder = "dot_order",
    HomeVisit = "home_visit",
    SendMessage = "send_message",
}
