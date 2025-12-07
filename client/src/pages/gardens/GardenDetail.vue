```
<template>
  <div class="min-h-screen bg-stone-50 font-sans selection:bg-amber-200 selection:text-amber-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        <!-- Header Section -->
        <div class="relative z-10 mb-8 bg-white/60 backdrop-blur-md rounded-3xl p-6 border border-white/50 shadow-xl shadow-amber-900/5">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div class="flex items-center gap-5">
              <div class="relative group">
                <div class="absolute inset-0 bg-amber-400 rounded-2xl blur opacity-20 group-hover:opacity-40 transition-opacity duration-500"></div>
                <div class="relative w-20 h-20 bg-gradient-to-br from-stone-600 to-stone-800 rounded-2xl flex items-center justify-center shadow-lg shadow-stone-600/30 transform group-hover:scale-105 transition-transform duration-300 border border-stone-500/50">
                  <span class="text-4xl filter drop-shadow-md">🌻</span>
                </div>
                <!-- Level Badge -->
                <div v-if="item" class="absolute -bottom-2 -right-2 bg-emerald-600 text-white text-xs font-black px-2 py-1 rounded-lg shadow-md border-2 border-white">
                  LVL {{ item.tier === 'SMALL' ? 1 : item?.tier === 'MEDIUM' ? 2 : 3 }}
                </div>
              </div>
              
              <div>
                <h1 class="text-3xl sm:text-4xl font-black text-stone-800 tracking-tight mb-1 drop-shadow-sm">{{ item?.name || 'Bahçe' }}</h1>
                <div class="flex items-center gap-3">
                  <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-amber-100/80 border border-amber-200 text-amber-800 text-xs font-bold uppercase tracking-wide">
                    <span class="w-1.5 h-1.5 rounded-full bg-amber-600"></span>
                    Botanik Bahçe
                  </span>
                </div>
              </div>
            </div>
            
            <RouterLink to="/home/gardens" 
              class="group flex items-center gap-2 px-5 py-3 bg-white border-2 border-amber-100 rounded-2xl text-stone-700 font-bold hover:border-amber-300 hover:text-amber-700 hover:bg-amber-50/50 transition-all shadow-sm hover:shadow-md active:scale-95">
              <svg class="w-5 h-5 transition-transform group-hover:-transtone-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
              </svg>
              Geri Dön
            </RouterLink>
          </div>
        </div>

        <div v-if="!item" class="text-center py-20">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-amber-300 border-t-stone-600 mb-4"></div>
          <div class="text-stone-600 text-lg font-medium">Bahçe verileri yükleniyor...</div>
        </div>

        <div v-else class="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8">
          
          <!-- Left Column: Main Controls (8 cols) -->
          <div class="lg:col-span-8 space-y-6">
            
            <!-- Stats Row -->
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <!-- Tier Card -->
              <div class="bg-white/70 backdrop-blur-sm rounded-2xl p-5 border border-white/60 shadow-lg shadow-amber-200/50 hover:shadow-xl hover:-transtone-y-1 transition-all duration-300 group">
                <div class="flex items-start justify-between mb-3">
                  <div class="p-2 bg-amber-100/50 rounded-xl text-amber-600 group-hover:bg-amber-500 group-hover:text-white transition-colors duration-300">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                    </svg>
                  </div>
                  <span class="text-xs font-bold text-stone-400 uppercase tracking-wider">Kapasite</span>
                </div>
                <div class="text-2xl font-black text-stone-800 mb-1">{{ getItemTierTr(item.tier) }}</div>
                <div class="text-sm font-medium text-stone-600/70">Max: {{ item.maxStock }} Birim</div>
              </div>

              <!-- Stock Card -->
              <div class="bg-white/70 backdrop-blur-sm rounded-2xl p-5 border border-white/60 shadow-lg shadow-amber-200/50 hover:shadow-xl hover:-transtone-y-1 transition-all duration-300 group">
                <div class="flex items-start justify-between mb-3">
                  <div class="p-2 bg-amber-100/50 rounded-xl text-amber-600 group-hover:bg-amber-500 group-hover:text-white transition-colors duration-300">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                    </svg>
                  </div>
                  <span class="text-xs font-bold text-stone-400 uppercase tracking-wider">Doluluk</span>
                </div>
                <div class="flex items-baseline gap-1 mb-2">
                  <span class="text-2xl font-black" :class="currentStock >= item.maxStock ? 'text-red-600' : 'text-stone-800'">{{ currentStock }}</span>
                  <span class="text-sm font-bold text-stone-600/70">/ {{ item.maxStock }}</span>
                </div>
                <div class="h-2 w-full bg-amber-100 rounded-full overflow-hidden border border-amber-100">
                  <div class="h-full bg-gradient-to-r from-amber-400 to-orange-500 rounded-full transition-all duration-700 ease-out shadow-[0_0_10px_rgba(245,158,11,0.5)]" :style="{ width: `${Math.min((currentStock / item.maxStock) * 100, 100)}%` }"></div>
                </div>
              </div>

                <div class="bg-white/70 backdrop-blur-sm rounded-2xl p-5 border border-white/60 shadow-lg shadow-amber-200/50 hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group">
                  <div class="flex items-start justify-between mb-3">
                    <div class="p-2 bg-purple-100/50 rounded-xl text-purple-600 group-hover:bg-purple-500 group-hover:text-white transition-colors duration-300">
                      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                      </svg>
                    </div>
                    <span class="text-xs font-bold text-stone-400 uppercase tracking-wider">Ürün Tipi</span>
                  </div>
                  <div class="text-2xl font-black text-stone-800 mb-1">
                    {{ item.items?.length || 0 }} Adet
                  </div>
                  <div class="text-sm font-medium text-purple-600">Üretilen Çeşit</div>
                </div>
            </div>

            <!-- Production Machine Panel -->
            <div class="bg-white rounded-3xl shadow-xl shadow-amber-200/60 border border-amber-100 overflow-hidden relative">
              <!-- Decorative Top Bar -->
              <div class="h-2 bg-gradient-to-r from-stone-400 via-amber-500 to-stone-800"></div>
              
              <div class="p-6 sm:p-8">
                <div class="flex items-center justify-between mb-8">
                  <h3 class="text-xl font-black text-stone-800 flex items-center gap-3">
                    <span class="w-8 h-8 rounded-lg bg-stone-800 text-white flex items-center justify-center shadow-lg shadow-stone-800/20">
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"/>
                      </svg>
                    </span>
                    Hasat Alanı
                  </h3>
                  
                  <!-- Status Indicator -->
                  <div class="flex items-center gap-2 px-3 py-1.5 rounded-full border" 
                       :class="isProducing ? 'bg-amber-50 border-amber-200 text-amber-700' : 'bg-stone-50 border-stone-200 text-stone-500'">
                    <span class="relative flex h-3 w-3">
                      <span v-if="isProducing" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                      <span class="relative inline-flex rounded-full h-3 w-3" :class="isProducing ? 'bg-amber-500' : 'bg-stone-400'"></span>
                    </span>
                    <span class="text-xs font-bold uppercase tracking-wide">{{ isProducing ? 'BÜYÜME SÜRÜYOR' : 'DURDURULDU' }}</span>
                  </div>
                </div>

                <!-- Active Production View -->
                <div v-if="isProducing" class="flex flex-col items-center justify-center py-6">
                  <!-- Animated Machine Visual -->
                  <div class="relative w-64 h-64 mb-8">
                    <!-- Outer Glow -->
                    <div class="absolute inset-0 bg-amber-400/20 rounded-full blur-3xl animate-pulse"></div>
                    
                    <!-- Progress Circle -->
                    <svg class="w-full h-full transform -rotate-90 relative z-10 drop-shadow-xl">
                      <!-- Track -->
                      <circle cx="128" cy="128" r="110" stroke="currentColor" stroke-width="12" fill="transparent" class="text-stone-100" />
                      <!-- Indicator -->
                      <circle cx="128" cy="128" r="110" stroke="currentColor" stroke-width="12" fill="transparent" 
                              class="text-amber-500 transition-all duration-1000 ease-linear" 
                              stroke-dasharray="691" 
                              :stroke-dashoffset="691 * (1 - progressPercentage)" 
                              stroke-linecap="round" />
                    </svg>
                    
                    <!-- Center Info -->
                    <div class="absolute inset-0 flex items-center justify-center flex-col z-20">
                      <div class="text-sm font-bold text-amber-600 uppercase tracking-widest mb-1">Kalan Süre</div>
                      <div class="text-5xl font-black text-stone-800 tracking-tighter tabular-nums">{{ timeLeft || '--:--' }}</div>
                    </div>
                  </div>
                  
                  <div v-if="!canCollect" class="text-center w-full max-w-md">
                     <div class="bg-stone-50 rounded-xl p-4 border border-stone-200 flex items-center justify-center gap-3">
                       <svg class="w-5 h-5 text-stone-400 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                         <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                       </svg>
                       <span class="text-stone-600 font-medium">Üretim süreci devam ediyor, lütfen bekleyin.</span>
                     </div>
                  </div>

                  <button 
                    v-else
                    @click="collectProduction" 
                    class="group relative w-full sm:w-auto px-6 py-4 sm:px-10 sm:py-5 bg-gradient-to-b from-green-500 to-green-600 text-white rounded-2xl font-black text-xl shadow-[0_6px_0_rgb(21,128,61),0_15px_20px_rgba(34,197,94,0.3)] hover:shadow-[0_3px_0_rgb(21,128,61),0_10px_15px_rgba(34,197,94,0.2)] hover:translate-y-[3px] active:shadow-none active:translate-y-[6px] transition-all flex items-center justify-center gap-3 overflow-hidden"
                  >
                    <div class="absolute inset-0 bg-white/20 transtone-y-full group-hover:transtone-y-0 transition-transform duration-300 skew-y-12"></div>
                    <svg class="w-8 h-8 relative z-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
                    </svg>
                    <span class="relative z-10">HASADI TOPLA</span>
                  </button>
                </div>

                <!-- Idle State View -->
                <div v-else class="text-center py-8">
                  <div class="w-24 h-24 bg-stone-100 rounded-full mx-auto flex items-center justify-center mb-6 text-stone-300">
                    <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                    </svg>
                  </div>
                  <h4 class="text-lg font-bold text-stone-700 mb-2">Bahçe Beklemede</h4>
                  <p class="text-stone-500 mb-8 max-w-md mx-auto">Bahçe şu anda üretim yapmıyor. Yeni bir üretim emri vermek için aşağıdaki butonu kullanın.</p>
                  
                  <button 
                    @click="showProductionModal = true" 
                    class="group relative w-full sm:w-auto px-6 py-4 sm:px-10 sm:py-5 bg-gradient-to-b from-amber-600 to-amber-700 text-white rounded-2xl font-black text-xl shadow-[0_6px_0_rgb(180,83,9),0_15px_20px_rgba(251,191,36,0.3)] hover:shadow-[0_3px_0_rgb(180,83,9),0_10px_15px_rgba(251,191,36,0.2)] hover:translate-y-[3px] active:shadow-none active:translate-y-[6px] transition-all flex items-center justify-center gap-3 mx-auto overflow-hidden"
                  >
                    <div class="absolute inset-0 bg-white/20 transtone-y-full group-hover:transtone-y-0 transition-transform duration-300 skew-y-12"></div>
                    <svg class="w-8 h-8 relative z-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                    </svg>
                    <span class="relative z-10">EKİM YAP</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Inventory Section -->
            <div class="bg-white rounded-3xl shadow-sm border border-amber-200 overflow-hidden">
              <div class="p-6 border-b border-amber-100 flex items-center justify-between bg-amber-50/50">
                <h3 class="text-lg font-bold text-stone-800 flex items-center gap-2">
                  <svg class="w-5 h-5 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                  </svg>
                  Depo Envanteri
                </h3>
                <span class="text-xs font-bold text-stone-600 bg-stone-100/50 px-3 py-1 rounded-full border border-stone-100">{{ item.items?.length || 0 }} Kalem</span>
              </div>
              <div class="p-0">
                <div v-if="!item.items || item.items.length === 0" class="p-12 text-center">
                  <div class="w-16 h-16 bg-amber-50 rounded-2xl mx-auto flex items-center justify-center mb-3 text-amber-300">
                    <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/>
                    </svg>
                  </div>
                  <p class="text-stone-400 font-medium">Depo boş. Ekim yaparak stok oluşturun.</p>
                </div>
                <div v-else class="bg-amber-50">
                  <BuildingInventoryTable 
                    :items="filteredItems" 
                    @transfer="startWithdraw" 
                    class="border-none shadow-none"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column: Info & Upgrades (4 cols) -->
          <div class="lg:col-span-4 space-y-6">
            <!-- Building Info -->
            <div class="bg-white rounded-3xl shadow-sm border border-amber-200 p-6 relative overflow-hidden">
              <div class="absolute top-0 right-0 w-32 h-32 bg-amber-50 rounded-bl-full -mr-8 -mt-8 pointer-events-none"></div>
              
              <h3 class="text-sm font-bold text-stone-400 uppercase tracking-wider mb-4 relative z-10">Bahçe Bilgileri</h3>
              <p class="text-stone-700 text-sm leading-relaxed mb-8 relative z-10">
                {{ item.description }}
              </p>
              
              <div class="space-y-4 relative z-10">
                <button 
                  @click="handleUpgrade"
                  :disabled="item.level >= 3"
                  class="w-full p-4 bg-white border-2 border-amber-100 text-stone-800 rounded-2xl font-bold hover:border-amber-400 hover:text-amber-700 hover:bg-amber-50/50 hover:shadow-md transition-all flex items-center justify-between group disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:border-amber-100 disabled:hover:bg-white disabled:hover:shadow-none"
                >
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center text-amber-600 group-hover:scale-110 transition-transform">
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"/>
                      </svg>
                    </div>
                    <div class="text-left">
                      <div class="text-sm font-bold text-stone-800 group-hover:text-amber-800">Seviye Yükselt</div>
                      <div class="text-xs text-stone-500 font-medium">Kapasite Artışı</div>
                    </div>
                  </div>
                  <div class="text-xs font-black bg-amber-100 px-2 py-1 rounded-lg text-amber-700 group-hover:bg-amber-200 group-hover:text-amber-800 transition-colors">
                    {{ item.level >= 3 ? 'MAX' : '15K VP' }}
                  </div>
                </button>

                <button 
                  @click="handleClose"
                  class="w-full p-4 bg-white border-2 border-amber-100 text-stone-800 rounded-2xl font-bold hover:border-red-400 hover:text-red-600 hover:bg-red-50/50 hover:shadow-md transition-all flex items-center justify-between group"
                >
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-red-50 flex items-center justify-center text-red-500 group-hover:scale-110 transition-transform">
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                      </svg>
                    </div>
                    <div class="text-left">
                      <div class="text-sm font-bold text-stone-800 group-hover:text-red-700">Yıkım Emri</div>
                      <div class="text-xs text-stone-500 font-medium">Bahçeyi Kapat</div>
                    </div>
                  </div>
                </button>
              </div>
            </div>

            <!-- Quick Tips -->
            <div class="bg-gradient-to-br from-amber-50 to-orange-100 rounded-3xl p-6 text-amber-900 shadow-lg shadow-amber-900/5 relative overflow-hidden">
              <div class="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl -mr-10 -mt-10"></div>
              <div class="absolute bottom-0 left-0 w-24 h-24 bg-black/10 rounded-full blur-xl -ml-10 -mb-10"></div>
              
              <h4 class="font-bold mb-3 flex items-center gap-2 relative z-10">
                <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                Yönetici Notu
              </h4>
              <p class="text-sm text-amber-800 leading-relaxed relative z-10">
                Bahçeler sürekli bakım ister. Hasat zamanını kaçırma, ürünler taze kalsın!
              </p>
            </div>
          </div>
        </div>

      <!-- Modals -->
      <!-- Production Start Modal -->
      <Teleport to="body">
        <div 
          v-if="showProductionModal" 
          class="fixed inset-0 bg-stone-900/60 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
          @click.self="showProductionModal = false"
        >
          <div class="bg-white rounded-3xl w-full max-w-md overflow-hidden shadow-2xl relative border border-amber-100">
            <div class="p-6 border-b border-amber-100 flex justify-between items-center bg-amber-50/50">
              <h2 class="text-xl font-black text-stone-800">Üretim Emri</h2>
              <button @click="showProductionModal = false" class="w-8 h-8 rounded-full bg-stone-100 flex items-center justify-center text-stone-400 hover:bg-stone-200 hover:text-stone-600 transition-colors">
                <XMarkIcon class="w-5 h-5" />
              </button>
            </div>
            
            <div class="p-6 space-y-6">
              <SelectBox
                v-model="selectedProduct"
                :options="productionOptions"
                label="Ekilicek Ürün"
                placeholder="Bir ürün seçin..."
                value-key="id"
                label-key="name"
              />
              
              <div class="bg-amber-50 rounded-xl p-4 border border-amber-100">
                <div class="flex gap-3">
                  <div class="w-10 h-10 rounded-lg bg-amber-100 flex items-center justify-center text-amber-600 shrink-0">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                  </div>
                  <div>
                    <h4 class="font-bold text-stone-800 text-sm">Tahmini Süre</h4>
                    <p class="text-stone-700 text-sm mt-0.5">
                      Bu ekim işlemi yaklaşık <strong class="text-stone-800">{{ buildingConfig?.productionDuration || '-' }} dakika</strong> sürecektir.
                    </p>
                  </div>
                </div>
              </div>

              <div class="flex gap-3 pt-2">
                <button 
                  @click="showProductionModal = false" 
                  class="flex-1 py-4 text-stone-600 font-bold hover:bg-amber-50 rounded-2xl transition-colors border-2 border-transparent hover:border-amber-100"
                >
                  İptal
                </button>
                <button 
                  @click="confirmStartProduction"
                  :disabled="!selectedProduct"
                  class="flex-1 py-4 bg-amber-600 text-white font-bold rounded-2xl hover:bg-amber-700 transition-all shadow-lg shadow-amber-600/20 disabled:opacity-50 disabled:cursor-not-allowed transform active:scale-95"
                >
                  Ekim Yap
                </button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- Withdraw Modal -->
      <BuildingTransferModal
        :show="showWithdrawModal"
        :item="withdrawingItem"
        v-model:quantity="withdrawQuantity"
        @close="showWithdrawModal = false"
        @confirm="confirmWithdraw"
      />

      <!-- Upgrade Modal -->
      <Teleport to="body">
        <div 
          v-if="showUpgradeModal" 
          class="fixed inset-0 bg-stone-900/60 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
          @click.self="showUpgradeModal = false"
        >
          <div class="bg-white rounded-3xl w-full max-w-md overflow-hidden shadow-2xl relative border border-amber-100">
            <div class="p-6 border-b border-amber-100 flex justify-between items-center bg-amber-50/50">
              <h2 class="text-xl font-black text-stone-800">Binayı Yükselt</h2>
              <button @click="showUpgradeModal = false" class="w-8 h-8 rounded-full bg-stone-100 flex items-center justify-center text-stone-400 hover:bg-stone-200 hover:text-stone-600 transition-colors">
                <XMarkIcon class="w-5 h-5" />
              </button>
            </div>
            
            <div class="p-6 space-y-6">
              <div class="bg-gradient-to-br from-amber-50 to-orange-50 border border-amber-100 rounded-2xl p-5">
                <div class="flex items-center gap-3 mb-4">
                  <div class="w-10 h-10 rounded-xl bg-white shadow-sm flex items-center justify-center text-amber-600">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                    </svg>
                  </div>
                  <div>
                    <h3 class="font-bold text-stone-800">Seviye Yükseltme</h3>
                    <div class="text-xs font-bold text-amber-600 uppercase tracking-wide">Kapasite Artışı</div>
                  </div>
                </div>
                
                <div class="space-y-3">
                  <div class="flex justify-between items-center p-3 bg-white/60 rounded-xl">
                    <span class="text-sm text-stone-500 font-medium">Mevcut Seviye</span>
                    <span class="font-bold text-stone-800">LVL {{ item.level }}</span>
                  </div>
                  <div class="flex justify-between items-center p-3 bg-white rounded-xl shadow-sm border border-amber-100">
                    <span class="text-sm text-amber-600 font-bold">Yeni Seviye</span>
                    <span class="font-black text-amber-600 text-lg">LVL {{ item.level + 1 }}</span>
                  </div>
                  <div class="flex justify-between items-center p-3 bg-stone-900 rounded-xl text-white shadow-lg shadow-stone-900/10">
                    <span class="text-sm font-medium text-stone-300">Gerekli Tutar</span>
                    <Currency :amount="15000" :icon-size="16" class-name="text-lg" />
                  </div>
                </div>
              </div>

              <div class="flex gap-3 pt-2">
                <button 
                  @click="showUpgradeModal = false" 
                  class="flex-1 py-4 text-stone-600 font-bold hover:bg-amber-50 rounded-2xl transition-colors border-2 border-transparent hover:border-amber-100"
                >
                  İptal
                </button>
                <button 
                  @click="confirmUpgrade"
                  class="flex-1 py-4 bg-amber-600 text-white font-bold rounded-2xl hover:bg-amber-700 transition-all shadow-lg shadow-amber-600/20 transform active:scale-95"
                >
                  Onayla ve Yükselt
                </button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- Close Modal -->
      <Teleport to="body">
        <div 
          v-if="showCloseModal" 
          class="fixed inset-0 bg-stone-900/60 backdrop-blur-sm flex items-center justify-center z-[9999] p-4"
          @click.self="showCloseModal = false"
        >
          <div class="bg-white rounded-3xl w-full max-w-md overflow-hidden shadow-2xl relative border border-amber-100">
            <div class="p-6 border-b border-amber-100 flex justify-between items-center bg-amber-50/50">
              <h2 class="text-xl font-black text-stone-800">Binayı Kapat</h2>
              <button @click="showCloseModal = false" class="w-8 h-8 rounded-full bg-stone-100 flex items-center justify-center text-stone-400 hover:bg-stone-200 hover:text-stone-600 transition-colors">
                <XMarkIcon class="w-5 h-5" />
              </button>
            </div>
            
            <div class="p-6 space-y-6">
              <div class="bg-red-50 border border-red-100 rounded-2xl p-5">
                <div class="flex items-center gap-3 mb-3">
                  <div class="w-10 h-10 rounded-xl bg-white shadow-sm flex items-center justify-center text-red-500">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    </svg>
                  </div>
                  <h3 class="font-bold text-red-900">Kalıcı İşlem Uyarısı</h3>
                </div>
                <p class="text-sm text-red-800 leading-relaxed">
                  Bu işlem geri alınamaz. Bina kalıcı olarak silinecek ve üretim durdurulacaktır.
                </p>
              </div>

              <div class="bg-emerald-50 border border-emerald-100 rounded-2xl p-5 flex justify-between items-center">
                <span class="text-sm font-bold text-emerald-800">İade Edilecek Tutar</span>
                <Currency :amount="item.cost - 10000" :icon-size="18" class-name="text-lg font-black" />
              </div>

              <div class="flex gap-3 pt-2">
                <button 
                  @click="showCloseModal = false" 
                  class="flex-1 py-4 text-emerald-600 font-bold hover:bg-amber-50 rounded-2xl transition-colors border-2 border-transparent hover:border-amber-100"
                >
                  Vazgeç
                </button>
                <button 
                  @click="confirmClose"
                  class="flex-1 py-4 bg-red-600 text-white font-bold rounded-2xl hover:bg-red-700 transition-all shadow-lg shadow-red-600/20 transform active:scale-95"
                >
                  Binayı Kapat
                </button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useGardensStore } from '../../stores/gardensStore'
import { useAuthStore } from '../../stores/authStore'
import { useGameDataStore } from '../../stores/gameDataStore'
import { useBuildingConfigStore } from '../../stores/buildingConfigStore'
import BuildingService from '../../services/BuildingService'
import { useToast } from '../../composables/useToast'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import Currency from "../../components/Currency.vue"
import SelectBox from "../../components/SelectBox.vue"
import BuildingInventoryTable from "../../components/BuildingInventoryTable.vue"
import BuildingTransferModal from "../../components/BuildingTransferModal.vue"
import { getItemTierTr } from '../../utils/translations'

const route = useRoute()
const { addToast } = useToast()
const store = useGardensStore()
const authStore = useAuthStore()
const gameDataStore = useGameDataStore()
const configStore = useBuildingConfigStore()
const item = computed(() => store.items.find(i => i.id === route.params.id))
const buildingConfig = computed(() => item.value ? configStore.getConfig('GARDEN', item.value.tier) : null)

const productionOptions = ref([])
const selectedProduct = ref(null)
const timeLeft = ref('')
const progressPercentage = ref(1) // Start full
const showUpgradeModal = ref(false)
const showCloseModal = ref(false)
const showProductionModal = ref(false)
const now = ref(Date.now())
let timerInterval = null

const isProducing = computed(() => item.value?.isProducing)
const canCollect = computed(() => {
    if (!item.value?.isProducing || !item.value?.productionEndsAt) return false
    return new Date(item.value.productionEndsAt).getTime() <= now.value
})

const currentStock = computed(() => {
  if (!item.value?.items) return 0
  return item.value.items.reduce((total, i) => total + i.quantity, 0)
})

const filteredItems = computed(() => {
  if (!item.value?.items) return []
  return item.value.items.filter(i => !i.isProducing)
})

const updateTimer = async () => {
  if (!item.value?.productionEndsAt) {
    timeLeft.value = ''
    return
  }
  
  const end = new Date(item.value.productionEndsAt).getTime()
  
  if (isNaN(end)) {
    timeLeft.value = ''
    return
  }

  now.value = Date.now()
  const diff = end - now.value
  
  if (diff <= 0) {
    timeLeft.value = 'Tamamlandı'
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    
    // Auto-collect removed. User must click collect button.
    return
  }
  
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  if (isNaN(minutes) || isNaN(seconds)) {
    timeLeft.value = ''
    return
  }

  timeLeft.value = `${minutes}:${String(seconds).padStart(2, '0')}`

  // Calculate progress
  if (buildingConfig.value?.productionDuration) {
    const totalDuration = buildingConfig.value.productionDuration * 60 * 1000
    const progress = Math.max(0, Math.min(1, diff / totalDuration))
    progressPercentage.value = progress
  }
}


async function confirmStartProduction() {
  if (!selectedProduct.value) {
    addToast('Lütfen bir ürün seçin', 'error')
    return
  }
  
  showProductionModal.value = false
  try {
    await BuildingService.startProduction(item.value.id, selectedProduct.value)
    addToast('Üretim başladı!', 'success')
    selectedProduct.value = null
    await store.load()
    
    if (!timerInterval) {
      timerInterval = setInterval(updateTimer, 1000)
    }
    updateTimer()
  } catch (error) {
    addToast('Üretim başlatılamadı: ' + (error.response?.data?.message || error.message), 'error')
  }
}

async function startProduction() {
  try {
    await BuildingService.startProduction(item.value.id)
    addToast('Üretim başladı!', 'success')
    await store.load()
    
    if (!timerInterval) {
      timerInterval = setInterval(updateTimer, 1000)
    }
    updateTimer()
  } catch (error) {
    console.error(error)
    addToast('Üretim başlatılamadı', 'error')
  }
}

async function collectProduction() {
  try {
    await BuildingService.collect(item.value.id)
    addToast('Ürünler toplandı!', 'success')
    await store.load()
    // Refresh user balance/inventory if needed
  } catch (error) {
    console.error(error)
    addToast('Toplama başarısız', 'error')
  }
}



const handleUpgrade = async () => {
  if (item.value.level >= 10) {
    addToast('Bina zaten maksimum seviyede', 'error')
    return
  }
  showUpgradeModal.value = true
}

const confirmUpgrade = async () => {
  showUpgradeModal.value = false
  try {
    const response = await BuildingService.upgrade(item.value.id)
    addToast('Bina başarıyla yükseltildi!', 'success')
    await store.load()
    if (response.data) {
      await authStore.fetchUser()
    }
  } catch (error) {
    console.error(error)
    const message = error.response?.data?.message || 'Bina yükseltilemedi'
    addToast(message, 'error')
  }
}

const showWithdrawModal = ref(false)
const withdrawingItem = ref(null)
const withdrawQuantity = ref(1)

const startWithdraw = (item) => {
  withdrawingItem.value = item
  withdrawQuantity.value = item.quantity
  showWithdrawModal.value = true
}

const confirmWithdraw = async () => {
  if (!withdrawingItem.value || withdrawQuantity.value <= 0) return
  
  try {
    await BuildingService.withdraw(item.value.id, withdrawingItem.value.name, withdrawQuantity.value)
    addToast('Ürünler envantere aktarıldı', 'success')
    showWithdrawModal.value = false
    await store.load()
  } catch (error) {
    console.error(error)
    // Global interceptor handles the toast
  }
}

const handleClose = async () => {
  const hasItems = (item.value?.items?.length || 0) > 0
  if (hasItems) {
    addToast('Binada ürün bulunuyor. Önce tüm ürünleri satmalısınız veya kullanmalısınız.', 'error')
    return
  }
  showCloseModal.value = true
}

const confirmClose = async () => {
  showCloseModal.value = false
  try {
    const response = await BuildingService.closeBuilding(item.value.id)
    addToast('Bina başarıyla kapatıldı. İade tutarı hesabınıza eklendi.', 'success')
    if (response.data) {
      authStore.fetchUser()
    }
    await store.load()
    window.location.href = '/home/gardens'
  } catch (error) {
    console.error(error)
    const message = error.response?.data?.message || 'Bina kapatılamadı'
    addToast(message, 'error')
  }
}

onMounted(async () => {
  await configStore.fetchConfigs()
  
  if (!store.items.length) {
    await store.load()
  }
  
  
  productionOptions.value = gameDataStore.getItemsByType('GARDEN')
  timerInterval = setInterval(updateTimer, 1000)
  updateTimer()
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>
