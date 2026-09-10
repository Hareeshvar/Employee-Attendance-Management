import React from 'react';
import '../styles/landing.css';

import LandingNavbar from '../components/landing/LandingNavbar';
import HeroSection from '../components/landing/HeroSection';
import ValueStrip from '../components/landing/ValueStrip';
import FeaturesSection from '../components/landing/FeaturesSection';
import BenefitsSection from '../components/landing/BenefitsSection';
import WorkflowSection from '../components/landing/WorkflowSection';
import RolesSection from '../components/landing/RolesSection';
import SecuritySection from '../components/landing/SecuritySection';
import FAQSection from '../components/landing/FAQSection';
import CTASection from '../components/landing/CTASection';
import LandingFooter from '../components/landing/LandingFooter';

const LandingPage = () => {
  return (
    <div className="landing-container">
      <LandingNavbar />
      <HeroSection />
      <ValueStrip />
      <FeaturesSection />
      <BenefitsSection />
      <WorkflowSection />
      <RolesSection />
      <SecuritySection />
      <FAQSection />
      <CTASection />
      <LandingFooter />
    </div>
  );
};

export default LandingPage;
