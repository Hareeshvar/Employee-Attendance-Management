import api from './api';

export const employeeShiftService = {
  getAll: async () => {
    const response = await api.get('/employee-shifts');
    return response.data;
  },
  getById: async (id) => {
    const response = await api.get(`/employee-shifts/${id}`);
    return response.data;
  },
  getByUserId: async (userId) => {
    const response = await api.get(`/employee-shifts/user/${userId}`);
    return response.data;
  },
  assign: async (data) => {
    const response = await api.post('/employee-shifts', data);
    return response.data;
  },
  update: async (id, data) => {
    const response = await api.put(`/employee-shifts/${id}`, data);
    return response.data;
  },
  delete: async (id) => {
    const response = await api.delete(`/employee-shifts/${id}`);
    return response.data;
  },
};
