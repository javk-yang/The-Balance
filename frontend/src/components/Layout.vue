<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const darkMode = ref(false)
const sidebarOpen = ref(false)

// 导航图标（细线 SVG，克制专业）
const icons: Record<string, string> = {
  dashboard: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z',
  transactions: 'M4 6h16M4 12h10M4 18h7M18 15l3 3-3 3',
  categories: 'M7 7h.01M7 3h5c1 0 1.5.5 2 1l4 4c.5.5 1 1 1 2v5a2 2 0 01-2 2H7a2 2 0 01-2-2V5a2 2 0 012-2zM13 3v5h6',
  budgets: 'M12 3a9 9 0 109 9h-9V3z',
  accounts: 'M3 10h18M5 6h14a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2zm13 8h.01',
  loans: 'M3 11l9-8 9 8M5 9v11h14V9M10 20v-6h4v6',
  assets: 'M4 21V7l8-4 8 4v14M9 21v-6h6v6M4 21h16',
  projects: 'M3 7a2 2 0 012-2h4l2 2h8a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V7z',
  profile: 'M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z',
}

const navItems = [
  { path: '/dashboard', label: '数据看板', icon: 'dashboard' },
  { path: '/transactions', label: '记账流水', icon: 'transactions' },
  { path: '/categories', label: '消费板块', icon: 'categories' },
  { path: '/budgets', label: '预算管理', icon: 'budgets' },
  { path: '/accounts', label: '账户管理', icon: 'accounts' },
  { path: '/loans', label: '贷款管理', icon: 'loans' },
  { path: '/assets', label: '固定资产', icon: 'assets' },
  { path: '/projects', label: '已签约项目', icon: 'projects' },
]

onMounted(() => {
  authStore.init()
  const saved = localStorage.getItem('theme')
  if (saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    darkMode.value = true
    document.documentElement.classList.add('dark')
  }
})

