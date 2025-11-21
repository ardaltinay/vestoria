<template>
  <div class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <div class="fixed inset-0 bg-gray-900 bg-opacity-75 transition-opacity" aria-hidden="true" @click="$emit('close')"></div>

      <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

      <div class="inline-block align-bottom bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-3xl sm:w-full border border-gray-700">
        
        <!-- Header -->
        <div class="bg-gray-700 px-4 py-3 sm:px-6 flex justify-between items-center">
          <h3 class="text-lg leading-6 font-medium text-white" id="modal-title">
            Yeni {{ buildingTypeName }} Oluştur
          </h3>
          <button @click="$emit('close')" class="text-gray-400 hover:text-white">
            <span class="sr-only">Kapat</span>
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Progress Bar -->
        <div class="relative pt-1 px-6 mt-4">
          <div class="flex mb-2 items-center justify-between">
            <div class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-indigo-200 bg-indigo-900">
              Adım {{ isShop ? step : step - 1 }} / {{ isShop ? 3 : 2 }}
            </div>
          </div>
          <div class="overflow-hidden h-2 mb-4 text-xs flex rounded bg-gray-700">
            <div :style="{ width: (isShop ? (step / 3) : ((step - 1) / 2)) * 100 + '%' }" class="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center bg-indigo-500 transition-all duration-300"></div>
          </div>
        </div>

        <!-- Content -->
        <div class="px-4 pt-5 pb-4 sm:p-6 sm:pb-4 min-h-[300px]">
          
          <!-- Step 1: Select SubType -->
          <div v-if="step === 1">
            <h4 class="text-md font-medium text-gray-300 mb-4">İşletme Türünü Seçin</h4>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div v-for="option in subTypeOptions" :key="option.value" 
                   @click="selectSubType(option.value)"
                   class="cursor-pointer rounded-lg border border-gray-600 p-4 hover:border-indigo-500 hover:bg-gray-700 transition-all"
                   :class="{ 'border-indigo-500 bg-gray-700 ring-2 ring-indigo-500': selectedSubType === option.value }">
                <div class="flex items-center justify-between">
                  <span class="text-lg font-semibold text-white">{{ option.label }}</span>
                  <span v-if="selectedSubType === option.value" class="text-indigo-400">
                    <svg class="h-6 w-6" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>
                  </span>
                </div>
                <p class="text-sm text-gray-400 mt-2">{{ option.description }}</p>
              </div>
            </div>
          </div>

          <!-- Step 2: Select Tier -->
          <div v-if="step === 2">
            <h4 class="text-md font-medium text-gray-300 mb-4">Büyüklük Seçin</h4>
            <div v-if="loading" class="text-center text-white">Yükleniyor...</div>
            <div v-else class="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <!-- Small -->
              <div @click="selectTier('SMALL')" 
                   class="cursor-pointer rounded-lg border border-gray-600 p-4 hover:border-indigo-500 hover:bg-gray-700 transition-all relative overflow-hidden"
                   :class="{ 'border-indigo-500 bg-gray-700 ring-2 ring-indigo-500': selectedTier === 'SMALL' }">
                <div class="absolute top-0 right-0 bg-gray-600 text-xs text-white px-2 py-1 rounded-bl">Başlangıç</div>
                <h5 class="text-lg font-bold text-white mb-2">Küçük</h5>
                <div class="space-y-2 text-sm text-gray-300">
                  <div class="flex justify-between"><span>Maliyet:</span> <span class="text-green-400 font-mono">{{ formatMoney(getConfig('SMALL').cost) }}</span></div>
                  <div class="flex justify-between"><span>Kapasite:</span> <span class="text-white">{{ getCapacityLabel('SMALL') }}</span></div>
                  <div v-if="buildingType === 'SHOP'" class="flex justify-between"><span>Stok:</span> <span class="text-white">{{ getStockLabel('SMALL') }}</span></div>
                  <div v-else class="flex justify-between"><span>Üretim:</span> <span class="text-white">{{ getProductionLabel('SMALL') }} /saat</span></div>
                </div>
              </div>

              <!-- Medium -->
              <div @click="selectTier('MEDIUM')" 
                   class="cursor-pointer rounded-lg border border-gray-600 p-4 hover:border-indigo-500 hover:bg-gray-700 transition-all relative overflow-hidden"
                   :class="{ 'border-indigo-500 bg-gray-700 ring-2 ring-indigo-500': selectedTier === 'MEDIUM' }">
                <div class="absolute top-0 right-0 bg-indigo-600 text-xs text-white px-2 py-1 rounded-bl">Popüler</div>
                <h5 class="text-lg font-bold text-white mb-2">Orta</h5>
                <div class="space-y-2 text-sm text-gray-300">
                  <div class="flex justify-between"><span>Maliyet:</span> <span class="text-green-400 font-mono">{{ formatMoney(getConfig('MEDIUM').cost) }}</span></div>
                  <div class="flex justify-between"><span>Kapasite:</span> <span class="text-white">{{ getCapacityLabel('MEDIUM') }}</span></div>
                  <div v-if="buildingType === 'SHOP'" class="flex justify-between"><span>Stok:</span> <span class="text-white">{{ getStockLabel('MEDIUM') }}</span></div>
                  <div v-else class="flex justify-between"><span>Üretim:</span> <span class="text-white">{{ getProductionLabel('MEDIUM') }} /saat</span></div>
                </div>
              </div>

              <!-- Large -->
              <div @click="selectTier('LARGE')" 
                   class="cursor-pointer rounded-lg border border-gray-600 p-4 hover:border-indigo-500 hover:bg-gray-700 transition-all relative overflow-hidden"
                   :class="{ 'border-indigo-500 bg-gray-700 ring-2 ring-indigo-500': selectedTier === 'LARGE' }">
                <div class="absolute top-0 right-0 bg-yellow-600 text-xs text-white px-2 py-1 rounded-bl">Pro</div>
                <h5 class="text-lg font-bold text-white mb-2">Büyük</h5>
                <div class="space-y-2 text-sm text-gray-300">
                  <div class="flex justify-between"><span>Maliyet:</span> <span class="text-green-400 font-mono">{{ formatMoney(getConfig('LARGE').cost) }}</span></div>
                  <div class="flex justify-between"><span>Kapasite:</span> <span class="text-white">{{ getCapacityLabel('LARGE') }}</span></div>
                  <div v-if="buildingType === 'SHOP'" class="flex justify-between"><span>Stok:</span> <span class="text-white">{{ getStockLabel('LARGE') }}</span></div>
                  <div v-else class="flex justify-between"><span>Üretim:</span> <span class="text-white">{{ getProductionLabel('LARGE') }} /saat</span></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Step 3: Confirmation -->
          <div v-if="step === 3">
            <h4 class="text-md font-medium text-gray-300 mb-4">Özet ve Onay</h4>
            <div class="bg-gray-900 rounded-lg p-6 border border-gray-700">
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="text-gray-400">Tür:</div>
                <div class="text-white font-medium">{{ getSubTypeLabel(selectedSubType) }}</div>
                
                <div class="text-gray-400">Büyüklük:</div>
                <div class="text-white font-medium">{{ getTierLabel(selectedTier) }}</div>
                
                <div class="text-gray-400">Toplam Maliyet:</div>
                <div class="text-green-400 font-mono font-bold text-lg">{{ formatMoney(calculateTotalCost()) }}</div>
                
                <div class="text-gray-400">Kapasite:</div>
                <div class="text-white">{{ getCapacityLabel(selectedTier) }}</div>

                <div class="text-gray-400">{{ buildingType === 'SHOP' ? 'Stok:' : 'Üretim:' }}</div>
                <div class="text-white">
                  {{ buildingType === 'SHOP' ? getStockLabel(selectedTier) : getProductionLabel(selectedTier) + ' /saat' }}
                </div>
              </div>
            </div>
            <p class="mt-4 text-sm text-gray-400 text-center">
              "Oluştur" butonuna tıkladığınızda hesabınızdan belirtilen tutar düşülecektir.
            </p>
          </div>

        </div>

        <!-- Footer -->
        <div class="bg-gray-700 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button v-if="step === 3" @click="confirmCreation" type="button" 
                  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-green-600 text-base font-medium text-white hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 sm:ml-3 sm:w-auto sm:text-sm">
            Oluştur
          </button>
          <button v-if="step < 3" @click="nextStep" type="button" 
                  :disabled="!canProceed"
                  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50 disabled:cursor-not-allowed">
            İleri
          </button>
          <button v-if="step > (isShop ? 1 : 2)" @click="prevStep" type="button" 
                  class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-500 shadow-sm px-4 py-2 bg-gray-600 text-base font-medium text-white hover:bg-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
            Geri
          </button>
          <button v-if="step === (isShop ? 1 : 2)" @click="$emit('close')" type="button" 
                  class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-500 shadow-sm px-4 py-2 bg-gray-600 text-base font-medium text-white hover:bg-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
            İptal
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import BuildingService from '../services/BuildingService'

