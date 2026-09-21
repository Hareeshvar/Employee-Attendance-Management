import React, { useState, useEffect, useCallback } from 'react';
import { auditLogService } from '../services/auditLogService';
import Pagination from '../components/Pagination';
import LoadingSpinner from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import { Search, Filter, RefreshCw, ShieldAlert, Eye, Calendar, User, FileText, Activity } from 'lucide-react';

const ACTION_OPTIONS = [
  'USER_CREATED', 'USER_UPDATED', 'USER_DELETED', 'ROLE_CHANGED', 'USER_STATUS_CHANGED',
  'DEPARTMENT_CREATED', 'DEPARTMENT_UPDATED', 'DEPARTMENT_DELETED',
  'DESIGNATION_CREATED', 'DESIGNATION_UPDATED', 'DESIGNATION_DELETED',
  'SHIFT_CREATED', 'SHIFT_UPDATED', 'SHIFT_DELETED', 'SHIFT_ASSIGNED',
  'ATTENDANCE_CHECK_IN', 'ATTENDANCE_CHECK_OUT', 'ATTENDANCE_UPDATED', 'ATTENDANCE_DELETED',
  'LEAVE_CREATED', 'LEAVE_UPDATED', 'LEAVE_CANCELLED', 'LEAVE_APPROVED', 'LEAVE_REJECTED',
  'PAYROLL_CREATED', 'PAYROLL_UPDATED', 'PAYROLL_DELETED',
  'REPORT_GENERATED',
  'LOGIN_SUCCESS', 'LOGIN_FAILED', 'ACCESS_DENIED'
];

const RESULT_OPTIONS = ['SUCCESS', 'FAILURE', 'DENIED'];
const ENTITY_OPTIONS = ['USER', 'DEPARTMENT', 'DESIGNATION', 'SHIFT', 'EMPLOYEE_SHIFT', 'ATTENDANCE', 'LEAVE_REQUEST', 'PAYROLL', 'REPORT'];

