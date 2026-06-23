import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/api/client'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(null)
  const refreshToken = ref(null)
  const initialized = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => ['ADMIN', 'SUPER_ADMIN'].includes(user.value?.roleName))
  const userId = computed(() => user.value?.id)

  function initFromStorage() {
    const storedToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')
    const storedUser = localStorage.getItem('user')

    if (storedToken) {
      token.value = storedToken
      refreshToken.value = storedRefreshToken
      try {
        user.value = storedUser ? JSON.parse(storedUser) : null
      } catch {
        user.value = null
      }
    }
    initialized.value = true
  }

  async function login(username, password) {
    const response = await apiClient.post('/auth/login', { username, password })
    const data = response.data

    token.value = data.accessToken
    refreshToken.value = data.refreshToken

    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)

    await fetchCurrentUser()

    return data
  }

  async function fetchCurrentUser() {
    try {
      const response = await apiClient.get('/auth/me')
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(response.data))
    } catch (error) {
      console.error('Failed to fetch current user:', error)
    }
  }

  async function refreshAccessToken() {
    try {
      const response = await apiClient.post('/auth/refresh', {
        refreshToken: refreshToken.value
      })
      token.value = response.data.accessToken
      refreshToken.value = response.data.refreshToken
      localStorage.setItem('accessToken', response.data.accessToken)
      localStorage.setItem('refreshToken', response.data.refreshToken)
      return response.data
    } catch (error) {
      logout()
      throw error
    }
  }

  async function changePassword(oldPassword, newPassword) {
    await apiClient.post('/auth/change-password', {
      oldPassword,
      newPassword
    })
  }

  function logout() {
    user.value = null
    token.value = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return {
    user,
    token,
    refreshToken,
    initialized,
    isAuthenticated,
    isAdmin,
    userId,
    initFromStorage,
    login,
    logout,
    fetchCurrentUser,
    refreshAccessToken,
    changePassword
  }
})
