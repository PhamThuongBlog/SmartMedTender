<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Quản lý người dùng</h1>
        <p class="page-subtitle">Quản lý tài khoản và phân quyền người dùng</p>
      </div>
      <Button label="Thêm người dùng" icon="pi pi-user-plus" @click="openCreateDialog" />
    </div>

    <div class="card-container">
      <!-- Filters -->
      <div class="filters-row mb-4">
        <span class="p-input-icon-left">
          <i class="pi pi-search" />
          <InputText v-model="searchQuery" placeholder="Tìm kiếm người dùng..." @input="onSearch" style="width: 300px;" />
        </span>
        <Dropdown v-model="roleFilter" :options="roleOptions" optionLabel="label" optionValue="value" placeholder="Vai trò" @change="onFilterChange" showClear style="width: 180px;" />
        <Dropdown v-model="statusFilter" :options="statusOptions" optionLabel="label" optionValue="value" placeholder="Trạng thái" @change="onFilterChange" showClear style="width: 180px;" />
      </div>

      <DataTable
        :value="users"
        :paginator="true"
        :rows="pageSize"
        :total-records="totalRecords"
        :loading="loading"
        :lazy="true"
        data-key="id"
        @page="onPage"
      >
        <Column field="username" header="Tên đăng nhập" sortable></Column>
        <Column field="fullName" header="Họ và tên" sortable>
          <template #body="{ data }">
            <div class="user-name-cell">
              <Avatar :label="getInitials(data.fullName || data.username)" shape="circle" size="normal" style="background-color: var(--primary-color); color: white; margin-right: 0.5rem;" />
              <span>{{ data.fullName || '-' }}</span>
            </div>
          </template>
        </Column>
        <Column field="email" header="Email" sortable style="width: 220px;">
          <template #body="{ data }">
            {{ data.email || '-' }}
          </template>
        </Column>
        <Column field="roleName" header="Vai trò" sortable style="width: 120px;">
          <template #body="{ data }">
            <Tag :value="data.roleName" :severity="data.roleName === 'ADMIN' ? 'danger' : 'info'" />
          </template>
        </Column>
        <Column field="enabled" header="Trạng thái" style="width: 120px;">
          <template #body="{ data }">
            <Tag :value="data.enabled ? 'Hoạt động' : 'Vô hiệu'" :severity="data.enabled ? 'success' : 'secondary'" />
          </template>
        </Column>
        <Column field="lastLoginAt" header="Đăng nhập cuối" sortable style="width: 160px;">
          <template #body="{ data }">
            {{ formatDate(data.lastLoginAt) }}
          </template>
        </Column>
        <Column header="Thao tác" style="width: 160px;">
          <template #body="{ data }">
            <div class="action-buttons">
              <Button icon="pi pi-pencil" severity="secondary" text rounded size="small" v-tooltip.top="'Chỉnh sửa'" @click="openEditDialog(data)" />
              <Button icon="pi pi-lock" :severity="data.accountLocked ? 'warning' : 'secondary'" text rounded size="small" v-tooltip.top="data.accountLocked ? 'Mở khóa' : 'Khóa'" @click="toggleLock(data)" />
              <Button icon="pi pi-key" severity="info" text rounded size="small" v-tooltip.top="'Đặt lại mật khẩu'" @click="resetPassword(data)" />
              <Button icon="pi pi-trash" severity="danger" text rounded size="small" v-tooltip.top="'Xóa'" @click="confirmDelete(data)" />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- User Dialog -->
    <Dialog
      v-model:visible="showUserDialog"
      :header="isEditing ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới'"
      :modal="true"
      :style="{ width: '500px' }"
    >
      <form @submit.prevent="saveUser" class="user-form">
        <div class="form-field">
          <label>Tên đăng nhập <span class="required">*</span></label>
          <InputText v-model="userForm.username" :disabled="isEditing" placeholder="Nhập tên đăng nhập" fluid />
        </div>
        <div class="form-field">
          <label>Họ và tên</label>
          <InputText v-model="userForm.fullName" placeholder="Nhập họ và tên" fluid />
        </div>
        <div class="form-field">
          <label>Email</label>
          <InputText v-model="userForm.email" placeholder="Nhập email" fluid />
        </div>
        <div class="form-field">
          <label>Số điện thoại</label>
          <InputText v-model="userForm.phone" placeholder="Nhập số điện thoại" fluid />
        </div>
        <div v-if="!isEditing" class="form-field">
          <label>Mật khẩu <span class="required">*</span></label>
          <Password v-model="userForm.password" placeholder="Nhập mật khẩu" :feedback="false" toggleMask fluid />
        </div>
        <div class="form-field">
          <label>Vai trò</label>
          <Dropdown v-model="userForm.roleName" :options="roleOptions" optionLabel="label" optionValue="value" placeholder="Chọn vai trò" fluid />
        </div>
        <div class="flex-between mt-4">
          <Button label="Hủy" severity="secondary" outlined @click="showUserDialog = false" />
          <Button type="submit" :label="isEditing ? 'Cập nhật' : 'Tạo mới'" icon="pi pi-check" :loading="saving" />
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
import Password from 'primevue/password'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import Avatar from 'primevue/avatar'

const toast = useToast()
const confirm = useConfirm()

