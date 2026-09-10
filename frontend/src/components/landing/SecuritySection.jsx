import React from 'react';
import { Lock, ShieldCheck, Key, Server, Cpu, Database, CheckCircle2 } from 'lucide-react';

const SecuritySection = () => {
  const securityPillars = [
    {
      icon: Key,
      title: 'Stateless JWT Authentication',
      description: 'Encrypted JSON Web Tokens validate user identity with configurable expiration and automated token revocation on logout.',
    },
    {
      icon: Lock,
      title: 'Protected Route Governance',
      description: 'Strict client and server route guards prevent unauthorized access to restricted application modules and REST endpoints.',
    },
    {
      icon: ShieldCheck,
      title: 'Granular Role Permissions (RBAC)',
      description: 'Role-based access control enforces strict boundary isolation between Admin, HR, Manager, and Employee operations.',
    },
    {
      icon: Database,
      title: 'Protected Workforce Repository',
      description: 'Centralized relational database architecture with password hashing (BCrypt) and isolated access layers.',
    },
  ];

  return (
    <section id="security" className="landing-section">
      <div className="landing-content-wrapper">
        <div className="section-header">
          <div className="section-badge">SECURITY & RELIABILITY</div>
          <h2 className="section-title">
            Enterprise Architecture Built on <span className="gradient-text">Trust</span>
          </h2>
          <p className="section-subtitle">
            Robust security mechanisms ensure data integrity, privacy compliance, and system reliability across all workforce operations.
          </p>
        </div>

        <div className="security-grid">
          {securityPillars.map((item, index) => {
            const Icon = item.icon;
            return (
              <div key={index} className="security-card">
                <div className="security-icon">
                  <Icon size={24} />
                </div>
                <h3 className="feature-title">{item.title}</h3>
                <p className="feature-desc">{item.description}</p>
              </div>
            );
          })}
        </div>

        {/* Capability Metrics Banner */}
        <div className="stats-banner">
          <div>
            <div className="stat-num">10+</div>
            <div className="stat-lbl">Workforce Modules</div>
          </div>

          <div>
            <div className="stat-num">4</div>
            <div className="stat-lbl">Role-Tailored Interfaces</div>
          </div>

          <div>
            <div className="stat-num">100%</div>
            <div className="stat-lbl">Role-Based Access</div>
          </div>

          <div>
            <div className="stat-num">Real-Time</div>
            <div className="stat-lbl">Attendance Tracking</div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default SecuritySection;
