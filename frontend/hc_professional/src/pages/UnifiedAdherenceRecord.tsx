import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Share2,
  Download,
  ChevronLeft,
  ChevronRight,
  Zap,
  ShieldCheck,
  Info,
  X,
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
} from 'recharts';
import { MOCK_PATIENTS, type DayStatus, type HeatmapDay } from '../data/mockData';
import HeartQuota from '../components/HeartQuota';

// ─── Heatmap constants ─────────────────────────────────────────────────────

const STATUS_STYLE: Record<DayStatus, string> = {
  'app-recorded': 'bg-emerald-500 text-white hover:bg-emerald-600 active:scale-95',
  'provider-reconciled': 'bg-emerald-700 text-white hover:bg-emerald-800 active:scale-95',
  'technical-miss': 'bg-yellow-400 text-yellow-900 hover:bg-yellow-500 active:scale-95',
  'unverified-absence': 'bg-red-500 text-white hover:bg-red-600 active:scale-95',
  future: 'bg-gray-100 text-gray-300 cursor-default opacity-50',
};

const STATUS_LABEL: Record<DayStatus, string> = {
  'app-recorded': 'App-Recorded',
  'provider-reconciled': 'Provider-Reconciled',
  'technical-miss': 'Technical Miss',
  'unverified-absence': 'Unverified Absence',
  future: 'Future',
};

const STATUS_BG: Record<DayStatus, string> = {
  'app-recorded': 'bg-emerald-50 border-emerald-200',
  'provider-reconciled': 'bg-emerald-50 border-emerald-200',
  'technical-miss': 'bg-yellow-50 border-yellow-200',
  'unverified-absence': 'bg-red-50 border-red-200',
  future: 'bg-gray-50 border-gray-200',
};

const STATUS_TEXT: Record<DayStatus, string> = {
  'app-recorded': 'text-emerald-700',
  'provider-reconciled': 'text-emerald-800',
  'technical-miss': 'text-yellow-800',
  'unverified-absence': 'text-red-700',
  future: 'text-gray-400',
};

// ─── Heatmap Cell ──────────────────────────────────────────────────────────

