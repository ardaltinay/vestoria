<template>
  <div class="p-6 max-w-6xl mx-auto">
    <div class="mb-8 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">Sosyal</h1>
        <p class="text-slate-500">Arkadaşlarını davet et ve gelişmelerden haberdar ol</p>
      </div>
      <button 
        v-if="isAdmin"
        @click="showCreateModal = true"
        class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors flex items-center gap-2"
      >
        <PlusIcon class="w-5 h-5" />
        Duyuru Ekle
      </button>
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

    <!-- Create Announcement Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
      <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 bg-gray-900 bg-opacity-75 transition-opacity" aria-hidden="true" @click="showCreateModal = false"></div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4" id="modal-title">Yeni Duyuru Ekle</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700">Başlık</label>
                <input v-model="newAnnouncement.title" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm">
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700">Tür</label>
                <select v-model="newAnnouncement.type" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm">
                  <option value="NEWS">Haber</option>
                  <option value="UPDATE">Güncelleme</option>
                  <option value="EVENT">Etkinlik</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700">İçerik</label>
                <textarea v-model="newAnnouncement.content" rows="4" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"></textarea>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button @click="createAnnouncement" type="button" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm">
              Yayınla
            </button>
            <button @click="showCreateModal = false" type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
              İptal
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserPlusIcon, EnvelopeIcon, NewspaperIcon, PlusIcon } from '@heroicons/vue/24/outline'
import AnnouncementService from '../services/AnnouncementService'
import { useAuthStore } from '../stores/authStore'
import { useToast } from '../composables/useToast'

const authStore = useAuthStore()
const { addToast } = useToast()
const inviteEmail = ref('')
const announcements = ref([])
const loading = ref(true)
const showCreateModal = ref(false)
const newAnnouncement = ref({
  title: '',
  type: 'NEWS',
  content: ''
})

const isAdmin = computed(() => authStore.user?.isAdmin)

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
    const response = await AnnouncementService.getActiveAnnouncements()
    announcements.value = response.data
  } catch (error) {
    console.error('Failed to fetch announcements', error)
  } finally {
    loading.value = false
  }
}

const createAnnouncement = async () => {
  try {
    await AnnouncementService.createAnnouncement(newAnnouncement.value)
    addToast('Duyuru başarıyla oluşturuldu', 'success')
    showCreateModal.value = false
    newAnnouncement.value = { title: '', type: 'NEWS', content: '' }
    fetchAnnouncements()
  } catch (error) {
    console.error('Failed to create announcement', error)
    addToast('Duyuru oluşturulamadı: ' + (error.response?.data || error.message), 'error')
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>
