<template>
  <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
    <div class="flex items-center justify-between mb-4">
      <h3 class="font-bold text-slate-800">Pazar Trendleri</h3>
      <span class="text-xs font-medium px-2 py-1 rounded-full bg-emerald-50 text-emerald-600">Canlı</span>
    </div>
    
    <div class="h-40 relative">
      <Line v-if="chartData" :data="chartData" :options="chartOptions" />
      <div v-else class="absolute inset-0 flex items-center justify-center text-slate-400 text-sm">
        Veri yükleniyor...
      </div>
    </div>

    <div class="mt-4 flex items-center justify-between">
      <div>
        <div class="text-xs text-slate-400">En Çok İşlem Gören</div>
        <div class="font-bold text-slate-900">{{ topItem?.itemName || '-' }}</div>
      </div>
      <div class="text-right">
        <div class="text-xs text-slate-400">Son Fiyat</div>
        <div class="font-bold text-emerald-600">{{ topItem ? formatCurrency(topItem.currentPrice) : '-' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Filler
} from 'chart.js'
import { Line } from 'vue-chartjs'
import MarketService from '../../services/MarketService'
import { formatCurrency } from '../../utils/currency'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Filler
)

const chartData = ref(null)
const topItem = ref(null)

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      mode: 'index',
      intersect: false,
      backgroundColor: '#1e293b',
      titleColor: '#f8fafc',
      bodyColor: '#f8fafc',
      padding: 10,
      cornerRadius: 8,
      displayColors: false
    }
  },
  scales: {
    x: { display: false },
    y: { display: false, min: 0 }
  },
  elements: {
    line: {
      tension: 0.4,
      borderWidth: 2,
      borderColor: '#10b981',
      backgroundColor: 'rgba(16, 185, 129, 0.1)',
      fill: true
    },
    point: {
      radius: 0,
      hoverRadius: 4,
      hoverBackgroundColor: '#10b981',
      hoverBorderColor: '#fff',
      hoverBorderWidth: 2
    }
  },
  interaction: {
    mode: 'nearest',
    axis: 'x',
    intersect: false
  }
}

const fetchTrends = async () => {
  try {
    const response = await MarketService.getMarketTrends()
    const trends = response.data
    
    if (trends && trends.length > 0) {
      const item = trends[0] // Pick top item
      topItem.value = item
      
      if (item && item.history) {
          const labels = item.history.map(h => new Date(h.date).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' }))
          const data = item.history.map(h => h.price)
          
          chartData.value = {
            labels,
            datasets: [{
              label: 'Fiyat',
              data,
              borderColor: '#10b981',
              backgroundColor: 'rgba(16, 185, 129, 0.1)',
              fill: true,
              tension: 0.4
            }]
          }
      }
    }
  } catch (error) {
    console.error('Failed to fetch market trends:', error)
  }
}

onMounted(() => {
  fetchTrends()
})
</script>
