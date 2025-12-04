<template>
  <div class="space-y-6">
    <div class="relative overflow-hidden rounded-3xl bg-white border border-slate-200 shadow-xl">
      <!-- Background Pattern & Gradients -->
      <div class="absolute inset-0 bg-gradient-to-br from-slate-50 via-white to-primary-50/30"></div>
      <div class="absolute top-0 right-0 -mt-20 -mr-20 w-96 h-96 bg-primary-100/40 rounded-full blur-3xl animate-pulse"></div>
      <div class="absolute bottom-0 left-0 -mb-20 -ml-20 w-72 h-72 bg-emerald-100/40 rounded-full blur-3xl"></div>
      
      <!-- Content -->
      <div class="relative z-10 p-8 sm:p-12">
        <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div>
            <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white border border-slate-200 shadow-sm text-xs font-medium text-slate-600 mb-4">
              <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Piyasalar Açık
            </div>
            <h1 class="text-3xl sm:text-4xl font-display font-bold mb-2 tracking-tight text-slate-900">
              Hoş geldiniz, <span class="text-transparent bg-clip-text bg-gradient-to-r from-primary-600 to-primary-400">{{ username }}</span>
            </h1>
            <p class="text-slate-500 text-lg max-w-xl leading-relaxed">
              İmparatorluğunuz büyümeye devam ediyor. Bugün yeni fırsatları değerlendirmek için harika bir gün.
            </p>
          </div>
          
          <!-- Quick Action Button -->
          <div class="flex flex-col gap-3 min-w-[160px]">
            <button 
              @click="handleViewReports"
              class="group relative overflow-hidden bg-slate-900 text-white px-6 py-3 rounded-xl font-bold shadow-lg hover:shadow-xl hover:bg-slate-800 transition-all transform hover:-translate-y-0.5"
            >
              <span class="relative z-10 flex items-center justify-center gap-2">
                Raporları İncele
                <ArrowRightIcon class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </span>
            </button>
            <button 
              @click="handleNewInvestment"
              class="px-6 py-3 rounded-xl font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors border border-slate-200 bg-white"
            >
              Yeni Yatırım Yap
            </button>
          </div>
        </div>

        <!-- Stats Row -->
        <div class="mt-10 grid grid-cols-2 sm:grid-cols-4 gap-4 pt-8 border-t border-slate-100">
          <div>
            <div class="text-slate-400 text-xs uppercase tracking-wider font-semibold mb-1">Günlük Kazanç</div>
            <div class="text-2xl font-bold text-slate-900 flex items-center gap-2">
              <template v-if="dashboardStats.dailyEarnings">
                <CurrencyIcon :size="24" />
                {{ formatCurrency(dashboardStats.dailyEarnings) }}
              </template>
              <template v-else>-</template>
            </div>
          </div>
          <div>
            <div class="text-slate-400 text-xs uppercase tracking-wider font-semibold mb-1">Aktif İşletme</div>
            <div class="text-2xl font-bold text-slate-900">{{ dashboardStats.activeBusinesses !== null ? dashboardStats.activeBusinesses : '-' }}</div>
          </div>
          <div>
            <div class="text-slate-400 text-xs uppercase tracking-wider font-semibold mb-1">Pazar Payı</div>
            <div class="text-2xl font-bold text-emerald-600 flex items-center gap-1">
              {{ dashboardStats.marketShare ? '%' + dashboardStats.marketShare.toFixed(1) : '-' }}
              <ArrowTrendingUpIcon v-if="dashboardStats.marketShare > 0" class="w-4 h-4" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dashboard Widgets -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
       <!-- Market Trends -->
       <MarketTrendsWidget />

       <!-- Production Summary -->
       <ProductionWidget />

       <!-- Recent Activity -->
       <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
         <h3 class="font-bold text-slate-800 mb-4">Son Aktiviteler</h3>
         <div class="space-y-4">
           <div v-if="notifications.length === 0" class="text-slate-400 text-sm">Henüz bir aktivite yok.</div>
           <div v-for="notification in notifications.slice(0, 3)" :key="notification.id" class="flex items-center gap-3 text-sm">
             <div class="w-2 h-2 rounded-full flex-shrink-0" :class="notification.isRead ? 'bg-slate-300' : 'bg-emerald-500'"></div>
             <span class="text-slate-600 truncate hover:whitespace-normal hover:overflow-visible transition-all duration-200 cursor-help" :title="notification.message">{{ notification.message }}</span>
             <span class="ml-auto text-slate-400 text-xs whitespace-nowrap">{{ new Date(notification.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}</span>
           </div>
         </div>
       </div>
    </div>

    <!-- Create Wizard -->
    <CreateBuildingWizard 
      v-if="showWizard" 
      :building-type="wizardType" 
      :base-cost="25000"
      @close="showWizard = false"
      @create="handleCreate"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import UserService from '../services/UserService'
import { useNotificationStore } from '../stores/notificationStore'
import { storeToRefs } from 'pinia'
import { useToast } from '../composables/useToast'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'
import CurrencyIcon from '../components/CurrencyIcon.vue'
import { formatCurrency } from '../utils/currency'
import { 
  ArrowRightIcon, 
  ArrowTrendingUpIcon 
} from '@heroicons/vue/24/outline'
import MarketTrendsWidget from '../components/dashboard/MarketTrendsWidget.vue'
import ProductionWidget from '../components/dashboard/ProductionWidget.vue'

const authStore = useAuthStore()
const router = useRouter()
const notificationStore = useNotificationStore()
const { notifications } = storeToRefs(notificationStore)

const username = computed(() => authStore.user?.username || 'Oyuncu')
const dashboardStats = ref({})
const showWizard = ref(false)
const wizardType = ref('SHOP')
const { addToast } = useToast()

const fetchDashboardStats = async () => {
  try {
    const response = await UserService.getDashboardStats()
    dashboardStats.value = response.data
  } catch (error) {
    console.error('Failed to fetch dashboard stats', error)
  }
}

const handleNewInvestment = () => {
  wizardType.value = 'SHOP'
  showWizard.value = true
}

const handleViewReports = () => {
  router.push('/home/reports')
}

const handleCreate = async (payload) => {
  try {
    let store;
    switch(payload.type) {
        case 'SHOP':
            const { useShopsStore } = await import('../stores/shopsStore');
            store = useShopsStore();
            break;
        case 'FARM':
            const { useFarmsStore } = await import('../stores/farmsStore');
            store = useFarmsStore();
            break;
        case 'FACTORY':
            const { useFactoriesStore } = await import('../stores/factoriesStore');
            store = useFactoriesStore();
            break;
        case 'MINE':
            const { useMinesStore } = await import('../stores/minesStore');
            store = useMinesStore();
            break;
        case 'GARDEN':
            const { useGardensStore } = await import('../stores/gardensStore');
            store = useGardensStore();
            break;
    }
    
    if (store) {
        await store.create(payload)
        addToast('Yatırım başarıyla gerçekleştirildi!', 'success')
        showWizard.value = false
        fetchDashboardStats()
    }
  } catch (error) {
     console.error(error)
  }
}

onMounted(() => {
  fetchDashboardStats()
  // Notifications are fetched by Home.vue (parent)
  // We just use the store state which is reactive
})
</script>
