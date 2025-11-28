<template>
  <div>
    <PageHeader 
      title="Dükkanlarım" 
      description="Mevcut dükkanlarınızı yönetin ve gelir durumlarını kontrol edin."
    >
      <template #actions>
        <AppButton variant="primary" @click="showWizard = true">
          <template #icon-left>
            <PlusIcon class="w-5 h-5" />
          </template>
          Yeni Dükkan Aç
        </AppButton>
      </template>
    </PageHeader>

    <!-- Loading State -->
    <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-6 shadow-sm border border-slate-200 animate-pulse h-48"></div>
    </div>

    <!-- Empty State -->
    <div v-else-if="shops.length === 0" class="bg-white rounded-xl border border-slate-200 p-12 text-center">
      <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <BuildingStorefrontIcon class="w-8 h-8 text-slate-400" />
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-2">Henüz Dükkanınız Yok</h3>
      <p class="text-slate-500 max-w-md mx-auto mb-6">Ticaret hayatına atılmak için ilk dükkanınızı açın. Dükkanlar size düzenli nakit akışı sağlar.</p>
      <AppButton variant="primary" @click="showWizard = true">
        Hemen Başla
      </AppButton>
    </div>

    <!-- Content Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
      <div 
        v-for="shop in shops" 
        :key="shop.id" 
        @click="$router.push(`/home/shops/${shop.id}`)"
        class="group cursor-pointer bg-white rounded-lg shadow-sm border-l-4 border-amber-500 hover:shadow-lg transition-all duration-200"
      >
        <!-- Header -->
        <div class="p-4 sm:p-5 border-b border-gray-100">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3 flex-1">
              <div class="w-12 h-12 bg-amber-50 rounded-lg flex items-center justify-center flex-shrink-0 group-hover:bg-amber-100 transition-colors">
                <BuildingStorefrontIcon class="w-6 h-6 text-amber-600" />
              </div>
              <div class="flex-1 min-w-0">
                <h3 class="font-bold text-gray-900 group-hover:text-amber-600 transition-colors text-sm sm:text-base truncate">{{ shop.name }}</h3>
                <p class="text-xs text-gray-500 truncate">{{ convertSubTypeToTr(shop.subType) }}</p>
                <span class="inline-block mt-1 text-xs font-semibold text-amber-700 bg-amber-100 px-2 py-0.5 rounded-full">
                  Seviye {{ convertTierToTr(shop.tier) }}
                </span>
              </div>
            </div>
            <div class="w-2 h-2 rounded-full bg-emerald-500 shadow-sm animate-pulse flex-shrink-0" title="Aktif"></div>
          </div>
        </div>

        <!-- Content -->
        <div class="p-4 sm:p-5 space-y-4">
          <p class="text-xs sm:text-sm text-gray-600 line-clamp-2 min-h-[2.5rem]">{{ shop.description || 'Açıklama bulunmuyor.' }}</p>
          
          <!-- Stats Grid -->
          <div class="grid grid-cols-2 gap-2 sm:gap-3">
            <div class="bg-gray-50 rounded-lg p-2.5 sm:p-3 border border-gray-200">
              <div class="text-xs font-medium text-gray-500 mb-0.5 sm:mb-1">Gelir /dak</div>
              <div class="text-base sm:text-lg font-bold text-gray-900">
                <Currency :amount="shop.productionRate || 0" :icon-size="16" />
              </div>
            </div>
            <div class="bg-gray-50 rounded-lg p-2.5 sm:p-3 border border-gray-200">
              <div class="text-xs font-medium text-gray-500 mb-0.5 sm:mb-1">Stok</div>
              <div class="text-base sm:text-lg font-bold text-gray-900">{{ shop.currentStock || 0 }}/{{ shop.maxStock }}</div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-4 sm:px-5 pb-4 sm:pb-5">
          <button class="w-full px-4 py-2.5 bg-amber-600 text-white rounded-lg font-semibold hover:bg-amber-700 transition-colors shadow-sm text-sm">
            <span class="flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
              Yönet
            </span>
          </button>
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
import PageHeader from '../components/PageHeader.vue'
import AppButton from '../components/AppButton.vue'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import Currency from '../components/Currency.vue'
import { PlusIcon, BuildingStorefrontIcon } from '@heroicons/vue/24/outline'

const store = useShopsStore()
const authStore = useAuthStore()
const router = useRouter() // Added useRouter
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

