import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ShieldCheck, Users, AlertTriangle, AlertOctagon, Printer, UserCheck, MessageSquare } from 'lucide-react';
import Sidebar from '../components/Sidebar';
import { MOCK_PATIENTS, type Patient, type RiskTier } from '../data/mockData';

const TIER_CONFIG: Record<RiskTier, {
  label: string;
  dotColor: string;
  badgeClasses: string;
  actionLabel?: string;
  ActionIcon?: React.ComponentType<{ size: number; className?: string }>;
  actionClasses?: string;
}> = {
  tier1: {
    label: 'Tier 1 (Low-Mid) — Care Message',
    dotColor: 'bg-yellow-400',
    badgeClasses: 'bg-yellow-100 text-yellow-700 border-yellow-200',
    actionLabel: 'SEND CARE MESSAGE',
    ActionIcon: MessageSquare,
    actionClasses: 'bg-yellow-500 hover:bg-yellow-600',
  },
  tier2: {
    label: 'Tier 2 (High/Sustained) — BHW Visit',
    dotColor: 'bg-orange-500',
    badgeClasses: 'bg-orange-100 text-orange-700 border-orange-200',
    actionLabel: 'DISPATCH BHW VISIT',
    ActionIcon: UserCheck,
    actionClasses: 'bg-orange-500 hover:bg-orange-600',
  },
  tier3: {
    label: 'Tier 3 (Critical) — DOT Order',
    dotColor: 'bg-red-500',
    badgeClasses: 'bg-red-100 text-red-700 border-red-200',
    actionLabel: 'PRINT DOT ORDER',
    ActionIcon: Printer,
    actionClasses: 'bg-red-600 hover:bg-red-700',
  },
  safe: {
    label: 'Safe — No action',
    dotColor: 'bg-green-500',
    badgeClasses: 'bg-green-100 text-green-700 border-green-200',
  },
};

function ActionButton({ patient }: { patient: Patient }) {
  const [triggered, setTriggered] = useState(false);
  const cfg = TIER_CONFIG[patient.riskTier];
  if (patient.riskTier === 'safe' || !cfg.actionLabel || !cfg.ActionIcon) {
    return <span className="text-xs text-gray-300 italic">NONE</span>;
  }
  const Icon = cfg.ActionIcon;
  return (
    <button
      onClick={(e) => {
        e.stopPropagation();
        setTriggered(true);
      }}
      className={`flex items-center gap-1.5 text-white text-[11px] font-bold px-3 py-1.5 rounded-lg transition-colors ${
        triggered ? 'bg-green-500 cursor-default' : cfg.actionClasses
      }`}
    >
      {triggered ? (
        <>
          <ShieldCheck size={12} />
          DISPATCHED
        </>
      ) : (
        <>
          <Icon size={12} />
          {cfg.actionLabel}
        </>
      )}
    </button>
  );
}

function ProgressBar({ current, total }: { current: number; total: number }) {
  const pct = Math.min((current / total) * 100, 100);
  return (
    <div className="flex items-center gap-2">
      <div className="w-20 h-1.5 bg-gray-200 rounded-full overflow-hidden">
        <div
          className="h-full bg-blue-500 rounded-full"
          style={{ width: `${pct}%` }}
        />
      </div>
      <span className="text-xs text-gray-500">
        {current}/{total}
      </span>
    </div>
  );
}

