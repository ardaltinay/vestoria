import { defineStore } from 'pinia'
import BuildingService from '../services/BuildingService'

export const useBuildingConfigStore = defineStore('buildingConfig', {
  state: () => ({
    configs: [],
    loading: false,
    error: null,
    lastFetched: null
  }),

  getters: {
    /**
     * Get config for specific building type and tier
     * @param {string} type - Building type (SHOP, FARM, etc.)
     * @param {string} tier - Tier (SMALL, MEDIUM, LARGE)
     */
    getConfig: (state) => (type, tier) => {
      return state.configs.find(c => c.type === type && c.tier === tier) || null
    },

    /**
     * Get all configs for a specific building type
     * @param {string} type - Building type
     */
    getConfigsByType: (state) => (type) => {
      return state.configs.filter(c => c.type === type)
    },

    /**
     * Check if configs are loaded
     */
    isLoaded: (state) => state.configs.length > 0,

    /**
     * Get all configs
     */
    allConfigs: (state) => state.configs
  },

  actions: {
    /**
     * Fetch building configs from API
     * @param {boolean} force - Force refresh even if already loaded
     */
    async fetchConfigs(force = false) {
      // Skip if already loaded and not forcing refresh
      if (this.configs.length > 0 && !force) {
        return this.configs
      }

      this.loading = true
      this.error = null

      try {
        const response = await BuildingService.getBuildingConfigs()
        this.configs = response.data
        this.lastFetched = new Date()
        return this.configs
      } catch (error) {
        this.error = error.message || 'Failed to fetch building configs'
        console.error('Error fetching building configs:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * Clear cached configs (useful for testing or manual refresh)
     */
    clearCache() {
      this.configs = []
      this.lastFetched = null
      this.error = null
    }
  }
})

export default useBuildingConfigStore
