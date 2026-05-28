import { AlertCircle, ArrowLeft, CheckCircle2, ChevronRight, Loader2, ShieldCheck, X, Zap } from 'lucide-react';
import { useEffect, useMemo, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import gsap from 'gsap';
import { ReconciliationMethodEnum } from '../api_types/Web_ReconcileAnomalyPayload';
import { usePatients } from '../context/PatientContext';
import { type AnomalousEntry } from '../data/mockData';
import { toAnomalousEntry } from '../services/adapters';

// ─── Status config ──────────────────────────────────────────────────────────

const STATUS_CONFIG = {
  'unverified-miss': {
    label: 'UNVERIFIED MISS (U)',
    classes: 'bg-rose-50 text-rose-700 border-rose-200',
    dot: 'bg-rose-500',
  },
  'tech-failure': {
    label: 'TECH FAILURE (T)',
    classes: 'bg-amber-50 text-amber-700 border-amber-200',
    dot: 'bg-amber-500',
  },
  'app-miss': {
    label: 'APP MISS (A)',
    classes: 'bg-blue-50 text-blue-700 border-blue-200',
    dot: 'bg-teal-500',
  },
};

const RECONCILED_CONFIG = {
  label: 'PROVIDER-VERIFIED (★)',
  classes: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  dot: 'bg-emerald-500',
};

// ─── Toast ──────────────────────────────────────────────────────────────────

function Toast({ onDismiss }: { onDismiss: () => void }) {
  useEffect(() => {
    const t = setTimeout(onDismiss, 5000);
    return () => clearTimeout(t);
  }, [onDismiss]);

  return (
    <div className="fixed top-6 left-1/2 -translate-x-1/2 z-[200] flex items-center gap-3 bg-emerald-600 text-white text-sm font-bold px-6 py-4 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.2)] border border-emerald-500 max-w-lg w-[90%] animate-in slide-in-from-top-4 fade-in">
      <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center shrink-0">
        <CheckCircle2 size={18} className="text-white" />
      </div>
      <span className="leading-snug flex-1">Reconciliation applied. Penalties reversed and streak restored.</span>
      <button onClick={onDismiss} className="text-emerald-200 hover:text-white transition-colors bg-black/10 hover:bg-black/20 p-1.5 rounded-full">
        <X size={16} />
      </button>
    </div>
  );
}

// ─── Reconcile Modal ────────────────────────────────────────────────────────

function ReconcileModal({
  entries,
  submitting,
  onClose,
  onConfirm,
}: {
  entries: AnomalousEntry[];
  submitting: boolean;
  onClose: () => void;
  onConfirm: (method: ReconciliationMethodEnum, reason: string) => void;
}) {
  const [method, setMethod] = useState(ReconciliationMethodEnum.HomeVisit);
  const [reason, setReason] = useState('');

  return (
    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden border border-slate-100 transform transition-all animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center gap-4 p-6 bg-gradient-to-br from-blue-50 to-blue-100/50 border-b border-blue-100">
          <div className="w-12 h-12 rounded-2xl bg-white shadow-sm border border-blue-200 flex items-center justify-center shrink-0">
            <ShieldCheck size={24} className="text-blue-600" />
          </div>
          <div>
            <h3 className="font-black text-slate-900 text-xl tracking-tight">Confirm Override</h3>
            <p className="text-[10px] font-bold uppercase tracking-widest text-blue-600 mt-1">
              Provider-Verified Stamp
            </p>
          </div>
        </div>

        <div className="p-8 space-y-6">
          {/* Selected dates */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-3">
              Selected Dates
            </label>
            <div className="flex flex-wrap gap-2">
              {entries.map((e) => (
                <span
                  key={e.id}
                  className="text-xs font-bold bg-slate-100 text-slate-700 px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm"
                >
                  {e.date}
                </span>
              ))}
            </div>
          </div>

          {/* Verification method */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">
              Verification Method
            </label>
            <select
              value={method}
              onChange={(e) => setMethod(e.target.value as ReconciliationMethodEnum)}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3.5 text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all appearance-none"
              style={{ backgroundImage: `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e")`, backgroundPosition: 'right 0.5rem center', backgroundRepeat: 'no-repeat', backgroundSize: '1.5em 1.5em' }}
            >
              <option value={ReconciliationMethodEnum.HomeVisit}>BHW Home Visit Confirmed</option>
              <option value={ReconciliationMethodEnum.DotOrder}>In-Person DOT Order</option>
              <option value={ReconciliationMethodEnum.SendMessage}>Direct Patient Message</option>
            </select>
          </div>

          {/* Reason */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">
              Reason for Override
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. Physical logbook reviewed, connectivity issues confirmed..."
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium text-slate-700 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 resize-none transition-all"
            />
          </div>
        </div>

        {/* Actions */}
        <div className="p-6 pt-0 flex gap-3 bg-slate-50/50 border-t border-slate-50">
          <button
            onClick={onClose}
            disabled={submitting}
            className="flex-1 border border-slate-200 bg-white text-slate-600 text-sm font-bold py-3.5 rounded-xl hover:bg-slate-50 hover:text-slate-900 disabled:opacity-40 transition-all shadow-sm"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(method, reason)}
            disabled={!reason.trim() || submitting}
            className="flex-[2] bg-emerald-600 text-white text-sm font-bold py-3.5 rounded-xl hover:bg-emerald-700 disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2 shadow-lg shadow-emerald-600/20"
          >
            {submitting ? (
              <>
                <Loader2 size={16} className="animate-spin" />
                Applying Stamp…
              </>
            ) : (
              'Apply Verified Stamp'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Page ───────────────────────────────────────────────────────────────────

export default function DoseReconciliation() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const numericId = Number(id);
  const { patientBundles, ensurePatientBundle, reconcilePatientAnomalies } = usePatients();
  const bundle = Number.isNaN(numericId) ? null : patientBundles[numericId] ?? null;

  const [submitting, setSubmitting] = useState(false);
  const fetchError = bundle?.error ?? '';
  const containerRef = useRef<HTMLDivElement>(null);

  // ── UI state ─────────────────────────────────────────────────────────────
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [showModal, setShowModal] = useState(false);
  const [reconciled, setReconciled] = useState<Set<string>>(new Set());
  const [showToast, setShowToast] = useState(false);
  const [liveStreak, setLiveStreak] = useState(0);
  const [baseStreak, setBaseStreak] = useState(0);

  const patientDetail = bundle?.detail ?? null;
  const loading = Number.isNaN(numericId) ? false : (!bundle || bundle.detailLoading || bundle.anomaliesLoading);

  const [entries, rawIds] = useMemo(() => {
    const response = bundle?.anomalies;
    if (!response) return [[], new Map<string, number>()] as const;

    const idMap = new Map<string, number>();
    const adapted = response.entries.map((entry) => {
      const local = toAnomalousEntry(entry);
      idMap.set(local.id, entry.id);
      return local;
    });

    return [adapted, idMap] as const;
  }, [bundle?.anomalies]);

  useEffect(() => {
    if (Number.isNaN(numericId)) return;
    void ensurePatientBundle(numericId);
  }, [numericId, ensurePatientBundle]);

  useEffect(() => {
    if (patientDetail) {
      setLiveStreak(0);
      setBaseStreak(0);
    }
  }, [patientDetail]);

  // GSAP Animations
  useEffect(() => {
    if (!loading && !fetchError && patientDetail) {
      const ctx = gsap.context(() => {
        gsap.fromTo('.animate-header', { y: -15, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, ease: 'power3.out' });
        gsap.fromTo('.animate-card', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, stagger: 0.1, ease: 'power3.out', delay: 0.1 });
        gsap.fromTo('.reconcile-row', { y: 15, opacity: 0 }, { y: 0, opacity: 1, duration: 0.4, stagger: 0.05, ease: 'power2.out', delay: 0.3 });
      }, containerRef);
      return () => ctx.revert();
    }
  }, [loading, fetchError, patientDetail]);

  // ── Helpers ──────────────────────────────────────────────────────────────
  const toggle = (entryId: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(entryId) ? next.delete(entryId) : next.add(entryId);
      return next;
    });
  };

  const toggleAll = () => {
    if (selected.size === pending.length && pending.length > 0) {
      setSelected(new Set()); // Deselect all
    } else {
      setSelected(new Set(pending.map(e => e.id))); // Select all
    }
  };

  const handleConfirm = async (method: ReconciliationMethodEnum, reason: string) => {
    const entryIds: number[] = [];
    for (const localId of selected) {
      const rawId = rawIds.get(localId);
      if (rawId !== undefined) entryIds.push(rawId);
    }

    setSubmitting(true);
    try {
      const result = await reconcilePatientAnomalies(numericId, {
        entry_ids: entryIds,
        verification_method: method,
        reason,
      });

      setReconciled((prev) => new Set([...prev, ...selected]));
      setSelected(new Set());
      setShowModal(false);
      setShowToast(true);
      setLiveStreak(result.updated_streak);
    } catch {
      // Keep modal open
    } finally {
      setSubmitting(false);
    }
  };

  // ── Derived ──────────────────────────────────────────────────────────────
  const selectedEntries = entries.filter((e) => selected.has(e.id));
  const pending = entries.filter((e) => !reconciled.has(e.id));
  const reconciledEntries = entries.filter((e) => reconciled.has(e.id));
  const streakChanged = liveStreak > baseStreak;

  // ── Loading ──────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-4 text-slate-500">
          <Loader2 size={36} className="animate-spin text-blue-500" />
          <p className="text-sm font-bold uppercase tracking-widest">Loading Reconciliations…</p>
        </div>
      </div>
    );
  }

  // ── Error ─────────────────────────────────────────────────────────────────
  if (fetchError || !patientDetail) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-8">
        <div className="text-center bg-white p-10 rounded-3xl shadow-sm border border-slate-100">
          <AlertCircle size={48} className="mx-auto mb-4 text-rose-500" />
          <p className="font-black text-slate-800 text-lg mb-2">Unable to load data</p>
          <p className="text-sm font-medium text-slate-500 mb-6">{fetchError || 'Patient profile not found.'}</p>
          <button
            onClick={() => navigate('/')}
            className="text-sm text-blue-600 font-bold hover:text-blue-800 transition-colors"
          >
            ← Return to Patient Roster
          </button>
        </div>
      </div>
    );
  }

  const patientName = `${patientDetail.firstname} ${patientDetail.lastname}`;
  const allSelected = pending.length > 0 && selected.size === pending.length;

  return (
    <div className="min-h-screen bg-slate-50 relative font-sans">
      
      {/* Toast */}
      {showToast && <Toast onDismiss={() => setShowToast(false)} />}

      {/* Top bar */}
      <div className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-10 shadow-sm">
        <button
          onClick={() => navigate(`/patient/${id}`)}
          className="flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-blue-600 transition-colors"
        >
          <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center">
            <ArrowLeft size={16} />
          </div>
          Patient Profile
        </button>
        <button
          onClick={() => navigate('/')}
          className="text-xs font-bold uppercase tracking-widest text-slate-400 hover:text-slate-600 transition-colors"
        >
          Roster
        </button>
      </div>

      <div className="max-w-4xl mx-auto p-6 md:p-10" ref={containerRef}>
        
        {/* Page title */}
        <div className="animate-header mb-8">
          <h1 className="text-3xl font-black text-slate-900 tracking-tight">Dose Reconciliation</h1>
          <p className="text-sm font-medium text-slate-500 mt-2">
            Review and verify unlogged doses to prevent automated penalty escalation.
          </p>
        </div>

        {/* Notice banner */}
        <div className="animate-card flex items-start gap-3 bg-blue-50 border border-blue-100 rounded-3xl p-6 mb-8">
          <div className="w-10 h-10 rounded-xl bg-blue-100 flex items-center justify-center shrink-0">
            <ShieldCheck size={20} className="text-blue-600" />
          </div>
          <p className="text-sm font-medium text-blue-800 leading-relaxed pt-1">
            Reconciled doses are retroactively counted in the patient's PDC (Proportion of Days Covered) and automatically remove applied penalties.{' '}
            <strong className="font-black block mt-1">Every provider action is logged securely with a timestamp.</strong>
          </p>
        </div>

        {/* Patient info card */}
        <div className="animate-card bg-white border border-slate-100 rounded-3xl p-6 md:p-8 mb-8 shadow-sm">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Patient Name</p>
              <p className="font-black text-slate-900 text-lg">{patientName}</p>
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Patient ID</p>
              <p className="font-bold text-slate-600 text-lg">#{patientDetail.id}</p>
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Contact</p>
              <p className="text-sm font-bold text-slate-800">{patientDetail.contact || '—'}</p>
              <p className="text-[11px] font-medium text-slate-400 mt-0.5">{patientDetail.email || ''}</p>
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Regimen Start</p>
              <p className="text-sm font-bold text-slate-800">
                {patientDetail.regimen_start
                  ? new Date(patientDetail.regimen_start).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
                  : '—'}
              </p>
            </div>
          </div>

          <div className="mt-6 pt-6 border-t border-slate-100 flex flex-col md:flex-row md:items-center gap-6 md:gap-12">
            {/* Live streak */}
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">Current Streak</p>
              <div className="flex items-center gap-2">
                <div className={`w-8 h-8 rounded-lg flex items-center justify-center transition-colors ${streakChanged ? 'bg-emerald-100' : 'bg-amber-100'}`}>
                  <Zap size={16} className={`transition-colors ${streakChanged ? 'text-emerald-500 fill-emerald-400' : 'text-amber-500 fill-amber-400'}`} />
                </div>
                <span className={`font-black text-2xl tracking-tighter transition-colors ${streakChanged ? 'text-emerald-600' : 'text-slate-900'}`}>
                  {liveStreak}
                </span>
                <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">days</span>
                {streakChanged && (
                  <span className="text-[10px] bg-emerald-50 border border-emerald-200 text-emerald-600 font-bold px-2 py-1 rounded-md uppercase tracking-wider ml-2 shadow-sm animate-in zoom-in">
                    ↑ Restored
                  </span>
                )}
              </div>
            </div>

            {/* Treatment progress */}
            <div className="flex-1 max-w-xs">
              <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">Treatment Progress</p>
              <div className="flex flex-col gap-1.5">
                <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden border border-slate-200">
                  <div className="h-full bg-blue-500 rounded-full transition-all duration-1000 ease-out" style={{ width: `${(patientDetail.current_day / patientDetail.total_days) * 100}%` }} />
                </div>
                <span className="text-[10px] font-black text-blue-600 uppercase tracking-widest">
                  Day {patientDetail.current_day} of {patientDetail.total_days}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Classification Legend */}
        <div className="animate-card bg-white border border-slate-100 rounded-2xl p-5 mb-8 shadow-sm">
          <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-3">
            Anomaly Classifications
          </p>
          <div className="flex flex-wrap gap-4 text-xs font-bold uppercase tracking-wider">
            {[
              { color: 'bg-amber-400', label: 'Technical Miss (T)' },
              { color: 'bg-teal-400', label: 'App Miss (A)' },
              { color: 'bg-rose-500', label: 'Unverified Miss (U)' },
              { color: 'bg-emerald-500', label: 'Provider-Verified (★)' },
            ].map((l) => (
              <div key={l.label} className="flex items-center gap-2 bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-100">
                <span className={`w-2.5 h-2.5 rounded-full ${l.color} shadow-sm`} />
                <span className="text-slate-600">{l.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Anomalous entries table */}
        <div className="animate-card bg-white border border-slate-100 rounded-3xl overflow-hidden mb-8 shadow-sm">
          
          {/* Table header bar */}
          <div className="flex flex-col md:flex-row md:items-center justify-between p-6 bg-slate-50/50 border-b border-slate-100 gap-4">
            <div>
              <h2 className="font-black text-slate-900 text-lg">Action Required Queue</h2>
              <p className="text-xs font-medium text-slate-500 mt-1">Select missing logs to manually override via BYPASS.</p>
            </div>
            <button
              onClick={() => selected.size > 0 && setShowModal(true)}
              disabled={selected.size === 0}
              className="flex items-center justify-center gap-2 bg-[#0A0F24] text-white text-sm font-bold px-6 py-3 rounded-xl disabled:opacity-40 disabled:bg-slate-300 disabled:text-slate-500 hover:bg-blue-900 transition-all shadow-md"
            >
              Reconcile Selected
              {selected.size > 0 && (
                <span className="bg-white/20 text-white text-[11px] px-2 py-0.5 rounded-md">
                  {selected.size}
                </span>
              )}
            </button>
          </div>

          {/* Column headers */}
          <div className="hidden md:grid grid-cols-[40px_1.5fr_2fr_2fr] gap-4 px-6 py-3 bg-white border-b border-slate-100 items-center">
            <button 
              onClick={toggleAll}
              disabled={pending.length === 0}
              className={`w-5 h-5 rounded-md border-2 flex items-center justify-center transition-all disabled:opacity-50 ${allSelected ? 'bg-blue-600 border-blue-600' : 'border-slate-300 hover:border-blue-400'}`}
            >
               {allSelected && <CheckCircle2 size={12} className="text-white" />}
            </button>
            {['Date', 'Status Matrix', 'System Diagnosis'].map((h) => (
              <span key={h} className="text-[10px] font-bold uppercase tracking-widest text-slate-400">
                {h}
              </span>
            ))}
          </div>

          {/* Pending rows */}
          {pending.length === 0 && reconciledEntries.length === 0 ? (
            <div className="py-16 text-center">
              <div className="w-16 h-16 bg-emerald-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-emerald-100">
                <ShieldCheck size={32} className="text-emerald-500" />
              </div>
              <p className="font-black text-slate-800 text-lg">Fully Reconciled</p>
              <p className="text-sm font-medium text-slate-500 mt-1">This patient has no pending entries requiring action.</p>
            </div>
          ) : (
            <div className="divide-y divide-slate-50">
              {pending.map((entry) => {
                const cfg = STATUS_CONFIG[entry.statusBadge];
                const isSelected = selected.has(entry.id);
                return (
                  <div
                    key={entry.id}
                    onClick={() => toggle(entry.id)}
                    className={`reconcile-row group flex flex-col md:grid md:grid-cols-[40px_1.5fr_2fr_2fr] gap-3 md:gap-4 items-start md:items-center p-5 cursor-pointer transition-all ${
                      isSelected ? 'bg-blue-50/50' : 'hover:bg-slate-50'
                    }`}
                  >
                    {/* Checkbox */}
                    <div className="hidden md:flex w-5 h-5 rounded-md border-2 items-center justify-center transition-all group-hover:border-blue-400"
                         style={{ borderColor: isSelected ? '#2563eb' : '', backgroundColor: isSelected ? '#2563eb' : '' }}>
                      {isSelected && <CheckCircle2 size={14} className="text-white" />}
                    </div>
                    
                    <div className="flex items-center gap-3 md:hidden w-full mb-1">
                      <div className={`w-5 h-5 rounded-md border-2 flex items-center justify-center transition-all ${isSelected ? 'bg-blue-600 border-blue-600' : 'border-slate-300'}`}>
                        {isSelected && <CheckCircle2 size={14} className="text-white" />}
                      </div>
                      <span className="font-black text-slate-900 text-sm">{entry.date}</span>
                    </div>

                    <span className="hidden md:block font-black text-slate-900 text-sm group-hover:text-blue-600 transition-colors">{entry.date}</span>
                    
                    <span className={`text-[10px] font-black uppercase tracking-widest px-2.5 py-1 rounded-lg border w-fit ${cfg.classes}`}>
                      {cfg.label}
                    </span>
                    
                    <span className="text-sm font-medium text-slate-600 leading-snug">{entry.detectedCause}</span>
                  </div>
                );
              })}

              {/* Reconciled rows */}
              {reconciledEntries.map((entry) => (
                <div
                  key={entry.id}
                  className="reconcile-row flex flex-col md:grid md:grid-cols-[40px_1.5fr_2fr_2fr] gap-3 md:gap-4 items-start md:items-center p-5 bg-emerald-50/30"
                >
                  <div className="hidden md:flex w-5 h-5 rounded-md border-2 bg-emerald-500 border-emerald-500 items-center justify-center shadow-sm">
                    <CheckCircle2 size={14} className="text-white" />
                  </div>
                  
                  <div className="flex items-center gap-3 md:hidden w-full mb-1">
                     <div className="w-5 h-5 rounded-md border-2 bg-emerald-500 border-emerald-500 flex items-center justify-center">
                        <CheckCircle2 size={14} className="text-white" />
                     </div>
                     <span className="font-bold text-slate-400 text-sm line-through decoration-slate-300">{entry.date}</span>
                  </div>

                  <span className="hidden md:block font-bold text-slate-400 text-sm line-through decoration-slate-300">{entry.date}</span>
                  
                  <span className={`text-[10px] font-black uppercase tracking-widest px-2.5 py-1 rounded-lg border w-fit ${RECONCILED_CONFIG.classes}`}>
                    {RECONCILED_CONFIG.label}
                  </span>
                  
                  <span className="text-sm font-bold text-emerald-700/70 italic">Reconciled successfully this session</span>
                </div>
              ))}
            </div>
          )}

          {/* Summary footer */}
          {reconciled.size > 0 && (
            <div className="px-6 py-4 bg-emerald-50 border-t border-emerald-100 flex items-center gap-3">
              <div className="w-8 h-8 bg-emerald-100 rounded-full flex items-center justify-center shrink-0">
                <CheckCircle2 size={16} className="text-emerald-600" />
              </div>
              <p className="text-xs text-emerald-800 font-bold leading-relaxed">
                {reconciled.size} entr{reconciled.size === 1 ? 'y' : 'ies'} successfully reconciled. Automated penalties reversed and streak logic restored.
              </p>
            </div>
          )}
        </div>

        {/* Continue */}
        <button
          onClick={() => navigate(`/patient/${id}`)}
          className="animate-card flex items-center justify-center gap-2 w-full md:w-auto bg-white border border-slate-200 text-sm font-bold text-slate-600 px-6 py-4 rounded-2xl hover:bg-slate-50 hover:text-slate-900 transition-all shadow-sm mx-auto"
        >
          Return to Patient Dashboard
          <ChevronRight size={16} className="text-slate-400" />
        </button>
      </div>

      {/* Modal */}
      {showModal && (
        <ReconcileModal
          entries={selectedEntries}
          submitting={submitting}
          onClose={() => setShowModal(false)}
          onConfirm={handleConfirm}
        />
      )}
    </div>
  );
}