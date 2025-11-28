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

    <div v-else class="space-y-4 sm:space-y-6">
      <!-- Stats Grid - Mobile Responsive -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
        <div class="bg-white rounded-lg p-4 sm:p-5 border-l-4 border-blue-500 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <div class="text-xs sm:text-sm font-medium text-gray-500 mb-1">Seviye</div>
              <div class="text-md sm:text-lg font-bold text-gray-900">{{ convertTierToTr(shop.tier) }}</div>
            </div>
            <div class="w-10 h-10 sm:w-12 sm:h-12 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 sm:w-6 sm:h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg p-4 sm:p-5 border-l-4 border-amber-500 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <div class="text-xs sm:text-sm font-medium text-gray-500 mb-1">Stok Durumu</div>
              <div class="text-2xl sm:text-3xl font-bold" :class="{'text-red-600': !shop.currentStock, 'text-gray-900': shop.currentStock > 0}">
                {{ shop.currentStock || 0 }}<span class="text-base sm:text-lg text-gray-500 font-normal">/{{ shop.maxStock }}</span>
              </div>
            </div>
            <div class="w-10 h-10 sm:w-12 sm:h-12 bg-amber-50 rounded-lg flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 sm:w-6 sm:h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg p-4 sm:p-5 border-l-4 border-green-500 shadow-sm hover:shadow-md transition-shadow sm:col-span-2 lg:col-span-1">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <div class="text-xs sm:text-sm font-medium text-gray-500 mb-1">Son Satış Geliri</div>
              <div class="text-2xl sm:text-3xl font-bold text-gray-900">
                <Currency :amount="shop.lastRevenue || 0" :icon-size="24" />
              </div>
            </div>
            <div class="w-10 h-10 sm:w-12 sm:h-12 bg-green-50 rounded-lg flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 sm:w-6 sm:h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <!-- Description -->
        <div class="p-4 sm:p-6 border-b border-gray-200">
          <p class="text-sm sm:text-base text-gray-700">{{ shop.description }}</p>
        </div>

        <!-- Inventory List -->
        <div class="p-4 sm:p-6">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4 gap-2">
            <h3 class="text-base sm:text-lg font-bold text-gray-900">Dükkan Envanteri</h3>
            <span class="text-xs sm:text-sm text-gray-500">{{ shop.items?.length || 0 }} / {{ shop.level }} Çeşit</span>
          </div>
          
          <div v-if="!shop.items || shop.items.length === 0" class="text-center py-8 text-gray-500 italic text-sm">
            Henüz ürün yok.
          </div>
          <div v-else class="overflow-x-auto -mx-4 sm:mx-0">
            <div class="inline-block min-w-full align-middle">
              <table class="min-w-full text-sm">
                <thead class="bg-gray-50 border-y border-gray-200">
                  <tr>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Ürün</th>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Miktar</th>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm hidden sm:table-cell">Kalite</th>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm hidden md:table-cell">Birim Maliyet</th>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Satış Fiyatı</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 bg-white">
                  <tr v-for="item in shop.items" :key="item.id" class="hover:bg-gray-50 transition-colors">
                    <td class="px-3 sm:px-4 py-2 sm:py-3 font-medium text-gray-900 text-xs sm:text-sm">
                      <div class="flex items-center gap-2">
                        <ProductIcon :name="item.name" size="sm" />
                        {{ item.name }}
                      </div>
                    </td>
                    <td class="px-3 sm:px-4 py-2 sm:py-3 text-gray-600 text-xs sm:text-sm">{{ item.quantity }}</td>
                    <td class="px-3 sm:px-4 py-2 sm:py-3 hidden sm:table-cell">
                      <StarRating :score="item.qualityScore" size="xs" />
                    </td>
                    <td class="px-3 sm:px-4 py-2 sm:py-3 text-gray-600 text-xs sm:text-sm hidden md:table-cell">
                      <Currency :amount="item.cost || 0" :icon-size="14" />
                    </td>
                    <td class="px-3 sm:px-4 py-2 sm:py-3">
                      <div v-if="editingItem === item.id" class="flex items-center gap-2">
                        <input 
                          v-model.number="newPrice" 
                          type="number" 
                          min="0.1" 
                          step="0.1" 
                          class="w-20 sm:w-24 px-2 sm:px-3 py-1 sm:py-1.5 border border-gray-300 rounded-lg text-xs sm:text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                          @keyup.enter="savePrice(item)"
                        >
                        <button @click="savePrice(item)" class="px-2 sm:px-3 py-1 sm:py-1.5 bg-green-500 text-white rounded-lg hover:bg-green-600 text-xs font-medium">Kaydet</button>
                        <button @click="cancelEditPrice" class="px-2 sm:px-3 py-1 sm:py-1.5 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 text-xs font-medium">İptal</button>
                      </div>
                      <div v-else class="flex items-center gap-2">
                        <span class="font-medium text-gray-900 text-xs sm:text-sm">
                          <Currency :amount="item.price" :icon-size="14" />
                        </span>
                        <button 
                          @click="startEditPrice(item)" 
                          class="p-1 sm:p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Fiyatı Düzenle"
                        >
                          <PencilSquareIcon class="w-3 h-3 sm:w-4 sm:h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Actions - Mobile Responsive -->
        <div class="p-4 sm:p-6 bg-gray-50 border-t border-gray-200">
          <div class="flex flex-col sm:flex-row gap-2">
            <button 
              v-if="isSelling == null || !isSelling"
              @click="startSales" 
              :disabled="!hasStock"
              class="flex-1 px-3 sm:px-4 py-2 bg-emerald-600 text-white rounded-lg font-medium hover:bg-emerald-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm"
              :title="!hasStock ? 'Satılacak ürün yok' : 'Satışı Başlat'"
            >
              <span class="flex items-center justify-center gap-1.5">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                </svg>
                Satış Başlat
              </span>
            </button>
            <div v-else class="flex-1 px-3 sm:px-4 py-2 bg-emerald-600 text-white rounded-lg font-medium flex items-center justify-center gap-1.5 text-sm">
              <svg class="w-4 h-4 animate-pulse" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <span class="font-mono">{{ timeLeft }}</span>
            </div>
            
            <button 
              @click="handleUpgrade"
              :disabled="shop.level >= 3"
              class="px-3 sm:px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg font-medium hover:border-gray-400 hover:bg-gray-50 transition-all text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              :title="shop.level >= 3 ? 'Maksimum seviyedesiniz' : 'Yükselt (15,000 VP)'"
            >
              <span class="flex items-center justify-center gap-1.5">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"/>
                </svg>
                <span class="hidden sm:inline">Yükselt</span>
                <span class="sm:hidden">↑</span>
              </span>
            </button>
            
            <RouterLink to="/home/market" class="px-3 sm:px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors text-sm">
              <span class="flex items-center justify-center gap-1.5">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
                </svg>
                <span class="hidden sm:inline">Ürün Al</span>
                <span class="sm:hidden">Al</span>
              </span>
            </RouterLink>
            
            <button 
              @click="handleClose"
              class="px-3 sm:px-4 py-2 bg-white border border-red-300 text-red-600 rounded-lg font-medium hover:bg-red-50 hover:border-red-400 transition-all text-sm"
            >
              <span class="flex items-center justify-center gap-1.5">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
                <span class="hidden sm:inline">Kapat</span>
                <span class="sm:hidden">×</span>
              </span>
            </button>
          </div>
        </div>
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
            <h2 class="text-xl font-bold text-slate-900">Satış Başlat</h2>
            <button @click="showSellModal = false" class="text-slate-400 hover:text-slate-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          
          <div class="p-6 space-y-4">
            <p class="text-slate-600 text-sm">
              Dükkanınızdaki ürünleri satışa çıkarmak üzeresiniz. Satış {{ buildingConfig?.salesDuration || '-' }} dakika sürecektir.
            </p>

            <div class="flex gap-3 pt-4">
              <button 
                @click="showSellModal = false" 
                class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
              >
                İptal
              </button>
              <button 
                @click="confirmStartSales"
                class="flex-1 py-3 bg-primary-600 text-white font-bold rounded-xl hover:bg-primary-700 transition-colors shadow-lg shadow-primary-600/20"
              >
                Başlat
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Upgrade Modal -->
    <Teleport to="body">
      <div 
        v-if="showUpgradeModal" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
        @click.self="showUpgradeModal = false"
      >
        <div class="bg-white rounded-2xl w-full max-w-md overflow-hidden shadow-2xl relative">
          <div class="p-6 border-b border-slate-100 flex justify-between items-center">
            <h2 class="text-xl font-bold text-slate-900">Binayı Yükselt</h2>
            <button @click="showUpgradeModal = false" class="text-slate-400 hover:text-slate-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div class="flex items-center gap-3 mb-2">
                <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                <span class="font-semibold text-blue-900">Yükseltme Bilgileri</span>
              </div>
              <ul class="text-sm text-blue-800 space-y-1 ml-8">
                <li>• Mevcut Seviye: <strong>{{ shop.level }}</strong></li>
                <li>• Yeni Seviye: <strong>{{ shop.level + 1 }}</strong></li>
                <li>• Maliyet: <strong><Currency :amount="15000" :icon-size="14" class-name="inline-flex" /></strong></li>
              </ul>
            </div>

            <p class="text-slate-600 text-sm">
              Binayı yükseltmek üretim kapasitesini ve verimliliğini artıracaktır.
            </p>

            <div class="flex gap-3 pt-4">
              <button 
                @click="showUpgradeModal = false" 
                class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
              >
                İptal
              </button>
              <button 
                @click="confirmUpgrade"
                class="flex-1 py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 transition-colors shadow-lg shadow-blue-600/20"
              >
                Yükselt
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Close Modal -->
    <Teleport to="body">
      <div 
        v-if="showCloseModal" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
        @click.self="showCloseModal = false"
      >
        <div class="bg-white rounded-2xl w-full max-w-md overflow-hidden shadow-2xl relative">
          <div class="p-6 border-b border-slate-100 flex justify-between items-center">
            <h2 class="text-xl font-bold text-slate-900">Binayı Kapat</h2>
            <button @click="showCloseModal = false" class="text-slate-400 hover:text-slate-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="bg-red-50 border border-red-200 rounded-lg p-4">
              <div class="flex items-center gap-3 mb-2">
                <svg class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                </svg>
                <span class="font-semibold text-red-900">Dikkat!</span>
              </div>
              <p class="text-sm text-red-800 ml-8">
                Bu işlem geri alınamaz. Bina kalıcı olarak silinecektir.
              </p>
            </div>

            <div class="bg-emerald-50 border border-emerald-200 rounded-lg p-4">
              <div class="text-sm text-emerald-800">
                <strong>İade Edilecek Tutar:</strong> <Currency :amount="shop.cost - 10000" :icon-size="16" />
              </div>
            </div>

            <p class="text-slate-600 text-sm">
              Binayı kapatmadan önce tüm ürünlerin satıldığından veya kullanıldığından emin olun.
            </p>

            <div class="flex gap-3 pt-4">
              <button 
                @click="showCloseModal = false" 
                class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
              >
                İptal
              </button>
              <button 
                @click="confirmClose"
                class="flex-1 py-3 bg-red-600 text-white font-bold rounded-xl hover:bg-red-700 transition-colors shadow-lg shadow-red-600/20"
              >
                Kapat
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useShopsStore } from '../../stores/shopsStore'
import { useAuthStore } from '../../stores/authStore'
import { useBuildingConfigStore } from '../../stores/buildingConfigStore'
import BuildingService from '../../services/BuildingService'
import InventoryService from '../../services/InventoryService'
import { useToast } from '../../composables/useToast'
import { XMarkIcon, PencilSquareIcon } from '@heroicons/vue/24/outline'
import Currency from '../../components/Currency.vue'
import StarRating from '../../components/StarRating.vue'
import ProductIcon from '../../components/ProductIcon.vue'

