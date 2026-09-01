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
      const attendRes = await attendanceService.getAll().catch(() => []);
      const leavesRes = await leaveService.getAll().catch(() => []);
      setAttendances(attendRes);
      setLeaves(leavesRes);

      if (isAdmin || isHr || isManager) {
        const usersRes = await userService.getAll().catch(() => []);
        setUsers(usersRes);
      }

      if (isAdmin || isHr) {
        const deptRes = await departmentService.getAll().catch(() => []);
        const payRes = await payrollService.getAll().catch(() => []);
        setDepartments(deptRes);
        setPayrolls(payRes);
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
            {(isAdmin || isHr)
              ? 'Log your attendance or manage check-in/out for employees.'
              : 'Record your daily check-in or check-out time.'}
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          {(isAdmin || isHr) && users.length > 0 && (
            <select
              className="form-select"
              style={{ width: '220px' }}
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
            onClick={handleSelfCheckIn}
            disabled={actionLoading}
          >
            <LogIn size={18} />
            <span>Check In</span>
          </button>

          <button
            className="btn btn-danger"
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
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Employees</div>
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{users.length}</div>
            </div>
            <div className="stat-icon"><Users size={26} /></div>
          </div>
        )}

        {isManager && (
          <div className="card stat-card">
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>Team Members</div>
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{users.length}</div>
            </div>
            <div className="stat-icon"><Users size={26} /></div>
          </div>
        )}

        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              {isEmployee ? 'Today Status' : 'Present Today'}
            </div>
            <div className="stat-value" style={{ color: '#34d399' }}>
              {isEmployee ? (userTodayAttendance ? userTodayAttendance.status : 'NOT CHECKED IN') : todayPresent}
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.12)', color: '#34d399' }}>
            <CheckCircle2 size={26} />
          </div>
        </div>

        <div className="card stat-card">
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              {isEmployee ? 'My Leaves' : 'Pending Leaves'}
            </div>
            <div className="stat-value" style={{ color: '#fbbf24' }}>
              {isEmployee ? leaves.length : pendingLeaves}
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'rgba(245, 158, 11, 0.12)', color: '#fbbf24' }}>
            <CalendarDays size={26} />
          </div>
        </div>

        {(isAdmin || isHr) && (
          <div className="card stat-card">
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>Departments</div>
              <div className="stat-value" style={{ color: '#a855f7' }}>{departments.length}</div>
            </div>
            <div className="stat-icon" style={{ background: 'rgba(168, 85, 247, 0.12)', color: '#a855f7' }}>
              <Building2 size={26} />
            </div>
          </div>
        )}
      </div>

      {/* Recent Attendance Activity Feed */}
      <div className="table-container" style={{ marginTop: '2rem' }}>
        <div className="table-header-bar">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <TrendingUp size={20} style={{ color: 'var(--primary)' }} />
            <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>
              {isEmployee ? 'My Attendance History' : isManager ? 'Team Attendance Logs' : 'Recent Attendance Logs'}
            </h3>
          </div>
        </div>

        {attendances.length === 0 ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
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
                  <td style={{ fontWeight: 600 }}>#{item.attendanceId || item.id}</td>
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
