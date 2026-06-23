<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Thiết lập doanh nghiệp</h1>
        <p class="page-subtitle">Quản lý hồ sơ pháp lý và tài liệu doanh nghiệp</p>
      </div>
    </div>

    <TabView v-model:activeIndex="activeTab">
      <!-- Tab 1: Hồ sơ doanh nghiệp -->
      <TabPanel header="Hồ sơ doanh nghiệp">
        <div class="card-container">
          <form @submit.prevent="saveProfile" class="profile-form">
            <div class="form-grid">
              <div class="form-field">
                <label>Tên công ty <span class="required">*</span></label>
                <InputText v-model="profile.companyName" placeholder="Tên tiếng Việt" fluid />
              </div>
              <div class="form-field">
                <label>Tên tiếng Anh</label>
                <InputText v-model="profile.companyNameEn" placeholder="English name" fluid />
              </div>
              <div class="form-field">
                <label>Người đại diện pháp luật</label>
                <InputText v-model="profile.legalRepresentative" placeholder="Họ tên" fluid />
              </div>
              <div class="form-field">
                <label>Chức vụ</label>
                <InputText v-model="profile.legalRepPosition" placeholder="Chức vụ" fluid />
              </div>
              <div class="form-field">
                <label>Mã số thuế</label>
                <InputText v-model="profile.taxCode" placeholder="Mã số thuế" fluid />
              </div>
              <div class="form-field">
                <label>Số điện thoại</label>
                <InputText v-model="profile.phone" placeholder="Số điện thoại" fluid />
              </div>
              <div class="form-field">
                <label>Email</label>
                <InputText v-model="profile.email" placeholder="Email công ty" fluid />
              </div>
              <div class="form-field">
                <label>Website</label>
                <InputText v-model="profile.website" placeholder="https://..." fluid />
              </div>
              <div class="form-field full-width">
                <label>Địa chỉ</label>
                <InputText v-model="profile.address" placeholder="Địa chỉ trụ sở chính" fluid />
              </div>
            </div>

            <Divider />

            <h4 style="margin-bottom: 1rem;">Giấy phép kinh doanh</h4>
            <div class="form-grid">
              <div class="form-field">
                <label>Số GPKD</label>
                <InputText v-model="profile.businessLicenseNumber" placeholder="Số giấy phép" fluid />
              </div>
              <div class="form-field">
                <label>Cơ quan cấp</label>
                <InputText v-model="profile.issuingAuthority" placeholder="Sở KH&ĐT..." fluid />
              </div>
              <div class="form-field">
                <label>Ngày cấp</label>
                <Calendar v-model="profile.businessLicenseIssueDate" dateFormat="dd/mm/yy" showIcon fluid />
              </div>
              <div class="form-field">
                <label>Ngày hết hạn</label>
                <Calendar v-model="profile.businessLicenseExpiryDate" dateFormat="dd/mm/yy" showIcon fluid />
              </div>
            </div>

            <div class="flex-between mt-4">
              <div></div>
              <Button type="submit" label="Lưu hồ sơ" icon="pi pi-save" :loading="savingProfile" />
            </div>
          </form>
        </div>
      </TabPanel>

      <!-- Tab 2: Tài liệu pháp lý -->
      <TabPanel header="Tài liệu pháp lý">
        <div class="card-container">
          <div class="flex-between mb-3">
            <div class="filters-row">
              <span class="p-input-icon-left">
                <i class="pi pi-search" />
                <InputText v-model="legalSearch" placeholder="Tìm kiếm..." @input="fetchLegalDocs" style="width: 250px;" />
              </span>
              <Dropdown v-model="legalTypeFilter" :options="legalDocTypeOptions" placeholder="Loại tài liệu" @change="fetchLegalDocs" showClear style="width: 200px;" />
            </div>
            <Button label="Tải lên" icon="pi pi-upload" @click="showLegalUpload = true" />
          </div>

          <DataTable :value="legalDocs" :loading="loadingLegalDocs" :paginator="true" :rows="10" :total-records="legalTotal" @page="onLegalPage" lazy data-key="id">
            <Column field="documentName" header="Tên tài liệu">
              <template #body="{ data }">
                <div class="doc-name-cell">
                  <span class="doc-name">{{ data.documentName }}</span>
                  <span class="doc-desc">{{ data.description || '-' }}</span>
                </div>
              </template>
            </Column>
            <Column field="documentType" header="Loại" style="width: 180px;">
              <template #body="{ data }">
                <Tag :value="formatLegalDocType(data.documentType)" :severity="getLegalDocSeverity(data.documentType)" />
              </template>
            </Column>
            <Column field="issuingAuthority" header="Cơ quan cấp" style="width: 200px;">
              <template #body="{ data }">{{ data.issuingAuthority || '-' }}</template>
            </Column>
            <Column field="issueDate" header="Ngày cấp" style="width: 120px;">
              <template #body="{ data }">{{ formatDate(data.issueDate) }}</template>
            </Column>
            <Column field="expiryDate" header="Ngày hết hạn" style="width: 130px;">
              <template #body="{ data }">
                <span :class="{ 'expiry-warning': isNearExpiry(data.expiryDate) }">
                  {{ formatDate(data.expiryDate) }}
                </span>
              </template>
            </Column>
            <Column header="Thao tác" style="width: 140px;">
              <template #body="{ data }">
                <div class="flex-center gap-2">
                  <Button icon="pi pi-download" severity="secondary" text rounded size="small" @click="downloadLegalDoc(data)" v-tooltip.top="'Tải xuống'" />
                  <Button icon="pi pi-pencil" severity="secondary" text rounded size="small" @click="openLegalEdit(data)" v-tooltip.top="'Sửa'" />
                  <Button icon="pi pi-trash" severity="danger" text rounded size="small" @click="confirmDeleteLegal(data)" v-tooltip.top="'Xóa'" />
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
      </TabPanel>
    </TabView>

    <!-- Upload Legal Document Dialog -->
    <Dialog v-model:visible="showLegalUpload" header="Tải lên tài liệu pháp lý" :modal="true" :style="{ width: '550px' }">
      <form @submit.prevent="uploadLegalDoc" class="upload-form">
        <div class="form-field">
          <label>Loại tài liệu <span class="required">*</span></label>
          <Dropdown v-model="legalForm.documentType" :options="legalDocTypeOptions" placeholder="Chọn loại" fluid />
        </div>
        <div class="form-field">
          <label>Tên tài liệu <span class="required">*</span></label>
          <InputText v-model="legalForm.documentName" placeholder="Nhập tên tài liệu" fluid />
        </div>
        <div class="form-field">
          <label>Mô tả</label>
          <InputText v-model="legalForm.description" placeholder="Mô tả ngắn" fluid />
        </div>
        <div class="form-field">
          <label>Cơ quan cấp</label>
          <InputText v-model="legalForm.issuingAuthority" placeholder="Cơ quan cấp" fluid />
        </div>
        <div class="form-row">
          <div class="form-field">
            <label>Ngày cấp</label>
            <Calendar v-model="legalForm.issueDate" dateFormat="dd/mm/yy" fluid />
          </div>
          <div class="form-field">
            <label>Ngày hết hạn</label>
            <Calendar v-model="legalForm.expiryDate" dateFormat="dd/mm/yy" fluid />
          </div>
        </div>
        <div class="form-field">
          <label>Tệp <span class="required">*</span></label>
          <input type="file" ref="legalFileInput" accept=".pdf,.docx,.doc,.png,.jpg,.jpeg" class="file-input" />
        </div>
        <div class="flex-between mt-4">
          <Button label="Hủy" severity="secondary" outlined @click="showLegalUpload = false" />
          <Button type="submit" label="Tải lên" icon="pi pi-upload" :loading="uploadingLegal" />
        </div>
      </form>
    </Dialog>

    <!-- Edit Legal Document Dialog -->
    <Dialog v-model:visible="showLegalEdit" header="Sửa tài liệu pháp lý" :modal="true" :style="{ width: '500px' }">
      <form @submit.prevent="updateLegalDoc" class="upload-form">
        <div class="form-field">
          <label>Tên tài liệu</label>
          <InputText v-model="legalEditForm.documentName" fluid />
        </div>
        <div class="form-field">
          <label>Loại tài liệu</label>
          <Dropdown v-model="legalEditForm.documentType" :options="legalDocTypeOptions" fluid />
        </div>
        <div class="form-field">
          <label>Mô tả</label>
          <InputText v-model="legalEditForm.description" fluid />
        </div>
        <div class="form-field">
          <label>Cơ quan cấp</label>
          <InputText v-model="legalEditForm.issuingAuthority" fluid />
        </div>
        <div class="form-row">
          <div class="form-field">
            <label>Ngày cấp</label>
            <Calendar v-model="legalEditForm.issueDate" dateFormat="dd/mm/yy" fluid />
          </div>
          <div class="form-field">
            <label>Ngày hết hạn</label>
            <Calendar v-model="legalEditForm.expiryDate" dateFormat="dd/mm/yy" fluid />
          </div>
        </div>
        <div class="flex-between mt-4">
          <Button label="Hủy" severity="secondary" outlined @click="showLegalEdit = false" />
          <Button type="submit" label="Cập nhật" icon="pi pi-check" :loading="updatingLegal" />
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
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import InputText from 'primevue/inputtext'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Divider from 'primevue/divider'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

