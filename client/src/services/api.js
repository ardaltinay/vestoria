import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
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

// Helper to get cookie
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
}

// Request interceptor to add Idempotency-Key and CSRF Token
api.interceptors.request.use(async config => {
  const method = config.method.toUpperCase();

  // Add Idempotency Key for mutating requests
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
    if (!config.headers['Idempotency-Key']) {
      config.headers['Idempotency-Key'] = generateUUID();
    }

    // Manually set CSRF token if available
    let csrfToken = getCookie('XSRF-TOKEN');

    // If no token, try to fetch it
    if (!csrfToken) {
      try {
        // Use a new instance to avoid interceptors
        const response = await axios.get('/api/auth/csrf', {
          withCredentials: true
        });

        // Try to get from body (most reliable)
        if (response.data && response.data.token) {
          csrfToken = response.data.token;
        } else {
          // Fallback to cookie
          csrfToken = getCookie('XSRF-TOKEN');
        }
      } catch (e) {
        console.error("Failed to fetch CSRF token", e);
      }
    }

    if (csrfToken) {
      config.headers['X-XSRF-TOKEN'] = csrfToken;
    }
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  async error => {
    const message = error.response?.data?.message || 'Bilinmeyen bir hata oluştu.';

    // Handle CSRF Token Mismatch (403)
    if (error.response?.status === 403 && !error.config._retryCsrf) {
      error.config._retryCsrf = true;
      try {
        console.log("CSRF token invalid, refreshing...");
        const csrfResponse = await axios.get('/api/auth/csrf', {
          baseURL: '/api',
          withCredentials: true
        });

        let newToken = csrfResponse.data?.token;
        if (!newToken) {
          newToken = getCookie('XSRF-TOKEN');
        }

        if (newToken) {
          console.log("CSRF token refreshed, retrying request...");
          error.config.headers['X-XSRF-TOKEN'] = newToken;
          return api(error.config);
        }
      } catch (refreshError) {
        console.error("Failed to refresh CSRF token", refreshError);
      }
    }

    // Handle Session Expiry or Unauthorized Access
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Clear user data from local storage
      localStorage.removeItem('current_user');

      // Redirect to login page if not already on public pages
      const publicPages = ['/login', '/register', '/'];
      if (!publicPages.includes(window.location.pathname)) {
        window.location.href = '/login';
      }

      // Optional: Show toast only if it's not a routine check and not on landing page
      if (!error.config?.suppressToast && window.location.pathname !== '/') {
        addToast('Oturumunuz sona erdi. Lütfen tekrar giriş yapın.', 'warning');
      }
    } else if (!error.config?.suppressToast) {
      addToast(message, 'error');
    }

    return Promise.reject(error);
  }
);

export default api;
