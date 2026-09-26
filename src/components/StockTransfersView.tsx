import React, { useState } from 'react';
import {
  ArrowLeftRight,
  Search,
  Plus,
  CheckCircle2,
  AlertCircle,
  Warehouse as WarehouseIcon,
  Package
} from 'lucide-react';
import {
  StockTransfer,
  Warehouse,
  Product,
  InventoryRecord
} from '../data/initialData';

interface StockTransfersViewProps {
  transfers: StockTransfer[];
  warehouses: Warehouse[];
  products: Product[];
  inventory: InventoryRecord[];
  onExecuteTransfer: (t: Omit<StockTransfer, 'id'>) => { success: boolean; message: string };
}

export const StockTransfersView: React.FC<StockTransfersViewProps> = ({
  transfers,
  warehouses,
  products,
  inventory,
  onExecuteTransfer
}) => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [modalOpen, setModalOpen] = useState(false);
  const [alertNotice, setAlertNotice] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  // Form State
  const [sourceId, setSourceId] = useState(warehouses[0]?.id || 1);
  const [destId, setDestId] = useState(warehouses[1]?.id || 2);
  const [selectedProductId, setSelectedProductId] = useState(products[0]?.id || 1);
  const [quantity, setQuantity] = useState('');
  const [notes, setNotes] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  // Availability calculation
  const currentInv = inventory.find(i => i.productId === selectedProductId && i.warehouseId === sourceId);
  const availableInSource = currentInv?.quantityAvailable || 0;

  const filtered = transfers.filter(t => {
    const matchesSearch =
      t.transferNumber.toLowerCase().includes(search.toLowerCase()) ||
      t.sourceWarehouseName.toLowerCase().includes(search.toLowerCase()) ||
      t.destinationWarehouseName.toLowerCase().includes(search.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (sourceId === destId) {
      setErrorMsg('Source warehouse and destination warehouse must be different.');
      return;
    }

    const qty = parseInt(quantity, 10);
    if (isNaN(qty) || qty <= 0) {
      setErrorMsg('Quantity must be greater than zero.');
      return;
    }

    if (qty > availableInSource) {
      setErrorMsg(`Insufficient stock: Only ${availableInSource} units available in source facility.`);
      return;
    }

    const sourceWh = warehouses.find(w => w.id === sourceId);
    const destWh = warehouses.find(w => w.id === destId);
    const prod = products.find(p => p.id === selectedProductId);

    const trNum = `TR-2026-${String(transfers.length + 501).padStart(3, '0')}`;

    const res = onExecuteTransfer({
      transferNumber: trNum,
      sourceWarehouseId: sourceId,
      sourceWarehouseName: sourceWh?.name || 'Source',
      destinationWarehouseId: destId,
      destinationWarehouseName: destWh?.name || 'Dest',
      transferDate: new Date().toISOString().split('T')[0],
      status: 'COMPLETED',
      initiatedBy: 'Elena Rostova',
      notes,
      items: [
        {
          productId: prod?.id || 1,
          sku: prod?.sku || 'SKU',
          productName: prod?.name || 'Product',
          quantity: qty
        }
      ]
    });

    if (res.success) {
      setAlertNotice({ type: 'success', message: res.message });
      setModalOpen(false);
      setQuantity('');
      setNotes('');
    } else {
      setErrorMsg(res.message);
    }
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Inter-Warehouse Stock Transfers</h2>
          <p className="text-xs text-slate-500 mt-1">
            Rebalance facility inventories with atomic source deduction, destination increment, and audit trails
          </p>
        </div>
        <button
          onClick={() => {
            setModalOpen(true);
            setErrorMsg('');
          }}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-xs"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Initiate Stock Transfer</span>
        </button>
      </div>

      {/* Notice Banner */}
      {alertNotice && (
        <div className="p-3.5 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-800 text-xs font-medium flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
            <span>{alertNotice.message}</span>
          </div>
          <button onClick={() => setAlertNotice(null)} className="text-slate-400 hover:text-slate-600">✕</button>
        </div>
      )}

      {/* Toolbar */}
      <div className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search transfer #, source warehouse, destination..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white"
          />
        </div>

        <select
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
          className="px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
        >
          <option value="ALL">All Transfer Statuses</option>
          <option value="COMPLETED">Completed</option>
          <option value="IN_TRANSIT">In Transit</option>
          <option value="PENDING">Pending</option>
        </select>

        <button
          onClick={() => { setSearch(''); setStatusFilter('ALL'); }}
          className="px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 rounded-lg hover:bg-slate-200 transition-colors"
        >
          Reset Filters
        </button>
      </div>

      {/* Transfers Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 font-semibold select-none">
              <tr>
                <th className="py-3 px-4">Transfer #</th>
                <th className="py-3 px-4">Source Warehouse (Origin)</th>
                <th className="py-3 px-4">Destination Hub (Target)</th>
                <th className="py-3 px-4">Items Relocated</th>
                <th className="py-3 px-4">Date</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4">Initiated By</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map(t => (
                <tr key={t.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3 px-4 font-mono font-bold text-blue-700">{t.transferNumber}</td>
                  <td className="py-3 px-4 font-medium text-slate-800">{t.sourceWarehouseName.split(' - ')[0]}</td>
                  <td className="py-3 px-4 font-medium text-slate-800">{t.destinationWarehouseName.split(' - ')[0]}</td>
                  <td className="py-3 px-4 text-slate-600">
                    {t.items.map((i, idx) => (
                      <span key={idx} className="block text-[11px]">
                        <span className="font-semibold text-slate-900">{i.quantity}x</span> {i.productName}
                      </span>
                    ))}
                  </td>
                  <td className="py-3 px-4 text-slate-500 font-mono">{t.transferDate}</td>
                  <td className="py-3 px-4 text-center">
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 uppercase">
                      {t.status}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-slate-600">{t.initiatedBy}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Transfer Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-slate-200 shadow-2xl max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900">Authorize Stock Relocation</h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            {errorMsg && (
              <div className="p-3 rounded-lg bg-rose-50 text-rose-700 text-xs font-medium border border-rose-200">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Source Origin Facility</label>
                <select
                  value={sourceId}
                  onChange={e => setSourceId(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  {warehouses.map(w => (
                    <option key={w.id} value={w.id}>{w.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Product Item</label>
                <select
                  value={selectedProductId}
                  onChange={e => setSelectedProductId(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  {products.map(p => (
                    <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>
                  ))}
                </select>
              </div>

              {/* Real-time availability banner */}
              <div className="p-2.5 rounded-lg bg-blue-50 text-blue-800 text-[11px] font-semibold flex items-center justify-between border border-blue-200">
                <span>Available in Origin:</span>
                <span className="font-mono text-xs">{availableInSource} Units</span>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Quantity to Relocate</label>
                <input
                  type="number"
                  min="1"
                  max={availableInSource}
                  placeholder={`Max: ${availableInSource}`}
                  value={quantity}
                  onChange={e => setQuantity(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  required
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Destination Target Facility</label>
                <select
                  value={destId}
                  onChange={e => setDestId(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  {warehouses.map(w => (
                    <option key={w.id} value={w.id}>{w.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Transfer Notes / Reason</label>
                <input
                  type="text"
                  placeholder="Seasonal rebalancing, production demand spike..."
                  value={notes}
                  onChange={e => setNotes(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                />
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
                  Authorize &amp; Transfer
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
