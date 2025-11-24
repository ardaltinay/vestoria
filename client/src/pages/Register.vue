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
          <h2 class="text-2xl font-bold text-slate-900">Aramıza Katılın</h2>
          <p class="text-slate-500 mt-2">Yeni bir hesap oluşturun ve ticaret dünyasına adım atın.</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleRegister">
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
            <label class="block text-sm font-medium text-slate-700 mb-1">E-posta Adresi</label>
            <input 
              v-model="form.email"
              type="email" 
              class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all"
              :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.email }"
              placeholder="ornek@email.com"
            />
            <p v-if="errors.email" class="text-xs text-red-500 mt-1">{{ errors.email }}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Şifre</label>
            <input 
              v-model="form.password"
              type="password" 
              class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all"
              :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.password }"
              placeholder="••••••••"
            />
            <p v-if="errors.password" class="text-xs text-red-500 mt-1">{{ errors.password }}</p>
            <p v-else class="text-xs text-slate-400 mt-1">En az 8 karakter, 1 büyük, 1 küçük harf, 1 rakam ve 1 özel karakter.</p>
          </div>

          <AppButton variant="primary" block size="lg" type="submit" class="mt-6" :loading="loading">
            Kayıt Ol
          </AppButton>
        </form>

        <div class="mt-6 text-center text-sm text-slate-500">
          Zaten hesabınız var mı? 
          <router-link to="/login" class="font-medium text-primary-600 hover:text-primary-700">Giriş Yapın</router-link>
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
import { ref, computed } from 'vue'
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
  email: '',
  password: ''
})

const errors = ref({
  username: '',
  email: '',
  password: ''
})

const loading = ref(false)

const validate = () => {
  let isValid = true
  errors.value = { username: '', email: '', password: '' }

  // Username validation
  if (!form.value.username) {
    errors.value.username = 'Kullanıcı adı zorunludur'
    isValid = false
  } else if (form.value.username.length < 4) {
    errors.value.username = 'Kullanıcı adı en az 4 karakter olmalıdır'
    isValid = false
  }

  // Email validation
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.value.email) {
    errors.value.email = 'E-posta adresi zorunludur'
    isValid = false
  } else if (!emailRegex.test(form.value.email)) {
    errors.value.email = 'Geçerli bir e-posta adresi giriniz'
    isValid = false
  }

  // Password validation
  const passwordRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._,-]).{8,}$/
  if (!form.value.password) {
    errors.value.password = 'Şifre zorunludur'
    isValid = false
  } else if (form.value.password.length < 8) {
    errors.value.password = 'Şifre en az 8 karakter olmalıdır'
    isValid = false
  } else if (!passwordRegex.test(form.value.password)) {
    errors.value.password = 'Şifre en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir'
    isValid = false
  }

  return isValid
}

async function handleRegister() {
  if (!validate()) return

  try {
    loading.value = true
    await authStore.register(form.value)
    router.push('/home')
  } catch (error) {
    console.error('Registration failed:', error)
    if (error.response?.data?.message) {
      // Handle backend validation errors if any
      addToast(error.response.data.message, 'error')
    } else {
      addToast('Kayıt işlemi başarısız oldu. Lütfen tekrar deneyiniz.', 'error')
    }
  } finally {
    loading.value = false
  }
}
</script>
