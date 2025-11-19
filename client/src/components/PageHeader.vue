<template>
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
    <div>
      <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">{{ title }}</h1>
      <p v-if="description" class="mt-1 text-slate-500 text-sm sm:text-base">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="flex items-center gap-3">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  }
})
</script>
