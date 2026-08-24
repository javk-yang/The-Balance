import request from './request'
import type { Category, PageData, Transaction } from '@/types'

export const transactionApi = {
  list: (params: {
    startDate?: string
    endDate?: string
    categoryId?: number
    type?: string
    keyword?: string
    page?: number
    size?: number
  }) => request.get('/transactions', { params }) as unknown as Promise<PageData<Transaction>>,

  create: (data: {
    accountId: number
    categoryId: number
    amount: number
    type: string
    date: string
    note?: string
  }) => request.post('/transactions', data) as unknown as Promise<Transaction>,

  update: (id: number, data: {
    accountId: number
    categoryId: number
    amount: number
    type: string
    date: string
    note?: string
  }) => request.put(`/transactions/${id}`, data) as unknown as Promise<Transaction>,

  delete: (id: number) => request.delete(`/transactions/${id}`),
}
