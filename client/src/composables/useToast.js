import { ref } from 'vue';
import SoundService from '../services/SoundService';

const toasts = ref([]);

export function useToast() {
  const addToast = (message, type = 'info', duration = 3000) => {
    const id = Date.now();
    toasts.value.push({ id, message, type });

    // Play sound based on type
    if (type === 'success') SoundService.play('success');
    else if (type === 'error') SoundService.play('error');
    else SoundService.play('notification');

    setTimeout(() => {
      removeToast(id);
    }, duration);
  };

  const removeToast = (id) => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  };

  return {
    toasts,
    addToast,
    removeToast
  };
}
