<template>
  <div class="fixed inset-0 pointer-events-none z-[9999] overflow-hidden">
    <div
      v-for="item in items"
      :key="item.id"
      class="absolute flex items-center gap-1 font-black text-2xl select-none"
      :style="{
        left: item.x + 'px',
        top: item.y + 'px',
        color: item.color || '#fbbf24',
        textShadow: '0 2px 4px rgba(0,0,0,0.3)'
      }"
      v-motion
      :initial="{ opacity: 0, y: 0, scale: 0.5 }"
      :enter="{ opacity: 1, y: -50, scale: 1.2, transition: { duration: 200 } }"
      :leave="{ opacity: 0, y: -100, scale: 1, transition: { duration: 500, delay: 500 } }"
    >
      <span v-if="item.icon">{{ item.icon }}</span>
      {{ item.text }}
    </div>
  </div>
</template>

<script setup>
import { useFloatingText } from '../../composables/useFloatingText'

const { items } = useFloatingText()
</script>
