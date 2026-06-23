<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Xem xét yêu cầu kỹ thuật</h1>
        <p class="page-subtitle">Kiểm tra, chỉnh sửa và phê duyệt các yêu cầu được trích xuất từ HSMT</p>
      </div>
      <div class="flex-center gap-3">
        <Dropdown
          v-model="selectedTender"
          :options="tenders"
          optionLabel="name"
          optionValue="id"
          placeholder="Chọn gói thầu"
          :loading="loadingTenders"
          @change="fetchRequirements"
          style="min-width: 250px;"
        />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Empty State -->
    <div v-else-if="!selectedTender" class="card-container empty-state-page">
      <i class="pi pi-file-pdf" style="font-size: 3rem; color: var(--text-secondary);"></i>
      <h3>Chưa có yêu cầu kỹ thuật</h3>
      <p>Vui lòng chọn gói thầu hoặc tải lên HSMT để bắt đầu trích xuất yêu cầu.</p>
      <Button label="Tải lên HSMT" icon="pi pi-upload" @click="router.push('/hsmt/upload')" />
    </div>

    <template v-else>
      <!-- Summary Bar -->
      <div class="summary-bar">
        <div class="summary-item">
          <span class="summary-label">Tổng số yêu cầu</span>
          <span class="summary-value">{{ requirements.length }}</span>
        </div>
        <Divider layout="vertical" />
        <div class="summary-item">
          <span class="summary-label">Đã duyệt</span>
          <span class="summary-value success">{{ approvedCount }}</span>
        </div>
        <Divider layout="vertical" />
        <div class="summary-item">
          <span class="summary-label">Đã từ chối</span>
          <span class="summary-value danger">{{ rejectedCount }}</span>
        </div>
        <Divider layout="vertical" />
        <div class="summary-item">
          <span class="summary-label">Chờ xử lý</span>
          <span class="summary-value warning">{{ pendingCount }}</span>
        </div>
      </div>

      <!-- Requirements Table -->
      <div class="card-container">
        <div class="flex-between mb-4">
          <div class="flex-center gap-3">
            <InputText v-model="searchQuery" placeholder="Tìm kiếm yêu cầu..." style="width: 300px;" />
            <Dropdown v-model="statusFilter" :options="filterOptions" optionLabel="label" optionValue="value" placeholder="Lọc trạng thái" showClear style="width: 180px;" />
          </div>
          <Button label="Phê duyệt tất cả" icon="pi pi-check-circle" severity="success" @click="approveAll" :disabled="pendingCount === 0" />
        </div>

        <DataTable
          :value="filteredRequirements"
          :paginator="true"
          :rows="10"
          editMode="row"
          @row-edit-save="onRowEditSave"
        >
          <Column field="description" header="Mô tả yêu cầu" sortable>
            <template #editor="{ data }">
              <InputText v-model="data.description" fluid />
            </template>
          </Column>
          <Column field="type" header="Loại" sortable style="width: 120px;">
            <template #body="{ data }">
              <Tag :value="data.type" />
            </template>
            <template #editor="{ data }">
              <Dropdown v-model="data.type" :options="requirementTypes" style="width: 100%;" />
            </template>
          </Column>
          <Column field="operator" header="Toán tử" style="width: 100px;">
            <template #editor="{ data }">
              <Dropdown v-model="data.operator" :options="operators" style="width: 100%;" />
            </template>
          </Column>
          <Column field="value" header="Giá trị" style="width: 100px;">
            <template #editor="{ data }">
              <InputText v-model="data.value" style="width: 100%;" />
            </template>
          </Column>
          <Column field="unit" header="Đơn vị" style="width: 80px;">
            <template #editor="{ data }">
              <InputText v-model="data.unit" style="width: 100%;" />
            </template>
          </Column>
          <Column field="mandatory" header="Bắt buộc" style="width: 100px;">
            <template #body="{ data }">
              <i v-if="data.mandatory" class="pi pi-check" style="color: var(--success-color);"></i>
              <i v-else class="pi pi-times" style="color: var(--text-secondary);"></i>
            </template>
            <template #editor="{ data }">
              <input type="checkbox" v-model="data.mandatory" class="toggle-checkbox" />
            </template>
          </Column>
          <Column field="confidence" header="Độ tin cậy" style="width: 120px;">
            <template #body="{ data }">
              <span :class="{ 'low-confidence': (data.confidence || 0) < 0.7 }">
                {{ data.confidence ? (data.confidence * 100).toFixed(0) + '%' : 'N/A' }}
              </span>
            </template>
          </Column>
          <Column field="status" header="Trạng thái" style="width: 120px;">
            <template #body="{ data }">
              <Tag
                :value="statusLabel(data.status)"
                :severity="statusSeverity(data.status)"
              />
            </template>
          </Column>
          <Column :rowEditor="true" header="Thao tác" style="width: 200px;">
            <template #body="{ data, index }">
              <div class="action-buttons">
                <Button
                  v-if="data.status !== 'APPROVED'"
                  icon="pi pi-check"
                  severity="success"
                  text
                  rounded
                  size="small"
                  v-tooltip.top="'Phê duyệt'"
                  @click="approveRequirement(index)"
                />
                <Button
                  v-if="data.status !== 'REJECTED'"
                  icon="pi pi-times"
                  severity="danger"
                  text
                  rounded
                  size="small"
                  v-tooltip.top="'Từ chối'"
                  @click="rejectRequirement(index)"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import apiClient from '@/api/client'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Divider from 'primevue/divider'
