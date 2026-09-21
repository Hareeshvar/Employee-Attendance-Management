import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  X,
  Clock,
  Clock3,
  Users,
  Building2,
  Briefcase,
  MapPin,
  Calendar,
  DollarSign,
  FileText,
  Bell,
  Shield,
  ShieldAlert,
  User,
  LayoutDashboard,
  ArrowRight,
  Sparkles,
  CornerDownLeft,
  Loader2,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../services/userService';
import { shiftService } from '../services/shiftService';

const ALL_PAGES = [
  {
    id: 'page-dashboard',
    title: 'Dashboard Overview',
    path: '/dashboard',
    icon: LayoutDashboard,
    category: 'Navigation',
    description: 'Real-time attendance metrics, stats & workforce analytics',
    keywords: ['home', 'stats', 'analytics', 'overview', 'metrics', 'chart', 'summary'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
  },
  {
    id: 'page-attendance',
    title: 'Attendance Punch Station',
    path: '/attendance',
    icon: Clock,
    category: 'Navigation',
    description: 'Clock in / clock out, biometric logs, daily timesheets',
    keywords: ['punch', 'check in', 'check out', 'clock in', 'timesheet', 'history', 'log', 'daily'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
  },
  {
    id: 'page-leaves',
    title: 'Leave Management',
    path: '/leaves',
    icon: Calendar,
    category: 'Navigation',
    description: 'Apply for leave, track requests, review pending approvals',
    keywords: ['time off', 'vacation', 'sick', 'holiday', 'absence', 'apply', 'request', 'approval'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
  },
  {
    id: 'page-shifts',
    title: 'Work Shift Management',
    path: '/shifts',
    icon: Clock3,
    category: 'Navigation',
    description: 'Configure shift templates, timings, assign work schedules',
    keywords: ['shift', 'schedule', 'timing', 'assign', 'morning', 'evening', 'night', 'grace', 'roster'],
    allowedRoles: ['ADMIN', 'HR'],
  },
  {
    id: 'page-users',
    title: 'Staff Directory & Employees',
    path: '/users',
    icon: Users,
    category: 'Navigation',
    description: 'Manage staff accounts, roles, departments, designations',
    keywords: ['staff', 'employee', 'user', 'people', 'team', 'account', 'worker', 'directory'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER'],
  },
  {
    id: 'page-workplaces',
    title: 'Geofenced Workplaces',
    path: '/workplaces',
    icon: MapPin,
    category: 'Navigation',
    description: 'Office GPS coordinates, geofence radius & boundary validation',
    keywords: ['office', 'location', 'gps', 'geofence', 'radius', 'site', 'workplace', 'fence'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
  },
  {
    id: 'page-departments',
    title: 'Departments',
    path: '/departments',
    icon: Building2,
    category: 'Navigation',
    description: 'Organizational divisions and operational department setup',
    keywords: ['department', 'division', 'dept', 'org', 'business unit'],
    allowedRoles: ['ADMIN', 'HR'],
  },
  {
    id: 'page-designations',
    title: 'Designations',
    path: '/designations',
    icon: Briefcase,
    category: 'Navigation',
    description: 'Job positions, corporate titles and hierarchy levels',
    keywords: ['designation', 'job', 'position', 'title', 'role', 'level'],
    allowedRoles: ['ADMIN', 'HR'],
  },
  {
    id: 'page-payrolls',
    title: 'Payroll & Salary Slips',
    path: '/payrolls',
    icon: DollarSign,
    category: 'Navigation',
    description: 'Salary disbursement, payslip generation, deductions',
    keywords: ['payroll', 'salary', 'payslip', 'wages', 'compensation', 'pay', 'slip'],
    allowedRoles: ['ADMIN', 'HR', 'EMPLOYEE'],
  },
  {
    id: 'page-reports',
    title: 'Reports & Export Analytics',
    path: '/reports',
    icon: FileText,
    category: 'Navigation',
    description: 'Attendance summaries, CSV export, monthly staff reports',
    keywords: ['report', 'export', 'csv', 'summary', 'analytics', 'download', 'pdf'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
  },
  {
    id: 'page-notifications',
    title: 'Notifications & Alerts',
    path: '/notifications',
    icon: Bell,
    category: 'Navigation',
    description: 'System announcements, leave approvals, broadcast notices',
    keywords: ['bell', 'alert', 'notice', 'announcement', 'message', 'inbox'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
  },
  {
    id: 'page-audit-logs',
    title: 'Security Audit Logs',
    path: '/audit-logs',
    icon: ShieldAlert,
    category: 'Navigation',
    description: 'Immutable security trails, access logs, compliance audit',
    keywords: ['audit', 'security', 'log', 'trail', 'history', 'event', 'trace'],
    allowedRoles: ['ADMIN', 'HR'],
  },
  {
    id: 'page-roles',
    title: 'Roles & RBAC Permissions',
    path: '/roles',
    icon: Shield,
    category: 'Navigation',
    description: 'Configure security roles and system permission privileges',
    keywords: ['role', 'permission', 'rbac', 'access', 'admin', 'authority', 'security'],
    allowedRoles: ['ADMIN'],
  },
  {
    id: 'page-profile',
    title: 'User Profile & Settings',
    path: '/profile',
    icon: User,
    category: 'Navigation',
    description: 'Account settings, password updates & user details',
    keywords: ['profile', 'account', 'settings', 'password', 'me', 'security'],
    allowedRoles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'],
  },
];

const GlobalSearch = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  const [query, setQuery] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [results, setResults] = useState([]);
  const [loadingData, setLoadingData] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);

  const containerRef = useRef(null);
  const inputRef = useRef(null);

  // Accessible pages for current user
  const availablePages = ALL_PAGES.filter(
    (page) => !page.allowedRoles || page.allowedRoles.includes(role || 'EMPLOYEE')
  );

  // Global shortcut Ctrl+K or / to focus search
  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        inputRef.current?.focus();
        setIsOpen(true);
      } else if (
        e.key === '/' &&
        !['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)
      ) {
        e.preventDefault();
        inputRef.current?.focus();
        setIsOpen(true);
      } else if (e.key === 'Escape' && isOpen) {
        setIsOpen(false);
        inputRef.current?.blur();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  // Click outside listener
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Search logic
  useEffect(() => {
    const q = query.trim().toLowerCase();

    if (!q) {
      // Default: top 5 quick navigation links
      setResults(availablePages.slice(0, 5));
      setActiveIndex(0);
      return;
    }

    // 1. Filter local pages
    const pageMatches = availablePages.filter((page) => {
      const matchTitle = page.title.toLowerCase().includes(q);
      const matchDesc = page.description.toLowerCase().includes(q);
      const matchKeywords = page.keywords.some((kw) => kw.toLowerCase().includes(q));
      return matchTitle || matchDesc || matchKeywords;
    });

    let isMounted = true;
    setLoadingData(true);

    const timer = setTimeout(async () => {
      let staffMatches = [];
      let shiftMatches = [];

      try {
        const promises = [];

        // Search staff if permitted
        if (['ADMIN', 'HR', 'MANAGER'].includes(role)) {
          promises.push(
            userService
              .getAll({ search: q, page: 0, size: 4 })
              .then((res) => {
                const list = res?.content || (Array.isArray(res) ? res : []);
                return list.map((u) => ({
                  id: `user-${u.userId}`,
                  title: `${u.firstName} ${u.lastName}`,
                  subtitle: `${u.email} • ${u.roleName || 'Employee'} (#${u.userId})`,
                  path: `/users?search=${encodeURIComponent(u.username || u.firstName)}`,
                  icon: Users,
                  category: 'Staff Directory',
                  badge: u.roleName || 'EMPLOYEE',
                }));
              })
              .catch(() => [])
          );
        }

        // Search shifts if permitted
        if (['ADMIN', 'HR'].includes(role)) {
          promises.push(
            shiftService
              .getAll()
              .then((shifts) => {
                if (!Array.isArray(shifts)) return [];
                return shifts
                  .filter((s) => s.shiftName?.toLowerCase().includes(q))
                  .slice(0, 3)
                  .map((s) => ({
                    id: `shift-${s.shiftId || s.id}`,
                    title: s.shiftName,
                    subtitle: `${s.startTime || '09:00'} - ${s.endTime || '17:00'} • ${s.workingHours} hrs (Grace ${s.graceMinutes ?? 15}m)`,
                    path: '/shifts',
                    icon: Clock3,
                    category: 'Shift Templates',
                    badge: `${s.workingHours}h`,
                  }));
              })
              .catch(() => [])
          );
        }

        const [staffRes = [], shiftRes = []] = await Promise.all(promises);
        if (isMounted) {
          staffMatches = staffRes;
          shiftMatches = shiftRes;
        }
      } catch (err) {
        console.error('Search error', err);
      } finally {
        if (isMounted) {
          setLoadingData(false);
          const combined = [...pageMatches, ...staffMatches, ...shiftMatches];
          setResults(combined);
          setActiveIndex(0);
        }
      }
    }, 200);

    return () => {
      isMounted = false;
      clearTimeout(timer);
    };
  }, [query, role]);

  // Keyboard navigation within dropdown
  const handleKeyDown = (e) => {
    if (!isOpen || results.length === 0) return;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex((prev) => (prev + 1) % results.length);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex((prev) => (prev - 1 + results.length) % results.length);
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (results[activeIndex]) {
        handleSelect(results[activeIndex]);
      }
    }
  };

  const handleSelect = (item) => {
    setIsOpen(false);
    setQuery('');
    inputRef.current?.blur();
    navigate(item.path);
  };

  return (
    <div
      ref={containerRef}
      style={{
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        flex: 1,
        maxWidth: '440px',
      }}
    >
      {/* Search Input Bar */}
      <div style={{ position: 'relative', width: '100%' }}>
        <Search
          size={16}
          style={{
            position: 'absolute',
            left: '0.85rem',
            top: '50%',
            transform: 'translateY(-50%)',
            color: isOpen ? 'var(--primary)' : 'var(--text-muted)',
            transition: 'color 0.2s ease',
          }}
        />

        <input
          ref={inputRef}
          type="text"
          className="form-input"
          style={{
            width: '100%',
            paddingLeft: '2.4rem',
            paddingRight: query ? '2.4rem' : '4.5rem',
            paddingTop: '0.55rem',
            paddingBottom: '0.55rem',
            fontSize: '0.85rem',
            borderRadius: 'var(--radius-full)',
            background: isOpen ? 'rgba(15, 23, 42, 0.95)' : 'rgba(15, 21, 35, 0.6)',
            borderColor: isOpen ? 'var(--primary)' : 'rgba(255, 255, 255, 0.1)',
            boxShadow: isOpen ? '0 0 0 3px rgba(59, 130, 246, 0.25)' : 'none',
            transition: 'all 0.2s ease',
          }}
          placeholder="Search pages, staff, shifts..."
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            if (!isOpen) setIsOpen(true);
          }}
          onFocus={() => setIsOpen(true)}
          onKeyDown={handleKeyDown}
        />

        {/* Clear Button or Ctrl+K shortcut badge */}
        {query ? (
          <button
            type="button"
            onClick={() => {
              setQuery('');
              inputRef.current?.focus();
            }}
            style={{
              position: 'absolute',
              right: '0.75rem',
              top: '50%',
              transform: 'translateY(-50%)',
              background: 'rgba(255, 255, 255, 0.1)',
              border: 'none',
              borderRadius: '50%',
              width: 20,
              height: 20,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
            }}
            title="Clear search"
          >
            <X size={12} />
          </button>
        ) : (
          <div
            onClick={() => {
              inputRef.current?.focus();
              setIsOpen(true);
            }}
            style={{
              position: 'absolute',
              right: '0.75rem',
              top: '50%',
              transform: 'translateY(-50%)',
              display: 'flex',
              alignItems: 'center',
              gap: '2px',
              padding: '0.15rem 0.4rem',
              background: 'rgba(255, 255, 255, 0.08)',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              borderRadius: '4px',
              fontSize: '0.675rem',
              fontWeight: 700,
              color: 'var(--text-muted)',
              pointerEvents: 'none',
              userSelect: 'none',
            }}
          >
            <span>Ctrl</span>
            <span>K</span>
          </div>
        )}
      </div>

      {/* Floating Results Panel */}
      {isOpen && (
        <div
          style={{
            position: 'absolute',
            top: 'calc(100% + 8px)',
            left: 0,
            width: 'min(520px, calc(100vw - 2rem))',
            maxWidth: 'calc(100vw - 2rem)',
            background: 'rgba(15, 23, 42, 0.96)',
            backdropFilter: 'blur(24px)',
            WebkitBackdropFilter: 'blur(24px)',
            border: '1px solid rgba(255, 255, 255, 0.12)',
            borderRadius: '16px',
            boxShadow: '0 20px 50px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.05)',
            zIndex: 100,
            overflow: 'hidden',
            animation: 'fadeIn 0.15s ease-out',
          }}
        >
          {/* Header Label */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '0.65rem 1rem',
              borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
              fontSize: '0.72rem',
              fontWeight: 700,
              color: 'var(--text-muted)',
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
            }}
          >
            <span>
              {query ? `Matching Results (${results.length})` : 'Quick Navigation'}
            </span>
            {loadingData && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--primary)' }}>
                <Loader2 size={12} className="spin" />
                <span>Searching...</span>
              </div>
            )}
          </div>

          {/* Results List */}
          <div
            style={{
              maxHeight: '380px',
              overflowY: 'auto',
              padding: '0.4rem',
            }}
          >
            {results.length === 0 && !loadingData ? (
              <div
                style={{
                  padding: '2rem 1rem',
                  textAlign: 'center',
                  color: 'var(--text-muted)',
                  fontSize: '0.85rem',
                }}
              >
                <Search size={28} style={{ margin: '0 auto 0.5rem', opacity: 0.4 }} />
                <p style={{ fontWeight: 600, color: 'var(--text-secondary)' }}>
                  No results found for "{query}"
                </p>
                <p style={{ fontSize: '0.75rem', marginTop: '0.25rem' }}>
                  Try searching for keywords like "attendance", "shifts", "leaves", or staff names.
                </p>
              </div>
            ) : (
              results.map((item, idx) => {
                const IconComponent = item.icon || ArrowRight;
                const isSelected = idx === activeIndex;

                return (
                  <div
                    key={item.id}
                    onClick={() => handleSelect(item)}
                    onMouseEnter={() => setActiveIndex(idx)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.85rem',
                      padding: '0.65rem 0.85rem',
                      borderRadius: '10px',
                      cursor: 'pointer',
                      background: isSelected
                        ? 'linear-gradient(90deg, rgba(59, 130, 246, 0.18), rgba(99, 102, 241, 0.12))'
                        : 'transparent',
                      borderLeft: isSelected ? '3px solid var(--primary)' : '3px solid transparent',
                      transition: 'all 0.12s ease',
                    }}
                  >
                    {/* Item Icon */}
                    <div
                      style={{
                        width: 34,
                        height: 34,
                        borderRadius: '8px',
                        background: isSelected
                          ? 'rgba(59, 130, 246, 0.25)'
                          : 'rgba(255, 255, 255, 0.05)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: isSelected ? 'var(--primary)' : 'var(--text-secondary)',
                        flexShrink: 0,
                      }}
                    >
                      <IconComponent size={17} />
                    </div>

                    {/* Content */}
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span
                          style={{
                            fontSize: '0.85rem',
                            fontWeight: isSelected ? 700 : 600,
                            color: isSelected ? '#fff' : 'var(--text-primary)',
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                          }}
                        >
                          {item.title}
                        </span>

                        {item.badge && (
                          <span
                            style={{
                              fontSize: '0.65rem',
                              fontWeight: 700,
                              padding: '0.1rem 0.4rem',
                              borderRadius: '4px',
                              background: 'rgba(59, 130, 246, 0.15)',
                              color: '#60a5fa',
                              border: '1px solid rgba(59, 130, 246, 0.3)',
                            }}
                          >
                            {item.badge}
                          </span>
                        )}
                      </div>

                      <p
                        style={{
                          fontSize: '0.725rem',
                          color: 'var(--text-muted)',
                          margin: '0.15rem 0 0 0',
                          whiteSpace: 'nowrap',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                        }}
                      >
                        {item.subtitle || item.description}
                      </p>
                    </div>

                    {/* Category pill / Enter icon */}
                    <div
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.4rem',
                        flexShrink: 0,
                      }}
                    >
                      <span
                        style={{
                          fontSize: '0.675rem',
                          fontWeight: 600,
                          color: 'var(--text-muted)',
                          background: 'rgba(255, 255, 255, 0.04)',
                          padding: '0.15rem 0.45rem',
                          borderRadius: '4px',
                        }}
                      >
                        {item.category}
                      </span>
                      {isSelected && (
                        <CornerDownLeft size={13} style={{ color: 'var(--primary)' }} />
                      )}
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Footer Guide */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '0.55rem 1rem',
              background: 'rgba(0, 0, 0, 0.25)',
              borderTop: '1px solid rgba(255, 255, 255, 0.06)',
              fontSize: '0.7rem',
              color: 'var(--text-muted)',
            }}
          >
            <div style={{ display: 'flex', gap: '0.85rem' }}>
              <span>
                <strong style={{ color: 'var(--text-primary)' }}>↑↓</strong> navigate
              </span>
              <span>
                <strong style={{ color: 'var(--text-primary)' }}>↵</strong> select
              </span>
              <span>
                <strong style={{ color: 'var(--text-primary)' }}>esc</strong> close
              </span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '3px' }}>
              <Sparkles size={11} style={{ color: 'var(--primary)' }} />
              <span>Global Spotlight</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default GlobalSearch;
