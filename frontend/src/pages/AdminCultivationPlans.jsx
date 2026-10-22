import { Fragment, useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { adminService } from '../services/adminService.js';
import { getErrorMessage } from '../services/api.js';
import { formatDate, titleCase } from '../utils/format.js';

const PAGE_SIZE = 20;
const statuses = ['', 'PLANNED', 'PLANTED', 'GROWING', 'HARVESTED', 'CANCELLED'];

export default function AdminCultivationPlans() {
  const [plans, setPlans] = useState([]);
  const [query, setQuery] = useState('');
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({ totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [historyPlanId, setHistoryPlanId] = useState(null);
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState('');

  async function load(requestedPage = page, requestedSearch = search, requestedStatus = status) {
    setLoading(true);
    setError('');
    try {
      const result = await adminService.cultivationPlans({
        page: requestedPage, size: PAGE_SIZE, search: requestedSearch, status: requestedStatus,
      });
      setPlans(result.plans);
      setPageInfo(result);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setSearch(query.trim());
      setPage(0);
    }, 250);
    return () => window.clearTimeout(timer);
  }, [query]);

  useEffect(() => { load(page, search, status); }, [page, search, status]);

  function changeStatus(value) {
    setStatus(value);
    setPage(0);
  }

  async function toggleHistory(plan) {
    if (historyPlanId === plan.id) {
      setHistoryPlanId(null);
      return;
    }
    setHistoryPlanId(plan.id);
    setHistory([]);
    setHistoryError('');
    setHistoryLoading(true);
    try {
      setHistory(await adminService.dispositionHistory(plan.id));
    } catch (requestError) {
      setHistoryError(getErrorMessage(requestError));
    } finally {
      setHistoryLoading(false);
    }
  }

  return (
    <>
      <PageHeader eyebrow="ADMIN WORKSPACE" title="Cultivation plans" description="Monitor planned and recorded crop production across farmer accounts." />
      {error ? <ErrorState message={error} onRetry={() => load(page, search, status)} /> : (
        <section className="content-panel">
          <div className="admin-plan-filters">
            <label className="form-field admin-search"><span>Search farmer or crop</span>
              <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Farmer name, email, or crop" />
            </label>
            <label className="form-field admin-status-filter"><span>Plan status</span>
              <select value={status} onChange={(event) => changeStatus(event.target.value)}>
                {statuses.map((value) => <option key={value} value={value}>{value ? titleCase(value) : 'All statuses'}</option>)}
              </select>
            </label>
            <span className="record-count">{pageInfo.totalElements} plans</span>
          </div>
          {loading ? <LoadingState label="Loading cultivation plans" /> : plans.length ? (
            <div className="table-wrap"><table className="data-table admin-plan-table">
              <thead><tr><th>Farmer</th><th>Crop</th><th>Status</th><th>Area</th><th>Planned</th><th>Actual harvest</th><th>Discarded</th><th>Planting</th><th>Expected harvest</th><th>History</th></tr></thead>
              <tbody>{plans.map((plan) => (
                <Fragment key={plan.id}>
                  <tr>
                    <td><strong>{plan.farmerName}</strong><small className="cell-sub">{plan.farmerEmail}</small></td>
                    <td>{plan.cropName}</td>
                    <td><span className={`status-badge status-${plan.status.toLowerCase()}`}>{titleCase(plan.status)}</span></td>
                    <td>{plan.areaHectares} ha</td>
                    <td>{Number(plan.plannedQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</td>
                    <td>{plan.actualHarvestQuantity == null ? '—' : `${Number(plan.actualHarvestQuantity).toLocaleString('en-IN')} ${plan.quantityUnit}`}</td>
                    <td>{plan.actualHarvestQuantity == null ? '—' : `${Number(plan.discardedQuantity || 0).toLocaleString('en-IN')} ${plan.quantityUnit}`}</td>
                    <td>{formatDate(plan.plantingDate)}</td>
                    <td>{formatDate(plan.actualHarvestDate || plan.expectedHarvestDate)}</td>
                    <td>{plan.actualHarvestQuantity == null ? '—' : <button className="text-button" disabled={historyLoading} onClick={() => toggleHistory(plan)}>{historyPlanId === plan.id ? 'Hide history' : 'View history'}</button>}</td>
                  </tr>
                  {historyPlanId === plan.id && <tr><td colSpan="10" className="audit-row">
                    {historyLoading ? <span>Loading audit history…</span> : historyError ? <span className="danger-text">{historyError}</span> : history.length ? (
                      <div className="table-wrap"><table className="data-table audit-table">
                        <thead><tr><th>Event</th><th>Sold, previous → current</th><th>Stored, previous → current</th><th>Discarded, previous → current</th><th>Recorded</th></tr></thead>
                        <tbody>{history.map((entry) => <tr key={entry.id}>
                          <td><strong>{titleCase(entry.action)}</strong></td>
                          <td>{Number(entry.previousSoldQuantity).toLocaleString('en-IN')} → {Number(entry.soldQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</td>
                          <td>{Number(entry.previousStoredQuantity).toLocaleString('en-IN')} → {Number(entry.storedQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</td>
                          <td>{Number(entry.previousDiscardedQuantity).toLocaleString('en-IN')} → {Number(entry.discardedQuantity).toLocaleString('en-IN')} {plan.quantityUnit}</td>
                          <td>{new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(entry.changedAt))}</td>
                        </tr>)}</tbody>
                      </table></div>
                    ) : <span>No disposition events for this plan.</span>}
                  </td></tr>}
                </Fragment>
              ))}</tbody>
            </table></div>
          ) : <EmptyState title={search || status ? 'No matching plans' : 'No cultivation plans yet'}
            description="Plans will appear here after farmers add them to ANNRAKSH." />}
          {!loading && pageInfo.totalPages > 1 && <div className="pagination-controls">
            <button className="button button-secondary button-small" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>Previous</button>
            <span>Page {page + 1} of {pageInfo.totalPages}</span>
            <button className="button button-secondary button-small" disabled={page + 1 >= pageInfo.totalPages} onClick={() => setPage((current) => current + 1)}>Next</button>
          </div>}
        </section>
      )}
    </>
  );
}
