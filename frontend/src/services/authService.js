import api from './api.js';

export const authService = {
  login: async (credentials) => (await api.post('/api/auth/login', credentials)).data,
  register: async (details) => (await api.post('/api/auth/register', details)).data,
};
