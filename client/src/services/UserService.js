import api from './api'

export default {
  getDashboardStats() {
    return api.get('/users/dashboard-stats')
  }
}
