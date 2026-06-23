<template>
  <aside class="app-sidebar" :class="{ collapsed }">
    <div class="sidebar-header">
      <div class="logo-container">
        <i class="pi pi-heart-fill logo-icon"></i>
        <span v-if="!collapsed" class="logo-text">MedTender</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.to"
        :to="item.to"
        class="nav-item"
        :class="{ active: isActive(item.to) }"
        v-tooltip.right="collapsed ? item.label : ''"
      >
        <i :class="item.icon"></i>
        <span v-if="!collapsed" class="nav-label">{{ item.label }}</span>
        <span v-if="!collapsed && item.badge" class="nav-badge">{{ item.badge }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="user-info" v-if="!collapsed && authStore.user">
        <Avatar
          :label="getInitials(authStore.user.fullName || authStore.user.username)"
          shape="circle"
          size="normal"
          style="background-color: var(--primary-color); color: white;"
        />
        <div class="user-details">
          <span class="user-name">{{ authStore.user.fullName || authStore.user.username }}</span>
          <span class="user-role">{{ authStore.user.roleName }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Avatar from 'primevue/avatar'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const authStore = useAuthStore()

const menuItems = computed(() => {
  const items = [
    { to: '/dashboard', icon: 'pi pi-th-large', label: 'Tổng quan' },
    { to: '/tenders', icon: 'pi pi-briefcase', label: 'Gói thầu' },
    { to: '/products', icon: 'pi pi-box', label: 'Sản phẩm' },
    { to: '/hsmt/upload', icon: 'pi pi-upload', label: 'Tải lên HSMT' },
    { to: '/match', icon: 'pi pi-chart-bar', label: 'So sánh kỹ thuật' },
    { to: '/hsdt-builder', icon: 'pi pi-file-edit', label: 'Tạo HSDT' },
    { to: '/export', icon: 'pi pi-download', label: 'Xuất tài liệu' },
    { to: '/enterprise', icon: 'pi pi-building', label: 'Hồ sơ doanh nghiệp' },
    { to: '/documents', icon: 'pi pi-file', label: 'Thư viện tài liệu' },
    { to: '/expiry-alerts', icon: 'pi pi-exclamation-triangle', label: 'Cảnh báo hết hạn' },
    { to: '/chatbot', icon: 'pi pi-comments', label: 'Trợ lý AI' },
    { to: '/notifications', icon: 'pi pi-bell', label: 'Thông báo' },
    { to: '/settings', icon: 'pi pi-cog', label: 'Cài đặt' }
  ]

  if (authStore.isAdmin) {
    items.splice(1, 0, { to: '/users', icon: 'pi pi-users', label: 'Người dùng' })
  }

  return items
})

function isActive(path) {
  return route.path.startsWith(path)
}

function getInitials(name) {
  if (!name) return 'U'
  return name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
}

defineEmits(['toggle'])
</script>

<style scoped>
.app-sidebar {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  width: var(--sidebar-width);
  background: var(--surface-card);
  border-right: 1px solid var(--surface-border);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: width var(--transition-speed) ease;
  overflow: hidden;
}

.app-sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 1.25rem;
  border-bottom: 1px solid var(--surface-border);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-icon {
  font-size: 1.5rem;
  color: var(--primary-color);
  flex-shrink: 0;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0.75rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 0.875rem;
  border-radius: 8px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s ease;
  white-space: nowrap;
  position: relative;
}

.nav-item i {
  font-size: 1.125rem;
  width: 1.25rem;
  text-align: center;
  flex-shrink: 0;
}

.nav-item:hover {
  background: var(--primary-light);
  color: var(--primary-color);
}

.nav-item.active,
.nav-item.router-link-exact-active {
  background: var(--primary-color);
  color: white;
}

.nav-label {
  font-size: 0.875rem;
  font-weight: 500;
}

.nav-badge {
  margin-left: auto;
  background: var(--danger-color);
  color: white;
  font-size: 0.6875rem;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-weight: 600;
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid var(--surface-border);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-details {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.collapsed .nav-item {
  justify-content: center;
  padding: 0.75rem;
}

@media (max-width: 768px) {
  .app-sidebar:not(.collapsed) {
    width: var(--sidebar-width);
    box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
  }
}
</style>
