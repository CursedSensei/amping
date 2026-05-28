import { useState, useEffect, useRef, useCallback } from 'react';
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
  ImageIcon,
} from 'lucide-react';
import { useReactToPrint } from 'react-to-print';
import gsap from 'gsap';
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
    <div className="fixed top-6 left-1/2 -translate-x-1/2 z-[300] flex items-center gap-3 bg-[#0A0F24] text-white text-sm font-bold px-6 py-4 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.2)] border border-white/10 max-w-md w-[90%] transform transition-all animate-in slide-in-from-top-4 fade-in">
      <div className="w-8 h-8 rounded-full bg-blue-500/20 flex items-center justify-center shrink-0">
        <MessageSquare size={16} className="text-blue-400" />
      </div>
      <span className="leading-snug flex-1">{message}</span>
      <button onClick={onDismiss} className="text-slate-400 hover:text-white transition-colors bg-white/5 hover:bg-white/10 p-1.5 rounded-full">
        <X size={16} />
      </button>
    </div>
  );
}

// ─── BHW Dispatch Modal ─────────────────────────────────────────────────────

function BHWModal({ patient, onClose }: { patient: Patient; onClose: () => void }) {
  const [confirmed, setConfirmed] = useState(false);

  const handleConfirm = () => {
    setConfirmed(true);
    setTimeout(onClose, 2000);
  };

  return (
    <div className="fixed inset-0 z-[100] bg-slate-900/40 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden border border-slate-100 transform transition-all">
        <div className="flex items-center gap-4 p-6 bg-gradient-to-br from-orange-50 to-orange-100/50 border-b border-orange-100">
          <div className="w-12 h-12 rounded-2xl bg-white shadow-sm border border-orange-200 flex items-center justify-center shrink-0">
            <UserCheck size={24} className="text-orange-500" />
          </div>
          <div>
            <h3 className="font-black text-slate-900 text-xl tracking-tight">Dispatch BHW Visit</h3>
            <p className="text-[10px] font-bold uppercase tracking-widest text-orange-600 mt-1">Tier 2 Escalation</p>
          </div>
        </div>
        
        <div className="p-8">
          {confirmed ? (
            <div className="flex flex-col items-center py-6 gap-4 text-center animate-in zoom-in-95 duration-300">
              <div className="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center mb-2">
                <CheckCircle2 size={32} className="text-emerald-500" />
              </div>
              <div>
                <p className="text-xl font-black text-slate-900 tracking-tight">BHW visit dispatched!</p>
                <p className="text-sm text-slate-500 font-medium mt-2">Case file securely sent to <strong>{patient.bhw}</strong>.</p>
              </div>
            </div>
          ) : (
            <>
              <p className="text-slate-700 font-medium text-lg mb-3 leading-snug">
                Generate a BHW Home Visit Case File for <strong className="text-slate-900 font-black">{patient.name}</strong>?
              </p>
              <p className="text-sm text-slate-500 font-medium leading-relaxed bg-slate-50 p-4 rounded-xl border border-slate-100">
                This will create an actionable task for <strong className="text-slate-700">{patient.bhw}</strong> (assigned BHW) and automatically log the escalation with a timestamp.
              </p>
            </>
          )}
        </div>

        {!confirmed && (
          <div className="p-6 pt-0 flex gap-3 bg-slate-50/50 border-t border-slate-50">
            <button onClick={onClose} className="flex-1 border border-slate-200 bg-white text-slate-600 text-sm font-bold py-3.5 rounded-xl hover:bg-slate-50 hover:text-slate-900 transition-all shadow-sm">
              Cancel
            </button>
            <button onClick={handleConfirm} className="flex-1 bg-orange-500 text-white text-sm font-bold py-3.5 rounded-xl hover:bg-orange-600 transition-all shadow-lg shadow-orange-500/20">
              Confirm Dispatch
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

// ─── DOT Order Modal ────────────────────────────────────────────────────────

function DOTModal({ patient, onClose }: { patient: Patient; onClose: () => void }) {
  const printRef = useRef<HTMLDivElement>(null);

  const [reason, setReason] = useState(patient.triggerReason);
  const [body, setBody] = useState(
    'By order of the attending provider, the patient listed above is hereby mandated to return to ' +
    'in-person Directly Observed Therapy (DOT) immediately. Gamified application privileges are ' +
    'suspended pending provider review.'
  );

  const [sigImg, setSigImg] = useState<string | null>(null);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handlePrint = useReactToPrint({
    contentRef: printRef,
    documentTitle: `DOT_Order_${patient.patientId}`,
  });

  const loadFile = (file: File) => {
    if (!file.type.startsWith('image/')) return;
    const reader = new FileReader();
    reader.onload = (e) => setSigImg(e.target?.result as string);
    reader.readAsDataURL(file);
  };

  const onDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files[0];
    if (file) loadFile(file);
  }, []);

  const onFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) loadFile(file);
  };

  const today = new Date().toLocaleDateString('en-PH', { year: 'numeric', month: 'long', day: 'numeric' });

  return (
    <div className="fixed inset-0 z-[100] bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 lg:p-10">
      <div className="bg-white rounded-3xl w-full max-w-5xl shadow-2xl overflow-hidden border border-slate-200 flex flex-col max-h-[95vh]">
        
        {/* Modal header */}
        <div className="flex items-center justify-between px-8 py-5 bg-white border-b border-slate-100 shrink-0">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-xl bg-rose-100 flex items-center justify-center">
              <Printer size={20} className="text-rose-600" />
            </div>
            <div>
              <h3 className="font-black text-slate-900 text-lg tracking-tight">DOT Reinstatement Order</h3>
              <p className="text-xs text-slate-500 font-medium mt-0.5">Edit document parameters before exporting.</p>
            </div>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 bg-slate-100 hover:bg-slate-200 p-2 rounded-full transition-colors">
            <X size={18} />
          </button>
        </div>

        {/* Two-column Layout */}
        <div className="flex flex-1 min-h-0 overflow-hidden bg-slate-50">
          
          {/* Left — edit panel */}
          <div className="w-80 shrink-0 border-r border-slate-200 p-6 overflow-y-auto space-y-6 bg-white">
            <div>
              <label className="block text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">
                Reason for Escalation
              </label>
              <textarea
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                rows={4}
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 resize-none transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">
                Order Body
              </label>
              <textarea
                value={body}
                onChange={(e) => setBody(e.target.value)}
                rows={6}
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 resize-none transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">
                Provider Signature
              </label>
              {sigImg ? (
                <div className="relative border border-slate-200 rounded-xl overflow-hidden bg-slate-50 p-2">
                  <img src={sigImg} alt="Signature" className="w-full max-h-24 object-contain bg-white rounded-lg border border-slate-100" />
                  <button
                    onClick={() => setSigImg(null)}
                    className="absolute top-3 right-3 bg-white rounded-full p-1.5 text-slate-400 hover:text-rose-500 shadow-md transition-colors"
                  >
                    <X size={14} />
                  </button>
                </div>
              ) : (
                <div
                  onDrop={onDrop}
                  onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                  onDragLeave={() => setDragOver(false)}
                  onClick={() => fileInputRef.current?.click()}
                  className={`border-2 border-dashed rounded-xl p-6 flex flex-col items-center gap-3 cursor-pointer transition-all ${dragOver ? 'border-blue-400 bg-blue-50 scale-[1.02]' : 'border-slate-200 hover:border-blue-300 hover:bg-blue-50/30'}`}
                >
                  <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center">
                    <ImageIcon size={20} className="text-slate-400" />
                  </div>
                  <p className="text-xs font-medium text-slate-500 text-center leading-relaxed">Drag & drop or click to upload signature</p>
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={onFileChange}
                  />
                </div>
              )}
            </div>
          </div>

          {/* Right — document preview */}
          <div className="flex-1 overflow-y-auto p-8 flex justify-center items-start">
            <div
              ref={printRef}
              className="bg-white w-full max-w-[210mm] p-[10%] shadow-[0_8px_30px_rgb(0,0,0,0.12)] border border-slate-200 flex flex-col gap-8 font-serif text-slate-900 min-h-[297mm]"
            >
              {/* DoH letterhead */}
              <div className="text-center border-b-2 border-slate-900 pb-6">
                <div className="flex items-center justify-center gap-4 mb-4">
                  <div className="w-12 h-12 rounded-full bg-blue-700 flex items-center justify-center text-white text-xs font-black tracking-tighter">DoH</div>
                  <div className="text-left">
                    <h1 className="text-xl font-black uppercase tracking-widest text-slate-900 leading-tight">
                      Department of Health
                    </h1>
                    <p className="text-[10px] text-slate-500 uppercase tracking-widest font-sans font-bold mt-1">Republic of the Philippines</p>
                  </div>
                </div>
                <h2 className="text-2xl font-bold uppercase mt-4 tracking-tight">DOT Reinstatement Order</h2>
                <p className="text-xs font-bold text-slate-400 mt-1 tracking-[0.2em] font-sans">FORM EC-10</p>
              </div>

              {/* Patient info fields */}
              <div className="grid grid-cols-2 gap-y-6 gap-x-8 font-sans">
                {[
                  { label: 'Date',           value: today },
                  { label: 'Clinic',         value: patient.clinic },
                  { label: 'Patient Name',   value: patient.name },
                  { label: 'Patient ID',     value: patient.patientId },
                  { label: 'Age Profile',    value: patient.ageProfile },
                  { label: 'Treatment Day',  value: `Day ${patient.currentDay} of ${patient.totalDays}` },
                ].map((f) => (
                  <div key={f.label}>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">{f.label}</p>
                    <p className="font-bold text-base text-slate-900 break-words">{f.value}</p>
                  </div>
                ))}
              </div>

              {/* Reason */}
              <div className="font-sans">
                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2">Reason for Escalation</p>
                <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 font-medium whitespace-pre-wrap">
                  {reason || '(no reason entered)'}
                </div>
              </div>

              {/* Body */}
              <p className="text-justify leading-relaxed font-sans text-slate-800 whitespace-pre-wrap text-sm">
                {body}
              </p>

              {/* Signature */}
              <div className="mt-auto pt-12 flex justify-end">
                <div className="w-64 text-center font-sans">
                  {sigImg ? (
                    <img src={sigImg} alt="Signature" className="w-full max-h-20 object-contain mb-2" />
                  ) : (
                    <div className="h-16 border-b-2 border-slate-900 mb-2" />
                  )}
                  <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">Provider Signature</p>
                  <p className="text-xs font-bold text-slate-800 mt-1">{patient.provider}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer actions */}
        <div className="px-8 py-5 border-t border-slate-200 bg-white flex gap-4 justify-end shrink-0">
          <button
            onClick={() => handlePrint()}
            className="flex items-center gap-2 border border-slate-200 text-slate-600 bg-white text-sm font-bold px-6 py-3 rounded-xl hover:bg-slate-50 transition-all shadow-sm"
          >
            <Download size={16} />
            Export PDF
          </button>
          <button
            onClick={() => handlePrint()}
            className="flex items-center gap-2 bg-rose-600 text-white text-sm font-bold px-6 py-3 rounded-xl hover:bg-rose-700 transition-all shadow-lg shadow-rose-600/20"
          >
            <Printer size={16} />
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
    dotColor: 'bg-amber-400',
    badgeClasses: 'bg-amber-50 text-amber-700 border-amber-200',
    actionLabel: 'SEND MESSAGE',
    ActionIcon: MessageSquare,
    actionClasses: 'bg-amber-500 hover:bg-amber-600 text-white shadow-md shadow-amber-500/20',
  },
  tier2: {
    label: 'Tier 2 (Sustained) — BHW Visit',
    dotColor: 'bg-orange-500',
    badgeClasses: 'bg-orange-50 text-orange-700 border-orange-200',
    actionLabel: 'DISPATCH VISIT',
    ActionIcon: UserCheck,
    actionClasses: 'bg-orange-500 hover:bg-orange-600 text-white shadow-md shadow-orange-500/20',
  },
  tier3: {
    label: 'Tier 3 (Critical) — DOT Order',
    dotColor: 'bg-rose-500',
    badgeClasses: 'bg-rose-50 text-rose-700 border-rose-200',
    actionLabel: 'PRINT DOT ORDER',
    ActionIcon: Printer,
    actionClasses: 'bg-rose-600 hover:bg-rose-700 text-white shadow-md shadow-rose-600/20',
  },
  safe: {
    label: 'Safe — No action',
    dotColor: 'bg-emerald-500',
    badgeClasses: 'bg-emerald-50 text-emerald-700 border-emerald-200',
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
    return <span className="text-xs text-slate-300 font-bold italic tracking-wide">NONE</span>;
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
      className={`flex items-center gap-2 text-[10px] uppercase tracking-wider font-black px-4 py-2.5 rounded-xl transition-all ${triggered ? 'bg-emerald-500 text-white cursor-default shadow-md shadow-emerald-500/20' : cfg.actionClasses}`}
    >
      {triggered ? (
        <><CheckCircle2 size={14} /> SENT</>
      ) : (
        <><Icon size={14} /> {cfg.actionLabel}</>
      )}
    </button>
  );
}

