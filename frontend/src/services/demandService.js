import api from './api.js';

export const demandService = {
  list: async (location) => (await api.get('/api/demand', { params: location ? { location } : {} })).data,
  create: async (record) => (await api.post('/api/demand', record)).data,
};
