import api from './api';

export const leaveService = {
  getAll: async (params) => {
    const response = await api.get('/leaves', { params });
    return response.data;
  },
  getById: async (id) => {
    const response = await api.get(`/leaves/${id}`);
    return response.data;
  },
  apply: async (data) => {
    const response = await api.post('/leaves', data);
    return response.data;
  },
  approve: async (id) => {
    const response = await api.put(`/leaves/${id}/approve`);
    return response.data;
  },
  reject: async (id) => {
    const response = await api.put(`/leaves/${id}/reject`);
    return response.data;
  },
  delete: async (id) => {
    const response = await api.delete(`/leaves/${id}`);
    return response.data;
  },
};
