import api from './api.js';

export const profileService = {
  get: async () => (await api.get('/api/farmer/profile')).data,
  update: async (profile) => (await api.put('/api/farmer/profile', profile)).data,
};
