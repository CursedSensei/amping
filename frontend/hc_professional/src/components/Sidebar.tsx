import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Search, Shield, Users, AlertTriangle, LogOut } from 'lucide-react';
import { MOCK_PATIENTS } from '../data/mockData';

type RiskFilter = 'all' | 'high' | 'low';

interface SidebarProps {
  onSearch?: (q: string) => void;
  onFilter?: (f: RiskFilter) => void;
  activeFilter?: RiskFilter;
  onLogout?: () => void;
}

export default function Sidebar({ onSearch, onFilter, activeFilter = 'all', onLogout }: SidebarProps) {
  const [query, setQuery] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  const handleSearch = (val: string) => {
    setQuery(val);
    onSearch?.(val);
  };

  const isRoster = location.pathname === '/';
  const isRisk = location.pathname === '/risk';

  const highRiskCount = MOCK_PATIENTS.filter(
    (p) => p.riskTier === 'tier2' || p.riskTier === 'tier3'
  ).length;

  return (
    <aside className="w-64 min-h-screen bg-[#0f1117] text-white flex flex-col shrink-0">
      {/* Brand */}
      <div className="p-5 border-b border-white/10">
        <div className="flex items-center gap-2 mb-1">
          <div className="w-7 h-7 rounded-lg bg-blue-500 flex items-center justify-center">
            <Shield size={14} className="text-white" />
          </div>
          <span className="font-semibold text-sm">Provider Console</span>
        </div>
        <p className="text-xs text-gray-400 ml-9">Actionable Analytics</p>
      </div>

      {/* Nav */}
      <nav className="p-3 flex-1">
        <p className="text-[10px] uppercase tracking-widest text-gray-500 px-2 mb-2">Navigation</p>

        <button
          onClick={() => navigate('/')}
          className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm mb-1 transition-colors ${
            isRoster
              ? 'bg-blue-600 text-white'
              : 'text-gray-300 hover:bg-white/5'
          }`}
        >
          <Users size={15} />
          Patient Roster
          <span className="ml-auto text-[11px] bg-white/10 px-1.5 py-0.5 rounded-full">
            {MOCK_PATIENTS.length}
          </span>
        </button>

        <button
          onClick={() => navigate('/risk')}
          className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-colors ${
            isRisk
              ? 'bg-blue-600 text-white'
              : 'text-gray-300 hover:bg-white/5'
          }`}
        >
          <AlertTriangle size={15} />
          Risk Stratification
          {highRiskCount > 0 && (
            <span className="ml-auto text-[11px] bg-red-500 text-white px-1.5 py-0.5 rounded-full">
              {highRiskCount}
            </span>
          )}
        </button>
      </nav>

      {/* Search + Filter — only on roster */}
      {isRoster && (
        <div className="p-3 border-t border-white/10">
          <p className="text-[10px] uppercase tracking-widest text-gray-500 px-2 mb-2">Search Patient</p>
          <div className="relative mb-3">
            <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
            <input
              type="text"
              placeholder="Name or ID..."
              value={query}
              onChange={(e) => handleSearch(e.target.value)}
              className="w-full bg-white/5 border border-white/10 rounded-lg pl-8 pr-3 py-1.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>

          <p className="text-[10px] uppercase tracking-widest text-gray-500 px-2 mb-2">Filter by Risk</p>
          {(['all', 'high', 'low'] as RiskFilter[]).map((f) => (
            <button
              key={f}
              onClick={() => onFilter?.(f)}
              className={`w-full text-left px-3 py-1.5 rounded-lg text-sm mb-1 transition-colors ${
                activeFilter === f
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-white/5'
              }`}
            >
              {f === 'all' ? 'All Risk' : f === 'high' ? 'High Risk' : 'Low Risk'}
            </button>
          ))}
        </div>
      )}

      {/* Footer */}
      <div className="p-4 border-t border-white/10">
        <div className="flex items-center gap-2 text-gray-400 text-sm">
          <div className="w-7 h-7 rounded-full bg-blue-700 flex items-center justify-center text-xs font-bold text-white">
            DR
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-white text-xs font-medium truncate">Dr. Alicia Tan</p>
            <p className="text-[11px] text-gray-500 truncate">Lapu-Lapu City HO</p>
          </div>
          <LogOut size={14} className="shrink-0 cursor-pointer hover:text-white transition-colors" onClick={onLogout} />
        </div>
      </div>
    </aside>
  );
}
