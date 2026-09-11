import React, { useEffect, useState } from 'react';
import {
  Users,
  Clock,
  CalendarDays,
  Building2,
  CheckCircle2,
  LogOut,
  LogIn,
  TrendingUp,
  FileText,
  DollarSign,
  UserCheck,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../services/userService';
import { attendanceService } from '../services/attendanceService';
import { leaveService } from '../services/leaveService';
import { departmentService } from '../services/departmentService';
import { payrollService } from '../services/payrollService';
import { LoadingSpinner, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Toast from '../components/Toast';
import { formatDate, formatTime, getErrorMessage } from '../utils/formatters';
import AttendanceAnalytics from '../components/analytics/AttendanceAnalytics';

const DashboardPage = () => {
  const { username, firstName, role, isAdmin, isHr, isManager, isEmployee, userId } = useAuth();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [users, setUsers] = useState([]);
  const [attendances, setAttendances] = useState([]);
  const [leaves, setLeaves] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [payrolls, setPayrolls] = useState([]);

  const [selectedUserId, setSelectedUserId] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const attendRes = await attendanceService.getAnalytics().catch(() => []);
      const leavesRes = await leaveService.getAll({ page: 0, size: 1000 }).catch(() => []);
      setAttendances(Array.isArray(attendRes) ? attendRes : attendRes.content || []);
      setLeaves(Array.isArray(leavesRes) ? leavesRes : leavesRes.content || []);

      if (isAdmin || isHr || isManager) {
        const usersRes = await userService.getAll({ page: 0, size: 1000 }).catch(() => []);
        setUsers(Array.isArray(usersRes) ? usersRes : usersRes.content || []);
      }

      if (isAdmin || isHr) {
        const deptRes = await departmentService.getAll().catch(() => []);
        const payRes = await payrollService.getAll({ page: 0, size: 1000 }).catch(() => []);
        setDepartments(Array.isArray(deptRes) ? deptRes : deptRes.content || []);
        setPayrolls(Array.isArray(payRes) ? payRes : payRes.content || []);
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, [role]);

  const handleSelfCheckIn = async () => {
    setActionLoading(true);
    try {
      const targetId = (isAdmin || isHr) && selectedUserId ? selectedUserId : null;
      await attendanceService.checkIn(targetId);
      setToast({ message: 'Successfully checked in!', type: 'success' });
      loadDashboardData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleSelfCheckOut = async () => {
    setActionLoading(true);
    try {
      const targetId = (isAdmin || isHr) && selectedUserId ? selectedUserId : null;
      await attendanceService.checkOut(targetId);
      setToast({ message: 'Successfully checked out!', type: 'success' });
      loadDashboardData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <LoadingSpinner text="Fetching live dashboard metrics..." />;
  if (error) return <ErrorState message={error} onRetry={loadDashboardData} />;

  const todayStr = new Date().toISOString().split('T')[0];
  const todayPresent = attendances.filter(
    (a) => a.attendanceDate === todayStr || String(a.status).toUpperCase() === 'PRESENT'
  ).length;

  const pendingLeaves = leaves.filter(
    (l) => String(l.status).toUpperCase() === 'PENDING'
  ).length;

  const userTodayAttendance = attendances.find((a) => a.attendanceDate === todayStr);

  const getDashboardTitle = () => {
    const greeting = firstName ? `Welcome back, ${firstName}!` : `Welcome back, ${username}!`;
    switch (role) {
      case 'ADMIN': return `${greeting} (System Administration)`;
      case 'HR': return `${greeting} (HR Operations)`;
      case 'MANAGER': return `${greeting} (Team Dashboard)`;
      case 'EMPLOYEE': default: return `${greeting} (Employee Portal)`;
    }
  };

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-primary)' }}>
          {getDashboardTitle()}
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
          Real-time organizational attendance, leave requests, and workforce analytics.
        </p>
      </div>

      {/* Attendance Punch Station */}
      <div
        className="card"
        style={{
          marginBottom: '2rem',
          background: 'linear-gradient(135deg, rgba(26, 36, 56, 0.85) 0%, rgba(15, 21, 35, 0.9) 100%)',
          border: '1px solid var(--border-highlight)',
          borderRadius: 'var(--radius-xl)',
          padding: '1.75rem 2rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1.5rem',
          boxShadow: 'var(--shadow-md)',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.35rem' }}>
            <div
              style={{
                width: '38px',
                height: '38px',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(59, 130, 246, 0.15)',
                border: '1px solid rgba(59, 130, 246, 0.3)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--primary)',
              }}
            >
              <Clock size={20} />
            </div>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-primary)' }}>
              Attendance Punch Station
            </h3>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginLeft: '3.1rem' }}>
            {(isAdmin || isHr)
              ? 'Log your personal attendance or manage check-in/out for registered staff.'
              : 'Record your daily check-in or check-out timestamp.'}
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          {(isAdmin || isHr) && users.length > 0 && (
            <select
              className="form-select"
              style={{ width: '230px', borderRadius: 'var(--radius-md)' }}
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
            >
              <option value="">Self ({username})</option>
              {users.map((u) => (
                <option key={u.userId} value={u.userId}>
                  {u.firstName} {u.lastName} (@{u.username})
                </option>
              ))}
            </select>
          )}

          <button
            className="btn btn-success"
            style={{ borderRadius: 'var(--radius-md)', padding: '0.7rem 1.4rem' }}
            onClick={handleSelfCheckIn}
            disabled={actionLoading}
          >
            <LogIn size={18} />
            <span>Check In</span>
          </button>

          <button
            className="btn btn-danger"
            style={{ borderRadius: 'var(--radius-md)', padding: '0.7rem 1.4rem' }}
            onClick={handleSelfCheckOut}
            disabled={actionLoading}
          >
            <LogOut size={18} />
            <span>Check Out</span>
          </button>
        </div>
      </div>

      {/* Role-Specific Metric Cards Grid */}
      <div className="grid-stats">
        {(isAdmin || isHr) && (
          <div className="card stat-card">
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Total Employees</div>
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{users.length}</div>
            </div>
            <div className="stat-icon"><Users size={26} /></div>
          </div>
        )}

        {isManager && (
          <div className="card stat-card">
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Team Members</div>
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{users.length}</div>
            </div>
            <div className="stat-icon"><Users size={26} /></div>
          </div>
        )}

        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              {isEmployee ? 'Today Status' : 'Present Today'}
            </div>
            <div className="stat-value" style={{ color: '#34d399' }}>
              {isEmployee ? (userTodayAttendance ? userTodayAttendance.status : 'NOT CHECKED IN') : todayPresent}
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.12)', borderColor: 'rgba(16, 185, 129, 0.3)', color: '#34d399' }}>
            <CheckCircle2 size={26} />
          </div>
        </div>

        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              {isEmployee ? 'My Leaves' : 'Pending Leaves'}
            </div>
            <div className="stat-value" style={{ color: '#fbbf24' }}>
              {isEmployee ? leaves.length : pendingLeaves}
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'rgba(245, 158, 11, 0.12)', borderColor: 'rgba(245, 158, 11, 0.3)', color: '#fbbf24' }}>
            <CalendarDays size={26} />
          </div>
        </div>

        {(isAdmin || isHr) && (
          <div className="card stat-card">
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Departments</div>
              <div className="stat-value" style={{ color: '#a855f7' }}>{departments.length}</div>
            </div>
            <div className="stat-icon" style={{ background: 'rgba(168, 85, 247, 0.12)', borderColor: 'rgba(168, 85, 247, 0.3)', color: '#a855f7' }}>
              <Building2 size={26} />
            </div>
          </div>
        )}
      </div>

      {/* Graphical Attendance Analytics (RBAC-Aware) */}
      <AttendanceAnalytics
        records={attendances}
        role={role}
        loading={loading}
        error={error}
        onRetry={loadDashboardData}
      />

      {/* Recent Attendance Activity Feed */}
      <div className="table-container" style={{ marginTop: '2rem' }}>
        <div className="table-header-bar">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: 'var(--radius-sm)',
                background: 'rgba(59, 130, 246, 0.12)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--primary)',
              }}
            >
              <TrendingUp size={18} />
            </div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--text-primary)' }}>
              {isEmployee ? 'My Attendance History' : isManager ? 'Team Attendance Logs' : 'Recent Attendance Logs'}
            </h3>
          </div>
        </div>

        {attendances.length === 0 ? (
          <div style={{ padding: '3rem 2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No attendance records found. Use the Punch Station above to log attendance.
          </div>
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>Log ID</th>
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
                  <td style={{ fontWeight: 700, color: 'var(--primary)' }}>#{item.attendanceId || item.id}</td>
                  <td>{formatDate(item.attendanceDate)}</td>
                  <td>{formatTime(item.checkInTime)}</td>
                  <td>{formatTime(item.checkOutTime)}</td>
                  <td>{item.workingHours !== null && item.workingHours !== undefined ? `${item.workingHours} hrs` : '--'}</td>
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
