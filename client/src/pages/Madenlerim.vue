<template>
  <div>
    <PageHeader 
      title="Madenlerim" 
      description="Mevcut madenlerinizi yönetin ve kaynak çıkarımını kontrol edin."
    >
      <template #actions>
        <AppButton variant="primary" @click="showWizard = true">
          <template #icon-left>
            <PlusIcon class="w-5 h-5" />
          </template>
          Yeni Maden Aç
        </AppButton>
      </template>
    </PageHeader>

    <!-- Loading State -->
    <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-6 shadow-sm border border-slate-200 animate-pulse h-48"></div>
    </div>

    <!-- Empty State -->
    <div v-else-if="mines.length === 0" class="bg-white rounded-xl border border-slate-200 p-12 text-center">
      <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <MapIcon class="w-8 h-8 text-slate-400" />
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-2">Henüz Madeniniz Yok</h3>
      <p class="text-slate-500 max-w-md mx-auto mb-6">Yeraltı zenginliklerine ulaşmak için ilk madenini aç. Madenler değerli kaynaklar sağlar.</p>
      <AppButton variant="primary" @click="showWizard = true">
        Hemen Başla
      </AppButton>
    </div>

    <!-- Content Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
      <div 
        v-for="mine in mines" 
        :key="mine.id" 
        @click="$router.push(`/home/mines/${mine.id}`)"
        class="group cursor-pointer bg-white rounded-lg shadow-sm border-l-4 border-orange-500 hover:shadow-lg transition-all duration-200"
      >
        <!-- Header -->
        <div class="p-4 sm:p-5 border-b border-gray-100">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3 flex-1">
              <div class="w-12 h-12 bg-orange-50 rounded-lg flex items-center justify-center flex-shrink-0 group-hover:bg-orange-100 transition-colors">
                <MapIcon class="w-6 h-6 text-orange-600" />
              </div>
              <div class="flex-1 min-w-0">
                <h3 class="font-bold text-gray-900 group-hover:text-orange-600 transition-colors text-sm sm:text-base truncate">{{ mine.name || it.subType }}</h3>
                <p class="text-xs text-gray-500 truncate">{{ mine.subType }}</p>
                <span class="inline-block mt-1 text-xs font-semibold text-orange-700 bg-orange-100 px-2 py-0.5 rounded-full">
                  Seviye {{ convertTierToTr(mine.tier) }}
                </span>
              </div>
            </div>
            <div class="w-2 h-2 rounded-full bg-emerald-500 shadow-sm animate-pulse flex-shrink-0" title="Aktif"></div>
          </div>
        </div>

        <!-- Content -->
        <div class="p-4 sm:p-5 space-y-4">
          <p class="text-xs sm:text-sm text-gray-600 line-clamp-2 min-h-[2.5rem]">{{ mine.description || 'Açıklama bulunmuyor.' }}</p>
          
          <!-- Stats Grid -->
          <div class="grid grid-cols-2 gap-2 sm:gap-3">
            <div class="bg-gray-50 rounded-lg p-2.5 sm:p-3 border border-gray-200">
              <div class="text-xs font-medium text-gray-500 mb-0.5 sm:mb-1">Gelir /dak</div>
              <div class="text-base sm:text-lg font-bold text-gray-900">
                <Currency :amount="mine.revenue || 0" :icon-size="16" />
              </div>
            </div>
            <div class="bg-gray-50 rounded-lg p-2.5 sm:p-3 border border-gray-200">
              <div class="text-xs font-medium text-gray-500 mb-0.5 sm:mb-1">Ürün Çeşidi</div>
              <div class="text-base sm:text-lg font-bold text-gray-900">{{ mine.items?.length || 0 }}</div>
            </div>
          </div>

          <!-- Progress Bar -->
          <div>
              <div class="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                <div class="bg-orange-500 h-2 rounded-full transition-all" :style="{ width: `${(mine.currentStock / mine.maxStock) * 100}%` }"></div>
              </div>
              <p class="text-xs text-gray-500 mt-1">{{ mine.currentStock || 0 }}/{{ mine.maxStock }}</p>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-4 sm:px-5 pb-4 sm:pb-5">
          <button class="w-full px-4 py-2.5 bg-orange-600 text-white rounded-lg font-semibold hover:bg-orange-700 transition-colors shadow-sm text-sm">
            <span class="flex items-center justify-center gap-2">
              <WrenchScrewdriverIcon class="w-4 h-4" />
              Yönet
            </span>
          </button>
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
import PageHeader from '../components/PageHeader.vue'
import AppButton from '../components/AppButton.vue'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import Currency from '../components/Currency.vue'
import { PlusIcon, WrenchScrewdriverIcon, MapIcon } from '@heroicons/vue/24/outline'

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

