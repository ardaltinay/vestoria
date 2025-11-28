import { defineStore } from 'pinia'
import BuildingService from '../services/BuildingService'

export const useFactoriesStore = defineStore('factories', {
  state: () => ({ items: [] }),
  actions: {
    async load() {
      try {
        const response = await BuildingService.getUserBuildings()
        this.items = response.data.filter(b => b.type === 'FACTORY')
      } catch (error) {
        console.error('Failed to load factories:', error)
      }
    },
    async getById(id) {
      if (this.items.length === 0) await this.load()
      return this.items.find(i => i.id === id)
    },
    async create(payload) {
      const response = await BuildingService.createBuilding('FACTORY', payload.tier, payload.subType, payload.name)
      await this.load()
      return response.data
    },
    async update(id, patch) {
      await this.load()
    }
  }
})
