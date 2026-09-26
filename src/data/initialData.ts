export interface Role {
  id: number;
  name: string;
  displayName: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roleId: number;
  role: string;
  status: 'ACTIVE' | 'INACTIVE';
  password?: string;
}

export interface Category {
  id: number;
  name: string;
  code: string;
  description: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface Product {
  id: number;
  sku: string;
  name: string;
  categoryId: number;
  categoryName: string;
  description: string;
  unitPrice: number;
  reorderLevel: number;
  unit: string;
  status: 'ACTIVE' | 'DISCONTINUED' | 'OUT_OF_STOCK';
}

export interface Warehouse {
  id: number;
  name: string;
  code: string;
  location: string;
  manager: string;
  capacity: number;
  currentUtilization: number;
  status: 'ACTIVE' | 'MAINTENANCE' | 'INACTIVE';
}

export interface Supplier {
  id: number;
  name: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  country: string;
  status: 'ACTIVE' | 'INACTIVE' | 'BLACKLISTED';
}

export interface InventoryRecord {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  categoryName: string;
  warehouseId: number;
  warehouseName: string;
  warehouseCode: string;
  quantityAvailable: number;
  quantityReserved: number;
  reorderLevel: number;
  unitPrice: number;
  lastUpdated: string;
}

export interface StockTransaction {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  type: 'INBOUND_PO' | 'OUTBOUND_SO' | 'TRANSFER_OUT' | 'TRANSFER_IN' | 'ADJUSTMENT_IN' | 'ADJUSTMENT_OUT';
  quantity: number;
  referenceNumber: string;
  notes: string;
  performedBy: string;
  timestamp: string;
}

export interface PurchaseOrderItem {
  productId: number;
  sku: string;
  productName: string;
  quantityOrdered: number;
  quantityReceived: number;
  unitCost: number;
  subtotal: number;
}

export interface PurchaseOrder {
  id: number;
  poNumber: string;
  supplierId: number;
  supplierName: string;
  warehouseId: number;
  warehouseName: string;
  orderDate: string;
  expectedDate: string;
  actualDate?: string;
  totalAmount: number;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'RECEIVED' | 'CANCELLED';
  createdBy: string;
  approvedBy?: string;
  notes: string;
  items: PurchaseOrderItem[];
}

export interface SalesOrderItem {
  productId: number;
  sku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface SalesOrder {
  id: number;
  soNumber: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  shippingAddress: string;
  warehouseId: number;
  warehouseName: string;
  orderDate: string;
  totalAmount: number;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  createdBy: string;
  notes: string;
  items: SalesOrderItem[];
}

export interface Shipment {
  id: number;
  soId: number;
  soNumber: string;
  customerName: string;
  trackingNumber: string;
  carrier: string;
  shipDate: string;
  expectedDelivery: string;
  actualDelivery?: string;
  status: 'READY' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'DELAYED' | 'CANCELLED';
  notes: string;
}

export interface StockTransfer {
  id: number;
  transferNumber: string;
  sourceWarehouseId: number;
  sourceWarehouseName: string;
  destinationWarehouseId: number;
  destinationWarehouseName: string;
  transferDate: string;
  status: 'PENDING' | 'IN_TRANSIT' | 'COMPLETED' | 'CANCELLED';
  initiatedBy: string;
  notes: string;
  items: {
    productId: number;
    sku: string;
    productName: string;
    quantity: number;
  }[];
}

export const initialCategories: Category[] = [
  { id: 1, name: 'Industrial Raw Materials', code: 'CAT-RAW', description: 'Steel alloys, structural polymers, and raw composites', status: 'ACTIVE' },
  { id: 2, name: 'Precision Electronics & Chips', code: 'CAT-ELEC', description: 'Semiconductors, integrated circuits, microcontrollers, and optical sensors', status: 'ACTIVE' },
  { id: 3, name: 'Hydraulics & Fluid Power', code: 'CAT-HYD', description: 'High-pressure pumps, hydraulic cylinders, and valves', status: 'ACTIVE' },
  { id: 4, name: 'Packaging & Crating', code: 'CAT-PKG', description: 'Reinforced corrugated export cartons and heat-treated Euro pallets', status: 'ACTIVE' },
  { id: 5, name: 'Power Tools & Assembly Hardware', code: 'CAT-TOOL', description: 'Industrial brushless impact tools and structural fasteners', status: 'ACTIVE' },
  { id: 6, name: 'Safety & PPE Equipment', code: 'CAT-SAFE', description: 'Powered respirators, cut-resistant gauntlets, and hazard boots', status: 'ACTIVE' },
];

export const initialWarehouses: Warehouse[] = [
  { id: 1, name: 'Central Fulfillment Hub - Chicago', code: 'WH-CHI-01', location: '4500 Industrial Dr, Chicago, IL', manager: 'Elena Rostova', capacity: 50000, currentUtilization: 31200, status: 'ACTIVE' },
  { id: 2, name: 'West Coast Port Distribution - Oakland', code: 'WH-OAK-02', location: '120 Maritime St, Oakland, CA', manager: 'Tariq Al-Mansoor', capacity: 40000, currentUtilization: 24500, status: 'ACTIVE' },
  { id: 3, name: 'East Coast Gateway - Newark', code: 'WH-NWK-03', location: '77 Terminal Rd, Newark, NJ', manager: 'Colleen Bradley', capacity: 35000, currentUtilization: 18900, status: 'ACTIVE' },
  { id: 4, name: 'European Hub - Rotterdam', code: 'WH-RTM-04', location: 'Maasvlakte Haven 45, Rotterdam, Netherlands', manager: 'Wouter Van Dijk', capacity: 60000, currentUtilization: 42000, status: 'ACTIVE' },
];

export const initialSuppliers: Supplier[] = [
  { id: 1, name: 'Apex Metallurgical Corp', contactPerson: 'Marcus Thorne', email: 'm.thorne@apexmetallurgical.com', phone: '+1-555-019-2831', address: '800 Iron Forge Pkwy', city: 'Pittsburgh', country: 'USA', status: 'ACTIVE' },
  { id: 2, name: 'SiliconCore Dynamics GmbH', contactPerson: 'Dr. Helga Weiss', email: 'orders@siliconcore-de.com', phone: '+49-89-9238-110', address: 'Technologiepark 14', city: 'Munich', country: 'Germany', status: 'ACTIVE' },
  { id: 3, name: 'Pacific Rim Precision Ltd', contactPerson: 'Kenji Tanaka', email: 'tanaka@pacrimprecision.jp', phone: '+81-3-5555-0142', address: '2-8-1 Nihonbashi', city: 'Tokyo', country: 'Japan', status: 'ACTIVE' },
  { id: 4, name: 'Nordic Fluid & Pressure AS', contactPerson: 'Lars Lindqvist', email: 'support@nordicfluid.no', phone: '+47-22-33-4455', address: 'Fjordveien 88', city: 'Oslo', country: 'Norway', status: 'ACTIVE' },
  { id: 5, name: 'OmniPack Global Solutions', contactPerson: 'Maria Gonzales', email: 'mgonzales@omnipackglobal.com', phone: '+1-555-012-7744', address: '420 Logistics Blvd', city: 'Dallas', country: 'USA', status: 'ACTIVE' },
  { id: 6, name: 'Vulcan Fasteners & Tooling', contactPerson: 'Robert MacIntyre', email: 'rmacintyre@vulcantools.co.uk', phone: '+44-20-7946-0921', address: '74 Industrial Way', city: 'Birmingham', country: 'UK', status: 'ACTIVE' },
  { id: 7, name: 'Seoul Sensor Technologies', contactPerson: 'Ji-Hoon Park', email: 'jhpark@seoulsensors.kr', phone: '+82-2-3456-7890', address: 'Gasan Digital 1-ro', city: 'Seoul', country: 'South Korea', status: 'ACTIVE' },
  { id: 8, name: 'SafeGuard Industrial Gear', contactPerson: 'Brenda O\'Connor', email: 'brenda@safeguardindustrial.com', phone: '+1-555-018-9321', address: '12 Safety Harbor Rd', city: 'Cleveland', country: 'USA', status: 'ACTIVE' },
];

export const initialProducts: Product[] = [
  { id: 1, sku: 'SKU-RAW-101', name: 'Cold-Rolled Carbon Steel Sheet 2mm', categoryId: 1, categoryName: 'Industrial Raw Materials', description: 'High tensile steel sheet for automotive framing', unitPrice: 85.50, reorderLevel: 50, unit: 'Sheets', status: 'ACTIVE' },
  { id: 2, sku: 'SKU-RAW-102', name: 'Aircraft Grade Aluminum 6061 Bar', categoryId: 1, categoryName: 'Industrial Raw Materials', description: 'Corrosion-resistant aluminum rod for CNC aerospace parts', unitPrice: 120.00, reorderLevel: 40, unit: 'Bars', status: 'ACTIVE' },
  { id: 3, sku: 'SKU-RAW-103', name: 'Industrial Polypropylene Pellets (50kg)', categoryId: 1, categoryName: 'Industrial Raw Materials', description: 'Thermoplastic polymer resin for injection molding', unitPrice: 95.00, reorderLevel: 30, unit: 'Bags', status: 'ACTIVE' },
  { id: 4, sku: 'SKU-RAW-104', name: 'Titanium Grade 5 Round Wire 3mm', categoryId: 1, categoryName: 'Industrial Raw Materials', description: 'Biomedical and marine-grade high durability titanium alloy', unitPrice: 340.00, reorderLevel: 20, unit: 'Spools', status: 'ACTIVE' },
  { id: 5, sku: 'SKU-ELEC-201', name: 'ARM Cortex-M4 Microcontroller 120MHz', categoryId: 2, categoryName: 'Precision Electronics & Chips', description: 'Low power embedded SoC with hardware floating-point DSP', unitPrice: 14.50, reorderLevel: 200, unit: 'Units', status: 'ACTIVE' },
  { id: 6, sku: 'SKU-ELEC-202', name: 'Industrial Optical LIDAR Sensor 40m', categoryId: 2, categoryName: 'Precision Electronics & Chips', description: 'Time-of-flight laser distance sensor for automated AGVs', unitPrice: 420.00, reorderLevel: 15, unit: 'Units', status: 'ACTIVE' },
  { id: 7, sku: 'SKU-ELEC-203', name: 'Thermal Imaging Core 640x512', categoryId: 2, categoryName: 'Precision Electronics & Chips', description: 'Uncooled long-wave infrared sensor engine', unitPrice: 1250.00, reorderLevel: 10, unit: 'Units', status: 'ACTIVE' },
  { id: 8, sku: 'SKU-ELEC-204', name: 'High-Speed Optocoupler 10MBd (100pk)', categoryId: 2, categoryName: 'Precision Electronics & Chips', description: 'Galvanic signal isolator for factory PLC bus controllers', unitPrice: 48.00, reorderLevel: 80, unit: 'Packs', status: 'ACTIVE' },
  { id: 9, sku: 'SKU-HYD-301', name: 'Heavy Duty Double-Acting Hydraulic Cylinder', categoryId: 3, categoryName: 'Hydraulics & Fluid Power', description: '5000 PSI rated heavy earthmover hydraulic ram actuator', unitPrice: 580.00, reorderLevel: 15, unit: 'Units', status: 'ACTIVE' },
  { id: 10, sku: 'SKU-HYD-302', name: 'Variable Displacement Piston Pump', categoryId: 3, categoryName: 'Hydraulics & Fluid Power', description: 'High pressure 350 bar axial piston pump with load sensing', unitPrice: 890.00, reorderLevel: 12, unit: 'Units', status: 'ACTIVE' },
  { id: 11, sku: 'SKU-HYD-303', name: 'Steel Braided Hydraulic Hose 3/8" (50m)', categoryId: 3, categoryName: 'Hydraulics & Fluid Power', description: '4-spiral wire reinforced ultra-flex high abrasion hose', unitPrice: 220.00, reorderLevel: 25, unit: 'Rolls', status: 'ACTIVE' },
  { id: 12, sku: 'SKU-HYD-304', name: 'Proportional Flow Control Valve 24VDC', categoryId: 3, categoryName: 'Hydraulics & Fluid Power', description: 'Closed-loop electronic hydraulic proportional throttle valve', unitPrice: 310.00, reorderLevel: 20, unit: 'Units', status: 'ACTIVE' },
  { id: 13, sku: 'SKU-PKG-401', name: 'Heavy-Duty Double-Wall Corrugated Carton (50pk)', categoryId: 4, categoryName: 'Packaging & Crating', description: 'Bursting test 275lb shipping cartons for export freight', unitPrice: 75.00, reorderLevel: 100, unit: 'Bundles', status: 'ACTIVE' },
  { id: 14, sku: 'SKU-PKG-402', name: 'Heat-Treated Euro Pallet 1200x800', categoryId: 4, categoryName: 'Packaging & Crating', description: 'ISPM-15 certified birch wood warehouse storage pallet', unitPrice: 28.00, reorderLevel: 150, unit: 'Units', status: 'ACTIVE' },
  { id: 15, sku: 'SKU-PKG-403', name: 'Anti-Static ESD Bubble Film 100m Roll', categoryId: 4, categoryName: 'Packaging & Crating', description: 'Conductive protective wrap for semiconductor PCBs', unitPrice: 62.00, reorderLevel: 60, unit: 'Rolls', status: 'ACTIVE' },
  { id: 16, sku: 'SKU-TOOL-501', name: 'Digital Cordless Brushless Impact Wrench 18V', categoryId: 5, categoryName: 'Power Tools & Assembly Hardware', description: 'High torque 1200Nm industrial assembly impact gun with Bluetooth', unitPrice: 265.00, reorderLevel: 25, unit: 'Kits', status: 'ACTIVE' },
  { id: 17, sku: 'SKU-TOOL-502', name: 'Automated Pneumatic Blind Riveter', categoryId: 5, categoryName: 'Power Tools & Assembly Hardware', description: 'Traction power 18000N aerospace structural blind rivet puller', unitPrice: 415.00, reorderLevel: 15, unit: 'Units', status: 'ACTIVE' },
  { id: 18, sku: 'SKU-TOOL-503', name: 'Titanium Nitride Coated End Mill Set (12pc)', categoryId: 5, categoryName: 'Power Tools & Assembly Hardware', description: 'Solid carbide CNC milling bits for hardened tool steel machining', unitPrice: 185.00, reorderLevel: 40, unit: 'Sets', status: 'ACTIVE' },
  { id: 19, sku: 'SKU-TOOL-504', name: 'Grade 8 High-Tensile Flange Bolt M12 (250pk)', categoryId: 5, categoryName: 'Power Tools & Assembly Hardware', description: 'Zinc phosphate coated automotive structural fasteners', unitPrice: 92.00, reorderLevel: 75, unit: 'Boxes', status: 'ACTIVE' },
  { id: 20, sku: 'SKU-SAFE-601', name: 'Full-Face Powered Air Respirator (PAPR)', categoryId: 6, categoryName: 'Safety & PPE Equipment', description: 'HEPA particulate filtration breathing system with visor', unitPrice: 740.00, reorderLevel: 15, unit: 'Units', status: 'ACTIVE' },
  { id: 21, sku: 'SKU-SAFE-602', name: 'Kevlar Cut Resistant Gauntlet Gloves (12pr)', categoryId: 6, categoryName: 'Safety & PPE Equipment', description: 'Level A9 maximum cut resistance for metal stamping', unitPrice: 115.00, reorderLevel: 45, unit: 'Packs', status: 'ACTIVE' },
  { id: 22, sku: 'SKU-SAFE-603', name: 'Composite Toe Electrical Hazard Boots (10)', categoryId: 6, categoryName: 'Safety & PPE Equipment', description: 'Waterproof full-grain leather boots with puncture plate', unitPrice: 145.00, reorderLevel: 30, unit: 'Pairs', status: 'ACTIVE' },
];

export const initialInventory: InventoryRecord[] = [
  { id: 1, productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', categoryName: 'Industrial Raw Materials', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 180, quantityReserved: 20, reorderLevel: 50, unitPrice: 85.50, lastUpdated: '2026-09-26 10:15:00' },
  { id: 2, productId: 2, sku: 'SKU-RAW-102', productName: 'Aircraft Grade Aluminum 6061 Bar', categoryName: 'Industrial Raw Materials', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 140, quantityReserved: 10, reorderLevel: 40, unitPrice: 120.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 3, productId: 3, sku: 'SKU-RAW-103', productName: 'Industrial Polypropylene Pellets (50kg)', categoryName: 'Industrial Raw Materials', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 85, quantityReserved: 5, reorderLevel: 30, unitPrice: 95.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 4, productId: 4, sku: 'SKU-RAW-104', productName: 'Titanium Grade 5 Round Wire 3mm', categoryName: 'Industrial Raw Materials', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 8, quantityReserved: 2, reorderLevel: 20, unitPrice: 340.00, lastUpdated: '2026-09-26 10:15:00' }, // LOW STOCK
  { id: 5, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', categoryName: 'Precision Electronics & Chips', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 850, quantityReserved: 50, reorderLevel: 200, unitPrice: 14.50, lastUpdated: '2026-09-26 10:15:00' },
  { id: 6, productId: 6, sku: 'SKU-ELEC-202', productName: 'Industrial Optical LIDAR Sensor 40m', categoryName: 'Precision Electronics & Chips', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 42, quantityReserved: 4, reorderLevel: 15, unitPrice: 420.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 7, productId: 7, sku: 'SKU-ELEC-203', productName: 'Thermal Imaging Core 640x512', categoryName: 'Precision Electronics & Chips', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 3, quantityReserved: 1, reorderLevel: 10, unitPrice: 1250.00, lastUpdated: '2026-09-26 10:15:00' }, // LOW STOCK
  { id: 8, productId: 8, sku: 'SKU-ELEC-204', productName: 'High-Speed Optocoupler 10MBd (100pk)', categoryName: 'Precision Electronics & Chips', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 240, quantityReserved: 20, reorderLevel: 80, unitPrice: 48.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 9, productId: 9, sku: 'SKU-HYD-301', productName: 'Heavy Duty Double-Acting Hydraulic Cylinder', categoryName: 'Hydraulics & Fluid Power', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 25, quantityReserved: 5, reorderLevel: 15, unitPrice: 580.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 10, productId: 10, sku: 'SKU-HYD-302', productName: 'Variable Displacement Piston Pump', categoryName: 'Hydraulics & Fluid Power', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 4, quantityReserved: 1, reorderLevel: 12, unitPrice: 890.00, lastUpdated: '2026-09-26 10:15:00' }, // LOW STOCK
  { id: 11, productId: 11, sku: 'SKU-HYD-303', productName: 'Steel Braided Hydraulic Hose 3/8" (50m)', categoryName: 'Hydraulics & Fluid Power', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 60, quantityReserved: 5, reorderLevel: 25, unitPrice: 220.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 12, productId: 13, sku: 'SKU-PKG-401', productName: 'Heavy-Duty Double-Wall Corrugated Carton (50pk)', categoryName: 'Packaging & Crating', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 320, quantityReserved: 30, reorderLevel: 100, unitPrice: 75.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 13, productId: 14, sku: 'SKU-PKG-402', productName: 'Heat-Treated Euro Pallet 1200x800', categoryName: 'Packaging & Crating', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 450, quantityReserved: 40, reorderLevel: 150, unitPrice: 28.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 14, productId: 16, sku: 'SKU-TOOL-501', productName: 'Digital Cordless Brushless Impact Wrench 18V', categoryName: 'Power Tools & Assembly Hardware', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 48, quantityReserved: 6, reorderLevel: 25, unitPrice: 265.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 15, productId: 20, sku: 'SKU-SAFE-601', productName: 'Full-Face Powered Air Respirator (PAPR)', categoryName: 'Safety & PPE Equipment', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', warehouseCode: 'WH-CHI-01', quantityAvailable: 18, quantityReserved: 2, reorderLevel: 15, unitPrice: 740.00, lastUpdated: '2026-09-26 10:15:00' },
  { id: 16, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', categoryName: 'Precision Electronics & Chips', warehouseId: 2, warehouseName: 'West Coast Port Distribution - Oakland', warehouseCode: 'WH-OAK-02', quantityAvailable: 420, quantityReserved: 30, reorderLevel: 200, unitPrice: 14.50, lastUpdated: '2026-09-26 10:15:00' },
  { id: 17, productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', categoryName: 'Industrial Raw Materials', warehouseId: 2, warehouseName: 'West Coast Port Distribution - Oakland', warehouseCode: 'WH-OAK-02', quantityAvailable: 95, quantityReserved: 15, reorderLevel: 50, unitPrice: 85.50, lastUpdated: '2026-09-26 10:15:00' },
  { id: 18, productId: 20, sku: 'SKU-SAFE-601', productName: 'Full-Face Powered Air Respirator (PAPR)', categoryName: 'Safety & PPE Equipment', warehouseId: 3, warehouseName: 'East Coast Gateway - Newark', warehouseCode: 'WH-NWK-03', quantityAvailable: 9, quantityReserved: 1, reorderLevel: 15, unitPrice: 740.00, lastUpdated: '2026-09-26 10:15:00' }, // LOW STOCK
  { id: 19, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', categoryName: 'Precision Electronics & Chips', warehouseId: 4, warehouseName: 'European Hub - Rotterdam', warehouseCode: 'WH-RTM-04', quantityAvailable: 940, quantityReserved: 80, reorderLevel: 200, unitPrice: 14.50, lastUpdated: '2026-09-26 10:15:00' },
];

export const initialTransactions: StockTransaction[] = [
  { id: 1, productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', type: 'INBOUND_PO', quantity: 100, referenceNumber: 'PO-2026-001', notes: 'Initial bulk receiving from Apex Metallurgical', performedBy: 'David Sterling', timestamp: '2026-09-09 14:22:10' },
  { id: 2, productId: 2, sku: 'SKU-RAW-102', productName: 'Aircraft Grade Aluminum 6061 Bar', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', type: 'INBOUND_PO', quantity: 80, referenceNumber: 'PO-2026-001', notes: 'Initial shipment of aircraft aluminum bars', performedBy: 'David Sterling', timestamp: '2026-09-09 14:23:45' },
  { id: 3, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', type: 'INBOUND_PO', quantity: 500, referenceNumber: 'PO-2026-002', notes: 'Quarterly batch microcontroller chips', performedBy: 'David Sterling', timestamp: '2026-09-17 11:10:00' },
  { id: 4, productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', type: 'OUTBOUND_SO', quantity: -20, referenceNumber: 'SO-2026-101', notes: 'Dispatched for Midwestern Heavy Machining Order', performedBy: 'Sarah Chen', timestamp: '2026-09-12 16:45:00' },
  { id: 5, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', type: 'TRANSFER_OUT', quantity: -100, referenceNumber: 'TR-2026-501', notes: 'Transfer to West Coast Distribution Hub', performedBy: 'Elena Rostova', timestamp: '2026-09-10 09:30:00' },
  { id: 6, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', warehouseId: 2, warehouseName: 'West Coast Port Distribution - Oakland', type: 'TRANSFER_IN', quantity: 100, referenceNumber: 'TR-2026-501', notes: 'Received at West Coast Distribution Hub', performedBy: 'Elena Rostova', timestamp: '2026-09-10 17:00:00' },
  { id: 7, productId: 4, sku: 'SKU-RAW-104', productName: 'Titanium Grade 5 Round Wire 3mm', warehouseId: 1, warehouseName: 'Central Fulfillment Hub - Chicago', type: 'ADJUSTMENT_OUT', quantity: -2, referenceNumber: 'ADJ-2026-01', notes: 'Material calibration testing damage write-off', performedBy: 'Elena Rostova', timestamp: '2026-09-15 13:20:00' },
];

export const initialPurchaseOrders: PurchaseOrder[] = [
  {
    id: 1,
    poNumber: 'PO-2026-001',
    supplierId: 1,
    supplierName: 'Apex Metallurgical Corp',
    warehouseId: 1,
    warehouseName: 'Central Fulfillment Hub - Chicago',
    orderDate: '2026-09-01',
    expectedDate: '2026-09-10',
    actualDate: '2026-09-09',
    totalAmount: 18150.00,
    status: 'RECEIVED',
    createdBy: 'David Sterling',
    approvedBy: 'Alexander Vance',
    notes: 'Received in full. High quality grade verified by inspection team.',
    items: [
      { productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', quantityOrdered: 100, quantityReceived: 100, unitCost: 85.50, subtotal: 8550.00 },
      { productId: 2, sku: 'SKU-RAW-102', productName: 'Aircraft Grade Aluminum 6061 Bar', quantityOrdered: 80, quantityReceived: 80, unitCost: 120.00, subtotal: 9600.00 }
    ]
  },
  {
    id: 2,
    poNumber: 'PO-2026-002',
    supplierId: 2,
    supplierName: 'SiliconCore Dynamics GmbH',
    warehouseId: 1,
    warehouseName: 'Central Fulfillment Hub - Chicago',
    orderDate: '2026-09-05',
    expectedDate: '2026-09-18',
    actualDate: '2026-09-17',
    totalAmount: 19850.00,
    status: 'RECEIVED',
    createdBy: 'David Sterling',
    approvedBy: 'Alexander Vance',
    notes: 'Microcontrollers and LiDAR sensors received and cataloged.',
    items: [
      { productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', quantityOrdered: 500, quantityReceived: 500, unitCost: 14.50, subtotal: 7250.00 },
      { productId: 6, sku: 'SKU-ELEC-202', productName: 'Industrial Optical LIDAR Sensor 40m', quantityOrdered: 30, quantityReceived: 30, unitCost: 420.00, subtotal: 12600.00 }
    ]
  },
  {
    id: 3,
    poNumber: 'PO-2026-003',
    supplierId: 4,
    supplierName: 'Nordic Fluid & Pressure AS',
    warehouseId: 1,
    warehouseName: 'Central Fulfillment Hub - Chicago',
    orderDate: '2026-09-18',
    expectedDate: '2026-09-30',
    totalAmount: 15480.00,
    status: 'APPROVED',
    createdBy: 'David Sterling',
    approvedBy: 'Alexander Vance',
    notes: 'Hydraulic replacement pumps and braided hoses for automotive assembly line.',
    items: [
      { productId: 9, sku: 'SKU-HYD-301', productName: 'Heavy Duty Double-Acting Hydraulic Cylinder', quantityOrdered: 20, quantityReceived: 0, unitCost: 580.00, subtotal: 11600.00 },
      { productId: 11, sku: 'SKU-HYD-303', productName: 'Steel Braided Hydraulic Hose 3/8" (50m)', quantityOrdered: 20, quantityReceived: 0, unitCost: 194.00, subtotal: 3880.00 }
    ]
  },
  {
    id: 4,
    poNumber: 'PO-2026-004',
    supplierId: 3,
    supplierName: 'Pacific Rim Precision Ltd',
    warehouseId: 2,
    warehouseName: 'West Coast Port Distribution - Oakland',
    orderDate: '2026-09-22',
    expectedDate: '2026-10-05',
    totalAmount: 24600.00,
    status: 'PENDING',
    createdBy: 'David Sterling',
    notes: 'Awaiting executive sign-off on high volume titanium wire spools.',
    items: [
      { productId: 4, sku: 'SKU-RAW-104', productName: 'Titanium Grade 5 Round Wire 3mm', quantityOrdered: 60, quantityReceived: 0, unitCost: 310.00, subtotal: 18600.00 },
      { productId: 7, sku: 'SKU-ELEC-203', productName: 'Thermal Imaging Core 640x512', quantityOrdered: 5, quantityReceived: 0, unitCost: 1200.00, subtotal: 6000.00 }
    ]
  },
  {
    id: 5,
    poNumber: 'PO-2026-005',
    supplierId: 7,
    supplierName: 'Seoul Sensor Technologies',
    warehouseId: 4,
    warehouseName: 'European Hub - Rotterdam',
    orderDate: '2026-09-24',
    expectedDate: '2026-10-12',
    totalAmount: 12500.00,
    status: 'DRAFT',
    createdBy: 'David Sterling',
    notes: 'Draft order for Rotterdam hub sensor replenishment.',
    items: [
      { productId: 7, sku: 'SKU-ELEC-203', productName: 'Thermal Imaging Core 640x512', quantityOrdered: 10, quantityReceived: 0, unitCost: 1250.00, subtotal: 12500.00 }
    ]
  }
];

export const initialSalesOrders: SalesOrder[] = [
  {
    id: 1,
    soNumber: 'SO-2026-101',
    customerName: 'Midwest Industrial Dynamics',
    customerEmail: 'procure@midwestind.com',
    customerPhone: '+1-312-555-0199',
    shippingAddress: '1400 Prairie Way, Naperville, IL',
    warehouseId: 1,
    warehouseName: 'Central Fulfillment Hub - Chicago',
    orderDate: '2026-09-12',
    totalAmount: 5910.00,
    status: 'DELIVERED',
    createdBy: 'Sarah Chen',
    notes: 'Critical tooling and structural materials. Priority delivery completed.',
    items: [
      { productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', quantity: 20, unitPrice: 85.50, subtotal: 1710.00 },
      { productId: 16, sku: 'SKU-TOOL-501', productName: 'Digital Cordless Brushless Impact Wrench 18V', quantity: 10, unitPrice: 265.00, subtotal: 2650.00 },
      { productId: 18, sku: 'SKU-TOOL-503', productName: 'Titanium Nitride Coated End Mill Set (12pc)', quantity: 5, unitPrice: 185.00, subtotal: 925.00 },
      { productId: 21, sku: 'SKU-SAFE-602', productName: 'Kevlar Cut Resistant Gauntlet Gloves (12pr)', quantity: 5, unitPrice: 125.00, subtotal: 625.00 }
    ]
  },
  {
    id: 2,
    soNumber: 'SO-2026-102',
    customerName: 'NextGen Robotics Labs Inc',
    customerEmail: 'components@nextgenrobotics.io',
    customerPhone: '+1-415-555-0182',
    shippingAddress: '500 Innovation Way, Palo Alto, CA',
    warehouseId: 1,
    warehouseName: 'Central Fulfillment Hub - Chicago',
    orderDate: '2026-09-16',
    totalAmount: 7420.00,
    status: 'SHIPPED',
    createdBy: 'Sarah Chen',
    notes: 'Sensor kits and microcontrollers for autonomous mobile robots.',
    items: [
      { productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', quantity: 100, unitPrice: 14.50, subtotal: 1450.00 },
      { productId: 6, sku: 'SKU-ELEC-202', productName: 'Industrial Optical LIDAR Sensor 40m', quantity: 10, unitPrice: 420.00, subtotal: 4200.00 },
      { productId: 8, sku: 'SKU-ELEC-204', productName: 'High-Speed Optocoupler 10MBd (100pk)', quantity: 20, unitPrice: 50.00, subtotal: 1000.00 },
      { productId: 15, sku: 'SKU-PKG-403', productName: 'Anti-Static ESD Bubble Film 100m Roll', quantity: 10, unitPrice: 77.00, subtotal: 770.00 }
    ]
  },
  {
    id: 3,
    soNumber: 'SO-2026-103',
    customerName: 'Nordic Maritime Fabrication',
    customerEmail: 'supplies@nordicmaritime.no',
    customerPhone: '+47-55-90-1122',
    shippingAddress: 'Dock 4B, Bergen Harbor, Norway',
    warehouseId: 4,
    warehouseName: 'European Hub - Rotterdam',
    orderDate: '2026-09-21',
    totalAmount: 12380.00,
    status: 'PROCESSING',
    createdBy: 'Sarah Chen',
    notes: 'Heavy hydraulics and aluminum structural materials.',
    items: [
      { productId: 9, sku: 'SKU-HYD-301', productName: 'Heavy Duty Double-Acting Hydraulic Cylinder', quantity: 10, unitPrice: 580.00, subtotal: 5800.00 },
      { productId: 10, sku: 'SKU-HYD-302', productName: 'Variable Displacement Piston Pump', quantity: 6, unitPrice: 890.00, subtotal: 5340.00 },
      { productId: 11, sku: 'SKU-HYD-303', productName: 'Steel Braided Hydraulic Hose 3/8" (50m)', quantity: 5, unitPrice: 248.00, subtotal: 1240.00 }
    ]
  },
  {
    id: 4,
    soNumber: 'SO-2026-104',
    customerName: 'Apex Automation Solutions',
    customerEmail: 'kurt@apexautomation.com',
    customerPhone: '+1-201-555-0144',
    shippingAddress: '88 Skyway Dr, Jersey City, NJ',
    warehouseId: 3,
    warehouseName: 'East Coast Gateway - Newark',
    orderDate: '2026-09-24',
    totalAmount: 4180.00,
    status: 'CONFIRMED',
    createdBy: 'Sarah Chen',
    notes: 'Stock reserved. Ready for crating and logistics dispatch.',
    items: [
      { productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', quantity: 50, unitPrice: 14.50, subtotal: 725.00 },
      { productId: 16, sku: 'SKU-TOOL-501', productName: 'Digital Cordless Brushless Impact Wrench 18V', quantity: 8, unitPrice: 265.00, subtotal: 2120.00 },
      { productId: 17, sku: 'SKU-TOOL-502', productName: 'Automated Pneumatic Blind Riveter', quantity: 3, unitPrice: 445.00, subtotal: 1335.00 }
    ]
  },
  {
    id: 5,
    soNumber: 'SO-2026-105',
    customerName: 'Quantum Aerospace Tech',
    customerEmail: 'buyer@quantumaero.com',
    customerPhone: '+1-512-555-0177',
    shippingAddress: '900 Propulsion Rd, Austin, TX',
    warehouseId: 1,
    warehouseName: 'Central Fulfillment Hub - Chicago',
    orderDate: '2026-09-25',
    totalAmount: 9600.00,
    status: 'PENDING',
    createdBy: 'Sarah Chen',
    notes: 'Customer purchase order received. Inventory verification in progress.',
    items: [
      { productId: 2, sku: 'SKU-RAW-102', productName: 'Aircraft Grade Aluminum 6061 Bar', quantity: 40, unitPrice: 120.00, subtotal: 4800.00 },
      { productId: 4, sku: 'SKU-RAW-104', productName: 'Titanium Grade 5 Round Wire 3mm', quantity: 10, unitPrice: 360.00, subtotal: 3600.00 },
      { productId: 14, sku: 'SKU-PKG-402', productName: 'Heat-Treated Euro Pallet 1200x800', quantity: 40, unitPrice: 30.00, subtotal: 1200.00 }
    ]
  }
];

export const initialShipments: Shipment[] = [
  {
    id: 1,
    soId: 1,
    soNumber: 'SO-2026-101',
    customerName: 'Midwest Industrial Dynamics',
    trackingNumber: 'TRK-DHL-94812039',
    carrier: 'DHL Express Freight',
    shipDate: '2026-09-13',
    expectedDelivery: '2026-09-15',
    actualDelivery: '2026-09-15',
    status: 'DELIVERED',
    notes: 'Delivered to Receiving Bay 2. Signature on file.'
  },
  {
    id: 2,
    soId: 2,
    soNumber: 'SO-2026-102',
    customerName: 'NextGen Robotics Labs Inc',
    trackingNumber: 'TRK-FDX-77381920',
    carrier: 'FedEx Logistics Direct',
    shipDate: '2026-09-17',
    expectedDelivery: '2026-09-20',
    status: 'IN_TRANSIT',
    notes: 'In transit at Sacramento consolidation terminal.'
  },
  {
    id: 3,
    soId: 3,
    soNumber: 'SO-2026-103',
    customerName: 'Nordic Maritime Fabrication',
    trackingNumber: 'TRK-MSK-44910283',
    carrier: 'Maersk Intermodal',
    shipDate: '2026-09-23',
    expectedDelivery: '2026-09-28',
    status: 'READY',
    notes: 'Departed Rotterdam container yard aboard regional carrier.'
  }
];

export const initialTransfers: StockTransfer[] = [
  {
    id: 1,
    transferNumber: 'TR-2026-501',
    sourceWarehouseId: 1,
    sourceWarehouseName: 'Central Fulfillment Hub - Chicago',
    destinationWarehouseId: 2,
    destinationWarehouseName: 'West Coast Port Distribution - Oakland',
    transferDate: '2026-09-10',
    status: 'COMPLETED',
    initiatedBy: 'Elena Rostova',
    notes: 'Rebalancing West Coast microcontrollers before holiday cycle.',
    items: [
      { productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', quantity: 100 }
    ]
  },
  {
    id: 2,
    transferNumber: 'TR-2026-502',
    sourceWarehouseId: 1,
    sourceWarehouseName: 'Central Fulfillment Hub - Chicago',
    destinationWarehouseId: 3,
    destinationWarehouseName: 'East Coast Gateway - Newark',
    transferDate: '2026-09-20',
    status: 'IN_TRANSIT',
    initiatedBy: 'Elena Rostova',
    notes: 'Replenishing East Coast safety equipment and carton inventory.',
    items: [
      { productId: 21, sku: 'SKU-SAFE-602', productName: 'Kevlar Cut Resistant Gauntlet Gloves (12pr)', quantity: 25 },
      { productId: 13, sku: 'SKU-PKG-401', productName: 'Heavy-Duty Double-Wall Corrugated Carton (50pk)', quantity: 80 }
    ]
  },
  {
    id: 3,
    transferNumber: 'TR-2026-503',
    sourceWarehouseId: 4,
    sourceWarehouseName: 'European Hub - Rotterdam',
    destinationWarehouseId: 1,
    destinationWarehouseName: 'Central Fulfillment Hub - Chicago',
    transferDate: '2026-09-25',
    status: 'PENDING',
    initiatedBy: 'Elena Rostova',
    notes: 'Inter-continental transfer of titanium alloy wire spools.',
    items: [
      { productId: 4, sku: 'SKU-RAW-104', productName: 'Titanium Grade 5 Round Wire 3mm', quantity: 15 }
    ]
  }
];

export const initialRoles: Role[] = [
  { id: 1, name: 'ADMIN', displayName: 'Administrator' },
  { id: 2, name: 'WAREHOUSE_MANAGER', displayName: 'Warehouse Manager' },
  { id: 3, name: 'PROCUREMENT_MANAGER', displayName: 'Procurement Manager' },
  { id: 4, name: 'SALES_MANAGER', displayName: 'Sales Manager' },
];

export const initialUsers: User[] = [
  { id: 1, username: 'admin', email: 'admin@supplychainx.com', fullName: 'Alexander Vance', roleId: 1, role: 'Administrator', status: 'ACTIVE', password: 'admin123' },
  { id: 2, username: 'warehouse', email: 'warehouse@supplychainx.com', fullName: 'Elena Rostova', roleId: 2, role: 'Warehouse Manager', status: 'ACTIVE', password: 'warehouse123' },
  { id: 3, username: 'procure', email: 'procure@supplychainx.com', fullName: 'David Sterling', roleId: 3, role: 'Procurement Manager', status: 'ACTIVE', password: 'procure123' },
  { id: 4, username: 'sales', email: 'sales@supplychainx.com', fullName: 'Sarah Chen', roleId: 4, role: 'Sales Manager', status: 'ACTIVE', password: 'sales123' },
];

// =====================================================================
// AI / ML Intelligence Data Models & Seeds
// =====================================================================

export interface DemandForecast {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  categoryName: string;
  forecastPeriodDays: number;
  historicalDailyAvg: number;
  predictedQuantity: number;
  confidenceLower: number;
  confidenceUpper: number;
  trendDirection: 'UP' | 'DOWN' | 'STABLE';
  trendSlope: number;
  mae: number;
  rmse: number;
  calculatedAt: string;
}

export interface StockRiskPrediction {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  categoryName: string;
  currentStock: number;
  reorderLevel: number;
  dailyVelocity: number;
  predicted30dDemand: number;
  daysUntilStockout: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  riskScore: number;
  recommendedOrderQty: number;
  evaluatedAt: string;
}

export interface SupplierDelayPrediction {
  id: number;
  supplierId: number;
  supplierName: string;
  contactPerson: string;
  email: string;
  averageLeadTimeDays: number;
  lateDeliveryCount: number;
  totalOrdersEvaluated: number;
  delayProbability: number;
  riskCategory: 'LOW' | 'MODERATE' | 'CRITICAL';
  reliabilityScore: number;
  evaluatedAt: string;
}

export interface ProcurementRecommendation {
  productId: number;
  sku: string;
  productName: string;
  categoryName: string;
  supplierId: number;
  supplierName: string;
  currentStock: number;
  safetyStock: number;
  predicted30dDemand: number;
  dailyVelocity: number;
  supplierLeadTimeDays: number;
  supplierDelayRiskPct: number;
  recommendedOrderQty: number;
  unitPrice: number;
  estimatedTotalCost: number;
  urgency: 'CRITICAL' | 'HIGH' | 'NORMAL';
}

export interface ModelTrainingMetadata {
  id: number;
  modelName: string;
  modelType: string;
  algorithm: string;
  trainingSampleSize: number;
  mae?: number;
  rmse?: number;
  rSquared?: number;
  accuracyScore?: number;
  lastTrainedAt: string;
  status: 'TRAINED' | 'OUTDATED' | 'TRAINING_FAILED';
  notes: string;
}

export const initialModelMetadata: ModelTrainingMetadata[] = [
  {
    id: 1,
    modelName: 'DEMAND_FORECAST_OLS',
    modelType: 'REGRESSION',
    algorithm: 'Ordinary Least Squares & Weighted Trend Analysis',
    trainingSampleSize: 142,
    mae: 4.35,
    rmse: 5.82,
    rSquared: 0.892,
    accuracyScore: 0.912,
    lastTrainedAt: '2026-09-26 12:30:00',
    status: 'TRAINED',
    notes: 'Trained on multi-month sales orders with 7-day and 30-day lookaheads.'
  },
  {
    id: 2,
    modelName: 'STOCKOUT_RISK_VELOCITY',
    modelType: 'HEURISTIC_ML',
    algorithm: 'Dynamic Sales Velocity & Lead-Time Runout Model',
    trainingSampleSize: 88,
    mae: 1.20,
    rmse: 1.85,
    rSquared: 0.934,
    accuracyScore: 0.945,
    lastTrainedAt: '2026-09-26 12:30:00',
    status: 'TRAINED',
    notes: 'Evaluates physical available stock against sales velocity and supplier lead-times.'
  },
  {
    id: 3,
    modelName: 'SUPPLIER_DELAY_PROBABILITY',
    modelType: 'CLASSIFICATION',
    algorithm: 'Empirical Lead-Time Variance & Logistic Risk Classifier',
    trainingSampleSize: 64,
    accuracyScore: 0.918,
    lastTrainedAt: '2026-09-26 12:30:00',
    status: 'TRAINED',
    notes: 'Classifies supplier purchase order fulfillment into Low, Moderate, and Critical delay risks.'
  }
];

export const initialDemandForecasts: DemandForecast[] = [
  { id: 1, productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', categoryName: 'Industrial Raw Materials', forecastPeriodDays: 30, historicalDailyAvg: 2.80, predictedQuantity: 94, confidenceLower: 82, confidenceUpper: 106, trendDirection: 'UP', trendSlope: 0.1250, mae: 3.10, rmse: 4.20, calculatedAt: '2026-09-26 12:30:00' },
  { id: 2, productId: 2, sku: 'SKU-RAW-102', productName: 'Aircraft Grade Aluminum 6061 Bar', categoryName: 'Industrial Raw Materials', forecastPeriodDays: 30, historicalDailyAvg: 1.50, predictedQuantity: 48, confidenceLower: 41, confidenceUpper: 55, trendDirection: 'STABLE', trendSlope: 0.0120, mae: 2.40, rmse: 3.10, calculatedAt: '2026-09-26 12:30:00' },
  { id: 3, productId: 3, sku: 'SKU-RAW-103', productName: 'Industrial Polypropylene Pellets (50kg)', categoryName: 'Industrial Raw Materials', forecastPeriodDays: 30, historicalDailyAvg: 1.10, predictedQuantity: 36, confidenceLower: 29, confidenceUpper: 43, trendDirection: 'DOWN', trendSlope: -0.0450, mae: 2.90, rmse: 3.80, calculatedAt: '2026-09-26 12:30:00' },
  { id: 4, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', categoryName: 'Precision Electronics & Chips', forecastPeriodDays: 30, historicalDailyAvg: 8.40, predictedQuantity: 275, confidenceLower: 245, confidenceUpper: 305, trendDirection: 'UP', trendSlope: 0.3400, mae: 5.40, rmse: 6.90, calculatedAt: '2026-09-26 12:30:00' },
  { id: 5, productId: 6, sku: 'SKU-ELEC-202', productName: 'Industrial High-Precision LiDAR Sensor', categoryName: 'Precision Electronics & Chips', forecastPeriodDays: 30, historicalDailyAvg: 0.90, predictedQuantity: 31, confidenceLower: 25, confidenceUpper: 37, trendDirection: 'STABLE', trendSlope: 0.0080, mae: 1.80, rmse: 2.30, calculatedAt: '2026-09-26 12:30:00' },
  { id: 6, productId: 9, sku: 'SKU-HYD-301', productName: 'High-Pressure Hydraulic Axial Piston Pump', categoryName: 'Hydraulics & Fluid Power', forecastPeriodDays: 30, historicalDailyAvg: 1.20, predictedQuantity: 42, confidenceLower: 35, confidenceUpper: 49, trendDirection: 'UP', trendSlope: 0.0820, mae: 2.20, rmse: 3.00, calculatedAt: '2026-09-26 12:30:00' },
  { id: 7, productId: 13, sku: 'SKU-PKG-401', productName: 'Heavy-Duty Double-Wall Corrugated Carton (50pk)', categoryName: 'Packaging & Crating', forecastPeriodDays: 30, historicalDailyAvg: 6.20, predictedQuantity: 195, confidenceLower: 172, confidenceUpper: 218, trendDirection: 'STABLE', trendSlope: 0.0150, mae: 4.80, rmse: 6.20, calculatedAt: '2026-09-26 12:30:00' },
  { id: 8, productId: 16, sku: 'SKU-TOOL-501', productName: 'Brushless Industrial Impact Driver 18V', categoryName: 'Power Tools & Assembly Hardware', forecastPeriodDays: 30, historicalDailyAvg: 2.40, predictedQuantity: 78, confidenceLower: 68, confidenceUpper: 88, trendDirection: 'UP', trendSlope: 0.1100, mae: 3.50, rmse: 4.60, calculatedAt: '2026-09-26 12:30:00' },
  { id: 9, productId: 21, sku: 'SKU-SAFE-602', productName: 'Kevlar Cut Resistant Gauntlet Gloves (12pr)', categoryName: 'Safety & PPE Equipment', forecastPeriodDays: 30, historicalDailyAvg: 1.80, predictedQuantity: 58, confidenceLower: 50, confidenceUpper: 66, trendDirection: 'UP', trendSlope: 0.0750, mae: 2.70, rmse: 3.40, calculatedAt: '2026-09-26 12:30:00' }
];

export const initialStockRisks: StockRiskPrediction[] = [
  { id: 1, productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', categoryName: 'Precision Electronics & Chips', currentStock: 20, reorderLevel: 50, dailyVelocity: 8.40, predicted30dDemand: 275, daysUntilStockout: 2, riskLevel: 'HIGH', riskScore: 92.5, recommendedOrderQty: 300, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 2, productId: 16, sku: 'SKU-TOOL-501', productName: 'Brushless Industrial Impact Driver 18V', categoryName: 'Power Tools & Assembly Hardware', currentStock: 15, reorderLevel: 25, dailyVelocity: 2.40, predicted30dDemand: 78, daysUntilStockout: 6, riskLevel: 'HIGH', riskScore: 84.0, recommendedOrderQty: 80, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 3, productId: 21, sku: 'SKU-SAFE-602', productName: 'Kevlar Cut Resistant Gauntlet Gloves (12pr)', categoryName: 'Safety & PPE Equipment', currentStock: 18, reorderLevel: 25, dailyVelocity: 1.80, predicted30dDemand: 58, daysUntilStockout: 10, riskLevel: 'HIGH', riskScore: 76.5, recommendedOrderQty: 60, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 4, productId: 9, sku: 'SKU-HYD-301', productName: 'High-Pressure Hydraulic Axial Piston Pump', categoryName: 'Hydraulics & Fluid Power', currentStock: 12, reorderLevel: 15, dailyVelocity: 1.20, predicted30dDemand: 42, daysUntilStockout: 10, riskLevel: 'HIGH', riskScore: 78.0, recommendedOrderQty: 45, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 5, productId: 3, sku: 'SKU-RAW-103', productName: 'Industrial Polypropylene Pellets (50kg)', categoryName: 'Industrial Raw Materials', currentStock: 40, reorderLevel: 30, dailyVelocity: 1.10, predicted30dDemand: 36, daysUntilStockout: 36, riskLevel: 'MEDIUM', riskScore: 52.0, recommendedOrderQty: 25, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 6, productId: 6, sku: 'SKU-ELEC-202', productName: 'Industrial High-Precision LiDAR Sensor', categoryName: 'Precision Electronics & Chips', currentStock: 28, reorderLevel: 20, dailyVelocity: 0.90, predicted30dDemand: 31, daysUntilStockout: 31, riskLevel: 'MEDIUM', riskScore: 48.5, recommendedOrderQty: 20, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 7, productId: 1, sku: 'SKU-RAW-101', productName: 'Cold-Rolled Carbon Steel Sheet 2mm', categoryName: 'Industrial Raw Materials', currentStock: 145, reorderLevel: 50, dailyVelocity: 2.80, predicted30dDemand: 94, daysUntilStockout: 51, riskLevel: 'LOW', riskScore: 18.2, recommendedOrderQty: 0, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 8, productId: 2, sku: 'SKU-RAW-102', productName: 'Aircraft Grade Aluminum 6061 Bar', categoryName: 'Industrial Raw Materials', currentStock: 85, reorderLevel: 40, dailyVelocity: 1.50, predicted30dDemand: 48, daysUntilStockout: 56, riskLevel: 'LOW', riskScore: 22.0, recommendedOrderQty: 0, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 9, productId: 13, sku: 'SKU-PKG-401', productName: 'Heavy-Duty Double-Wall Corrugated Carton (50pk)', categoryName: 'Packaging & Crating', currentStock: 310, reorderLevel: 100, dailyVelocity: 6.20, predicted30dDemand: 195, daysUntilStockout: 50, riskLevel: 'LOW', riskScore: 15.0, recommendedOrderQty: 0, evaluatedAt: '2026-09-26 12:30:00' }
];

export const initialSupplierDelays: SupplierDelayPrediction[] = [
  { id: 1, supplierId: 1, supplierName: 'Apex Metallurgical Corp', contactPerson: 'Marcus Thorne', email: 'm.thorne@apexmetallurgical.com', averageLeadTimeDays: 6.2, lateDeliveryCount: 1, totalOrdersEvaluated: 14, delayProbability: 7.14, riskCategory: 'LOW', reliabilityScore: 94.5, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 2, supplierId: 2, supplierName: 'SiliconCore Dynamics GmbH', contactPerson: 'Dr. Helga Weiss', email: 'orders@siliconcore-de.com', averageLeadTimeDays: 12.8, lateDeliveryCount: 5, totalOrdersEvaluated: 16, delayProbability: 31.25, riskCategory: 'CRITICAL', reliabilityScore: 68.0, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 3, supplierId: 3, supplierName: 'Pacific Rim Precision Ltd', contactPerson: 'Kenji Tanaka', email: 'tanaka@pacrimprecision.jp', averageLeadTimeDays: 14.5, lateDeliveryCount: 4, totalOrdersEvaluated: 15, delayProbability: 26.67, riskCategory: 'MODERATE', reliabilityScore: 74.0, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 4, supplierId: 4, supplierName: 'Nordic Fluid & Pressure AS', contactPerson: 'Lars Lindqvist', email: 'support@nordicfluid.no', averageLeadTimeDays: 10.2, lateDeliveryCount: 2, totalOrdersEvaluated: 11, delayProbability: 18.18, riskCategory: 'LOW', reliabilityScore: 86.5, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 5, supplierId: 5, supplierName: 'OmniPack Global Solutions', contactPerson: 'Maria Gonzales', email: 'mgonzales@omnipackglobal.com', averageLeadTimeDays: 4.5, lateDeliveryCount: 0, totalOrdersEvaluated: 12, delayProbability: 0.0, riskCategory: 'LOW', reliabilityScore: 98.0, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 6, supplierId: 6, supplierName: 'Vulcan Fasteners & Tooling', contactPerson: 'Robert MacIntyre', email: 'rmacintyre@vulcantools.co.uk', averageLeadTimeDays: 8.1, lateDeliveryCount: 2, totalOrdersEvaluated: 9, delayProbability: 22.22, riskCategory: 'MODERATE', reliabilityScore: 79.5, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 7, supplierId: 7, supplierName: 'Seoul Sensor Technologies', contactPerson: 'Ji-Hoon Park', email: 'jhpark@seoulsensors.kr', averageLeadTimeDays: 13.4, lateDeliveryCount: 4, totalOrdersEvaluated: 13, delayProbability: 30.77, riskCategory: 'CRITICAL', reliabilityScore: 69.5, evaluatedAt: '2026-09-26 12:30:00' },
  { id: 8, supplierId: 8, supplierName: 'SafeGuard Industrial Gear', contactPerson: "Brenda O'Connor", email: 'brenda@safeguardindustrial.com', averageLeadTimeDays: 5.0, lateDeliveryCount: 1, totalOrdersEvaluated: 10, delayProbability: 10.0, riskCategory: 'LOW', reliabilityScore: 92.0, evaluatedAt: '2026-09-26 12:30:00' }
];

export const initialProcurementRecommendations: ProcurementRecommendation[] = [
  { productId: 5, sku: 'SKU-ELEC-201', productName: 'ARM Cortex-M4 Microcontroller 120MHz', categoryName: 'Precision Electronics & Chips', supplierId: 2, supplierName: 'SiliconCore Dynamics GmbH', currentStock: 20, safetyStock: 50, predicted30dDemand: 275, dailyVelocity: 8.4, supplierLeadTimeDays: 12.8, supplierDelayRiskPct: 31.25, recommendedOrderQty: 300, unitPrice: 14.50, estimatedTotalCost: 4350.00, urgency: 'CRITICAL' },
  { productId: 16, sku: 'SKU-TOOL-501', productName: 'Brushless Industrial Impact Driver 18V', categoryName: 'Power Tools & Assembly Hardware', supplierId: 6, supplierName: 'Vulcan Fasteners & Tooling', currentStock: 15, safetyStock: 25, predicted30dDemand: 78, dailyVelocity: 2.4, supplierLeadTimeDays: 8.1, supplierDelayRiskPct: 22.22, recommendedOrderQty: 80, unitPrice: 265.00, estimatedTotalCost: 21200.00, urgency: 'CRITICAL' },
  { productId: 9, sku: 'SKU-HYD-301', productName: 'High-Pressure Hydraulic Axial Piston Pump', categoryName: 'Hydraulics & Fluid Power', supplierId: 4, supplierName: 'Nordic Fluid & Pressure AS', currentStock: 12, safetyStock: 15, predicted30dDemand: 42, dailyVelocity: 1.2, supplierLeadTimeDays: 10.2, supplierDelayRiskPct: 18.18, recommendedOrderQty: 45, unitPrice: 580.00, estimatedTotalCost: 26100.00, urgency: 'CRITICAL' },
  { productId: 21, sku: 'SKU-SAFE-602', productName: 'Kevlar Cut Resistant Gauntlet Gloves (12pr)', categoryName: 'Safety & PPE Equipment', supplierId: 8, supplierName: 'SafeGuard Industrial Gear', currentStock: 18, safetyStock: 25, predicted30dDemand: 58, dailyVelocity: 1.8, supplierLeadTimeDays: 5.0, supplierDelayRiskPct: 10.0, recommendedOrderQty: 60, unitPrice: 125.00, estimatedTotalCost: 7500.00, urgency: 'HIGH' },
  { productId: 3, sku: 'SKU-RAW-103', productName: 'Industrial Polypropylene Pellets (50kg)', categoryName: 'Industrial Raw Materials', supplierId: 1, supplierName: 'Apex Metallurgical Corp', currentStock: 40, safetyStock: 30, predicted30dDemand: 36, dailyVelocity: 1.1, supplierLeadTimeDays: 6.2, supplierDelayRiskPct: 7.14, recommendedOrderQty: 25, unitPrice: 95.00, estimatedTotalCost: 2375.00, urgency: 'NORMAL' },
  { productId: 6, sku: 'SKU-ELEC-202', productName: 'Industrial High-Precision LiDAR Sensor', categoryName: 'Precision Electronics & Chips', supplierId: 7, supplierName: 'Seoul Sensor Technologies', currentStock: 28, safetyStock: 20, predicted30dDemand: 31, dailyVelocity: 0.9, supplierLeadTimeDays: 13.4, supplierDelayRiskPct: 30.77, recommendedOrderQty: 20, unitPrice: 420.00, estimatedTotalCost: 8400.00, urgency: 'NORMAL' }
];

