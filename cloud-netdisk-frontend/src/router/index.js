import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'folders/:folderId?', name: 'Folders', component: () => import('../views/Home.vue') },
      { 
        path: 'user/management', 
        name: 'UserManagement', 
        component: () => import('../views/UserManagement.vue'),
        meta: { requiresAdmin: true }
      },
      { 
        path: 'recycle', 
        name: 'RecycleBin', 
        component: () => import('../views/RecycleBin.vue'),
        meta: { requiresNotGuest: true }
      },
      { 
        path: 'starred', 
        name: 'Starred', 
        component: () => import('../views/StarredFiles.vue'),
        meta: { requiresNotGuest: true }
      },
      {
        path: 'share',
        name: 'Share',
        component: () => import('../views/Share.vue'),
        meta: { requiresNotGuest: true }
      },
      {
        path: 'share-management',
        name: 'ShareManagement',
        component: () => import('../views/ShareManagement.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const userStore = useUserStore()

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    if (!token) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }

    // 确保用户信息已加载
    if (!userStore.userId) {
      await userStore.fetchUserInfo()
    }

    // 检查管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin()) {
      ElMessage.warning('需要管理员权限')
      next('/home')
      return
    }

    // 检查非访客权限
    if (to.meta.requiresNotGuest && userStore.isGuest()) {
      ElMessage.warning(userStore.getNoPermissionMessage('访问此功能'))
      next('/home')
      return
    }
  }

  // 如果已登录用户访问登录或注册页面，重定向到首页
  if (token && (to.path === '/login' || to.path === '/register')) {
    next('/home')
    return
  }

  next()
})

export default router 