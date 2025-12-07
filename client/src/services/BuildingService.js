import api from './api';

export default {
  createBuilding(type, tier, subType, name = null) {
    return api.post('/build/create', { type, tier, subType, name });
  },
  upgrade(buildingId) {
    return api.put(`/build/upgrade/${buildingId}`);
  },
  closeBuilding(buildingId) {
    return api.delete(`/build/close/${buildingId}`);
  },

  setProduction(buildingId, productionType) {
    return api.post(`/build/production/${buildingId}`, { productionType });
  },

  getUserBuildings() {
    // Dedup concurrent calls
    if (this._buildingsPromise) return this._buildingsPromise;

    this._buildingsPromise = api.get('/build/list')
      .finally(() => {
        // Clear promise after a short delay to allow subsequent refreshes
        setTimeout(() => {
          this._buildingsPromise = null;
        }, 100);
      });

    return this._buildingsPromise;
  },
  getBuildingConfigs() {
    return api.get('/build/config');
  },
  getProductionTypes() {
    return api.get('/build/production-types');
  },
  startSales(buildingId) {
    return api.post(`/build/${buildingId}/start-sales`);
  },
  startProduction(buildingId, productId) {
    return api.post(`/build/${buildingId}/start-production`, { productId });
  },
  collect(buildingId) {
    return api.post(`/build/${buildingId}/collect`);
  },
  completeSale(buildingId) {
    return api.post(`/build/${buildingId}/complete-sale`);
  },
  withdraw(buildingId, productId, quantity) {
    return api.post(`/build/${buildingId}/withdraw`, { productId, quantity });
  }
};
