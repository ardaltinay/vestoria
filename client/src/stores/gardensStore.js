import { defineStore } from 'pinia'
import { createResourceService } from '../services/resourceService'

const svc = createResourceService('gardens', [
  { id: 'garden-1', name: 'Orman Bahçesi', description: 'Taze meyve bahçesi.', level: 1, revenue: 2.4 }
])

export const useGardensStore = defineStore('gardens', {
  state: () => ({ items: [] }),
  actions: {
    load() { this.items = svc.getAll() },
    getById(id) { return svc.getById(id) },
    create(payload) { const it = svc.create(payload); this.load(); return it },
    update(id, patch) { const it = svc.update(id, patch); this.load(); return it }
  }
})
