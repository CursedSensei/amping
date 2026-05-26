export interface WebGamificationResponse {
    best_streak:     number;
    current_streak:  number;
    heart_quota:     number;
    penalty_history: WebPenaltyEvent[];
}

export interface WebPenaltyEvent {
    date:  Date;
    label: string;
    tier:  number;
}
