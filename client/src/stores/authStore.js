import { defineStore } from 'pinia'
import api from '../services/api.js'

// Only store user info client-side; authentication is handled by HttpOnly cookie.
const USER_KEY = 'current_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
    loading: false,
    error: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.user,
  },
  actions: {
    async register({ username, email, password }) {
      this.loading = true
      this.error = null
      try {
        const res = await api.post('/auth/register', { username, email, password })
        // Expect backend to set HttpOnly cookie for session and return user object
        const data = res.data
        // If data has user property use it, otherwise data itself is the user object
        const user = data.user || data
        this.user = user
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
        this.loading = false
        return res
      } catch (err) {
        this.error = err?.body || err?.message || 'Kayıt sırasında bir hata oluştu.'
        this.loading = false
        throw err
      }
    },

    async login({ username, email, password }) {
      this.loading = true
      this.error = null
      try {
        const payload = {}
        if (username) payload.username = username
        if (email) payload.email = email
        if (password) payload.password = password
        // Backend should set HttpOnly cookie on successful login and return user
        const res = await api.post('/auth/login', payload)
        const data = res.data
        // If data has user property use it, otherwise data itself is the user object
        // Don't reconstruct the object to avoid losing fields like balance
        const user = data.user || data
        if (user) {
          this.user = user
          localStorage.setItem(USER_KEY, JSON.stringify(user))
        }
        this.loading = false
        return res
      } catch (err) {
        this.error = err?.body || err?.message || 'Giriş sırasında bir hata oluştu.'
        this.loading = false
        throw err
      }
    },

    updateUser(userData) {
      if (userData) {
        // Merge with existing user data to preserve other fields if needed
        this.user = { ...this.user, ...userData }
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      }
    },

    setUser(user) {
      this.user = user
      if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
      else localStorage.removeItem(USER_KEY)
    },

    logout() {
      // Call backend logout to clear cookie if endpoint exists
      try { api.post('/auth/logout') } catch { }
      this.user = null
      localStorage.removeItem(USER_KEY)
    },
  },
})

export default useAuthStore
