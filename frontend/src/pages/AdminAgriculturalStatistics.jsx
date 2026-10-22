import { useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { adminService } from '../services/adminService.js';
import { getErrorMessage } from '../services/api.js';
import { formatDate } from '../utils/format.js';

export default function AdminAgriculturalStatistics() {
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      setStatistics(await adminService.agriculturalStatistics());
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <>
      <PageHeader eyebrow="ADMIN WORKSPACE" title="Agricultural statistics"
        description="Review the newest recorded market balances by crop and location, alongside recorded harvest output." />
      {loading ? <LoadingState label="Loading agricultural statistics" /> : error ? <ErrorState message={error} onRetry={load} /> : (
        <div className="dashboard-content">
          <div className="metric-grid">
            <StatCard label="Latest market snapshots" value={statistics.latestMarketSnapshotCount} tone="blue" />
            <StatCard label="Potential surplus signals" value={statistics.potentialSurplusSignalCount} tone="gold" />
            <StatCard label="Supply gap signals" value={statistics.supplyGapSignalCount} tone="sage" />
          </div>
          <div className="helper-banner">A potential surplus means the latest recorded supply exceeds demand for that crop and location. Discarded quantity below comes from farmer-reported harvest outcomes; it is recorded waste, not an independent measurement.</div>
          <section className="content-panel">
            <div className="panel-heading"><div><p className="eyebrow">CURRENT RECORDED BALANCE</p><h2>Latest market snapshots</h2></div></div>
            {statistics.latestMarketBalances.length ? <div className="table-wrap"><table className="data-table admin-stat-table">
              <thead><tr><th>Crop</th><th>Location</th><th>Demand</th><th>Supply</th><th>Supply minus demand</th><th>Signal</th><th>Recorded</th></tr></thead>
              <tbody>{statistics.latestMarketBalances.map((item) => (
                <tr key={`${item.cropId}-${item.location.toLowerCase()}`}>
                  <td><strong>{item.cropName}</strong></td><td>{item.location}</td>
                  <td>{Number(item.demandQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.supplyQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.supplyMinusDemand).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td><span className={`market-signal signal-${item.signal.toLowerCase()}`}>{signalLabel(item.signal)}</span></td>
                  <td>{formatDate(item.recordedDate)}</td>
                </tr>
              ))}</tbody>
            </table></div> : <EmptyState title="No demand snapshots yet" description="Once demand and supply records are added, their latest crop and location balances will appear here." />}
          </section>
          <section className="content-panel">
            <div className="panel-heading"><div><p className="eyebrow">RECORDED FARM OUTPUT</p><h2>Harvest production by crop and unit</h2></div></div>
            {statistics.harvestProductionByCropAndUnit.length ? <div className="table-wrap"><table className="data-table admin-stat-table">
              <thead><tr><th>Crop</th><th>Unit</th><th>Harvest records</th><th>Planned total</th><th>Harvested total</th><th>Sold</th><th>Stored</th><th>Discarded</th><th>Unallocated</th><th>Total area</th><th>Harvest per hectare</th></tr></thead>
              <tbody>{statistics.harvestProductionByCropAndUnit.map((item) => (
                <tr key={`${item.cropId}-${item.quantityUnit}`}>
                  <td><strong>{item.cropName}</strong></td><td>{item.quantityUnit}</td><td>{item.harvestCount}</td>
                  <td>{Number(item.totalPlannedQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.totalHarvestedQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.totalSoldQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.totalStoredQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.totalDiscardedQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.totalUnallocatedQuantity).toLocaleString('en-IN')} {item.quantityUnit}</td>
                  <td>{Number(item.totalAreaHectares).toLocaleString('en-IN')} ha</td>
                  <td>{Number(item.harvestedQuantityPerHectare).toLocaleString('en-IN')} {item.quantityUnit}/ha</td>
                </tr>
              ))}</tbody>
            </table></div> : <EmptyState title="No harvest data yet" description="Farmers’ recorded harvest quantities will be summarized here, grouped by crop and unit." />}
          </section>
        </div>
      )}
    </>
  );
}

function StatCard({ label, value, tone }) {
  return <article className="metric-card"><div className={`metric-icon ${tone}`}>↗</div><div className="metric-value">{value}</div><div className="metric-label">{label}</div></article>;
}

function signalLabel(signal) {
  return ({ POTENTIAL_SURPLUS: 'Potential surplus', SUPPLY_GAP: 'Supply gap', BALANCED: 'Balanced' })[signal] || signal;
}
