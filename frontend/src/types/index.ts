// 全局类型定义

export interface User {
  userId: number
  username: string
  email: string
  phone?: string | null
  avatar?: string | null
  token: string
}

export interface Category {
  id: number
  userId: number
  name: string
  type: 'INCOME' | 'EXPENSE'
  icon: string
  color: string
  parentId: number | null
  sortOrder: number
  status: number
}

export interface Account {
  id: number
  userId: number
  name: string
  type: string
  balance: number
  remark: string
}

export interface Transaction {
  id: number
  userId: number
  accountId: number
  categoryId: number
  amount: number
  type: 'INCOME' | 'EXPENSE'
  date: string
  note: string
  createdAt: string
  updatedAt: string
}

export interface Budget {
  id: number
  categoryId: number
  amount: number
  month: string
  spent: number
  remaining: number
  overBudget: boolean
}

export interface Loan {
  id: number
  userId?: number
  name: string
  lender: string
  type: string
  principal: number
  annualRate: number
  termMonths: number
  startDate: string
  paymentDay: number
  remark: string
  status: string
  remainingPrincipal: number
  paidPrincipal: number
  paidInterest: number
  monthlyPayment?: number
  progress: number
}

export interface Payment {
  id: number
  loanId: number
  amount: number
  paymentDate: string
  principalAmount: number
  interestAmount: number
  note: string
}

export interface LoanOverview {
  totalBorrowed: number
  remainingPrincipal: number
  paidInterest: number
  loanCount: number
}

export interface Project {
  id: number
  userId?: number
  name: string
  customer?: string
  client?: string
  price: number
  depositAmount: number
  depositStatus: string
  balanceStatus: string
  signedDate?: string
  contractDate?: string
  deliveryDate?: string
  dueDate?: string
  status: string
  remark: string
  receivedAmount: number
  receivableAmount: number
  progress: number
  createdAt?: string
  updatedAt?: string
}

export interface ProjectPayload {
  name: string
  client: string
  price: number
  depositAmount: number
  depositStatus: string
  balanceStatus: string
  contractDate: string
  dueDate?: string
  status: string
  remark?: string
}

export interface ProjectOverview {
  totalContractValue: number
  totalReceived: number
  totalPending: number
  currentMonthReceived: number
  projectCount: number
  activeProjectCount?: number
}


export interface Asset {
  id: number
  userId?: number
  name: string
  category: string
  purchasePrice: number
  currentValue: number
  liquidatable: boolean
  status: string
  purchaseDate?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface AssetPayload {
  name: string
  category: string
  purchasePrice: number
  currentValue?: number
  liquidatable: boolean
  status: string
  purchaseDate?: string
  remark?: string
}

export interface AssetOverview {
  totalPurchasePrice: number
  totalCurrentValue: number
  totalDepreciation: number
  totalAppreciation: number
  liquidatableValue: number
  assetCount: number
  activeCount?: number
}

export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
}

export interface PageData<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
