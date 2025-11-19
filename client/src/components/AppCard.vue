<template>
  <div class="bg-white rounded-xl border border-slate-200 shadow-card overflow-hidden transition-all duration-300 hover:shadow-soft">
    <div v-if="$slots.header" class="px-5 py-4 border-b border-slate-100 bg-slate-50/50">
      <slot name="header" />
    </div>
    <div class="p-5">
      <slot />
    </div>
    <div v-if="$slots.footer" class="px-5 py-3 bg-slate-50 border-t border-slate-100">
      <slot name="footer" />
    </div>
  </div>
</template>
