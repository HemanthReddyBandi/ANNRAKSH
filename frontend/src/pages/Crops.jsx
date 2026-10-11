import { useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { cropService } from '../services/cropService.js';
import { getErrorMessage } from '../services/api.js';

const blankCrop = { name: '', category: '', season: '', averageGrowthDays: '' };

export default function Crops() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [crops, setCrops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(blankCrop);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState('');

  async function loadCrops() {
    setLoading(true);
    setError('');
    try { setCrops(await cropService.list()); }
    catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setLoading(false); }
  }

  useEffect(() => { loadCrops(); }, []);

  function startCreate() {
    setEditingId(null);
    setForm(blankCrop);
    setFormOpen(true);
    setNotice('');
  }

  function startEdit(crop) {
    setEditingId(crop.id);
    setForm({ name: crop.name, category: crop.category, season: crop.season, averageGrowthDays: crop.averageGrowthDays });
    setFormOpen(true);
    setNotice('');
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    const payload = { ...form, name: form.name.trim(), category: form.category.trim(), season: form.season.trim(), averageGrowthDays: Number(form.averageGrowthDays) };
    try {
      if (editingId) await cropService.update(editingId, payload);
      else await cropService.create(payload);
      setFormOpen(false);
      setNotice(editingId ? 'Crop details updated.' : 'Crop added to the library.');
      await loadCrops();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally { setSaving(false); }
  }

  async function remove(crop) {
    if (!window.confirm(`Remove ${crop.name} from the crop library?`)) return;
    setError('');
    setNotice('');
    try {
      await cropService.remove(crop.id);
      setNotice(`${crop.name} removed.`);
      await loadCrops();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
  }

  return (
    <>
      <PageHeader eyebrow="CROP LIBRARY" title="Crops" description="Explore crops and the growing conditions recorded for each variety."
        action={isAdmin && <button className="button button-primary" onClick={startCreate}>＋ Add crop</button>} />
      {error && <ErrorState message={error} onRetry={loadCrops} />}
      {notice && <div className="success-alert" role="status">✓ &nbsp;{notice}</div>}
      {formOpen && isAdmin && <section className="content-panel form-panel"><div className="panel-heading"><div><p className="eyebrow">{editingId ? 'EDIT VARIETY' : 'NEW VARIETY'}</p><h2>{editingId ? 'Update crop details' : 'Add a crop'}</h2></div><button className="icon-button" aria-label="Close form" onClick={() => setFormOpen(false)}>×</button></div>
        <form className="data-form" onSubmit={submit}>
          <label className="form-field"><span>Crop name</span><input required maxLength="100" placeholder="e.g. Tomato" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label>
          <label className="form-field"><span>Category</span><input required maxLength="80" placeholder="e.g. Vegetable" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} /></label>
          <label className="form-field"><span>Suitable season</span><input required maxLength="120" placeholder="Kharif, Rabi, Zaid, or Year-round" value={form.season} onChange={(event) => setForm({ ...form, season: event.target.value })} /></label>
          <label className="form-field"><span>Average growth days</span><input type="number" min="1" required value={form.averageGrowthDays} onChange={(event) => setForm({ ...form, averageGrowthDays: event.target.value })} /></label>
          <div className="form-actions"><button className="button button-secondary" type="button" onClick={() => setFormOpen(false)}>Cancel</button><button className="button button-primary" disabled={saving}>{saving ? 'Saving…' : editingId ? 'Save changes' : 'Add crop'}</button></div>
        </form>
      </section>}
      {loading ? <LoadingState label="Loading crop library" /> : crops.length ? <div className="crop-grid">{crops.map((crop, index) => <article className="crop-card" key={crop.id}>
        <div className={`crop-card-art crop-art-${index % 4}`}><span>{crop.name.slice(0, 1).toUpperCase()}</span><small>{crop.category}</small></div>
        <div className="crop-card-body"><div className="crop-card-title"><div><h2>{crop.name}</h2><span className="category-label">{crop.category}</span></div>{isAdmin && <button className="more-button" aria-label={`More actions for ${crop.name}`} onClick={() => startEdit(crop)}>···</button>}</div>
          <div className="crop-detail-row"><span>Growing season</span><strong>{crop.season}</strong></div><div className="crop-detail-row"><span>Average growth</span><strong>{crop.averageGrowthDays} days</strong></div>
          {isAdmin && <div className="crop-card-actions"><button className="text-button" onClick={() => startEdit(crop)}>Edit details</button><button className="text-button danger-text" onClick={() => remove(crop)}>Remove</button></div>}
        </div>
      </article>)}</div> : <EmptyState title="Your crop library is empty" description={isAdmin ? 'Add a crop so farmers can create plans and receive useful recommendations.' : 'Crop varieties will appear here once they are added by an administrator.'} action={isAdmin && <button className="button button-primary" onClick={startCreate}>＋ Add the first crop</button>} />}
    </>
  );
}
