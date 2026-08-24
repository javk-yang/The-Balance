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
  <div class="space-y-5 animate-fade-up">
    <!-- 月份导航 -->
    <div class="card flex items-center justify-between">
      <button @click="prevMonth" class="btn-ghost !py-2 !px-4">← 上月</button>
      <span class="tabular text-lg font-semibold text-slate-800 dark:text-white">{{ currentMonth }}</span>
      <button @click="nextMonth" class="btn-ghost !py-2 !px-4">下月 →</button>
    </div>

    <!-- 总览卡片 -->
    <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
      <div class="card card-hover">
        <p class="mb-1 text-sm text-slate-500 dark:text-slate-400">总预算</p>
        <p class="tabular text-2xl font-bold text-primary-500">¥{{ totalBudget.toFixed(2) }}</p>
      </div>
      <div class="card card-hover">
        <p class="mb-1 text-sm text-slate-500 dark:text-slate-400">已支出</p>
        <p class="tabular text-2xl font-bold text-expense-500">¥{{ totalSpent.toFixed(2) }}</p>
      </div>
      <div class="card card-hover">
        <p class="mb-1 text-sm text-slate-500 dark:text-slate-400">剩余</p>
        <p class="tabular text-2xl font-bold" :class="remaining >= 0 ? 'text-income-600 dark:text-income-300' : 'text-expense-600 dark:text-expense-300'">
          ¥{{ remaining.toFixed(2) }}
        </p>
      </div>
    </div>

    <!-- 预算列表 -->
    <div class="card">
      <div class="mb-5 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">分类预算</h3>
        <button @click="openCreate" class="btn-primary">+ 设置预算</button>
      </div>

      <div v-if="loading" class="py-10 text-center text-slate-400">加载中...</div>
      <div v-else-if="budgets.length === 0" class="py-10 text-center text-slate-400">
        本月暂无预算，点击「设置预算」
      </div>
      <div v-else class="space-y-4">
        <div
          v-for="b in budgets"
          :key="b.id"
          class="rounded-2xl border border-slate-100 p-4 transition-all hover:shadow-soft dark:border-white/5"
        >
          <div class="mb-2.5 flex items-center justify-between">
            <div class="flex items-center gap-2.5">
              <span
                :style="{ backgroundColor: getCategoryColor(b.categoryId) + '20' }"
                class="flex h-8 w-8 items-center justify-center rounded-full text-lg"
              >
                {{ getCategoryIcon(b.categoryId) }}
              </span>
              <span class="text-sm font-medium text-slate-700 dark:text-slate-200">{{ getCategoryName(b.categoryId) }}</span>
              <span v-if="b.overBudget" class="badge bg-expense-500/12 text-expense-600 dark:text-expense-300">超支！</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="tabular text-sm text-slate-500 dark:text-slate-400">
                ¥{{ Number(b.spent).toFixed(0) }} / ¥{{ Number(b.amount).toFixed(0) }}
              </span>
              <button @click="handleDelete(b.id)" class="btn-danger !px-3 !py-1">删除</button>
            </div>
          </div>
          <div class="h-3 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-white/5">
            <div
              :class="b.overBudget ? 'bg-expense-gradient' : getProgress(b.spent, b.amount) > 80 ? 'bg-amber-500' : 'bg-income-gradient'"
              class="h-full rounded-full shadow-glow transition-all"
              :style="{ width: Math.min(getProgress(b.spent, b.amount), 100) + '%' }"
            ></div>
          </div>
          <p class="mt-1.5 text-right tabular text-xs text-slate-400">
            {{ getProgress(b.spent, b.amount) }}%
          </p>
        </div>
      </div>
    </div>

    <!-- 设置预算弹窗 -->
    <Teleport :to="modalRoot">
      <div v-if="showModal" @click.self="showModal = false" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-sm">
        <div class="card w-full max-w-md animate-scale-in max-h-[80vh] overflow-auto p-6">
        <h3 class="mb-5 text-lg font-semibold text-slate-800 dark:text-white">设置预算</h3>
        <div class="space-y-4">
          <div>
            <label class="label-base">分类</label>
            <select v-model="form.categoryId" class="input-base">
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
            </select>
          </div>
          <div>
            <label class="label-base">预算金额</label>
            <input
              v-model="form.amount"
              type="number"
              step="0.01"
              placeholder="0.00"
              class="input-base tabular"
            />
          </div>
          <div>
            <label class="label-base">月份</label>
            <input v-model="form.month" type="month" class="input-base" />
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
