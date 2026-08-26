import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
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
    saveUser(result)
  }

  // 注册
  const register = async (username: string, email: string, password: string, phone?: string) => {
    const result = await authApi.register({ username, email, password, phone })
    saveUser(result)
  }

  const saveUser = (value: User) => {
    user.value = value
    token.value = value.token
    localStorage.setItem('token', value.token)
    localStorage.setItem('user', JSON.stringify(value))
  }

  const updateProfile = async (data: { username: string; email: string; phone?: string; avatar?: string | null }) => {
    const result = await authApi.updateProfile(data)
    saveUser(result)
  }

  // 退出
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, token, isLoggedIn, init, login, register, updateProfile, logout }
})
