export interface WebPDCTrendResponse {
    pdc_target: number;
    weekly_pdc: WebWeeklyPDCEntry[];
}

export interface WebWeeklyPDCEntry {
    pdc:  number;
    week: number;
}
