import { defineStore } from 'pinia'
import { createResourceService } from '../services/resourceService'

const svc = createResourceService('shops', [
  { id: 'shop-1', name: 'Merkez Dükkan', description: 'Şehrin merkezi dükkanı.', level: 2, revenue: 12.5 },
  { id: 'shop-2', name: 'Küçük Bakkal', description: 'Mahalle bakkalı.', level: 1, revenue: 3.2 }
])

export const useShopsStore = defineStore('shops', {
  state: () => ({ items: [] }),
  actions: {
    load() { this.items = svc.getAll() },
    getById(id) { return svc.getById(id) },
    create(payload) { const it = svc.create(payload); this.load(); return it },
    update(id, patch) { const it = svc.update(id, patch); this.load(); return it }
  }
})
