<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">{{ item?.name || 'Çiftlik bulunamadı' }}</h1>
        <p v-if="item" class="text-sm text-gray-500">Çiftlik ayrıntıları</p>
      </div>
      <div>
        <RouterLink to="/home/farms" class="px-3 py-1 text-sm text-gray-600 hover:text-brand-yellow">Geri</RouterLink>
      </div>
    </div>

    <div v-if="!item" class="text-gray-500">Çiftlik bulunamadı veya silinmiş.</div>

    <div v-else>
      <!-- Production Selection (If SubType is null) -->
      <div v-if="!item.subType" class="bg-white rounded-lg p-6 shadow mb-6 border-l-4 border-indigo-500">
        <h2 class="text-lg font-bold text-gray-900 mb-2">Üretime Başla</h2>
        <p class="text-gray-600 mb-4">Bu çiftlik henüz üretim yapmıyor. Üretime başlamak için bir tür seçin:</p>
        
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div v-for="option in productionOptions" :key="option.value" 
               @click="selectProduction(option.value)"
               class="cursor-pointer rounded-lg border border-gray-200 p-4 hover:border-indigo-500 hover:bg-indigo-50 transition-all group">
            <div class="flex items-center justify-between mb-2">
              <span class="font-bold text-gray-900 group-hover:text-indigo-700">{{ option.label }}</span>
              <span class="text-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity">
                Seç &rarr;
              </span>
            </div>
            <p class="text-sm text-gray-500">{{ option.description }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg p-4 shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-gray-500">Seviye</div>
            <div class="font-bold text-lg">{{ item.level }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">Tür</div>
            <div class="font-bold text-lg">{{ getSubTypeLabel(item.subType) || 'Seçilmedi' }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">Gelir /dak</div>
            <div class="font-bold text-lg">{{ item.revenue }} ₺</div>
          </div>
        </div>
        <div class="mt-4 text-gray-700">{{ item.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useFarmsStore } from '../../stores/farmsStore'
import BuildingService from '../../services/BuildingService'

import { useToast } from '../../composables/useToast'

const route = useRoute()
const { addToast } = useToast()
const store = useFarmsStore()
const item = ref(null)

const productionOptions = ref([])

function load() {
  store.load()
  item.value = store.getById(route.params.id)
  fetchProductionTypes()
}

async function fetchProductionTypes() {
  try {
    const response = await BuildingService.getProductionTypes()
    productionOptions.value = response.data.filter(t => t.parentType === 'FARM')
  } catch (error) {
    console.error('Failed to fetch production types:', error)
  }
}

async function selectProduction(type) {
  if (!confirm('Üretim türünü seçmek üzeresiniz. Bu işlem geri alınamaz. Emin misiniz?')) return

  try {
    await BuildingService.setProduction(item.value.id, type)
    // Reload data
    await store.load() // Reload store to get fresh data
    item.value = store.getById(route.params.id) // Update local item
  } catch (error) {
    console.error('Failed to set production:', error)
    addToast('Üretim türü seçilemedi: ' + (error.response?.data?.message || error.message), 'error')
  }
}

function getSubTypeLabel(value) {
  const option = productionOptions.find(o => o.value === value)
  return option ? option.label : value
}

onMounted(load)
</script>
