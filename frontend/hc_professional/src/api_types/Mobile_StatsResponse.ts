export interface MobileStatsResponse {
    best_streak:        number;
    current_streak:     number;
    heart_quota:        number;
    penalty_history:    MobilePenaltyEvent[];
    total_regimen_days: number;
}

export interface MobilePenaltyEvent {
    date:  Date;
    label: string;
    tier:  number;
}
