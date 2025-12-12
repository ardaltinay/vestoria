<template>
  <div class="min-h-screen bg-[#f8fafc] font-sans selection:bg-amber-100 selection:text-amber-900 pb-20">
    <!-- Header Section -->
    <div class="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm bg-opacity-80 backdrop-blur-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-6">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
          <div>
            <h1 class="text-xl sm:text-2xl lg:text-3xl font-black text-slate-800 tracking-tight flex items-center gap-3">
              Madenlerim
            </h1>
            <p class="text-sm sm:text-base text-slate-500 font-medium mt-1 hidden sm:block">
              Mevcut madenlerinizi yönetin ve kaynak çıkarımını kontrol edin.
            </p>
          </div>
          
          <button 
            @click="showWizard = true"
            class="w-full sm:w-auto group flex items-center justify-center gap-2 px-4 sm:px-6 py-2.5 sm:py-3 bg-amber-600 text-white rounded-xl font-bold hover:bg-amber-700 transition-all shadow-lg shadow-amber-600/20 active:scale-95 touch-target"
          >
            <div class="w-5 h-5 sm:w-6 sm:h-6 rounded-lg bg-amber-500 flex items-center justify-center group-hover:bg-amber-400 transition-colors">
              <PlusIcon class="w-3.5 h-3.5 sm:w-4 sm:h-4 text-white" />
            </div>
            <span>Yeni Maden Aç</span>
          </button>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Loading State -->
      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="i in 3" :key="i" class="bg-white rounded-3xl p-6 shadow-sm border border-slate-100 animate-pulse h-64"></div>
      </div>

      <!-- Empty State -->
      <div v-else-if="mines.length === 0" class="bg-white rounded-2xl sm:rounded-3xl border border-dashed border-slate-300 p-8 sm:p-16 text-center max-w-2xl mx-auto mt-6 sm:mt-10">
        <div class="w-16 h-16 sm:w-24 sm:h-24 bg-amber-50 rounded-full flex items-center justify-center mx-auto mb-4 sm:mb-6">
          <span class="text-2xl sm:text-4xl">⛏️</span>
        </div>
        <h3 class="text-xl sm:text-2xl font-black text-slate-800 mb-2 sm:mb-3">Henüz Madeniniz Yok</h3>
        <p class="text-sm sm:text-lg text-slate-500 mb-6 sm:mb-8 leading-relaxed">
          Yeraltı zenginliklerine ulaşmak için ilk madeninizi açın.
        </p>
        <button 
          @click="showWizard = true"
          class="w-full sm:w-auto px-6 sm:px-8 py-3 sm:py-4 bg-amber-600 text-white rounded-xl sm:rounded-2xl font-bold text-base sm:text-lg hover:bg-amber-700 transition-all shadow-xl shadow-amber-600/20 hover:-translate-y-1 touch-target"
        >
          Hemen Başla
        </button>
      </div>

      <!-- Content Grid -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          v-for="mine in mines" 
          :key="mine.id" 
          @click="$router.push(`/home/mines/${mine.id}`)"
          class="group cursor-pointer bg-white rounded-3xl shadow-sm border border-slate-200 hover:shadow-xl hover:border-amber-200 hover:-translate-y-1 transition-all duration-300 relative overflow-hidden"
        >
          <!-- Decorative Background -->
          <div class="absolute top-0 right-0 w-32 h-32 bg-amber-50 rounded-bl-full -mr-8 -mt-8 transition-transform group-hover:scale-110 duration-500"></div>
          
          <div class="p-6 relative z-10">
            <!-- Header -->
            <div class="flex items-start justify-between mb-6">
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 bg-gradient-to-br from-amber-100 to-orange-200 rounded-2xl flex items-center justify-center shadow-inner text-2xl group-hover:scale-105 transition-transform duration-300">
                  ⛏️
                </div>
                <div>
                  <h3 class="font-black text-slate-800 text-lg leading-tight group-hover:text-amber-700 transition-colors">{{ mine.name || mine.subType }}</h3>
                  <div class="flex items-center gap-2 mt-1">
                    <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">{{ mine.subType }}</span>
                    <span class="w-1 h-1 rounded-full bg-slate-300"></span>
                    <span class="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-md border border-amber-100">
                      LVL {{ mine.tier === 'SMALL' ? 1 : mine.tier === 'MEDIUM' ? 2 : 3 }}
                    </span>
                  </div>
                </div>
              </div>
              
              <!-- Status Badge -->
              <div class="flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-xs font-bold uppercase tracking-wide"
                :class="mine.isProducing ? 'bg-amber-50 border-amber-200 text-amber-700' : 'bg-slate-50 border-slate-200 text-slate-500'">
                <span class="relative flex h-2 w-2">
                  <span v-if="mine.isProducing" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2 w-2" :class="mine.isProducing ? 'bg-amber-500' : 'bg-slate-400'"></span>
                </span>
                {{ mine.isProducing ? 'ÜRETİMDE' : 'BOŞTA' }}
              </div>
            </div>

            <!-- Stats -->
            <div class="grid grid-cols-2 gap-3 mb-6">
              <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100 group-hover:border-amber-100 transition-colors">
                <div class="text-xs font-bold text-slate-400 uppercase mb-1">Stok</div>
                <div class="font-black text-slate-700 text-lg">
                  {{ mine.currentStock || 0 }}<span class="text-sm text-slate-400 font-medium">/{{ mine.maxStock }}</span>
                </div>
              </div>
              <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100 group-hover:border-amber-100 transition-colors">
                <div class="text-xs font-bold text-slate-400 uppercase mb-1">Çeşit</div>
                <div class="font-black text-slate-700 text-lg">
                  {{ mine.items?.length || 0 }} <span class="text-xs text-slate-400 font-medium">Ürün</span>
                </div>
              </div>
            </div>

            <!-- Progress Bar -->
            <div class="mb-6">
              <div class="flex justify-between text-xs font-bold text-slate-400 mb-1.5">
                <span>Doluluk Oranı</span>
                <span>%{{ Math.round(((mine.currentStock || 0) / mine.maxStock) * 100) }}</span>
              </div>
              <div class="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden border border-slate-100">
                <div class="h-full rounded-full transition-all duration-500"
                     :class="(mine.currentStock || 0) >= mine.maxStock ? 'bg-red-500' : 'bg-amber-500'"
                     :style="{ width: `${Math.min(((mine.currentStock || 0) / mine.maxStock) * 100, 100)}%` }">
                </div>
              </div>
            </div>

            <!-- Action Button -->
            <button class="w-full py-3 bg-white border-2 border-slate-100 text-slate-600 rounded-xl font-bold hover:border-amber-500 hover:text-amber-600 hover:bg-amber-50 transition-all flex items-center justify-center gap-2 group-hover:shadow-md">
              <span>Yönetim Paneli</span>
              <svg class="w-4 h-4 transition-transform group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Wizard -->
    <CreateBuildingWizard 
      v-if="showWizard" 
      building-type="MINE" 
      :base-cost="25000"
      @close="showWizard = false"
      @create="handleCreate"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useMinesStore } from '../stores/minesStore'
import { useAuthStore } from '../stores/authStore'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import { PlusIcon } from '@heroicons/vue/24/outline'

const store = useMinesStore()
const authStore = useAuthStore()
const mines = ref([])
const loading = ref(true)
const showWizard = ref(false)

async function load() {
  loading.value = true
  try {
    await store.load()
    mines.value = store.items
  } finally {
    loading.value = false
  }
}

async function handleCreate(payload) {
  try {
    await store.create(payload)
    showWizard.value = false
    await load()
    // Refresh user balance
    await authStore.fetchUser()
  } catch (error) {
    console.error('Failed to create mine:', error)
  }
}

onMounted(load)
</script>
