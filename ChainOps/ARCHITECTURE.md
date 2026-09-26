# ChainOps – High-Level System Architecture

This document details the multi-tiered architecture, component design, and data-flow patterns of the **ChainOps – Supply Chain Management System**.

---

## 🏛️ High-Level Architectural Blueprint

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│                    TIER 1: PRESENTATION LAYER (JavaFX 21)                    │
│                                                                              │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌──────────────────┐  │
│  │    14 FXML Layouts    │  │   Enterprise CSS      │  │ NavigationManager│  │
│  │ (Dashboard, Orders,   │  │   Theme (style.css)   │  │ (Scene Director) │  │
│  │  Inventory, Shipments)│  │                       │  │                  │  │
│  └───────────────────────┘  └───────────────────────┘  └──────────────────┘  │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │  User Actions / Property Bindings
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│               TIER 2: CONTROLLER & EVENT DISPATCH LAYER                      │
│                                                                              │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌──────────────────┐  │
│  │ Operations Controllers│  │Master Data Controllers│  │Auth & Security   │  │
│  │ (Inventory, PO, SO,   │  │ (Product, Category,   │  │ (LoginController,│  │
│  │  Shipment, Transfer)  │  │  Supplier, Warehouse) │  │  UserController) │  │
│  └───────────────────────┘  └───────────────────────┘  └──────────────────┘  │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │  DTOs / Service Calls
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│               TIER 3: BUSINESS LOGIC & SERVICE LAYER                         │
│                                                                              │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌──────────────────┐  │
│  │      AuthService      │  │   InventoryService    │  │StockTransferSvc  │  │
│  │ (BCrypt Hash & RBAC)  │  │(Zero-Negative Check & │  │ (Atomic Multi-   │  │
│  │                       │  │ Reorder Thresholds)   │  │  Warehouse Tx)   │  │
│  └───────────────────────┘  └───────────────────────┘  └──────────────────┘  │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌──────────────────┐  │
│  │  PurchaseOrderService │  │   SalesOrderService   │  │  CsvExportUtil   │  │
│  │ (Inbound Goods Receipt│  │(Inventory Reservation │  │ (Valuation/Spend │  │
│  │  & Supplier Spend)    │  │ & Fulfillment Logic)  │  │  Data Streaming) │  │
│  └───────────────────────┘  └───────────────────────┘  └──────────────────┘  │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │  CRUD & Transaction Execution
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│            TIER 4: DATA ACCESS OBJECT (DAO) & JDBC LAYER                     │
│                                                                              │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌──────────────────┐  │
│  │     DAO Interfaces    │  │  HikariCP Connection  │  │  ACID Management │  │
│  │ (UserDao, ProductDao, │  │         Pool          │  │ (setAutoCommit(F)│  │
│  │  OrderDao, Shipment)  │  │(DatabaseConnection.j) │  │  commit/rollback)│  │
│  └───────────────────────┘  └───────────────────────┘  └──────────────────┘  │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │  Parameterized SQL (PreparedStatement)
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│               TIER 5: RELATIONAL PERSISTENCE (MySQL 8.0)                     │
│                                                                              │
│   InnoDB Storage Engine • Foreign Key RESTRICT • Unique SKU Indexes • B-Trees│
│                                                                              │
│   [roles]            [users]              [categories]      [products]       │
│   [suppliers]        [warehouses]         [inventory]       [transactions]   │
│   [purchase_orders]  [purchase_order_items]                                  │
│   [sales_orders]     [sales_order_items]  [shipments]       [stock_transfers]│
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Core End-to-End Workflow Data Flows

### 1. Inbound Procurement & Goods Receipt Workflow
1. **User Action**: Procurement Manager creates a new Purchase Order (`PO-2026-XXX`) in `PurchaseOrders.fxml`.
2. **Controller**: `PurchaseOrderController` validates vendor, items, and target facility, calling `PurchaseOrderService.createPurchaseOrder()`.
3. **DAO Layer**: `PurchaseOrderDao.create()` executes inside a single transaction to insert the master record and line items.
4. **Approval & Receipt**:
   - When marked **RECEIVED**, `PurchaseOrderService.receivePurchaseOrder()` coordinates:
     1. Status updated to `RECEIVED`.
     2. `InventoryDao.adjustStock()` increases `quantity_available` in the destination warehouse.
     3. `StockTransactionDao.record()` creates immutable audit trail record `INBOUND_PO`.
   - If any step fails, the connection calls `rollback()` to prevent inventory skew.

### 2. Outbound Order Fulfillment & Inventory Reservation
1. **Sales Order Creation**: Sales rep enters order with SKU and quantities in `SalesOrders.fxml`.
2. **Availability Check**: `SalesOrderService.confirmOrder()` verifies:
   $$\text{Quantity Available} \ge \text{Quantity Requested}$$
3. **Reservation Lock**: Items are reserved (`quantity_reserved` incremented).
4. **Logistics Dispatch**:
   - `ShipmentService.createShipment()` generates tracking number and dispatches with carrier.
   - On delivery, reserved stock is decremented and `OUTBOUND_SO` is logged.

### 3. Atomic Multi-Warehouse Stock Transfers
1. **Rebalance Request**: Transfer from *Central Fulfillment Hub (Chicago)* to *West Coast Port (Oakland)*.
2. **Validation**: Source facility available inventory $\ge$ transfer quantity.
3. **Atomic Multi-Hop DB Transaction**:
   ```sql
   START TRANSACTION;
   -- 1. Deduct from origin warehouse
   UPDATE inventory SET quantity_available = quantity_available - ? WHERE product_id = ? AND warehouse_id = ?;
   INSERT INTO stock_transactions (type, warehouse_id, ...) VALUES ('TRANSFER_OUT', ...);
   
   -- 2. Credit to destination warehouse
   UPDATE inventory SET quantity_available = quantity_available + ? WHERE product_id = ? AND warehouse_id = ?;
   INSERT INTO stock_transactions (type, warehouse_id, ...) VALUES ('TRANSFER_IN', ...);
   
   -- 3. Update transfer header
   UPDATE stock_transfers SET status = 'COMPLETED' WHERE transfer_id = ?;
   COMMIT;
   ```

---

## 🔒 Security Architecture (RBAC & Cryptography)

| Role | Operational Scope | Modules Allowed |
| :--- | :--- | :--- |
| **Administrator** | Full Enterprise Governance | All modules, User Access, Catalog, Audits |
| **Warehouse Manager** | Stock Handling & Transfers | Inventory, Stock Transfers, Goods Receipt, Shipments |
| **Procurement Manager**| Vendor Relations & Inbound | Purchase Orders, Suppliers, Inbound Receiving |
| **Sales Manager** | Customer Orders & Outbound | Sales Orders, Logistics Dispatch, Reports |

- **BCrypt Work Factor 12**: Passwords are salted and hashed using standard BCrypt (`PasswordUtil.java`), immune to rainbow tables.
- **Session Context**: Thread-safe `AuthService.getCurrentUser()` provides role checks at controller boundary.
