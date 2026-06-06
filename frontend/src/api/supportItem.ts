import { http } from './http'
import type { PageQuery, PageResult, SupportItem } from './types'

export const supportItemApi = {
  list: async (params: PageQuery & { enabled?: boolean } = {}) =>
    (await http.get<PageResult<SupportItem>>('/support-items', { params })).data,
  create: async (data: Partial<SupportItem>) => (await http.post<SupportItem>('/support-items', data)).data,
  update: async (id: number, data: Partial<SupportItem>) => (await http.put<SupportItem>(`/support-items/${id}`, data)).data,
  setEnabled: async (id: number, enabled: boolean) => (await http.patch<SupportItem>(`/support-items/${id}/enabled`, { enabled })).data
}
