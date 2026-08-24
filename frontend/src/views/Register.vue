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
    await authStore.register(form.username, form.email, form.password)
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen relative flex items-center justify-center overflow-hidden px-4">
    <!-- 背景光斑 -->
    <div class="absolute -top-40 -right-40 w-[36rem] h-[36rem] bg-primary-500/30 rounded-full blur-3xl animate-float"></div>
    <div class="absolute -bottom-40 -left-40 w-[36rem] h-[36rem] bg-fuchsia-500/25 rounded-full blur-3xl animate-float" style="animation-delay: -3s"></div>
    <div class="absolute top-1/4 right-1/3 w-[24rem] h-[24rem] bg-emerald-500/10 rounded-full blur-3xl"></div>

    <!-- 玻璃卡片 -->
    <div class="relative w-full max-w-md animate-fade-up">
      <div class="bg-white/80 dark:bg-ink-850/80 backdrop-blur-2xl rounded-3xl border border-white/60 dark:border-white/10 shadow-soft-lg p-8 sm:p-10">
        <div class="text-center mb-8">
          <div class="w-16 h-16 rounded-2xl bg-brand-gradient flex items-center justify-center text-3xl shadow-glow mx-auto mb-4">
            🐕
          </div>
          <h2 class="text-2xl font-bold text-slate-800 dark:text-white">创建账号</h2>
          <p class="mt-1.5 text-sm text-slate-500 dark:text-slate-400">开启你的财务清晰之旅</p>
        </div>

        <div v-if="error" class="mb-5 px-4 py-3 bg-expense-500/10 border border-expense-500/20 text-expense-600 dark:text-expense-400 text-sm rounded-xl">
          {{ error }}
        </div>

        <form @submit.prevent="handleRegister" class="space-y-4">
          <div>
            <label class="label-base">用户名 *</label>
            <input v-model="form.username" type="text" placeholder="2-50个字符" class="input-base" />
          </div>
          <div>
            <label class="label-base">邮箱 *</label>
            <input v-model="form.email" type="email" placeholder="your@email.com" class="input-base" />
          </div>
          <div>
            <label class="label-base">密码 *</label>
            <input v-model="form.password" type="password" placeholder="至少6位" class="input-base" />
          </div>
          <div>
            <label class="label-base">手机号</label>
            <input v-model="form.phone" type="text" placeholder="选填" class="input-base" />
          </div>
          <button type="submit" :disabled="loading" class="btn-primary w-full py-3 text-base mt-2">
            {{ loading ? '注册中...' : '注 册' }}
          </button>
        </form>

        <p class="mt-7 text-center text-sm text-slate-500 dark:text-slate-400">
          已有账号？
          <router-link to="/login" class="text-primary-500 hover:text-primary-400 font-semibold">返回登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