const route = useRoute()
const shopsStore = useShopsStore()
const authStore = useAuthStore()
const configStore = useBuildingConfigStore()
const { addToast } = useToast()

const shop = computed(() => shopsStore.items.find(s => s.id === route.params.id))
const buildingConfig = computed(() => shop.value ? configStore.getConfig('SHOP', shop.value.tier) : null)
const timeLeft = ref('')
let timerInterval = null

const isSelling = computed(() => shop.value?.isSelling)
const hasStock = computed(() => (shop.value?.currentStock || 0) > 0)
const showSellModal = ref(false)
const showUpgradeModal = ref(false)
const showCloseModal = ref(false)
const editingItem = ref(null)
const newPrice = ref(0)

const updateTimer = async () => {
  if (!shop.value?.salesEndsAt) {
    timeLeft.value = ''
    return
  }
  
  const end = new Date(shop.value.salesEndsAt).getTime()
  const now = new Date().getTime()
  const diff = end - now
  
  if (diff <= 0) {
    timeLeft.value = 'Tamamlandı'
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    
    // Wait 2 seconds to ensure backend clock is synced/ahead
    setTimeout(async () => {
      try {
        console.log('Triggering auto-complete sale...')
        await BuildingService.completeSale(shop.value.id)
        addToast('Satış tamamlandı!', 'success')
        await shopsStore.load()
        await authStore.fetchUser()
      } catch (error) {
        console.error('Auto-complete failed:', error)
        // Fallback to polling if manual trigger fails
        const pollInterval = setInterval(async () => {
          await shopsStore.load()
          if (!shop.value?.isSelling) {
            clearInterval(pollInterval)
            await shopsStore.load()
            await authStore.fetchUser()
          }
        }, 2000)
      }
    }, 2000)
    
    return
  }
  
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  timeLeft.value = `${minutes}:${seconds.toString().padStart(2, '0')}`
}

