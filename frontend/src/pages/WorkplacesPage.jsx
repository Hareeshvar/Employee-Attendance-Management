import React, { useEffect, useState } from 'react';
import { Plus, MapPin, UserCheck, Trash2, Edit, CheckCircle2, XCircle, Compass } from 'lucide-react';
import { workplaceService } from '../services/workplaceService';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { formatDate, getErrorMessage } from '../utils/formatters';

const WorkplacesPage = () => {
  const { isAdmin, isHr } = useAuth();
  const [activeTab, setActiveTab] = useState('locations'); // 'locations' | 'my-workplaces'
  const [workplaces, setWorkplaces] = useState([]);
  const [myWorkplaces, setMyWorkplaces] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  // Modal States
  const [isWorkplaceModalOpen, setIsWorkplaceModalOpen] = useState(false);
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [selectedWorkplace, setSelectedWorkplace] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Forms
  const [workplaceForm, setWorkplaceForm] = useState({
    name: '',
    code: '',
    description: '',
    latitude: '',
    longitude: '',
    radiusMeters: 200,
    maxAccuracyMeters: 100,
    active: true,
  });

  const [assignForm, setAssignForm] = useState({
    userId: '',
    workplaceId: '',
    effectiveFrom: new Date().toISOString().split('T')[0],
    effectiveTo: '',
    primaryWorkplace: true,
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const promises = [
        workplaceService.getMyWorkplaces().catch(() => []),
      ];

      if (isAdmin || isHr) {
        promises.push(workplaceService.getAll({ page: 0, size: 1000 }).catch(() => []));
        promises.push(userService.getAll({ page: 0, size: 1000 }).catch(() => []));
      }

      const [myWp, allWp, usersData] = await Promise.all(promises);
      setMyWorkplaces(Array.isArray(myWp) ? myWp : (myWp?.content || []));
      if (isAdmin || isHr) {
        setWorkplaces(Array.isArray(allWp) ? allWp : (allWp?.content || []));
        setUsers(Array.isArray(usersData) ? usersData : (usersData?.content || []));
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [isAdmin, isHr]);

  const openWorkplaceModal = (wp = null) => {
    setSelectedWorkplace(wp);
    if (wp) {
      setWorkplaceForm({
        name: wp.name || '',
        code: wp.code || '',
        description: wp.description || '',
        latitude: wp.latitude !== undefined ? wp.latitude : '',
        longitude: wp.longitude !== undefined ? wp.longitude : '',
        radiusMeters: wp.radiusMeters || 200,
        maxAccuracyMeters: wp.maxAccuracyMeters || 1000,
        active: wp.isActive !== undefined ? wp.isActive : (wp.active ?? true),
      });
    } else {
      setWorkplaceForm({
        name: '',
        code: '',
        description: '',
        latitude: '',
        longitude: '',
        radiusMeters: 200,
        maxAccuracyMeters: 1000,
        active: true,
      });
    }
    setIsWorkplaceModalOpen(true);
  };

  const openAssignModal = (wp = null) => {
    const defaultWpId = wp ? (wp.workplaceId || wp.id) : (workplaces.length > 0 ? (workplaces[0].workplaceId || workplaces[0].id) : '');
    const defaultUserId = users.length > 0 ? (users[0].userId || users[0].id) : '';
    setAssignForm({
      userId: defaultUserId,
      workplaceId: defaultWpId,
      effectiveFrom: new Date().toISOString().split('T')[0],
      effectiveTo: '',
      primaryWorkplace: true,
    });
    setIsAssignModalOpen(true);
  };

  const handleWorkplaceSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = {
        name: workplaceForm.name.trim(),
        code: workplaceForm.code.trim().toUpperCase(),
        description: workplaceForm.description?.trim() || null,
        latitude: parseFloat(workplaceForm.latitude),
        longitude: parseFloat(workplaceForm.longitude),
        radiusMeters: parseFloat(workplaceForm.radiusMeters),
        maxAccuracyMeters: parseFloat(workplaceForm.maxAccuracyMeters || 1000),
        isActive: workplaceForm.active,
      };

      const wpId = selectedWorkplace ? (selectedWorkplace.workplaceId || selectedWorkplace.id) : null;
      if (wpId) {
        await workplaceService.update(wpId, payload);
        setToast({ message: 'Workplace updated successfully.', type: 'success' });
      } else {
        await workplaceService.create(payload);
        setToast({ message: 'Workplace created successfully.', type: 'success' });
      }
      setIsWorkplaceModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    if (!assignForm.userId || !assignForm.workplaceId) {
      setToast({ message: 'Please select both an employee and a workplace.', type: 'error' });
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        userId: parseInt(assignForm.userId, 10),
        workplaceId: parseInt(assignForm.workplaceId, 10),
        effectiveFrom: assignForm.effectiveFrom,
        effectiveTo: assignForm.effectiveTo || null,
        isPrimary: assignForm.primaryWorkplace,
      };
      await workplaceService.assignEmployee(payload);
      setToast({ message: 'Employee successfully assigned to workplace.', type: 'success' });
      setIsAssignModalOpen(false);
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (wp) => {
    try {
      const wpId = wp.workplaceId || wp.id;
      const currentActive = wp.isActive !== undefined ? wp.isActive : wp.active;
      const newStatus = !currentActive;
      await workplaceService.setStatus(wpId, newStatus);
      setToast({
        message: `Workplace "${wp.name}" marked as ${newStatus ? 'ACTIVE' : 'INACTIVE'}.`,
        type: 'success',
      });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  const handleDeleteWorkplace = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete workplace "${name}"? This action cannot be undone.`)) {
      return;
    }
    try {
      await workplaceService.delete(id);
      setToast({ message: `Workplace "${name}" deleted.`, type: 'success' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading) return <LoadingSpinner text="Loading geofenced workplaces..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Geofenced Workplaces</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Configure authoritative physical work locations, perimeter radii, and staff geofence assignments.
          </p>
        </div>

        {(isAdmin || isHr) && (
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            {isAdmin && (
              <button className="btn btn-secondary" onClick={() => openWorkplaceModal()}>
                <Plus size={18} />
                <span>New Workplace</span>
              </button>
            )}
            <button className="btn btn-primary" onClick={() => openAssignModal()}>
              <UserCheck size={18} />
              <span>Assign Staff</span>
            </button>
          </div>
        )}
      </div>

      {/* Tabs Header */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)' }}>
        {(isAdmin || isHr) && (
          <button
            onClick={() => setActiveTab('locations')}
            style={{
              padding: '0.75rem 1.25rem',
              background: 'none',
              border: 'none',
              borderBottom: activeTab === 'locations' ? '3px solid var(--primary)' : '3px solid transparent',
              color: activeTab === 'locations' ? 'var(--primary)' : 'var(--text-secondary)',
              fontWeight: 700,
              cursor: 'pointer',
            }}
          >
            All Workplaces ({workplaces.length})
          </button>
        )}
        <button
          onClick={() => setActiveTab('my-workplaces')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'my-workplaces' ? '3px solid var(--primary)' : '3px solid transparent',
            color: activeTab === 'my-workplaces' ? 'var(--primary)' : 'var(--text-secondary)',
            fontWeight: 700,
            cursor: 'pointer',
          }}
        >
          My Authorized Locations ({myWorkplaces.length})
        </button>
      </div>

      {/* Tab 1: All Workplaces (Admin/HR) */}
      {activeTab === 'locations' && (isAdmin || isHr) && (
        <div className="table-container">
          {workplaces.length === 0 ? (
            <EmptyState
              title="No Workplaces Configured"
              description="Click 'New Workplace' to add an authoritative physical office or site."
            />
          ) : (
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Workplace Name</th>
                  <th>Coordinates (Lat / Long)</th>
                  <th>Radius</th>
                  <th>Max GPS Accuracy</th>
                  <th>Status</th>
                  <th>Description</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {workplaces.map((wp) => {
                  const wpId = wp.workplaceId || wp.id;
                  const isActive = wp.isActive !== undefined ? wp.isActive : wp.active;
                  return (
                    <tr key={wpId}>
                      <td>
                        <span style={{ fontWeight: 700, letterSpacing: '0.05em', color: 'var(--primary-light)' }}>
                          {wp.code}
                        </span>
                      </td>
                      <td style={{ fontWeight: 600 }}>{wp.name}</td>
                      <td>
                        <code style={{ fontSize: '0.8rem', background: 'rgba(255,255,255,0.06)', padding: '0.2rem 0.4rem', borderRadius: '4px' }}>
                          {wp.latitude?.toFixed ? wp.latitude.toFixed(5) : wp.latitude}, {wp.longitude?.toFixed ? wp.longitude.toFixed(5) : wp.longitude}
                        </code>
                      </td>
                      <td>
                        <span className="badge badge-info" style={{ fontSize: '0.75rem' }}>
                          {wp.radiusMeters} m
                        </span>
                      </td>
                      <td>
                        <span className="badge" style={{ fontSize: '0.75rem', background: 'rgba(255,255,255,0.08)' }}>
                          {wp.maxAccuracyMeters || 100} m
                        </span>
                      </td>
                      <td>
                        <span
                          className={`badge ${isActive ? 'badge-success' : 'badge-danger'}`}
                          style={{ fontSize: '0.75rem' }}
                        >
                          {isActive ? 'ACTIVE' : 'INACTIVE'}
                        </span>
                      </td>
                      <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                        {wp.description || '--'}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          {isAdmin && (
                            <button
                              className="btn btn-secondary btn-sm"
                              title="Edit Workplace"
                              onClick={() => openWorkplaceModal(wp)}
                            >
                              <Edit size={15} />
                            </button>
                          )}
                          {(isAdmin || isHr) && (
                            <button
                              className="btn btn-secondary btn-sm"
                              title={isActive ? 'Deactivate' : 'Activate'}
                              onClick={() => handleToggleStatus(wp)}
                            >
                              {isActive ? <XCircle size={15} color="#f87171" /> : <CheckCircle2 size={15} color="#34d399" />}
                            </button>
                          )}
                          {isAdmin && (
                            <button
                              className="btn btn-danger btn-sm"
                              title="Delete Workplace"
                              onClick={() => handleDeleteWorkplace(wpId, wp.name)}
                            >
                              <Trash2 size={15} />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Tab 2: My Authorized Locations (Any Employee) */}
      {activeTab === 'my-workplaces' && (
        <div>
          {myWorkplaces.length === 0 ? (
            <EmptyState
              title="No Assigned Geofences"
              description="You do not have any active workplace geofences assigned. Please contact HR or your manager."
            />
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1.25rem' }}>
              {myWorkplaces.map((wp) => {
                const wpId = wp.workplaceId || wp.id;
                return (
                  <div
                    key={wpId}
                    className="card"
                    style={{
                      padding: '1.5rem',
                      border: '1px solid var(--border-highlight)',
                      borderRadius: 'var(--radius-lg)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '1rem',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <div
                          style={{
                            width: '40px',
                            height: '40px',
                            borderRadius: 'var(--radius-md)',
                            background: 'rgba(59, 130, 246, 0.15)',
                            border: '1px solid rgba(59, 130, 246, 0.3)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: 'var(--primary)',
                          }}
                        >
                          <MapPin size={20} />
                        </div>
                        <div>
                          <h3 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0 }}>{wp.name}</h3>
                          <span style={{ fontSize: '0.75rem', color: 'var(--primary-light)', fontWeight: 600 }}>
                            {wp.isPrimary ? '★ Primary Workplace' : 'Authorized Site'}
                          </span>
                        </div>
                      </div>
                      <span className="badge badge-success" style={{ fontSize: '0.7rem' }}>
                        Authorized
                      </span>
                    </div>

                    {wp.description && (
                      <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0 }}>
                        {wp.description}
                      </p>
                    )}

                    <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      <span>Permitted Radius:</span>
                      <span style={{ fontWeight: 700, color: 'var(--text-primary)' }}>{wp.radiusMeters} meters</span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Modal: Create/Edit Workplace */}
      <Modal
        isOpen={isWorkplaceModalOpen}
        onClose={() => setIsWorkplaceModalOpen(false)}
        title={selectedWorkplace ? 'Edit Geofenced Workplace' : 'Create Geofenced Workplace'}
      >
        <form onSubmit={handleWorkplaceSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="form-group">
            <label className="form-label">Workplace Name *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="e.g., Downtown Headquarters"
              value={workplaceForm.name}
              onChange={(e) => setWorkplaceForm({ ...workplaceForm, name: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Workplace Code *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="e.g., HQ-MAIN"
              value={workplaceForm.code}
              onChange={(e) => setWorkplaceForm({ ...workplaceForm, code: e.target.value })}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Latitude (-90 to +90) *</label>
              <input
                type="number"
                step="any"
                className="form-input"
                required
                placeholder="10.997930"
                value={workplaceForm.latitude}
                onChange={(e) => setWorkplaceForm({ ...workplaceForm, latitude: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Longitude (-180 to +180) *</label>
              <input
                type="number"
                step="any"
                className="form-input"
                required
                placeholder="76.954902"
                value={workplaceForm.longitude}
                onChange={(e) => setWorkplaceForm({ ...workplaceForm, longitude: e.target.value })}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Geofence Radius (meters) *</label>
              <input
                type="number"
                min="10"
                max="5000"
                className="form-input"
                required
                placeholder="200"
                value={workplaceForm.radiusMeters}
                onChange={(e) => setWorkplaceForm({ ...workplaceForm, radiusMeters: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Max GPS Accuracy (meters) *</label>
              <input
                type="number"
                min="10"
                max="5000"
                className="form-input"
                required
                placeholder="1000"
                value={workplaceForm.maxAccuracyMeters}
                onChange={(e) => setWorkplaceForm({ ...workplaceForm, maxAccuracyMeters: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Description (optional)</label>
            <textarea
              className="form-input"
              rows={2}
              placeholder="Facility address or floor instructions..."
              value={workplaceForm.description}
              onChange={(e) => setWorkplaceForm({ ...workplaceForm, description: e.target.value })}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.25rem' }}>
            <input
              type="checkbox"
              id="wpActiveCheck"
              checked={workplaceForm.active}
              onChange={(e) => setWorkplaceForm({ ...workplaceForm, active: e.target.checked })}
            />
            <label htmlFor="wpActiveCheck" style={{ fontSize: '0.875rem', cursor: 'pointer' }}>
              Active workplace location (available for punching)
            </label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setIsWorkplaceModalOpen(false)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : (selectedWorkplace ? 'Update Workplace' : 'Create Workplace')}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal: Assign Workplace to Employee */}
      <Modal
        isOpen={isAssignModalOpen}
        onClose={() => setIsAssignModalOpen(false)}
        title="Assign Workplace to Employee"
      >
        <form onSubmit={handleAssignSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="form-group">
            <label className="form-label">Select Employee *</label>
            <select
              className="form-select"
              required
              value={assignForm.userId}
              onChange={(e) => setAssignForm({ ...assignForm, userId: e.target.value })}
            >
              <option value="">Select Employee...</option>
              {users.map((u) => {
                const uId = u.userId || u.id;
                return (
                  <option key={uId} value={uId}>
                    {u.firstName} {u.lastName} (@{u.username})
                  </option>
                );
              })}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Select Workplace *</label>
            <select
              className="form-select"
              required
              value={assignForm.workplaceId}
              onChange={(e) => setAssignForm({ ...assignForm, workplaceId: e.target.value })}
            >
              <option value="">Select Workplace...</option>
              {workplaces.map((w) => {
                const wId = w.workplaceId || w.id;
                return (
                  <option key={wId} value={wId}>
                    {w.name} ({w.code} - {w.radiusMeters}m radius)
                  </option>
                );
              })}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Effective From *</label>
              <input
                type="date"
                className="form-input"
                required
                value={assignForm.effectiveFrom}
                onChange={(e) => setAssignForm({ ...assignForm, effectiveFrom: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Effective To (optional)</label>
              <input
                type="date"
                className="form-input"
                value={assignForm.effectiveTo}
                onChange={(e) => setAssignForm({ ...assignForm, effectiveTo: e.target.value })}
              />
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <input
              type="checkbox"
              id="primaryWpCheck"
              checked={assignForm.primaryWorkplace}
              onChange={(e) => setAssignForm({ ...assignForm, primaryWorkplace: e.target.checked })}
            />
            <label htmlFor="primaryWpCheck" style={{ fontSize: '0.875rem', cursor: 'pointer' }}>
              Set as primary workplace
            </label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setIsAssignModalOpen(false)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Assigning...' : 'Confirm Assignment'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default WorkplacesPage;
