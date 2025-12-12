<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 z-50 lg:hidden safe-area-bottom">
    <div class="flex items-center justify-around h-16">
      <RouterLink 
        v-for="item in navItems" 
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center justify-center flex-1 h-full py-2 transition-colors touch-target"
        :class="[
          isActive(item.path, item.exact) 
            ? 'text-primary-600' 
            : 'text-slate-500 hover:text-slate-700'
        ]"
      >
        <component 
          :is="item.icon" 
          class="w-6 h-6 mb-1"
          :class="isActive(item.path, item.exact) ? 'text-primary-600' : 'text-slate-400'"
        />
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </RouterLink>
    </div>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { 
  HomeIcon,
  BuildingStorefrontIcon,
  ShoppingCartIcon,
  ArchiveBoxIcon,
  UserIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()

const navItems = [
  { label: 'Ana Sayfa', path: '/home', icon: HomeIcon, exact: true },
  { label: 'Dükkanlar', path: '/home/shops', icon: BuildingStorefrontIcon },
  { label: 'Pazar', path: '/home/market', icon: ShoppingCartIcon },
  { label: 'Envanter', path: '/home/inventory', icon: ArchiveBoxIcon },
  { label: 'Profil', path: '/home/profile', icon: UserIcon },
]

const isActive = (path, exact = false) => {
  if (exact) {
    return route.path === path
  }
  return route.path.startsWith(path)
}
</script>

