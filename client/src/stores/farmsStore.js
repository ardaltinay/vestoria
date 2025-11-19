import { defineStore } from 'pinia'
import { createResourceService } from '../services/resourceService'

const svc = createResourceService('farms', [
  { id: 'farm-1', name: 'Büyük Çiftlik', description: 'Süt ve tahıl üreten çiftlik.', level: 3, revenue: 8.5 }
])

export const useFarmsStore = defineStore('farms', {
  state: () => ({ items: [] }),
  actions: {
    load() { this.items = svc.getAll() },
    getById(id) { return svc.getById(id) },
    create(payload) { const it = svc.create(payload); this.load(); return it },
    update(id, patch) { const it = svc.update(id, patch); this.load(); return it }
  }
})