const props = defineProps({
  buildingType: {
    type: String,
    required: true
  },
  // baseCost prop is no longer the primary source of truth, but kept for fallback
  baseCost: {
    type: Number,
    default: 25000
  }
})

const emit = defineEmits(['close', 'create'])

const isShop = computed(() => props.buildingType === 'SHOP')
const step = ref(isShop.value ? 1 : 2)
const selectedSubType = ref(null)
const selectedTier = ref(null)
const buildingConfigs = ref([])
const loading = ref(true)

const productionTypes = ref([])

onMounted(async () => {
  try {
    const [configRes, typesRes] = await Promise.all([
      BuildingService.getBuildingConfigs(),
      BuildingService.getProductionTypes()
    ])
    buildingConfigs.value = configRes.data
    productionTypes.value = typesRes.data
  } catch (error) {
    console.error('Failed to fetch building data:', error)
  } finally {
    loading.value = false
  }
})

const getConfig = (tier) => {
  return buildingConfigs.value.find(c => c.type === props.buildingType && c.tier === tier) || {}
}

const buildingTypeName = computed(() => {
  switch(props.buildingType) {
    case 'SHOP': return 'Dükkan'
    case 'FARM': return 'Çiftlik'
    case 'FACTORY': return 'Fabrika'
    case 'MINE': return 'Maden'
    case 'GARDEN': return 'Bahçe'
    default: return 'Bina'
  }
})

