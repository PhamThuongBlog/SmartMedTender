<template>
  <div class="file-uploader">
    <div
      class="upload-area"
      :class="{ 'drag-over': isDragging, 'has-file': files.length > 0 }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
      @click="$refs.fileInput.click()"
    >
      <input
        ref="fileInput"
        type="file"
        :accept="accept"
        :multiple="multiple"
        hidden
        @change="handleFileChange"
      />
      <div v-if="!uploading && files.length === 0" class="upload-placeholder">
        <i class="pi pi-cloud-upload upload-icon"></i>
        <h4>Kéo thả tệp vào đây</h4>
        <p>hoặc nhấn để chọn tệp</p>
        <small>Hỗ trợ: {{ accept }}</small>
      </div>
    </div>

    <!-- File List -->
    <div v-if="files.length > 0" class="file-list">
      <div v-for="(file, index) in files" :key="index" class="file-item">
        <div class="file-info">
          <i :class="getFileIcon(file.name)" class="file-type-icon"></i>
          <div class="file-details">
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatSize(file.size) }}</span>
          </div>
        </div>
        <div class="file-actions">
          <ProgressBar v-if="file.progress !== undefined && file.progress < 100" :value="file.progress" style="width: 120px; height: 6px;" />
          <i v-else-if="file.status === 'success'" class="pi pi-check-circle" style="color: var(--success-color);"></i>
          <i v-else-if="file.status === 'error'" class="pi pi-times-circle" style="color: var(--danger-color);"></i>
          <Button
            v-if="!uploading"
            icon="pi pi-times"
            severity="secondary"
            text
            rounded
            size="small"
            @click="removeFile(index)"
          />
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div v-if="files.length > 0 && !uploading" class="upload-actions">
      <Button label="Hủy" severity="secondary" outlined @click="clearAll" />
      <Button :label="buttonLabel" icon="pi pi-upload" @click="$emit('upload', files)" :loading="uploading" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Button from 'primevue/button'
import ProgressBar from 'primevue/progressbar'

const props = defineProps({
  accept: { type: String, default: '.pdf,.docx,.doc,.zip' },
  multiple: { type: Boolean, default: true },
  uploading: { type: Boolean, default: false },
  buttonLabel: { type: String, default: 'Tải lên' },
  maxFileSize: { type: Number, default: 50 * 1024 * 1024 } // 50MB
})

defineEmits(['upload'])

const isDragging = ref(false)
const files = ref([])
const fileInput = ref(null)

function handleDrop(event) {
  isDragging.value = false
  const droppedFiles = Array.from(event.dataTransfer.files)
  addFiles(droppedFiles)
}

function handleFileChange(event) {
  const selectedFiles = Array.from(event.target.files)
  addFiles(selectedFiles)
  event.target.value = ''
}

function addFiles(newFiles) {
  newFiles.forEach(file => {
    if (file.size > props.maxFileSize) {
      files.value.push({
        name: file.name,
        size: file.size,
        status: 'error',
        error: `Kích thước tệp vượt quá ${formatSize(props.maxFileSize)}`
      })
      return
    }
    files.value.push({
      name: file.name,
      size: file.size,
      status: 'pending',
      file: file,
      progress: 0
    })
  })
}

function removeFile(index) {
  files.value.splice(index, 1)
}

function clearAll() {
  files.value = []
}

function getFileIcon(filename) {
  const ext = filename.split('.').pop()?.toLowerCase()
  const icons = {
    pdf: 'pi pi-file-pdf',
    doc: 'pi pi-file-word',
    docx: 'pi pi-file-word',
    xls: 'pi pi-file-excel',
    xlsx: 'pi pi-file-excel',
    zip: 'pi pi-box',
    rar: 'pi pi-box',
    png: 'pi pi-image',
    jpg: 'pi pi-image',
    jpeg: 'pi pi-image'
  }
  return icons[ext] || 'pi pi-file'
}

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function setProgress(index, progress) {
  if (files.value[index]) {
    files.value[index].progress = progress
  }
}

function setStatus(index, status) {
  if (files.value[index]) {
    files.value[index].status = status
  }
}

defineExpose({ files, setProgress, setStatus, clearAll })
</script>

<style scoped>
.file-uploader {
  width: 100%;
}

.upload-area {
  border: 2px dashed var(--surface-border);
  border-radius: 12px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--surface-ground);
}

.upload-area:hover,
.upload-area.drag-over {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.upload-area.has-file {
  padding: 0.75rem;
  border-style: solid;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.upload-icon {
  font-size: 3rem;
  color: var(--primary-color);
  margin-bottom: 0.5rem;
}

.upload-placeholder h4 {
  font-size: 1rem;
  color: var(--text-primary);
  margin: 0;
}

.upload-placeholder p {
  color: var(--text-secondary);
  margin: 0;
}

.upload-placeholder small {
  color: var(--text-secondary);
  font-size: 0.75rem;
  margin-top: 0.5rem;
}

.file-list {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  background: var(--surface-ground);
  border-radius: 8px;
  border: 1px solid var(--surface-border);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow: hidden;
}

.file-type-icon {
  font-size: 1.5rem;
  color: var(--primary-color);
  flex-shrink: 0;
}

.file-details {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.file-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.upload-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
}
</style>
