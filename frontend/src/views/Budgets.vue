<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { budgetApi } from '@/api/budget'
const modalRoot = document.body
import { categoryApi } from '@/api/category'
import type { Category, Budget } from '@/types'

const loading = ref(false)
const budgets = ref<Budget[]>([])
const categories = ref<Category[]>([])
const currentMonth = ref(new Date().toISOString().slice(0, 7))

// 弹窗
const showModal = ref(false)
const form = reactive({
  categoryId: undefined as number | undefined,
  amount: '',
  month: new Date().toISOString().slice(0, 7),
})

// 月份导航
const prevMonth = () => {
  const [y, m] = currentMonth.value.split('-').map(Number)
  const date = new Date(y, m - 1, 1)
  date.setMonth(date.getMonth() - 1)
  currentMonth.value = date.toISOString().slice(0, 7)
  fetchData()
}
const nextMonth = () => {
  const [y, m] = currentMonth.value.split('-').map(Number)
  const date = new Date(y, m - 1, 1)
  date.setMonth(date.getMonth() + 1)
  currentMonth.value = date.toISOString().slice(0, 7)
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    budgets.value = await budgetApi.list(currentMonth.value)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  categories.value = await categoryApi.list('EXPENSE')
}

const getCategoryName = (id: number) => categories.value.find(c => c.id === id)?.name || '未知'
const getCategoryIcon = (id: number) => categories.value.find(c => c.id === id)?.icon || '💰'
const getCategoryColor = (id: number) => categories.value.find(c => c.id === id)?.color || '#6366f1'

const openCreate = () => {
  form.categoryId = categories.value[0]?.id
  form.amount = ''
  form.month = currentMonth.value
  showModal.value = true
}

