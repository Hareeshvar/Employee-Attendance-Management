import React, { useEffect, useState } from 'react';
import { Plus, Check, X, Trash2, Calendar, FileText } from 'lucide-react';
import { leaveService } from '../services/leaveService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { formatDate, getErrorMessage } from '../utils/formatters';

const LeavesPage = () => {
  const { isAdmin, userId } = useAuth();
  const [leaves, setLeaves] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
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

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [leavesData, usersData] = await Promise.all([
        leaveService.getAll(),
        isAdmin ? userService.getAll().catch(() => []) : Promise.resolve([]),
      ]);
      setLeaves(leavesData);
      setUsers(usersData);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [isAdmin]);

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
      loadData();
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
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleReject = async (id) => {
    try {
      await leaveService.reject(id);
      setToast({ message: `Leave request #${id} rejected.`, type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Are you sure you want to delete leave application #${id}?`)) return;
    try {
      await leaveService.delete(id);
      setToast({ message: 'Leave application deleted.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const filteredLeaves = leaves.filter((item) => {
    if (statusFilter === 'ALL') return true;
    return String(item.status || item.leaveStatus).toUpperCase() === statusFilter;
  });

  if (loading) return <LoadingSpinner text="Fetching leave applications..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

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
        <div className="table-header-bar">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Calendar size={18} style={{ color: 'var(--primary)' }} />
            <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Leave Applications Directory</h3>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Status:</span>
            <select
              className="form-select"
              style={{ width: '150px' }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">All Status</option>
              <option value="PENDING">PENDING</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
            </select>
          </div>
        </div>

        {filteredLeaves.length === 0 ? (
          <EmptyState
            title="No Leave Applications"
            description="There are currently no leave requests filed in the system."
            action={
              <button className="btn btn-primary btn-sm" onClick={openApplyModal}>
                Apply For Leave
              </button>
            }
          />
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>Leave ID</th>
                <th>User ID</th>
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
              {filteredLeaves.map((item) => {
                const id = item.leaveId || item.id;
                const status = item.status || item.leaveStatus || 'PENDING';
                const isPending = String(status).toUpperCase() === 'PENDING';
                const days = item.totalDays || (item.startDate && item.endDate ? (Math.max(Math.ceil((new Date(item.endDate) - new Date(item.startDate)) / (1000 * 60 * 60 * 24)) + 1, 1)) : 1);

                return (
                  <tr key={id}>
                    <td style={{ fontWeight: 700 }}>#{id}</td>
                    <td>User #{item.userId}</td>
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
                        {isAdmin && isPending && (
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
        )}
      </div>

      {/* Apply Leave Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Apply For Leave">
        <form onSubmit={handleApply}>
          <div className="form-group">
            <label className="form-label">User ID *</label>
            {isAdmin && users.length > 0 ? (
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
              <option value="EARNED">EARNED</option>
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
