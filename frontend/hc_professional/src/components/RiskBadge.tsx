import type { RiskTier } from '../api_types/Patient';

interface RiskBadgeProps {
  tier: RiskTier;
  month3Protected?: boolean;
  showLabel?: boolean;
}

const CONFIG: Record<RiskTier, { label: string; classes: string }> = {
  tier1: { label: 'TIER 1 (LOW-MID)', classes: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
  tier2: { label: 'TIER 2 (HIGH/SUSTAINED)', classes: 'bg-orange-100 text-orange-700 border-orange-200' },
  tier3: { label: 'TIER 3 (CRITICAL)', classes: 'bg-red-100 text-red-700 border-red-200' },
  safe: { label: 'SAFE (NO RISK)', classes: 'bg-green-100 text-green-700 border-green-200' },
};

const SIMPLE: Record<RiskTier, { label: string; classes: string }> = {
  tier1: { label: 'HIGH RISK', classes: 'bg-red-500/10 text-red-600 border-red-200' },
  tier2: { label: 'HIGH RISK', classes: 'bg-red-500/10 text-red-600 border-red-200' },
  tier3: { label: 'HIGH RISK', classes: 'bg-red-500/10 text-red-600 border-red-200' },
  safe: { label: 'LOW RISK', classes: 'bg-green-500/10 text-green-700 border-green-200' },
};

export default function RiskBadge({ tier, month3Protected, showLabel = false }: RiskBadgeProps) {
  const config = showLabel ? CONFIG[tier] : SIMPLE[tier];
  return (
    <div className="flex flex-col items-end gap-1">
      <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${config.classes}`}>
        {config.label}
      </span>
      {month3Protected && (
        <span className="text-[9px] font-semibold px-2 py-0.5 rounded border bg-teal-50 text-teal-700 border-teal-200">
          MONTH 3 OVERRIDE ACTIVE
        </span>
      )}
    </div>
  );
}
