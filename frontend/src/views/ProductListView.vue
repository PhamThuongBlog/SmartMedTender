<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Danh sách sản phẩm</h1>
        <p class="page-subtitle">Quản lý danh mục sản phẩm y tế</p>
      </div>
      <Button label="Thêm sản phẩm" icon="pi pi-plus" @click="showProductDialog = true" />
    </div>

    <div class="card-container">
      <!-- Filters -->
      <div class="filters-row">
        <span class="p-input-icon-left">
          <i class="pi pi-search" />
          <InputText v-model="searchQuery" placeholder="Tìm kiếm sản phẩm..." @input="onSearch" style="width: 300px;" />
        </span>
        <Dropdown
          v-model="categoryFilter"
          :options="categories"
          placeholder="Danh mục"
          @change="onFilterChange"
          showClear
          style="width: 200px;"
        />
        <Dropdown
          v-model="statusFilter"
          :options="statusOptions"
          placeholder="Trạng thái"
          @change="onFilterChange"
          showClear
          style="width: 180px;"
        />
      </div>

      <!-- Product Table -->
      <DataTable
        :value="products"
        :paginator="true"
        :rows="pageSize"
        :total-records="totalRecords"
        :loading="loading"
        :lazy="true"
        data-key="id"
        @page="onPage"
        @row-click="onRowClick"
        row-hover
      >
        <Column field="name" header="Tên sản phẩm" sortable>
          <template #body="{ data }">
            <div class="product-name-cell">
              <span class="product-name">{{ data.name }}</span>
              <span class="product-reg">{{ data.registrationNumber || '-' }}</span>
            </div>
          </template>
        </Column>
        <Column field="manufacturer" header="Hãng sản xuất" sortable style="width: 180px;">
          <template #body="{ data }">
            {{ data.manufacturer || '-' }}
          </template>
        </Column>
        <Column field="brand" header="Thương hiệu" sortable style="width: 150px;">
          <template #body="{ data }">
            {{ data.brand || data.model || '-' }}
          </template>
        </Column>
        <Column field="originCountry" header="Xuất xứ" sortable style="width: 120px;">
          <template #body="{ data }">
            {{ data.originCountry || '-' }}
          </template>
        </Column>
        <Column field="category" header="Danh mục" sortable style="width: 150px;">
          <template #body="{ data }">
            <Tag :value="data.category" severity="info" />
          </template>
        </Column>
        <Column header="Thao tác" style="width: 100px;">
          <template #body="{ data }">
            <Button
              icon="pi pi-ellipsis-h"
              severity="secondary"
              text
              rounded
              size="small"
              @click.stop="toggleMenu($event, data)"
            />
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- Context Menu -->
    <Menu ref="actionMenu" :model="menuItems" :popup="true" />

    <!-- Create/Edit Product Dialog -->
    <Dialog
      v-model:visible="showProductDialog"
      :header="isEditing ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'"
      :modal="true"
      :style="{ width: '650px' }"
    >
      <form @submit.prevent="saveProduct" class="product-form">
        <div class="form-grid">
          <div class="form-field full-width">
            <label>Tên sản phẩm <span class="required">*</span></label>
            <InputText v-model="productForm.name" placeholder="Nhập tên sản phẩm" fluid />
          </div>
          <div class="form-field">
            <label>Hãng sản xuất</label>
            <InputText v-model="productForm.manufacturer" placeholder="Tên hãng" />
          </div>
          <div class="form-field">
            <label>Thương hiệu</label>
            <InputText v-model="productForm.brand" placeholder="Thương hiệu" />
          </div>
          <div class="form-field">
            <label>Model</label>
            <InputText v-model="productForm.model" placeholder="Model" />
          </div>
          <div class="form-field">
            <label>Xuất xứ</label>
            <InputText v-model="productForm.originCountry" placeholder="Quốc gia" />
          </div>
          <div class="form-field">
            <label>Danh mục</label>
            <Dropdown v-model="productForm.category" :options="categories" placeholder="Chọn danh mục" />
          </div>
          <div class="form-field">
            <label>Số đăng ký</label>
            <InputText v-model="productForm.registrationNumber" placeholder="Số đăng ký" />
          </div>
          <div class="form-field">
            <label>Ngày cấp</label>
            <Calendar v-model="productForm.registrationIssueDate" dateFormat="dd/mm/yy" />
          </div>
          <div class="form-field">
            <label>Ngày hết hạn</label>
            <Calendar v-model="productForm.registrationExpiryDate" dateFormat="dd/mm/yy" />
          </div>
          <div class="form-field full-width">
            <label>Mô tả</label>
            <Textarea v-model="productForm.description" rows="3" autoResize fluid />
          </div>
          <div class="form-field full-width">
            <label>Chứng chỉ</label>
            <div class="flex-center gap-4">
              <div class="flex-center gap-2">
                <Checkbox v-model="productForm.hasIso" :binary="true" inputId="hasIso" />
                <label for="hasIso">ISO</label>
              </div>
              <div class="flex-center gap-2">
                <Checkbox v-model="productForm.hasFda" :binary="true" inputId="hasFda" />
                <label for="hasFda">FDA</label>
              </div>
              <div class="flex-center gap-2">
                <Checkbox v-model="productForm.hasCe" :binary="true" inputId="hasCe" />
                <label for="hasCe">CE</label>
              </div>
              <div class="flex-center gap-2">
                <Checkbox v-model="productForm.hasCoCq" :binary="true" inputId="hasCoCq" />
                <label for="hasCoCq">CO/CQ</label>
              </div>
            </div>
          </div>
        </div>
        <div class="flex-between mt-4">
          <Button label="Hủy" severity="secondary" outlined @click="showProductDialog = false" />
          <Button type="submit" label="Lưu" icon="pi pi-check" :loading="saving" />
        </div>
      </form>
    </Dialog>
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
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import Menu from 'primevue/menu'

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const loading = ref(false)
const saving = ref(false)
const products = ref([])
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
const searchQuery = ref('')
const categoryFilter = ref(null)
const statusFilter = ref(null)
const showProductDialog = ref(false)
const isEditing = ref(false)
const actionMenu = ref(null)
const selectedProduct = ref(null)

