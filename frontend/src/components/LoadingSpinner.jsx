import React from 'react';
import { AlertTriangle, Database } from 'lucide-react';

export const LoadingSpinner = ({ text = 'Loading data...' }) => (
  <div className="state-container">
    <div className="spinner"></div>
    <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>{text}</p>
  </div>
);

export const EmptyState = ({ title = 'No records found', description = 'There are no items to display at this moment.', action }) => (
  <div className="state-container">
    <Database size={48} style={{ opacity: 0.4, color: 'var(--primary)' }} />
    <h3 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)' }}>{title}</h3>
    <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', maxWidth: '400px' }}>{description}</p>
    {action && <div style={{ marginTop: '0.5rem' }}>{action}</div>}
  </div>
);

export const ErrorState = ({ title = 'Failed to load data', message, onRetry }) => (
  <div className="state-container" style={{ border: '1px solid rgba(239, 68, 68, 0.2)', background: 'rgba(239, 68, 68, 0.05)', borderRadius: 'var(--radius-lg)' }}>
    <AlertTriangle size={44} style={{ color: 'var(--accent-rose)' }} />
    <h3 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--accent-rose)' }}>{title}</h3>
    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', maxWidth: '480px' }}>{message}</p>
    {onRetry && (
      <button className="btn btn-secondary btn-sm" onClick={onRetry} style={{ marginTop: '0.5rem' }}>
        Retry
      </button>
    )}
  </div>
);
