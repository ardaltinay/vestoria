<template>
  <Teleport to="body">
    <div 
      v-if="show" 
      class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
      @click.self="$emit('close')"
    >
      <div class="bg-white rounded-2xl w-full max-w-md overflow-hidden shadow-2xl relative">
        <div class="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 class="text-xl font-bold text-slate-900">Envantere Aktar</h2>
          <button @click="$emit('close')" class="text-slate-400 hover:text-slate-600">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
        
        <div class="p-6 space-y-4">
          <div v-if="item">
            <p class="text-sm text-slate-600 mb-4">
              <span class="font-semibold">{{ itemName }}</span> ürününü envanterinize aktarmak istiyor musunuz?
            </p>

            <!-- Quantity -->
            <div class="mb-4">
              <label class="block text-sm font-medium text-slate-700 mb-2">Miktar</label>
              <input 
                :value="quantity"
                @input="$emit('update:quantity', Number($event.target.value))"
                type="number"
                :max="item.quantity"
                min="1"
                class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Miktar girin"
              />
              <p class="text-xs text-slate-500 mt-1">Maksimum: {{ item.quantity }}</p>
            </div>
          </div>

          <div class="flex gap-3 pt-4">
            <button 
              @click="$emit('close')" 
              class="flex-1 py-3 text-slate-600 font-bold hover:bg-slate-50 rounded-xl transition-colors"
            >
              İptal
            </button>
            <button 
              @click="$emit('confirm')"
              :disabled="!quantity || loading"
              class="flex-1 py-3 bg-primary-600 text-white font-bold rounded-xl hover:bg-primary-700 transition-colors shadow-lg shadow-primary-600/20 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ loading ? 'Aktarılıyor...' : 'Aktar' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { useGameDataStore } from '../stores/gameDataStore'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  item: {
    type: Object,
    default: null
  },
  quantity: {
    type: Number,
    default: 1
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['close', 'confirm', 'update:quantity'])

const gameDataStore = useGameDataStore()

const itemName = computed(() => {
  if (!props.item) return ''
  return props.item.subType ? gameDataStore.getItem(props.item.subType)?.name : props.item.name
})
</script>
