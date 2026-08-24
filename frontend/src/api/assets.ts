import request from './request'
import type { Asset, AssetOverview, AssetPayload } from '@/types'

export const assetApi = {
  list: () => request.get('/assets') as unknown as Promise<Asset[]>,
  overview: () => request.get('/assets/overview') as unknown as Promise<AssetOverview>,
  create: (data: AssetPayload) => request.post('/assets', data) as unknown as Promise<Asset>,
  update: (id: number, data: AssetPayload) => request.put(`/assets/${id}`, data) as unknown as Promise<Asset>,
  delete: (id: number) => request.delete(`/assets/${id}`),
}