export default function RiskStratification() {
  const navigate = useNavigate();

  const stats = {
    total: MOCK_PATIENTS.length,
    atRisk: MOCK_PATIENTS.filter((p) => p.riskTier !== 'safe').length,
    safe: MOCK_PATIENTS.filter((p) => p.riskTier === 'safe').length,
    month3: MOCK_PATIENTS.filter((p) => p.month3Protected).length,
  };

  // sort: tier3 → tier2 → tier1 → safe
  const tierOrder: RiskTier[] = ['tier3', 'tier2', 'tier1', 'safe'];
  const sorted = [...MOCK_PATIENTS].sort(
    (a, b) => tierOrder.indexOf(a.riskTier) - tierOrder.indexOf(b.riskTier)
  );

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />

      <main className="flex-1 p-8 overflow-y-auto">
        {/* Page header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <button
              onClick={() => navigate('/')}
              className="flex items-center gap-1.5 text-sm text-gray-400 hover:text-blue-600 mb-2 transition-colors"
            >
              <ArrowLeft size={14} />
              Back to Roster
            </button>
            <h1 className="text-2xl font-bold text-gray-900">Risk Stratification & Escalation Center</h1>
            <p className="text-sm text-gray-400 mt-0.5">
              Identify patients at risk of treatment abandonment and trigger escalation workflows.
            </p>
          </div>
        </div>

        {/* Escalation Reset Rule */}
        <div className="flex items-start gap-3 bg-blue-50 border border-blue-100 rounded-xl p-4 mb-6">
          <ShieldCheck size={15} className="text-blue-500 mt-0.5 shrink-0" />
          <p className="text-sm text-blue-700">
            <strong>Escalation Reset rule:</strong> If a provider logs a manual dose reconciliation (see Dose
            Reconciliation), the active alert clears and risk resets to baseline.
          </p>
        </div>

        {/* Stats row */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          {[
            { label: 'Total Active Patients', value: stats.total, icon: Users, color: 'text-gray-900', bg: 'bg-white' },
            { label: 'At Risk', value: stats.atRisk, icon: AlertOctagon, color: 'text-red-600', bg: 'bg-red-50' },
            { label: 'Safe', value: stats.safe, icon: ShieldCheck, color: 'text-green-600', bg: 'bg-green-50' },
            { label: 'Month 3 Protected', value: stats.month3, icon: AlertTriangle, color: 'text-teal-600', bg: 'bg-teal-50' },
          ].map((s) => (
            <div key={s.label} className={`${s.bg} border border-gray-100 rounded-xl p-4 flex flex-col items-center`}>
              <span className={`text-3xl font-bold ${s.color}`}>{s.value}</span>
              <span className="text-xs text-gray-500 text-center mt-1">{s.label}</span>
            </div>
          ))}
        </div>

        {/* Tier legend */}
        <div className="bg-white border border-gray-100 rounded-xl p-4 mb-6">
          <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-3 font-semibold">
            Escalation Tier System
          </p>
          <div className="flex flex-wrap gap-4 text-xs font-medium">
            {(Object.entries(TIER_CONFIG) as [RiskTier, typeof TIER_CONFIG[RiskTier]][]).map(([, cfg]) => (
              <div key={cfg.label} className="flex items-center gap-1.5">
                <span className={`w-2.5 h-2.5 rounded-full ${cfg.dotColor}`} />
                <span className="text-gray-600">{cfg.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Escalation table */}
        <div className="bg-white border border-gray-100 rounded-xl overflow-hidden">
          {/* Table header */}
          <div className="grid grid-cols-[2fr_1fr_1.5fr_2fr_1fr_1.5fr] gap-4 px-6 py-3 bg-gray-50 border-b border-gray-100">
            {['Patient', 'Progress', 'Risk Tier', 'Trigger Reason', 'Last Active', 'Action'].map((h) => (
              <span key={h} className="text-[10px] uppercase tracking-wider text-gray-400 font-semibold">
                {h}
              </span>
            ))}
          </div>

          {sorted.map((patient) => {
            const tierCfg = TIER_CONFIG[patient.riskTier];
            return (
              <div
                key={patient.id}
                onClick={() => navigate(`/patient/${patient.id}`)}
                className="grid grid-cols-[2fr_1fr_1.5fr_2fr_1fr_1.5fr] gap-4 items-center px-6 py-4 border-b border-gray-50 hover:bg-gray-50 cursor-pointer transition-colors"
              >
                {/* Patient name */}
                <div>
                  <p className="font-semibold text-gray-900 text-sm">{patient.name}</p>
                  <p className="text-[11px] text-gray-400">{patient.patientId} · {patient.ageProfile}</p>
                </div>

                {/* Progress */}
                <ProgressBar current={patient.currentDay} total={patient.totalDays} />

                {/* Risk tier */}
                <div className="flex flex-col gap-1">
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border w-fit ${tierCfg.badgeClasses}`}>
                    {patient.riskTier === 'safe'
                      ? 'SAFE (NO RISK)'
                      : patient.riskTier === 'tier1'
                      ? 'TIER 1 (LOW-MID)'
                      : patient.riskTier === 'tier2'
                      ? 'TIER 2 (HIGH/SUSTAINED)'
                      : 'TIER 3 (CRITICAL)'}
                  </span>
                  {patient.month3Protected && (
                    <span className="text-[9px] font-semibold px-2 py-0.5 rounded border bg-teal-50 text-teal-700 border-teal-200 w-fit">
                      MONTH 3 OVERRIDE ACTIVE
                    </span>
                  )}
                </div>

                {/* Trigger reason */}
                <p className="text-xs text-gray-500">{patient.triggerReason}</p>

                {/* Last active */}
                <p className="text-xs text-gray-500">{patient.lastActive}</p>

                {/* Action */}
                <div onClick={(e) => e.stopPropagation()}>
                  <ActionButton patient={patient} />
                </div>
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}