const loading = ref(false)
const saving = ref(false)
const users = ref([])
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
const searchQuery = ref('')
const roleFilter = ref(null)
const statusFilter = ref(null)
const showUserDialog = ref(false)
const isEditing = ref(false)
const selectedUser = ref(null)

let searchTimeout = null

const roleOptions = [
  { label: 'Super Admin', value: 'SUPER_ADMIN' },
  { label: 'Quản trị viên', value: 'ADMIN' },
  { label: 'Quản lý nghiệp vụ', value: 'MANAGER' },
  { label: 'Nhân viên xử lý', value: 'STAFF' },
  { label: 'Người kiểm duyệt', value: 'REVIEWER' },
  { label: 'Chuyên viên pháp lý', value: 'LEGAL' },
  { label: 'Nhân viên KD', value: 'SALES' }
]

const statusOptions = [
  { label: 'Hoạt động', value: true },
  { label: 'Vô hiệu', value: false }
]

const userForm = ref({
  username: '', fullName: '', email: '', phone: '',
  password: '', roleName: 'USER'
})

async function fetchUsers() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchQuery.value) params.search = searchQuery.value
    if (roleFilter.value) params.role = roleFilter.value
    if (statusFilter.value !== null) params.enabled = statusFilter.value

    const response = await apiClient.get('/users', { params })
    if (response.data.content) {
      users.value = response.data.content
      totalRecords.value = response.data.totalElements
    } else {
      users.value = Array.isArray(response.data) ? response.data : []
      totalRecords.value = users.value.length
    }
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách người dùng', life: 5000 })
  } finally {
    loading.value = false
  }
}

function onSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => { currentPage.value = 0; fetchUsers() }, 300)
}

function onFilterChange() { currentPage.value = 0; fetchUsers() }
function onPage(event) { currentPage.value = event.page; pageSize.value = event.rows; fetchUsers() }

function openCreateDialog() {
  isEditing.value = false
  selectedUser.value = null
  userForm.value = { username: '', fullName: '', email: '', phone: '', password: '', roleName: 'USER' }
  showUserDialog.value = true
}

function openEditDialog(user) {
  isEditing.value = true
  selectedUser.value = user
  userForm.value = {
    username: user.username, fullName: user.fullName || '',
    email: user.email || '', phone: user.phone || '',
    password: '', roleName: user.roleName || 'USER'
  }
  showUserDialog.value = true
}

async function saveUser() {
  if (!userForm.value.username.trim()) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Tên đăng nhập không được để trống', life: 3000 })
    return
  }
  if (!isEditing.value && !userForm.value.password) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Mật khẩu không được để trống', life: 3000 })
    return
  }

  saving.value = true
  try {
    if (isEditing.value) {
      await apiClient.put(`/users/${selectedUser.value.id}`, userForm.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật người dùng', life: 3000 })
    } else {
      await apiClient.post('/users', userForm.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo người dùng mới', life: 3000 })
    }
    showUserDialog.value = false
    fetchUsers()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu người dùng', life: 5000 })
  } finally {
    saving.value = false
  }
}

async function toggleLock(user) {
  const action = user.accountLocked ? 'Mở khóa' : 'Khóa'
  confirm.require({
    message: `Bạn có chắc muốn ${action.toLowerCase()} tài khoản "${user.username}"?`,
    header: `Xác nhận ${action.toLowerCase()} tài khoản`,
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: action,
    rejectLabel: 'Hủy',
    accept: async () => {
      try {
        await apiClient.patch(`/users/${user.id}/lock`, { locked: !user.accountLocked })
        toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã ${action.toLowerCase()} tài khoản`, life: 3000 })
        fetchUsers()
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Thao tác thất bại', life: 5000 })
      }
    }
  })
}

function resetPassword(user) {
  const newPassword = prompt(`Nhập mật khẩu mới cho "${user.username}" (tối thiểu 8 ký tự):`)
  if (!newPassword || newPassword.length < 8) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Mật khẩu phải có ít nhất 8 ký tự', life: 3000 })
    return
  }
  confirm.require({
    message: `Bạn muốn đặt lại mật khẩu cho "${user.username}" thành "${'*'.repeat(newPassword.length)}"?`,
    header: 'Đặt lại mật khẩu',
    icon: 'pi pi-key',
    acceptLabel: 'Đặt lại',
    rejectLabel: 'Hủy',
    accept: async () => {
      try {
        await apiClient.patch(`/users/${user.id}/reset-password`, { newPassword })
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã đặt lại mật khẩu', life: 3000 })
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể đặt lại mật khẩu', life: 5000 })
      }
    }
  })
}

function confirmDelete(user) {
  confirm.require({
    message: `Bạn có chắc muốn xóa người dùng "${user.username}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Xóa',
    rejectLabel: 'Hủy',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await apiClient.delete(`/users/${user.id}`)
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa người dùng', life: 3000 })
        fetchUsers()
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa người dùng', life: 5000 })
      }
    }
  })
}

function getInitials(name) {
  if (!name) return 'U'
  return name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => { fetchUsers() })
</script>

<style scoped>
.filters-row {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.user-name-cell {
  display: flex;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 0.125rem;
}

.user-form {
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

.required { color: var(--danger-color); }
.mb-4 { margin-bottom: 1rem; }
.mt-4 { margin-top: 1rem; }
</style>
