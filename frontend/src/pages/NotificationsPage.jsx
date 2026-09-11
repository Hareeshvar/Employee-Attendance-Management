import React, { useEffect, useState } from 'react';
import { Plus, Search, Trash2, CheckCircle2 } from 'lucide-react';
import { notificationService } from '../services/notificationService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import Pagination from '../components/Pagination';
import { getErrorMessage } from '../utils/formatters';

const NotificationsPage = () => {
  const { isAdmin } = useAuth();
  const [notifications, setNotifications] = useState([]);
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
  const [isReadFilter, setIsReadFilter] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    userId: '',
    title: '',
    message: '',
    isRead: false,
  });

  useEffect(() => {
    if (isAdmin) {
      userService.getAll({ page: 0, size: 1000 })
        .then((res) => setUsers(Array.isArray(res) ? res : res.content || []))
        .catch(() => setUsers([]));
    }
  }, [isAdmin]);

  const loadNotifications = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size: pageSize,
        search: search || undefined,
        isRead: isReadFilter === 'true' ? true : isReadFilter === 'false' ? false : undefined,
      };

      const res = await notificationService.getAll(params);
      if (res && res.content !== undefined) {
        setNotifications(res.content);
        setPaginationInfo({
          pageNumber: res.pageNumber,
          pageSize: res.pageSize,
          totalElements: res.totalElements,
          totalPages: res.totalPages,
          first: res.first,
          last: res.last,
        });
      } else if (Array.isArray(res)) {
        setNotifications(res);
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
    loadNotifications();
  }, [page, pageSize, search, isReadFilter]);

  const handleSearchChange = (val) => {
    setSearch(val);
    setPage(0);
  };

  const handleIsReadFilterChange = (val) => {
    setIsReadFilter(val);
    setPage(0);
  };

  const openModal = () => {
    setFormData({
      userId: users.length > 0 ? users[0].userId : '',
      title: '',
      message: '',
      isRead: false,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      userId: Number(formData.userId),
      title: formData.title,
      message: formData.message,
      isRead: formData.isRead,
    };

    try {
      await notificationService.create(payload);
      setToast({ message: 'Notification dispatched successfully!', type: 'success' });
      setIsModalOpen(false);
      loadNotifications();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleRead = async (notif) => {
    const id = notif.notificationId || notif.id;
    try {
      const payload = {
        userId: notif.userId,
        title: notif.title,
        message: notif.message,
        isRead: !notif.isRead,
      };
      await notificationService.update(id, payload);
      setToast({ message: 'Notification state updated.', type: 'info' });
      loadNotifications();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Delete notification #${id}?`)) return;
    try {
      await notificationService.delete(id);
      setToast({ message: 'Notification deleted.', type: 'info' });
      loadNotifications();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading && notifications.length === 0) return <LoadingSpinner text="Fetching system notifications..." />;
  if (error && notifications.length === 0) return <ErrorState message={error} onRetry={loadNotifications} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>System Notifications Center</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Announcements, alerts, and system notifications broadcasted to employees.
          </p>
        </div>

        {isAdmin && (
          <button className="btn btn-primary" onClick={openModal}>
            <Plus size={18} />
            <span>Send Notification</span>
          </button>
        )}
      </div>

      <div className="table-container">
        <div className="table-header-bar" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
          <div className="search-box" style={{ minWidth: '220px', flex: 1 }}>
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search by title or message content..."
              value={search}
              onChange={(e) => handleSearchChange(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <select
              className="form-select"
              style={{ width: '140px' }}
              value={isReadFilter}
              onChange={(e) => handleIsReadFilterChange(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="false">Unread Only</option>
              <option value="true">Read Only</option>
            </select>
          </div>
        </div>

        {notifications.length === 0 ? (
          <EmptyState title="No Notifications" description="There are no notifications matching your criteria." />
        ) : (
          <>
            <table className="custom-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Target User</th>
                  <th>Notification Title</th>
                  <th>Message Content</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {notifications.map((n) => {
                  const id = n.notificationId || n.id;
                  return (
                    <tr key={id}>
                      <td style={{ fontWeight: 700 }}>#{id}</td>
                      <td>User #{n.userId}</td>
                      <td style={{ fontWeight: 600 }}>{n.title}</td>
                      <td style={{ color: 'var(--text-secondary)' }}>{n.message}</td>
                      <td>
                        <StatusBadge status={n.isRead ? 'Read' : 'Unread'} />
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleToggleRead(n)} title="Toggle Read State">
                            <CheckCircle2 size={15} />
                          </button>
                          {isAdmin && (
                            <button className="btn btn-danger btn-sm" onClick={() => handleDelete(id)}>
                              <Trash2 size={15} />
                            </button>
                          )}
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

      {/* Notification Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Broadcast Notification">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Target User ID *</label>
            {users.length > 0 ? (
              <select
                className="form-select"
                required
                value={formData.userId}
                onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
              >
                <option value="">Select Employee</option>
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
                placeholder="User ID"
                value={formData.userId}
                onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
              />
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Notification Title *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="e.g. System Maintenance Notice"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Message Content *</label>
            <textarea
              className="form-textarea"
              rows={3}
              required
              placeholder="Write message details..."
              value={formData.message}
              onChange={(e) => setFormData({ ...formData, message: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Sending...' : 'Send Notification'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default NotificationsPage;