const startSales = () => {
  if (!hasStock.value) {
    addToast('Satılacak ürün yok! Önce ürün eklemelisiniz.', 'warning')
    return
  }
  showSellModal.value = true
}

const confirmStartSales = async () => {
  try {
    const response = await BuildingService.startSales(shop.value.id)
    addToast('Satış başlatıldı! Müşteriler bekleniyor...', 'success')
    showSellModal.value = false

    //await shopsStore.load()
    
    // Manually set 1 minute timer for testing as requested
    const oneMinuteLater = new Date(new Date().getTime() + 60000).toISOString()
    
    // Update local shop state immediately
    if (shop.value) {
      shop.value.isSelling = true
      shop.value.salesEndsAt = oneMinuteLater
    }
    
    // Update user balance from response
    if (response.data) {
      authStore.updateUser(response.data)
    }
    
    if (!timerInterval) {
      timerInterval = setInterval(updateTimer, 1000)
    }
    updateTimer()
  } catch (error) {
    console.error(error)
    addToast('Satış başlatılamadı', 'error')
  }
}

const startEditPrice = (item) => {
  editingItem.value = item.id
  newPrice.value = item.price
}

const convertTierToTr = (tier) => {
  switch (tier) {
    case 'SMALL':
      return 'KÜÇÜK'
    case 'MEDIUM':
      return 'ORTA'
    case 'LARGE':
      return 'BÜYÜK'
    default:
      return 'Bilinmeyen'
  }
}