const toast = useToast()
const confirm = useConfirm()

const activeTab = ref(0)
const savingProfile = ref(false)

const profile = ref({
  companyName: '', companyNameEn: '', taxCode: '', address: '',
  phone: '', email: '', website: '', legalRepresentative: '', legalRepPosition: '',
  businessLicenseNumber: '', issuingAuthority: '',
  businessLicenseIssueDate: null, businessLicenseExpiryDate: null
})

const legalDocs = ref([])
const legalTotal = ref(0)
const loadingLegalDocs = ref(false)
const legalPage = ref(0)
const legalSearch = ref('')
const legalTypeFilter = ref(null)
let legalSearchTimer = null

const showLegalUpload = ref(false)
const showLegalEdit = ref(false)
const uploadingLegal = ref(false)
const updatingLegal = ref(false)
const legalFileInput = ref(null)
const editingLegalDoc = ref(null)

const legalDocTypeOptions = [
  { label: 'Đăng ký kinh doanh', value: 'BUSINESS_LICENSE' },
  { label: 'Đăng ký thuế', value: 'TAX_REGISTRATION' },
  { label: 'Chứng nhận GMP', value: 'GMP_CERT' },
  { label: 'Giấy phép phân phối', value: 'DISTRIBUTION_AUTH' },
  { label: 'CO - Chứng nhận xuất xứ', value: 'CO' },
  { label: 'CQ - Chứng nhận chất lượng', value: 'CQ' },
  { label: 'ISO 13485', value: 'ISO_13485' },
  { label: 'ISO 9001', value: 'ISO_9001' },
  { label: 'CE Marking', value: 'CE' },
  { label: 'FDA', value: 'FDA' },
  { label: 'GSP - Thực hành tốt bảo quản', value: 'GSP' },
  { label: 'GDP - Thực hành tốt phân phối', value: 'GDP' },
  { label: 'Catalogue', value: 'CATALOGUE' },
  { label: 'Khác', value: 'OTHER' }
]

