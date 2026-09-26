-- =====================================================================
-- ChainOps – Supply Chain Management System
-- Seed Data Script (MySQL 8.0+)
-- Database: supplychainx
-- =====================================================================

USE supplychainx;

-- 1. Insert Roles
INSERT INTO roles (role_id, role_name, description) VALUES
(1, 'ADMIN', 'System Administrator with full access to all modules and configurations'),
(2, 'WAREHOUSE_MANAGER', 'Manages warehouses, stock operations, transfers, and inventory levels'),
(3, 'PROCUREMENT_MANAGER', 'Manages supplier relations, purchase orders, and inbound shipments'),
(4, 'SALES_MANAGER', 'Manages customer orders, order fulfillment, and outbound logistics');

-- 2. Insert Users (BCrypt hashes for passwords)
-- admin@supplychainx.com -> admin123 ($2a$10$e8w99f3HkdQ184YJc9R39eC729K82mS8zN6h57vG54L4K2Bq3vDmi)
-- warehouse@supplychainx.com -> warehouse123 ($2a$10$g3Z1v4W6eX5y7pL2dE1q9uU8kL7bA6nC5mV3xK2jH1fG0yT9rE8wi)
-- procurement@supplychainx.com -> procure123 ($2a$10$t5P2q8W9lY1k3xZ7aB4c6dE8fG0hJ2kL4mN6pQ8rT0vV2xY4zB6xi)
-- sales@supplychainx.com -> sales123 ($2a$10$x4R7m1N9qZ3w5vT8bK2l6pC0dF2hJ4kM6nQ8sU0wX2yZ4aC6eD8yi)

