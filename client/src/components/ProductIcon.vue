<template>
  <div :class="[containerClass, 'flex items-center justify-center rounded-lg bg-slate-50 border border-slate-100 shadow-sm']">
    <span :class="textSizeClass" role="img" :aria-label="name">{{ emoji }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import emojiMap from '../assets/emoji-map.json'

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'md' // sm, md, lg, xl
  }
})

const containerClass = computed(() => {
  switch (props.size) {
    case 'sm': return 'w-8 h-8'
    case 'md': return 'w-10 h-10'
    case 'lg': return 'w-12 h-12'
    case 'xl': return 'w-16 h-16'
    default: return 'w-10 h-10'
  }
})

const textSizeClass = computed(() => {
  switch (props.size) {
    case 'sm': return 'text-lg'
    case 'md': return 'text-xl'
    case 'lg': return 'text-2xl'
    case 'xl': return 'text-3xl'
    default: return 'text-xl'
  }
})

const emoji = computed(() => {
  if (!props.name) return '📦'
  
  const searchName = props.name.toLowerCase().trim()
  
  // 1. Exact Match
  if (emojiMap[searchName]) {
    return emojiMap[searchName]
  }

  // 2. Partial Match (Map key is inside Product Name)
  // e.g. Product: "Demir Cevheri", Map Key: "demir" -> Match
  const partialKey = Object.keys(emojiMap).find(key => searchName.includes(key))
  if (partialKey) {
    return emojiMap[partialKey]
  }

  // 3. Reverse Partial Match (Product Name is inside Map Key - less likely but possible)
  // e.g. Product: "Elma", Map Key: "kırmızı elma" -> Match
  // const reverseKey = Object.keys(emojiMap).find(key => key.includes(searchName))
  // if (reverseKey) {
  //   return emojiMap[reverseKey]
  // }

  return '📦'
})

</script>
