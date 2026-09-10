import React, { useState } from 'react';
import { Sparkles, AlertCircle, Inbox } from 'lucide-react';
import AttendanceRateCard from './AttendanceRateCard';
import AttendanceTrendChart from './AttendanceTrendChart';
import AttendanceBreakdownChart from './AttendanceBreakdownChart';
import DepartmentAnalyticsChart from './DepartmentAnalyticsChart';
import { filterRecordsByRange, calculateAvgWorkingHours } from './analyticsUtils';

const AttendanceAnalytics = ({ records = [], role = 'EMPLOYEE', loading = false, error = null, onRetry }) => {
  const [range, setRange] = useState('7D');

  if (loading) {
    return (
      <div
        className="card"
        style={{
          padding: '2.5rem',
          marginBottom: '2rem',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '1rem',
        }}
      >
        <div className="spinner" />
        <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 600 }}>
          Calculating graphical attendance analytics...
        </span>
      </div>
    );
  }

  if (error) {
    return (
      <div
        className="card"
        style={{
          padding: '2rem',
          marginBottom: '2rem',
          border: '1px solid rgba(239, 68, 68, 0.3)',
          background: 'rgba(239, 68, 68, 0.08)',
          textAlign: 'center',
        }}
      >
        <AlertCircle size={32} style={{ color: '#f87171', marginBottom: '0.5rem' }} />
        <h4 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
          Unable to load attendance analytics
        </h4>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
          {error}
        </p>
        {onRetry && (
          <button className="btn btn-secondary btn-sm" onClick={onRetry}>
            Try Again
          </button>
        )}
      </div>
    );
  }

  const filteredRecords = filterRecordsByRange(records, range);
  const avgWorkingHours = calculateAvgWorkingHours(filteredRecords);

  const isManagementRole = role === 'ADMIN' || role === 'HR';

  return (
    <div style={{ marginBottom: '2rem' }}>
      {/* Section Header */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '1.25rem',
          flexWrap: 'wrap',
          gap: '0.75rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Sparkles size={20} style={{ color: 'var(--primary)' }} />
          <h2 style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-primary)' }}>
            Attendance Graphical Analytics
          </h2>
        </div>

        {role === 'EMPLOYEE' && avgWorkingHours > 0 && (
          <div
            style={{
              padding: '0.4rem 0.85rem',
              borderRadius: 'var(--radius-full)',
              background: 'rgba(59, 130, 246, 0.12)',
              border: '1px solid rgba(59, 130, 246, 0.25)',
              fontSize: '0.8rem',
              fontWeight: 700,
              color: 'var(--primary)',
            }}
          >
            Avg Working Hours: {avgWorkingHours} hrs/shift
          </div>
        )}
      </div>

      {records.length === 0 ? (
        <div
          className="card"
          style={{
            padding: '3rem 2rem',
            textAlign: 'center',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '0.75rem',
            color: 'var(--text-muted)',
          }}
        >
          <Inbox size={40} style={{ opacity: 0.5 }} />
          <h4 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
            No Attendance Data Available
          </h4>
          <p style={{ fontSize: '0.85rem', maxWidth: '400px' }}>
            Attendance analytics graphs and metrics will automatically generate as soon as check-in or check-out logs exist in the system.
          </p>
        </div>
      ) : (
        <>
          {/* Upper Analytics Grid: Rate Card + Status Breakdown */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
              gap: '1.5rem',
              marginBottom: '1.5rem',
            }}
          >
            <AttendanceRateCard records={filteredRecords} role={role} />
            <AttendanceBreakdownChart records={filteredRecords} />
          </div>

          {/* Lower Main Trend Chart */}
          <AttendanceTrendChart
            records={records}
            range={range}
            onRangeChange={(newRange) => setRange(newRange)}
          />

          {/* Department Comparison Chart for Admin / HR */}
          {isManagementRole && <DepartmentAnalyticsChart records={filteredRecords} />}
        </>
      )}
    </div>
  );
};

export default AttendanceAnalytics;
