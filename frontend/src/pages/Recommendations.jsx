import { useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { getErrorMessage } from '../services/api.js';
import { recommendationService } from '../services/recommendationService.js';
import { formatDate, formatDateTime, titleCase } from '../utils/format.js';

export default function Recommendations() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try { setItems(await recommendationService.list()); }
    catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  async function generate() {
    setGenerating(true);
    setError('');
    setNotice('');
    try {
      const generated = await recommendationService.generate();
      setNotice(`${generated.length} explainable recommendation${generated.length === 1 ? '' : 's'} generated from your location’s demand and harvest history.`);
      await load();
    } catch (requestError) { setError(getErrorMessage(requestError)); }
    finally { setGenerating(false); }
  }

  return <>
    <PageHeader eyebrow="DECISION SUPPORT" title="Crop recommendations" description="A transparent scoring guide that combines local demand, supply, seasonal fit, and your recorded harvest history."
      action={<button className="button button-primary" onClick={generate} disabled={generating}>{generating ? 'Calculating…' : '✳ Generate recommendations'}</button>} />
    {error && <ErrorState message={error} onRetry={load} />}
    {notice && <div className="success-alert" role="status">✓ &nbsp;{notice}</div>}
    <div className="recommendation-note"><span className="note-icon">i</span><span><strong>How scoring works</strong><small>Scores are rule-based decision support, not guaranteed yield predictions. Each factor is shown below so you can judge the recommendation against local conditions.</small></span></div>
    {loading ? <LoadingState label="Loading recommendations" /> : items.length ? <div className="recommendation-grid">{items.map((item) => <RecommendationCard key={item.id} item={item} />)}</div>
      : <EmptyState title="No recommendations saved yet" description="Recommendations need crop records and demand snapshots for your location. Generate a fresh set when that data is ready." action={<button className="button button-primary" onClick={generate} disabled={generating}>{generating ? 'Calculating…' : '✳ Generate recommendations'}</button>} />}
  </>;
}

function RecommendationCard({ item }) {
  const factors = [
    ['Market', item.marketScore],
    ['Season', item.seasonScore],
    ['Your harvest history', item.historicalPerformanceScore],
  ];
  const decision = String(item.decision || '').toLowerCase().replaceAll('_', '-');
  return <article className="recommendation-card">
    <div className="recommendation-card-head"><div className="crop-avatar large">{item.cropName?.slice(0, 1) || 'C'}</div><div className="recommendation-title"><span className="eyebrow">{titleCase(item.demandLevel)} DEMAND</span><h2>{item.cropName}</h2></div><span className={`decision-badge decision-${decision}`}>{titleCase(item.decision)}</span></div>
    <div className="recommendation-score"><div className="score-ring" style={{ '--score': `${Math.max(0, Math.min(100, Number(item.score) || 0))}%` }}><span>{item.score}<small>/100</small></span></div><div><strong>Recommendation score</strong><small>{item.reason}</small></div></div>
    <div className="factor-list">{factors.map(([label, score]) => <div className="factor-row" key={label}><div><span>{label}</span><strong>{score ?? '—'}{score != null && <small>/100</small>}</strong></div><div className="factor-track"><span style={{ width: `${Math.max(0, Math.min(100, Number(score) || 0))}%` }} /></div></div>)}</div>
    <div className="recommendation-meta"><span>Demand data <strong>{formatDate(item.demandRecordedDate)}</strong></span><span>Generated <strong>{formatDateTime(item.createdAt)}</strong></span></div>
  </article>;
}
