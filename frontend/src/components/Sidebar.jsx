import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Clock,
  CalendarDays,
  Building2,
  Briefcase,
  Layers,
  DollarSign,
  Bell,
  FileText,
  Shield,
  Clock3,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Sidebar = () => {
  const { isAdmin } = useAuth();

  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard, role: 'all' },
    { name: 'Attendance', path: '/attendance', icon: Clock, role: 'all' },
    { name: 'Leaves', path: '/leaves', icon: CalendarDays, role: 'all' },
    { name: 'Notifications', path: '/notifications', icon: Bell, role: 'all' },
    
    // Admin Only Modules
    { name: 'Users', path: '/users', icon: Users, role: 'admin' },
    { name: 'Departments', path: '/departments', icon: Building2, role: 'admin' },
    { name: 'Designations', path: '/designations', icon: Briefcase, role: 'admin' },
    { name: 'Shifts', path: '/shifts', icon: Clock3, role: 'admin' },
    { name: 'Payroll', path: '/payrolls', icon: DollarSign, role: 'admin' },
    { name: 'Reports', path: '/reports', icon: FileText, role: 'admin' },
    { name: 'Roles', path: '/roles', icon: Shield, role: 'admin' },
  ];

  const filteredNavItems = navItems.filter(
    (item) => item.role === 'all' || (item.role === 'admin' && isAdmin)
  );

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <Clock size={28} style={{ color: 'var(--primary)' }} />
        <span>Attendify HR</span>
      </div>

      <nav className="sidebar-nav">
        {filteredNavItems.map((item) => {
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
