<template>
  <div class="min-h-screen bg-slate-50 flex flex-col">
    <!-- Navbar -->
    <nav class="w-full py-6 px-4 sm:px-8 flex justify-between items-center max-w-7xl mx-auto">
      <Logo size="md" />
      <div class="flex gap-4">
        <AppButton variant="ghost" @click="$router.push('/login')">Giriş Yap</AppButton>
        <AppButton variant="primary" @click="$router.push('/register')">Kayıt Ol</AppButton>
      </div>
    </nav>

    <!-- Hero Section -->
    <main class="flex-1 flex items-center justify-center px-4 sm:px-8 py-12">
      <div class="max-w-4xl mx-auto text-center">
        <div class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary-50 text-primary-700 text-sm font-medium mb-8 animate-fade-in-up">
          <span class="relative flex h-2 w-2">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-primary-500"></span>
          </span>
          Yeni Sezon Başladı
        </div>
        
        <h1 class="text-4xl sm:text-6xl font-bold text-slate-900 tracking-tight mb-6 leading-tight">
          Ekonomik İmparatorluğunu <br/>
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-primary-600 to-accent-500">İnşa Etmeye Başla</span>
        </h1>
        
        <p class="text-lg sm:text-xl text-slate-600 mb-10 max-w-2xl mx-auto leading-relaxed">
          Şirketini kur, fabrikalarını yönet ve ticaret ağını genişlet. 
          Gerçek zamanlı ekonomik strateji oyunu ile rakiplerini geride bırak.
        </p>
        
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
          <AppButton variant="primary" size="lg" @click="$router.push('/register')" class="w-full sm:w-auto shadow-xl shadow-primary-500/20">
            Hemen Başla
            <template #icon-right>
              <ArrowRightIcon class="w-5 h-5" />
            </template>
          </AppButton>
          <AppButton variant="outline" size="lg" @click="$router.push('/login')" class="w-full sm:w-auto bg-white">
            Giriş Yap
          </AppButton>
        </div>

        <!-- Features Grid -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-8 mt-20 text-left">
          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <div class="w-12 h-12 bg-emerald-100 rounded-xl flex items-center justify-center text-emerald-600 mb-4">
              <ChartBarIcon class="w-6 h-6" />
            </div>
            <h3 class="font-bold text-slate-900 text-lg mb-2">Gerçek Ekonomi</h3>
            <p class="text-slate-500">Dinamik piyasa koşullarına göre stratejini belirle ve karını maksimize et.</p>
          </div>
          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <div class="w-12 h-12 bg-amber-100 rounded-xl flex items-center justify-center text-amber-600 mb-4">
              <BuildingOffice2Icon class="w-6 h-6" />
            </div>
            <h3 class="font-bold text-slate-900 text-lg mb-2">Sanayi Devrimi</h3>
            <p class="text-slate-500">Fabrikalar kur, üretim hatlarını optimize et ve hammaddeyi işleyerek değer kat.</p>
          </div>
          <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center text-blue-600 mb-4">
              <GlobeAltIcon class="w-6 h-6" />
            </div>
            <h3 class="font-bold text-slate-900 text-lg mb-2">Global Ticaret</h3>
            <p class="text-slate-500">Diğer oyuncularla ticaret yap, ittifaklar kur ve dünya pazarında söz sahibi ol.</p>
          </div>
        </div>
      </div>
    </main>

    <footer class="py-8 text-center text-slate-400 text-sm">
      &copy; 2025 Vestoria. Tüm hakları saklıdır.
    </footer>
  </div>
</template>

<script setup>
import AppButton from '../components/AppButton.vue'
import Logo from '../components/Logo.vue'
import { ArrowRightIcon, ChartBarIcon, BuildingOffice2Icon, GlobeAltIcon } from '@heroicons/vue/24/outline'
</script>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
