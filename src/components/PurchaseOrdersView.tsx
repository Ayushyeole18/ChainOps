import React, { useState } from 'react';
import {
  ClipboardList,
  Search,
  Plus,
  CheckCircle,
  PackageCheck,
  XCircle,
  Trash2,
  Calendar,
  Building2,
  Warehouse as WarehouseIcon
} from 'lucide-react';
import {
  PurchaseOrder,
  PurchaseOrderItem,
  Supplier,
  Warehouse,
  Product
} from '../data/initialData';

interface PurchaseOrdersViewProps {
  purchaseOrders: PurchaseOrder[];
  suppliers: Supplier[];
  warehouses: Warehouse[];
  products: Product[];
  onCreatePO: (po: Omit<PurchaseOrder, 'id'>) => void;
  onApprovePO: (id: number) => void;
  onReceivePO: (id: number) => void;
  onCancelPO: (id: number) => void;
}

export const PurchaseOrdersView: React.FC<PurchaseOrdersViewProps> = ({
  purchaseOrders,
  suppliers,
  warehouses,
  products,
  onCreatePO,
  onApprovePO,
  onReceivePO,
  onCancelPO
}) => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [modalOpen, setModalOpen] = useState(false);

  // New PO Draft State
  const [selectedSupplierId, setSelectedSupplierId] = useState(suppliers[0]?.id || 1);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState(warehouses[0]?.id || 1);
  const [expectedDate, setExpectedDate] = useState('2026-10-15');
  const [notes, setNotes] = useState('');
  const [draftItems, setDraftItems] = useState<PurchaseOrderItem[]>([]);

  // Item builder inputs
  const [lineProductId, setLineProductId] = useState(products[0]?.id || 1);
  const [lineQty, setLineQty] = useState('');
  const [linePrice, setLinePrice] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const filtered = purchaseOrders.filter(po => {
    const matchesSearch =
      po.poNumber.toLowerCase().includes(search.toLowerCase()) ||
      po.supplierName.toLowerCase().includes(search.toLowerCase()) ||
      po.warehouseName.toLowerCase().includes(search.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || po.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleAddLineItem = () => {
    const qty = parseInt(lineQty, 10);
    const price = parseFloat(linePrice);

    if (isNaN(qty) || qty <= 0) {
      setErrorMsg('Quantity must be greater than zero.');
      return;
    }
    if (isNaN(price) || price < 0) {
      setErrorMsg('Price cannot be negative.');
      return;
    }

    const prod = products.find(p => p.id === lineProductId);
    if (!prod) return;

    // Check if already in draft
    const existingIndex = draftItems.findIndex(i => i.productId === prod.id);
    if (existingIndex >= 0) {
      const updated = [...draftItems];
      updated[existingIndex].quantityOrdered += qty;
      updated[existingIndex].subtotal = updated[existingIndex].quantityOrdered * updated[existingIndex].unitCost;
      setDraftItems(updated);
    } else {
      const item: PurchaseOrderItem = {
        productId: prod.id,
        sku: prod.sku,
        productName: prod.name,
        quantityOrdered: qty,
        quantityReceived: 0,
        unitCost: price,
        subtotal: qty * price
      };
      setDraftItems([...draftItems, item]);
    }

    setLineQty('');
    setErrorMsg('');
  };

  const handleRemoveLineItem = (index: number) => {
    setDraftItems(draftItems.filter((_, i) => i !== index));
  };

  const totalDraftAmount = draftItems.reduce((sum, item) => sum + item.subtotal, 0);

  const handleSubmitPO = (e: React.FormEvent) => {
    e.preventDefault();
    if (draftItems.length === 0) {
      setErrorMsg('At least one product line item is required.');
      return;
    }

    const supplier = suppliers.find(s => s.id === selectedSupplierId);
    const warehouse = warehouses.find(w => w.id === selectedWarehouseId);

    const poNum = `PO-2026-${String(purchaseOrders.length + 1).padStart(3, '0')}`;

    onCreatePO({
      poNumber: poNum,
      supplierId: selectedSupplierId,
      supplierName: supplier?.name || 'Supplier',
      warehouseId: selectedWarehouseId,
      warehouseName: warehouse?.name || 'Warehouse',
      orderDate: new Date().toISOString().split('T')[0],
      expectedDate: expectedDate || '2026-10-15',
      totalAmount: totalDraftAmount,
      status: 'PENDING',
      createdBy: 'David Sterling',
      notes,
      items: draftItems
    });

    setModalOpen(false);
    setDraftItems([]);
    setNotes('');
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Purchase Orders &amp; Procurement</h2>
          <p className="text-xs text-slate-500 mt-1">
            Supplier procurement contracts, approval gates, and atomic receiving into warehouse inventory
          </p>
        </div>
        <button
          onClick={() => {
            setModalOpen(true);
            setDraftItems([]);
            setErrorMsg('');
            if (products[0]) {
              setLineProductId(products[0].id);
              setLinePrice(products[0].unitPrice.toString());
            }
          }}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-xs"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Draft New Purchase Order</span>
        </button>
      </div>

      {/* Toolbar */}
      <div className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search PO number, supplier, warehouse..."
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
          <option value="ALL">All Order Statuses</option>
          <option value="DRAFT">Draft</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="RECEIVED">Received</option>
          <option value="CANCELLED">Cancelled</option>
        </select>

        <button
          onClick={() => { setSearch(''); setStatusFilter('ALL'); }}
          className="px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 rounded-lg hover:bg-slate-200 transition-colors"
        >
          Reset Filters
        </button>
      </div>

      {/* Purchase Orders Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 font-semibold select-none">
              <tr>
                <th className="py-3 px-4">PO #</th>
                <th className="py-3 px-4">Supplier Partner</th>
                <th className="py-3 px-4">Destination Hub</th>
                <th className="py-3 px-4">Order Date</th>
                <th className="py-3 px-4">Expected Date</th>
                <th className="py-3 px-4 text-right">Total Amount</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Workflow Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map(po => {
                let badgeColor = 'bg-slate-100 text-slate-700';
                if (po.status === 'RECEIVED') badgeColor = 'bg-emerald-100 text-emerald-800 font-bold';
                else if (po.status === 'APPROVED') badgeColor = 'bg-blue-100 text-blue-800 font-bold';
                else if (po.status === 'PENDING') badgeColor = 'bg-amber-100 text-amber-800 font-bold';
                else if (po.status === 'CANCELLED') badgeColor = 'bg-rose-100 text-rose-800 line-through';

                return (
                  <tr key={po.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-blue-700">{po.poNumber}</td>
                    <td className="py-3 px-4 font-semibold text-slate-900">{po.supplierName}</td>
                    <td className="py-3 px-4 text-slate-600">{po.warehouseName.split(' - ')[0]}</td>
                    <td className="py-3 px-4 text-slate-500 font-mono">{po.orderDate}</td>
                    <td className="py-3 px-4 text-slate-500 font-mono">{po.expectedDate}</td>
                    <td className="py-3 px-4 text-right font-bold text-slate-900 text-sm">
                      ${po.totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className={`px-2.5 py-0.5 rounded-full text-[10px] uppercase tracking-wider ${badgeColor}`}>
                        {po.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        {po.status === 'PENDING' && (
                          <button
                            onClick={() => onApprovePO(po.id)}
                            title="Approve PO"
                            className="px-2 py-1 rounded bg-blue-50 text-blue-700 hover:bg-blue-100 font-semibold text-[11px] transition-colors"
                          >
                            Approve
                          </button>
                        )}
                        {(po.status === 'APPROVED' || po.status === 'PENDING') && (
                          <button
                            onClick={() => onReceivePO(po.id)}
                            title="Receive Inbound Stock"
                            className="flex items-center gap-1 px-2.5 py-1 rounded bg-emerald-600 text-white hover:bg-emerald-700 font-semibold text-[11px] transition-colors shadow-2xs"
                          >
                            <PackageCheck className="w-3.5 h-3.5" />
                            <span>Receive Stock</span>
                          </button>
                        )}
                        {po.status !== 'RECEIVED' && po.status !== 'CANCELLED' && (
                          <button
                            onClick={() => onCancelPO(po.id)}
                            title="Cancel Order"
                            className="p-1 rounded text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                          >
                            <XCircle className="w-3.5 h-3.5" />
                          </button>
                        )}
                        {po.status === 'RECEIVED' && (
                          <span className="text-[10px] text-emerald-700 font-semibold flex items-center gap-1">
                            <CheckCircle className="w-3.5 h-3.5" /> Stock Credited
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create Purchase Order Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50 overflow-y-auto">
          <div className="bg-white rounded-xl border border-slate-200 shadow-2xl max-w-2xl w-full p-6 space-y-4 my-8">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <h3 className="text-base font-bold text-slate-900">Create Procurement Purchase Order</h3>
                <p className="text-xs text-slate-500">Configure vendor agreement, receiving hub, and order line items</p>
              </div>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            {errorMsg && (
              <div className="p-3 rounded-lg bg-rose-50 text-rose-700 text-xs font-medium border border-rose-200">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmitPO} className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Supplier Partner</label>
                  <select
                    value={selectedSupplierId}
                    onChange={e => setSelectedSupplierId(Number(e.target.value))}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    {suppliers.map(s => (
                      <option key={s.id} value={s.id}>{s.name} ({s.country})</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Destination Warehouse</label>
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
                  <label className="block font-semibold text-slate-700 mb-1">Expected Delivery Date</label>
                  <input
                    type="date"
                    value={expectedDate}
                    onChange={e => setExpectedDate(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Terms / Notes</label>
                  <input
                    type="text"
                    value={notes}
                    onChange={e => setNotes(e.target.value)}
                    placeholder="Inspection certificate required, CIF terms..."
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  />
                </div>
              </div>

              {/* Line Items Builder */}
              <div className="p-3.5 bg-slate-50 rounded-lg border border-slate-200 space-y-3">
                <h4 className="font-bold text-slate-900 text-xs">Line Items</h4>

                <div className="grid grid-cols-12 gap-2 items-end">
                  <div className="col-span-6">
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Product</label>
                    <select
                      value={lineProductId}
                      onChange={e => {
                        const id = Number(e.target.value);
                        setLineProductId(id);
                        const p = products.find(prod => prod.id === id);
                        if (p) setLinePrice(p.unitPrice.toString());
                      }}
                      className="w-full px-2.5 py-1.5 text-xs border border-slate-200 rounded bg-white"
                    >
                      {products.map(p => (
                        <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>
                      ))}
                    </select>
                  </div>
                  <div className="col-span-3">
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Quantity</label>
                    <input
                      type="number"
                      min="1"
                      placeholder="Qty"
                      value={lineQty}
                      onChange={e => setLineQty(e.target.value)}
                      className="w-full px-2.5 py-1.5 text-xs border border-slate-200 rounded bg-white"
                    />
                  </div>
                  <div className="col-span-2">
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Unit Cost ($)</label>
                    <input
                      type="number"
                      step="0.01"
                      value={linePrice}
                      onChange={e => setLinePrice(e.target.value)}
                      className="w-full px-2.5 py-1.5 text-xs border border-slate-200 rounded bg-white"
                    />
                  </div>
                  <div className="col-span-1">
                    <button
                      type="button"
                      onClick={handleAddLineItem}
                      className="w-full py-1.5 rounded bg-blue-600 text-white font-bold hover:bg-blue-700 text-center"
                    >
                      +
                    </button>
                  </div>
                </div>

                {/* Items Table */}
                <div className="border border-slate-200 rounded bg-white max-h-40 overflow-y-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-100/70 border-b border-slate-200 text-[11px] font-semibold text-slate-600">
                      <tr>
                        <th className="py-1.5 px-3">SKU</th>
                        <th className="py-1.5 px-3">Product Name</th>
                        <th className="py-1.5 px-3 text-right">Qty</th>
                        <th className="py-1.5 px-3 text-right">Cost</th>
                        <th className="py-1.5 px-3 text-right">Subtotal</th>
                        <th className="py-1.5 px-3 text-center">Del</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 font-mono text-[11px]">
                      {draftItems.length === 0 ? (
                        <tr>
                          <td colSpan={6} className="py-3 text-center text-slate-400 font-sans text-xs">
                            No items added yet. Use form above to add lines.
                          </td>
                        </tr>
                      ) : (
                        draftItems.map((item, i) => (
                          <tr key={i}>
                            <td className="py-1.5 px-3 text-slate-500">{item.sku}</td>
                            <td className="py-1.5 px-3 font-sans text-slate-800">{item.productName}</td>
                            <td className="py-1.5 px-3 text-right">{item.quantityOrdered}</td>
                            <td className="py-1.5 px-3 text-right">${item.unitCost.toFixed(2)}</td>
                            <td className="py-1.5 px-3 text-right font-bold text-slate-900">${item.subtotal.toFixed(2)}</td>
                            <td className="py-1.5 px-3 text-center">
                              <button
                                type="button"
                                onClick={() => handleRemoveLineItem(i)}
                                className="text-slate-400 hover:text-rose-600"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

                <div className="flex justify-end text-sm font-bold text-slate-900 pt-1">
                  <span>Total Amount: ${totalDraftAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                </div>
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
                  Issue Purchase Order
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
