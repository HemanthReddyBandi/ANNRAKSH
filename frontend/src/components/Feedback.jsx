export function LoadingState({ label = 'Loading your workspace' }) {
  return <div className="loading-state"><span className="spinner" /><span>{label}</span></div>;
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="feedback-card error-feedback">
      <span className="feedback-symbol">!</span>
      <div><strong>We couldn’t load this data</strong><p>{message}</p></div>
      {onRetry && <button className="button button-small button-secondary" onClick={onRetry}>Try again</button>}
    </div>
  );
}

export function EmptyState({ title, description, action }) {
  return (
    <div className="empty-state">
      <span className="empty-glyph">⌁</span>
      <h3>{title}</h3>
      <p>{description}</p>
      {action}
    </div>
  );
}
