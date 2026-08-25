import api from './api';

export const reportService = {
  getAll: async () => {
    const response = await api.get('/reports');
    return response.data;
  },
  getById: async (id) => {
    const response = await api.get(`/reports/${id}`);
    return response.data;
  },
  create: async (data) => {
    const response = await api.post('/reports', data);
    return response.data;
  },
  update: async (id, data) => {
    const response = await api.put(`/reports/${id}`, data);
    return response.data;
  },
  delete: async (id) => {
    const response = await api.delete(`/reports/${id}`);
    return response.data;
  },
};
