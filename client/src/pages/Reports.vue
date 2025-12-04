<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-display font-bold text-slate-900">Finansal Raporlar</h1>
        <p class="text-slate-500 text-sm">İşletmenizin detaylı gelir ve gider analizi.</p>
      </div>
      <div class="flex items-center gap-2 bg-white p-1 rounded-lg border border-slate-200 shadow-sm overflow-x-auto max-w-full no-scrollbar">
        <button 
          v-for="range in [7, 30]" 
          :key="range"
          @click="selectedRange = range"
          class="px-3 py-1.5 text-xs font-medium rounded-md transition-colors whitespace-nowrap flex-shrink-0"
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
      
      <!-- Daily Financials (Bar Chart) -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="p-5 border-b border-slate-100 bg-slate-50/50">
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <ChartBarIcon class="w-5 h-5 text-slate-400" />
            Günlük Gelir/Gider
          </h3>
        </div>
        <div class="p-6 h-80">
          <Bar v-if="barChartData" :data="barChartData" :options="barChartOptions" />
        </div>
      </div>

      <!-- Category Breakdown (Doughnut Chart) -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="p-5 border-b border-slate-100 bg-slate-50/50">
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <ChartPieIcon class="w-5 h-5 text-slate-400" />
            Gelir Dağılımı
          </h3>
        </div>
        <div class="p-6 flex flex-col items-center">
          <div class="h-64 w-full max-w-xs relative">
             <Doughnut v-if="doughnutChartData" :data="doughnutChartData" :options="doughnutChartOptions" />
             <div v-if="!doughnutChartData" class="absolute inset-0 flex items-center justify-center text-slate-400 text-sm">
               Veri yok
             </div>
          </div>
          
          <!-- Legend/List -->
          <div class="w-full mt-6 space-y-3">
             <div v-for="(item, index) in reportData.incomeByCategory" :key="index" class="flex items-center justify-between text-sm">
                <div class="flex items-center gap-2">
                  <span class="w-3 h-3 rounded-full" :style="{ backgroundColor: doughnutColors[index % doughnutColors.length] }"></span>
                  <span class="text-slate-600">{{ item.category }}</span>
                </div>
                <div class="font-bold text-slate-900">{{ formatCurrency(item.totalAmount) }}</div>
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
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement
} from 'chart.js'
import { Bar, Doughnut } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement)

const loading = ref(true)
const selectedRange = ref(7)
const reportData = ref({
  dailyIncome: [],
  dailyExpense: [],
  incomeByCategory: []
})

const doughnutColors = [
  '#10b981', // emerald-500
  '#3b82f6', // blue-500
  '#f59e0b', // amber-500
  '#8b5cf6', // violet-500
  '#ec4899', // pink-500
]

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

// --- Bar Chart Logic ---
const barChartData = computed(() => {
  if (loading.value) return null
  
  // Prepare dates
  const labels = []
  const incomeData = []
  const expenseData = []
  
  const today = new Date()
  for (let i = selectedRange.value - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const dateStr = d.toISOString().split('T')[0]
    
    labels.push(new Intl.DateTimeFormat('tr-TR', { day: 'numeric', month: 'short' }).format(d))
    
    const income = reportData.value.dailyIncome.find(x => x.date === dateStr)?.amount || 0
    const expense = reportData.value.dailyExpense.find(x => x.date === dateStr)?.amount || 0
    
    incomeData.push(income)
    expenseData.push(expense)
  }

  return {
    labels,
    datasets: [
      {
        label: 'Gelir',
        backgroundColor: '#10b981',
        data: incomeData,
        borderRadius: 4
      },
      {
        label: 'Gider',
        backgroundColor: '#f43f5e',
        data: expenseData,
        borderRadius: 4
      }
    ]
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: { usePointStyle: true, boxWidth: 8 }
    },
    tooltip: {
      mode: 'index',
      intersect: false,
      backgroundColor: '#1e293b',
      padding: 10,
      cornerRadius: 8
    }
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { font: { size: 10 } }
    },
    y: {
      grid: { color: '#f1f5f9' },
      ticks: { callback: (value) => formatCurrency(value, 'TRY', 'compact') } // simplified format
    }
  }
}

// --- Doughnut Chart Logic ---
const doughnutChartData = computed(() => {
  if (loading.value || reportData.value.incomeByCategory.length === 0) return null

  return {
    labels: reportData.value.incomeByCategory.map(x => x.category),
    datasets: [{
      backgroundColor: doughnutColors,
      data: reportData.value.incomeByCategory.map(x => x.totalAmount),
      borderWidth: 0
    }]
  }
})

const doughnutChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (context) => {
          const value = context.raw
          const total = context.dataset.data.reduce((a, b) => a + b, 0)
          const percentage = ((value / total) * 100).toFixed(1) + '%'
          return `${context.label}: ${formatCurrency(value)} (${percentage})`
        }
      }
    }
  },
  cutout: '75%'
}

watch(selectedRange, () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})
</script>
