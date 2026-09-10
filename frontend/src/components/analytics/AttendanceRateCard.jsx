import React from 'react';
import { Award, TrendingUp, CheckCircle2, AlertTriangle, XCircle, Clock } from 'lucide-react';
import { calculateAttendanceRate } from './analyticsUtils';

const AttendanceRateCard = ({ records = [], role = 'EMPLOYEE' }) => {
  const { rate, presentCount, lateCount, absentCount, halfDayCount, total } = calculateAttendanceRate(records);

  const getScopeLabel = () => {
    switch (role) {
      case 'ADMIN': return 'Organization Attendance Rate';
      case 'HR': return 'Workforce Attendance Rate';
      case 'MANAGER': return 'Team Attendance Rate';
      case 'EMPLOYEE': default: return 'My Attendance Rate';
    }
  };

  const getRateColor = () => {
    if (rate >= 90) return '#34d399'; // Emerald
    if (rate >= 75) return '#fbbf24'; // Amber
    return '#f87171'; // Rose
  };

  return (
    <div
      className="card"
      style={{
        background: 'linear-gradient(135deg, rgba(22, 30, 46, 0.85) 0%, rgba(15, 21, 35, 0.9) 100%)',
        border: '1px solid var(--border-highlight)',
        borderRadius: 'var(--radius-xl)',
        padding: '1.5rem',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Background ambient glow circle */}
      <div
        style={{
          position: 'absolute',
          top: '-40px',
          right: '-40px',
          width: '140px',
          height: '140px',
          borderRadius: '50%',
          background: `radial-gradient(circle, ${getRateColor()}20 0%, transparent 70%)`,
          pointerEvents: 'none',
        }}
      />

      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 700 }}>
            {getScopeLabel()}
          </span>
          <div
            style={{
              width: '36px',
              height: '36px',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(59, 130, 246, 0.12)',
              border: '1px solid rgba(59, 130, 246, 0.25)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--primary)',
            }}
          >
            <Award size={20} />
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem', marginBottom: '0.75rem' }}>
          <span style={{ fontSize: '2.5rem', fontWeight: 800, color: getRateColor(), letterSpacing: '-0.03em' }}>
            {total > 0 ? `${rate}%` : 'N/A'}
          </span>
          {total > 0 && (
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>
              ({total} log{total > 1 ? 's' : ''})
            </span>
          )}
        </div>

        {/* Progress bar */}
        <div
          style={{
            height: '8px',
            width: '100%',
            background: 'rgba(255, 255, 255, 0.08)',
            borderRadius: 'var(--radius-full)',
            overflow: 'hidden',
            marginBottom: '1.25rem',
          }}
        >
          <div
            style={{
              height: '100%',
              width: `${total > 0 ? Math.min(100, Math.max(0, rate)) : 0}%`,
              background: `linear-gradient(90deg, ${getRateColor()} 0%, #6366f1 100%)`,
              borderRadius: 'var(--radius-full)',
              transition: 'width 0.6s cubic-bezier(0.16, 1, 0.3, 1)',
            }}
          />
        </div>
      </div>

      {/* Status Summary Pills */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.65rem' }}>
        <div
          style={{
            padding: '0.5rem 0.75rem',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
          }}
        >
          <CheckCircle2 size={15} style={{ color: '#34d399' }} />
          <div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600 }}>PRESENT</div>
            <div style={{ fontSize: '0.9rem', fontWeight: 800, color: '#34d399' }}>{presentCount}</div>
          </div>
        </div>

        <div
          style={{
            padding: '0.5rem 0.75rem',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(245, 158, 11, 0.1)',
            border: '1px solid rgba(245, 158, 11, 0.2)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
          }}
        >
          <AlertTriangle size={15} style={{ color: '#fbbf24' }} />
          <div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600 }}>LATE</div>
            <div style={{ fontSize: '0.9rem', fontWeight: 800, color: '#fbbf24' }}>{lateCount}</div>
          </div>
        </div>

        <div
          style={{
            padding: '0.5rem 0.75rem',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(239, 68, 68, 0.1)',
            border: '1px solid rgba(239, 68, 68, 0.2)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
          }}
        >
          <XCircle size={15} style={{ color: '#f87171' }} />
          <div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600 }}>ABSENT</div>
            <div style={{ fontSize: '0.9rem', fontWeight: 800, color: '#f87171' }}>{absentCount}</div>
          </div>
        </div>

        <div
          style={{
            padding: '0.5rem 0.75rem',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(59, 130, 246, 0.1)',
            border: '1px solid rgba(59, 130, 246, 0.2)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
          }}
        >
          <Clock size={15} style={{ color: '#60a5fa' }} />
          <div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600 }}>HALF DAY</div>
            <div style={{ fontSize: '0.9rem', fontWeight: 800, color: '#60a5fa' }}>{halfDayCount}</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AttendanceRateCard;
