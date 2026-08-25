import api from './api';

export const shiftService = {
  getAll: async () => {
    const response = await api.get('/shifts');
    return response.data;
  },
  getById: async (id) => {
    const response = await api.get(`/shifts/${id}`);
    return response.data;
  },
  create: async (data) => {
    const response = await api.post('/shifts', data);
    return response.data;
  },
  update: async (id, data) => {
    const response = await api.put(`/shifts/${id}`, data);
    return response.data;
  },
  delete: async (id) => {
    const response = await api.delete(`/shifts/${id}`);
    return response.data;
  },
};
