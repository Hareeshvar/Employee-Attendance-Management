import React, { useEffect, useState } from 'react';
import { Plus, Clock, UserCheck, Trash2, Edit, Calendar, AlertCircle } from 'lucide-react';
import { shiftService } from '../services/shiftService';
import { employeeShiftService } from '../services/employeeShiftService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { formatDate, formatTime, getErrorMessage } from '../utils/formatters';

const ShiftsPage = () => {
  const { isAdmin, isHr } = useAuth();
  const canManage = isAdmin || isHr;

  const [activeTab, setActiveTab] = useState('templates'); // 'templates' | 'assignments'
  const [shifts, setShifts] = useState([]);
  const [employeeShifts, setEmployeeShifts] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  // Modal States
  const [isShiftModalOpen, setIsShiftModalOpen] = useState(false);
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [selectedShift, setSelectedShift] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Forms
  const [shiftForm, setShiftForm] = useState({
    shiftName: '',
    startTime: '09:00',
    endTime: '17:00',
    workingHours: 8,
    graceMinutes: 15,
    description: '',
  });

  const [assignForm, setAssignForm] = useState({
    userId: '',
    shiftId: '',
    effectiveDate: new Date().toISOString().split('T')[0],
    status: 'ACTIVE',
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [shiftsData, empShiftsData, usersRes] = await Promise.all([
        shiftService.getAll(),
        employeeShiftService.getAll().catch(() => []),
        userService.getAll({ page: 0, size: 1000 }).catch(() => ({ content: [] })),
      ]);
      const shiftList = Array.isArray(shiftsData) ? shiftsData : [];
      const empShiftList = Array.isArray(empShiftsData) ? empShiftsData : [];
      const userList = usersRes?.content || (Array.isArray(usersRes) ? usersRes : []);

      setShifts(shiftList);
      setEmployeeShifts(empShiftList);
      setUsers(userList);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openShiftModal = (shift = null) => {
    setSelectedShift(shift);
    if (shift) {
      setShiftForm({
        shiftName: shift.shiftName || '',
        startTime: shift.startTime || '09:00',
        endTime: shift.endTime || '17:00',
        workingHours: shift.workingHours || 8,
        graceMinutes: shift.graceMinutes !== undefined ? shift.graceMinutes : 15,
        description: shift.description || '',
      });
    } else {
      setShiftForm({
        shiftName: '',
        startTime: '09:00',
        endTime: '17:00',
        workingHours: 8,
        graceMinutes: 15,
        description: '',
      });
    }
    setIsShiftModalOpen(true);
  };

  const openAssignModal = () => {
    setAssignForm({
      userId: users.length > 0 ? String(users[0].userId) : '',
      shiftId: shifts.length > 0 ? String(shifts[0].shiftId || shifts[0].id) : '',
      effectiveDate: new Date().toISOString().split('T')[0],
      status: 'ACTIVE',
    });
    setIsAssignModalOpen(true);
  };

  const handleShiftSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      shiftName: shiftForm.shiftName.trim(),
      startTime: shiftForm.startTime.length === 5 ? `${shiftForm.startTime}:00` : shiftForm.startTime,
      endTime: shiftForm.endTime.length === 5 ? `${shiftForm.endTime}:00` : shiftForm.endTime,
      workingHours: Number(shiftForm.workingHours),
      graceMinutes: Number(shiftForm.graceMinutes ?? 15),
      description: shiftForm.description,
    };

    try {
      if (selectedShift) {
        await shiftService.update(selectedShift.shiftId || selectedShift.id, payload);
        setToast({ message: 'Shift updated successfully!', type: 'success' });
      } else {
        await shiftService.create(payload);
        setToast({ message: 'Shift created successfully!', type: 'success' });
      }
      setIsShiftModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    if (!assignForm.userId || !assignForm.shiftId) {
      setToast({ message: 'Please select both an employee and a shift template.', type: 'error' });
      return;
    }
    setSubmitting(true);

    const payload = {
      userId: Number(assignForm.userId),
      shiftId: Number(assignForm.shiftId),
      effectiveDate: assignForm.effectiveDate,
      status: assignForm.status,
    };

    try {
      await employeeShiftService.assign(payload);
      setToast({ message: 'Shift assigned to employee successfully!', type: 'success' });
      setIsAssignModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteShift = async (id) => {
    if (!window.confirm(`Delete shift template #${id}?`)) return;
    try {
      await shiftService.delete(id);
      setToast({ message: 'Shift template deleted.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleDeleteAssignment = async (id) => {
    if (!window.confirm(`Remove shift assignment #${id}?`)) return;
    try {
      await employeeShiftService.delete(id);
      setToast({ message: 'Shift assignment removed.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading) return <LoadingSpinner text="Loading shifts & schedules..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Work Shift Management</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Define shift timings and assign work schedules to staff members.
          </p>
        </div>

        {canManage && (
          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
            <button className="btn btn-secondary" onClick={() => openShiftModal()}>
              <Plus size={18} />
              <span>Create Shift Template</span>
            </button>
            <button className="btn btn-primary" onClick={openAssignModal}>
              <UserCheck size={18} />
              <span>Assign Shift to Employee</span>
            </button>
          </div>
        )}
      </div>

      {/* Tabs Header */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', overflowX: 'auto', WebkitOverflowScrolling: 'touch' }}>
        <button
          onClick={() => setActiveTab('templates')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'templates' ? '3px solid var(--primary)' : '3px solid transparent',
            color: activeTab === 'templates' ? 'var(--primary)' : 'var(--text-secondary)',
            fontWeight: 700,
            cursor: 'pointer',
            whiteSpace: 'nowrap',
          }}
        >
          Shift Templates ({shifts.length})
        </button>
        <button
          onClick={() => setActiveTab('assignments')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'assignments' ? '3px solid var(--primary)' : '3px solid transparent',
            color: activeTab === 'assignments' ? 'var(--primary)' : 'var(--text-secondary)',
            fontWeight: 700,
            cursor: 'pointer',
            whiteSpace: 'nowrap',
          }}
        >
          Employee Shift Assignments ({employeeShifts.length})
        </button>
      </div>

      {/* Tab 1: Shift Templates */}
      {activeTab === 'templates' && (
        <div className="table-container">
          {shifts.length === 0 ? (
            <EmptyState title="No Shifts Configured" description="Create your first shift timing template." />
          ) : (
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Shift ID</th>
                  <th>Shift Name</th>
                  <th>Start Time</th>
                  <th>End Time</th>
                  <th>Working Hours</th>
                  <th>Grace Period</th>
                  <th>Description</th>
                  {canManage && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {shifts.map((s) => {
                  const id = s.shiftId || s.id;
                  return (
                    <tr key={id}>
                      <td style={{ fontWeight: 700 }}>#{id}</td>
                      <td style={{ fontWeight: 600 }}>{s.shiftName}</td>
                      <td>{formatTime(s.startTime)}</td>
                      <td>{formatTime(s.endTime)}</td>
                      <td>{s.workingHours} hrs</td>
                      <td>{s.graceMinutes ?? 15} mins</td>
                      <td style={{ color: 'var(--text-secondary)' }}>{s.description || 'N/A'}</td>
                      {canManage && (
                        <td style={{ textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => openShiftModal(s)} title="Edit Shift">
                              <Edit size={15} />
                            </button>
                            {isAdmin && (
                              <button className="btn btn-danger btn-sm" onClick={() => handleDeleteShift(id)} title="Delete Shift">
                                <Trash2 size={15} />
                              </button>
                            )}
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
      )}

      {/* Tab 2: Employee Shift Assignments */}
      {activeTab === 'assignments' && (
        <div className="table-container">
          {employeeShifts.length === 0 ? (
            <EmptyState title="No Shift Assignments" description="No employees have been assigned to shifts yet." />
          ) : (
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Assignment ID</th>
                  <th>Employee</th>
                  <th>Shift Template</th>
                  <th>Effective Date</th>
                  <th>Status</th>
                  {canManage && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {employeeShifts.map((es) => {
                  const id = es.employeeShiftId || es.id;
                  return (
                    <tr key={id}>
                      <td style={{ fontWeight: 700 }}>#{id}</td>
                      <td style={{ fontWeight: 600 }}>{es.username ? `${es.username} (#${es.userId})` : `User #${es.userId}`}</td>
                      <td>{es.shiftName || `Shift #${es.shiftId}`}</td>
                      <td>{formatDate(es.effectiveDate)}</td>
                      <td>
                        <StatusBadge status={es.status || 'ACTIVE'} />
                      </td>
                      {canManage && (
                        <td style={{ textAlign: 'right' }}>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDeleteAssignment(id)} title="Remove Assignment">
                            <Trash2 size={15} />
                          </button>
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Shift Template Modal */}
      <Modal isOpen={isShiftModalOpen} onClose={() => setIsShiftModalOpen(false)} title={selectedShift ? 'Edit Shift Template' : 'Create Shift Template'}>
        <form onSubmit={handleShiftSubmit}>
          <div className="form-group">
            <label className="form-label">Shift Name *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="e.g. Morning Shift"
              value={shiftForm.shiftName}
              onChange={(e) => setShiftForm({ ...shiftForm, shiftName: e.target.value })}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Start Time *</label>
              <input
                type="time"
                step="1"
                className="form-input"
                required
                value={shiftForm.startTime}
                onChange={(e) => setShiftForm({ ...shiftForm, startTime: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">End Time *</label>
              <input
                type="time"
                step="1"
                className="form-input"
                required
                value={shiftForm.endTime}
                onChange={(e) => setShiftForm({ ...shiftForm, endTime: e.target.value })}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Working Hours *</label>
              <input
                type="number"
                className="form-input"
                required
                min="1"
                max="24"
                value={shiftForm.workingHours}
                onChange={(e) => setShiftForm({ ...shiftForm, workingHours: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Grace Period (Minutes)</label>
              <input
                type="number"
                className="form-input"
                min="0"
                max="120"
                placeholder="15"
                value={shiftForm.graceMinutes}
                onChange={(e) => setShiftForm({ ...shiftForm, graceMinutes: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-textarea"
              rows={2}
              value={shiftForm.description}
              onChange={(e) => setShiftForm({ ...shiftForm, description: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsShiftModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : selectedShift ? 'Update Shift' : 'Create Shift'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Assign Shift Modal */}
      <Modal isOpen={isAssignModalOpen} onClose={() => setIsAssignModalOpen(false)} title="Assign Shift to Employee">
        <form onSubmit={handleAssignSubmit}>
          {shifts.length === 0 && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.75rem 1rem', background: 'rgba(234, 179, 8, 0.1)', border: '1px solid rgba(234, 179, 8, 0.3)', borderRadius: '0.5rem', color: '#eab308', marginBottom: '1rem', fontSize: '0.875rem' }}>
              <AlertCircle size={16} />
              <span>No shift templates found. Please create a shift template first.</span>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Employee User *</label>
            {users.length > 0 ? (
              <select
                className="form-select"
                required
                value={assignForm.userId}
                onChange={(e) => setAssignForm({ ...assignForm, userId: e.target.value })}
              >
                <option value="">Select Employee</option>
                {users.map((u) => (
                  <option key={u.userId} value={u.userId}>
                    {u.firstName} {u.lastName} ({u.username} - {u.roleName || 'Employee'})
                  </option>
                ))}
              </select>
            ) : (
              <input
                type="number"
                className="form-input"
                required
                placeholder="User ID"
                value={assignForm.userId}
                onChange={(e) => setAssignForm({ ...assignForm, userId: e.target.value })}
              />
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Shift Template *</label>
            <select
              className="form-select"
              required
              disabled={shifts.length === 0}
              value={assignForm.shiftId}
              onChange={(e) => setAssignForm({ ...assignForm, shiftId: e.target.value })}
            >
              <option value="">Select Shift</option>
              {shifts.map((s) => {
                const sId = s.shiftId || s.id;
                return (
                  <option key={sId} value={sId}>
                    {s.shiftName} ({formatTime(s.startTime)} - {formatTime(s.endTime)} / {s.workingHours} hrs)
                  </option>
                );
              })}
            </select>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Effective Date *</label>
              <input
                type="date"
                className="form-input"
                required
                value={assignForm.effectiveDate}
                onChange={(e) => setAssignForm({ ...assignForm, effectiveDate: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Status *</label>
              <select
                className="form-select"
                value={assignForm.status}
                onChange={(e) => setAssignForm({ ...assignForm, status: e.target.value })}
              >
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsAssignModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting || shifts.length === 0}>
              {submitting ? 'Assigning...' : 'Assign Shift'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ShiftsPage;
