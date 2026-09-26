import React, { useState } from 'react';
import {
  Package,
  Search,
  Plus,
  Edit,
  Trash2,
  Tag
} from 'lucide-react';
import { Product, Category } from '../data/initialData';

interface ProductsViewProps {
  products: Product[];
  categories: Category[];
  onCreateProduct: (p: Omit<Product, 'id'>) => { success: boolean; message: string };
  onUpdateProduct: (p: Product) => { success: boolean; message: string };
  onDeleteProduct: (id: number) => { success: boolean; message: string };
}

export const ProductsView: React.FC<ProductsViewProps> = ({
  products,
  categories,
  onCreateProduct,
  onUpdateProduct,
  onDeleteProduct
}) => {
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const [modalOpen, setModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  // Form
  const [sku, setSku] = useState('');
  const [name, setName] = useState('');
  const [categoryId, setCategoryId] = useState(categories[0]?.id || 1);
  const [description, setDescription] = useState('');
  const [unitPrice, setUnitPrice] = useState('50.00');
  const [reorderLevel, setReorderLevel] = useState('20');
  const [unit, setUnit] = useState('Units');
  const [status, setStatus] = useState<'ACTIVE' | 'DISCONTINUED' | 'OUT_OF_STOCK'>('ACTIVE');
  const [errorMsg, setErrorMsg] = useState('');

  const filtered = products.filter(p => {
    const matchesSearch =
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.sku.toLowerCase().includes(search.toLowerCase()) ||
      p.description.toLowerCase().includes(search.toLowerCase());

    const matchesCat = categoryFilter === 'ALL' || p.categoryId === Number(categoryFilter);
    const matchesStatus = statusFilter === 'ALL' || p.status === statusFilter;
    return matchesSearch && matchesCat && matchesStatus;
  });

  const handleOpenAdd = () => {
    setEditingProduct(null);
    setSku(`SKU-NEW-${Math.floor(100 + Math.random() * 900)}`);
    setName('');
    setCategoryId(categories[0]?.id || 1);
    setDescription('');
    setUnitPrice('50.00');
    setReorderLevel('20');
    setUnit('Units');
    setStatus('ACTIVE');
    setErrorMsg('');
    setModalOpen(true);
  };

  const handleOpenEdit = (p: Product) => {
    setEditingProduct(p);
    setSku(p.sku);
    setName(p.name);
    setCategoryId(p.categoryId);
    setDescription(p.description);
    setUnitPrice(p.unitPrice.toString());
    setReorderLevel(p.reorderLevel.toString());
    setUnit(p.unit);
    setStatus(p.status);
    setErrorMsg('');
    setModalOpen(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMsg('Product name is required.');
      return;
    }

    const price = parseFloat(unitPrice);
    if (isNaN(price) || price < 0) {
      setErrorMsg('Unit price cannot be negative.');
      return;
    }

    const reorder = parseInt(reorderLevel, 10);
    if (isNaN(reorder) || reorder < 0) {
      setErrorMsg('Reorder level cannot be negative.');
      return;
    }

    const cat = categories.find(c => c.id === categoryId);

    if (editingProduct) {
      const res = onUpdateProduct({
        ...editingProduct,
        sku,
        name,
        categoryId,
        categoryName: cat?.name || 'Category',
        description,
        unitPrice: price,
        reorderLevel: reorder,
        unit,
        status
      });
      if (!res.success) {
        setErrorMsg(res.message);
        return;
      }
    } else {
      const res = onCreateProduct({
        sku,
        name,
        categoryId,
        categoryName: cat?.name || 'Category',
        description,
        unitPrice: price,
        reorderLevel: reorder,
        unit,
        status
      });
      if (!res.success) {
        setErrorMsg(res.message);
        return;
      }
    }

    setModalOpen(false);
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Product Master Data</h2>
          <p className="text-xs text-slate-500 mt-1">
            Centralized SKU catalog, unit pricing, reorder baselines, and taxonomy mapping
          </p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-xs"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add New Product</span>
        </button>
      </div>

      {/* Toolbar */}
      <div className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search product name, SKU, or specs..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 focus:bg-white"
          />
        </div>

        <select
          value={categoryFilter}
          onChange={e => setCategoryFilter(e.target.value)}
          className="px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
        >
          <option value="ALL">All Categories</option>
          {categories.map(c => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        <select
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
          className="px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-blue-500"
        >
          <option value="ALL">All Statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="DISCONTINUED">Discontinued</option>
          <option value="OUT_OF_STOCK">Out of Stock</option>
        </select>

        <button
          onClick={() => { setSearch(''); setCategoryFilter('ALL'); setStatusFilter('ALL'); }}
          className="px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 rounded-lg hover:bg-slate-200 transition-colors"
        >
          Reset Filters
        </button>
      </div>

      {/* Products Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 font-semibold select-none">
              <tr>
                <th className="py-3 px-4">SKU Code</th>
                <th className="py-3 px-4">Product Name</th>
                <th className="py-3 px-4">Category</th>
                <th className="py-3 px-4 text-right">Unit Price ($)</th>
                <th className="py-3 px-4 text-right">Reorder Level</th>
                <th className="py-3 px-4">Unit</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map(p => (
                <tr key={p.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3 px-4 font-mono font-bold text-blue-700">{p.sku}</td>
                  <td className="py-3 px-4 font-semibold text-slate-900">
                    <div>{p.name}</div>
                    <div className="text-[10px] text-slate-400 font-normal line-clamp-1">{p.description}</div>
                  </td>
                  <td className="py-3 px-4 text-slate-600">{p.categoryName}</td>
                  <td className="py-3 px-4 text-right font-mono font-bold text-slate-900">
                    ${p.unitPrice.toFixed(2)}
                  </td>
                  <td className="py-3 px-4 text-right font-mono text-slate-600">{p.reorderLevel}</td>
                  <td className="py-3 px-4 text-slate-500">{p.unit}</td>
                  <td className="py-3 px-4 text-center">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                      p.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-600'
                    }`}>
                      {p.status}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-right">
                    <div className="flex items-center justify-end gap-1.5">
                      <button
                        onClick={() => handleOpenEdit(p)}
                        className="p-1 rounded text-slate-500 hover:text-blue-600 hover:bg-blue-50"
                      >
                        <Edit className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => {
                          if (confirm(`Delete product ${p.name}?`)) {
                            const res = onDeleteProduct(p.id);
                            if (!res.success) alert(res.message);
                          }
                        }}
                        className="p-1 rounded text-slate-400 hover:text-rose-600 hover:bg-rose-50"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl border border-slate-200 shadow-2xl max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900">
                {editingProduct ? 'Edit Product Item' : 'Add New Product'}
              </h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            {errorMsg && (
              <div className="p-3 rounded-lg bg-rose-50 text-rose-700 text-xs font-medium border border-rose-200">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">SKU Code</label>
                  <input
                    type="text"
                    value={sku}
                    onChange={e => setSku(e.target.value.toUpperCase())}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white font-mono"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Category</label>
                  <select
                    value={categoryId}
                    onChange={e => setCategoryId(Number(e.target.value))}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  >
                    {categories.map(c => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Product Name</label>
                <input
                  type="text"
                  placeholder="e.g. Cold-Rolled Carbon Steel Sheet 2mm"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                  required
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Unit Price ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={unitPrice}
                    onChange={e => setUnitPrice(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Reorder Level</label>
                  <input
                    type="number"
                    min="0"
                    value={reorderLevel}
                    onChange={e => setReorderLevel(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Unit</label>
                  <input
                    type="text"
                    value={unit}
                    onChange={e => setUnit(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description</label>
                <textarea
                  rows={2}
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  placeholder="Technical specs, material ratings..."
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                ></textarea>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Status</label>
                <select
                  value={status}
                  onChange={e => setStatus(e.target.value as any)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white"
                >
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="DISCONTINUED">DISCONTINUED</option>
                  <option value="OUT_OF_STOCK">OUT_OF_STOCK</option>
                </select>
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 rounded-lg font-medium text-slate-600 hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg font-bold bg-blue-600 text-white hover:bg-blue-700 shadow-xs"
                >
                  Save Product Record
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
