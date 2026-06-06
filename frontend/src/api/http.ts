import axios from 'axios'

export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}

export const http = axios.create({
  baseURL: '/api'
})

http.interceptors.response.use((response) => {
  const body = response.data as ApiResponse<unknown>
  if (body && typeof body.success === 'boolean') {
    if (!body.success) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    response.data = body.data
  }
  return response
})
