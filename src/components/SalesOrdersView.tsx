import React, { useState } from 'react';
import {
  ShoppingCart,
  Search,
  Plus,
  CheckCircle2,
  XCircle,
  Truck,
  Trash2,
  AlertCircle
} from 'lucide-react';
import {
  SalesOrder,
  SalesOrderItem,
  Warehouse,
  Product,
  InventoryRecord
} from '../data/initialData';

interface SalesOrdersViewProps {
  salesOrders: SalesOrder[];
  warehouses: Warehouse[];
  products: Product[];
  inventory: InventoryRecord[];
  onCreateSO: (so: Omit<SalesOrder, 'id'>) => void;
  onConfirmSO: (id: number) => { success: boolean; message: string };
  onCancelSO: (id: number) => void;
  onNavigateToShipments: () => void;
}

export const SalesOrdersView: React.FC<SalesOrdersViewProps> = ({
  salesOrders,
  warehouses,
  products,
  inventory,
  onCreateSO,
  onConfirmSO,
  onCancelSO,
  onNavigateToShipments
}) => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [modalOpen, setModalOpen] = useState(false);
  const [alertNotice, setAlertNotice] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  // New SO Form State
  const [customerName, setCustomerName] = useState('');
  const [customerEmail, setCustomerEmail] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [shippingAddress, setShippingAddress] = useState('');
  const [selectedWarehouseId, setSelectedWarehouseId] = useState(warehouses[0]?.id || 1);
  const [notes, setNotes] = useState('');
  const [draftItems, setDraftItems] = useState<SalesOrderItem[]>([]);

  // Item builder inputs
  const [lineProductId, setLineProductId] = useState(products[0]?.id || 1);
  const [lineQty, setLineQty] = useState('');
  const [linePrice, setLinePrice] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const filtered = salesOrders.filter(so => {
    const matchesSearch =
      so.soNumber.toLowerCase().includes(search.toLowerCase()) ||
      so.customerName.toLowerCase().includes(search.toLowerCase()) ||
      so.customerEmail.toLowerCase().includes(search.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || so.status === statusFilter;
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

    // Check if in draft
    const existingIndex = draftItems.findIndex(i => i.productId === prod.id);
    if (existingIndex >= 0) {
      const updated = [...draftItems];
      updated[existingIndex].quantity += qty;
      updated[existingIndex].subtotal = updated[existingIndex].quantity * updated[existingIndex].unitPrice;
      setDraftItems(updated);
    } else {
      const item: SalesOrderItem = {
        productId: prod.id,
        sku: prod.sku,
        productName: prod.name,
        quantity: qty,
        unitPrice: price,
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

  const handleSubmitSO = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customerName.trim() || !customerEmail.trim()) {
      setErrorMsg('Customer name and email are required.');
      return;
    }
    if (draftItems.length === 0) {
      setErrorMsg('At least one order line item is required.');
      return;
    }

    const warehouse = warehouses.find(w => w.id === selectedWarehouseId);
    const soNum = `SO-2026-${String(salesOrders.length + 101).padStart(3, '0')}`;

    onCreateSO({
      soNumber: soNum,
      customerName,
      customerEmail,
      customerPhone: customerPhone || '+1-555-0100',
      shippingAddress: shippingAddress || '100 Industrial Pkwy',
      warehouseId: selectedWarehouseId,
      warehouseName: warehouse?.name || 'Warehouse',
      orderDate: new Date().toISOString().split('T')[0],
      totalAmount: totalDraftAmount,
      status: 'PENDING',
      createdBy: 'Sarah Chen',
      notes,
      items: draftItems
    });

    setModalOpen(false);
    setCustomerName('');
    setCustomerEmail('');
    setDraftItems([]);
  };

  const handleConfirmOrder = (id: number) => {
    const res = onConfirmSO(id);
    if (res.success) {
      setAlertNotice({ type: 'success', message: res.message });
    } else {
      setAlertNotice({ type: 'error', message: res.message });
    }
    setTimeout(() => setAlertNotice(null), 6000);
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Sales Orders &amp; Fulfillment</h2>
          <p className="text-xs text-slate-500 mt-1">
            Customer order processing, inventory availability checking, stock reservation, and freight dispatch
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
          <span>Create New Sales Order</span>
        </button>
      </div>

      {/* Notice Banner */}
      {alertNotice && (
        <div className={`p-3.5 rounded-xl border text-xs font-medium flex items-center justify-between transition-all ${
          alertNotice.type === 'success'
            ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
            : 'bg-rose-50 text-rose-800 border-rose-200'
        }`}>
          <div className="flex items-center gap-2">
            {alertNotice.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
            ) : (
              <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />
            )}
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
            placeholder="Search SO number, customer name, email..."
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
          <option value="PENDING">Pending</option>
          <option value="CONFIRMED">Confirmed (Reserved)</option>
          <option value="PROCESSING">Processing</option>
          <option value="SHIPPED">Shipped</option>
          <option value="DELIVERED">Delivered</option>
          <option value="CANCELLED">Cancelled</option>
        </select>

        <button
          onClick={() => { setSearch(''); setStatusFilter('ALL'); }}
          className="px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 rounded-lg hover:bg-slate-200 transition-colors"
        >
          Reset Filters
        </button>
      </div>

      {/* Sales Orders Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 font-semibold select-none">
              <tr>
                <th className="py-3 px-4">SO #</th>
                <th className="py-3 px-4">Customer Client</th>
                <th className="py-3 px-4">Fulfillment Hub</th>
                <th className="py-3 px-4">Order Date</th>
                <th className="py-3 px-4 text-right">Total Amount</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Workflow Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map(so => {
                let badgeColor = 'bg-slate-100 text-slate-700';
                if (so.status === 'DELIVERED') badgeColor = 'bg-emerald-100 text-emerald-800 font-bold';
                else if (so.status === 'SHIPPED') badgeColor = 'bg-cyan-100 text-cyan-800 font-bold';
                else if (so.status === 'CONFIRMED') badgeColor = 'bg-blue-100 text-blue-800 font-bold';
                else if (so.status === 'PROCESSING') badgeColor = 'bg-indigo-100 text-indigo-800 font-bold';
                else if (so.status === 'PENDING') badgeColor = 'bg-amber-100 text-amber-800 font-bold';
                else if (so.status === 'CANCELLED') badgeColor = 'bg-rose-100 text-rose-800 line-through';

                return (
                  <tr key={so.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-blue-700">{so.soNumber}</td>
                    <td className="py-3 px-4 font-semibold text-slate-900">
                      <div>{so.customerName}</div>
                      <div className="text-[10px] text-slate-400 font-normal">{so.customerEmail}</div>
                    </td>
                    <td className="py-3 px-4 text-slate-600">{so.warehouseName.split(' - ')[0]}</td>
                    <td className="py-3 px-4 text-slate-500 font-mono">{so.orderDate}</td>
                    <td className="py-3 px-4 text-right font-bold text-slate-900 text-sm">
                      ${so.totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className={`px-2.5 py-0.5 rounded-full text-[10px] uppercase tracking-wider ${badgeColor}`}>
                        {so.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        {so.status === 'PENDING' && (
                          <button
                            onClick={() => handleConfirmOrder(so.id)}
                            title="Confirm and Reserve Inventory"
                            className="flex items-center gap-1 px-2.5 py-1 rounded bg-emerald-600 text-white hover:bg-emerald-700 font-semibold text-[11px] transition-colors shadow-2xs"
                          >
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            <span>Confirm &amp; Reserve</span>
                          </button>
                        )}
                        {(so.status === 'CONFIRMED' || so.status === 'PROCESSING') && (
                          <button
                            onClick={onNavigateToShipments}
                            title="Assign Freight Shipment"
                            className="flex items-center gap-1 px-2.5 py-1 rounded bg-blue-600 text-white hover:bg-blue-700 font-semibold text-[11px] transition-colors shadow-2xs"
                          >
                            <Truck className="w-3.5 h-3.5" />
                            <span>Dispatch</span>
                          </button>
                        )}
                        {so.status !== 'SHIPPED' && so.status !== 'DELIVERED' && so.status !== 'CANCELLED' && (
                          <button
                            onClick={() => onCancelSO(so.id)}
                            title="Cancel Order"
                            className="p-1 rounded text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                          >
                            <XCircle className="w-3.5 h-3.5" />
                          </button>
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

      {/* Create Sales Order Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50 overflow-y-auto">
          <div className="bg-white rounded-xl border border-slate-200 shadow-2xl max-w-2xl w-full p-6 space-y-4 my-8">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <h3 className="text-base font-bold text-slate-900">Create Outbound Sales Order</h3>
                <p className="text-xs text-slate-500">Enter customer destination, fulfillment warehouse, and line items</p>
              </div>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            {errorMsg && (
              <div className="p-3 rounded-lg bg-rose-50 text-rose-700 text-xs font-medium border border-rose-200">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmitSO} className="space-y-4 text-xs">
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Customer Client Name</label>
                  <input
                    type="text"
                    placeholder="Acme Industrial Corp"
                    value={customerName}
                    onChange={e => setCustomerName(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Customer Email</label>
                  <input
                    type="email"
                    placeholder="buyer@acmeind.com"
                    value={customerEmail}
                    onChange={e => setCustomerEmail(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Customer Phone</label>
                  <input
                    type="text"
                    placeholder="+1-555-0199"
                    value={customerPhone}
                    onChange={e => setCustomerPhone(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  />
                </div>

                <div className="col-span-2">
                  <label className="block font-semibold text-slate-700 mb-1">Shipping Street Address</label>
                  <input
                    type="text"
                    placeholder="1400 Prairie Way, Naperville, IL"
                    value={shippingAddress}
                    onChange={e => setShippingAddress(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Fulfillment Hub</label>
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
              </div>

              {/* Line Items Builder */}
              <div className="p-3.5 bg-slate-50 rounded-lg border border-slate-200 space-y-3">
                <h4 className="font-bold text-slate-900 text-xs">Requested Product Items</h4>

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
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Price ($)</label>
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
                        <th className="py-1.5 px-3 text-right">Price</th>
                        <th className="py-1.5 px-3 text-right">Subtotal</th>
                        <th className="py-1.5 px-3 text-center">Del</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 font-mono text-[11px]">
                      {draftItems.length === 0 ? (
                        <tr>
                          <td colSpan={6} className="py-3 text-center text-slate-400 font-sans text-xs">
                            No line items added yet.
                          </td>
                        </tr>
                      ) : (
                        draftItems.map((item, i) => (
                          <tr key={i}>
                            <td className="py-1.5 px-3 text-slate-500">{item.sku}</td>
                            <td className="py-1.5 px-3 font-sans text-slate-800">{item.productName}</td>
                            <td className="py-1.5 px-3 text-right">{item.quantity}</td>
                            <td className="py-1.5 px-3 text-right">${item.unitPrice.toFixed(2)}</td>
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
                  <span>Order Total: ${totalDraftAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
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
                  Submit Order
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
