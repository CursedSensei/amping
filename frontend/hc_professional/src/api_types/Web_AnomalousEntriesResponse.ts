export interface WebAnomalousEntriesResponse {
    entries: WebAnomalousEntry[];
}

export interface WebAnomalousEntry {
    date:   Date;
    reason: string;
}
