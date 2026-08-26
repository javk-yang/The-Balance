<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    error.value = '请填写用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await authStore.login(form.username, form.password)
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="min-h-[100dvh] bg-[#f3f2ee] px-4 py-6 sm:px-6 sm:py-10 lg:flex lg:items-center lg:py-12 dark:bg-ink-950">
    <div class="mx-auto grid w-full max-w-6xl overflow-hidden rounded-2xl border border-[#dfdfda] bg-white shadow-soft-lg lg:min-h-[680px] lg:grid-cols-[0.88fr_1.12fr] dark:border-white/[0.08] dark:bg-ink-850">
      <section class="relative flex flex-col justify-between overflow-hidden bg-primary-950 px-6 py-7 text-white sm:px-10 sm:py-10 lg:px-12 lg:py-12">
        <div class="relative z-10">
          <div class="flex items-center gap-3">
            <svg class="h-9 w-9 text-primary-200" viewBox="0 0 40 40" fill="none" aria-hidden="true">
              <rect x="5.5" y="5.5" width="29" height="29" rx="7" stroke="currentColor" />
              <path d="M12 27V17.5M20 27V12M28 27v-6.5M10 27.5h20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
            <div>
              <p class="text-base font-semibold tracking-[0.08em]">Balance</p>
              <p class="text-xs text-primary-200">PERSONAL FINANCE</p>
            </div>
          </div>

          <div class="mt-10 max-w-sm sm:mt-14 lg:mt-24">
            <p class="text-xs font-semibold uppercase tracking-[0.24em] text-primary-200">清晰 · 稳健 · 长期</p>
            <h1 class="mt-4 text-3xl font-semibold leading-tight tracking-tight sm:text-4xl lg:text-[2.75rem]">
              看清每一笔收支，<br class="hidden sm:block" />做更从容的决定。
            </h1>
            <p class="mt-5 max-w-xs text-sm leading-7 text-primary-100/80 sm:text-base">
              从日常账目到长期资产，用简单可靠的方式建立属于你的财务秩序。
            </p>
          </div>
        </div>

        <div class="relative z-10 mt-10 hidden border-t border-white/15 pt-6 lg:block">
          <div class="grid grid-cols-3 gap-4 text-sm">
            <div>
              <p class="text-primary-200">01</p>
              <p class="mt-1.5 text-white/90">收支归档</p>
            </div>
            <div>
              <p class="text-primary-200">02</p>
              <p class="mt-1.5 text-white/90">预算管理</p>
            </div>
            <div>
              <p class="text-primary-200">03</p>
              <p class="mt-1.5 text-white/90">资产总览</p>
            </div>
          </div>
        </div>

        <svg class="pointer-events-none absolute -bottom-8 -right-12 h-72 w-72 text-white/[0.07]" viewBox="0 0 300 300" fill="none" aria-hidden="true">
          <circle cx="150" cy="150" r="108" stroke="currentColor" />
          <circle cx="150" cy="150" r="76" stroke="currentColor" />
          <path d="M43 150h214M150 43v214M74 74l152 152M226 74 74 226" stroke="currentColor" />
        </svg>
      </section>

      <section class="flex items-center px-6 py-10 sm:px-12 sm:py-14 lg:px-20">
        <div class="mx-auto w-full max-w-md animate-fade-up">
          <div class="mb-9">
            <p class="text-sm font-medium text-primary-700 dark:text-primary-300">欢迎回来</p>
            <h2 class="mt-2 text-3xl font-semibold tracking-tight text-ink-900 dark:text-white">登录你的账户</h2>
            <p class="mt-3 text-sm leading-6 text-slate-500 dark:text-slate-400">继续管理你的账目、预算与资产。</p>
          </div>

          <div
            v-if="error"
            role="alert"
            aria-live="polite"
            class="mb-6 border-l-2 border-expense-500 bg-expense-50 px-4 py-3 text-sm text-expense-700 dark:bg-expense-500/10 dark:text-expense-300"
          >
            {{ error }}
          </div>

          <form class="space-y-5" @submit.prevent="handleLogin">
            <div>
              <label for="login-username" class="label-base">用户名 / 邮箱</label>
              <input
                id="login-username"
                v-model="form.username"
                type="text"
                autocomplete="username"
                placeholder="请输入用户名或邮箱"
                class="input-base h-12"
              />
            </div>
            <div>
              <label for="login-password" class="label-base">密码</label>
              <input
                id="login-password"
                v-model="form.password"
                type="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                class="input-base h-12"
              />
            </div>
            <button
              type="submit"
              :disabled="loading"
              class="btn-primary mt-2 h-12 w-full text-base focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 focus-visible:ring-offset-2 disabled:pointer-events-none"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </button>
          </form>

          <div class="mt-8 flex items-center gap-4" aria-hidden="true">
            <span class="h-px flex-1 bg-[#e7e7e2] dark:bg-white/10"></span>
            <span class="text-xs uppercase tracking-[0.2em] text-slate-400">新用户</span>
            <span class="h-px flex-1 bg-[#e7e7e2] dark:bg-white/10"></span>
          </div>

          <p class="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
            还没有账号？
            <router-link
              to="/register"
              class="font-semibold text-primary-700 underline-offset-4 transition-colors hover:text-primary-900 hover:underline focus-visible:rounded-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 dark:text-primary-300 dark:hover:text-primary-200"
            >
              立即注册
            </router-link>
          </p>
        </div>
      </section>
    </div>
  </main>
</template>
