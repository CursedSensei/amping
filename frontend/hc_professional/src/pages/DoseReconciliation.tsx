import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, ShieldCheck, ChevronRight, Zap } from 'lucide-react';
import { MOCK_PATIENTS, type AnomalousEntry } from '../data/mockData';

const STATUS_CONFIG = {
  'unverified-miss': {
    label: 'UNVERIFIED MISS (U)',
    classes: 'bg-red-100 text-red-700 border-red-200',
  },
  'tech-failure': {
    label: 'TECH FAILURE (T)',
    classes: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  },
  'app-miss': {
    label: 'APP MISS (A)',
    classes: 'bg-blue-100 text-blue-700 border-blue-200',
  },
};

function ReconcileModal({
  entries,
  onClose,
  onConfirm,
}: {
  entries: AnomalousEntry[];
  onClose: () => void;
  onConfirm: (method: string, reason: string) => void;
}) {
  const [method, setMethod] = useState('Home Visit');
  const [reason, setReason] = useState('');

  return (
    <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-md shadow-2xl">
        {/* Header */}
        <div className="p-6 border-b border-gray-100">
          <div className="flex items-center gap-3 mb-1">
            <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
              <ShieldCheck size={16} className="text-blue-600" />
            </div>
            <div>
              <h3 className="font-bold text-gray-900">Confirm Reconciliation</h3>
              <p className="text-[11px] font-semibold uppercase tracking-wider text-blue-500">
                Provider-Verified Stamp
              </p>
            </div>
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
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-300"
            >
              <option>Home Visit</option>
              <option>Clinic Visit</option>
              <option>Phone Verification</option>
              <option>Physical Record Review</option>
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
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 resize-none"
            />
          </div>
        </div>

        {/* Actions */}
        <div className="p-6 pt-0 flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 border border-gray-200 text-gray-600 text-sm font-medium py-2.5 rounded-xl hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(method, reason)}
            disabled={!reason.trim()}
            className="flex-1 bg-blue-600 text-white text-sm font-semibold py-2.5 rounded-xl hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            Apply Stamp
          </button>
        </div>
      </div>
    </div>
  );
}

export default function DoseReconciliation() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const patient = MOCK_PATIENTS.find((p) => p.id === id);

  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [showModal, setShowModal] = useState(false);
  const [reconciled, setReconciled] = useState<Set<string>>(new Set());

  if (!patient) return <div className="p-8 text-gray-500">Patient not found.</div>;

  const toggle = (entryId: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(entryId)) next.delete(entryId);
      else next.add(entryId);
      return next;
    });
  };

  const handleConfirm = () => {
    setReconciled((prev) => new Set([...prev, ...selected]));
    setSelected(new Set());
    setShowModal(false);
  };

  const selectedEntries = patient.anomalousEntries.filter((e) => selected.has(e.id));
  const pending = patient.anomalousEntries.filter((e) => !reconciled.has(e.id));

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between">
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
            Reconciled doses are retroactively counted in the patient's PDC (Proportion of Days Covered)
            and remove any applied penalty.{' '}
            <strong>Provider action is logged with timestamp.</strong>
          </p>
        </div>

        {/* Patient info card */}
        <div className="bg-white border border-gray-100 rounded-xl p-5 mb-6">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Patient Name</p>
              <p className="font-semibold text-gray-900">{patient.name}</p>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Patient ID</p>
              <p className="font-medium text-gray-700">{patient.patientId}</p>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Clinic & BHW</p>
              <p className="text-sm text-gray-700">{patient.clinic}</p>
              <p className="text-xs text-gray-400">BHW: {patient.bhw}</p>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-0.5">Profile</p>
              <p className="text-sm text-gray-700">{patient.ageProfile}</p>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-gray-100 flex items-center gap-6">
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Current Streak</p>
              <div className="flex items-center gap-1">
                <Zap size={13} className="text-yellow-500 fill-yellow-400" />
                <span className="font-bold text-gray-900">{patient.currentStreak} days</span>
                {patient.currentStreak < patient.bestStreak && (
                  <span className="text-[11px] text-gray-400">(broken from {patient.bestStreak})</span>
                )}
              </div>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Treatment Progress</p>
              <div className="flex items-center gap-2">
                <div className="w-24 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-blue-500 rounded-full"
                    style={{ width: `${(patient.currentDay / patient.totalDays) * 100}%` }}
                  />
                </div>
                <span className="text-sm font-medium text-gray-700">
                  Day {patient.currentDay} of {patient.totalDays}
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
          <div className="flex flex-wrap gap-3 text-xs font-medium">
            {[
              { color: 'bg-yellow-400', label: 'Technical Miss (T)' },
              { color: 'bg-green-500', label: 'App Miss (A)' },
              { color: 'bg-red-500', label: 'Unverified Miss (U)' },
              { color: 'bg-emerald-600', label: 'Provider-Verified (★)' },
            ].map((l) => (
              <div key={l.label} className="flex items-center gap-1.5">
                <span className={`w-2.5 h-2.5 rounded-full ${l.color}`} />
                <span className="text-gray-600">{l.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Anomalous entries */}
        <div className="bg-white border border-gray-100 rounded-xl overflow-hidden mb-6">
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
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

          {pending.length === 0 ? (
            <div className="py-12 text-center text-gray-400">
              <ShieldCheck size={32} className="mx-auto mb-2 text-green-400" />
              <p className="font-medium text-gray-600">All entries reconciled</p>
              <p className="text-sm">No pending anomalous entries for this patient.</p>
            </div>
          ) : (
            <>
              {/* Table header */}
              <div className="grid grid-cols-[32px_1fr_160px_1fr] gap-4 px-5 py-2.5 bg-gray-50 border-b border-gray-100">
                <span />
                <span className="text-[10px] uppercase tracking-wider text-gray-400 font-semibold">Date</span>
                <span className="text-[10px] uppercase tracking-wider text-gray-400 font-semibold">Status Badge</span>
                <span className="text-[10px] uppercase tracking-wider text-gray-400 font-semibold">Detected Cause</span>
              </div>

              {/* Rows */}
              {pending.map((entry) => {
                const cfg = STATUS_CONFIG[entry.statusBadge];
                const isSelected = selected.has(entry.id);
                return (
                  <div
                    key={entry.id}
                    onClick={() => toggle(entry.id)}
                    className={`grid grid-cols-[32px_1fr_160px_1fr] gap-4 items-center px-5 py-3.5 border-b border-gray-50 cursor-pointer transition-colors ${
                      isSelected ? 'bg-blue-50' : 'hover:bg-gray-50'
                    }`}
                  >
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
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded border w-fit ${cfg.classes}`}
                    >
                      {cfg.label}
                    </span>
                    <span className="text-sm text-gray-500">{entry.detectedCause}</span>
                  </div>
                );
              })}
            </>
          )}

          {/* Reconciled entries */}
          {reconciled.size > 0 && (
            <div className="px-5 py-3 bg-emerald-50/50 border-t border-emerald-100">
              <p className="text-xs text-emerald-600 font-medium flex items-center gap-1.5">
                <ShieldCheck size={12} />
                {reconciled.size} entr{reconciled.size === 1 ? 'y' : 'ies'} reconciled this session
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
          onClose={() => setShowModal(false)}
          onConfirm={handleConfirm}
        />
      )}
    </div>
  );
}
