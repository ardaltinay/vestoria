<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">{{ shop?.name || 'Dükkan bulunamadı' }}</h1>
        <p v-if="shop" class="text-sm text-gray-500">Dükkan ayrıntıları</p>
      </div>
      <div>
        <RouterLink to="/home/shops" class="px-3 py-1 text-sm text-gray-600 hover:text-brand-yellow">Geri</RouterLink>
      </div>
    </div>

    <div v-if="!shop" class="text-gray-500">Dükkan bulunamadı veya silinmiş.</div>

    <div v-else class="grid grid-cols-1 gap-4">
      <div class="bg-white rounded-lg p-4 shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-gray-500">Seviye</div>
            <div class="font-bold text-lg">{{ shop.level }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">Gelir /dak</div>
            <div class="font-bold text-lg">{{ shop.revenue }} ₺</div>
          </div>
        </div>
        <div class="mt-4 text-gray-700">{{ shop.description }}</div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow">
        <h3 class="font-semibold mb-2">Hızlı İşlemler</h3>
        <div class="flex gap-2">
          <button class="px-3 py-2 bg-brand-yellow text-white rounded">Çalıştır</button>
          <button class="px-3 py-2 bg-gray-200 text-gray-800 rounded">Yükselt</button>
          <button class="px-3 py-2 bg-red-100 text-red-600 rounded">Kapat</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import ShopService from '../../services/shopService'

const route = useRoute()
const shop = ref(null)

function load() {
  const id = route.params.id
  shop.value = ShopService.getById(id)
}

onMounted(load)
</script>
