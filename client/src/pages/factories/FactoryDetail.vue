<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">{{ item?.name || 'Fabrika bulunamadı' }}</h1>
        <p v-if="item" class="text-sm text-gray-500">Fabrika ayrıntıları</p>
      </div>
      <div>
        <RouterLink to="/home/factories" class="px-3 py-1 text-sm text-gray-600 hover:text-brand-yellow">Geri</RouterLink>
      </div>
    </div>

    <div v-if="!item" class="text-gray-500">Fabrika bulunamadı veya silinmiş.</div>

    <div v-else class="space-y-6">

      <!-- Stats Grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
        <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-4 sm:p-5 border border-blue-200">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs sm:text-sm font-medium text-blue-600 mb-1">Seviye</div>
              <div class="text-2xl sm:text-3xl font-bold text-blue-900">{{ convertTierToTr(item.tier) }}</div>
            </div>
            <div class="w-10 h-10 sm:w-12 sm:h-12 bg-blue-200 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 sm:w-6 sm:h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-4 sm:p-5 border border-purple-200">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs sm:text-sm font-medium text-purple-600 mb-1">Stok Durumu</div>
              <div class="text-2xl sm:text-3xl font-bold" :class="{'text-red-600': !currentStock, 'text-purple-900': currentStock > 0}">
                {{ currentStock }}<span class="text-base sm:text-lg text-purple-700 font-normal">/{{ item.maxStock }}</span>
              </div>
            </div>
            <div class="w-10 h-10 sm:w-12 sm:h-12 bg-purple-200 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 sm:w-6 sm:h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-4 sm:p-5 border border-green-200">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs sm:text-sm font-medium text-green-600 mb-1">Gelir /dak</div>
              <div class="text-2xl sm:text-3xl font-bold text-green-900">
                <Currency :amount="item.revenue" :icon-size="24" />
              </div>
            </div>
            <div class="w-10 h-10 sm:w-12 sm:h-12 bg-green-200 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 sm:w-6 sm:h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <!-- Description -->
        <div class="p-6 border-b border-gray-200">
          <p class="text-gray-700">{{ item.description }}</p>
        </div>

        <!-- Inventory List -->
        <div class="p-4 sm:p-6">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4 gap-2">
            <h3 class="text-base sm:text-lg font-bold text-gray-900">Envanter</h3>
            <span class="text-xs sm:text-sm text-gray-500">{{ item.items?.length || 0 }} Çeşit</span>
          </div>
          
          <div v-if="!item.items || item.items.length === 0" class="text-center py-8 text-gray-500 italic text-sm">
            Henüz ürün yok.
          </div>
          <div v-else class="overflow-x-auto -mx-4 sm:mx-0">
            <div class="inline-block min-w-full align-middle">
              <table class="min-w-full text-sm">
                <thead class="bg-gray-50 border-y border-gray-200">
                  <tr>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Ürün</th>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Miktar</th>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Kalite</th>
                    <th class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Birim Maliyet</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  <tr v-for="prodItem in item.items" :key="prodItem.id" class="hover:bg-gray-50 transition-colors">
                    <td class="px-3 sm:px-4 py-2 sm:py-3 font-medium text-gray-900 text-xs sm:text-sm">{{ prodItem.subType ? gameDataStore.getItem(prodItem.subType)?.name : prodItem.name }}</td>
                    <td class="px-3 sm:px-4 py-2 sm:py-3 text-gray-600 text-xs sm:text-sm">{{ prodItem.quantity }}</td>
                    <td class="px-3 sm:px-4 py-2 sm:py-3">
                      <StarRating :score="prodItem.qualityScore" size="xs" />
                    </td>
                    <td class="px-3 sm:px-4 py-2 sm:py-3 text-gray-600 text-xs sm:text-sm">
                      <Currency :amount="prodItem.price" :icon-size="14" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="p-4 sm:p-6 bg-gray-50 border-t border-gray-200 rounded-b-xl">
          <div class="flex flex-col sm:flex-row gap-2">
            <button 
                v-if="!isProducing"
                @click="showProductionModal = true" 
                class="flex-1 px-3 sm:px-4 py-2 bg-gradient-to-r from-primary-500 to-primary-600 text-white rounded-lg font-semibold hover:from-primary-600 hover:to-primary-700 transition-all shadow-lg shadow-primary-500/30 text-sm"
            >
                <span class="flex items-center justify-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                  </svg>
                  Üretimi Başlat
                </span>
            </button>
            
            <div v-else-if="!canCollect" class="flex-1 px-3 sm:px-4 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg font-bold flex items-center justify-center gap-1.5 shadow-lg shadow-blue-500/30 text-sm">
                <svg class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                <span class="font-mono">{{ timeLeft }}</span>
            </div>
            
            <button 
                v-else
                @click="collectProduction" 
                class="flex-1 px-3 sm:px-4 py-2 bg-gradient-to-r from-green-500 to-green-600 text-white rounded-lg font-semibold hover:from-green-600 hover:to-green-700 transition-all shadow-lg shadow-green-500/30 animate-pulse text-sm"
            >
                <span class="flex items-center justify-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                  Ürünleri Topla
                </span>
            </button>

            <button 
              @click="handleUpgrade"
              :disabled="item.level >= 3"
              class="px-3 sm:px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg font-medium hover:border-gray-400 hover:bg-gray-50 transition-all text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              :title="item.level >= 3 ? 'Maksimum seviyedesiniz' : 'Yükselt (15,000 VP)'"
            >
              <span class="flex items-center justify-center gap-1.5">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"/>
                </svg>
                <span class="hidden sm:inline">Yükselt</span>
                <span class="sm:hidden">↑</span>
              </span>
            </button>

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

    <!-- Production Start Modal -->
    <Teleport to="body">
      <div 
        v-if="showProductionModal" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
        @click.self="showProductionModal = false"
      >
        <div class="bg-white rounded-2xl w-full max-w-md overflow-hidden shadow-2xl relative">
          <div class="p-6 border-b border-slate-100 flex justify-between items-center">
            <h2 class="text-xl font-bold text-slate-900">Üretimi Başlat</h2>
            <button @click="showProductionModal = false" class="text-slate-400 hover:text-slate-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          
          <div class="p-6 space-y-4">
            <SelectBox
              v-model="selectedProduct"
              :options="productionOptions"
              label="Üretilecek Ürün"
              placeholder="Bir ürün seçin..."
              value-key="id"
              label-key="name"
            />
            
            <p class="text-slate-600 text-sm">
              Fabrikanızda üretim başlatmak üzeresiniz. Üretim {{ buildingConfig?.productionDuration || '-' }} dakika sürecektir.
            </p>

            <div class="flex gap-3 pt-4">
              <button 
                @click="showProductionModal = false" 
                class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
              >
                İptal
              </button>
              <button 
                @click="confirmStartProduction"
                :disabled="!selectedProduct"
                class="flex-1 py-3 bg-primary-600 text-white font-bold rounded-xl hover:bg-primary-700 transition-colors shadow-lg shadow-primary-600/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Başlat
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
        </div>
      </div>
    </div>

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
                <li>• Mevcut Seviye: <strong>{{ item.level }}</strong></li>
                <li>• Yeni Seviye: <strong>{{ item.level + 1 }}</strong></li>
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
                <strong>İade Edilecek Tutar:</strong> <Currency :amount="item.cost - 10000" :icon-size="16" />
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
import { useFactoriesStore } from '../../stores/factoriesStore'
import { useAuthStore } from '../../stores/authStore'
import { useGameDataStore } from '../../stores/gameDataStore'
import { useBuildingConfigStore } from '../../stores/buildingConfigStore'
import BuildingService from '../../services/BuildingService'
import { useToast } from '../../composables/useToast'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import Currency from "../../components/Currency.vue"
import SelectBox from "../../components/SelectBox.vue"
import StarRating from "../../components/StarRating.vue"

