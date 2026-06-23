<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Xuất tài liệu</h1>
        <p class="page-subtitle">Xuất hồ sơ dự thầu và các tài liệu liên quan</p>
      </div>
      <Button label="Xuất mới" icon="pi pi-plus" @click="router.push('/hsdt-builder')" />
    </div>

    <!-- Export History -->
    <div class="card-container">
      <h3 class="section-title">Lịch sử xuất</h3>

      <div v-if="loading" class="loading-container">
        <ProgressSpinner />
      </div>

      <div v-else-if="exports.length === 0" class="empty-state">
        <i class="pi pi-download"></i>
        <p>Chưa có tài liệu xuất nào</p>
        <Button label="Tạo HSDT mới" icon="pi pi-file-edit" @click="router.push('/hsdt-builder')" />
      </div>

      <DataTable v-else :value="exports" :paginator="true" :rows="10">
        <Column field="tenderName" header="Gói thầu">
          <template #body="{ data }">
            <router-link :to="`/tenders/${data.tenderId}`" class="tender-link">
              {{ data.tenderName || 'Gói thầu ' + data.tenderId?.substring(0, 8) }}
            </router-link>
          </template>
        </Column>
        <Column field="format" header="Định dạng" style="width: 100px;">
          <template #body="{ data }">
            <Tag :value="data.format" :severity="formatSeverity(data.format)" />
          </template>
        </Column>
        <Column field="fileSize" header="Kích thước" style="width: 120px;">
          <template #body="{ data }">
            {{ formatSize(data.fileSize) }}
          </template>
        </Column>
        <Column field="status" header="Trạng thái" style="width: 130px;">
          <template #body="{ data }">
            <StatusBadge :status="data.status || 'COMPLETED'" />
          </template>
        </Column>
        <Column field="createdAt" header="Ngày xuất" sortable style="width: 160px;">
          <template #body="{ data }">
            {{ formatDate(data.createdAt) }}
          </template>
        </Column>
        <Column header="Thao tác" style="width: 200px;">
          <template #body="{ data }">
            <div class="action-buttons">
              <Button
                label="Tải xuống"
                icon="pi pi-download"
                size="small"
                severity="primary"
                @click="downloadExport(data)"
              />
              <Button
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                size="small"
                v-tooltip.top="'Xóa'"
                @click="deleteExport(data)"
              />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- Quick Export Section -->
    <div class="card-container mt-4">
      <h3 class="section-title">Xuất nhanh</h3>
      <p class="section-desc">Chọn gói thầu và định dạng để xuất nhanh</p>

      <div class="quick-export-form">
        <Dropdown
          v-model="exportTender"
          :options="tenders"
          optionLabel="name"
          optionValue="id"
          placeholder="Chọn gói thầu"
          :loading="loadingTenders"
          filter
          style="min-width: 300px;"
        />
        <div class="export-format-buttons">
          <Button
            label="Word (.docx)"
            icon="pi pi-file-word"
            outlined
            @click="quickExport('word')"
            :disabled="!exportTender"
            :loading="exporting === 'word'"
          />
          <Button
            label="PDF (.pdf)"
            icon="pi pi-file-pdf"
            outlined
            @click="quickExport('pdf')"
            :disabled="!exportTender"
            :loading="exporting === 'pdf'"
          />
          <Button
            label="ZIP (.zip)"
            icon="pi pi-box"
            outlined
            @click="quickExport('zip')"
            :disabled="!exportTender"
            :loading="exporting === 'zip'"
          />
        </div>
      </div>
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
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import ProgressSpinner from 'primevue/progressspinner'
import StatusBadge from '@/components/StatusBadge.vue'

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const loading = ref(false)
const loadingTenders = ref(false)
const exporting = ref(null)
const exports = ref([])
const tenders = ref([])
const exportTender = ref(null)

async function fetchTenders() {
  loadingTenders.value = true
  try {
    const response = await apiClient.get('/tenders', { params: { size: 100 } })
    tenders.value = response.data.content || response.data || []
  } catch (error) {
    // Silently handle
  } finally {
    loadingTenders.value = false
  }
}

function formatSeverity(format) {
  const map = { WORD: 'info', PDF: 'danger', ZIP: 'warning' }
  return map[format?.toUpperCase()] || 'secondary'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

async function quickExport(format) {
  if (!exportTender.value) return
  exporting.value = format
  try {
    const response = await apiClient.get(`/export/${format}/${exportTender.value}`, {
      responseType: 'blob'
    })
    const tenderName = tenders.value.find(t => t.id === exportTender.value)?.name || 'document'
    const ext = format === 'zip' ? 'zip' : format === 'pdf' ? 'pdf' : 'docx'
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${tenderName}.${ext}`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã xuất tài liệu định dạng ${format.toUpperCase()}`, life: 3000 })
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xuất tài liệu', life: 5000 })
  } finally {
    exporting.value = null
  }
}

async function downloadExport(exportItem) {
  if (exportItem.tenderId) {
    try {
      const format = exportItem.format?.toLowerCase() || 'word'
      await quickExport(format)
    } catch (error) {
      toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải tài liệu', life: 5000 })
    }
  }
}

function deleteExport(data) {
  confirm.require({
    message: 'Bạn có chắc muốn xóa bản xuất này?',
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Xóa',
    rejectLabel: 'Hủy',
    acceptClass: 'p-button-danger',
    accept: () => {
      exports.value = exports.value.filter(e => e.id !== data.id)
      toast.add({ severity: 'success', summary: 'Đã xóa', detail: 'Đã xóa bản xuất', life: 3000 })
    }
  })
}

onMounted(() => {
  fetchTenders()
})
</script>

<style scoped>
.section-title {
  font-size: 1.0625rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.section-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-bottom: 1.25rem;
}

.tender-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
}

.tender-link:hover {
  text-decoration: underline;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quick-export-form {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.export-format-buttons {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.mt-4 { margin-top: 1.25rem; }
</style>
