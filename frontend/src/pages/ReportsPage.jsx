import React, { useEffect, useState } from 'react';
import { Plus, FileText, Trash2, Search } from 'lucide-react';
import { reportService } from '../services/reportService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import Pagination from '../components/Pagination';
import { formatDate, getErrorMessage } from '../utils/formatters';

const ReportsPage = () => {
  const { username } = useAuth();
  const [reports, setReports] = useState([]);
  const [paginationInfo, setPaginationInfo] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 1,
    first: true,
    last: true,
  });

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState('');
  const [reportTypeFilter, setReportTypeFilter] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    reportName: '',
    reportType: 'ATTENDANCE_SUMMARY',
    generatedBy: username || 'Admin',
    description: '',
  });

  const loadReports = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size: pageSize,
        search: search || undefined,
        reportType: reportTypeFilter || undefined,
      };

      const res = await reportService.getAll(params);
      if (res && res.content !== undefined) {
        setReports(res.content);
        setPaginationInfo({
          pageNumber: res.pageNumber,
          pageSize: res.pageSize,
          totalElements: res.totalElements,
          totalPages: res.totalPages,
          first: res.first,
          last: res.last,
        });
      } else if (Array.isArray(res)) {
        setReports(res);
        setPaginationInfo({
          pageNumber: 0,
          pageSize: res.length,
          totalElements: res.length,
          totalPages: 1,
          first: true,
          last: true,
        });
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReports();
  }, [page, pageSize, search, reportTypeFilter]);

  const handleSearchChange = (val) => {
    setSearch(val);
    setPage(0);
  };

  const handleReportTypeFilterChange = (val) => {
    setReportTypeFilter(val);
    setPage(0);
  };

  const openModal = () => {
    setFormData({
      reportName: '',
      reportType: 'ATTENDANCE_SUMMARY',
      generatedBy: username || 'Admin',
      description: '',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      reportName: formData.reportName,
      reportType: formData.reportType,
      generatedDate: new Date().toISOString(),
      generatedBy: formData.generatedBy,
      description: formData.description,
    };

    try {
      await reportService.create(payload);
      setToast({ message: 'System report generated successfully!', type: 'success' });
      setIsModalOpen(false);
      loadReports();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Delete report record #${id}?`)) return;
    try {
      await reportService.delete(id);
      setToast({ message: 'Report record deleted.', type: 'info' });
      loadReports();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading && reports.length === 0) return <LoadingSpinner text="Loading system reports..." />;
  if (error && reports.length === 0) return <ErrorState message={error} onRetry={loadReports} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Audit & HR System Reports</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Generate and archive attendance, leave, and payroll summary reports.
          </p>
        </div>

        <button className="btn btn-primary" onClick={openModal}>
          <Plus size={18} />
          <span>Generate New Report</span>
        </button>
      </div>

      <div className="table-container">
        <div className="table-header-bar" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
          <div className="search-box" style={{ minWidth: '220px', flex: 1 }}>
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search report name or description..."
              value={search}
              onChange={(e) => handleSearchChange(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <select
              className="form-select"
              style={{ width: '180px' }}
              value={reportTypeFilter}
              onChange={(e) => handleReportTypeFilterChange(e.target.value)}
            >
              <option value="">All Report Types</option>
              <option value="ATTENDANCE_SUMMARY">ATTENDANCE_SUMMARY</option>
              <option value="LEAVE_AUDIT">LEAVE_AUDIT</option>
              <option value="PAYROLL_SUMMARY">PAYROLL_SUMMARY</option>
              <option value="DEPARTMENT_ANALYTICS">DEPARTMENT_ANALYTICS</option>
              <option value="PERSONAL">PERSONAL</option>
              <option value="TEAM">TEAM</option>
              <option value="SYSTEM">SYSTEM</option>
            </select>
          </div>
        </div>

        {reports.length === 0 ? (
          <EmptyState title="No Generated Reports" description="No system reports match your search or filter criteria." />
        ) : (
          <>
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Report ID</th>
                  <th>Report Name</th>
                  <th>Type</th>
                  <th>Generated By</th>
                  <th>Date</th>
                  <th>Description</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {reports.map((r) => {
                  const id = r.reportId || r.id;
                  return (
                    <tr key={id}>
                      <td style={{ fontWeight: 700 }}>#{id}</td>
                      <td style={{ fontWeight: 600 }}>{r.reportName}</td>
                      <td>
                        <span className="badge badge-info">{r.reportType}</span>
                      </td>
                      <td>{r.generatedBy}</td>
                      <td>{formatDate(r.generatedDate)}</td>
                      <td style={{ color: 'var(--text-secondary)' }}>{r.description || 'N/A'}</td>
                      <td style={{ textAlign: 'right' }}>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(id)}>
                          <Trash2 size={15} />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            <Pagination
              pageNumber={paginationInfo.pageNumber}
              pageSize={paginationInfo.pageSize}
              totalElements={paginationInfo.totalElements}
              totalPages={paginationInfo.totalPages}
              first={paginationInfo.first}
              last={paginationInfo.last}
              onPageChange={(newPage) => setPage(newPage)}
              onPageSizeChange={(newSize) => {
                setPageSize(newSize);
                setPage(0);
              }}
            />
          </>
        )}
      </div>

      {/* Generate Report Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Generate HR System Report">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Report Name *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="e.g. Q3 Monthly Attendance Audit"
              value={formData.reportName}
              onChange={(e) => setFormData({ ...formData, reportName: e.target.value })}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Report Type *</label>
              <select
                className="form-select"
                required
                value={formData.reportType}
                onChange={(e) => setFormData({ ...formData, reportType: e.target.value })}
              >
                <option value="ATTENDANCE_SUMMARY">ATTENDANCE_SUMMARY</option>
                <option value="LEAVE_AUDIT">LEAVE_AUDIT</option>
                <option value="PAYROLL_SUMMARY">PAYROLL_SUMMARY</option>
                <option value="DEPARTMENT_ANALYTICS">DEPARTMENT_ANALYTICS</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Generated By *</label>
              <input
                type="text"
                className="form-input"
                required
                value={formData.generatedBy}
                onChange={(e) => setFormData({ ...formData, generatedBy: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Description / Parameters</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Audit scope, date range, or additional details..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Generating...' : 'Compile & Save Report'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ReportsPage;
