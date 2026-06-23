<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Thư viện tài liệu</h1>
        <p class="page-subtitle">Quản lý chứng chỉ CO/CQ/ISO/CE/FDA/Catalogue sản phẩm</p>
      </div>
      <Button label="Tải lên tài liệu" icon="pi pi-upload" @click="showUploadDialog = true" />
    </div>

    <div class="card-container">
      <div class="filters-row">
        <span class="p-input-icon-left">
          <i class="pi pi-search" />
          <InputText v-model="searchQuery" placeholder="Tìm tài liệu..." @input="onSearch" style="width: 280px;" />
        </span>
        <Dropdown v-model="filterProductId" :options="productOptions" optionLabel="label" optionValue="value" placeholder="Lọc theo sản phẩm" @change="onFilterChange" showClear style="width: 260px;" />
        <Dropdown v-model="filterDocType" :options="documentTypeOptions" placeholder="Loại tài liệu" @change="onFilterChange" showClear style="width: 180px;" />
        <Dropdown v-model="filterStatus" :options="statusOptions" placeholder="Trạng thái" @change="onFilterChange" showClear style="width: 150px;" />
      </div>

      <DataTable :value="documents" :loading="loading" :paginator="true" :rows="pageSize" :total-records="totalRecords" :lazy="true" data-key="id" @page="onPage" @row-click="onRowClick" row-hover>
        <Column field="documentName" header="Tên tài liệu">
          <template #body="{ data }">
            <div class="doc-cell">
              <span class="doc-name">{{ data.documentName }}</span>
              <span class="doc-product">{{ data.product?.name || '-' }}</span>
            </div>
          </template>
        </Column>
        <Column field="documentType" header="Loại" style="width: 130px;">
          <template #body="{ data }">
            <Tag :value="data.documentType" :severity="getDocTypeSeverity(data.documentType)" />
          </template>
        </Column>
        <Column field="issuingAuthority" header="Cơ quan cấp" style="width: 200px;">
          <template #body="{ data }">{{ data.issuingAuthority || '-' }}</template>
        </Column>
        <Column field="issueDate" header="Ngày cấp" style="width: 110px;">
          <template #body="{ data }">{{ formatDate(data.issueDate) }}</template>
        </Column>
        <Column field="expiryDate" header="Hết hạn" style="width: 120px;">
          <template #body="{ data }">
            <span :class="{ 'expiry-critical': isCriticallyExpired(data.expiryDate), 'expiry-warning': isNearExpiry(data.expiryDate) }">
              {{ formatDate(data.expiryDate) }}
            </span>
          </template>
        </Column>
        <Column field="status" header="Trạng thái" style="width: 110px;">
          <template #body="{ data }">
            <Tag :value="data.status || 'ACTIVE'" :severity="data.status === 'ACTIVE' ? 'success' : 'warning'" />
          </template>
        </Column>
        <Column header="Thao tác" style="width: 150px;">
          <template #body="{ data }">
            <div class="flex-center gap-2">
              <Button icon="pi pi-download" severity="secondary" text rounded size="small" @click.stop="downloadDoc(data)" v-tooltip.top="'Tải xuống'" />
              <Button icon="pi pi-pencil" severity="secondary" text rounded size="small" @click.stop="openEdit(data)" v-tooltip.top="'Sửa'" />
              <Button icon="pi pi-trash" severity="danger" text rounded size="small" @click.stop="confirmDelete(data)" v-tooltip.top="'Xóa'" />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- Upload Dialog -->
    <Dialog v-model:visible="showUploadDialog" header="Tải lên tài liệu mới" :modal="true" :style="{ width: '550px' }">
      <form @submit.prevent="uploadDoc" class="form-stack">
        <div class="form-field">
          <label>Sản phẩm <span class="required">*</span></label>
          <Dropdown v-model="uploadForm.productId" :options="productOptions" optionLabel="label" optionValue="value" placeholder="Chọn sản phẩm" filter fluid />
        </div>
        <div class="form-field">
          <label>Loại tài liệu <span class="required">*</span></label>
          <Dropdown v-model="uploadForm.documentType" :options="documentTypeOptions" placeholder="Chọn loại" fluid />
        </div>
        <div class="form-field">
          <label>Tên tài liệu <span class="required">*</span></label>
          <InputText v-model="uploadForm.documentName" placeholder="Nhập tên tài liệu" fluid />
        </div>
        <div class="form-field">
          <label>Mô tả</label>
          <InputText v-model="uploadForm.description" placeholder="Mô tả ngắn" fluid />
        </div>
        <div class="form-field">
          <label>Cơ quan cấp</label>
          <InputText v-model="uploadForm.issuingAuthority" placeholder="Cơ quan cấp chứng chỉ" fluid />
        </div>
        <div class="form-row">
          <div class="form-field">
            <label>Ngày cấp</label>
            <Calendar v-model="uploadForm.issueDate" dateFormat="dd/mm/yy" fluid />
          </div>
          <div class="form-field">
            <label>Ngày hết hạn</label>
            <Calendar v-model="uploadForm.expiryDate" dateFormat="dd/mm/yy" fluid />
          </div>
        </div>
        <div class="form-field">
          <label>Tệp <span class="required">*</span></label>
          <input type="file" ref="fileInput" accept=".pdf,.docx,.doc,.png,.jpg,.jpeg" class="file-input" />
        </div>
        <div class="flex-between mt-3">
          <Button label="Hủy" severity="secondary" outlined @click="showUploadDialog = false" />
          <Button type="submit" label="Tải lên" icon="pi pi-upload" :loading="uploading" />
        </div>
      </form>
    </Dialog>

    <!-- Edit Dialog -->
    <Dialog v-model:visible="showEditDialog" header="Sửa tài liệu" :modal="true" :style="{ width: '500px' }">
      <form @submit.prevent="updateDoc" class="form-stack">
        <div class="form-field">
          <label>Tên tài liệu</label>
          <InputText v-model="editForm.documentName" fluid />
        </div>
        <div class="form-field">
          <label>Loại tài liệu</label>
          <Dropdown v-model="editForm.documentType" :options="documentTypeOptions" fluid />
        </div>
        <div class="form-field">
          <label>Sản phẩm</label>
          <Dropdown v-model="editForm.productId" :options="productOptions" optionLabel="label" optionValue="value" filter fluid />
        </div>
        <div class="form-field">
          <label>Mô tả</label>
          <InputText v-model="editForm.description" fluid />
        </div>
        <div class="form-field">
          <label>Cơ quan cấp</label>
          <InputText v-model="editForm.issuingAuthority" fluid />
        </div>
        <div class="form-row">
          <div class="form-field">
            <label>Ngày cấp</label>
            <Calendar v-model="editForm.issueDate" dateFormat="dd/mm/yy" fluid />
          </div>
          <div class="form-field">
            <label>Ngày hết hạn</label>
            <Calendar v-model="editForm.expiryDate" dateFormat="dd/mm/yy" fluid />
          </div>
        </div>
        <div class="flex-between mt-3">
          <Button label="Hủy" severity="secondary" outlined @click="showEditDialog = false" />
          <Button type="submit" label="Cập nhật" icon="pi pi-check" :loading="updating" />
        </div>
      </form>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import apiClient from '@/api/client'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'