let searchTimeout = null

const categories = ['Thiết bị y tế', 'Dược phẩm', 'Vật tư tiêu hao', 'Hóa chất xét nghiệm', 'Thiết bị chẩn đoán hình ảnh', 'Khác']
const statusOptions = ['ACTIVE', 'INACTIVE', 'DISCONTINUED']

const productForm = ref({
  name: '', manufacturer: '', brand: '', model: '', originCountry: '',
  category: '', description: '', registrationNumber: '',
  registrationIssueDate: null, registrationExpiryDate: null,
  hasIso: false, hasFda: false, hasCe: false, hasCoCq: false
})

const menuItems = [
  {
    label: 'Xem chi tiết',
    icon: 'pi pi-eye',
    command: () => selectedProduct.value && router.push(`/products/${selectedProduct.value.id}`)
  },
  {
    label: 'Chỉnh sửa',
    icon: 'pi pi-pencil',
    command: () => { isEditing.value = true; openEditDialog(selectedProduct.value) }
  },
  { separator: true },
  {
    label: 'Xóa',
    icon: 'pi pi-trash',
    command: () => confirmDelete(selectedProduct.value)
  }
]

function toggleMenu(event, product) {
  selectedProduct.value = product
  actionMenu.value?.toggle(event)
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchQuery.value) params.search = searchQuery.value
    if (categoryFilter.value) params.category = categoryFilter.value
    if (statusFilter.value) params.status = statusFilter.value

    const response = await apiClient.get('/products', { params })
    if (response.data.content) {
      products.value = response.data.content
      totalRecords.value = response.data.totalElements
    } else {
      products.value = Array.isArray(response.data) ? response.data : []
      totalRecords.value = products.value.length
    }
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách sản phẩm', life: 5000 })
  } finally {
    loading.value = false
  }
}

function onSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => { currentPage.value = 0; fetchProducts() }, 300)
}

function onFilterChange() {
  currentPage.value = 0
  fetchProducts()
}

function onPage(event) {
  currentPage.value = event.page
  pageSize.value = event.rows
  fetchProducts()
}

function onRowClick(event) {
  router.push(`/products/${event.data.id}`)
}

function openEditDialog(product) {
  productForm.value = {
    name: product.name || '',
    manufacturer: product.manufacturer || '',
    brand: product.brand || '',
    model: product.model || '',
    originCountry: product.originCountry || '',
    category: product.category || '',
    description: product.description || '',
    registrationNumber: product.registrationNumber || '',
    registrationIssueDate: product.registrationIssueDate ? new Date(product.registrationIssueDate) : null,
    registrationExpiryDate: product.registrationExpiryDate ? new Date(product.registrationExpiryDate) : null,
    hasIso: product.hasIso || false,
    hasFda: product.hasFda || false,
    hasCe: product.hasCe || false,
    hasCoCq: product.hasCoCq || false
  }
  showProductDialog.value = true
}

async function saveProduct() {
  if (!productForm.value.name.trim()) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Tên sản phẩm không được để trống', life: 3000 })
    return
  }
  saving.value = true
  try {
    if (isEditing.value && selectedProduct.value) {
      await apiClient.put(`/products/${selectedProduct.value.id}`, productForm.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật sản phẩm', life: 3000 })
    } else {
      await apiClient.post('/products', productForm.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm sản phẩm mới', life: 3000 })
    }
    showProductDialog.value = false
    resetForm()
    fetchProducts()
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu sản phẩm', life: 5000 })
  } finally {
    saving.value = false
  }
}

function confirmDelete(product) {
  confirm.require({
    message: `Bạn có chắc muốn xóa sản phẩm "${product.name}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Xóa',
    rejectLabel: 'Hủy',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await apiClient.delete(`/products/${product.id}`)
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa sản phẩm', life: 3000 })
        fetchProducts()
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa sản phẩm', life: 5000 })
      }
    }
  })
}

function resetForm() {
  isEditing.value = false
  selectedProduct.value = null
  productForm.value = {
    name: '', manufacturer: '', brand: '', model: '', originCountry: '',
    category: '', description: '', registrationNumber: '',
    registrationIssueDate: null, registrationExpiryDate: null,
    hasIso: false, hasFda: false, hasCe: false, hasCoCq: false
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.filters-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.product-name-cell {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: 500;
  color: var(--primary-color);
  cursor: pointer;
}

.product-name:hover {
  text-decoration: underline;
}

.product-reg {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.product-form .form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field label {
  font-size: 0.875rem;
  font-weight: 600;
}

.required { color: var(--danger-color); }
.mt-4 { margin-top: 1rem; }
</style>
