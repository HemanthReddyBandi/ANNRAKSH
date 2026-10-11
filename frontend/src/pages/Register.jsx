import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { authService } from '../services/authService.js';
import { getErrorMessage } from '../services/api.js';

export default function Register() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '', location: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      login(await authService.register({ ...form, phone: form.phone || null, location: form.location || null }));
      navigate('/dashboard', { replace: true });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page register-page">
      <section className="auth-story">
        <Link className="brand-lockup auth-brand" to="/login"><span className="brand-mark">A</span><span><strong>ANNRAKSH</strong><small>Grow with the market</small></span></Link>
        <div className="story-content">
          <span className="story-kicker">A little more certainty</span>
          <h1>Make every<br />acre count.</h1>
          <p>Keep your crop plans connected to the people and markets they are meant to serve.</p>
          <div className="quote-card"><span>“</span><p>Good planning begins with listening to the land and the market.</p><small>ANNRAKSH FIELD NOTE</small></div>
        </div>
        <p className="story-foot">ANNRAKSH <span>·</span> Better planning, less waste</p>
      </section>
      <section className="auth-form-side">
        <div className="auth-form-wrap register-form-wrap">
          <p className="eyebrow">GET STARTED</p>
          <h2>Create your farmer account</h2>
          <p className="auth-subtitle">Your location helps us find demand data for your market.</p>
          {error && <div className="inline-alert" role="alert">{error}</div>}
          <form className="form-stack" onSubmit={handleSubmit}>
            <label className="form-field"><span>Full name</span><input autoComplete="name" required maxLength="120" placeholder="Your name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label>
            <label className="form-field"><span>Email address</span><input type="email" autoComplete="email" required maxLength="254" placeholder="you@example.com" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
            <div className="form-row">
              <label className="form-field"><span>Phone <small>Optional</small></span><input type="tel" autoComplete="tel" maxLength="30" placeholder="Phone number" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label>
              <label className="form-field"><span>Location <small>Optional</small></span><input autoComplete="address-level2" maxLength="160" placeholder="Town or district" value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} /></label>
            </div>
            <label className="form-field"><span>Password</span><input type="password" autoComplete="new-password" required minLength="8" maxLength="72" placeholder="At least 8 characters" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /><small className="field-hint">Use at least 8 characters.</small></label>
            <button className="button button-primary button-wide" type="submit" disabled={loading}>{loading ? 'Creating account…' : 'Create account'}<span aria-hidden="true">→</span></button>
          </form>
          <p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p>
          <p className="auth-legal">Registration creates a farmer account. Administrator access is assigned separately.</p>
        </div>
      </section>
    </div>
  );
}
