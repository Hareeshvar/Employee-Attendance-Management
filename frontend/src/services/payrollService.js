import api from './api';

export const payrollService = {
  getAll: async (params) => {
    const response = await api.get('/payrolls', { params });
    return response.data;
  },
  getById: async (id) => {
    const response = await api.get(`/payrolls/${id}`);
    return response.data;
  },
  create: async (data) => {
    const response = await api.post('/payrolls', data);
    return response.data;
  },
  update: async (id, data) => {
    const response = await api.put(`/payrolls/${id}`, data);
    return response.data;
  },
  delete: async (id) => {
    const response = await api.delete(`/payrolls/${id}`);
    return response.data;
  },
};
