import React, { useEffect, useState } from 'react';
import { Plus, DollarSign, Trash2, Edit } from 'lucide-react';
import { payrollService } from '../services/payrollService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { formatCurrency, getErrorMessage } from '../utils/formatters';

const PayrollsPage = () => {
  const { isAdmin } = useAuth();
  const [payrolls, setPayrolls] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedPayroll, setSelectedPayroll] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    userId: '',
    month: 'JANUARY',
    year: new Date().getFullYear(),
    basicSalary: 5000,
    bonus: 500,
    deduction: 200,
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [payData, usersData] = await Promise.all([
        payrollService.getAll(),
        isAdmin ? userService.getAll().catch(() => []) : Promise.resolve([]),
      ]);
      setPayrolls(payData);
      setUsers(usersData);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [isAdmin]);

  const calculatedNetSalary = Math.max(
    0,
    (Number(formData.basicSalary) || 0) + (Number(formData.bonus) || 0) - (Number(formData.deduction) || 0)
  );

  const openCreateModal = () => {
    setSelectedPayroll(null);
    setFormData({
      userId: users.length > 0 ? users[0].userId : '',
      month: 'JANUARY',
      year: new Date().getFullYear(),
      basicSalary: 5000,
      bonus: 500,
      deduction: 200,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (p) => {
    setSelectedPayroll(p);
    setFormData({
      userId: p.userId || '',
      month: p.month || 'JANUARY',
      year: p.year || new Date().getFullYear(),
      basicSalary: p.basicSalary || 0,
      bonus: p.bonus || 0,
      deduction: p.deduction || 0,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      userId: Number(formData.userId),
      month: formData.month,
      year: Number(formData.year),
      basicSalary: Number(formData.basicSalary),
      bonus: Number(formData.bonus),
      deduction: Number(formData.deduction),
      netSalary: calculatedNetSalary,
    };

    try {
      if (selectedPayroll) {
        await payrollService.update(selectedPayroll.payrollId || selectedPayroll.id, payload);
        setToast({ message: 'Payroll updated successfully!', type: 'success' });
      } else {
        await payrollService.create(payload);
        setToast({ message: 'Payroll slip generated successfully!', type: 'success' });
      }
      setIsModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Delete payroll record #${id}?`)) return;
    try {
      await payrollService.delete(id);
      setToast({ message: 'Payroll record deleted.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading) return <LoadingSpinner text="Fetching payroll records..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Employee Payroll Records</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Manage monthly salary slips, deductions, bonuses, and net pay.
          </p>
        </div>

        {isAdmin && (
          <button className="btn btn-primary" onClick={openCreateModal}>
            <Plus size={18} />
            <span>Generate Salary Slip</span>
          </button>
        )}
      </div>

      <div className="table-container">
        {payrolls.length === 0 ? (
          <EmptyState title="No Payroll Records" description="No salary slips have been generated yet." />
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>Slip ID</th>
                <th>User ID</th>
                <th>Period</th>
                <th>Basic Salary</th>
                <th>Bonus</th>
                <th>Deductions</th>
                <th>Net Salary</th>
                {isAdmin && <th style={{ textAlign: 'right' }}>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {payrolls.map((p) => {
                const id = p.payrollId || p.id;
                return (
                  <tr key={id}>
                    <td style={{ fontWeight: 700 }}>#{id}</td>
                    <td>User #{p.userId}</td>
                    <td>
                      <span className="badge badge-info">
                        {p.month} {p.year}
                      </span>
                    </td>
                    <td>{formatCurrency(p.basicSalary)}</td>
                    <td style={{ color: '#34d399' }}>+{formatCurrency(p.bonus)}</td>
                    <td style={{ color: '#f87171' }}>-{formatCurrency(p.deduction)}</td>
                    <td style={{ fontWeight: 800, color: 'var(--primary)' }}>{formatCurrency(p.netSalary)}</td>
                    {isAdmin && (
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(p)}>
                            <Edit size={15} />
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(id)}>
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Payroll Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedPayroll ? 'Edit Salary Slip' : 'Generate Salary Slip'}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Employee User *</label>
            {users.length > 0 ? (
              <select
                className="form-select"
                required
                value={formData.userId}
                onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
              >
                <option value="">Select Employee</option>
                {users.map((u) => (
                  <option key={u.userId} value={u.userId}>
                    {u.firstName} {u.lastName} (#{u.userId})
                  </option>
                ))}
              </select>
            ) : (
              <input
                type="number"
                className="form-input"
                required
                placeholder="User ID"
                value={formData.userId}
                onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
              />
            )}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Month *</label>
              <select
                className="form-select"
                required
                value={formData.month}
                onChange={(e) => setFormData({ ...formData, month: e.target.value })}
              >
                {[
                  'JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE',
                  'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER',
                ].map((m) => (
                  <option key={m} value={m}>{m}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Year *</label>
              <input
                type="number"
                className="form-input"
                required
                value={formData.year}
                onChange={(e) => setFormData({ ...formData, year: e.target.value })}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Basic Salary ($) *</label>
              <input
                type="number"
                className="form-input"
                required
                value={formData.basicSalary}
                onChange={(e) => setFormData({ ...formData, basicSalary: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Bonus ($) *</label>
              <input
                type="number"
                className="form-input"
                required
                value={formData.bonus}
                onChange={(e) => setFormData({ ...formData, bonus: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Deduction ($) *</label>
              <input
                type="number"
                className="form-input"
                required
                value={formData.deduction}
                onChange={(e) => setFormData({ ...formData, deduction: e.target.value })}
              />
            </div>
          </div>

          <div className="card" style={{ background: 'var(--bg-primary)', margin: '1rem 0', textAlign: 'center' }}>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Calculated Net Pay</div>
            <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--primary)' }}>
              {formatCurrency(calculatedNetSalary)}
            </div>
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : selectedPayroll ? 'Update Slip' : 'Generate Slip'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default PayrollsPage;
