import { useEffect, useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/Feedback.jsx';
import { adminService } from '../services/adminService.js';
import { getErrorMessage } from '../services/api.js';
import { formatDateTime } from '../utils/format.js';

export default function AdminFarmers() {
  const [farmers, setFarmers] = useState([]);
  const [query, setQuery] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({ size: 20, totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadFarmers(requestedPage = page, requestedSearch = search) {
    setLoading(true);
    setError('');
    try {
      const result = await adminService.farmers({ page: requestedPage, size: 20, search: requestedSearch });
      setFarmers(result.farmers);
      setPageInfo(result);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const normalizedSearch = query.trim();
      setSearch(normalizedSearch);
      setPage(0);
    }, 250);
    return () => window.clearTimeout(timer);
  }, [query]);

  useEffect(() => {
    loadFarmers(page, search);
  }, [page, search]);

  return (
    <>
      <PageHeader eyebrow="ADMIN WORKSPACE" title="Farmers" description="Review registered farmer profiles and their account locations." />
      {loading ? <LoadingState label="Loading farmer accounts" /> : error ? <ErrorState message={error} onRetry={loadFarmers} /> : (
        <section className="content-panel">
          <div className="filter-bar">
            <label className="form-field admin-search"><span>Search farmer accounts</span>
              <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Name, email, phone, or location" />
            </label>
            <span className="record-count">{pageInfo.totalElements} farmers · page {pageInfo.totalPages ? page + 1 : 0} of {pageInfo.totalPages}</span>
          </div>
          {farmers.length ? (
            <div className="table-wrap"><table className="data-table">
              <thead><tr><th>Farmer</th><th>Email</th><th>Phone</th><th>Location</th><th>Joined</th></tr></thead>
              <tbody>{farmers.map((farmer) => (
                <tr key={farmer.id}>
                  <td><strong>{farmer.name}</strong></td><td>{farmer.email}</td><td>{farmer.phone || '—'}</td>
                  <td>{farmer.location || '—'}</td><td>{formatDateTime(farmer.createdAt)}</td>
                </tr>
              ))}</tbody>
            </table></div>
          ) : <EmptyState title={search ? 'No matching farmers' : 'No farmer accounts yet'}
            description={search ? 'Try another name, email, phone number, or location.' : 'Farmer registrations will appear here.'} />}
          {pageInfo.totalPages > 1 && <div className="pagination-controls">
            <button className="button button-secondary button-small" disabled={page === 0 || loading} onClick={() => setPage((current) => current - 1)}>Previous</button>
            <span>Page {page + 1} of {pageInfo.totalPages}</span>
            <button className="button button-secondary button-small" disabled={page + 1 >= pageInfo.totalPages || loading} onClick={() => setPage((current) => current + 1)}>Next</button>
          </div>}
        </section>
      )}
    </>
  );
}
