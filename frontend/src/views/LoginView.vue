<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">
            <i class="pi pi-heart-fill"></i>
          </div>
          <h1>MedTender</h1>
          <p>Hệ thống quản lý đấu thầu y tế</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="username">Tên đăng nhập</label>
            <div class="p-input-icon-left w-full">
              <i class="pi pi-user"></i>
              <InputText
                id="username"
                v-model="username"
                placeholder="Nhập tên đăng nhập"
                :class="{ 'p-invalid': errors.username }"
                :disabled="loading"
                autocomplete="username"
                fluid
              />
            </div>
            <small v-if="errors.username" class="error-text">{{ errors.username }}</small>
          </div>

          <div class="form-group">
            <label for="password">Mật khẩu</label>
            <div class="p-input-icon-left w-full">
              <i class="pi pi-lock"></i>
              <Password
                id="password"
                v-model="password"
                placeholder="Nhập mật khẩu"
                :feedback="false"
                :class="{ 'p-invalid': errors.password }"
                :disabled="loading"
                toggleMask
                fluid
              />
            </div>
            <small v-if="errors.password" class="error-text">{{ errors.password }}</small>
          </div>

          <div v-if="loginError" class="login-error">
            <i class="pi pi-exclamation-circle"></i>
            {{ loginError }}
          </div>

          <Button
            type="submit"
            label="Đăng nhập"
            icon="pi pi-sign-in"
            :loading="loading"
            fluid
            class="login-btn"
          />
        </form>

        <div class="login-footer">
          <p>&copy; 2026 MedTender. Đã đăng ký bản quyền.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const username = ref('')
const password = ref('')
const loading = ref(false)
const loginError = ref('')
const errors = ref({})

function validate() {
  errors.value = {}
  if (!username.value.trim()) {
    errors.value.username = 'Vui lòng nhập tên đăng nhập'
  }
  if (!password.value.trim()) {
    errors.value.password = 'Vui lòng nhập mật khẩu'
  }
  return Object.keys(errors.value).length === 0
}

async function handleLogin() {
  loginError.value = ''
  if (!validate()) return

  loading.value = true
  try {
    await authStore.login(username.value.trim(), password.value)
    toast.add({
      severity: 'success',
      summary: 'Đăng nhập thành công',
      detail: 'Chào mừng bạn đến với MedTender!',
      life: 3000
    })
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    const msg = error.response?.data?.message || error.message || 'Đăng nhập thất bại'
    if (error.response?.status === 401) {
      loginError.value = 'Tên đăng nhập hoặc mật khẩu không chính xác'
    } else if (error.response?.status === 423) {
      loginError.value = 'Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.'
    } else {
      loginError.value = msg
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 50%, #60a5fa 100%);
  padding: 1.5rem;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: var(--surface-card);
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-logo {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.login-logo i {
  font-size: 2rem;
  color: white;
}

.login-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.login-header p {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.error-text {
  color: var(--danger-color);
  font-size: 0.75rem;
}

.login-error {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #991b1b;
  font-size: 0.875rem;
}

.login-btn {
  margin-top: 0.5rem;
}

.login-footer {
  text-align: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}

.login-footer p {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.w-full {
  width: 100%;
}
</style>