const toggleDark = () => {
  darkMode.value = !darkMode.value
  if (darkMode.value) {
    document.documentElement.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="flex h-[100dvh] overflow-hidden">
    <!-- 侧边栏 -->
    <aside
      :class="[sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0']"
      class="fixed lg:relative lg:shrink-0 z-50 w-64 h-full bg-white dark:bg-ink-900 border-r border-[#e4e5e1] dark:border-white/[0.06] transition-transform duration-300 flex flex-col"
    >
      <!-- 品牌 -->
      <div class="h-16 flex items-center px-6 border-b border-[#e4e5e1] dark:border-white/[0.06]">
        <div class="w-8 h-8 rounded-lg bg-primary-700 flex items-center justify-center mr-3">
          <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 17l6-6 4 4 8-8M14 7h7v7" />
          </svg>
        </div>
        <div>
          <p class="text-[15px] font-semibold text-ink-900 dark:text-white leading-tight">Balance</p>
          <p class="text-[10px] text-slate-400 tracking-[0.18em] uppercase">Personal Finance</p>
        </div>
      </div>

      <!-- 导航 -->
      <nav class="flex-1 py-4 px-3 space-y-0.5 overflow-y-auto">
        <p class="px-3 pb-2 text-[10px] font-semibold uppercase tracking-[0.16em] text-slate-400">菜单</p>
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          @click="sidebarOpen = false"
          :class="[
            route.path === item.path
              ? 'bg-primary-50 text-primary-800 dark:bg-primary-500/10 dark:text-primary-300'
              : 'text-slate-600 dark:text-slate-400 hover:bg-[#f2f2ef] dark:hover:bg-white/[0.04]',
          ]"
          class="group relative flex items-center px-3 py-2.5 rounded-lg transition-colors duration-150"
        >
          <svg class="w-[18px] h-[18px] mr-3 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.7">
            <path stroke-linecap="round" stroke-linejoin="round" :d="icons[item.icon]" />
          </svg>
          <span class="text-sm font-medium">{{ item.label }}</span>
        </router-link>

        <p class="px-3 pt-5 pb-2 text-[10px] font-semibold uppercase tracking-[0.16em] text-slate-400">账户</p>
        <router-link
          to="/profile"
          @click="sidebarOpen = false"
          :class="[
            route.path === '/profile'
              ? 'bg-primary-50 text-primary-800 dark:bg-primary-500/10 dark:text-primary-300'
              : 'text-slate-600 dark:text-slate-400 hover:bg-[#f2f2ef] dark:hover:bg-white/[0.04]',
          ]"
          class="flex items-center px-3 py-2.5 rounded-lg transition-colors duration-150"
        >
          <svg class="w-[18px] h-[18px] mr-3 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.7">
            <path stroke-linecap="round" stroke-linejoin="round" :d="icons.profile" />
          </svg>
          <span class="text-sm font-medium">个人设置</span>
        </router-link>
      </nav>

      <!-- 底部 -->
      <div class="p-4 border-t border-[#e4e5e1] dark:border-white/[0.06]">
        <div class="flex items-center justify-between mb-3 px-1">
          <span class="text-sm text-slate-500 dark:text-slate-400">深色模式</span>
          <button
            @click="toggleDark"
            :class="darkMode ? 'bg-primary-700' : 'bg-[#dcdcd7] dark:bg-white/10'"
            class="relative w-10 h-[22px] rounded-full transition-colors duration-200"
            role="switch"
            :aria-checked="darkMode"
            aria-label="切换深色模式"
          >
            <span
              :class="darkMode ? 'translate-x-[19px]' : 'translate-x-[3px]'"
              class="absolute top-[3px] w-4 h-4 bg-white rounded-full transition-transform shadow-sm"
            ></span>
          </button>
        </div>
        <button
          @click="handleLogout"
          class="w-full px-4 py-2 text-sm text-slate-500 hover:text-expense-600 hover:bg-expense-500/[0.07] dark:text-slate-400 rounded-lg transition-colors font-medium"
        >
          退出登录
        </button>
      </div>
    </aside>

    <!-- 移动端遮罩 -->
    <div
      v-if="sidebarOpen"
      @click="sidebarOpen = false"
      class="fixed inset-0 bg-ink-950/30 z-40 lg:hidden"
    ></div>

    <!-- 主内容区 -->
    <div class="flex h-full min-w-0 flex-1 flex-col overflow-hidden">
      <!-- 顶栏 -->
      <header class="h-16 shrink-0 flex items-center justify-between px-5 lg:px-8 border-b border-[#e4e5e1] dark:border-white/[0.06] bg-white/80 dark:bg-ink-900/60 backdrop-blur-md">
        <div class="flex items-center min-w-0">
          <button @click="sidebarOpen = !sidebarOpen" class="lg:hidden text-slate-600 dark:text-slate-300 mr-3" aria-label="打开菜单">
            <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <h1 class="text-lg font-semibold text-ink-900 dark:text-white truncate">{{ route.meta.title }}</h1>
        </div>
        <button
          @click="router.push('/profile')"
          class="flex items-center gap-3 rounded-xl px-2 py-1.5 hover:bg-[#f2f2ef] dark:hover:bg-white/[0.05] transition-colors"
          title="进入个人设置"
        >
          <div class="text-right hidden sm:block">
            <p class="text-sm font-medium text-ink-900 dark:text-slate-200 leading-tight">{{ authStore.user?.username }}</p>
            <p class="text-[11px] text-slate-400">个人账户</p>
          </div>
          <img
            v-if="authStore.user?.avatar"
            :src="authStore.user.avatar"
            alt="头像"
            class="w-9 h-9 rounded-full object-cover border border-[#e4e5e1] dark:border-white/10"
          />
          <div v-else class="w-9 h-9 rounded-full bg-primary-700 flex items-center justify-center text-sm text-white font-semibold">
            {{ authStore.user?.username?.charAt(0).toUpperCase() }}
          </div>
        </button>
      </header>

      <!-- 页面内容 -->
      <main class="min-h-0 flex-1 overflow-y-auto overflow-x-hidden p-5 lg:p-8">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>
