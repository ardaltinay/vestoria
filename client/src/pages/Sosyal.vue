<template>
  <div class="p-6 max-w-6xl mx-auto">
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-slate-900">Sosyal</h1>
      <p class="text-slate-500">Arkadaşlarını davet et ve gelişmelerden haberdar ol</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Invite Section -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 sticky top-6">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600">
              <UserPlusIcon class="w-6 h-6" />
            </div>
            <div>
              <h2 class="font-bold text-slate-900">Arkadaşını Davet Et</h2>
              <p class="text-xs text-slate-500">Birlikte büyüyün</p>
            </div>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">E-posta Adresi</label>
              <input 
                v-model="inviteEmail"
                type="email" 
                placeholder="ornek@email.com"
                class="w-full px-4 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
              >
            </div>
            
            <button 
              @click="handleInvite"
              :disabled="!isValidEmail"
              class="w-full py-3 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-xl font-bold transition-colors flex items-center justify-center gap-2"
            >
              <EnvelopeIcon class="w-5 h-5" />
              Davet Gönder
            </button>
            
            <p class="text-xs text-slate-400 text-center">
              Davet linki e-posta uygulamanız üzerinden gönderilecektir.
            </p>
          </div>
        </div>
      </div>

      <!-- Newspaper Section -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600">
                <NewspaperIcon class="w-6 h-6" />
              </div>
              <div>
                <h2 class="font-bold text-slate-900">Vestoria Gazetesi</h2>
                <p class="text-xs text-slate-500">Son gelişmeler ve duyurular</p>
              </div>
            </div>
            <div class="text-xs font-mono text-slate-400">SAYI #{{ announcements.length + 100 }}</div>
          </div>

          <div class="divide-y divide-slate-100">
            <div v-if="loading" class="p-8 text-center text-slate-400">
              Yükleniyor...
            </div>
            
            <div v-else-if="announcements.length === 0" class="p-12 text-center">
              <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-400">
                <NewspaperIcon class="w-8 h-8" />
              </div>
              <h3 class="text-slate-900 font-medium mb-1">Henüz haber yok</h3>
              <p class="text-slate-500 text-sm">Şimdilik her şey sakin görünüyor.</p>
            </div>

            <div v-for="news in announcements" :key="news.id" class="p-6 hover:bg-slate-50 transition-colors group">
              <div class="flex items-start gap-4">
                <!-- Type Badge -->
                <div 
                  class="shrink-0 px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider border"
                  :class="{
                    'bg-blue-50 text-blue-600 border-blue-100': news.type === 'UPDATE',
                    'bg-emerald-50 text-emerald-600 border-emerald-100': news.type === 'NEWS',
                    'bg-purple-50 text-purple-600 border-purple-100': news.type === 'EVENT'
                  }"
                >
                  {{ getTypeLabel(news.type) }}
                </div>
                
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-2">
                    <h3 class="font-bold text-slate-900 text-lg group-hover:text-indigo-600 transition-colors">
                      {{ news.title }}
                    </h3>
                    <span class="text-xs text-slate-400 whitespace-nowrap ml-4">
                      {{ formatDate(news.createdAt) }}
                    </span>
                  </div>
                  <p class="text-slate-600 leading-relaxed text-sm whitespace-pre-line">
                    {{ news.content }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserPlusIcon, EnvelopeIcon, NewspaperIcon } from '@heroicons/vue/24/outline'
import api from '../services/api'

const inviteEmail = ref('')
const announcements = ref([])
const loading = ref(true)

const isValidEmail = computed(() => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(inviteEmail.value)
})

const getTypeLabel = (type) => {
  switch(type) {
    case 'UPDATE': return 'GÜNCELLEME'
    case 'NEWS': return 'HABER'
    case 'EVENT': return 'ETKİNLİK'
    default: return type
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('tr-TR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const handleInvite = () => {
  if (!isValidEmail.value) return
  
  const subject = encodeURIComponent("Vestoria'ya Davet")
  const body = encodeURIComponent(`Merhaba,\n\nSeni Vestoria dünyasına davet ediyorum! Kendi imparatorluğunu kurmak için hemen katıl.\n\nhttps://vestoria.io`)
  
  window.location.href = `mailto:${inviteEmail.value}?subject=${subject}&body=${body}`
  
  inviteEmail.value = ''
}

const fetchAnnouncements = async () => {
  try {
    loading.value = true
    const response = await api.get('/announcements')
    announcements.value = response.data
  } catch (error) {
    console.error('Failed to fetch announcements', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>
