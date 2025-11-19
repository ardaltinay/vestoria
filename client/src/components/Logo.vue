<template>
  <div class="flex items-center gap-3">
    <!-- Logo Icon -->
    <div :class="['relative flex items-center justify-center', sizeClasses[size]]">
      <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full text-primary-600">
        <!-- Main V Shape -->
        <path d="M20 40L0 0H12L20 20L28 0H40L20 40Z" fill="currentColor" fill-opacity="0.2"/>
        <path d="M20 40L8 10H16L20 22L24 10H32L20 40Z" fill="currentColor"/>
        <!-- Accent/Growth Element -->
        <circle cx="20" cy="8" r="4" class="text-accent-500" fill="currentColor"/>
      </svg>
    </div>
    
    <!-- Logo Text -->
    <span v-if="showText" :class="['font-display font-bold tracking-tight text-slate-900', textClasses[size]]">
      Vestoria
    </span>
  </div>
</template>

<script setup>
defineProps({
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg', 'xl'].includes(value)
  },
  showText: {
    type: Boolean,
    default: true
  }
})

const sizeClasses = {
  sm: 'w-8 h-8',
  md: 'w-10 h-10',
  lg: 'w-12 h-12',
  xl: 'w-16 h-16'
}

const textClasses = {
  sm: 'text-lg',
  md: 'text-xl',
  lg: 'text-2xl',
  xl: 'text-4xl'
}
</script>
