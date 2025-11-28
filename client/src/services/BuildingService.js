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
    // Assuming we might add this endpoint or use a user endpoint that returns buildings
    // For now, let's assume the backend has /build/my-buildings or similar, 
    // OR we fetch user details which includes buildings.
    // The backend BuildingController doesn't have getUserBuildings yet.
    // But BuildingService has getUserBuildings(userId).
    // We should add an endpoint for it in BuildingController.
    // For now, I'll add a placeholder or use what I have.
    return api.get(`/build/list?t=${new Date().getTime()}`); // Prevent caching
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
  completeProduction(buildingId) {
    return api.post(`/build/${buildingId}/complete-production`);
  },
  withdraw(buildingId, productId, quantity) {
    return api.post(`/build/${buildingId}/withdraw`, { productId, quantity });
  }
};
