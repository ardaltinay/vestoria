import { defineStore } from 'pinia'
import api from '../services/api'

export const useGameDataStore = defineStore('gameData', {
  state: () => ({
    items: [],
    loading: false,
    error: null
  }),

  getters: {
    getItem: (state) => (id) => {
      if (!id) return null
      return state.items.find(item => item.id === id) || { name: id, description: '' }
    },

    getItemsByType: (state) => (type) => {
      return state.items.filter(item => item.type === type && item.id !== type)
    }
  },

  actions: {
    async fetchGameData() {
      if (this.items.length > 0) return // Already loaded

      this.loading = true
      this.error = null

      try {
        const response = await api.get('/game-data/items')
        this.items = response.data
      } catch (err) {
        console.error('Failed to fetch game data:', err)
        this.error = 'Oyun verileri yüklenemedi.'
      } finally {
        this.loading = false
      }
    }
  }
})
