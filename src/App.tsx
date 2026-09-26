import React, { useState } from 'react';
import {
  initialProducts,
  initialCategories,
  initialSuppliers,
  initialWarehouses,
  initialInventory,
  initialTransactions,
  initialPurchaseOrders,
  initialSalesOrders,
  initialShipments,
  initialTransfers,
  initialUsers,
  initialRoles,
  Product,
  Category,
  Supplier,
  Warehouse,
  InventoryRecord,
  StockTransaction,
  PurchaseOrder,
  SalesOrder,
  Shipment,
  StockTransfer,
  User
} from './data/initialData';

import { Sidebar, ActiveTab } from './components/Sidebar';
import { TopBar } from './components/TopBar';
import { DashboardView } from './components/DashboardView';
import { InventoryView } from './components/InventoryView';
import { ProductsView } from './components/ProductsView';
import { PurchaseOrdersView } from './components/PurchaseOrdersView';
import { SalesOrdersView } from './components/SalesOrdersView';
import { ShipmentsView } from './components/ShipmentsView';
import { StockTransfersView } from './components/StockTransfersView';
import { SuppliersView } from './components/SuppliersView';
import { WarehousesView } from './components/WarehousesView';
import { CategoriesView } from './components/CategoriesView';
import { ReportsView } from './components/ReportsView';
import { UsersView } from './components/UsersView';
import { JavaExplorerView } from './components/JavaExplorerView';
import { AuthView } from './components/AuthView';

