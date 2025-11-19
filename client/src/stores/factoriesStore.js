import { defineStore } from 'pinia'
import { createResourceService } from '../services/resourceService'

const svc = createResourceService('factories', [
  { id: 'factory-1', name: 'Demir Fabrikası', description: 'Temel üretim fabrikası.', level: 2, revenue: 6.0 }
])

export const useFactoriesStore = defineStore('factories', {
  state: () => ({ items: [] }),
  actions: {
    load() { this.items = svc.getAll() },
    getById(id) { return svc.getById(id) },
    create(payload) { const it = svc.create(payload); this.load(); return it },
    update(id, patch) { const it = svc.update(id, patch); this.load(); return it }
  }
})
