import { defineStore } from 'pinia'
import InventoryService from '../services/InventoryService'

export const useInventoryStore = defineStore('inventory', {
  state: () => ({
    items: [],
    centralizedItems: [],
    loading: false,
    error: null
  }),

  getters: {
    /**
     * Get items by building ID
     */
    getItemsByBuilding: (state) => (buildingId) => {
      return state.items.filter(item => item.building?.id === buildingId)
    },

    /**
     * Get centralized inventory items (no building assigned)
     */
    getCentralized: (state) => state.centralizedItems,

    /**
     * Check if inventory is loaded
     */
    isLoaded: (state) => state.items.length > 0 || state.centralizedItems.length > 0
  },

  actions: {
    /**
     * Load all inventory items
     */
    async loadAll() {
      this.loading = true
      this.error = null
      try {
        const response = await InventoryService.getMyInventory()
        this.items = response.data
        return this.items
      } catch (error) {
        this.error = error.message
        console.error('Failed to load inventory:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * Load only centralized inventory
     */
    async loadCentralized() {
      this.loading = true
      this.error = null
      try {
        const response = await InventoryService.getCentralizedInventory()
        this.centralizedItems = response.data
        return this.centralizedItems
      } catch (error) {
        this.error = error.message
        console.error('Failed to load centralized inventory:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * Transfer item to building
     */
    async transferToBuilding(itemId, buildingId, quantity) {
      try {
        await InventoryService.transferToBuilding(itemId, buildingId, quantity)
        // Reload centralized inventory after transfer
        await this.loadCentralized()
      } catch (error) {
        console.error('Failed to transfer item:', error)
        throw error
      }
    },

    /**
     * Update item price
     */
    async updatePrice(itemId, price) {
      try {
        await InventoryService.updateItemPrice(itemId, price)
        await this.loadAll()
      } catch (error) {
        console.error('Failed to update price:', error)
        throw error
      }
    }
  }
})

export default useInventoryStore
