<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Cài đặt</h1>
        <p class="page-subtitle">Quản lý tài khoản và tùy chỉnh hệ thống</p>
      </div>
    </div>

    <div class="settings-grid">
      <!-- Profile Section -->
      <div class="card-container">
        <h3 class="section-title">Thông tin cá nhân</h3>

        <div class="profile-section">
          <div class="profile-avatar">
            <Avatar
              :label="userInitials"
              shape="circle"
              size="xlarge"
              style="background-color: var(--primary-color); color: white; width: 80px; height: 80px; font-size: 1.5rem;"
            />
          </div>
          <div class="profile-info">
            <h3>{{ authStore.user?.fullName || authStore.user?.username || 'Người dùng' }}</h3>
            <p class="profile-role">
              <Tag :value="authStore.user?.roleName" :severity="authStore.user?.roleName === 'ADMIN' ? 'danger' : 'info'" />
            </p>
          </div>
        </div>

        <Divider />

        <div class="profile-form">
          <div class="form-field">
            <label>Tên đăng nhập</label>
            <InputText :value="authStore.user?.username" disabled fluid />
          </div>
          <div class="form-field">
            <label>Họ và tên</label>
            <InputText v-model="profileForm.fullName" placeholder="Nhập họ và tên" fluid />
          </div>
          <div class="form-field">
            <label>Email</label>
            <InputText v-model="profileForm.email" placeholder="Nhập email" fluid />
          </div>
          <div class="form-field">
            <label>Số điện thoại</label>
            <InputText v-model="profileForm.phone" placeholder="Nhập số điện thoại" fluid />
          </div>
          <Button label="Cập nhật thông tin" icon="pi pi-check" :loading="savingProfile" @click="updateProfile" />
        </div>
      </div>

      <!-- Change Password -->
      <div class="card-container">
        <h3 class="section-title">Đổi mật khẩu</h3>
        <form @submit.prevent="changePassword" class="password-form">
          <div class="form-field">
            <label>Mật khẩu hiện tại <span class="required">*</span></label>
            <Password v-model="passwordForm.oldPassword" placeholder="Nhập mật khẩu hiện tại" :feedback="false" toggleMask fluid />
            <small v-if="passwordErrors.oldPassword" class="field-error">{{ passwordErrors.oldPassword }}</small>
          </div>
          <div class="form-field">
            <label>Mật khẩu mới <span class="required">*</span></label>
            <Password v-model="passwordForm.newPassword" placeholder="Nhập mật khẩu mới (tối thiểu 8 ký tự)" toggleMask fluid />
            <small v-if="passwordErrors.newPassword" class="field-error">{{ passwordErrors.newPassword }}</small>
          </div>
          <div class="form-field">
            <label>Xác nhận mật khẩu <span class="required">*</span></label>
            <Password v-model="passwordForm.confirmPassword" placeholder="Nhập lại mật khẩu mới" :feedback="false" toggleMask fluid />
            <small v-if="passwordErrors.confirmPassword" class="field-error">{{ passwordErrors.confirmPassword }}</small>
          </div>
          <Button type="submit" label="Đổi mật khẩu" icon="pi pi-lock" :loading="changingPassword" />
        </form>
      </div>

      <!-- Theme Settings -->
      <div class="card-container">
        <h3 class="section-title">Giao diện</h3>
        <div class="theme-settings">
          <div class="theme-item">
            <div>
              <h4>Chế độ tối</h4>
              <p>Chuyển đổi giữa giao diện sáng và tối</p>
            </div>
            <input type="checkbox" v-model="darkMode" @change="toggleDarkMode" class="toggle-checkbox" />
          </div>
        </div>
      </div>

      <!-- System Info -->
      <div class="card-container">
        <h3 class="section-title">Thông tin hệ thống</h3>
        <div class="system-info">
          <div class="info-row">
            <span class="info-label">Phiên bản</span>
            <span class="info-value">MedTender v2.0.0</span>
          </div>
          <div class="info-row">
            <span class="info-label">Môi trường</span>
            <Tag value="Development" severity="info" />
          </div>
          <div class="info-row">
            <span class="info-label">Trạng thái kết nối</span>
            <div class="flex-center gap-2">
              <i :class="backendStatus ? 'pi pi-check-circle' : 'pi pi-times-circle'" :style="{ color: backendStatus ? 'var(--success-color)' : 'var(--danger-color)' }"></i>
              <span>{{ backendStatus ? 'Đã kết nối' : 'Mất kết nối' }}</span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">Đăng nhập lần cuối</span>
            <span class="info-value">{{ formatDate(authStore.user?.lastLoginAt) }}</span>
          </div>
        </div>
        <Divider />
        <Button
          label="Kiểm tra kết nối"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          size="small"
          :loading="checkingHealth"
          @click="checkBackendHealth"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useAuthStore } from '@/stores/auth'
