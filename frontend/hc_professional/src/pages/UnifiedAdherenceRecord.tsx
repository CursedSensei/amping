import {
    AlertCircle,
    ArrowLeft,
    ChevronLeft,
    ChevronRight,
    Download,
    Info,
    Loader2,
    Plus,
    ShieldCheck,
    Thermometer,
    X,
    Zap,
} from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useReactToPrint } from 'react-to-print';
import {
    Line,
    LineChart,
    ReferenceLine,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import type { WebAdherenceMonthResponse } from '../api_types/Web_AdherenceMonthResponse';
import type { WebGamificationResponse } from '../api_types/Web_GamificationResponse';
import type { WebPatientDetailResponse } from '../api_types/Web_PatientDetailResponse';
import HeartQuota from '../components/HeartQuota';
import { type DayStatus, type PDCPoint, type PenaltyEvent } from '../data/mockData';
import { buildHeatmapFromApi, toPenaltyEvent } from '../services/adapters';
import { getPatient, getPatientAdherenceMonth, getPatientStats } from '../services/patient';

// ─── Local grid cell type ─────────────────────────────────────────────────
//  'empty'  = day exists in calendar but is before the patient's regimen start
//             → rendered as a blank, unstyled square (no colour, no number interaction)
//  DayStatus values are the standard adherence states

type CellStatus = DayStatus | 'empty';

interface GridCell {
  date: number;
  status: CellStatus;
  note?: string;
}

// ─── Status style maps (keyed on DayStatus only) ──────────────────────────

const STATUS_STYLE: Record<DayStatus, string> = {
  'app-recorded':        'bg-emerald-500 text-white hover:bg-emerald-600 active:scale-95',
  'provider-reconciled': 'bg-emerald-700 text-white hover:bg-emerald-800 active:scale-95',
  'technical-miss':      'bg-yellow-400 text-yellow-900 hover:bg-yellow-500 active:scale-95',
  'unverified-absence':  'bg-red-500 text-white hover:bg-red-600 active:scale-95',
  future:                'bg-gray-100 text-gray-300 cursor-default opacity-50',
};

const STATUS_LABEL: Record<DayStatus, string> = {
  'app-recorded':        'App-Recorded',
  'provider-reconciled': 'Provider-Reconciled',
  'technical-miss':      'Technical Miss',
  'unverified-absence':  'Unverified Absence',
  future:                'Future',
};

const STATUS_BG: Record<DayStatus, string> = {
  'app-recorded':        'bg-emerald-50 border-emerald-200',
  'provider-reconciled': 'bg-emerald-50 border-emerald-200',
  'technical-miss':      'bg-yellow-50 border-yellow-200',
  'unverified-absence':  'bg-red-50 border-red-200',
  future:                'bg-gray-50 border-gray-200',
};

const STATUS_TEXT: Record<DayStatus, string> = {
  'app-recorded':        'text-emerald-700',
  'provider-reconciled': 'text-emerald-800',
  'technical-miss':      'text-yellow-800',
  'unverified-absence':  'text-red-700',
  future:                'text-gray-400',
};

// ─── Calendar helpers ──────────────────────────────────────────────────────

const DOW_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

function monthStartCol(year: number, month: number): number {
  const jsDay = new Date(year, month, 1).getDay(); // 0=Sun
  return jsDay === 0 ? 6 : jsDay - 1;             // convert to 0=Mon
}

function buildGridCells(
  year: number,
  month: number,
  daysInMonth: number,
  classify: (day: number) => { status: CellStatus; note?: string },
): (null | GridCell)[] {
  const col = monthStartCol(year, month);
  const cells: (null | GridCell)[] = Array<null>(col).fill(null);
  for (let d = 1; d <= daysInMonth; d++) {
    const { status, note } = classify(d);
    cells.push({ date: d, status, note });
  }
  return cells;
}

// ─── Calendar Cell ─────────────────────────────────────────────────────────

function CalCell({
  cell,
  selected,
  onClick,
}: {
  cell: GridCell;
  selected: boolean;
  onClick: (c: GridCell) => void;
}) {
  if (cell.status === 'empty') {
    return <div className="w-10 h-10 rounded-xl bg-gray-50" />;
  }
  if (cell.status === 'future') {
    return (
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-sm font-semibold ${STATUS_STYLE.future}`}>
        {cell.date}
      </div>
    );
  }
  const style = STATUS_STYLE[cell.status];
  return (
    <button
      onClick={() => onClick(cell)}
      aria-label={`Day ${cell.date}: ${STATUS_LABEL[cell.status]}`}
      className={`w-10 h-10 rounded-xl flex items-center justify-center text-sm font-semibold transition-all select-none ring-offset-1 ${style} ${selected ? 'ring-2 ring-gray-900 scale-110 shadow-lg' : ''}`}
    >
      {cell.date}
    </button>
  );
}

// ─── Day Detail Panel ──────────────────────────────────────────────────────

function DayDetailPanel({
  cell,
  month,
  onClose,
}: {
  cell: GridCell;
  month: string;
  onClose: () => void;
}) {
  const status = cell.status as DayStatus;
  const [mo, yr] = month.split(' ');

  return (
    <div
      className={`border rounded-xl p-4 ${STATUS_BG[status]} flex items-start gap-3`}
      style={{ animation: 'fadein 0.15s ease' }}
    >
      <div
        className={`w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm ${STATUS_STYLE[status].split(' ')[0]} text-white shrink-0`}
      >
        {cell.date}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-0.5">
          <span className={`text-xs font-bold uppercase tracking-wider ${STATUS_TEXT[status]}`}>
            {STATUS_LABEL[status]}
          </span>
          <span className="text-[11px] text-gray-400">
            {mo} {cell.date}, {yr}
          </span>
        </div>
        <p className={`text-[12px] leading-relaxed ${STATUS_TEXT[status]} opacity-80`}>
          {cell.note ?? 'Video dose log submitted via Gabby and verified by upload.'}
        </p>
      </div>
      <button
        onClick={onClose}
        className="text-gray-400 hover:text-gray-600 transition-colors shrink-0 mt-0.5"
      >
        <X size={14} />
      </button>
    </div>
  );
}

// ─── Recharts Tooltip ──────────────────────────────────────────────────────

function CustomTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: { value: number }[];
  label?: string;
}) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-gray-200 rounded-lg shadow-lg px-3 py-2">
      <p className="text-[11px] text-gray-500 mb-0.5">{label}</p>
      <p className="text-sm font-bold text-blue-600">{payload[0].value}% PDC</p>
    </div>
  );
}

// ─── Symptoms / Side-effects panel ─────────────────────────────────────────

const PRESET_SYMPTOMS = [
  'Dizziness', 'Nausea', 'Vomiting', 'Headache', 'Fatigue',
  'Stomach pain', 'Skin rash', 'Joint pain', 'Vision changes', 'Jaundice',
  'Fever', 'Chills', 'Dark urine', 'Tingling / Numbness', 'Loss of appetite',
];

function SymptomsPanel({ initial }: { initial?: string[] }) {
  const [items, setItems] = useState<string[]>(() => (initial ? [...initial] : []));
  const [input, setInput] = useState('');
  const [showPresets, setShowPresets] = useState(false);

  const add = (s: string) => {
    const trimmed = s.trim();
    if (!trimmed || items.includes(trimmed)) return;
    setItems((prev) => [...prev, trimmed]);
    setInput('');
    setShowPresets(false);
  };

  const remove = (s: string) => setItems((prev) => prev.filter((x) => x !== s));

  return (
    <div className="bg-white border border-gray-100 rounded-xl p-5">
      <div className="flex items-center gap-2 mb-3">
        <Thermometer size={14} className="text-rose-500" />
        <h3 className="font-semibold text-gray-900 text-sm">Side Effects / Symptoms</h3>
      </div>

      {items.length === 0 ? (
        <p className="text-[12px] text-gray-400 mb-3 italic">No symptoms logged.</p>
      ) : (
        <div className="flex flex-wrap gap-2 mb-3">
          {items.map((s) => (
            <span
              key={s}
              className="flex items-center gap-1.5 bg-rose-50 border border-rose-200 text-rose-700 text-[11px] font-semibold px-2.5 py-1 rounded-full"
            >
              {s}
              <button
                onClick={() => remove(s)}
                className="text-rose-400 hover:text-rose-700 transition-colors"
                aria-label={`Remove ${s}`}
              >
                <X size={11} />
              </button>
            </span>
          ))}
        </div>
      )}

      <div className="flex gap-2 mb-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && add(input)}
          onFocus={() => setShowPresets(true)}
          placeholder="Type a symptom and press Enter…"
          className="flex-1 border border-gray-200 rounded-lg px-3 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-rose-300 focus:border-transparent transition"
        />
        <button
          onClick={() => add(input)}
          className="flex items-center gap-1 bg-rose-500 text-white text-xs font-semibold px-3 py-1.5 rounded-lg hover:bg-rose-600 transition-colors"
        >
          <Plus size={12} />
          Add
        </button>
      </div>

      {showPresets && (
        <div className="border border-gray-100 rounded-lg p-2 bg-gray-50">
          <p className="text-[10px] text-gray-400 uppercase tracking-wider font-semibold mb-1.5 px-1">
            Common symptoms — click to add
          </p>
          <div className="flex flex-wrap gap-1.5">
            {PRESET_SYMPTOMS.filter((ps) => !items.includes(ps)).map((ps) => (
              <button
                key={ps}
                onClick={() => add(ps)}
                className="text-[11px] px-2 py-0.5 rounded-full border border-gray-200 bg-white text-gray-600 hover:bg-rose-50 hover:border-rose-200 hover:text-rose-700 transition-colors"
              >
                {ps}
              </button>
            ))}
          </div>
          <button
            onClick={() => setShowPresets(false)}
            className="text-[10px] text-gray-400 mt-2 hover:text-gray-600 transition-colors"
          >
            Close suggestions
          </button>
        </div>
      )}

      {items.length > 0 && (
        <div className="flex items-start gap-1.5 mt-3 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2">
          <AlertCircle size={12} className="text-amber-500 mt-0.5 shrink-0" />
          <p className="text-[11px] text-amber-700">
            Reported symptoms are visible to the assigned provider and BHW. Patient will be notified.
          </p>
        </div>
      )}
    </div>
  );
}

// ─── Main Page ─────────────────────────────────────────────────────────────

export default function UnifiedAdherenceRecord() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const printRef = useRef<HTMLDivElement>(null);

  // ── API state ─────────────────────────────────────────────────────────────
  const [patientDetail, setPatientDetail] = useState<WebPatientDetailResponse | null>(null);
  const [adherence, setAdherence] = useState<WebAdherenceMonthResponse | null>(null);
  const [gamification, setGamification] = useState<WebGamificationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [adherenceLoading, setAdherenceLoading] = useState(false);
  const [fetchError, setFetchError] = useState('');

  const [selectedCell, setSelectedCell] = useState<GridCell | null>(null);
  const [currentDate, setCurrentDate] = useState<Date>(new Date());

  const numericId = id ? Number(id) : NaN;

  // Fetch patient detail + gamification once on mount
  useEffect(() => {
    if (isNaN(numericId)) return;
    setLoading(true);
    Promise.all([getPatient({ patient_id: numericId }), getPatientStats({ patient_id: numericId })])
      .then(([p, g]) => {
        setPatientDetail(p);
        setGamification(g);
      })
      .catch(() => setFetchError('Failed to load patient data.'))
      .finally(() => setLoading(false));
  }, [numericId]);

  // Re-fetch adherence whenever month changes
  useEffect(() => {
    if (isNaN(numericId)) return;
    const month = currentDate.getMonth() + 1; // 1-indexed
    const year  = currentDate.getFullYear();
    setAdherenceLoading(true);
    setSelectedCell(null);
    // TODO: the backend patient adherence endpoint currently derives month/year server-side.
    getPatientAdherenceMonth({ patient_id: numericId, payload: { month, year } })
      .then(setAdherence)
      .catch(() => setAdherence(null))
      .finally(() => setAdherenceLoading(false));
  }, [numericId, currentDate]);

  const handlePrevMonth = () =>
    setCurrentDate((prev) => new Date(prev.getFullYear(), prev.getMonth() - 1, 1));
  const handleNextMonth = () =>
    setCurrentDate((prev) => new Date(prev.getFullYear(), prev.getMonth() + 1, 1));

  const handlePrint = useReactToPrint({
    contentRef: printRef,
    documentTitle: patientDetail ? `UAR_${patientDetail.id}` : 'UnifiedAdherenceRecord',
  });

  // ── Loading / error guards ────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-50">
        <Loader2 size={28} className="animate-spin text-blue-400" />
      </div>
    );
  }
  if (fetchError || !patientDetail) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <p className="text-gray-500 mb-3">{fetchError || 'Patient not found.'}</p>
          <button onClick={() => navigate('/')} className="text-blue-600 text-sm underline">
            Back to Roster
          </button>
        </div>
      </div>
    );
  }

  // ── Derived display values ────────────────────────────────────────────────
  const monthPDC  = adherence?.month_pdc  ?? 0;
  const pdcTarget = adherence?.pdc_target ?? patientDetail.pdc_target;
  const isOnTrack = monthPDC >= pdcTarget;

  const patientName    = `${patientDetail.firstname} ${patientDetail.lastname}`;
  const regimenStartDate = new Date(patientDetail.regimen_start);
  regimenStartDate.setHours(0, 0, 0, 0);

  // Gamification
  const currentStreak   = gamification?.current_streak  ?? 0;
  const bestStreak      = gamification?.best_streak     ?? 0;
  const heartQuota      = gamification?.heart_quota     ?? 0;
  const penaltyHistory: PenaltyEvent[] = (gamification?.penalty_history ?? []).map(toPenaltyEvent);

  // Symptoms from this month's adherence days
  const symptomsThisMonth: string[] = adherence
    ? [...new Set(adherence.adherence_days.flatMap((d) => d.symptoms ?? []))]
    : [];

  // Anomaly count for CTA button
  const anomalyCount = adherence
    ? adherence.adherence_days.filter((d) => d.status === 'unverified_absence').length
    : 0;

  // Weekly PDC trend — not in current API; hidden when empty
  const pdcTrend: PDCPoint[] = [];

  // ── Calendar grid ─────────────────────────────────────────────────────────
  const year        = currentDate.getFullYear();
  const month0      = currentDate.getMonth(); // 0-indexed
  const daysInMonth = new Date(year, month0 + 1, 0).getDate();

  const displayMonthStr = currentDate
    .toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
    .toUpperCase();

  // Build override map from API adherence days
  const overrideMap = new Map<number, { status: DayStatus; note?: string }>();
  if (adherence) {
    const { heatmapDays } = buildHeatmapFromApi(
      adherence.adherence_days,
      year,
      month0 + 1,
    );
    for (const hd of heatmapDays) {
      if (hd.date !== null && hd.status !== 'future') {
        overrideMap.set(hd.date, { status: hd.status, note: hd.note });
      }
    }
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const classify = (day: number): { status: CellStatus; note?: string } => {
    const cellDate = new Date(year, month0, day);
    if (cellDate < regimenStartDate) return { status: 'empty' };
    if (cellDate > today)            return { status: 'future' };
    if (overrideMap.has(day))        return overrideMap.get(day)!;
    return { status: 'app-recorded' };
  };

  const gridCells = buildGridCells(year, month0, daysInMonth, classify);

  // ─── Render ───────────────────────────────────────────────────────────────
  return (
    <div className="h-screen bg-gray-50 flex flex-col overflow-hidden" style={{ animation: 'fadein 0.18s ease' }}>
      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between shrink-0 print:hidden">
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-2 text-sm text-gray-600 hover:text-blue-600 transition-colors"
        >
          <ArrowLeft size={16} />
          Back to Roster
        </button>
        <button
          onClick={() => handlePrint()}
          className="flex items-center gap-1.5 bg-gray-900 text-white text-sm px-3 py-1.5 rounded-lg hover:bg-gray-800 transition-colors"
        >
          <Download size={13} />
          Export PDF
        </button>
      </div>

      {/* Scrollable area */}
      <div className="flex-1 overflow-y-auto">
        {/* Printable area */}
        <div ref={printRef} className="max-w-6xl mx-auto p-8">
          {/* Page title */}
          <div className="mb-6">
            <h1 className="text-2xl font-bold text-gray-900">Unified Adherence Record</h1>
            <p className="text-sm text-gray-400 mt-0.5">Monthly Progress Report & Audit Trail</p>
          </div>

          {/* Patient info card */}
          <div className="bg-white border border-gray-100 rounded-xl p-6 mb-6">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-5">
              <div>
                <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Patient Name</p>
                <p className="font-bold text-gray-900">{patientName}</p>
              </div>
              <div>
                <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Patient ID</p>
                <p className="font-medium text-gray-700">{patientDetail.id}</p>
              </div>
              <div>
                <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Treatment Progress</p>
                <div className="flex items-center gap-2 mt-1">
                  <div className="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-blue-500 rounded-full"
                      style={{ width: `${(patientDetail.current_day / patientDetail.total_days) * 100}%` }}
                    />
                  </div>
                  <span className="text-xs font-semibold text-blue-600">
                    Day {patientDetail.current_day} of {patientDetail.total_days}
                  </span>
                </div>
              </div>
              <div>
                <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Regimen Start</p>
                <p className="text-sm text-gray-700">
                  {new Date(patientDetail.regimen_start).toLocaleDateString('en-US', {
                    month: 'long', day: 'numeric', year: 'numeric',
                  })}
                </p>
              </div>
            </div>
          </div>

          {/* Body: two columns */}
          <div className="grid grid-cols-1 xl:grid-cols-[1fr_300px] gap-6">
            {/* LEFT: Calendar heatmap + PDC trend */}
            <div className="space-y-6">
              {/* Calendar card */}
              <div className="bg-white border border-gray-100 rounded-xl p-6">
                {/* Header row */}
                <div className="flex items-center justify-between mb-6">
                  {/* Month nav */}
                  <div className="flex items-center gap-3">
                    <button
                      onClick={handlePrevMonth}
                      className="w-7 h-7 rounded-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50 transition-colors"
                    >
                      <ChevronLeft size={14} className="text-gray-500" />
                    </button>
                    <span className="text-sm font-semibold text-gray-700">{displayMonthStr}</span>
                    <button
                      onClick={handleNextMonth}
                      className="w-7 h-7 rounded-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50 transition-colors"
                    >
                      <ChevronRight size={14} className="text-gray-500" />
                    </button>
                  </div>

                  {/* PDC badge */}
                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      {adherenceLoading
                        ? <Loader2 size={24} className="animate-spin text-blue-300 ml-auto" />
                        : <span className="text-4xl font-bold text-gray-900">{monthPDC}%</span>
                      }
                      <p className="text-[11px] text-gray-400 uppercase tracking-wider">
                        Proportion of Days Covered
                      </p>
                    </div>
                    <div className="flex flex-col gap-1 items-end">
                      <span className="text-[11px] text-gray-400">Target ≥{pdcTarget}%</span>
                      <span
                        className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${
                          isOnTrack ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                        }`}
                      >
                        {isOnTrack ? '✓ ON TRACK' : '⚠ BELOW TARGET'}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Month 3 badge */}
                {patientDetail.month3_protected && (
                  <div className="flex items-center gap-2 bg-teal-50 border border-teal-100 rounded-lg px-3 py-2 mb-4">
                    <ShieldCheck size={13} className="text-teal-600" />
                    <span className="text-xs font-semibold text-teal-700">
                      Month 3 Adherence Protection Active
                    </span>
                  </div>
                )}

                {/* Day-of-week header */}
                <div className="grid grid-cols-7 gap-2 mb-2">
                  {DOW_LABELS.map((d) => (
                    <div key={d} className="text-[11px] text-gray-400 font-medium text-center">
                      {d}
                    </div>
                  ))}
                </div>

                {/* Calendar grid */}
                <div className="grid grid-cols-7 gap-2">
                  {gridCells.map((cell, i) =>
                    cell === null ? (
                      <div key={`pad-${i}`} />
                    ) : (
                      <div key={`${cell.date}-${i}`} className="flex items-center justify-center">
                        <CalCell
                          cell={cell}
                          selected={selectedCell?.date === cell.date && selectedCell?.status === cell.status}
                          onClick={(c) => setSelectedCell((prev) => (prev?.date === c.date ? null : c))}
                        />
                      </div>
                    ),
                  )}
                </div>

                {/* Day detail panel */}
                {selectedCell &&
                  selectedCell.status !== 'future' &&
                  selectedCell.status !== 'empty' && (
                    <div className="mt-4">
                      <DayDetailPanel
                        cell={selectedCell}
                        month={displayMonthStr}
                        onClose={() => setSelectedCell(null)}
                      />
                    </div>
                  )}

                {/* Legend */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-6 pt-5 border-t border-gray-100">
                  {(
                    [
                      { status: 'app-recorded'        as DayStatus, title: 'App-Recorded',        desc: 'Video dose logs submitted via Gabby and verified by upload.' },
                      { status: 'provider-reconciled' as DayStatus, title: 'Provider-Reconciled', desc: 'Provider or BHW manually verified; dose counted in PDC. Penalty reversed.' },
                      { status: 'technical-miss'      as DayStatus, title: 'Technical Miss',       desc: 'App crash or connectivity failure detected. No penalty applied.' },
                      { status: 'unverified-absence'  as DayStatus, title: 'Unverified Absence',   desc: 'No dose record, no reconciliation. Penalty applied.' },
                    ] as const
                  ).map((leg) => (
                    <div key={leg.status} className="flex gap-2">
                      <div className={`w-5 h-5 rounded-md shrink-0 ${STATUS_STYLE[leg.status].split(' ')[0]} mt-0.5`} />
                      <div>
                        <p className="text-[11px] font-semibold text-gray-800 uppercase tracking-wide">
                          {leg.title}
                        </p>
                        <p className="text-[10px] text-gray-400 leading-relaxed">{leg.desc}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* PDC Trend — shown only when backend provides weekly data */}
              {pdcTrend.length > 0 && (
                <div className="bg-white border border-gray-100 rounded-xl p-6">
                  <h3 className="font-semibold text-gray-900 mb-1 text-sm">Weekly PDC Trend</h3>
                  <p className="text-xs text-gray-400 mb-4">Proportion of Days Covered by week</p>
                  <ResponsiveContainer width="100%" height={160}>
                    <LineChart data={pdcTrend} margin={{ top: 8, right: 16, left: -16, bottom: 0 }}>
                      <XAxis dataKey="week" tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false} />
                      <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false} tickFormatter={(v) => `${v}%`} />
                      <Tooltip content={<CustomTooltip />} />
                      <ReferenceLine
                        y={pdcTarget}
                        stroke="#f59e0b"
                        strokeDasharray="4 4"
                        label={{ value: `Target ${pdcTarget}%`, fill: '#f59e0b', fontSize: 10, position: 'right' }}
                      />
                      <Line
                        type="monotone"
                        dataKey="pdc"
                        stroke="#3b82f6"
                        strokeWidth={2.5}
                        dot={{ fill: '#3b82f6', r: 4 }}
                        activeDot={{ r: 6, fill: '#1d4ed8' }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                  <div className="flex items-center gap-4 mt-3 text-[11px]">
                    <span className="flex items-center gap-1.5">
                      <span className="w-6 border-t-2 border-blue-500 border-dashed inline-block" />
                      <span className="text-gray-500">PDC this month</span>
                    </span>
                    <span className="flex items-center gap-1.5">
                      <span className="w-6 border-t-2 border-amber-400 border-dashed inline-block" />
                      <span className="text-gray-500">Target ≥{pdcTarget}%</span>
                    </span>
                  </div>
                </div>
              )}

              {/* Reconcile CTA */}
              {anomalyCount > 0 && (
                <button
                  onClick={() => navigate(`/patient/${id}/reconcile`)}
                  className="w-full flex items-center justify-center gap-2 bg-blue-600 text-white text-sm font-semibold py-3 rounded-xl hover:bg-blue-700 transition-colors print:hidden"
                >
                  <ShieldCheck size={15} />
                  Reconcile Anomalous Entries ({anomalyCount})
                </button>
              )}
            </div>

            {/* RIGHT: Gamification state + symptoms */}
            <div className="space-y-4">
              {/* Gamification card */}
              <div className="bg-white border border-gray-100 rounded-xl p-5">
                <h3 className="font-semibold text-gray-900 mb-4 text-sm">Gamification State</h3>

                <div className="grid grid-cols-2 gap-4 mb-4">
                  <div>
                    <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Current Streak</p>
                    <div className="flex items-center gap-1">
                      <Zap size={15} className="text-yellow-500 fill-yellow-400" />
                      <span className="font-bold text-gray-900 text-lg">{currentStreak}</span>
                      <span className="text-xs text-gray-400 ml-0.5">days</span>
                    </div>
                  </div>
                  <div>
                    <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Best Streak</p>
                    <div className="flex items-center gap-1">
                      <span className="font-bold text-gray-600 text-lg">{bestStreak}</span>
                      <span className="text-xs text-gray-400 ml-0.5">days</span>
                    </div>
                  </div>
                </div>

                <div className="mb-5">
                  <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Heart Quota</p>
                  <HeartQuota filled={heartQuota} />
                </div>

                {penaltyHistory.length > 0 && (
                  <div className="border-t border-gray-100 pt-4">
                    <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-3">Penalty History</p>
                    <div className="space-y-2">
                      {penaltyHistory.map((ev, i) => (
                        <div key={i} className="flex items-center gap-2">
                          <span
                            className={`text-[10px] font-bold w-5 h-5 rounded-full flex items-center justify-center ${
                              ev.tier === 1 ? 'bg-yellow-100 text-yellow-700' : 'bg-orange-100 text-orange-700'
                            }`}
                          >
                            T{ev.tier}
                          </span>
                          <div>
                            <p className="text-[11px] font-semibold text-gray-700">{ev.date} Penalty</p>
                            <p className="text-[10px] text-gray-400">{ev.label}</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Symptoms panel */}
              <SymptomsPanel initial={symptomsThisMonth} />

              {/* Month 3 protection card */}
              {patientDetail.month3_protected && (
                <div className="bg-teal-50 border border-teal-100 rounded-xl p-5">
                  <div className="flex items-center gap-2 mb-2">
                    <ShieldCheck size={14} className="text-teal-600" />
                    <span className="text-[11px] font-bold uppercase tracking-wider text-teal-700">
                      Month 3 Adherence Protection Active
                    </span>
                  </div>
                  <p className="text-[11px] text-teal-600 leading-relaxed">
                    Days 61–90 active. Last Tier downgrade: Tier 2 applied instead of Tier 3. Streak preserved via
                    baseline formula (⌊61 × 0.75⌋ = 45 days).
                  </p>
                </div>
              )}

              {/* Info card */}
              <div className="bg-gray-50 border border-gray-100 rounded-xl p-4">
                <div className="flex items-start gap-2">
                  <Info size={13} className="text-gray-400 mt-0.5 shrink-0" />
                  <p className="text-[11px] text-gray-500 leading-relaxed">
                    This record combines video entries, provider adjustments, technical connectivity issues, and
                    unverified gaps into a single audit-ready history.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
