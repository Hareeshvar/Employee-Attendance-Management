import React from 'react';
import { Building2 } from 'lucide-react';
import { buildDepartmentComparisonData } from './analyticsUtils';

const DepartmentAnalyticsChart = ({ records = [] }) => {
  const deptData = buildDepartmentComparisonData(records);

  if (deptData.length === 0) return null;

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
        marginTop: '1.5rem',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '1.25rem' }}>
        <div
          style={{
            width: '34px',
            height: '34px',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(168, 85, 247, 0.15)',
            border: '1px solid rgba(168, 85, 247, 0.3)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#a855f7',
          }}
        >
          <Building2 size={18} />
        </div>
        <div>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--text-primary)' }}>
            Department Attendance Comparison
          </h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Real-time attendance rates across organization departments
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {deptData.map((d) => (
          <div key={d.deptName}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.35rem' }}>
              <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                {d.deptName}
              </span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  ({d.presentCount} P / {d.lateCount} L / {d.absentCount} A)
                </span>
                <span
                  style={{
                    fontSize: '0.85rem',
                    fontWeight: 800,
                    color: d.rate >= 90 ? '#34d399' : d.rate >= 75 ? '#fbbf24' : '#f87171',
                  }}
                >
                  {d.rate}%
                </span>
              </div>
            </div>

            <div
              style={{
                height: '8px',
                width: '100%',
                background: 'rgba(255, 255, 255, 0.08)',
                borderRadius: 'var(--radius-full)',
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  height: '100%',
                  width: `${Math.min(100, Math.max(0, d.rate))}%`,
                  background:
                    d.rate >= 90
                      ? 'linear-gradient(90deg, #34d399 0%, #059669 100%)'
                      : d.rate >= 75
                      ? 'linear-gradient(90deg, #fbbf24 0%, #d97706 100%)'
                      : 'linear-gradient(90deg, #f87171 0%, #dc2626 100%)',
                  borderRadius: 'var(--radius-full)',
                  transition: 'width 0.5s ease-out',
                }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DepartmentAnalyticsChart;