const subTypeOptions = computed(() => {
  return productionTypes.value.filter(t => t.parentType === props.buildingType)
})

const canProceed = computed(() => {
  if (step.value === 1) return !!selectedSubType.value
  if (step.value === 2) return !!selectedTier.value
  return true
})

const selectSubType = (value) => {
  selectedSubType.value = value
}

const selectTier = (value) => {
  selectedTier.value = value
}

const nextStep = () => {
  if (canProceed.value) step.value++
}

const prevStep = () => {
  if (step.value > (isShop.value ? 1 : 2)) step.value--
}

const calculateTotalCost = () => {
  const config = getConfig(selectedTier.value)
  return config.cost || 0
}

const getSubTypeLabel = (value) => {
  const option = subTypeOptions.value.find(o => o.value === value)
  return option ? option.label : value
}

const getTierLabel = (value) => {
  switch(value) {
    case 'SMALL': return 'Küçük'
    case 'MEDIUM': return 'Orta'
    case 'LARGE': return 'Büyük'
    default: return value
  }
}

const getCapacityLabel = (tier) => {
  const config = getConfig(tier)
  if (!config) return '-'
  return `${config.maxSlots} Slot`
}

const getStockLabel = (tier) => {
  const config = getConfig(tier)
  if (!config) return '-'
  return config.maxStock
}

const getProductionLabel = (tier) => {
  const config = getConfig(tier)
  if (!config) return '-'
  return config.productionRate
}

const formatMoney = (amount) => {
  return new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' }).format(amount)
}

const confirmCreation = () => {
  emit('create', {
    type: props.buildingType,
    subType: selectedSubType.value,
    tier: selectedTier.value,
    cost: calculateTotalCost()
  })
}
</script>
