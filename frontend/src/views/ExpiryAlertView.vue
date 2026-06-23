<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Cảnh báo hết hạn</h1>
        <p class="page-subtitle">Theo dõi chứng chỉ, tài liệu và đăng ký sắp đến hạn</p>
      </div>
      <div class="flex-center gap-3">
        <Tag :value="`${alerts.length} cảnh báo`" severity="warning" />
        <Dropdown v-model="severityFilter" :options="severityOptions" placeholder="Mức độ" @change="onSeverityChange" showClear style="width: 160px;" />
        <Button label="Bỏ qua tất cả" icon="pi pi-check" severity="secondary" outlined @click="dismissAll" :disabled="alerts.length === 0" />
        <Button label="Kiểm tra ngay" icon="pi pi-refresh" :loading="checking" @click="triggerCheck" />
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <StatsCard label="Cảnh báo &lt;30 ngày" :value="criticalCount" icon="pi pi-exclamation-triangle" variant="danger" />
      <StatsCard label="Cảnh báo 30-60 ngày" :value="warningCount" icon="pi pi-clock" variant="warning" />
      <StatsCard label="Cảnh báo 60-90 ngày" :value="infoCount" icon="pi pi-info-circle" variant="info" />
      <StatsCard label="Đã hết hạn" :value="expiredCount" icon="pi pi-times-circle" variant="primary" />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Empty -->
    <div v-else-if="alerts.length === 0" class="card-container empty-state">
      <i class="pi pi-check-circle" style="font-size: 3rem; color: var(--success-color);"></i>
      <h3>Tất cả đều hợp lệ</h3>
      <p>Không có chứng chỉ hay tài liệu nào sắp hết hạn</p>
    </div>

    <!-- Alert List -->
    <div v-else class="alert-list">
      <div v-for="alert in alerts" :key="alert.id" class="alert-card" :class="getAlertClass(alert)">
        <div class="alert-left">
          <div class="alert-icon" :class="getAlertIconClass(alert)">
            <i :class="getAlertIcon(alert)"></i>
          </div>
        </div>
        <div class="alert-content">
          <div class="alert-header">
            <h4>{{ alert.title }}</h4>
            <Tag :value="getExpiryBadge(alert.message)" :severity="getExpirySeverity(alert.message)" />
          </div>
          <p>{{ alert.message }}</p>
          <span class="alert-time">{{ formatTime(alert.createdAt) }}</span>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <Paginator v-if="totalRecords > pageSize" :rows="pageSize" :total-records="totalRecords" @page="onPage" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import apiClient from '@/api/client'
import StatsCard from '@/components/StatsCard.vue'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Paginator from 'primevue/paginator'
import ProgressSpinner from 'primevue/progressspinner'

const toast = useToast()

const loading = ref(false)
const checking = ref(false)
const alerts = ref([])
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(20)
const severityFilter = ref(null)

const severityOptions = [
  { label: 'Tất cả', value: null },
  { label: 'Nghiêm trọng', value: 'CRITICAL' },
  { label: 'Cảnh báo', value: 'WARNING' },
  { label: 'Thông tin', value: 'INFO' }
]

const criticalCount = computed(() => alerts.value.filter(a => a.severity === 'CRITICAL').length)
const warningCount = computed(() => alerts.value.filter(a => a.severity === 'WARNING').length)
const infoCount = computed(() => alerts.value.filter(a => a.severity === 'INFO').length)
const expiredCount = computed(() => alerts.value.filter(a => a.daysRemaining < 0).length)

function countByDays(list, maxDays) {
  return list.filter(a => a.daysRemaining <= maxDays).length
}

function countByDaysRange(list, minDays, maxDays) {
  return list.filter(a => a.daysRemaining >= minDays && a.daysRemaining <= maxDays).length
}

async function fetchAlerts() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (severityFilter.value) params.severity = severityFilter.value
    const res = await apiClient.get('/expiry/alerts', { params })
    alerts.value = res.data.content || []
    totalRecords.value = res.data.totalElements || 0
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách cảnh báo', life: 5000 })
  } finally {
    loading.value = false
  }
}

function onSeverityChange() {
  currentPage.value = 0
  fetchAlerts()
}

async function triggerCheck() {
  checking.value = true
  try {
    const res = await apiClient.post('/expiry/check-now')
    toast.add({ severity: 'success', summary: 'Thành công', detail: res.data?.message || 'Đã hoàn tất kiểm tra', life: 3000 })
    currentPage.value = 0
    fetchAlerts()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể thực hiện kiểm tra', life: 5000 })
  } finally {
    checking.value = false
  }
}

async function dismissAlert(alert) {
  try {
    await apiClient.put(`/expiry/alerts/${alert.id}/dismiss`)
    toast.add({ severity: 'success', summary: 'Đã bỏ qua', detail: 'Đã bỏ qua cảnh báo', life: 2000 })
    fetchAlerts()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể bỏ qua cảnh báo', life: 5000 })
  }
}

async function dismissAll() {
  try {
    await apiClient.put('/expiry/alerts/dismiss-all')
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã bỏ qua tất cả cảnh báo', life: 3000 })
    fetchAlerts()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể bỏ qua', life: 5000 })
  }
}

function getAlertClass(alert) {
  const severity = (alert.severity || 'INFO').toLowerCase()
  return `border-${severity}`
}

function getAlertIconClass(alert) {
  const severity = (alert.severity || 'INFO').toLowerCase()
  return `icon-${severity}`
}

function getAlertIcon(alert) {
  if (alert.alertType?.includes('REGISTRATION')) return 'pi pi-shield'
  if (alert.alertType?.includes('LEGAL')) return 'pi pi-building'
  if (alert.alertType?.includes('DOCUMENT')) return 'pi pi-verified'
  return 'pi pi-exclamation-triangle'
}

function getExpiryBadge(message) {
  const match = message?.match(/(\d+)\s+ngày/)
  return match ? `Còn ${match[1]} ngày` : (message?.includes('quá hạn') ? 'Hết hạn' : 'Sắp hết hạn')
}

function getExpirySeverity(message) {
  const match = message?.match(/(\d+)\s+ngày/)
  if (!match) return 'danger'
  const days = parseInt(match[1])
  if (days <= 30) return 'danger'
  if (days <= 60) return 'warning'
  return 'info'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return 'Vừa xong'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} phút trước`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} giờ trước`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} ngày trước`
  return date.toLocaleDateString('vi-VN')
}

function onPage(event) {
  currentPage.value = event.page
  fetchAlerts()
}

onMounted(() => {
  fetchAlerts()
})
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.alert-card {
  display: flex;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--surface-card);
  border: 1px solid var(--surface-border);
  border-left: 4px solid var(--surface-border);
  border-radius: 12px;
  transition: all 0.2s;
}

.alert-card:hover {
  background: var(--surface-ground);
}

.alert-card.border-danger { border-left-color: var(--danger-color); }
.alert-card.border-warning { border-left-color: #f59e0b; }
.alert-card.border-info { border-left-color: var(--info-color); }

.alert-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.icon-danger { background: #fee2e2; color: var(--danger-color); }
.icon-warning { background: #fef3c7; color: #92400e; }
.icon-info { background: #dbeafe; color: var(--info-color); }

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.375rem;
}

.alert-header h4 {
  font-size: 0.9375rem;
  font-weight: 600;
  margin: 0;
}

.alert-content p {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
}

.alert-time {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.gap-3 { gap: 0.75rem; }

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
