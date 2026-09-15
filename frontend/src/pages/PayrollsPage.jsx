import React, { useEffect, useState } from 'react';
import { Plus, Search, Trash2, Edit } from 'lucide-react';
import { payrollService } from '../services/payrollService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import Pagination from '../components/Pagination';
import { formatCurrency, getErrorMessage } from '../utils/formatters';

const MONTH_NAMES = [
  'JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE',
  'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER',
];

const PayrollsPage = () => {
  const { isAdmin } = useAuth();
  const [payrolls, setPayrolls] = useState([]);
  const [paginationInfo, setPaginationInfo] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 1,
    first: true,
    last: true,
  });

  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState('');
  const [monthFilter, setMonthFilter] = useState('');
  const [yearFilter, setYearFilter] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedPayroll, setSelectedPayroll] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    userId: '',
    month: MONTH_NAMES[new Date().getMonth()],
    year: new Date().getFullYear(),
    basicSalary: 5000,
    bonus: 500,
    deduction: 200,
  });

  useEffect(() => {
    if (isAdmin) {
      userService.getAll({ page: 0, size: 1000 })
        .then((res) => setUsers(Array.isArray(res) ? res : res.content || []))
        .catch(() => setUsers([]));
    }
  }, [isAdmin]);

  const loadPayrolls = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size: pageSize,
        search: search || undefined,
        month: monthFilter || undefined,
        year: yearFilter || undefined,
      };

      const res = await payrollService.getAll(params);
      if (res && res.content !== undefined) {
        setPayrolls(res.content);
        setPaginationInfo({
          pageNumber: res.pageNumber,
          pageSize: res.pageSize,
          totalElements: res.totalElements,
          totalPages: res.totalPages,
          first: res.first,
          last: res.last,
        });
      } else if (Array.isArray(res)) {
        setPayrolls(res);
        setPaginationInfo({
          pageNumber: 0,
          pageSize: res.length,
          totalElements: res.length,
          totalPages: 1,
          first: true,
          last: true,
        });
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPayrolls();
  }, [page, pageSize, search, monthFilter, yearFilter]);

  const handleSearchChange = (val) => {
    setSearch(val);
    setPage(0);
  };

  const handleMonthFilterChange = (val) => {
    setMonthFilter(val);
    setPage(0);
  };

  const handleYearFilterChange = (val) => {
    setYearFilter(val);
    setPage(0);
  };

  const calculatedNetSalary = Math.max(
    0,
    Math.round(((Number(formData.basicSalary) || 0) + (Number(formData.bonus) || 0) - (Number(formData.deduction) || 0)) * 100) / 100
  );

  const openCreateModal = () => {
    setSelectedPayroll(null);
    setFormData({
      userId: users.length > 0 ? users[0].userId : '',
      month: MONTH_NAMES[new Date().getMonth()],
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
      loadPayrolls();
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
      loadPayrolls();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading && payrolls.length === 0) return <LoadingSpinner text="Fetching payroll records..." />;
  if (error && payrolls.length === 0) return <ErrorState message={error} onRetry={loadPayrolls} />;

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
        <div className="table-header-bar" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
          <div className="search-box" style={{ minWidth: '220px', flex: 1 }}>
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search user name or email..."
              value={search}
              onChange={(e) => handleSearchChange(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <select
              className="form-select"
              style={{ width: '140px' }}
              value={monthFilter}
              onChange={(e) => handleMonthFilterChange(e.target.value)}
            >
              <option value="">All Months</option>
              {[
                'JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE',
                'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER',
              ].map((m) => (
                <option key={m} value={m}>{m}</option>
              ))}
            </select>

            <input
              type="number"
              className="form-input"
              style={{ width: '110px' }}
              placeholder="Year"
              value={yearFilter}
              onChange={(e) => handleYearFilterChange(e.target.value)}
            />
          </div>
        </div>

        {payrolls.length === 0 ? (
          <EmptyState title="No Payroll Records" description="No salary slips match your search or filter criteria." />
        ) : (
          <>
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Slip ID</th>
                  <th>Employee Name</th>
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
                      <td>
                        <div style={{ fontWeight: 600 }}>
                          {p.userFirstName ? `${p.userFirstName} ${p.userLastName || ''}`.trim() : p.username ? p.username : `User #${p.userId}`}
                        </div>
                      </td>
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

            <Pagination
              pageNumber={paginationInfo.pageNumber}
              pageSize={paginationInfo.pageSize}
              totalElements={paginationInfo.totalElements}
              totalPages={paginationInfo.totalPages}
              first={paginationInfo.first}
              last={paginationInfo.last}
              onPageChange={(newPage) => setPage(newPage)}
              onPageSizeChange={(newSize) => {
                setPageSize(newSize);
                setPage(0);
              }}
            />
          </>
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
