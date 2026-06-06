import { http } from './http'
import type { PageQuery, PageResult, PurchaseList } from './types'

export interface GenerateItemInput {
  productId?: number
  quantity: number
  remark?: string
}

export const purchaseListApi = {
  manual: async (items: GenerateItemInput[], remark?: string) =>
    (await http.post<PurchaseList>('/purchase-lists/manual', { items, remark })).data,
  excel: async (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return (await http.post<PurchaseList>('/purchase-lists/excel', form)).data
  },
  list: async (params: PageQuery & { status?: string; startDate?: string; endDate?: string } = {}) =>
    (await http.get<PageResult<PurchaseList>>('/purchase-lists', { params })).data,
  detail: async (id: number) => (await http.get<PurchaseList>(`/purchase-lists/${id}`)).data,
  updatePrice: async (id: number, itemId: number, actualUnitPrice: number, remark?: string) =>
    (await http.patch<PurchaseList>(`/purchase-lists/${id}/items/${itemId}/price`, { actualUnitPrice, remark })).data,
  confirm: async (id: number) => (await http.post<PurchaseList>(`/purchase-lists/${id}/confirm`)).data,
  markPurchased: async (id: number) => (await http.post<PurchaseList>(`/purchase-lists/${id}/mark-purchased`)).data,
  templateUrl: '/api/import-templates/products',
  exportUrl: (id: number) => `/api/purchase-lists/${id}/export`
}
