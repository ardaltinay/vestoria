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
            <div class="text-xs text-slate-500">Seviye 1 Girişimci</div>
          </div>
        </div>
        
        <!-- Quick Stats -->
        <div class="grid grid-cols-2 gap-2">
          <div class="bg-white p-2 rounded-lg border border-slate-200 shadow-sm">
            <div class="text-[10px] uppercase tracking-wider text-slate-400 font-semibold">Nakit</div>
            <div class="text-sm font-bold text-emerald-600">₺4.414</div>
          </div>
          <div class="bg-white p-2 rounded-lg border border-slate-200 shadow-sm">
            <div class="text-[10px] uppercase tracking-wider text-slate-400 font-semibold">Prestij</div>
            <div class="text-sm font-bold text-amber-500">★ 24</div>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 overflow-y-auto py-4 px-3 space-y-1">
        <div class="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">İşletmeler</div>
        
        <RouterLink 
          v-for="item in menuItems" 
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

        <div class="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2 mt-6">Sosyal</div>
        <a href="#" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group">
          <UsersIcon class="w-5 h-5 text-slate-400 group-hover:text-slate-600" />
          Davet Et
        </a>
        <a href="#" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group">
          <NewspaperIcon class="w-5 h-5 text-slate-400 group-hover:text-slate-600" />
          Gazete
        </a>
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
          <button class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors relative">
            <BellIcon class="w-6 h-6" />
            <span class="absolute top-2 right-2 w-2 h-2 bg-rose-500 rounded-full border border-white"></span>
          </button>
          <div class="h-8 w-px bg-slate-200 mx-1 hidden sm:block"></div>
          <button class="flex items-center gap-2 text-sm font-medium text-slate-700 hover:text-slate-900">
            <span class="hidden sm:block">Hesabım</span>
            <ChevronDownIcon class="w-4 h-4 text-slate-400" />
          </button>
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
                    <button class="group relative overflow-hidden bg-slate-900 text-white px-6 py-3 rounded-xl font-bold shadow-lg hover:shadow-xl hover:bg-slate-800 transition-all transform hover:-translate-y-0.5">
                      <span class="relative z-10 flex items-center justify-center gap-2">
                        Raporları İncele
                        <ArrowRightIcon class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                      </span>
                    </button>
                    <button class="px-6 py-3 rounded-xl font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors border border-slate-200 bg-white">
                      Yeni Yatırım Yap
                    </button>
                  </div>
                </div>

                <!-- Stats Row -->
                <div class="mt-10 grid grid-cols-2 sm:grid-cols-4 gap-4 pt-8 border-t border-slate-100">
                  <div>
                    <div class="text-slate-400 text-xs uppercase tracking-wider font-semibold mb-1">Günlük Kazanç</div>
                    <div class="text-2xl font-bold text-slate-900">₺12.450</div>
                  </div>
                  <div>
                    <div class="text-slate-400 text-xs uppercase tracking-wider font-semibold mb-1">Aktif İşletme</div>
                    <div class="text-2xl font-bold text-slate-900">8</div>
                  </div>
                  <div>
                    <div class="text-slate-400 text-xs uppercase tracking-wider font-semibold mb-1">Pazar Payı</div>
                    <div class="text-2xl font-bold text-emerald-600 flex items-center gap-1">
                      %4.2
                      <ArrowTrendingUpIcon class="w-4 h-4" />
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
                   <div class="flex items-center gap-3 text-sm">
                     <div class="w-2 h-2 rounded-full bg-emerald-500"></div>
                     <span class="text-slate-600">Çiftlik hasatı tamamlandı</span>
                     <span class="ml-auto text-slate-400 text-xs">2dk önce</span>
                   </div>
                   <div class="flex items-center gap-3 text-sm">
                     <div class="w-2 h-2 rounded-full bg-amber-500"></div>
                     <span class="text-slate-600">Yeni dükkan açıldı</span>
                     <span class="ml-auto text-slate-400 text-xs">1s önce</span>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
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

const route = useRoute()
const isSidebarOpen = ref(false)
const username = ref('Arda') // Mock username

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

const menuItems = [
  { label: 'Dükkanlar', path: '/home/shops', icon: BuildingStorefrontIcon },
  { label: 'Çiftlikler', path: '/home/farms', icon: TrophyIcon }, // Using Trophy as placeholder for Farm
  { label: 'Fabrikalar', path: '/home/factories', icon: BeakerIcon }, // Using Beaker as placeholder for Factory
  { label: 'Madenler', path: '/home/mines', icon: MapIcon }, // Using Map as placeholder for Mine
  { label: 'Bahçeler', path: '/home/gardens', icon: HomeIcon }, // Using Home as placeholder for Garden
]
</script>
