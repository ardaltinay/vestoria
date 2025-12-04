<template>
  <div class="space-y-6">
    <div class="bg-white rounded-2xl p-8 border border-slate-200 shadow-sm">
      <div class="flex flex-col items-center sm:flex-row sm:items-start gap-6">
        <div class="relative group">
          <img 
            :src="`https://ui-avatars.com/api/?name=${user?.username}&background=random&size=128`" 
            :alt="user?.username" 
            class="w-32 h-32 rounded-full border-4 border-white shadow-lg"
          />
          <div class="absolute inset-0 rounded-full border-2 border-slate-100 pointer-events-none"></div>
        </div>
        
        <div class="flex-1 text-center sm:text-left">
          <h1 class="text-3xl font-bold text-slate-900 mb-2">{{ user?.username }}</h1>
          <div class="flex flex-wrap justify-center sm:justify-start gap-3 mb-6">
            <span class="px-3 py-1 rounded-full bg-primary-50 text-primary-700 text-sm font-bold border border-primary-100">
              Seviye {{ user?.level || 1 }}
            </span>
            <span class="px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 text-sm font-bold border border-emerald-100">
              {{ formatCurrency(user?.balance || 0) }}
            </span>
          </div>
          
          <!-- XP Bar -->
          <div class="max-w-md">
            <div class="flex justify-between text-xs font-bold text-slate-500 mb-1">
              <span>XP İlerlemesi</span>
              <span>{{ user?.xp || 0 }} / {{ nextLevelXp }} XP</span>
            </div>
            <div class="w-full bg-slate-100 rounded-full h-3 overflow-hidden border border-slate-200">
              <div 
                class="bg-gradient-to-r from-primary-500 to-primary-400 h-full rounded-full transition-all duration-500 relative" 
                :style="{ width: `${xpPercentage}%` }"
              >
                <div class="absolute inset-0 bg-white/20 animate-[shimmer_2s_infinite]"></div>
              </div>
            </div>
            <p class="text-xs text-slate-400 mt-2">Bir sonraki seviyeye ulaşmak için {{ nextLevelXp - (user?.xp || 0) }} XP daha kazanmalısın.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <div class="text-sm text-slate-500 font-medium mb-1">Toplam Varlık</div>
        <div class="text-2xl font-bold text-slate-900">{{ formatCurrency(user?.netWorth || user?.balance || 0) }}</div>
      </div>
      <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <div class="text-sm text-slate-500 font-medium mb-1">İşletme Sayısı</div>
        <div class="text-2xl font-bold text-slate-900">{{ totalBuildings }}</div>
      </div>
      <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <div class="text-sm text-slate-500 font-medium mb-1">Kayıt Tarihi</div>
        <div class="text-2xl font-bold text-slate-900">{{ formatDate(user?.createdAt) }}</div>
      </div>
      <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <div class="text-sm text-slate-500 font-medium mb-1">Prestij Puanı</div>
        <div class="text-2xl font-bold text-amber-500">0</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/authStore'
import { useShopsStore } from '../stores/shopsStore'
import { useFarmsStore } from '../stores/farmsStore'
import { useFactoriesStore } from '../stores/factoriesStore'
import { useMinesStore } from '../stores/minesStore'
import { useGardensStore } from '../stores/gardensStore'
import { formatCurrency } from '../utils/currency'

const authStore = useAuthStore()
const shopsStore = useShopsStore()
const farmsStore = useFarmsStore()
const factoriesStore = useFactoriesStore()
const minesStore = useMinesStore()
const gardensStore = useGardensStore()

const user = computed(() => authStore.user)
const nextLevelXp = computed(() => (user.value?.level || 1) * 1000)
const xpPercentage = computed(() => {
  const percentage = ((user.value?.xp || 0) / nextLevelXp.value) * 100
  return Math.min(percentage, 100)
})

const totalBuildings = computed(() => {
  return shopsStore.items.length + 
         farmsStore.items.length + 
         factoriesStore.items.length + 
         minesStore.items.length + 
         gardensStore.items.length
})

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('tr-TR', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

onMounted(async () => {
  await Promise.all([
    authStore.fetchUser(),
    shopsStore.load(),
    farmsStore.load(),
    factoriesStore.load(),
    minesStore.load(),
    gardensStore.load()
  ])
})
</script>
