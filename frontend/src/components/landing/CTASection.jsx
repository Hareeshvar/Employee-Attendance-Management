import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, LogIn, Sparkles } from 'lucide-react';

const CTASection = () => {
  const navigate = useNavigate();

  const scrollToFeatures = () => {
    const element = document.getElementById('features');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <section className="landing-section">
      <div className="landing-content-wrapper">
        <div className="cta-banner">
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.35rem 0.9rem',
              borderRadius: 'var(--radius-full)',
              background: 'rgba(255, 255, 255, 0.15)',
              color: 'white',
              fontSize: '0.75rem',
              fontWeight: 700,
              letterSpacing: 0.08,
              marginBottom: '1rem',
            }}
          >
            <Sparkles size={14} />
            <span>START MANAGING WORKFORCE TODAY</span>
          </div>

          <h2 className="cta-title">Ready to Simplify Workforce Management?</h2>

          <p className="cta-desc">
            Streamline daily attendance, employee directory, leave approvals, shifts, and payroll records from one enterprise platform.
          </p>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <button
              className="btn"
              style={{
                background: 'white',
                color: '#1e1b4b',
                padding: '0.875rem 2rem',
                fontSize: '1rem',
                fontWeight: 700,
                borderRadius: 'var(--radius-md)',
                boxShadow: '0 8px 20px rgba(0,0,0,0.3)',
              }}
              onClick={() => navigate('/login')}
            >
              <LogIn size={18} />
              <span>Sign In to System</span>
            </button>

            <button
              className="btn"
              style={{
                background: 'rgba(255,255,255,0.12)',
                border: '1px solid rgba(255,255,255,0.3)',
                color: 'white',
                padding: '0.875rem 1.75rem',
                fontSize: '1rem',
                fontWeight: 600,
                borderRadius: 'var(--radius-md)',
              }}
              onClick={scrollToFeatures}
            >
              <span>Explore Features</span>
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};

export default CTASection;
