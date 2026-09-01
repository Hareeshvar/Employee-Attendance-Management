import React from 'react';
import { LogOut, UserCheck, Shield } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { firstName, lastName, username, role, logout } = useAuth();
  const displayName = firstName ? `${firstName} ${lastName || ''}`.trim() : (username || 'User');

  const getRoleBadgeStyle = () => {
    switch (role) {
      case 'ADMIN': return { background: 'rgba(239, 68, 68, 0.15)', color: '#ef4444' };
      case 'HR': return { background: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6' };
      case 'MANAGER': return { background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b' };
      case 'EMPLOYEE': default: return { background: 'rgba(16, 185, 129, 0.15)', color: '#10b981' };
    }
  };

  const badgeStyle = getRoleBadgeStyle();

  return (
    <header className="navbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
          Attendance & HR Portal
        </h2>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
        <Link to="/profile" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div
            style={{
              width: 36,
              height: 36,
              borderRadius: '50%',
              background: 'var(--primary-gradient)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 700,
              fontSize: '0.9rem',
              color: '#fff',
            }}
          >
            {displayName.charAt(0).toUpperCase()}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-primary)' }}>
              {displayName}
            </span>
            <span
              style={{
                fontSize: '0.7rem',
                fontWeight: 700,
                padding: '0.1rem 0.4rem',
                borderRadius: '4px',
                width: 'fit-content',
                ...badgeStyle
              }}
            >
              {role || 'EMPLOYEE'}
            </span>
          </div>
        </Link>

        <button
          className="btn btn-secondary btn-sm"
          onClick={logout}
          title="Sign out of attendance portal"
        >
          <LogOut size={16} />
          <span>Logout</span>
        </button>
      </div>
    </header>
  );
};

export default Navbar;
