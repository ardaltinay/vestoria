import api from './api'

export default {
  getMyInventory() {
    return api.get('/inventory', { suppressToast: true })
  },
  updateItemPrice(itemId, price) {
    return api.put(`/inventory/item/${itemId}/price`, { price })
  }
}
