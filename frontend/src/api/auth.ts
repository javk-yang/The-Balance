import request from './request'
import type { User } from '@/types'

export const authApi = {
  login: (data: { username: string; password: string }) =>
    request.post('/auth/login', data) as unknown as Promise<User>,

  register: (data: {
    username: string
    email: string
    password: string
    phone?: string
  }) => request.post('/auth/register', data) as unknown as Promise<User>,

  profile: () => request.get('/auth/profile') as unknown as Promise<Omit<User, 'token'>>,

  updateProfile: (data: { username: string; email: string; phone?: string; avatar?: string | null }) =>
    request.put('/auth/profile', data) as unknown as Promise<User>,

  updatePassword: (data: { currentPassword: string; newPassword: string }) =>
    request.put('/auth/password', data) as unknown as Promise<void>,
}
