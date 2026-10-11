import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('annraksh.token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('annraksh.token');
      sessionStorage.removeItem('annraksh.user');
      window.dispatchEvent(new Event('annraksh:auth-expired'));
    }
    return Promise.reject(error);
  },
);

export function getErrorMessage(error) {
  const data = error.response?.data;
  const validation = data?.validationErrors;
  if (validation && Object.keys(validation).length) return Object.values(validation)[0];
  return data?.message || error.message || 'Something went wrong. Please try again.';
}

export default api;
