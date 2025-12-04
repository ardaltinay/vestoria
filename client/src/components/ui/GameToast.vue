<template>
  <div class="fixed top-20 right-4 z-[9999] flex flex-col gap-3 pointer-events-none w-full max-w-sm sm:w-auto">
    <TransitionGroup 
      enter-active-class="transform ease-out duration-300 transition" 
      enter-from-class="translate-x-8 opacity-0" 
      enter-to-class="translate-x-0 opacity-100" 
      leave-active-class="transition ease-in duration-200" 
      leave-from-class="opacity-100" 
      leave-to-class="translate-x-8 opacity-0"
    >
      <div 
        v-for="toast in toasts" 
        :key="toast.id"
        class="pointer-events-auto relative overflow-hidden rounded-xl shadow-2xl backdrop-blur-md border border-white/10 min-w-[300px]"
        :class="{
          'bg-slate-900/95': true,
          'shadow-emerald-500/20': toast.type === 'success',
          'shadow-rose-500/20': toast.type === 'error',
          'shadow-amber-500/20': toast.type === 'warning',
          'shadow-blue-500/20': toast.type === 'info'
        }"
      >
        <!-- Progress Bar (Optional, for duration) -->
        <div class="absolute bottom-0 left-0 h-1 bg-white/20 w-full">
          <div 
            class="h-full transition-all duration-[3000ms] ease-linear w-0"
            :class="{
              'bg-emerald-500': toast.type === 'success',
              'bg-rose-500': toast.type === 'error',
              'bg-amber-500': toast.type === 'warning',
              'bg-blue-500': toast.type === 'info',
              'w-full': true // We would need to animate this width from 100 to 0 actually
            }"
          ></div>
        </div>

        <div class="p-4 flex items-start gap-4">
          <!-- Icon Container -->
          <div 
            class="flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center shadow-inner"
            :class="{
              'bg-emerald-500/20 text-emerald-400': toast.type === 'success',
              'bg-rose-500/20 text-rose-400': toast.type === 'error',
              'bg-amber-500/20 text-amber-400': toast.type === 'warning',
              'bg-blue-500/20 text-blue-400': toast.type === 'info'
            }"
          >
            <component :is="getIcon(toast.type)" class="h-6 w-6" aria-hidden="true" />
          </div>

          <!-- Content -->
          <div class="flex-1 pt-0.5">
            <h3 
              class="text-sm font-black uppercase tracking-wider mb-0.5"
              :class="{
                'text-emerald-400': toast.type === 'success',
                'text-rose-400': toast.type === 'error',
                'text-amber-400': toast.type === 'warning',
                'text-blue-400': toast.type === 'info'
              }"
            >
              {{ getTitle(toast.type) }}
            </h3>
            <p class="text-sm font-medium text-slate-300 leading-snug">{{ toast.message }}</p>
          </div>

          <!-- Close Button -->
          <button 
            @click="removeToast(toast.id)" 
            class="flex-shrink-0 text-slate-500 hover:text-white transition-colors"
          >
            <XMarkIcon class="h-5 w-5" />
          </button>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useToast } from '../../composables/useToast'
import { CheckCircleIcon, XCircleIcon, InformationCircleIcon, ExclamationTriangleIcon, XMarkIcon } from '@heroicons/vue/24/outline'

const { toasts, removeToast } = useToast()

const getIcon = (type) => {
  switch (type) {
    case 'success': return CheckCircleIcon
    case 'error': return XCircleIcon
    case 'warning': return ExclamationTriangleIcon
    default: return InformationCircleIcon
  }
}

const getColor = (type) => {
  switch (type) {
    case 'success': return 'text-emerald-500'
    case 'error': return 'text-rose-500'
    case 'warning': return 'text-amber-500'
    default: return 'text-blue-500'
  }
}

const getTitle = (type) => {
  switch (type) {
    case 'success': return 'Başarılı'
    case 'error': return 'Hata'
    case 'warning': return 'Uyarı'
    default: return 'Bilgi'
  }
}
</script>
