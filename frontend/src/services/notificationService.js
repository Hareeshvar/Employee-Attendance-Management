import api from './api';

export const notificationService = {
  getAll: async () => {
    const response = await api.get('/notifications');
    return response.data;
  },
  getById: async (id) => {
    const response = await api.get(`/notifications/${id}`);
    return response.data;
  },
  create: async (data) => {
    const response = await api.post('/notifications', data);
    return response.data;
  },
  update: async (id, data) => {
    const response = await api.put(`/notifications/${id}`, data);
    return response.data;
  },
  delete: async (id) => {
    const response = await api.delete(`/notifications/${id}`);
    return response.data;
  },
};
