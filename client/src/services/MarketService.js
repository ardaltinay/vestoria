import api from './api';

export default {
  getActiveListings() {
    return api.get('/market/list');
  },
  listItem(itemId, quantity, price) {
    return api.post('/market/sell', { itemId, quantity, price });
  },
  buyItem(marketItemId, quantity) {
    return api.post('/market/buy', { marketItemId, quantity });
  }
};
