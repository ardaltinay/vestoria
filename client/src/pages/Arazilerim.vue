<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">Arazilerim</h1>
        <p class="text-sm text-gray-500">Mevcut arazilerinizi görüntüleyebilir, yeni arazi alabilir veya bir araziye tıklayarak ayrıntılarına gidebilirsiniz.</p>
      </div>
      <div>
        <RouterLink to="/home/lands/new" class="inline-flex items-center gap-2 px-4 py-2 bg-brand-yellow text-white rounded shadow">Yeni Arazi Ekle</RouterLink>
      </div>
    </div>

    <div v-if="items.length === 0" class="text-center py-12 text-gray-500">Henüz araziniz yok. Yeni arazi ekleyebilirsiniz.</div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="it in items" :key="it.id" class="bg-white rounded-lg p-4 shadow">
        <div class="flex items-start justify-between">
          <div>
            <h3 class="font-semibold text-lg">{{ it.name }}</h3>
            <div class="text-sm text-gray-500">Seviye: {{ it.level }}</div>
          </div>
          <div>
            <RouterLink :to="`/home/lands/${it.id}`" class="text-sm text-brand-yellow font-semibold">Ayrıntılar →</RouterLink>
          </div>
        </div>
        <div class="mt-3 text-sm text-gray-600">{{ it.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useLandsStore } from '../stores/landsStore'

const store = useLandsStore()
const items = ref([])

function load() {
  store.load()
  items.value = store.items
}

onMounted(load)
</script>

