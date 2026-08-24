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
  <div class="min-h-screen relative flex items-center justify-center overflow-hidden px-4">
    <!-- 背景光斑 -->
    <div class="absolute -top-40 -left-40 w-[36rem] h-[36rem] bg-primary-500/30 rounded-full blur-3xl animate-float"></div>
    <div class="absolute -bottom-40 -right-40 w-[36rem] h-[36rem] bg-fuchsia-500/25 rounded-full blur-3xl animate-float" style="animation-delay: -3s"></div>
    <div class="absolute top-1/3 left-1/2 w-[28rem] h-[28rem] bg-emerald-500/10 rounded-full blur-3xl"></div>

    <!-- 玻璃卡片 -->
    <div class="relative w-full max-w-md animate-fade-up">
      <div class="bg-white/80 dark:bg-ink-850/80 backdrop-blur-2xl rounded-3xl border border-white/60 dark:border-white/10 shadow-soft-lg p-8 sm:p-10">
        <!-- 品牌区 -->
        <div class="text-center mb-8">
          <div class="w-16 h-16 rounded-2xl bg-brand-gradient flex items-center justify-center text-3xl shadow-glow mx-auto mb-4">
            🐕
          </div>
          <h2 class="text-2xl font-bold text-slate-800 dark:text-white">二狗记账</h2>
          <p class="mt-1.5 text-sm text-slate-500 dark:text-slate-400">让每一笔钱都清晰可见</p>
        </div>

        <div v-if="error" class="mb-5 px-4 py-3 bg-expense-500/10 border border-expense-500/20 text-expense-600 dark:text-expense-400 text-sm rounded-xl">
          {{ error }}
        </div>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label class="label-base">用户名 / 邮箱</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名或邮箱"
              class="input-base"
            />
          </div>
          <div>
            <label class="label-base">密码</label>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              class="input-base"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="btn-primary w-full py-3 text-base mt-2"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <p class="mt-7 text-center text-sm text-slate-500 dark:text-slate-400">
          还没有账号？
          <router-link to="/register" class="text-primary-500 hover:text-primary-400 font-semibold">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