const toast = useToast()
const confirm = useConfirm()

const loading = ref(false)
const documents = ref([])
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
const searchQuery = ref('')
const filterProductId = ref(null)
const filterDocType = ref(null)
const filterStatus = ref(null)
let searchTimer = null

const productOptions = ref([])
const documentTypeOptions = ['CO', 'CQ', 'ISO_13485', 'ISO_9001', 'CE', 'FDA', 'CATALOGUE', 'OTHER']
const statusOptions = ['ACTIVE', 'INACTIVE']

const showUploadDialog = ref(false)
const showEditDialog = ref(false)
const uploading = ref(false)
const updating = ref(false)
const fileInput = ref(null)
const editingDoc = ref(null)

const uploadForm = ref({ productId: null, documentType: '', documentName: '', description: '', issuingAuthority: '', issueDate: null, expiryDate: null })
const editForm = ref({ documentName: '', documentType: '', productId: null, description: '', issuingAuthority: '', issueDate: null, expiryDate: null })

async function fetchProducts() {
  try {
    const res = await apiClient.get('/products', { params: { size: 1000 } })
    const list = res.data.content || (Array.isArray(res.data) ? res.data : [])
    productOptions.value = list.map(p => ({ label: p.name, value: p.id }))
  } catch (e) {
    // silent
  }
}

