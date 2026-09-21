import React, { useEffect, useState } from 'react';
import { Plus, Search, LogIn, LogOut, Trash2, Edit, Clock, Filter, MapPin } from 'lucide-react';
import { attendanceService } from '../services/attendanceService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import Pagination from '../components/Pagination';
import { formatDate, formatTime, formatMinutes, getErrorMessage } from '../utils/formatters';
import { getCurrentLocation } from '../utils/geolocation';

const AttendancePage = () => {
  const { isAdmin } = useAuth();
  const [attendances, setAttendances] = useState([]);
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
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  // Punch Action State
  const [punchUserId, setPunchUserId] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [geoStatus, setGeoStatus] = useState('');

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

  // Load user list for dropdowns if admin
  useEffect(() => {
    if (isAdmin) {
      userService.getAll({ page: 0, size: 1000 })
        .then((res) => setUsers(Array.isArray(res) ? res : res.content || []))
        .catch(() => setUsers([]));
    }
  }, [isAdmin]);

  const loadAttendance = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size: pageSize,
        search: search || undefined,
        status: statusFilter || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
      };

      const res = await attendanceService.getAll(params);
      if (res && res.content !== undefined) {
        setAttendances(res.content);
        setPaginationInfo({
          pageNumber: res.pageNumber,
          pageSize: res.pageSize,
          totalElements: res.totalElements,
          totalPages: res.totalPages,
          first: res.first,
          last: res.last,
        });
      } else if (Array.isArray(res)) {
        setAttendances(res);
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
    loadAttendance();
  }, [page, pageSize, search, statusFilter, startDate, endDate]);

  const handleSearchChange = (val) => {
    setSearch(val);
    setPage(0);
  };

  const handleStatusFilterChange = (val) => {
    setStatusFilter(val);
    setPage(0);
  };

  const handleStartDateChange = (val) => {
    setStartDate(val);
    setPage(0);
  };

  const handleEndDateChange = (val) => {
    setEndDate(val);
    setPage(0);
  };

  const handleQuickCheckIn = async () => {
    setActionLoading(true);
    setGeoStatus('');
    try {
      let locationPayload = null;
      if (!punchUserId) {
        setGeoStatus('Acquiring high-accuracy GPS coordinates...');
        const coords = await getCurrentLocation();
        locationPayload = coords;
        setGeoStatus('Verifying workplace perimeter...');
      }
      await attendanceService.checkIn(punchUserId || null, locationPayload);
      setToast({ message: punchUserId ? `Checked In user #${punchUserId} successfully!` : 'Checked In successfully!', type: 'success' });
      loadAttendance();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
      setGeoStatus('');
    }
  };

  const handleQuickCheckOut = async () => {
    setActionLoading(true);
    setGeoStatus('');
    try {
      let locationPayload = null;
      if (!punchUserId) {
        setGeoStatus('Acquiring high-accuracy GPS coordinates...');
        const coords = await getCurrentLocation();
        locationPayload = coords;
        setGeoStatus('Verifying workplace perimeter...');
      }
      await attendanceService.checkOut(punchUserId || null, locationPayload);
      setToast({ message: punchUserId ? `Checked Out user #${punchUserId} successfully!` : 'Checked Out successfully!', type: 'success' });
      loadAttendance();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
      setGeoStatus('');
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
      loadAttendance();
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
      loadAttendance();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading && attendances.length === 0) return <LoadingSpinner text="Fetching attendance records..." />;
  if (error && attendances.length === 0) return <ErrorState message={error} onRetry={loadAttendance} />;

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
            <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Punch Station:</span>
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
          {geoStatus && (
            <div
              style={{
                width: '100%',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontSize: '0.825rem',
                color: '#60a5fa',
                background: 'rgba(59, 130, 246, 0.1)',
                padding: '0.45rem 0.8rem',
                borderRadius: 'var(--radius-md)',
                border: '1px solid rgba(59, 130, 246, 0.25)',
                marginTop: '0.5rem',
              }}
            >
              <MapPin size={15} style={{ animation: 'pulse 1.5s infinite' }} />
              <span>{geoStatus}</span>
            </div>
          )}
        </div>
      </div>

      {/* Filter and Table */}
      <div className="table-container">
        <div className="table-header-bar" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
          <div className="search-box" style={{ minWidth: '220px', flex: 1 }}>
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search user name or email..."
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
              <option value="PRESENT">PRESENT</option>
              <option value="ABSENT">ABSENT</option>
              <option value="LATE">LATE</option>
              <option value="HALF_DAY">HALF_DAY</option>
              <option value="ON_LEAVE">ON_LEAVE</option>
            </select>

            <input
              type="date"
              className="form-input"
              style={{ flex: '1 1 130px', minWidth: '120px' }}
              value={startDate}
              placeholder="Start Date"
              onChange={(e) => handleStartDateChange(e.target.value)}
            />

            <input
              type="date"
              className="form-input"
              style={{ flex: '1 1 130px', minWidth: '120px' }}
              value={endDate}
              placeholder="End Date"
              onChange={(e) => handleEndDateChange(e.target.value)}
            />
          </div>
        </div>

        {attendances.length === 0 ? (
          <EmptyState title="No Attendance Logs" description="No attendance entries match your search or filter criteria." />
        ) : (
          <>
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Record ID</th>
                  <th>Employee Name</th>
                  <th>Shift</th>
                  <th>Date</th>
                  <th>Check-In</th>
                  <th>Check-Out</th>
                  <th>Location Verification</th>
                  <th>Working Duration</th>
                  <th>Status & Exceptions</th>
                  {isAdmin && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {attendances.map((record) => {
                  const id = record.attendanceId || record.id;
                  const exList = record.exceptions || [];
                  return (
                    <tr key={id}>
                      <td style={{ fontWeight: 700 }}>#{id}</td>
                      <td>
                        <div style={{ fontWeight: 600 }}>
                          {record.userFirstName ? `${record.userFirstName} ${record.userLastName}` : `User #${record.userId}`}
                        </div>
                      </td>
                      <td>
                        <span style={{ fontSize: '0.8rem', padding: '0.2rem 0.5rem', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', border: '1px solid rgba(255,255,255,0.1)' }}>
                          {record.shiftName || 'Unassigned'}
                        </span>
                      </td>
                      <td>{formatDate(record.attendanceDate)}</td>
                      <td>{formatTime(record.checkInTime)}</td>
                      <td>{formatTime(record.checkOutTime)}</td>
                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem', fontSize: '0.78rem' }}>
                          {record.checkInVerification ? (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', flexWrap: 'wrap' }}>
                              <span style={{ color: 'var(--text-secondary)' }}>In:</span>
                              <span style={{ fontWeight: 600, color: record.checkInVerification.locationVerified ? '#34d399' : '#f87171' }}>
                                {record.checkInVerification.workplaceName || 'Site'}
                              </span>
                              <span className="badge" style={{
                                fontSize: '0.65rem',
                                padding: '0.1rem 0.35rem',
                                background: record.checkInVerification.verificationMethod === 'ADMIN_OVERRIDE' ? 'rgba(234, 179, 8, 0.15)' : 'rgba(52, 211, 153, 0.15)',
                                color: record.checkInVerification.verificationMethod === 'ADMIN_OVERRIDE' ? '#fbbf24' : '#34d399'
                              }}>
                                {record.checkInVerification.verificationMethod}
                              </span>
                              {record.checkInVerification.distanceMeters !== undefined && record.checkInVerification.distanceMeters !== null && (
                                <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem' }}>
                                  ({Math.round(record.checkInVerification.distanceMeters)}m)
                                </span>
                              )}
                            </div>
                          ) : null}
                          {record.checkOutVerification ? (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', flexWrap: 'wrap' }}>
                              <span style={{ color: 'var(--text-secondary)' }}>Out:</span>
                              <span style={{ fontWeight: 600, color: record.checkOutVerification.locationVerified ? '#34d399' : '#f87171' }}>
                                {record.checkOutVerification.workplaceName || 'Site'}
                              </span>
                              <span className="badge" style={{
                                fontSize: '0.65rem',
                                padding: '0.1rem 0.35rem',
                                background: record.checkOutVerification.verificationMethod === 'ADMIN_OVERRIDE' ? 'rgba(234, 179, 8, 0.15)' : 'rgba(52, 211, 153, 0.15)',
                                color: record.checkOutVerification.verificationMethod === 'ADMIN_OVERRIDE' ? '#fbbf24' : '#34d399'
                              }}>
                                {record.checkOutVerification.verificationMethod}
                              </span>
                              {record.checkOutVerification.distanceMeters !== undefined && record.checkOutVerification.distanceMeters !== null && (
                                <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem' }}>
                                  ({Math.round(record.checkOutVerification.distanceMeters)}m)
                                </span>
                              )}
                            </div>
                          ) : null}
                          {!record.checkInVerification && !record.checkOutVerification && (
                            <span style={{ color: 'var(--text-muted)' }}>--</span>
                          )}
                        </div>
                      </td>
                      <td>
                        {record.workingMinutes > 0 ? (
                          <div style={{ fontWeight: 600, color: 'var(--primary-light)' }}>
                            {formatMinutes(record.workingMinutes)}
                          </div>
                        ) : record.workingHours ? (
                          `${record.workingHours} hrs`
                        ) : (
                          '--'
                        )}
                      </td>
                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                          <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', alignItems: 'center' }}>
                            <StatusBadge status={record.status} />
                            {record.lateMinutes > 0 && (
                              <span className="badge badge-warning" style={{ fontSize: '0.7rem' }}>
                                Late {formatMinutes(record.lateMinutes)}
                              </span>
                            )}
                            {record.earlyDepartureMinutes > 0 && (
                              <span className="badge badge-warning" style={{ fontSize: '0.7rem' }}>
                                Early {formatMinutes(record.earlyDepartureMinutes)}
                              </span>
                            )}
                            {record.overtimeMinutes > 0 && (
                              <span className="badge badge-info" style={{ fontSize: '0.7rem' }}>
                                OT {formatMinutes(record.overtimeMinutes)}
                              </span>
                            )}
                          </div>
                          {exList.length > 0 && (
                            <div style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                              {exList.map((ex, idx) => (
                                <span key={idx} style={{ fontSize: '0.65rem', padding: '0.1rem 0.35rem', borderRadius: '3px', background: ex === 'NO_SHIFT_ASSIGNED' ? 'rgba(100,100,100,0.2)' : 'rgba(239, 68, 68, 0.15)', color: ex === 'NO_SHIFT_ASSIGNED' ? '#aaa' : '#f87171', border: '1px solid rgba(255,255,255,0.08)' }}>
                                  ⚠ {ex.replace(/_/g, ' ')}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
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
