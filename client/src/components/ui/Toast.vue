<script setup>
import { useToast } from '../../composables/useToast';

const { toasts, removeToast } = useToast();

const getTypeClasses = (type) => {
  switch (type) {
    case 'success':
      return 'bg-green-500 text-white';
    case 'error':
      return 'bg-red-500 text-white';
    case 'warning':
      return 'bg-yellow-500 text-white';
    default:
      return 'bg-blue-500 text-white';
  }
};
</script>

<template>
  <div class="fixed top-4 right-4 z-50 flex flex-col gap-2">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="px-4 py-3 rounded shadow-lg flex items-center justify-between min-w-[300px] cursor-pointer"
        :class="getTypeClasses(toast.type)"
        @click="removeToast(toast.id)"
      >
        <span>{{ toast.message }}</span>
        <span class="ml-4 text-sm opacity-75 hover:opacity-100">✕</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
