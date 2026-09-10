import React from 'react';

const StatusBadge = ({ status }) => {
  if (!status) return null;

  const normalized = String(status).toLowerCase();
  
  let className = 'badge-info';
  
  if (['present', 'active', 'approved', 'success'].includes(normalized)) {
    className = 'badge-success';
  } else if (['pending', 'late', 'half_day', 'warning'].includes(normalized)) {
    className = 'badge-warning';
  } else if (['absent', 'inactive', 'suspended', 'rejected', 'danger'].includes(normalized)) {
    className = 'badge-danger';
  }

  return (
    <span className={`badge ${className}`}>
      <span
        style={{
          width: 6,
          height: 6,
          borderRadius: '50%',
          background: 'currentColor',
          display: 'inline-block',
        }}
      ></span>
      <span>{status}</span>
    </span>
  );
};

export default StatusBadge;
