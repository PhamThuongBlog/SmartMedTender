<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div class="flex-center gap-3">
        <Button icon="pi pi-arrow-left" severity="secondary" text rounded @click="router.back()" />
        <div>
          <h1 class="page-title">{{ isEdit ? 'Chỉnh sửa gói thầu' : 'Tạo gói thầu mới' }}</h1>
          <p class="page-subtitle">{{ isEdit ? 'Cập nhật thông tin gói thầu' : 'Nhập thông tin gói thầu mới' }}</p>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Form -->
    <form v-else class="tender-form" @submit.prevent="handleSubmit">
      <div class="card-container">
        <!-- Basic Info -->
        <h3 class="section-title">Thông tin cơ bản</h3>
        <div class="form-grid">
          <div class="form-field full-width">
            <label>Tên gói thầu <span class="required">*</span></label>
            <InputText
              v-model="form.name"
              placeholder="Nhập tên gói thầu"
              :class="{ 'p-invalid': validationErrors.name }"
              fluid
            />
            <small v-if="validationErrors.name" class="field-error">{{ validationErrors.name }}</small>
          </div>

          <div class="form-field">
            <label>Mã gói thầu</label>
            <InputText
              v-model="form.bidPackageCode"
              placeholder="VD: TB-2026-001"
              :class="{ 'p-invalid': validationErrors.bidPackageCode }"
            />
            <small v-if="validationErrors.bidPackageCode" class="field-error">{{ validationErrors.bidPackageCode }}</small>
          </div>

          <div class="form-field">
            <label>Bên mời thầu</label>
            <InputText
              v-model="form.procuringEntity"
              placeholder="Tên chủ đầu tư / bên mời thầu"
              :class="{ 'p-invalid': validationErrors.procuringEntity }"
            />
            <small v-if="validationErrors.procuringEntity" class="field-error">{{ validationErrors.procuringEntity }}</small>
          </div>

          <div class="form-field full-width">
            <label>Mô tả</label>
            <Textarea
              v-model="form.description"
              placeholder="Nhập mô tả chi tiết về gói thầu"
              rows="3"
              autoResize
              fluid
            />
          </div>
        </div>
      </div>

      <div class="card-container mt-4">
        <!-- Schedule & Financial -->
        <h3 class="section-title">Thời gian & Tài chính</h3>
        <div class="form-grid">
          <div class="form-field">
            <label>Hạn nộp hồ sơ</label>
            <Calendar
              v-model="form.submissionDeadline"
              :showTime="true"
              dateFormat="dd/mm/yy"
              placeholder="Chọn ngày giờ"
              fluid
            />
          </div>

          <div class="form-field">
            <label>Ngày mở thầu</label>
            <Calendar
              v-model="form.openingDate"
              :showTime="true"
              dateFormat="dd/mm/yy"
              placeholder="Chọn ngày giờ"
              fluid
            />
          </div>

          <div class="form-field">
            <label>Giá dự toán</label>
            <InputNumber
              v-model="form.estimatedValue"
              placeholder="Nhập giá dự toán"
              mode="currency"
              currency="VND"
              locale="vi-VN"
              fluid
            />
          </div>

          <div class="form-field">
            <label>Đơn vị tiền tệ</label>
            <Dropdown
              v-model="form.currency"
              :options="currencies"
              placeholder="Chọn đơn vị tiền tệ"
              fluid
            />
          </div>
        </div>
      </div>

      <div class="card-container mt-4">
        <!-- Notes -->
        <h3 class="section-title">Ghi chú</h3>
        <Textarea
          v-model="form.notes"
          placeholder="Nhập ghi chú bổ sung (nếu có)"
          rows="2"
          autoResize
          fluid
        />
      </div>

      <!-- Form Actions -->
      <div class="form-actions">
        <Button
          label="Hủy"
          severity="secondary"
          outlined
          @click="router.back()"
        />
        <div class="flex-center gap-3">
          <Button
            v-if="isEdit"
            label="Lưu nháp"
            icon="pi pi-save"
            severity="secondary"
            @click="saveAsDraft"
          />
          <Button
            type="submit"
            :label="isEdit ? 'Cập nhật' : 'Tạo gói thầu'"
            icon="pi pi-check"
            :loading="submitting"
          />
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import apiClient from '@/api/client'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import InputNumber from 'primevue/inputnumber'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitting = ref(false)

