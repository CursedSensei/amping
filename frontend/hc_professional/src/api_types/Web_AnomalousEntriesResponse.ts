export interface WebAnomalousEntriesResponse {
    entries: WebAnomalousEntry[];
}

export interface WebAnomalousEntry {
    date:   Date;
    id:     number;
    reason: string;
}
