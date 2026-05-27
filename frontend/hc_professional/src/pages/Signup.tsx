import { AlertCircle, CheckCircle2, Eye, EyeOff, Loader2, ShieldCheck } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const ROLES = ['Doctor / Physician', 'Nurse', 'Barangay Health Worker (BHW)', 'Health Center Admin'];

export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    role: '',
    employeeId: '',
    password: '',
    confirmPassword: '',
  });
  const [showPass, setShowPass] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const set = (field: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!form.fullName.trim() || !form.email.trim() || !form.role || !form.password) {
      setError('Please fill in all required fields.');
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      setError('Please enter a valid email address.');
      return;
    }
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
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
    // onLogin();
    navigate('/', { replace: true });
  };

  const passwordStrength = (p: string) => {
    if (!p) return null;
    if (p.length < 6) return { label: 'Weak', color: 'bg-red-500', width: '25%' };
    if (p.length < 8) return { label: 'Fair', color: 'bg-yellow-400', width: '50%' };
    if (p.length < 12 || !/[0-9]/.test(p)) return { label: 'Good', color: 'bg-blue-400', width: '75%' };
    return { label: 'Strong', color: 'bg-emerald-500', width: '100%' };
  };
  const strength = passwordStrength(form.password);

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
              <h2 className="text-xl font-bold text-white">Account Created!</h2>
              <p className="text-blue-300 text-sm">Signing you in…</p>
            </div>
          ) : (
            <>
              <h2 className="text-xl font-bold text-white mb-1">Create account</h2>
              <p className="text-blue-300 text-sm mb-6">Register as a healthcare professional</p>

              {error && (
                <div className="flex items-center gap-2 bg-red-500/20 border border-red-400/40 text-red-200 text-sm rounded-lg px-3 py-2.5 mb-5">
                  <AlertCircle size={15} className="shrink-0" />
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                {/* Full Name */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Full Name <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <input
                    type="text"
                    value={form.fullName}
                    onChange={set('fullName')}
                    placeholder="Dr. Juan dela Cruz"
                    autoComplete="name"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
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
                    placeholder="doctor@clinic.ph"
                    autoComplete="email"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Role */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Role <span className="text-blue-400 normal-case font-normal">(required)</span>
                  </label>
                  <select
                    value={form.role}
                    onChange={set('role')}
                    className="w-full bg-blue-900/60 border border-white/20 text-white rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  >
                    <option value="" disabled className="text-gray-500">Select your role…</option>
                    {ROLES.map((r) => (
                      <option key={r} value={r} className="text-gray-900 bg-white">{r}</option>
                    ))}
                  </select>
                </div>

                {/* Employee ID (optional) */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Employee / License ID <span className="text-blue-400 normal-case font-normal">(optional)</span>
                  </label>
                  <input
                    type="text"
                    value={form.employeeId}
                    onChange={set('employeeId')}
                    placeholder="PRC-12345 or HR-0001"
                    className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                  />
                </div>

                {/* Password */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Password <span className="text-blue-400 normal-case font-normal">(min. 8 characters)</span>
                  </label>
                  <div className="relative">
                    <input
                      type={showPass ? 'text' : 'password'}
                      value={form.password}
                      onChange={set('password')}
                      placeholder="••••••••"
                      autoComplete="new-password"
                      className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 pr-11 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPass((v) => !v)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-blue-300 hover:text-white transition-colors"
                    >
                      {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                  {/* Strength bar */}
                  {strength && (
                    <div className="mt-1.5">
                      <div className="h-1 rounded-full bg-white/20 overflow-hidden">
                        <div
                          className={`h-full rounded-full transition-all duration-300 ${strength.color}`}
                          style={{ width: strength.width }}
                        />
                      </div>
                      <p className="text-[11px] text-blue-300 mt-1">{strength.label} password</p>
                    </div>
                  )}
                </div>

                {/* Confirm Password */}
                <div>
                  <label className="block text-xs font-semibold text-blue-200 uppercase tracking-wider mb-1.5">
                    Confirm Password
                  </label>
                  <div className="relative">
                    <input
                      type={showConfirm ? 'text' : 'password'}
                      value={form.confirmPassword}
                      onChange={set('confirmPassword')}
                      placeholder="••••••••"
                      autoComplete="new-password"
                      className="w-full bg-white/10 border border-white/20 text-white placeholder-blue-400/60 rounded-xl px-4 py-3 pr-11 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirm((v) => !v)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-blue-300 hover:text-white transition-colors"
                    >
                      {showConfirm ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                  {form.confirmPassword && form.password !== form.confirmPassword && (
                    <p className="text-[11px] text-red-300 mt-1">Passwords do not match</p>
                  )}
                  {form.confirmPassword && form.password === form.confirmPassword && form.confirmPassword.length > 0 && (
                    <p className="text-[11px] text-emerald-400 mt-1">✓ Passwords match</p>
                  )}
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
                      Creating account…
                    </>
                  ) : (
                    'Create Account'
                  )}
                </button>
              </form>

              <div className="mt-6 text-center text-sm text-blue-300">
                Already have an account?{' '}
                <Link
                  to="/login"
                  className="text-white font-semibold hover:text-blue-200 transition-colors underline underline-offset-2"
                >
                  Sign in
                </Link>
              </div>
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
