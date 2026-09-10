import React from 'react';
import { LogOut, Search, Bell, Shield, User } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
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
      {/* Search Input Bar Mockup */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flex: 1, maxWidth: '400px' }}>
        <div style={{ position: 'relative', width: '100%' }}>
          <Search
            size={16}
            style={{
              position: 'absolute',
              left: '0.85rem',
              top: '50%',
              transform: 'translateY(-50%)',
              color: 'var(--text-muted)',
            }}
          />
          <input
            type="text"
            className="form-input"
            style={{
              paddingLeft: '2.4rem',
              paddingTop: '0.5rem',
              paddingBottom: '0.5rem',
              fontSize: '0.825rem',
              borderRadius: 'var(--radius-full)',
              background: 'rgba(15, 21, 35, 0.6)',
            }}
            placeholder="Search records, staff, shifts..."
            readOnly
          />
        </div>
      </div>

      {/* Right Controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
        {/* Notifications Icon Button */}
        <button
          className="btn btn-secondary btn-sm"
          style={{
            borderRadius: '50%',
            width: 38,
            height: 38,
            padding: 0,
            position: 'relative',
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
        <Link to="/profile" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
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
            }}
          >
            {displayName.charAt(0).toUpperCase()}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-primary)' }}>
              {displayName}
            </span>
            <span
              style={{
                fontSize: '0.675rem',
                fontWeight: 800,
                padding: '0.1rem 0.5rem',
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
        >
          <LogOut size={16} />
          <span>Logout</span>
        </button>
      </div>
    </header>
  );
};

export default Navbar;
