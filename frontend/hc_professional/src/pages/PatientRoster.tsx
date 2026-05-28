import { Activity, AlertCircle, ChevronRight, Wifi, Zap } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import type { AdherenceStatusEnum, WebAdherenceDayEntry, WebAdherenceMonthResponse } from '../api_types/Web_AdherenceMonthResponse';
import type { WebGamificationResponse } from '../api_types/Web_GamificationResponse';
import type { WebPatientEntry } from '../api_types/Web_GetAllPatientsResponse';
import type { WebPatientDetailResponse } from '../api_types/Web_PatientDetailResponse';
import HeartQuota from '../components/HeartQuota';
import RiskBadge from '../components/RiskBadge';
import Sidebar from '../components/Sidebar';
import { type Patient } from '../data/mockData';
import { toPenaltyEvent } from '../services/adapters';
import {
    getAllPatients,
    getPatient,
    getPatientAdherenceMonth,
    getPatientStats,
} from '../services/patient';

type RiskFilter = 'all' | 'high' | 'low';

const DAY_LABELS = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];
const DEFAULT_WEEKLY_COMPLIANCE: Patient['weeklyCompliance'] = [
  { day: 'Mon', status: 'done' },
  { day: 'Tue', status: 'done' },
  { day: 'Wed', status: 'done' },
  { day: 'Thu', status: 'done' },
  { day: 'Fri', status: 'done' },
  { day: 'Sat', status: 'pending' },
  { day: 'Sun', status: 'pending' },
];

function getAgeFromBirthYear(birthyear: number): number {
  return Math.max(0, new Date().getFullYear() - birthyear);
}

function getAgeProfile(age: number): Patient['ageProfile'] {
  if (age < 18) return 'Child';
  if (age >= 60) return 'Senior';
  return 'Adult';
}

function toComplianceStatus(status: AdherenceStatusEnum): 'done' | 'missed' | 'pending' {
  if (status === 'app_recorded' || status === 'provider_reconciled') return 'done';
  if (status === 'technical_miss' || status === 'unverified_absence') return 'missed';
  return 'pending';
}

function buildWeeklyCompliance(days: WebAdherenceDayEntry[]): Patient['weeklyCompliance'] {
  if (days.length === 0) return DEFAULT_WEEKLY_COMPLIANCE;

  const recentDays = [...days]
    .sort((left, right) => new Date(left.date).getTime() - new Date(right.date).getTime())
    .slice(-7);

  if (recentDays.length < 7) return DEFAULT_WEEKLY_COMPLIANCE;

  const labels: Patient['weeklyCompliance'] = [
    { day: 'Mon', status: 'pending' },
    { day: 'Tue', status: 'pending' },
    { day: 'Wed', status: 'pending' },
    { day: 'Thu', status: 'pending' },
    { day: 'Fri', status: 'pending' },
    { day: 'Sat', status: 'pending' },
    { day: 'Sun', status: 'pending' },
  ];

  return labels.map((entry, index) => ({
    day: entry.day,
    status: toComplianceStatus(recentDays[index].status),
  }));
}

function buildPatientFromApi(
  summary: WebPatientEntry,
  detail: WebPatientDetailResponse | null,
  stats: WebGamificationResponse | null,
  adherence: WebAdherenceMonthResponse | null,
): Patient {
  const age = getAgeFromBirthYear(summary.birthyear);
  const heartQuota = stats?.heart_quota ?? 0;
  const adherenceDays = adherence?.adherence_days ?? [];

  // TODO: replace this heuristic once the backend exposes risk_tier on the patient summary.
  const riskTier: Patient['riskTier'] =
    heartQuota === 0 || (stats?.current_streak ?? 0) <= 3
      ? 'tier3'
      : (stats?.current_streak ?? 0) <= 7
        ? 'tier2'
        : (stats?.current_streak ?? 0) <= 14
          ? 'tier1'
          : 'safe';

  // TODO: the backend does not provide roster metadata yet, so these values are placeholders.
  return {
    id: String(summary.id),
    name: `${summary.firstname} ${summary.lastname}`,
    age,
    ageProfile: getAgeProfile(age),
    clinic: 'TODO: backend clinic name unavailable',
    provider: 'TODO: backend provider name unavailable',
    bhw: 'TODO: backend BHW unavailable',
    patientId: `TODO-${summary.id}`,
    regimentStart: detail?.regimen_start ? new Date(detail.regimen_start).toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    }) : 'TODO: backend regimen start unavailable',
    currentDay: detail?.current_day ?? 0,
    totalDays: detail?.total_days ?? stats?.total_regimen_days ?? 0,
    currentStreak: stats?.current_streak ?? 0,
    bestStreak: stats?.best_streak ?? stats?.current_streak ?? 0,
    heartQuota,
    riskTier,
    lastActive: 'TODO: backend last active label unavailable',
    triggerReason: 'TODO: backend trigger reason unavailable',
    lastSyncLabel: 'TODO: backend sync label unavailable',
    symptomReported: adherenceDays.flatMap((day) => day.symptoms ?? []).slice(0, 3),
    weeklyCompliance: buildWeeklyCompliance(adherenceDays),
    anomalousEntries: [],
    penaltyHistory: stats?.penalty_history.map(toPenaltyEvent) ?? [],
    pdcTrend: [],
    heatmapMonth: adherence ? `MONTH ${adherence.month}/${adherence.year}` : 'TODO: backend month unavailable',
    heatmapStartDay: 0,
    heatmapDays: [],
    monthPDC: adherence?.month_pdc ?? 0,
    pdcTarget: adherence?.pdc_target ?? detail?.pdc_target ?? 85,
    month3Protected: detail?.month3_protected ?? false,
  };
}

