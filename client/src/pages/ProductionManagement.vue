<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl sm:text-3xl font-display font-bold text-slate-900">İşletme Yönetimi</h1>
        <p class="text-slate-500 mt-1">Tüm üretim tesislerinizi tek bir yerden yönetin.</p>
      </div>
      
      <!-- Filters -->
      <div class="flex items-center gap-2 bg-white p-1 rounded-xl border border-slate-200 shadow-sm overflow-x-auto max-w-full no-scrollbar">
        <button 
          v-for="filter in filters" 
          :key="filter.value"
          @click="activeFilter = filter.value"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-all whitespace-nowrap flex-shrink-0"
          :class="activeFilter === filter.value ? 'bg-slate-900 text-white shadow-md' : 'text-slate-600 hover:bg-slate-50'"
        >
          {{ filter.label }}
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="i in 6" :key="i" class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm animate-pulse h-48"></div>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredItems.length === 0" class="bg-white rounded-3xl p-12 text-center border border-slate-200 shadow-sm">
      <div class="w-16 h-16 bg-slate-50 rounded-2xl mx-auto flex items-center justify-center mb-4 text-slate-300">
        <BuildingStorefrontIcon class="w-8 h-8" />
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-2">İşletme Bulunamadı</h3>
      <p class="text-slate-500 max-w-md mx-auto">
        {{ activeFilter === 'ALL' ? 'Henüz hiç işletmeniz yok. Yeni bir yatırım yaparak başlayın.' : 'Seçili filtreye uygun işletme bulunamadı.' }}
      </p>
    </div>

    <!-- Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="item in filteredItems" 
        :key="item.id"
        class="group bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden"
      >
        <!-- Card Header -->
        <div class="p-6 border-b border-slate-100 bg-slate-50/50">
          <div class="flex items-start justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl flex items-center justify-center text-white shadow-lg shadow-slate-200" :class="item.color">
                <component :is="item.icon" class="w-6 h-6" />
              </div>
              <div>
                <h3 class="font-bold text-slate-900 group-hover:text-primary-600 transition-colors">{{ item.name }}</h3>
                <span class="text-xs font-medium px-2 py-0.5 rounded-full border" :class="getTypeBadgeClass(item.type)">
                  {{ getTypeName(item.type) }} • LVL {{ item.tier === 'SMALL' ? 1 : item.tier === 'MEDIUM' ? 2 : 3 }}
                </span>
              </div>
            </div>
            <div class="flex flex-col items-end">
               <span class="text-xs font-bold uppercase tracking-wider mb-1" :class="getStatusColor(item.status)">{{ item.statusText }}</span>
               <span v-if="item.timeLeft" class="text-xs font-mono text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded">{{ item.timeLeft }}</span>
            </div>
          </div>
          
          <!-- Progress Bar -->
          <div class="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
            <div 
              class="h-full rounded-full transition-all duration-1000 ease-linear relative overflow-hidden"
              :class="item.color"
              :style="{ width: item.progress + '%' }"
            >
              <div class="absolute inset-0 bg-white/20 animate-[shimmer_2s_infinite] w-full h-full" v-if="item.isWorking"></div>
            </div>
          </div>
        </div>

        <!-- Card Body -->
        <div class="p-6">
          <div class="grid grid-cols-2 gap-4 mb-6">
            <div class="bg-slate-50 rounded-xl p-3 border border-slate-100">
              <div class="text-xs text-slate-500 font-medium mb-1">Kapasite</div>
              <div class="font-bold text-slate-800">{{ item.currentStock || 0 }} / {{ item.maxStock || '-' }}</div>
            </div>
            <div class="bg-slate-50 rounded-xl p-3 border border-slate-100">
              <div class="text-xs text-slate-500 font-medium mb-1">Çeşit</div>
              <div class="font-bold text-slate-800 flex items-baseline gap-1">
                {{ item.items?.length || 0 }} <span class="text-xs text-slate-500 font-normal">Ürün</span>
              </div>
            </div>
          </div>

          <button 
            @click="navigateToDetail(item)"
            class="w-full py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2"
            :class="item.isWorking ? 'bg-slate-900 text-white hover:bg-slate-800 shadow-lg shadow-slate-900/20' : 'bg-white border-2 border-slate-200 text-slate-600 hover:border-primary-200 hover:text-primary-700 hover:bg-primary-50'"
          >
            {{ item.isWorking ? 'Detayları Gör' : 'Yönet' }}
            <ArrowRightIcon class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  BuildingStorefrontIcon, 
  WrenchScrewdriverIcon, 
  TruckIcon,
  ArchiveBoxIcon,
  Cog6ToothIcon,
  ArrowRightIcon
} from '@heroicons/vue/24/outline'
import CurrencyIcon from '../components/CurrencyIcon.vue'
import { useFarmsStore } from '../stores/farmsStore'
import { useGardensStore } from '../stores/gardensStore'
import { useFactoriesStore } from '../stores/factoriesStore'
import { useMinesStore } from '../stores/minesStore'
import { useShopsStore } from '../stores/shopsStore'
import { useBuildingConfigStore } from '../stores/buildingConfigStore'

