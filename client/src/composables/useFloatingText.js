import { ref } from 'vue';

const items = ref([]);
let nextId = 0;

export function useFloatingText() {
  const addFloatingText = (text, x, y, options = {}) => {
    const id = nextId++;
    items.value.push({ id, text, x, y, ...options });

    // Auto remove after animation
    setTimeout(() => {
      items.value = items.value.filter(i => i.id !== id);
    }, 1500);
  };

  return {
    items,
    addFloatingText
  };
}
