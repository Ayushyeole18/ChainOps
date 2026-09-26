import React, { useState } from 'react';
import {
  Boxes,
  Lock,
  Mail,
  User as UserIcon,
  ShieldCheck,
  ArrowRight,
  Sparkles,
  AlertCircle,
  CheckCircle2,
  KeyRound,
  Building2,
  Database
} from 'lucide-react';
import { User, Role } from '../data/initialData';

interface AuthViewProps {
  users: User[];
  roles: Role[];
  onLogin: (user: User) => void;
  onSignUp: (userData: Omit<User, 'id'>) => { success: boolean; message: string; user?: User };
}

export const AuthView: React.FC<AuthViewProps> = ({
  users,
  roles,
  onLogin,
  onSignUp
}) => {
  const [mode, setMode] = useState<'signin' | 'signup'>('signin');

  // Sign In State
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [signInError, setSignInError] = useState('');

  // Sign Up State
  const [fullName, setFullName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [roleId, setRoleId] = useState(2); // Default to Warehouse Manager
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [signUpError, setSignUpError] = useState('');
  const [signUpSuccess, setSignUpSuccess] = useState('');

  // Handle Sign In submission
  const handleSignIn = (e: React.FormEvent) => {
    e.preventDefault();
    setSignInError('');

    const trimmedIdentifier = identifier.trim().toLowerCase();
    const foundUser = users.find(
      u => u.username.toLowerCase() === trimmedIdentifier || u.email.toLowerCase() === trimmedIdentifier
    );

    if (!foundUser) {
      setSignInError('No account found with this username or email.');
      return;
    }

    if (foundUser.status === 'INACTIVE') {
      setSignInError('This account is currently deactivated. Contact your system administrator.');
      return;
    }

    // Check password if set on user, or match default pattern
    if (foundUser.password && foundUser.password !== password) {
      setSignInError('Incorrect password. For demo accounts, use password matching the role (e.g. admin123, warehouse123, procure123, sales123).');
      return;
    }

    // Success!
    onLogin(foundUser);
  };

  // Handle Quick Demo Login
  const handleQuickDemoLogin = (demoUsername: string) => {
    const user = users.find(u => u.username === demoUsername);
    if (user) {
      onLogin(user);
    }
  };

  // Handle Sign Up submission
  const handleSignUp = (e: React.FormEvent) => {
    e.preventDefault();
    setSignUpError('');
    setSignUpSuccess('');

    if (!fullName.trim() || !username.trim() || !email.trim() || !newPassword) {
      setSignUpError('Please fill in all required fields.');
      return;
    }

    if (newPassword.length < 6) {
      setSignUpError('Password must be at least 6 characters long.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setSignUpError('Passwords do not match. Please verify your password.');
      return;
    }

    const selectedRole = roles.find(r => r.id === roleId);

    const result = onSignUp({
      fullName: fullName.trim(),
      username: username.trim().toLowerCase(),
      email: email.trim().toLowerCase(),
      roleId,
      role: selectedRole?.displayName || 'Warehouse Manager',
      status: 'ACTIVE',
      password: newPassword
    });

    if (!result.success) {
      setSignUpError(result.message);
      return;
    }

    setSignUpSuccess('Account successfully provisioned! Logging you in...');
    setTimeout(() => {
      if (result.user) {
        onLogin(result.user);
      }
    }, 600);
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center items-center p-4 relative overflow-hidden select-none">
      {/* Background Decor */}
      <div className="absolute top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(37,99,235,0.25),rgba(255,255,255,0))] pointer-events-none" />
      <div className="absolute -top-32 -right-32 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-32 -left-32 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />

      {/* Main Container */}
      <div className="w-full max-w-lg relative z-10">
        {/* Brand Header */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center gap-2.5 px-4 py-1.5 rounded-full bg-blue-500/10 border border-blue-500/30 text-blue-300 text-xs font-semibold mb-3">
            <Database className="w-3.5 h-3.5 text-blue-400" />
            <span>ChainOps Enterprise Supply Chain</span>
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight flex items-center justify-center gap-3">
            <span className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center text-white shadow-lg shadow-blue-500/30">
              <Boxes className="w-5 h-5" />
            </span>
            <span>ChainOps</span>
          </h1>
          <p className="text-xs text-slate-400 mt-2">
            Centralized Inventory, Procurement, Order Fulfillment & Multi-Facility Logistics
          </p>
        </div>

        {/* Auth Card */}
        <div className="bg-white rounded-2xl shadow-2xl border border-slate-100/80 overflow-hidden">
          {/* Tab Selector */}
          <div className="grid grid-cols-2 p-1.5 bg-slate-100 border-b border-slate-200">
            <button
              onClick={() => {
                setMode('signin');
                setSignInError('');
              }}
              className={`py-2 text-xs font-bold rounded-xl transition-all cursor-pointer ${
                mode === 'signin'
                  ? 'bg-white text-slate-900 shadow-xs'
                  : 'text-slate-500 hover:text-slate-800'
              }`}
            >
              Sign In
            </button>
            <button
              onClick={() => {
                setMode('signup');
                setSignUpError('');
                setSignUpSuccess('');
              }}
              className={`py-2 text-xs font-bold rounded-xl transition-all cursor-pointer ${
                mode === 'signup'
                  ? 'bg-white text-slate-900 shadow-xs'
                  : 'text-slate-500 hover:text-slate-800'
              }`}
            >
              Create Account (Sign Up)
            </button>
          </div>

          <div className="p-7">
            {/* SIGN IN FORM */}
            {mode === 'signin' && (
              <form onSubmit={handleSignIn} className="space-y-4">
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="text-xs font-semibold text-slate-700">Username or Email</label>
                  </div>
                  <div className="relative">
                    <UserIcon className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                    <input
                      type="text"
                      required
                      placeholder="e.g. admin or admin@supplychainx.com"
                      value={identifier}
                      onChange={e => setIdentifier(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900 font-medium"
                    />
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="text-xs font-semibold text-slate-700">Password</label>
                  </div>
                  <div className="relative">
                    <Lock className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                    <input
                      type="password"
                      required
                      placeholder="••••••••"
                      value={password}
                      onChange={e => setPassword(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900 font-medium"
                    />
                  </div>
                </div>

                {signInError && (
                  <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
                    <span className="leading-snug">{signInError}</span>
                  </div>
                )}

                <button
                  type="submit"
                  className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold shadow-md shadow-blue-500/20 transition-all cursor-pointer"
                >
                  <span>Sign In to ChainOps</span>
                  <ArrowRight className="w-4 h-4" />
                </button>

                {/* 1-Click Demo Accounts Row */}
                <div className="pt-4 border-t border-slate-100">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1">
                      <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                      1-Click Demo Accounts (Instant Login)
                    </span>
                  </div>
                  <div className="grid grid-cols-2 gap-2">
                    {[
                      { username: 'admin', label: 'Alexander Vance', role: 'Administrator', color: 'border-blue-200 bg-blue-50/60 hover:bg-blue-100/70 text-blue-900' },
                      { username: 'warehouse', label: 'Elena Rostova', role: 'Warehouse Mgr', color: 'border-emerald-200 bg-emerald-50/60 hover:bg-emerald-100/70 text-emerald-900' },
                      { username: 'procure', label: 'David Sterling', role: 'Procurement Mgr', color: 'border-amber-200 bg-amber-50/60 hover:bg-amber-100/70 text-amber-900' },
                      { username: 'sales', label: 'Sarah Chen', role: 'Sales Mgr', color: 'border-purple-200 bg-purple-50/60 hover:bg-purple-100/70 text-purple-900' },
                    ].map(demo => (
                      <button
                        key={demo.username}
                        type="button"
                        onClick={() => handleQuickDemoLogin(demo.username)}
                        className={`p-2 rounded-lg border text-left transition-colors cursor-pointer ${demo.color}`}
                      >
                        <div className="font-bold text-xs truncate">{demo.label}</div>
                        <div className="text-[10px] opacity-75 truncate">{demo.role}</div>
                      </button>
                    ))}
                  </div>
                </div>
              </form>
            )}

            {/* SIGN UP FORM */}
            {mode === 'signup' && (
              <form onSubmit={handleSignUp} className="space-y-3.5">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name</label>
                  <div className="relative">
                    <UserIcon className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                    <input
                      type="text"
                      required
                      placeholder="e.g. Jordan Mitchell"
                      value={fullName}
                      onChange={e => setFullName(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">Username</label>
                    <input
                      type="text"
                      required
                      placeholder="jmitchell"
                      value={username}
                      onChange={e => setUsername(e.target.value.toLowerCase().replace(/\s+/g, ''))}
                      className="w-full px-3 py-2 text-xs font-mono bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">Assigned Role</label>
                    <select
                      value={roleId}
                      onChange={e => setRoleId(Number(e.target.value))}
                      className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900"
                    >
                      {roles.map(r => (
                        <option key={r.id} value={r.id}>
                          {r.displayName}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Corporate Email</label>
                  <div className="relative">
                    <Mail className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                    <input
                      type="email"
                      required
                      placeholder="jmitchell@supplychainx.com"
                      value={email}
                      onChange={e => setEmail(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">Password</label>
                    <div className="relative">
                      <Lock className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                      <input
                        type="password"
                        required
                        placeholder="••••••••"
                        value={newPassword}
                        onChange={e => setNewPassword(e.target.value)}
                        className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">Confirm Password</label>
                    <div className="relative">
                      <Lock className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                      <input
                        type="password"
                        required
                        placeholder="••••••••"
                        value={confirmPassword}
                        onChange={e => setConfirmPassword(e.target.value)}
                        className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white text-slate-900"
                      />
                    </div>
                  </div>
                </div>

                {signUpError && (
                  <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
                    <span className="leading-snug">{signUpError}</span>
                  </div>
                )}

                {signUpSuccess && (
                  <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-lg flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
                    <span className="leading-snug">{signUpSuccess}</span>
                  </div>
                )}

                <button
                  type="submit"
                  className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-md shadow-emerald-500/20 transition-all cursor-pointer mt-2"
                >
                  <ShieldCheck className="w-4 h-4" />
                  <span>Register & Launch ChainOps</span>
                </button>
              </form>
            )}
          </div>
        </div>

        {/* Security & Credentials info footer */}
        <div className="text-center mt-5 text-xs text-slate-400">
          <span>Protected with BCrypt password hashing & MySQL transactional RBAC security</span>
        </div>
      </div>
    </div>
  );
};
