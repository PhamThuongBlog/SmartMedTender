import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: 'Tổng quan' }
      },
      {
        path: 'tenders',
        name: 'Tenders',
        component: () => import('@/views/TenderListView.vue'),
        meta: { title: 'Quản lý gói thầu' }
      },
      {
        path: 'tenders/create',
        name: 'TenderCreate',
        component: () => import('@/views/TenderFormView.vue'),
        meta: { title: 'Tạo gói thầu mới' }
      },
      {
        path: 'tenders/:id',
        name: 'TenderDetail',
        component: () => import('@/views/TenderDetailView.vue'),
        meta: { title: 'Chi tiết gói thầu' }
      },
      {
        path: 'tenders/:id/edit',
        name: 'TenderEdit',
        component: () => import('@/views/TenderFormView.vue'),
        meta: { title: 'Chỉnh sửa gói thầu' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/ProductListView.vue'),
        meta: { title: 'Danh sách sản phẩm' }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetailView.vue'),
        meta: { title: 'Chi tiết sản phẩm' }
      },
      {
        path: 'hsmt/upload',
        name: 'HSMTUpload',
        component: () => import('@/views/HSMTUploadView.vue'),
        meta: { title: 'Tải lên HSMT' }
      },
      {
        path: 'ocr/review',
        name: 'OCRReview',
        component: () => import('@/views/OCRReviewView.vue'),
        meta: { title: 'Xem xét yêu cầu kỹ thuật' }
      },
      {
        path: 'match',
        name: 'TechnicalComparison',
        component: () => import('@/views/TechnicalComparisonView.vue'),
        meta: { title: 'So sánh kỹ thuật' }
      },
      {
        path: 'hsdt-builder',
        name: 'HSDTBuilder',
        component: () => import('@/views/HSDTBuilderView.vue'),
        meta: { title: 'Tạo HSDT' }
      },
      {
        path: 'export',
        name: 'ExportCenter',
        component: () => import('@/views/ExportCenterView.vue'),
        meta: { title: 'Xuất tài liệu' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/UserManagementView.vue'),
        meta: { title: 'Quản lý người dùng', requiresAdmin: true }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/NotificationCenterView.vue'),
        meta: { title: 'Thông báo' }
      },
      {
        path: 'chatbot',
        name: 'Chatbot',
        component: () => import('@/views/ChatbotView.vue'),
        meta: { title: 'Trợ lý AI' }
      },
      {
        path: 'enterprise',
        name: 'Enterprise',
        component: () => import('@/views/EnterpriseSetupView.vue'),
        meta: { title: 'Thiết lập doanh nghiệp' }
      },
      {
        path: 'documents',
        name: 'DocumentLibrary',
        component: () => import('@/views/DocumentLibraryView.vue'),
        meta: { title: 'Thư viện tài liệu' }
      },
      {
        path: 'expiry-alerts',
        name: 'ExpiryAlerts',
        component: () => import('@/views/ExpiryAlertView.vue'),
        meta: { title: 'Cảnh báo hết hạn' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/SettingsView.vue'),
        meta: { title: 'Cài đặt' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.name === 'Login' && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
    return
  }

  const adminRoles = ['ADMIN', 'SUPER_ADMIN']
  if (to.meta.requiresAdmin && !adminRoles.includes(authStore.user?.roleName)) {
    next({ name: 'Dashboard' })
    return
  }

  next()
})

export default router