const handleSave = async () => {
  if (!form.categoryId || !form.amount) {
    alert('请填写完整')
    return
  }
  try {
    await budgetApi.set({
      categoryId: form.categoryId,
      amount: parseFloat(form.amount),
      month: form.month,
    })
    showModal.value = false
    fetchData()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确定删除此预算？')) return
  try {
    await budgetApi.delete(id)
    fetchData()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

const getProgress = (spent: number, amount: number) => {
  if (amount === 0) return 0
  return Math.min(Math.round((spent / amount) * 100), 999)
}

const totalBudget = computed(() => budgets.value.reduce((sum, b) => sum + Number(b.amount), 0))
const totalSpent = computed(() => budgets.value.reduce((sum, b) => sum + Number(b.spent), 0))
const remaining = computed(() => totalBudget.value - totalSpent.value)

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<template>
  <div class="space-y-6 animate-fade-up">
    <section class="flex flex-col gap-5 border-b border-stone-200/80 pb-6 dark:border-white/10 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-emerald-700 dark:text-emerald-400">Budget control</p>
        <h1 class="text-2xl font-semibold tracking-tight text-stone-950 dark:text-white sm:text-3xl">预算管理</h1>
        <p class="mt-2 max-w-2xl text-sm leading-6 text-stone-500 dark:text-stone-400">按月设定分类预算，对照实际支出，及时识别接近额度或已经超支的项目。</p>
      </div>
      <button @click="openCreate" class="btn-primary w-full sm:w-auto">设置预算</button>
    </section>

    <section class="card flex items-center justify-between gap-3 p-3 sm:p-4">
      <button @click="prevMonth" class="btn-secondary !px-3 sm:!px-4"><span aria-hidden="true">←</span><span class="hidden sm:inline"> 上月</span></button>
      <div class="text-center">
        <p class="text-xs font-medium uppercase tracking-[0.16em] text-stone-400">预算周期</p>
        <p class="mt-1 tabular text-lg font-semibold text-stone-950 dark:text-white">{{ currentMonth }}</p>
      </div>
      <button @click="nextMonth" class="btn-secondary !px-3 sm:!px-4"><span class="hidden sm:inline">下月 </span><span aria-hidden="true">→</span></button>
    </section>

    <section class="grid gap-3 sm:grid-cols-3">
      <article class="card relative overflow-hidden">
        <div class="absolute inset-x-0 top-0 h-0.5 bg-stone-800 dark:bg-stone-300"></div>
        <p class="text-xs font-semibold uppercase tracking-wider text-stone-500 dark:text-stone-400">总预算</p>
        <p class="mt-3 tabular text-2xl font-semibold tracking-tight text-stone-950 dark:text-white sm:text-3xl">¥{{ totalBudget.toFixed(2) }}</p>
        <p class="mt-2 text-xs text-stone-500 dark:text-stone-400">本月已配置额度</p>
      </article>
      <article class="card relative overflow-hidden">
        <div class="absolute inset-x-0 top-0 h-0.5 bg-red-700 dark:bg-red-500"></div>
        <p class="text-xs font-semibold uppercase tracking-wider text-stone-500 dark:text-stone-400">已支出</p>
        <p class="mt-3 tabular text-2xl font-semibold tracking-tight text-red-700 dark:text-red-400 sm:text-3xl">¥{{ totalSpent.toFixed(2) }}</p>
        <p class="mt-2 text-xs text-stone-500 dark:text-stone-400">预算执行金额</p>
      </article>
      <article class="card relative overflow-hidden">
        <div class="absolute inset-x-0 top-0 h-0.5" :class="remaining >= 0 ? 'bg-emerald-700 dark:bg-emerald-500' : 'bg-red-700 dark:bg-red-500'"></div>
        <p class="text-xs font-semibold uppercase tracking-wider text-stone-500 dark:text-stone-400">剩余额度</p>
        <p class="mt-3 tabular text-2xl font-semibold tracking-tight sm:text-3xl" :class="remaining >= 0 ? 'text-emerald-700 dark:text-emerald-400' : 'text-red-700 dark:text-red-400'">¥{{ remaining.toFixed(2) }}</p>
        <p class="mt-2 text-xs text-stone-500 dark:text-stone-400">{{ remaining >= 0 ? '当前仍在总预算范围内' : '当前总支出已超过预算' }}</p>
      </article>
    </section>

    <section class="card">
      <header class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-base font-semibold text-stone-900 dark:text-stone-100">分类预算执行</h2>
          <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">监控各支出分类的使用比例与剩余额度</p>
        </div>
        <button @click="openCreate" class="btn-secondary w-full sm:w-auto">新增分类预算</button>
      </header>

      <div v-if="loading" class="py-20 text-center text-sm text-stone-500 dark:text-stone-400">正在加载预算数据...</div>
      <div v-else-if="budgets.length === 0" class="rounded-xl border border-dashed border-stone-300 px-6 py-20 text-center dark:border-white/15">
        <p class="text-sm font-medium text-stone-700 dark:text-stone-300">{{ currentMonth }} 暂无预算</p>
        <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">设置分类预算后，可在此追踪执行进度。</p>
      </div>
      <div v-else class="divide-y divide-stone-100 rounded-xl border border-stone-200 dark:divide-white/[0.06] dark:border-white/10">
        <article v-for="b in budgets" :key="b.id" class="p-4 sm:p-5">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div class="flex min-w-0 items-center gap-3">
              <span :style="{ backgroundColor: getCategoryColor(b.categoryId) + '18' }" class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-xl">{{ getCategoryIcon(b.categoryId) }}</span>
              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <h3 class="truncate text-sm font-semibold text-stone-900 dark:text-stone-100">{{ getCategoryName(b.categoryId) }}</h3>
                  <span v-if="b.overBudget" class="inline-flex rounded-full border border-red-200 bg-red-50 px-2 py-0.5 text-[11px] font-semibold text-red-700 dark:border-red-900/60 dark:bg-red-950/30 dark:text-red-300">已超支</span>
                  <span v-else-if="getProgress(b.spent, b.amount) > 80" class="inline-flex rounded-full border border-amber-200 bg-amber-50 px-2 py-0.5 text-[11px] font-semibold text-amber-700 dark:border-amber-900/60 dark:bg-amber-950/30 dark:text-amber-300">接近额度</span>
                </div>
                <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">预算 ¥{{ Number(b.amount).toFixed(2) }}</p>
              </div>
            </div>
            <div class="flex items-center justify-between gap-4 sm:justify-end">
              <div class="text-left sm:text-right">
                <p class="tabular text-sm font-semibold" :class="b.overBudget ? 'text-red-700 dark:text-red-400' : 'text-stone-800 dark:text-stone-200'">已用 ¥{{ Number(b.spent).toFixed(2) }}</p>
                <p class="mt-1 tabular text-xs text-stone-500 dark:text-stone-400">{{ getProgress(b.spent, b.amount) }}%</p>
              </div>
              <button @click="handleDelete(b.id)" class="btn-danger !px-3 !py-1.5">删除</button>
            </div>
          </div>
          <div class="mt-4 h-2 w-full overflow-hidden rounded-full bg-stone-100 dark:bg-white/[0.06]">
            <div :class="b.overBudget ? 'bg-red-700 dark:bg-red-500' : getProgress(b.spent, b.amount) > 80 ? 'bg-amber-600 dark:bg-amber-500' : 'bg-emerald-700 dark:bg-emerald-500'" class="h-full rounded-full transition-all duration-300" :style="{ width: Math.min(getProgress(b.spent, b.amount), 100) + '%' }"></div>
          </div>
          <div class="mt-2 flex items-center justify-between text-xs text-stone-500 dark:text-stone-400">
            <span>¥0</span>
            <span v-if="!b.overBudget">剩余 ¥{{ Math.max(Number(b.amount) - Number(b.spent), 0).toFixed(2) }}</span>
            <span v-else class="font-medium text-red-700 dark:text-red-400">超出 ¥{{ (Number(b.spent) - Number(b.amount)).toFixed(2) }}</span>
          </div>
        </article>
      </div>
    </section>

    <Teleport :to="modalRoot">
      <div v-if="showModal" @click.self="showModal = false" class="fixed inset-0 z-[100] flex items-end justify-center bg-stone-950/55 sm:items-center sm:px-4">
        <div class="w-full max-w-md overflow-hidden rounded-t-2xl border border-stone-200 bg-white shadow-2xl dark:border-white/10 dark:bg-stone-900 sm:rounded-2xl">
          <header class="flex items-start justify-between border-b border-stone-200/80 px-5 py-5 dark:border-white/10 sm:px-6">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-emerald-700 dark:text-emerald-400">Budget</p>
              <h3 class="mt-1 text-xl font-semibold text-stone-950 dark:text-white">设置预算</h3>
            </div>
            <button @click="showModal = false" class="btn-ghost !h-9 !w-9 !p-0" aria-label="关闭弹窗">×</button>
          </header>
          <div class="space-y-5 px-5 py-5 sm:px-6">
            <label class="block">
              <span class="label-base">支出分类</span>
              <select v-model="form.categoryId" class="input-base">
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
              </select>
            </label>
            <label class="block">
              <span class="label-base">预算金额</span>
              <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-3.5 flex items-center text-sm font-medium text-stone-400">¥</span>
                <input v-model="form.amount" type="number" step="0.01" placeholder="0.00" class="input-base tabular !pl-8 text-lg font-semibold" />
              </div>
            </label>
            <label class="block">
              <span class="label-base">预算月份</span>
              <input v-model="form.month" type="month" class="input-base" />
            </label>
          </div>
          <footer class="flex gap-3 border-t border-stone-200/80 px-5 py-4 dark:border-white/10 sm:justify-end sm:px-6">
            <button @click="showModal = false" class="btn-secondary flex-1 sm:flex-none">取消</button>
            <button @click="handleSave" class="btn-primary flex-1 sm:flex-none">保存预算</button>
          </footer>
        </div>
      </div>
    </Teleport>
  </div>
</template>
