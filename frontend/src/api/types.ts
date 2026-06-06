export interface Product {
  id: number
  code: string
  name: string
  specification?: string
  unit: string
  enabled: boolean
  remark?: string
}

export interface SupportItem {
  id: number
  code: string
  name: string
  unit: string
  defaultPrice: number
  enabled: boolean
  remark?: string
}

export interface Rule {
  id: number
  productId: number
  productName: string
  supportItemId: number
  supportItemName: string
  calcType: 'FIXED' | 'RATIO'
  baseQuantity?: number
  supportQuantity: number
  roundingMode: 'CEIL' | 'DECIMAL'
  enabled: boolean
  remark?: string
}

export interface PurchaseListItem {
  id: number
  supportItemId: number
  supportItemCode: string
  supportItemName: string
  unit: string
  quantity: number
  defaultUnitPrice: number
  actualUnitPrice: number
  amount: number
  remark?: string
}

export interface PurchaseListProduct {
  id: number
  productId: number
  productCode: string
  productName: string
  unit: string
  quantity: number
  remark?: string
}

export interface PurchaseList {
  id: number
  listNo: string
  status: 'DRAFT' | 'CONFIRMED' | 'PURCHASED'
  totalAmount: number
  remark?: string
  createdAt: string
  products: PurchaseListProduct[]
  items: PurchaseListItem[]
  warnings: string[]
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface PageQuery {
  keyword?: string
  page?: number
  size?: number
}
