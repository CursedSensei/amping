import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  ShieldCheck,
  Users,
  AlertTriangle,
  AlertOctagon,
  Printer,
  UserCheck,
  MessageSquare,
  X,
  Download,
  CheckCircle2,
} from 'lucide-react';
import Sidebar from '../components/Sidebar';
import { MOCK_PATIENTS, type Patient, type RiskTier } from '../data/mockData';

// ─── Types ─────────────────────────────────────────────────────────────────

type ModalPatient = Patient | null;

// ─── Toast ─────────────────────────────────────────────────────────────────

function Toast({ message, onDismiss }: { message: string; onDismiss: () => void }) {
  useEffect(() => {
    const t = setTimeout(onDismiss, 5000);
    return () => clearTimeout(t);
  }, [onDismiss]);

  return (
    <div className="fixed top-5 left-1/2 -translate-x-1/2 z-[300] flex items-center gap-3 bg-gray-900 text-white text-sm font-medium px-5 py-3.5 rounded-xl shadow-2xl max-w-md w-[90%]">
      <MessageSquare size={18} className="text-yellow-400 shrink-0" />
      <span className="leading-snug">{message}</span>
      <button onClick={onDismiss} className="ml-auto text-gray-400 hover:text-white transition-colors">
        <X size={14} />
      </button>
    </div>
  );
}

// ─── BHW Dispatch Modal ─────────────────────────────────────────────────────

