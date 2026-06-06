import { http } from './http'
import type { PageQuery, PageResult, Product } from './types'

export const productApi = {
  list: async (params: PageQuery & { enabled?: boolean } = {}) =>
    (await http.get<PageResult<Product>>('/products', { params })).data,
  create: async (data: Partial<Product>) => (await http.post<Product>('/products', data)).data,
  update: async (id: number, data: Partial<Product>) => (await http.put<Product>(`/products/${id}`, data)).data,
  setEnabled: async (id: number, enabled: boolean) => (await http.patch<Product>(`/products/${id}/enabled`, { enabled })).data
}
