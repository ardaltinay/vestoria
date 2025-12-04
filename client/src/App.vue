
<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import GameToast from './components/ui/GameToast.vue'
import FloatingText from './components/ui/FloatingText.vue'
import { useGameDataStore } from './stores/gameDataStore'

const gameDataStore = useGameDataStore()

onMounted(() => {
  gameDataStore.fetchGameData()
})
</script>

<template>
  <GameToast />
  <FloatingText />
  <RouterView />
</template>

<!-- Tüm stiller Tailwind ile sağlanıyor -->
