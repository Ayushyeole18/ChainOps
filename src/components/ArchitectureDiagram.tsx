import React, { useState, useRef } from 'react';
import {
  Layers,
  Download,
  Database,
  Cpu,
  Monitor,
  ArrowDown,
  ArrowUp,
  CheckCircle2,
  Code2,
  ShieldCheck,
  Zap,
  Info,
  ExternalLink,
  Sparkles,
  Brain,
  FileImage
} from 'lucide-react';

export const ArchitectureDiagram: React.FC = () => {
  const [selectedLayer, setSelectedLayer] = useState<string | null>('services');
  const [isExportingPng, setIsExportingPng] = useState<boolean>(false);
  const svgRef = useRef<SVGSVGElement | null>(null);

  const layers = [
    {
      id: 'presentation',
      title: '1. Presentation Layer (Desktop UI)',
      tech: 'JavaFX 21.0.2 • FXML • Enterprise CSS',
      color: 'border-blue-500/40 bg-blue-500/10 text-blue-400',
      badgeColor: 'bg-blue-600 text-white',
      components: [
        { name: 'FXML View Templates', desc: '15 XML declarative view layouts (AiIntelligence.fxml, Dashboard, Inventory, Orders, Shipments, Transfers, Users, etc.)' },
        { name: 'Enterprise CSS Theme', desc: 'Centralized JavaFX stylesheet (style.css) with slate/navy aesthetic, responsive tables, rounded cards, and badges' },
        { name: 'Navigation Manager', desc: 'Stage controller managing scene transitions, view stack, and primary window properties' },
        { name: 'Alert & Dialog Helper', desc: 'Modal alerts, confirmation prompts, error banners, and input dialogs' }
      ]
    },
    {
      id: 'controllers',
      title: '2. Controller & Event Handling Layer',
      tech: 'JavaFX FXML Controllers • Data Binding',
      color: 'border-cyan-500/40 bg-cyan-500/10 text-cyan-400',
      badgeColor: 'bg-cyan-600 text-white',
      components: [
        { name: 'AiIntelligenceController', desc: 'Coordinates background model training, demand forecasting views, stock-out risk gauges, and supplier delay charts' },
        { name: 'Operations Controllers', desc: 'InventoryController, PurchaseOrderController, SalesOrderController, ShipmentController' },
        { name: 'Master Data Controllers', desc: 'ProductController, CategoryController, SupplierController, WarehouseController' },
        { name: 'Security & Auth Controllers', desc: 'LoginController (Sign In / Sign Up dialogs), UserController (RBAC Management)' }
      ]
    },
    {
      id: 'services',
      title: '3. Business Logic & AI/ML Service Layer',
      tech: 'Core Java 17+ • Native ML Engine • BCrypt Security',
      color: 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400',
      badgeColor: 'bg-emerald-600 text-white',
      components: [
        { name: 'DemandForecastService', desc: 'Time-series linear regression & moving-average trend modeling; predicts next 7-30 days demand with confidence intervals (MAE/RMSE)' },
        { name: 'StockRiskService', desc: 'Calculates stock-out risk (LOW, MEDIUM, HIGH), daily run rate velocity, and estimated days until depletion' },
        { name: 'SupplierDelayService', desc: 'Evaluates supplier delivery lead times, historical delays, reliability scores, and probability of delivery disruption' },
        { name: 'ProcurementAdvisorService', desc: 'Translates ML demand forecasts and safety stock requirements into actionable PO reorder recommendations' },
        { name: 'ModelTrainingService', desc: 'End-to-end pipeline: queries historical data, extracts features, trains regression/risk models, records metadata' },
        { name: 'Core Business Services', desc: 'AuthService (BCrypt hashing), InventoryService, OrderService, ShipmentService, ReportService' }
      ]
    },
    {
      id: 'dao',
      title: '4. Data Access Object (DAO) & JDBC Layer',
      tech: 'DAO Pattern • HikariCP Pool • JDBC PreparedStatements',
      color: 'border-amber-500/40 bg-amber-500/10 text-amber-400',
      badgeColor: 'bg-amber-600 text-white',
      components: [
        { name: 'MlDao & MlDaoImpl', desc: 'Persists and fetches demand forecasts, stock risk evaluations, supplier delay probabilities, and model training metrics' },
        { name: 'Operational DAOs', desc: 'UserDao, ProductDao, CategoryDao, SupplierDao, WarehouseDao, InventoryDao, OrderDao, ShipmentDao, ReportDao' },
        { name: 'HikariCP Connection Pool', desc: 'High-performance connection pool (DatabaseConnection.java) with min/max sizing, leak detection, and health checks' },
        { name: 'Transaction Coordinator', desc: 'Manual commit/rollback management (setAutoCommit(false)) ensuring strict ACID transactional guarantees' }
      ]
    },
    {
      id: 'database',
      title: '5. Relational Persistence Layer',
      tech: 'MySQL 8.0+ • InnoDB Engine • ACID Transactions',
      color: 'border-purple-500/40 bg-purple-500/10 text-purple-400',
      badgeColor: 'bg-purple-600 text-white',
      components: [
        { name: 'AI/ML Tables', desc: 'demand_forecasts, stock_risk_predictions, supplier_delay_predictions, ml_predictions, ml_model_metadata' },
        { name: 'Operational Tables', desc: 'inventory (product_id + warehouse_id), stock_transactions, purchase_orders, sales_orders, shipments, stock_transfers' },
        { name: 'Master Data Tables', desc: 'roles, users (with hashed passwords), categories, products, suppliers, warehouses' },
        { name: 'Data Integrity', desc: 'Foreign Keys with RESTRICT, unique indices, check constraints for positive inventory, UTF8MB4 charset' }
      ]
    }
  ];

  // Export diagram as an SVG vector file
  const handleExportSvg = () => {
    const svgEl = svgRef.current;
    if (!svgEl) return;
    const serializer = new XMLSerializer();
    const source = serializer.serializeToString(svgEl);
    const blob = new Blob([source], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'chainops_high_level_architecture.svg';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  // Export diagram as a high-resolution PNG image file via HTML5 Canvas
  const handleExportPng = () => {
    const svgEl = svgRef.current;
    if (!svgEl) return;
    setIsExportingPng(true);

    try {
      const serializer = new XMLSerializer();
      let source = serializer.serializeToString(svgEl);

      // Add xmlns if not present
      if (!source.match(/^<svg[^>]+xmlns="http:\/\/www\.w3\.org\/2000\/svg"/)) {
        source = source.replace(/^<svg/, '<svg xmlns="http://www.w3.org/2000/svg"');
      }

      const svgBlob = new Blob([source], { type: 'image/svg+xml;charset=utf-8' });
      const URL = window.URL || window.webkitURL;
      const blobURL = URL.createObjectURL(svgBlob);

      const img = new Image();
      img.onload = () => {
        // High-DPI canvas (2x scale: 2200 x 1600)
        const canvas = document.createElement('canvas');
        canvas.width = 2200;
        canvas.height = 1600;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.fillStyle = '#020617';
          ctx.fillRect(0, 0, canvas.width, canvas.height);
          ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

          const pngUrl = canvas.toDataURL('image/png');
          const a = document.createElement('a');
          a.href = pngUrl;
          a.download = 'chainops_high_level_architecture.png';
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
        }
        URL.revokeObjectURL(blobURL);
        setIsExportingPng(false);
      };

      img.onerror = () => {
        setIsExportingPng(false);
        // Fallback to SVG download if rasterization fails
        handleExportSvg();
      };

      img.src = blobURL;
    } catch {
      setIsExportingPng(false);
      handleExportSvg();
    }
  };

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 text-white flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-xl">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/20 text-blue-300 text-xs font-semibold mb-2">
            <Sparkles className="w-3.5 h-3.5 text-blue-400" />
            <span>High-Level Architectural Blueprint</span>
          </div>
          <h2 className="text-xl font-bold tracking-tight">ChainOps System Architecture &amp; Tiered Data Flow</h2>
          <p className="text-xs text-slate-400 mt-1 max-w-2xl">
            5-tier decoupled enterprise architecture: Presentation (JavaFX 21) &rarr; Controllers &rarr; Business Logic &amp; Native AI/ML Services &rarr; Data Access (DAO / HikariCP) &rarr; Persistence (MySQL 8.0)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleExportPng}
            disabled={isExportingPng}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white text-xs font-bold shadow-lg shadow-blue-500/20 transition-all cursor-pointer disabled:opacity-50 shrink-0"
          >
            <FileImage className="w-4 h-4" />
            <span>{isExportingPng ? 'Rendering PNG...' : 'Download Image (.png)'}</span>
          </button>

          <button
            onClick={handleExportSvg}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 hover:text-white border border-slate-700 text-xs font-bold transition-all cursor-pointer shrink-0"
          >
            <Download className="w-4 h-4" />
            <span>Vector (.svg)</span>
          </button>
        </div>
      </div>

      {/* High-Resolution SVG Architecture Visualizer */}
      <div className="bg-slate-950 rounded-2xl p-6 border border-slate-800 shadow-2xl overflow-x-auto">
        <svg
          ref={svgRef}
          viewBox="0 0 1100 800"
          className="w-full h-auto min-w-[850px] select-none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#0B0F19" />
              <stop offset="100%" stopColor="#020617" />
            </linearGradient>

            <linearGradient id="blueGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#1E40AF" />
              <stop offset="100%" stopColor="#3B82F6" />
            </linearGradient>

            <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#0E7490" />
              <stop offset="100%" stopColor="#06B6D4" />
            </linearGradient>

            <linearGradient id="emeraldGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#047857" />
              <stop offset="100%" stopColor="#10B981" />
            </linearGradient>

            <linearGradient id="purpleGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#6D28D9" />
              <stop offset="100%" stopColor="#8B5CF6" />
            </linearGradient>

            <linearGradient id="amberGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#B45309" />
              <stop offset="100%" stopColor="#F59E0B" />
            </linearGradient>

            <linearGradient id="roseGrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#BE123C" />
              <stop offset="100%" stopColor="#F43F5E" />
            </linearGradient>
          </defs>

          {/* Background Canvas */}
          <rect width="1100" height="800" rx="16" fill="url(#bgGrad)" />

          {/* Diagram Title Banner */}
          <text x="50" y="45" fill="#FFFFFF" fontSize="20" fontWeight="bold" fontFamily="sans-serif">
            ChainOps – Enterprise Supply Chain Management Architecture
          </text>
          <text x="50" y="68" fill="#94A3B8" fontSize="12" fontFamily="sans-serif">
            5-Tier Enterprise Architecture • Native Java ML Layer • Loose Coupling • ACID Transactional Integrity
          </text>

          {/* ========================================================= */}
          {/* TIER 1: Presentation Layer */}
          {/* ========================================================= */}
          <rect x="50" y="90" width="1000" height="96" rx="12" fill="#0F172A" stroke="#3B82F6" strokeWidth="2" />
          <rect x="65" y="102" width="240" height="24" rx="6" fill="url(#blueGrad)" />
          <text x="75" y="118" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="sans-serif">
            TIER 1: PRESENTATION (JavaFX 21 &amp; Web)
          </text>

          {/* Components */}
          <rect x="65" y="134" width="220" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="75" y="152" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">15 FXML View Templates</text>
          <text x="75" y="166" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Dashboard, Inventory, AiIntelligence.fxml</text>

          <rect x="300" y="134" width="220" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="310" y="152" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">Enterprise CSS Theme</text>
          <text x="310" y="166" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">style.css Design System &amp; Color Palette</text>

          <rect x="535" y="134" width="245" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="545" y="152" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">NavigationManager &amp; Scene Stack</text>
          <text x="545" y="166" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Stage Transition &amp; Active User Session</text>

          <rect x="795" y="134" width="240" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="805" y="152" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">AlertUtil &amp; Feedback System</text>
          <text x="805" y="166" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Modal Dialogs, Error Banners, Confirmations</text>

          {/* Connector Down 1 */}
          <path d="M 550 186 L 550 215" stroke="#38BDF8" strokeWidth="2" strokeDasharray="4 4" />
          <polygon points="546,215 554,215 550,222" fill="#38BDF8" />

          {/* ========================================================= */}
          {/* TIER 2: Controller Layer */}
          {/* ========================================================= */}
          <rect x="50" y="222" width="1000" height="96" rx="12" fill="#0F172A" stroke="#06B6D4" strokeWidth="2" />
          <rect x="65" y="234" width="240" height="24" rx="6" fill="url(#cyanGrad)" />
          <text x="75" y="250" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="sans-serif">
            TIER 2: CONTROLLERS &amp; DISPATCH
          </text>

          <rect x="65" y="266" width="230" height="40" rx="6" fill="#1E293B" stroke="#06B6D4" strokeWidth="1.5" />
          <text x="75" y="284" fill="#67E8F9" fontSize="11" fontWeight="bold" fontFamily="sans-serif">AiIntelligenceController</text>
          <text x="75" y="298" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Async ML training &amp; prediction binds</text>

          <rect x="310" y="266" width="230" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="320" y="284" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">Operations Controllers</text>
          <text x="320" y="298" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Inventory, PO, SO, Shipments, Transfers</text>

          <rect x="555" y="266" width="230" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="565" y="284" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">Master Data Controllers</text>
          <text x="565" y="298" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Products, Categories, Suppliers, Warehouses</text>

          <rect x="800" y="266" width="235" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="810" y="284" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">Auth &amp; Security Controllers</text>
          <text x="810" y="298" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">LoginController, UserController (RBAC)</text>

          {/* Connector Down 2 */}
          <path d="M 550 318 L 550 348" stroke="#34D399" strokeWidth="2" strokeDasharray="4 4" />
          <polygon points="546,348 554,348 550,355" fill="#34D399" />

          {/* ========================================================= */}
          {/* TIER 3: Business Logic & Native AI/ML Layer */}
          {/* ========================================================= */}
          <rect x="50" y="355" width="1000" height="125" rx="12" fill="#0F172A" stroke="#10B981" strokeWidth="2" />
          <rect x="65" y="367" width="310" height="24" rx="6" fill="url(#emeraldGrad)" />
          <text x="75" y="383" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="sans-serif">
            TIER 3: BUSINESS LOGIC &amp; NATIVE AI/ML LAYER
          </text>

          {/* AI/ML Sub-Services Box */}
          <rect x="65" y="399" width="480" height="70" rx="8" fill="#064E3B" fillOpacity="0.4" stroke="#10B981" />
          <text x="75" y="416" fill="#6EE7B7" fontSize="10" fontWeight="bold" fontFamily="sans-serif">
            INTEGRATED AI/ML SERVICES &amp; PIPELINES (Core Java 17+)
          </text>
          <text x="75" y="432" fill="#E2E8F0" fontSize="10" fontFamily="sans-serif">
            • <tspan fontWeight="bold">DemandForecastService</tspan>: 30d time-series regression + trend slope
          </text>
          <text x="75" y="446" fill="#E2E8F0" fontSize="10" fontFamily="sans-serif">
            • <tspan fontWeight="bold">StockRiskService</tspan>: Run rate velocity &amp; stock-out probability (Low/Med/High)
          </text>
          <text x="75" y="460" fill="#E2E8F0" fontSize="10" fontFamily="sans-serif">
            • <tspan fontWeight="bold">SupplierDelayService</tspan> &amp; <tspan fontWeight="bold">ProcurementAdvisorService</tspan>: Safety stock reorders
          </text>

          {/* Traditional Business Services Box */}
          <rect x="560" y="399" width="475" height="70" rx="8" fill="#1E293B" stroke="#475569" />
          <text x="570" y="416" fill="#94A3B8" fontSize="10" fontWeight="bold" fontFamily="sans-serif">
            CORE DOMAIN SERVICES &amp; WORKFLOWS
          </text>
          <text x="570" y="432" fill="#E2E8F0" fontSize="10" fontFamily="sans-serif">
            • <tspan fontWeight="bold">AuthService</tspan>: Salted BCrypt password hashing &amp; session guards
          </text>
          <text x="570" y="446" fill="#E2E8F0" fontSize="10" fontFamily="sans-serif">
            • <tspan fontWeight="bold">InventoryService &amp; StockTransferService</tspan>: Atomic stock movements
          </text>
          <text x="570" y="460" fill="#E2E8F0" fontSize="10" fontFamily="sans-serif">
            • <tspan fontWeight="bold">PurchaseOrderService, SalesOrderService, ReportService</tspan>
          </text>

          {/* Connector Down 3 */}
          <path d="M 550 480 L 550 510" stroke="#FBBF24" strokeWidth="2" strokeDasharray="4 4" />
          <polygon points="546,510 554,510 550,517" fill="#FBBF24" />

          {/* ========================================================= */}
          {/* TIER 4: Data Access Layer (DAO) */}
          {/* ========================================================= */}
          <rect x="50" y="517" width="1000" height="96" rx="12" fill="#0F172A" stroke="#F59E0B" strokeWidth="2" />
          <rect x="65" y="529" width="280" height="24" rx="6" fill="url(#amberGrad)" />
          <text x="75" y="545" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="sans-serif">
            TIER 4: DATA ACCESS OBJECTS (DAO) &amp; JDBC
          </text>

          <rect x="65" y="561" width="230" height="40" rx="6" fill="#1E293B" stroke="#F59E0B" strokeWidth="1.5" />
          <text x="75" y="579" fill="#FCD34D" fontSize="11" fontWeight="bold" fontFamily="sans-serif">MlDao &amp; MlDaoImpl</text>
          <text x="75" y="593" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Forecasts, risks, delays &amp; metadata queries</text>

          <rect x="310" y="561" width="230" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="320" y="579" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">HikariCP Connection Pool</text>
          <text x="320" y="593" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">DatabaseConnection.java (Auto-pooling)</text>

          <rect x="555" y="561" width="230" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="565" y="579" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">Operational DAOs</text>
          <text x="565" y="593" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">InventoryDao, OrderDao, UserDao, etc.</text>

          <rect x="800" y="561" width="235" height="40" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="810" y="579" fill="#E2E8F0" fontSize="11" fontWeight="600" fontFamily="sans-serif">PreparedStatement &amp; ACID</text>
          <text x="810" y="593" fill="#94A3B8" fontSize="9" fontFamily="sans-serif">Parameterized SQL • Commit/Rollback</text>

          {/* Connector Down 4 */}
          <path d="M 550 613 L 550 643" stroke="#A78BFA" strokeWidth="2" strokeDasharray="4 4" />
          <polygon points="546,643 554,643 550,650" fill="#A78BFA" />

          {/* ========================================================= */}
          {/* TIER 5: Persistence Layer (MySQL Database) */}
          {/* ========================================================= */}
          <rect x="50" y="650" width="1000" height="115" rx="12" fill="#0F172A" stroke="#8B5CF6" strokeWidth="2" />
          <rect x="65" y="662" width="310" height="24" rx="6" fill="url(#purpleGrad)" />
          <text x="75" y="678" fill="#FFFFFF" fontSize="11" fontWeight="bold" fontFamily="sans-serif">
            TIER 5: PERSISTENCE (MySQL 8.0 / InnoDB)
          </text>

          {/* Operational Tables */}
          <rect x="65" y="694" width="455" height="58" rx="6" fill="#1E293B" stroke="#475569" />
          <text x="75" y="711" fill="#DDD6FE" fontSize="10" fontWeight="bold" fontFamily="sans-serif">OPERATIONAL &amp; MASTER TABLES</text>
          <text x="75" y="726" fill="#94A3B8" fontSize="9" fontFamily="monospace">
            roles, users, categories, products, suppliers, warehouses,
          </text>
          <text x="75" y="740" fill="#94A3B8" fontSize="9" fontFamily="monospace">
            inventory, stock_transactions, purchase_orders, sales_orders, shipments
          </text>

          {/* AI/ML Prediction Tables */}
          <rect x="535" y="694" width="500" height="58" rx="6" fill="#2E1065" fillOpacity="0.4" stroke="#8B5CF6" />
          <text x="545" y="711" fill="#C4B5FD" fontSize="10" fontWeight="bold" fontFamily="sans-serif">AI/ML PREDICTION &amp; METRICS TABLES</text>
          <text x="545" y="726" fill="#E2E8F0" fontSize="9" fontFamily="monospace">
            demand_forecasts, stock_risk_predictions,
          </text>
          <text x="545" y="740" fill="#E2E8F0" fontSize="9" fontFamily="monospace">
            supplier_delay_predictions, ml_predictions, ml_model_metadata
          </text>
        </svg>
      </div>

      {/* Layer Drilldown Explorer */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
        {layers.map(layer => (
          <button
            key={layer.id}
            onClick={() => setSelectedLayer(layer.id)}
            className={`p-3.5 rounded-xl border text-left transition-all cursor-pointer ${
              selectedLayer === layer.id
                ? 'bg-slate-900 border-blue-500 shadow-md ring-1 ring-blue-500'
                : 'bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50'
            }`}
          >
            <div className={`text-[10px] font-bold uppercase tracking-wider mb-1 px-1.5 py-0.5 rounded w-fit ${layer.badgeColor}`}>
              {layer.title.split(' ')[0]} {layer.title.split(' ')[1]}
            </div>
            <div className={`text-xs font-bold truncate ${selectedLayer === layer.id ? 'text-white' : 'text-slate-900'}`}>
              {layer.title.split('(')[0]}
            </div>
            <div className="text-[10px] text-slate-500 truncate mt-0.5">{layer.tech.split('•')[0]}</div>
          </button>
        ))}
      </div>

      {/* Selected Layer Details Card */}
      {selectedLayer && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-4">
          {(() => {
            const current = layers.find(l => l.id === selectedLayer);
            if (!current) return null;
            return (
              <>
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-3">
                  <div>
                    <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                      <Layers className="w-5 h-5 text-blue-600" />
                      <span>{current.title}</span>
                    </h3>
                    <p className="text-xs text-slate-500 mt-0.5 font-mono">{current.tech}</p>
                  </div>
                  <span className="text-xs text-blue-600 font-semibold bg-blue-50 border border-blue-200 px-2.5 py-1 rounded-full w-fit">
                    Active Inspection
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {current.components.map((comp, idx) => (
                    <div key={idx} className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/60 space-y-1">
                      <div className="text-xs font-bold text-slate-900 flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                        <span>{comp.name}</span>
                      </div>
                      <p className="text-xs text-slate-600 leading-relaxed pl-5">{comp.desc}</p>
                    </div>
                  ))}
                </div>
              </>
            );
          })()}
        </div>
      )}
    </div>
  );
};