INSERT INTO users (user_id, username, email, password_hash, full_name, role_id, status) VALUES
(1, 'admin', 'admin@supplychainx.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alexander Vance (System Admin)', 1, 'ACTIVE'),
(2, 'warehouse', 'warehouse@supplychainx.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Elena Rostova (Warehouse Director)', 2, 'ACTIVE'),
(3, 'procure', 'procure@supplychainx.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'David Sterling (Procurement Lead)', 3, 'ACTIVE'),
(4, 'sales', 'sales@supplychainx.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sarah Chen (Sales & Logistics Head)', 4, 'ACTIVE');

-- 3. Insert Categories
INSERT INTO categories (category_id, category_name, code, description, status) VALUES
(1, 'Industrial Raw Materials', 'CAT-RAW', 'Steel alloys, polymers, structural composites, and raw commodities', 'ACTIVE'),
(2, 'Precision Electronics & Chips', 'CAT-ELEC', 'Semiconductors, integrated circuits, microcontrollers, and sensors', 'ACTIVE'),
(3, 'Hydraulics & Fluid Power', 'CAT-HYD', 'High-pressure pumps, hydraulic cylinders, valves, and fluid hoses', 'ACTIVE'),
(4, 'Packaging & Crating', 'CAT-PKG', 'Reinforced corrugated containers, moisture-barrier wrapping, wooden pallets', 'ACTIVE'),
(5, 'Power Tools & Assembly Hardware', 'CAT-TOOL', 'Torque drills, automated pneumatic screwdrivers, fasteners, and fixtures', 'ACTIVE'),
(6, 'Safety & PPE Equipment', 'CAT-SAFE', 'Industrial respirators, high-visibility hazard gear, steel-toe boots, thermal gloves', 'ACTIVE');

-- 4. Insert Suppliers
INSERT INTO suppliers (supplier_id, supplier_name, contact_person, email, phone, address, city, country, status) VALUES
(1, 'Apex Metallurgical Corp', 'Marcus Thorne', 'm.thorne@apexmetallurgical.com', '+1-555-019-2831', '800 Iron Forge Pkwy', 'Pittsburgh', 'USA', 'ACTIVE'),
(2, 'SiliconCore Dynamics GmbH', 'Dr. Helga Weiss', 'orders@siliconcore-de.com', '+49-89-9238-110', 'Technologiepark 14', 'Munich', 'Germany', 'ACTIVE'),
(3, 'Pacific Rim Precision Ltd', 'Kenji Tanaka', 'tanaka@pacrimprecision.jp', '+81-3-5555-0142', '2-8-1 Nihonbashi', 'Tokyo', 'Japan', 'ACTIVE'),
(4, 'Nordic Fluid & Pressure AS', 'Lars Lindqvist', 'support@nordicfluid.no', '+47-22-33-4455', 'Fjordveien 88', 'Oslo', 'Norway', 'ACTIVE'),
(5, 'OmniPack Global Solutions', 'Maria Gonzales', 'mgonzales@omnipackglobal.com', '+1-555-012-7744', '420 Logistics Blvd', 'Dallas', 'USA', 'ACTIVE'),
(6, 'Vulcan Fasteners & Tooling', 'Robert MacIntyre', 'rmacintyre@vulcantools.co.uk', '+44-20-7946-0921', '74 Industrial Way', 'Birmingham', 'UK'),
(7, 'Seoul Sensor Technologies', 'Ji-Hoon Park', 'jhpark@seoulsensors.kr', '+82-2-3456-7890', 'Gasan Digital 1-ro', 'Seoul', 'South Korea', 'ACTIVE'),
(8, 'SafeGuard Industrial Gear', 'Brenda O''Connor', 'brenda@safeguardindustrial.com', '+1-555-018-9321', '12 Safety Harbor Rd', 'Cleveland', 'USA', 'ACTIVE');

-- 5. Insert Warehouses
INSERT INTO warehouses (warehouse_id, warehouse_name, code, location, manager_name, capacity, current_utilization, status) VALUES
(1, 'Central Fulfillment Hub - Chicago', 'WH-CHI-01', '4500 Industrial Dr, Chicago, IL', 'Elena Rostova', 50000, 31200, 'ACTIVE'),
(2, 'West Coast Port Distribution - Oakland', 'WH-OAK-02', '120 Maritime St, Oakland, CA', 'Tariq Al-Mansoor', 40000, 24500, 'ACTIVE'),
(3, 'East Coast Gateway - Newark', 'WH-NWK-03', '77 Terminal Rd, Newark, NJ', 'Colleen Bradley', 35000, 18900, 'ACTIVE'),
(4, 'European Hub - Rotterdam', 'WH-RTM-04', 'Maasvlakte Haven 45, Rotterdam, Netherlands', 'Wouter Van Dijk', 60000, 42000, 'ACTIVE');

-- 6. Insert Products (22 products across categories)
INSERT INTO products (product_id, sku, product_name, category_id, description, unit_price, reorder_level, unit, status) VALUES
(1, 'SKU-RAW-101', 'Cold-Rolled Carbon Steel Sheet 2mm', 1, 'High tensile steel sheet for automotive and structural framing', 85.50, 50, 'Sheets', 'ACTIVE'),
(2, 'SKU-RAW-102', 'Aircraft Grade Aluminum 6061 Bar', 1, 'Corrosion-resistant aluminum rod for CNC aerospace parts', 120.00, 40, 'Bars', 'ACTIVE'),
(3, 'SKU-RAW-103', 'Industrial Polypropylene Pellets (50kg)', 1, 'Thermoplastic polymer resin for high-volume injection molding', 95.00, 30, 'Bags', 'ACTIVE'),
(4, 'SKU-RAW-104', 'Titanium Grade 5 Round Wire 3mm', 1, 'Biomedical and marine-grade high durability titanium alloy', 340.00, 20, 'Spools', 'ACTIVE'),
(5, 'SKU-ELEC-201', 'ARM Cortex-M4 Microcontroller 120MHz', 2, 'Low power embedded SoC with hardware floating-point DSP', 14.50, 200, 'Units', 'ACTIVE'),
(6, 'SKU-ELEC-202', 'Industrial Optical LIDAR Sensor 40m', 2, 'Time-of-flight laser distance sensor for automated AGVs', 420.00, 15, 'Units', 'ACTIVE'),
(7, 'SKU-ELEC-203', 'Thermal Imaging Core 640x512', 2, 'Uncooled long-wave infrared sensor engine for predictive maintenance', 1250.00, 10, 'Units', 'ACTIVE'),
(8, 'SKU-ELEC-204', 'High-Speed Optocoupler 10MBd (100pk)', 2, 'Galvanic signal isolator for factory PLC bus controllers', 48.00, 80, 'Packs', 'ACTIVE'),
(9, 'SKU-HYD-301', 'Heavy Duty Double-Acting Hydraulic Cylinder', 3, '5000 PSI rated heavy earthmover hydraulic ram actuator', 580.00, 15, 'Units', 'ACTIVE'),
(10, 'SKU-HYD-302', 'Variable Displacement Piston Pump', 3, 'High pressure 350 bar axial piston pump with load sensing', 890.00, 12, 'Units', 'ACTIVE'),
(11, 'SKU-HYD-303', 'Steel Braided Hydraulic Hose 3/8" (50m)', 3, '4-spiral wire reinforced ultra-flex high abrasion hose', 220.00, 25, 'Rolls', 'ACTIVE'),
(12, 'SKU-HYD-304', 'Proportional Flow Control Valve 24VDC', 3, 'Closed-loop electronic hydraulic proportional throttle valve', 310.00, 20, 'Units', 'ACTIVE'),
(13, 'SKU-PKG-401', 'Heavy-Duty Double-Wall Corrugated Carton (50pk)', 4, 'Bursting test 275lb shipping cartons for export freight', 75.00, 100, 'Bundles', 'ACTIVE'),
(14, 'SKU-PKG-402', 'Heat-Treated Euro Pallet 1200x800', 4, 'ISPM-15 certified birch wood warehouse storage pallet', 28.00, 150, 'Units', 'ACTIVE'),
(15, 'SKU-PKG-403', 'Anti-Static ESD Bubble Film 100m Roll', 4, 'Pink conductive protective wrap for sensitive semiconductor PCBs', 62.00, 60, 'Rolls', 'ACTIVE'),
(16, 'SKU-TOOL-501', 'Digital Cordless Brushless Impact Wrench 18V', 5, 'High torque 1200Nm industrial assembly impact gun with Bluetooth', 265.00, 25, 'Kits', 'ACTIVE'),
(17, 'SKU-TOOL-502', 'Automated Pneumatic Blind Riveter', 5, 'Traction power 18000N aerospace structural blind rivet puller', 415.00, 15, 'Units', 'ACTIVE'),
(18, 'SKU-TOOL-503', 'Titanium Nitride Coated End Mill Set (12pc)', 5, 'Solid carbide CNC milling bits for hardened tool steel machining', 185.00, 40, 'Sets', 'ACTIVE'),
(19, 'SKU-TOOL-504', 'Grade 8 High-Tensile Flange Bolt M12 (250pk)', 5, 'Zinc phosphate coated automotive structural fasteners', 92.00, 75, 'Boxes', 'ACTIVE'),
(20, 'SKU-SAFE-601', 'Full-Face Powered Air Respirator (PAPR)', 6, 'HEPA particulate filtration breathing system with visor', 740.00, 15, 'Units', 'ACTIVE'),
(21, 'SKU-SAFE-602', 'Kevlar Heat & Cut Resistant Gauntlet Gloves (12pr)', 6, 'Level A9 maximum cut resistance for metal stamping plant', 115.00, 45, 'Packs', 'ACTIVE'),
(22, 'SKU-SAFE-603', 'Composite Toe Electrical Hazard Boots (Size 10)', 6, 'Waterproof full-grain leather boots with puncture plate', 145.00, 30, 'Pairs', 'ACTIVE');

-- 7. Insert Inventory across warehouses
INSERT INTO inventory (product_id, warehouse_id, quantity_available, quantity_reserved) VALUES
-- Warehouse 1 (Chicago)
(1, 1, 180, 20),
(2, 1, 140, 10),
(3, 1, 85, 5),
(4, 1, 8, 2), -- LOW STOCK (Reorder level is 20)
(5, 1, 850, 50),
(6, 1, 42, 4),
(7, 1, 3, 1), -- LOW STOCK (Reorder level is 10)
(8, 1, 240, 20),
(9, 1, 25, 5),
(10, 1, 4, 1), -- LOW STOCK (Reorder level is 12)
(11, 1, 60, 5),
(12, 1, 35, 2),
(13, 1, 320, 30),
(14, 1, 450, 40),
(15, 1, 110, 10),
(16, 1, 48, 6),
(17, 1, 22, 3),
(18, 1, 75, 5),
(19, 1, 190, 15),
(20, 1, 18, 2),
(21, 1, 85, 10),
(22, 1, 52, 4),

-- Warehouse 2 (Oakland)
(1, 2, 95, 15),
(2, 2, 80, 8),
(5, 2, 420, 30),
(6, 2, 18, 2),
(8, 2, 130, 10),
(13, 2, 210, 20),
(14, 2, 300, 25),
(16, 2, 30, 4),
(19, 2, 110, 10),
(21, 2, 60, 5),

-- Warehouse 3 (Newark)
(3, 3, 45, 5),
(5, 3, 310, 20),
(9, 3, 18, 2),
(11, 3, 35, 3),
(13, 3, 180, 15),
(17, 3, 14, 2),
(20, 3, 9, 1), -- LOW STOCK (Reorder level is 15)
(22, 3, 28, 3),

-- Warehouse 4 (Rotterdam)
(1, 4, 210, 20),
(2, 4, 160, 15),
(5, 4, 940, 80),
(7, 4, 12, 2),
(9, 4, 30, 4),
(10, 4, 16, 2),
(14, 4, 520, 45),
(16, 4, 55, 5);

-- 8. Insert Stock Transactions (Audit Log)
INSERT INTO stock_transactions (product_id, warehouse_id, transaction_type, quantity, reference_number, notes, performed_by_user_id) VALUES
(1, 1, 'INBOUND_PO', 100, 'PO-2026-001', 'Initial bulk receiving from Apex Metallurgical', 3),
(2, 1, 'INBOUND_PO', 80, 'PO-2026-001', 'Initial shipment of aircraft aluminum bars', 3),
(5, 1, 'INBOUND_PO', 500, 'PO-2026-002', 'Quarterly batch microcontroller chips', 3),
(6, 1, 'INBOUND_PO', 30, 'PO-2026-002', 'Precision optical LIDAR units', 3),
(1, 1, 'OUTBOUND_SO', -20, 'SO-2026-101', 'Dispatched for Midwestern Heavy Machining Order', 4),
(5, 1, 'OUTBOUND_SO', -50, 'SO-2026-102', 'Automated robotics factory assembly kit', 4),
(5, 1, 'TRANSFER_OUT', -100, 'TR-2026-501', 'Transfer to West Coast Distribution Hub', 2),
(5, 2, 'TRANSFER_IN', 100, 'TR-2026-501', 'Received at West Coast Distribution Hub', 2),
(4, 1, 'ADJUSTMENT_OUT', -2, 'ADJ-2026-01', 'Material calibration testing damage write-off', 2);

-- 9. Insert Purchase Orders
INSERT INTO purchase_orders (po_id, po_number, supplier_id, warehouse_id, order_date, expected_delivery_date, actual_delivery_date, total_amount, status, created_by_user_id, approved_by_user_id, notes) VALUES
(1, 'PO-2026-001', 1, 1, '2026-09-01', '2026-09-10', '2026-09-09', 18150.00, 'RECEIVED', 3, 1, 'Received in full. High quality grade verified by inspection team.'),
(2, 'PO-2026-002', 2, 1, '2026-09-05', '2026-09-18', '2026-09-17', 19850.00, 'RECEIVED', 3, 1, 'Microcontrollers and LiDAR sensors received and cataloged.'),
(3, 'PO-2026-003', 4, 1, '2026-09-18', '2026-09-30', NULL, 15480.00, 'APPROVED', 3, 1, 'Hydraulic replacement pumps and braided hoses for automotive assembly line.'),
(4, 'PO-2026-004', 3, 2, '2026-09-22', '2026-10-05', NULL, 24600.00, 'PENDING', 3, NULL, 'Awaiting CFO sign-off on high volume titanium wire spools.'),
(5, 'PO-2026-005', 7, 4, '2026-09-24', '2026-10-12', NULL, 12500.00, 'DRAFT', 3, NULL, 'Draft order for Rotterdam hub sensor replenishment.');

-- 10. Insert Purchase Order Items
INSERT INTO purchase_order_items (po_id, product_id, quantity_ordered, quantity_received, unit_cost, subtotal) VALUES
(1, 1, 100, 100, 85.50, 8550.00),
(1, 2, 80, 80, 120.00, 9600.00),
(2, 5, 500, 500, 14.50, 7250.00),
(2, 6, 30, 30, 420.00, 12600.00),
(3, 9, 20, 0, 580.00, 11600.00),
(3, 11, 20, 0, 194.00, 3880.00),
(4, 4, 60, 0, 310.00, 18600.00),
(4, 7, 5, 0, 1200.00, 6000.00),
(5, 7, 10, 0, 1250.00, 12500.00);

-- 11. Insert Sales Orders
INSERT INTO sales_orders (so_id, so_number, customer_name, customer_email, customer_phone, shipping_address, warehouse_id, order_date, total_amount, status, created_by_user_id, notes) VALUES
(1, 'SO-2026-101', 'Midwest Industrial Dynamics', 'procure@midwestind.com', '+1-312-555-0199', '1400 Prairie Way, Naperville, IL', 1, '2026-09-12', 5910.00, 'DELIVERED', 4, 'Critical tooling and structural materials. Priority delivery completed.'),
(2, 'SO-2026-102', 'NextGen Robotics Labs Inc', 'components@nextgenrobotics.io', '+1-415-555-0182', '500 Innovation Way, Palo Alto, CA', 1, '2026-09-16', 7420.00, 'SHIPPED', 4, 'Sensor kits and microcontrollers for autonomous mobile robots.'),
(3, 'SO-2026-103', 'Nordic Maritime Fabrication', 'supplies@nordicmaritime.no', '+47-55-90-1122', 'Dock 4B, Bergen Harbor, Norway', 4, '2026-09-21', 12380.00, 'PROCESSING', 4, 'Heavy hydraulics and aluminum structural materials.'),
(4, 'SO-2026-104', 'Apex Automation Solutions', 'kurt@apexautomation.com', '+1-201-555-0144', '88 Skyway Dr, Jersey City, NJ', 3, '2026-09-24', 4180.00, 'CONFIRMED', 4, 'Stock reserved. Ready for crating and logistics dispatch.'),
(5, 'SO-2026-105', 'Quantum Aerospace Tech', 'buyer@quantumaero.com', '+1-512-555-0177', '900 Propulsion Rd, Austin, TX', 1, '2026-09-25', 9600.00, 'PENDING', 4, 'Customer purchase order received. Inventory verification in progress.');

-- 12. Insert Sales Order Items
INSERT INTO sales_order_items (so_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 20, 85.50, 1710.00),
(1, 16, 10, 265.00, 2650.00),
(1, 18, 5, 185.00, 925.00),
(1, 21, 5, 125.00, 625.00),
(2, 5, 100, 14.50, 1450.00),
(2, 6, 10, 420.00, 4200.00),
(2, 8, 20, 50.00, 1000.00),
(2, 15, 10, 77.00, 770.00),
(3, 9, 10, 580.00, 5800.00),
(3, 10, 6, 890.00, 5340.00),
(3, 11, 5, 248.00, 1240.00),
(4, 5, 50, 14.50, 725.00),
(4, 16, 8, 265.00, 2120.00),
(4, 17, 3, 445.00, 1335.00),
(5, 2, 40, 120.00, 4800.00),
(5, 4, 10, 360.00, 3600.00),
(5, 14, 40, 30.00, 1200.00);

-- 13. Insert Shipments
INSERT INTO shipments (shipment_id, so_id, tracking_number, carrier, shipment_date, expected_delivery, actual_delivery, shipping_notes, status) VALUES
(1, 1, 'TRK-DHL-94812039', 'DHL Express Freight', '2026-09-13', '2026-09-15', '2026-09-15', 'Delivered to Receiving Bay 2. Signature on file.', 'DELIVERED'),
(2, 2, 'TRK-FDX-77381920', 'FedEx Logistics Direct', '2026-09-17', '2026-09-20', NULL, 'In transit at Sacramento consolidation terminal.', 'IN_TRANSIT'),
(3, 3, 'TRK-MSK-44910283', 'Maersk Intermodal', '2026-09-23', '2026-09-28', NULL, 'Departed Rotterdam container yard aboard regional carrier.', 'READY');

-- 14. Insert Stock Transfers
INSERT INTO stock_transfers (transfer_id, transfer_number, source_warehouse_id, destination_warehouse_id, transfer_date, status, initiated_by_user_id, notes) VALUES
(1, 'TR-2026-501', 1, 2, '2026-09-10', 'COMPLETED', 2, 'Rebalancing West Coast microcontrollers before holiday cycle.'),
(2, 'TR-2026-502', 1, 3, '2026-09-20', 'IN_TRANSIT', 2, 'Replenishing East Coast safety equipment and carton inventory.'),
(3, 'TR-2026-503', 4, 1, '2026-09-25', 'PENDING', 2, 'Inter-continental transfer of titanium alloy wire spools.');

-- 15. Insert Stock Transfer Items
INSERT INTO stock_transfer_items (transfer_id, product_id, quantity) VALUES
(1, 5, 100),
(2, 21, 25),
(2, 13, 80),
(3, 4, 15);
