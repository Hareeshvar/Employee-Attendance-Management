import React, { useState } from 'react';
import { ChevronDown, HelpCircle } from 'lucide-react';

const FAQSection = () => {
  const [openIndex, setOpenIndex] = useState(0);

  const faqs = [
    {
      q: 'How is employee attendance tracked in the platform?',
      a: 'Attendance is recorded in real-time through the Punch Station widget. Employees sign in and click "Check In" or "Check Out", which logs exact time stamps, working hours, and status (Present, Late, Absent, Half-Day). Managers and HR can also view team logs.',
    },
    {
      q: 'Who can approve or reject employee leave requests?',
      a: 'Leave requests submitted by employees are directed to their Department Managers, HR Leads, or System Administrators. Approvers can review pending requests, check balances, and approve or reject them with feedback.',
    },
    {
      q: 'Is the platform role-based with distinct permission levels?',
      a: 'Yes! The system supports 4 distinct user roles (System Admin, HR Manager, Team Manager, and Employee). Each role receives a tailored interface and API access scoped strictly to their authorized responsibilities.',
    },
    {
      q: 'Can employees view their attendance history and payroll information?',
      a: 'Absolutely. Every employee has access to their personal dashboard where they can check daily attendance logs, submit leave applications, view monthly payroll details, and update profile information.',
    },
    {
      q: 'Are reports and analytics downloadable for HR and management?',
      a: 'Yes. HR managers and administrators can view real-time attendance trends, pending leave reports, payroll summaries, and department headcount analytics across the workforce.',
    },
  ];

  const toggleFAQ = (index) => {
    setOpenIndex(openIndex === index ? -1 : index);
  };

  return (
    <section id="faq" className="landing-section" style={{ background: 'rgba(19, 25, 38, 0.4)' }}>
      <div className="landing-content-wrapper">
        <div className="section-header">
          <div className="section-badge">FREQUENTLY ASKED QUESTIONS</div>
          <h2 className="section-title">
            Got Questions? <span className="gradient-text">We Have Answers</span>
          </h2>
          <p className="section-subtitle">
            Everything you need to know about Attendify HR attendance management system.
          </p>
        </div>

        <div className="faq-list">
          {faqs.map((faq, index) => {
            const isOpen = openIndex === index;
            return (
              <div key={index} className={`faq-item ${isOpen ? 'active' : ''}`}>
                <button className="faq-question" onClick={() => toggleFAQ(index)}>
                  <span>{faq.q}</span>
                  <ChevronDown
                    size={20}
                    style={{
                      transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)',
                      transition: 'transform 0.25s ease',
                      color: isOpen ? 'var(--primary)' : 'var(--text-muted)',
                      flexShrink: 0,
                    }}
                  />
                </button>
                {isOpen && <div className="faq-answer">{faq.a}</div>}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default FAQSection;
