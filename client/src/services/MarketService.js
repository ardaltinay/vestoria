import api from './api';

export default {
  getActiveListings(params) {
    return api.get('/market/listings', { params, suppressToast: true });
  },
  listItem(itemId, data) {
    return api.post(`/market/list/${itemId}`, data)
  },
  buyItem(marketItemId, request) {
    return api.post(`/market/buy/${marketItemId}`, request)
  },
  cancelListing(marketItemId) {
    return api.delete(`/market/listings/${marketItemId}`)
  },
  getMarketTrends() {
    return api.get('/market/trends');
  }
};
