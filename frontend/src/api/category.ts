import request from './request'
import type { Category } from '@/types'

export const categoryApi = {
  list: (type?: string) =>
    request.get('/categories', { params: { type } }) as unknown as Promise<Category[]>,

  create: (data: {
    name: string
    type: string
    icon?: string
    color?: string
    parentId?: number
    sortOrder?: number
  }) => request.post('/categories', data) as unknown as Promise<Category>,

  update: (id: number, data: {
    name: string
    type: string
    icon?: string
    color?: string
    parentId?: number
    sortOrder?: number
  }) => request.put(`/categories/${id}`, data) as unknown as Promise<Category>,

  delete: (id: number) => request.delete(`/categories/${id}`),
}