import apiClient from '@/api/client'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Avatar from 'primevue/avatar'
import Divider from 'primevue/divider'

const toast = useToast()
const authStore = useAuthStore()

const savingProfile = ref(false)
const changingPassword = ref(false)
const checkingHealth = ref(false)
const darkMode = ref(false)
const backendStatus = ref(true)

const userInitials = computed(() => {
  const name = authStore.user?.fullName || authStore.user?.username || 'U'
  return name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
})

const profileForm = ref({
  fullName: authStore.user?.fullName || '',
  email: authStore.user?.email || '',
  phone: authStore.user?.phone || ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordErrors = ref({})

async function updateProfile() {
  savingProfile.value = true
  try {
    await apiClient.put('/auth/profile', profileForm.value)
    await authStore.fetchCurrentUser()
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật thông tin cá nhân', life: 3000 })
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật', life: 5000 })
  } finally {
    savingProfile.value = false
  }
}

async function changePassword() {
  passwordErrors.value = {}
  if (!passwordForm.value.oldPassword) {
    passwordErrors.value.oldPassword = 'Vui lòng nhập mật khẩu hiện tại'
    return
  }
  if (!passwordForm.value.newPassword) {
    passwordErrors.value.newPassword = 'Vui lòng nhập mật khẩu mới'
    return
  }
  if (passwordForm.value.newPassword.length < 8) {
    passwordErrors.value.newPassword = 'Mật khẩu mới phải có ít nhất 8 ký tự'
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordErrors.value.confirmPassword = 'Mật khẩu xác nhận không khớp'
    return
  }

  changingPassword.value = true
  try {
    await authStore.changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã đổi mật khẩu thành công', life: 3000 })
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error) {
    const msg = error.response?.data?.message || 'Không thể đổi mật khẩu'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 5000 })
  } finally {
    changingPassword.value = false
  }
}

function toggleDarkMode() {
  localStorage.setItem('darkMode', darkMode.value.toString())
  if (darkMode.value) {
    document.documentElement.classList.add('dark-mode')
  } else {
    document.documentElement.classList.remove('dark-mode')
  }
  toast.add({
    severity: 'success',
    summary: 'Đã cập nhật',
    detail: darkMode.value ? 'Đã bật chế độ tối' : 'Đã tắt chế độ tối',
    life: 2000
  })
}

async function checkBackendHealth() {
  checkingHealth.value = true
  try {
    await apiClient.get('/health')
    backendStatus.value = true
    toast.add({ severity: 'success', summary: 'Kết nối tốt', detail: 'Backend đang hoạt động bình thường', life: 3000 })
  } catch (error) {
    backendStatus.value = false
    toast.add({ severity: 'error', summary: 'Lỗi kết nối', detail: 'Không thể kết nối đến backend', life: 5000 })
  } finally {
    checkingHealth.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}

onMounted(() => {
  darkMode.value = localStorage.getItem('darkMode') === 'true'
  if (darkMode.value) {
    document.documentElement.classList.add('dark-mode')
  }
})
</script>

<style scoped>
.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

.section-title {
  font-size: 1.0625rem;
  font-weight: 600;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--surface-border);
}

.profile-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 1.25rem;
}

.profile-info h3 {
  font-size: 1.125rem;
  margin-bottom: 0.5rem;
}

.profile-role {
  margin: 0;
}

.profile-form,
.password-form {
  display: flex;
  flex-direction: column;
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

.required {
  color: var(--danger-color);
}

.field-error {
  color: var(--danger-color);
  font-size: 0.75rem;
}

.theme-settings {
  display: flex;
  flex-direction: column;
}

.theme-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
}

.theme-item h4 {
  font-size: 0.9375rem;
  margin: 0 0 0.25rem 0;
}

.theme-item p {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin: 0;
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.gap-2 { gap: 0.5rem; }

@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
