import axios from 'axios'
import type { ApiResponse } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器 - 自动添加 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 - 统一处理
request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse
    if (data.code === 0) {
      return data.data
    }
    alert(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request
