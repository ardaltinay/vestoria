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

    <!-- Empty State -->
    <div v-if="listings.length === 0" class="text-center py-12 bg-white rounded-xl border border-slate-200 shadow-sm">
      <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-400">
        <ShoppingCartIcon class="w-8 h-8" />
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-1">Henüz İlan Yok</h3>
      <p class="text-slate-500 max-w-sm mx-auto mb-6">
        Şu anda pazarda aktif bir ilan bulunmuyor. İlk ilanı siz verin!
      </p>
      <button 
        @click="showSellModal = true"
        class="bg-primary-600 text-white px-6 py-2.5 rounded-xl font-bold hover:bg-primary-700 transition-colors shadow-lg shadow-primary-600/20"
      >
        Hemen İlan Ver
      </button>
    </div>

    <!-- Listings Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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

    <!-- Sell Modal -->
    <Teleport to="body">
      <div 
        v-if="showSellModal" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
        @click.self="showSellModal = false"
      >
        <div class="bg-white rounded-2xl w-full max-w-md overflow-hidden shadow-2xl relative">
          <div class="p-6 border-b border-slate-100 flex justify-between items-center">
            <h2 class="text-xl font-bold text-slate-900">İlan Oluştur</h2>
            <button @click="showSellModal = false" class="text-slate-400 hover:text-slate-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          
          <div class="p-6 space-y-4">
            <div v-if="loadingInventory" class="text-center py-8 text-slate-400">
              Envanter yükleniyor...
            </div>
            
            <div v-else-if="inventory.length === 0" class="text-center py-8">
              <div class="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-3 text-slate-400">
                <ArchiveBoxIcon class="w-6 h-6" />
              </div>
              <p class="text-slate-600 font-medium">Satılacak ürününüz yok</p>
              <p class="text-xs text-slate-400 mt-1">Üretim yaparak ürün elde edebilirsiniz.</p>
            </div>

            <form v-else @submit.prevent="handleCreateListing" class="space-y-4">
              <!-- Item Selection -->
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">Ürün Seçin</label>
                <div class="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto p-1">
                  <div 
                    v-for="item in inventory" 
                    :key="item.id"
                    @click="selectedItem = item"
                    class="p-3 rounded-xl border cursor-pointer transition-all relative"
                    :class="selectedItem?.id === item.id ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500' : 'border-slate-200 hover:border-primary-200 hover:bg-slate-50'"
                  >
                    <div class="font-bold text-slate-900 text-sm">{{ item.name }}</div>
                    <div class="text-xs text-slate-500 mt-1">{{ item.quantity }} {{ item.unit }}</div>
                    <div class="absolute top-2 right-2">
                      <div class="w-2 h-2 rounded-full" :class="getQualityColor(item.qualityScore)"></div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="selectedItem" class="grid grid-cols-2 gap-4">
                <!-- Quantity -->
                <div>
                  <label class="block text-sm font-medium text-slate-700 mb-1">Miktar</label>
                  <input 
                    v-model.number="listingForm.quantity"
                    type="number" 
                    min="1" 
                    :max="selectedItem.quantity"
                    class="w-full px-3 py-2 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500"
                  >
                </div>
                
                <!-- Price -->
                <div>
                  <label class="block text-sm font-medium text-slate-700 mb-1">Birim Fiyat (₺)</label>
                  <input 
                    v-model.number="listingForm.price"
                    type="number" 
                    min="1" 
                    step="0.1"
                    class="w-full px-3 py-2 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500"
                  >
                </div>
              </div>

              <!-- Summary -->
              <div v-if="selectedItem && listingForm.price && listingForm.quantity" class="bg-slate-50 p-3 rounded-lg text-sm flex justify-between items-center">
                <span class="text-slate-600">Toplam Kazanç:</span>
                <span class="font-bold text-emerald-600">₺{{ (listingForm.price * listingForm.quantity).toFixed(2) }}</span>
              </div>

              <div class="pt-2 flex gap-3">
                <button 
                  type="button" 
                  @click="showSellModal = false" 
                  class="flex-1 py-2.5 text-slate-600 font-medium hover:bg-slate-50 rounded-xl transition-colors"
                >
                  İptal
                </button>
                <button 
                  type="submit"
                  :disabled="!isValidListing"
                  class="flex-1 py-2.5 bg-primary-600 text-white font-bold rounded-xl hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg shadow-primary-600/20"
                >
                  İlanı Yayınla
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import MarketService from '../services/MarketService'
import InventoryService from '../services/InventoryService'
import { useToast } from '../composables/useToast'
import { XMarkIcon, ArchiveBoxIcon, ShoppingCartIcon } from '@heroicons/vue/24/outline'

const listings = ref([])
const inventory = ref([])
const showSellModal = ref(false)
const loadingInventory = ref(false)
const selectedItem = ref(null)
const listingForm = ref({
  quantity: 1,
  price: 10
})

const { addToast } = useToast()
let pollingInterval = null

const isValidListing = computed(() => {
  return selectedItem.value && 
         listingForm.value.quantity > 0 && 
         listingForm.value.quantity <= selectedItem.value.quantity &&
         listingForm.value.price > 0
})

const getQualityColor = (score) => {
  if (score >= 8) return 'bg-emerald-500'
  if (score >= 5) return 'bg-amber-500'
  return 'bg-slate-300'
}

const fetchListings = async () => {
  try {
    const response = await MarketService.getActiveListings()
    listings.value = response.data
  } catch (error) {
    console.error('Error fetching listings:', error)
  }
}

const fetchInventory = async () => {
  try {
    loadingInventory.value = true
    const response = await InventoryService.getMyInventory()
    // Filter out items with 0 quantity
    inventory.value = response.data.filter(item => item.quantity > 0)
  } catch (error) {
    console.error('Error fetching inventory:', error)
    addToast('Envanter yüklenemedi', 'error')
  } finally {
    loadingInventory.value = false
  }
}

const handleCreateListing = async () => {
  if (!isValidListing.value) return

  try {
    await MarketService.listItem(selectedItem.value.id, {
      itemId: selectedItem.value.id, // Redundant but required by DTO
      quantity: listingForm.value.quantity,
      price: listingForm.value.price
    })
    
    addToast('İlan başarıyla oluşturuldu!', 'success')
    showSellModal.value = false
    fetchListings()
    // Reset form
    selectedItem.value = null
    listingForm.value = { quantity: 1, price: 10 }
  } catch (error) {
    console.error('Listing creation failed:', error)
  }
}

const buyItem = async (listing) => {
  try {
    await MarketService.buyItem(listing.id, { quantity: 1 }) // Assuming buying 1 for now
    addToast('Satın alma başarılı!', 'success')
    fetchListings()
  } catch (error) {
    // Error handled globally
  }
}

watch(showSellModal, (newValue) => {
  if (newValue) {
    fetchInventory()
  }
})

onMounted(() => {
  fetchListings()
  // Poll every 10 seconds
  pollingInterval = setInterval(fetchListings, 10000)
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
})
</script>
