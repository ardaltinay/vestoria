<template>
  <div class="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
    <div class="w-full max-w-md">
      <!-- Logo -->
      <div class="flex justify-center mb-8">
        <Logo size="xl" />
      </div>

      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-xl border border-slate-200 p-8">
        <div class="text-center mb-8">
          <h2 class="text-2xl font-bold text-slate-900">Hoşgeldiniz</h2>
          <p class="text-slate-500 mt-2">Hesabınıza giriş yaparak imparatorluğunuzu yönetmeye devam edin.</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleLogin">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Kullanıcı Adı</label>
            <input 
              v-model="form.username"
              type="text" 
              class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all"
              :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.username }"
              placeholder="Kullanıcı adınız"
            />
            <p v-if="errors.username" class="text-xs text-red-500 mt-1">{{ errors.username }}</p>
          </div>
          
          <div>
            <div class="flex justify-between items-center mb-1">
              <label class="block text-sm font-medium text-slate-700">Şifre</label>
              <a href="#" class="text-xs font-medium text-primary-600 hover:text-primary-700">Şifremi Unuttum?</a>
            </div>
            <input 
              v-model="form.password"
              type="password" 
              class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all"
              :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.password }"
              placeholder="••••••••"
            />
            <p v-if="errors.password" class="text-xs text-red-500 mt-1">{{ errors.password }}</p>
          </div>

          <AppButton variant="primary" block size="lg" type="submit" class="mt-6" :loading="loading">
            Giriş Yap
          </AppButton>
        </form>

        <div class="mt-6 text-center text-sm text-slate-500">
          Hesabınız yok mu? 
          <router-link to="/register" class="font-medium text-primary-600 hover:text-primary-700">Hemen Kayıt Olun</router-link>
        </div>
      </div>
      
      <div class="mt-8 text-center">
        <router-link to="/" class="text-sm text-slate-400 hover:text-slate-600 flex items-center justify-center gap-2">
          <ArrowLeftIcon class="w-4 h-4" />
          Ana Sayfaya Dön
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppButton from '../components/AppButton.vue'
import Logo from '../components/Logo.vue'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import { useRouter } from 'vue-router'
import AuthService from '../services/AuthService'
import { useAuthStore } from '../stores/authStore'
import { useToast } from '../composables/useToast'

const router = useRouter()
const authStore = useAuthStore()
const { addToast } = useToast()

const form = ref({
  username: '',
  password: ''
})

const errors = ref({
  username: '',
  password: ''
})

const loading = ref(false)

const validate = () => {
  let isValid = true
  errors.value = { username: '', password: '' }

  if (!form.value.username) {
    errors.value.username = 'Kullanıcı adı zorunludur'
    isValid = false
  }

  if (!form.value.password) {
    errors.value.password = 'Şifre zorunludur'
    isValid = false
  }

  return isValid
}

async function handleLogin() {
  if (!validate()) return

  try {
    loading.value = true
    // Use authStore directly to handle state and API call in one place
    await authStore.login(form.value)
    router.push('/home')
  } catch (error) {
    console.error('Login failed:', error)
    addToast('Giriş başarısız. Kullanıcı adı veya şifre hatalı.', 'error')
  } finally {
    loading.value = false
  }
}
</script>
