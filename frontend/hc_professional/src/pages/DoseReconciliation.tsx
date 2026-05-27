import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, ShieldCheck, ChevronRight, Zap, CheckCircle2, X, Loader2, AlertCircle } from 'lucide-react';
import { type AnomalousEntry } from '../data/mockData';
import { getPatient, getAnomalousEntries, reconcileAnomalies } from '../services/api';
import { toAnomalousEntry } from '../services/adapters';
import type { WebPatientDetailResponse } from '../api_types/Web_PatientDetailResponse';

// ─── Status config ──────────────────────────────────────────────────────────

const STATUS_CONFIG = {
  'unverified-miss': {
    label: 'UNVERIFIED MISS (U)',
    classes: 'bg-red-100 text-red-700 border-red-200',
    dot: 'bg-red-400',
  },
  'tech-failure': {
    label: 'TECH FAILURE (T)',
    classes: 'bg-yellow-100 text-yellow-700 border-yellow-200',
    dot: 'bg-yellow-400',
  },
  'app-miss': {
    label: 'APP MISS (A)',
    classes: 'bg-blue-100 text-blue-700 border-blue-200',
    dot: 'bg-teal-400',
  },
};

const RECONCILED_CONFIG = {
  label: 'PROVIDER-VERIFIED (★)',
  classes: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  dot: 'bg-emerald-400',
};

// ─── Toast ──────────────────────────────────────────────────────────────────

