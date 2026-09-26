import React, { useState } from 'react';
import {
  Search,
  Filter,
  Plus,
  Minus,
  AlertTriangle,
  History,
  FileSpreadsheet,
  CheckCircle2,
  Boxes
} from 'lucide-react';
import {
  InventoryRecord,
  Product,
  Warehouse,
  StockTransaction
} from '../data/initialData';

interface InventoryViewProps {
  inventory: InventoryRecord[];
  products: Product[];
  warehouses: Warehouse[];
  transactions: StockTransaction[];
  onStockAdjustment: (productId: number, warehouseId: number, delta: number, type: 'ADJUSTMENT_IN' | 'ADJUSTMENT_OUT', ref: string, notes: string) => void;
}

export const InventoryView: React.FC<InventoryViewProps> = ({
  inventory,
  products,
  warehouses,
  transactions,
  onStockAdjustment
}) => {
  const [search, setSearch] = useState('');
  const [warehouseFilter, setWarehouseFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Modal for Stock In / Out
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState<'ADJUSTMENT_IN' | 'ADJUSTMENT_OUT'>('ADJUSTMENT_IN');
  const [selectedProductId, setSelectedProductId] = useState(products[0]?.id || 1);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState(warehouses[0]?.id || 1);
  const [adjQuantity, setAdjQuantity] = useState('');
  const [adjRef, setAdjRef] = useState('');
  const [adjNotes, setAdjNotes] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  // Filtering
  const filtered = inventory.filter(item => {
    const matchesSearch =
      item.productName.toLowerCase().includes(search.toLowerCase()) ||
      item.sku.toLowerCase().includes(search.toLowerCase()) ||
      item.categoryName.toLowerCase().includes(search.toLowerCase());

    const matchesWarehouse =
      warehouseFilter === 'ALL' || item.warehouseId === Number(warehouseFilter);

    let stockStatus = 'IN_STOCK';
    if (item.quantityAvailable === 0) stockStatus = 'OUT_OF_STOCK';
    else if (item.quantityAvailable <= item.reorderLevel) stockStatus = 'LOW_STOCK';

    const matchesStatus =
      statusFilter === 'ALL' || statusFilter === stockStatus;

    return matchesSearch && matchesWarehouse && matchesStatus;
  });

  const handleOpenModal = (type: 'ADJUSTMENT_IN' | 'ADJUSTMENT_OUT', prodId?: number, whId?: number) => {
    setModalType(type);
    if (prodId) setSelectedProductId(prodId);
    if (whId) setSelectedWarehouseId(whId);
    setAdjQuantity('');
    setAdjRef(`ADJ-2026-${Math.floor(100 + Math.random() * 900)}`);
    setAdjNotes('');
    setErrorMsg('');
    setModalOpen(true);
  };

  const handleExecute = (e: React.FormEvent) => {
    e.preventDefault();
    const qty = parseInt(adjQuantity, 10);
    if (isNaN(qty) || qty <= 0) {
      setErrorMsg('Quantity must be a positive integer greater than zero.');
      return;
    }

    // Check if deducting more than available
    if (modalType === 'ADJUSTMENT_OUT') {
      const current = inventory.find(i => i.productId === selectedProductId && i.warehouseId === selectedWarehouseId);
      if (!current || current.quantityAvailable < qty) {
        setErrorMsg(`Insufficient stock: Only ${current?.quantityAvailable || 0} units available.`);
        return;
      }
    }

    onStockAdjustment(selectedProductId, selectedWarehouseId, qty, modalType, adjRef, adjNotes);
    setModalOpen(false);
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Inventory &amp; Stock Levels</h2>
          <p className="text-xs text-slate-500 mt-1">
            Real-time multi-warehouse inventory telemetry, automated low-stock warnings, and transaction audit trails
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => handleOpenModal('ADJUSTMENT_IN')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 transition-colors shadow-xs"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Stock In (Inbound)</span>
          </button>
          <button
            onClick={() => handleOpenModal('ADJUSTMENT_OUT')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800 text-white hover:bg-slate-900 transition-colors shadow-xs"
          >
            <Minus className="w-3.5 h-3.5" />
            <span>Stock Out (Write-Off)</span>
          </button>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search product name, SKU, or category..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white"
          />
        </div>

        <select
          value={warehouseFilter}
          onChange={e => setWarehouseFilter(e.target.value)}
          className="px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
        >
          <option value="ALL">All Warehouses</option>
          {warehouses.map(w => (
            <option key={w.id} value={w.id}>{w.name}</option>
          ))}
        </select>

        <select
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
          className="px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
        >
          <option value="ALL">All Stock Statuses</option>
          <option value="IN_STOCK">In Stock Only</option>
          <option value="LOW_STOCK">Low Stock Alerts</option>
          <option value="OUT_OF_STOCK">Out of Stock</option>
        </select>

        <button
          onClick={() => { setSearch(''); setWarehouseFilter('ALL'); setStatusFilter('ALL'); }}
          className="px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 rounded-lg hover:bg-slate-200 transition-colors"
        >
          Reset Filters
        </button>

        <button
          onClick={() => setStatusFilter('LOW_STOCK')}
          className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-rose-700 bg-rose-50 border border-rose-200 rounded-lg hover:bg-rose-100 transition-colors ml-auto"
        >
          <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
          <span>Show Low Stock ({inventory.filter(i => i.quantityAvailable <= i.reorderLevel).length})</span>
        </button>
      </div>

      {/* Master Inventory Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 font-semibold select-none">
              <tr>
                <th className="py-3 px-4">SKU</th>
                <th className="py-3 px-4">Product Name</th>
                <th className="py-3 px-4">Category</th>
                <th className="py-3 px-4">Facility Warehouse</th>
                <th className="py-3 px-4 text-right">Available Qty</th>
                <th className="py-3 px-4 text-right">Reserved</th>
                <th className="py-3 px-4 text-right">Reorder Level</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={9} className="py-8 text-center text-slate-400">
                    No matching inventory records found.
                  </td>
                </tr>
              ) : (
                filtered.map(item => {
                  let statusBadge = (
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800">
                      IN STOCK
                    </span>
                  );
                  if (item.quantityAvailable === 0) {
                    statusBadge = (
                      <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-100 text-rose-800 animate-pulse">
                        OUT OF STOCK
                      </span>
                    );
                  } else if (item.quantityAvailable <= item.reorderLevel) {
                    statusBadge = (
                      <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                        LOW STOCK
                      </span>
                    );
                  }

                  return (
                    <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="py-3 px-4 font-mono font-medium text-slate-600">{item.sku}</td>
                      <td className="py-3 px-4 font-semibold text-slate-900">{item.productName}</td>
                      <td className="py-3 px-4 text-slate-500">{item.categoryName}</td>
                      <td className="py-3 px-4 text-slate-700">
                        <span className="font-medium">{item.warehouseName.split(' - ')[0]}</span>
                        <span className="text-[10px] text-slate-400 block font-mono">[{item.warehouseCode}]</span>
                      </td>
                      <td className="py-3 px-4 text-right font-bold text-slate-900 text-sm">
                        {item.quantityAvailable.toLocaleString()}
                      </td>
                      <td className="py-3 px-4 text-right text-slate-500 font-medium">
                        {item.quantityReserved.toLocaleString()}
                      </td>
                      <td className="py-3 px-4 text-right text-slate-600 font-mono">
                        {item.reorderLevel}
                      </td>
                      <td className="py-3 px-4 text-center">{statusBadge}</td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => handleOpenModal('ADJUSTMENT_IN', item.productId, item.warehouseId)}
                            title="Quick Stock In"
                            className="p-1 rounded bg-emerald-50 text-emerald-700 hover:bg-emerald-100 transition-colors"
                          >
                            <Plus className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleOpenModal('ADJUSTMENT_OUT', item.productId, item.warehouseId)}
                            title="Quick Stock Out"
                            className="p-1 rounded bg-slate-100 text-slate-700 hover:bg-slate-200 transition-colors"
                          >
                            <Minus className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Stock Movement Audit Log */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <History className="w-4 h-4 text-slate-600" />
              <span>Stock Transaction Audit Ledger (MySQL stock_transactions)</span>
            </h3>
            <p className="text-xs text-slate-500">Every adjustment, transfer, PO receipt, and sales dispatch recorded</p>
          </div>
          <span className="text-xs font-medium text-slate-500">Total Recorded Movements: {transactions.length}</span>
        </div>

        <div className="overflow-x-auto max-h-72 overflow-y-auto border border-slate-100 rounded-lg">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 font-semibold sticky top-0 border-b border-slate-200">
              <tr>
                <th className="py-2.5 px-3">Timestamp</th>
                <th className="py-2.5 px-3">SKU</th>
                <th className="py-2.5 px-3">Product Name</th>
                <th className="py-2.5 px-3">Warehouse</th>
                <th className="py-2.5 px-3">Type</th>
                <th className="py-2.5 px-3 text-right">Delta</th>
                <th className="py-2.5 px-3">Reference #</th>
                <th className="py-2.5 px-3">Operator</th>
                <th className="py-2.5 px-3">Notes</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {transactions.map(tx => (
                <tr key={tx.id} className="hover:bg-slate-50/60 font-mono text-[11px]">
                  <td className="py-2 px-3 text-slate-400">{tx.timestamp}</td>
                  <td className="py-2 px-3 text-slate-700 font-semibold">{tx.sku}</td>
                  <td className="py-2 px-3 font-sans text-slate-800">{tx.productName}</td>
                  <td className="py-2 px-3 font-sans text-slate-600">{tx.warehouseName.split(' - ')[0]}</td>
                  <td className="py-2 px-3 font-bold text-blue-600">{tx.type}</td>
                  <td className={`py-2 px-3 text-right font-bold ${tx.quantity > 0 ? 'text-emerald-600' : 'text-slate-800'}`}>
                    {tx.quantity > 0 ? `+${tx.quantity}` : tx.quantity}
                  </td>
                  <td className="py-2 px-3 text-slate-600">{tx.referenceNumber}</td>
                  <td className="py-2 px-3 font-sans text-slate-600">{tx.performedBy}</td>
                  <td className="py-2 px-3 font-sans text-slate-500 italic truncate max-w-xs">{tx.notes}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Stock Adjustment Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-slate-200 shadow-2xl max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900">
                {modalType === 'ADJUSTMENT_IN' ? 'Execute Stock In (Inbound)' : 'Execute Stock Out (Deduction)'}
              </h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            {errorMsg && (
              <div className="p-3 rounded-lg bg-rose-50 text-rose-700 text-xs font-medium border border-rose-200">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleExecute} className="space-y-3 text-xs">
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

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Target Warehouse</label>
                <select
                  value={selectedWarehouseId}
                  onChange={e => setSelectedWarehouseId(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  {warehouses.map(w => (
                    <option key={w.id} value={w.id}>{w.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Adjustment Quantity (Units)</label>
                <input
                  type="number"
                  min="1"
                  placeholder="e.g. 50"
                  value={adjQuantity}
                  onChange={e => setAdjQuantity(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  required
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Reference Document / Code</label>
                <input
                  type="text"
                  value={adjRef}
                  onChange={e => setAdjRef(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white font-mono"
                  required
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Audit Notes / Reason</label>
                <textarea
                  rows={2}
                  value={adjNotes}
                  onChange={e => setAdjNotes(e.target.value)}
                  placeholder="Cycle count adjustment, freight damage, or warehouse write-off..."
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                ></textarea>
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
                  className={`px-4 py-2 rounded-lg font-bold text-white shadow-xs ${
                    modalType === 'ADJUSTMENT_IN' ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-rose-600 hover:bg-rose-700'
                  }`}
                >
                  Commit Stock Movement
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
