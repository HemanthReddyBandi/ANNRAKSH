import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { cropService } from '../services/cropService.js';
import { demandService } from '../services/demandService.js';
import { planService } from '../services/planService.js';
import { recommendationService } from '../services/recommendationService.js';
import { getErrorMessage } from '../services/api.js';
import { formatDateTime, titleCase } from '../utils/format.js';

export default function Dashboard() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const result = isAdmin
        ? await Promise.all([cropService.list(), demandService.list()])
        : await Promise.all([planService.list(), recommendationService.list()]);
      setData(result);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [isAdmin]);

  const firstName = user?.name?.split(' ')[0] || 'there';
  return (
    <>
      <PageHeader eyebrow={isAdmin ? 'ADMIN WORKSPACE' : 'FARMER WORKSPACE'}
        title={isAdmin ? `Good day, ${firstName}` : `Welcome back, ${firstName}`}
        description={isAdmin ? 'Keep crop and market information current for your farming community.' : `Here is a clear view of your growing plans${user?.location ? ` around ${user.location}` : ''}.`} />
      {loading ? <LoadingState /> : error ? <ErrorState message={error} onRetry={load} /> : (
        isAdmin ? <AdminOverview crops={data?.[0] || []} demand={data?.[1] || []} />
          : <FarmerOverview plans={data?.[0] || []} recommendations={data?.[1] || []} user={user} />
      )}
    </>
  );
}

function FarmerOverview({ plans, recommendations, user }) {
  const growing = plans.filter((plan) => ['PLANTED', 'GROWING'].includes(plan.status));
  const harvested = plans.filter((plan) => plan.status === 'HARVESTED');
  const latest = recommendations.slice(0, 3);
  return (
    <div className="dashboard-content">
      <section className="welcome-banner">
        <div><span className="banner-kicker">YOUR FIELD, IN FOCUS</span><h2>Plan with the market in mind.</h2><p>Recommendations use the latest demand data for your location and your own recorded harvests.</p></div>
        <div className="banner-actions"><Link className="button button-light" to="/recommendations">View recommendations <span>→</span></Link><span className="banner-location">⌖ &nbsp;{user?.location || 'Add a location at registration'}</span></div>
        <span className="banner-orbit orbit-one" /><span className="banner-orbit orbit-two" />
      </section>
      <div className="metric-grid">
        <MetricCard label="Active growing plans" value={growing.length} note="Planted or growing" icon="↗" tone="sage" />
        <MetricCard label="Harvests recorded" value={harvested.length} note="Feeds your performance history" icon="✳" tone="gold" />
        <MetricCard label="Recommendations" value={recommendations.length} note="Saved for your account" icon="⌁" tone="blue" />
      </div>
      <section className="content-panel">
        <div className="panel-heading"><div><p className="eyebrow">LATEST INSIGHT</p><h2>Recommended for your market</h2></div><Link className="text-link" to="/recommendations">See all <span>→</span></Link></div>
        {latest.length ? <div className="insight-list">{latest.map((item) => <div className="insight-row" key={item.id}><div className="crop-avatar">{item.cropName?.slice(0, 1)}</div><div className="insight-main"><strong>{item.cropName}</strong><span>{titleCase(item.decision)} · demand data {formatDateTime(item.demandRecordedDate)}</span></div><div className="score-pill">{item.score}<small>/100</small></div></div>)}</div>
          : <EmptyState title="No recommendations yet" description="Generate your first recommendation once demand data is available for your location." action={<Link className="button button-secondary" to="/recommendations">Open recommendations</Link>} />}
      </section>
      <section className="quick-links-grid">
        <QuickLink to="/plans" title="Add a cultivation plan" text="Record crop, area, and expected harvest." icon="＋" />
        <QuickLink to="/demand" title="Check local demand" text="See what the market is asking for." icon="↗" />
      </section>
    </div>
  );
}

function AdminOverview({ crops, demand }) {
  const highDemand = demand.filter((record) => record.demandLevel === 'HIGH').length;
  const supplyGap = demand.filter((record) => Number(record.supplyQuantity) < Number(record.demandQuantity)).length;
  return (
    <div className="dashboard-content">
      <section className="welcome-banner admin-banner">
        <div><span className="banner-kicker">COMMUNITY MARKET</span><h2>Clear information supports better decisions.</h2><p>Maintain crop records and local demand snapshots for the farmers using ANNRAKSH.</p></div>
        <div className="banner-actions"><Link className="button button-light" to="/demand">Manage demand data <span>→</span></Link></div>
        <span className="banner-orbit orbit-one" /><span className="banner-orbit orbit-two" />
      </section>
      <div className="metric-grid">
        <MetricCard label="Crop varieties" value={crops.length} note="In the crop library" icon="◒" tone="sage" />
        <MetricCard label="Demand records" value={demand.length} note="Across all recorded locations" icon="↗" tone="blue" />
        <MetricCard label="High-demand signals" value={highDemand} note={`${supplyGap} records show supply below demand`} icon="✳" tone="gold" />
      </div>
      <section className="quick-links-grid">
        <QuickLink to="/crops" title="Maintain crop library" text="Add varieties, seasons, and growth duration." icon="◒" />
        <QuickLink to="/demand" title="Record local demand" text="Keep demand and supply snapshots current." icon="＋" />
      </section>
    </div>
  );
}

function MetricCard({ label, value, note, icon, tone }) {
  return <article className="metric-card"><div className={`metric-icon ${tone}`}>{icon}</div><div className="metric-value">{value}</div><div className="metric-label">{label}</div><div className="metric-note">{note}</div></article>;
}

function QuickLink({ to, title, text, icon }) {
  return <Link className="quick-link-card" to={to}><span className="quick-link-icon">{icon}</span><span><strong>{title}</strong><small>{text}</small></span><span className="quick-arrow">↗</span></Link>;
}
