<template>
  <div class="space-y-4 sm:space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4">
      <h1 class="text-xl sm:text-2xl font-bold text-slate-900">Pazar Yeri</h1>
      <div class="flex gap-2 sm:gap-3">
        <div class="flex-1 sm:flex-initial sm:w-64">
          <div class="relative">
            <input 
              v-model="searchQuery"
              @input="handleSearch"
              type="text" 
              placeholder="Ürün ara..." 
              class="w-full pl-9 sm:pl-10 pr-3 sm:pr-4 py-2 text-sm rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500"
            >
            <MagnifyingGlassIcon class="w-4 h-4 sm:w-5 sm:h-5 text-slate-400 absolute left-2.5 sm:left-3 top-2.5" />
          </div>
        </div>
        <button 
          @click="showSellModal = true"
          class="bg-primary-600 text-white px-3 sm:px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors text-sm font-medium whitespace-nowrap"
        >
          İlan Ver
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="listings.length === 0" class="text-center py-8 sm:py-12 bg-white rounded-lg sm:rounded-xl border border-slate-200 shadow-sm">
      <div class="w-12 h-12 sm:w-16 sm:h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-3 sm:mb-4 text-slate-400">
        <ShoppingCartIcon class="w-6 h-6 sm:w-8 sm:h-8" />
      </div>
      <h3 class="text-base sm:text-lg font-bold text-slate-900 mb-1">Henüz İlan Yok</h3>
      <p class="text-sm text-slate-500 max-w-sm mx-auto mb-4 sm:mb-6 px-4">
        Şu anda pazarda aktif bir ilan bulunmuyor. İlk ilanı siz verin!
      </p>
      <button 
        @click="showSellModal = true"
        class="bg-primary-600 text-white px-5 sm:px-6 py-2 sm:py-2.5 rounded-lg sm:rounded-xl font-bold hover:bg-primary-700 transition-colors shadow-lg shadow-primary-600/20 text-sm"
      >
        Hemen İlan Ver
      </button>
    </div>

    <!-- Content -->
    <div v-else class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500 font-semibold">
              <th class="px-3 sm:px-6 py-3 sm:py-4">Ürün</th>
              <th class="px-3 sm:px-6 py-3 sm:py-4">Satıcı</th>
              <th class="px-3 sm:px-6 py-3 sm:py-4">Miktar</th>
              <th class="px-3 sm:px-6 py-3 sm:py-4">Kalite</th>
              <th class="px-3 sm:px-6 py-3 sm:py-4">Fiyat</th>
              <th class="px-3 sm:px-6 py-3 sm:py-4 text-right">İşlem</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr 
              v-for="listing in listings" 
              :key="listing.id"
              class="hover:bg-slate-50 transition-colors group"
            >
              <td class="px-3 sm:px-6 py-3 sm:py-4">
                <div class="flex items-center gap-3">
                  <ProductIcon :name="listing.itemName.trim()" size="md" class="group-hover:scale-110 transition-transform" />
                  <div>
                    <div class="font-medium text-slate-900 text-sm sm:text-base">{{ listing.itemName }}</div>
                  </div>
                </div>
              </td>
              <td class="px-3 sm:px-6 py-3 sm:py-4 text-slate-600 text-xs sm:text-sm">{{ listing.sellerUsername }}</td>
              <td class="px-3 sm:px-6 py-3 sm:py-4">
                <span class="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-slate-100 text-slate-600">
                  {{ listing.sellerUsername.toLowerCase() === 'vestoria' ? '∞' : listing.quantity }}
                </span>
              </td>
              <td class="px-3 sm:px-6 py-3 sm:py-4">
                <StarRating :score="listing.qualityScore" size="xs" />
              </td>
              <td class="px-3 sm:px-6 py-3 sm:py-4 font-bold text-emerald-600 text-xs sm:text-sm">
                <div class="flex items-center gap-1">
                  <CurrencyIcon class="w-3 h-3 sm:w-4 sm:h-4" />
                  {{ formatCurrency(listing.price) }}
                </div>
              </td>
              <td class="px-3 sm:px-6 py-3 sm:py-4 text-right">
                <button 
                  v-if="listing.sellerUsername !== currentUser?.username"
                  @click="openBuyModal(listing)"
                  class="inline-flex items-center justify-center px-3 py-1.5 sm:px-4 sm:py-2 bg-primary-600 text-white text-xs sm:text-sm font-medium rounded-lg hover:bg-primary-700 transition-colors shadow-sm hover:shadow-md active:scale-95"
                  title="Satın Al"
                >
                  <ShoppingCartIcon class="w-3 h-3 sm:w-4 sm:h-4 mr-1.5" />
                  <span class="hidden sm:inline">Satın Al</span>
                  <span class="sm:hidden">Al</span>
                </button>
                <button 
                  v-else
                  @click="handleCancelListing(listing)"
                  :disabled="isCancelling"
                  class="inline-flex items-center justify-center px-3 py-1.5 sm:px-4 sm:py-2 bg-red-100 text-red-700 text-xs sm:text-sm font-medium rounded-lg hover:bg-red-200 transition-colors shadow-sm hover:shadow-md active:scale-95 disabled:opacity-50"
                  title="İlanı Kaldır"
                >
                  <XMarkIcon class="w-3 h-3 sm:w-4 sm:h-4 mr-1.5" />
                  <span class="hidden sm:inline">Kaldır</span>
                  <span class="sm:hidden">Sil</span>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination Controls -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-3 sm:gap-4 mt-4 sm:mt-6">
      <button 
        @click="changePage(currentPage - 1)"
        :disabled="currentPage === 0"
        class="px-3 sm:px-4 py-2 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed text-slate-600 font-medium text-sm"
      >
        Önceki
      </button>
      <span class="text-slate-600 text-sm">
        Sayfa {{ currentPage + 1 }} / {{ totalPages }}
      </span>
      <button 
        @click="changePage(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        class="px-3 sm:px-4 py-2 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed text-slate-600 font-medium text-sm"
      >
        Sonraki
      </button>
    </div>

    <!-- Buy Modal -->
    <Teleport to="body">
      <div 
        v-if="showBuyModal && selectedListing" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
        @click.self="closeBuyModal"
      >
        <div class="bg-white rounded-2xl w-full max-w-md overflow-hidden shadow-2xl relative">
          <div class="p-6 border-b border-slate-100 flex justify-between items-center">
            <h2 class="text-xl font-bold text-slate-900">Ürün Satın Al</h2>
            <button @click="closeBuyModal" class="text-slate-400 hover:text-slate-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          
          <div class="p-6 space-y-6">
            <!-- Item Details -->
            <div class="flex items-center gap-4 bg-slate-50 p-4 rounded-xl border border-slate-100">
              <div class="flex-1">
                <h3 class="font-bold text-slate-900">{{ selectedListing.itemName }}</h3>
                <div class="text-sm text-slate-500">Satıcı: {{ selectedListing.sellerUsername }}</div>
              </div>
              <div class="text-right">
                <div class="font-bold text-emerald-600 flex items-center justify-end gap-1">
                  <CurrencyIcon :size="18" variant="success" />
                  {{ formatCurrency(selectedListing.price) }}
                </div>
                <div class="text-xs text-slate-400">/ birim</div>
              </div>
            </div>

            <!-- Quantity Input -->
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">
                Miktar (Maks: {{ selectedListing.quantity }})
              </label>
              <div class="flex items-center gap-2">
                <input 
                  v-model.number="buyQuantity"
                  type="number" 
                  min="1" 
                  :max="selectedListing.quantity"
                  class="w-full px-4 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 font-bold text-lg"
                >
                <span class="text-slate-500 font-medium">{{ getItemUnitTr(selectedListing.itemUnit) }}</span>
              </div>
            </div>

            <!-- Total Cost -->
            <div class="flex justify-between items-center py-4 border-t border-slate-100">
              <span class="text-slate-600 font-medium">Toplam Tutar</span>
              <span class="text-2xl font-bold text-emerald-600 flex items-center gap-2">
                <CurrencyIcon :size="24" variant="success" />
                {{ formatCurrency((selectedListing.price * buyQuantity).toFixed(2)) }}
              </span>
            </div>

            <!-- Actions -->
            <div class="flex gap-3">
              <button 
                @click="closeBuyModal" 
                class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
              >
                İptal
              </button>
              <button 
                @click="confirmBuy"
                :disabled="isBuying || !isValidBuyQuantity"
                class="flex-1 py-3 bg-slate-900 text-white font-bold rounded-xl hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg shadow-slate-900/20"
              >
                <span v-if="isBuying">İşleniyor...</span>
                <span v-else>Satın Al</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Cancel Confirmation Modal -->
    <Teleport to="body">
      <div 
        v-if="showCancelModal && listingToCancel" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
        @click.self="closeCancelModal"
      >
        <div class="bg-white rounded-2xl w-full max-w-sm overflow-hidden shadow-2xl relative">
          <div class="p-6 text-center">
            <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <XMarkIcon class="w-8 h-8 text-red-600" />
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-2">İlanı Kaldır</h3>
            <p class="text-slate-500 mb-6">
              Bu ilanı kaldırmak istediğinize emin misiniz? Ürünler envanterinize iade edilecektir.
            </p>
            
            <div class="flex gap-3">
              <button 
                @click="closeCancelModal" 
                class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
              >
                Vazgeç
              </button>
              <button 
                @click="confirmCancel"
                :disabled="isCancelling"
                class="flex-1 py-3 bg-red-600 text-white font-bold rounded-xl hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg shadow-red-600/20"
              >
                <span v-if="isCancelling">Kaldırılıyor...</span>
                <span v-else>Evet, Kaldır</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

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
                  <div class="flex justify-between items-center mb-1">
                    <label class="block text-sm font-medium text-slate-700">Birim Fiyat (VP)</label>
                    <span v-if="estimatedPrice" class="text-xs text-primary-600 font-medium">
                      Piyasa: {{ formatCurrency(estimatedPrice) }}
                    </span>
                  </div>
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
                <span class="font-bold text-emerald-600 flex items-center gap-1">
                  <CurrencyIcon :size="16" variant="success" />
                  {{ formatCurrency((listingForm.price * listingForm.quantity).toFixed(2)) }}
                </span>
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
                  :disabled="!isValidListing || isListing"
                  class="flex-1 py-2.5 bg-primary-600 text-white font-bold rounded-xl hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg shadow-primary-600/20"
                >
                  <span v-if="isListing">Yayınlanıyor...</span>
                  <span v-else>İlanı Yayınla</span>
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
import { useAuthStore } from '../stores/authStore'
import { useMarketStore } from '../stores/marketStore'
import { useToast } from '../composables/useToast'
import { XMarkIcon, ArchiveBoxIcon, ShoppingCartIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import CurrencyIcon from '../components/CurrencyIcon.vue'
import StarRating from '../components/StarRating.vue'
import ProductIcon from '../components/ProductIcon.vue'
import { formatCurrency } from '../utils/currency'
import { getItemUnitTr } from '../utils/translations'

const authStore = useAuthStore()
const marketStore = useMarketStore()
const currentUser = computed(() => authStore.user)

const listings = computed(() => marketStore.listings)
const inventory = ref([])
const showSellModal = ref(false)
const showBuyModal = ref(false)
const loadingInventory = ref(false)
const isListing = ref(false)
const isBuying = ref(false)
const isCancelling = ref(false)
const selectedItem = ref(null)
const selectedListing = ref(null)
const buyQuantity = ref(1)

// Cancel Modal State
const showCancelModal = ref(false)
const listingToCancel = ref(null)

// Pagination & Search linked to store
const currentPage = computed({
  get: () => marketStore.currentPage,
  set: (val) => marketStore.currentPage = val
})
const totalPages = computed(() => marketStore.totalPages)
const searchQuery = ref('')

const listingForm = ref({
  quantity: 1,
  price: 10
})

const { addToast } = useToast()

const isValidListing = computed(() => {
  return selectedItem.value && 
         listingForm.value.quantity > 0 && 
         listingForm.value.quantity <= selectedItem.value.quantity &&
         listingForm.value.price > 0
})

const isValidBuyQuantity = computed(() => {
  return selectedListing.value && 
         buyQuantity.value > 0 && 
         buyQuantity.value <= selectedListing.value.quantity
})

const getQualityColor = (score) => {
  if (score >= 8) return 'bg-emerald-500'
  if (score >= 5) return 'bg-amber-500'
  return 'bg-slate-300'
}

const itemUnitToTr = (unit) => {
  switch (unit) {
    case 'PIECE':
      return 'Adet'
    case 'KG':
      return 'Kg'
    case 'LITER':
      return 'Litre'
    case 'METER':
      return 'Metre'
    default:
      return ''
  }
}

const fetchListings = async () => {
  try {
    await marketStore.loadListings(currentPage.value, searchQuery.value || '')
    // Store handles state updates
  } catch (error) {
    console.error('Error fetching listings:', error)
  }
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    fetchListings()
  }
}

