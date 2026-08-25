import React, { useEffect } from 'react';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

const Toast = ({ message, type = 'success', onClose, duration = 4000 }) => {
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => {
      onClose();
    }, duration);
    return () => clearTimeout(timer);
  }, [message, duration, onClose]);

  if (!message) return null;

  const icons = {
    success: <CheckCircle2 size={20} style={{ color: '#34d399' }} />,
    error: <AlertCircle size={20} style={{ color: '#f87171' }} />,
    info: <Info size={20} style={{ color: '#60a5fa' }} />,
  };

  return (
    <div className="toast-container">
      <div className={`toast toast-${type}`}>
        {icons[type]}
        <div style={{ flex: 1, fontSize: '0.875rem' }}>{message}</div>
        <button
          onClick={onClose}
          style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
        >
          <X size={16} />
        </button>
      </div>
    </div>
  );
};

export default Toast;
