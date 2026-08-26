<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const fileInput = ref<HTMLInputElement | null>(null)
const savingProfile = ref(false)
const savingPassword = ref(false)
const profileMessage = ref('')
const passwordMessage = ref('')
const error = ref('')

const profile = reactive({
  username: authStore.user?.username || '',
  email: authStore.user?.email || '',
  phone: authStore.user?.phone || '',
  avatar: authStore.user?.avatar || '',
})

const password = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const initial = computed(() => profile.username.trim().charAt(0).toUpperCase() || '我')

const selectAvatar = () => fileInput.value?.click()

const onAvatarChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    error.value = '请选择图片文件'
    return
  }
  if (file.size > 1024 * 1024) {
    error.value = '头像图片不能超过 1MB'
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    profile.avatar = String(reader.result)
    error.value = ''
  }
  reader.readAsDataURL(file)
}

const removeAvatar = () => {
  profile.avatar = ''
  if (fileInput.value) fileInput.value.value = ''
}

const saveProfile = async () => {
  if (!profile.username.trim() || !profile.email.trim()) {
    error.value = '用户名和邮箱不能为空'
    return
  }
  savingProfile.value = true
  profileMessage.value = ''
  error.value = ''
  try {
    await authStore.updateProfile({
      username: profile.username.trim(),
      email: profile.email.trim(),
      phone: profile.phone.trim(),
      avatar: profile.avatar || null,
    })
    profileMessage.value = '个人资料已保存'
  } catch (e: any) {
    error.value = e.message || '保存失败'
  } finally {
    savingProfile.value = false
  }
}

const savePassword = async () => {
  if (!password.currentPassword || !password.newPassword || !password.confirmPassword) {
    error.value = '请填写全部密码项'
    return
  }
  if (password.newPassword.length < 6) {
    error.value = '新密码至少 6 位'
    return
  }
  if (password.newPassword !== password.confirmPassword) {
    error.value = '两次输入的新密码不一致'
    return
  }
  savingPassword.value = true
  passwordMessage.value = ''
  error.value = ''
  try {
    await authApi.updatePassword({
      currentPassword: password.currentPassword,
      newPassword: password.newPassword,
    })
    password.currentPassword = ''
    password.newPassword = ''
    password.confirmPassword = ''
    passwordMessage.value = '密码已修改，下次登录请使用新密码'
  } catch (e: any) {
    error.value = e.message || '修改密码失败'
  } finally {
    savingPassword.value = false
  }
}
</script>

