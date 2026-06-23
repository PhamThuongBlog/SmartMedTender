<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Tải lên HSMT</h1>
        <p class="page-subtitle">Tải lên hồ sơ mời thầu và trích xuất yêu cầu kỹ thuật tự động</p>
      </div>
    </div>

    <div class="upload-container">
      <!-- Step 1: Select Tender -->
      <div class="card-container mb-4">
        <h3 class="section-title">Bước 1: Chọn gói thầu</h3>
        <Dropdown
          v-model="selectedTender"
          :options="tenders"
          optionLabel="name"
          optionValue="id"
          placeholder="Chọn gói thầu để tải HSMT"
          :loading="loadingTenders"
          filter
          showClear
          style="min-width: 400px;"
        >
          <template #option="{ option }">
            <div class="tender-option">
              <span>{{ option.name }}</span>
              <small>{{ option.bidPackageCode || '' }}</small>
            </div>
          </template>
        </Dropdown>
      </div>

      <!-- Step 2: Upload Files -->
      <div class="card-container mb-4">
        <h3 class="section-title">Bước 2: Tải lên tệp HSMT</h3>
        <p class="section-desc">Hỗ trợ định dạng PDF, DOCX, DOC, ZIP. Kích thước tối đa 50MB/tệp.</p>
        <FileUploader
          ref="uploaderRef"
          accept=".pdf,.docx,.doc,.zip"
          :uploading="uploading"
          button-label="Tải lên & Trích xuất"
          @upload="handleUpload"
        />
      </div>

      <!-- Step 3: Results -->
      <div v-if="uploadResult" class="card-container">
        <h3 class="section-title">Bước 3: Kết quả trích xuất</h3>

        <div v-if="uploadResult.status === 'PROCESSING'" class="processing-state">
          <ProgressSpinner style="width: 40px; height: 40px;" strokeWidth="4" />
          <div>
            <h4>Đang xử lý...</h4>
            <p>Tài liệu đang được phân tích và trích xuất yêu cầu kỹ thuật.</p>
          </div>
        </div>

        <div v-else-if="uploadResult.status === 'COMPLETED'" class="success-result">
          <div class="result-stats">
            <div class="result-stat">
              <i class="pi pi-check-circle"></i>
              <div>
                <span class="stat-number">{{ uploadResult.totalRequirements || 0 }}</span>
                <span class="stat-label">Yêu cầu đã trích xuất</span>
              </div>
            </div>
            <div class="result-stat">
              <i class="pi pi-star"></i>
              <div>
                <span class="stat-number">{{ uploadResult.mandatoryCount || 0 }}</span>
                <span class="stat-label">Yêu cầu bắt buộc</span>
              </div>
            </div>
            <div class="result-stat">
              <i class="pi pi-percentage"></i>
              <div>
                <span class="stat-number">{{ uploadResult.confidence ? (uploadResult.confidence * 100).toFixed(1) + '%' : 'N/A' }}</span>
                <span class="stat-label">Độ tin cậy trung bình</span>
              </div>
            </div>
          </div>

          <Divider />

          <h4 style="margin-bottom: 1rem;">Danh sách yêu cầu đã trích xuất</h4>
          <DataTable
            v-if="extractedRequirements.length > 0"
            :value="extractedRequirements"
            :paginator="true"
            :rows="5"
          >
            <Column field="description" header="Mô tả yêu cầu"></Column>
            <Column field="type" header="Loại" style="width: 120px;"></Column>
            <Column field="operator" header="Toán tử" style="width: 100px;"></Column>
            <Column field="value" header="Giá trị" style="width: 100px;"></Column>
            <Column field="unit" header="Đơn vị" style="width: 80px;"></Column>
            <Column field="mandatory" header="Bắt buộc" style="width: 100px;">
              <template #body="{ data }">
                <Tag :value="data.mandatory ? 'Bắt buộc' : 'Tùy chọn'" :severity="data.mandatory ? 'danger' : 'secondary'" />
              </template>
            </Column>
            <Column field="confidence" header="Độ tin cậy" style="width: 120px;">
              <template #body="{ data }">
                <div class="confidence-cell">
                  <ProgressBar :value="(data.confidence || 0) * 100" :style="{ width: '80px', height: '6px' }" />
                  <small>{{ data.confidence ? (data.confidence * 100).toFixed(0) + '%' : '0%' }}</small>
                </div>
              </template>
            </Column>
          </DataTable>

          <div class="upload-actions mt-4">
            <router-link :to="`/tenders/${selectedTender}`">
              <Button label="Xem chi tiết gói thầu" icon="pi pi-arrow-right" severity="secondary" outlined />
            </router-link>
            <Button label="Xem xét & Phê duyệt" icon="pi pi-check-circle" @click="router.push('/ocr/review')" />
          </div>
        </div>

        <div v-else-if="uploadResult.status === 'FAILED'" class="error-result">
          <i class="pi pi-times-circle"></i>
          <h4>Trích xuất thất bại</h4>
          <p>{{ uploadResult.error || 'Đã có lỗi xảy ra trong quá trình xử lý.' }}</p>
          <Button label="Thử lại" icon="pi pi-refresh" severity="secondary" @click="uploadResult = null" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import apiClient from '@/api/client'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'
