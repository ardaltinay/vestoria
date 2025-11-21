import api from './api'

export default {
  getMyInventory() {
    return api.get('/inventory', { suppressToast: true })
  }
}
