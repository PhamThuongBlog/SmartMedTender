<template>
  <header class="app-header">
    <div class="header-left">
      <button class="toggle-btn" @click="$emit('toggle-sidebar')">
        <i class="pi pi-bars"></i>
      </button>
      <Breadcrumb v-if="breadcrumbItems.length > 1" :model="breadcrumbItems" class="breadcrumb">
        <template #item="{ item }">
          <router-link v-if="item.to" :to="item.to" class="breadcrumb-link">
            <i v-if="item.icon" :class="item.icon"></i>
            <span>{{ item.label }}</span>
          </router-link>
          <span v-else class="breadcrumb-current">
            <i v-if="item.icon" :class="item.icon"></i>
            <span>{{ item.label }}</span>
          </span>
        </template>
      </Breadcrumb>
    </div>

    <div class="header-right">
      <!-- Notifications -->
      <button class="header-btn" @click="goToNotifications" v-tooltip.bottom="'Thông báo'">
        <i class="pi pi-bell"></i>
        <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </button>

      <!-- Dark mode toggle -->
      <button class="header-btn" @click="$emit('toggle-dark-mode')" v-tooltip.bottom="isDarkMode ? 'Chế độ sáng' : 'Chế độ tối'">
        <i :class="isDarkMode ? 'pi pi-sun' : 'pi pi-moon'"></i>
      </button>

      <!-- User menu -->
      <div class="user-menu">
        <button class="user-btn" @click="toggleUserMenu">
          <Avatar
            :label="userInitials"
            shape="circle"
            size="normal"
            style="background-color: var(--primary-color); color: white;"
          />
          <span class="user-name">{{ authStore.user?.fullName || authStore.user?.username || 'Người dùng' }}</span>
          <i class="pi pi-chevron-down" style="font-size: 0.75rem;"></i>
        </button>

        <Menu ref="userMenuRef" :model="userMenuItems" :popup="true" />
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import Menu from 'primevue/menu'
import Avatar from 'primevue/avatar'
import Breadcrumb from 'primevue/breadcrumb'

const props = defineProps({
  isDarkMode: Boolean
})

defineEmits(['toggle-sidebar', 'toggle-dark-mode'])

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const userMenuRef = ref(null)

const unreadCount = computed(() => notificationStore.unreadCount)

const userInitials = computed(() => {
  const name = authStore.user?.fullName || authStore.user?.username || 'U'
  return name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
})

const breadcrumbItems = computed(() => {
  const items = [
    { label: 'Trang chủ', to: '/dashboard', icon: 'pi pi-home' }
  ]
  if (route.meta.title) {
    items.push({ label: route.meta.title, icon: route.meta.icon })
  }
  return items
})

const userMenuItems = computed(() => [
  {
    label: 'Hồ sơ cá nhân',
    icon: 'pi pi-user',
    command: () => router.push('/settings')
  },
  {
    label: 'Cài đặt',
    icon: 'pi pi-cog',
    command: () => router.push('/settings')
  },
  { separator: true },
  {
    label: 'Đăng xuất',
    icon: 'pi pi-sign-out',
    command: () => {
      authStore.logout()
      router.push('/login')
    }
  }
])

function toggleUserMenu(event) {
  userMenuRef.value?.toggle(event)
}

function goToNotifications() {
  router.push('/notifications')
}

onMounted(() => {
  notificationStore.fetchUnreadCount()
  // Poll for new notifications every 30 seconds
  setInterval(() => notificationStore.fetchUnreadCount(), 30000)
})
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  right: 0;
  left: var(--sidebar-width);
  height: var(--header-height);
  background: var(--surface-card);
  border-bottom: 1px solid var(--surface-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  z-index: 50;
  transition: left var(--transition-speed) ease;
}

.sidebar-collapsed .app-header {
  left: var(--sidebar-collapsed-width);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.toggle-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.375rem;
  border-radius: 6px;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: var(--surface-ground);
  color: var(--text-primary);
}

.breadcrumb {
  background: transparent;
  border: none;
  padding: 0;
}

.breadcrumb :deep(.p-breadcrumb-list) {
  gap: 0.25rem;
}

.breadcrumb-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.8125rem;
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.breadcrumb-link:hover {
  color: var(--primary-color);
}

.breadcrumb-current {
  color: var(--text-primary);
  font-size: 0.8125rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header-btn {
  background: none;
  border: none;
  font-size: 1.125rem;
  color: var(--text-secondary);
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.2s;
}

.header-btn:hover {
  background: var(--surface-ground);
  color: var(--text-primary);
}

.badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: var(--danger-color);
  color: white;
  font-size: 0.625rem;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.user-menu {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-btn:hover {
  background: var(--surface-ground);
}

.user-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .app-header {
    left: 0;
    padding: 0 1rem;
  }

  .user-name {
    display: none;
  }

  .breadcrumb {
    display: none;
  }
}
</style>
