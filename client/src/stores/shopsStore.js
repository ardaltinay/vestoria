import { defineStore } from 'pinia'
import BuildingService from '../services/BuildingService'

export const useShopsStore = defineStore('shops', {
  state: () => ({ items: [] }),
  actions: {
    async load() {
      try {
        const response = await BuildingService.getUserBuildings()
        // Filter only SHOP type buildings
        this.items = response.data.filter(b => b.type === 'SHOP')
      } catch (error) {
        console.error('Failed to load shops:', error)
      }
    },
    async getById(id) {
      // If items not loaded, load them first? Or fetch specific?
      // For now, find in items or fetch all.
      if (this.items.length === 0) await this.load()
      return this.items.find(i => i.id === id)
    },
    async create(payload) {
      // payload should contain { type: 'SHOP', subType: '...', tier: '...' }
      const response = await BuildingService.createBuilding('SHOP', payload.tier, payload.subType || 'MARKET')
      await this.load()
      return response.data
    },
    async update(id, patch) {
      // Implement upgrade logic here if needed
      // For now, just reload
      await this.load()
    }
  }
})