import ProgressSpinner from 'primevue/progressspinner'

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const loading = ref(false)
const loadingTenders = ref(false)
const tenders = ref([])
const selectedTender = ref(null)
const requirements = ref([])
const searchQuery = ref('')
const statusFilter = ref(null)

const requirementTypes = ['TECHNICAL', 'CLINICAL', 'CERTIFICATION', 'EXPERIENCE', 'FINANCIAL', 'OTHER']
const operators = ['>=', '<=', '>', '<', '=', '!=', 'BETWEEN', 'CONTAINS', 'IN']
const filterOptions = [
  { label: 'Tất cả', value: null },
  { label: 'Đã trích xuất', value: 'EXTRACTED' },
  { label: 'Đã duyệt', value: 'VERIFIED' },
  { label: 'Đã từ chối', value: 'REJECTED' }
]

const approvedCount = computed(() => requirements.value.filter(r => r.status === 'VERIFIED').length)
const rejectedCount = computed(() => requirements.value.filter(r => r.status === 'REJECTED').length)
const pendingCount = computed(() => requirements.value.filter(r => r.status === 'EXTRACTED' || !r.status).length)

const filteredRequirements = computed(() => {
  let list = requirements.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(r => r.description?.toLowerCase().includes(q))
  }
  if (statusFilter.value) {
    list = list.filter(r => (r.status || 'EXTRACTED') === statusFilter.value)
  }
  return list
})

function statusLabel(status) {
  const map = { VERIFIED: 'Đã duyệt', REJECTED: 'Từ chối', EXTRACTED: 'Đã trích xuất', MATCHED: 'Đã đối chiếu' }
  return map[status] || 'Đã trích xuất'
}

function statusSeverity(status) {
  const map = { VERIFIED: 'success', REJECTED: 'danger', EXTRACTED: 'warning', MATCHED: 'info' }
  return map[status] || 'warning'
}

async function fetchTenders() {
  loadingTenders.value = true
  try {
    const response = await apiClient.get('/tenders', { params: { size: 100 } })
    tenders.value = response.data.content || response.data || []
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách gói thầu', life: 5000 })
  } finally {
    loadingTenders.value = false
  }
}

async function fetchRequirements() {
  if (!selectedTender.value) return
  loading.value = true
  try {
    const response = await apiClient.get(`/hsmt/${selectedTender.value}/requirements`)
    requirements.value = (response.data || []).map(r => ({
      ...r,
      status: r.status || 'EXTRACTED'
    }))
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải yêu cầu', life: 5000 })
  } finally {
    loading.value = false
  }
}

async function approveRequirement(index) {
  const req = requirements.value[index]
  if (!req || !req.id) return
  try {
    const res = await apiClient.post(`/hsmt/requirements/${req.id}/approve`)
    requirements.value[index] = { ...requirements.value[index], ...res.data, status: res.data.status }
    toast.add({ severity: 'success', summary: 'Đã duyệt', detail: 'Đã phê duyệt yêu cầu', life: 2000 })
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: error.response?.data?.message || 'Không thể phê duyệt', life: 5000 })
  }
}

async function rejectRequirement(index) {
  const req = requirements.value[index]
  if (!req || !req.id) return
  try {
    const res = await apiClient.post(`/hsmt/requirements/${req.id}/reject`, { reason: 'Từ chối bởi người kiểm duyệt' })
    requirements.value[index] = { ...requirements.value[index], ...res.data, status: res.data.status }
    toast.add({ severity: 'info', summary: 'Đã từ chối', detail: 'Đã từ chối yêu cầu', life: 2000 })
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: error.response?.data?.message || 'Không thể từ chối', life: 5000 })
  }
}

async function saveRequirementEdit(rowData) {
  try {
    await apiClient.put(`/hsmt/requirements/${rowData.id}`, {
      description: rowData.description,
      type: rowData.type,
      operator: rowData.operator,
      value: rowData.value,
      unit: rowData.unit,
      mandatory: rowData.mandatory
    })
    toast.add({ severity: 'success', summary: 'Đã lưu', detail: 'Đã cập nhật yêu cầu', life: 2000 })
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu thay đổi', life: 5000 })
  }
}

async function approveAll() {
  confirm.require({
    message: 'Bạn có chắc muốn phê duyệt tất cả yêu cầu đang chờ xử lý?',
    header: 'Phê duyệt tất cả',
    icon: 'pi pi-check-circle',
    acceptLabel: 'Phê duyệt',
    rejectLabel: 'Hủy',
    accept: async () => {
      try {
        const res = await apiClient.post('/hsmt/requirements/batch-approve', null, {
          params: { tenderId: selectedTender.value }
        })
        toast.add({ severity: 'success', summary: 'Thành công', detail: res.data?.message || 'Đã phê duyệt tất cả yêu cầu', life: 3000 })
        fetchRequirements()
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể phê duyệt hàng loạt', life: 5000 })
      }
    }
  })
}

function onRowEditSave(event) {
  saveRequirementEdit(event.newData)
}

onMounted(() => {
  fetchTenders()
})
</script>

<style scoped>
.summary-bar {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem 1.5rem;
  background: var(--surface-card);
  border-radius: 12px;
  border: 1px solid var(--surface-border);
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.summary-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.summary-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.summary-value.success { color: var(--success-color); }
.summary-value.danger { color: var(--danger-color); }
.summary-value.warning { color: var(--warning-color); }

.empty-state-page {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-state-page h3 {
  margin: 1rem 0 0.5rem;
}

.empty-state-page p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}

.low-confidence {
  color: var(--danger-color);
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 0.25rem;
}

.mb-4 { margin-bottom: 1rem; }
</style>
