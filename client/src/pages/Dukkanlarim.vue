<template>
  <div class="min-h-screen bg-[#f8fafc] font-sans selection:bg-orange-100 selection:text-orange-900 pb-20">
    <!-- Header Section -->
    <div class="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm bg-opacity-80 backdrop-blur-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 class="text-2xl sm:text-3xl font-black text-slate-800 tracking-tight flex items-center gap-3">
              Dükkanlarım
            </h1>
            <p class="text-slate-500 font-medium mt-1">
              Mevcut dükkanlarınızı yönetin ve gelir durumlarını kontrol edin.
            </p>
          </div>
          
          <button 
            @click="showWizard = true"
            class="group flex items-center justify-center gap-2 px-6 py-3 bg-orange-600 text-white rounded-xl font-bold hover:bg-orange-700 transition-all shadow-lg shadow-orange-600/20 active:scale-95"
          >
            <div class="w-6 h-6 rounded-lg bg-orange-500 flex items-center justify-center group-hover:bg-orange-400 transition-colors">
              <PlusIcon class="w-4 h-4 text-white" />
            </div>
            <span>Yeni Dükkan Aç</span>
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
      <div v-else-if="shops.length === 0" class="bg-white rounded-3xl border border-dashed border-slate-300 p-16 text-center max-w-2xl mx-auto mt-10">
        <div class="w-24 h-24 bg-orange-50 rounded-full flex items-center justify-center mx-auto mb-6">
          <span class="text-4xl">🏪</span>
        </div>
        <h3 class="text-2xl font-black text-slate-800 mb-3">Henüz Dükkanınız Yok</h3>
        <p class="text-slate-500 text-lg mb-8 leading-relaxed">
          Ticaret hayatına atılmak için ilk dükkanınızı açın. Dükkanlar size düzenli nakit akışı sağlar.
        </p>
        <button 
          @click="showWizard = true"
          class="px-8 py-4 bg-orange-600 text-white rounded-2xl font-bold text-lg hover:bg-orange-700 transition-all shadow-xl shadow-orange-600/20 hover:-translate-y-1"
        >
          Hemen Başla
        </button>
      </div>

      <!-- Content Grid -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          v-for="shop in shops" 
          :key="shop.id" 
          @click="$router.push(`/home/shops/${shop.id}`)"
          class="group cursor-pointer bg-white rounded-3xl shadow-sm border border-slate-200 hover:shadow-xl hover:border-orange-200 hover:-translate-y-1 transition-all duration-300 relative overflow-hidden"
        >
          <!-- Decorative Background -->
          <div class="absolute top-0 right-0 w-32 h-32 bg-orange-50 rounded-bl-full -mr-8 -mt-8 transition-transform group-hover:scale-110 duration-500"></div>
          
          <div class="p-6 relative z-10">
            <!-- Header -->
            <div class="flex items-start justify-between mb-6">
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 bg-gradient-to-br from-orange-100 to-amber-200 rounded-2xl flex items-center justify-center shadow-inner text-2xl group-hover:scale-105 transition-transform duration-300">
                  🏪
                </div>
                <div>
                  <h3 class="font-black text-slate-800 text-lg leading-tight group-hover:text-orange-700 transition-colors">{{ shop.name }}</h3>
                  <div class="flex items-center gap-2 mt-1">
                    <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">{{ convertSubTypeToTr(shop.subType) }}</span>
                    <span class="w-1 h-1 rounded-full bg-slate-300"></span>
                    <span class="text-xs font-bold text-orange-600 bg-orange-50 px-2 py-0.5 rounded-md border border-orange-100">
                      LVL {{ shop.tier === 'SMALL' ? 1 : shop.tier === 'MEDIUM' ? 2 : 3 }}
                    </span>
                  </div>
                </div>
              </div>
              
              <!-- Status Badge -->
              <div class="flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-xs font-bold uppercase tracking-wide"
                :class="shop.isSelling ? 'bg-orange-50 border-orange-200 text-orange-700' : 'bg-slate-50 border-slate-200 text-slate-500'">
                <span class="relative flex h-2 w-2">
                  <span v-if="shop.isSelling" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-orange-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2 w-2" :class="shop.isSelling ? 'bg-orange-500' : 'bg-slate-400'"></span>
                </span>
                {{ shop.isSelling ? 'SATIŞTA' : 'KAPALI' }}
              </div>
            </div>

            <!-- Stats -->
            <div class="grid grid-cols-2 gap-3 mb-6">
              <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100 group-hover:border-orange-100 transition-colors">
                <div class="text-xs font-bold text-slate-400 uppercase mb-1">Son Gelir</div>
                <div class="font-black text-slate-700 text-lg">
                  <Currency :amount="shop.lastRevenue || 0" :icon-size="16" />
                </div>
              </div>
              <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100 group-hover:border-orange-100 transition-colors">
                <div class="text-xs font-bold text-slate-400 uppercase mb-1">Stok</div>
                <div class="font-black text-slate-700 text-lg">
                  {{ shop.currentStock || 0 }}<span class="text-sm text-slate-400 font-medium">/{{ shop.maxStock }}</span>
                </div>
              </div>
            </div>

            <!-- Progress Bar -->
            <div class="mb-6">
              <div class="flex justify-between text-xs font-bold text-slate-400 mb-1.5">
                <span>Doluluk Oranı</span>
                <span>%{{ Math.round(((shop.currentStock || 0) / shop.maxStock) * 100) }}</span>
              </div>
              <div class="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden border border-slate-100">
                <div class="h-full rounded-full transition-all duration-500"
                     :class="(shop.currentStock || 0) >= shop.maxStock ? 'bg-red-500' : 'bg-orange-500'"
                     :style="{ width: `${Math.min(((shop.currentStock || 0) / shop.maxStock) * 100, 100)}%` }">
                </div>
              </div>
            </div>

            <!-- Action Button -->
            <button class="w-full py-3 bg-white border-2 border-slate-100 text-slate-600 rounded-xl font-bold hover:border-orange-500 hover:text-orange-600 hover:bg-orange-50 transition-all flex items-center justify-center gap-2 group-hover:shadow-md">
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
      building-type="SHOP" 
      :base-cost="25000"
      @close="showWizard = false"
      @create="handleCreate"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useShopsStore } from '../stores/shopsStore'
import { useAuthStore } from '../stores/authStore'
import { useRouter } from 'vue-router'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import Currency from '../components/Currency.vue'
import { PlusIcon } from '@heroicons/vue/24/outline'

const store = useShopsStore()
const authStore = useAuthStore()
const router = useRouter()
const shops = ref([])
const loading = ref(true)
const showWizard = ref(false)

async function load() {
  loading.value = true
  try {
    await store.load()
    shops.value = store.items
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
    console.error('Failed to create shop:', error)
  }
}

const convertSubTypeToTr = (subType) => {
  switch (subType) {
    case 'GREENGROCER':
      return 'MANAV'
    case 'CLOTHING':
      return 'GİYİM'
    case 'JEWELER':
      return 'KUYUMCU'
    default:
      return 'MARKET'
  }
}

onMounted(load)
</script>
