<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
const modalRoot = document.body
import { categoryApi } from '@/api/category'
import type { Category } from '@/types'

const loading = ref(false)
const categories = ref<Category[]>([])
const activeTab = ref<'EXPENSE' | 'INCOME'>('EXPENSE')

// 弹窗
const showModal = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  name: '',
  type: 'EXPENSE',
  icon: '💰',
  color: '#6366f1',
  sortOrder: 0,
})

// 可选图标
const icons = [
  '🍜', '🛍️', '🚗', '🏠', '🎮', '💊', '📚', '✈️', '☕', '🍔',
  '👗', '📱', '🎬', '💪', '🎁', '🐾', '🌿', '⚡', '💡', '💰',
  '💼', '📈', '🏦', '💵', '🎯', '⭐', '🔥', '🌈', '🎵', '👕',
]

// 可选颜色
const colors = [
  '#ef4444', '#f59e0b', '#3b82f6', '#10b981', '#8b5cf6',
  '#ec4899', '#6366f1', '#6b7280', '#14b8a6', '#f97316',
  '#a855f7', '#06b6d4', '#84cc16', '#eab304', '#e11d48',
]

const filteredCategories = () => categories.value.filter(c => c.type === activeTab.value)

const fetchData = async () => {
  loading.value = true
  try {
    categories.value = await categoryApi.list()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  form.name = ''
  form.type = activeTab.value
  form.icon = '💰'
  form.color = '#6366f1'
  form.sortOrder = 0
  showModal.value = true
}

const openEdit = (c: Category) => {
  editingId.value = c.id
  form.name = c.name
  form.type = c.type
  form.icon = c.icon
  form.color = c.color
  form.sortOrder = c.sortOrder
  showModal.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) {
    alert('请输入分类名称')
    return
  }
  try {
    if (editingId.value) {
      await categoryApi.update(editingId.value, { ...form })
    } else {
      await categoryApi.create({ ...form })
    }
    showModal.value = false
    fetchData()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('删除该分类后，已有记录将标记为「未知分类」，确定删除？')) return
  try {
    await categoryApi.delete(id)
    fetchData()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-5 animate-fade-up">
    <!-- 顶部说明 -->
    <div class="rounded-2xl bg-brand-gradient p-6 text-white shadow-glow">
      <h3 class="text-lg font-bold">🏷️ 消费板块管理</h3>
      <p class="mt-1 text-sm text-white/80">自定义你的消费分类，让记账更贴合你的生活</p>
    </div>

    <!-- Tab 切换 -->
    <div class="inline-flex rounded-xl bg-slate-100 p-1 dark:bg-white/5">
      <button
        @click="activeTab = 'EXPENSE'"
        :class="activeTab === 'EXPENSE' ? 'bg-expense-gradient text-white shadow-glow-expense' : 'text-slate-600 dark:text-slate-300'"
        class="rounded-lg px-5 py-2 text-sm font-medium transition-all"
      >
        支出板块
      </button>
      <button
        @click="activeTab = 'INCOME'"
        :class="activeTab === 'INCOME' ? 'bg-income-gradient text-white shadow-glow-income' : 'text-slate-600 dark:text-slate-300'"
        class="rounded-lg px-5 py-2 text-sm font-medium transition-all"
      >
        收入板块
      </button>
    </div>

    <!-- 分类网格 -->
    <div class="card">
      <div class="mb-5 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">
          {{ activeTab === 'EXPENSE' ? '支出' : '收入' }}板块（{{ filteredCategories().length }} 个）
        </h3>
        <button @click="openCreate" class="btn-primary">+ 新增板块</button>
      </div>

      <div v-if="loading" class="py-16 text-center text-slate-400">加载中...</div>
      <div v-else-if="filteredCategories().length === 0" class="py-16 text-center text-slate-400">
        还没有分类，点击「新增板块」添加
      </div>
      <div v-else class="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-4">
        <div
          v-for="c in filteredCategories()"
          :key="c.id"
          class="group relative card-hover rounded-2xl border border-slate-100 bg-white/70 p-4 dark:border-white/5 dark:bg-white/5"
        >
          <div class="mb-3 flex items-center gap-3">
            <span
              :style="{ backgroundColor: c.color + '20' }"
              class="flex h-10 w-10 items-center justify-center rounded-full text-xl"
            >
              {{ c.icon }}
            </span>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-slate-700 dark:text-slate-200">{{ c.name }}</p>
              <p class="text-xs text-slate-400">排序 {{ c.sortOrder }}</p>
            </div>
          </div>
          <div class="flex items-center justify-between opacity-0 transition-opacity group-hover:opacity-100">
            <button @click="openEdit(c)" class="text-xs font-medium text-primary-500 hover:text-primary-600 transition-colors">编辑</button>
            <button @click="handleDelete(c.id)" class="btn-danger !px-3 !py-1">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Teleport :to="modalRoot">
      <div v-if="showModal" @click.self="showModal = false" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-sm">
        <div class="card w-full max-w-md animate-scale-in max-h-[80vh] overflow-auto p-6">
        <h3 class="mb-5 text-lg font-semibold text-slate-800 dark:text-white">
          {{ editingId ? '编辑板块' : '新增板块' }}
        </h3>
        <div class="space-y-4">
          <div>
            <label class="label-base">名称</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="如：餐饮、购物、工资"
              class="input-base"
            />
          </div>
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
            <label class="label-base">图标</label>
            <div class="grid grid-cols-8 gap-2">
              <button
                v-for="icon in icons"
                :key="icon"
                @click="form.icon = icon"
                :class="form.icon === icon ? 'bg-brand-gradient text-white shadow-glow' : 'bg-slate-50 text-slate-600 dark:bg-white/5 dark:text-slate-300'"
                class="flex h-9 w-9 items-center justify-center rounded-lg text-lg transition-all hover:scale-110"
              >
                {{ icon }}
              </button>
            </div>
          </div>
          <div>
            <label class="label-base">颜色</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="color in colors"
                :key="color"
                @click="form.color = color"
                :class="form.color === color ? 'ring-2 ring-offset-2 ring-slate-400 dark:ring-slate-500' : ''"
                :style="{ backgroundColor: color }"
                class="h-8 w-8 rounded-full transition-all hover:scale-110"
              ></button>
            </div>
          </div>
          <div>
            <label class="label-base">排序（数字越小越靠前）</label>
            <input
              v-model.number="form.sortOrder"
              type="number"
              class="input-base tabular"
            />
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
