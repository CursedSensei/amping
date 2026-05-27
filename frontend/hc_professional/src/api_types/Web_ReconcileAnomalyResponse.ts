/**
 * Returned after a successful reconciliation batch.
 */
export interface WebReconcileAnomalyResponse {
    reconciled_count:    number;
    updated_heart_quota: number;
    updated_pdc:         number;
    updated_streak:      number;
}
