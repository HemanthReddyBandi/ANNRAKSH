import api from './api.js';

export const recommendationService = {
  list: async () => (await api.get('/api/recommendations')).data,
  generate: async () => (await api.post('/api/recommendations/generate')).data,
};