const savePrice = async (item) => {
  try {
    // We need an endpoint to update item price. 
    // Assuming InventoryService has updateItem or similar.
    // If not, we might need to create one.
    // For now, let's assume we can update it via a new method we'll add to InventoryService/Controller
    await InventoryService.updateItemPrice(item.id, newPrice.value)
    
    item.price = newPrice.value
    editingItem.value = null
    addToast('Fiyat güncellendi', 'success')
  } catch (error) {
    console.error(error)
    addToast('Fiyat güncellenemedi', 'error')
  }
}

const cancelEditPrice = () => {
  editingItem.value = null
}

const handleUpgrade = async () => {
  if (shop.value.level >= 3) {
    addToast('Bina zaten maksimum seviyede', 'error')
    return
  }

  showUpgradeModal.value = true
}

const confirmUpgrade = async () => {
  showUpgradeModal.value = false
  
  try {
    const response = await BuildingService.upgrade(shop.value.id)
    addToast('Bina başarıyla yükseltildi!', 'success')
    await shopsStore.load()
    
    // Update user balance
    if (response.data) {
      await authStore.fetchUser()
    }
  } catch (error) {
    console.error(error)
    const message = error.response?.data?.message || 'Bina yükseltilemedi'
    addToast(message, 'error')
  }
}

const handleClose = async () => {
  const hasItems = (shop.value?.currentStock || 0) > 0
  
  if (hasItems) {
    addToast('Binada ürün bulunuyor. Önce tüm ürünleri satmalısınız.', 'error')
    return
  }

  showCloseModal.value = true
}

const confirmClose = async () => {
  showCloseModal.value = false
  
  try {
    const response = await BuildingService.closeBuilding(shop.value.id)
    addToast('Bina başarıyla kapatıldı. İade tutarı hesabınıza eklendi.', 'success')
    
    // Update user balance from response
    if (response.data) {
      authStore.fetchUser()
    }
    
    // Redirect to shops list
    await shopsStore.load()
    window.location.href = '/home/shops'
  } catch (error) {
    console.error(error)
    const message = error.response?.data?.message || 'Bina kapatılamadı'
    addToast(message, 'error')
  }
}

onMounted(async () => {
  // Fetch building configs (will use cache if already loaded)
  await configStore.fetchConfigs()
  
  if (!shopsStore.items.length) {
    await shopsStore.load()
  }
  
  timerInterval = setInterval(updateTimer, 1000)
  updateTimer()
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>
