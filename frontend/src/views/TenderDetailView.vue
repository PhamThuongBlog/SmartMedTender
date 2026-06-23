<template>
  <div class="page-container">
    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-container">
      <i class="pi pi-exclamation-triangle"></i>
      <p>{{ error }}</p>
      <Button label="Thử lại" icon="pi pi-refresh" @click="fetchTender" />
    </div>

    <template v-else-if="tender">
      <!-- Header -->
      <div class="page-header">
        <div>
          <div class="flex-center gap-3 mb-2">
            <Button icon="pi pi-arrow-left" severity="secondary" text rounded @click="router.push('/tenders')" />
            <h1 class="page-title" style="margin-bottom: 0;">{{ tender.name }}</h1>
            <StatusBadge :status="tender.status" />
          </div>
          <p class="page-subtitle">Mã gói: {{ tender.bidPackageCode || 'Chưa có mã' }}</p>
        </div>
        <div class="flex-center gap-3">
          <Button
            v-if="tender.status === 'DRAFT'"
            label="Kích hoạt"
            icon="pi pi-play"
            severity="success"
            @click="updateStatus('ACTIVE')"
          />
          <Button
            v-if="['ACTIVE', 'SUBMITTED'].includes(tender.status)"
            label="Đánh dấu trúng thầu"
            icon="pi pi-check-circle"
            severity="success"
            @click="updateStatus('AWARDED')"
          />
          <Button
            v-if="['DRAFT', 'ACTIVE'].includes(tender.status)"
            label="Hủy"
            icon="pi pi-ban"
            severity="warning"
            outlined
            @click="updateStatus('CANCELLED')"
          />
          <Button
            label="Sửa"
            icon="pi pi-pencil"
            severity="secondary"
            outlined
            @click="router.push(`/tenders/${tender.id}/edit`)"
          />
          <Button
            label="Sao chép"
            icon="pi pi-copy"
            severity="secondary"
            text
            @click="cloneTender"
          />
        </div>
      </div>

      <!-- Tabs -->
      <TabView>
        <TabPanel header="Thông tin chung">
          <div class="tender-info-grid">
            <div class="info-card">
              <h3 class="info-card-title">Thông tin cơ bản</h3>
              <div class="info-row">
                <span class="info-label">Mã gói thầu</span>
                <span class="info-value">{{ tender.bidPackageCode || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Tên gói thầu</span>
                <span class="info-value">{{ tender.name }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Bên mời thầu</span>
                <span class="info-value">{{ tender.procuringEntity || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Mô tả</span>
                <span class="info-value">{{ tender.description || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Ghi chú</span>
                <span class="info-value">{{ tender.notes || '-' }}</span>
              </div>
            </div>

            <div class="info-card">
              <h3 class="info-card-title">Thông tin thời gian & tài chính</h3>
              <div class="info-row">
                <span class="info-label">Giá dự toán</span>
                <span class="info-value price">{{ formatCurrency(tender.estimatedValue) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Đơn vị tiền tệ</span>
                <span class="info-value">{{ tender.currency || 'VND' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Hạn nộp hồ sơ</span>
                <span class="info-value">{{ formatDate(tender.submissionDeadline) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Ngày mở thầu</span>
                <span class="info-value">{{ formatDate(tender.openingDate) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Ngày tạo</span>
                <span class="info-value">{{ formatDate(tender.createdAt) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Cập nhật lần cuối</span>
                <span class="info-value">{{ formatDate(tender.updatedAt) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Phiên bản</span>
                <span class="info-value">{{ tender.version || 1 }}</span>
              </div>
            </div>
          </div>

          <!-- Status Workflow -->
          <div class="info-card mt-4">
            <h3 class="info-card-title">Quy trình trạng thái</h3>
            <div class="status-workflow">
              <template v-for="(s, i) in workflowSteps" :key="s.value">
                <div class="workflow-step" :class="{ active: isStepActive(s.value), completed: isStepCompleted(s.value) }">
                  <div class="step-circle">
                    <i v-if="isStepCompleted(s.value)" class="pi pi-check"></i>
                    <i v-else-if="isStepActive(s.value)" class="pi pi-circle-fill"></i>
                    <span v-else>{{ i + 1 }}</span>
                  </div>
                  <span class="step-label">{{ s.label }}</span>
                </div>
                <div v-if="i < workflowSteps.length - 1" class="step-connector" :class="{ completed: isStepCompleted(workflowSteps[i + 1].value) }"></div>
              </template>
            </div>
          </div>
        </TabPanel>

        <TabPanel header="Yêu cầu kỹ thuật">
          <div class="flex-between mb-4">
            <h3>Danh sách yêu cầu kỹ thuật</h3>
            <Button label="Tải HSMT" icon="pi pi-upload" severity="secondary" outlined size="small" @click="router.push('/hsmt/upload')" />
          </div>
          <div class="empty-state" v-if="requirements.length === 0">
            <i class="pi pi-file"></i>
            <p>Chưa có yêu cầu kỹ thuật. Vui lòng tải lên HSMT.</p>
            <Button label="Tải HSMT" icon="pi pi-upload" @click="router.push('/hsmt/upload')" />
          </div>
          <DataTable v-else :value="requirements" :paginator="true" :rows="10">
            <Column field="description" header="Mô tả yêu cầu"></Column>
            <Column field="type" header="Loại" style="width: 120px;"></Column>
            <Column field="operator" header="Toán tử" style="width: 80px;"></Column>
            <Column field="value" header="Giá trị" style="width: 100px;"></Column>
            <Column field="unit" header="Đơn vị" style="width: 80px;"></Column>
            <Column field="mandatory" header="Bắt buộc" style="width: 100px;">
              <template #body="{ data }">
                <i v-if="data.mandatory" class="pi pi-check" style="color: var(--success-color);"></i>
                <i v-else class="pi pi-times" style="color: var(--text-secondary);"></i>
              </template>
            </Column>
          </DataTable>
        </TabPanel>

        <TabPanel header="Sản phẩm đề xuất">
          <div class="flex-between mb-4">
            <h3>Sản phẩm phù hợp</h3>
            <Button label="So sánh kỹ thuật" icon="pi pi-chart-bar" @click="router.push(`/match?tenderId=${tender.id}`)" />
          </div>
          <div class="empty-state">
            <i class="pi pi-box"></i>
            <p>Chưa có sản phẩm đề xuất</p>
            <Button label="So sánh kỹ thuật" icon="pi pi-chart-bar" @click="router.push(`/match?tenderId=${tender.id}`)" />
          </div>
        </TabPanel>

        <TabPanel header="Lịch sử">
          <div class="empty-state">
            <i class="pi pi-history"></i>
            <p>Chưa có lịch sử thay đổi</p>
          </div>
        </TabPanel>
      </TabView>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import apiClient from '@/api/client'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import StatusBadge from '@/components/StatusBadge.vue'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const loading = ref(false)
const error = ref(null)
const tender = ref(null)
const requirements = ref([])

const workflowSteps = [
  { label: 'Bản nháp', value: 'DRAFT' },
  { label: 'Đang hoạt động', value: 'ACTIVE' },
  { label: 'Đã nộp', value: 'SUBMITTED' },
  { label: 'Đang xem xét', value: 'UNDER_REVIEW' },
  { label: 'Trúng thầu', value: 'AWARDED' }
]

const statusOrder = ['DRAFT', 'ACTIVE', 'SUBMITTED', 'UNDER_REVIEW', 'AWARDED']

function isStepActive(step) {
  return tender.value?.status === step
}

function isStepCompleted(step) {
  if (!tender.value?.status) return false
  const currentIdx = statusOrder.indexOf(tender.value.status)
  const stepIdx = statusOrder.indexOf(step)
  return stepIdx < currentIdx
}

async function fetchTender() {
  loading.value = true
  error.value = null
  try {
    const id = route.params.id
    const [tenderRes, reqRes] = await Promise.all([
      apiClient.get(`/tenders/${id}`),
      apiClient.get(`/hsmt/${id}/requirements`).catch(() => ({ data: [] }))
    ])
    tender.value = tenderRes.data
    requirements.value = reqRes.data || []
  } catch (err) {
    error.value = err.response?.data?.message || 'Không thể tải thông tin gói thầu'
  } finally {
    loading.value = false
  }
}

async function updateStatus(newStatus) {
  const statusLabels = {
    ACTIVE: 'kích hoạt',
    AWARDED: 'đánh dấu trúng thầu',
    CANCELLED: 'hủy'
  }
  const action = statusLabels[newStatus] || 'thay đổi trạng thái'

  confirm.require({
    message: `Bạn có chắc muốn ${action} gói thầu này?`,
    header: 'Xác nhận',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Đồng ý',
    rejectLabel: 'Hủy',
    accept: async () => {
      try {
        await apiClient.patch(`/tenders/${tender.value.id}/status`, { status: newStatus })
        toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã ${action} gói thầu`, life: 3000 })
        fetchTender()
      } catch (err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Thao tác thất bại', life: 5000 })
      }
    }
  })
}

async function cloneTender() {
  try {
    await apiClient.post(`/tenders/${tender.value.id}/clone`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã sao chép gói thầu', life: 3000 })
    router.push('/tenders')
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể sao chép', life: 5000 })
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}

function formatCurrency(value) {
  if (!value) return '-'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

onMounted(() => {
  fetchTender()
})
</script>

<style scoped>
.tender-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

.info-card {
  background: var(--surface-ground);
  border-radius: 12px;
  padding: 1.25rem;
}

.info-card-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--surface-border);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.info-value {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
  text-align: right;
  max-width: 60%;
}

.info-value.price {
  color: var(--success-color);
  font-weight: 700;
}

.mt-4 {
  margin-top: 1.25rem;
}

.status-workflow {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem 0;
  overflow-x: auto;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.step-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-border);
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 600;
}

.workflow-step.active .step-circle {
  background: var(--primary-color);
  color: white;
}

.workflow-step.completed .step-circle {
  background: var(--success-color);
  color: white;
}

.step-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.workflow-step.active .step-label,
.workflow-step.completed .step-label {
  color: var(--text-primary);
  font-weight: 600;
}

.step-connector {
  width: 60px;
  height: 2px;
  background: var(--surface-border);
  margin: 0 0.5rem;
  margin-bottom: 1.5rem;
}

.step-connector.completed {
  background: var(--success-color);
}

@media (max-width: 768px) {
  .tender-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
