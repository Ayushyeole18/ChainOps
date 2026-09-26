-- =====================================================================
-- ChainOps – Supply Chain Management System
-- Database Schema Definition (MySQL 8.0+)
-- Database: supplychainx
-- =====================================================================

DROP DATABASE IF EXISTS supplychainx;
CREATE DATABASE supplychainx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE supplychainx;

-- ---------------------------------------------------------------------
-- Table: roles
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: categories
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: products
-- ---------------------------------------------------------------------
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(150) NOT NULL,
    category_id INT NOT NULL,
    description TEXT NULL,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    reorder_level INT NOT NULL DEFAULT 10,
    unit VARCHAR(20) NOT NULL DEFAULT 'Units',
    status ENUM('ACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: suppliers
-- ---------------------------------------------------------------------
CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BLACKLISTED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: warehouses
-- ---------------------------------------------------------------------
CREATE TABLE warehouses (
    warehouse_id INT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    location VARCHAR(200) NOT NULL,
    manager_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL DEFAULT 10000,
    current_utilization INT NOT NULL DEFAULT 0,
    status ENUM('ACTIVE', 'MAINTENANCE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: inventory
-- ---------------------------------------------------------------------
CREATE TABLE inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    warehouse_id INT NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    last_counted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT fk_inv_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT chk_positive_qty CHECK (quantity_available >= 0),
    CONSTRAINT chk_positive_res CHECK (quantity_reserved >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: stock_transactions
-- ---------------------------------------------------------------------
CREATE TABLE stock_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    warehouse_id INT NOT NULL,
    transaction_type ENUM('INBOUND_PO', 'OUTBOUND_SO', 'TRANSFER_OUT', 'TRANSFER_IN', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT') NOT NULL,
    quantity INT NOT NULL,
    reference_number VARCHAR(100) NULL,
    notes TEXT NULL,
    performed_by_user_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_user FOREIGN KEY (performed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: purchase_orders
-- ---------------------------------------------------------------------
CREATE TABLE purchase_orders (
    po_id INT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id INT NOT NULL,
    warehouse_id INT NOT NULL,
    order_date DATE NOT NULL,
    expected_delivery_date DATE NULL,
    actual_delivery_date DATE NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'RECEIVED', 'CANCELLED') DEFAULT 'DRAFT',
    created_by_user_id INT NULL,
    approved_by_user_id INT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_creator FOREIGN KEY (created_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: purchase_order_items
-- ---------------------------------------------------------------------
CREATE TABLE purchase_order_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    po_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity_ordered INT NOT NULL,
    quantity_received INT NOT NULL DEFAULT 0,
    unit_cost DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_poi_order FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: sales_orders
-- ---------------------------------------------------------------------
CREATE TABLE sales_orders (
    so_id INT AUTO_INCREMENT PRIMARY KEY,
    so_number VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(150) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(30) NULL,
    shipping_address TEXT NOT NULL,
    warehouse_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    created_by_user_id INT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_so_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT fk_so_creator FOREIGN KEY (created_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: sales_order_items
-- ---------------------------------------------------------------------
CREATE TABLE sales_order_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    so_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_soi_order FOREIGN KEY (so_id) REFERENCES sales_orders(so_id) ON DELETE CASCADE,
    CONSTRAINT fk_soi_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: shipments
-- ---------------------------------------------------------------------
CREATE TABLE shipments (
    shipment_id INT AUTO_INCREMENT PRIMARY KEY,
    so_id INT NOT NULL UNIQUE,
    tracking_number VARCHAR(100) NOT NULL UNIQUE,
    carrier VARCHAR(100) NOT NULL,
    shipment_date DATE NOT NULL,
    expected_delivery DATE NOT NULL,
    actual_delivery DATE NULL,
    shipping_notes TEXT NULL,
    status ENUM('READY', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'DELAYED', 'CANCELLED') DEFAULT 'READY',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ship_so FOREIGN KEY (so_id) REFERENCES sales_orders(so_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: stock_transfers
-- ---------------------------------------------------------------------
CREATE TABLE stock_transfers (
    transfer_id INT AUTO_INCREMENT PRIMARY KEY,
    transfer_number VARCHAR(50) NOT NULL UNIQUE,
    source_warehouse_id INT NOT NULL,
    destination_warehouse_id INT NOT NULL,
    transfer_date DATE NOT NULL,
    status ENUM('PENDING', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    initiated_by_user_id INT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_st_source FOREIGN KEY (source_warehouse_id) REFERENCES warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT fk_st_dest FOREIGN KEY (destination_warehouse_id) REFERENCES warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT fk_st_user FOREIGN KEY (initiated_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: stock_transfer_items
-- ---------------------------------------------------------------------
CREATE TABLE stock_transfer_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    transfer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_sti_transfer FOREIGN KEY (transfer_id) REFERENCES stock_transfers(transfer_id) ON DELETE CASCADE,
    CONSTRAINT fk_sti_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Indexes for query optimization
-- ---------------------------------------------------------------------
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_inventory_lookup ON inventory(product_id, warehouse_id);
CREATE INDEX idx_transactions_product ON stock_transactions(product_id);
CREATE INDEX idx_transactions_type ON stock_transactions(transaction_type);
CREATE INDEX idx_po_status ON purchase_orders(status);
CREATE INDEX idx_so_status ON sales_orders(status);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_number);

-- =====================================================================
-- AI / ML Predictive Intelligence Tables
-- =====================================================================

-- ---------------------------------------------------------------------
-- Table: ml_model_metadata
-- Tracks model training runs, algorithm types, and evaluation metrics
-- ---------------------------------------------------------------------
CREATE TABLE ml_model_metadata (
    model_id INT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL UNIQUE,
    model_type VARCHAR(50) NOT NULL,
    algorithm VARCHAR(150) NOT NULL,
    training_sample_size INT NOT NULL DEFAULT 0,
    mae DECIMAL(10,4) NULL,
    rmse DECIMAL(10,4) NULL,
    r_squared DECIMAL(6,4) NULL,
    accuracy_score DECIMAL(6,4) NULL,
    last_trained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('TRAINED', 'OUTDATED', 'TRAINING_FAILED') DEFAULT 'TRAINED',
    notes TEXT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: demand_forecasts
-- Stores generated product demand projections (7-day and 30-day)
-- ---------------------------------------------------------------------
CREATE TABLE demand_forecasts (
    forecast_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    forecast_period_days INT NOT NULL DEFAULT 30,
    historical_daily_avg DECIMAL(10,2) NOT NULL,
    predicted_quantity INT NOT NULL,
    confidence_lower INT NOT NULL,
    confidence_upper INT NOT NULL,
    trend_direction ENUM('UP', 'DOWN', 'STABLE') DEFAULT 'STABLE',
    trend_slope DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_forecast_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: stock_risk_predictions
-- Real-time stock-out vulnerability scores and estimated runout days
-- ---------------------------------------------------------------------
CREATE TABLE stock_risk_predictions (
    risk_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    current_stock INT NOT NULL,
    daily_velocity DECIMAL(10,2) NOT NULL,
    predicted_30d_demand INT NOT NULL,
    days_until_stockout INT NOT NULL,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    risk_score DECIMAL(5,2) NOT NULL,
    recommended_order_qty INT NOT NULL DEFAULT 0,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_risk_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: supplier_delay_predictions
-- Predictive delivery lead-time variance and delay probabilities
-- ---------------------------------------------------------------------
CREATE TABLE supplier_delay_predictions (
    delay_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    average_lead_time_days DECIMAL(6,2) NOT NULL,
    late_delivery_count INT NOT NULL DEFAULT 0,
    total_orders_evaluated INT NOT NULL DEFAULT 0,
    delay_probability DECIMAL(5,2) NOT NULL,
    risk_category ENUM('LOW', 'MODERATE', 'CRITICAL') NOT NULL,
    reliability_score DECIMAL(5,2) NOT NULL,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delay_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_forecast_prod ON demand_forecasts(product_id);
CREATE INDEX idx_stock_risk_prod ON stock_risk_predictions(product_id);
CREATE INDEX idx_delay_supp ON supplier_delay_predictions(supplier_id);

