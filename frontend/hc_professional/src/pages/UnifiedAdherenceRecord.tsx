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
import gsap from 'gsap';
import HeartQuota from '../components/HeartQuota';
import { usePatients } from '../context/PatientContext';
import { type DayStatus, type PDCPoint, type PenaltyEvent } from '../data/mockData';
import { buildHeatmapFromApi, toPenaltyEvent } from '../services/adapters';

// ─── Local grid cell type ─────────────────────────────────────────────────

type CellStatus = DayStatus | 'empty';

interface GridCell {
  date: number;
  status: CellStatus;
  note?: string;
}

// ─── Status style maps ──────────────────────────

const STATUS_STYLE: Record<DayStatus, string> = {
  'app-recorded':        'bg-emerald-500 text-white hover:bg-emerald-600 active:scale-95 shadow-sm shadow-emerald-500/20',
  'provider-reconciled': 'bg-teal-600 text-white hover:bg-teal-700 active:scale-95 shadow-sm shadow-teal-600/20',
  'technical-miss':      'bg-amber-400 text-amber-900 hover:bg-amber-500 active:scale-95 shadow-sm shadow-amber-400/20',
  'unverified-absence':  'bg-rose-500 text-white hover:bg-rose-600 active:scale-95 shadow-sm shadow-rose-500/20',
  future:                'bg-slate-100 text-slate-300 cursor-default opacity-60',
};

const STATUS_LABEL: Record<DayStatus, string> = {
  'app-recorded':        'App-Recorded',
  'provider-reconciled': 'Provider-Reconciled',
  'technical-miss':      'Technical Miss',
  'unverified-absence':  'Unverified Absence',
  future:                'Future',
};

const STATUS_BG: Record<DayStatus, string> = {
  'app-recorded':        'bg-emerald-50 border-emerald-100',
  'provider-reconciled': 'bg-teal-50 border-teal-100',
  'technical-miss':      'bg-amber-50 border-amber-100',
  'unverified-absence':  'bg-rose-50 border-rose-100',
  future:                'bg-slate-50 border-slate-100',
};

const STATUS_TEXT: Record<DayStatus, string> = {
  'app-recorded':        'text-emerald-700',
  'provider-reconciled': 'text-teal-800',
  'technical-miss':      'text-amber-800',
  'unverified-absence':  'text-rose-700',
  future:                'text-slate-400',
};

// ─── Calendar helpers ──────────────────────────────────────────────────────

const DOW_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

