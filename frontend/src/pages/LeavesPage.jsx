import React, { useEffect, useState } from 'react';
import { Plus, Check, X, Trash2, Calendar, Search } from 'lucide-react';
import { leaveService } from '../services/leaveService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import Pagination from '../components/Pagination';
import { formatDate, getErrorMessage } from '../utils/formatters';

const LeavesPage = () => {
  const { isAdmin, isHr, isManager, userId } = useAuth();
  const canApprove = isAdmin || isHr || isManager;

  const [leaves, setLeaves] = useState([]);
  const [paginationInfo, setPaginationInfo] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 1,
    first: true,
    last: true,
  });

  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [leaveTypeFilter, setLeaveTypeFilter] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    userId: userId || '',
    leaveType: 'CASUAL',
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    reason: '',
  });

  useEffect(() => {
    if (canApprove) {
      userService.getAll({ page: 0, size: 1000 })
        .then((res) => setUsers(Array.isArray(res) ? res : res.content || []))
        .catch(() => setUsers([]));
    }
  }, [canApprove]);

  const loadLeaves = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size: pageSize,
        search: search || undefined,
        status: statusFilter || undefined,
        leaveType: leaveTypeFilter || undefined,
      };

      const res = await leaveService.getAll(params);
      if (res && res.content !== undefined) {
        setLeaves(res.content);
        setPaginationInfo({
          pageNumber: res.pageNumber,
          pageSize: res.pageSize,
          totalElements: res.totalElements,
          totalPages: res.totalPages,
          first: res.first,
          last: res.last,
        });
      } else if (Array.isArray(res)) {
        setLeaves(res);
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
    loadLeaves();
  }, [page, pageSize, search, statusFilter, leaveTypeFilter]);

  const handleSearchChange = (val) => {
    setSearch(val);
    setPage(0);
  };

  const handleStatusFilterChange = (val) => {
    setStatusFilter(val);
    setPage(0);
  };

  const handleLeaveTypeFilterChange = (val) => {
    setLeaveTypeFilter(val);
    setPage(0);
  };

  const openApplyModal = () => {
    setFormData({
      userId: userId || (users.length > 0 ? users[0].userId : ''),
      leaveType: 'CASUAL',
      startDate: new Date().toISOString().split('T')[0],
      endDate: new Date().toISOString().split('T')[0],
      reason: '',
    });
    setIsModalOpen(true);
  };

  const handleApply = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      userId: Number(formData.userId),
      leaveType: formData.leaveType,
      startDate: formData.startDate,
      endDate: formData.endDate,
      reason: formData.reason,
    };

    try {
      await leaveService.apply(payload);
      setToast({ message: 'Leave application submitted successfully!', type: 'success' });
      setIsModalOpen(false);
      loadLeaves();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleApprove = async (id) => {
    try {
      await leaveService.approve(id);
      setToast({ message: `Leave request #${id} approved!`, type: 'success' });
      loadLeaves();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleReject = async (id) => {
    try {
      await leaveService.reject(id);
      setToast({ message: `Leave request #${id} rejected.`, type: 'info' });
      loadLeaves();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Are you sure you want to delete leave application #${id}?`)) return;
    try {
      await leaveService.delete(id);
      setToast({ message: 'Leave application deleted.', type: 'info' });
      loadLeaves();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading && leaves.length === 0) return <LoadingSpinner text="Fetching leave applications..." />;
  if (error && leaves.length === 0) return <ErrorState message={error} onRetry={loadLeaves} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Leave Requests & Approvals</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Apply for leave or manage employee time-off approval workflows.
          </p>
        </div>

        <button className="btn btn-primary" onClick={openApplyModal}>
          <Plus size={18} />
          <span>Apply For Leave</span>
        </button>
      </div>

      <div className="table-container">
        <div className="table-header-bar" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
          <div className="search-box" style={{ minWidth: '220px', flex: 1 }}>
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search user name or reason..."
              value={search}
              onChange={(e) => handleSearchChange(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center', width: '100%' }}>
            <select
              className="form-select"
              style={{ flex: '1 1 130px', minWidth: '120px' }}
              value={statusFilter}
              onChange={(e) => handleStatusFilterChange(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="PENDING">PENDING</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
            </select>

            <select
              className="form-select"
              style={{ flex: '1 1 130px', minWidth: '120px' }}
              value={leaveTypeFilter}
              onChange={(e) => handleLeaveTypeFilterChange(e.target.value)}
            >
              <option value="">All Types</option>
              <option value="CASUAL">CASUAL</option>
              <option value="SICK">SICK</option>
              <option value="ANNUAL">ANNUAL</option>
              <option value="EARNED">EARNED</option>
              <option value="UNPAID">UNPAID</option>
              <option value="MATERNITY">MATERNITY</option>
              <option value="PATERNITY">PATERNITY</option>
            </select>
          </div>
        </div>

        {leaves.length === 0 ? (
          <EmptyState
            title="No Leave Applications"
            description="There are currently no leave requests matching your criteria."
            action={
              <button className="btn btn-primary btn-sm" onClick={openApplyModal}>
                Apply For Leave
              </button>
            }
          />
        ) : (
          <>
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Leave ID</th>
                  <th>Employee Name</th>
                  <th>Leave Type</th>
                  <th>Start Date</th>
                  <th>End Date</th>
                  <th>Duration</th>
                  <th>Reason</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {leaves.map((item) => {
                  const id = item.leaveId || item.id;
                  const status = item.status || item.leaveStatus || 'PENDING';
                  const isPending = String(status).toUpperCase() === 'PENDING';
                  const days = item.totalDays || (item.startDate && item.endDate ? (Math.max(Math.ceil((new Date(item.endDate) - new Date(item.startDate)) / (1000 * 60 * 60 * 24)) + 1, 1)) : 1);

                  return (
                    <tr key={id}>
                      <td style={{ fontWeight: 700 }}>#{id}</td>
                      <td>
                        <div style={{ fontWeight: 600 }}>
                          {item.userFirstName ? `${item.userFirstName} ${item.userLastName}` : `User #${item.userId}`}
                        </div>
                      </td>
                      <td>
                        <span className="badge badge-info">{item.leaveType}</span>
                      </td>
                      <td>{formatDate(item.startDate)}</td>
                      <td>{formatDate(item.endDate)}</td>
                      <td>{days} {days === 1 ? 'day' : 'days'}</td>
                      <td style={{ maxWidth: '240px', color: 'var(--text-secondary)' }}>
                        {item.reason || 'No reason provided'}
                      </td>
                      <td>
                        <StatusBadge status={status} />
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          {canApprove && isPending && (
                            <>
                              <button
                                className="btn btn-success btn-sm"
                                onClick={() => handleApprove(id)}
                                title="Approve Leave"
                              >
                                <Check size={16} />
                              </button>
                              <button
                                className="btn btn-danger btn-sm"
                                onClick={() => handleReject(id)}
                                title="Reject Leave"
                              >
                                <X size={16} />
                              </button>
                            </>
                          )}
                          <button className="btn btn-secondary btn-sm" onClick={() => handleDelete(id)} title="Delete Application">
                            <Trash2 size={15} />
                          </button>
                        </div>
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

      {/* Apply Leave Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Apply For Leave">
        <form onSubmit={handleApply}>
          <div className="form-group">
            <label className="form-label">User ID *</label>
            {canApprove && users.length > 0 ? (
              <select
                className="form-select"
                required
                value={formData.userId}
                onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
              >
                <option value="">Select User</option>
                {users.map((u) => (
                  <option key={u.userId} value={u.userId}>
                    {u.firstName} {u.lastName} (#{u.userId})
                  </option>
                ))}
              </select>
            ) : (
              <input
                type="number"
                className="form-input"
                required
                placeholder="Your User ID"
                value={formData.userId}
                onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
              />
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Leave Type *</label>
            <select
              className="form-select"
              required
              value={formData.leaveType}
              onChange={(e) => setFormData({ ...formData, leaveType: e.target.value })}
            >
              <option value="CASUAL">CASUAL</option>
              <option value="SICK">SICK</option>
              <option value="ANNUAL">ANNUAL</option>
              <option value="EARNED">EARNED</option>
              <option value="UNPAID">UNPAID</option>
              <option value="MATERNITY">MATERNITY</option>
              <option value="PATERNITY">PATERNITY</option>
            </select>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Start Date *</label>
              <input
                type="date"
                className="form-input"
                required
                value={formData.startDate}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">End Date *</label>
              <input
                type="date"
                className="form-input"
                required
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Reason for Leave</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Provide context or explanation for leave..."
              value={formData.reason}
              onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Application'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default LeavesPage;
