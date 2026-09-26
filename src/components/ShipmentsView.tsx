import React, { useState } from 'react';
import {
  Truck,
  Search,
  Plus,
  CheckCircle2,
  Navigation,
  Clock,
  MapPin,
  ExternalLink
} from 'lucide-react';
import {
  Shipment,
  SalesOrder
} from '../data/initialData';

interface ShipmentsViewProps {
  shipments: Shipment[];
  salesOrders: SalesOrder[];
  onCreateShipment: (s: Omit<Shipment, 'id'>) => void;
  onUpdateShipmentStatus: (id: number, status: Shipment['status'], notes?: string) => void;
}

export const ShipmentsView: React.FC<ShipmentsViewProps> = ({
  shipments,
  salesOrders,
  onCreateShipment,
  onUpdateShipmentStatus
}) => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [modalOpen, setModalOpen] = useState(false);

  // Form State
  const confirmedSOs = salesOrders.filter(so => so.status === 'CONFIRMED' || so.status === 'PROCESSING');
  const [selectedSoId, setSelectedSoId] = useState(confirmedSOs[0]?.id || 1);
  const [carrier, setCarrier] = useState('FedEx Logistics Direct');
  const [shipDate, setShipDate] = useState(new Date().toISOString().split('T')[0]);
  const [expectedDelivery, setExpectedDelivery] = useState('2026-10-02');
  const [notes, setNotes] = useState('');

  const filtered = shipments.filter(s => {
    const matchesSearch =
      s.trackingNumber.toLowerCase().includes(search.toLowerCase()) ||
      s.soNumber.toLowerCase().includes(search.toLowerCase()) ||
      s.customerName.toLowerCase().includes(search.toLowerCase()) ||
      s.carrier.toLowerCase().includes(search.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || s.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const generateTracking = (c: string) => {
    let prefix = 'TRK-GEN';
    if (c.includes('FedEx')) prefix = 'TRK-FDX';
    else if (c.includes('DHL')) prefix = 'TRK-DHL';
    else if (c.includes('UPS')) prefix = 'TRK-UPS';
    else if (c.includes('Maersk')) prefix = 'TRK-MSK';
    return `${prefix}-${Math.floor(10000000 + Math.random() * 90000000)}`;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const so = salesOrders.find(o => o.id === selectedSoId);
    if (!so) return;

    onCreateShipment({
      soId: so.id,
      soNumber: so.soNumber,
      customerName: so.customerName,
      trackingNumber: generateTracking(carrier),
      carrier,
      shipDate,
      expectedDelivery,
      status: 'READY',
      notes
    });

    setModalOpen(false);
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Logistics &amp; Freight Shipments</h2>
          <p className="text-xs text-slate-500 mt-1">
            Dispatch third-party logistics freight, track carrier waybills, and update delivery checkpoints
          </p>
        </div>
        <button
          onClick={() => {
            if (confirmedSOs.length === 0) {
              alert('No confirmed sales orders ready for shipment. Please confirm a pending sales order first.');
              return;
            }
            setSelectedSoId(confirmedSOs[0].id);
            setModalOpen(true);
          }}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-xs"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Dispatch New Shipment</span>
        </button>
      </div>

      {/* Toolbar */}
      <div className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search tracking #, sales order #, customer, carrier..."
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
          <option value="ALL">All Shipment Statuses</option>
          <option value="READY">Ready for Pickup</option>
          <option value="IN_TRANSIT">In Transit</option>
          <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
          <option value="DELIVERED">Delivered</option>
          <option value="DELAYED">Delayed</option>
        </select>

        <button
          onClick={() => { setSearch(''); setStatusFilter('ALL'); }}
          className="px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 rounded-lg hover:bg-slate-200 transition-colors"
        >
          Reset Filters
        </button>
      </div>

      {/* Shipments Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 font-semibold select-none">
              <tr>
                <th className="py-3 px-4">Tracking Waybill #</th>
                <th className="py-3 px-4">Sales Order #</th>
                <th className="py-3 px-4">Customer Destination</th>
                <th className="py-3 px-4">Logistics Carrier</th>
                <th className="py-3 px-4">Ship Date</th>
                <th className="py-3 px-4">Expected Delivery</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Checkpoint Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map(s => {
                let badgeClass = 'bg-slate-100 text-slate-700';
                if (s.status === 'DELIVERED') badgeClass = 'bg-emerald-100 text-emerald-800 font-bold';
                else if (s.status === 'OUT_FOR_DELIVERY') badgeClass = 'bg-indigo-100 text-indigo-800 font-bold animate-pulse';
                else if (s.status === 'IN_TRANSIT') badgeClass = 'bg-blue-100 text-blue-800 font-bold';
                else if (s.status === 'READY') badgeClass = 'bg-amber-100 text-amber-800 font-bold';
                else if (s.status === 'DELAYED') badgeClass = 'bg-rose-100 text-rose-800 font-bold';

                return (
                  <tr key={s.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-blue-700">{s.trackingNumber}</td>
                    <td className="py-3 px-4 font-mono text-slate-600">{s.soNumber}</td>
                    <td className="py-3 px-4 font-semibold text-slate-900">{s.customerName}</td>
                    <td className="py-3 px-4 text-slate-700 font-medium">{s.carrier}</td>
                    <td className="py-3 px-4 text-slate-500 font-mono">{s.shipDate}</td>
                    <td className="py-3 px-4 text-slate-500 font-mono">{s.expectedDelivery}</td>
                    <td className="py-3 px-4 text-center">
                      <span className={`px-2.5 py-0.5 rounded-full text-[10px] uppercase tracking-wider ${badgeClass}`}>
                        {s.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        {s.status === 'READY' && (
                          <button
                            onClick={() => onUpdateShipmentStatus(s.id, 'IN_TRANSIT', 'Scanned at regional terminal')}
                            className="px-2.5 py-1 rounded bg-blue-50 text-blue-700 hover:bg-blue-100 font-semibold text-[11px] transition-colors"
                          >
                            Mark In Transit
                          </button>
                        )}
                        {s.status === 'IN_TRANSIT' && (
                          <button
                            onClick={() => onUpdateShipmentStatus(s.id, 'OUT_FOR_DELIVERY', 'Loaded on delivery truck')}
                            className="px-2.5 py-1 rounded bg-indigo-50 text-indigo-700 hover:bg-indigo-100 font-semibold text-[11px] transition-colors"
                          >
                            Out for Delivery
                          </button>
                        )}
                        {s.status === 'OUT_FOR_DELIVERY' && (
                          <button
                            onClick={() => onUpdateShipmentStatus(s.id, 'DELIVERED', 'Delivered & signed by recipient')}
                            className="px-2.5 py-1 rounded bg-emerald-600 text-white hover:bg-emerald-700 font-semibold text-[11px] transition-colors shadow-2xs"
                          >
                            Confirm Delivery
                          </button>
                        )}
                        {s.status === 'DELIVERED' && (
                          <span className="text-[11px] text-emerald-700 font-semibold flex items-center gap-1">
                            <CheckCircle2 className="w-3.5 h-3.5" /> Completed
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

      {/* Dispatch Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-slate-200 shadow-2xl max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900">Dispatch Freight Carrier</h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Confirmed Sales Order</label>
                <select
                  value={selectedSoId}
                  onChange={e => setSelectedSoId(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  {confirmedSOs.map(so => (
                    <option key={so.id} value={so.id}>
                      {so.soNumber} - {so.customerName} (${so.totalAmount})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Logistics Carrier</label>
                <select
                  value={carrier}
                  onChange={e => setCarrier(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  <option value="FedEx Logistics Direct">FedEx Logistics Direct</option>
                  <option value="DHL Express Freight">DHL Express Freight</option>
                  <option value="UPS Supply Chain">UPS Supply Chain</option>
                  <option value="Maersk Intermodal">Maersk Intermodal</option>
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Ship Date</label>
                  <input
                    type="date"
                    value={shipDate}
                    onChange={e => setShipDate(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Expected Delivery</label>
                  <input
                    type="date"
                    value={expectedDelivery}
                    onChange={e => setExpectedDelivery(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Waybill Notes</label>
                <input
                  type="text"
                  placeholder="Pallet count, gate dock 4B, fragile..."
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
                  Dispatch Shipment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
