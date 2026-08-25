import React, { useEffect, useState } from 'react';
import { Plus, Bell, Trash2, CheckCircle2 } from 'lucide-react';
import { notificationService } from '../services/notificationService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { getErrorMessage } from '../utils/formatters';

const NotificationsPage = () => {
  const { isAdmin } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [users, setUsers] = useState([]);

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

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [notifData, usersData] = await Promise.all([
        notificationService.getAll(),
        isAdmin ? userService.getAll().catch(() => []) : Promise.resolve([]),
      ]);
      setNotifications(notifData);
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
      loadData();
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
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Delete notification #${id}?`)) return;
    try {
      await notificationService.delete(id);
      setToast({ message: 'Notification deleted.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading) return <LoadingSpinner text="Fetching system notifications..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

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
        {notifications.length === 0 ? (
          <EmptyState title="No Notifications" description="There are no notifications in your inbox." />
        ) : (
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
