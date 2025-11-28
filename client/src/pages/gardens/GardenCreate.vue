<template>
  <div class="p-6 max-w-xl">
    <h1 class="text-2xl font-bold mb-4">Yeni Bahçe Aç</h1>
    <form @submit.prevent="submit" class="bg-white rounded-lg p-4 shadow">
      <label class="block mb-3">
        <div class="text-sm text-gray-600 mb-1">Bahçe İsmi</div>
        <input v-model="form.name" class="w-full border rounded px-3 py-2" required />
      </label>
      <label class="block mb-3">
        <div class="text-sm text-gray-600 mb-1">Açıklama</div>
        <textarea v-model="form.description" class="w-full border rounded px-3 py-2" rows="3"></textarea>
      </label>
      <label class="block mb-3">
        <div class="text-sm text-gray-600 mb-1">Başlangıç Seviyesi</div>
        <input type="number" v-model.number="form.level" class="w-24 border rounded px-3 py-2" min="1" />
      </label>
      <div class="flex items-center gap-2 mt-4">
        <button type="submit" class="px-4 py-2 bg-brand-yellow text-white rounded">Oluştur</button>
        <RouterLink to="/home/gardens" class="px-4 py-2 bg-gray-200 text-gray-800 rounded text-sm">İptal</RouterLink>
      </div>
    </form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useGardensStore } from '../../stores/gardensStore'
import { useToast } from '../../composables/useToast'

const router = useRouter()
const store = useGardensStore()
const { addToast } = useToast()
const form = reactive({ name: '', description: '', level: 1, revenue: 0 })

async function submit() {
  if (!form.name || !form.name.trim()) {
    addToast('Lütfen bahçe ismini giriniz', 'warning')
    return
  }

  try {
    const created = await store.create({ ...form })
    router.push(`/home/gardens/${created.id}`)
  } catch (error) {
    console.error(error)
    addToast('Bahçe oluşturulamadı', 'error')
  }
}
</script>
