<template>
  <div>
    <PageHeader 
      title="Bahçelerim" 
      description="Mevcut bahçelerinizi yönetin ve hasat durumlarını kontrol edin."
    >
      <template #actions>
        <AppButton variant="primary" @click="showWizard = true">
          <template #icon-left>
            <PlusIcon class="w-5 h-5" />
          </template>
          Yeni Bahçe Aç
        </AppButton>
      </template>
    </PageHeader>

    <!-- Loading State -->
    <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-6 shadow-sm border border-slate-200 animate-pulse h-48"></div>
    </div>

    <!-- Empty State -->
    <div v-else-if="gardens.length === 0" class="bg-white rounded-xl border border-slate-200 p-12 text-center">
      <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <HomeIcon class="w-8 h-8 text-slate-400" />
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-2">Henüz Bahçeniz Yok</h3>
      <p class="text-slate-500 max-w-md mx-auto mb-6">Doğayla iç içe olmak için ilk bahçenizi kurun. Bahçeler size çeşitli ürünler sağlar.</p>
      <AppButton variant="primary" @click="showWizard = true">
        Hemen Başla
      </AppButton>
    </div>

    <!-- Content Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
      <div 
        v-for="garden in gardens" 
        :key="garden.id" 
        @click="$router.push(`/home/gardens/${garden.id}`)"
        class="group cursor-pointer bg-white rounded-lg shadow-sm border-l-4 border-emerald-500 hover:shadow-lg transition-all duration-200"
      >
        <!-- Header -->
        <div class="p-4 sm:p-5 border-b border-gray-100">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3 flex-1">
              <div class="w-12 h-12 bg-emerald-50 rounded-lg flex items-center justify-center flex-shrink-0 group-hover:bg-emerald-100 transition-colors">
                <HomeIcon class="w-6 h-6 text-emerald-600" />
              </div>
              <div class="flex-1 min-w-0">
                <h3 class="font-bold text-gray-900 group-hover:text-emerald-600 transition-colors text-sm sm:text-base truncate">{{ garden.name || garden.subType }}</h3>
                <p class="text-xs text-gray-500 truncate">{{ garden.subType }}</p>
                <span class="inline-block mt-1 text-xs font-semibold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full">
                  Seviye {{ convertTierToTr(garden.tier) }}
                </span>
              </div>
            </div>
            <div class="w-2 h-2 rounded-full bg-emerald-500 shadow-sm animate-pulse flex-shrink-0" title="Aktif"></div>
          </div>
        </div>

        <!-- Content -->
        <div class="p-4 sm:p-5 space-y-4">
          <p class="text-xs sm:text-sm text-gray-600 line-clamp-2 min-h-[2.5rem]">{{ garden.description || 'Açıklama bulunmuyor.' }}</p>
          
          <!-- Stats Grid -->
          <div class="grid grid-cols-2 gap-2 sm:gap-3">
            <div class="bg-gray-50 rounded-lg p-2.5 sm:p-3 border border-gray-200">
              <div class="text-xs font-medium text-gray-500 mb-0.5 sm:mb-1">Gelir /dak</div>
              <div class="text-base sm:text-lg font-bold text-gray-900">
                <Currency :amount="garden.revenue || 0" :icon-size="16" />
              </div>
            </div>
            <div class="bg-gray-50 rounded-lg p-2.5 sm:p-3 border border-gray-200">
              <div class="text-xs font-medium text-gray-500 mb-0.5 sm:mb-1">Ürün Çeşidi</div>
              <div class="text-base sm:text-lg font-bold text-gray-900">{{ garden.items?.length || 0 }}</div>
            </div>
          </div>

          <!-- Progress Bar -->
          <div>
              <div class="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                <div class="bg-emerald-500 h-2 rounded-full transition-all" :style="{ width: `${(garden.currentStock / garden.maxStock) * 100}%` }"></div>
              </div>
              <p class="text-xs text-gray-500 mt-1">{{ garden.currentStock || 0 }}/{{ garden.maxStock }}</p>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-4 sm:px-5 pb-4 sm:pb-5">
          <button class="w-full px-4 py-2.5 bg-emerald-600 text-white rounded-lg font-semibold hover:bg-emerald-700 transition-colors shadow-sm text-sm">
            <span class="flex items-center justify-center gap-2">
              <ArchiveBoxIcon class="w-4 h-4" />
              Yönet
            </span>
          </button>
        </div>
      </div>
    </div>
    <!-- Create Wizard -->
    <CreateBuildingWizard 
      v-if="showWizard" 
      building-type="GARDEN" 
      :base-cost="25000"
      @close="showWizard = false"
      @create="handleCreate"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useGardensStore } from '../stores/gardensStore'
import { useAuthStore } from '../stores/authStore'
import PageHeader from '../components/PageHeader.vue'
import AppButton from '../components/AppButton.vue'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import Currency from '../components/Currency.vue'
import { PlusIcon, ArchiveBoxIcon, HomeIcon } from '@heroicons/vue/24/outline'

const store = useGardensStore()
const authStore = useAuthStore()
const router = useRouter()
const gardens = ref([])
const loading = ref(true)
const showWizard = ref(false)

async function load() {
  loading.value = true
  try {
    await store.load()
    gardens.value = store.items
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
    console.error('Failed to create garden:', error)
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

onMounted(load)
</script>

