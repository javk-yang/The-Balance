<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const darkMode = ref(false)
const sidebarOpen = ref(false)

const navItems = [
  { path: '/dashboard', label: '数据看板', icon: '📊' },
  { path: '/transactions', label: '记账流水', icon: '📝' },
  { path: '/categories', label: '消费板块', icon: '🏷️' },
  { path: '/budgets', label: '预算管理', icon: '🎯' },
  { path: '/accounts', label: '账户管理', icon: '💳' },
  { path: '/loans', label: '贷款管理', icon: '🏠' },
  { path: '/assets', label: '固定资产', icon: '🏢' },
  { path: '/projects', label: '已签约项目', icon: '📁' },
]

onMounted(() => {
  authStore.init()
  const saved = localStorage.getItem('theme')
  if (saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    darkMode.value = true
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
  <div class="min-h-screen flex">
    <!-- 侧边栏 -->
    <aside
      :class="[sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0']"
      class="fixed lg:static z-50 w-72 h-full lg:h-screen bg-white/80 dark:bg-ink-850/80 backdrop-blur-2xl border-r border-slate-200/70 dark:border-white/5 transition-transform duration-300 flex flex-col"
    >
      <!-- Logo -->
      <div class="h-20 flex items-center px-7">
        <div class="w-10 h-10 rounded-2xl bg-brand-gradient flex items-center justify-center text-xl shadow-glow mr-3">
          🐕
        </div>
        <div>
          <p class="text-lg font-bold text-slate-800 dark:text-white leading-tight">二狗记账</p>
          <p class="text-[11px] text-slate-400 tracking-wide">PERSONAL FINANCE</p>
        </div>
      </div>

      <!-- 导航 -->
      <nav class="flex-1 py-5 px-4 space-y-1.5 overflow-y-auto">
        <p class="px-3 text-[11px] font-semibold uppercase tracking-widest text-slate-400 mb-2">菜单</p>
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          @click="sidebarOpen = false"
          :class="[
            route.path === item.path
              ? 'bg-brand-gradient text-white shadow-glow'
              : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-white/5',
          ]"
          class="group relative flex items-center px-4 py-3 rounded-xl transition-all duration-200 cursor-pointer"
        >
          <span v-if="route.path === item.path" class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-7 bg-white rounded-r-full"></span>
          <span class="text-xl mr-3 transition-transform group-hover:scale-110">{{ item.icon }}</span>
          <span class="font-medium">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- 底部 -->
      <div class="p-4 border-t border-slate-200/70 dark:border-white/5">
        <div class="flex items-center justify-between mb-3 px-1">
          <span class="text-sm text-slate-500 dark:text-slate-400">深色模式</span>
          <button
            @click="toggleDark"
            :class="darkMode ? 'bg-brand-gradient' : 'bg-slate-300 dark:bg-slate-700'"
            class="relative w-11 h-6 rounded-full transition-colors duration-300"
          >
            <span class="absolute top-0.5 left-0.5 text-[10px] leading-5">{{ darkMode ? '🌙' : '☀️' }}</span>
            <span
              :class="darkMode ? 'translate-x-5' : 'translate-x-0'"
              class="absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform shadow"
            ></span>
          </button>
        </div>
        <button
          @click="handleLogout"
          class="w-full px-4 py-2.5 text-sm text-expense-500 hover:bg-expense-500/10 dark:hover:bg-expense-500/15 rounded-xl transition-colors font-medium"
        >
          退出登录
        </button>
      </div>
    </aside>

    <!-- 移动端遮罩 -->
    <div
      v-if="sidebarOpen"
      @click="sidebarOpen = false"
      class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-40 lg:hidden"
    ></div>

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 顶栏 -->
      <header class="h-20 flex items-center justify-between px-5 lg:px-8 border-b border-slate-200/70 dark:border-white/5 bg-white/60 dark:bg-ink-900/40 backdrop-blur-xl">
        <div class="flex items-center">
          <button @click="sidebarOpen = !sidebarOpen" class="lg:hidden text-slate-600 dark:text-slate-300 text-2xl mr-3">
            ☰
          </button>
          <div>
            <h1 class="text-xl font-bold text-slate-800 dark:text-white">{{ route.meta.title }}</h1>
            <p class="text-xs text-slate-400 mt-0.5">欢迎回来，{{ authStore.user?.username }}</p>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <div class="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-100 dark:bg-white/5 text-slate-500 dark:text-slate-400 text-xs">
            <span class="w-2 h-2 rounded-full bg-income-500 animate-pulse"></span>
            数据已同步
          </div>
          <div class="flex items-center gap-3">
            <div class="text-right hidden sm:block">
              <p class="text-sm font-semibold text-slate-700 dark:text-slate-200">{{ authStore.user?.username }}</p>
              <p class="text-[11px] text-slate-400">普通用户</p>
            </div>
            <div class="w-10 h-10 rounded-full bg-brand-gradient flex items-center justify-center text-white font-bold shadow-glow">
              {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </div>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="flex-1 overflow-auto p-5 lg:p-8">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>
