import React from 'react';
import {
  Package,
  Boxes,
  AlertTriangle,
  Building2,
  ClipboardList,
  ShoppingCart,
  Truck,
  Warehouse,
  ArrowUpRight,
  TrendingUp,
  Clock,
  Brain,
  ArrowRight,
  Sparkles
} from 'lucide-react';
import {
  Product,
  InventoryRecord,
  Supplier,
  PurchaseOrder,
  SalesOrder,
  Shipment,
  Warehouse as WarehouseType,
  Category,
  StockTransaction
} from '../data/initialData';

interface DashboardViewProps {
  products: Product[];
  inventory: InventoryRecord[];
  suppliers: Supplier[];
  purchaseOrders: PurchaseOrder[];
  salesOrders: SalesOrder[];
  shipments: Shipment[];
  warehouses: WarehouseType[];
  categories: Category[];
  transactions: StockTransaction[];
  onNavigateTab: (tab: any) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  products,
  inventory,
  suppliers,
  purchaseOrders,
  salesOrders,
  shipments,
  warehouses,
  categories,
  transactions,
  onNavigateTab
}) => {
  // Calculations
  const totalProducts = products.filter(p => p.status === 'ACTIVE').length;
  const totalStockUnits = inventory.reduce((sum, item) => sum + item.quantityAvailable, 0);
  const lowStockCount = inventory.filter(item => item.quantityAvailable <= item.reorderLevel).length;
  const activeSuppliers = suppliers.filter(s => s.status === 'ACTIVE').length;
  const pendingPOs = purchaseOrders.filter(po => po.status === 'PENDING' || po.status === 'APPROVED' || po.status === 'DRAFT').length;
  const pendingSOs = salesOrders.filter(so => so.status === 'PENDING' || so.status === 'CONFIRMED' || so.status === 'PROCESSING').length;
  const activeShipments = shipments.filter(s => s.status === 'READY' || s.status === 'IN_TRANSIT' || s.status === 'OUT_FOR_DELIVERY').length;
  const totalWarehouses = warehouses.filter(w => w.status === 'ACTIVE').length;

  // Chart 1: Stock by Category
  const catStats = categories.map(cat => {
    const units = inventory
      .filter(i => {
        const prod = products.find(p => p.id === i.productId);
        return prod?.categoryId === cat.id;
      })
      .reduce((sum, i) => sum + i.quantityAvailable, 0);
    return { name: cat.name, code: cat.code, units };
  });

  // Chart 2: Stock by Warehouse
  const whStats = warehouses.map(wh => {
    const units = inventory
      .filter(i => i.warehouseId === wh.id)
      .reduce((sum, i) => sum + i.quantityAvailable, 0);
    return { name: wh.name.split(' - ')[1] || wh.name, code: wh.code, units, capacity: wh.capacity };
  });

  // Chart 3: Order Status Breakdown
  const orderStatuses = [
    { label: 'Pending', count: salesOrders.filter(o => o.status === 'PENDING').length, color: 'bg-amber-500' },
    { label: 'Confirmed', count: salesOrders.filter(o => o.status === 'CONFIRMED').length, color: 'bg-blue-500' },
    { label: 'Processing', count: salesOrders.filter(o => o.status === 'PROCESSING').length, color: 'bg-indigo-500' },
    { label: 'Shipped', count: salesOrders.filter(o => o.status === 'SHIPPED').length, color: 'bg-cyan-500' },
    { label: 'Delivered', count: salesOrders.filter(o => o.status === 'DELIVERED').length, color: 'bg-emerald-500' },
    { label: 'Cancelled', count: salesOrders.filter(o => o.status === 'CANCELLED').length, color: 'bg-rose-500' },
  ];

  // Total Revenue
  const totalSalesRevenue = salesOrders
    .filter(o => o.status !== 'CANCELLED')
    .reduce((sum, o) => sum + o.totalAmount, 0);

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Executive Supply Chain Dashboard</h2>
          <p className="text-xs text-slate-500 mt-1">
            Real-time telemetry across multi-facility procurement, logistics fulfillment, and inventory balance
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-slate-500 bg-slate-100 px-3 py-1.5 rounded-lg border border-slate-200">
            Last Sync: Just now
          </span>
        </div>
      </div>

      {/* 8 Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1 */}
        <div
          onClick={() => onNavigateTab('products')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase">Active Products</span>
            <div className="p-2 rounded-lg bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <Package className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900">{totalProducts.toLocaleString()}</div>
          <div className="flex items-center gap-1 text-[11px] text-emerald-600 mt-1 font-medium">
            <span>In active catalog</span>
            <ArrowUpRight className="w-3 h-3" />
          </div>
        </div>

        {/* Card 2 */}
        <div
          onClick={() => onNavigateTab('inventory')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase">Physical Stock</span>
            <div className="p-2 rounded-lg bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <Boxes className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900">{totalStockUnits.toLocaleString()}</div>
          <div className="text-[11px] text-slate-500 mt-1">Available across all hubs</div>
        </div>

        {/* Card 3: Low Stock Alerts */}
        <div
          onClick={() => onNavigateTab('inventory')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-red-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase text-rose-600">Low Stock Alerts</span>
            <div className="p-2 rounded-lg bg-rose-50 text-rose-600 group-hover:bg-rose-600 group-hover:text-white transition-colors">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-rose-600">{lowStockCount}</div>
          <div className="text-[11px] text-rose-600 mt-1 font-semibold">Below safety reorder level</div>
        </div>

        {/* Card 4 */}
        <div
          onClick={() => onNavigateTab('suppliers')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase">Tier-1 Suppliers</span>
            <div className="p-2 rounded-lg bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <Building2 className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900">{activeSuppliers}</div>
          <div className="text-[11px] text-slate-500 mt-1">Vetted vendor roster</div>
        </div>

        {/* Card 5 */}
        <div
          onClick={() => onNavigateTab('purchase-orders')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase">Pending Inbound POs</span>
            <div className="p-2 rounded-lg bg-amber-50 text-amber-600 group-hover:bg-amber-600 group-hover:text-white transition-colors">
              <ClipboardList className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-amber-600">{pendingPOs}</div>
          <div className="text-[11px] text-slate-500 mt-1">Awaiting receiving</div>
        </div>

        {/* Card 6 */}
        <div
          onClick={() => onNavigateTab('sales-orders')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase">Open Sales Orders</span>
            <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
              <ShoppingCart className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-indigo-600">{pendingSOs}</div>
          <div className="text-[11px] text-slate-500 mt-1">In processing &amp; pending</div>
        </div>

        {/* Card 7 */}
        <div
          onClick={() => onNavigateTab('shipments')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase">Active Shipments</span>
            <div className="p-2 rounded-lg bg-cyan-50 text-cyan-600 group-hover:bg-cyan-600 group-hover:text-white transition-colors">
              <Truck className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900">{activeShipments}</div>
          <div className="text-[11px] text-emerald-600 mt-1 font-medium">In transit &amp; dispatched</div>
        </div>

        {/* Card 8 */}
        <div
          onClick={() => onNavigateTab('warehouses')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
        >
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-semibold tracking-wide uppercase">Warehouses</span>
            <div className="p-2 rounded-lg bg-emerald-50 text-emerald-600 group-hover:bg-emerald-600 group-hover:text-white transition-colors">
              <Warehouse className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-slate-900">{totalWarehouses}</div>
          <div className="text-[11px] text-slate-500 mt-1">Operational regional hubs</div>
        </div>
      </div>

      {/* AI / ML Operational Intelligence Banner & Insights */}
      <div className="bg-gradient-to-r from-blue-900/5 via-slate-900/5 to-indigo-900/5 border border-blue-200/80 rounded-2xl p-5 shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-blue-600 text-white flex items-center justify-center shadow-xs">
              <Brain className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-bold text-slate-900">AI / ML Operational Intelligence &amp; Risk Radar</h3>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-100 text-blue-800 border border-blue-200">
                  ML PREDICTIONS
                </span>
              </div>
              <p className="text-[11px] text-slate-500">
                Automated regression forecasts, lead-time variance analysis, and predictive replenishment alerts
              </p>
            </div>
          </div>

          <button
            onClick={() => onNavigateTab('ai-intelligence')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white shadow-xs transition-colors cursor-pointer shrink-0"
          >
            <span>Explore AI Intelligence</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          <div
            onClick={() => onNavigateTab('ai-intelligence')}
            className="bg-white p-3.5 rounded-xl border border-red-200/80 shadow-xs hover:border-red-400 transition-colors cursor-pointer"
          >
            <span className="text-[10px] font-bold text-red-600 uppercase tracking-wider block">High Stock-Out Threats</span>
            <span className="text-base font-bold text-slate-900 mt-1 block">4 SKUs at Critical Risk</span>
            <span className="text-[11px] text-slate-500 mt-0.5 block truncate">SKU-ELEC-201, SKU-TOOL-501</span>
          </div>

          <div
            onClick={() => onNavigateTab('ai-intelligence')}
            className="bg-white p-3.5 rounded-xl border border-emerald-200/80 shadow-xs hover:border-emerald-400 transition-colors cursor-pointer"
          >
            <span className="text-[10px] font-bold text-emerald-600 uppercase tracking-wider block">30-Day Demand Surges</span>
            <span className="text-base font-bold text-slate-900 mt-1 block">+28.5% Projected Growth</span>
            <span className="text-[11px] text-slate-500 mt-0.5 block truncate">Precision Electronics &amp; Tooling</span>
          </div>

          <div
            onClick={() => onNavigateTab('ai-intelligence')}
            className="bg-white p-3.5 rounded-xl border border-amber-200/80 shadow-xs hover:border-amber-400 transition-colors cursor-pointer"
          >
            <span className="text-[10px] font-bold text-amber-600 uppercase tracking-wider block">Supplier Delay Radar</span>
            <span className="text-base font-bold text-slate-900 mt-1 block">2 Vendors Elevated Risk</span>
            <span className="text-[11px] text-slate-500 mt-0.5 block truncate">SiliconCore Dynamics (31% delay)</span>
          </div>

          <div
            onClick={() => onNavigateTab('ai-intelligence')}
            className="bg-white p-3.5 rounded-xl border border-blue-200/80 shadow-xs hover:border-blue-400 transition-colors cursor-pointer"
          >
            <span className="text-[10px] font-bold text-blue-600 uppercase tracking-wider block">Recommended Reorder</span>
            <span className="text-base font-bold text-slate-900 mt-1 block">6 Orders Suggested</span>
            <span className="text-[11px] text-slate-500 mt-0.5 block truncate">Est. Spend: $69,425</span>
          </div>
        </div>
      </div>

      {/* Visual Charts & Operational Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Chart 1: Stock by Category */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900">Inventory Distribution by Category</h3>
              <p className="text-xs text-slate-500">Physical units allocated per item taxonomy</p>
            </div>
            <span className="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-1 rounded">
              {totalStockUnits.toLocaleString()} Units
            </span>
          </div>

          <div className="space-y-3">
            {catStats.map((c, idx) => {
              const pct = totalStockUnits > 0 ? (c.units / totalStockUnits) * 100 : 0;
              return (
                <div key={idx} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-medium text-slate-700 truncate">{c.name}</span>
                    <span className="font-semibold text-slate-900">{c.units.toLocaleString()} ({pct.toFixed(1)}%)</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                    <div
                      className="bg-blue-600 h-2 rounded-full transition-all duration-500"
                      style={{ width: `${pct}%` }}
                    ></div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Chart 2: Warehouse Volume */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900">Warehouse Storage Load</h3>
              <p className="text-xs text-slate-500">Utilization against max storage unit capacity</p>
            </div>
            <TrendingUp className="w-4 h-4 text-slate-400" />
          </div>

          <div className="space-y-4">
            {whStats.map((w, idx) => {
              const utilPct = w.capacity > 0 ? (w.units / w.capacity) * 100 : 0;
              const isHigh = utilPct > 80;
              return (
                <div key={idx} className="p-3 rounded-lg border border-slate-100 bg-slate-50/60 space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <div>
                      <span className="font-bold text-slate-900">{w.name}</span>
                      <span className="text-[10px] text-slate-500 ml-2 font-mono">[{w.code}]</span>
                    </div>
                    <span className={`font-bold px-2 py-0.5 rounded text-[10px] ${
                      isHigh ? 'bg-amber-100 text-amber-800' : 'bg-emerald-100 text-emerald-800'
                    }`}>
                      {utilPct.toFixed(1)}% Capacity
                    </span>
                  </div>
                  <div className="w-full bg-slate-200 rounded-full h-2.5 overflow-hidden">
                    <div
                      className={`h-2.5 rounded-full ${isHigh ? 'bg-amber-500' : 'bg-blue-600'}`}
                      style={{ width: `${Math.min(utilPct, 100)}%` }}
                    ></div>
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-500">
                    <span>{w.units.toLocaleString()} Units In Stock</span>
                    <span>Max: {w.capacity.toLocaleString()}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Chart 3: Order Status Pipeline */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900">Order Fulfillment Pipeline</h3>
              <p className="text-xs text-slate-500">Sales order volume across operational lifecycle</p>
            </div>
            <span className="text-xs font-semibold text-slate-600">Total: {salesOrders.length} Orders</span>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {orderStatuses.map((st, i) => (
              <div key={i} className="p-3 rounded-lg border border-slate-100 bg-slate-50 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className={`w-2.5 h-2.5 rounded-full ${st.color}`}></span>
                  <span className="text-xs font-medium text-slate-700">{st.label}</span>
                </div>
                <span className="text-sm font-bold text-slate-900">{st.count}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Audit Transactions */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900">Recent Inventory Audit Activity</h3>
              <p className="text-xs text-slate-500">Latest immutable stock movements logged to MySQL</p>
            </div>
            <Clock className="w-4 h-4 text-slate-400" />
          </div>

          <div className="divide-y divide-slate-100 max-h-60 overflow-y-auto">
            {transactions.slice(0, 5).map(tx => (
              <div key={tx.id} className="py-2.5 flex items-center justify-between text-xs">
                <div className="overflow-hidden pr-3">
                  <p className="font-semibold text-slate-800 truncate">{tx.productName}</p>
                  <p className="text-[11px] text-slate-500 flex items-center gap-1.5 mt-0.5">
                    <span className="font-mono text-blue-600">{tx.type}</span>
                    <span>•</span>
                    <span className="text-slate-400">{tx.timestamp}</span>
                  </p>
                </div>
                <div className={`text-right font-bold text-xs flex-shrink-0 ${
                  tx.quantity > 0 ? 'text-emerald-600' : 'text-slate-700'
                }`}>
                  {tx.quantity > 0 ? `+${tx.quantity}` : tx.quantity} Units
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
