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

// UUID generator for Idempotency-Key
function generateUUID() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

// Request interceptor to add Idempotency-Key
api.interceptors.request.use(config => {
  const method = config.method.toUpperCase();
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
    if (!config.headers['Idempotency-Key']) {
      config.headers['Idempotency-Key'] = generateUUID();
    }
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  error => {
    const message = error.response?.data?.message || 'Bir hata oluştu';
    // Don't show toast for 401 (Unauthorized) as it might be just a session expiry redirect
    // Also check if the request config explicitly asked to suppress toasts
    if (error.response?.status !== 401 && !error.config?.suppressToast) {
      addToast(message, 'error');
    }
    return Promise.reject(error);
  }
);

export default api;
