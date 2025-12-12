<template>
  <div>
    <PageHeader 
      title="Envanter" 
      description="Merkezi envanterinizdeki ürünleri görüntüleyin ve işletmelerinize transfer edin."
    />

    <!-- Loading State -->
    <div v-if="loading" class="bg-white rounded-xl p-8 text-center border border-slate-200">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
      <p class="mt-4 text-slate-600">Envanter yükleniyor...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="items.length === 0" class="bg-white rounded-xl border border-slate-200 p-12 text-center">
      <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
        </svg>
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-2">Envanteriniz Boş</h3>
      <p class="text-slate-500 max-w-md mx-auto">Pazardan ürün satın aldığınızda veya işletmenizden ürün transfer ettiğinizde buraya gelecektir.</p>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Mobile Card Layout -->
      <div class="md:hidden space-y-3">
        <div 
          v-for="item in items" 
          :key="'mobile-' + item.id"
          class="bg-white rounded-xl border border-slate-200 p-4 shadow-sm"
        >
          <div class="flex items-start gap-3">
            <ProductIcon :name="item.name.trim()" size="md" class="flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <h4 class="font-semibold text-slate-900 truncate">{{ item.name }}</h4>
              <div class="flex items-center gap-3 mt-1">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">
                  {{ item.quantity }} {{ getItemUnitTr(item.unit) }}
                </span>
                <StarRating :score="item.qualityScore || 0" size="xs" />
              </div>
            </div>
            <div class="text-right">
              <Currency :amount="item.cost || item.price || 0" :icon-size="14" class-name="justify-end text-sm" />
            </div>
          </div>
          
          <div class="flex gap-2 mt-3 pt-3 border-t border-slate-100">
            <button
              @click="openSellModal(item)"
              class="flex-1 py-2.5 bg-emerald-600 text-white rounded-lg font-semibold hover:bg-emerald-700 transition-colors text-sm touch-target"
            >
              Pazara Koy
            </button>
            <button
              @click="openTransferModal(item)"
              class="flex-1 py-2.5 bg-primary-600 text-white rounded-lg font-semibold hover:bg-primary-700 transition-colors text-sm touch-target"
            >
              Transfer
            </button>
          </div>
        </div>
      </div>

      <!-- Desktop Table Layout -->
      <div class="hidden md:block bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500 font-semibold">
                <th class="px-6 py-3 whitespace-nowrap">Ürün</th>
                <th class="px-6 py-3 text-center whitespace-nowrap">Miktar</th>
                <th class="px-6 py-3 text-center whitespace-nowrap">Kalite</th>
                <th class="px-6 py-3 text-center whitespace-nowrap">Maliyet</th>
                <th class="px-6 py-3 text-right whitespace-nowrap">İşlemler</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-200">
              <tr 
                v-for="item in items" 
                :key="'desktop-' + item.id"
                class="hover:bg-slate-50 transition-colors"
              >
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center gap-3">
                    <ProductIcon :name="item.name.trim()" size="md" class="flex-shrink-0" />
                    <div>
                      <h4 class="font-semibold text-slate-900">{{ item.name }}</h4>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 text-center whitespace-nowrap">
                  <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold bg-blue-100 text-blue-800">
                    {{ item.quantity }} {{ getItemUnitTr(item.unit) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-center whitespace-nowrap">
                  <div class="flex flex-col items-center gap-1">
                    <StarRating :score="item.qualityScore || 0" size="xs" />
                  </div>
                </td>
                <td class="px-6 py-4 text-center whitespace-nowrap">
                  <Currency :amount="item.cost || item.price || 0" :icon-size="16" class-name="justify-center" />
                </td>
                <td class="px-6 py-4 text-right whitespace-nowrap">
                  <button
                    @click="openSellModal(item)"
                    class="px-4 py-2 bg-emerald-600 text-white rounded-lg font-semibold hover:bg-emerald-700 transition-colors text-sm mr-2"
                  >
                    Pazara Koy
                  </button>
                  <button
                    @click="openTransferModal(item)"
                    class="px-4 py-2 bg-primary-600 text-white rounded-lg font-semibold hover:bg-primary-700 transition-colors text-sm"
                  >
                    Transfer
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Transfer Modal -->
    <Teleport to="body">
      <div 
        v-if="showTransferModal" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
        @click.self="showTransferModal = false"
      >
        <div class="bg-white rounded-2xl w-full max-w-md overflow-hidden shadow-2xl relative">
          <div class="p-6 border-b border-slate-100 flex justify-between items-center">
            <h2 class="text-xl font-bold text-slate-900">İşletmeye Transfer</h2>
            <button @click="showTransferModal = false" class="text-slate-400 hover:text-slate-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>
          
          <div class="p-6 space-y-4">
            <div v-if="selectedItem">
              <p class="text-sm text-slate-600 mb-4">
                <span class="font-semibold">{{ selectedItem.name }}</span> ürününü hangi işletmenize transfer etmek istiyorsunuz?
              </p>

              <!-- Building Selection -->
              <div class="mb-4">
                <SelectBox
                  v-model="transferBuildingId"
                  :options="buildingOptions"
                  label="İşletme Seçin"
                  placeholder="İşletme Seçiniz..."
                />
              </div>

              <!-- Quantity -->
              <div class="mb-4">
                <label class="block text-sm font-medium text-slate-700 mb-2">Miktar</label>
                <input 
                  v-model.number="transferQuantity"
                  type="number"
                  :max="selectedItem.quantity"
                  min="1"
                  class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  placeholder="Miktar girin"
                />
                <p class="text-xs text-slate-500 mt-1">Maksimum: {{ selectedItem.quantity }}</p>
              </div>
            </div>

            <div class="flex gap-3 pt-4">
              <button 
                @click="showTransferModal = false" 
                class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
              >
                İptal
              </button>
              <button 
                @click="confirmTransfer"
                :disabled="!transferBuildingId || !transferQuantity || transferring"
                class="flex-1 py-3 bg-primary-600 text-white font-bold rounded-xl hover:bg-primary-700 transition-colors shadow-lg shadow-primary-600/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ transferring ? 'Transfer Ediliyor...' : 'Transfer Et' }}
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
            <div v-if="selectedItem">
              <div class="flex items-center gap-4 bg-slate-50 p-4 rounded-xl border border-slate-100 mb-4">
                <ProductIcon :name="selectedItem.name" size="md" />
                <div>
                  <h3 class="font-bold text-slate-900">{{ selectedItem.name }}</h3>
                  <div class="text-sm text-slate-500">Mevcut: {{ selectedItem.quantity }} {{ getItemUnitTr(selectedItem.unit) }}</div>
                </div>
              </div>

              <form @submit.prevent="handleCreateListing" class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
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
                    <label class="block text-sm font-medium text-slate-700 mb-1">Birim Fiyat (VP)</label>
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
                <div v-if="listingForm.price && listingForm.quantity" class="bg-slate-50 p-3 rounded-lg text-sm flex justify-between items-center">
                  <span class="text-slate-600">Toplam Kazanç:</span>
                  <span class="font-bold text-emerald-600 flex items-center gap-1">
                    <CurrencyIcon :size="16" variant="success" />
                    {{ (listingForm.price * listingForm.quantity).toFixed(2) }}
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
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useInventoryStore } from '../stores/inventoryStore'
import { useShopsStore } from '../stores/shopsStore'
import { useFarmsStore } from '../stores/farmsStore'
import { useFactoriesStore } from '../stores/factoriesStore'
import { useMinesStore } from '../stores/minesStore'
import { useGardensStore } from '../stores/gardensStore'
import { useGameDataStore } from '../stores/gameDataStore'
import { useToast } from '../composables/useToast'
import PageHeader from '../components/PageHeader.vue'
import Currency from '../components/Currency.vue'
import StarRating from '../components/StarRating.vue'
import ProductIcon from '../components/ProductIcon.vue'
import SelectBox from '../components/SelectBox.vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { getBuildingTypeTr, getBuildingSubTypeTr, getItemUnitTr } from '../utils/translations'

const inventoryStore = useInventoryStore()
const shopsStore = useShopsStore()
const farmsStore = useFarmsStore()
const factoriesStore = useFactoriesStore()
const minesStore = useMinesStore()
const gardensStore = useGardensStore()
const { addToast } = useToast()

const loading = ref(true)
const items = ref([])
const showTransferModal = ref(false)
const selectedItem = ref(null)
const transferBuildingId = ref('')
const transferQuantity = ref(1)
const transferring = ref(false)

// Get all buildings from all stores
const availableBuildings = computed(() => {
  const buildings = [
    ...shopsStore.items.map(b => ({ ...b, type: 'SHOP' })),
    ...farmsStore.items.map(b => ({ ...b, type: 'FARM' })),
    ...factoriesStore.items.map(b => ({ ...b, type: 'FACTORY' })),
    ...minesStore.items.map(b => ({ ...b, type: 'MINE' })),
    ...gardensStore.items.map(b => ({ ...b, type: 'GARDEN' }))
  ]
  return buildings
})

const gameDataStore = useGameDataStore()

const filteredBuildings = computed(() => {
  if (!selectedItem.value) return []
  
  const itemName = selectedItem.value.name
  console.log('Filtering for item:', itemName)
  
  return availableBuildings.value.filter(building => {
    // Find definition for this building type/subtype
    // GameDataService maps subType name (e.g. "MARKET", "FACTORY") to ID
    // Some buildings might not have subType set (null), so we fallback to type (e.g. "FARM", "GARDEN")
    // which matches the BuildingSubType enum names for those types.
    const definitionId = building.subType || building.type
    const definition = gameDataStore.items.find(d => d.id === definitionId)
    
    if (!definition) {
      console.warn('Definition not found for building:', building.name, 'ID:', definitionId)
      return false
    }

    console.log(`Building: ${building.name}, Type: ${building.type}, SubType: ${building.subType}`)
    console.log('Definition allowed items:', definition.allowedItems)

    if (building.type === 'SHOP') {
      const allowed = definition.allowedItems?.some(i => i.trim() === itemName.trim())
      console.log(`Is allowed in ${building.name}?`, allowed)
      return allowed
    }
    
    if (building.type === 'FACTORY') {
      const allowed = definition.rawMaterials?.some(i => i.trim() === itemName.trim())
      console.log(`Is raw material for ${building.name}?`, allowed)
      // Also check produced items (if we want to allow sending back produced items)
      // But usually factories take raw materials. 
      // User said: "envanterden herhangi bir building e transfer yapabilelim fakat yine dükkandaki marketableProduct kontrolü yaptığımız gibi burda da producedItemNames kontrolü yapmalıyız"
      // This implies for non-shops we check producedItemNames.
      // But for Factory, it consumes raw materials. 
      // If the user wants to transfer *produced* items back to factory, we should check producedItemNames.
      // If the user wants to transfer *raw materials* to factory, we check rawMaterials.
      // Let's allow BOTH.
      const isProduced = definition.producedItemNames?.some(i => i.trim() === itemName.trim())
      return allowed || isProduced
    }

    // For other buildings (FARM, MINE, GARDEN)
    const isProduced = definition.producedItemNames?.some(i => i.trim() === itemName.trim())
    console.log(`Is produced by ${building.name}?`, isProduced)
    return isProduced
    
    return false
  })
})

const buildingOptions = computed(() => {
  return filteredBuildings.value.map(b => ({
    id: b.id,
    name: `${b.name || b.subType} (${getBuildingSubTypeTr(b.subType) || getBuildingTypeTr(b.type)})`
  }))
})

async function load() {
  loading.value = true
  try {
    // Load game data first to get definitions
    await gameDataStore.fetchGameData()

    // Load centralized inventory
    await inventoryStore.loadCentralized()
    items.value = inventoryStore.centralizedItems

    // Load all buildings
    await Promise.all([
      shopsStore.load(),
      farmsStore.load(),
      factoriesStore.load(),
      minesStore.load(),
      gardensStore.load()
    ])
  } catch (error) {
    addToast('Envanter yüklenemedi: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    loading.value = false
  }
}

function openTransferModal(item) {
  selectedItem.value = item
  transferBuildingId.value = ''
  transferQuantity.value = Math.min(item.quantity, 1)
  showTransferModal.value = true
}



async function confirmTransfer() {
  if (!transferBuildingId.value || !transferQuantity.value) {
    addToast('Lütfen tüm alanları doldurun', 'warning')
    return
  }

  if (transferQuantity.value > selectedItem.value.quantity) {
    addToast('Yetersiz miktar', 'error')
    return
  }

  transferring.value = true
  try {
    await inventoryStore.transferToBuilding(
      selectedItem.value.id,
      transferBuildingId.value,
      transferQuantity.value
    )
    addToast('Ürün başarıyla transfer edildi!', 'success')
    showTransferModal.value = false
    await load() // Reload inventory
  } catch (error) {
    // Error handled globally
  } finally {
    transferring.value = false
  }
}

import MarketService from '../services/MarketService'
import CurrencyIcon from '../components/CurrencyIcon.vue'

const showSellModal = ref(false)
const listingForm = ref({ quantity: 1, price: 10 })
const isListing = ref(false)

const isValidListing = computed(() => {
  return selectedItem.value && 
         listingForm.value.quantity > 0 && 
         listingForm.value.quantity <= selectedItem.value.quantity &&
         listingForm.value.price > 0
})

function openSellModal(item) {
  selectedItem.value = item
  listingForm.value = { quantity: 1, price: item.price || 10 }
  showSellModal.value = true
}

async function handleCreateListing() {
  if (!isValidListing.value || isListing.value) return

  try {
    isListing.value = true
    await MarketService.listItem(selectedItem.value.id, {
      itemId: selectedItem.value.id,
      quantity: listingForm.value.quantity,
      price: listingForm.value.price
    })
    
    addToast('İlan başarıyla oluşturuldu!', 'success')
    showSellModal.value = false
    await load() // Reload inventory to update quantities
  } catch (error) {
    // Error handled globally
  } finally {
    isListing.value = false
  }
}

onMounted(load)
</script>