const legalForm = ref({ documentType: '', documentName: '', description: '', issuingAuthority: '', issueDate: null, expiryDate: null })
const legalEditForm = ref({ documentName: '', documentType: '', description: '', issuingAuthority: '', issueDate: null, expiryDate: null })

async function fetchProfile() {
  try {
    const res = await apiClient.get('/enterprise/profile')
    if (res.data && res.data.id) {
      const d = res.data
      profile.value = {
        companyName: d.companyName || '', companyNameEn: d.companyNameEn || '',
        taxCode: d.taxCode || '', address: d.address || '',
        phone: d.phone || '', email: d.email || '', website: d.website || '',
        legalRepresentative: d.legalRepresentative || '', legalRepPosition: d.legalRepPosition || '',
        businessLicenseNumber: d.businessLicenseNumber || '',
        issuingAuthority: d.issuingAuthority || '',
        businessLicenseIssueDate: d.businessLicenseIssueDate ? new Date(d.businessLicenseIssueDate) : null,
        businessLicenseExpiryDate: d.businessLicenseExpiryDate ? new Date(d.businessLicenseExpiryDate) : null
      }
    }
  } catch (e) {
    // profile not set yet - ignore
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await apiClient.put('/enterprise/profile', profile.value)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã lưu hồ sơ doanh nghiệp', life: 3000 })
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu hồ sơ', life: 5000 })
  } finally {
    savingProfile.value = false
  }
}

async function fetchLegalDocs() {
  loadingLegalDocs.value = true
  try {
    const params = { page: legalPage.value, size: 10 }
    if (legalSearch.value) params.search = legalSearch.value
    if (legalTypeFilter.value) params.documentType = legalTypeFilter.value

    const res = await apiClient.get('/enterprise/legal-docs', { params })
    legalDocs.value = res.data.content || []
    legalTotal.value = res.data.totalElements || 0
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách tài liệu', life: 5000 })
  } finally {
    loadingLegalDocs.value = false
  }
}

