import api from './api.js'

const STORAGE_KEY = 'kapitalizm_shops_v1'

function ensureSeed() {
  if (!localStorage.getItem(STORAGE_KEY)) {
    const sample = [
      { id: 'shop-1', name: 'Merkez Dükkan', description: 'Şehrin merkezi dükkanı. Yüksek müşteri trafiği.', level: 2, revenue: 12.5 },
      { id: 'shop-2', name: 'Küçük Bakkal', description: 'Mahalle tipi bakkal.', level: 1, revenue: 3.2 }
    ]
    localStorage.setItem(STORAGE_KEY, JSON.stringify(sample))
  }
}

export default {
  async getAll() {
    try {
      return await api.get('/api/shops')
    } catch {
      ensureSeed()
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    }
  },
  async getById(id) {
    try {
      return await api.get(`/api/shops/${id}`)
    } catch {
      ensureSeed()
      const arr = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
      return arr.find(s => s.id === id) || null
    }
  },
  async create(payload) {
    try {
      return await api.post('/api/shops', payload)
    } catch {
      ensureSeed()
      const arr = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
      const id = 'shop-' + (Date.now())
      const item = { id, ...payload }
      arr.push(item)
      localStorage.setItem(STORAGE_KEY, JSON.stringify(arr))
      return item
    }
  },
  async update(id, patch) {
    try {
      return await api.put(`/api/shops/${id}`, patch)
    } catch {
      const arr = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
      const idx = arr.findIndex(s => s.id === id)
      if (idx === -1) return null
      arr[idx] = { ...arr[idx], ...patch }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(arr))
      return arr[idx]
    }
  }
}
