import api from './api'

export default {
  /**
   * Get all inventory items for current user
   */
  getMyInventory() {
    return api.get('/inventory')
  },

  /**
   * Get only centralized inventory (items not in any building)
   */
  getCentralizedInventory() {
    return api.get('/inventory/centralized')
  },

  /**
   * Transfer item from centralized inventory to a building
   * @param {string} itemId - Item UUID
   * @param {string} buildingId - Building UUID
   * @param {number} quantity - Quantity to transfer
   */
  transferToBuilding(itemId, buildingId, quantity) {
    return api.post('/inventory/transfer', {
      itemId,
      buildingId,
      quantity
    })
  },

  /**
   * Update item price
   * @param {string} itemId - Item UUID
   * @param {number} price - New price
   */
  updateItemPrice(itemId, price) {
    return api.put(`/inventory/item/${itemId}/price`, { price })
  }
}
