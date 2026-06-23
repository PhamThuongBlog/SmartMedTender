<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Thông báo</h1>
        <p class="page-subtitle">Trung tâm thông báo và cập nhật hệ thống</p>
      </div>
      <Button
        v-if="unreadCount > 0"
        label="Đánh dấu tất cả đã đọc"
        icon="pi pi-check-circle"
        severity="secondary"
        outlined
        @click="markAllRead"
      />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Empty -->
    <div v-else-if="notifications.length === 0" class="card-container empty-state">
      <i class="pi pi-bell" style="font-size: 3rem; color: var(--text-secondary);"></i>
      <p>Không có thông báo nào</p>
    </div>

    <!-- Notification List -->
    <div v-else class="notification-list">
      <div
        v-for="notif in notifications"
        :key="notif.id"
        class="notification-item"
        :class="{ unread: !notif.isRead }"
        @click="handleNotificationClick(notif)"
      >
        <div class="notification-icon" :class="`type-${notif.type?.toLowerCase()}`">
          <i :class="getTypeIcon(notif.type)"></i>
        </div>
        <div class="notification-content">
          <div class="notification-header">
            <span class="notification-title">{{ notif.title }}</span>
            <span class="notification-priority">
              <Tag
                v-if="notif.priority === 'HIGH'"
                value="Quan trọng"
                severity="danger"
              />
              <Tag
                v-else-if="notif.priority === 'MEDIUM'"
                value="Bình thường"
                severity="warning"
              />
            </span>
          </div>
          <p class="notification-message">{{ notif.message }}</p>
          <div class="notification-footer">
            <span class="notification-time">{{ formatTime(notif.createdAt) }}</span>
            <Button
              v-if="!notif.isRead"
              label="Đánh dấu đã đọc"
              severity="info"
              text
              size="small"
              @click.stop="markRead(notif.id)"
            />
          </div>
        </div>
        <div v-if="!notif.isRead" class="unread-dot"></div>
      </div>
    </div>

    <!-- Pagination -->
    <Paginator
      v-if="totalRecords > pageSize"
      :rows="pageSize"
      :total-records="totalRecords"
      @page="onPage"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useNotificationStore } from '@/stores/notification'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Paginator from 'primevue/paginator'
import ProgressSpinner from 'primevue/progressspinner'

const router = useRouter()
const toast = useToast()
const notificationStore = useNotificationStore()

const notifications = computed(() => notificationStore.notifications)
const loading = computed(() => notificationStore.loading)
const totalRecords = computed(() => notificationStore.totalRecords)
const unreadCount = computed(() => notificationStore.unreadCount)
const pageSize = ref(20)
const currentPage = ref(0)

function getTypeIcon(type) {
  const icons = {
    TENDER: 'pi pi-briefcase',
    PRODUCT: 'pi pi-box',
    MATCH: 'pi pi-chart-bar',
    SYSTEM: 'pi pi-cog',
    USER: 'pi pi-user',
    DOCUMENT: 'pi pi-file',
    ALERT: 'pi pi-exclamation-triangle',
    OCR: 'pi pi-file-pdf'
  }
  return icons[type?.toUpperCase()] || 'pi pi-bell'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return 'Vừa xong'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} phút trước`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} giờ trước`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} ngày trước`
  return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

async function markRead(id) {
  await notificationStore.markAsRead(id)
}

async function markAllRead() {
  await notificationStore.markAllAsRead()
  toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã đánh dấu tất cả đã đọc', life: 3000 })
}

function handleNotificationClick(notif) {
  if (!notif.isRead) {
    markRead(notif.id)
  }
  if (notif.link) {
    router.push(notif.link)
  }
}

function onPage(event) {
  currentPage.value = event.page
  notificationStore.fetchNotifications(event.page, pageSize.value)
}

onMounted(() => {
  notificationStore.fetchNotifications(0, pageSize.value)
  notificationStore.fetchUnreadCount()
})
</script>

<style scoped>
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: var(--surface-card);
  border: 1px solid var(--surface-border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.notification-item:hover {
  background: var(--surface-ground);
}

.notification-item.unread {
  border-left: 3px solid var(--primary-color);
  background: var(--primary-light);
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.125rem;
}

.type-tender { background: #dbeafe; color: var(--primary-color); }
.type-product { background: #d1fae5; color: var(--success-color); }
.type-match { background: #e0e7ff; color: #4f46e5; }
.type-system { background: #f3f4f6; color: var(--text-secondary); }
.type-user { background: #fef3c7; color: var(--warning-color); }
.type-alert { background: #fee2e2; color: var(--danger-color); }
.type-ocr, .type-document { background: #cffafe; color: var(--info-color); }

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.notification-title {
  font-weight: 600;
  font-size: 0.9375rem;
  color: var(--text-primary);
}

.notification-message {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.4;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
}

.notification-time {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--primary-color);
  border-radius: 50%;
  position: absolute;
  top: 1.25rem;
  right: 1.25rem;
}
</style>
