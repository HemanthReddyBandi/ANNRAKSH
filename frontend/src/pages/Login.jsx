import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { authService } from '../services/authService.js';
import { getErrorMessage } from '../services/api.js';

export default function Login() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      login(await authService.login(form));
      navigate('/dashboard', { replace: true });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-story">
        <Link className="brand-lockup auth-brand" to="/login">
          <span className="brand-mark">A</span><span><strong>ANNRAKSH</strong><small>Grow with the market</small></span>
        </Link>
        <div className="story-content">
          <span className="story-kicker">A steadier harvest starts here</span>
          <h1>Plan for what<br />people need.</h1>
          <p>Bring local demand, growing seasons, and your own harvest history into one clear crop plan.</p>
          <div className="field-note"><span>✳</span><div><strong>Grounded in your market</strong><small>Recommendations explain the signals behind every score.</small></div></div>
        </div>
        <p className="story-foot">ANNRAKSH <span>·</span> Better planning, less waste</p>
      </section>
      <section className="auth-form-side">
        <div className="auth-form-wrap">
          <p className="eyebrow">WELCOME BACK</p>
          <h2>Sign in to your account</h2>
          <p className="auth-subtitle">Pick up where your next growing season begins.</p>
          {error && <div className="inline-alert" role="alert">{error}</div>}
          <form className="form-stack" onSubmit={handleSubmit}>
            <label className="form-field"><span>Email address</span><input type="email" autoComplete="email" required maxLength="254" placeholder="you@example.com" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
            <label className="form-field"><span>Password</span><input type="password" autoComplete="current-password" required maxLength="72" placeholder="Enter your password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
            <button className="button button-primary button-wide" type="submit" disabled={loading}>{loading ? 'Signing in…' : 'Sign in'}<span aria-hidden="true">→</span></button>
          </form>
          <p className="auth-switch">New to ANNRAKSH? <Link to="/register">Create an account</Link></p>
          <p className="auth-legal">By continuing, you agree to use recommendations as planning guidance alongside local agricultural advice.</p>
        </div>
      </section>
    </div>
  );
}
