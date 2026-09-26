import React from 'react';
import {
  LayoutDashboard,
  Boxes,
  ClipboardList,
  ShoppingCart,
  Truck,
  ArrowLeftRight,
  Package,
  FolderTree,
  Building2,
  Warehouse,
  BarChart3,
  Users,
  Code2,
  LogOut,
  ShieldCheck
} from 'lucide-react';
import { User } from '../data/initialData';

export type ActiveTab =
  | 'dashboard'
  | 'inventory'
  | 'purchase-orders'
  | 'sales-orders'
  | 'shipments'
  | 'stock-transfers'
  | 'products'
  | 'categories'
  | 'suppliers'
  | 'warehouses'
  | 'reports'
  | 'users'
  | 'java-explorer';

interface SidebarProps {
  activeTab: ActiveTab;
  setActiveTab: (tab: ActiveTab) => void;
  currentUser: User | null;
  onLogout: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  setActiveTab,
  currentUser,
  onLogout
}) => {
  const isAdmin = currentUser?.roleId === 1;

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, category: null },

    { id: 'inventory', label: 'Inventory Stock', icon: Boxes, category: 'OPERATIONS' },
    { id: 'purchase-orders', label: 'Purchase Orders', icon: ClipboardList, category: 'OPERATIONS' },
    { id: 'sales-orders', label: 'Sales Orders', icon: ShoppingCart, category: 'OPERATIONS' },
    { id: 'shipments', label: 'Shipments & Tracking', icon: Truck, category: 'OPERATIONS' },
    { id: 'stock-transfers', label: 'Stock Transfers', icon: ArrowLeftRight, category: 'OPERATIONS' },

    { id: 'products', label: 'Products Master', icon: Package, category: 'MASTER DATA' },
    { id: 'categories', label: 'Categories', icon: FolderTree, category: 'MASTER DATA' },
    { id: 'suppliers', label: 'Suppliers', icon: Building2, category: 'MASTER DATA' },
    { id: 'warehouses', label: 'Warehouses', icon: Warehouse, category: 'MASTER DATA' },

    { id: 'reports', label: 'Reports & CSV Export', icon: BarChart3, category: 'ANALYTICS' },

    { id: 'users', label: 'User Roles & RBAC', icon: Users, category: 'ADMINISTRATION', adminOnly: true },
    { id: 'java-explorer', label: 'Java Code & Maven Hub', icon: Code2, category: 'PROJECT REPO' }
  ];

  let lastCategory: string | null = null;

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col flex-shrink-0 text-slate-300 select-none">
      {/* Brand Header */}
      <div className="p-4 border-b border-slate-800 flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xl font-bold tracking-tight text-white flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block animate-pulse"></span>
              ChainOps
            </span>
            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-blue-900/60 text-blue-300 border border-blue-700/50">
              v1.0
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">Supply Chain Management</p>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 overflow-y-auto py-3 px-2 space-y-1">
        {navItems.map((item) => {
          if (item.adminOnly && !isAdmin) return null;

          const showCategory = item.category && item.category !== lastCategory;
          if (item.category) lastCategory = item.category;

          const isActive = activeTab === item.id;
          const Icon = item.icon;

          return (
            <React.Fragment key={item.id}>
              {showCategory && (
                <div className="pt-3 pb-1 px-3 text-[10px] font-bold tracking-wider text-slate-400 uppercase">
                  {item.category}
                </div>
              )}
              <button
                onClick={() => setActiveTab(item.id as ActiveTab)}
                className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm shadow-blue-500/20 font-semibold'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                <span className="truncate">{item.label}</span>
                {item.id === 'java-explorer' && (
                  <span className="ml-auto text-[10px] px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                    Java
                  </span>
                )}
              </button>
            </React.Fragment>
          );
        })}
      </div>

      {/* User Info & Logout Footer */}
      <div className="p-3 border-t border-slate-800 bg-slate-950/40">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2 overflow-hidden">
            <div className="w-8 h-8 rounded-full bg-blue-600/30 border border-blue-500/40 flex items-center justify-center text-blue-300 font-bold text-xs flex-shrink-0">
              {currentUser?.fullName.charAt(0) || 'U'}
            </div>
            <div className="overflow-hidden">
              <p className="text-xs font-semibold text-slate-200 truncate">{currentUser?.fullName}</p>
              <p className="text-[10px] text-blue-400 font-medium truncate flex items-center gap-1">
                <ShieldCheck className="w-3 h-3 text-blue-400" />
                {currentUser?.role}
              </p>
            </div>
          </div>
        </div>

        <button
          onClick={onLogout}
          className="w-full flex items-center justify-center gap-2 text-xs py-1.5 rounded-md bg-slate-800/80 hover:bg-red-950/60 text-slate-300 hover:text-red-300 border border-slate-700/50 hover:border-red-700/50 transition-colors"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
