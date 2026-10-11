import api from './api.js';

export const planService = {
  list: async () => (await api.get('/api/cultivation-plans')).data,
  create: async (plan) => (await api.post('/api/cultivation-plans', plan)).data,
  updateStatus: async (id, status) => (await api.patch(`/api/cultivation-plans/${id}/status`, { status })).data,
  recordHarvest: async (id, record) => (await api.patch(`/api/cultivation-plans/${id}/harvest`, record)).data,
};
