import React, { useState } from 'react';
import { Calendar, TrendingUp } from 'lucide-react';
import { buildDailyTrendData } from './analyticsUtils';

const AttendanceTrendChart = ({ records = [], range = '7D', onRangeChange }) => {
  const [hoveredPoint, setHoveredPoint] = useState(null);

  const data = buildDailyTrendData(records, range);

  // SVG Dimensions
  const width = 680;
  const height = 240;
  const paddingX = 40;
  const paddingY = 30;

  const chartWidth = width - paddingX * 2;
  const chartHeight = height - paddingY * 2;

  // Find max value for scaling
  const maxVal = Math.max(1, ...data.map((d) => Math.max(d.present, d.late, d.absent, d.halfDay, d.total)));

  // Coordinate mapping helpers
  const getX = (index) => {
    if (data.length <= 1) return paddingX + chartWidth / 2;
    return paddingX + (index / (data.length - 1)) * chartWidth;
  };

  const getY = (val) => {
    return height - paddingY - (val / maxVal) * chartHeight;
  };

  // Generate SVG smooth bezier path d string
  const generatePathD = (key) => {
    if (data.length === 0) return '';
    const points = data.map((d, idx) => ({ x: getX(idx), y: getY(d[key]) }));

    if (points.length === 1) return `M ${points[0].x} ${points[0].y}`;

    let path = `M ${points[0].x} ${points[0].y}`;

    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i];
      const p1 = points[i + 1];
      const cp1x = p0.x + (p1.x - p0.x) / 2;
      const cp1y = p0.y;
      const cp2x = p0.x + (p1.x - p0.x) / 2;
      const cp2y = p1.y;
      path += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p1.x} ${p1.y}`;
    }

    return path;
  };

  const generateAreaD = (key) => {
    const lineD = generatePathD(key);
    if (!lineD) return '';
    const firstX = getX(0);
    const lastX = getX(data.length - 1);
    const bottomY = height - paddingY;
    return `${lineD} L ${lastX} ${bottomY} L ${firstX} ${bottomY} Z`;
  };

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
        gap: '1rem',
      }}
    >
      {/* Header & Controls */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <div
            style={{
              width: '34px',
              height: '34px',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(59, 130, 246, 0.15)',
              border: '1px solid rgba(59, 130, 246, 0.3)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--primary)',
            }}
          >
            <TrendingUp size={18} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--text-primary)' }}>
              Attendance Activity Trend
            </h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Daily log trajectory over time
            </p>
          </div>
        </div>

        {/* Range Switcher Tabs */}
        <div
          style={{
            display: 'inline-flex',
            background: 'rgba(15, 21, 35, 0.8)',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-md)',
            padding: '3px',
            gap: '3px',
          }}
        >
          <button
            type="button"
            className="btn"
            style={{
              padding: '0.35rem 0.85rem',
              fontSize: '0.775rem',
              fontWeight: 700,
              borderRadius: 'var(--radius-sm)',
              background: range === '7D' ? 'var(--primary-gradient)' : 'transparent',
              color: range === '7D' ? '#fff' : 'var(--text-secondary)',
              border: 'none',
              boxShadow: range === '7D' ? '0 4px 12px rgba(59, 130, 246, 0.3)' : 'none',
            }}
            onClick={() => onRangeChange && onRangeChange('7D')}
          >
            7 Days
          </button>

          <button
            type="button"
            className="btn"
            style={{
              padding: '0.35rem 0.85rem',
              fontSize: '0.775rem',
              fontWeight: 700,
              borderRadius: 'var(--radius-sm)',
              background: range === '30D' ? 'var(--primary-gradient)' : 'transparent',
              color: range === '30D' ? '#fff' : 'var(--text-secondary)',
              border: 'none',
              boxShadow: range === '30D' ? '0 4px 12px rgba(59, 130, 246, 0.3)' : 'none',
            }}
            onClick={() => onRangeChange && onRangeChange('30D')}
          >
            30 Days
          </button>
        </div>
      </div>

      {/* SVG Container */}
      <div style={{ position: 'relative', width: '100%', overflowX: 'auto' }}>
        <svg
          viewBox={`0 0 ${width} ${height}`}
          style={{ width: '100%', height: 'auto', minWidth: '480px', overflow: 'visible' }}
        >
          <defs>
            <linearGradient id="presentGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#34d399" stopOpacity="0.35" />
              <stop offset="100%" stopColor="#34d399" stopOpacity="0.0" />
            </linearGradient>
            <linearGradient id="lateGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#fbbf24" stopOpacity="0.25" />
              <stop offset="100%" stopColor="#fbbf24" stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Grid lines */}
          {[0, 0.33, 0.66, 1].map((ratio, i) => {
            const y = paddingY + ratio * chartHeight;
            return (
              <line
                key={i}
                x1={paddingX}
                y1={y}
                x2={width - paddingX}
                y2={y}
                stroke="rgba(255, 255, 255, 0.06)"
                strokeDasharray="4 4"
              />
            );
          })}

          {/* Present Area & Line */}
          <path d={generateAreaD('present')} fill="url(#presentGrad)" />
          <path
            d={generatePathD('present')}
            fill="none"
            stroke="#34d399"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Late Line */}
          <path
            d={generatePathD('late')}
            fill="none"
            stroke="#fbbf24"
            strokeWidth="2"
            strokeDasharray="3 3"
            strokeLinecap="round"
          />

          {/* Interactive Data Points */}
          {data.map((item, idx) => {
            const cx = getX(idx);
            const cy = getY(item.present);
            const isHovered = hoveredPoint && hoveredPoint.date === item.date;

            return (
              <g
                key={item.date}
                style={{ cursor: 'pointer' }}
                onMouseEnter={() => setHoveredPoint(item)}
                onMouseLeave={() => setHoveredPoint(null)}
              >
                {/* Vertical hover guide line */}
                {isHovered && (
                  <line
                    x1={cx}
                    y1={paddingY}
                    x2={cx}
                    y2={height - paddingY}
                    stroke="rgba(255, 255, 255, 0.2)"
                    strokeDasharray="2 2"
                  />
                )}

                {/* Point dot */}
                <circle
                  cx={cx}
                  cy={cy}
                  r={isHovered ? 6 : 4}
                  fill="#34d399"
                  stroke="#070a11"
                  strokeWidth="2"
                  style={{ transition: 'all 0.15s ease' }}
                />

                {/* X Axis Label */}
                {(range === '7D' || idx % 5 === 0 || idx === data.length - 1) && (
                  <text
                    x={cx}
                    y={height - 8}
                    textAnchor="middle"
                    fill="var(--text-muted)"
                    fontSize="11"
                    fontWeight="600"
                  >
                    {item.label}
                  </text>
                )}
              </g>
            );
          })}
        </svg>

        {/* Hover Tooltip Overlay */}
        {hoveredPoint && (
          <div
            style={{
              position: 'absolute',
              top: '10px',
              right: '10px',
              background: 'var(--bg-card-solid)',
              border: '1px solid var(--border-highlight)',
              borderRadius: 'var(--radius-md)',
              padding: '0.65rem 1rem',
              boxShadow: 'var(--shadow-md)',
              zIndex: 10,
              fontSize: '0.8rem',
              pointerEvents: 'none',
              animation: 'fadeIn 0.15s ease-out',
            }}
          >
            <div style={{ fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
              {hoveredPoint.date} ({hoveredPoint.label})
            </div>
            <div style={{ display: 'flex', gap: '0.85rem', fontSize: '0.775rem' }}>
              <span style={{ color: '#34d399', fontWeight: 600 }}>Present: {hoveredPoint.present}</span>
              <span style={{ color: '#fbbf24', fontWeight: 600 }}>Late: {hoveredPoint.late}</span>
              <span style={{ color: '#f87171', fontWeight: 600 }}>Absent: {hoveredPoint.absent}</span>
            </div>
          </div>
        )}
      </div>

      {/* Chart Legend */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1.5rem', fontSize: '0.775rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <div style={{ width: '12px', height: '12px', borderRadius: '50%', background: '#34d399' }} />
          <span style={{ color: 'var(--text-secondary)', fontWeight: 600 }}>Present Logs</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <div style={{ width: '12px', height: '2px', background: '#fbbf24' }} />
          <span style={{ color: 'var(--text-secondary)', fontWeight: 600 }}>Late Logs</span>
        </div>
      </div>
    </div>
  );
};

export default AttendanceTrendChart;
