import { defineStore } from 'pinia'
import { createResourceService } from '../services/resourceService'

const svc = createResourceService('lands', [
  { id: 'land-1', name: 'Kuzey Arazisi', description: 'Verimli toprak.', level: 1, revenue: 1.2 }
])

export const useLandsStore = defineStore('lands', {
  state: () => ({ items: [] }),
  actions: {
    load() { this.items = svc.getAll() },
    getById(id) { return svc.getById(id) },
    create(payload) { const it = svc.create(payload); this.load(); return it },
    update(id, patch) { const it = svc.update(id, patch); this.load(); return it }
  }
})
