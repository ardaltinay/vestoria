import api from './api.js'

export function createResourceService(storageKey, seed = [], apiPath = null) {
  const KEY = storageKey

  function ensureSeed() {
    if (!localStorage.getItem(KEY)) {
      localStorage.setItem(KEY, JSON.stringify(seed))
    }
  }

  return {
    async getAll() {
      if (apiPath) {
        try {
          return await api.get(apiPath)
        } catch {
          // fallback to localStorage
        }
      }
      ensureSeed()
      return JSON.parse(localStorage.getItem(KEY) || '[]')
    },
    async getById(id) {
      if (apiPath) {
        try {
          return await api.get(`${apiPath}/${id}`)
        } catch {
          // fallback
        }
      }
      ensureSeed()
      const arr = JSON.parse(localStorage.getItem(KEY) || '[]')
      return arr.find(r => r.id === id) || null
    },
    async create(payload) {
      if (apiPath) {
        try {
          return await api.post(apiPath, payload)
        } catch {
          // fallback
        }
      }
      ensureSeed()
      const arr = JSON.parse(localStorage.getItem(KEY) || '[]')
      const id = `${storageKey}-${Date.now()}`
      const item = { id, ...payload }
      arr.push(item)
      localStorage.setItem(KEY, JSON.stringify(arr))
      return item
    },
    async update(id, patch) {
      if (apiPath) {
        try {
          return await api.put(`${apiPath}/${id}`, patch)
        } catch {
          // fallback
        }
      }
      const arr = JSON.parse(localStorage.getItem(KEY) || '[]')
      const idx = arr.findIndex(i => i.id === id)
      if (idx === -1) return null
      arr[idx] = { ...arr[idx], ...patch }
      localStorage.setItem(KEY, JSON.stringify(arr))
      return arr[idx]
    }
  }
}

export default createResourceService
