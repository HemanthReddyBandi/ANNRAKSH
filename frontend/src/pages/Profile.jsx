import { useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { ErrorState, LoadingState } from '../components/Feedback.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { getErrorMessage } from '../services/api.js';
import { profileService } from '../services/profileService.js';

function toForm(profile) {
  return {
    name: profile?.name || '',
    email: profile?.email || '',
    phone: profile?.phone || '',
    location: profile?.location || '',
  };
}

export default function Profile() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState(() => toForm(user));
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const profile = await profileService.get();
      setForm(toForm(profile));
      updateUser(profile);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    try {
      const profile = await profileService.update({
        name: form.name,
        phone: form.phone,
        location: form.location,
      });
      setForm(toForm(profile));
      updateUser(profile);
      setNotice('Your profile has been updated. Recommendations will use this location.');
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  return <>
    <PageHeader eyebrow="FARMER ACCOUNT" title="Your profile"
      description="Keep your contact details and market location current. Recommendations use your location to find matching demand data." />
    {loading ? <LoadingState label="Loading your profile" /> : error && !form.email ? <ErrorState message={error} onRetry={load} /> : (
      <section className="content-panel profile-panel">
        {error && <div className="error-alert" role="alert">{error}</div>}
        {notice && <div className="success-alert" role="status">{notice}</div>}
        <form className="data-form" onSubmit={submit}>
          <label className="form-field"><span>Full name</span><input required maxLength="120" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label>
          <label className="form-field"><span>Email address</span><input type="email" value={form.email} readOnly /><small className="field-hint">Email is used to sign in and cannot be changed here.</small></label>
          <label className="form-field"><span>Phone <small>Optional</small></span><input type="tel" maxLength="30" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label>
          <label className="form-field"><span>Market location</span><input required maxLength="160" placeholder="Town or district" value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} /><small className="field-hint">Use the same location name as the demand records in ANNRAKSH.</small></label>
          <p className="form-note">Account type: {user?.role === 'ADMIN' ? 'Administrator' : 'Farmer'}. Roles and passwords are managed separately.</p>
          <div className="form-actions"><button className="button button-primary" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save profile'}</button></div>
        </form>
      </section>
    )}
  </>;
}