async function fetchDocuments() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchQuery.value) params.search = searchQuery.value
    if (filterProductId.value) params.productId = filterProductId.value
    if (filterDocType.value) params.documentType = filterDocType.value
    if (filterStatus.value) params.status = filterStatus.value

    const res = await apiClient.get('/documents', { params })
    documents.value = res.data.content || []
    totalRecords.value = res.data.totalElements || 0
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách tài liệu', life: 5000 })
  } finally {
    loading.value = false
  }
}

function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 0; fetchDocuments() }, 300)
}

function onFilterChange() {
  currentPage.value = 0
  fetchDocuments()
}

function onPage(event) {
  currentPage.value = event.page
  pageSize.value = event.rows
  fetchDocuments()
}

function onRowClick(event) {
  downloadDoc(event.data)
}

async function uploadDoc() {
  if (!uploadForm.value.productId || !uploadForm.value.documentType || !uploadForm.value.documentName) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Vui lòng chọn sản phẩm, loại và nhập tên tài liệu', life: 3000 })
    return
  }
  if (!fileInput.value?.files?.length) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Vui lòng chọn tệp', life: 3000 })
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', fileInput.value.files[0])
    Object.entries(uploadForm.value).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') {
        formData.append(k, v instanceof Date ? v.toISOString().split('T')[0] : v)
      }
    })

    await apiClient.post('/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải lên tài liệu', life: 3000 })
    showUploadDialog.value = false
    uploadForm.value = { productId: null, documentType: '', documentName: '', description: '', issuingAuthority: '', issueDate: null, expiryDate: null }
    if (fileInput.value) fileInput.value.value = ''
    fetchDocuments()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải lên', life: 5000 })
  } finally {
    uploading.value = false
  }
}

function openEdit(doc) {
  editingDoc.value = doc
  editForm.value = {
    documentName: doc.documentName || '',
    documentType: doc.documentType || '',
    productId: doc.product?.id || null,
    description: doc.description || '',
    issuingAuthority: doc.issuingAuthority || '',
    issueDate: doc.issueDate ? new Date(doc.issueDate) : null,
    expiryDate: doc.expiryDate ? new Date(doc.expiryDate) : null
  }
  showEditDialog.value = true
}

async function updateDoc() {
  updating.value = true
  try {
    await apiClient.put(`/documents/${editingDoc.value.id}`, editForm.value)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật tài liệu', life: 3000 })
    showEditDialog.value = false
    fetchDocuments()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật', life: 5000 })
  } finally {
    updating.value = false
  }
}

function confirmDelete(doc) {
  confirm.require({
    message: `Bạn có chắc muốn xóa tài liệu "${doc.documentName}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Xóa',
    rejectLabel: 'Hủy',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await apiClient.delete(`/documents/${doc.id}`)
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa tài liệu', life: 3000 })
        fetchDocuments()
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa', life: 5000 })
      }
    }
  })
}

function downloadDoc(doc) {
  apiClient.get(`/documents/${doc.id}/download`, { responseType: 'blob' })
    .then(res => {
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', doc.fileName || 'document')
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    })
    .catch(() => {
      toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải xuống (tệp mẫu chưa tồn tại)', life: 5000 })
    })
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN')
}

function getDocTypeSeverity(type) {
  const map = { CO: 'primary', CQ: 'info', ISO_13485: 'success', ISO_9001: 'success', CE: 'warning', FDA: 'danger', CATALOGUE: 'secondary', OTHER: 'secondary' }
  return map[type] || 'secondary'
}

function isNearExpiry(dateStr) {
  if (!dateStr) return false
  const days = Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24))
  return days <= 90 && days > 30
}

function isCriticallyExpired(dateStr) {
  if (!dateStr) return false
  const days = Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24))
  return days <= 30 && days > 0
}

onMounted(() => {
  fetchProducts()
  fetchDocuments()
})
</script>

<style scoped>
.filters-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.form-field label {
  font-size: 0.875rem;
  font-weight: 600;
}

.required { color: var(--danger-color); }
.mt-3 { margin-top: 0.75rem; }

.doc-cell {
  display: flex;
  flex-direction: column;
}

.doc-name {
  font-weight: 500;
  color: var(--primary-color);
  cursor: pointer;
}

.doc-product {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.expiry-warning {
  color: #f59e0b;
  font-weight: 600;
}

.expiry-critical {
  color: var(--danger-color);
  font-weight: 600;
}

.file-input {
  padding: 0.5rem 0;
}

.gap-2 { gap: 0.5rem; }
</style>
