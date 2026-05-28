import { useState, useRef, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, ShieldCheck, AlertCircle, Loader2, Activity, HeartPulse, Stethoscope, Pill } from 'lucide-react';
import Gabby from '../components/Gabby';
import axios from 'axios';
import { Field, Label, Input, Button } from '@headlessui/react';
import gsap from 'gsap';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [currentSlide, setCurrentSlide] = useState(0);

  const SLIDES = [
    {
      id: 1,
      line1: "Meet Gabby:",
      highlight: "Your AI",
      line2: "Companion.",
      desc: "Gabby talks to patients every day, checking their mood and cheering them on. It's like having a friendly helper instead of a strict alarm.",
      features: [
        { icon: <Pill size={20} className="text-purple-400" />, iconBg: "bg-purple-500/10 border-purple-500/20", title: "Fun Quests", desc: "Earn points, keep up streaks, and unlock badges!" },
        { icon: <HeartPulse size={20} className="text-emerald-400" />, iconBg: "bg-emerald-500/10 border-emerald-500/20", title: "Personalized Care", desc: "Friendly chats that adjust to exactly what the patient needs." }
      ]
    },
    {
      id: 2,
      line1: "Making",
      highlight: "TB Treatment",
      line2: "A Fun Quest.",
      desc: "We turn a tough daily routine into an engaging experience. Amping lets patients record themselves taking medicine right on their phone.",
      features: [
        { icon: <Activity size={20} className="text-blue-400" />, iconBg: "bg-blue-500/10 border-blue-500/20", title: "Easy Recording", desc: "Just point the camera and take your meds—no hassle." },
        { icon: <ShieldCheck size={20} className="text-indigo-400" />, iconBg: "bg-indigo-500/10 border-indigo-500/20", title: "Safe & Private", desc: "Everything is kept completely secure and confidential." }
      ]
    },
    {
      id: 3,
      line1: "Empowering",
      highlight: "Healthcare",
      line2: "Workers.",
      desc: "A simple screen where doctors can easily watch videos of patients taking their meds and notice if anyone needs extra help.",
      features: [
        { icon: <Stethoscope size={20} className="text-rose-400" />, iconBg: "bg-rose-500/10 border-rose-500/20", title: "Watch Anytime", desc: "Review patients' videos whenever you have time." },
        { icon: <Activity size={20} className="text-amber-400" />, iconBg: "bg-amber-500/10 border-amber-500/20", title: "Smart Alerts", desc: "Catch problems early before patients fall behind." }
      ]
    }
  ];

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % SLIDES.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [SLIDES.length]);

  // GSAP Animation Ref
  const heroRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Context keeps animations scoped and makes cleanup easy for React strict mode
    const ctx = gsap.context(() => {
      // 1. Staggered entrance for the sparse pitch lines
      gsap.from('.hero-line', {
        y: 30,
        opacity: 0,
        duration: 1,
        stagger: 0.25,
        ease: 'power3.out',
        delay: 0.2,
      });

      // 2. Animate the popping chat bubbles
      const bubbles = gsap.utils.toArray('.pop-bubble');
      
      // Initial pop-in
      gsap.from(bubbles, {
        scale: 0,
        opacity: 0,
        duration: 0.8,
        stagger: {
          each: 0.3,
          from: 'random',
        },
        ease: 'back.out(1.5)',
        delay: 0.5,
      });

      // Continuous sporadic floating
      bubbles.forEach((bubble: any) => {
        gsap.to(bubble, {
          y: 'random(-15, 15)',
          x: 'random(-15, 15)',
          rotation: 'random(-5, 5)',
          duration: 'random(2, 4)',
          repeat: -1,
          yoyo: true,
          ease: 'sine.inOut',
          delay: 'random(0, 1)',
        });
      });
    }, heroRef);

    return () => ctx.revert();
  }, []);

  const { login: apiLogin } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email.trim() || !password.trim()) {
      setError('Please fill in all fields.');
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Please enter a valid email address.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setLoading(true);
    try {
      await apiLogin(email, password);
      navigate('/', { replace: true });
    } catch (err) {
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 400) {
          // Already authenticated — just go home
          navigate('/', { replace: true });
        } else if (err.response?.status === 401) {
          setError('Invalid email or password.');
        } else {
          setError('Something went wrong. Please try again.');
        }
      } else {
        setError('Unable to connect. Check your network and try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col lg:flex-row bg-slate-50 overflow-hidden">
      
      {/* Left Panel: Hero & Immersive Context */}
      <div 
        ref={heroRef}
        className="relative w-full lg:w-5/12 bg-[#0A0F24] flex flex-col justify-center p-8 lg:p-16 overflow-hidden min-h-[50vh] lg:min-h-screen"
      >
        {/* Animated Background Gradients */}
        <div className="absolute inset-0 pointer-events-none">
          <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-br from-blue-900/40 to-indigo-900/40 opacity-50" />
          <div className="absolute -top-32 -left-32 w-[30rem] h-[30rem] rounded-full bg-blue-500/10 blur-[100px]" />
          <div className="absolute bottom-0 right-0 w-[30rem] h-[30rem] rounded-full bg-indigo-500/10 blur-[100px]" />
        </div>

        {/* Floating Popping Elements */}
        <div className="absolute inset-0 pointer-events-none overflow-hidden">
          <div className="pop-bubble absolute top-[15%] right-[10%] bg-white/10 backdrop-blur-md border border-white/10 text-white font-medium px-4 py-2.5 rounded-2xl rounded-br-sm shadow-2xl text-sm transform -rotate-2 flex items-center gap-2">
            <Activity size={16} className="text-blue-400" />
            Patient Vitals Logged
          </div>
          <div className="pop-bubble absolute bottom-[35%] right-[8%] bg-indigo-500/80 backdrop-blur-md border border-indigo-400/30 text-white font-medium px-4 py-2.5 rounded-2xl rounded-br-sm shadow-2xl text-sm transform -rotate-1 flex items-center gap-2">
            <ShieldCheck size={16} className="text-indigo-100" />
            VOT Verified
          </div>
          <div className="pop-bubble absolute bottom-[5%] left-[8%] bg-white/10 backdrop-blur-md border border-white/10 text-white font-medium px-4 py-2.5 rounded-2xl rounded-bl-sm shadow-2xl text-sm transform rotate-6 flex items-center gap-2">
            <Pill size={16} className="text-purple-400" />
            Dose recorded
          </div>
        </div>

        <div className="relative z-10 max-w-xl mx-auto lg:mx-0 w-full overflow-hidden">
          <div className="hero-line flex items-center justify-between gap-3 mb-12">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg shadow-blue-500/30 border border-white/10">
                <ShieldCheck size={24} className="text-white" />
              </div>
              <h1 className="text-2xl font-black text-white tracking-widest uppercase">Amping</h1>
            </div>
            <div className="absolute top-5 right-1 w-64 h-64 flex-shrink-0">
              <Gabby />
            </div>
          </div>

          {/* Carousel Container */}
          <div 
            className="flex transition-transform duration-700 ease-[cubic-bezier(0.25,1,0.5,1)] w-full"
            style={{ transform: `translateX(-${currentSlide * 100}%)` }}
          >
            {SLIDES.map((slide) => (
              <div key={slide.id} className="w-full flex-shrink-0 pe-6">
                <div className="space-y-6">
                  <h2 className="font-extrabold text-white text-4xl lg:text-5xl leading-[1.15] tracking-tight">
                    {slide.line1} <br />
                    <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-indigo-400">{slide.highlight}</span> 
                    <br /> {slide.line2}
                  </h2>
                  
                  <p className="text-lg text-slate-300 leading-relaxed max-w-md font-medium min-h-[4.5rem]">
                    {slide.desc}
                  </p>
                </div>

                <div className="mt-12 grid grid-cols-1 sm:grid-cols-2 gap-6">
                  {slide.features.map((feature, idx) => (
                    <div key={idx} className="flex items-start gap-4">
                      <div className={`flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center border ${feature.iconBg}`}>
                        {feature.icon}
                      </div>
                      <div>
                        <h3 className="text-white font-bold mb-1">{feature.title}</h3>
                        <p className="text-slate-400 text-sm leading-snug">{feature.desc}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
          
          {/* Pagination Indicators */}
          <div className="hero-line flex items-center gap-2 mt-12">
            {SLIDES.map((_, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => setCurrentSlide(idx)}
                className={`h-1.5 rounded-full transition-all duration-300 ${
                  currentSlide === idx ? 'w-8 bg-blue-500' : 'w-2 bg-white/20 hover:bg-white/40'
                }`}
                aria-label={`Go to slide ${idx + 1}`}
              />
            ))}
          </div>
        </div>
      </div>

      {/* Right Panel: Login Form */}
      <div className="relative w-full lg:w-7/12 flex items-center justify-center p-8 lg:p-16 z-10">
        <div className="w-full max-w-md bg-white p-8 lg:p-10 rounded-3xl shadow-[0_8px_40px_rgb(0,0,0,0.06)] border border-slate-100">
          
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-slate-900 mb-2">Welcome back</h2>
            <p className="text-slate-500 text-sm">Sign in to monitor and engage your patients.</p>
          </div>

          {error && (
            <div className="flex items-center gap-2 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-6">
              <AlertCircle size={16} className="shrink-0" />
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            
            {/* Email Field */}
            <Field>
              <Label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                Email Address
              </Label>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="doctor@clinic.ph"
                autoComplete="email"
                className="w-full bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition-all data-[focus]:border-blue-500 data-[focus]:ring-2 data-[focus]:ring-blue-500/20"
              />
            </Field>

            {/* Password Field */}
            <Field>
              <Label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                Password
              </Label>
              <div className="relative">
                <Input
                  type={showPass ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  autoComplete="current-password"
                  className="w-full bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 rounded-xl px-4 py-3.5 pr-11 text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition-all data-[focus]:border-blue-500 data-[focus]:ring-2 data-[focus]:ring-blue-500/20"
                />
                <Button
                  type="button"
                  onClick={() => setShowPass((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded-md p-1"
                >
                  {showPass ? <EyeOff size={18} /> : <Eye size={18} />}
                </Button>
              </div>
            </Field>

            {/* Submit Button */}
            <Button
              type="submit"
              disabled={loading}
              className="w-full bg-[#0A0F24] hover:bg-blue-900 disabled:bg-slate-300 disabled:cursor-not-allowed text-white font-bold rounded-xl py-4 text-sm transition-all flex items-center justify-center gap-2 mt-6 shadow-lg shadow-blue-900/20 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2"
            >
              {loading ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  Authenticating…
                </>
              ) : (
                'Sign In'
              )}
            </Button>
          </form>

          <div className="mt-8 text-center text-sm text-slate-500">
            Need provider access?{' '}
            <Link
              to="/signup"
              className="text-blue-600 font-bold hover:text-blue-800 transition-colors"
            >
              Request an account
            </Link>
          </div>
          
        </div>
      </div>
      
    </div>
  );
}