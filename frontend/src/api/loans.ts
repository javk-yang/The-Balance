import request from './request'
import type { Loan, LoanOverview, Payment } from '@/types'

export interface LoanPayload {
  name: string
  lender: string
  type: string
  principal: number
  annualRate: number
  termMonths: number
  startDate: string
  paymentDay: number
  monthlyPayment?: number
  remark?: string
  status?: string
}

export interface PaymentPayload {
  amount: number
  paymentDate: string
  principalAmount: number
  interestAmount: number
  note?: string
}

export const loanApi = {
  list: () => request.get('/loans') as unknown as Promise<Loan[]>,
  overview: () => request.get('/loans/overview') as unknown as Promise<LoanOverview>,
  create: (data: LoanPayload) => request.post('/loans', data) as unknown as Promise<Loan>,
  update: (id: number, data: LoanPayload) => request.put(`/loans/${id}`, data) as unknown as Promise<Loan>,
  delete: (id: number) => request.delete(`/loans/${id}`),
  payments: (loanId: number) => request.get(`/loans/${loanId}/payments`) as unknown as Promise<Payment[]>,
  createPayment: (loanId: number, data: PaymentPayload) =>
    request.post(`/loans/${loanId}/payments`, data) as unknown as Promise<Payment>,
  deletePayment: (loanId: number, paymentId: number) =>
    request.delete(`/loans/${loanId}/payments/${paymentId}`),
}
