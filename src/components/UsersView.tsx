import React, { useState } from 'react';
import {
  Users as UsersIcon,
  Search,
  Plus,
  Shield,
  ShieldCheck,
  Check,
  X,
  UserCheck,
  AlertCircle
} from 'lucide-react';
import { User, Role } from '../data/initialData';

interface UsersViewProps {
  users: User[];
  roles: Role[];
  currentUser: User | null;
  onCreateUser: (u: Omit<User, 'id'>) => { success: boolean; message: string };
  onToggleUserStatus: (id: number) => void;
  onUpdateUserRole: (id: number, roleId: number) => void;
}

export const UsersView: React.FC<UsersViewProps> = ({
  users,
  roles,
  currentUser,
  onCreateUser,
  onToggleUserStatus,
  onUpdateUserRole
}) => {
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);

  // Form State
  const [username, setUsername] = useState('');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [roleId, setRoleId] = useState(2);
  const [errorMsg, setErrorMsg] = useState('');

  const filtered = users.filter(u =>
    u.fullName.toLowerCase().includes(search.toLowerCase()) ||
    u.username.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase()) ||
    u.role.toLowerCase().includes(search.toLowerCase())
  );

  const handleOpenAdd = () => {
    setUsername('');
    setFullName('');
    setEmail('');
    setRoleId(2);
    setErrorMsg('');
    setModalOpen(true);
  };

  const handleSaveUser = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !fullName.trim() || !email.trim()) {
      setErrorMsg('All fields are mandatory.');
      return;
    }
    const roleObj = roles.find(r => r.id === roleId);
    const result = onCreateUser({
      username: username.trim().toLowerCase(),
      fullName: fullName.trim(),
      email: email.trim().toLowerCase(),
      roleId,
      role: roleObj?.displayName || 'Warehouse Manager',
      status: 'ACTIVE'
    });

    if (!result.success) {
      setErrorMsg(result.message);
      return;
    }

    setModalOpen(false);
  };

  // RBAC permissions matrix definition
  const permissionModules = [
    { module: 'Dashboard Metrics', admin: true, warehouse: true, procure: true, sales: true },
    { module: 'Inventory Stock & In/Out Adjustments', admin: true, warehouse: true, procure: false, sales: false },
    { module: 'Stock Transfers Between Hubs', admin: true, warehouse: true, procure: false, sales: false },
    { module: 'Purchase Orders & Inbound Receiving', admin: true, warehouse: true, procure: true, sales: false },
    { module: 'Sales Orders & Stock Reservations', admin: true, warehouse: false, procure: false, sales: true },
    { module: 'Shipment Tracking & Dispatches', admin: true, warehouse: true, procure: false, sales: true },
    { module: 'Product & Category Catalog (Write)', admin: true, warehouse: false, procure: true, sales: false },
    { module: 'Supplier Directory Management', admin: true, warehouse: false, procure: true, sales: false },
    { module: 'Warehouse Configuration', admin: true, warehouse: false, procure: false, sales: false },
    { module: 'User Management & Security Roles', admin: true, warehouse: false, procure: false, sales: false },
    { module: 'Financial & CSV Reports Export', admin: true, warehouse: true, procure: true, sales: true }
  ];

  return (
    <div className="p-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2.5">
            <UsersIcon className="w-6 h-6 text-blue-600" />
            User Management & Role-Based Access Control (RBAC)
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Enforce role policies, manage user authorization credentials, and audit operational permissions
          </p>
        </div>

        <button
          onClick={handleOpenAdd}
          className="flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white shadow-sm transition-colors cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Provision New User</span>
        </button>
      </div>

      {/* Search and stats */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-xl border border-slate-200">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
          <input
            type="text"
            placeholder="Search users by name, email, or role..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-blue-500"
          />
        </div>

        <div className="flex items-center gap-4 text-xs text-slate-600">
          <span className="flex items-center gap-1.5 font-medium">
            <ShieldCheck className="w-4 h-4 text-emerald-600" />
            <span>Active Users: {users.filter(u => u.status === 'ACTIVE').length}</span>
          </span>
          <span className="text-slate-300">|</span>
          <span className="flex items-center gap-1.5 font-medium">
            <Shield className="w-4 h-4 text-blue-600" />
            <span>Roles Defined: {roles.length}</span>
          </span>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 text-slate-500 border-b border-slate-200 uppercase font-semibold">
            <tr>
              <th className="py-3 px-4">User</th>
              <th className="py-3 px-4">Username</th>
              <th className="py-3 px-4">Assigned Role</th>
              <th className="py-3 px-4">Account Status</th>
              <th className="py-3 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-slate-700">
            {filtered.map(user => {
              const isCurrent = currentUser?.id === user.id;
              return (
                <tr key={user.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center font-bold text-slate-700 text-xs">
                        {user.fullName.split(' ').map(n => n[0]).join('')}
                      </div>
                      <div>
                        <div className="font-semibold text-slate-900 flex items-center gap-2">
                          {user.fullName}
                          {isCurrent && (
                            <span className="px-1.5 py-0.2 rounded text-[9px] font-bold bg-blue-100 text-blue-700">
                              YOU
                            </span>
                          )}
                        </div>
                        <div className="text-[11px] text-slate-500">{user.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-4 font-mono font-medium text-slate-600">@{user.username}</td>
                  <td className="py-3 px-4">
                    <select
                      value={user.roleId}
                      onChange={e => onUpdateUserRole(user.id, Number(e.target.value))}
                      className="px-2.5 py-1 text-xs rounded-lg border border-slate-200 bg-white font-medium text-slate-800 focus:outline-none focus:border-blue-500 cursor-pointer"
                    >
                      {roles.map(r => (
                        <option key={r.id} value={r.id}>
                          {r.displayName}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td className="py-3 px-4">
                    <button
                      onClick={() => onToggleUserStatus(user.id)}
                      className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold cursor-pointer transition-colors ${
                        user.status === 'ACTIVE'
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100'
                          : 'bg-slate-100 text-slate-500 border border-slate-200 hover:bg-slate-200'
                      }`}
                    >
                      {user.status}
                    </button>
                  </td>
                  <td className="py-3 px-4 text-right">
                    <button
                      onClick={() => onToggleUserStatus(user.id)}
                      disabled={isCurrent}
                      className={`text-xs px-2.5 py-1 rounded-md transition-colors cursor-pointer ${
                        isCurrent
                          ? 'text-slate-300 cursor-not-allowed'
                          : user.status === 'ACTIVE'
                          ? 'text-amber-600 hover:bg-amber-50'
                          : 'text-emerald-600 hover:bg-emerald-50'
                      }`}
                    >
                      {user.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Role Permission Matrix Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-6 space-y-4">
        <div className="flex items-center gap-2">
          <Shield className="w-5 h-5 text-blue-600" />
          <h3 className="text-base font-bold text-slate-900">Role-Based Access Control (RBAC) Permission Matrix</h3>
        </div>
        <p className="text-xs text-slate-500">
          Enforced across all Java controllers and UI routing based on user session role credentials.
        </p>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 uppercase font-semibold">
              <tr>
                <th className="py-2.5 px-4">System Capability / Module</th>
                <th className="py-2.5 px-4 text-center">Administrator</th>
                <th className="py-2.5 px-4 text-center">Warehouse Manager</th>
                <th className="py-2.5 px-4 text-center">Procurement Manager</th>
                <th className="py-2.5 px-4 text-center">Sales Manager</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {permissionModules.map((pm, idx) => (
                <tr key={idx} className="hover:bg-slate-50/50">
                  <td className="py-2.5 px-4 font-medium text-slate-800">{pm.module}</td>
                  <td className="py-2.5 px-4 text-center">
                    {pm.admin ? <Check className="w-4 h-4 text-emerald-600 mx-auto" /> : <X className="w-4 h-4 text-slate-300 mx-auto" />}
                  </td>
                  <td className="py-2.5 px-4 text-center">
                    {pm.warehouse ? <Check className="w-4 h-4 text-emerald-600 mx-auto" /> : <X className="w-4 h-4 text-slate-300 mx-auto" />}
                  </td>
                  <td className="py-2.5 px-4 text-center">
                    {pm.procure ? <Check className="w-4 h-4 text-emerald-600 mx-auto" /> : <X className="w-4 h-4 text-slate-300 mx-auto" />}
                  </td>
                  <td className="py-2.5 px-4 text-center">
                    {pm.sales ? <Check className="w-4 h-4 text-emerald-600 mx-auto" /> : <X className="w-4 h-4 text-slate-300 mx-auto" />}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add User Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-slate-200 overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <UserCheck className="w-5 h-5 text-blue-600" />
                Provision New System User
              </h3>
              <button
                onClick={() => setModalOpen(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-md"
              >
                &times;
              </button>
            </div>

            <form onSubmit={handleSaveUser} className="p-6 space-y-4">
              {errorMsg && (
                <div className="p-3 bg-red-50 text-red-700 text-xs rounded-lg flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{errorMsg}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name</label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={e => setFullName(e.target.value)}
                  placeholder="e.g. Jordan Mitchell"
                  className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Username</label>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  placeholder="e.g. jmitchell"
                  className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Corporate Email</label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  placeholder="e.g. jmitchell@supplychainx.com"
                  className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Assigned Role</label>
                <select
                  value={roleId}
                  onChange={e => setRoleId(Number(e.target.value))}
                  className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
                >
                  {roles.map(r => (
                    <option key={r.id} value={r.id}>
                      {r.displayName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 text-xs text-slate-600 hover:bg-slate-100 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg shadow-sm"
                >
                  Create User
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