function HeatmapCell({
  day,
  selected,
  onClick,
}: {
  day: HeatmapDay;
  selected: boolean;
  onClick: (d: HeatmapDay) => void;
}) {
  if (day.date === null) return <div />;
  if (day.status === 'future') {
    return (
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-sm font-semibold ${STATUS_STYLE.future}`}>
        {day.date}
      </div>
    );
  }

  return (
    <button
      onClick={() => onClick(day)}
      className={`w-10 h-10 rounded-xl flex items-center justify-center text-sm font-semibold transition-all select-none ring-offset-1 ${
        STATUS_STYLE[day.status]
      } ${selected ? 'ring-2 ring-gray-900 scale-110 shadow-lg' : ''}`}
    >
      {day.date}
    </button>
  );
}

// ─── Day Detail Panel ──────────────────────────────────────────────────────

function DayDetailPanel({ day, month, onClose }: { day: HeatmapDay; month: string; onClose: () => void }) {
  return (
    <div
      className={`border rounded-xl p-4 ${STATUS_BG[day.status]} flex items-start gap-3 animate-in fade-in slide-in-from-top-1 duration-150`}
    >
      <div className={`w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm ${STATUS_STYLE[day.status].split(' ')[0]} text-white shrink-0`}>
        {day.date}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-0.5">
          <span className={`text-xs font-bold uppercase tracking-wider ${STATUS_TEXT[day.status]}`}>
            {STATUS_LABEL[day.status]}
          </span>
          <span className="text-[11px] text-gray-400">{month.split(' ')[0]} {day.date}, {month.split(' ')[1]}</span>
        </div>
        <p className={`text-[12px] leading-relaxed ${STATUS_TEXT[day.status]} opacity-80`}>
          {day.note ?? 'Video dose log submitted via Gabby and verified by upload.'}
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


// ─── Custom Recharts Tooltip ───────────────────────────────────────────────

function CustomTooltip({ active, payload, label }: {
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

// ─── Main Page ─────────────────────────────────────────────────────────────

export default function UnifiedAdherenceRecord() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const patient = MOCK_PATIENTS.find((p) => p.id === id);

  const [selectedDay, setSelectedDay] = useState<HeatmapDay | null>(null);

  if (!patient) return <div className="p-8 text-gray-500">Patient not found.</div>;

  const isOnTrack = patient.monthPDC >= patient.pdcTarget;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between sticky top-0 z-20">
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-2 text-sm text-gray-600 hover:text-blue-600 transition-colors"
        >
          <ArrowLeft size={16} />
          Back to Roster
        </button>
        <div className="flex items-center gap-2">
          <button className="flex items-center gap-1.5 border border-gray-200 text-gray-600 text-sm px-3 py-1.5 rounded-lg hover:bg-gray-50 transition-colors">
            <Share2 size={13} />
            Share with Nurse
          </button>
          <button className="flex items-center gap-1.5 bg-gray-900 text-white text-sm px-3 py-1.5 rounded-lg hover:bg-gray-800 transition-colors">
            <Download size={13} />
            Export PDF
          </button>
        </div>
      </div>

      <div className="max-w-6xl mx-auto p-8">
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
              <p className="font-bold text-gray-900">{patient.name}</p>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Patient ID</p>
              <p className="font-medium text-gray-700">{patient.patientId}</p>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Age Bracket</p>
              <p className="font-medium text-gray-700">{patient.ageProfile}</p>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Treatment Progress</p>
              <div className="flex items-center gap-2 mt-1">
                <div className="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-blue-500 rounded-full"
                    style={{ width: `${(patient.currentDay / patient.totalDays) * 100}%` }}
                  />
                </div>
                <span className="text-xs font-semibold text-blue-600">
                  Day {patient.currentDay} of {patient.totalDays}
                </span>
              </div>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Clinic</p>
              <p className="text-sm text-gray-700">{patient.clinic}</p>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Provider & BHW</p>
              <p className="text-sm text-gray-700">{patient.provider}</p>
              <p className="text-xs text-gray-400">BHW: {patient.bhw}</p>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-0.5">Regimen Start</p>
              <p className="text-sm text-gray-700">{patient.regimentStart}</p>
            </div>
          </div>
        </div>

        {/* Body: two columns */}
        <div className="grid grid-cols-1 xl:grid-cols-[1fr_300px] gap-6">
          {/* LEFT: Heatmap + PDC */}
          <div className="space-y-6">
            {/* Month & PDC header */}
            <div className="bg-white border border-gray-100 rounded-xl p-6">
              <div className="flex items-center justify-between mb-6">
                {/* Month nav */}
                <div className="flex items-center gap-3">
                  <button className="w-7 h-7 rounded-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50 transition-colors">
                    <ChevronLeft size={14} className="text-gray-500" />
                  </button>
                  <span className="text-sm font-semibold text-gray-700">{patient.heatmapMonth}</span>
                  <button className="w-7 h-7 rounded-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50 transition-colors">
                    <ChevronRight size={14} className="text-gray-500" />
                  </button>
                </div>

                {/* PDC */}
                <div className="flex items-center gap-4">
                  <div className="text-right">
                    <div className="flex items-end gap-1">
                      <span className="text-4xl font-bold text-gray-900">{patient.monthPDC}%</span>
                    </div>
                    <p className="text-[11px] text-gray-400 uppercase tracking-wider">
                      Proportion of Days Covered
                    </p>
                  </div>
                  <div className="flex flex-col gap-1 items-end">
                    <span className="text-[11px] text-gray-400">
                      Target ≥{patient.pdcTarget}%
                    </span>
                    <span
                      className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${
                        isOnTrack
                          ? 'bg-emerald-100 text-emerald-700'
                          : 'bg-red-100 text-red-700'
                      }`}
                    >
                      {isOnTrack ? '✓ ON TRACK' : '⚠ BELOW TARGET'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Month 3 badge */}
              {patient.month3Protected && (
                <div className="flex items-center gap-2 bg-teal-50 border border-teal-100 rounded-lg px-3 py-2 mb-4">
                  <ShieldCheck size={13} className="text-teal-600" />
                  <span className="text-xs font-semibold text-teal-700">Month 3 Adherence Protection Active</span>
                </div>
              )}

              {/* Day-of-week labels */}
              <div className="grid grid-cols-7 gap-2 mb-2">
                {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((d) => (
                  <div key={d} className="text-[11px] text-gray-400 font-medium text-center">
                    {d}
                  </div>
                ))}
              </div>

              {/* Heatmap grid */}
              <div className="grid grid-cols-7 gap-2">
                {patient.heatmapDays.map((day, i) => (
                  <div key={i} className="flex items-center justify-center">
                    <HeatmapCell
                      day={day}
                      selected={selectedDay === day}
                      onClick={(d) => setSelectedDay((prev) => (prev === d ? null : d))}
                    />
                  </div>
                ))}
              </div>

              {/* Day detail panel — appears below the grid on click */}
              {selectedDay && selectedDay.status !== 'future' && (
                <div className="mt-4">
                  <DayDetailPanel
                    day={selectedDay}
                    month={patient.heatmapMonth}
                    onClose={() => setSelectedDay(null)}
                  />
                </div>
              )}

              {/* Legend */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-6 pt-5 border-t border-gray-100">
                {[
                  { status: 'app-recorded' as DayStatus, count: 17, title: 'App-Recorded', desc: 'Video dose logs submitted via Gabby and verified by upload.' },
                  { status: 'provider-reconciled' as DayStatus, count: 8, title: 'Provider-Reconciled', desc: 'Provider or BHW manually verified; dose counted in PDC. Penalty reversed.' },
                  { status: 'technical-miss' as DayStatus, count: 1, title: 'Technical Miss', desc: 'App crash or connectivity failure detected. No penalty applied. Gate 0: T-type.' },
                  { status: 'unverified-absence' as DayStatus, count: 2, title: 'Unverified Absence', desc: 'No dose record, no reconciliation. Gate 0: U-type. Penalty applied.' },
                ].map((leg) => (
                  <div key={leg.status} className="flex gap-2">
                    <div className={`w-7 h-7 rounded-lg shrink-0 ${STATUS_STYLE[leg.status].split(' ')[0]} flex items-center justify-center text-white text-[11px] font-bold`}>
                      {leg.count}
                    </div>
                    <div>
                      <p className="text-[11px] font-semibold text-gray-800 uppercase tracking-wide">{leg.title}</p>
                      <p className="text-[10px] text-gray-400 leading-relaxed">{leg.desc}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* PDC Trend */}
            <div className="bg-white border border-gray-100 rounded-xl p-6">
              <h3 className="font-semibold text-gray-900 mb-1 text-sm">Weekly PDC Trend</h3>
              <p className="text-xs text-gray-400 mb-4">Proportion of Days Covered by week</p>
              <ResponsiveContainer width="100%" height={160}>
                <LineChart data={patient.pdcTrend} margin={{ top: 8, right: 16, left: -16, bottom: 0 }}>
                  <XAxis
                    dataKey="week"
                    tick={{ fontSize: 11, fill: '#9ca3af' }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <YAxis
                    domain={[0, 100]}
                    tick={{ fontSize: 11, fill: '#9ca3af' }}
                    axisLine={false}
                    tickLine={false}
                    tickFormatter={(v) => `${v}%`}
                  />
                  <Tooltip content={<CustomTooltip />} />
                  <ReferenceLine
                    y={patient.pdcTarget}
                    stroke="#f59e0b"
                    strokeDasharray="4 4"
                    label={{ value: `Target ${patient.pdcTarget}%`, fill: '#f59e0b', fontSize: 10, position: 'right' }}
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
                  <span className="text-gray-500">Target ≥{patient.pdcTarget}%</span>
                </span>
              </div>
            </div>

            {/* Reconcile CTA */}
            {patient.anomalousEntries.length > 0 && (
              <button
                onClick={() => navigate(`/patient/${id}/reconcile`)}
                className="w-full flex items-center justify-center gap-2 bg-blue-600 text-white text-sm font-semibold py-3 rounded-xl hover:bg-blue-700 transition-colors"
              >
                <ShieldCheck size={15} />
                Reconcile Anomalous Entries ({patient.anomalousEntries.length})
              </button>
            )}
          </div>

          {/* RIGHT: Gamification State */}
          <div className="space-y-4">
            <div className="bg-white border border-gray-100 rounded-xl p-5">
              <h3 className="font-semibold text-gray-900 mb-4 text-sm">Gamification State</h3>

              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Current Streak</p>
                  <div className="flex items-center gap-1">
                    <Zap size={15} className="text-yellow-500 fill-yellow-400" />
                    <span className="font-bold text-gray-900 text-lg">{patient.currentStreak}</span>
                    <span className="text-xs text-gray-400 ml-0.5">days</span>
                  </div>
                </div>
                <div>
                  <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Best Streak</p>
                  <div className="flex items-center gap-1">
                    <span className="font-bold text-gray-600 text-lg">{patient.bestStreak}</span>
                    <span className="text-xs text-gray-400 ml-0.5">days</span>
                  </div>
                </div>
              </div>

              <div className="mb-5">
                <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Heart Quota</p>
                <HeartQuota filled={patient.heartQuota} />
              </div>

              {/* Penalty history */}
              {patient.penaltyHistory.length > 0 && (
                <div className="border-t border-gray-100 pt-4">
                  <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-3">Penalty History (May)</p>
                  <div className="space-y-2">
                    {patient.penaltyHistory.map((ev, i) => (
                      <div key={i} className="flex items-center gap-2">
                        <span
                          className={`text-[10px] font-bold w-5 h-5 rounded-full flex items-center justify-center ${
                            ev.tier === 1
                              ? 'bg-yellow-100 text-yellow-700'
                              : 'bg-orange-100 text-orange-700'
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

            {/* Month 3 protection card */}
            {patient.month3Protected && (
              <div className="bg-teal-50 border border-teal-100 rounded-xl p-5">
                <div className="flex items-center gap-2 mb-2">
                  <ShieldCheck size={14} className="text-teal-600" />
                  <span className="text-[11px] font-bold uppercase tracking-wider text-teal-700">
                    Month 3 Adherence Protection Active
                  </span>
                </div>
                <p className="text-[11px] text-teal-600 leading-relaxed">
                  Days 61–90 active. Last Tier downgrade: Tier 2 applied instead of Tier 3. Streak preserved via baseline formula (⌊61 × 0.75⌋ = 45 days).
                </p>
              </div>
            )}

            {/* Info card */}
            <div className="bg-gray-50 border border-gray-100 rounded-xl p-4">
              <div className="flex items-start gap-2">
                <Info size={13} className="text-gray-400 mt-0.5 shrink-0" />
                <p className="text-[11px] text-gray-500 leading-relaxed">
                  This record combines video entries, provider adjustments, technical connectivity issues, and unverified gaps into a single audit-ready history.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
