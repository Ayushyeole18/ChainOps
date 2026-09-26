import React from 'react';
import { Database, Download, RotateCcw, UserCheck, Shield } from 'lucide-react';
import { User, initialUsers } from '../data/initialData';

interface TopBarProps {
  title: string;
  currentUser: User | null;
  onSwitchUser: (user: User) => void;
  onDownloadZip: () => void;
  onResetData: () => void;
}

export const TopBar: React.FC<TopBarProps> = ({
  title,
  currentUser,
  onSwitchUser,
  onDownloadZip,
  onResetData
}) => {
  return (
    <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between select-none">
      {/* Title & Connection Status */}
      <div className="flex items-center gap-3">
        <h1 className="text-lg font-bold text-slate-900 tracking-tight">{title}</h1>
        <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
          <Database className="w-3.5 h-3.5 text-emerald-600" />
          <span>MySQL 8.0 Connected</span>
        </div>
      </div>

      {/* Action Controls */}
      <div className="flex items-center gap-3">
        {/* Reset Seed Data */}
        <button
          onClick={onResetData}
          title="Reset inventory, orders, and products to initial seed values"
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 hover:bg-slate-200 transition-colors"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          <span>Reset Demo Data</span>
        </button>

        {/* Quick Role Switcher */}
        <div className="relative group">
          <button className="flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium bg-blue-50 text-blue-700 hover:bg-blue-100 border border-blue-200 transition-colors">
            <UserCheck className="w-3.5 h-3.5" />
            <span>Role: {currentUser?.role}</span>
          </button>
          <div className="absolute right-0 top-full mt-1 w-52 bg-white rounded-lg shadow-lg border border-slate-200 py-1.5 hidden group-hover:block z-50">
            <div className="px-3 py-1 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
              Switch Demo User Role
            </div>
            {initialUsers.map((u) => (
              <button
                key={u.id}
                onClick={() => onSwitchUser(u)}
                className={`w-full text-left px-3 py-1.5 text-xs flex items-center justify-between hover:bg-slate-50 ${
                  currentUser?.id === u.id ? 'bg-blue-50 font-semibold text-blue-600' : 'text-slate-700'
                }`}
              >
                <span>{u.fullName}</span>
                <span className="text-[10px] text-slate-400 font-mono">{u.role.split(' ')[0]}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Download Project ZIP Button */}
        <button
          onClick={onDownloadZip}
          className="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-500/20 transition-colors"
        >
          <Download className="w-3.5 h-3.5" />
          <span>Download Java Project (.zip)</span>
        </button>
      </div>
    </header>
  );
};
