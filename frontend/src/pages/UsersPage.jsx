import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Plus, Search, Trash2, Edit, Filter } from 'lucide-react';
import { userService } from '../services/userService';
import { roleService } from '../services/roleService';
import { departmentService } from '../services/departmentService';
import { designationService } from '../services/designationService';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import Pagination from '../components/Pagination';
import { getErrorMessage } from '../utils/formatters';

const UsersPage = () => {
  const [searchParams] = useSearchParams();
  const urlSearch = searchParams.get('search') || '';

  const [users, setUsers] = useState([]);
  const [paginationInfo, setPaginationInfo] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 1,
    first: true,
    last: true,
  });

  const [roles, setRoles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState(urlSearch);
  const [departmentFilter, setDepartmentFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');

  // Sync when URL search parameter changes
  useEffect(() => {
    const q = searchParams.get('search');
    if (q !== null && q !== search) {
      setSearch(q);
      setPage(0);
    }
  }, [searchParams]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
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

  // Load auxiliary data once
  useEffect(() => {
    const loadAuxData = async () => {
      try {
        const [rolesData, deptsData, desigData] = await Promise.all([
          roleService.getAll().catch(() => []),
          departmentService.getAll().catch(() => []),
          designationService.getAll().catch(() => []),
        ]);
        setRoles(rolesData);
        setDepartments(deptsData);
        setDesignations(desigData);
      } catch (err) {
        console.error('Error loading auxiliary metadata:', err);
      }
    };
    loadAuxData();
  }, []);

  // Reset page to 0 when search or filters change
  const handleSearchChange = (val) => {
    setSearch(val);
    setPage(0);
  };

  const handleDepartmentFilterChange = (val) => {
    setDepartmentFilter(val);
    setPage(0);
  };

  const handleStatusFilterChange = (val) => {
    setStatusFilter(val);
    setPage(0);
  };

  const handleRoleFilterChange = (val) => {
    setRoleFilter(val);
    setPage(0);
  };

  const loadUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size: pageSize,
        search: search || undefined,
        departmentId: departmentFilter || undefined,
        status: statusFilter || undefined,
        roleId: roleFilter || undefined,
      };

      const res = await userService.getAll(params);
      if (res && res.content !== undefined) {
        setUsers(res.content);
        setPaginationInfo({
          pageNumber: res.pageNumber,
          pageSize: res.pageSize,
          totalElements: res.totalElements,
          totalPages: res.totalPages,
          first: res.first,
          last: res.last,
        });
      } else if (Array.isArray(res)) {
        setUsers(res);
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
    loadUsers();
  }, [page, pageSize, search, departmentFilter, statusFilter, roleFilter]);

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
      password: '',
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
      loadUsers();
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
      loadUsers();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading && users.length === 0) return <LoadingSpinner text="Loading users management directory..." />;
  if (error && users.length === 0) return <ErrorState message={error} onRetry={loadUsers} />;

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
        <div className="table-header-bar" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
          <div className="search-box" style={{ minWidth: '240px', flex: 1 }}>
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="form-input"
              placeholder="Search by name, username, email..."
              value={search}
              onChange={(e) => handleSearchChange(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center', width: '100%' }}>
            <select
              className="form-select"
              style={{ flex: '1 1 140px', minWidth: '130px' }}
              value={departmentFilter}
              onChange={(e) => handleDepartmentFilterChange(e.target.value)}
            >
              <option value="">All Departments</option>
              {departments.map((d) => (
                <option key={d.departmentId || d.id} value={d.departmentId || d.id}>
                  {d.departmentName}
                </option>
              ))}
            </select>

            <select
              className="form-select"
              style={{ flex: '1 1 130px', minWidth: '120px' }}
              value={statusFilter}
              onChange={(e) => handleStatusFilterChange(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="SUSPENDED">SUSPENDED</option>
            </select>

            <select
              className="form-select"
              style={{ flex: '1 1 130px', minWidth: '120px' }}
              value={roleFilter}
              onChange={(e) => handleRoleFilterChange(e.target.value)}
            >
              <option value="">All Roles</option>
              {roles.map((r) => (
                <option key={r.roleId || r.id} value={r.roleId || r.id}>
                  {r.roleName}
                </option>
              ))}
            </select>
          </div>
        </div>

        {users.length === 0 ? (
          <EmptyState
            title="No Users Found"
            description="There are no user accounts matching your search or filter criteria."
            action={
              <button className="btn btn-primary btn-sm" onClick={openCreateModal}>
                Create First User
              </button>
            }
          />
        ) : (
          <>
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
                {users.map((user) => (
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
