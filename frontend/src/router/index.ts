import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

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
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 - 检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.public) {
    if (token && (to.name === 'Login' || to.name === 'Register')) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
