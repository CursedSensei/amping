import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Zap, Wifi, AlertCircle, ChevronRight } from 'lucide-react';
import Sidebar from '../components/Sidebar';
import RiskBadge from '../components/RiskBadge';
import HeartQuota from '../components/HeartQuota';
import { type Patient } from '../data/mockData';
import { getPatients } from '../services/api';
import { toPatientListEntry } from '../services/adapters';

type RiskFilter = 'all' | 'high' | 'low';

const DAY_LABELS = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

// ─── Sub-components ───────────────────────────────────────────────────────────

function ComplianceDot({ status }: { status: 'done' | 'missed' | 'pending' }) {
  if (status === 'done')
    return (
      <div className="w-7 h-7 rounded-full bg-[#2e7d32] flex items-center justify-center shadow-[0px_2px_1px_-1px_rgba(0,0,0,0.2),0px_1px_1px_0px_rgba(0,0,0,0.14),0px_1px_3px_0px_rgba(0,0,0,0.12)]">
        <svg viewBox="0 0 12 12" className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M2 6l3 3 5-5" />
        </svg>
      </div>
    );
  if (status === 'missed')
    return (
      <div className="w-7 h-7 rounded-full bg-[#ffebee] border border-[#d32f2f] flex items-center justify-center">
        <svg viewBox="0 0 12 12" className="w-3.5 h-3.5 text-[#d32f2f]" fill="none" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" d="M3 3l6 6M9 3l-6 6" />
        </svg>
      </div>
    );
  return <div className="w-7 h-7 rounded-full border-[1.5px] border-dashed border-gray-400 bg-gray-50" />;
}