import ProgressBar from 'primevue/progressbar'
import Divider from 'primevue/divider'
import Tag from 'primevue/tag'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import FileUploader from '@/components/FileUploader.vue'

const router = useRouter()
const toast = useToast()

const loadingTenders = ref(false)
const uploading = ref(false)
const tenders = ref([])
const selectedTender = ref(null)
const uploadResult = ref(null)
const extractedRequirements = ref([])
const uploaderRef = ref(null)

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

async function handleUpload(files) {
  if (!selectedTender.value) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng chọn gói thầu trước khi tải lên', life: 3000 })
    return
  }

  uploading.value = true
  uploadResult.value = { status: 'PROCESSING' }

  try {
    const formData = new FormData()
    // Backend expects single file with param name "file"
    const fileItem = files[0]
    formData.append('file', fileItem.file)
    if (uploaderRef.value) {
      uploaderRef.value.setProgress(0, 20)
    }
    formData.append('tenderId', selectedTender.value)

    const response = await apiClient.post('/hsmt/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (progressEvent) => {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        if (uploaderRef.value) {
          uploaderRef.value.setProgress(0, 20 + (progress * 0.3))
        }
      }
    })

    uploadResult.value = response.data
    extractedRequirements.value = response.data.requirements || []

    if (response.data.status === 'COMPLETED') {
      toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã trích xuất ${response.data.totalRequirements || 0} yêu cầu`, life: 5000 })
    } else if (response.data.status === 'FAILED') {
      toast.add({ severity: 'error', summary: 'Thất bại', detail: 'Không thể trích xuất yêu cầu từ tệp', life: 5000 })
    } else {
      toast.add({ severity: 'info', summary: 'Đang xử lý', detail: 'Tài liệu đã được tải lên', life: 3000 })
    }
  } catch (error) {
    uploadResult.value = {
      status: 'FAILED',
      error: error.response?.data?.message || 'Không thể xử lý tệp HSMT'
    }
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Tải lên thất bại', life: 5000 })
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  fetchTenders()
})
</script>

<style scoped>
.upload-container {
  max-width: 900px;
}

.section-title {
  font-size: 1.0625rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.section-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.tender-option {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.tender-option small {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.processing-state {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 2rem;
}

.processing-state h4 {
  margin: 0 0 0.25rem 0;
}

.processing-state p {
  color: var(--text-secondary);
  margin: 0;
}

.result-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.result-stat {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: var(--surface-ground);
  border-radius: 8px;
}

.result-stat i {
  font-size: 1.5rem;
  color: var(--success-color);
}

.stat-number {
  font-size: 1.25rem;
  font-weight: 700;
  display: block;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  display: block;
}

.confidence-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.confidence-cell small {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.error-result {
  text-align: center;
  padding: 2rem;
}

.error-result i {
  font-size: 3rem;
  color: var(--danger-color);
  margin-bottom: 1rem;
}

.error-result h4 {
  color: var(--danger-color);
  margin-bottom: 0.5rem;
}

.upload-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.mb-4 { margin-bottom: 1.25rem; }
.mt-4 { margin-top: 1.25rem; }
</style>
