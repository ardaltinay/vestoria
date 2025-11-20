<template>
  <div class="min-h-screen bg-slate-50 font-sans flex">
    <!-- Mobile Sidebar Overlay -->
    <div 
      v-if="isSidebarOpen" 
      class="fixed inset-0 bg-slate-900/50 z-40 lg:hidden backdrop-blur-sm transition-opacity"
      @click="isSidebarOpen = false"
    ></div>

    <!-- Sidebar -->
    <aside 
      :class="[
        'fixed lg:static inset-y-0 left-0 z-50 w-72 bg-white border-r border-slate-200 transform transition-transform duration-300 ease-in-out flex flex-col',
        isSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      ]"
    >
      <!-- Logo Area -->
      <div class="h-16 flex items-center px-6 border-b border-slate-100">
        <RouterLink to="/home" class="flex items-center gap-3 group">
          <Logo size="sm" :showText="true" />
        </RouterLink>
      </div>

      <!-- User Profile Summary -->
      <div class="p-4 border-b border-slate-100 bg-slate-50/50">
        <div class="flex items-center gap-3 mb-3">
          <img 
            src="https://ui-avatars.com/api/?name=Oyuncu&background=random" 
            alt="Oyuncu" 
            class="w-10 h-10 rounded-full border-2 border-white shadow-sm" 
          />
          <div class="flex-1 min-w-0">
            <div class="text-sm font-bold text-slate-900 truncate">{{ username }}</div>
            <!-- XP Bar -->
            <div class="mt-1">
              <div class="flex justify-between text-[10px] text-slate-500 mb-0.5">
                <span>Seviye {{ currentLevel }}</span>
                <span>{{ currentXp }}/{{ nextLevelXp }} XP</span>
              </div>
              <div class="w-full bg-slate-200 rounded-full h-1.5 overflow-hidden">
                <div 
                  class="bg-primary-500 h-1.5 rounded-full transition-all duration-500" 
                  :style="{ width: `${xpPercentage}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Quick Stats -->
        <div class="grid grid-cols-2 gap-2">
          <div class="bg-white p-2 rounded-lg border border-slate-200 shadow-sm">
            <div class="text-[10px] uppercase tracking-wider text-slate-400 font-semibold">Nakit</div>
            <div class="text-sm font-bold text-emerald-600">₺{{ balance }}</div>
          </div>
          <div class="bg-white p-2 rounded-lg border border-slate-200 shadow-sm">
            <div class="text-[10px] uppercase tracking-wider text-slate-400 font-semibold">Prestij</div>
            <div class="text-sm font-bold text-amber-500">★ 24</div>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <!-- Navigation -->
      <nav class="flex-1 overflow-y-auto py-4 px-3 space-y-6">
        <!-- Business Section -->
        <div>
          <div class="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">İşletmeler</div>
          <div class="space-y-1">
            <RouterLink 
              v-for="item in businessItems" 
              :key="item.path" 
              :to="item.path"
              class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group"
              :class="[
                $route.path.startsWith(item.path) 
                  ? 'bg-primary-50 text-primary-700' 
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              ]"
              @click="isSidebarOpen = false"
            >
              <component 
                :is="item.icon" 
                class="w-5 h-5 transition-colors"
                :class="[
                  $route.path.startsWith(item.path) ? 'text-primary-600' : 'text-slate-400 group-hover:text-slate-600'
                ]"
              />
              {{ item.label }}
            </RouterLink>
          </div>
        </div>

        <!-- Market Section -->
        <div>
          <div class="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">Pazar</div>
          <div class="space-y-1">
            <RouterLink 
              v-for="item in marketItems" 
              :key="item.path" 
              :to="item.path"
              class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group"
              :class="[
                $route.path.startsWith(item.path) 
                  ? 'bg-primary-50 text-primary-700' 
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              ]"
              @click="isSidebarOpen = false"
            >
              <component 
                :is="item.icon" 
                class="w-5 h-5 transition-colors"
                :class="[
                  $route.path.startsWith(item.path) ? 'text-primary-600' : 'text-slate-400 group-hover:text-slate-600'
                ]"
              />
              {{ item.label }}
            </RouterLink>
          </div>
        </div>

        <!-- Social Section -->
        <div>
          <div class="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">Sosyal</div>
          <div class="space-y-1">
            <a 
              v-for="item in socialItems"
              :key="item.label"
              :href="item.path" 
              class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
            >
              <component 
                :is="item.icon" 
                class="w-5 h-5 text-slate-400 group-hover:text-slate-600 transition-colors"
              />
              {{ item.label }}
            </a>
          </div>
        </div>


      </nav>

      <!-- Footer -->
      <div class="p-4 border-t border-slate-100 text-xs text-center text-slate-400">
        Vestoria v2.0 &copy; 2025
      </div>
    </aside>

    <!-- Main Content Wrapper -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Top Header -->
      <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 lg:px-8 shadow-sm z-30">
        <div class="flex items-center gap-4">
          <button 
            @click="isSidebarOpen = true"
            class="lg:hidden p-2 -ml-2 text-slate-500 hover:bg-slate-100 rounded-lg"
          >
            <Bars3Icon class="w-6 h-6" />
          </button>
          
          <!-- Breadcrumbs or Page Title could go here -->
          <div class="hidden sm:flex items-center text-sm text-slate-500">
            <span class="font-medium text-slate-900">Ana Sayfa</span>
            <ChevronRightIcon class="w-4 h-4 mx-2" />
            <span v-if="$route.name !== 'HomeMain'" class="text-slate-600">{{ currentRouteName }}</span>
          </div>
        </div>

        <div class="flex items-center gap-3 sm:gap-4">
          <div class="relative">
            <button 
              @click="toggleNotifications"
              class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors relative"
            >
              <BellIcon class="w-6 h-6" />
              <span v-if="unreadCount > 0" class="absolute top-2 right-2 w-2 h-2 bg-rose-500 rounded-full border border-white"></span>
            </button>

            <!-- Notification Dropdown -->
            <div v-if="showNotifications" class="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-lg border border-slate-200 z-50 overflow-hidden">
              <div class="p-3 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                <h3 class="text-sm font-bold text-slate-700">Bildirimler</h3>
                <button @click="markAllRead" class="text-xs text-primary-600 hover:text-primary-700 font-medium">Tümünü Okundu Yap</button>
              </div>
              <div class="max-h-96 overflow-y-auto">
                <div v-if="notifications.length === 0" class="p-4 text-center text-sm text-slate-500">
                  Henüz bildiriminiz yok.
                </div>
                <div 
                  v-for="notification in notifications" 
                  :key="notification.id"
                  class="p-3 border-b border-slate-50 hover:bg-slate-50 transition-colors flex gap-3"
                  :class="{ 'bg-primary-50/30': !notification.isRead }"
                >
                  <div class="mt-1 w-2 h-2 rounded-full flex-shrink-0" :class="notification.isRead ? 'bg-slate-200' : 'bg-primary-500'"></div>
                  <div class="flex-1">
                    <p class="text-sm text-slate-800 leading-snug">{{ notification.message }}</p>
                    <p class="text-xs text-slate-400 mt-1">{{ formatDate(notification.createdAt) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="h-8 w-px bg-slate-200 mx-1 hidden sm:block"></div>
          
          <!-- User Menu Dropdown -->
          <div class="relative">
            <button 
              @click="showUserMenu = !showUserMenu"
              class="flex items-center gap-2 text-sm font-medium text-slate-700 hover:text-slate-900"
            >
              <span class="hidden sm:block">Hesabım</span>
              <ChevronDownIcon class="w-4 h-4 text-slate-400" />
            </button>

            <div v-if="showUserMenu" class="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-lg border border-slate-200 z-50 overflow-hidden py-1">
              <button 
                @click="handleLogout"
                class="w-full flex items-center gap-2 px-4 py-2 text-sm text-rose-600 hover:bg-rose-50 transition-colors"
              >
                <ArrowRightIcon class="w-4 h-4" />
                Çıkış Yap
              </button>
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content Area -->
      <main class="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8 scroll-smooth">
        <div class="max-w-7xl mx-auto">
          <div v-if="$route.name === 'HomeMain'" class="space-y-6">
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
                    <div class="text-2xl font-bold text-slate-900">{{ dashboardStats.dailyEarnings ? '₺' + dashboardStats.dailyEarnings : '-' }}</div>
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

            <!-- Dashboard Widgets Placeholder -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
               <!-- Example Widgets -->
               <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                 <h3 class="font-bold text-slate-800 mb-4">Son Aktiviteler</h3>
                 <div class="space-y-4">
                   <div v-if="notifications.length === 0" class="text-slate-400 text-sm">Henüz bir aktivite yok.</div>
                   <div v-for="notification in notifications.slice(0, 3)" :key="notification.id" class="flex items-center gap-3 text-sm">
                     <div class="w-2 h-2 rounded-full" :class="notification.isRead ? 'bg-slate-300' : 'bg-emerald-500'"></div>
                     <span class="text-slate-600 truncate">{{ notification.message }}</span>
                     <span class="ml-auto text-slate-400 text-xs whitespace-nowrap">{{ new Date(notification.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}</span>
                   </div>
                 </div>
               </div>
            </div>
          </div>
          
          <RouterView v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </RouterView>
        </div>
      </main>
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
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  HomeIcon, 
  BuildingStorefrontIcon, 
  TrophyIcon, 
  BeakerIcon, 
  MapIcon,
  NewspaperIcon,
  UsersIcon,
  Bars3Icon,
  BellIcon,
  ChevronRightIcon,
  ChevronDownIcon,
  ArrowRightIcon,
  ArrowTrendingUpIcon
} from '@heroicons/vue/24/outline'
import Logo from '../components/Logo.vue'
import AuthService from '../services/AuthService'
import { useAuthStore } from '../stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isSidebarOpen = ref(false)
const username = computed(() => authStore.user?.username || 'Oyuncu')
const balance = computed(() => authStore.user?.balance || 0)
const currentLevel = computed(() => authStore.user?.level || 1)
const currentXp = computed(() => authStore.user?.xp || 0)
const nextLevelXp = computed(() => currentLevel.value * 1000)
const xpPercentage = computed(() => {
  const percentage = (currentXp.value / nextLevelXp.value) * 100
  return Math.min(percentage, 100)
})

// Notifications
import NotificationService from '../services/NotificationService'
import { onMounted, onUnmounted } from 'vue'

import UserService from '../services/UserService'
import { useToast } from '../composables/useToast'
import CreateBuildingWizard from '../components/CreateBuildingWizard.vue'

const showNotifications = ref(false)
const showUserMenu = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const dashboardStats = ref({})
const showWizard = ref(false)
const wizardType = ref('SHOP') // Default
const { addToast } = useToast()

let pollingInterval = null

const fetchDashboardStats = async () => {
  try {
    const response = await UserService.getDashboardStats()
    dashboardStats.value = response.data
  } catch (error) {
    console.error('Failed to fetch dashboard stats', error)
  }
}

const handleNewInvestment = () => {
  wizardType.value = 'SHOP' // Default or ask user? Let's default to Shop for now
  showWizard.value = true
}

const handleViewReports = () => {
  addToast('Detaylı raporlar çok yakında!', 'info')
}

const handleCreate = async (payload) => {
  // The wizard emits create, but the store call happens inside the specific page usually.
  // But here we are on Home. We need to know which store to call based on type.
  // Actually, the pages (Shops, Farms etc) handle the creation logic via the store.
  // If we open the wizard here, we need to handle the creation.
  // Let's import the stores dynamically or just use a switch.
  // OR, simpler: Redirect to the specific page and open wizard there?
  // No, the user wants to do it from here.
  
  // Let's implement a simple switch to call the right store.
  // We need to import stores.
  // Ideally, we should refactor creation logic to a service, but for now:
  
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
        fetchDashboardStats() // Refresh stats
        // Optionally redirect to the relevant page
    }
  } catch (error) {
     // Error handled globally or by store
     console.error(error)
  }
}

const fetchNotifications = async () => {
  try {
    const [notifsRes, countRes] = await Promise.all([
      NotificationService.getNotifications(),
      NotificationService.getUnreadCount()
    ])
    notifications.value = notifsRes.data
    unreadCount.value = countRes.data
  } catch (error) {
    console.error('Failed to fetch notifications', error)
  }
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    fetchNotifications()
  }
}

