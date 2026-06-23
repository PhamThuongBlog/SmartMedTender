<template>
  <span class="status-badge" :class="statusClass">
    <i v-if="showIcon" :class="statusIcon" class="status-icon"></i>
    {{ displayText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true },
  showIcon: { type: Boolean, default: false }
})

const statusMap = {
  DRAFT: { label: 'Bản nháp', class: 'status-draft', icon: 'pi pi-pencil' },
  ACTIVE: { label: 'Đang hoạt động', class: 'status-active', icon: 'pi pi-play' },
  SUBMITTED: { label: 'Đã nộp', class: 'status-submitted', icon: 'pi pi-send' },
  UNDER_REVIEW: { label: 'Đang xem xét', class: 'status-under-review', icon: 'pi pi-eye' },
  AWARDED: { label: 'Trúng thầu', class: 'status-awarded', icon: 'pi pi-check-circle' },
  REJECTED: { label: 'Từ chối', class: 'status-rejected', icon: 'pi pi-times-circle' },
  CANCELLED: { label: 'Đã hủy', class: 'status-cancelled', icon: 'pi pi-ban' },
  EXPIRED: { label: 'Hết hạn', class: 'status-expired', icon: 'pi pi-clock' },
  PENDING: { label: 'Chờ xử lý', class: 'status-submitted', icon: 'pi pi-hourglass' },
  APPROVED: { label: 'Đã duyệt', class: 'status-awarded', icon: 'pi pi-verified' },
  PROCESSING: { label: 'Đang xử lý', class: 'status-active', icon: 'pi pi-spin pi-spinner' },
  COMPLETED: { label: 'Hoàn thành', class: 'status-awarded', icon: 'pi pi-check' },
  FAILED: { label: 'Thất bại', class: 'status-rejected', icon: 'pi pi-exclamation-triangle' }
}

const statusInfo = computed(() => {
  const key = props.status?.toUpperCase()
  return statusMap[key] || { label: props.status || 'Không xác định', class: 'status-draft', icon: 'pi pi-question-circle' }
})

const statusClass = computed(() => statusInfo.value.class)
const statusIcon = computed(() => statusInfo.value.icon)
const displayText = computed(() => statusInfo.value.label)
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-icon {
  font-size: 0.75rem;
}
</style>
