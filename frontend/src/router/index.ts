import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { authApi } from '@/api/auth'

let verifiedToken = ''

const clearStoredSession = () => {
  verifiedToken = ''
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

// 首次进入业务页面时向后端校验 Token，避免伪造或过期 Token 绕过前端守卫
const verifySession = async (token: string) => {
  if (verifiedToken === token) return true

  try {
    await authApi.profile()
    verifiedToken = token
    return true
  } catch {
    clearStoredSession()
    return false
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板' },
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: () => import('@/views/Transactions.vue'),
        meta: { title: '记账流水' },
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/Categories.vue'),
        meta: { title: '消费板块' },
      },
      {
        path: 'budgets',
        name: 'Budgets',
        component: () => import('@/views/Budgets.vue'),
        meta: { title: '预算管理' },
      },
      {
        path: 'accounts',
        name: 'Accounts',
        component: () => import('@/views/Accounts.vue'),
        meta: { title: '账户管理' },
      },
      {
        path: 'loans',
        name: 'Loans',
        component: () => import('@/views/Loans.vue'),
        meta: { title: '贷款管理' },
      },
      {
        path: 'assets',
        name: 'Assets',
        component: () => import('@/views/Assets.vue'),
        meta: { title: '固定资产' },
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/Projects.vue'),
        meta: { title: '已签约项目' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人设置' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 - 未登录或登录凭证无效时禁止访问业务页面
router.beforeEach(async (to) => {
  const token = localStorage.getItem('token') || ''
  const isPublicRoute = to.meta.public === true

  if (isPublicRoute) {
    if (token && (await verifySession(token))) {
      return { path: '/dashboard' }
    }
    return true
  }

  if (!token || !(await verifySession(token))) {
    return {
      path: '/login',
      query: to.fullPath === '/' ? undefined : { redirect: to.fullPath },
    }
  }

  return true
})

export default router
