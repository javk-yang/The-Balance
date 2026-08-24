<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { loanApi } from '@/api/loans'
import type { Loan, LoanOverview, Payment } from '@/types'

// Teleport 到 body，避免 root 元素的 animate-fade-up transform 截断 fixed 定位
const modalRoot = document.body

const loading = ref(false)
const loans = ref<Loan[]>([])
const overview = ref<LoanOverview>({ totalBorrowed: 0, remainingPrincipal: 0, paidInterest: 0, loanCount: 0 })
const showLoanModal = ref(false)
const showPaymentModal = ref(false)
const editingId = ref<number | null>(null)
const selectedLoan = ref<Loan | null>(null)
const payments = ref<Payment[]>([])
const paymentLoading = ref(false)

const loanTypes = [
  { value: 'MORTGAGE', label: '房贷' },
  { value: 'CAR', label: '车贷' },
  { value: 'CONSUMER', label: '消费贷' },
  { value: 'OTHER', label: '其他' },
]

const form = reactive({
  name: '', lender: '', type: 'MORTGAGE', principal: '', annualRate: '', termMonths: '', monthlyPayment: '', startDate: '', paymentDay: '1', remark: '', status: 'ACTIVE',
})
const paymentForm = reactive({ amount: '', paymentDate: new Date().toISOString().slice(0, 10), principalAmount: '', interestAmount: '', note: '' })

const money = (value: unknown) => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
const typeLabel = (type: string) => loanTypes.find((item) => item.value === type)?.label || type || '其他'
const activeLoans = computed(() => loans.value.filter((loan) => loan.status !== 'PAID' && loan.status !== 'CLOSED'))

