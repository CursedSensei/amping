import { AlertCircle, AlertTriangle, CheckCircle2, LogOut, Search, ShieldCheck, Users } from 'lucide-react';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { usePatients } from '../context/PatientContext';

type RiskFilter = 'all' | 'high' | 'low';

interface SidebarProps {
  onSearch?: (q: string) => void;
  onFilter?: (f: RiskFilter) => void;
  activeFilter?: RiskFilter;
}

export default function Sidebar({ onSearch, onFilter, activeFilter = 'all' }: SidebarProps) {
  const [query, setQuery] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  const { logout } = useAuth();
  const { patients } = usePatients();

  const handleSearch = (val: string) => {
    setQuery(val);
    onSearch?.(val);
  };

  const isRoster = location.pathname === '/';
  const isRisk = location.pathname === '/risk';

  const highRiskCount = patients.filter(
    (p) => p.riskTier === 'tier2' || p.riskTier === 'tier3'
  ).length;

  return (
    <aside className="relative w-72 min-h-screen bg-[#0A0F24] flex flex-col shrink-0 overflow-hidden border-r border-white/5">
      
      {/* Ambient Background Glows (Matching Login Page) */}
      <div className="absolute inset-0 pointer-events-none">
        <div
          className="absolute top-0 left-0 w-full h-full"
          style={{ backgroundImage: 'linear-gradient(to bottom right, rgba(37, 99, 235, 0.10), rgba(147, 51, 234, 0.10))' }}
        />
        <div className="absolute -top-24 -left-24 w-64 h-64 rounded-full bg-blue-500/10 blur-[80px]" />
      </div>

      {/* Relative wrapper keeps content above the ambient glows */}
      <div className="relative z-10 flex flex-col h-full">
        
        {/* Brand */}
        <div className="p-6 border-b border-white/10">
          <div className="flex items-center gap-3 mb-1">
            <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-white/10 backdrop-blur border border-white/20">
              <ShieldCheck size={20} className="text-blue-300" />
            </div>
            <div>
              <h1 className="text-lg font-black text-white tracking-widest uppercase leading-none">Amping</h1>
              <span className="text-xs text-blue-300/80 font-medium tracking-wide">Provider Console</span>
            </div>
          </div>
        </div>

        {/* Nav */}
        <nav className="p-4 flex-none space-y-1">
          <p className="text-[10px] uppercase tracking-widest text-slate-500 font-bold mb-3 px-2 mt-2">
            Navigation
          </p>

          <button
            onClick={() => navigate('/')}
            className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold transition-all ${
              isRoster
                ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20'
                : 'text-slate-400 hover:text-white hover:bg-white/5'
            }`}
          >
            <Users size={18} className={isRoster ? 'text-white' : 'text-slate-500'} />
            Patient Roster
            <span className={`ml-auto text-[11px] px-2 py-0.5 rounded-full ${isRoster ? 'bg-white/20' : 'bg-white/10'}`}>
              {patients.length}
            </span>
          </button>

          <button
            onClick={() => navigate('/risk')}
            className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold transition-all ${
              isRisk
                ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20'
                : 'text-slate-400 hover:text-white hover:bg-white/5'
            }`}
          >
            <AlertTriangle size={18} className={isRisk ? 'text-white' : 'text-slate-500'} />
            Risk Stratification
            {highRiskCount > 0 && (
              <span className="ml-auto text-[11px] bg-orange-500 text-white px-2 py-0.5 rounded-full shadow-sm">
                {highRiskCount}
              </span>
            )}
          </button>
        </nav>

        {/* Search + Filter — only on roster */}
        {isRoster && (
          <div className="p-4 border-t border-white/5 flex-1 overflow-y-auto">
            
            <p className="text-[10px] uppercase tracking-widest text-slate-500 font-bold mb-3 px-2">
              Search Patient
            </p>
            <div className="relative mb-6">
              <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                placeholder="Name or ID..."
                value={query}
                onChange={(e) => handleSearch(e.target.value)}
                className="w-full bg-white/5 border border-white/10 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all shadow-inner"
              />
            </div>

            <p className="text-[10px] uppercase tracking-widest text-slate-500 font-bold mb-3 px-2">
              Filter by Risk
            </p>
            <div className="space-y-1">
              <button
                onClick={() => onFilter?.('all')}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold transition-all ${
                  activeFilter === 'all'
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`}
              >
                <Users size={16} className={activeFilter === 'all' ? 'text-white' : 'text-slate-500'} />
                All Risk
              </button>

              <button
                onClick={() => onFilter?.('high')}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold transition-all ${
                  activeFilter === 'high'
                    ? 'bg-orange-500/20 text-orange-400 border border-orange-500/20'
                    : 'text-slate-400 hover:text-orange-400 hover:bg-white/5'
                }`}
              >
                <AlertCircle size={16} className={activeFilter === 'high' ? 'text-orange-400' : 'text-slate-500'} />
                High Risk
              </button>

              <button
                onClick={() => onFilter?.('low')}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold transition-all ${
                  activeFilter === 'low'
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/20'
                    : 'text-slate-400 hover:text-emerald-400 hover:bg-white/5'
                }`}
              >
                <CheckCircle2 size={16} className={activeFilter === 'low' ? 'text-emerald-400' : 'text-slate-500'} />
                Low Risk
              </button>
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="p-4 border-t border-white/10 mt-auto bg-black/10">
          <div className="flex items-center gap-3 text-slate-400">
            <div
              className="w-9 h-9 rounded-full flex items-center justify-center text-xs font-bold text-white shadow-md"
              style={{ backgroundImage: 'linear-gradient(to top right, rgb(37, 99, 235), rgb(99, 102, 241))' }}
            >
              AT
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-white text-sm font-bold truncate">Dr. Alicia Tan</p>
              <p className="text-[11px] text-blue-300/70 font-medium truncate">Lapu-Lapu City HO</p>
            </div>
            <button 
              onClick={logout}
              className="p-2 rounded-lg hover:bg-white/10 hover:text-white transition-colors focus:outline-none"
              title="Log out"
            >
              <LogOut size={16} className="shrink-0" />
            </button>
          </div>
        </div>

      </div>
    </aside>
  );
}