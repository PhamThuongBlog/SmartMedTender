<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Tổng quan</h1>
        <p class="page-subtitle">Tổng quan hoạt động đấu thầu y tế</p>
      </div>
      <div class="flex-center gap-3">
        <SelectButton v-model="period" :options="periodOptions" optionLabel="label" optionValue="value" size="small" />
        <Button icon="pi pi-refresh" severity="secondary" text rounded @click="fetchDashboardData" :loading="loading" />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading && !stats" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Error -->
    <div v-if="error && !stats" class="error-container">
      <i class="pi pi-exclamation-triangle"></i>
      <p>{{ error }}</p>
      <Button label="Thử lại" icon="pi pi-refresh" @click="fetchDashboardData" />
    </div>

    <!-- Dashboard Content -->
    <template v-if="stats">
      <!-- Stats Cards -->
      <div class="stats-grid">
        <StatsCard
          label="Tổng gói thầu"
          :value="stats.totalTenders"
          icon="pi pi-briefcase"
          variant="primary"
        />
        <StatsCard
          label="Đang hoạt động"
          :value="stats.activeTenders"
          icon="pi pi-play-circle"
          variant="info"
        />
        <StatsCard
          label="Trúng thầu"
          :value="stats.wonTenders"
          icon="pi pi-trophy"
          variant="success"
        />
        <StatsCard
          label="Tỷ lệ trúng thầu"
          :value="stats.winRate"
          icon="pi pi-chart-line"
          variant="warning"
          format="percent"
        />
      </div>

      <!-- Charts Row -->
      <div class="charts-grid">
        <div class="card-container">
          <h3 class="chart-title">Thống kê gói thầu theo tháng</h3>
          <Line v-if="lineChartData" :data="lineChartData" :options="lineChartOptions" class="chart" />
          <div v-else class="empty-state">
            <i class="pi pi-chart-line"></i>
            <p>Chưa có dữ liệu thống kê</p>
          </div>
        </div>

        <div class="card-container">
          <h3 class="chart-title">Gói thầu theo trạng thái</h3>
          <Bar v-if="barChartData" :data="barChartData" :options="barChartOptions" class="chart" />
          <div v-else class="empty-state">
            <i class="pi pi-chart-bar"></i>
            <p>Chưa có dữ liệu thống kê</p>
          </div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="card-container">
          <h3 class="chart-title">Danh mục sản phẩm hàng đầu</h3>
          <Pie v-if="pieChartData" :data="pieChartData" :options="pieChartOptions" class="chart pie-chart" />
          <div v-else class="empty-state">
            <i class="pi pi-chart-pie"></i>
            <p>Chưa có dữ liệu thống kê</p>
          </div>
        </div>

        <div class="card-container">
          <h3 class="chart-title">Thông tin bổ sung</h3>
          <div class="quick-stats">
            <div class="quick-stat-item">
              <span class="quick-stat-label">Tổng sản phẩm</span>
              <span class="quick-stat-value">{{ stats.totalProducts || 0 }}</span>
            </div>
            <div class="quick-stat-item">
              <span class="quick-stat-label">Tài liệu sắp hết hạn</span>
              <span class="quick-stat-value warning">{{ stats.expiringDocuments || 0 }}</span>
            </div>
            <div class="quick-stat-item">
              <span class="quick-stat-label">Tổng doanh thu</span>
              <span class="quick-stat-value success">{{ formatCurrency(stats.totalRevenue || 0) }}</span>
            </div>
            <div class="quick-stat-item">
              <span class="quick-stat-label">Gói thầu thất bại</span>
              <span class="quick-stat-value danger">{{ stats.lostTenders || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import apiClient from '@/api/client'
import StatsCard from '@/components/StatsCard.vue'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'
import SelectButton from 'primevue/selectbutton'
import { Line, Bar, Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'

ChartJS.register(
  CategoryScale, LinearScale, PointElement, LineElement,
  BarElement, ArcElement, Title, Tooltip, Legend, Filler
)

const loading = ref(false)
const error = ref(null)
const stats = ref(null)
const period = ref('year')

const periodOptions = [
  { label: 'Tháng', value: 'month' },
  { label: 'Quý', value: 'quarter' },
  { label: 'Năm', value: 'year' }
]

const lineChartData = computed(() => {
  if (!stats.value?.monthlyStats?.length) return null
  return {
    labels: stats.value.monthlyStats.map(s => s.month),
    datasets: [
      {
        label: 'Số gói thầu',
        data: stats.value.monthlyStats.map(s => s.tenderCount),
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37, 99, 235, 0.1)',
        fill: true,
        tension: 0.4,
        pointBackgroundColor: '#2563eb'
      }
    ]
  }
})

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: { precision: 0 }
    }
  }
}

const barChartData = computed(() => {
  if (!stats.value?.tendersByStatus) return null
  const statusLabels = {
    DRAFT: 'Bản nháp',
    ACTIVE: 'Đang hoạt động',
    SUBMITTED: 'Đã nộp',
    UNDER_REVIEW: 'Đang xem xét',
    AWARDED: 'Trúng thầu',
    REJECTED: 'Từ chối',
    CANCELLED: 'Đã hủy'
  }
  const entries = Object.entries(stats.value.tendersByStatus || {})
  return {
    labels: entries.map(([k]) => statusLabels[k] || k),
    datasets: [{
      label: 'Số lượng',
      data: entries.map(([, v]) => v),
      backgroundColor: [
        '#94a3b8', '#3b82f6', '#f59e0b', '#6366f1',
        '#10b981', '#ef4444', '#6b7280'
      ]
    }]
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: { precision: 0 }
    }
  }
}

const pieChartData = computed(() => {
  if (!stats.value?.topCategories?.length) return null
  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899', '#f97316']
  return {
    labels: stats.value.topCategories.map(c => c.category),
    datasets: [{
      data: stats.value.topCategories.map(c => c.count),
      backgroundColor: colors.slice(0, stats.value.topCategories.length)
    }]
  }
})

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'right',
      labels: {
        boxWidth: 12,
        padding: 16
      }
    }
  }
}

function formatCurrency(value) {
  if (!value) return '0 VND'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

async function fetchDashboardData() {
  loading.value = true
  error.value = null
  try {
    const response = await apiClient.get('/dashboard/stats')
    stats.value = response.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Không thể tải dữ liệu tổng quan'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.chart-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.chart {
  height: 300px;
  width: 100%;
}

.pie-chart {
  height: 250px;
}

.quick-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.quick-stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--surface-ground);
  border-radius: 8px;
}

.quick-stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.quick-stat-value {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.quick-stat-value.warning { color: var(--warning-color); }
.quick-stat-value.success { color: var(--success-color); }
.quick-stat-value.danger { color: var(--danger-color); }

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