const currencies = ['VND', 'USD', 'EUR']

const form = ref({
  name: '',
  bidPackageCode: '',
  procuringEntity: '',
  description: '',
  submissionDeadline: null,
  openingDate: null,
  estimatedValue: null,
  currency: 'VND',
  notes: ''
})

const validationErrors = ref({})

function validate() {
  validationErrors.value = {}
  if (!form.value.name.trim()) {
    validationErrors.value.name = 'Tên gói thầu không được để trống'
  }
  if (form.value.name.length > 500) {
    validationErrors.value.name = 'Tên gói thầu không được vượt quá 500 ký tự'
  }
  if (form.value.bidPackageCode && form.value.bidPackageCode.length > 100) {
    validationErrors.value.bidPackageCode = 'Mã gói thầu không được vượt quá 100 ký tự'
  }
  if (form.value.procuringEntity && form.value.procuringEntity.length > 500) {
    validationErrors.value.procuringEntity = 'Tên bên mời thầu không được vượt quá 500 ký tự'
  }
  return Object.keys(validationErrors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    const payload = {
      ...form.value,
      submissionDeadline: form.value.submissionDeadline
        ? new Date(form.value.submissionDeadline).toISOString()
        : null,
      openingDate: form.value.openingDate
        ? new Date(form.value.openingDate).toISOString()
        : null
    }

    if (isEdit.value) {
      await apiClient.put(`/tenders/${route.params.id}`, payload)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật gói thầu', life: 3000 })
    } else {
      await apiClient.post('/tenders', payload)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo gói thầu mới', life: 3000 })
    }
    router.push('/tenders')
  } catch (error) {
    const msg = error.response?.data?.message || 'Không thể lưu gói thầu'
    if (error.response?.data?.errors) {
      // Map validation errors from backend
      const errs = error.response.data.errors
      for (const [field, message] of Object.entries(errs)) {
        validationErrors.value[field] = message
      }
    }
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 5000 })
  } finally {
    submitting.value = false
  }
}

async function saveAsDraft() {
  if (!form.value.name.trim()) {
    validationErrors.value.name = 'Tên gói thầu không được để trống'
    return
  }
  submitting.value = true
  try {
    const payload = { ...form.value, status: 'DRAFT' }
    if (isEdit.value) {
      await apiClient.put(`/tenders/${route.params.id}`, payload)
    } else {
      await apiClient.post('/tenders', payload)
    }
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã lưu bản nháp', life: 3000 })
    router.push('/tenders')
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu bản nháp', life: 5000 })
  } finally {
    submitting.value = false
  }
}

async function fetchTender() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const response = await apiClient.get(`/tenders/${route.params.id}`)
    const data = response.data
    form.value = {
      name: data.name || '',
      bidPackageCode: data.bidPackageCode || '',
      procuringEntity: data.procuringEntity || '',
      description: data.description || '',
      submissionDeadline: data.submissionDeadline ? new Date(data.submissionDeadline) : null,
      openingDate: data.openingDate ? new Date(data.openingDate) : null,
      estimatedValue: data.estimatedValue || null,
      currency: data.currency || 'VND',
      notes: data.notes || ''
    }
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải thông tin gói thầu', life: 5000 })
    router.push('/tenders')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTender()
})
</script>

<style scoped>
.tender-form {
  max-width: 900px;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 1.25rem;
  color: var(--text-primary);
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--surface-border);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.form-field label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.required {
  color: var(--danger-color);
}

.field-error {
  color: var(--danger-color);
  font-size: 0.75rem;
}

.mt-4 {
  margin-top: 1.25rem;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}
</style>