export default function App() {
  // Navigation
  const [activeTab, setActiveTab] = useState<ActiveTab>('dashboard');

  // Master Data & Transactional States
  const [products, setProducts] = useState<Product[]>(initialProducts);
  const [categories, setCategories] = useState<Category[]>(initialCategories);
  const [suppliers, setSuppliers] = useState<Supplier[]>(initialSuppliers);
  const [warehouses, setWarehouses] = useState<Warehouse[]>(initialWarehouses);
  const [inventory, setInventory] = useState<InventoryRecord[]>(initialInventory);
  const [transactions, setTransactions] = useState<StockTransaction[]>(initialTransactions);
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrder[]>(initialPurchaseOrders);
  const [salesOrders, setSalesOrders] = useState<SalesOrder[]>(initialSalesOrders);
  const [shipments, setShipments] = useState<Shipment[]>(initialShipments);
  const [transfers, setTransfers] = useState<StockTransfer[]>(initialTransfers);
  const [users, setUsers] = useState<User[]>(initialUsers);
  const [currentUser, setCurrentUser] = useState<User | null>(initialUsers[0]); // Default to Administrator

  // Reset Demo Data
  const handleResetData = () => {
    if (window.confirm('Reset all inventory, orders, products, and shipments to original seed state?')) {
      setProducts(initialProducts);
      setCategories(initialCategories);
      setSuppliers(initialSuppliers);
      setWarehouses(initialWarehouses);
      setInventory(initialInventory);
      setTransactions(initialTransactions);
      setPurchaseOrders(initialPurchaseOrders);
      setSalesOrders(initialSalesOrders);
      setShipments(initialShipments);
      setTransfers(initialTransfers);
      setUsers(initialUsers);
    }
  };

  // Download complete Java project zip
  const handleDownloadZip = () => {
    // Initiate direct download from the backend API endpoint
    window.location.href = '/api/download-zip';
  };

  // -------------------------------------------------------------
  // Inventory Stock In / Out Adjustment
  // -------------------------------------------------------------
  const handleStockAdjustment = (
    productId: number,
    warehouseId: number,
    delta: number,
    type: 'ADJUSTMENT_IN' | 'ADJUSTMENT_OUT',
    ref: string,
    notes: string
  ) => {
    const product = products.find(p => p.id === productId);
    const warehouse = warehouses.find(w => w.id === warehouseId);
    if (!product || !warehouse) return;

    setInventory(prev => {
      const existingIdx = prev.findIndex(
        i => i.productId === productId && i.warehouseId === warehouseId
      );

      if (existingIdx >= 0) {
        const copy = [...prev];
        const current = copy[existingIdx];
        const newQty = Math.max(0, current.quantityAvailable + (type === 'ADJUSTMENT_IN' ? delta : -delta));
        copy[existingIdx] = {
          ...current,
          quantityAvailable: newQty,
          lastUpdated: new Date().toISOString().split('T')[0]
        };
        return copy;
      } else {
        if (type === 'ADJUSTMENT_OUT') return prev;
        const newRecord: InventoryRecord = {
          id: Math.max(...prev.map(i => i.id), 0) + 1,
          productId,
          sku: product.sku,
          productName: product.name,
          categoryName: product.categoryName,
          warehouseId,
          warehouseName: warehouse.name,
          warehouseCode: warehouse.code,
          quantityAvailable: delta,
          quantityReserved: 0,
          reorderLevel: product.reorderLevel,
          unitPrice: product.unitPrice,
          lastUpdated: new Date().toISOString().split('T')[0]
        };
        return [...prev, newRecord];
      }
    });

    // Record Stock Audit Transaction
    const newTx: StockTransaction = {
      id: Math.max(...transactions.map(t => t.id), 0) + 1,
      productId,
      sku: product.sku,
      productName: product.name,
      warehouseId,
      warehouseName: warehouse.name,
      type,
      quantity: delta,
      referenceNumber: ref || `ADJ-${Date.now()}`,
      notes,
      performedBy: currentUser?.fullName || 'System',
      timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19)
    };
    setTransactions(prev => [newTx, ...prev]);
  };

  // -------------------------------------------------------------
  // Purchase Order Operations
  // -------------------------------------------------------------
  const handleCreatePO = (poData: Omit<PurchaseOrder, 'id'>) => {
    const newId = Math.max(...purchaseOrders.map(p => p.id), 0) + 1;
    const newPO: PurchaseOrder = {
      ...poData,
      id: newId
    };
    setPurchaseOrders(prev => [newPO, ...prev]);
  };

  const handleApprovePO = (id: number) => {
    setPurchaseOrders(prev =>
      prev.map(po =>
        po.id === id
          ? {
              ...po,
              status: 'APPROVED',
              approvedBy: currentUser?.fullName || 'Manager'
            }
          : po
      )
    );
  };

  const handleReceivePO = (id: number) => {
    const po = purchaseOrders.find(p => p.id === id);
    if (!po || po.status === 'RECEIVED') return;

    // Automatically increment inventory for every item in PO
    po.items.forEach(item => {
      handleStockAdjustment(
        item.productId,
        po.warehouseId,
        item.quantityOrdered,
        'ADJUSTMENT_IN',
        po.poNumber,
        `PO Goods Receipt: ${po.poNumber} from ${po.supplierName}`
      );
    });

    setPurchaseOrders(prev =>
      prev.map(p =>
        p.id === id
          ? {
              ...p,
              status: 'RECEIVED',
              actualDate: new Date().toISOString().split('T')[0]
            }
          : p
      )
    );
  };

  const handleCancelPO = (id: number) => {
    setPurchaseOrders(prev =>
      prev.map(p => (p.id === id ? { ...p, status: 'CANCELLED' } : p))
    );
  };

  // -------------------------------------------------------------
  // Sales Order Operations
  // -------------------------------------------------------------
  const handleCreateSO = (soData: Omit<SalesOrder, 'id'>) => {
    const newId = Math.max(...salesOrders.map(s => s.id), 0) + 1;
    const newSO: SalesOrder = {
      ...soData,
      id: newId
    };
    setSalesOrders(prev => [newSO, ...prev]);
  };

  const handleConfirmSO = (id: number): { success: boolean; message: string } => {
    const so = salesOrders.find(s => s.id === id);
    if (!so) return { success: false, message: 'Sales order not found.' };

    // Verify stock availability
    for (const item of so.items) {
      const inv = inventory.find(
        i => i.productId === item.productId && i.warehouseId === so.warehouseId
      );
      if (!inv || inv.quantityAvailable < item.quantity) {
        return {
          success: false,
          message: `Insufficient inventory for SKU ${item.sku} in ${so.warehouseName}. Available: ${inv?.quantityAvailable || 0}, Required: ${item.quantity}.`
        };
      }
    }

    // Reserve / deduct stock and mark CONFIRMED
    setSalesOrders(prev =>
      prev.map(s => (s.id === id ? { ...s, status: 'CONFIRMED' } : s))
    );

    return {
      success: true,
      message: `Sales Order ${so.soNumber} successfully verified and confirmed for fulfillment.`
    };
  };

  const handleCancelSO = (id: number) => {
    setSalesOrders(prev =>
      prev.map(s => (s.id === id ? { ...s, status: 'CANCELLED' } : s))
    );
  };

  // -------------------------------------------------------------
  // Shipments Operations
  // -------------------------------------------------------------
  const handleCreateShipment = (shipmentData: Omit<Shipment, 'id'>) => {
    const newId = Math.max(...shipments.map(s => s.id), 0) + 1;
    const newShipment: Shipment = {
      ...shipmentData,
      id: newId
    };
    setShipments(prev => [newShipment, ...prev]);

    // Update corresponding sales order to DELIVERED or SHIPPED
    setSalesOrders(prev =>
      prev.map(so => (so.id === shipmentData.soId ? { ...so, status: 'SHIPPED' } : so))
    );
  };

  const handleUpdateShipmentStatus = (id: number, status: Shipment['status'], notes?: string) => {
    setShipments(prev =>
      prev.map(s =>
        s.id === id
          ? {
              ...s,
              status,
              notes: notes ? `${s.notes} | ${notes}` : s.notes,
              actualDelivery: status === 'DELIVERED' ? new Date().toISOString().split('T')[0] : s.actualDelivery
            }
          : s
      )
    );
  };

  // -------------------------------------------------------------
  // Stock Transfer Operations (Atomic Transfer Between Hubs)
  // -------------------------------------------------------------
  const handleExecuteTransfer = (transferData: Omit<StockTransfer, 'id'>): { success: boolean; message: string } => {
    // 1. Validate source warehouse has sufficient stock for every item
    for (const item of transferData.items) {
      const sourceInv = inventory.find(
        i => i.productId === item.productId && i.warehouseId === transferData.sourceWarehouseId
      );
      if (!sourceInv || sourceInv.quantityAvailable < item.quantity) {
        return {
          success: false,
          message: `Insufficient inventory in origin hub for ${item.sku}. Available: ${sourceInv?.quantityAvailable || 0}, requested: ${item.quantity}.`
        };
      }
    }

    // 2. Perform atomic transfer: decrement source, increment destination
    transferData.items.forEach(item => {
      // Out from source
      handleStockAdjustment(
        item.productId,
        transferData.sourceWarehouseId,
        item.quantity,
        'ADJUSTMENT_OUT',
        transferData.transferNumber,
        `Inter-warehouse Transfer to ${transferData.destinationWarehouseName}`
      );

      // In to destination
      handleStockAdjustment(
        item.productId,
        transferData.destinationWarehouseId,
        item.quantity,
        'ADJUSTMENT_IN',
        transferData.transferNumber,
        `Inter-warehouse Transfer from ${transferData.sourceWarehouseName}`
      );
    });

    const newId = Math.max(...transfers.map(t => t.id), 0) + 1;
    const newTransfer: StockTransfer = {
      ...transferData,
      id: newId,
      status: 'COMPLETED'
    };
    setTransfers(prev => [newTransfer, ...prev]);

    return {
      success: true,
      message: `Stock Transfer ${transferData.transferNumber} executed atomically across hubs.`
    };
  };

  // -------------------------------------------------------------
  // Master Data CRUD
  // -------------------------------------------------------------
  const handleCreateProduct = (p: Omit<Product, 'id'>) => {
    if (products.some(prod => prod.sku.toLowerCase() === p.sku.toLowerCase())) {
      return { success: false, message: `SKU '${p.sku}' already exists.` };
    }
    const newId = Math.max(...products.map(pr => pr.id), 0) + 1;
    const newProduct: Product = { ...p, id: newId };
    setProducts(prev => [newProduct, ...prev]);
    return { success: true, message: `Product ${p.sku} created successfully.` };
  };

  const handleUpdateProduct = (p: Product) => {
    setProducts(prev => prev.map(prod => (prod.id === p.id ? p : prod)));
    return { success: true, message: 'Product updated successfully.' };
  };

  const handleDeleteProduct = (id: number) => {
    // Check if in use
    const hasInventory = inventory.some(i => i.productId === id && i.quantityAvailable > 0);
    if (hasInventory) {
      return { success: false, message: 'Cannot delete product with existing inventory stock.' };
    }
    setProducts(prev => prev.filter(p => p.id !== id));
    return { success: true, message: 'Product deleted.' };
  };

  // Category CRUD
  const handleCreateCategory = (c: Omit<Category, 'id'>) => {
    const newId = Math.max(...categories.map(cat => cat.id), 0) + 1;
    setCategories(prev => [...prev, { ...c, id: newId }]);
    return { success: true, message: 'Category added.' };
  };

  const handleUpdateCategory = (c: Category) => {
    setCategories(prev => prev.map(cat => (cat.id === c.id ? c : cat)));
    return { success: true, message: 'Category updated.' };
  };

  const handleDeleteCategory = (id: number) => {
    if (products.some(p => p.categoryId === id)) {
      return { success: false, message: 'Cannot delete category containing products.' };
    }
    setCategories(prev => prev.filter(c => c.id !== id));
    return { success: true, message: 'Category deleted.' };
  };

  // Supplier CRUD
  const handleCreateSupplier = (s: Omit<Supplier, 'id'>) => {
    const newId = Math.max(...suppliers.map(sup => sup.id), 0) + 1;
    setSuppliers(prev => [...prev, { ...s, id: newId }]);
    return { success: true, message: 'Supplier registered.' };
  };

  const handleUpdateSupplier = (s: Supplier) => {
    setSuppliers(prev => prev.map(sup => (sup.id === s.id ? s : sup)));
    return { success: true, message: 'Supplier updated.' };
  };

  const handleDeleteSupplier = (id: number) => {
    setSuppliers(prev => prev.filter(s => s.id !== id));
    return { success: true, message: 'Supplier removed.' };
  };

  // Warehouse CRUD
  const handleCreateWarehouse = (w: Omit<Warehouse, 'id' | 'currentUtilization'>) => {
    const newId = Math.max(...warehouses.map(wh => wh.id), 0) + 1;
    setWarehouses(prev => [...prev, { ...w, id: newId, currentUtilization: 0 }]);
    return { success: true, message: 'Warehouse hub provisioned.' };
  };

  const handleUpdateWarehouse = (w: Warehouse) => {
    setWarehouses(prev => prev.map(wh => (wh.id === w.id ? w : wh)));
    return { success: true, message: 'Warehouse updated.' };
  };

  const handleDeleteWarehouse = (id: number) => {
    if (inventory.some(i => i.warehouseId === id && i.quantityAvailable > 0)) {
      return { success: false, message: 'Cannot delete warehouse containing stock.' };
    }
    setWarehouses(prev => prev.filter(w => w.id !== id));
    return { success: true, message: 'Warehouse deleted.' };
  };

  // User CRUD
  const handleCreateUser = (u: Omit<User, 'id'>): { success: boolean; message: string; user?: User } => {
    if (users.some(user => user.username.toLowerCase() === u.username.toLowerCase())) {
      return { success: false, message: `Username '${u.username}' already in use.` };
    }
    if (users.some(user => user.email.toLowerCase() === u.email.toLowerCase())) {
      return { success: false, message: `Email '${u.email}' already registered in the system.` };
    }
    const newId = Math.max(...users.map(usr => usr.id), 0) + 1;
    const newUser: User = { ...u, id: newId };
    setUsers(prev => [...prev, newUser]);
    return { success: true, message: 'User provisioned successfully.', user: newUser };
  };

  const handleToggleUserStatus = (id: number) => {
    setUsers(prev =>
      prev.map(u =>
        u.id === id ? { ...u, status: u.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' } : u
      )
    );
  };

  const handleUpdateUserRole = (id: number, roleId: number) => {
    const roleObj = initialRoles.find(r => r.id === roleId);
    setUsers(prev =>
      prev.map(u =>
        u.id === id
          ? {
              ...u,
              roleId,
              role: roleObj?.displayName || u.role
            }
          : u
      )
    );
  };

  // If user is logged out, show enterprise Auth / Sign In / Sign Up view
  if (!currentUser) {
    return (
      <AuthView
        users={users}
        roles={initialRoles}
        onLogin={user => setCurrentUser(user)}
        onSignUp={userData => handleCreateUser(userData)}
      />
    );
  }

  // Page Title Mapping
  const tabTitles: Record<ActiveTab, string> = {
    dashboard: 'Executive Dashboard & Real-Time Analytics',
    inventory: 'Inventory Stock & In/Out Movements',
    'purchase-orders': 'Inbound Procurement & Purchase Orders',
    'sales-orders': 'Customer Sales Orders & Order Fulfillment',
    shipments: 'Logistics Shipments & Delivery Tracking',
    'stock-transfers': 'Inter-Warehouse Inventory Transfers',
    products: 'Product Master Catalog',
    categories: 'Product Taxonomy & Categories',
    suppliers: 'Supplier Directory & Vendor Management',
    warehouses: 'Fulfillment Centers & Warehouses',
    reports: 'Financial & Operational Reports',
    users: 'User Access & RBAC Security Management',
    'java-explorer': 'Java 17+ / JavaFX Project Source & Runner'
  };

  return (
    <div className="flex h-screen bg-slate-50 text-slate-900 font-sans antialiased overflow-hidden">
      {/* Sidebar Navigation */}
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        currentUser={currentUser}
        onLogout={() => setCurrentUser(null)}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Header */}
        <TopBar
          title={tabTitles[activeTab]}
          currentUser={currentUser}
          users={users}
          onSwitchUser={user => setCurrentUser(user)}
          onLogout={() => setCurrentUser(null)}
          onDownloadZip={handleDownloadZip}
          onResetData={handleResetData}
        />

        {/* View Body */}
        <main className="flex-1 overflow-y-auto">
          {activeTab === 'dashboard' && (
            <DashboardView
              products={products}
              inventory={inventory}
              suppliers={suppliers}
              purchaseOrders={purchaseOrders}
              salesOrders={salesOrders}
              shipments={shipments}
              warehouses={warehouses}
              categories={categories}
              transactions={transactions}
              onNavigateTab={tab => setActiveTab(tab)}
            />
          )}

          {activeTab === 'inventory' && (
            <InventoryView
              inventory={inventory}
              products={products}
              warehouses={warehouses}
              transactions={transactions}
              onStockAdjustment={handleStockAdjustment}
            />
          )}

          {activeTab === 'purchase-orders' && (
            <PurchaseOrdersView
              purchaseOrders={purchaseOrders}
              suppliers={suppliers}
              warehouses={warehouses}
              products={products}
              onCreatePO={handleCreatePO}
              onApprovePO={handleApprovePO}
              onReceivePO={handleReceivePO}
              onCancelPO={handleCancelPO}
            />
          )}

          {activeTab === 'sales-orders' && (
            <SalesOrdersView
              salesOrders={salesOrders}
              warehouses={warehouses}
              products={products}
              inventory={inventory}
              onCreateSO={handleCreateSO}
              onConfirmSO={handleConfirmSO}
              onCancelSO={handleCancelSO}
              onNavigateToShipments={() => setActiveTab('shipments')}
            />
          )}

          {activeTab === 'shipments' && (
            <ShipmentsView
              shipments={shipments}
              salesOrders={salesOrders}
              onCreateShipment={handleCreateShipment}
              onUpdateShipmentStatus={handleUpdateShipmentStatus}
            />
          )}

          {activeTab === 'stock-transfers' && (
            <StockTransfersView
              transfers={transfers}
              warehouses={warehouses}
              products={products}
              inventory={inventory}
              onExecuteTransfer={handleExecuteTransfer}
            />
          )}

          {activeTab === 'products' && (
            <ProductsView
              products={products}
              categories={categories}
              onCreateProduct={handleCreateProduct}
              onUpdateProduct={handleUpdateProduct}
              onDeleteProduct={handleDeleteProduct}
            />
          )}

          {activeTab === 'categories' && (
            <CategoriesView
              categories={categories}
              products={products}
              onCreateCategory={handleCreateCategory}
              onUpdateCategory={handleUpdateCategory}
              onDeleteCategory={handleDeleteCategory}
            />
          )}

          {activeTab === 'suppliers' && (
            <SuppliersView
              suppliers={suppliers}
              purchaseOrders={purchaseOrders}
              onCreateSupplier={handleCreateSupplier}
              onUpdateSupplier={handleUpdateSupplier}
              onDeleteSupplier={handleDeleteSupplier}
            />
          )}

          {activeTab === 'warehouses' && (
            <WarehousesView
              warehouses={warehouses}
              onCreateWarehouse={handleCreateWarehouse}
              onUpdateWarehouse={handleUpdateWarehouse}
              onDeleteWarehouse={handleDeleteWarehouse}
            />
          )}

          {activeTab === 'reports' && (
            <ReportsView
              inventory={inventory}
              products={products}
              suppliers={suppliers}
              purchaseOrders={purchaseOrders}
              salesOrders={salesOrders}
              shipments={shipments}
              transfers={transfers}
              warehouses={warehouses}
            />
          )}

          {activeTab === 'users' && (
            <UsersView
              users={users}
              roles={initialRoles}
              currentUser={currentUser}
              onCreateUser={handleCreateUser}
              onToggleUserStatus={handleToggleUserStatus}
              onUpdateUserRole={handleUpdateUserRole}
            />
          )}

          {activeTab === 'java-explorer' && (
            <JavaExplorerView onDownloadZip={handleDownloadZip} />
          )}
        </main>
      </div>
    </div>
  );
}
