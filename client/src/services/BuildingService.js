import api from './api';

export default {
  createBuilding(type, tier, subType) {
    return api.post('/build/create', { type, tier, subType });
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
    return api.get('/build/list'); // Need to implement this in backend if not exists
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
  startProduction(buildingId) {
    return api.post(`/build/${buildingId}/start-production`);
  },
  collect(buildingId) {
    return api.post(`/build/${buildingId}/collect`);
  }
};
