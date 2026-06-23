<template>
  <div id="app-root">
    <Toast position="top-right" />
    <ConfirmDialog />
    <router-view />
  </div>
</template>

<script setup>
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import { useAuthStore } from '@/stores/auth'
import { onMounted } from 'vue'

const authStore = useAuthStore()

onMounted(() => {
  authStore.initFromStorage()
})
</script>

<style>
#app-root {
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style>
