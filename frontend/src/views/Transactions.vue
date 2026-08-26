<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
const modalRoot = document.body
import { transactionApi } from '@/api/transaction'
import { categoryApi } from '@/api/category'
import { accountApi } from '@/api/account'
import type { Category, Account, Transaction } from '@/types'

const loading = ref(false)
const transactions = ref<Transaction[]>([])
const total = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
const categories = ref<Category[]>([])
const accounts = ref<Account[]>([])

// 筛选条件
const filters = reactive({
  startDate: '',
  endDate: '',
  categoryId: undefined as number | undefined,
  type: '',
  keyword: '',
})

// 弹窗状态
const showModal = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  accountId: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  amount: '',
  type: 'EXPENSE',
  date: new Date().toISOString().slice(0, 10),
  note: '',
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await transactionApi.list({
      startDate: filters.startDate || undefined,
      endDate: filters.endDate || undefined,
      categoryId: filters.categoryId || undefined,
      type: filters.type || undefined,
      keyword: filters.keyword || undefined,
      page: currentPage.value,
      size: pageSize.value,
    })
    transactions.value = res.content
    total.value = res.totalElements
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  categories.value = await categoryApi.list()
  accounts.value = await accountApi.list()
}

const resetFilters = () => {
  filters.startDate = ''
  filters.endDate = ''
  filters.categoryId = undefined
  filters.type = ''
  filters.keyword = ''
  currentPage.value = 0
  fetchData()
}

const openCreate = () => {
  editingId.value = null
  form.accountId = accounts.value[0]?.id
  form.categoryId = categories.value.find(c => c.type === 'EXPENSE')?.id
  form.amount = ''
  form.type = 'EXPENSE'
  form.date = new Date().toISOString().slice(0, 10)
  form.note = ''
  showModal.value = true
}

const openEdit = (t: Transaction) => {
  editingId.value = t.id
  form.accountId = t.accountId
  form.categoryId = t.categoryId
  form.amount = String(t.amount)
  form.type = t.type
  form.date = t.date
  form.note = t.note
  showModal.value = true
}

