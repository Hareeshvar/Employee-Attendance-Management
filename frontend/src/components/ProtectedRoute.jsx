import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ adminOnly = false }) => {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (adminOnly && !isAdmin) {
    return (
      <div className="page-wrapper" style={{ textAlign: 'center', paddingTop: '4rem' }}>
        <h2 style={{ color: 'var(--accent-rose)', marginBottom: '1rem' }}>403 - Access Forbidden</h2>
        <p style={{ color: 'var(--text-secondary)' }}>You do not have administrative permissions to view this module.</p>
      </div>
    );
  }

  return <Outlet />;
};

export default ProtectedRoute;
