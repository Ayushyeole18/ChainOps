import React, { useState } from 'react';
import { Warehouse as WarehouseIcon, Search, Plus, Edit, Trash2, MapPin, User, Gauge } from 'lucide-react';
import { Warehouse } from '../data/initialData';

interface WarehousesViewProps {
  warehouses: Warehouse[];
  onCreateWarehouse: (w: Omit<Warehouse, 'id' | 'currentUtilization'>) => { success: boolean; message: string };
  onUpdateWarehouse: (w: Warehouse) => { success: boolean; message: string };
  onDeleteWarehouse: (id: number) => { success: boolean; message: string };
}

export const WarehousesView: React.FC<WarehousesViewProps> = ({
  warehouses,
  onCreateWarehouse,
  onUpdateWarehouse,
  onDeleteWarehouse
}) => {
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(null);

  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [location, setLocation] = useState('');
  const [manager, setManager] = useState('');
  const [capacity, setCapacity] = useState('50000');
  const [status, setStatus] = useState<'ACTIVE' | 'MAINTENANCE' | 'INACTIVE'>('ACTIVE');
  const [errorMsg, setErrorMsg] = useState('');

  const filtered = warehouses.filter(w =>
    w.name.toLowerCase().includes(search.toLowerCase()) ||
    w.code.toLowerCase().includes(search.toLowerCase()) ||
    w.location.toLowerCase().includes(search.toLowerCase()) ||
    w.manager.toLowerCase().includes(search.toLowerCase())
  );

  const handleOpenAdd = () => {
    setEditingWarehouse(null);
    setCode(`WH-HUB-${Math.floor(10 + Math.random() * 90)}`);
    setName('');
    setLocation('');
    setManager('');
    setCapacity('50000');
    setStatus('ACTIVE');
    setErrorMsg('');
    setModalOpen(true);
  };

  const handleOpenEdit = (w: Warehouse) => {
    setEditingWarehouse(w);
    setCode(w.code);
    setName(w.name);
    setLocation(w.location);
    setManager(w.manager);
    setCapacity(w.capacity.toString());
    setStatus(w.status);
    setErrorMsg('');
    setModalOpen(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !code.trim() || !location.trim()) {
      setErrorMsg('Facility name, code, and location are required.');
      return;
    }

    const cap = parseInt(capacity, 10);
    if (isNaN(cap) || cap <= 0) {
      setErrorMsg('Capacity must be a positive integer.');
      return;
    }

    if (editingWarehouse) {
      const res = onUpdateWarehouse({
        ...editingWarehouse,
        code,
        name,
        location,
        manager,
        capacity: cap,
        status
      });
      if (!res.success) { setErrorMsg(res.message); return; }
    } else {
      const res = onCreateWarehouse({
        code,
        name,
        location,
        manager,
        capacity: cap,
        status
      });
      if (!res.success) { setErrorMsg(res.message); return; }
    }

    setModalOpen(false);
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Warehouses &amp; Fulfillment Centers</h2>
          <p className="text-xs text-slate-500 mt-1">
            Regional distribution hubs, storage capacity constraints, and facility utilization
          </p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-xs"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add New Warehouse</span>
        </button>
      </div>

      {/* Toolbar */}
      <div className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search warehouse name, facility code, city..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white"
          />
        </div>
      </div>

      {/* Facility Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filtered.map(w => {
          const utilPct = w.capacity > 0 ? (w.currentUtilization / w.capacity) * 100 : 0;
          const isHigh = utilPct > 80;

          return (
            <div key={w.id} className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs space-y-4">
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-bold text-slate-900 text-sm">{w.name}</h3>
                    <span className="font-mono text-[10px] px-1.5 py-0.5 rounded bg-blue-50 text-blue-700 border border-blue-200 font-semibold">
                      {w.code}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500 flex items-center gap-1 mt-1">
                    <MapPin className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                    <span>{w.location}</span>
                  </p>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => handleOpenEdit(w)}
                    className="p-1 rounded text-slate-400 hover:text-blue-600 hover:bg-blue-50"
                  >
                    <Edit className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(`Delete warehouse ${w.name}?`)) {
                        const res = onDeleteWarehouse(w.id);
                        if (!res.success) alert(res.message);
                      }
                    }}
                    className="p-1 rounded text-slate-400 hover:text-rose-600 hover:bg-rose-50"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Progress Bar */}
              <div className="space-y-1.5 bg-slate-50 p-3 rounded-lg border border-slate-100">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-600 font-medium">Storage Utilization</span>
                  <span className={`font-bold ${isHigh ? 'text-amber-700' : 'text-slate-900'}`}>
                    {w.currentUtilization.toLocaleString()} / {w.capacity.toLocaleString()} Units ({utilPct.toFixed(1)}%)
                  </span>
                </div>
                <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                  <div
                    className={`h-2 rounded-full ${isHigh ? 'bg-amber-500' : 'bg-blue-600'}`}
                    style={{ width: `${Math.min(utilPct, 100)}%` }}
                  ></div>
                </div>
              </div>

              <div className="flex items-center justify-between text-xs pt-1 border-t border-slate-100">
                <span className="text-slate-500 flex items-center gap-1">
                  <User className="w-3.5 h-3.5 text-slate-400" />
                  Manager: <strong className="text-slate-700">{w.manager}</strong>
                </span>
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                  w.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-600'
                }`}>
                  {w.status}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-slate-200 shadow-2xl max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900">
                {editingWarehouse ? 'Edit Facility' : 'Register New Warehouse Hub'}
              </h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            {errorMsg && (
              <div className="p-3 rounded-lg bg-rose-50 text-rose-700 text-xs font-medium border border-rose-200">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Warehouse Code</label>
                  <input
                    type="text"
                    value={code}
                    onChange={e => setCode(e.target.value.toUpperCase())}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white font-mono"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Storage Capacity (Units)</label>
                  <input
                    type="number"
                    min="1"
                    value={capacity}
                    onChange={e => setCapacity(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Facility Name</label>
                <input
                  type="text"
                  placeholder="Central Fulfillment Hub - Chicago"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  required
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Location Address</label>
                <input
                  type="text"
                  placeholder="4500 Industrial Dr, Chicago, IL"
                  value={location}
                  onChange={e => setLocation(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  required
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Facility Manager</label>
                <input
                  type="text"
                  placeholder="Elena Rostova"
                  value={manager}
                  onChange={e => setManager(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  required
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Operating Status</label>
                <select
                  value={status}
                  onChange={e => setStatus(e.target.value as any)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="MAINTENANCE">MAINTENANCE</option>
                  <option value="INACTIVE">INACTIVE</option>
                </select>
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 rounded-lg font-medium text-slate-600 hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg font-bold bg-blue-600 text-white hover:bg-blue-700 shadow-xs"
                >
                  Save Warehouse
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