const handleSave = async () => {
  if (!form.accountId || !form.categoryId || !form.amount || !form.date) {
    alert('请填写完整信息')
    return
  }
  const data = {
    accountId: form.accountId,
    categoryId: form.categoryId,
    amount: parseFloat(form.amount),
    type: form.type,
    date: form.date,
    note: form.note,
  }
  try {
    if (editingId.value) {
      await transactionApi.update(editingId.value, data)
    } else {
      await transactionApi.create(data)
    }
    showModal.value = false
    fetchData()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确定删除这条记录？')) return
  try {
    await transactionApi.delete(id)
    fetchData()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

// 类型变化时自动切换分类
watch(() => form.type, (newType) => {
  form.categoryId = categories.value.find(c => c.type === newType)?.id
})

// 分类名称映射
const getCategoryName = (id: number) => categories.value.find(c => c.id === id)?.name || '未知'
const getCategoryIcon = (id: number) => categories.value.find(c => c.id === id)?.icon || '💰'
const getAccountName = (id: number) => accounts.value.find(a => a.id === id)?.name || '未知'

const totalPages = () => Math.ceil(total.value / pageSize.value)

onMounted(() => {
  fetchOptions().then(fetchData)
})
</script>

<template>
  <div class="space-y-6 animate-fade-up">
    <section class="flex flex-col gap-5 border-b border-stone-200/80 pb-6 dark:border-white/10 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-emerald-700 dark:text-emerald-400">Cash flow ledger</p>
        <h1 class="text-2xl font-semibold tracking-tight text-stone-950 dark:text-white sm:text-3xl">交易流水</h1>
        <p class="mt-2 max-w-2xl text-sm leading-6 text-stone-500 dark:text-stone-400">集中查看每一笔收入与支出，通过日期、分类和备注快速定位记录。</p>
      </div>
      <button @click="openCreate" class="btn-primary w-full sm:w-auto">新增记账</button>
    </section>

    <section class="card">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <h2 class="text-sm font-semibold text-stone-900 dark:text-stone-100">筛选条件</h2>
          <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">组合条件可缩小流水查询范围</p>
        </div>
        <button @click="resetFilters" class="btn-ghost hidden sm:inline-flex">重置筛选</button>
      </div>
      <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-12">
        <label class="xl:col-span-2">
          <span class="label-base">开始日期</span>
          <input v-model="filters.startDate" type="date" class="input-base" />
        </label>
        <label class="xl:col-span-2">
          <span class="label-base">结束日期</span>
          <input v-model="filters.endDate" type="date" class="input-base" />
        </label>
        <label class="xl:col-span-2">
          <span class="label-base">分类</span>
          <select v-model="filters.categoryId" class="input-base">
            <option :value="undefined">全部分类</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
          </select>
        </label>
        <label class="xl:col-span-2">
          <span class="label-base">收支类型</span>
          <select v-model="filters.type" class="input-base">
            <option value="">全部类型</option>
            <option value="INCOME">收入</option>
            <option value="EXPENSE">支出</option>
          </select>
        </label>
        <label class="sm:col-span-2 xl:col-span-3">
          <span class="label-base">备注关键词</span>
          <input v-model="filters.keyword" type="text" placeholder="输入关键词搜索" class="input-base" @keyup.enter="currentPage = 0; fetchData()" />
        </label>
        <div class="flex items-end gap-2 sm:col-span-2 xl:col-span-1">
          <button @click="currentPage = 0; fetchData()" class="btn-primary flex-1 xl:w-full">查询</button>
          <button @click="resetFilters" class="btn-secondary sm:hidden">重置</button>
        </div>
      </div>
    </section>

    <section class="card overflow-hidden p-0">
      <header class="flex flex-col gap-3 border-b border-stone-200/80 px-4 py-4 dark:border-white/10 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <div>
          <h2 class="text-base font-semibold text-stone-900 dark:text-stone-100">流水明细</h2>
          <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">当前共 {{ total }} 条记录</p>
        </div>
        <button @click="openCreate" class="btn-secondary w-full sm:w-auto">新增一笔</button>
      </header>

      <div v-if="loading" class="px-6 py-20 text-center text-sm text-stone-500 dark:text-stone-400">正在加载交易记录...</div>
      <div v-else-if="transactions.length === 0" class="px-6 py-20 text-center">
        <p class="text-sm font-medium text-stone-700 dark:text-stone-300">暂无符合条件的流水</p>
        <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">调整筛选条件，或新增第一笔记账记录。</p>
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[900px] text-sm">
          <thead>
            <tr class="border-b border-stone-200/80 bg-stone-50/80 text-xs uppercase tracking-wider text-stone-500 dark:border-white/10 dark:bg-white/[0.03] dark:text-stone-400">
              <th class="px-6 py-3.5 text-left font-medium">日期</th>
              <th class="px-6 py-3.5 text-left font-medium">分类</th>
              <th class="px-6 py-3.5 text-left font-medium">类型</th>
              <th class="px-6 py-3.5 text-left font-medium">账户</th>
              <th class="px-6 py-3.5 text-right font-medium">金额</th>
              <th class="px-6 py-3.5 text-left font-medium">备注</th>
              <th class="px-6 py-3.5 text-right font-medium">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-stone-100 dark:divide-white/[0.06]">
            <tr v-for="t in transactions" :key="t.id" class="transition-colors hover:bg-stone-50/70 dark:hover:bg-white/[0.03]">
              <td class="whitespace-nowrap px-6 py-4 font-medium tabular text-stone-600 dark:text-stone-300">{{ t.date }}</td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-2.5">
                  <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-stone-100 text-base dark:bg-white/[0.06]">{{ getCategoryIcon(t.categoryId) }}</span>
                  <span class="font-medium text-stone-800 dark:text-stone-200">{{ getCategoryName(t.categoryId) }}</span>
                </div>
              </td>
              <td class="px-6 py-4">
                <span :class="t.type === 'INCOME' ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-800/60 dark:bg-emerald-950/30 dark:text-emerald-300' : 'border-red-200 bg-red-50 text-red-700 dark:border-red-900/60 dark:bg-red-950/30 dark:text-red-300'" class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold">
                  {{ t.type === 'INCOME' ? '收入' : '支出' }}
                </span>
              </td>
              <td class="px-6 py-4 text-stone-600 dark:text-stone-300">{{ getAccountName(t.accountId) }}</td>
              <td :class="t.type === 'INCOME' ? 'text-emerald-700 dark:text-emerald-400' : 'text-red-700 dark:text-red-400'" class="whitespace-nowrap px-6 py-4 text-right font-semibold tabular">
                {{ t.type === 'INCOME' ? '+' : '-' }}¥{{ Number(t.amount).toFixed(2) }}
              </td>
              <td class="max-w-xs truncate px-6 py-4 text-stone-500 dark:text-stone-400">{{ t.note || '-' }}</td>
              <td class="whitespace-nowrap px-6 py-4 text-right">
                <button @click="openEdit(t)" class="btn-ghost !px-3 !py-1.5">编辑</button>
                <button @click="handleDelete(t.id)" class="btn-danger ml-1 !px-3 !py-1.5">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer v-if="total > 0" class="flex flex-col gap-3 border-t border-stone-200/80 px-4 py-4 text-sm dark:border-white/10 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <span class="text-center text-stone-500 dark:text-stone-400 sm:text-left">第 {{ currentPage + 1 }} 页，共 {{ totalPages() }} 页</span>
        <div class="grid grid-cols-[1fr_auto_1fr] items-center gap-2">
          <button @click="currentPage--; fetchData()" :disabled="currentPage === 0" class="btn-secondary !py-2 disabled:cursor-not-allowed disabled:opacity-40">上一页</button>
          <span class="min-w-[4rem] text-center tabular text-stone-600 dark:text-stone-300">{{ currentPage + 1 }} / {{ totalPages() }}</span>
          <button @click="currentPage++; fetchData()" :disabled="currentPage + 1 >= totalPages()" class="btn-secondary !py-2 disabled:cursor-not-allowed disabled:opacity-40">下一页</button>
        </div>
      </footer>
    </section>

    <Teleport :to="modalRoot">
      <div v-if="showModal" @click.self="showModal = false" class="fixed inset-0 z-[100] flex items-end justify-center bg-stone-950/55 sm:items-center sm:px-4">
        <div class="w-full max-w-lg overflow-auto rounded-t-2xl border border-stone-200 bg-white shadow-2xl dark:border-white/10 dark:bg-stone-900 sm:max-h-[90vh] sm:rounded-2xl">
          <header class="flex items-start justify-between border-b border-stone-200/80 px-5 py-5 dark:border-white/10 sm:px-6">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-emerald-700 dark:text-emerald-400">Transaction</p>
              <h3 class="mt-1 text-xl font-semibold text-stone-950 dark:text-white">{{ editingId ? '编辑记账' : '新增记账' }}</h3>
            </div>
            <button @click="showModal = false" class="btn-ghost !h-9 !w-9 !p-0" aria-label="关闭弹窗">×</button>
          </header>
          <div class="max-h-[calc(100vh-11rem)] space-y-5 overflow-y-auto px-5 py-5 sm:px-6">
            <div>
              <label class="label-base">收支类型</label>
              <div class="grid grid-cols-2 rounded-xl bg-stone-100 p-1 dark:bg-white/[0.05]">
                <button @click="form.type = 'EXPENSE'" :class="form.type === 'EXPENSE' ? 'bg-white text-red-700 shadow-sm ring-1 ring-stone-200 dark:bg-stone-800 dark:text-red-300 dark:ring-white/10' : 'text-stone-500 dark:text-stone-400'" class="rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors">支出</button>
                <button @click="form.type = 'INCOME'" :class="form.type === 'INCOME' ? 'bg-white text-emerald-700 shadow-sm ring-1 ring-stone-200 dark:bg-stone-800 dark:text-emerald-300 dark:ring-white/10' : 'text-stone-500 dark:text-stone-400'" class="rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors">收入</button>
              </div>
            </div>
            <label class="block">
              <span class="label-base">金额</span>
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3.5 flex items-center text-sm font-medium text-stone-400">¥</span>
                <input v-model="form.amount" type="number" step="0.01" placeholder="0.00" class="input-base tabular !pl-8 text-lg font-semibold" />
              </div>
            </label>
            <div>
              <label class="label-base">分类</label>
              <div class="grid max-h-40 grid-cols-2 gap-2 overflow-y-auto pr-1 sm:grid-cols-3">
                <button v-for="c in categories.filter(c => c.type === form.type)" :key="c.id" @click="form.categoryId = c.id" :class="form.categoryId === c.id ? 'border-emerald-700 bg-emerald-50 text-emerald-900 dark:border-emerald-600 dark:bg-emerald-950/30 dark:text-emerald-200' : 'border-stone-200 bg-white text-stone-600 hover:border-stone-300 dark:border-white/10 dark:bg-white/[0.03] dark:text-stone-300'" class="flex min-h-[44px] items-center gap-2 rounded-lg border px-3 py-2 text-left text-sm font-medium transition-colors">
                  <span>{{ c.icon }}</span><span class="truncate">{{ c.name }}</span>
                </button>
              </div>
            </div>
            <div class="grid gap-4 sm:grid-cols-2">
              <label>
                <span class="label-base">账户</span>
                <select v-model="form.accountId" class="input-base">
                  <option v-for="a in accounts" :key="a.id" :value="a.id">{{ a.name }} (¥{{ Number(a.balance).toFixed(2) }})</option>
                </select>
              </label>
              <label>
                <span class="label-base">日期</span>
                <input v-model="form.date" type="date" class="input-base" />
              </label>
            </div>
            <label class="block">
              <span class="label-base">备注</span>
              <input v-model="form.note" type="text" placeholder="选填，例如：午餐、月度工资" class="input-base" />
            </label>
          </div>
          <footer class="flex gap-3 border-t border-stone-200/80 px-5 py-4 dark:border-white/10 sm:justify-end sm:px-6">
            <button @click="showModal = false" class="btn-secondary flex-1 sm:flex-none">取消</button>
            <button @click="handleSave" class="btn-primary flex-1 sm:flex-none">保存记录</button>
          </footer>
        </div>
      </div>
    </Teleport>
  </div>
</template>
