import React, { useEffect, useState } from 'react';
import {
  Users,
  Clock,
  CalendarDays,
  Building2,
  CheckCircle2,
  LogOut,
  LogIn,
  AlertCircle,
  TrendingUp,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../services/userService';
import { attendanceService } from '../services/attendanceService';
import { leaveService } from '../services/leaveService';
import { departmentService } from '../services/departmentService';
import { LoadingSpinner, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Toast from '../components/Toast';
import { formatDate, formatTime, getErrorMessage } from '../utils/formatters';

const DashboardPage = () => {
  const { username, isAdmin, userId, saveUserId } = useAuth();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [users, setUsers] = useState([]);
  const [attendances, setAttendances] = useState([]);
  const [leaves, setLeaves] = useState([]);
  const [departments, setDepartments] = useState([]);

  const [selectedUserId, setSelectedUserId] = useState(userId || '');
  const [actionLoading, setActionLoading] = useState(false);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Fetch available metrics from real endpoints supported by user role
      const attendRes = await attendanceService.getAll().catch(() => []);
      const leavesRes = await leaveService.getAll().catch(() => []);

      setAttendances(attendRes);
      setLeaves(leavesRes);

      if (isAdmin) {
        const usersRes = await userService.getAll().catch(() => []);
        const deptRes = await departmentService.getAll().catch(() => []);
        setUsers(usersRes);
        setDepartments(deptRes);

        // Pre-select first user if non-selected
        if (usersRes.length > 0 && !selectedUserId) {
          setSelectedUserId(usersRes[0].userId);
        }
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, [isAdmin]);

  const handleCheckIn = async () => {
    const idToUse = selectedUserId || userId;
    if (!idToUse) {
      setToast({ message: 'Please select or enter a User ID for Check-In.', type: 'error' });
      return;
    }

    setActionLoading(true);
    try {
      const result = await attendanceService.checkIn(idToUse);
      saveUserId(idToUse);
      setToast({ message: `Successfully Checked In for user #${idToUse}!`, type: 'success' });
      loadDashboardData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleCheckOut = async () => {
    const idToUse = selectedUserId || userId;
    if (!idToUse) {
      setToast({ message: 'Please select or enter a User ID for Check-Out.', type: 'error' });
      return;
    }

    setActionLoading(true);
    try {
      const result = await attendanceService.checkOut(idToUse);
      saveUserId(idToUse);
      setToast({ message: `Successfully Checked Out for user #${idToUse}!`, type: 'success' });
      loadDashboardData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <LoadingSpinner text="Fetching live dashboard metrics..." />;
  if (error) return <ErrorState message={error} onRetry={loadDashboardData} />;

  // Calculate real metrics
  const totalEmployees = users.length;
  const pendingLeaves = leaves.filter((l) => String(l.leaveType || l.status).toUpperCase() === 'PENDING' || String(l.status).toUpperCase() === 'PENDING').length;
  const totalDepartments = departments.length;
  
  // Today's attendance logs
  const todayStr = new Date().toISOString().split('T')[0];
  const todayPresent = attendances.filter((a) => a.attendanceDate === todayStr || a.status === 'PRESENT').length;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-primary)' }}>
          Welcome back, {username}!
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
          Here is what is happening with your organization's attendance & workforce today.
        </p>
      </div>

      {/* Quick Action Bar for Check-In / Check-Out */}
      <div
        className="card"
        style={{
          marginBottom: '2rem',
          background: 'linear-gradient(135deg, rgba(35, 45, 66, 0.9), rgba(19, 25, 38, 0.9))',
          border: '1px solid rgba(59, 130, 246, 0.2)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1.5rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
            <Clock style={{ color: 'var(--primary)' }} size={22} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Attendance Punch Station</h3>
          </div>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Record real-time attendance check-in or check-out directly into Spring Boot & MySQL.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          {isAdmin && users.length > 0 ? (
            <select
              className="form-select"
              style={{ width: '220px' }}
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
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
              style={{ width: '160px' }}
              placeholder="User ID (e.g. 1)"
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
            />
          )}

          <button
            className="btn btn-success"
            onClick={handleCheckIn}
            disabled={actionLoading}
          >
            <LogIn size={18} />
            <span>Check In</span>
          </button>

          <button
            className="btn btn-danger"
            onClick={handleCheckOut}
            disabled={actionLoading}
          >
            <LogOut size={18} />
            <span>Check Out</span>
          </button>
        </div>
      </div>

      {/* Key Statistics Grid */}
      <div className="grid-stats">
        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              Total Employees
            </div>
            <div className="stat-value" style={{ color: 'var(--primary)' }}>
              {isAdmin ? totalEmployees : '--'}
            </div>
          </div>
          <div className="stat-icon">
            <Users size={26} />
          </div>
        </div>

        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              Present Today
            </div>
            <div className="stat-value" style={{ color: '#34d399' }}>
              {todayPresent}
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.12)', color: '#34d399' }}>
            <CheckCircle2 size={26} />
          </div>
        </div>

        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              Pending Leaves
            </div>
            <div className="stat-value" style={{ color: '#fbbf24' }}>
              {pendingLeaves}
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'rgba(245, 158, 11, 0.12)', color: '#fbbf24' }}>
            <CalendarDays size={26} />
          </div>
        </div>

        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              Departments
            </div>
            <div className="stat-value" style={{ color: '#a855f7' }}>
              {isAdmin ? totalDepartments : '--'}
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'rgba(168, 85, 247, 0.12)', color: '#a855f7' }}>
            <Building2 size={26} />
          </div>
        </div>
      </div>

      {/* Recent Attendance Activity Feed */}
      <div className="table-container">
        <div className="table-header-bar">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <TrendingUp size={20} style={{ color: 'var(--primary)' }} />
            <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Recent Attendance Logs</h3>
          </div>
        </div>

        {attendances.length === 0 ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No attendance records found in database. Use Check-In above to log attendance.
          </div>
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>Log ID</th>
                <th>User ID</th>
                <th>Date</th>
                <th>Check-In Time</th>
                <th>Check-Out Time</th>
                <th>Working Hours</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {attendances.slice(0, 7).map((item) => (
                <tr key={item.attendanceId || item.id}>
                  <td style={{ fontWeight: 600 }}>#{item.attendanceId || item.id}</td>
                  <td>User #{item.userId}</td>
                  <td>{formatDate(item.attendanceDate)}</td>
                  <td>{formatTime(item.checkInTime)}</td>
                  <td>{formatTime(item.checkOutTime)}</td>
                  <td>{item.workingHours ? `${item.workingHours} hrs` : '--'}</td>
                  <td>
                    <StatusBadge status={item.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
