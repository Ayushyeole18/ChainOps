# ChainOps – Enterprise Supply Chain Management System

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

> **ChainOps** is a production-grade, enterprise desktop Supply Chain Management System built in **Java 17+**, **JavaFX 21**, **Maven**, and **MySQL with JDBC**. It centralizes the complete lifecycle of end-to-end supply chain operations: procurement, multi-warehouse inventory tracking, sales order processing, stock reservations, freight logistics shipments, inter-facility stock transfers, and automated CSV analytics.

---

## 🚀 Live Web Preview + Java Source Code Hub

This repository provides two complete artifacts:
1. **The Pure Java 17+ / JavaFX / Maven / MySQL Application**: Found in the `/ChainOps/` directory and runnable locally with `mvn javafx:run`.
2. **The ChainOps Live Interactive Preview & Source Code Explorer**: Runs in this web container, providing instant live testing of all 12 modules, realistic seeded database simulation, and a full Code Inspector & Exporter.

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

---

## ⚙️ Configuration

Copy the template database configuration:
```bash
cp ChainOps/src/main/resources/config/db.example.properties ChainOps/src/main/resources/config/db.properties
```

Edit `ChainOps/src/main/resources/config/db.properties` to match your local MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/supplychainx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.username=root
db.password=YOUR_PASSWORD_HERE
db.pool.maxSize=10
db.pool.minIdle=2
db.pool.timeout=30000
```

---

## 🚀 How to Run

### Option 1: Run via Maven (Recommended)
Navigate into the `ChainOps` directory:
```bash
cd ChainOps
mvn clean compile javafx:run
```

Or run the bundled startup script:
- Linux / macOS: `./run.sh`
- Windows: `run.bat`

### Option 2: Build Standalone Fat JAR
```bash
cd ChainOps
mvn clean package
java -jar target/chainops-system-1.0.0.jar
```

---

## 🔑 Demo Credentials

All demo accounts are pre-seeded in the database with secure BCrypt hashes:

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
cd ChainOps
mvn test
```
