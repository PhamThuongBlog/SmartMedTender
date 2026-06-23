<template>
  <div class="custom-data-table">
    <div v-if="$slots.header || showHeader" class="table-header">
      <slot name="header">
        <div class="flex-between">
          <div class="flex-center gap-3">
            <span v-if="title" class="table-title">{{ title }}</span>
            <slot name="headerActions" />
          </div>
          <div class="flex-center gap-3">
            <span v-if="showSearch" class="p-input-icon-left">
              <i class="pi pi-search" />
              <InputText
                v-model="searchQuery"
                placeholder="Tìm kiếm..."
                @input="debouncedSearch"
                size="small"
                style="width: 250px;"
              />
            </span>
            <slot name="headerRight" />
          </div>
        </div>
      </slot>
    </div>

    <DataTable
      v-bind="$attrs"
      :value="value"
      :paginator="paginator"
      :rows="rows"
      :total-records="totalRecords"
      :lazy="lazy"
      :loading="loading"
      :rows-per-page-options="rowsPerPageOptions"
      :current-page-report-template="currentPageReportTemplate"
      :paginator-template="paginatorTemplate"
      :row-hover="rowHover"
      :selection-mode="selectionMode"
      :selection="selection"
      :filters="filters"
      :data-key="dataKey"
      :sort-field="sortField"
      :sort-order="sortOrder"
      :global-filter-fields="globalFilterFields"
      :empty-message="emptyMessage"
      :show-gridlines="showGridlines"
      :striped-rows="stripedRows"
      :size="size"
      :scrollable="scrollable"
      :scroll-height="scrollHeight"
      :resizable-columns="resizableColumns"
      :reorderable-columns="reorderableColumns"
      @page="$emit('page', $event)"
      @sort="$emit('sort', $event)"
      @filter="$emit('filter', $event)"
      @row-click="$emit('row-click', $event)"
      @selection-change="$emit('selection-change', $event)"
    >
      <template v-for="(_, slot) in $slots" #[slot]="scope">
        <slot :name="slot" v-bind="scope || {}" />
      </template>
    </DataTable>

    <!-- Loading overlay -->
    <div v-if="loading" class="table-loading-overlay">
      <ProgressSpinner style="width: 40px; height: 40px;" strokeWidth="4" />
    </div>

    <!-- Error state -->
    <div v-if="error && !loading" class="error-container mt-3">
      <i class="pi pi-exclamation-triangle"></i>
      <p>{{ error }}</p>
      <Button label="Thử lại" icon="pi pi-refresh" severity="secondary" @click="$emit('retry')" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'

const props = defineProps({
  value: { type: Array, default: () => [] },
  title: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  error: { type: String, default: null },
  paginator: { type: Boolean, default: true },
  rows: { type: Number, default: 10 },
  totalRecords: { type: Number, default: 0 },
  lazy: { type: Boolean, default: false },
  showSearch: { type: Boolean, default: false },
  showHeader: { type: Boolean, default: true },
  rowsPerPageOptions: { type: Array, default: () => [5, 10, 20, 50] },
  currentPageReportTemplate: { type: String, default: 'Hiển thị {first} đến {last} trong {totalRecords} bản ghi' },
  paginatorTemplate: { type: String, default: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown CurrentPageReport' },
  rowHover: { type: Boolean, default: true },
  selectionMode: { type: String, default: null },
  selection: { type: [Array, Object], default: null },
  filters: { type: Object, default: null },
  dataKey: { type: String, default: 'id' },
  sortField: { type: String, default: null },
  sortOrder: { type: Number, default: null },
  globalFilterFields: { type: Array, default: null },
  emptyMessage: { type: String, default: 'Không có dữ liệu' },
  showGridlines: { type: Boolean, default: false },
  stripedRows: { type: Boolean, default: false },
  size: { type: String, default: 'normal' },
  scrollable: { type: Boolean, default: false },
  scrollHeight: { type: String, default: 'flex' },
  resizableColumns: { type: Boolean, default: false },
  reorderableColumns: { type: Boolean, default: false },
  searchDelay: { type: Number, default: 300 }
})

const emit = defineEmits(['search', 'page', 'sort', 'filter', 'row-click', 'selection-change', 'retry'])

const searchQuery = ref('')
let searchTimeout = null

function debouncedSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    emit('search', searchQuery.value)
  }, props.searchDelay)
}

watch(() => props.loading, (val) => {
  if (!val) {
    // loading finished
  }
})
</script>

<style scoped>
.custom-data-table {
  position: relative;
}

.table-header {
  margin-bottom: 1rem;
}

.table-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.table-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  z-index: 10;
}
</style>
