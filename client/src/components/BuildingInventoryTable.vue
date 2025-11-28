<template>
  <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500 font-semibold">
            <th class="px-3 sm:px-4 py-2 sm:py-3 whitespace-nowrap">Ürün</th>
            <th class="px-3 sm:px-4 py-2 sm:py-3 text-center whitespace-nowrap">Miktar</th>
            <th class="px-3 sm:px-4 py-2 sm:py-3 text-center whitespace-nowrap">Kalite</th>
            <th v-if="showPrice" class="px-3 sm:px-4 py-2 sm:py-3 text-left font-semibold text-gray-700 text-xs sm:text-sm">Satış Fiyatı</th>
            <th class="px-3 sm:px-4 py-2 sm:py-3 text-right whitespace-nowrap">İşlemler</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading" class="animate-pulse">
            <td :colspan="showPrice ? 5 : 4" class="px-4 py-8 text-center text-gray-500">Yükleniyor...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td :colspan="showPrice ? 5 : 4" class="px-4 py-8 text-center text-gray-500">Bu işletmede henüz ürün bulunmuyor.</td>
          </tr>
          <tr v-else v-for="item in items" :key="item.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-3 sm:px-4 py-2 sm:py-3 font-medium text-gray-900 text-xs sm:text-sm">
              <div class="flex items-center gap-2">
                <ProductIcon :name="item.subType ? gameDataStore.getItem(item.subType)?.name : item.name" size="sm" />
                {{ item.subType ? gameDataStore.getItem(item.subType)?.name : item.name }}
              </div>
            </td>
            <td class="px-3 sm:px-4 py-2 sm:py-3 text-center text-gray-600 text-xs sm:text-sm">{{ item.quantity }}</td>
            <td class="px-3 sm:px-4 py-2 sm:py-3 text-center">
              <div class="flex justify-center">
                <StarRating :score="item.qualityScore" size="xs" />
              </div>
            </td>
            <td v-if="showPrice" class="px-3 sm:px-4 py-2 sm:py-3">
              <div v-if="editingItem === item.id" class="flex items-center gap-2">
                <input 
                  :value="newPrice"
                  @input="$emit('update:newPrice', Number($event.target.value))"
                  type="number" 
                  min="0.1" 
                  step="0.1" 
                  class="w-20 sm:w-24 px-2 sm:px-3 py-1 sm:py-1.5 border border-gray-300 rounded-lg text-xs sm:text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  @keyup.enter="$emit('save-price', item)"
                >
                <button @click="$emit('save-price', item)" class="px-2 sm:px-3 py-1 sm:py-1.5 bg-green-500 text-white rounded-lg hover:bg-green-600 text-xs font-medium">Kaydet</button>
                <button @click="$emit('cancel-edit-price')" class="px-2 sm:px-3 py-1 sm:py-1.5 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 text-xs font-medium">İptal</button>
              </div>
              <div v-else class="flex items-center gap-2">
                <span class="font-medium text-gray-900 text-xs sm:text-sm">
                  <Currency :amount="item.price" :icon-size="14" />
                </span>
                <button 
                  @click="$emit('start-edit-price', item)" 
                  class="p-1 sm:p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                  title="Fiyatı Düzenle"
                >
                  <PencilSquareIcon class="w-3 h-3 sm:w-4 sm:h-4" />
                </button>
              </div>
            </td>
            <td class="px-3 sm:px-4 py-2 sm:py-3 text-right">
              <button 
                @click="$emit('transfer', item)"
                class="text-primary-600 hover:text-primary-700 font-medium text-xs sm:text-sm"
              >
                Envantere Aktar
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { useGameDataStore } from '../stores/gameDataStore'
import StarRating from './StarRating.vue'
import ProductIcon from './ProductIcon.vue'
import Currency from './Currency.vue'
import { PencilSquareIcon } from '@heroicons/vue/24/outline'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showPrice: {
    type: Boolean,
    default: false
  },
  editingItem: {
    type: String,
    default: null
  },
  newPrice: {
    type: Number,
    default: 0
  }
})

defineEmits(['transfer', 'start-edit-price', 'save-price', 'cancel-edit-price', 'update:newPrice'])

const gameDataStore = useGameDataStore()
</script>
