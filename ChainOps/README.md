# ChainOps – Enterprise Supply Chain Management System

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

> **ChainOps** is a production-grade, enterprise desktop Supply Chain Management System built in **Java 17+**, **JavaFX 21**, **Maven**, and **MySQL with JDBC**. It centralizes the complete lifecycle of end-to-end supply chain operations: procurement, multi-warehouse inventory tracking, sales order processing, stock reservations, freight logistics shipments, inter-facility stock transfers, and automated CSV analytics.

---

## 📑 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Database Design & Schema](#database-design--schema)
- [Core Business Workflows](#core-business-workflows)
- [Security & Authentication](#security--authentication)
- [Technology Stack](#technology-stack)
- [Project Directory Structure](#project-directory-structure)
- [Prerequisites & Setup](#prerequisites--setup)
- [Database Initialization](#database-initialization)
- [Configuration](#configuration)
- [How to Run](#how-to-run)
- [Demo Credentials](#demo-credentials)
- [Automated Testing](#automated-testing)
- [College Viva & Interview Guide](#college-viva--interview-guide)

---

## 🌟 Overview

Modern supply chain management requires strict synchronization between procurement pipelines and warehouse availability. **ChainOps** replaces legacy, fragmented inventory spreadsheets with an atomic, transactional, multi-tiered architecture that enforces strict domain business rules:
- **Zero Negative Inventory Guarantees**: Stock cannot fall below zero under any concurrency condition.
- **Atomic Multi-Warehouse Transfers**: Inter-facility transfers debit origin and credit destination atomically within isolated database transactions.
- **Duplicate Prevention**: Inbound purchase orders cannot be received twice, preventing inventory inflation.
- **Automated Reorder Thresholding**: Real-time evaluation of low-stock and out-of-stock items against safety baselines.
- **Cryptographic Security**: Passwords are protected using salted **BCrypt** hashing.

---

## 🚀 Key Features

### 1. Executive Operations Dashboard
- Real-time stat cards: Total Products, Physical Stock Units, Low-Stock Alerts, Active Suppliers, Pending POs, Pending SOs, Active Shipments, Facilities.
- Dynamic charts loaded directly from MySQL:
  - *Inventory Volume by Category* (Pie Chart)
  - *Facility Stock Distribution* (Bar Chart)
  - *Sales Order Lifecycle Pipeline* (Pie Chart)
  - *Monthly Revenue Trends* (Line Chart)

### 2. Multi-Facility Inventory Management
- Real-time tracking of Available vs. Reserved stock across regional warehouse hubs.
- Automatic Stock Status evaluation:
  - `Available == 0` $\rightarrow$ **OUT OF STOCK**
  - `Available <= Reorder Level` $\rightarrow$ **LOW STOCK**
  - `Available > Reorder Level` $\rightarrow$ **IN STOCK**
- Stock In / Stock Out manual adjustment tools with required reference codes and audit justifications.
- Complete 500-movement immutable audit ledger tracking all inbound, outbound, transfer, and adjustment transactions.

### 3. Inbound Procurement (Purchase Orders)
- Supplier selection, facility destination, expected delivery schedule, and dynamic multi-line item builder.
- Automatic PO number sequencing (`PO-2026-XXX`).
- Two-stage approval workflow (`DRAFT` $\rightarrow$ `PENDING` $\rightarrow$ `APPROVED` $\rightarrow$ `RECEIVED`).
- **Atomic Receiving**: When received, inventory levels automatically increase, line item received quantities update, warehouse utilization recalibrates, and stock audit logs are written—all in one atomic transaction.

### 4. Outbound Fulfillment (Sales Orders)
- Customer order entry, delivery address capture, and fulfillment hub assignment.
- **Stock Reservation Engine**: Confirming an order checks physical availability, verifies no shortage exists, reserves inventory, and records outbound audit movements.
- Full cancellation workflow that gracefully releases reserved inventory back to available stock.

### 5. Freight Logistics & Shipment Tracking
- Automated tracking number generation by carrier (e.g. `TRK-FDX-XXXXXXXX`, `TRK-DHL-XXXXXXXX`, `TRK-MSK-XXXXXXXX`).
- Checkpoint status updates: `READY` $\rightarrow$ `IN_TRANSIT` $\rightarrow$ `OUT_FOR_DELIVERY` $\rightarrow$ `DELIVERED`.
- Synchronizes with sales order fulfillment status.

### 6. Inter-Warehouse Stock Transfers
- Safe rebalancing between facilities.
- Real-time live stock availability feedback prior to transfer submission.
- Source stock deduction and destination credit performed inside a single transaction.

### 7. Master Data Catalogs
- **Products**: SKU uniqueness enforcement, category linking, safety reorder thresholds, and unit of measure.
- **Categories**: Prevents accidental deletion if products are currently assigned.
- **Suppliers**: Contact directory, email/phone format validation, and spend tracking.
- **Warehouses**: Capacity limits, facility managers, and storage utilization gauges.

### 8. Analytics & Report Engine
- 8 dedicated reports:
  1. *Inventory Valuation & Stock Levels Report*
  2. *Critical Low-Stock & Reorder Report*
  3. *Supplier Performance & Vendor Roster*
  4. *Purchase Orders & Inbound Procurements*
  5. *Sales Orders & Fulfillment Revenue*
  6. *Logistics & Dispatch Shipments Report*
  7. *Inventory Audit & Stock Movements Log*
  8. *Warehouse Capacity & Storage Utilization*
- Date range filtering, facility filtering, and **1-click clean CSV file export**.

### 9. User Administration & RBAC
- Role-based security model: `ADMIN`, `WAREHOUSE_MANAGER`, `PROCUREMENT_MANAGER`, `SALES_MANAGER`.
- Password reset and account activation/suspension.

---

## 🏗️ System Architecture

ChainOps strictly follows the clean **Layered Architecture Pattern**:

```text
┌────────────────────────────────────────────────────────┐
│                   JavaFX UI Layer                      │
│      (FXML Views, CSS Theme, Controllers, Navigation)   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    Service Layer                       │
│    (Business Rules, Validations, Trans. Orchestration) │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                      DAO Layer                         │
│    (PreparedStatements, ResultSets, SQL Mappings)      │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                  JDBC Connection Pool                  │
│       (HikariCP High Performance Connection Pooling)   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    MySQL Database                      │
│         (supplychainx - ACID Relational Store)         │
└────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Design & Schema

The relational database `supplychainx` contains 15 interconnected tables optimized with foreign keys, constraints, and B-tree indexes:

```text
+------------------+         +-----------------+         +---------------------+
|      roles       |         |   categories    |         |      suppliers      |
+------------------+         +-----------------+         +---------------------+
| role_id (PK)     |         | category_id(PK) |         | supplier_id (PK)    |
| role_name        |         | category_name   |         | supplier_name       |
+--------+---------+         +--------+--------+         +----------+----------+
         |                            |                             |
         | 1:N                        | 1:N                         | 1:N
+--------v---------+         +--------v--------+         +----------v----------+
|      users       |         |    products     |         |   purchase_orders   |
+------------------+         +-----------------+         +---------------------+
| user_id (PK)     |         | product_id (PK) |<--------| po_id (PK)          |
| role_id (FK)     |         | category_id(FK) |   1:N   | supplier_id (FK)    |
| password_hash    |         | sku (UNIQUE)    |         | warehouse_id (FK)   |
+--------+---------+         +--------+--------+         +----------+----------+
         |                            |                             |
         |                            | 1:N                         | 1:N
         |                   +--------v--------+         +----------v----------+
         |                   |    inventory    |         | purchase_order_items|
         |                   +-----------------+         +---------------------+
         |                   | inventory_id(PK)|         | item_id (PK)        |
         |                   | product_id (FK) |         | po_id (FK)          |
         |                   | warehouse_id(FK)|         | product_id (FK)     |
         |                   +--------^--------+         +---------------------+
         |                            |
         |                            | 1:N
         |                   +--------+--------+
         |                   |   warehouses    |
         |                   +-----------------+
         |                   | warehouse_id(PK)|
         |                   | warehouse_name  |
         |                   +--------+--------+
         |                            |
         | 1:N                        | 1:N
+--------v----------------------------v--------+         +---------------------+
|             stock_transactions               |         |    sales_orders     |
+----------------------------------------------+         +---------------------+
| transaction_id (PK)                          |         | so_id (PK)          |
| product_id (FK)                              |<--------| warehouse_id (FK)   |
| warehouse_id (FK)                            |   1:N   | status              |
| performed_by_user_id (FK)                    |         +----------+----------+
+----------------------------------------------+                    |
                                                                    | 1:1
                                                         +----------v----------+
                                                         |      shipments      |
                                                         +---------------------+
                                                         | shipment_id (PK)    |
                                                         | so_id (FK, UNIQUE)  |
                                                         | tracking_number     |
                                                         +---------------------+
```

---

## ⚙️ Core Business Workflows

### 1. Purchase Order Receiving Workflow
```text
[PO Draft Created] ──> [Pending Approval] ──> [Approved by Manager]
                                                       │
                                                       ▼
                                            [Click "Receive Stock"]
                                                       │
                           ┌───────────────────────────┴───────────────────────────┐
                           ▼                                                       ▼
             Check PO Status != RECEIVED                             Verify Lines & Warehouse
                           │                                                       │
                           └───────────────────────────┬───────────────────────────┘
                                                       │
                                                       ▼
                                         [START DB TRANSACTION]
                                                       │
                                   ┌───────────────────┴───────────────────┐
                                   ▼                                       ▼
                       Increase Inventory Available             Record INBOUND_PO Transaction
                                   │                                       │
                                   └───────────────────┬───────────────────┘
                                                       │
                                                       ▼
                                          Mark PO Status = 'RECEIVED'
                                          Set actual_delivery_date = TODAY
                                          Update Warehouse Current Utilization
                                                       │
                                                       ▼
                                             [COMMIT TRANSACTION]
```

### 2. Sales Order Stock Reservation Workflow
```text
[Customer Order Placed] (Status: PENDING)
          │
          ▼
[Click "Confirm Order"]
          │
          ▼
[START DB TRANSACTION]
          │
          ├──> Query `inventory` with FOR UPDATE
          ├──> Available Quantity >= Requested Quantity?
          │         ├── NO  ──> ROLLBACK & throw BusinessRuleException("Insufficient Inventory")
          │         └── YES ──>
          │                 ├── Available Quantity -= Requested Quantity
          │                 ├── Reserved Quantity  += Requested Quantity
          │                 ├── Insert OUTBOUND_SO into `stock_transactions`
          │                 └── Set Sales Order status = 'CONFIRMED'
          │
          ▼
[COMMIT TRANSACTION]
          │
          ▼
[Ready for Shipment Waybill Generation]
```

---

## 🔒 Security & Authentication

- **BCrypt Password Hashing**: Passwords stored in MySQL use strong salts (work factor 12) via `jBCrypt`. Plaintext passwords are never persisted.
- **SQL Injection Prevention**: 100% of database interactions utilize parameterized `PreparedStatement`. No dynamic SQL string concatenation is permitted.
- **Connection Leak Prevention**: All connections, statements, and result sets are wrapped in Java `try-with-resources` blocks.
- **Role-Based Access Control (RBAC)**:
  - `ADMIN`: Unrestricted access across all operational, catalog, and user management modules.
  - `WAREHOUSE_MANAGER`: Access to Inventory, Stock Transfers, Warehouses, and Reports.
  - `PROCUREMENT_MANAGER`: Access to Suppliers, Purchase Orders, and Reports.
  - `SALES_MANAGER`: Access to Sales Orders, Shipments, and Reports.

---

## 💻 Technology Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| **Language** | Java (OpenJDK) | 17 LTS / 21 LTS | Core enterprise backend & client application |
| **Desktop UI** | JavaFX Controls & FXML | 21.0.2 | Hardware-accelerated desktop interface |
| **Build Tool** | Apache Maven | 3.8+ | Dependency management and build packaging |
| **Database** | MySQL Server | 8.0+ | Relational data persistence with ACID transactions |
| **Connection Pool**| HikariCP | 5.1.0 | High-performance JDBC connection pooling |
| **Security** | jBCrypt | 0.4 | Adaptive salted cryptographic password hashing |
| **Logging** | SLF4J Simple | 2.0.12 | Structured diagnostic and error logging |
| **Testing** | JUnit 5 | 5.10.2 | Automated unit and business logic regression testing |

---

## 📁 Project Directory Structure

```text
ChainOps/
├── pom.xml                                  # Maven dependencies & build configuration
├── README.md                                # Comprehensive documentation
├── LICENSE                                  # MIT open source license
├── .gitignore                               # Clean Git tracking configuration
│
├── database/
│   ├── schema.sql                           # MySQL database schema definition
│   └── seed.sql                             # Comprehensive realistic dataset
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/supplychainx/
    │   │       ├── Main.java                # JavaFX Application entrypoint
    │   │       ├── config/
    │   │       │   ├── DatabaseConfig.java  # Property file & environment loader
    │   │       │   └── DatabaseConnection.java # HikariCP pool & transaction helper
    │   │       ├── model/
    │   │       │   ├── Role.java            # RBAC Enum
    │   │       │   ├── User.java            # User credentials entity
    │   │       │   ├── Product.java         # SKU & pricing entity
    │   │       │   ├── Category.java        # Classification entity
    │   │       │   ├── Supplier.java        # Vendor partner entity
    │   │       │   ├── Warehouse.java       # Storage hub facility
    │   │       │   ├── InventoryItem.java   # Stock tracking & calculated status
    │   │       │   ├── StockTransaction.java# Audit ledger movement record
    │   │       │   ├── PurchaseOrder.java   # Inbound procurement header
    │   │       │   ├── PurchaseOrderItem.java# Inbound line item
    │   │       │   ├── SalesOrder.java      # Customer order header
    │   │       │   ├── SalesOrderItem.java  # Customer line item
    │   │       │   ├── Shipment.java        # Freight waybill & tracking
    │   │       │   ├── StockTransfer.java   # Inter-facility movement header
    │   │       │   ├── StockTransferItem.java # Transfer item
    │   │       │   └── DashboardMetrics.java# Analytics summary DTO
    │   │       ├── dao/
    │   │       │   ├── UserDao.java & UserDaoImpl.java
    │   │       │   ├── ProductDao.java & ProductDaoImpl.java
    │   │       │   ├── CategoryDao.java & CategoryDaoImpl.java
    │   │       │   ├── SupplierDao.java & SupplierDaoImpl.java
    │   │       │   ├── WarehouseDao.java & WarehouseDaoImpl.java
    │   │       │   ├── InventoryDao.java & InventoryDaoImpl.java
    │   │       │   ├── StockTransactionDao.java & StockTransactionDaoImpl.java
    │   │       │   ├── PurchaseOrderDao.java & PurchaseOrderDaoImpl.java
    │   │       │   ├── SalesOrderDao.java & SalesOrderDaoImpl.java
    │   │       │   ├── ShipmentDao.java & ShipmentDaoImpl.java
    │   │       │   ├── StockTransferDao.java & StockTransferDaoImpl.java
    │   │       │   └── DashboardDao.java & DashboardDaoImpl.java
    │   │       ├── service/
    │   │       │   ├── AuthService.java     # User login & session tracking
    │   │       │   ├── ProductService.java  # Catalog rules & validation
    │   │       │   ├── CategoryService.java # Safety deletion checks
    │   │       │   ├── SupplierService.java # Contact validation
    │   │       │   ├── WarehouseService.java# Capacity validation
    │   │       │   ├── InventoryService.java# Manual stock in/out adjustments
    │   │       │   ├── PurchaseOrderService.java # Atomic receiving workflow
    │   │       │   ├── SalesOrderService.java    # Stock reservation engine
    │   │       │   ├── ShipmentService.java # Automated tracking numbers
    │   │       │   ├── StockTransferService.java # Multi-hub transfers
    │   │       │   ├── DashboardService.java# Analytics aggregator
    │   │       │   └── ReportService.java   # 8 Custom reports & CSV exporter
    │   │       ├── util/
    │   │       │   ├── PasswordUtil.java    # BCrypt hashing & verification
    │   │       │   ├── ValidationUtil.java  # Regular expression & number rules
    │   │       │   └── CsvExportUtil.java   # CSV RFC 4180 file exporter
    │   │       ├── exception/
    │   │       │   ├── DatabaseException.java
    │   │       │   ├── ValidationException.java
    │   │       │   ├── BusinessRuleException.java
    │   │       │   └── AuthenticationException.java
    │   │       ├── ui/
    │   │       │   ├── AlertUtil.java       # Styled alert dialogs
    │   │       │   └── NavigationManager.java# View switcher & session router
    │   │       └── controller/
    │   │           ├── LoginController.java
    │   │           ├── MainLayoutController.java
    │   │           ├── DashboardController.java
    │   │           ├── InventoryController.java
    │   │           ├── PurchaseOrderController.java
    │   │           ├── SalesOrderController.java
    │   │           ├── ShipmentController.java
    │   │           ├── StockTransferController.java
    │   │           ├── ProductController.java
    │   │           ├── CategoryController.java
    │   │           ├── SupplierController.java
    │   │           ├── WarehouseController.java
    │   │           ├── ReportController.java
    │   │           └── UserController.java
    │   │
    │   └── resources/
    │       ├── config/
    │       │   ├── db.properties            # Active local MySQL credentials
    │       │   └── db.example.properties    # Safe template for GitHub
    │       ├── css/
    │       │   └── style.css                # Enterprise Dark Navy / Slate design system
    │       └── fxml/
    │           ├── Login.fxml
    │           ├── MainLayout.fxml
    │           ├── Dashboard.fxml
    │           ├── Inventory.fxml
    │           ├── PurchaseOrders.fxml
    │           ├── SalesOrders.fxml
    │           ├── Shipments.fxml
    │           ├── StockTransfers.fxml
    │           ├── Products.fxml
    │           ├── Categories.fxml
    │           ├── Suppliers.fxml
    │           ├── Warehouses.fxml
    │           ├── Reports.fxml
    │           └── Users.fxml
    │
    └── test/
        └── java/com/supplychainx/
            ├── ProductValidationTest.java      # SKU & price validation tests
            ├── InventoryValidationTest.java    # Low stock & status tests
            ├── OrderBusinessRuleTest.java      # Calculation & duplicate PO tests
            └── StockTransferValidationTest.java# Same-warehouse transfer tests
```

---

## 🔧 Prerequisites & Setup

Ensure the following tools are installed on your workstation:
1. **Java Development Kit (JDK) 17 or higher** (e.g. Eclipse Temurin, Oracle JDK, Amazon Corretto).
   Verify via terminal:
   ```bash
   java -version
   ```
2. **Apache Maven 3.8+**:
   Verify via terminal:
   ```bash
   mvn -version
   ```
3. **MySQL Server 8.0+** running locally on port `3306`.

---

## 🗃️ Database Initialization

Open MySQL terminal or your preferred tool (MySQL Workbench, DBeaver, DataGrip):

1. **Execute Schema Creation**:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
2. **Seed Realistic Sample Data**:
   ```bash
   mysql -u root -p < database/seed.sql
   ```

*Alternatively, execute the queries inside `database/schema.sql` followed by `database/seed.sql` directly inside MySQL Workbench.*

---

## ⚙️ Configuration

Copy the template database configuration:
```bash
cp src/main/resources/config/db.example.properties src/main/resources/config/db.properties
```

Edit `src/main/resources/config/db.properties` to match your local MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/supplychainx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.username=root
db.password=YOUR_PASSWORD_HERE
db.pool.maxSize=10
db.pool.minIdle=2
db.pool.timeout=30000
```

*(You may also configure environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD`)*.

---

## 🚀 How to Run

### Option 1: Run via Maven (Recommended)
From the project root:
```bash
mvn clean compile javafx:run
```

### Option 2: Build Standalone Fat JAR
```bash
mvn clean package
java -jar target/chainops-system-1.0.0.jar
```

---

## 🔑 Demo Credentials

All demo accounts are pre-seeded in the database with secure BCrypt hashes. You can also use the one-click quick credential buttons on the login screen:

| Role | Username / Email | Password | Access Scope |
|---|---|---|---|
| **System Administrator** | `admin@supplychainx.com` | `admin123` | Full access across all 12 modules |
| **Warehouse Manager** | `warehouse@supplychainx.com` | `warehouse123` | Inventory, Stock Transfers, Warehouses, Reports |
| **Procurement Lead** | `procure@supplychainx.com` | `procure123` | Suppliers, Purchase Orders, Reports |
| **Sales & Logistics** | `sales@supplychainx.com` | `sales123` | Sales Orders, Shipments, Reports |

---

## 🧪 Automated Testing

Run the full suite of automated unit tests using Maven Surefire:
```bash
mvn test
```

Test coverage includes:
- **Product Validation**: Tests SKU regex, uniqueness checks, price positivity, and negative reorder rejection.
- **Inventory Validation**: Tests stock status boundaries (`OUT OF STOCK`, `LOW STOCK`, `IN STOCK`), safety levels, and physical stock aggregation.
- **Order Business Rules**: Tests PO total recalculations, line item cost multiplication, duplicate receiving block, and sales order email constraints.
- **Stock Transfer Rules**: Tests transfer between identical source and destination facilities, zero item transfer validation, and quantity checks.

---

## 🎓 College Viva & Interview Guide

### 1. Why use JDBC over an ORM like Hibernate in this system?
*Direct JDBC with HikariCP provides fine-grained control over database transactions, precise locking (`SELECT ... FOR UPDATE`), and zero overhead for batch movements. It demonstrates mastery of core SQL, transaction boundaries (`conn.setAutoCommit(false)`), and PreparedStatement security.*

### 2. How are race conditions avoided during concurrent orders?
*When confirming a sales order, rows in `inventory` are locked using `FOR UPDATE`. If available stock is insufficient, the transaction rolls back immediately with a user-friendly `BusinessRuleException`, guaranteeing that inventory can never become negative.*

### 3. How does the application enforce the MVC pattern with JavaFX?
*FXML files declare the declarative View structure; CSS defines the visual design system; Controller classes capture user events and delegate processing to the Service Layer; Models encapsulate data state; and DAOs isolate all JDBC persistence.*
