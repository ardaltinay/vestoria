import api from './api';

export default {
  getActiveListings() {
    return api.get('/market/listings', { suppressToast: true });
  },
  listItem(itemId, data) {
    return api.post(`/market/list/${itemId}`, data)
  },
  buyItem(marketItemId, data) {
    return api.post(`/market/buy/${marketItemId}`, data)
  }
};
