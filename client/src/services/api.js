import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add a request interceptor to include the token if it exists
// Note: Since we are using httpOnly cookies for accessToken, we might not need this if the browser handles it.
// But if we were using localStorage, we would add it here.
// The backend sets a cookie 'accessToken', so browser should send it automatically.

import { useToast } from '../composables/useToast';

const { addToast } = useToast();

api.interceptors.response.use(
  response => response,
  error => {
    const message = error.response?.data?.message || 'Bir hata oluştu';
    // Don't show toast for 401 (Unauthorized) as it might be just a session expiry redirect
    if (error.response?.status !== 401) {
      addToast(message, 'error');
    }
    return Promise.reject(error);
  }
);

export default api;