<template>
  <main class="mx-auto max-w-6xl animate-fade-up space-y-6">
    <header class="border-b border-[#dfdfda] pb-6 dark:border-white/[0.08]">
      <p class="text-sm font-medium text-primary-700 dark:text-primary-300">账户设置</p>
      <h1 class="mt-1.5 text-2xl font-semibold tracking-tight text-ink-900 sm:text-3xl dark:text-white">个人资料与安全</h1>
      <p class="mt-2 max-w-2xl text-sm leading-6 text-slate-500 dark:text-slate-400">维护你的基本账户信息、头像和登录密码。</p>
    </header>

    <div
      v-if="error"
      role="alert"
      aria-live="polite"
      class="border-l-2 border-expense-500 bg-expense-50 px-4 py-3 text-sm text-expense-700 dark:bg-expense-500/10 dark:text-expense-300"
    >
      {{ error }}
    </div>

    <section class="glass-card overflow-hidden p-0">
      <div class="grid lg:grid-cols-[280px_1fr]">
        <aside class="border-b border-[#e4e5e1] bg-[#fafaf8] p-6 sm:p-8 lg:border-b-0 lg:border-r dark:border-white/[0.06] dark:bg-white/[0.02]">
          <h2 class="text-lg font-semibold text-ink-900 dark:text-white">基本资料</h2>
          <p class="mt-1 text-sm leading-6 text-slate-500 dark:text-slate-400">这些信息用于识别你的账户。</p>

          <div class="mt-7">
            <div class="relative inline-flex">
              <img
                v-if="profile.avatar"
                :src="profile.avatar"
                alt="当前头像"
                class="h-24 w-24 rounded-2xl border border-[#dfdfda] object-cover dark:border-white/10"
              />
              <div
                v-else
                class="flex h-24 w-24 items-center justify-center rounded-2xl border border-primary-800 bg-primary-950 text-3xl font-semibold text-white"
                aria-label="默认头像"
              >
                {{ initial }}
              </div>
              <button
                type="button"
                title="更换头像"
                aria-label="更换头像"
                class="absolute -bottom-2 -right-2 flex h-9 w-9 items-center justify-center rounded-full border border-[#dfdfda] bg-white text-ink-700 shadow-soft transition-colors hover:border-primary-400 hover:text-primary-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 focus-visible:ring-offset-2 dark:border-white/10 dark:bg-ink-800 dark:text-slate-200"
                @click="selectAvatar"
              >
                <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M4 16.5V20h3.5L18.1 9.4l-3.5-3.5L4 16.5Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
                  <path d="m13.6 6.9 3.5 3.5M15.7 4.8l1.1-1.1a1.5 1.5 0 0 1 2.1 0l1.4 1.4a1.5 1.5 0 0 1 0 2.1l-1.1 1.1" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
                </svg>
              </button>
            </div>

            <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="onAvatarChange" />
            <div class="mt-5 flex flex-wrap gap-2">
              <button
                type="button"
                class="btn-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 focus-visible:ring-offset-2"
                @click="selectAvatar"
              >
                选择头像
              </button>
              <button
                v-if="profile.avatar"
                type="button"
                class="btn-ghost text-expense-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-expense-500 focus-visible:ring-offset-2 dark:text-expense-300"
                @click="removeAvatar"
              >
                移除
              </button>
            </div>
            <p class="mt-3 text-xs leading-5 text-slate-400">支持 JPG、PNG、WebP，最大 1MB</p>
          </div>
        </aside>

        <div class="p-6 sm:p-8 lg:p-10">
          <div
            v-if="profileMessage"
            role="status"
            aria-live="polite"
            class="mb-6 border-l-2 border-income-500 bg-income-50 px-4 py-3 text-sm text-income-700 dark:bg-income-500/10 dark:text-income-300"
          >
            {{ profileMessage }}
          </div>

          <form class="grid gap-5 sm:grid-cols-2" @submit.prevent="saveProfile">
            <div>
              <label for="profile-username" class="label-base">用户名 *</label>
              <input id="profile-username" v-model="profile.username" class="input-base h-12" maxlength="50" autocomplete="username" />
            </div>
            <div>
              <label for="profile-email" class="label-base">邮箱 *</label>
              <input id="profile-email" v-model="profile.email" type="email" class="input-base h-12" maxlength="100" autocomplete="email" />
            </div>
            <div>
              <label for="profile-phone" class="label-base">手机号</label>
              <input id="profile-phone" v-model="profile.phone" class="input-base h-12" maxlength="20" placeholder="选填" autocomplete="tel" />
            </div>
            <div class="flex items-end sm:justify-end">
              <button
                type="submit"
                :disabled="savingProfile"
                class="btn-primary h-12 w-full px-7 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 focus-visible:ring-offset-2 disabled:pointer-events-none sm:w-auto"
              >
                {{ savingProfile ? '保存中...' : '保存个人资料' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>

    <section class="glass-card p-6 sm:p-8 lg:p-10">
      <div class="grid gap-7 lg:grid-cols-[240px_1fr] lg:gap-10">
        <div>
          <div class="flex h-10 w-10 items-center justify-center rounded-xl border border-primary-200 bg-primary-50 text-primary-700 dark:border-primary-500/20 dark:bg-primary-500/10 dark:text-primary-300">
            <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <rect x="5" y="10" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.6" />
              <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
            </svg>
          </div>
          <h2 class="mt-4 text-lg font-semibold text-ink-900 dark:text-white">登录密码</h2>
          <p class="mt-1 text-sm leading-6 text-slate-500 dark:text-slate-400">修改前需验证当前密码，新密码至少 6 位。</p>
        </div>

        <div>
          <div
            v-if="passwordMessage"
            role="status"
            aria-live="polite"
            class="mb-6 border-l-2 border-income-500 bg-income-50 px-4 py-3 text-sm text-income-700 dark:bg-income-500/10 dark:text-income-300"
          >
            {{ passwordMessage }}
          </div>

          <form class="grid gap-5 sm:grid-cols-2" @submit.prevent="savePassword">
            <div class="sm:col-span-2">
              <label for="current-password" class="label-base">当前密码 *</label>
              <input id="current-password" v-model="password.currentPassword" type="password" class="input-base h-12" autocomplete="current-password" />
            </div>
            <div>
              <label for="new-password" class="label-base">新密码 *</label>
              <input id="new-password" v-model="password.newPassword" type="password" class="input-base h-12" autocomplete="new-password" placeholder="至少 6 位" />
            </div>
            <div>
              <label for="confirm-password" class="label-base">确认新密码 *</label>
              <input id="confirm-password" v-model="password.confirmPassword" type="password" class="input-base h-12" autocomplete="new-password" />
            </div>
            <div class="sm:col-span-2 sm:text-right">
              <button
                type="submit"
                :disabled="savingPassword"
                class="btn-primary h-12 w-full px-7 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 focus-visible:ring-offset-2 disabled:pointer-events-none sm:w-auto"
              >
                {{ savingPassword ? '修改中...' : '修改密码' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>
  </main>
</template>
