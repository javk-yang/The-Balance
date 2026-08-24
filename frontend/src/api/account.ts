import request from './request'
import type { Account } from '@/types'

export const accountApi = {
  list: () => request.get('/accounts') as unknown as Promise<Account[]>,

  create: (data: { name: string; type: string; balance?: number; remark?: string }) =>
    request.post('/accounts', data) as unknown as Promise<Account>,

  update: (id: number, data: { name: string; type: string; balance?: number; remark?: string }) =>
    request.put(`/accounts/${id}`, data) as unknown as Promise<Account>,

  delete: (id: number) => request.delete(`/accounts/${id}`),
}