// Debounce search
let searchTimeout = null
const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0 // Reset to first page on search
    fetchListings()
  }, 300)
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
  if (!isValidListing.value || isListing.value) return

  try {
    isListing.value = true
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
    // Error handled globally
  } finally {
    isListing.value = false
  }
}

const openBuyModal = (listing) => {
  selectedListing.value = listing
  buyQuantity.value = 1
  showBuyModal.value = true
}

const closeBuyModal = () => {
  showBuyModal.value = false
  selectedListing.value = null
  buyQuantity.value = 1
}

const confirmBuy = async () => {
  if (!selectedListing.value || isBuying.value) return
  
  try {
    isBuying.value = true
    await MarketService.buyItem(selectedListing.value.id, { quantity: buyQuantity.value })
    
    // Update user balance
    await authStore.fetchUser()
    
    addToast('Ürün başarıyla satın alındı ve envantere eklendi.', 'success')
    fetchListings()
    closeBuyModal()
  } catch (error) {
    // Error handled globally
  } finally {
    isBuying.value = false
  }
}

const handleCancelListing = (listing) => {
  listingToCancel.value = listing
  showCancelModal.value = true
}

const closeCancelModal = () => {
  showCancelModal.value = false
  listingToCancel.value = null
}

const confirmCancel = async () => {
  if (!listingToCancel.value || isCancelling.value) return

  try {
    isCancelling.value = true
    await MarketService.cancelListing(listingToCancel.value.id)
    addToast('İlan başarıyla kaldırıldı ve ürünler envantere eklendi.', 'success')
    closeCancelModal()
  } catch (error) {
    console.error('Cancel failed', error)
    const msg = error.response?.data?.message || 'İlan kaldırılamadı'
    addToast(msg, 'error')
  } finally {
    isCancelling.value = false
  }
}

watch(showSellModal, (newValue) => {
  if (newValue) {
    fetchInventory()
  }
})

watch(selectedItem, async (newItem) => {
  if (newItem) {
    listingForm.value.price = 10 // Reset
    estimatedPrice.value = null
    try {
      const response = await MarketService.getPriceEstimate(newItem.name)
      if (response.data) {
        listingForm.value.price = response.data
        estimatedPrice.value = response.data
      }
    } catch (error) {
      console.error('Price estimate failed', error)
    }
  }
})

onMounted(() => {
  fetchListings()
  // Connect to WebSocket for real-time updates
  marketStore.connect()
})

onUnmounted(() => {
  // Disconnect from WebSocket
  marketStore.disconnect()
})
</script>
