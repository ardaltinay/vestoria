<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-slate-900">Pazar Yeri</h1>
      <button 
        @click="showSellModal = true"
        class="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
      >
        İlan Ver
      </button>
    </div>

    <!-- Listings Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="listing in listings" :key="listing.id" class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex justify-between items-start mb-4">
          <div>
            <h3 class="font-bold text-slate-900">{{ listing.item.name }}</h3>
            <div class="text-sm text-slate-500">Satıcı: {{ listing.seller.username }}</div>
          </div>
          <div class="bg-emerald-50 text-emerald-700 px-2 py-1 rounded text-xs font-bold">
            ₺{{ listing.price }}
          </div>
        </div>
        
        <div class="space-y-2 mb-4">
          <div class="flex justify-between text-sm">
            <span class="text-slate-500">Miktar:</span>
            <span class="font-medium">{{ listing.quantity }} {{ listing.item.unit }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-slate-500">Kalite:</span>
            <span class="font-medium text-amber-500">★ {{ listing.item.qualityScore }}</span>
          </div>
        </div>

        <button 
          @click="buyItem(listing)"
          class="w-full bg-slate-900 text-white py-2 rounded-lg hover:bg-slate-800 transition-colors text-sm font-medium"
        >
          Satın Al
        </button>
      </div>
    </div>

    <!-- Sell Modal (Simplified) -->
    <div v-if="showSellModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white p-6 rounded-xl w-full max-w-md">
        <h2 class="text-xl font-bold mb-4">İlan Oluştur</h2>
        <!-- Form fields would go here -->
        <p class="text-slate-500 mb-4">Bu özellik henüz yapım aşamasında.</p>
        <div class="flex justify-end gap-3">
          <button @click="showSellModal = false" class="text-slate-600 hover:text-slate-900">İptal</button>
          <button @click="showSellModal = false" class="bg-primary-600 text-white px-4 py-2 rounded-lg">Tamam</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MarketService from '../services/MarketService'
import { useToast } from '../composables/useToast'

const listings = ref([])
const showSellModal = ref(false)
const { addToast } = useToast()

const fetchListings = async () => {
  try {
    const response = await MarketService.getActiveListings()
    listings.value = response.data
  } catch (error) {
    console.error('Error fetching listings:', error)
  }
}

const buyItem = async (listing) => {
  try {
    // For simplicity, buying 1 unit or all? Let's assume buying 1 for now or prompt quantity
    // The API expects quantity.
    const quantity = 1; // Default to 1 for now
    await MarketService.buyItem(listing.id, quantity)
    addToast('Satın alma başarılı!', 'success')
    fetchListings() // Refresh
  } catch (error) {
    // Error handled globally
  }
}

onMounted(() => {
  fetchListings()
})
</script>
