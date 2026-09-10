import React from 'react';
import { PieChart } from 'lucide-react';
import { calculateAttendanceRate } from './analyticsUtils';

const AttendanceBreakdownChart = ({ records = [] }) => {
  const { presentCount, lateCount, absentCount, halfDayCount, total } = calculateAttendanceRate(records);

  const items = [
    { label: 'PRESENT', count: presentCount, color: '#34d399', bg: 'rgba(16, 185, 129, 0.15)' },
    { label: 'LATE', count: lateCount, color: '#fbbf24', bg: 'rgba(245, 158, 11, 0.15)' },
    { label: 'ABSENT', count: absentCount, color: '#f87171', bg: 'rgba(239, 68, 68, 0.15)' },
    { label: 'HALF DAY', count: halfDayCount, color: '#60a5fa', bg: 'rgba(59, 130, 246, 0.15)' },
  ];

  return (
    <div
      className="card"
      style={{
        background: 'var(--bg-card)',
        backdropFilter: 'blur(16px)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-xl)',
        padding: '1.5rem',
        boxShadow: 'var(--shadow-sm)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '1.25rem' }}>
        <div
          style={{
            width: '34px',
            height: '34px',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(139, 92, 246, 0.15)',
            border: '1px solid rgba(139, 92, 246, 0.3)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--accent-purple)',
          }}
        >
          <PieChart size={18} />
        </div>
        <div>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--text-primary)' }}>
            Status Distribution
          </h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Breakdown of logged attendance states
          </p>
        </div>
      </div>

      {/* Segmented Distribution Bar */}
      <div
        style={{
          height: '14px',
          width: '100%',
          background: 'rgba(255, 255, 255, 0.08)',
          borderRadius: 'var(--radius-full)',
          overflow: 'hidden',
          display: 'flex',
          marginBottom: '1.5rem',
        }}
      >
        {total > 0 &&
          items.map((item) => {
            const pct = (item.count / total) * 100;
            if (pct <= 0) return null;
            return (
              <div
                key={item.label}
                title={`${item.label}: ${item.count} (${Math.round(pct)}%)`}
                style={{
                  width: `${pct}%`,
                  height: '100%',
                  background: item.color,
                  transition: 'width 0.4s ease',
                }}
              />
            );
          })}
      </div>

      {/* Detailed Status Breakdown List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {items.map((item) => {
          const pct = total > 0 ? Math.round((item.count / total) * 100) : 0;

          return (
            <div
              key={item.label}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.6rem 0.85rem',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(15, 21, 35, 0.6)',
                border: '1px solid var(--border-color)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <div
                  style={{
                    width: '10px',
                    height: '10px',
                    borderRadius: '50%',
                    background: item.color,
                  }}
                />
                <span style={{ fontSize: '0.825rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                  {item.label}
                </span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 800, color: item.color }}>
                  {item.count}
                </span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', minWidth: '36px', textAlign: 'right' }}>
                  {pct}%
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default AttendanceBreakdownChart;
