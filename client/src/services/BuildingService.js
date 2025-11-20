import api from './api';

export default {
  createBuilding(type, tier, subType) {
    return api.post('/build/create', { type, tier, subType });
  },
  upgradeBuilding(buildingId) {
    return api.post(`/build/upgrade/${buildingId}`);
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
  }
};
