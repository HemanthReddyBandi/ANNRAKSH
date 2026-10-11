import { useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { cropService } from '../services/cropService.js';
import { demandService } from '../services/demandService.js';
import { getErrorMessage } from '../services/api.js';
import { formatDate, todayLocal, titleCase } from '../utils/format.js';

const initialForm = { cropId: '', location: '', demandLevel: 'MEDIUM', demandQuantity: '', supplyQuantity: '', quantityUnit: 'kg', recordedDate: todayLocal() };

export default function Demand() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [filter, setFilter] = useState(user?.location || '');
  const [locationInput, setLocationInput] = useState(user?.location || '');
  const [records, setRecords] = useState([]);
  const [crops, setCrops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState({ ...initialForm, location: user?.location || '' });
  const [saving, setSaving] = useState(false);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [demandRecords, cropList] = await Promise.all([
        demandService.list(filter.trim()),
        isAdmin ? cropService.list() : Promise.resolve([]),
      ]);
      setRecords(demandRecords);
      setCrops(cropList);
      if (!form.cropId && cropList.length) setForm((current) => ({ ...current, cropId: String(cropList[0].id) }));
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(); }, [filter, isAdmin]);

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    try {
      await demandService.create({
        ...form,
        cropId: Number(form.cropId),
        location: form.location.trim(),
        demandQuantity: Number(form.demandQuantity),
        supplyQuantity: Number(form.supplyQuantity),
        quantityUnit: form.quantityUnit.trim(),
      });
      setFormOpen(false);
      setNotice('Demand snapshot recorded.');
      await load();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setSaving(false); }
  }

  function applyFilter(event) {
    event.preventDefault();
    setFilter(locationInput);
  }

  return (
    <>
      <PageHeader eyebrow="LOCAL MARKET SIGNALS" title="Crop demand" description="Compare recorded demand and supply by location. Each recommendation shows which snapshot it used."
        action={isAdmin && <button className="button button-primary" onClick={() => { setForm({ ...initialForm, location: user?.location || '', cropId: crops[0] ? String(crops[0].id) : '' }); setFormOpen((open) => !open); setNotice(''); }}>＋ Record demand</button>} />
      {error && <ErrorState message={error} onRetry={load} />}
      {notice && <div className="success-alert" role="status">✓ &nbsp;{notice}</div>}
      <section className="filter-bar"><form className="filter-form" onSubmit={applyFilter}><label className="form-field"><span>Filter by location</span><div className="input-with-icon"><span>⌖</span><input value={locationInput} maxLength="160" placeholder="All locations" onChange={(event) => setLocationInput(event.target.value)} /></div></label><button className="button button-secondary" type="submit">Apply filter</button>{filter && <button className="text-button" type="button" onClick={() => { setLocationInput(''); setFilter(''); }}>Clear</button>}</form><span className="record-count">{records.length} {records.length === 1 ? 'record' : 'records'}</span></section>
      {formOpen && isAdmin && <section className="content-panel form-panel"><div className="panel-heading"><div><p className="eyebrow">MARKET SNAPSHOT</p><h2>Record crop demand</h2></div><button className="icon-button" aria-label="Close form" onClick={() => setFormOpen(false)}>×</button></div>
        {!crops.length ? <p className="helper-banner">Add a crop to the library before recording demand.</p> : <form className="data-form" onSubmit={submit}>
          <label className="form-field"><span>Crop</span><select required value={form.cropId} onChange={(event) => setForm({ ...form, cropId: event.target.value })}>{crops.map((crop) => <option key={crop.id} value={crop.id}>{crop.name}</option>)}</select></label>
          <label className="form-field"><span>Market location</span><input required maxLength="160" placeholder="Town or district" value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} /></label>
          <label className="form-field"><span>Demand level</span><select value={form.demandLevel} onChange={(event) => setForm({ ...form, demandLevel: event.target.value })}><option value="HIGH">High</option><option value="MEDIUM">Medium</option><option value="LOW">Low</option></select></label>
          <label className="form-field"><span>Demand quantity</span><input type="number" min="0" step="0.001" required value={form.demandQuantity} onChange={(event) => setForm({ ...form, demandQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Supply quantity</span><input type="number" min="0" step="0.001" required value={form.supplyQuantity} onChange={(event) => setForm({ ...form, supplyQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Quantity unit</span><input required maxLength="20" placeholder="kg" value={form.quantityUnit} onChange={(event) => setForm({ ...form, quantityUnit: event.target.value })} /></label>
          <label className="form-field"><span>Date recorded</span><input type="date" required value={form.recordedDate} onChange={(event) => setForm({ ...form, recordedDate: event.target.value })} /></label>
          <div className="form-actions"><button className="button button-secondary" type="button" onClick={() => setFormOpen(false)}>Cancel</button><button className="button button-primary" disabled={saving}>{saving ? 'Saving…' : 'Save snapshot'}</button></div>
        </form>}
      </section>}
      {loading ? <LoadingState label="Loading local demand" /> : records.length ? <div className="table-wrap"><table className="data-table"><thead><tr><th>Crop</th><th>Location</th><th>Demand signal</th><th>Demand</th><th>Supply</th><th>Recorded</th></tr></thead><tbody>{records.map((record) => <tr key={record.id}><td><strong>{record.cropName}</strong><small className="cell-sub">{record.quantityUnit}</small></td><td>{record.location}</td><td><span className={`demand-badge demand-${record.demandLevel.toLowerCase()}`}><span />{titleCase(record.demandLevel)}</span></td><td>{Number(record.demandQuantity).toLocaleString('en-IN')} <small>{record.quantityUnit}</small></td><td>{Number(record.supplyQuantity).toLocaleString('en-IN')} <small>{record.quantityUnit}</small></td><td>{formatDate(record.recordedDate)}</td></tr>)}</tbody></table></div>
        : <EmptyState title="No demand records found" description={filter ? `There are no market snapshots recorded for ${filter} yet.` : 'Market demand snapshots will appear here when administrators record them.'} action={isAdmin && <button className="button button-primary" onClick={() => setFormOpen(true)}>＋ Record demand</button>} />}
    </>
  );
}
