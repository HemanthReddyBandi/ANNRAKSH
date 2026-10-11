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
  const [harvestForm, setHarvestForm] = useState({ actualHarvestQuantity: '', actualHarvestDate: todayLocal() });
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
      await planService.recordHarvest(harvestPlan.id, { actualHarvestQuantity: Number(harvestForm.actualHarvestQuantity), actualHarvestDate: harvestForm.actualHarvestDate });
      setNotice(`Harvest for ${harvestPlan.cropName} recorded.`);
      setHarvestPlan(null);
      await load();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setSaving(false); }
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
      {harvestPlan && <section className="content-panel form-panel harvest-form-panel"><div className="panel-heading"><div><p className="eyebrow">HARVEST RECORD</p><h2>{harvestPlan.cropName}</h2></div><button className="icon-button" aria-label="Close harvest form" onClick={() => setHarvestPlan(null)}>×</button></div>
        <form className="data-form" onSubmit={recordHarvest}><label className="form-field"><span>Actual harvested quantity ({harvestPlan.quantityUnit})</span><input type="number" min="0.001" step="0.001" required value={harvestForm.actualHarvestQuantity} onChange={(event) => setHarvestForm({ ...harvestForm, actualHarvestQuantity: event.target.value })} /></label><label className="form-field"><span>Harvest date</span><input type="date" required min={harvestPlan.plantingDate} max={todayLocal()} value={harvestForm.actualHarvestDate} onChange={(event) => setHarvestForm({ ...harvestForm, actualHarvestDate: event.target.value })} /></label><p className="form-note">Recorded harvests improve future recommendations for this crop.</p><div className="form-actions"><button className="button button-secondary" type="button" onClick={() => setHarvestPlan(null)}>Cancel</button><button className="button button-primary" disabled={saving}>{saving ? 'Saving…' : 'Save harvest'}</button></div></form>
      </section>}
      {loading ? <LoadingState label="Loading your plans" /> : plans.length ? <div className="plan-list">{plans.map((plan) => <article className="plan-card" key={plan.id}>
        <div className="plan-card-top"><div className="plan-crop-mark">{plan.cropName?.slice(0, 1)}</div><div className="plan-title"><h2>{plan.cropName}</h2><span>{plan.areaHectares} ha · Plan #{plan.id}</span></div><span className={`status-badge status-${plan.status.toLowerCase()}`}>{titleCase(plan.status)}</span></div>
        <div className="plan-progress"><div className="plan-date"><span>Planted</span><strong>{formatDate(plan.plantingDate)}</strong></div><div className="plan-timeline"><span className={['PLANTED', 'GROWING', 'HARVESTED'].includes(plan.status) ? 'filled' : ''} /><i className={['GROWING', 'HARVESTED'].includes(plan.status) ? 'filled' : ''} /><b className={plan.status === 'HARVESTED' ? 'filled' : ''} /></div><div className="plan-date align-right"><span>{plan.status === 'HARVESTED' ? 'Harvested' : 'Expected harvest'}</span><strong>{formatDate(plan.actualHarvestDate || plan.expectedHarvestDate)}</strong></div></div>
        <div className="plan-card-bottom"><div><span>Planned yield</span><strong>{Number(plan.plannedQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</strong></div>{plan.actualHarvestQuantity != null && <div><span>Actual yield</span><strong>{Number(plan.actualHarvestQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</strong></div>}<div className="plan-actions">
          {plan.status === 'PLANNED' && <button className="text-button" onClick={() => updateStatus(plan, 'PLANTED')}>Mark planted</button>}
          {plan.status === 'PLANTED' && <button className="text-button" onClick={() => updateStatus(plan, 'GROWING')}>Mark growing</button>}
          {['PLANTED', 'GROWING'].includes(plan.status) && <button className="text-button" onClick={() => { setHarvestPlan(plan); setHarvestForm({ actualHarvestQuantity: '', actualHarvestDate: todayLocal() }); }}>Record harvest</button>}
          {['PLANNED', 'PLANTED', 'GROWING'].includes(plan.status) && <button className="text-button danger-text" onClick={() => updateStatus(plan, 'CANCELLED')}>Cancel</button>}
        </div></div>
      </article>)}</div> : <EmptyState title="Your first plan starts here" description="Add a crop plan to track planting dates and expected harvests." action={<button className="button button-primary" onClick={openCreate}>＋ Create a plan</button>} />}
    </>
  );
}
