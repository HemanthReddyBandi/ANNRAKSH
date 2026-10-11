import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const farmerLinks = [
  { to: '/dashboard', label: 'Overview', icon: '▦' },
  { to: '/crops', label: 'Crop library', icon: '◒' },
  { to: '/demand', label: 'Market demand', icon: '↗' },
  { to: '/plans', label: 'Cultivation plans', icon: '▤' },
  { to: '/recommendations', label: 'Recommendations', icon: '✳' },
];

const adminLinks = [
  { to: '/dashboard', label: 'Overview', icon: '▦' },
  { to: '/crops', label: 'Crop library', icon: '◒' },
  { to: '/demand', label: 'Market demand', icon: '↗' },
];

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const links = user?.role === 'ADMIN' ? adminLinks : farmerLinks;
  const initials = user?.name?.split(' ').map((part) => part[0]).slice(0, 2).join('').toUpperCase() || 'A';

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="app-shell">
      <button className={`sidebar-scrim ${menuOpen ? 'visible' : ''}`} aria-label="Close menu" onClick={() => setMenuOpen(false)} />
      <aside className={`sidebar ${menuOpen ? 'sidebar-open' : ''}`}>
        <NavLink to="/dashboard" className="brand-lockup" onClick={() => setMenuOpen(false)}>
          <span className="brand-mark">A</span>
          <span><strong>ANNRAKSH</strong><small>Grow with the market</small></span>
        </NavLink>
        <div className="nav-caption">WORKSPACE</div>
        <nav className="main-nav" aria-label="Main navigation">
          {links.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/dashboard'}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              onClick={() => setMenuOpen(false)}>
              <span className="nav-icon" aria-hidden="true">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-tip"><span className="tip-sun">✳</span><strong>Plan ahead</strong><p>Use local demand and past harvests to make your next crop decision.</p></div>
          <button className="logout-button" onClick={handleLogout}><span>↪</span> Sign out</button>
        </div>
      </aside>

      <section className="main-shell">
        <header className="topbar">
          <button className="mobile-menu-button" aria-label="Open navigation" onClick={() => setMenuOpen(true)}>☰</button>
          <div className="topbar-context"><span className="status-dot" /> Market-aware planning</div>
          <div className="account-chip">
            <span className="avatar">{initials}</span>
            <span className="account-copy"><strong>{user?.name}</strong><small>{user?.role === 'ADMIN' ? 'Administrator' : user?.location || 'Farmer account'}</small></span>
          </div>
        </header>
        <main className="page-container"><Outlet /></main>
      </section>
    </div>
  );
}
