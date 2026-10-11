import api from './api.js';

export const cropService = {
  list: async () => (await api.get('/api/crops')).data,
  create: async (crop) => (await api.post('/api/crops', crop)).data,
  update: async (id, crop) => (await api.put(`/api/crops/${id}`, crop)).data,
  remove: async (id) => api.delete(`/api/crops/${id}`),
};