const markAllRead = async () => {
  try {
    await NotificationService.markAllAsRead()
    unreadCount.value = 0
    notifications.value.forEach(n => n.isRead = true)
  } catch (error) {
    console.error('Failed to mark all read', error)
  }
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('tr-TR', { hour: '2-digit', minute: '2-digit' }).format(date)
}

onMounted(() => {
  fetchNotifications()
  fetchDashboardStats()
  // Poll every 30 seconds for notifications only
  pollingInterval = setInterval(() => {
    fetchNotifications()
  }, 30000)
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
})

const handleLogout = async () => {
  try {
    await AuthService.logout()
    authStore.logout() // Clear local store
    router.push('/login')
  } catch (error) {
    console.error('Logout failed:', error)
  }
}

const currentRouteName = computed(() => {
  const map = {
    'Shops': 'Dükkanlar',
    'Farms': 'Çiftlikler',
    'Factories': 'Fabrikalar',
    'Mines': 'Madenler',
    'Gardens': 'Bahçeler'
  }
  return map[route.name] || route.name
})

const businessItems = [
  { label: 'Dükkanlar', path: '/home/shops', icon: BuildingStorefrontIcon },
  { label: 'Bahçeler', path: '/home/gardens', icon: HomeIcon },
  { label: 'Çiftlikler', path: '/home/farms', icon: TrophyIcon },
  { label: 'Fabrikalar', path: '/home/factories', icon: BeakerIcon },
  { label: 'Madenler', path: '/home/mines', icon: MapIcon },
]

const marketItems = [
  { label: 'Pazar Yeri', path: '/home/market', icon: ArrowTrendingUpIcon },
]

const socialItems = [
  { label: 'Davet Et', path: '#', icon: UsersIcon },
  { label: 'Gazete', path: '#', icon: NewspaperIcon },
]
</script>
