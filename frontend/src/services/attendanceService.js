import api from './api';

export const attendanceService = {
  getAll: async (params) => {
    const response = await api.get('/attendance', { params });
    return response.data;
  },
  getAnalytics: async (startDate, endDate) => {
    const response = await api.get('/attendance/analytics', {
      params: { startDate, endDate },
    });
    return response.data;
  },
  getById: async (id) => {
    const response = await api.get(`/attendance/${id}`);
    return response.data;
  },
  create: async (data) => {
    const response = await api.post('/attendance', data);
    return response.data;
  },
  update: async (id, data) => {
    const response = await api.put(`/attendance/${id}`, data);
    return response.data;
  },
  delete: async (id) => {
    const response = await api.delete(`/attendance/${id}`);
    return response.data;
  },
  checkIn: async (userId) => {
    const url = (userId && userId !== 'null') ? `/attendance/checkin/${userId}` : '/attendance/checkin';
    const response = await api.post(url);
    return response.data;
  },
  checkOut: async (userId) => {
    const url = (userId && userId !== 'null') ? `/attendance/checkout/${userId}` : '/attendance/checkout';
    const response = await api.post(url);
    return response.data;
  },
};
