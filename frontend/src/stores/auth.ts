import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)
  const token = ref<string>(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)

  // 初始化 - 从 localStorage 恢复用户信息
  const init = () => {
    const saved = localStorage.getItem('user')
    if (saved) {
      user.value = JSON.parse(saved)
    }
  }

  // 登录
  const login = async (username: string, password: string) => {
    const result = await authApi.login({ username, password })
    token.value = result.token
    user.value = result
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  // 注册
  const register = async (username: string, email: string, password: string) => {
    const result = await authApi.register({ username, email, password })
    token.value = result.token
    user.value = result
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  // 退出
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, token, isLoggedIn, init, login, register, logout }
})
