<template>
  <svg 
    :width="size" 
    :height="size" 
    viewBox="0 0 24 24" 
    fill="none" 
    xmlns="http://www.w3.org/2000/svg"
    :class="className"
  >
    <!-- Hexagonal coin shape -->
    <path 
      d="M12 2L20.66 7V17L12 22L3.34 17V7L12 2Z" 
      :fill="bgColor"
      :stroke="strokeColor"
      stroke-width="1.5"
      stroke-linejoin="round"
    />
    
    <!-- V letter -->
    <path 
      d="M8 8L12 16L16 8" 
      :stroke="iconColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    
    <!-- P letter -->
    <path 
      d="M12 16V11M12 11H14.5C15.3284 11 16 11.6716 16 12.5C16 13.3284 15.3284 14 14.5 14H12V11Z" 
      :stroke="iconColor"
      stroke-width="1.8"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  size: {
    type: [Number, String],
    default: 20
  },
  variant: {
    type: String,
    default: 'default', // 'default', 'success', 'primary', 'muted'
    validator: (value) => ['default', 'success', 'primary', 'muted'].includes(value)
  },
  className: {
    type: String,
    default: ''
  }
})

const bgColor = computed(() => {
  switch (props.variant) {
    case 'success':
      return '#10b981' // emerald-500
    case 'primary':
      return '#3b82f6' // blue-500
    case 'muted':
      return '#94a3b8' // slate-400
    default:
      return '#f59e0b' // amber-500 (gold color for currency)
  }
})

const strokeColor = computed(() => {
  switch (props.variant) {
    case 'success':
      return '#059669' // emerald-600
    case 'primary':
      return '#2563eb' // blue-600
    case 'muted':
      return '#64748b' // slate-500
    default:
      return '#d97706' // amber-600
  }
})

const iconColor = computed(() => {
  return '#ffffff' // white for contrast
})
</script>
