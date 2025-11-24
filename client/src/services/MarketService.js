import api from './api';

export default {
  getActiveListings(params) {
    return api.get('/market/listings', { params, suppressToast: true });
  },
  listItem(itemId, data) {
    return api.post(`/market/list/${itemId}`, data)
  },
  buyItem(marketItemId, data) {
    return api.post(`/market/buy/${marketItemId}`, data)
  }
};
