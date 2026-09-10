import React from 'react';
import { CheckCircle2, Clock, Users, CalendarDays, Shield, FileText } from 'lucide-react';

const ValueStrip = () => {
  const items = [
    { icon: Clock, label: 'Centralized Attendance' },
    { icon: Users, label: 'Employee Records Directory' },
    { icon: CalendarDays, label: 'Leave Approval Workflow' },
    { icon: Shield, label: 'Role-Based Security (RBAC)' },
    { icon: FileText, label: 'Real-Time Payroll & Reports' },
  ];

  return (
    <div className="value-strip">
      <div className="landing-content-wrapper value-strip-flex">
        {items.map((item, index) => {
          const Icon = item.icon;
          return (
            <div key={index} className="value-item">
              <CheckCircle2 size={18} className="value-item-icon" />
              <span>{item.label}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default ValueStrip;
