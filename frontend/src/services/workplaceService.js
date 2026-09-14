import api from './api';

export const workplaceService = {
  getAll: async (params) => {
    const response = await api.get('/workplaces', { params });
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/workplaces/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await api.post('/workplaces', data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await api.put(`/workplaces/${id}`, data);
    return response.data;
  },

  setStatus: async (id, active) => {
    const response = await api.patch(`/workplaces/${id}/status`, null, {
      params: { active },
    });
    return response.data;
  },

  updateStatus: async (id, active) => {
    const response = await api.patch(`/workplaces/${id}/status`, null, {
      params: { active },
    });
    return response.data;
  },

  delete: async (id) => {
    const response = await api.delete(`/workplaces/${id}`);
    return response.data;
  },

  getMyWorkplaces: async () => {
    const response = await api.get('/workplaces/my-workplaces');
    return response.data;
  },

  assignEmployee: async (data) => {
    const response = await api.post('/workplaces/assignments', data);
    return response.data;
  },

  removeAssignment: async (id) => {
    const response = await api.delete(`/workplaces/assignments/${id}`);
    return response.data;
  },
};
