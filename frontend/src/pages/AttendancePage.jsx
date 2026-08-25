import React, { useEffect, useState } from 'react';
import { Plus, Search, LogIn, LogOut, Trash2, Edit, Clock, Calendar } from 'lucide-react';
import { attendanceService } from '../services/attendanceService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { formatDate, formatTime, getErrorMessage } from '../utils/formatters';

const AttendancePage = () => {
  const { isAdmin } = useAuth();
  const [attendances, setAttendances] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [toast, setToast] = useState({ message: '', type: 'success' });

  // Punch Action State
  const [punchUserId, setPunchUserId] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  // Modal States
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    userId: '',
    departmentId: '',
    attendanceDate: new Date().toISOString().split('T')[0],
    checkInTime: '09:00',
    checkOutTime: '17:00',
    workingHours: 8,
    status: 'PRESENT',
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [attData, usersData] = await Promise.all([
        attendanceService.getAll(),
        isAdmin ? userService.getAll().catch(() => []) : Promise.resolve([]),
      ]);
      setAttendances(attData);
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

  const handleQuickCheckIn = async () => {
    if (!punchUserId) {
      setToast({ message: 'Please specify a User ID to Check In.', type: 'error' });
      return;
    }
    setActionLoading(true);
    try {
      await attendanceService.checkIn(punchUserId);
      setToast({ message: `Checked In user #${punchUserId} successfully!`, type: 'success' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleQuickCheckOut = async () => {
    if (!punchUserId) {
      setToast({ message: 'Please specify a User ID to Check Out.', type: 'error' });
      return;
    }
    setActionLoading(true);
    try {
      await attendanceService.checkOut(punchUserId);
      setToast({ message: `Checked Out user #${punchUserId} successfully!`, type: 'success' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const openCreateModal = () => {
    setSelectedRecord(null);
    setFormData({
      userId: users.length > 0 ? users[0].userId : '',
      departmentId: users.length > 0 && users[0].departmentId ? users[0].departmentId : '',
      attendanceDate: new Date().toISOString().split('T')[0],
      checkInTime: '09:00',
      checkOutTime: '17:00',
      workingHours: 8,
      status: 'PRESENT',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (record) => {
    setSelectedRecord(record);
    setFormData({
      userId: record.userId || '',
      departmentId: record.departmentId || '',
      attendanceDate: record.attendanceDate || new Date().toISOString().split('T')[0],
      checkInTime: record.checkInTime || '09:00',
      checkOutTime: record.checkOutTime || '17:00',
      workingHours: record.workingHours || 8,
      status: record.status || 'PRESENT',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      userId: Number(formData.userId),
      departmentId: formData.departmentId ? Number(formData.departmentId) : null,
      attendanceDate: formData.attendanceDate,
      checkInTime: formData.checkInTime.length === 5 ? `${formData.checkInTime}:00` : formData.checkInTime,
      checkOutTime: formData.checkOutTime.length === 5 ? `${formData.checkOutTime}:00` : formData.checkOutTime,
      workingHours: Number(formData.workingHours),
      status: formData.status,
    };

    try {
      if (selectedRecord) {
        await attendanceService.update(selectedRecord.attendanceId || selectedRecord.id, payload);
        setToast({ message: 'Attendance record updated successfully!', type: 'success' });
      } else {
        await attendanceService.create(payload);
        setToast({ message: 'Attendance record logged successfully!', type: 'success' });
      }
      setIsModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Are you sure you want to delete attendance record #${id}?`)) return;
    try {
      await attendanceService.delete(id);
      setToast({ message: 'Attendance record deleted.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const filteredAttendances = attendances.filter((att) => {
    const query = search.toLowerCase();
    const matchesUser = String(att.userId).includes(query) || (att.attendanceDate && att.attendanceDate.includes(query));
    const matchesStatus = statusFilter === 'ALL' || att.status === statusFilter;
    return matchesUser && matchesStatus;
  });

  if (loading) return <LoadingSpinner text="Fetching attendance records..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Attendance Logs & Punching</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Monitor check-ins, check-outs, working hours, and status histories.
          </p>
        </div>

        {isAdmin && (
          <button className="btn btn-primary" onClick={openCreateModal}>
            <Plus size={18} />
            <span>Manual Attendance Entry</span>
          </button>
        )}
      </div>

      {/* Quick Punch Bar */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flex: 1, minWidth: '220px' }}>
            <Clock size={20} style={{ color: 'var(--primary)' }} />
            <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Punch Puncher:</span>
            {users.length > 0 ? (
              <select
                className="form-select"
                style={{ flex: 1 }}
                value={punchUserId}
                onChange={(e) => setPunchUserId(e.target.value)}
              >
                <option value="">Select User for Punch</option>
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
                style={{ width: '160px' }}
                placeholder="User ID"
                value={punchUserId}
                onChange={(e) => setPunchUserId(e.target.value)}
              />
            )}
          </div>

          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button className="btn btn-success" onClick={handleQuickCheckIn} disabled={actionLoading}>
              <LogIn size={18} />
              <span>Check In</span>
            </button>
            <button className="btn btn-danger" onClick={handleQuickCheckOut} disabled={actionLoading}>
              <LogOut size={18} />
              <span>Check Out</span>
            </button>
          </div>
        </div>
      </div>

      {/* Filter and Table */}
      <div className="table-container">
        <div className="table-header-bar">
          <div className="search-box">
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search by User ID or Date (YYYY-MM-DD)..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Status:</span>
            <select
              className="form-select"
              style={{ width: '140px' }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">All Status</option>
              <option value="PRESENT">PRESENT</option>
              <option value="ABSENT">ABSENT</option>
              <option value="LATE">LATE</option>
              <option value="HALF_DAY">HALF_DAY</option>
            </select>
          </div>
        </div>

        {filteredAttendances.length === 0 ? (
          <EmptyState title="No Attendance Logs" description="No attendance entries match your search criteria." />
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>Record ID</th>
                <th>User ID</th>
                <th>Date</th>
                <th>Check-In</th>
                <th>Check-Out</th>
                <th>Working Hours</th>
                <th>Status</th>
                {isAdmin && <th style={{ textAlign: 'right' }}>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {filteredAttendances.map((record) => {
                const id = record.attendanceId || record.id;
                return (
                  <tr key={id}>
                    <td style={{ fontWeight: 700 }}>#{id}</td>
                    <td>User #{record.userId}</td>
                    <td>{formatDate(record.attendanceDate)}</td>
                    <td>{formatTime(record.checkInTime)}</td>
                    <td>{formatTime(record.checkOutTime)}</td>
                    <td>{record.workingHours !== null ? `${record.workingHours} hrs` : '--'}</td>
                    <td>
                      <StatusBadge status={record.status} />
                    </td>
                    {isAdmin && (
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(record)}>
                            <Edit size={15} />
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(id)}>
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Manual Entry Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedRecord ? 'Edit Attendance Record' : 'Manual Attendance Entry'}>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">User ID *</label>
              {users.length > 0 ? (
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
                  value={formData.userId}
                  onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
                />
              )}
            </div>
            <div className="form-group">
              <label className="form-label">Attendance Date *</label>
              <input
                type="date"
                className="form-input"
                required
                value={formData.attendanceDate}
                onChange={(e) => setFormData({ ...formData, attendanceDate: e.target.value })}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Check-In Time</label>
              <input
                type="time"
                step="1"
                className="form-input"
                value={formData.checkInTime}
                onChange={(e) => setFormData({ ...formData, checkInTime: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Check-Out Time</label>
              <input
                type="time"
                step="1"
                className="form-input"
                value={formData.checkOutTime}
                onChange={(e) => setFormData({ ...formData, checkOutTime: e.target.value })}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Working Hours</label>
              <input
                type="number"
                step="0.5"
                className="form-input"
                value={formData.workingHours}
                onChange={(e) => setFormData({ ...formData, workingHours: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Status *</label>
              <select
                className="form-select"
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              >
                <option value="PRESENT">PRESENT</option>
                <option value="ABSENT">ABSENT</option>
                <option value="LATE">LATE</option>
                <option value="HALF_DAY">HALF_DAY</option>
              </select>
            </div>
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : selectedRecord ? 'Update Record' : 'Save Attendance'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default AttendancePage;