const AuditLogsPage = () => {
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters & Pagination state
  const [search, setSearch] = useState('');
  const [actionFilter, setActionFilter] = useState('');
  const [resultFilter, setResultFilter] = useState('');
  const [entityFilter, setEntityFilter] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [pageData, setPageData] = useState({
    totalElements: 0,
    totalPages: 1,
    first: true,
    last: true
  });

  // Modal State
  const [selectedLog, setSelectedLog] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchAuditLogs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size,
        sortBy: 'timestamp',
        sortDir: 'desc'
      };
      if (search.trim()) params.search = search.trim();
      if (actionFilter) params.action = actionFilter;
      if (resultFilter) params.result = resultFilter;
      if (entityFilter) params.entityType = entityFilter;

      const data = await auditLogService.getAuditLogs(params);
      setAuditLogs(data.content || []);
      setPageData({
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 1,
        first: data.first ?? true,
        last: data.last ?? true
      });
    } catch (err) {
      console.error("Error loading audit logs:", err);
      setError("Unable to load audit logs. Please ensure you have appropriate administrator permissions.");
    } finally {
      setLoading(false);
    }
  }, [page, size, search, actionFilter, resultFilter, entityFilter]);

  useEffect(() => {
    fetchAuditLogs();
  }, [fetchAuditLogs]);

  const handleSearchChange = (e) => {
    setSearch(e.target.value);
    setPage(0);
  };

  const handleActionChange = (e) => {
    setActionFilter(e.target.value);
    setPage(0);
  };

  const handleResultChange = (e) => {
    setResultFilter(e.target.value);
    setPage(0);
  };

  const handleEntityChange = (e) => {
    setEntityFilter(e.target.value);
    setPage(0);
  };

  const handleResetFilters = () => {
    setSearch('');
    setActionFilter('');
    setResultFilter('');
    setEntityFilter('');
    setPage(0);
  };

  const openDetailModal = (log) => {
    setSelectedLog(log);
    setIsModalOpen(true);
  };

  const renderResultBadge = (result) => {
    let bg = 'rgba(34, 197, 94, 0.15)';
    let color = '#4ade80';
    let border = 'rgba(34, 197, 94, 0.3)';

    if (result === 'FAILURE') {
      bg = 'rgba(239, 68, 68, 0.15)';
      color = '#f87171';
      border = 'rgba(239, 68, 68, 0.3)';
    } else if (result === 'DENIED') {
      bg = 'rgba(245, 158, 11, 0.15)';
      color = '#fbbf24';
      border = 'rgba(245, 158, 11, 0.3)';
    }

    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.25rem',
          padding: '0.2rem 0.6rem',
          borderRadius: '9999px',
          fontSize: '0.75rem',
          fontWeight: 600,
          backgroundColor: bg,
          color: color,
          border: `1px solid ${border}`
        }}
      >
        {result}
      </span>
    );
  };

  const formatTimestamp = (ts) => {
    if (!ts) return 'N/A';
    try {
      const d = new Date(ts);
      return d.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch {
      return ts;
    }
  };

  return (
    <div className="container" style={{ padding: '2rem 1rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
          <div style={{ padding: '0.6rem', background: 'rgba(59, 130, 246, 0.15)', borderRadius: 'var(--radius-md)', color: 'var(--primary-color)' }}>
            <Activity size={24} />
          </div>
          <div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: 0 }}>Enterprise Audit Trail</h1>
            <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '0.9rem' }}>
              Immutable record of system security, user mutations, and operational business events.
            </p>
          </div>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="card" style={{ padding: '1.25rem', marginBottom: '1.5rem', background: 'rgba(15, 23, 42, 0.6)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', alignItems: 'end' }}>
          {/* Keyword Search */}
          <div>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.35rem', display: 'block' }}>Search Keyword</label>
            <div style={{ position: 'relative' }}>
              <Search size={16} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }} />
              <input
                type="text"
                className="form-control"
                style={{ paddingLeft: '2.25rem', fontSize: '0.875rem' }}
                placeholder="Search actor, description..."
                value={search}
                onChange={handleSearchChange}
              />
            </div>
          </div>

          {/* Action Filter */}
          <div>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.35rem', display: 'block' }}>Action Type</label>
            <select className="form-select" style={{ fontSize: '0.875rem' }} value={actionFilter} onChange={handleActionChange}>
              <option value="">All Actions</option>
              {ACTION_OPTIONS.map((act) => (
                <option key={act} value={act}>{act}</option>
              ))}
            </select>
          </div>

          {/* Entity Filter */}
          <div>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.35rem', display: 'block' }}>Entity Type</label>
            <select className="form-select" style={{ fontSize: '0.875rem' }} value={entityFilter} onChange={handleEntityChange}>
              <option value="">All Entities</option>
              {ENTITY_OPTIONS.map((ent) => (
                <option key={ent} value={ent}>{ent}</option>
              ))}
            </select>
          </div>

          {/* Result Filter */}
          <div>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.35rem', display: 'block' }}>Result</label>
            <select className="form-select" style={{ fontSize: '0.875rem' }} value={resultFilter} onChange={handleResultChange}>
              <option value="">All Results</option>
              {RESULT_OPTIONS.map((res) => (
                <option key={res} value={res}>{res}</option>
              ))}
            </select>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="btn btn-outline" style={{ flex: 1, padding: '0.5rem' }} onClick={handleResetFilters} title="Reset Filters">
              <Filter size={16} /> Reset
            </button>
            <button className="btn btn-primary" style={{ padding: '0.5rem 0.75rem' }} onClick={fetchAuditLogs} title="Refresh Table">
              <RefreshCw size={16} />
            </button>
          </div>
        </div>
      </div>

      {/* Main Table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center' }}>
            <LoadingSpinner message="Loading audit logs from database..." />
          </div>
        ) : error ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#f87171' }}>
            <ShieldAlert size={48} style={{ marginBottom: '1rem', opacity: 0.8 }} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '0.5rem' }}>Access Denied or Server Error</h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>{error}</p>
          </div>
        ) : auditLogs.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
            <FileText size={48} style={{ marginBottom: '1rem', opacity: 0.4 }} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>No audit events found</h3>
            <p style={{ fontSize: '0.875rem' }}>Try clearing your filters or searching with a different term.</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto', WebkitOverflowScrolling: 'touch' }}>
            <table className="table" style={{ width: '100%', minWidth: '650px', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
              <thead>
                <tr style={{ background: 'rgba(15, 23, 42, 0.8)', borderBottom: '1px solid var(--border-color)', textAlign: 'left' }}>
                  <th style={{ padding: '0.85rem 1rem' }}>Timestamp</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Actor</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Action</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Target Entity</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Result</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Description</th>
                  <th style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>Details</th>
                </tr>
              </thead>
              <tbody>
                {auditLogs.map((log) => (
                  <tr key={log.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                    <td style={{ padding: '0.85rem 1rem', whiteSpace: 'nowrap', color: 'var(--text-secondary)' }}>
                      {formatTimestamp(log.timestamp)}
                    </td>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{log.actorUsername}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{log.actorRole}</div>
                    </td>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <span style={{ fontFamily: 'monospace', fontSize: '0.8rem', background: 'rgba(255, 255, 255, 0.08)', padding: '0.2rem 0.4rem', borderRadius: '4px' }}>
                        {log.action}
                      </span>
                    </td>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <span style={{ fontWeight: 500 }}>{log.entityType}</span>
                      {log.entityId && <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginLeft: '0.35rem' }}>#{log.entityId}</span>}
                    </td>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      {renderResultBadge(log.result)}
                    </td>
                    <td style={{ padding: '0.85rem 1rem', maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={log.description}>
                      {log.description}
                    </td>
                    <td style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>
                      <button
                        className="btn btn-outline"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}
                        onClick={() => openDetailModal(log)}
                      >
                        <Eye size={14} /> View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Phase 2 Pagination Component */}
        <Pagination
          pageNumber={page}
          pageSize={size}
          totalElements={pageData.totalElements}
          totalPages={pageData.totalPages}
          first={pageData.first}
          last={pageData.last}
          onPageChange={(newPage) => setPage(newPage)}
          onPageSizeChange={(newSize) => {
            setSize(newSize);
            setPage(0);
          }}
        />
      </div>

      {/* Audit Detail Modal */}
      {isModalOpen && selectedLog && (
        <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={`Audit Event #${selectedLog.id}`}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.9rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', background: 'rgba(15, 23, 42, 0.4)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'block' }}>ACTOR</span>
                <strong>{selectedLog.actorUsername}</strong> ({selectedLog.actorRole})
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'block' }}>TIMESTAMP</span>
                <strong>{formatTimestamp(selectedLog.timestamp)}</strong>
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'block' }}>ACTION</span>
                <span style={{ fontFamily: 'monospace', fontWeight: 600 }}>{selectedLog.action}</span>
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'block' }}>RESULT</span>
                {renderResultBadge(selectedLog.result)}
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'block' }}>ENTITY</span>
                <strong>{selectedLog.entityType}</strong> {selectedLog.entityId ? `#${selectedLog.entityId}` : ''}
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'block' }}>CLIENT IP</span>
                <span>{selectedLog.ipAddress || 'N/A'}</span>
              </div>
            </div>

            <div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '0.25rem' }}>DESCRIPTION</span>
              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', borderLeft: '3px solid var(--primary-color)' }}>
                {selectedLog.description}
              </div>
            </div>

            <div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '0.25rem' }}>USER AGENT</span>
              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '0.5rem', borderRadius: 'var(--radius-sm)', fontSize: '0.75rem', fontFamily: 'monospace', wordBreak: 'break-all', color: 'var(--text-secondary)' }}>
                {selectedLog.userAgent || 'UNKNOWN'}
              </div>
            </div>

            {selectedLog.metadata && (
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '0.25rem' }}>WHITELISTED METADATA</span>
                <pre style={{ background: 'rgba(0,0,0,0.4)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', fontSize: '0.8rem', color: '#4ade80', overflowX: 'auto' }}>
                  {(() => {
                    try {
                      return JSON.stringify(JSON.parse(selectedLog.metadata), null, 2);
                    } catch {
                      return selectedLog.metadata;
                    }
                  })()}
                </pre>
              </div>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
};

export default AuditLogsPage;
