import React from 'react';
import {
  Zap,
  Target,
  Clock,
  Eye,
  ShieldCheck,
  Database
} from 'lucide-react';

const BenefitsSection = () => {
  const benefits = [
    {
      icon: Zap,
      title: 'Reduce Manual Overhead',
      description: 'Replace cumbersome manual spreadsheets and paper logs with automated digital punch stations and instant record generation.',
    },
    {
      icon: Target,
      title: 'Eliminate Errors & Discrepancies',
      description: 'Centralized calculations ensure exact attendance hours, late arrival tracking, and accurate payroll processing without human error.',
    },
    {
      icon: Clock,
      title: 'Save Administrative Time',
      description: 'Accelerate leave approvals, shift rotations, and monthly payroll reviews with automated workflows for HR managers and department leads.',
    },
    {
      icon: Eye,
      title: 'Real-Time Operational Visibility',
      description: 'Gain instant insight into present staff count, absenteeism rates, department headcounts, and active shift distributions.',
    },
    {
      icon: ShieldCheck,
      title: 'Enterprise-Grade RBAC Security',
      description: 'Restricted module access tailored to user roles ensures sensitive payroll, user management, and system settings remain completely protected.',
    },
    {
      icon: Database,
      title: 'Unified Workforce Data',
      description: 'Consolidate employee profiles, attendance logs, leave balances, shifts, and departmental records into a single source of truth.',
    },
  ];

  return (
    <section id="benefits" className="landing-section" style={{ background: 'rgba(19, 25, 38, 0.4)' }}>
      <div className="landing-content-wrapper">
        <div className="section-header">
          <div className="section-badge">BUSINESS VALUE</div>
          <h2 className="section-title">
            Why Leading Teams Choose <span className="gradient-text">Attendify HR</span>
          </h2>
          <p className="section-subtitle">
            Transform workforce administration into a seamless, automated, and secure operational experience.
          </p>
        </div>

        <div className="benefits-grid">
          {benefits.map((item, index) => {
            const Icon = item.icon;
            return (
              <div key={index} className="benefit-card">
                <div className="benefit-icon">
                  <Icon size={22} />
                </div>
                <div>
                  <h3 className="benefit-title">{item.title}</h3>
                  <p className="benefit-desc">{item.description}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default BenefitsSection;
