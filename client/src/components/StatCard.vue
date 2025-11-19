<template>
  <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm flex items-center gap-4 transition-transform hover:-translate-y-1">
    <div :class="['w-12 h-12 rounded-lg flex items-center justify-center shrink-0', colorClasses[color]]">
      <slot name="icon" />
    </div>
    <div>
      <div class="text-sm font-medium text-slate-500">{{ label }}</div>
      <div class="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">{{ value }}</div>
      <div v-if="subtext" class="text-xs mt-0.5" :class="trend === 'up' ? 'text-emerald-600' : trend === 'down' ? 'text-rose-600' : 'text-slate-400'">
        {{ subtext }}
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  subtext: {
    type: String,
    default: ''
  },
  trend: {
    type: String,
    default: 'neutral', // 'up', 'down', 'neutral'
  },
  color: {
    type: String,
    default: 'primary',
    validator: (val) => ['primary', 'secondary', 'accent', 'danger', 'slate'].includes(val)
  }
})

const colorClasses = {
  primary: 'bg-primary-50 text-primary-600',
  secondary: 'bg-secondary-50 text-secondary-600',
  accent: 'bg-accent-50 text-accent-600',
  danger: 'bg-danger-50 text-danger-600',
  slate: 'bg-slate-100 text-slate-600'
}
</script>
