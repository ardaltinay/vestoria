
<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import Toast from './components/ui/Toast.vue'
import { useGameDataStore } from './stores/gameDataStore'

const gameDataStore = useGameDataStore()

onMounted(() => {
  gameDataStore.fetchGameData()
})
</script>

<template>
  <Toast />
  <RouterView />
</template>

<!-- Tüm stiller Tailwind ile sağlanıyor -->
