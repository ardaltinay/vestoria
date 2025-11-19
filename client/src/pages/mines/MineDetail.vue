<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">{{ item?.name || 'Maden bulunamadı' }}</h1>
        <p v-if="item" class="text-sm text-gray-500">Maden ayrıntıları</p>
      </div>
      <div>
        <RouterLink to="/home/mines" class="px-3 py-1 text-sm text-gray-600 hover:text-brand-yellow">Geri</RouterLink>
      </div>
    </div>

    <div v-if="!item" class="text-gray-500">Maden bulunamadı veya silinmiş.</div>

    <div v-else>
      <div class="bg-white rounded-lg p-4 shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-gray-500">Seviye</div>
            <div class="font-bold text-lg">{{ item.level }}</div>
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
import { ref, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useMinesStore } from '../../stores/minesStore'

const route = useRoute()
const store = useMinesStore()
const item = ref(null)

function load() {
  store.load()
  item.value = store.getById(route.params.id)
}

onMounted(load)
</script>