const route = useRoute()
const { addToast } = useToast()
const store = useFactoriesStore()
const authStore = useAuthStore()
const gameDataStore = useGameDataStore()
const configStore = useBuildingConfigStore()
const item = computed(() => store.items.find(i => i.id === route.params.id))
const buildingConfig = computed(() => item.value ? configStore.getConfig('FACTORY', item.value.tier) : null)

const productionOptions = ref([])
const selectedProduct = ref(null)
const timeLeft = ref('')
const showUpgradeModal = ref(false)
const showCloseModal = ref(false)
const showProductionModal = ref(false)
let timerInterval = null

const isProducing = computed(() => item.value?.isProducing)
const canCollect = computed(() => {
    if (!item.value?.isProducing || !item.value?.productionEndsAt) return false
    return new Date(item.value.productionEndsAt).getTime() <= new Date().getTime()
})

const currentStock = computed(() => {
  if (!item.value?.items) return 0
  return item.value.items.reduce((total, i) => total + i.quantity, 0)
})

const updateTimer = async () => {
  if (!item.value?.productionEndsAt) {
    timeLeft.value = ''
    return
  }
  
  const end = new Date(item.value.productionEndsAt).getTime()
  const now = new Date().getTime()
  const diff = end - now
  
  if (diff <= 0) {
    timeLeft.value = 'Tamamlandı'
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    
    try {
      await BuildingService.completeProduction(item.value.id)
      addToast('Üretim tamamlandı!', 'success')
      await store.load()
    } catch (error) {
      console.error('Auto-complete production failed:', error)
    }
    return
  } else {
    const minutes = Math.floor(diff / 60000)
    const seconds = Math.floor((diff % 60000) / 1000)
    timeLeft.value = `${minutes}:${seconds.toString().padStart(2, '0')}`
  }
}



