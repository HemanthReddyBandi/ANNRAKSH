import { useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { cropService } from '../services/cropService.js';
import { planService } from '../services/planService.js';
import { getErrorMessage } from '../services/api.js';
import { formatDate, todayLocal, titleCase } from '../utils/format.js';

function addDays(dateText, days) {
  if (!dateText) return '';
  const date = new Date(`${dateText}T12:00:00`);
  date.setDate(date.getDate() + Number(days || 0));
  return date.toISOString().slice(0, 10);
}

export default function CultivationPlans() {
  const [plans, setPlans] = useState([]);
  const [crops, setCrops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [harvestPlan, setHarvestPlan] = useState(null);
  const [dispositionPlan, setDispositionPlan] = useState(null);
  const [dispositionForm, setDispositionForm] = useState({ soldQuantity: '0', storedQuantity: '0', discardedQuantity: '0' });
  const [historyPlan, setHistoryPlan] = useState(null);
  const [dispositionHistory, setDispositionHistory] = useState([]);
  const [harvestForm, setHarvestForm] = useState({ actualHarvestQuantity: '', actualHarvestDate: todayLocal(), soldQuantity: '0', storedQuantity: '0', discardedQuantity: '0' });
  const [form, setForm] = useState({ cropId: '', areaHectares: '', plannedQuantity: '', quantityUnit: 'kg', plantingDate: todayLocal(), expectedHarvestDate: '' });

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [planList, cropList] = await Promise.all([planService.list(), cropService.list()]);
      setPlans(planList);
      setCrops(cropList);
      setForm((current) => current.cropId || !cropList.length ? current : {
        ...current,
        cropId: String(cropList[0].id),
        expectedHarvestDate: addDays(current.plantingDate || todayLocal(), cropList[0].averageGrowthDays),
      });
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  function openCreate() {
    const firstCrop = crops[0];
    const plantingDate = todayLocal();
    setForm({ cropId: firstCrop ? String(firstCrop.id) : '', areaHectares: '', plannedQuantity: '', quantityUnit: 'kg', plantingDate, expectedHarvestDate: firstCrop ? addDays(plantingDate, firstCrop.averageGrowthDays) : '' });
    setFormOpen(true);
    setNotice('');
  }

  function changeCrop(cropId) {
    const crop = crops.find((item) => String(item.id) === cropId);
    setForm((current) => ({ ...current, cropId, expectedHarvestDate: crop ? addDays(current.plantingDate, crop.averageGrowthDays) : current.expectedHarvestDate }));
  }

  async function createPlan(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    try {
      await planService.create({ ...form, cropId: Number(form.cropId), areaHectares: Number(form.areaHectares), plannedQuantity: Number(form.plannedQuantity) });
      setFormOpen(false);
      setNotice('Cultivation plan created.');
      await load();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setSaving(false); }
  }

  async function updateStatus(plan, status) {
    setError('');
    setNotice('');
    try {
      await planService.updateStatus(plan.id, status);
      setNotice(`${plan.cropName} plan marked ${titleCase(status).toLowerCase()}.`);
      await load();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
  }

  async function recordHarvest(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    try {
      await planService.recordHarvest(harvestPlan.id, {
        actualHarvestQuantity: Number(harvestForm.actualHarvestQuantity),
        actualHarvestDate: harvestForm.actualHarvestDate,
        soldQuantity: Number(harvestForm.soldQuantity || 0),
        storedQuantity: Number(harvestForm.storedQuantity || 0),
        discardedQuantity: Number(harvestForm.discardedQuantity || 0),
      });
      setNotice(`Harvest for ${harvestPlan.cropName} recorded.`);
      setHarvestPlan(null);
      await load();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setSaving(false); }
  }

  async function updateDisposition(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    try {
      await planService.updateDisposition(dispositionPlan.id, {
        soldQuantity: Number(dispositionForm.soldQuantity),
        storedQuantity: Number(dispositionForm.storedQuantity),
        discardedQuantity: Number(dispositionForm.discardedQuantity),
      });
      setNotice(`Harvest outcome for ${dispositionPlan.cropName} updated.`);
      setDispositionPlan(null);
      await load();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setSaving(false); }
  }

  async function viewDispositionHistory(plan) {
    setError('');
    try {
      setDispositionHistory(await planService.dispositionHistory(plan.id));
      setHistoryPlan(plan);
    } catch (requestError) { setError(getErrorMessage(requestError)); }
  }

  return (
    <>
      <PageHeader eyebrow="YOUR FIELD PLAN" title="Cultivation plans" description="Track what you plant, when you expect to harvest, and what you actually bring in."
        action={<button className="button button-primary" onClick={openCreate}>＋ New plan</button>} />
      {error && <ErrorState message={error} onRetry={load} />}
      {notice && <div className="success-alert" role="status">✓ &nbsp;{notice}</div>}
      {formOpen && <section className="content-panel form-panel"><div className="panel-heading"><div><p className="eyebrow">NEW CULTIVATION</p><h2>Plan a crop</h2></div><button className="icon-button" aria-label="Close form" onClick={() => setFormOpen(false)}>×</button></div>
        {!crops.length ? <p className="helper-banner">Crop options will be available after an administrator adds them to the library.</p> : <form className="data-form" onSubmit={createPlan}>
          <label className="form-field"><span>Crop</span><select required value={form.cropId} onChange={(event) => changeCrop(event.target.value)}>{crops.map((crop) => <option key={crop.id} value={crop.id}>{crop.name} · {crop.averageGrowthDays} days</option>)}</select></label>
          <label className="form-field"><span>Area (hectares)</span><input type="number" min="0.001" step="0.001" required value={form.areaHectares} onChange={(event) => setForm({ ...form, areaHectares: event.target.value })} /></label>
          <label className="form-field"><span>Planned quantity</span><input type="number" min="0.001" step="0.001" required value={form.plannedQuantity} onChange={(event) => setForm({ ...form, plannedQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Quantity unit</span><input required maxLength="20" value={form.quantityUnit} onChange={(event) => setForm({ ...form, quantityUnit: event.target.value })} /></label>
          <label className="form-field"><span>Planting date</span><input type="date" required value={form.plantingDate} onChange={(event) => setForm({ ...form, plantingDate: event.target.value, expectedHarvestDate: addDays(event.target.value, crops.find((crop) => String(crop.id) === form.cropId)?.averageGrowthDays) })} /></label>
          <label className="form-field"><span>Expected harvest date</span><input type="date" required min={form.plantingDate} value={form.expectedHarvestDate} onChange={(event) => setForm({ ...form, expectedHarvestDate: event.target.value })} /></label>
          <p className="form-note">The expected date starts from the crop’s average growth period. Adjust it for your local conditions.</p>
          <div className="form-actions"><button className="button button-secondary" type="button" onClick={() => setFormOpen(false)}>Cancel</button><button className="button button-primary" disabled={saving || !crops.length}>{saving ? 'Saving…' : 'Create plan'}</button></div>
        </form>}
      </section>}
      {harvestPlan && <section className="content-panel form-panel harvest-form-panel"><div className="panel-heading"><div><p className="eyebrow">HARVEST AND OUTCOME</p><h2>{harvestPlan.cropName}</h2></div><button className="icon-button" aria-label="Close harvest form" onClick={() => setHarvestPlan(null)}>×</button></div>
        <form className="data-form" onSubmit={recordHarvest}>
          <label className="form-field"><span>Total harvested ({harvestPlan.quantityUnit})</span><input type="number" min="0.001" step="0.001" required value={harvestForm.actualHarvestQuantity} onChange={(event) => setHarvestForm({ ...harvestForm, actualHarvestQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Harvest date</span><input type="date" required min={harvestPlan.plantingDate} max={todayLocal()} value={harvestForm.actualHarvestDate} onChange={(event) => setHarvestForm({ ...harvestForm, actualHarvestDate: event.target.value })} /></label>
          <label className="form-field"><span>Sold ({harvestPlan.quantityUnit})</span><input type="number" min="0" step="0.001" value={harvestForm.soldQuantity} onChange={(event) => setHarvestForm({ ...harvestForm, soldQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Stored ({harvestPlan.quantityUnit})</span><input type="number" min="0" step="0.001" value={harvestForm.storedQuantity} onChange={(event) => setHarvestForm({ ...harvestForm, storedQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Discarded ({harvestPlan.quantityUnit})</span><input type="number" min="0" step="0.001" value={harvestForm.discardedQuantity} onChange={(event) => setHarvestForm({ ...harvestForm, discardedQuantity: event.target.value })} /></label>
          <p className="form-note">Sold, stored, and discarded quantities may total less than the harvest. The remaining amount is shown as unallocated; these quantities must not exceed the total harvested.</p>
          <div className="form-actions"><button className="button button-secondary" type="button" onClick={() => setHarvestPlan(null)}>Cancel</button><button className="button button-primary" disabled={saving}>{saving ? 'Saving…' : 'Save harvest'}</button></div>
        </form>
      </section>}
      {dispositionPlan && <section className="content-panel form-panel"><div className="panel-heading"><div><p className="eyebrow">UPDATE HARVEST OUTCOME</p><h2>{dispositionPlan.cropName}</h2></div><button className="icon-button" aria-label="Close outcome form" onClick={() => setDispositionPlan(null)}>×</button></div>
        <form className="data-form" onSubmit={updateDisposition}>
          <label className="form-field"><span>Sold ({dispositionPlan.quantityUnit})</span><input type="number" min="0" step="0.001" required value={dispositionForm.soldQuantity} onChange={(event) => setDispositionForm({ ...dispositionForm, soldQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Stored ({dispositionPlan.quantityUnit})</span><input type="number" min="0" step="0.001" required value={dispositionForm.storedQuantity} onChange={(event) => setDispositionForm({ ...dispositionForm, storedQuantity: event.target.value })} /></label>
          <label className="form-field"><span>Discarded ({dispositionPlan.quantityUnit})</span><input type="number" min="0" step="0.001" required value={dispositionForm.discardedQuantity} onChange={(event) => setDispositionForm({ ...dispositionForm, discardedQuantity: event.target.value })} /></label>
          <p className="form-note">These are updated totals, not extra amounts. Changes are saved to the harvest history.</p>
          <div className="form-actions"><button className="button button-secondary" type="button" onClick={() => setDispositionPlan(null)}>Cancel</button><button className="button button-primary" disabled={saving}>{saving ? 'Saving…' : 'Save outcome'}</button></div>
        </form>
      </section>}
      {historyPlan && <section className="content-panel form-panel"><div className="panel-heading"><div><p className="eyebrow">AUDIT HISTORY</p><h2>{historyPlan.cropName} · Plan #{historyPlan.id}</h2></div><button className="icon-button" aria-label="Close history" onClick={() => setHistoryPlan(null)}>×</button></div>
        {dispositionHistory.length ? <div className="table-wrap"><table className="data-table">
          <thead><tr><th>Event</th><th>Sold (previous → current)</th><th>Stored (previous → current)</th><th>Discarded (previous → current)</th><th>Updated</th></tr></thead>
          <tbody>{dispositionHistory.map((entry) => <tr key={entry.id}>
            <td><strong>{titleCase(entry.action)}</strong></td>
            <td>{Number(entry.previousSoldQuantity).toLocaleString('en-IN')} → {Number(entry.soldQuantity).toLocaleString('en-IN')} {historyPlan.quantityUnit}</td>
            <td>{Number(entry.previousStoredQuantity).toLocaleString('en-IN')} → {Number(entry.storedQuantity).toLocaleString('en-IN')} {historyPlan.quantityUnit}</td>
            <td>{Number(entry.previousDiscardedQuantity).toLocaleString('en-IN')} → {Number(entry.discardedQuantity).toLocaleString('en-IN')} {historyPlan.quantityUnit}</td>
            <td>{new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(entry.changedAt))}</td>
          </tr>)}</tbody>
        </table></div> : <EmptyState title="No disposition changes recorded" description="The harvest record or later outcome changes will appear here." />}
      </section>}
      {loading ? <LoadingState label="Loading your plans" /> : plans.length ? <div className="plan-list">{plans.map((plan) => <article className="plan-card" key={plan.id}>
        <div className="plan-card-top"><div className="plan-crop-mark">{plan.cropName?.slice(0, 1)}</div><div className="plan-title"><h2>{plan.cropName}</h2><span>{plan.areaHectares} ha · Plan #{plan.id}</span></div><span className={`status-badge status-${plan.status.toLowerCase()}`}>{titleCase(plan.status)}</span></div>
        <div className="plan-progress"><div className="plan-date"><span>Planted</span><strong>{formatDate(plan.plantingDate)}</strong></div><div className="plan-timeline"><span className={['PLANTED', 'GROWING', 'HARVESTED'].includes(plan.status) ? 'filled' : ''} /><i className={['GROWING', 'HARVESTED'].includes(plan.status) ? 'filled' : ''} /><b className={plan.status === 'HARVESTED' ? 'filled' : ''} /></div><div className="plan-date align-right"><span>{plan.status === 'HARVESTED' ? 'Harvested' : 'Expected harvest'}</span><strong>{formatDate(plan.actualHarvestDate || plan.expectedHarvestDate)}</strong></div></div>
        <div className="plan-card-bottom"><div><span>Planned yield</span><strong>{Number(plan.plannedQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</strong></div>{plan.actualHarvestQuantity != null && <><div><span>Actual yield</span><strong>{Number(plan.actualHarvestQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</strong></div><div><span>Discarded</span><strong>{Number(plan.discardedQuantity || 0).toLocaleString('en-IN')} {plan.quantityUnit}</strong></div></>}<div className="plan-actions">
          {plan.status === 'PLANNED' && <button className="text-button" onClick={() => updateStatus(plan, 'PLANTED')}>Mark planted</button>}
          {plan.status === 'PLANTED' && <button className="text-button" onClick={() => updateStatus(plan, 'GROWING')}>Mark growing</button>}
          {['PLANTED', 'GROWING'].includes(plan.status) && <button className="text-button" onClick={() => { setHarvestPlan(plan); setHarvestForm({ actualHarvestQuantity: '', actualHarvestDate: todayLocal(), soldQuantity: '0', storedQuantity: '0', discardedQuantity: '0' }); }}>Record harvest</button>}
          {plan.status === 'HARVESTED' && <><button className="text-button" onClick={() => { setDispositionPlan(plan); setDispositionForm({ soldQuantity: String(plan.soldQuantity || 0), storedQuantity: String(plan.storedQuantity || 0), discardedQuantity: String(plan.discardedQuantity || 0) }); }}>Update outcome</button><button className="text-button" onClick={() => viewDispositionHistory(plan)}>History</button></>}
          {['PLANNED', 'PLANTED', 'GROWING'].includes(plan.status) && <button className="text-button danger-text" onClick={() => updateStatus(plan, 'CANCELLED')}>Cancel</button>}
        </div></div>
      </article>)}</div> : <EmptyState title="Your first plan starts here" description="Add a crop plan to track planting dates and expected harvests." action={<button className="button button-primary" onClick={openCreate}>＋ Create a plan</button>} />}
    </>
  );
}
