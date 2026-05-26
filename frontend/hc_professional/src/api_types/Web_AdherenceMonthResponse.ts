export interface WebAdherenceMonthResponse {
    adherence_days: WebAdherenceDayEntry[];
    month:          number;
    year:           number;
}

export interface WebAdherenceDayEntry {
    adherence_type: string;
    date:           Date;
    symptoms:       string[];
    video_link:     null | string;
}
