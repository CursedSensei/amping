import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Zap, Wifi, AlertCircle, ChevronRight, LogOut } from 'lucide-react';
import Sidebar from '../components/Sidebar';
import RiskBadge from '../components/RiskBadge';
import HeartQuota from '../components/HeartQuota';
import { MOCK_PATIENTS, type Patient } from '../data/mockData';

type RiskFilter = 'all' | 'high' | 'low';

const DAY_LABELS = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

function ComplianceDot({ status }: { status: 'done' | 'missed' | 'pending' }) {
  if (status === 'done')
    return (
      <div className="w-7 h-7 rounded-full bg-emerald-500 flex items-center justify-center">
        <svg viewBox="0 0 12 12" className="w-3 h-3 text-white" fill="none" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M2 6l3 3 5-5" />
        </svg>
      </div>
    );
  if (status === 'missed')
    return (
      <div className="w-7 h-7 rounded-full bg-red-100 border-2 border-red-400 flex items-center justify-center">
        <svg viewBox="0 0 12 12" className="w-3 h-3 text-red-500" fill="none" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" d="M3 3l6 6M9 3l-6 6" />
        </svg>
      </div>
    );
  return <div className="w-7 h-7 rounded-full border-2 border-dashed border-gray-300 bg-gray-50" />;
}

function PatientCard({ patient }: { patient: Patient }) {
  const navigate = useNavigate();

  return (
    <div
      className="bg-white rounded-xl border border-gray-100 p-5 cursor-pointer hover:shadow-md hover:border-blue-200 transition-all group"
      onClick={() => navigate(`/patient/${patient.id}`)}
    >
      {/* Header row */}
      <div className="flex items-start justify-between mb-3">
        <div>
          <div className="flex items-center gap-2 mb-0.5">
            <h3 className="font-semibold text-gray-900 text-base group-hover:text-blue-700 transition-colors">
              {patient.name}
            </h3>
            {patient.riskTier !== 'safe' && (
              <AlertCircle size={14} className="text-orange-400" />
            )}
          </div>
          <p className="text-xs text-gray-400">
            {patient.age} Yrs · {patient.ageProfile} Profile
          </p>
        </div>
        <div className="flex items-center gap-2">
          <RiskBadge tier={patient.riskTier} month3Protected={patient.month3Protected} />
          <ChevronRight size={16} className="text-gray-300 group-hover:text-blue-400 transition-colors" />
        </div>
      </div>

      {/* Sync & symptom */}
      <div className="flex items-center gap-3 mb-4">
        <div className="flex items-center gap-1.5 text-[11px] text-gray-400">
          <Wifi size={11} />
          Sync: {patient.lastSyncLabel}
        </div>
        {patient.symptomReported && (
          <div className="flex items-center gap-1.5 text-[11px] text-amber-600 bg-amber-50 border border-amber-100 px-2 py-0.5 rounded-full">
            <AlertCircle size={10} />
            {patient.symptomReported}
          </div>
        )}
      </div>

      {/* Metrics row */}
      <div className="flex items-center gap-5 mb-4">
        <div>
          <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Current Streak</p>
          <div className="flex items-center gap-1">
            <Zap size={13} className="text-yellow-500 fill-yellow-400" />
            <span className="font-bold text-gray-900 text-lg leading-none">{patient.currentStreak}</span>
          </div>
        </div>
        <div>
          <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-1">Heart Quota</p>
          <HeartQuota filled={patient.heartQuota} />
        </div>
      </div>

      {/* 7-day compliance */}
      <div>
        <p className="text-[10px] uppercase tracking-wider text-gray-400 mb-2">7-Day Compliance History</p>
        <div className="flex items-center gap-1">
          {patient.weeklyCompliance.map((d, i) => (
            <div key={i} className="flex flex-col items-center gap-1">
              <span className="text-[9px] text-gray-400 font-medium">{DAY_LABELS[i]}</span>
              <ComplianceDot status={d.status} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default function PatientRoster() {
  const [filter, setFilter] = useState<RiskFilter>('all');
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const filtered = MOCK_PATIENTS.filter((p) => {
    const matchSearch =
      !search ||
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.patientId.toLowerCase().includes(search.toLowerCase());
    const matchFilter =
      filter === 'all' ||
      (filter === 'high' && (p.riskTier === 'tier2' || p.riskTier === 'tier3' || p.riskTier === 'tier1')) ||
      (filter === 'low' && p.riskTier === 'safe');
    return matchSearch && matchFilter;
  });

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar
        onSearch={setSearch}
        onFilter={setFilter}
        activeFilter={filter}
      />

      <main className="flex-1 p-8 overflow-y-auto">
        {/* Page header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Patient Roster</h1>
            <p className="text-sm text-gray-400 mt-0.5">
              {filtered.length} patient{filtered.length !== 1 ? 's' : ''} shown
            </p>
          </div>
          <button
            onClick={() => navigate('/risk')}
            className="flex items-center gap-2 bg-white border border-gray-200 text-gray-600 text-sm px-4 py-2 rounded-lg hover:bg-gray-50 hover:border-gray-300 transition-colors"
          >
            <LogOut size={14} />
            Risk Center
          </button>
        </div>

        {/* Cards grid */}
        <div className="flex flex-col gap-4">
          {filtered.length === 0 ? (
            <div className="text-center py-16 text-gray-400">
              <p className="text-lg font-medium">No patients found</p>
              <p className="text-sm mt-1">Try adjusting your search or filter.</p>
            </div>
          ) : (
            filtered.map((p) => <PatientCard key={p.id} patient={p} />)
          )}
        </div>
      </main>
    </div>
  );
}
