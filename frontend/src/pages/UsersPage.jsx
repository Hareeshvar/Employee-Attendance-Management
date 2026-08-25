import React, { useEffect, useState } from 'react';
import { Plus, Search, Trash2, Edit, User, Mail, Shield, Building2, Briefcase } from 'lucide-react';
import { userService } from '../services/userService';
import { roleService } from '../services/roleService';
import { departmentService } from '../services/departmentService';
import { designationService } from '../services/designationService';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { getErrorMessage } from '../utils/formatters';

const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [toast, setToast] = useState({ message: '', type: 'success' });

  // Modal States
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Form State
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    gender: 'MALE',
    status: 'ACTIVE',
    roleId: '',
    departmentId: '',
    designationId: '',
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [usersData, rolesData, deptsData, desigData] = await Promise.all([
        userService.getAll(),
        roleService.getAll().catch(() => []),
        departmentService.getAll().catch(() => []),
        designationService.getAll().catch(() => []),
      ]);

      setUsers(usersData);
      setRoles(rolesData);
      setDepartments(deptsData);
      setDesignations(desigData);
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
    setSelectedUser(null);
    setFormData({
      firstName: '',
      lastName: '',
      username: '',
      email: '',
      password: '',
      gender: 'MALE',
      status: 'ACTIVE',
      roleId: roles.length > 0 ? roles[0].roleId || roles[0].id : '',
      departmentId: departments.length > 0 ? departments[0].departmentId || departments[0].id : '',
      designationId: designations.length > 0 ? designations[0].designationId || designations[0].id : '',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (user) => {
    setSelectedUser(user);
    setFormData({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      username: user.username || '',
      email: user.email || '',
      password: '', // Blank password unless changing
      gender: user.gender || 'MALE',
      status: user.status || 'ACTIVE',
      roleId: user.roleId || '',
      departmentId: user.departmentId || '',
      designationId: user.designationId || '',
    });
    setIsModalOpen(true);
  };

  const openDeleteModal = (user) => {
    setSelectedUser(user);
    setIsDeleteModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    const payload = {
      firstName: formData.firstName,
      lastName: formData.lastName,
      username: formData.username,
      email: formData.email,
      password: formData.password || 'password123',
      gender: formData.gender,
      status: formData.status,
      roleId: Number(formData.roleId),
      departmentId: formData.departmentId ? Number(formData.departmentId) : null,
      designationId: Number(formData.designationId),
    };

    try {
      if (selectedUser) {
        await userService.update(selectedUser.userId, payload);
        setToast({ message: 'User updated successfully!', type: 'success' });
      } else {
        await userService.create(payload);
        setToast({ message: 'User created successfully!', type: 'success' });
      }
      setIsModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedUser) return;
    setSubmitting(true);
    try {
      await userService.delete(selectedUser.userId);
      setToast({ message: 'User deleted successfully!', type: 'success' });
      setIsDeleteModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const filteredUsers = users.filter((u) => {
    const query = search.toLowerCase();
    const fullName = `${u.firstName || ''} ${u.lastName || ''}`.toLowerCase();
    return (
      fullName.includes(query) ||
      (u.username && u.username.toLowerCase().includes(query)) ||
      (u.email && u.email.toLowerCase().includes(query))
    );
  });

  if (loading) return <LoadingSpinner text="Loading users management directory..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Employee User Directory</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Manage staff accounts, department assignments, designations, and roles.
          </p>
        </div>

        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} />
          <span>Add New User</span>
        </button>
      </div>

      <div className="table-container">
        <div className="table-header-bar">
          <div className="search-box">
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search user by name or email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        {filteredUsers.length === 0 ? (
          <EmptyState
            title="No Users Found"
            description="There are no user accounts matching your criteria."
            action={
              <button className="btn btn-primary btn-sm" onClick={openCreateModal}>
                Create First User
              </button>
            }
          />
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>User ID</th>
                <th>Employee Name</th>
                <th>Username & Email</th>
                <th>Role</th>
                <th>Department</th>
                <th>Designation</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((user) => (
                <tr key={user.userId}>
                  <td style={{ fontWeight: 700 }}>#{user.userId}</td>
                  <td>
                    <div style={{ fontWeight: 600 }}>
                      {user.firstName} {user.lastName}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user.gender}</div>
                  </td>
                  <td>
                    <div>{user.username}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user.email}</div>
                  </td>
                  <td>
                    <span className="badge badge-info">{user.roleName || `Role #${user.roleId}`}</span>
                  </td>
                  <td>{user.departmentName || (user.departmentId ? `Dept #${user.departmentId}` : 'N/A')}</td>
                  <td>{user.designationName || (user.designationId ? `Desig #${user.designationId}` : 'N/A')}</td>
                  <td>
                    <StatusBadge status={user.status} />
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(user)} title="Edit user">
                        <Edit size={15} />
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={() => openDeleteModal(user)} title="Delete user">
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Create / Edit User Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedUser ? 'Edit User Account' : 'Create New User Account'}>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">First Name *</label>
              <input
                type="text"
                className="form-input"
                required
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Last Name *</label>
              <input
                type="text"
                className="form-input"
                required
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Username *</label>
              <input
                type="text"
                className="form-input"
                required
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Email *</label>
              <input
                type="email"
                className="form-input"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Password {selectedUser ? '(Leave blank to keep existing)' : '*'}</label>
            <input
              type="password"
              className="form-input"
              required={!selectedUser}
              placeholder={selectedUser ? '••••••••' : 'Enter password'}
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Gender *</label>
              <select
                className="form-select"
                value={formData.gender}
                onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
              >
                <option value="MALE">MALE</option>
                <option value="FEMALE">FEMALE</option>
                <option value="OTHER">OTHER</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Status *</label>
              <select
                className="form-select"
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              >
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
                <option value="SUSPENDED">SUSPENDED</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Role *</label>
              <select
                className="form-select"
                required
                value={formData.roleId}
                onChange={(e) => setFormData({ ...formData, roleId: e.target.value })}
              >
                <option value="">Select Role</option>
                {roles.map((r) => (
                  <option key={r.roleId || r.id} value={r.roleId || r.id}>
                    {r.roleName}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Department</label>
              <select
                className="form-select"
                value={formData.departmentId}
                onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
              >
                <option value="">Select Department</option>
                {departments.map((d) => (
                  <option key={d.departmentId || d.id} value={d.departmentId || d.id}>
                    {d.departmentName}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Designation *</label>
            <select
              className="form-select"
              required
              value={formData.designationId}
              onChange={(e) => setFormData({ ...formData, designationId: e.target.value })}
            >
              <option value="">Select Designation</option>
              {designations.map((ds) => (
                <option key={ds.designationId || ds.id} value={ds.designationId || ds.id}>
                  {ds.designationName}
                </option>
              ))}
            </select>
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : selectedUser ? 'Update User' : 'Create User'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete User Modal */}
      <Modal isOpen={isDeleteModalOpen} onClose={() => setIsDeleteModalOpen(false)} title="Confirm Delete User">
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
          Are you sure you want to permanently delete user <strong>{selectedUser?.firstName} {selectedUser?.lastName}</strong> (#{selectedUser?.userId})?
          This action cannot be undone.
        </p>
        <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
          <button type="button" className="btn btn-secondary" onClick={() => setIsDeleteModalOpen(false)}>
            Cancel
          </button>
          <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={submitting}>
            {submitting ? 'Deleting...' : 'Delete User'}
          </button>
        </div>
      </Modal>
    </div>
  );
};

export default UsersPage;
