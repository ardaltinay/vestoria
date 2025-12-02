<template>
  <div class="min-h-screen bg-[#f8fafc] font-sans selection:bg-blue-100 selection:text-blue-900 pb-20">
    <!-- Header Section -->
    <div class="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm bg-opacity-80 backdrop-blur-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 class="text-2xl sm:text-3xl font-black text-slate-800 tracking-tight flex items-center gap-3">
              Fabrikalarım
            </h1>
            <p class="text-slate-500 font-medium mt-1">
              Mevcut fabrikalarınızı yönetin ve üretim hatlarını kontrol edin.
            </p>
          </div>
          
          <button 
            @click="showWizard = true"
            class="group flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition-all shadow-lg shadow-blue-600/20 active:scale-95"
          >
            <div class="w-6 h-6 rounded-lg bg-blue-500 flex items-center justify-center group-hover:bg-blue-400 transition-colors">
              <PlusIcon class="w-4 h-4 text-white" />
            </div>
            <span>Yeni Fabrika Kur</span>
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
      <div v-else-if="factories.length === 0" class="bg-white rounded-3xl border border-dashed border-slate-300 p-16 text-center max-w-2xl mx-auto mt-10">
        <div class="w-24 h-24 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6">
          <span class="text-4xl">🏭</span>
        </div>
        <h3 class="text-2xl font-black text-slate-800 mb-3">Henüz Fabrikanız Yok</h3>
        <p class="text-slate-500 text-lg mb-8 leading-relaxed">
          Sanayi devrimine katılmak için ilk fabrikanızı kurun. Fabrikalar hammaddeyi işleyerek değerli ürünlere dönüştürür.
        </p>
        <button 
          @click="showWizard = true"
          class="px-8 py-4 bg-blue-600 text-white rounded-2xl font-bold text-lg hover:bg-blue-700 transition-all shadow-xl shadow-blue-600/20 hover:-translate-y-1"
        >
          Hemen Başla
        </button>
      </div>

      <!-- Content Grid -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          v-for="factory in factories" 
          :key="factory.id" 
          @click="$router.push(`/home/factories/${factory.id}`)"
          class="group cursor-pointer bg-white rounded-3xl shadow-sm border border-slate-200 hover:shadow-xl hover:border-blue-200 hover:-translate-y-1 transition-all duration-300 relative overflow-hidden"
        >
          <!-- Decorative Background -->
          <div class="absolute top-0 right-0 w-32 h-32 bg-blue-50 rounded-bl-full -mr-8 -mt-8 transition-transform group-hover:scale-110 duration-500"></div>
          
          <div class="p-6 relative z-10">
            <!-- Header -->
            <div class="flex items-start justify-between mb-6">
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 bg-gradient-to-br from-blue-100 to-indigo-200 rounded-2xl flex items-center justify-center shadow-inner text-2xl group-hover:scale-105 transition-transform duration-300">
                  🏭
                </div>
                <div>
                  <h3 class="font-black text-slate-800 text-lg leading-tight group-hover:text-blue-700 transition-colors">{{ factory.name || factory.subType }}</h3>
                  <div class="flex items-center gap-2 mt-1">
                    <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">{{ factory.subType }}</span>
                    <span class="w-1 h-1 rounded-full bg-slate-300"></span>
                    <span class="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded-md border border-blue-100">
                      LVL {{ factory.tier === 'SMALL' ? 1 : factory.tier === 'MEDIUM' ? 2 : 3 }}
                    </span>
                  </div>
                </div>
              </div>
              
              <!-- Status Badge -->
              <div class="flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-xs font-bold uppercase tracking-wide"
                :class="factory.isProducing ? 'bg-blue-50 border-blue-200 text-blue-700' : 'bg-slate-50 border-slate-200 text-slate-500'">
                <span class="relative flex h-2 w-2">
                  <span v-if="factory.isProducing" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2 w-2" :class="factory.isProducing ? 'bg-blue-500' : 'bg-slate-400'"></span>
                </span>
                {{ factory.isProducing ? 'ÜRETİMDE' : 'BOŞTA' }}
              </div>
            </div>

            <!-- Stats -->
            <div class="grid grid-cols-2 gap-3 mb-6">
              <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100 group-hover:border-blue-100 transition-colors">
                <div class="text-xs font-bold text-slate-400 uppercase mb-1">Stok</div>
                <div class="font-black text-slate-700 text-lg">
                  {{ factory.currentStock || 0 }}<span class="text-sm text-slate-400 font-medium">/{{ factory.maxStock }}</span>
                </div>
              </div>
              <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100 group-hover:border-blue-100 transition-colors">
                <div class="text-xs font-bold text-slate-400 uppercase mb-1">Çeşit</div>
                <div class="font-black text-slate-700 text-lg">
                  {{ factory.items?.length || 0 }} <span class="text-xs text-slate-400 font-medium">Ürün</span>
                </div>
              </div>
            </div>

            <!-- Progress Bar -->
            <div class="mb-6">
              <div class="flex justify-between text-xs font-bold text-slate-400 mb-1.5">
                <span>Doluluk Oranı</span>
                <span>%{{ Math.round(((factory.currentStock || 0) / factory.maxStock) * 100) }}</span>
              </div>
              <div class="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden border border-slate-100">
                <div class="h-full rounded-full transition-all duration-500"
                     :class="(factory.currentStock || 0) >= factory.maxStock ? 'bg-red-500' : 'bg-blue-500'"
                     :style="{ width: `${Math.min(((factory.currentStock || 0) / factory.maxStock) * 100, 100)}%` }">
                </div>
              </div>
            </div>

            <!-- Action Button -->
            <button class="w-full py-3 bg-white border-2 border-slate-100 text-slate-600 rounded-xl font-bold hover:border-blue-500 hover:text-blue-600 hover:bg-blue-50 transition-all flex items-center justify-center gap-2 group-hover:shadow-md">
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
      building-type="FACTORY" 
      :base-cost="25000"
      @close="showWizard = false"
      @create="handleCreate"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useFactoriesStore } from '../stores/factoriesStore'
import { useAuthStore } from '../stores/authStore'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import { PlusIcon } from '@heroicons/vue/24/outline'

const store = useFactoriesStore()
const authStore = useAuthStore()
const factories = ref([])
const loading = ref(true)
const showWizard = ref(false)

async function load() {
  loading.value = true
  try {
    await store.load()
    factories.value = store.items
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
    console.error('Failed to create factory:', error)
  }
}

onMounted(load)
</script>
