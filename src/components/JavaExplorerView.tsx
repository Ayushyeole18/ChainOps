import React, { useState, useEffect } from 'react';
import {
  Code2,
  Folder,
  FolderOpen,
  FileCode,
  FileText,
  Database,
  Terminal,
  Download,
  Copy,
  Check,
  Play,
  Layers,
  Sparkles,
  ExternalLink,
  ChevronRight,
  ChevronDown
} from 'lucide-react';
import { FileNode, projectFilesTree } from '../data/projectFiles';

interface JavaExplorerViewProps {
  onDownloadZip: () => void;
}

export const JavaExplorerView: React.FC<JavaExplorerViewProps> = ({ onDownloadZip }) => {
  const [treeData, setTreeData] = useState<FileNode[]>(projectFilesTree);
  const [selectedFile, setSelectedFile] = useState<FileNode | null>(null);
  const [copied, setCopied] = useState(false);
  const [activeTab, setActiveTab] = useState<'code' | 'guide' | 'schema'>('code');
  const [expandedFolders, setExpandedFolders] = useState<Record<string, boolean>>({
    'database': true,
    'src': true,
    'src/main': true,
    'src/main/java': true,
    'src/main/java/com/supplychainx': true,
    'src/main/resources': true,
    'src/main/resources/fxml': true
  });

  // Try fetching live tree from the Vite dev server endpoint
  useEffect(() => {
    fetch('/api/project-tree')
      .then(res => {
        if (res.ok) return res.json();
        throw new Error('Failed to load project tree');
      })
      .then(data => {
        if (Array.isArray(data) && data.length > 0) {
          setTreeData(data);
          // Auto select pom.xml or Main.java
          const pom = data.find((n: FileNode) => n.name === 'pom.xml');
          if (pom) setSelectedFile(pom);
        }
      })
      .catch(() => {
        // Fallback to projectFilesTree
        const pom = projectFilesTree.find(n => n.name === 'pom.xml');
        if (pom) setSelectedFile(pom);
      });
  }, []);

  const toggleFolder = (path: string) => {
    setExpandedFolders(prev => ({
      ...prev,
      [path]: !prev[path]
    }));
  };

  const handleCopyCode = () => {
    if (selectedFile?.content) {
      navigator.clipboard.writeText(selectedFile.content);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const renderTree = (nodes: FileNode[], depth = 0) => {
    return (
      <div className="space-y-0.5" style={{ paddingLeft: depth > 0 ? '14px' : '0' }}>
        {nodes.map(node => {
          if (node.type === 'folder') {
            const isExpanded = !!expandedFolders[node.path];
            return (
              <div key={node.path}>
                <button
                  onClick={() => toggleFolder(node.path)}
                  className="w-full flex items-center gap-1.5 px-2 py-1 text-left text-xs font-medium text-slate-700 hover:text-slate-900 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
                >
                  {isExpanded ? (
                    <ChevronDown className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                  ) : (
                    <ChevronRight className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                  )}
                  {isExpanded ? (
                    <FolderOpen className="w-3.5 h-3.5 text-amber-500 shrink-0" />
                  ) : (
                    <Folder className="w-3.5 h-3.5 text-amber-500 shrink-0" />
                  )}
                  <span className="truncate">{node.name}</span>
                </button>
                {isExpanded && node.children && renderTree(node.children, depth + 1)}
              </div>
            );
          }

          const isSelected = selectedFile?.path === node.path;
          let fileIcon = <FileCode className="w-3.5 h-3.5 text-blue-500 shrink-0" />;
          if (node.name.endsWith('.sql')) fileIcon = <Database className="w-3.5 h-3.5 text-emerald-500 shrink-0" />;
          else if (node.name.endsWith('.md')) fileIcon = <FileText className="w-3.5 h-3.5 text-slate-400 shrink-0" />;
          else if (node.name.endsWith('.xml') || node.name.endsWith('.fxml')) fileIcon = <Code2 className="w-3.5 h-3.5 text-amber-600 shrink-0" />;

          return (
            <button
              key={node.path}
              onClick={() => {
                setSelectedFile(node);
                setActiveTab('code');
              }}
              className={`w-full flex items-center gap-2 px-2 py-1 text-left text-xs rounded-md transition-colors cursor-pointer ${
                isSelected
                  ? 'bg-blue-50 text-blue-700 font-semibold border-l-2 border-blue-600'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              {fileIcon}
              <span className="truncate">{node.name}</span>
            </button>
          );
        })}
      </div>
    );
  };

  return (
    <div className="p-8 space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2.5">
            <Code2 className="w-6 h-6 text-blue-600" />
            Java 17+ & JavaFX Project Source Explorer
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Complete enterprise codebase: Maven, HikariCP, JDBC, MySQL, JUnit 5, FXML, and responsive JavaFX CSS
          </p>
        </div>

        <button
          onClick={onDownloadZip}
          className="flex items-center gap-2 px-4 py-2.5 rounded-lg text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white shadow-md shadow-blue-500/20 transition-all cursor-pointer hover:scale-[1.01]"
        >
          <Download className="w-4 h-4" />
          <span>Download Complete Project (.zip)</span>
        </button>
      </div>

      {/* Mode Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-2">
        <button
          onClick={() => setActiveTab('code')}
          className={`flex items-center gap-2 px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-colors cursor-pointer ${
            activeTab === 'code' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Code2 className="w-3.5 h-3.5" />
          <span>Source Code Viewer</span>
        </button>
        <button
          onClick={() => setActiveTab('guide')}
          className={`flex items-center gap-2 px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-colors cursor-pointer ${
            activeTab === 'guide' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Terminal className="w-3.5 h-3.5" />
          <span>Run Locally & Maven Guide</span>
        </button>
        <button
          onClick={() => setActiveTab('schema')}
          className={`flex items-center gap-2 px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-colors cursor-pointer ${
            activeTab === 'schema' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Database className="w-3.5 h-3.5" />
          <span>MySQL Schema & Architecture</span>
        </button>
      </div>

      {/* Main Content Area */}
      {activeTab === 'code' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* File Tree Sidebar */}
          <div className="lg:col-span-4 bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="p-3 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
              <span className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                <Folder className="w-3.5 h-3.5 text-amber-500" />
                <span>Project Explorer</span>
              </span>
              <span className="text-[10px] font-mono text-slate-400 bg-slate-200/60 px-1.5 py-0.5 rounded">
                ChainOps/
              </span>
            </div>
            <div className="p-3 max-h-[640px] overflow-y-auto">
              {renderTree(treeData)}
            </div>
          </div>

          {/* Code Viewer Panel */}
          <div className="lg:col-span-8 bg-slate-950 text-slate-100 rounded-xl border border-slate-800 shadow-lg overflow-hidden flex flex-col">
            {/* Header */}
            <div className="px-4 py-3 bg-slate-900 border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-blue-400 font-semibold">
                  {selectedFile ? selectedFile.path : 'Select a file to view'}
                </span>
                {selectedFile?.language && (
                  <span className="px-2 py-0.5 rounded text-[10px] uppercase font-mono font-bold bg-slate-800 text-slate-300">
                    {selectedFile.language}
                  </span>
                )}
              </div>

              {selectedFile?.content && (
                <button
                  onClick={handleCopyCode}
                  className="flex items-center gap-1 px-2.5 py-1 rounded text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 transition-colors cursor-pointer"
                >
                  {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copied ? 'Copied!' : 'Copy Code'}</span>
                </button>
              )}
            </div>

            {/* Code Body */}
            <div className="p-4 max-h-[600px] overflow-y-auto font-mono text-xs leading-relaxed bg-[#0d1117] text-slate-200">
              {selectedFile?.content ? (
                <pre className="whitespace-pre overflow-x-auto">
                  <code>{selectedFile.content}</code>
                </pre>
              ) : (
                <div className="py-20 text-center text-slate-500">
                  Select any file from the explorer on the left to inspect its complete Java or FXML source code.
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Guide Tab */}
      {activeTab === 'guide' && (
        <div className="space-y-6">
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-6 space-y-4">
            <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <Terminal className="w-5 h-5 text-blue-600" />
              How to Run ChainOps Locally (Terminal & IDE)
            </h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              ChainOps is built with standard Maven, Java 17+, JavaFX 21, and MySQL 8. You can run it on macOS, Linux, or Windows with a single Maven command or by importing it into IntelliJ IDEA / Eclipse.
            </p>

            <div className="space-y-4 pt-2">
              <div className="p-4 bg-slate-900 text-slate-100 rounded-xl font-mono text-xs space-y-2">
                <div className="text-slate-400"># 1. Unzip the project and navigate to the directory</div>
                <div className="text-emerald-400 font-semibold">cd ChainOps</div>
                <div className="text-slate-400 pt-2"># 2. Initialize the MySQL database</div>
                <div className="text-emerald-400 font-semibold">
                  mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS supplychainx;"
                </div>
                <div className="text-emerald-400 font-semibold">
                  mysql -u root -p supplychainx &lt; database/schema.sql
                </div>
                <div className="text-emerald-400 font-semibold">
                  mysql -u root -p supplychainx &lt; database/seed.sql
                </div>
                <div className="text-slate-400 pt-2"># 3. Configure credentials in src/main/resources/config/db.properties</div>
                <div className="text-emerald-400 font-semibold">
                  # Edit db.username and db.password if different from root/root123
                </div>
                <div className="text-slate-400 pt-2"># 4. Build and launch the JavaFX application</div>
                <div className="text-emerald-400 font-bold">mvn clean compile javafx:run</div>
                <div className="text-slate-400 pt-2"># 5. Run test suite (JUnit 5 tests)</div>
                <div className="text-emerald-400 font-semibold">mvn test</div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
                <div className="p-4 bg-blue-50 border border-blue-200 rounded-xl">
                  <h4 className="font-bold text-blue-900 text-xs flex items-center gap-1.5 mb-2">
                    <Sparkles className="w-4 h-4 text-blue-600" />
                    IntelliJ IDEA Setup
                  </h4>
                  <ul className="text-xs text-blue-800 space-y-1.5 list-disc list-inside">
                    <li>Open IntelliJ IDEA &rarr; File &rarr; Open &rarr; Select <code className="font-mono bg-blue-100 px-1 py-0.5 rounded">ChainOps/pom.xml</code></li>
                    <li>Wait for Maven to download dependencies (JavaFX, HikariCP, MySQL Connector, BCrypt)</li>
                    <li>Open <code className="font-mono bg-blue-100 px-1 py-0.5 rounded">com.supplychainx.Main</code> and click Run &#9654;</li>
                  </ul>
                </div>

                <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl">
                  <h4 className="font-bold text-emerald-900 text-xs flex items-center gap-1.5 mb-2">
                    <Layers className="w-4 h-4 text-emerald-600" />
                    Demo Credentials (Seeded)
                  </h4>
                  <div className="text-xs text-emerald-800 space-y-1">
                    <div><strong>Admin:</strong> <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">admin</code> / <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">admin123</code></div>
                    <div><strong>Warehouse:</strong> <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">warehouse</code> / <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">warehouse123</code></div>
                    <div><strong>Procurement:</strong> <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">procure</code> / <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">procure123</code></div>
                    <div><strong>Sales:</strong> <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">sales</code> / <code className="font-mono bg-emerald-100 px-1 py-0.5 rounded">sales123</code></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Schema Tab */}
      {activeTab === 'schema' && (
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-6 space-y-6">
          <div>
            <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <Database className="w-5 h-5 text-blue-600" />
              Database Architecture (MySQL 8.0 / InnoDB)
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              Normalized relational schema with strict Foreign Keys, Indexes, Unique constraints, and Transactional ACID compliance.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[
              {
                table: 'roles & users',
                desc: 'User identity, BCrypt hashes, and Role-Based Access Control (Admin, Warehouse, Procure, Sales).'
              },
              {
                table: 'categories & products',
                desc: 'Hierarchical product taxonomy with SKUs, units of measure, reorder thresholds, and active status.'
              },
              {
                table: 'warehouses & inventory',
                desc: 'Multi-warehouse stock holding with unique (product_id, warehouse_id) indexing, available and reserved quantities.'
              },
              {
                table: 'stock_transactions',
                desc: 'Immutable audit trail of all movements: INBOUND_PO, OUTBOUND_SO, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT.'
              },
              {
                table: 'purchase_orders & items',
                desc: 'Procurement workflows from DRAFT to APPROVED, with atomic stock increment upon RECEIVING.'
              },
              {
                table: 'sales_orders & items',
                desc: 'Customer orders with stock availability checks, reservation locks, and confirmation validation.'
              },
              {
                table: 'shipments',
                desc: 'Carrier tracking, tracking numbers, dispatch dates, and real-time delivery state machine.'
              },
              {
                table: 'stock_transfers & items',
                desc: 'Inter-warehouse inventory rebalancing with atomic multi-warehouse balance transfers.'
              }
            ].map((s, idx) => (
              <div key={idx} className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 space-y-1.5">
                <div className="text-xs font-mono font-bold text-blue-700 uppercase">{s.table}</div>
                <div className="text-xs text-slate-600 leading-relaxed">{s.desc}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
