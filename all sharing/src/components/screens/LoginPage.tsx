import React, { useState } from 'react';
import { 
  Leaf, 
  Lock, 
  Mail, 
  User, 
  Sparkles, 
  ArrowRight, 
  ShieldCheck, 
  AlertCircle, 
  Eye, 
  EyeOff, 
  Layers, 
  CheckCircle2, 
  TrendingUp, 
  RefreshCw,
  Users,
  Compass
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useApp } from '../../context/AppContext';

export const LoginPage: React.FC = () => {
  const { login, signup, demoUsers, switchUser } = useAuth();
  const { showToast } = useApp();

  const [mode, setMode] = useState<'signin' | 'signup'>('signin');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Please fill in your email address and password.');
      return;
    }

    try {
      setIsSubmitting(true);
      if (mode === 'signup') {
        if (!name.trim()) {
          setError('Please provide your full name to create an account.');
          setIsSubmitting(false);
          return;
        }
        await signup(email, password, name);
        showToast({
          type: 'success',
          title: 'Welcome to EcoShare! 🌱',
          message: `Account created for ${name}. Welcome to the sharing community!`,
        });
      } else {
        await login(email, password);
        showToast({
          type: 'success',
          title: 'Welcome back! 👋',
          message: 'Signed in successfully. Explore items in your area!',
        });
      }
    } catch (err: any) {
      const errMsg = err?.message || '';
      if (errMsg.includes('configuration-not-found') || errMsg.includes('auth/')) {
        setError(
          'Firebase Auth is optional. You can sign in immediately using a 1-Click persona below, or check Firebase settings.'
        );
      } else {
        setError(errMsg || 'Authentication failed. Please check credentials or select a quick persona.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSelectDemoUser = (userId: string, userName: string) => {
    switchUser(userId);
    showToast({
      type: 'success',
      title: `Welcome, ${userName}! 👋`,
      message: 'Logged in as demo persona. You now have full access to share and request items.',
    });
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between selection:bg-emerald-500 selection:text-white relative overflow-hidden font-sans">
      
      {/* Dynamic Background Ambient Blobs */}
      <div className="absolute top-0 -left-40 w-96 h-96 bg-emerald-600/20 rounded-full blur-3xl pointer-events-none animate-pulse-subtle" />
      <div className="absolute top-1/3 -right-40 w-[30rem] h-[30rem] bg-teal-500/15 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-20 left-1/4 w-96 h-96 bg-emerald-700/15 rounded-full blur-3xl pointer-events-none" />

      {/* Subtle Grid Overlay */}
      <div 
        className="absolute inset-0 opacity-[0.03] pointer-events-none bg-[radial-gradient(#ffffff_1px,transparent_1px)] [background-size:24px_24px]" 
      />

      {/* Top Header */}
      <header className="relative z-10 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 pt-6 pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center text-white shadow-lg shadow-emerald-500/30">
              <Leaf className="w-6 h-6 fill-white/20" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-2xl tracking-tight bg-gradient-to-r from-emerald-400 via-teal-300 to-emerald-200 bg-clip-text text-transparent">
                  EcoShare
                </span>
                <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  Network
                </span>
              </div>
              <p className="text-[11px] text-slate-400 font-medium">Sustainable Neighborhood Resource Sharing</p>
            </div>
          </div>

          <div className="hidden sm:flex items-center gap-2 text-xs font-semibold text-emerald-400/90 bg-emerald-950/60 border border-emerald-800/40 px-3.5 py-1.5 rounded-full backdrop-blur-md">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            <span>Over 1,420 Items Diverted from Landfills</span>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="relative z-10 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 flex items-center">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center w-full">
          
          {/* Left Column: Hero & Mission Showcase */}
          <section className="lg:col-span-6 space-y-6 text-center lg:text-left">
            
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-emerald-950/70 border border-emerald-500/30 text-emerald-300 text-xs font-semibold backdrop-blur-md shadow-sm">
              <Sparkles className="w-3.5 h-3.5 text-amber-400 fill-amber-400" />
              <span>Free Community Sharing • Zero Waste Movement</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight leading-[1.1]">
              Share Resources.<br />
              <span className="bg-gradient-to-r from-emerald-400 via-teal-300 to-emerald-200 bg-clip-text text-transparent">
                Reduce Waste.
              </span><br />
              Empower Neighbors.
            </h1>

            <p className="text-sm sm:text-base text-slate-300/90 leading-relaxed max-w-xl mx-auto lg:mx-0">
              Why buy tools, camping equipment, textbooks, or electronics you will only use once? 
              Borrow what you need, loan what you own, earn <strong className="text-emerald-400 font-bold">EcoPoints</strong>, 
              and cut your carbon footprint with trusted neighbors.
            </p>

            {/* Feature Cards Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3.5 pt-2">
              <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 backdrop-blur-md hover:border-emerald-500/40 transition-colors text-left group">
                <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-2.5 group-hover:scale-105 transition-transform">
                  <RefreshCw className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-sm text-white mb-1">Borrow & Reuse</h3>
                <p className="text-xs text-slate-400 leading-snug">Access power tools, appliances, and media on demand.</p>
              </div>

              <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 backdrop-blur-md hover:border-teal-500/40 transition-colors text-left group">
                <div className="w-9 h-9 rounded-xl bg-teal-500/10 text-teal-400 flex items-center justify-center mb-2.5 group-hover:scale-105 transition-transform">
                  <TrendingUp className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-sm text-white mb-1">Earn EcoPoints</h3>
                <p className="text-xs text-slate-400 leading-snug">Gain points every time an item is shared, unlocking badges.</p>
              </div>

              <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 backdrop-blur-md hover:border-emerald-500/40 transition-colors text-left group">
                <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-2.5 group-hover:scale-105 transition-transform">
                  <ShieldCheck className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-sm text-white mb-1">Verified Trust</h3>
                <p className="text-xs text-slate-400 leading-snug">Direct borrower requests, chat notifications, and safe pickups.</p>
              </div>
            </div>

            {/* Live Eco Impact Statistics */}
            <div className="pt-4 border-t border-slate-800/60 grid grid-cols-3 gap-4 text-center">
              <div>
                <p className="text-2xl sm:text-3xl font-extrabold text-white">1,420+</p>
                <p className="text-[11px] font-medium text-slate-400 uppercase tracking-wider mt-0.5">Items Shared</p>
              </div>
              <div className="border-x border-slate-800">
                <p className="text-2xl sm:text-3xl font-extrabold text-emerald-400">3,850 kg</p>
                <p className="text-[11px] font-medium text-slate-400 uppercase tracking-wider mt-0.5">CO₂ Prevented</p>
              </div>
              <div>
                <p className="text-2xl sm:text-3xl font-extrabold text-teal-300">100%</p>
                <p className="text-[11px] font-medium text-slate-400 uppercase tracking-wider mt-0.5">Free Sharing</p>
              </div>
            </div>

          </section>

          {/* Right Column: High-End Authentication Card */}
          <section className="lg:col-span-6 w-full max-w-lg mx-auto">
            <div className="bg-slate-900/85 backdrop-blur-2xl border border-slate-800 rounded-3xl p-6 sm:p-8 shadow-2xl shadow-emerald-950/70 relative">
              
              {/* Card Header & Tab Switcher */}
              <div className="mb-6">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-2xl font-black text-white tracking-tight">
                      {mode === 'signin' ? 'Sign In to EcoShare' : 'Join the Community'}
                    </h2>
                    <p className="text-xs text-slate-400 mt-1">
                      {mode === 'signin' 
                        ? 'Log in to manage your inventory and respond to neighbor requests.'
                        : 'Create your free account to start borrowing and listing items.'}
                    </p>
                  </div>
                  <div className="w-10 h-10 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
                    <Compass className="w-5 h-5" />
                  </div>
                </div>

                {/* Switcher Pills */}
                <div className="grid grid-cols-2 p-1 bg-slate-950/80 rounded-2xl border border-slate-800">
                  <button
                    id="login-tab-signin"
                    type="button"
                    onClick={() => { setMode('signin'); setError(''); }}
                    className={`py-2 text-xs font-bold rounded-xl transition-all ${
                      mode === 'signin'
                        ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30'
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    Sign In
                  </button>
                  <button
                    id="login-tab-signup"
                    type="button"
                    onClick={() => { setMode('signup'); setError(''); }}
                    className={`py-2 text-xs font-bold rounded-xl transition-all ${
                      mode === 'signup'
                        ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30'
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    Create Account
                  </button>
                </div>
              </div>

              {/* Error Alert */}
              {error && (
                <div className="mb-4 p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs font-medium flex items-start gap-2 animate-in fade-in">
                  <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
                  <span>{error}</span>
                </div>
              )}

              {/* Form */}
              <form onSubmit={handleSubmit} className="space-y-4">
                
                {mode === 'signup' && (
                  <div>
                    <label 
                      htmlFor="login-name-input" 
                      className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5"
                    >
                      Full Name
                    </label>
                    <div className="relative">
                      <User className="absolute left-3.5 top-3 w-4 h-4 text-slate-500" />
                      <input
                        id="login-name-input"
                        type="text"
                        required
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="e.g. Maya Lin"
                        className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-950/70 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
                      />
                    </div>
                  </div>
                )}

                <div>
                  <label 
                    htmlFor="login-email-input" 
                    className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5"
                  >
                    Email Address
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3.5 top-3 w-4 h-4 text-slate-500" />
                    <input
                      id="login-email-input"
                      type="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="you@domain.com"
                      className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-950/70 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
                    />
                  </div>
                </div>

                <div>
                  <label 
                    htmlFor="login-password-input" 
                    className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5"
                  >
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3.5 top-3 w-4 h-4 text-slate-500" />
                    <input
                      id="login-password-input"
                      type={showPassword ? 'text' : 'password'}
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      className="w-full pl-10 pr-10 py-2.5 rounded-xl bg-slate-950/70 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
                    />
                    <button
                      id="login-toggle-password-btn"
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3.5 top-3 text-slate-500 hover:text-slate-300"
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                <button
                  id="login-submit-button"
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-3 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold text-sm transition-all duration-200 shadow-lg shadow-emerald-700/30 flex items-center justify-center gap-2 active:scale-98 disabled:opacity-60 cursor-pointer mt-2"
                >
                  {isSubmitting ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      <span>Authenticating...</span>
                    </>
                  ) : (
                    <>
                      <span>{mode === 'signup' ? 'Create Account & Enter' : 'Sign In to EcoShare'}</span>
                      <ArrowRight className="w-4 h-4" />
                    </>
                  )}
                </button>
              </form>

              {/* 1-Click Instant Demo Persona Access */}
              <div className="mt-6 pt-5 border-t border-slate-800/80">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                    <span className="text-[11px] font-bold text-slate-300 uppercase tracking-wider">
                      1-Click Instant Access
                    </span>
                  </div>
                  <span className="text-[10px] font-medium text-emerald-400 bg-emerald-950/80 border border-emerald-800/50 px-2 py-0.5 rounded-full">
                    No Password Needed
                  </span>
                </div>

                <p className="text-[11px] text-slate-400 mb-2.5">
                  Click any resident persona below to launch inside the application immediately:
                </p>

                <div className="grid grid-cols-3 gap-2.5">
                  {demoUsers.map((user) => (
                    <button
                      key={user.userId}
                      id={`demo-user-${user.userId}`}
                      type="button"
                      onClick={() => handleSelectDemoUser(user.userId, user.name)}
                      className="p-2.5 rounded-2xl bg-slate-950/60 border border-slate-800 hover:border-emerald-500/60 hover:bg-emerald-950/30 transition-all text-center flex flex-col items-center group cursor-pointer active:scale-95"
                    >
                      <div className="relative mb-1.5">
                        <img
                          src={user.avatarUrl}
                          alt={user.name}
                          className="w-10 h-10 rounded-full object-cover border-2 border-slate-700 group-hover:border-emerald-400 transition-colors"
                        />
                        <div className="absolute -bottom-1 -right-1 w-4 h-4 rounded-full bg-emerald-500 border-2 border-slate-900 flex items-center justify-center">
                          <CheckCircle2 className="w-2.5 h-2.5 text-white" />
                        </div>
                      </div>
                      
                      <span className="text-xs font-bold text-white group-hover:text-emerald-300 transition-colors truncate max-w-full">
                        {user.name.split(' ')[0]}
                      </span>
                      
                      <span className="text-[10px] font-extrabold text-emerald-400">
                        {user.ecoPoints} pts
                      </span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Bottom Security Info */}
              <div className="mt-4 pt-3 border-t border-slate-800/50 flex items-center justify-center gap-1.5 text-[11px] text-slate-500">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                <span>Protected by Local Session & Real-Time Sync</span>
              </div>

            </div>
          </section>

        </div>
      </main>

      {/* Footer */}
      <footer className="relative z-10 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-5 border-t border-slate-800/80 text-xs text-slate-500 flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Leaf className="w-4 h-4 text-emerald-500" />
          <span className="font-semibold text-slate-400">EcoShare Community</span>
          <span>• Promoting neighborhood reuse and sustainable living</span>
        </div>
        <p className="text-[11px] text-slate-500">
          Sign in to borrow, lend, and save our planet together.
        </p>
      </footer>

    </div>
  );
};
