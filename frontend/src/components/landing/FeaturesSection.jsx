import React from 'react';
import {
  Clock,
  Users,
  CalendarDays,
  Clock3,
  DollarSign,
  FileText,
  Building2,
  Bell,
  ShieldCheck,
  Briefcase
} from 'lucide-react';

const FeaturesSection = () => {
  const features = [
    {
      icon: Clock,
      title: 'Attendance Management',
      description: 'Daily check-in and check-out punch station, automated working hours calculation, status tracking (Present, Late, Absent, Half-Day), and historical attendance logs.',
    },
    {
      icon: Users,
      title: 'Employee Directory',
      description: 'Centralized staff directory managing employee profiles, account status (Active, Inactive, Suspended), gender, email, username, department, and designation mapping.',
    },
    {
      icon: CalendarDays,
      title: 'Leave Management',
      description: 'Streamlined employee leave requests, balance tracking, and multi-level approval/rejection workflows for Administrators, HR Managers, and Department Leads.',
    },
    {
      icon: Clock3,
      title: 'Shift Management',
      description: 'Configurable work shifts with start/end times, grace period management, and automated employee shift assignments across departments.',
    },
    {
      icon: DollarSign,
      title: 'Payroll Administration',
      description: 'Comprehensive payroll records management, basic salary, allowances, deductions, net pay calculation, payment status tracking, and salary history.',
    },
    {
      icon: FileText,
      title: 'Reports & Analytics',
      description: 'Real-time attendance statistics, leave summaries, department-wise headcount analytics, and downloadable workforce compliance reports.',
    },
    {
      icon: Building2,
      title: 'Departments & Designations',
      description: 'Structured organizational hierarchy configuration with department codes, designation titles, and manager assignments.',
    },
    {
      icon: Bell,
      title: 'Notifications System',
      description: 'System-wide announcements, real-time leave status alerts, attendance reminders, and instant administrative updates.',
    },
    {
      icon: ShieldCheck,
      title: 'Role-Based Access (RBAC)',
      description: 'Granular role authorization for Admin, HR, Manager, and Employee accounts, guaranteeing data privacy and security.',
    },
  ];

  return (
    <section id="features" className="landing-section">
      <div className="landing-content-wrapper">
        <div className="section-header">
          <div className="section-badge">CORE CAPABILITIES</div>
          <h2 className="section-title">
            Everything Required for <span className="gradient-text">Workforce Operations</span>
          </h2>
          <p className="section-subtitle">
            Designed to digitize and automate attendance, leave, shift scheduling, payroll, and organizational reporting in one unified platform.
          </p>
        </div>

        <div className="features-grid">
          {features.map((item, index) => {
            const Icon = item.icon;
            return (
              <div key={index} className="feature-card">
                <div className="feature-icon-box">
                  <Icon size={26} />
                </div>
                <h3 className="feature-title">{item.title}</h3>
                <p className="feature-desc">{item.description}</p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default FeaturesSection;
