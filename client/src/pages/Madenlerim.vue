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
    <div v-else-if="items.length === 0" class="bg-white rounded-xl border border-slate-200 p-12 text-center">
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
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <AppCard v-for="it in items" :key="it.id" class="group">
        <template #header>
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600">
                <MapIcon class="w-6 h-6" />
              </div>
              <div>
                <h3 class="font-bold text-slate-900 group-hover:text-primary-600 transition-colors">{{ it.name }}</h3>
                <div class="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full inline-block mt-1">
                  Seviye {{ it.level }}
                </div>
              </div>
            </div>
            <!-- Status Indicator (Mock) -->
            <div class="w-2.5 h-2.5 rounded-full bg-emerald-500 shadow-sm" title="Aktif"></div>
          </div>
        </template>

        <div class="space-y-4">
          <p class="text-sm text-slate-600 line-clamp-2 min-h-[2.5rem]">{{ it.description || 'Açıklama bulunmuyor.' }}</p>
          
          <!-- Progress Bar Mock -->
          <div>
            <div class="flex justify-between text-xs mb-1">
              <span class="text-slate-500">Çıkarım</span>
              <span class="font-medium text-slate-700">30%</span>
            </div>
            <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
              <div class="bg-slate-500 h-1.5 rounded-full" style="width: 30%"></div>
            </div>
          </div>
        </div>

        <template #footer>
          <AppButton 
            variant="outline" 
            size="sm" 
            block 
            @click="$router.push(`/home/mines/${it.id}`)"
          >
            Yönet
          </AppButton>
        </template>
      </AppCard>
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
import PageHeader from '../components/PageHeader.vue'
import AppButton from '../components/AppButton.vue'
import AppCard from '../components/AppCard.vue'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import { PlusIcon, MapIcon } from '@heroicons/vue/24/outline'

const store = useMinesStore()
const items = ref([])
const loading = ref(true)
const showWizard = ref(false)

async function load() {
  loading.value = true
  try {
    await store.load()
    items.value = store.items
  } finally {
    loading.value = false
  }
}

async function handleCreate(payload) {
  try {
    await store.create(payload)
    showWizard.value = false
    await load()
  } catch (error) {
    console.error('Failed to create mine:', error)
  }
}

onMounted(load)
</script>

