<template>
  <div class="relative inline-flex items-center" :title="`Kalite: ${score}/100`">
    <!-- Background Stars (Gray) -->
    <div class="flex text-gray-300">
      <svg v-for="i in 5" :key="`bg-${i}`" :class="sizeClass" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
      </svg>
    </div>
    
    <!-- Foreground Stars (Yellow) - Clipped -->
    <div class="absolute top-0 left-0 flex text-amber-400 overflow-hidden" :style="{ width: `${score}%` }">
      <div class="flex"> <!-- Wrapper to keep stars fixed width regardless of parent clipping -->
        <svg v-for="i in 5" :key="`fg-${i}`" :class="sizeClass" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  score: {
    type: Number,
    required: true,
    default: 0
  },
  size: {
    type: String,
    default: 'md' // sm, md, lg
  }
})

const sizeClass = computed(() => {
  switch (props.size) {
    case 'xs': return 'w-3 h-3'
    case 'sm': return 'w-4 h-4'
    case 'lg': return 'w-6 h-6'
    default: return 'w-5 h-5'
  }
})
</script>