function PatientCard({ patient }: { patient: Patient }) {
  const navigate = useNavigate();

  return (
    <div
      className="bg-white rounded-[4px] shadow-[0px_2px_1px_-1px_rgba(0,0,0,0.2),0px_1px_1px_0px_rgba(0,0,0,0.14),0px_1px_3px_0px_rgba(0,0,0,0.12)] p-6 cursor-pointer hover:shadow-[0px_3px_3px_-2px_rgba(0,0,0,0.2),0px_3px_4px_0px_rgba(0,0,0,0.14),0px_1px_8px_0px_rgba(0,0,0,0.12)] transition-shadow duration-300 group"
      onClick={() => navigate(`/patient/${patient.id}`)}
    >
      {/* Header row */}
      <div className="flex items-start justify-between mb-4">
        <div>
          <div className="flex items-center gap-2 mb-0.5">
            <h3 className="text-[1.25rem] font-medium leading-tight text-black/87 group-hover:text-[#1976d2] transition-colors">
              {patient.name}
            </h3>
            {patient.riskTier !== 'safe' && (
              <AlertCircle size={16} className="text-[#ed6c02]" />
            )}
          </div>
          <p className="text-[0.875rem] text-black/60 font-normal">
            {patient.age} Yrs · {patient.ageProfile} Profile
          </p>
        </div>
        <div className="flex items-center gap-2">
          <RiskBadge tier={patient.riskTier} month3Protected={patient.month3Protected} />
          <ChevronRight size={20} className="text-black/30 group-hover:text-[#1976d2] transition-colors" />
        </div>
      </div>

      {/* Sync & symptom */}
      <div className="flex items-center gap-4 mb-5">
        <div className="flex items-center gap-1.5 text-[0.8125rem] text-black/60 font-medium">
          <Wifi size={14} />
          Sync: {patient.lastSyncLabel}
        </div>
        {patient.symptomReported && patient.symptomReported.length > 0 && (
          <div className="flex items-center gap-1.5 text-[0.8125rem] text-[#e65100] bg-[#fff3e0] px-3 py-1 rounded-[16px] font-medium">
            <AlertCircle size={14} />
            {patient.symptomReported.join(', ')}
          </div>
        )}
      </div>

      {/* Metrics row */}
      <div className="flex items-center gap-6 mb-5">
        <div>
          <p className="text-[0.75rem] uppercase tracking-[0.08333em] text-black/60 font-medium mb-1">
            Current Streak
          </p>
          <div className="flex items-center gap-1.5">
            <Zap size={16} className="text-[#ed6c02] fill-[#ed6c02]" />
            <span className="font-medium text-black/87 text-[1.25rem] leading-none">
              {patient.currentStreak}
            </span>
          </div>
        </div>
        <div>
          <p className="text-[0.75rem] uppercase tracking-[0.08333em] text-black/60 font-medium mb-1">
            Heart Quota
          </p>
          <HeartQuota filled={patient.heartQuota} />
        </div>
      </div>

      {/* 7-day compliance */}
      <div>
        <p className="text-[0.75rem] uppercase tracking-[0.08333em] text-black/60 font-medium mb-2">
          7-Day Compliance History
        </p>
        <div className="flex items-center gap-1.5">
          {patient.weeklyCompliance.map((d, i) => (
            <div key={i} className="flex flex-col items-center gap-1">
              <span className="text-[0.6875rem] text-black/60 font-medium">{DAY_LABELS[i]}</span>
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
    <div className="bg-white rounded-[4px] shadow-[0px_2px_1px_-1px_rgba(0,0,0,0.2),0px_1px_1px_0px_rgba(0,0,0,0.14),0px_1px_3px_0px_rgba(0,0,0,0.12)] p-6 animate-pulse">
      <div className="flex items-start justify-between mb-4">
        <div className="space-y-2.5">
          <div className="h-6 w-40 bg-black/10 rounded-[4px]" />
          <div className="h-4 w-24 bg-black/10 rounded-[4px]" />
        </div>
        <div className="h-6 w-16 bg-black/10 rounded-[16px]" />
      </div>
      <div className="h-4 w-32 bg-black/10 rounded-[4px] mb-5" />
      <div className="flex gap-6 mb-5">
        <div className="space-y-2">
           <div className="h-3 w-20 bg-black/10 rounded-[4px]" />
           <div className="h-6 w-12 bg-black/10 rounded-[4px]" />
        </div>
        <div className="space-y-2">
           <div className="h-3 w-20 bg-black/10 rounded-[4px]" />
           <div className="h-6 w-24 bg-black/10 rounded-[4px]" />
        </div>
      </div>
      <div>
         <div className="h-3 w-32 bg-black/10 rounded-[4px] mb-3" />
         <div className="flex gap-1.5">
           {Array.from({ length: 7 }).map((_, i) => (
             <div key={i} className="w-7 h-7 rounded-full bg-black/10" />
           ))}
         </div>
      </div>
    </div>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function PatientRoster({ onLogout }: { onLogout?: () => void }) {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');
  const [filter, setFilter] = useState<RiskFilter>('all');
  const [search, setSearch] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setFetchError('');

    getPatients()
      .then((res) => {
        if (cancelled) return;
        setPatients(res.patients.map((p) => toPatientListEntry(p) as Patient));
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
    <div className="flex h-screen bg-[#f5f5f5] overflow-hidden">
      <Sidebar
        onSearch={setSearch}
        onFilter={setFilter}
        activeFilter={filter}
        onLogout={onLogout}
      />

      <main className="flex-1 p-8 overflow-y-auto h-full">
        {/* Page header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-[1.5rem] font-normal tracking-tight text-black/87">
              Patient Roster
            </h1>
            <p className="text-[0.875rem] text-black/60 mt-1">
              {loading
                ? 'Loading…'
                : `${filtered.length} patient${filtered.length !== 1 ? 's' : ''} shown`}
            </p>
          </div>
        </div>

        {/* Error banner */}
        {fetchError && (
          <div className="flex items-center gap-3 bg-[#ffebee] text-[#d32f2f] text-[0.875rem] rounded-[4px] px-4 py-3 mb-6 shadow-[0px_2px_1px_-1px_rgba(0,0,0,0.2),0px_1px_1px_0px_rgba(0,0,0,0.14),0px_1px_3px_0px_rgba(0,0,0,0.12)]">
            <AlertCircle size={20} className="shrink-0" />
            <span className="font-medium">{fetchError}</span>
          </div>
        )}

        {/* Cards */}
        <div className="flex flex-col gap-4">
          {loading ? (
            Array.from({ length: 3 }).map((_, i) => <PatientCardSkeleton key={i} />)
          ) : filtered.length === 0 ? (
            <div className="text-center py-16 text-black/60">
              <p className="text-[1.25rem] font-medium">No patients found</p>
              <p className="text-[0.875rem] mt-1">Try adjusting your search or filter.</p>
            </div>
          ) : (
            filtered.map((p) => <PatientCard key={p.id} patient={p} />)
          )}
        </div>
      </main>
    </div>
  );
}