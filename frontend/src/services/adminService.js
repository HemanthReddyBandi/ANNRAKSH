import api from './api.js';

export const adminService = {
  dashboard: async () => (await api.get('/api/admin/dashboard')).data,
  farmers: async ({ page = 0, size = 20, search = '' } = {}) =>
    (await api.get('/api/admin/farmers', { params: { page, size, ...(search ? { search } : {}) } })).data,
};