function monthStartCol(year: number, month: number): number {
  const jsDay = new Date(year, month, 1).getDay();
  return jsDay === 0 ? 6 : jsDay - 1; 
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

function CalCell({ cell, selected, onClick }: { cell: GridCell; selected: boolean; onClick: (c: GridCell) => void; }) {
  if (cell.status === 'empty') {
    return <div className="w-10 h-10 md:w-12 md:h-12 rounded-2xl bg-slate-50/50" />;
  }
  if (cell.status === 'future') {
    return (
      <div className={`cal-cell w-10 h-10 md:w-12 md:h-12 rounded-2xl flex items-center justify-center text-sm font-bold ${STATUS_STYLE.future}`}>
        {cell.date}
      </div>
    );
  }
  const style = STATUS_STYLE[cell.status];
  return (
    <button
      onClick={() => onClick(cell)}
      aria-label={`Day ${cell.date}: ${STATUS_LABEL[cell.status]}`}
      className={`cal-cell w-10 h-10 md:w-12 md:h-12 rounded-2xl flex items-center justify-center text-sm font-bold transition-all select-none ring-offset-2 ${style} ${selected ? 'ring-2 ring-[#0A0F24] scale-110 z-10' : 'hover:scale-105'}`}
    >
      {cell.date}
    </button>
  );
}

// ─── Day Detail Panel ──────────────────────────────────────────────────────

function DayDetailPanel({ cell, month, onClose }: { cell: GridCell; month: string; onClose: () => void; }) {
  const status = cell.status as DayStatus;
  const [mo, yr] = month.split(' ');

  return (
    <div className={`animate-in slide-in-from-bottom-2 fade-in border rounded-2xl p-5 ${STATUS_BG[status]} flex items-start gap-4 shadow-sm`}>
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center font-black text-sm ${STATUS_STYLE[status].split(' ')[0]} text-white shrink-0 shadow-sm`}>
        {cell.date}
      </div>
      <div className="flex-1 min-w-0 pt-0.5">
        <div className="flex items-center justify-between mb-1">
          <span className={`text-[10px] font-black uppercase tracking-widest ${STATUS_TEXT[status]}`}>
            {STATUS_LABEL[status]}
          </span>
          <span className="text-[11px] font-bold text-slate-400">
            {mo} {cell.date}, {yr}
          </span>
        </div>
        <p className={`text-sm font-medium leading-relaxed ${STATUS_TEXT[status]} opacity-90`}>
          {cell.note ?? 'Video dose log submitted via Gabby and verified by upload.'}
        </p>
      </div>
      <button onClick={onClose} className="text-slate-400 hover:text-slate-700 transition-colors shrink-0 bg-white/50 hover:bg-white p-1.5 rounded-full">
        <X size={16} />
      </button>
    </div>
  );
}

// ─── Recharts Tooltip ──────────────────────────────────────────────────────

function CustomTooltip({ active, payload, label }: { active?: boolean; payload?: { value: number }[]; label?: string; }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#0A0F24] border border-white/10 rounded-xl shadow-xl px-4 py-3">
      <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">{label}</p>
      <p className="text-base font-black text-white">{payload[0].value}% PDC</p>
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
    <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
      <div className="flex items-center gap-2 mb-4">
        <div className="w-8 h-8 rounded-lg bg-rose-50 flex items-center justify-center">
          <Thermometer size={16} className="text-rose-500" />
        </div>
        <h3 className="font-bold text-slate-900 text-sm">Side Effects / Symptoms</h3>
      </div>

      {items.length === 0 ? (
        <p className="text-xs font-medium text-slate-400 mb-4 bg-slate-50 p-3 rounded-xl border border-slate-100 border-dashed">No active symptoms reported.</p>
      ) : (
        <div className="flex flex-wrap gap-2 mb-4">
          {items.map((s) => (
            <span key={s} className="flex items-center gap-1.5 bg-rose-50 border border-rose-100 text-rose-700 text-xs font-bold px-3 py-1.5 rounded-full shadow-sm">
              {s}
              <button onClick={() => remove(s)} className="text-rose-400 hover:text-rose-700 transition-colors bg-white rounded-full p-0.5">
                <X size={12} />
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
          placeholder="Type symptom..."
          className="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
        />
        <button onClick={() => add(input)} className="flex items-center gap-1 bg-[#0A0F24] text-white text-xs font-bold px-4 py-2.5 rounded-xl hover:bg-blue-900 transition-all shadow-md">
          <Plus size={14} /> Add
        </button>
      </div>

      {showPresets && (
        <div className="animate-in fade-in slide-in-from-top-2 border border-slate-100 rounded-2xl p-4 bg-slate-50 mt-3 shadow-sm">
          <p className="text-[10px] text-slate-400 uppercase tracking-widest font-bold mb-3 px-1">Common suggestions</p>
          <div className="flex flex-wrap gap-2">
            {PRESET_SYMPTOMS.filter((ps) => !items.includes(ps)).map((ps) => (
              <button
                key={ps}
                onClick={() => add(ps)}
                className="text-[11px] font-bold px-3 py-1.5 rounded-full border border-slate-200 bg-white text-slate-600 hover:bg-rose-50 hover:border-rose-200 hover:text-rose-700 transition-colors shadow-sm"
              >
                {ps}
              </button>
            ))}
          </div>
          <button onClick={() => setShowPresets(false)} className="text-[10px] font-bold text-slate-400 mt-4 hover:text-slate-600 transition-colors uppercase tracking-widest">
            Close
          </button>
        </div>
      )}

      {items.length > 0 && (
        <div className="flex items-start gap-2 mt-4 bg-amber-50 border border-amber-100 rounded-xl px-4 py-3">
          <AlertCircle size={14} className="text-amber-500 mt-0.5 shrink-0" />
          <p className="text-xs font-medium text-amber-700 leading-relaxed">
            Reported symptoms are visible to the assigned provider and BHW. The patient will be notified automatically.
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
  const containerRef = useRef<HTMLDivElement>(null);
  const { patientBundles, ensurePatientBundle, loadPatientAdherence } = usePatients();

  const [selectedCell, setSelectedCell] = useState<GridCell | null>(null);
  const [currentDate, setCurrentDate] = useState<Date>(new Date());

  const numericId = id ? Number(id) : NaN;
  const month = currentDate.getMonth() + 1;
  const selectedYear = currentDate.getFullYear();
  const adherenceKey = `${selectedYear}-${String(month).padStart(2, '0')}` as `${number}-${number}`;
  const bundle = Number.isNaN(numericId) ? null : patientBundles[numericId] ?? null;
  const patientDetail = bundle?.detail ?? null;
  const gamification = bundle?.gamification ?? null;
  const adherence = bundle?.adherenceByMonth[adherenceKey] ?? null;
  
  const loading = Number.isNaN(numericId) ? false : (!bundle || bundle.detailLoading || bundle.gamificationLoading);
  const adherenceLoading = Number.isNaN(numericId) ? false : !!bundle?.adherenceLoading[adherenceKey];
  const fetchError = bundle?.error ?? '';

  useEffect(() => {
    if (Number.isNaN(numericId)) return;
    void ensurePatientBundle(numericId);
  }, [numericId, ensurePatientBundle]);

  useEffect(() => {
    if (Number.isNaN(numericId)) return;
    setSelectedCell(null);
    void loadPatientAdherence(numericId, month, selectedYear);
  }, [numericId, currentDate, loadPatientAdherence, month, selectedYear]);

  // GSAP Animations
  useEffect(() => {
    if (!loading && !fetchError && patientDetail) {
      const ctx = gsap.context(() => {
        gsap.fromTo('.animate-header', { y: -15, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, ease: 'power3.out' });
        gsap.fromTo('.animate-card', { y: 20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5, stagger: 0.1, ease: 'power3.out', delay: 0.1 });
        gsap.fromTo('.cal-cell', { scale: 0.8, opacity: 0 }, { scale: 1, opacity: 1, duration: 0.3, stagger: 0.015, ease: 'back.out(1.5)', delay: 0.3 });
      }, containerRef);
      return () => ctx.revert();
    }
  }, [loading, fetchError, patientDetail, currentDate]);

  const handlePrevMonth = () => setCurrentDate((prev) => new Date(prev.getFullYear(), prev.getMonth() - 1, 1));
  const handleNextMonth = () => setCurrentDate((prev) => new Date(prev.getFullYear(), prev.getMonth() + 1, 1));

  const handlePrint = useReactToPrint({
    contentRef: printRef,
    documentTitle: patientDetail ? `UAR_${patientDetail.id}` : 'UnifiedAdherenceRecord',
  });

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center bg-slate-50">
        <Loader2 size={32} className="animate-spin text-blue-500" />
      </div>
    );
  }
  
  if (fetchError || !patientDetail) {
    return (
      <div className="h-screen flex items-center justify-center bg-slate-50">
        <div className="text-center bg-white p-8 rounded-3xl shadow-sm border border-slate-100">
          <AlertCircle size={40} className="mx-auto text-rose-500 mb-4" />
          <p className="text-slate-600 font-bold mb-4">{fetchError || 'Patient profile not found.'}</p>
          <button onClick={() => navigate('/')} className="text-blue-600 text-sm font-bold hover:underline">
            Return to Roster
          </button>
        </div>
      </div>
    );
  }

  const monthPDC = adherence?.month_pdc ?? 0;
  const pdcTarget = adherence?.pdc_target ?? patientDetail.pdc_target;
  const isOnTrack = monthPDC >= pdcTarget;
  const patientName = `${patientDetail.firstname} ${patientDetail.lastname}`;
  const regimenStartDate = new Date(patientDetail.regimen_start);
  regimenStartDate.setHours(0, 0, 0, 0);

  const currentStreak = gamification?.current_streak ?? 0;
  const bestStreak = gamification?.best_streak ?? 0;
  const heartQuota = gamification?.heart_quota ?? 0;
  const penaltyHistory: PenaltyEvent[] = (gamification?.penalty_history ?? []).map(toPenaltyEvent);

  const symptomsThisMonth: string[] = adherence ? [...new Set(adherence.adherence_days.flatMap((d) => d.symptoms ?? []))] : [];
  const anomalyCount = adherence ? adherence.adherence_days.filter((d) => d.status === 'unverified_absence').length : 0;
  const pdcTrend: PDCPoint[] = [];

  const calendarYear = currentDate.getFullYear();
  const month0 = currentDate.getMonth();
  const daysInMonth = new Date(calendarYear, month0 + 1, 0).getDate();
  const displayMonthStr = currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  const overrideMap = new Map<number, { status: DayStatus; note?: string }>();
  if (adherence) {
    const { heatmapDays } = buildHeatmapFromApi(adherence.adherence_days, calendarYear, month0 + 1);
    for (const hd of heatmapDays) {
      if (hd.date !== null && hd.status !== 'future') {
        overrideMap.set(hd.date, { status: hd.status, note: hd.note });
      }
    }
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const classify = (day: number): { status: CellStatus; note?: string } => {
    const cellDate = new Date(calendarYear, month0, day);
    if (cellDate < regimenStartDate) return { status: 'empty' };
    if (cellDate > today) return { status: 'future' };
    if (overrideMap.has(day)) return overrideMap.get(day)!;
    return { status: 'app-recorded' };
  };

  const gridCells = buildGridCells(calendarYear, month0, daysInMonth, classify);

  return (
    <div className="h-screen bg-slate-50 flex flex-col overflow-hidden font-sans">
      
      {/* Top bar */}
      <div className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between shrink-0 shadow-sm z-10 print:hidden">
        <button onClick={() => navigate('/')} className="flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-blue-600 transition-colors">
          <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center">
            <ArrowLeft size={16} />
          </div>
          Back to Roster
        </button>
        <button onClick={() => handlePrint()} className="flex items-center gap-2 bg-[#0A0F24] text-white text-sm font-bold px-4 py-2 rounded-xl hover:bg-blue-900 transition-all shadow-md">
          <Download size={14} />
          Export Report
        </button>
      </div>

      {/* Scrollable area */}
      <div className="flex-1 overflow-y-auto scroll-smooth" ref={containerRef}>
        <div ref={printRef} className="max-w-7xl mx-auto p-6 md:p-10">
          
          {/* Page title */}
          <div className="animate-header mb-8">
            <h1 className="text-3xl font-black text-slate-900 tracking-tight">Unified Adherence Record</h1>
            <p className="text-sm font-medium text-slate-500 mt-2">Comprehensive monthly progress report and verified audit trail.</p>
          </div>

          {/* Patient info card */}
          <div className="animate-card bg-white border border-slate-100 rounded-3xl p-6 md:p-8 mb-8 shadow-sm">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-8">
              <div>
                <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Patient Name</p>
                <p className="font-black text-slate-900 text-lg">{patientName}</p>
              </div>
              <div>
                <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Patient ID</p>
                <p className="font-bold text-slate-600 text-lg">{patientDetail.id}</p>
              </div>
              <div className="col-span-2 md:col-span-1">
                <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">Treatment Progress</p>
                <div className="flex flex-col gap-1.5">
                  <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden border border-slate-200">
                    <div
                      className="h-full bg-blue-500 rounded-full transition-all duration-1000 ease-out"
                      style={{ width: `${(patientDetail.current_day / patientDetail.total_days) * 100}%` }}
                    />
                  </div>
                  <span className="text-xs font-black text-blue-600 uppercase tracking-widest">
                    Day {patientDetail.current_day} of {patientDetail.total_days}
                  </span>
                </div>
              </div>
              <div className="col-span-2 md:col-span-1">
                <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Regimen Start</p>
                <p className="font-bold text-slate-700 text-lg">
                  {new Date(patientDetail.regimen_start).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
                </p>
              </div>
            </div>
          </div>

          {/* Body: two columns */}
          <div className="grid grid-cols-1 xl:grid-cols-[1fr_340px] gap-8">
            
            {/* LEFT: Calendar heatmap + PDC trend */}
            <div className="space-y-8">
              
              {/* Calendar card */}
              <div className="animate-card bg-white border border-slate-100 rounded-3xl p-6 md:p-8 shadow-sm">
                
                {/* Header row */}
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-8">
                  {/* Month nav */}
                  <div className="flex items-center gap-4 bg-slate-50 p-2 rounded-2xl border border-slate-100">
                    <button onClick={handlePrevMonth} className="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center hover:bg-slate-50 hover:scale-105 transition-all shadow-sm">
                      <ChevronLeft size={18} className="text-slate-600" />
                    </button>
                    <span className="text-lg font-black text-slate-800 w-36 text-center tracking-tight">{displayMonthStr}</span>
                    <button onClick={handleNextMonth} className="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center hover:bg-slate-50 hover:scale-105 transition-all shadow-sm">
                      <ChevronRight size={18} className="text-slate-600" />
                    </button>
                  </div>

                  {/* PDC badge */}
                  <div className="flex items-center gap-5">
                    <div className="text-right">
                      {adherenceLoading
                        ? <Loader2 size={28} className="animate-spin text-blue-400 ml-auto" />
                        : <span className="text-5xl font-black text-slate-900 tracking-tighter">{monthPDC}%</span>
                      }
                      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mt-1">
                        Proportion of Days Covered
                      </p>
                    </div>
                    <div className="flex flex-col gap-1.5 items-end">
                      <span className="text-xs font-bold text-slate-500 bg-slate-50 px-3 py-1 rounded-lg border border-slate-100">Target ≥{pdcTarget}%</span>
                      <span className={`text-[10px] font-black px-3 py-1 rounded-lg uppercase tracking-widest border ${isOnTrack ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-rose-50 text-rose-700 border-rose-200'}`}>
                        {isOnTrack ? '✓ ON TRACK' : '⚠ BELOW TARGET'}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Month 3 badge */}
                {patientDetail.month3_protected && (
                  <div className="flex items-center gap-3 bg-amber-50 border border-amber-200 rounded-2xl px-5 py-4 mb-6">
                    <div className="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center shrink-0">
                      <ShieldCheck size={16} className="text-amber-600" />
                    </div>
                    <span className="text-sm font-bold text-amber-800">Month 3 Adherence Protection Active</span>
                  </div>
                )}

                {/* Day-of-week header */}
                <div className="grid grid-cols-7 gap-2 mb-3">
                  {DOW_LABELS.map((d) => (
                    <div key={d} className="text-[10px] font-black text-slate-400 uppercase tracking-widest text-center">
                      {d}
                    </div>
                  ))}
                </div>

                {/* Calendar grid */}
                <div className="grid grid-cols-7 gap-2 md:gap-3">
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
                {selectedCell && selectedCell.status !== 'future' && selectedCell.status !== 'empty' && (
                  <div className="mt-6">
                    <DayDetailPanel cell={selectedCell} month={displayMonthStr} onClose={() => setSelectedCell(null)} />
                  </div>
                )}

                {/* Legend */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-8 pt-6 border-t border-slate-100">
                  {(
                    [
                      { status: 'app-recorded'        as DayStatus, title: 'App-Recorded',        desc: 'Video dose verified by system.' },
                      { status: 'provider-reconciled' as DayStatus, title: 'Provider-Reconciled', desc: 'Manual override (counted in PDC).' },
                      { status: 'technical-miss'      as DayStatus, title: 'Technical Miss',       desc: 'App failure detected (no penalty).' },
                      { status: 'unverified-absence'  as DayStatus, title: 'Unverified Absence',   desc: 'No record. Penalty applied.' },
                    ] as const
                  ).map((leg) => (
                    <div key={leg.status} className="flex gap-3 bg-slate-50 p-3 rounded-2xl border border-slate-100">
                      <div className={`w-8 h-8 rounded-xl shrink-0 ${STATUS_STYLE[leg.status].split(' ')[0]} shadow-sm`} />
                      <div>
                        <p className="text-[11px] font-black text-slate-800 uppercase tracking-widest mb-0.5">{leg.title}</p>
                        <p className="text-[11px] font-medium text-slate-500 leading-snug">{leg.desc}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* PDC Trend */}
              {pdcTrend.length > 0 && (
                <div className="animate-card bg-white border border-slate-100 rounded-3xl p-6 md:p-8 shadow-sm">
                  <h3 className="font-black text-slate-900 text-lg mb-1">Weekly PDC Trend</h3>
                  <p className="text-sm font-medium text-slate-500 mb-6">Proportion of Days Covered by week</p>
                  <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={pdcTrend} margin={{ top: 8, right: 16, left: -16, bottom: 0 }}>
                      <XAxis dataKey="week" tick={{ fontSize: 11, fill: '#64748b', fontWeight: 'bold' }} axisLine={false} tickLine={false} />
                      <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748b', fontWeight: 'bold' }} axisLine={false} tickLine={false} tickFormatter={(v) => `${v}%`} />
                      <Tooltip content={<CustomTooltip />} />
                      <ReferenceLine y={pdcTarget} stroke="#f59e0b" strokeDasharray="4 4" label={{ value: `Target ${pdcTarget}%`, fill: '#f59e0b', fontSize: 10, fontWeight: 'bold', position: 'right' }} />
                      <Line type="monotone" dataKey="pdc" stroke="#3b82f6" strokeWidth={3} dot={{ fill: '#3b82f6', r: 5, strokeWidth: 2, stroke: '#fff' }} activeDot={{ r: 8, fill: '#1d4ed8', stroke: '#fff', strokeWidth: 2 }} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              )}

              {/* Reconcile CTA */}
              {anomalyCount > 0 && (
                <button
                  onClick={() => navigate(`/patient/${id}/reconcile`)}
                  className="animate-card w-full flex items-center justify-center gap-2 bg-[#0A0F24] text-white text-base font-bold py-4 rounded-2xl hover:bg-blue-900 transition-all shadow-lg shadow-blue-900/20 print:hidden"
                >
                  <ShieldCheck size={18} />
                  Reconcile Anomalous Entries ({anomalyCount})
                </button>
              )}
            </div>

            {/* RIGHT: Gamification state + symptoms */}
            <div className="space-y-6">
              
              {/* Gamification card */}
              <div className="animate-card bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
                <h3 className="font-black text-slate-900 text-lg mb-6">Gamification State</h3>

                <div className="grid grid-cols-2 gap-4 mb-6">
                  <div className="bg-amber-50 p-4 rounded-2xl border border-amber-100">
                    <p className="text-[10px] font-bold uppercase tracking-widest text-amber-600 mb-1.5">Current Streak</p>
                    <div className="flex items-center gap-1.5">
                      <Zap size={20} className="text-amber-500 fill-amber-400" />
                      <span className="font-black text-slate-900 text-2xl tracking-tighter">{currentStreak}</span>
                    </div>
                  </div>
                  <div className="bg-slate-50 p-4 rounded-2xl border border-slate-100">
                    <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500 mb-1.5">Best Streak</p>
                    <div className="flex items-center gap-1">
                      <span className="font-black text-slate-700 text-2xl tracking-tighter">{bestStreak}</span>
                    </div>
                  </div>
                </div>

                <div className="mb-6">
                  <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500 mb-3">Heart Quota</p>
                  <HeartQuota filled={heartQuota} />
                </div>

                {penaltyHistory.length > 0 && (
                  <div className="border-t border-slate-100 pt-5">
                    <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500 mb-4">Penalty History</p>
                    <div className="space-y-3">
                      {penaltyHistory.map((ev, i) => (
                        <div key={i} className="flex items-center gap-3 bg-slate-50 p-3 rounded-xl border border-slate-100">
                          <span className={`text-[10px] font-black w-8 h-8 rounded-lg flex items-center justify-center shrink-0 shadow-sm ${ev.tier === 1 ? 'bg-amber-100 text-amber-700 border border-amber-200' : 'bg-orange-100 text-orange-700 border border-orange-200'}`}>
                            T{ev.tier}
                          </span>
                          <div>
                            <p className="text-[11px] font-black text-slate-800">{ev.date} Penalty</p>
                            <p className="text-[11px] font-medium text-slate-500 leading-snug">{ev.label}</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Symptoms panel */}
              <div className="animate-card">
                <SymptomsPanel initial={symptomsThisMonth} />
              </div>

              {/* Month 3 protection card */}
              {patientDetail.month3_protected && (
                <div className="animate-card bg-amber-50 border border-amber-200 rounded-3xl p-6 shadow-sm">
                  <div className="flex items-center gap-3 mb-3">
                    <div className="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center shrink-0">
                      <ShieldCheck size={20} className="text-amber-600" />
                    </div>
                    <span className="text-xs font-black uppercase tracking-widest text-amber-800">
                      Protection Active
                    </span>
                  </div>
                  <p className="text-xs font-medium text-amber-700 leading-relaxed bg-white/50 p-3 rounded-xl">
                    Days 61–90 active. Last Tier downgrade: Tier 2 applied instead of Tier 3. Streak preserved via baseline formula (⌊61 × 0.75⌋ = 45 days).
                  </p>
                </div>
              )}

              {/* Info card */}
              <div className="animate-card bg-gradient-to-br from-blue-600 to-indigo-700 rounded-3xl p-6 text-white shadow-lg shadow-blue-900/10 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl -mr-10 -mt-10" />
                <div className="flex items-start gap-3 relative z-10">
                  <Info size={16} className="text-blue-300 mt-0.5 shrink-0" />
                  <p className="text-xs font-medium text-blue-100 leading-relaxed">
                    This record combines video entries, provider adjustments, technical connectivity issues, and unverified gaps into a single audit-ready history.
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