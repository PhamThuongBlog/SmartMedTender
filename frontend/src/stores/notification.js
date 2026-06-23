import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/api/client'
import { useAuthStore } from './auth'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const totalRecords = ref(0)

  async function fetchNotifications(page = 0, size = 20) {
    const authStore = useAuthStore()
    if (!authStore.userId) return

    loading.value = true
    try {
      const response = await apiClient.get('/notifications', {
        params: { userId: authStore.userId, page, size }
      })
      notifications.value = response.data.content || response.data
      totalRecords.value = response.data.totalElements || 0
    } catch (error) {
      console.error('Failed to fetch notifications:', error)
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    const authStore = useAuthStore()
    if (!authStore.userId) return

    try {
      const response = await apiClient.get('/notifications/unread-count', {
        params: { userId: authStore.userId }
      })
      unreadCount.value = response.data.count || 0
    } catch (error) {
      console.error('Failed to fetch unread count:', error)
    }
  }

  async function markAsRead(notificationId) {
    try {
      await apiClient.patch(`/notifications/${notificationId}/read`)
      const notif = notifications.value.find(n => n.id === notificationId)
      if (notif) {
        notif.isRead = true
        notif.readAt = new Date().toISOString()
      }
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }

  async function markAllAsRead() {
    const authStore = useAuthStore()
    if (!authStore.userId) return

    try {
      await apiClient.patch('/notifications/read-all', null, {
        params: { userId: authStore.userId }
      })
      notifications.value.forEach(n => {
        n.isRead = true
        n.readAt = new Date().toISOString()
      })
      unreadCount.value = 0
    } catch (error) {
      console.error('Failed to mark all as read:', error)
    }
  }

  return {
    notifications,
    unreadCount,
    loading,
    totalRecords,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead
  }
})
