import React, { useState } from 'react';
import {
  Users,
  Clock,
  CalendarDays,
  CheckCircle2,
  AlertCircle,
  LogIn,
  LogOut,
  TrendingUp,
  Building2,
  DollarSign,
  Shield,
  Briefcase
} from 'lucide-react';

const DashboardPreview = () => {
  const [activeTab, setActiveTab] = useState('attendance');
  const [isCheckedIn, setIsCheckedIn] = useState(false);

  return (
    <div className="hero-mockup-wrapper">
      <div className="dashboard-mockup">
        {/* Browser Mockup Window Bar */}
        <div className="mockup-header">
          <div className="mockup-dots">
            <span className="mockup-dot dot-red"></span>
            <span className="mockup-dot dot-yellow"></span>
            <span className="mockup-dot dot-green"></span>
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: '0 auto', fontWeight: 600 }}>
            attendify.app/dashboard
          </span>
        </div>

        {/* Dashboard Canvas Container */}
        <div className="mockup-body">
          {/* Mock Sidebar */}
          <div className="mockup-sidebar">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', padding: '0.25rem' }}>
              <Clock size={18} style={{ color: 'var(--primary)' }} />
              <span style={{ fontWeight: 800, fontSize: '0.85rem' }}>Attendify</span>
            </div>

            <div
              className={`mockup-nav-item ${activeTab === 'attendance' ? 'active' : ''}`}
              onClick={() => setActiveTab('attendance')}
              style={{ cursor: 'pointer' }}
            >
              <Clock size={14} />
              <span>Dashboard</span>
            </div>

            <div
              className={`mockup-nav-item ${activeTab === 'employees' ? 'active' : ''}`}
              onClick={() => setActiveTab('employees')}
              style={{ cursor: 'pointer' }}
            >
              <Users size={14} />
              <span>Employees</span>
            </div>

            <div
              className={`mockup-nav-item ${activeTab === 'leaves' ? 'active' : ''}`}
              onClick={() => setActiveTab('leaves')}
              style={{ cursor: 'pointer' }}
            >
              <CalendarDays size={14} />
              <span>Leave Requests</span>
            </div>

            <div
              className={`mockup-nav-item ${activeTab === 'payroll' ? 'active' : ''}`}
              onClick={() => setActiveTab('payroll')}
              style={{ cursor: 'pointer' }}
            >
              <DollarSign size={14} />
              <span>Payroll</span>
            </div>
          </div>

          {/* Main Mock Content */}
          <div className="mockup-content">
            {/* Live Punch Station Mini Widget */}
            <div
              style={{
                background: 'linear-gradient(135deg, rgba(35, 45, 66, 0.9), rgba(19, 25, 38, 0.9))',
                border: '1px solid rgba(59, 130, 246, 0.25)',
                borderRadius: 'var(--radius-md)',
                padding: '0.75rem 1rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Clock size={14} style={{ color: 'var(--primary)' }} />
                  <span>Punch Station</span>
                </div>
                <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>
                  Status: {isCheckedIn ? 'Checked In (09:00 AM)' : 'Not Checked In'}
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.35rem' }}>
                <button
                  className="btn btn-success btn-sm"
                  style={{ fontSize: '0.7rem', padding: '0.2rem 0.5rem' }}
                  onClick={() => setIsCheckedIn(true)}
                >
                  <LogIn size={12} />
                  <span>Check In</span>
                </button>
                <button
                  className="btn btn-danger btn-sm"
                  style={{ fontSize: '0.7rem', padding: '0.2rem 0.5rem' }}
                  onClick={() => setIsCheckedIn(false)}
                >
                  <LogOut size={12} />
                  <span>Check Out</span>
                </button>
              </div>
            </div>

            {/* Stat Cards Grid */}
            <div className="mockup-grid">
              <div className="mockup-card">
                <div className="mockup-card-title">Total Staff</div>
                <div className="mockup-card-val" style={{ color: 'var(--primary)' }}>248</div>
                <div style={{ fontSize: '0.65rem', color: '#34d399', marginTop: '0.1rem' }}>+12 this month</div>
              </div>

              <div className="mockup-card">
                <div className="mockup-card-title">Present Today</div>
                <div className="mockup-card-val" style={{ color: '#34d399' }}>94%</div>
                <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', marginTop: '0.1rem' }}>233 / 248 staff</div>
              </div>

              <div className="mockup-card">
                <div className="mockup-card-title">Pending Leaves</div>
                <div className="mockup-card-val" style={{ color: '#fbbf24' }}>5</div>
                <div style={{ fontSize: '0.65rem', color: '#fbbf24', marginTop: '0.1rem' }}>Requires approval</div>
              </div>
            </div>

            {/* Bar Chart Mockup */}
            <div className="mockup-card" style={{ flex: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <TrendingUp size={14} style={{ color: 'var(--primary)' }} />
                  <span>Weekly Workforce Attendance Trend</span>
                </div>
                <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>Mon - Fri</span>
              </div>

              <div className="mockup-bar-chart">
                <div className="bar-col" style={{ height: '85%' }} title="Mon: 92%"></div>
                <div className="bar-col" style={{ height: '96%' }} title="Tue: 96%"></div>
                <div className="bar-col" style={{ height: '90%' }} title="Wed: 90%"></div>
                <div className="bar-col" style={{ height: '98%' }} title="Thu: 98%"></div>
                <div className="bar-col" style={{ height: '94%' }} title="Fri: 94%"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPreview;
