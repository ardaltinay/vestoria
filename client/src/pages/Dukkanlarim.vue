<template>
  <div>
    <PageHeader 
      title="Dükkanlarım" 
      description="Mevcut dükkanlarınızı yönetin ve gelir durumlarını kontrol edin."
    >
      <template #actions>
        <AppButton variant="primary" @click="$router.push('/home/shops/new')">
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
      <AppButton variant="primary" @click="$router.push('/home/shops/new')">
        Hemen Başla
      </AppButton>
    </div>

    <!-- Content Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <AppCard v-for="shop in shops" :key="shop.id" class="group">
        <template #header>
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg bg-amber-100 flex items-center justify-center text-amber-600">
                <BuildingStorefrontIcon class="w-6 h-6" />
              </div>
              <div>
                <h3 class="font-bold text-slate-900 group-hover:text-primary-600 transition-colors">{{ shop.name }}</h3>
                <div class="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full inline-block mt-1">
                  Seviye {{ shop.level }}
                </div>
              </div>
            </div>
            <!-- Status Indicator (Mock) -->
            <div class="w-2.5 h-2.5 rounded-full bg-emerald-500 shadow-sm" title="Aktif"></div>
          </div>
        </template>

        <div class="space-y-4">
          <p class="text-sm text-slate-600 line-clamp-2 min-h-[2.5rem]">{{ shop.description || 'Açıklama bulunmuyor.' }}</p>
          
          <div class="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-100">
            <span class="text-xs font-medium text-slate-500 uppercase">Gelir</span>
            <span class="text-sm font-bold text-emerald-600">{{ shop.revenue }} ₺/dk</span>
          </div>
        </div>

        <template #footer>
          <AppButton 
            variant="outline" 
            size="sm" 
            block 
            @click="$router.push(`/home/shops/${shop.id}`)"
          >
            Yönet
          </AppButton>
        </template>
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useShopsStore } from '../stores/shopsStore'
import PageHeader from '../components/PageHeader.vue'
import AppButton from '../components/AppButton.vue'
import AppCard from '../components/AppCard.vue'
import { PlusIcon, BuildingStorefrontIcon } from '@heroicons/vue/24/outline'

const store = useShopsStore()
const shops = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    await store.load()
    shops.value = store.items
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

