import React from 'react';

const WorkflowSection = () => {
  const steps = [
    {
      num: '01',
      title: 'Secure Authentication',
      description: 'Users sign in through JWT-authenticated login tailored to their assigned role (Admin, HR, Manager, or Employee).',
    },
    {
      num: '02',
      title: 'Track Attendance & Shifts',
      description: 'Staff log daily check-ins/outs via the punch station while managers configure work shifts and monitor team activity.',
    },
    {
      num: '03',
      title: 'Manage Leave & Payroll',
      description: 'Employees submit leave requests for quick manager approval, while HR processes monthly payroll records.',
    },
    {
      num: '04',
      title: 'Gain Actionable Insights',
      description: 'Executives and HR leads generate comprehensive attendance summaries, attendance rates, and compliance reports.',
    },
  ];

  return (
    <section className="landing-section">
      <div className="landing-content-wrapper">
        <div className="section-header">
          <div className="section-badge">SIMPLE WORKFLOW</div>
          <h2 className="section-title">
            How <span className="gradient-text">Attendify HR</span> Works
          </h2>
          <p className="section-subtitle">
            A simple, intuitive 4-step workflow designed to streamline workforce management from day one.
          </p>
        </div>

        <div className="workflow-steps">
          {steps.map((step, index) => (
            <div key={index} className="workflow-card">
              <div className="workflow-num">{step.num}</div>
              <h3 className="workflow-title">{step.title}</h3>
              <p className="workflow-desc">{step.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default WorkflowSection;
