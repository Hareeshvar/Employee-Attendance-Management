import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Clock,
  CalendarDays,
  Building2,
  Briefcase,
  DollarSign,
  Bell,
  FileText,
  Shield,
  Clock3,
  UserCheck,
  Activity,
  MapPin,
  X
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Sidebar = ({ isOpen = false, onClose }) => {
  const { role, firstName, lastName, username } = useAuth();
  const displayName = firstName ? `${firstName} ${lastName || ''}`.trim() : (username || 'User');

  const getNavItems = () => {
    switch (role) {
      case 'ADMIN':
        return [
          { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
          { name: 'Users', path: '/users', icon: Users },
          { name: 'Departments', path: '/departments', icon: Building2 },
          { name: 'Designations', path: '/designations', icon: Briefcase },
          { name: 'Shifts', path: '/shifts', icon: Clock3 },
          { name: 'Workplaces', path: '/workplaces', icon: MapPin },
          { name: 'Attendance', path: '/attendance', icon: Clock },
          { name: 'Leaves', path: '/leaves', icon: CalendarDays },
          { name: 'Payroll', path: '/payrolls', icon: DollarSign },
          { name: 'Reports', path: '/reports', icon: FileText },
          { name: 'Audit Logs', path: '/audit-logs', icon: Activity },
          { name: 'Roles', path: '/roles', icon: Shield },
          { name: 'Notifications', path: '/notifications', icon: Bell },
        ];

      case 'HR':
        return [
          { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
          { name: 'Employees', path: '/users', icon: Users },
          { name: 'Departments', path: '/departments', icon: Building2 },
          { name: 'Designations', path: '/designations', icon: Briefcase },
          { name: 'Shifts', path: '/shifts', icon: Clock3 },
          { name: 'Workplaces', path: '/workplaces', icon: MapPin },
          { name: 'Attendance Logs', path: '/attendance', icon: Clock },
          { name: 'Leave Requests', path: '/leaves', icon: CalendarDays },
          { name: 'Payroll', path: '/payrolls', icon: DollarSign },
          { name: 'HR Reports', path: '/reports', icon: FileText },
          { name: 'Audit Logs', path: '/audit-logs', icon: Activity },
          { name: 'Notifications', path: '/notifications', icon: Bell },
        ];

      case 'MANAGER':
        return [
          { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
          { name: 'Team Members', path: '/users', icon: Users },
          { name: 'Team Attendance', path: '/attendance', icon: Clock },
          { name: 'Team Leaves', path: '/leaves', icon: CalendarDays },
          { name: 'Team Reports', path: '/reports', icon: FileText },
          { name: 'Notifications', path: '/notifications', icon: Bell },
        ];

      case 'EMPLOYEE':
      default:
        return [
          { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
          { name: 'My Attendance', path: '/attendance', icon: Clock },
          { name: 'My Workplaces', path: '/workplaces', icon: MapPin },
          { name: 'My Leaves', path: '/leaves', icon: CalendarDays },
          { name: 'My Payroll', path: '/payrolls', icon: DollarSign },
          { name: 'Notifications', path: '/notifications', icon: Bell },
          { name: 'My Profile', path: '/profile', icon: UserCheck },
        ];
    }
  };

  const navItems = getNavItems();

  return (
    <aside className={`sidebar ${isOpen ? 'mobile-open' : ''}`}>
      <div className="sidebar-logo">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Clock size={28} style={{ color: 'var(--primary)' }} />
          <span>Attendify HR</span>
        </div>
        {/* Mobile Dismiss Button */}
        <button
          className="sidebar-close-btn"
          onClick={onClose}
          aria-label="Close navigation"
        >
          <X size={20} />
        </button>
      </div>

      <nav className="sidebar-nav">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `nav-item ${isActive ? 'active' : ''}`
              }
              end={item.path === '/'}
              onClick={onClose}
            >
              <Icon size={18} />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </nav>

      <div
        style={{
          padding: '1rem',
          borderTop: '1px solid var(--border-color)',
          display: 'flex',
          alignItems: 'center',
          gap: '0.75rem',
        }}
      >
        <div
          style={{
            width: 38,
            height: 38,
            borderRadius: '50%',
            background: 'var(--primary-gradient)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 800,
            fontSize: '0.9rem',
            color: '#fff',
            flexShrink: 0,
            boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)',
          }}
        >
          {displayName.charAt(0).toUpperCase()}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', minWidth: 0 }}>
          <span
            style={{
              fontSize: '0.85rem',
              fontWeight: 700,
              color: 'var(--text-primary)',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            {displayName}
          </span>
          <span style={{ fontSize: '0.725rem', color: 'var(--text-muted)' }}>
            {role || 'EMPLOYEE'}
          </span>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
