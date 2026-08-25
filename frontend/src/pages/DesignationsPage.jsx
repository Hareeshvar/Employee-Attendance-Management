import React, { useEffect, useState } from 'react';
import { Plus, Briefcase, Trash2, Edit } from 'lucide-react';
import { designationService } from '../services/designationService';
import { LoadingSpinner, EmptyState, ErrorState } from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import Toast from '../components/Toast';
import { getErrorMessage } from '../utils/formatters';

const DesignationsPage = () => {
  const [designations, setDesignations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedDesig, setSelectedDesig] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    designationName: '',
    description: '',
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await designationService.getAll();
      setDesignations(data);
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
    setSelectedDesig(null);
    setFormData({ designationName: '', description: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (desig) => {
    setSelectedDesig(desig);
    setFormData({
      designationName: desig.designationName || '',
      description: desig.description || '',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      if (selectedDesig) {
        await designationService.update(selectedDesig.designationId || selectedDesig.id, formData);
        setToast({ message: 'Designation updated successfully!', type: 'success' });
      } else {
        await designationService.create(formData);
        setToast({ message: 'Designation created successfully!', type: 'success' });
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
    if (!window.confirm(`Are you sure you want to delete designation #${id}?`)) return;
    try {
      await designationService.delete(id);
      setToast({ message: 'Designation deleted.', type: 'info' });
      loadData();
    } catch (err) {
      setToast({ message: getErrorMessage(err), type: 'error' });
    }
  };

  if (loading) return <LoadingSpinner text="Fetching job designations..." />;
  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div className="page-wrapper">
      <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '', type: 'success' })} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Designations Directory</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Manage staff job titles and position roles.
          </p>
        </div>

        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} />
          <span>Add Designation</span>
        </button>
      </div>

      <div className="table-container">
        {designations.length === 0 ? (
          <EmptyState
            title="No Designations Found"
            description="You have not defined any job designations yet."
            action={
              <button className="btn btn-primary btn-sm" onClick={openCreateModal}>
                Create Designation
              </button>
            }
          />
        ) : (
          <table className="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Designation Title</th>
                <th>Description</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {designations.map((desig) => {
                const id = desig.designationId || desig.id;
                return (
                  <tr key={id}>
                    <td style={{ fontWeight: 700 }}>#{id}</td>
                    <td style={{ fontWeight: 600 }}>{desig.designationName}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{desig.description || 'N/A'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(desig)}>
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

      {/* Designation Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedDesig ? 'Edit Designation' : 'Create Designation'}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Designation Title *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="e.g. Senior Software Engineer"
              value={formData.designationName}
              onChange={(e) => setFormData({ ...formData, designationName: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Job responsibilities or designation notes..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : selectedDesig ? 'Update Designation' : 'Create Designation'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default DesignationsPage;
