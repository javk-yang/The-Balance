import request from './request'
import type { Budget } from '@/types'

export const budgetApi = {
  list: (month: string) =>
    request.get('/budgets', { params: { month } }) as unknown as Promise<Budget[]>,

  set: (data: { categoryId: number; amount: number; month: string }) =>
    request.post('/budgets', data) as unknown as Promise<any>,

  delete: (id: number) => request.delete(`/budgets/${id}`),
}
