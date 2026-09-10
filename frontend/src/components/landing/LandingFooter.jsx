import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Clock } from 'lucide-react';

const LandingFooter = () => {
  const navigate = useNavigate();

  const scrollToSection = (id) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <footer className="landing-footer">
      <div className="landing-content-wrapper">
        <div className="footer-grid">
          <div className="footer-brand">
            <Link to="/" className="landing-logo">
              <div className="landing-logo-icon">
                <Clock size={22} />
              </div>
              <span>Attendify HR</span>
            </Link>

            <p className="footer-desc">
              A comprehensive employee attendance & HR management platform built for modern organizations to digitize workforce operations, attendance punch tracking, leave approvals, shift scheduling, and payroll.
            </p>
          </div>

          <div>
            <h4 className="footer-col-title">Navigation</h4>
            <ul className="footer-links">
              <li>
                <span style={{ cursor: 'pointer' }} onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
                  Home
                </span>
              </li>
              <li>
                <span style={{ cursor: 'pointer' }} onClick={() => scrollToSection('features')}>
                  Features
                </span>
              </li>
              <li>
                <span style={{ cursor: 'pointer' }} onClick={() => scrollToSection('benefits')}>
                  Benefits
                </span>
              </li>
              <li>
                <span style={{ cursor: 'pointer' }} onClick={() => scrollToSection('roles')}>
                  Roles & Permissions
                </span>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="footer-col-title">Platform</h4>
            <ul className="footer-links">
              <li>
                <span style={{ cursor: 'pointer' }} onClick={() => scrollToSection('security')}>
                  Security Architecture
                </span>
              </li>
              <li>
                <span style={{ cursor: 'pointer' }} onClick={() => scrollToSection('faq')}>
                  FAQ
                </span>
              </li>
              <li>
                <Link to="/login">Sign In to System</Link>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="footer-col-title">Built With</h4>
            <div className="tech-badges">
              <span className="tech-badge">React 18</span>
              <span className="tech-badge">Spring Boot 3</span>
              <span className="tech-badge">Stateless JWT</span>
              <span className="tech-badge">Spring Security</span>
              <span className="tech-badge">MySQL</span>
              <span className="tech-badge">Vite</span>
            </div>
          </div>
        </div>

        <div className="footer-bottom">
          <div>© 2026 Attendify HR - Employee Attendance Management System. All rights reserved.</div>
          <div style={{ display: 'flex', gap: '1.5rem' }}>
            <span style={{ cursor: 'pointer' }} onClick={() => navigate('/login')}>
              Login Portal
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default LandingFooter;
