<template>
  <div class="main-layout" :class="{ 'dark-mode': isDarkMode, 'sidebar-collapsed': sidebarCollapsed }">
    <AppSidebar :collapsed="sidebarCollapsed" @toggle="sidebarCollapsed = !sidebarCollapsed" />
    <div class="main-content">
      <AppHeader
        :sidebar-collapsed="sidebarCollapsed"
        :is-dark-mode="isDarkMode"
        @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
        @toggle-dark-mode="toggleDarkMode"
      />
      <div class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'

const sidebarCollapsed = ref(false)
const isDarkMode = ref(false)

function toggleDarkMode() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value.toString())
}

function handleResize() {
  if (window.innerWidth < 768) {
    sidebarCollapsed.value = true
  }
}

onMounted(() => {
  const savedDarkMode = localStorage.getItem('darkMode')
  if (savedDarkMode === 'true') {
    isDarkMode.value = true
  }
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--surface-ground);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-speed) ease;
}

.main-layout.sidebar-collapsed .main-content {
  margin-left: var(--sidebar-collapsed-width);
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  margin-top: var(--header-height);
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }

  .main-layout.sidebar-collapsed .main-content {
    margin-left: 0;
  }
}
</style>