async function confirmStartProduction() {
  if (!selectedProduct.value) {
    addToast('Lütfen bir ürün seçin', 'error')
    return
  }
  
  showProductionModal.value = false
  try {
    await BuildingService.startProduction(item.value.id, selectedProduct.value)
    addToast('Üretim başladı!', 'success')
    selectedProduct.value = null
    await store.load()
  } catch (error) {
    addToast('Üretim başlatılamadı: ' + (error.response?.data?.message || error.message), 'error')
  }
}

async function collectProduction() {
    try {
        await BuildingService.collect(item.value.id)
        addToast('Ürünler toplandı!', 'success')
        await store.load()
    } catch (error) {
        addToast('Toplama başarısız: ' + (error.response?.data?.message || error.message), 'error')
    }
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

const handleUpgrade = async () => {
  if (item.value.tier === 'LARGE') {
    addToast('Bina zaten maksimum seviyede', 'error')
    return
  }
  showUpgradeModal.value = true
}

const confirmUpgrade = async () => {
  showUpgradeModal.value = false
  try {
    const response = await BuildingService.upgrade(item.value.id)
    addToast('Bina başarıyla yükseltildi!', 'success')
    await store.load()
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
  const hasItems = (item.value?.items?.length || 0) > 0
  if (hasItems) {
    addToast('Binada ürün bulunuyor. Önce tüm ürünleri satmalısınız veya kullanmalısınız.', 'error')
    return
  }
  showCloseModal.value = true
}

const confirmClose = async () => {
  showCloseModal.value = false
  try {
    await BuildingService.closeBuilding(item.value.id)
    addToast('Bina başarıyla kapatıldı. İade tutarı hesabınıza eklendi.', 'success')
    await authStore.fetchUser()
    await store.load()
    window.location.href = '/home/factories'
  } catch (error) {
    console.error(error)
    const message = error.response?.data?.message || 'Bina kapatılamadı'
    addToast(message, 'error')
  }
}


onMounted(async () => {
  await configStore.fetchConfigs()
  
  if (!store.items.length) {
    await store.load()
  }
  
  productionOptions.value = gameDataStore.getItemsByType('FACTORY')
  
  timerInterval = setInterval(updateTimer, 1000)
  updateTimer()
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>
