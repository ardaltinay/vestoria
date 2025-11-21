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
            <RouterLink 
              v-for="item in socialItems" 
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
              aria-label="Notifications"
              class="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center text-slate-600 hover:bg-slate-50 hover:text-indigo-600 transition-colors relative"
            >
              <BellIcon class="w-6 h-6" />
              <span v-if="unreadCount > 0" class="absolute top-2 right-2 w-2 h-2 bg-rose-500 rounded-full border border-white"></span>
            </button>

            <!-- Notification Dropdown -->
            <div
              v-if="showNotifications"
              ref="notificationDropdown"
              class="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-lg border border-slate-100 py-2 z-50 overflow-hidden"
            >
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
              aria-label="User Menu"
              class="flex items-center gap-2 text-sm font-medium text-slate-700 hover:text-slate-900"
            >
              <span class="hidden sm:block">Hesabım</span>
              <ChevronDownIcon class="w-4 h-4 text-slate-400" />
            </button>

            <div v-if="showUserMenu" ref="userMenuDropdown" class="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-lg border border-slate-200 z-50 overflow-hidden py-1">
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
          <RouterView v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </RouterView>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

import { 
  BuildingStorefrontIcon,
  NewspaperIcon,
  Bars3Icon,
  BellIcon,
  ChevronRightIcon,
  ChevronDownIcon,
  ArrowRightIcon,
  WrenchScrewdriverIcon,
  ShoppingCartIcon,
  SunIcon,
  Cog6ToothIcon,
  SparklesIcon,
  ArchiveBoxIcon,
  TruckIcon
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
import UserService from '../services/UserService'
import { useToast } from '../composables/useToast'

const showNotifications = ref(false)
const showUserMenu = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const { addToast } = useToast()

let pollingInterval = null

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

const notificationDropdown = ref(null)
const userMenuDropdown = ref(null)

const handleClickOutside = (event) => {
  // Close Notifications
  if (showNotifications.value && notificationDropdown.value && !notificationDropdown.value.contains(event.target) && !event.target.closest('button[aria-label="Notifications"]')) {
    showNotifications.value = false
  }
  // Close User Menu
  if (showUserMenu.value && userMenuDropdown.value && !userMenuDropdown.value.contains(event.target) && !event.target.closest('button[aria-label="User Menu"]')) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  fetchNotifications()
  // Poll every 30 seconds for notifications only
  pollingInterval = setInterval(() => {
    fetchNotifications()
  }, 30000)
  
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
  document.removeEventListener('click', handleClickOutside)
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
    'Gardens': 'Bahçeler',
    'Marketplace': 'Pazar Yeri',
    'Social': 'Sosyal'
  }
  return map[route.name] || route.name
})

const businessItems = [
  { label: 'Dükkanlar', path: '/home/shops', icon: BuildingStorefrontIcon },
  { label: 'Bahçeler', path: '/home/gardens', icon: ArchiveBoxIcon },
  { label: 'Çiftlikler', path: '/home/farms', icon: TruckIcon },
  { label: 'Fabrikalar', path: '/home/factories', icon: Cog6ToothIcon },
  { label: 'Madenler', path: '/home/mines', icon: WrenchScrewdriverIcon },
]

const marketItems = [
  { label: 'Pazar Yeri', path: '/home/market', icon: ShoppingCartIcon },
]

const socialItems = [
  { label: 'Gazete', path: '/home/social', icon: NewspaperIcon },
]
</script>
