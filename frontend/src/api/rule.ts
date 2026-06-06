import { http } from './http'
import type { Rule } from './types'

export const ruleApi = {
  listByProduct: async (productId: number) => (await http.get<Rule[]>(`/products/${productId}/support-rules`)).data,
  create: async (productId: number, data: Partial<Rule>) => (await http.post<Rule>(`/products/${productId}/support-rules`, data)).data,
  update: async (id: number, data: Partial<Rule>) => (await http.put<Rule>(`/support-rules/${id}`, data)).data,
  setEnabled: async (id: number, enabled: boolean) => (await http.patch<Rule>(`/support-rules/${id}/enabled`, { enabled })).data,
  delete: async (id: number) => (await http.delete<void>(`/support-rules/${id}`)).data
}