function Toast({ onDismiss }: { onDismiss: () => void }) {
  useEffect(() => {
    const t = setTimeout(onDismiss, 5000);
    return () => clearTimeout(t);
  }, [onDismiss]);

  return (
    <div className="fixed top-5 left-1/2 -translate-x-1/2 z-[200] flex items-center gap-3 bg-emerald-600 text-white text-sm font-semibold px-5 py-4 rounded-xl shadow-2xl max-w-lg w-[90%]">
      <CheckCircle2 size={22} className="shrink-0" />
      <span>Reconciliation applied. Penalties reversed. Streak restored.</span>
      <button onClick={onDismiss} className="ml-auto text-white/70 hover:text-white transition-colors">
        <X size={14} />
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
  onConfirm: (method: string, reason: string) => void;
}) {
  const [method, setMethod] = useState('Home Visit');
  const [reason, setReason] = useState('');

  return (
    <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden border border-blue-100">
        {/* Header */}
        <div className="flex items-center gap-3 p-6 bg-blue-50 border-b border-blue-100">
          <div className="w-10 h-10 rounded-full bg-white border-2 border-blue-200 flex items-center justify-center shrink-0">
            <ShieldCheck size={18} className="text-blue-600" />
          </div>
          <div>
            <h3 className="font-bold text-gray-900">Confirm Reconciliation</h3>
            <p className="text-[11px] font-semibold uppercase tracking-wider text-blue-500">
              Provider-Verified Stamp
            </p>
          </div>
        </div>

        <div className="p-6 space-y-4">
          {/* Selected dates */}
          <div>
            <label className="block text-[11px] uppercase tracking-wider text-gray-400 mb-2">
              Selected Dates for Override
            </label>
            <div className="flex flex-wrap gap-2">
              {entries.map((e) => (
                <span
                  key={e.id}
                  className="text-sm font-medium bg-gray-100 text-gray-700 px-3 py-1 rounded-full"
                >
                  {e.date}
                </span>
              ))}
            </div>
          </div>

          {/* Verification method */}
          <div>
            <label className="block text-[11px] uppercase tracking-wider text-gray-400 mb-2">
              Verification Method
            </label>
            <select
              value={method}
              onChange={(e) => setMethod(e.target.value)}
              className="w-full bg-gray-50 border-2 border-gray-200 rounded-xl px-3 py-2.5 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300 transition-shadow"
            >
              <option>Home Visit</option>
              <option>Clinic Visit</option>
              <option>Phone Verification</option>
              <option>Physical Treatment Card</option>
            </select>
          </div>

          {/* Reason */}
          <div>
            <label className="block text-[11px] uppercase tracking-wider text-gray-400 mb-2">
              Reason for Override
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. home visit confirmed, physical record reviewed"
              className="w-full bg-gray-50 border-2 border-gray-200 rounded-xl px-3 py-2.5 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300 resize-none transition-shadow"
            />
          </div>
        </div>

        {/* Actions */}
        <div className="p-6 pt-0 flex gap-3">
          <button
            onClick={onClose}
            disabled={submitting}
            className="flex-1 border border-gray-200 text-gray-600 text-sm font-medium py-3 rounded-xl hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(method, reason)}
            disabled={!reason.trim() || submitting}
            className="flex-[2] bg-emerald-600 text-white text-sm font-semibold py-3 rounded-xl hover:bg-emerald-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            {submitting ? (
              <>
                <Loader2 size={15} className="animate-spin" />
                Applying…
              </>
            ) : (
              'Apply Stamp'
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

  // ── API state ────────────────────────────────────────────────────────────
  const [patientDetail, setPatientDetail] = useState<WebPatientDetailResponse | null>(null);
  const [entries, setEntries] = useState<AnomalousEntry[]>([]);
  // Map<localStringId, rawNumericId> — needed to build entry_ids payload
  const [rawIds, setRawIds] = useState<Map<string, number>>(new Map());
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [fetchError, setFetchError] = useState('');

  // ── UI state ─────────────────────────────────────────────────────────────
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [showModal, setShowModal] = useState(false);
  const [reconciled, setReconciled] = useState<Set<string>>(new Set());
  const [showToast, setShowToast] = useState(false);
  const [liveStreak, setLiveStreak] = useState(0);
  const [baseStreak, setBaseStreak] = useState(0);

  // ── Fetch on mount ───────────────────────────────────────────────────────
  useEffect(() => {
    if (!numericId) return;
    let cancelled = false;
    setLoading(true);
    setFetchError('');

    Promise.all([
      getPatient(numericId),
      getAnomalousEntries(numericId),
    ])
      .then(([detail, anomalousRes]) => {
        if (cancelled) return;
        setPatientDetail(detail);

        // Build local entries + rawIds map
        const idMap = new Map<string, number>();
        const adapted = anomalousRes.entries.map((e) => {
          const local = toAnomalousEntry(e);
          idMap.set(local.id, e.id);
          return local;
        });
        setEntries(adapted);
        setRawIds(idMap);
      })
      .catch(() => {
        if (!cancelled) setFetchError('Failed to load patient data. Please go back and try again.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => { cancelled = true; };
  }, [numericId]);

  // Sync liveStreak once patient loads (gamification not fetched here — use
  // current_day as a proxy; real streak comes from reconcile response)
  useEffect(() => {
    if (patientDetail) {
      // We don't have streak from the detail endpoint; default 0 until reconcile updates it
      setLiveStreak(0);
      setBaseStreak(0);
    }
  }, [patientDetail]);

  // ── Helpers ──────────────────────────────────────────────────────────────
  const toggle = (entryId: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(entryId) ? next.delete(entryId) : next.add(entryId);
      return next;
    });
  };

  const handleConfirm = async (method: string, reason: string) => {
    // Build numeric entry_ids from the rawIds map
    const entryIds: number[] = [];
    for (const localId of selected) {
      const rawId = rawIds.get(localId);
      if (rawId !== undefined) entryIds.push(rawId);
    }

    setSubmitting(true);
    try {
      const result = await reconcileAnomalies(numericId, {
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
      // Keep modal open — user can retry or cancel
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
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3 text-gray-400">
          <Loader2 size={32} className="animate-spin text-blue-500" />
          <p className="text-sm font-medium">Loading reconciliation data…</p>
        </div>
      </div>
    );
  }

  // ── Error ─────────────────────────────────────────────────────────────────
  if (fetchError || !patientDetail) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-8">
        <div className="text-center">
          <AlertCircle size={40} className="mx-auto mb-3 text-red-400" />
          <p className="font-semibold text-gray-700 mb-1">Unable to load data</p>
          <p className="text-sm text-gray-400 mb-4">{fetchError || 'Patient not found.'}</p>
          <button
            onClick={() => navigate('/')}
            className="text-sm text-blue-600 font-medium hover:text-blue-700 transition-colors"
          >
            ← Back to Roster
          </button>
        </div>
      </div>
    );
  }

  const patientName = `${patientDetail.firstname} ${patientDetail.lastname}`;

  return (
    <div className="min-h-screen bg-gray-50 relative">
      {/* Toast */}
      {showToast && <Toast onDismiss={() => setShowToast(false)} />}

      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between sticky top-0 z-10">
        <button
          onClick={() => navigate(`/patient/${id}`)}
          className="flex items-center gap-2 text-sm text-gray-600 hover:text-blue-600 transition-colors"
        >
          <ArrowLeft size={16} />
          Back to Patient Record
        </button>
        <button
          onClick={() => navigate('/')}
          className="text-sm text-gray-400 hover:text-gray-600 transition-colors"
        >
          Back to Roster
        </button>
      </div>

      <div className="max-w-3xl mx-auto p-8">
        {/* Page title */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Dose Reconciliation</h1>
          <p className="text-sm text-gray-400 mt-0.5">
            Review and verify unlogged doses to prevent patient penalties.
          </p>
        </div>

        {/* Notice banner */}
        <div className="flex items-start gap-3 bg-blue-50 border border-blue-100 rounded-xl p-4 mb-6">
          <ShieldCheck size={16} className="text-blue-500 mt-0.5 shrink-0" />
          <p className="text-sm text-blue-700 leading-relaxed">
            Reconciled doses are retroactively counted in the patient's PDC (Proportion of Days Covered) and
            remove any applied penalty.{' '}
            <strong>Provider action is logged with timestamp.</strong>
          </p>
        </div>

        {/* Patient info card */}
        <div className="bg-white border border-gray-100 rounded-xl p-5 mb-6">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Patient Name</p>
              <p className="font-semibold text-gray-900">{patientName}</p>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Patient ID</p>
              <p className="font-medium text-gray-700">#{patientDetail.id}</p>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Contact</p>
              <p className="text-sm text-gray-700">{patientDetail.contact || '—'}</p>
              <p className="text-xs text-gray-400">{patientDetail.email || ''}</p>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Regimen Start</p>
              <p className="text-sm text-gray-700">
                {patientDetail.regimen_start
                  ? new Date(patientDetail.regimen_start).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                      year: 'numeric',
                    })
                  : '—'}
              </p>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-gray-100 flex items-center gap-6">
            {/* Live streak */}
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Current Streak</p>
              <div className="flex items-center gap-1.5">
                <Zap
                  size={15}
                  className={`transition-colors ${streakChanged ? 'text-green-500 fill-green-400' : 'text-yellow-500 fill-yellow-400'}`}
                />
                <span
                  className={`font-bold text-xl leading-none transition-colors ${streakChanged ? 'text-green-600' : 'text-gray-900'}`}
                >
                  {liveStreak}
                </span>
                <span className="text-xs text-gray-400 ml-0.5">days</span>
                {streakChanged && (
                  <span className="text-[11px] text-green-600 font-semibold ml-1">↑ Restored</span>
                )}
              </div>
            </div>

            {/* Treatment progress */}
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Treatment Progress</p>
              <div className="flex items-center gap-2">
                <div className="w-24 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-blue-500 rounded-full"
                    style={{ width: `${(patientDetail.current_day / patientDetail.total_days) * 100}%` }}
                  />
                </div>
                <span className="text-sm font-medium text-gray-700">
                  Day {patientDetail.current_day} of {patientDetail.total_days}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Classification Legend */}
        <div className="bg-white border border-gray-100 rounded-xl p-4 mb-6">
          <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-3">
            Classification Legend (Gate 0)
          </p>
          <div className="flex flex-wrap gap-4 text-xs font-medium">
            {[
              { color: 'bg-yellow-400', label: 'Technical Miss (T)' },
              { color: 'bg-teal-400', label: 'App Miss (A)' },
              { color: 'bg-red-400', label: 'Unverified Miss (U)' },
              { color: 'bg-emerald-500', label: 'Provider-Verified (★)' },
            ].map((l) => (
              <div key={l.label} className="flex items-center gap-1.5">
                <span className={`w-2.5 h-2.5 rounded-full ${l.color}`} />
                <span className="text-gray-600">{l.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Anomalous entries table */}
        <div className="bg-white border border-gray-100 rounded-xl overflow-hidden mb-6">
          {/* Table header bar */}
          <div className="flex items-center justify-between px-5 py-4 bg-gray-50 border-b border-gray-100">
            <div>
              <h2 className="font-semibold text-gray-900">Anomalous Entries Requiring Action</h2>
              <p className="text-xs text-gray-400 mt-0.5">Select entries below to manually override via BYPASS.</p>
            </div>
            <button
              onClick={() => selected.size > 0 && setShowModal(true)}
              disabled={selected.size === 0}
              className="flex items-center gap-1.5 bg-blue-600 text-white text-sm font-medium px-4 py-2 rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
            >
              Reconcile Selected
              {selected.size > 0 && (
                <span className="bg-white/30 text-white text-[11px] font-bold px-1.5 py-0.5 rounded-full">
                  {selected.size}
                </span>
              )}
            </button>
          </div>

          {/* Column headers */}
          <div className="grid grid-cols-[32px_1fr_180px_1fr] gap-4 px-5 py-2.5 bg-white border-b border-gray-100">
            <span />
            {['Date', 'Status Badge', 'Detected Cause'].map((h) => (
              <span key={h} className="text-[10px] uppercase tracking-wider text-gray-400 font-semibold">
                {h}
              </span>
            ))}
          </div>

          {/* Pending rows */}
          {pending.length === 0 && reconciledEntries.length === 0 ? (
            <div className="py-12 text-center text-gray-400">
              <ShieldCheck size={32} className="mx-auto mb-2 text-green-400" />
              <p className="font-medium text-gray-600">No anomalous entries</p>
              <p className="text-sm">This patient has no pending entries to reconcile.</p>
            </div>
          ) : (
            <>
              {pending.map((entry) => {
                const cfg = STATUS_CONFIG[entry.statusBadge];
                const isSelected = selected.has(entry.id);
                return (
                  <div
                    key={entry.id}
                    onClick={() => toggle(entry.id)}
                    className={`grid grid-cols-[32px_1fr_180px_1fr] gap-4 items-center px-5 py-3.5 border-b border-gray-50 cursor-pointer transition-colors ${
                      isSelected ? 'bg-blue-50' : 'hover:bg-gray-50'
                    }`}
                  >
                    {/* Checkbox */}
                    <div
                      className={`w-4 h-4 rounded border-2 flex items-center justify-center transition-colors ${
                        isSelected ? 'bg-blue-600 border-blue-600' : 'border-gray-300'
                      }`}
                    >
                      {isSelected && (
                        <svg viewBox="0 0 10 8" className="w-2.5 h-2" fill="none" stroke="white" strokeWidth={2}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M1 4l3 3 5-4" />
                        </svg>
                      )}
                    </div>
                    <span className="font-medium text-gray-700 text-sm">{entry.date}</span>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded border w-fit ${cfg.classes}`}>
                      {cfg.label}
                    </span>
                    <span className="text-sm text-gray-500">{entry.detectedCause}</span>
                  </div>
                );
              })}

              {/* Reconciled rows */}
              {reconciledEntries.map((entry) => (
                <div
                  key={entry.id}
                  className="grid grid-cols-[32px_1fr_180px_1fr] gap-4 items-center px-5 py-3.5 border-b border-gray-50 bg-emerald-50/40"
                >
                  <div className="w-4 h-4 rounded border-2 bg-emerald-600 border-emerald-600 flex items-center justify-center">
                    <svg viewBox="0 0 10 8" className="w-2.5 h-2" fill="none" stroke="white" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M1 4l3 3 5-4" />
                    </svg>
                  </div>
                  <span className="font-medium text-gray-400 text-sm line-through">{entry.date}</span>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border w-fit ${RECONCILED_CONFIG.classes}`}>
                    {RECONCILED_CONFIG.label}
                  </span>
                  <span className="text-sm text-gray-400 italic">Reconciled this session</span>
                </div>
              ))}
            </>
          )}

          {/* Summary footer */}
          {reconciled.size > 0 && (
            <div className="px-5 py-3 bg-emerald-50 border-t border-emerald-100 flex items-center gap-2">
              <CheckCircle2 size={13} className="text-emerald-600" />
              <p className="text-xs text-emerald-700 font-medium">
                {reconciled.size} entr{reconciled.size === 1 ? 'y' : 'ies'} reconciled — penalties reversed and streak restored.
              </p>
            </div>
          )}
        </div>

        {/* Continue */}
        <button
          onClick={() => navigate(`/patient/${id}`)}
          className="flex items-center gap-2 text-sm text-blue-600 font-medium hover:text-blue-700 transition-colors"
        >
          View Full Adherence Record
          <ChevronRight size={14} />
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
