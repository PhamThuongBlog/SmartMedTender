<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Quản lý gói thầu</h1>
        <p class="page-subtitle">Danh sách các gói thầu y tế</p>
      </div>
      <Button label="Tạo mới" icon="pi pi-plus" @click="router.push('/tenders/create')" />
    </div>

    <div class="card-container">
      <!-- Filters -->
      <div class="filters-row">
        <div class="flex-center gap-3">
          <span class="p-input-icon-left">
            <i class="pi pi-search" />
            <InputText v-model="searchQuery" placeholder="Tìm kiếm gói thầu..." @input="onSearch" style="width: 300px;" />
          </span>
          <Dropdown
            v-model="statusFilter"
            :options="statusOptions"
            optionLabel="label"
            optionValue="value"
            placeholder="Trạng thái"
            @change="onFilterChange"
            showClear
            style="width: 200px;"
          />
        </div>
        <div class="flex-center gap-3">
          <Button
            label="Xuất tất cả"
            icon="pi pi-download"
            severity="secondary"
            outlined
            size="small"
            @click="router.push('/export')"
          />
        </div>
      </div>

      <!-- Tender Table -->
      <DataTable
        :value="tenders"
        :paginator="true"
        :rows="pageSize"
        :total-records="totalRecords"
        :loading="loading"
        :lazy="true"
        data-key="id"
        @page="onPage"
        @sort="onSort"
        :rows-per-page-options="[10, 20, 50]"
        current-page-report-template="Hiển thị {first} đến {last} trong {totalRecords} gói thầu"
      >
        <Column field="bidPackageCode" header="Mã gói" sortable style="width: 120px;">
          <template #body="{ data }">
            <span class="code-text">{{ data.bidPackageCode || '-' }}</span>
          </template>
        </Column>
        <Column field="name" header="Tên gói thầu" sortable>
          <template #body="{ data }">
            <router-link :to="`/tenders/${data.id}`" class="tender-name-link">
              {{ data.name }}
            </router-link>
          </template>
        </Column>
        <Column field="procuringEntity" header="Bên mời thầu" sortable style="width: 200px;">
          <template #body="{ data }">
            {{ data.procuringEntity || '-' }}
          </template>
        </Column>
        <Column field="submissionDeadline" header="Hạn nộp" sortable style="width: 160px;">
          <template #body="{ data }">
            <span :class="{ 'deadline-expired': isExpired(data.submissionDeadline) }">
              {{ formatDate(data.submissionDeadline) }}
            </span>
          </template>
        </Column>
        <Column field="estimatedValue" header="Giá dự toán" sortable style="width: 180px;">
          <template #body="{ data }">
            {{ formatCurrency(data.estimatedValue) }}
          </template>
        </Column>
        <Column field="status" header="Trạng thái" sortable style="width: 150px;">
          <template #body="{ data }">
            <StatusBadge :status="data.status" />
          </template>
        </Column>
        <Column header="Thao tác" style="width: 180px;">
          <template #body="{ data }">
            <div class="action-buttons">
              <Button
                icon="pi pi-eye"
                severity="info"
                text
                rounded
                size="small"
                v-tooltip.top="'Xem chi tiết'"
                @click="router.push(`/tenders/${data.id}`)"
              />
              <Button
                icon="pi pi-pencil"
                severity="secondary"
                text
                rounded
                size="small"
                v-tooltip.top="'Chỉnh sửa'"
                @click="router.push(`/tenders/${data.id}/edit`)"
              />
              <Button
                icon="pi pi-copy"
                severity="secondary"
                text
                rounded
                size="small"
                v-tooltip.top="'Sao chép'"
                @click="cloneTender(data.id)"
              />
              <Button
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                size="small"
                v-tooltip.top="'Xóa'"
                @click="confirmDelete(data)"
              />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import apiClient from '@/api/client'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import StatusBadge from '@/components/StatusBadge.vue'

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const loading = ref(false)
const tenders = ref([])
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
const searchQuery = ref('')
const statusFilter = ref(null)
const sortField = ref(null)
const sortOrder = ref(null)

let searchTimeout = null

const statusOptions = [
  { label: 'Bản nháp', value: 'DRAFT' },
  { label: 'Đang hoạt động', value: 'ACTIVE' },
  { label: 'Đã nộp', value: 'SUBMITTED' },
  { label: 'Đang xem xét', value: 'UNDER_REVIEW' },
  { label: 'Trúng thầu', value: 'AWARDED' },
  { label: 'Từ chối', value: 'REJECTED' },
  { label: 'Đã hủy', value: 'CANCELLED' },
  { label: 'Hết hạn', value: 'EXPIRED' }
]

async function fetchTenders() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchQuery.value) params.search = searchQuery.value
    if (statusFilter.value) params.status = statusFilter.value
    if (sortField.value) {
      params.sort = `${sortField.value},${sortOrder.value === -1 ? 'desc' : 'asc'}`
    }

    const response = await apiClient.get('/tenders', { params })
    if (response.data.content) {
      tenders.value = response.data.content
      totalRecords.value = response.data.totalElements
    } else {
      tenders.value = Array.isArray(response.data) ? response.data : []
      totalRecords.value = tenders.value.length
    }
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: 'Không thể tải danh sách gói thầu',
      life: 5000
    })
  } finally {
    loading.value = false
  }
}

function onSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    fetchTenders()
  }, 300)
}

function onFilterChange() {
  currentPage.value = 0
  fetchTenders()
}

function onPage(event) {
  currentPage.value = event.page
  pageSize.value = event.rows
  fetchTenders()
}

function onSort(event) {
  sortField.value = event.sortField
  sortOrder.value = event.sortOrder
  fetchTenders()
}

async function cloneTender(id) {
  try {
    await apiClient.post(`/tenders/${id}/clone`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã sao chép gói thầu', life: 3000 })
    fetchTenders()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể sao chép gói thầu', life: 5000 })
  }
}

function confirmDelete(tender) {
  confirm.require({
    message: `Bạn có chắc muốn xóa gói thầu "${tender.name}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Xóa',
    rejectLabel: 'Hủy',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await apiClient.delete(`/tenders/${tender.id}`)
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa gói thầu', life: 3000 })
        fetchTenders()
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa gói thầu', life: 5000 })
      }
    }
  })
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatCurrency(value) {
  if (!value) return '-'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

function isExpired(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

onMounted(() => {
  fetchTenders()
})
</script>

<style scoped>
.filters-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.code-text {
  font-family: 'Courier New', monospace;
  font-size: 0.8125rem;
  color: var(--text-secondary);
  background: var(--surface-ground);
  padding: 0.125rem 0.5rem;
  border-radius: 4px;
}

.tender-name-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
}

.tender-name-link:hover {
  text-decoration: underline;
}

.deadline-expired {
  color: var(--danger-color);
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 0.25rem;
}
</style>
