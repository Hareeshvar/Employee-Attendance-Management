import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from './LoadingSpinner';
import { ShieldAlert } from 'lucide-react';

const ProtectedRoute = ({ allowedRoles = [], allowedPermissions = [], adminOnly = false }) => {
  const { isAuthenticated, role, permissions, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Handle legacy adminOnly flag
  if (adminOnly) {
    allowedRoles = ['ADMIN'];
  }

  const roleAuthorized = allowedRoles.length === 0 || allowedRoles.includes(role);
  const permAuthorized = allowedPermissions.length === 0 || allowedPermissions.some(p => permissions.includes(p));

  if (!roleAuthorized || !permAuthorized) {
    return (
      <div className="page-wrapper" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', textAlign: 'center' }}>
        <div style={{ background: 'rgba(239, 68, 68, 0.1)', padding: '1.5rem', borderRadius: '50%', marginBottom: '1.5rem', color: '#ef4444' }}>
          <ShieldAlert size={48} />
        </div>
        <h2 style={{ color: 'var(--text-primary, #f8fafc)', fontSize: '1.75rem', fontWeight: '700', marginBottom: '0.5rem' }}>
          403 - Access Forbidden
        </h2>
        <p style={{ color: 'var(--text-secondary, #94a3b8)', maxWidth: '450px', lineHeight: '1.6' }}>
          You do not have the required permissions ({allowedRoles.join(', ')}) to view this module. Your session remains active.
        </p>
      </div>
    );
  }

  return <Outlet />;
};

export default ProtectedRoute;
