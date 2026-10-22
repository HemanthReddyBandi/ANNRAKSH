import api from './api.js';

export const adminService = {
  dashboard: async () => (await api.get('/api/admin/dashboard')).data,
  farmers: async ({ page = 0, size = 20, search = '' } = {}) =>
    (await api.get('/api/admin/farmers', { params: { page, size, ...(search ? { search } : {}) } })).data,
  cultivationPlans: async ({ page = 0, size = 20, search = '', status = '' } = {}) =>
    (await api.get('/api/admin/cultivation-plans', {
      params: { page, size, ...(search ? { search } : {}), ...(status ? { status } : {}) },
    })).data,
  agriculturalStatistics: async () => (await api.get('/api/admin/agricultural-statistics')).data,
  dispositionHistory: async (planId) =>
    (await api.get(`/api/admin/cultivation-plans/${planId}/harvest-disposition/history`)).data,
};