const fetchData = async () => {
  loading.value = true
  try {
    const [list, summary] = await Promise.all([loanApi.list(), loanApi.overview()])
    loans.value = Array.isArray(list) ? list : []
    overview.value = {
      totalBorrowed: Number((summary as any)?.totalBorrowed ?? (summary as any)?.totalPrincipal ?? 0),
      remainingPrincipal: Number((summary as any)?.remainingPrincipal ?? 0),
      paidInterest: Number((summary as any)?.paidInterest ?? 0),
      loanCount: Number((summary as any)?.loanCount ?? loans.value.length),
    }
  } catch (error: any) {
    alert(error?.message || '贷款数据加载失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => Object.assign(form, { name: '', lender: '', type: 'MORTGAGE', principal: '', annualRate: '', termMonths: '', monthlyPayment: '', startDate: new Date().toISOString().slice(0, 10), paymentDay: '1', remark: '', status: 'ACTIVE' })
const openCreate = () => { editingId.value = null; resetForm(); showLoanModal.value = true }
const openEdit = (loan: Loan) => {
  editingId.value = loan.id
  Object.assign(form, { name: loan.name, lender: loan.lender, type: loan.type, principal: String(loan.principal), annualRate: String(loan.annualRate), termMonths: String(loan.termMonths), monthlyPayment: String(loan.monthlyPayment || ''), startDate: loan.startDate, paymentDay: String(loan.paymentDay), remark: loan.remark || '', status: loan.status || 'ACTIVE' })
  showLoanModal.value = true
}
const saveLoan = async () => {
  if (!form.name.trim() || !form.lender.trim() || !form.principal || !form.termMonths || Number(form.termMonths) < 1 || !form.startDate) {
    return alert('请填写贷款名称、出借方、本金、期限和起始日期')
  }
  if (!form.paymentDay || Number(form.paymentDay) < 1 || Number(form.paymentDay) > 31) {
    return alert('还款日必须在1到31之间')
  }
  const data = { name: form.name.trim(), lender: form.lender.trim(), type: form.type, principal: Number(form.principal), annualRate: Number(form.annualRate || 0), termMonths: Number(form.termMonths), monthlyPayment: Number(form.monthlyPayment || 0), startDate: form.startDate, paymentDay: Number(form.paymentDay), remark: form.remark, status: form.status }
  try { editingId.value ? await loanApi.update(editingId.value, data) : await loanApi.create(data); showLoanModal.value = false; await fetchData() } catch (error: any) { alert(error?.message || '保存失败') }
}
const deleteLoan = async (id: number) => {
  if (!confirm('确定删除这笔贷款及其还款记录吗？')) return
  try { await loanApi.delete(id); await fetchData() } catch (error: any) { alert(error?.message || '删除失败') }
}

const openPayments = async (loan: Loan) => {
  selectedLoan.value = loan; showPaymentModal.value = true; paymentLoading.value = true
  Object.assign(paymentForm, { amount: '', paymentDate: new Date().toISOString().slice(0, 10), principalAmount: '', interestAmount: '', note: '' })
  try { payments.value = await loanApi.payments(loan.id) } catch (error: any) { alert(error?.message || '还款记录加载失败') } finally { paymentLoading.value = false }
}
const savePayment = async () => {
  if (!selectedLoan.value || !paymentForm.amount || !paymentForm.paymentDate) return alert('请填写还款金额和日期')
  const amount = Number(paymentForm.amount); const principalAmount = Number(paymentForm.principalAmount || 0); const interestAmount = Number(paymentForm.interestAmount || Math.max(0, amount - principalAmount))
  try { await loanApi.createPayment(selectedLoan.value.id, { amount, paymentDate: paymentForm.paymentDate, principalAmount, interestAmount, note: paymentForm.note }); await openPayments(selectedLoan.value); await fetchData() } catch (error: any) { alert(error?.message || '登记还款失败') }
}
const deletePayment = async (payment: Payment) => {
  if (!selectedLoan.value || !confirm('确定删除这条还款记录吗？')) return
  try { await loanApi.deletePayment(selectedLoan.value.id, payment.id); await openPayments(selectedLoan.value); await fetchData() } catch (error: any) { alert(error?.message || '删除还款失败') }
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-5 animate-fade-up">
    <div class="flex items-center justify-between">
      <div><h2 class="text-2xl font-bold text-slate-800 dark:text-white">贷款管理</h2><p class="mt-1 text-sm text-slate-400">统一管理借款与每期还款进度</p></div>
      <button class="btn-primary" @click="openCreate">+ 新增贷款</button>
    </div>

    <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <div class="card"><p class="text-xs text-slate-400">总借款</p><p class="mt-2 tabular text-xl font-bold text-slate-800 dark:text-white">{{ money(overview.totalBorrowed) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">剩余本金</p><p class="mt-2 tabular text-xl font-bold text-primary-500">{{ money(overview.remainingPrincipal) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">已还利息</p><p class="mt-2 tabular text-xl font-bold text-expense-500">{{ money(overview.paidInterest) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">贷款数量</p><p class="mt-2 tabular text-xl font-bold text-slate-800 dark:text-white">{{ overview.loanCount }} <span class="text-xs font-normal text-slate-400">笔</span></p></div>
    </div>

    <div class="card">
      <div class="mb-5 flex items-center justify-between"><h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">贷款列表</h3><span class="text-xs text-slate-400">{{ activeLoans.length }} 笔进行中</span></div>
      <div v-if="loading" class="py-12 text-center text-slate-400">加载中...</div>
      <div v-else-if="loans.length === 0" class="py-12 text-center text-slate-400">暂无贷款，点击右上角添加</div>
      <div v-else class="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <div v-for="loan in loans" :key="loan.id" class="card-hover rounded-2xl border border-slate-100 p-5 dark:border-white/5">
          <div class="flex items-start justify-between gap-3"><div><h4 class="font-semibold text-slate-800 dark:text-white">{{ loan.name }}</h4><p class="mt-1 text-xs text-slate-400">{{ loan.lender }} · {{ typeLabel(loan.type) }}</p></div><span :class="loan.status === 'PAID' || loan.status === 'CLOSED' ? 'badge bg-slate-100 text-slate-500 dark:bg-white/10' : 'badge badge-income'">{{ loan.status === 'PAID' || loan.status === 'CLOSED' ? '已结清' : '还款中' }}</span></div>
          <div class="mt-5 grid grid-cols-3 gap-3"><div><p class="text-xs text-slate-400">剩余本金</p><p class="mt-1 tabular text-base font-bold text-slate-800 dark:text-white">{{ money(loan.remainingPrincipal) }}</p></div><div><p class="text-xs text-slate-400">年利率</p><p class="mt-1 tabular text-base font-semibold text-slate-700 dark:text-slate-200">{{ Number(loan.annualRate || 0).toFixed(2) }}%</p></div><div><p class="text-xs text-slate-400">月供日</p><p class="mt-1 text-base font-semibold text-slate-700 dark:text-slate-200">每月 {{ loan.paymentDay }} 日</p></div></div>
          <div class="mt-5"><div class="mb-1 flex justify-between text-xs text-slate-400"><span>还款进度</span><span>{{ Number(loan.progress || 0).toFixed(1) }}%</span></div><div class="h-2 overflow-hidden rounded-full bg-slate-100 dark:bg-white/10"><div class="h-full rounded-full bg-brand-gradient transition-all" :style="{ width: `${Math.min(100, Math.max(0, Number(loan.progress || 0)))}%` }"></div></div></div>
          <div class="mt-5 flex items-center justify-between"><button class="text-xs font-medium text-primary-500 hover:text-primary-600" @click="openPayments(loan)">登记还款</button><div class="flex gap-3"><button class="text-xs font-medium text-slate-500 hover:text-primary-500" @click="openEdit(loan)">编辑</button><button class="btn-danger !px-3 !py-1" @click="deleteLoan(loan.id)">删除</button></div></div>
        </div>
      </div>
    </div>

    <Teleport :to="modalRoot">
      <div v-if="showLoanModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-sm" @click.self="showLoanModal = false"><div class="card max-h-[80vh] w-full max-w-xl animate-scale-in overflow-auto p-6"><h3 class="mb-5 text-lg font-semibold text-slate-800 dark:text-white">{{ editingId ? '编辑贷款' : '新增贷款' }}</h3><div class="grid grid-cols-1 gap-4 sm:grid-cols-2"><div><label class="label-base">贷款名称</label><input v-model="form.name" class="input-base" placeholder="如：招商银行房贷" /></div><div><label class="label-base">出借方</label><input v-model="form.lender" class="input-base" placeholder="银行或机构名称" /></div><div><label class="label-base">贷款类型</label><select v-model="form.type" class="input-base"><option v-for="item in loanTypes" :key="item.value" :value="item.value">{{ item.label }}</option></select></div><div><label class="label-base">本金</label><input v-model="form.principal" type="number" min="0" step="0.01" class="input-base" /></div><div><label class="label-base">年利率（%）</label><input v-model="form.annualRate" type="number" min="0" step="0.01" class="input-base" /></div><div><label class="label-base">期限（月）</label><input v-model="form.termMonths" type="number" min="0" class="input-base" /></div><div><label class="label-base">月供金额</label><input v-model="form.monthlyPayment" type="number" min="0" step="0.01" class="input-base" /></div><div><label class="label-base">起始日期</label><input v-model="form.startDate" type="date" class="input-base" /></div><div><label class="label-base">还款日</label><input v-model="form.paymentDay" type="number" min="1" max="31" class="input-base" /></div><div class="sm:col-span-2"><label class="label-base">备注</label><input v-model="form.remark" class="input-base" placeholder="选填" /></div></div><div class="mt-6 flex gap-3"><button class="btn-ghost flex-1" @click="showLoanModal = false">取消</button><button class="btn-primary flex-1" @click="saveLoan">保存</button></div></div></div>
    </Teleport>

    <Teleport :to="modalRoot">
      <div v-if="showPaymentModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 py-6 backdrop-blur-sm" @click.self="showPaymentModal = false"><div class="card max-h-[80vh] w-full max-w-xl animate-scale-in overflow-auto p-6"><div class="flex items-center justify-between"><div><h3 class="text-lg font-semibold text-slate-800 dark:text-white">登记还款</h3><p class="mt-1 text-xs text-slate-400">{{ selectedLoan?.name }}</p></div><button class="text-slate-400 hover:text-slate-700" @click="showPaymentModal = false">×</button></div><div class="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2"><div><label class="label-base">还款金额</label><input v-model="paymentForm.amount" type="number" min="0" step="0.01" class="input-base" /></div><div><label class="label-base">还款日期</label><input v-model="paymentForm.paymentDate" type="date" class="input-base" /></div><div><label class="label-base">归还本金</label><input v-model="paymentForm.principalAmount" type="number" min="0" step="0.01" class="input-base" /></div><div><label class="label-base">支付利息</label><input v-model="paymentForm.interestAmount" type="number" min="0" step="0.01" class="input-base" /></div><div class="sm:col-span-2"><label class="label-base">备注</label><input v-model="paymentForm.note" class="input-base" placeholder="选填" /></div></div><button class="btn-primary mt-5 w-full" @click="savePayment">保存还款</button><div class="mt-6 border-t border-slate-100 pt-5 dark:border-white/10"><h4 class="mb-3 text-sm font-semibold text-slate-700 dark:text-slate-200">还款记录</h4><div v-if="paymentLoading" class="py-4 text-center text-xs text-slate-400">加载中...</div><div v-else-if="payments.length === 0" class="py-4 text-center text-xs text-slate-400">暂无还款记录</div><div v-else class="space-y-2"><div v-for="payment in payments" :key="payment.id" class="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-2.5 dark:bg-white/5"><div><p class="text-sm font-medium text-slate-700 dark:text-slate-200">{{ payment.paymentDate }} · {{ money(payment.amount) }}</p><p class="mt-0.5 text-xs text-slate-400">本金 {{ money(payment.principalAmount) }} / 利息 {{ money(payment.interestAmount) }}</p></div><button class="text-xs text-expense-500 hover:text-expense-600" @click="deletePayment(payment)">删除</button></div></div></div></div></div>
    </Teleport>
  </div>
</template>
