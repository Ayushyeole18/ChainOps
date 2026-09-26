import React, { useState } from 'react';
import {
  BarChart3,
  Download,
  Calendar,
  Filter,
  DollarSign,
  Package,
  TrendingUp,
  AlertTriangle,
  Truck,
  Building2,
  FileSpreadsheet
} from 'lucide-react';
import {
  InventoryRecord,
  Product,
  Supplier,
  PurchaseOrder,
  SalesOrder,
  Shipment,
  StockTransfer,
  Warehouse
} from '../data/initialData';

interface ReportsViewProps {
  inventory: InventoryRecord[];
  products: Product[];
  suppliers: Supplier[];
  purchaseOrders: PurchaseOrder[];
  salesOrders: SalesOrder[];
  shipments: Shipment[];
  transfers: StockTransfer[];
  warehouses: Warehouse[];
}

export const ReportsView: React.FC<ReportsViewProps> = ({
  inventory,
  products,
  suppliers,
  purchaseOrders,
  salesOrders,
  shipments,
  transfers,
  warehouses
}) => {
  const [selectedReport, setSelectedReport] = useState<
    'inventory-valuation' | 'low-stock' | 'purchase-orders' | 'sales-orders' | 'shipments' | 'transfers'
  >('inventory-valuation');

  // Download CSV helper
  const exportCsv = (filename: string, headers: string[], rows: (string | number)[][]) => {
    const csvContent =
      'data:text/csv;charset=utf-8,' +
      [headers.join(','), ...rows.map(e => e.map(val => `"${String(val).replace(/"/g, '""')}"`).join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `${filename}_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Calculations
  const totalValuation = inventory.reduce((sum, item) => sum + item.quantityAvailable * item.unitPrice, 0);
  const lowStockItems = inventory.filter(i => i.quantityAvailable <= i.reorderLevel);
  const totalPurchaseSpend = purchaseOrders
    .filter(po => po.status !== 'CANCELLED')
    .reduce((sum, po) => sum + po.totalAmount, 0);
  const totalSalesRevenue = salesOrders
    .filter(so => so.status !== 'CANCELLED')
    .reduce((sum, so) => sum + so.totalAmount, 0);

  const handleExportCurrent = () => {
    if (selectedReport === 'inventory-valuation') {
      const headers = ['Inventory ID', 'Product SKU', 'Product Name', 'Category', 'Warehouse', 'Units Available', 'Units Reserved', 'Unit Price ($)', 'Total Value ($)', 'Status'];
      const rows = inventory.map(i => [
        i.id,
        i.sku,
        i.productName,
        i.categoryName,
        i.warehouseName,
        i.quantityAvailable,
        i.quantityReserved,
        i.unitPrice.toFixed(2),
        (i.quantityAvailable * i.unitPrice).toFixed(2),
        i.quantityAvailable <= i.reorderLevel ? 'LOW_STOCK' : 'HEALTHY'
      ]);
      exportCsv('chainops_inventory_valuation', headers, rows);
    } else if (selectedReport === 'low-stock') {
      const headers = ['Product SKU', 'Product Name', 'Warehouse', 'Available Qty', 'Reorder Threshold', 'Shortage Deficit', 'Unit Price ($)', 'Recommended Reorder ($)'];
      const rows = lowStockItems.map(i => [
        i.sku,
        i.productName,
        i.warehouseName,
        i.quantityAvailable,
        i.reorderLevel,
        Math.max(0, i.reorderLevel - i.quantityAvailable),
        i.unitPrice.toFixed(2),
        ((i.reorderLevel * 2 - i.quantityAvailable) * i.unitPrice).toFixed(2)
      ]);
      exportCsv('chainops_low_stock_alerts', headers, rows);
    } else if (selectedReport === 'purchase-orders') {
      const headers = ['PO Number', 'Supplier', 'Destination Warehouse', 'Order Date', 'Expected Date', 'Items Count', 'Total Amount ($)', 'Status'];
      const rows = purchaseOrders.map(po => [
        po.poNumber,
        po.supplierName,
        po.warehouseName,
        po.orderDate,
        po.expectedDate,
        po.items.length,
        po.totalAmount.toFixed(2),
        po.status
      ]);
      exportCsv('chainops_purchase_orders_report', headers, rows);
    } else if (selectedReport === 'sales-orders') {
      const headers = ['SO Number', 'Customer Name', 'Shipping Warehouse', 'Order Date', 'Total Amount ($)', 'Status'];
      const rows = salesOrders.map(so => [
        so.soNumber,
        so.customerName,
        so.warehouseName,
        so.orderDate,
        so.totalAmount.toFixed(2),
        so.status
      ]);
      exportCsv('chainops_sales_orders_report', headers, rows);
    } else if (selectedReport === 'shipments') {
      const headers = ['Tracking Number', 'Sales Order', 'Customer', 'Carrier', 'Ship Date', 'Expected Delivery', 'Status'];
      const rows = shipments.map(s => [
        s.trackingNumber,
        s.soNumber,
        s.customerName,
        s.carrier,
        s.shipDate,
        s.expectedDelivery,
        s.status
      ]);
      exportCsv('chainops_shipments_report', headers, rows);
    } else if (selectedReport === 'transfers') {
      const headers = ['Transfer Number', 'Source Warehouse', 'Destination Warehouse', 'Transfer Date', 'Items Count', 'Status', 'Initiated By'];
      const rows = transfers.map(t => [
        t.transferNumber,
        t.sourceWarehouseName,
        t.destinationWarehouseName,
        t.transferDate,
        t.items.length,
        t.status,
        t.initiatedBy
      ]);
      exportCsv('chainops_stock_transfers_report', headers, rows);
    }
  };

  return (
    <div className="p-8 space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2.5">
            <BarChart3 className="w-6 h-6 text-blue-600" />
            Supply Chain Intelligence & Reporting
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Real-time financial valuation, inventory levels, order velocity, and automated CSV data exports
          </p>
        </div>

        <button
          onClick={handleExportCurrent}
          className="flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm transition-colors cursor-pointer"
        >
          <Download className="w-4 h-4" />
          <span>Export Current Report to CSV</span>
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Inventory Value</span>
            <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
              <DollarSign className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900 mt-2">
            ${totalValuation.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <span className="text-[11px] text-emerald-600 font-medium mt-1 inline-flex items-center gap-1">
            Across {warehouses.length} active global hubs
          </span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Low Stock SKUs</span>
            <div className="w-8 h-8 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-amber-600 mt-2">{lowStockItems.length} SKUs</div>
          <span className="text-[11px] text-slate-500 mt-1 block">Below safety reorder threshold</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Procurement Spend</span>
            <div className="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Building2 className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900 mt-2">
            ${totalPurchaseSpend.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <span className="text-[11px] text-slate-500 mt-1 block">{purchaseOrders.length} Purchase Orders issued</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Sales Order Revenue</span>
            <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900 mt-2">
            ${totalSalesRevenue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <span className="text-[11px] text-slate-500 mt-1 block">{salesOrders.length} customer sales orders</span>
        </div>
      </div>

      {/* Report Selector Pills */}
      <div className="flex flex-wrap items-center gap-2 p-1.5 bg-slate-100 rounded-xl border border-slate-200">
        {[
          { id: 'inventory-valuation', label: 'Inventory Valuation', icon: DollarSign },
          { id: 'low-stock', label: 'Low Stock Alerts', icon: AlertTriangle },
          { id: 'purchase-orders', label: 'Purchase Orders & Spend', icon: Building2 },
          { id: 'sales-orders', label: 'Sales Fulfillment', icon: TrendingUp },
          { id: 'shipments', label: 'Shipments & Logistics', icon: Truck },
          { id: 'transfers', label: 'Inter-Warehouse Transfers', icon: Package }
        ].map(item => (
          <button
            key={item.id}
            onClick={() => setSelectedReport(item.id as any)}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-medium transition-colors cursor-pointer ${
              selectedReport === item.id
                ? 'bg-white text-blue-600 shadow-xs font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/60'
            }`}
          >
            <item.icon className="w-3.5 h-3.5" />
            <span>{item.label}</span>
          </button>
        ))}
      </div>

      {/* Report Table Display */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {selectedReport === 'inventory-valuation' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 border-b border-slate-200 uppercase font-semibold">
                <tr>
                  <th className="py-3 px-4">SKU</th>
                  <th className="py-3 px-4">Product Name</th>
                  <th className="py-3 px-4">Warehouse</th>
                  <th className="py-3 px-4 text-right">Available</th>
                  <th className="py-3 px-4 text-right">Reserved</th>
                  <th className="py-3 px-4 text-right">Unit Price</th>
                  <th className="py-3 px-4 text-right">Total Valuation</th>
                  <th className="py-3 px-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {inventory.map(item => (
                  <tr key={item.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-4 font-mono font-semibold text-blue-600">{item.sku}</td>
                    <td className="py-3 px-4 font-medium text-slate-900">{item.productName}</td>
                    <td className="py-3 px-4">{item.warehouseName}</td>
                    <td className="py-3 px-4 text-right font-semibold">{item.quantityAvailable.toLocaleString()}</td>
                    <td className="py-3 px-4 text-right text-slate-400">{item.quantityReserved}</td>
                    <td className="py-3 px-4 text-right">${item.unitPrice.toFixed(2)}</td>
                    <td className="py-3 px-4 text-right font-bold text-slate-900">
                      ${(item.quantityAvailable * item.unitPrice).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="py-3 px-4">
                      {item.quantityAvailable <= item.reorderLevel ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-amber-50 text-amber-700 border border-amber-200">
                          Reorder Required
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                          Optimal
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {selectedReport === 'low-stock' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-amber-50/60 text-amber-900 border-b border-amber-200 uppercase font-semibold">
                <tr>
                  <th className="py-3 px-4">SKU</th>
                  <th className="py-3 px-4">Product Name</th>
                  <th className="py-3 px-4">Warehouse Location</th>
                  <th className="py-3 px-4 text-right">Available Stock</th>
                  <th className="py-3 px-4 text-right">Reorder Threshold</th>
                  <th className="py-3 px-4 text-right">Shortage Deficit</th>
                  <th className="py-3 px-4 text-right">Unit Price</th>
                  <th className="py-3 px-4 text-right">Est. Replenish Cost</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {lowStockItems.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="py-8 text-center text-slate-400">
                      All inventory levels are currently above reorder safety thresholds.
                    </td>
                  </tr>
                ) : (
                  lowStockItems.map(item => {
                    const deficit = Math.max(0, item.reorderLevel - item.quantityAvailable);
                    const replenishCost = (item.reorderLevel * 2 - item.quantityAvailable) * item.unitPrice;
                    return (
                      <tr key={item.id} className="hover:bg-amber-50/20 transition-colors">
                        <td className="py-3 px-4 font-mono font-bold text-red-600">{item.sku}</td>
                        <td className="py-3 px-4 font-medium text-slate-900">{item.productName}</td>
                        <td className="py-3 px-4">{item.warehouseName}</td>
                        <td className="py-3 px-4 text-right font-bold text-red-600">{item.quantityAvailable}</td>
                        <td className="py-3 px-4 text-right text-slate-500">{item.reorderLevel}</td>
                        <td className="py-3 px-4 text-right font-semibold text-amber-700">+{deficit}</td>
                        <td className="py-3 px-4 text-right">${item.unitPrice.toFixed(2)}</td>
                        <td className="py-3 px-4 text-right font-bold text-slate-900">
                          ${replenishCost.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        )}

        {selectedReport === 'purchase-orders' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 border-b border-slate-200 uppercase font-semibold">
                <tr>
                  <th className="py-3 px-4">PO Number</th>
                  <th className="py-3 px-4">Supplier</th>
                  <th className="py-3 px-4">Destination Hub</th>
                  <th className="py-3 px-4">Order Date</th>
                  <th className="py-3 px-4">Expected Date</th>
                  <th className="py-3 px-4 text-center">Items</th>
                  <th className="py-3 px-4 text-right">Total Amount</th>
                  <th className="py-3 px-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {purchaseOrders.map(po => (
                  <tr key={po.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-blue-600">{po.poNumber}</td>
                    <td className="py-3 px-4 font-medium text-slate-900">{po.supplierName}</td>
                    <td className="py-3 px-4">{po.warehouseName}</td>
                    <td className="py-3 px-4">{po.orderDate}</td>
                    <td className="py-3 px-4">{po.expectedDate}</td>
                    <td className="py-3 px-4 text-center">{po.items.length}</td>
                    <td className="py-3 px-4 text-right font-bold text-slate-900">
                      ${po.totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="py-3 px-4">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-blue-50 text-blue-700 border border-blue-200">
                        {po.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {selectedReport === 'sales-orders' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 border-b border-slate-200 uppercase font-semibold">
                <tr>
                  <th className="py-3 px-4">SO Number</th>
                  <th className="py-3 px-4">Customer Name</th>
                  <th className="py-3 px-4">Warehouse</th>
                  <th className="py-3 px-4">Order Date</th>
                  <th className="py-3 px-4 text-center">Lines</th>
                  <th className="py-3 px-4 text-right">Total Amount</th>
                  <th className="py-3 px-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {salesOrders.map(so => (
                  <tr key={so.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-emerald-600">{so.soNumber}</td>
                    <td className="py-3 px-4 font-medium text-slate-900">{so.customerName}</td>
                    <td className="py-3 px-4">{so.warehouseName}</td>
                    <td className="py-3 px-4">{so.orderDate}</td>
                    <td className="py-3 px-4 text-center">{so.items.length}</td>
                    <td className="py-3 px-4 text-right font-bold text-slate-900">
                      ${so.totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="py-3 px-4">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                        {so.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {selectedReport === 'shipments' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 border-b border-slate-200 uppercase font-semibold">
                <tr>
                  <th className="py-3 px-4">Tracking Number</th>
                  <th className="py-3 px-4">SO Reference</th>
                  <th className="py-3 px-4">Customer</th>
                  <th className="py-3 px-4">Logistics Carrier</th>
                  <th className="py-3 px-4">Ship Date</th>
                  <th className="py-3 px-4">Expected Delivery</th>
                  <th className="py-3 px-4">Delivery Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {shipments.map(s => (
                  <tr key={s.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-indigo-600">{s.trackingNumber}</td>
                    <td className="py-3 px-4 font-mono">{s.soNumber}</td>
                    <td className="py-3 px-4 font-medium text-slate-900">{s.customerName}</td>
                    <td className="py-3 px-4">{s.carrier}</td>
                    <td className="py-3 px-4">{s.shipDate}</td>
                    <td className="py-3 px-4">{s.expectedDelivery}</td>
                    <td className="py-3 px-4">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
                        {s.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {selectedReport === 'transfers' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 border-b border-slate-200 uppercase font-semibold">
                <tr>
                  <th className="py-3 px-4">Transfer Number</th>
                  <th className="py-3 px-4">Origin Hub</th>
                  <th className="py-3 px-4">Destination Hub</th>
                  <th className="py-3 px-4">Date</th>
                  <th className="py-3 px-4 text-center">Items Transferred</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Initiator</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {transfers.map(t => (
                  <tr key={t.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-blue-600">{t.transferNumber}</td>
                    <td className="py-3 px-4 font-medium text-slate-900">{t.sourceWarehouseName}</td>
                    <td className="py-3 px-4 font-medium text-slate-900">{t.destinationWarehouseName}</td>
                    <td className="py-3 px-4">{t.transferDate}</td>
                    <td className="py-3 px-4 text-center">{t.items.reduce((s, i) => s + i.quantity, 0)} units</td>
                    <td className="py-3 px-4">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-slate-100 text-slate-700 border border-slate-200">
                        {t.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-500">{t.initiatedBy}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