function BHWModal({ patient, onClose }: { patient: Patient; onClose: () => void }) {
  const [confirmed, setConfirmed] = useState(false);

  const handleConfirm = () => {
    setConfirmed(true);
    setTimeout(onClose, 1800);
  };

  return (
    <div className="fixed inset-0 z-[100] bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden border border-orange-100">
        {/* Header */}
        <div className="flex items-center gap-4 p-6 bg-orange-50 border-b border-orange-100">
          <div className="w-11 h-11 rounded-full bg-white border-2 border-orange-200 flex items-center justify-center shrink-0">
            <UserCheck size={22} className="text-orange-600" />
          </div>
          <div>
            <h3 className="font-bold text-gray-900 text-lg">Dispatch BHW Visit</h3>
            <p className="text-[11px] font-semibold uppercase tracking-widest text-orange-600">Tier 2 Escalation</p>
          </div>
        </div>

        <div className="p-6">
          {confirmed ? (
            <div className="flex flex-col items-center py-4 gap-3 text-center">
              <CheckCircle2 size={40} className="text-green-500" />
              <p className="font-bold text-gray-900">BHW visit dispatched!</p>
              <p className="text-sm text-gray-400">Case file sent to {patient.bhw}.</p>
            </div>
          ) : (
            <>
              <p className="text-gray-700 font-medium mb-2">
                Generate a BHW Home Visit Case File for{' '}
                <strong className="text-gray-900">{patient.name}</strong>?
              </p>
              <p className="text-sm text-gray-400">
                This will create an action task for <strong>{patient.bhw}</strong> (assigned BHW) and log the
                escalation with a timestamp.
              </p>
            </>
          )}
        </div>

        {!confirmed && (
          <div className="p-6 pt-0 flex gap-3">
            <button
              onClick={onClose}
              className="flex-1 border border-gray-200 text-gray-600 text-sm font-medium py-2.5 rounded-xl hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirm}
              className="flex-1 bg-orange-500 text-white text-sm font-semibold py-2.5 rounded-xl hover:bg-orange-600 transition-colors"
            >
              Confirm Dispatch
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

// ─── DOT Order Print Modal ──────────────────────────────────────────────────

function DOTModal({ patient, onClose }: { patient: Patient; onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-[100] bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl w-full max-w-2xl shadow-2xl overflow-hidden border border-gray-200 flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h3 className="font-bold text-gray-900">Document Preview</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 bg-gray-200 p-1.5 rounded-full transition-colors"
          >
            <X size={16} />
          </button>
        </div>

        {/* Document */}
        <div className="flex-1 overflow-y-auto bg-gray-200 p-6 flex justify-center shadow-inner">
          <div className="bg-white w-full max-w-lg p-10 shadow-lg border border-gray-300 flex flex-col gap-6 font-serif text-gray-800 text-sm">
            {/* Doc header */}
            <div className="text-center border-b-2 border-gray-800 pb-5">
              <h1 className="text-xl font-black uppercase tracking-widest text-gray-900">
                Department of Health
              </h1>
              <h2 className="text-lg font-bold uppercase mt-1">DOT Reinstatement Order</h2>
              <p className="text-xs font-bold text-gray-400 mt-1 tracking-widest font-sans">FORM EC-10</p>
            </div>

            {/* Fields */}
            <div className="grid grid-cols-2 gap-y-5 gap-x-6 font-sans">
              {[
                { label: 'Date', value: 'May 24, 2026' },
                { label: 'Clinic', value: patient.clinic },
                { label: 'Patient Name', value: patient.name },
                { label: 'Patient ID', value: patient.patientId },
                { label: 'Age Profile', value: patient.ageProfile },
                { label: 'Treatment Day', value: `Day ${patient.currentDay} of ${patient.totalDays}` },
              ].map((f) => (
                <div key={f.label}>
                  <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-0.5">{f.label}</p>
                  <p className="font-bold text-base text-gray-900">{f.value}</p>
                </div>
              ))}
            </div>

            {/* Reason */}
            <div className="font-sans">
              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">
                Reason for Escalation
              </p>
              <p className="p-3 bg-gray-50 border border-gray-200 rounded-lg text-sm text-gray-700 font-medium">
                {patient.triggerReason}
              </p>
            </div>

            {/* Body */}
            <p className="text-justify leading-relaxed font-sans">
              By order of the attending provider, the patient listed above is hereby mandated to return to
              in-person Directly Observed Therapy (DOT) immediately. Gamified application privileges are
              suspended pending provider review.
            </p>

            {/* Signature */}
            <div className="mt-10 flex justify-end">
              <div className="w-52 border-t-2 border-gray-800 pt-2 text-center font-sans">
                <p className="text-[10px] font-black uppercase tracking-widest text-gray-500">
                  Provider Signature
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 bg-gray-50 flex gap-3 justify-end">
          <button className="flex items-center gap-1.5 border border-gray-200 text-gray-600 text-sm font-medium px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors">
            <Download size={14} />
            Download PDF
          </button>
          <button
            onClick={onClose}
            className="flex items-center gap-1.5 bg-red-600 text-white text-sm font-semibold px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            <Printer size={14} />
            Print Order
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Action Button ──────────────────────────────────────────────────────────

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
    actionLabel: 'SEND MESSAGE',
    ActionIcon: MessageSquare,
    actionClasses: 'bg-yellow-500 hover:bg-yellow-600',
  },
  tier2: {
    label: 'Tier 2 (High/Sustained) — BHW Visit',
    dotColor: 'bg-orange-500',
    badgeClasses: 'bg-orange-100 text-orange-700 border-orange-200',
    actionLabel: 'DISPATCH VISIT',
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

function ActionButton({
  patient,
  onCareMessage,
  onBHW,
  onDOT,
}: {
  patient: Patient;
  onCareMessage: () => void;
  onBHW: () => void;
  onDOT: () => void;
}) {
  const [triggered, setTriggered] = useState(false);
  const cfg = TIER_CONFIG[patient.riskTier];

  if (patient.riskTier === 'safe' || !cfg.actionLabel || !cfg.ActionIcon) {
    return <span className="text-xs text-gray-300 italic">NONE</span>;
  }

  const Icon = cfg.ActionIcon;

  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (triggered) return;
    if (patient.riskTier === 'tier1') {
      setTriggered(true);
      onCareMessage();
    } else if (patient.riskTier === 'tier2') {
      onBHW();
    } else if (patient.riskTier === 'tier3') {
      onDOT();
    }
  };

  return (
    <button
      onClick={handleClick}
      className={`flex items-center gap-1.5 text-white text-[11px] font-bold px-3 py-1.5 rounded-lg transition-colors ${
        triggered ? 'bg-green-500 cursor-default' : cfg.actionClasses
      }`}
    >
      {triggered ? (
        <>
          <CheckCircle2 size={12} />
          SENT
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
        <div className="h-full bg-blue-500 rounded-full" style={{ width: `${pct}%` }} />
      </div>
      <span className="text-xs text-gray-500">{current}/{total}</span>
    </div>
  );
}

// ─── Page ───────────────────────────────────────────────────────────────────

export default function RiskStratification() {
  const navigate = useNavigate();

  const [toast, setToast] = useState('');
  const [bhwPatient, setBhwPatient] = useState<ModalPatient>(null);
  const [dotPatient, setDotPatient] = useState<ModalPatient>(null);

  const stats = {
    total: MOCK_PATIENTS.length,
    atRisk: MOCK_PATIENTS.filter((p) => p.riskTier !== 'safe').length,
    safe: MOCK_PATIENTS.filter((p) => p.riskTier === 'safe').length,
    month3: MOCK_PATIENTS.filter((p) => p.month3Protected).length,
  };

  const tierOrder: RiskTier[] = ['tier3', 'tier2', 'tier1', 'safe'];
  const sorted = [...MOCK_PATIENTS].sort(
    (a, b) => tierOrder.indexOf(a.riskTier) - tierOrder.indexOf(b.riskTier)
  );

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />

      {/* Toast */}
      {toast && <Toast message={toast} onDismiss={() => setToast('')} />}

      {/* BHW Modal */}
      {bhwPatient && <BHWModal patient={bhwPatient} onClose={() => setBhwPatient(null)} />}

      {/* DOT Modal */}
      {dotPatient && <DOTModal patient={dotPatient} onClose={() => setDotPatient(null)} />}

      <main className="flex-1 p-8 overflow-y-auto">
        {/* Page header */}
        <div className="mb-6">
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
          <div className="grid grid-cols-[2fr_1fr_1.5fr_2fr_1fr_1.5fr] gap-4 px-6 py-3 bg-gray-50 border-b border-gray-100">
            {['Patient', 'Progress', 'Risk Tier', 'Trigger Reason', 'Last Active', 'Action'].map((h) => (
              <span key={h} className="text-[10px] uppercase tracking-wider text-gray-400 font-semibold">{h}</span>
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
                {/* Patient */}
                <div>
                  <p className="font-semibold text-gray-900 text-sm">{patient.name}</p>
                  <p className="text-[11px] text-gray-400">{patient.patientId} · {patient.ageProfile}</p>
                </div>

                {/* Progress */}
                <ProgressBar current={patient.currentDay} total={patient.totalDays} />

                {/* Risk tier */}
                <div className="flex flex-col gap-1">
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border w-fit ${tierCfg.badgeClasses}`}>
                    {patient.riskTier === 'safe' ? 'SAFE (NO RISK)'
                      : patient.riskTier === 'tier1' ? 'TIER 1 (LOW-MID)'
                      : patient.riskTier === 'tier2' ? 'TIER 2 (HIGH/SUSTAINED)'
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
                  <ActionButton
                    patient={patient}
                    onCareMessage={() =>
                      setToast(
                        `Care message queued for ${patient.name}. Patient will receive a check-in from their provider character avatar.`
                      )
                    }
                    onBHW={() => setBhwPatient(patient)}
                    onDOT={() => setDotPatient(patient)}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}
