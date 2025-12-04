<template>
  <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
    <div class="flex items-center justify-between mb-4">
      <h3 class="font-bold text-slate-800">Üretim Özeti</h3>
      <button @click="router.push('/home/production')" class="text-xs text-primary-600 hover:text-primary-700 font-medium">Yönet</button>
    </div>

    <div class="space-y-4">
      <div v-for="item in items" :key="item.id" class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-lg bg-slate-50 flex items-center justify-center text-slate-400">
          <component :is="item.icon" class="w-5 h-5" />
        </div>
        <div class="flex-1">
          <div class="flex justify-between mb-1">
            <span class="text-sm font-medium text-slate-700">{{ item.name }}</span>
            <span class="text-xs text-slate-500">{{ item.status }}</span>
          </div>
          <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
            <div 
              class="h-full rounded-full transition-all duration-500"
              :class="item.color"
              :style="{ width: item.progress + '%' }"
            ></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  BuildingStorefrontIcon, 
  WrenchScrewdriverIcon, 
  TruckIcon,
  BeakerIcon,
  BoltIcon
} from '@heroicons/vue/24/outline'
import { useBuildingConfigStore } from '../../stores/buildingConfigStore'
import { useFarmsStore } from '../../stores/farmsStore'
import { useGardensStore } from '../../stores/gardensStore'
import { useFactoriesStore } from '../../stores/factoriesStore'
import { useMinesStore } from '../../stores/minesStore'
import { useShopsStore } from '../../stores/shopsStore'

const router = useRouter()
const farmsStore = useFarmsStore()
const gardensStore = useGardensStore()
const factoriesStore = useFactoriesStore()
const minesStore = useMinesStore()
const shopsStore = useShopsStore()
const configStore = useBuildingConfigStore()

onMounted(async () => {
  await Promise.all([
    farmsStore.load(),
    gardensStore.load(),
    factoriesStore.load(),
    minesStore.load(),
    shopsStore.load(),
    configStore.fetchConfigs()
  ])
})

const items = computed(() => {
  const allItems = [
    ...farmsStore.items.map(i => ({ ...i, type: 'FARM', icon: TruckIcon, color: 'bg-emerald-500' })),
    ...gardensStore.items.map(i => ({ ...i, type: 'GARDEN', icon: BeakerIcon, color: 'bg-green-500' })),
    ...factoriesStore.items.map(i => ({ ...i, type: 'FACTORY', icon: WrenchScrewdriverIcon, color: 'bg-blue-500' })),
    ...minesStore.items.map(i => ({ ...i, type: 'MINE', icon: BoltIcon, color: 'bg-amber-600' })),
    ...shopsStore.items.map(i => ({ ...i, type: 'SHOP', icon: BuildingStorefrontIcon, color: 'bg-purple-500' }))
  ]

  // Sort by activity (producing/selling first) then by tier/level
  return allItems
    .sort((a, b) => {
        const aActive = a.isProducing || a.isSelling
        const bActive = b.isProducing || b.isSelling
        return (bActive ? 1 : 0) - (aActive ? 1 : 0)
    })
    .slice(0, 4) // Show top 4
    .map(item => {
      let status = 'Beklemede'
      let progress = 0

      if (item.isProducing && item.productionEndsAt) {
        const config = configStore.getConfig(item.type, item.tier)
        const total = config?.productionDuration ? config.productionDuration * 60 * 1000 : 0
        
        if (total > 0) {
            const end = new Date(item.productionEndsAt).getTime()
            const now = new Date().getTime()
            const diff = end - now
            
            // If diff is negative, it means production finished but not collected
            if (diff <= 0) {
                status = 'Tamamlandı'
                progress = 100
            } else {
                const p = Math.max(0, Math.min(1, 1 - (diff / total)))
                progress = p * 100
                status = 'Üretiliyor...'
            }
        } else {
            // Fallback if config missing but producing
            status = 'Üretiliyor...'
            progress = 50 // Indeterminate
        }
      } else if (item.type === 'SHOP') {
          if (item.isSelling && item.salesEndsAt) {
              const config = configStore.getConfig(item.type, item.tier)
              const total = config?.salesDuration ? config.salesDuration * 60 * 1000 : 0
              
              if (total > 0) {
                  const end = new Date(item.salesEndsAt).getTime()
                  const now = new Date().getTime()
                  const diff = end - now
                  
                  if (diff <= 0) {
                      status = 'Tamamlandı'
                      progress = 100
                  } else {
                      const p = Math.max(0, Math.min(1, 1 - (diff / total)))
                      progress = p * 100
                      status = 'Satışta'
                  }
              } else {
                  status = 'Satışta'
                  progress = 50
              }
          } else {
              status = 'Beklemede'
              progress = 0
          }
      }

      return {
        id: item.id,
        name: item.name || getDefaultName(item.type),
        status,
        progress,
        icon: item.icon,
        color: item.color
      }
    })
})

function getDefaultName(type) {
    switch(type) {
        case 'FARM': return 'Çiftlik'
        case 'GARDEN': return 'Bahçe'
        case 'FACTORY': return 'Fabrika'
        case 'MINE': return 'Maden'
        case 'SHOP': return 'Dükkan'
        default: return 'İşletme'
    }
}
</script>
