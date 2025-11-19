import { defineStore } from 'pinia'
import { createResourceService } from '../services/resourceService'

const svc = createResourceService('mines', [
  { id: 'mine-1', name: 'Bakır Ocağı', description: 'Yer altı kaynak çıkartma ocağı.', level: 1, revenue: 4.0 }
])

export const useMinesStore = defineStore('mines', {
  state: () => ({ items: [] }),
  actions: {
    load() { this.items = svc.getAll() },
    getById(id) { return svc.getById(id) },
    create(payload) { const it = svc.create(payload); this.load(); return it },
    update(id, patch) { const it = svc.update(id, patch); this.load(); return it }
  }
})