const router = useRouter()
const farmsStore = useFarmsStore()
const gardensStore = useGardensStore()
const factoriesStore = useFactoriesStore()
const minesStore = useMinesStore()
const shopsStore = useShopsStore()
const configStore = useBuildingConfigStore()

const loading = ref(true)
const activeFilter = ref('ALL')
const filters = [
  { label: 'Tümü', value: 'ALL' },
  { label: 'Üretimde', value: 'PRODUCING' },
  { label: 'Satışta', value: 'SELLING' },
  { label: 'Beklemede', value: 'IDLE' },
]

// Timer for updating progress
let updateInterval = null
const now = ref(Date.now())

onMounted(async () => {
  try {
    await Promise.all([
      farmsStore.load(),
      gardensStore.load(),
      factoriesStore.load(),
      minesStore.load(),
      shopsStore.load(),
      configStore.fetchConfigs()
    ])
    loading.value = false
    
    updateInterval = setInterval(() => {
      now.value = Date.now()
    }, 1000)
  } catch (error) {
    console.error('Failed to load data', error)
    loading.value = false
  }
})

onUnmounted(() => {
  if (updateInterval) clearInterval(updateInterval)
})

const allItems = computed(() => {
  const items = [
    ...farmsStore.items.map(i => ({ ...i, type: 'FARM', icon: TruckIcon, color: 'bg-emerald-500' })),
    ...gardensStore.items.map(i => ({ ...i, type: 'GARDEN', icon: ArchiveBoxIcon, color: 'bg-green-500' })),
    ...factoriesStore.items.map(i => ({ ...i, type: 'FACTORY', icon: Cog6ToothIcon, color: 'bg-blue-500' })),
    ...minesStore.items.map(i => ({ ...i, type: 'MINE', icon: WrenchScrewdriverIcon, color: 'bg-amber-600' })),
    ...shopsStore.items.map(i => ({ ...i, type: 'SHOP', icon: BuildingStorefrontIcon, color: 'bg-purple-500' }))
  ]

  return items.map(processItem)
})

const filteredItems = computed(() => {
  if (activeFilter.value === 'ALL') return allItems.value
  if (activeFilter.value === 'PRODUCING') return allItems.value.filter(i => i.status === 'PRODUCING')
  if (activeFilter.value === 'SELLING') return allItems.value.filter(i => i.status === 'SELLING')
  if (activeFilter.value === 'IDLE') return allItems.value.filter(i => i.status === 'IDLE')
  return allItems.value
})

