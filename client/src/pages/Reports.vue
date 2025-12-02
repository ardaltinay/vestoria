<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-display font-bold text-slate-900">Finansal Raporlar</h1>
        <p class="text-slate-500 text-sm">İşletmenizin detaylı gelir ve gider analizi.</p>
      </div>
      <div class="flex items-center gap-2 bg-white p-1 rounded-lg border border-slate-200 shadow-sm">
        <button 
          v-for="range in [7, 30]" 
          :key="range"
          @click="selectedRange = range"
          class="px-3 py-1.5 text-xs font-medium rounded-md transition-colors"
          :class="selectedRange === range ? 'bg-slate-900 text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'"
        >
          Son {{ range }} Gün
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
    </div>

    <!-- Content -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      
      <!-- Daily Financials -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="p-5 border-b border-slate-100 bg-slate-50/50">
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <ChartBarIcon class="w-5 h-5 text-slate-400" />
            Günlük Gelir/Gider
          </h3>
        </div>
        <div class="p-6">
          <div class="h-64 flex items-end gap-2 justify-between">
            <div v-for="(day, index) in chartData" :key="index" class="flex-1 flex flex-col justify-end gap-1 group relative">
              <!-- Tooltip -->
              <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 bg-slate-800 text-white text-xs p-2 rounded shadow-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-10">
                <div class="font-bold">{{ formatDate(day.date) }}</div>
                <div class="text-emerald-400">+{{ formatCurrency(day.income) }}</div>
                <div class="text-rose-400">-{{ formatCurrency(day.expense) }}</div>
              </div>
              
              <!-- Bars -->
              <div class="w-full bg-emerald-500/80 hover:bg-emerald-500 transition-colors rounded-t-sm" :style="{ height: getBarHeight(day.income) }"></div>
              <div class="w-full bg-rose-500/80 hover:bg-rose-500 transition-colors rounded-b-sm" :style="{ height: getBarHeight(day.expense) }"></div>
            </div>
          </div>
          <!-- X Axis Labels -->
          <div class="flex justify-between mt-2 text-xs text-slate-400">
             <span>{{ formatDate(chartData[0]?.date) }}</span>
             <span>{{ formatDate(chartData[chartData.length - 1]?.date) }}</span>
          </div>
        </div>
      </div>

      <!-- Category Breakdown -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="p-5 border-b border-slate-100 bg-slate-50/50">
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <ChartPieIcon class="w-5 h-5 text-slate-400" />
            Gelir Dağılımı
          </h3>
        </div>
        <div class="p-0">
          <div v-if="reportData.incomeByCategory.length === 0" class="p-8 text-center text-slate-400 text-sm">
            Veri bulunamadı.
          </div>
          <div v-else class="divide-y divide-slate-50">
            <div v-for="(item, index) in reportData.incomeByCategory" :key="index" class="p-4 flex items-center justify-between hover:bg-slate-50 transition-colors">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-500 font-bold text-xs">
                  {{ index + 1 }}
                </div>
                <div>
                  <div class="font-bold text-slate-800 text-sm">{{ item.category }}</div>
                  <div class="text-xs text-slate-500">Satış Geliri</div>
                </div>
              </div>
              <div class="text-right">
                <div class="font-bold text-slate-900 text-sm">
                  {{ formatCurrency(item.totalAmount) }}
                </div>
                <div class="text-xs text-emerald-600 font-medium">
                  %{{ calculatePercentage(item.totalAmount) }}
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
import { ref, onMounted, watch, computed } from 'vue'
import api from '../services/api'
import { formatCurrency } from '../utils/currency'
import { 
  ChartBarIcon, 
  ChartPieIcon 
} from '@heroicons/vue/24/outline'

const loading = ref(true)
const selectedRange = ref(7)
const reportData = ref({
  dailyIncome: [],
  dailyExpense: [],
  incomeByCategory: []
})

const fetchData = async () => {
  loading.value = true
  try {
    const response = await api.get('/reports', { params: { days: selectedRange.value } })
    reportData.value = response.data
  } catch (error) {
    console.error('Failed to fetch reports', error)
  } finally {
    loading.value = false
  }
}

const chartData = computed(() => {
  // Merge income and expense by date
  const map = new Map()
  
  // Initialize dates
  const today = new Date()
  for (let i = selectedRange.value - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const dateStr = d.toISOString().split('T')[0]
    map.set(dateStr, { date: dateStr, income: 0, expense: 0 })
  }

  reportData.value.dailyIncome.forEach(item => {
    if (map.has(item.date)) map.get(item.date).income = item.amount
  })
  
  reportData.value.dailyExpense.forEach(item => {
    if (map.has(item.date)) map.get(item.date).expense = item.amount
  })

  return Array.from(map.values())
})

const maxAmount = computed(() => {
  let max = 0
  chartData.value.forEach(d => {
    if (d.income > max) max = d.income
    if (d.expense > max) max = d.expense
  })
  return max || 100 // Avoid division by zero
})

const getBarHeight = (amount) => {
  const percentage = (amount / maxAmount.value) * 100
  return `${Math.max(percentage, 2)}%` // Min height 2%
}

const totalIncome = computed(() => {
  return reportData.value.incomeByCategory.reduce((sum, item) => sum + item.totalAmount, 0)
})

const calculatePercentage = (amount) => {
  if (!totalIncome.value) return 0
  return ((amount / totalIncome.value) * 100).toFixed(1)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return new Intl.DateTimeFormat('tr-TR', { day: 'numeric', month: 'short' }).format(date)
}

watch(selectedRange, () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})
</script>
