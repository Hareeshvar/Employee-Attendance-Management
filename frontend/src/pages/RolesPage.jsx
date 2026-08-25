import React, { useEffect, useState } from 'react';
import { Plus, Shield, Trash2, Edit } from 'lucide-react';
import { roleService } from '../services/roleService';
import { departmentService } from '../services/departmentService';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { getErrorMessage } from '../utils/formatters';

const RolesPage = () => {
  const [roles, setRoles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    roleName: 'EMPLOYEE',
    description: '',
    departmentId: '',
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [rolesData, deptsData] = await Promise.all([
        roleService.getAll(),
        departmentService.getAll().catch(() => []),
      ]);
      setRoles(rolesData);
      setDepartments(deptsData);
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
    setSelectedRole(null);
    setFormData({
      roleName: 'EMPLOYEE',
      description: '',
      departmentId: '',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (role) => {
    setSelectedRole(role);
    setFormData({
      roleName: role.roleName || 'EMPLOYEE',
      description: role.description || '',
      departmentId: role.departmentId || '',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      roleName: formData.roleName,
      description: formData.description,
      departmentId: formData.departmentId ? Number(formData.departmentId) : null,
    };

    try {
      if (selectedRole) {
        await roleService.update(selectedRole.roleId || selectedRole.id, payload);
        setToast({ message: 'Role updated successfully!', type: 'success' });
      } else {
        await roleService.create(payload);
        setToast({ message: 'Role created successfully!', type: 'success' });
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
    if (!window.confirm(`Delete role record #${id}?`)) return;
    try {
      await roleService.delete(id);
      setToast({ message: 'Role deleted.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading) return <LoadingSpinner text="Fetching security roles..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Security & User Roles</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Manage access control authority levels (ADMIN, HR, MANAGER, EMPLOYEE).
          </p>
        </div>

        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} />
          <span>Add New Role</span>
        </button>
      </div>

      <div className="table-container">
        {roles.length === 0 ? (
          <EmptyState title="No System Roles" description="No custom security roles configured yet." />
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>Role ID</th>
                <th>Role Authority</th>
                <th>Description</th>
                <th>Department Scope</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {roles.map((r) => {
                const id = r.roleId || r.id;
                return (
                  <tr key={id}>
                    <td style={{ fontWeight: 700 }}>#{id}</td>
                    <td>
                      <span className="badge badge-info">{r.roleName}</span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)' }}>{r.description || 'N/A'}</td>
                    <td>{r.departmentId ? `Dept #${r.departmentId}` : 'Global System Wide'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(r)}>
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

      {/* Role Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedRole ? 'Edit Role' : 'Create Role'}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Role Authority *</label>
            <select
              className="form-select"
              required
              value={formData.roleName}
              onChange={(e) => setFormData({ ...formData, roleName: e.target.value })}
            >
              <option value="ADMIN">ADMIN</option>
              <option value="HR">HR</option>
              <option value="MANAGER">MANAGER</option>
              <option value="EMPLOYEE">EMPLOYEE</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Department Scope (Optional)</label>
            <select
              className="form-select"
              value={formData.departmentId}
              onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
            >
              <option value="">Global (All Departments)</option>
              {departments.map((d) => (
                <option key={d.departmentId || d.id} value={d.departmentId || d.id}>
                  {d.departmentName}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Permission overview..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : selectedRole ? 'Update Role' : 'Create Role'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default RolesPage;
