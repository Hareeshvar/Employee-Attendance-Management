import React from 'react';
import { Shield, Building2, Users, UserCheck, Check } from 'lucide-react';

const RolesSection = () => {
  const roles = [
    {
      icon: Shield,
      title: 'Administrator',
      badge: 'System Admin',
      badgeStyle: { background: 'rgba(239, 68, 68, 0.15)', color: '#ef4444' },
      description: 'Complete system control, role permissions configuration, master user directory management, and system governance.',
      features: [
        'Full System Configuration & Roles',
        'User Account Creation & Deletion',
        'Global Department & Designation Setup',
        'Full Attendance & Payroll Override',
        'System Audit & Reports Access',
      ],
    },
    {
      icon: Building2,
      title: 'HR Manager',
      badge: 'HR Operations',
      badgeStyle: { background: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6' },
      description: 'Employee onboarding, department management, leave request approvals, payroll administration, and HR analytics.',
      features: [
        'Employee Account Management',
        'Leave Approval & Cancellation',
        'Payroll Records & Allowances',
        'Shift Scheduling & Assignments',
        'HR Workforce Reports & Metrics',
      ],
    },
    {
      icon: Users,
      title: 'Team Manager',
      badge: 'Team Lead',
      badgeStyle: { background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b' },
      description: 'Departmental team monitoring, team member attendance logs, leave approvals, and department report reviews.',
      features: [
        'Department Team Member Directory',
        'Team Attendance Logs & Monitoring',
        'Team Leave Request Approvals',
        'Departmental Attendance Reports',
        'Self Check-In / Check-Out Punch',
      ],
    },
    {
      icon: UserCheck,
      title: 'Employee',
      badge: 'Staff Portal',
      badgeStyle: { background: 'rgba(16, 185, 129, 0.15)', color: '#10b981' },
      description: 'Personal self-service portal for attendance check-in/out, leave application submission, payroll slip views, and profile management.',
      features: [
        'Self Punch Station (Check In / Out)',
        'Personal Attendance History Logs',
        'Leave Application Submission',
        'Personal Payroll Records View',
        'Self Profile Information Update',
      ],
    },
  ];

  return (
    <section id="roles" className="landing-section" style={{ background: 'rgba(19, 25, 38, 0.4)' }}>
      <div className="landing-content-wrapper">
        <div className="section-header">
          <div className="section-badge">ROLE-BASED EXPERIENCE</div>
          <h2 className="section-title">
            Tailored Experiences for <span className="gradient-text">Every Role</span>
          </h2>
          <p className="section-subtitle">
            Fine-grained permissions ensure users access exactly what they need for their organizational responsibilities.
          </p>
        </div>

        <div className="roles-grid">
          {roles.map((item, index) => {
            const Icon = item.icon;
            return (
              <div key={index} className="role-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                  <div className="stat-icon" style={{ width: 44, height: 44 }}>
                    <Icon size={22} />
                  </div>
                  <span className="role-badge" style={item.badgeStyle}>
                    {item.badge}
                  </span>
                </div>

                <h3 className="role-title">{item.title}</h3>
                <p className="role-desc">{item.description}</p>

                <ul className="role-feature-list">
                  {item.features.map((feat, fIdx) => (
                    <li key={fIdx}>
                      <Check size={14} style={{ color: 'var(--accent-emerald)', flexShrink: 0 }} />
                      <span>{feat}</span>
                    </li>
                  ))}
                </ul>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default RolesSection;
