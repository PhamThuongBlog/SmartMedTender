import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

export function useApi(apiFunction) {
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const toast = useToast()

  async function execute(...args) {
    loading.value = true
    error.value = null
    try {
      const response = await apiFunction(...args)
      data.value = response.data ?? response
      return data.value
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra'
      toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: error.value,
        life: 5000
      })
      throw err
    } finally {
      loading.value = false
    }
  }

  async function executeWithoutToast(...args) {
    loading.value = true
    error.value = null
    try {
      const response = await apiFunction(...args)
      data.value = response.data ?? response
      return data.value
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra'
      throw err
    } finally {
      loading.value = false
    }
  }

  function reset() {
    data.value = null
    loading.value = false
    error.value = null
  }

  return {
    data,
    loading,
    error,
    execute,
    executeWithoutToast,
    reset
  }
}

export function useApiWithPagination(apiFunction) {
  const data = ref([])
  const loading = ref(false)
  const error = ref(null)
  const totalRecords = ref(0)
  const currentPage = ref(0)
  const pageSize = ref(10)
  const toast = useToast()

  async function execute(params = {}) {
    loading.value = true
    error.value = null
    try {
      const requestParams = {
        page: currentPage.value,
        size: pageSize.value,
        ...params
      }
      const response = await apiFunction(requestParams)
      const responseData = response.data
      if (responseData.content) {
        data.value = responseData.content
        totalRecords.value = responseData.totalElements
      } else {
        data.value = Array.isArray(responseData) ? responseData : []
        totalRecords.value = data.value.length
      }
      return data.value
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra'
      toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: error.value,
        life: 5000
      })
      throw err
    } finally {
      loading.value = false
    }
  }

  function onPageChange(event) {
    currentPage.value = event.page
    pageSize.value = event.rows
  }

  function reset() {
    data.value = []
    loading.value = false
    error.value = null
    totalRecords.value = 0
    currentPage.value = 0
  }

  return {
    data,
    loading,
    error,
    totalRecords,
    currentPage,
    pageSize,
    execute,
    onPageChange,
    reset
  }
}