function ProgressBar({ current, total }: { current: number; total: number }) {
  const pct = Math.min((current / total) * 100, 100);
  return (
    <div className="flex flex-col gap-1.5 w-full max-w-[120px]">
      <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden border border-slate-200">
        <div className="h-full bg-blue-500 rounded-full transition-all duration-1000 ease-out" style={{ width: `${pct}%` }} />
      </div>
      <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">{current} / {total} Days</span>
    </div>
  );
}

// ─── Page ───────────────────────────────────────────────────────────────────

export default function RiskStratification() {
  const navigate = useNavigate();
  const containerRef = useRef<HTMLDivElement>(null);

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

  // GSAP Animations
  useEffect(() => {
    const ctx = gsap.context(() => {
      // Animate stat cards
      gsap.fromTo('.stat-card', 
        { y: 20, opacity: 0 }, 
        { y: 0, opacity: 1, duration: 0.5, stagger: 0.1, ease: 'power3.out' }
      );
      
      // Animate legend and rule
      gsap.fromTo('.info-block',
        { y: 15, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.5, ease: 'power3.out', delay: 0.2 }
      );

      // Animate table rows
      gsap.fromTo('.escalation-row',
        { y: 15, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.4, stagger: 0.05, ease: 'power2.out', delay: 0.3 }
      );
    }, containerRef);
    return () => ctx.revert();
  }, []);

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden font-sans">
      <Sidebar />

      {toast && <Toast message={toast} onDismiss={() => setToast('')} />}
      {bhwPatient && <BHWModal patient={bhwPatient} onClose={() => setBhwPatient(null)} />}
      {dotPatient && <DOTModal patient={dotPatient} onClose={() => setDotPatient(null)} />}

      <main className="flex-1 p-6 md:p-10 overflow-y-auto h-full scroll-smooth">
        <div ref={containerRef} className="max-w-7xl mx-auto pb-10">
          
          <div className="mb-8">
            <button onClick={() => navigate('/')} className="inline-flex items-center gap-2 text-sm font-bold text-slate-400 hover:text-blue-600 mb-4 transition-colors">
              <ArrowLeft size={16} />
              Back to Patient Roster
            </button>
            <h1 className="text-3xl font-black text-slate-900 tracking-tight">Risk Stratification & Escalation</h1>
            <p className="text-sm font-medium text-slate-500 mt-2">Monitor patient risk tiers and trigger mandatory escalation workflows to prevent treatment abandonment.</p>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
            {[
              { label: 'Active Patients', value: stats.total,  icon: Users,         color: 'text-blue-600',   bg: 'bg-white' },
              { label: 'At Risk (Escalate)',value: stats.atRisk, icon: AlertOctagon,  color: 'text-rose-600',   bg: 'bg-rose-50' },
              { label: 'Safe (On Track)', value: stats.safe,   icon: ShieldCheck,   color: 'text-emerald-600', bg: 'bg-emerald-50' },
              { label: 'Month 3 Protected', value: stats.month3, icon: AlertTriangle, color: 'text-amber-600',  bg: 'bg-amber-50' },
            ].map((s, i) => (
              <div key={i} className={`stat-card ${s.bg} border border-slate-100 rounded-3xl p-6 flex flex-col items-start shadow-sm`}>
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-4 bg-white shadow-sm border border-slate-100/50 ${s.color}`}>
                  <s.icon size={20} />
                </div>
                <span className={`text-3xl font-black tracking-tight ${s.color}`}>{s.value}</span>
                <span className="text-xs font-bold uppercase tracking-widest text-slate-400 mt-1">{s.label}</span>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
            {/* Escalation Reset Rule */}
            <div className="info-block lg:col-span-1 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-3xl p-6 text-white shadow-lg shadow-blue-900/10 flex flex-col justify-center relative overflow-hidden">
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl -mr-10 -mt-10" />
              <div className="flex items-center gap-3 mb-3 relative z-10">
                <ShieldCheck size={20} className="text-blue-300" />
                <h3 className="font-bold tracking-wide">Escalation Reset Rule</h3>
              </div>
              <p className="text-sm text-blue-100 font-medium leading-relaxed relative z-10">
                If a provider logs a manual dose reconciliation (via the Patient Profile), the active alert clears and risk resets to baseline.
              </p>
            </div>

            {/* Tier legend */}
            <div className="info-block lg:col-span-2 bg-white border border-slate-100 rounded-3xl p-6 shadow-sm overflow-hidden flex flex-col justify-center">
              <p className="text-[10px] uppercase tracking-widest text-slate-400 font-bold mb-4">
                Escalation Tier System Matrix
              </p>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {(Object.entries(TIER_CONFIG) as [RiskTier, typeof TIER_CONFIG[RiskTier]][]).map(([, cfg]) => {
                  const parts = cfg.label.split(' — ');
                  const level = parts[0];
                  const response = parts.slice(1).join(' — ') || 'No action required';
                  return (
                    <div key={cfg.label} className="flex flex-col gap-1.5 p-3 rounded-2xl bg-slate-50 border border-slate-100">
                      <div className="flex items-center gap-2">
                        <span className={`w-2.5 h-2.5 rounded-full ${cfg.dotColor} shadow-sm shrink-0`} />
                        <span className="font-black text-slate-800 text-xs">{level}</span>
                      </div>
                      <span className="text-[10px] font-bold text-slate-500 tracking-wide uppercase">{response}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Escalation List */}
          <div className="info-block">
            <h2 className="text-lg font-black text-slate-900 tracking-tight mb-4 flex items-center gap-2">
              Action Required Queue
              <span className="bg-rose-100 text-rose-600 text-[10px] uppercase tracking-widest px-2 py-1 rounded-lg font-bold">Priority</span>
            </h2>
            
            <div className="flex flex-col gap-3">
              {/* Header row (hidden on mobile, visible on lg screens) */}
              <div className="hidden lg:grid grid-cols-[2fr_1fr_1.5fr_2fr_1.5fr] gap-4 px-6 py-2">
                {['Patient', 'Progress', 'Risk Status', 'Trigger Reason', 'Required Action'].map((h) => (
                  <span key={h} className="text-[10px] uppercase tracking-widest text-slate-400 font-bold">{h}</span>
                ))}
              </div>

              {sorted.map((patient) => {
                const tierCfg = TIER_CONFIG[patient.riskTier];
                return (
                  <div
                    key={patient.id}
                    onClick={() => navigate(`/patient/${patient.id}`)}
                    className="escalation-row group bg-white rounded-2xl p-5 border border-slate-100 shadow-[0_2px_10px_rgb(0,0,0,0.02)] hover:shadow-md hover:border-blue-100 cursor-pointer transition-all flex flex-col lg:grid lg:grid-cols-[2fr_1fr_1.5fr_2fr_1.5fr] gap-4 lg:items-center relative overflow-hidden"
                  >
                    {/* Hover indicator */}
                    <div className="absolute left-0 top-0 w-1 h-full bg-gradient-to-b from-blue-400 to-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity" />

                    {/* 1. Patient Name/ID */}
                    <div>
                      <p className="font-black text-slate-900 group-hover:text-blue-600 transition-colors text-base">{patient.name}</p>
                      <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wide mt-1">{patient.patientId} · {patient.ageProfile}</p>
                    </div>
                    
                    {/* 2. Progress */}
                    <div className="hidden lg:block">
                      <ProgressBar current={patient.currentDay} total={patient.totalDays} />
                    </div>

                    {/* 3. Risk Tier */}
                    <div className="flex flex-col gap-1.5 items-start">
                      <span className={`text-[10px] font-black px-2.5 py-1 rounded-lg border uppercase tracking-wider ${tierCfg.badgeClasses}`}>
                        {patient.riskTier === 'safe'  ? 'SAFE (NO RISK)'
                         : patient.riskTier === 'tier1' ? 'TIER 1 (LOW-MID)'
                         : patient.riskTier === 'tier2' ? 'TIER 2 (HIGH RISK)'
                         : 'TIER 3 (CRITICAL)'}
                      </span>
                      {patient.month3Protected && (
                        <span className="text-[9px] font-black px-2 py-1 rounded-lg border bg-amber-50 text-amber-700 border-amber-200 uppercase tracking-widest">
                          M3 OVERRIDE
                        </span>
                      )}
                    </div>

                    {/* 4. Trigger Reason */}
                    <div className="text-sm font-medium text-slate-600 leading-snug">
                      {patient.triggerReason}
                      <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-1">Last seen: {patient.lastActive}</p>
                    </div>

                    {/* 5. Action Button */}
                    <div className="mt-2 lg:mt-0 lg:justify-self-end" onClick={(e) => e.stopPropagation()}>
                      <ActionButton
                        patient={patient}
                        onCareMessage={() =>
                          setToast(`Care message queued for ${patient.name}. They will receive a ping on their device shortly.`)
                        }
                        onBHW={() => setBhwPatient(patient)}
                        onDOT={() => setDotPatient(patient)}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
          
        </div>
      </main>
    </div>
  );
}