function onLegalSearch() {
  clearTimeout(legalSearchTimer)
  legalSearchTimer = setTimeout(() => { legalPage.value = 0; fetchLegalDocs() }, 300)
}

function onLegalPage(event) {
  legalPage.value = event.page
  fetchLegalDocs()
}

async function uploadLegalDoc() {
  if (!legalForm.value.documentType || !legalForm.value.documentName) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Vui lòng chọn loại và nhập tên tài liệu', life: 3000 })
    return
  }
  if (!legalFileInput.value?.files?.length) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Vui lòng chọn tệp', life: 3000 })
    return
  }

  uploadingLegal.value = true
  try {
    const formData = new FormData()
    formData.append('file', legalFileInput.value.files[0])
    Object.entries(legalForm.value).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') {
        formData.append(k, v instanceof Date ? v.toISOString().split('T')[0] : v)
      }
    })

    await apiClient.post('/enterprise/legal-docs', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải lên tài liệu', life: 3000 })
    showLegalUpload.value = false
    legalForm.value = { documentType: '', documentName: '', description: '', issuingAuthority: '', issueDate: null, expiryDate: null }
    if (legalFileInput.value) legalFileInput.value.value = ''
    fetchLegalDocs()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải lên', life: 5000 })
  } finally {
    uploadingLegal.value = false
  }
}

function openLegalEdit(doc) {
  editingLegalDoc.value = doc
  legalEditForm.value = {
    documentName: doc.documentName || '',
    documentType: doc.documentType || '',
    description: doc.description || '',
    issuingAuthority: doc.issuingAuthority || '',
    issueDate: doc.issueDate ? new Date(doc.issueDate) : null,
    expiryDate: doc.expiryDate ? new Date(doc.expiryDate) : null
  }
  showLegalEdit.value = true
}

async function updateLegalDoc() {
  updatingLegal.value = true
  try {
    await apiClient.put(`/enterprise/legal-docs/${editingLegalDoc.value.id}`, legalEditForm.value)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật tài liệu', life: 3000 })
    showLegalEdit.value = false
    fetchLegalDocs()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật', life: 5000 })
  } finally {
    updatingLegal.value = false
  }
}

function confirmDeleteLegal(doc) {
  confirm.require({
    message: `Bạn có chắc muốn xóa tài liệu "${doc.documentName}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Xóa',
    rejectLabel: 'Hủy',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await apiClient.delete(`/enterprise/legal-docs/${doc.id}`)
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa tài liệu', life: 3000 })
        fetchLegalDocs()
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa', life: 5000 })
      }
    }
  })
}

function downloadLegalDoc(doc) {
  apiClient.get(`/enterprise/legal-docs/${doc.id}/download`, { responseType: 'blob' })
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
      toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải xuống', life: 5000 })
    })
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN')
}

function formatLegalDocType(type) {
  const map = { BUSINESS_LICENSE: 'ĐKKD', TAX_REGISTRATION: 'Thuế', GMP_CERT: 'GMP', DISTRIBUTION_AUTH: 'Phân phối', OTHER: 'Khác' }
  return map[type] || type
}

function getLegalDocSeverity(type) {
  const map = { BUSINESS_LICENSE: 'primary', GMP_CERT: 'success', DISTRIBUTION_AUTH: 'warning', TAX_REGISTRATION: 'info', OTHER: 'secondary' }
  return map[type] || 'secondary'
}

function isNearExpiry(dateStr) {
  if (!dateStr) return false
  const days = Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24))
  return days <= 90 && days > 0
}

onMounted(() => {
  fetchProfile()
  fetchLegalDocs()
})
</script>

<style scoped>
.profile-form .form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.upload-form {
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

.full-width { grid-column: 1 / -1; }
.required { color: var(--danger-color); }
.mt-4 { margin-top: 1rem; }
.mb-3 { margin-bottom: 1rem; }

.filters-row {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.doc-name-cell {
  display: flex;
  flex-direction: column;
}

.doc-name {
  font-weight: 500;
}

.doc-desc {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.expiry-warning {
  color: var(--danger-color);
  font-weight: 600;
}

.file-input {
  padding: 0.5rem 0;
}

.gap-2 { gap: 0.5rem; }
</style>