function processItem(item) {
  let statusText = 'Beklemede'
  let status = 'IDLE'
  let progress = 0
  let timeLeft = ''
  let isWorking = false // Helper for animation

  if (item.type === 'SHOP') {
      if (item.isSelling && item.salesEndsAt) {
          const config = configStore.getConfig(item.type, item.tier)
          const total = config?.salesDuration ? config.salesDuration * 60 * 1000 : 0
          
          if (total > 0) {
              const end = new Date(item.salesEndsAt).getTime()
              const diff = end - now.value
              
              if (diff <= 0) {
                  statusText = 'Tamamlandı'
                  status = 'COMPLETED'
                  progress = 100
                  timeLeft = '00:00'
              } else {
                  const p = Math.max(0, Math.min(1, 1 - (diff / total)))
                  progress = p * 100
                  statusText = 'Satışta'
                  status = 'SELLING'
                  isWorking = true
                  
                  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
                  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
                  timeLeft = `${minutes}:${String(seconds).padStart(2, '0')}`
              }
          } else {
              statusText = 'Satışta'
              status = 'SELLING'
              progress = 50
              isWorking = true
          }
      } else {
          statusText = 'Beklemede'
          status = 'IDLE'
          progress = 0
      }
  } else if (item.isProducing && item.productionEndsAt) {
    const config = configStore.getConfig(item.type, item.tier)
    const total = config?.productionDuration ? config.productionDuration * 60 * 1000 : 0
    
    if (total > 0) {
        const end = new Date(item.productionEndsAt).getTime()
        const diff = end - now.value
        
        if (diff <= 0) {
            statusText = 'Tamamlandı'
            status = 'COMPLETED'
            progress = 100
            timeLeft = '00:00'
        } else {
            const p = Math.max(0, Math.min(1, 1 - (diff / total)))
            progress = p * 100
            statusText = 'Üretiliyor'
            status = 'PRODUCING'
            isWorking = true
            
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
            const seconds = Math.floor((diff % (1000 * 60)) / 1000)
            timeLeft = `${minutes}:${String(seconds).padStart(2, '0')}`
        }
    } else {
        statusText = 'Üretiliyor'
        status = 'PRODUCING'
        progress = 50
        isWorking = true
    }
  }

  // Get config for this item to enrich data
  const config = configStore.getConfig(item.type, item.tier)
  
  // Enrich with config data if missing on item
  const maxStock = item.maxStock || config?.maxStock || 0
  const revenue = item.revenue || config?.incomePerMinute || config?.revenue || 0
  
  // Calculate current stock (sum of quantities)
  const currentStock = item.items ? item.items.reduce((total, i) => total + i.quantity, 0) : 0

  return {
    ...item,
    name: item.name || getTypeName(item.type),
    statusText,
    status,
    progress,
    timeLeft,
    isWorking,
    maxStock,
    revenue,
    currentStock
  }
}

function getTypeName(type) {
    switch(type) {
        case 'FARM': return 'Çiftlik'
        case 'GARDEN': return 'Bahçe'
        case 'FACTORY': return 'Fabrika'
        case 'MINE': return 'Maden'
        case 'SHOP': return 'Dükkan'
        default: return 'İşletme'
    }
}

function getTypeBadgeClass(type) {
    switch(type) {
        case 'FARM': return 'bg-emerald-50 text-emerald-700 border-emerald-100'
        case 'GARDEN': return 'bg-green-50 text-green-700 border-green-100'
        case 'FACTORY': return 'bg-blue-50 text-blue-700 border-blue-100'
        case 'MINE': return 'bg-amber-50 text-amber-700 border-amber-100'
        case 'SHOP': return 'bg-purple-50 text-purple-700 border-purple-100'
        default: return 'bg-slate-50 text-slate-700 border-slate-100'
    }
}

function getStatusColor(status) {
  switch (status) {
    case 'SELLING':
    case 'PRODUCING':
      return 'text-emerald-600 bg-emerald-50 border-emerald-100'
    case 'COMPLETED':
      return 'text-blue-600 bg-blue-50 border-blue-100'
    default:
      return 'text-slate-500 bg-slate-50 border-slate-100'
  }
}

function navigateToDetail(item) {
    let routeName = ''
    switch(item.type) {
        case 'FARM': routeName = 'FarmDetail'; break;
        case 'GARDEN': routeName = 'GardenDetail'; break;
        case 'FACTORY': routeName = 'FactoryDetail'; break;
        case 'MINE': routeName = 'MineDetail'; break;
        case 'SHOP': routeName = 'ShopDetail'; break;
    }
    
    if (routeName) {
        router.push({ name: routeName, params: { id: item.id } })
    }
}
</script>
```