// ─── Sub-components ───────────────────────────────────────────────────────────

function ComplianceDot({ status }: { status: 'done' | 'missed' | 'pending' }) {
  if (status === 'done')
    return (
      <div className="w-8 h-8 rounded-full bg-emerald-500 flex items-center justify-center shadow-sm shadow-emerald-500/20 transform hover:scale-110 transition-transform">
        <svg viewBox="0 0 12 12" className="w-4 h-4 text-white" fill="none" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M2 6l3 3 5-5" />
        </svg>
      </div>
    );
  if (status === 'missed')
    return (
      <div className="w-8 h-8 rounded-full bg-red-50 border-2 border-red-400 flex items-center justify-center transform hover:scale-110 transition-transform">
        <svg viewBox="0 0 12 12" className="w-3.5 h-3.5 text-red-500" fill="none" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" d="M3 3l6 6M9 3l-6 6" />
        </svg>
      </div>
    );
  return <div className="w-8 h-8 rounded-full border-2 border-dashed border-slate-300 bg-slate-50" />;
}

function PatientCard({ patient }: { patient: Patient }) {
  const navigate = useNavigate();

  return (
    <div
      className="patient-card group bg-white rounded-2xl p-6 cursor-pointer border border-slate-100 shadow-[0_2px_10px_rgb(0,0,0,0.02)] hover:shadow-[0_8px_30px_rgb(0,0,0,0.06)] hover:border-blue-100 transition-all duration-300 relative overflow-hidden"
      onClick={() => navigate(`/patient/${patient.id}`)}
    >
      {/* Decorative hover gradient */}
      <div className="absolute top-0 left-0 w-1 h-full bg-linear-to-b from-blue-400 to-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity" />

      {/* Header row */}
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-5">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h3 className="text-xl font-bold text-slate-800 group-hover:text-blue-600 transition-colors">
              {patient.name}
            </h3>
            {patient.riskTier !== 'safe' && (
              <AlertCircle size={18} className="text-orange-500" />
            )}
          </div>
          <p className="text-sm text-slate-500 font-medium">
            {patient.age} Yrs · {patient.ageProfile} Profile
          </p>
        </div>
        <div className="flex items-center gap-3 self-start">
          <RiskBadge tier={patient.riskTier} month3Protected={patient.month3Protected} />
          <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center group-hover:bg-blue-50 transition-colors">
            <ChevronRight size={18} className="text-slate-400 group-hover:text-blue-600 transition-colors" />
          </div>
        </div>
      </div>

      {/* Sync & symptom */}
      <div className="flex flex-wrap items-center gap-3 mb-6">
        <div className="flex items-center gap-1.5 text-xs text-slate-500 font-bold tracking-wide uppercase bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-100">
          <Wifi size={14} className="text-blue-500" />
          Sync: {patient.lastSyncLabel}
        </div>
        {patient.symptomReported && patient.symptomReported.length > 0 && (
          <div className="flex items-center gap-1.5 text-xs text-orange-700 bg-orange-50 px-3 py-1.5 rounded-lg font-bold border border-orange-100">
            <AlertCircle size={14} />
            {patient.symptomReported.join(', ')}
          </div>
        )}
      </div>

      {/* Metrics row */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-6 sm:gap-10 mb-6">
        <div className="bg-orange-50/50 rounded-xl p-3 border border-orange-100/50 flex-1 sm:flex-none">
          <p className="text-[10px] uppercase tracking-widest text-orange-600/70 font-bold mb-1">
            Current Streak
          </p>
          <div className="flex items-center gap-1.5">
            <Zap size={18} className="text-orange-500 fill-orange-500" />
            <span className="font-black text-slate-800 text-2xl leading-none">
              {patient.currentStreak}
            </span>
          </div>
        </div>
        <div className="flex-1 sm:flex-none">
          <p className="text-[10px] uppercase tracking-widest text-slate-400 font-bold mb-2">
            Heart Quota
          </p>
          <HeartQuota filled={patient.heartQuota} />
        </div>
      </div>

      {/* 7-day compliance */}
      <div className="pt-5 border-t border-slate-100">
        <p className="text-[10px] uppercase tracking-widest text-slate-400 font-bold mb-3">
          7-Day Compliance History
        </p>
        <div className="flex items-center gap-2 sm:gap-4">
          {patient.weeklyCompliance.map((d, i) => (
            <div key={i} className="flex flex-col items-center gap-2">
              <span className="text-[10px] text-slate-400 font-bold">{DAY_LABELS[i]}</span>
              <ComplianceDot status={d.status} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ─── Loading skeleton ─────────────────────────────────────────────────────────

function PatientCardSkeleton() {
  return (
    <div className="bg-white rounded-2xl p-6 border border-slate-100 shadow-sm animate-pulse">
      <div className="flex items-start justify-between mb-5">
        <div className="space-y-3">
          <div className="h-6 w-48 bg-slate-200 rounded-md" />
          <div className="h-4 w-24 bg-slate-100 rounded-md" />
        </div>
        <div className="h-8 w-20 bg-slate-100 rounded-full" />
      </div>
      <div className="h-6 w-32 bg-slate-100 rounded-lg mb-6" />
      <div className="flex gap-6 mb-6">
        <div className="space-y-2">
           <div className="h-3 w-20 bg-slate-100 rounded-md" />
           <div className="h-8 w-12 bg-slate-200 rounded-md" />
        </div>
        <div className="space-y-2">
           <div className="h-3 w-20 bg-slate-100 rounded-md" />
           <div className="h-8 w-32 bg-slate-100 rounded-md" />
        </div>
      </div>
      <div className="pt-5 border-t border-slate-50">
         <div className="h-3 w-40 bg-slate-100 rounded-md mb-4" />
         <div className="flex gap-3">
           {Array.from({ length: 7 }).map((_, i) => (
             <div key={i} className="w-8 h-8 rounded-full bg-slate-100" />
           ))}
         </div>
      </div>
    </div>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function PatientRoster() {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');
  const [filter, setFilter] = useState<RiskFilter>('all');
  const [search, setSearch] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setFetchError('');

    getAllPatients()
      .then(async (res) => {
        const enriched = await Promise.all(
          res.patients.map(async (summary) => {
            const [detailResult, statsResult, adherenceResult] = await Promise.allSettled([
              getPatient({ patient_id: summary.id }),
              getPatientStats({ patient_id: summary.id }),
              getPatientAdherenceMonth({ patient_id: summary.id, payload: { month: new Date().getMonth() + 1, year: new Date().getFullYear() } }),
            ]);

            const detail = detailResult.status === 'fulfilled' ? detailResult.value : null;
            const stats = statsResult.status === 'fulfilled' ? statsResult.value : null;
            const adherence = adherenceResult.status === 'fulfilled' ? adherenceResult.value : null;

            return buildPatientFromApi(summary, detail, stats, adherence);
          })
        );

        if (cancelled) return;
        setPatients(enriched);
      })
      .catch(() => {
        if (!cancelled) setFetchError('Failed to load patients. Please refresh.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => { cancelled = true; };
  }, []);

  const filtered = patients.filter((p) => {
    const matchSearch =
      !search ||
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.patientId.toLowerCase().includes(search.toLowerCase());
    const matchFilter =
      filter === 'all' ||
      (filter === 'high' && (p.riskTier === 'tier1' || p.riskTier === 'tier2' || p.riskTier === 'tier3')) ||
      (filter === 'low' && p.riskTier === 'safe');
    return matchSearch && matchFilter;
  });


  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden font-sans">
      <Sidebar
        onSearch={setSearch}
        onFilter={setFilter}
        activeFilter={filter}
      />

      <main className="flex-1 p-6 md:p-10 overflow-y-auto h-full scroll-smooth">
        <div className="max-w-5xl mx-auto">
          
          {/* Page header */}
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-8">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 bg-blue-100/50 text-blue-700 text-xs font-bold rounded-full mb-3">
                <Activity size={14} />
                Live Monitoring
              </div>
              <h1 className="text-3xl font-black tracking-tight text-slate-900">
                Patient Roster
              </h1>
              <p className="text-sm text-slate-500 mt-2 font-medium">
                {loading
                  ? 'Syncing active profiles…'
                  : `Tracking ${filtered.length} active patient${filtered.length !== 1 ? 's' : ''}`}
              </p>
            </div>
          </div>

          {/* Error banner */}
          {fetchError && (
            <div className="flex items-center gap-3 bg-red-50 text-red-600 border border-red-100 text-sm rounded-xl px-5 py-4 mb-8 shadow-sm">
              <AlertCircle size={20} className="shrink-0" />
              <span className="font-bold">{fetchError}</span>
            </div>
          )}

          {/* Cards Container */}
          <div className="flex flex-col gap-5">
            {loading ? (
              Array.from({ length: 3 }).map((_, i) => <PatientCardSkeleton key={i} />)
            ) : filtered.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-20 text-slate-400 bg-white rounded-3xl border border-slate-100 border-dashed">
                <div className="w-16 h-16 bg-slate-50 rounded-2xl flex items-center justify-center mb-4">
                  <AlertCircle size={32} className="text-slate-300" />
                </div>
                <p className="text-xl font-bold text-slate-600">No patients found</p>
                <p className="text-sm mt-2 font-medium">Try adjusting your search or filter parameters.</p>
              </div>
            ) : (
              filtered.map((p) => <PatientCard key={p.id} patient={p} />)
            )}
          </div>

        </div>
      </main>
    </div>
  );
}