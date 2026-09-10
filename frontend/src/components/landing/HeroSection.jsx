import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Sparkles, ShieldCheck, PlayCircle } from 'lucide-react';
import DashboardPreview from './DashboardPreview';

const HeroSection = () => {
  const navigate = useNavigate();

  const scrollToFeatures = () => {
    const element = document.getElementById('features');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <section className="hero-section">
      <div className="hero-bg-glow"></div>

      <div className="landing-content-wrapper hero-grid">
        <div>
          <div className="section-badge">
            <Sparkles size={14} />
            <span>SMART WORKFORCE MANAGEMENT</span>
          </div>

          <h1 className="hero-title">
            Simplify Employee Attendance.{' '}
            <span className="gradient-text">Empower Your Workforce.</span>
          </h1>

          <p className="hero-desc">
            A modern, enterprise-grade attendance & HR platform designed for organizations to track attendance in real-time, streamline leave approvals, schedule employee shifts, manage payroll, and gain data-driven operational insights.
          </p>

          <div className="hero-cta-group">
            <button className="btn-hero-primary" onClick={() => navigate('/login')}>
              <span>Get Started Now</span>
              <ArrowRight size={18} />
            </button>

            <button className="btn-hero-secondary" onClick={scrollToFeatures}>
              <span>Explore Features</span>
            </button>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginTop: '2.5rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              <ShieldCheck size={18} style={{ color: 'var(--accent-emerald)' }} />
              <span>Role-Based Access (RBAC)</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              <ShieldCheck size={18} style={{ color: 'var(--primary)' }} />
              <span>Stateless JWT Security</span>
            </div>
          </div>
        </div>

        <DashboardPreview />
      </div>
    </section>
  );
};

export default HeroSection;
