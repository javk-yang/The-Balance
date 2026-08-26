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
  email: '',
  password: '',
  phone: '',
})

const handleRegister = async () => {
  if (!form.username || !form.email || !form.password) {
    error.value = '请填写所有必填项'
    return
  }
  if (form.password.length < 6) {
    error.value = '密码至少6位'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await authStore.register(form.username, form.email, form.password, form.phone)
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="min-h-[100dvh] bg-[#f3f2ee] px-4 py-6 sm:px-6 sm:py-10 lg:flex lg:items-center lg:py-12 dark:bg-ink-950">
    <div class="mx-auto grid w-full max-w-6xl overflow-hidden rounded-2xl border border-[#dfdfda] bg-white shadow-soft-lg lg:min-h-[720px] lg:grid-cols-[1.12fr_0.88fr] dark:border-white/[0.08] dark:bg-ink-850">
      <section class="flex items-center px-6 py-10 sm:px-12 sm:py-14 lg:px-20">
        <div class="mx-auto w-full max-w-lg animate-fade-up">
          <div class="mb-8">
            <p class="text-sm font-medium text-primary-700 dark:text-primary-300">建立你的财务档案</p>
            <h1 class="mt-2 text-3xl font-semibold tracking-tight text-ink-900 dark:text-white">创建账户</h1>
            <p class="mt-3 text-sm leading-6 text-slate-500 dark:text-slate-400">从第一笔记录开始，逐步形成清晰、可持续的财务习惯。</p>
          </div>

          <div
            v-if="error"
            role="alert"
            aria-live="polite"
            class="mb-6 border-l-2 border-expense-500 bg-expense-50 px-4 py-3 text-sm text-expense-700 dark:bg-expense-500/10 dark:text-expense-300"
          >
            {{ error }}
          </div>

          <form class="grid gap-5 sm:grid-cols-2" @submit.prevent="handleRegister">
            <div>
              <label for="register-username" class="label-base">用户名 *</label>
              <input
                id="register-username"
                v-model="form.username"
                type="text"
                autocomplete="username"
                placeholder="2-50 个字符"
                class="input-base h-12"
              />
            </div>
            <div>
              <label for="register-email" class="label-base">邮箱 *</label>
              <input
                id="register-email"
                v-model="form.email"
                type="email"
                autocomplete="email"
                placeholder="your@email.com"
                class="input-base h-12"
              />
            </div>
            <div>
              <label for="register-password" class="label-base">密码 *</label>
              <input
                id="register-password"
                v-model="form.password"
                type="password"
                autocomplete="new-password"
                placeholder="至少 6 位"
                class="input-base h-12"
              />
            </div>
            <div>
              <label for="register-phone" class="label-base">手机号</label>
              <input
                id="register-phone"
                v-model="form.phone"
                type="text"
                autocomplete="tel"
                placeholder="选填"
                class="input-base h-12"
              />
            </div>
            <button
              type="submit"
              :disabled="loading"
              class="btn-primary h-12 w-full text-base focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 focus-visible:ring-offset-2 disabled:pointer-events-none sm:col-span-2"
            >
              {{ loading ? '注册中...' : '注 册' }}
            </button>
          </form>

          <p class="mt-7 text-center text-sm text-slate-500 dark:text-slate-400">
            已有账号？
            <router-link
              to="/login"
              class="font-semibold text-primary-700 underline-offset-4 transition-colors hover:text-primary-900 hover:underline focus-visible:rounded-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 dark:text-primary-300 dark:hover:text-primary-200"
            >
              返回登录
            </router-link>
          </p>
        </div>
      </section>

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
            <p class="text-xs font-semibold uppercase tracking-[0.24em] text-primary-200">从今天开始</p>
            <h2 class="mt-4 text-3xl font-semibold leading-tight tracking-tight sm:text-4xl lg:text-[2.75rem]">
              把复杂的财务，<br class="hidden sm:block" />整理成简单的日常。
            </h2>
            <p class="mt-5 max-w-xs text-sm leading-7 text-primary-100/80 sm:text-base">
              统一记录收支、预算和资产变化，让每一次查看都有明确答案。
            </p>
          </div>
        </div>

        <div class="relative z-10 mt-10 hidden space-y-4 border-t border-white/15 pt-6 lg:block">
          <div class="flex items-center gap-3 text-sm text-white/90">
            <span class="flex h-6 w-6 items-center justify-center rounded-full border border-primary-300 text-xs text-primary-200">1</span>
            <span>记录日常收支与分类</span>
          </div>
          <div class="flex items-center gap-3 text-sm text-white/90">
            <span class="flex h-6 w-6 items-center justify-center rounded-full border border-primary-300 text-xs text-primary-200">2</span>
            <span>设置预算并追踪执行</span>
          </div>
          <div class="flex items-center gap-3 text-sm text-white/90">
            <span class="flex h-6 w-6 items-center justify-center rounded-full border border-primary-300 text-xs text-primary-200">3</span>
            <span>汇总账户与资产全貌</span>
          </div>
        </div>

        <svg class="pointer-events-none absolute -bottom-10 -right-14 h-72 w-72 text-white/[0.07]" viewBox="0 0 300 300" fill="none" aria-hidden="true">
          <rect x="48" y="48" width="204" height="204" rx="32" stroke="currentColor" />
          <rect x="83" y="83" width="134" height="134" rx="20" stroke="currentColor" />
          <path d="M48 116h204M116 48v204M184 48v204M48 184h204" stroke="currentColor" />
        </svg>
      </section>
    </div>
  </main>
</template>
