import React, { useEffect, useState } from 'react';
import { Plus, Building2, Trash2, Edit } from 'lucide-react';
import { departmentService } from '../services/departmentService';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { getErrorMessage } from '../utils/formatters';

const DepartmentsPage = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedDept, setSelectedDept] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    departmentName: '',
    departmentCode: '',
    description: '',
    status: 'ACTIVE',
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await departmentService.getAll();
      setDepartments(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openCreateModal = () => {
    setSelectedDept(null);
    setFormData({
      departmentName: '',
      departmentCode: '',
      description: '',
      status: 'ACTIVE',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (dept) => {
    setSelectedDept(dept);
    setFormData({
      departmentName: dept.departmentName || '',
      departmentCode: dept.departmentCode || '',
      description: dept.description || '',
      status: dept.status || 'ACTIVE',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      if (selectedDept) {
        await departmentService.update(selectedDept.departmentId || selectedDept.id, formData);
        setToast({ message: 'Department updated successfully!', type: 'success' });
      } else {
        await departmentService.create(formData);
        setToast({ message: 'Department created successfully!', type: 'success' });
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
    if (!window.confirm(`Are you sure you want to delete department #${id}?`)) return;
    try {
      await departmentService.delete(id);
      setToast({ message: 'Department deleted successfully.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading) return <LoadingSpinner text="Fetching department structures..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Department Management</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Configure corporate organizational departments and codes.
          </p>
        </div>

        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} />
          <span>Add Department</span>
        </button>
      </div>

      <div className="table-container">
        {departments.length === 0 ? (
          <EmptyState
            title="No Departments Created"
            description="You have not created any departments yet."
            action={
              <button className="btn btn-primary btn-sm" onClick={openCreateModal}>
                Create Department
              </button>
            }
          />
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Department Name</th>
                <th>Code</th>
                <th>Description</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {departments.map((dept) => {
                const id = dept.departmentId || dept.id;
                return (
                  <tr key={id}>
                    <td style={{ fontWeight: 700 }}>#{id}</td>
                    <td style={{ fontWeight: 600 }}>{dept.departmentName}</td>
                    <td>
                      <span className="badge badge-info">{dept.departmentCode}</span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)' }}>{dept.description || 'N/A'}</td>
                    <td>
                      <StatusBadge status={dept.status || 'ACTIVE'} />
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(dept)}>
                          <Edit size={15} />
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(id)}>
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Department Form Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedDept ? 'Edit Department' : 'Create Department'}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Department Name *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="e.g. Engineering"
              value={formData.departmentName}
              onChange={(e) => setFormData({ ...formData, departmentName: e.target.value })}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Department Code *</label>
              <input
                type="text"
                className="form-input"
                required
                placeholder="e.g. ENG-01"
                value={formData.departmentCode}
                onChange={(e) => setFormData({ ...formData, departmentCode: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              >
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Brief description of department scope..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : selectedDept ? 'Update Department' : 'Create Department'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default DepartmentsPage;
