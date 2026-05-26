import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, ShieldCheck, AlertCircle, CheckCircle2, Loader2, CalendarDays } from 'lucide-react';

const ROLES = ['Doctor / Physician', 'Nurse', 'Barangay Health Worker (BHW)', 'Health Center Admin'];

function computeAge(birthdate: string): number | null {
  if (!birthdate) return null;
  const birth = new Date(birthdate);
  if (isNaN(birth.getTime())) return null;
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age -= 1;
  }
  return age >= 0 ? age : null;
}

export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstname: '',
    lastname: '',
    contact: '',
    email: '',
    birthdate: '',
    employeeId: '',
    guardianfirstname: '',
    guardianlastname: '',
    guardiancontact: '',
    guardianemail: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const set = (field: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }));

  const age = computeAge(form.birthdate);

  // Clamp max date to today so future birthdates are not selectable
  const todayISO = new Date().toISOString().split('T')[0];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!form.firstname.trim() || !form.lastname.trim() || !form.birthdate.trim() || !form.email.trim() || !form.contact.trim() || !form.employeeId.trim() || !form.guardianfirstname.trim() || !form.guardianlastname.trim() || !form.guardianemail.trim() || !form.guardiancontact.trim()) {
      setError('Please fill in all required fields.');
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      setError('Please enter a valid email address.');
      return;
    }

    // Simulate network call — no backend yet
    setLoading(true);
    await new Promise((r) => setTimeout(r, 1100));
    setLoading(false);
    setDone(true);

    // Auto-login after signup
    await new Promise((r) => setTimeout(r, 1400));
    sessionStorage.setItem('hc_auth', 'true');
    navigate('/', { replace: true });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-950 via-blue-900 to-indigo-900 flex items-center justify-center p-4">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-32 -right-32 w-96 h-96 rounded-full bg-blue-700/20 blur-3xl" />
        <div className="absolute -bottom-32 -left-32 w-96 h-96 rounded-full bg-indigo-600/20 blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        {/* Logo / Brand */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-white/10 backdrop-blur border border-white/20 mb-4">
            <ShieldCheck size={32} className="text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">AMPING</h1>
          <p className="text-blue-300 text-sm mt-1">Healthcare Professional Portal</p>
        </div>

        {/* Card */}
        <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-8 shadow-2xl">
          {done ? (
            <div className="flex flex-col items-center py-6 gap-4 text-center">
              <CheckCircle2 size={48} className="text-emerald-400" />
              <h2 className="text-xl font-bold text-white">Patient Registered!</h2>
            </div>
          ) : (
            <>
              <h2 className="text-xl font-bold text-white mb-1">Register Patient</h2>
              <p className="text-blue-300 text-sm mb-6">Register a new patient under a healthcare professional</p>

              {error && (
                <div className="flex items-center gap-2 bg-red-500/20 border border-red-400/40 text-red-200 text-sm rounded-lg px-3 py-2.5 mb-5">
                  <AlertCircle size={15} className="shrink-0" />
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                {/* First Name */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    First Name <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.firstname}
                    onChange={set('firstname')}
                    placeholder="Juana"
                    autoComplete="name"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>
                {/* Last Name */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Last Name <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.lastname}
                    onChange={set('lastname')}
                    placeholder="Dela Cruz"
                    autoComplete="name"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Birthdate + Age */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Date of Birth <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <div className="flex gap-3 items-stretch">
                    {/* Calendar date input */}
                    <div className="relative flex-1">
                      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-blue-300 pointer-events-none">
                        <CalendarDays size={15} />
                      </span>
                      <input
                        type="date"
                        value={form.birthdate}
                        onChange={set('birthdate')}
                        max={todayISO}
                        className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl pl-9 pr-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition
                          [color-scheme:dark]
                          [&::-webkit-calendar-picker-indicator]:opacity-0
                          [&::-webkit-calendar-picker-indicator]:absolute
                          [&::-webkit-calendar-picker-indicator]:inset-0
                          [&::-webkit-calendar-picker-indicator]:w-full
                          [&::-webkit-calendar-picker-indicator]:cursor-pointer"
                      />
                    </div>

                    {/* Computed Age display */}
                    <div className="flex flex-col items-center justify-center bg-white/10 border border-white/20 rounded-xl px-4 py-2 min-w-[80px] select-none">
                      {age !== null ? (
                        <>
                          <span className="text-2xl font-bold text-white leading-none">{age}</span>
                          <span className="text-blue-300 text-xs mt-0.5">yrs old</span>
                        </>
                      ) : (
                        <>
                          <span className="text-2xl font-bold text-white/20 leading-none">—</span>
                          <span className="text-blue-400/50 text-xs mt-0.5">age</span>
                        </>
                      )}
                    </div>
                  </div>
                </div>

                {/* Email */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Email Address <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="email"
                    value={form.email}
                    onChange={set('email')}
                    placeholder="JuanaDelaCruz@gmail.com"
                    autoComplete="email"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Contact Number */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Contact Number <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.contact}
                    onChange={set('contact')}
                    placeholder="09123456789"
                    autoComplete="tel"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Employee ID*/}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Healthcare Provider ID <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.employeeId}
                    onChange={set('employeeId')}
                    placeholder="PRC-12345 or HR-0001"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Guardian Firstname*/}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Guardian Firstname <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.guardianfirstname}
                    onChange={set('guardianfirstname')}
                    placeholder="John"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Guardian Lastname*/}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Guardian Lastname <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.guardianlastname}
                    onChange={set('guardianlastname')}
                    placeholder="Doe"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Guardian Email */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Guardian Email <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="email"
                    value={form.guardianemail}
                    onChange={set('guardianemail')}
                    placeholder="JohnDoe@gmail.com"
                    autoComplete="email"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Guardian Contact Number */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Guardian Contact Number <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.guardiancontact}
                    onChange={set('guardiancontact')}
                    placeholder="09987654321"
                    autoComplete="tel"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Submit */}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-500 hover:bg-blue-400 disabled:bg-blue-800 disabled:cursor-not-allowed text-white font-semibold rounded-xl py-3 text-sm transition-colors flex items-center justify-center gap-2 mt-2"
                >
                  {loading ? (
                    <>
                      <Loader2 size={16} className="animate-spin" />
                      Registering patient…
                    </>
                  ) : (
                    'Register Patient'
                  )}
                </button>
              </form>
            </>
          )}
        </div>

        <p className="text-center text-blue-500 text-xs mt-6">
          For authorized healthcare personnel only · AMPING TB Program
        </p>
      </div>
    </div>
  );
}
