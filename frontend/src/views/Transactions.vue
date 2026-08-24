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
  <div class="space-y-5 animate-fade-up">
    <!-- 筛选栏 -->
    <div class="card">
      <div class="grid grid-cols-2 gap-3 md:grid-cols-6">
        <input
          v-model="filters.startDate"
          type="date"
          class="input-base"
          placeholder="开始日期"
        />
        <input
          v-model="filters.endDate"
          type="date"
          class="input-base"
          placeholder="结束日期"
        />
        <select v-model="filters.categoryId" class="input-base">
          <option :value="undefined">全部分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
        </select>
        <select v-model="filters.type" class="input-base">
          <option value="">全部类型</option>
          <option value="INCOME">收入</option>
          <option value="EXPENSE">支出</option>
        </select>
        <input
          v-model="filters.keyword"
          type="text"
          placeholder="搜索备注"
          class="input-base"
        />
        <div class="flex gap-2">
          <button @click="currentPage = 0; fetchData()" class="btn-primary flex-1">
            搜索
          </button>
          <button @click="resetFilters" class="btn-ghost">
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 列表 -->
    <div class="card overflow-hidden p-0">
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4 dark:border-white/5">
        <h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">交易流水</h3>
        <button @click="openCreate" class="btn-primary">+ 新增记账</button>
      </div>

      <div v-if="loading" class="py-16 text-center text-slate-400">加载中...</div>
      <div v-else-if="transactions.length === 0" class="py-16 text-center text-slate-400">
        暂无记录，点击「新增记账」开始
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-slate-500 dark:text-slate-400 bg-slate-50/60 dark:bg-white/5">
              <th class="px-6 py-3 text-left font-medium">日期</th>
              <th class="px-6 py-3 text-left font-medium">分类</th>
              <th class="px-6 py-3 text-left font-medium">类型</th>
              <th class="px-6 py-3 text-left font-medium">账户</th>
              <th class="px-6 py-3 text-right font-medium">金额</th>
              <th class="px-6 py-3 text-left font-medium">备注</th>
              <th class="px-6 py-3 text-center font-medium">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="t in transactions"
              :key="t.id"
              class="border-t border-slate-100 transition-colors hover:bg-slate-50/70 dark:border-white/5 dark:hover:bg-white/5"
            >
              <td class="px-6 py-3.5 tabular text-slate-500 dark:text-slate-400">{{ t.date }}</td>
              <td class="px-6 py-3.5">
                <span class="mr-1">{{ getCategoryIcon(t.categoryId) }}</span>
                <span class="text-slate-700 dark:text-slate-200">{{ getCategoryName(t.categoryId) }}</span>
              </td>
              <td class="px-6 py-3.5">
                <span :class="t.type === 'INCOME' ? 'badge badge-income' : 'badge badge-expense'">
                  {{ t.type === 'INCOME' ? '收入' : '支出' }}
                </span>
              </td>
              <td class="px-6 py-3.5 text-slate-600 dark:text-slate-300">{{ getAccountName(t.accountId) }}</td>
              <td :class="t.type === 'INCOME' ? 'text-income-600 dark:text-income-300' : 'text-expense-600 dark:text-expense-300'" class="px-6 py-3.5 text-right font-semibold tabular">
                {{ t.type === 'INCOME' ? '+' : '-' }}¥{{ Number(t.amount).toFixed(2) }}
              </td>
              <td class="px-6 py-3.5 text-slate-500 dark:text-slate-400 max-w-xs truncate">{{ t.note || '-' }}</td>
              <td class="px-6 py-3.5 text-center whitespace-nowrap">
                <button @click="openEdit(t)" class="text-sm font-medium text-primary-500 hover:text-primary-600 transition-colors mr-3">编辑</button>
                <button @click="handleDelete(t.id)" class="btn-danger !px-3 !py-1">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="flex items-center justify-between border-t border-slate-100 px-6 py-3.5 text-sm dark:border-white/5">
        <span class="text-slate-500 dark:text-slate-400">共 {{ total }} 条</span>
        <div class="flex items-center gap-1.5">
          <button
            @click="currentPage--; fetchData()"
            :disabled="currentPage === 0"
            class="btn-ghost !py-1.5 !px-3 disabled:opacity-40"
          >
            上一页
          </button>
          <span class="px-2 tabular text-slate-500 dark:text-slate-400">{{ currentPage + 1 }} / {{ totalPages() }}</span>
          <button
            @click="currentPage++; fetchData()"
            :disabled="currentPage + 1 >= totalPages()"
            class="btn-ghost !py-1.5 !px-3 disabled:opacity-40"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Teleport :to="modalRoot">
      <div v-if="showModal" @click.self="showModal = false" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-sm">
        <div class="card w-full max-w-md animate-scale-in max-h-[80vh] overflow-auto p-6">
        <h3 class="mb-5 text-lg font-semibold text-slate-800 dark:text-white">
          {{ editingId ? '编辑记账' : '新增记账' }}
        </h3>
        <div class="space-y-4">
          <!-- 类型切换 -->
          <div>
            <label class="label-base">类型</label>
            <div class="flex gap-2">
              <button
                @click="form.type = 'EXPENSE'"
                :class="form.type === 'EXPENSE' ? 'bg-expense-gradient text-white shadow-glow-expense' : 'bg-slate-100 text-slate-600 dark:bg-white/5 dark:text-slate-300'"
                class="flex-1 rounded-xl py-2.5 text-sm font-medium transition-all"
              >
                支出
              </button>
              <button
                @click="form.type = 'INCOME'"
                :class="form.type === 'INCOME' ? 'bg-income-gradient text-white shadow-glow-income' : 'bg-slate-100 text-slate-600 dark:bg-white/5 dark:text-slate-300'"
                class="flex-1 rounded-xl py-2.5 text-sm font-medium transition-all"
              >
                收入
              </button>
            </div>
          </div>

          <div>
            <label class="label-base">金额</label>
            <input
              v-model="form.amount"
              type="number"
              step="0.01"
              placeholder="0.00"
              class="input-base tabular"
            />
          </div>
          <div>
            <label class="label-base">分类</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="c in categories.filter(c => c.type === form.type)"
                :key="c.id"
                @click="form.categoryId = c.id"
                :class="form.categoryId === c.id ? 'bg-brand-gradient text-white shadow-glow' : 'bg-slate-100 text-slate-600 dark:bg-white/5 dark:text-slate-300'"
                class="rounded-lg px-3 py-1.5 text-sm transition-all"
              >
                {{ c.icon }} {{ c.name }}
              </button>
            </div>
          </div>
          <div>
            <label class="label-base">账户</label>
            <select v-model="form.accountId" class="input-base">
              <option v-for="a in accounts" :key="a.id" :value="a.id">{{ a.name }} (¥{{ Number(a.balance).toFixed(2) }})</option>
            </select>
          </div>
          <div>
            <label class="label-base">日期</label>
            <input v-model="form.date" type="date" class="input-base" />
          </div>
          <div>
            <label class="label-base">备注</label>
            <input v-model="form.note" type="text" placeholder="选填" class="input-base" />
          </div>
        </div>
        <div class="mt-6 flex gap-3">
          <button @click="showModal = false" class="btn-ghost flex-1">取消</button>
          <button @click="handleSave" class="btn-primary flex-1">保存</button>
        </div>
      </div>
      </div>
    </Teleport>
  </div>
</template>
