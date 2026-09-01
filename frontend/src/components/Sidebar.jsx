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
  UserCheck
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Sidebar = () => {
  const { role } = useAuth();

  const getNavItems = () => {
    switch (role) {
      case 'ADMIN':
        return [
          { name: 'Dashboard', path: '/', icon: LayoutDashboard },
          { name: 'Users', path: '/users', icon: Users },
          { name: 'Departments', path: '/departments', icon: Building2 },
          { name: 'Designations', path: '/designations', icon: Briefcase },
          { name: 'Shifts', path: '/shifts', icon: Clock3 },
          { name: 'Attendance', path: '/attendance', icon: Clock },
          { name: 'Leaves', path: '/leaves', icon: CalendarDays },
          { name: 'Payroll', path: '/payrolls', icon: DollarSign },
          { name: 'Reports', path: '/reports', icon: FileText },
          { name: 'Roles', path: '/roles', icon: Shield },
          { name: 'Notifications', path: '/notifications', icon: Bell },
        ];

      case 'HR':
        return [
          { name: 'Dashboard', path: '/', icon: LayoutDashboard },
          { name: 'Employees', path: '/users', icon: Users },
          { name: 'Departments', path: '/departments', icon: Building2 },
          { name: 'Designations', path: '/designations', icon: Briefcase },
          { name: 'Shifts', path: '/shifts', icon: Clock3 },
          { name: 'Attendance Logs', path: '/attendance', icon: Clock },
          { name: 'Leave Requests', path: '/leaves', icon: CalendarDays },
          { name: 'Payroll', path: '/payrolls', icon: DollarSign },
          { name: 'HR Reports', path: '/reports', icon: FileText },
          { name: 'Notifications', path: '/notifications', icon: Bell },
        ];

      case 'MANAGER':
        return [
          { name: 'Dashboard', path: '/', icon: LayoutDashboard },
          { name: 'Team Members', path: '/users', icon: Users },
          { name: 'Team Attendance', path: '/attendance', icon: Clock },
          { name: 'Team Leaves', path: '/leaves', icon: CalendarDays },
          { name: 'Team Reports', path: '/reports', icon: FileText },
          { name: 'Notifications', path: '/notifications', icon: Bell },
        ];

      case 'EMPLOYEE':
      default:
        return [
          { name: 'Dashboard', path: '/', icon: LayoutDashboard },
          { name: 'My Attendance', path: '/attendance', icon: Clock },
          { name: 'My Leaves', path: '/leaves', icon: CalendarDays },
          { name: 'My Payroll', path: '/payrolls', icon: DollarSign },
          { name: 'Notifications', path: '/notifications', icon: Bell },
          { name: 'My Profile', path: '/profile', icon: UserCheck },
        ];
    }
  };

  const navItems = getNavItems();

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <Clock size={28} style={{ color: 'var(--primary)' }} />
        <span>Attendify HR</span>
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
            >
              <Icon size={18} />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </nav>
    </aside>
  );
};

export default Sidebar;
