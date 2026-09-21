import React from 'react';
import { LogOut, Bell, Shield, User, Menu } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import GlobalSearch from './GlobalSearch';

const Navbar = ({ onToggleSidebar }) => {
  const { firstName, lastName, username, role, logout } = useAuth();
  const navigate = useNavigate();
  const displayName = firstName ? `${firstName} ${lastName || ''}`.trim() : (username || 'User');

  const getRoleBadgeStyle = () => {
    switch (role) {
      case 'ADMIN': return { background: 'rgba(239, 68, 68, 0.15)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' };
      case 'HR': return { background: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.3)' };
      case 'MANAGER': return { background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b', border: '1px solid rgba(245, 158, 11, 0.3)' };
      case 'EMPLOYEE': default: return { background: 'rgba(16, 185, 129, 0.15)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)' };
    }
  };

  const badgeStyle = getRoleBadgeStyle();

  return (
    <header className="navbar">
      {/* Left side: Hamburger Toggle + Search */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: 1, minWidth: 0 }}>
        <button
          className="navbar-mobile-toggle btn btn-secondary btn-sm"
          onClick={onToggleSidebar}
          aria-label="Toggle navigation menu"
          style={{
            width: 38,
            height: 38,
            padding: 0,
            borderRadius: 'var(--radius-md)',
            flexShrink: 0,
          }}
        >
          <Menu size={20} />
        </button>

        {/* Global Interactive Search */}
        <GlobalSearch />
      </div>

      {/* Right Controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexShrink: 0 }}>
        {/* Notifications Icon Button */}
        <button
          className="btn btn-secondary btn-sm"
          style={{
            borderRadius: '50%',
            width: 38,
            height: 38,
            padding: 0,
            position: 'relative',
            flexShrink: 0,
          }}
          onClick={() => navigate('/notifications')}
          title="Notifications"
        >
          <Bell size={17} />
          <span
            style={{
              position: 'absolute',
              top: 6,
              right: 6,
              width: 8,
              height: 8,
              borderRadius: '50%',
              background: 'var(--primary)',
            }}
          ></span>
        </button>

        {/* User Profile Pill Link */}
        <Link to="/profile" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
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
              boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)',
              flexShrink: 0,
            }}
          >
            {displayName.charAt(0).toUpperCase()}
          </div>

          <div className="navbar-user-text" style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-primary)', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {displayName}
            </span>
            <span
              style={{
                fontSize: '0.65rem',
                fontWeight: 800,
                padding: '0.1rem 0.45rem',
                borderRadius: 'var(--radius-full)',
                width: 'fit-content',
                textTransform: 'uppercase',
                letterSpacing: '0.04em',
                marginTop: '0.1rem',
                ...badgeStyle
              }}
            >
              {role || 'EMPLOYEE'}
            </span>
          </div>
        </Link>

        {/* Logout Button */}
        <button
          className="btn btn-secondary btn-sm"
          onClick={logout}
          title="Sign out"
          style={{ flexShrink: 0 }}
        >
          <LogOut size={16} />
          <span className="navbar-logout-text">Logout</span>
        </button>
      </div>
    </header>
  );
};

export default Navbar;
