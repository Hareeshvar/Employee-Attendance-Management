import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Clock, Menu, X, ArrowRight, LogIn } from 'lucide-react';

const LandingNavbar = () => {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 30) {
        setScrolled(true);
      } else {
        setScrolled(false);
      }
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const scrollToSection = (id) => {
    setMobileMenuOpen(false);
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <nav className={`landing-nav ${scrolled ? 'scrolled' : ''}`}>
      <div className="landing-content-wrapper landing-nav-inner">
        <Link to="/" className="landing-logo">
          <div className="landing-logo-icon">
            <Clock size={24} />
          </div>
          <span>Attendify HR</span>
        </Link>

        <ul className="landing-nav-links">
          <li>
            <span className="landing-nav-link" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
              Home
            </span>
          </li>
          <li>
            <span className="landing-nav-link" onClick={() => scrollToSection('features')}>
              Features
            </span>
          </li>
          <li>
            <span className="landing-nav-link" onClick={() => scrollToSection('benefits')}>
              Benefits
            </span>
          </li>
          <li>
            <span className="landing-nav-link" onClick={() => scrollToSection('roles')}>
              Roles
            </span>
          </li>
          <li>
            <span className="landing-nav-link" onClick={() => scrollToSection('security')}>
              Security
            </span>
          </li>
          <li>
            <span className="landing-nav-link" onClick={() => scrollToSection('faq')}>
              FAQ
            </span>
          </li>
        </ul>

        <div className="landing-nav-actions">
          <button className="btn btn-primary" onClick={() => navigate('/login')}>
            <LogIn size={18} />
            <span>Sign In</span>
          </button>

          <button
            className="mobile-toggle"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle Navigation Menu"
          >
            {mobileMenuOpen ? <X size={26} /> : <Menu size={26} />}
          </button>
        </div>
      </div>

      {mobileMenuOpen && (
        <div className="mobile-menu-drawer">
          <span className="landing-nav-link" onClick={() => scrollToSection('features')}>
            Features
          </span>
          <span className="landing-nav-link" onClick={() => scrollToSection('benefits')}>
            Benefits
          </span>
          <span className="landing-nav-link" onClick={() => scrollToSection('roles')}>
            Roles & Permissions
          </span>
          <span className="landing-nav-link" onClick={() => scrollToSection('security')}>
            Security & Compliance
          </span>
          <span className="landing-nav-link" onClick={() => scrollToSection('faq')}>
            FAQ
          </span>
          <button
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem' }}
            onClick={() => {
              setMobileMenuOpen(false);
              navigate('/login');
            }}
          >
            <LogIn size={18} />
            <span>Sign In to Portal</span>
          </button>
        </div>
      )}
    </nav>
  );
};

export default LandingNavbar;
