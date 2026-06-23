<template>
  <div class="page-container">
    <div v-if="loading" class="loading-container">
      <ProgressSpinner />
    </div>

    <div v-else-if="error" class="error-container">
      <i class="pi pi-exclamation-triangle"></i>
      <p>{{ error }}</p>
      <Button label="Thử lại" icon="pi pi-refresh" @click="fetchProduct" />
    </div>

    <template v-else-if="product">
      <div class="page-header">
        <div class="flex-center gap-3">
          <Button icon="pi pi-arrow-left" severity="secondary" text rounded @click="router.push('/products')" />
          <div>
            <h1 class="page-title">{{ product.name }}</h1>
            <p class="page-subtitle">
              {{ product.manufacturer }} - {{ product.brand || product.model || 'N/A' }}
            </p>
          </div>
        </div>
        <div class="flex-center gap-3">
          <Button
            :label="editMode ? 'Hủy chỉnh sửa' : 'Chỉnh sửa'"
            :icon="editMode ? 'pi pi-times' : 'pi pi-pencil'"
            :severity="editMode ? 'secondary' : 'primary'"
            @click="editMode = !editMode"
          />
          <Button
            v-if="!editMode"
            label="So sánh kỹ thuật"
            icon="pi pi-chart-bar"
            severity="info"
            outlined
            @click="router.push('/match')"
          />
        </div>
      </div>

      <div class="product-detail-grid">
        <!-- Basic Info Card -->
        <div class="card-container">
          <h3 class="card-title">Thông tin cơ bản</h3>
          <template v-if="!editMode">
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">Tên sản phẩm</span>
                <span class="detail-value">{{ product.name }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Hãng sản xuất</span>
                <span class="detail-value">{{ product.manufacturer || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Thương hiệu</span>
                <span class="detail-value">{{ product.brand || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Model</span>
                <span class="detail-value">{{ product.model || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Xuất xứ</span>
                <span class="detail-value">{{ product.originCountry || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Danh mục</span>
                <Tag :value="product.category" severity="info" />
              </div>
              <div class="detail-item">
                <span class="detail-label">Trạng thái</span>
                <StatusBadge :status="product.status" />
              </div>
              <div class="detail-item full-width">
                <span class="detail-label">Mô tả</span>
                <span class="detail-value">{{ product.description || '-' }}</span>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="edit-form">
              <div class="form-row"><InputText v-model="editForm.name" placeholder="Tên sản phẩm" fluid /></div>
              <div class="form-row"><InputText v-model="editForm.manufacturer" placeholder="Hãng sản xuất" /></div>
              <div class="form-row"><InputText v-model="editForm.brand" placeholder="Thương hiệu" /></div>
              <div class="form-row"><InputText v-model="editForm.model" placeholder="Model" /></div>
              <div class="form-row"><InputText v-model="editForm.originCountry" placeholder="Xuất xứ" /></div>
              <div class="form-row"><Dropdown v-model="editForm.category" :options="categories" placeholder="Danh mục" /></div>
              <div class="form-row full-width"><Textarea v-model="editForm.description" placeholder="Mô tả" rows="3" autoResize /></div>
              <Button label="Lưu thay đổi" icon="pi pi-check" @click="saveChanges" :loading="saving" />
            </div>
          </template>
        </div>

        <!-- Registration Card -->
        <div class="card-container">
          <h3 class="card-title">Đăng ký & Chứng chỉ</h3>
          <template v-if="!editMode">
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">Số đăng ký</span>
                <span class="detail-value">{{ product.registrationNumber || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Ngày cấp</span>
                <span class="detail-value">{{ formatDate(product.registrationIssueDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Ngày hết hạn</span>
                <span class="detail-value" :class="{ 'expired-text': isExpired(product.registrationExpiryDate) }">
                  {{ formatDate(product.registrationExpiryDate) }}
                </span>
              </div>
            </div>
            <Divider />
            <h4 style="margin-bottom: 0.75rem; font-size: 0.875rem; color: var(--text-secondary);">Chứng chỉ</h4>
            <div class="cert-grid">
              <div class="cert-badge" :class="{ active: product.hasIso }">
                <i :class="product.hasIso ? 'pi pi-check-circle' : 'pi pi-circle'" />
                <span>ISO</span>
              </div>
              <div class="cert-badge" :class="{ active: product.hasFda }">
                <i :class="product.hasFda ? 'pi pi-check-circle' : 'pi pi-circle'" />
                <span>FDA</span>
              </div>
              <div class="cert-badge" :class="{ active: product.hasCe }">
                <i :class="product.hasCe ? 'pi pi-check-circle' : 'pi pi-circle'" />
                <span>CE</span>
              </div>
              <div class="cert-badge" :class="{ active: product.hasCoCq }">
                <i :class="product.hasCoCq ? 'pi pi-check-circle' : 'pi pi-circle'" />
                <span>CO/CQ</span>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="edit-form">
              <div class="form-row"><InputText v-model="editForm.registrationNumber" placeholder="Số đăng ký" /></div>
              <div class="form-row"><Calendar v-model="editForm.registrationIssueDate" dateFormat="dd/mm/yy" placeholder="Ngày cấp" /></div>
              <div class="form-row"><Calendar v-model="editForm.registrationExpiryDate" dateFormat="dd/mm/yy" placeholder="Ngày hết hạn" /></div>
              <div class="cert-edit">
                <div class="flex-center gap-2"><Checkbox v-model="editForm.hasIso" :binary="true" inputId="eIso" /><label for="eIso">ISO</label></div>
                <div class="flex-center gap-2"><Checkbox v-model="editForm.hasFda" :binary="true" inputId="eFda" /><label for="eFda">FDA</label></div>
                <div class="flex-center gap-2"><Checkbox v-model="editForm.hasCe" :binary="true" inputId="eCe" /><label for="eCe">CE</label></div>
                <div class="flex-center gap-2"><Checkbox v-model="editForm.hasCoCq" :binary="true" inputId="eCoCq" /><label for="eCoCq">CO/CQ</label></div>
              </div>
            </div>
          </template>
        </div>

        <!-- Technical Specs Card -->
        <div class="card-container full-width-card">
          <h3 class="card-title">Thông số kỹ thuật</h3>
          <div v-if="product.technicalSpecs && Object.keys(product.technicalSpecs).length > 0" class="specs-table">
            <div v-for="(value, key) in product.technicalSpecs" :key="key" class="spec-row">
              <span class="spec-key">{{ key }}</span>
              <span class="spec-value">{{ value }}</span>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="pi pi-info-circle"></i>
            <p>Chưa có thông số kỹ thuật</p>
          </div>
        </div>

        <!-- Metadata Card -->
        <div class="card-container">
          <h3 class="card-title">Thông tin hệ thống</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">Ngày tạo</span>
              <span class="detail-value">{{ formatDate(product.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Cập nhật lần cuối</span>
              <span class="detail-value">{{ formatDate(product.updatedAt) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">ID</span>
              <span class="detail-value mono">{{ product.id }}</span>
            </div>
          </div>
        </div>

        <!-- Documents Card -->
        <div class="card-container full-width-card">
          <div class="flex-between mb-3">
            <h3 class="card-title" style="border: none; margin: 0; padding: 0;">Tài liệu sản phẩm (CO/CQ/ISO/CE/FDA/Catalogue)</h3>
            <Button label="Tải lên" icon="pi pi-upload" size="small" @click="showDocUpload = true" />
          </div>
          <DataTable v-if="productDocuments.length > 0" :value="productDocuments" :paginator="true" :rows="5">
            <Column field="documentName" header="Tên tài liệu" />
            <Column field="documentType" header="Loại" style="width: 130px;">
              <template #body="{ data }">
                <Tag :value="data.documentType" :severity="getDocTypeSeverity(data.documentType)" />
              </template>
            </Column>
            <Column field="issueDate" header="Ngày cấp" style="width: 110px;">
              <template #body="{ data }">{{ formatDate(data.issueDate) }}</template>
            </Column>
            <Column field="expiryDate" header="Hết hạn" style="width: 120px;">
              <template #body="{ data }">
                <span :class="{ 'expiry-warning': isNearExpiry(data.expiryDate) }">{{ formatDate(data.expiryDate) }}</span>
              </template>
            </Column>
            <Column header="Thao tác" style="width: 120px;">
              <template #body="{ data }">
                <div class="flex-center gap-2">
                  <Button icon="pi pi-download" severity="secondary" text rounded size="small" @click="downloadProductDoc(data)" v-tooltip.top="'Tải xuống'" />
                  <Button icon="pi pi-trash" severity="danger" text rounded size="small" @click="deleteProductDoc(data)" v-tooltip.top="'Xóa'" />
                </div>
              </template>
            </Column>
          </DataTable>
          <div v-else class="empty-state">
            <i class="pi pi-file"></i>
            <p>Chưa có tài liệu nào. Tải lên CO, CQ, ISO, CE, FDA hoặc Catalogue cho sản phẩm này.</p>
          </div>
        </div>
      </div>

      <!-- Upload Document Dialog -->
      <Dialog v-model:visible="showDocUpload" header="Tải lên tài liệu sản phẩm" :modal="true" :style="{ width: '500px' }">
        <form @submit.prevent="uploadProductDoc" class="form-stack">
          <div class="form-field"><label>Loại tài liệu <span class="required">*</span></label>
            <Dropdown v-model="docForm.documentType" :options="docTypeOptions" placeholder="Chọn loại" fluid />
          </div>
          <div class="form-field"><label>Tên tài liệu <span class="required">*</span></label>
            <InputText v-model="docForm.documentName" placeholder="Nhập tên" fluid />
          </div>
          <div class="form-row">
            <div class="form-field"><label>Ngày cấp</label><Calendar v-model="docForm.issueDate" dateFormat="dd/mm/yy" fluid /></div>
            <div class="form-field"><label>Ngày hết hạn</label><Calendar v-model="docForm.expiryDate" dateFormat="dd/mm/yy" fluid /></div>
          </div>
          <div class="form-field"><label>Tệp <span class="required">*</span></label>
            <input type="file" ref="docFileInput" accept=".pdf,.docx,.doc,.png,.jpg,.jpeg" class="file-input" />
          </div>
          <div class="flex-between"><Button label="Hủy" severity="secondary" outlined @click="showDocUpload = false" /><Button type="submit" label="Tải lên" icon="pi pi-upload" :loading="uploadingDoc" /></div>
        </form>
      </Dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import apiClient from '@/api/client'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Divider from 'primevue/divider'
import ProgressSpinner from 'primevue/progressspinner'
import StatusBadge from '@/components/StatusBadge.vue'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const loading = ref(false)
const saving = ref(false)
const error = ref(null)
const product = ref(null)
const editMode = ref(false)

const categories = ['Thiết bị y tế', 'Dược phẩm', 'Vật tư tiêu hao', 'Hóa chất xét nghiệm', 'Thiết bị chẩn đoán hình ảnh', 'Khác']

const editForm = ref({})

// Document management
const productDocuments = ref([])
const showDocUpload = ref(false)
const uploadingDoc = ref(false)
const docFileInput = ref(null)
const docForm = ref({ documentType: '', documentName: '', issueDate: null, expiryDate: null })
const docTypeOptions = ['CO', 'CQ', 'ISO_13485', 'ISO_9001', 'CE', 'FDA', 'CATALOGUE', 'OTHER']

async function fetchProductDocuments() {
  if (!product.value?.id) return
  try {
    const res = await apiClient.get(`/products/${product.value.id}/documents`)
    productDocuments.value = Array.isArray(res.data) ? res.data : (res.data?.content || [])
  } catch (e) { /* silent */ }
}

async function uploadProductDoc() {
  if (!docForm.value.documentType || !docForm.value.documentName) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Vui lòng chọn loại và nhập tên tài liệu', life: 3000 })
    return
  }
  if (!docFileInput.value?.files?.length) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Vui lòng chọn tệp', life: 3000 })
    return
  }
  uploadingDoc.value = true
  try {
    const formData = new FormData()
    formData.append('file', docFileInput.value.files[0])
    formData.append('productId', product.value.id)
    formData.append('documentType', docForm.value.documentType)
    formData.append('documentName', docForm.value.documentName)
    if (docForm.value.issueDate) formData.append('issueDate', docForm.value.issueDate.toISOString().split('T')[0])
    if (docForm.value.expiryDate) formData.append('expiryDate', docForm.value.expiryDate.toISOString().split('T')[0])

    await apiClient.post('/documents', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải lên tài liệu', life: 3000 })
    showDocUpload.value = false
    docForm.value = { documentType: '', documentName: '', issueDate: null, expiryDate: null }
    if (docFileInput.value) docFileInput.value.value = ''
    fetchProductDocuments()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải lên', life: 5000 })
  } finally {
    uploadingDoc.value = false
  }
}

function downloadProductDoc(doc) {
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
    .catch(() => toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải xuống', life: 5000 }))
}

async function deleteProductDoc(doc) {
  try {
    await apiClient.delete(`/documents/${doc.id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa tài liệu', life: 3000 })
    fetchProductDocuments()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa', life: 5000 })
  }
}

function isNearExpiry(dateStr) {
  if (!dateStr) return false
  const days = Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24))
  return days <= 90 && days > 0
}

function getDocTypeSeverity(type) {
  const map = { CO: 'primary', CQ: 'info', ISO_13485: 'success', ISO_9001: 'success', CE: 'warning', FDA: 'danger', CATALOGUE: 'secondary', OTHER: 'secondary' }
  return map[type] || 'secondary'
}

async function fetchProduct() {
  loading.value = true
  error.value = null
  try {
    const response = await apiClient.get(`/products/${route.params.id}`)
    product.value = response.data
    editForm.value = { ...response.data }
  } catch (err) {
    error.value = err.response?.data?.message || 'Không thể tải thông tin sản phẩm'
  } finally {
    loading.value = false
  }
}

async function saveChanges() {
  saving.value = true
  try {
    const response = await apiClient.put(`/products/${product.value.id}`, editForm.value)
    product.value = response.data
    editMode.value = false
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật sản phẩm', life: 3000 })
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu thay đổi', life: 5000 })
  } finally {
    saving.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function isExpired(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

onMounted(() => {
  fetchProduct().then(() => fetchProductDocuments())
})
</script>

<style scoped>
.product-detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

.full-width-card {
  grid-column: 1 / -1;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--surface-border);
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--surface-border);
}

.detail-item:last-child { border-bottom: none; }
.detail-item.full-width { flex-direction: column; align-items: flex-start; gap: 0.25rem; }

.detail-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.detail-value {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
}

.detail-value.mono {
  font-family: 'Courier New', monospace;
  font-size: 0.75rem;
}

.expired-text { color: var(--danger-color); font-weight: 600; }

.cert-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
}

.cert-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  background: var(--surface-ground);
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.cert-badge.active {
  background: #d1fae5;
  color: var(--success-color);
  font-weight: 600;
}

.specs-table {
  display: flex;
  flex-direction: column;
}

.spec-row {
  display: flex;
  justify-content: space-between;
  padding: 0.625rem 0;
  border-bottom: 1px solid var(--surface-border);
}

.spec-key {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  text-transform: capitalize;
}

.spec-value {
  font-size: 0.875rem;
  font-weight: 500;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-row { width: 100%; }
.form-row.full-width { grid-column: 1 / -1; }
.cert-edit { display: flex; gap: 1rem; flex-wrap: wrap; }

.form-stack { display: flex; flex-direction: column; gap: 1rem; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-field { display: flex; flex-direction: column; gap: 0.375rem; }
.form-field label { font-size: 0.875rem; font-weight: 600; }
.required { color: var(--danger-color); }
.file-input { padding: 0.5rem 0; }
.mb-3 { margin-bottom: 0.75rem; }
.gap-2 { gap: 0.5rem; }
.expiry-warning { color: #f59e0b; font-weight: 600; }

@media (max-width: 768px) {
  .product-detail-grid { grid-template-columns: 1fr; }
}
</style>
