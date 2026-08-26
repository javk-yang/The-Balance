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
  <div class="space-y-6 animate-fade-up">
    <section class="flex flex-col gap-5 border-b border-stone-200/80 pb-6 dark:border-white/10 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.22em] text-emerald-700 dark:text-emerald-400">Classification</p>
        <h1 class="text-2xl font-semibold tracking-tight text-stone-950 dark:text-white sm:text-3xl">分类管理</h1>
        <p class="mt-2 max-w-2xl text-sm leading-6 text-stone-500 dark:text-stone-400">维护统一的收支分类、识别图标与展示顺序，让财务数据更容易归纳和分析。</p>
      </div>
      <button @click="openCreate" class="btn-primary w-full sm:w-auto">新增分类</button>
    </section>

    <section class="card p-2">
      <div class="grid grid-cols-2 gap-1" role="tablist" aria-label="分类类型">
        <button @click="activeTab = 'EXPENSE'" :class="activeTab === 'EXPENSE' ? 'bg-white text-red-700 shadow-sm ring-1 ring-stone-200 dark:bg-stone-800 dark:text-red-300 dark:ring-white/10' : 'text-stone-500 hover:text-stone-800 dark:text-stone-400 dark:hover:text-stone-200'" class="rounded-xl px-4 py-3 text-sm font-semibold transition-colors">
          支出分类
          <span class="ml-1.5 text-xs font-normal opacity-70">{{ categories.filter(c => c.type === 'EXPENSE').length }}</span>
        </button>
        <button @click="activeTab = 'INCOME'" :class="activeTab === 'INCOME' ? 'bg-white text-emerald-700 shadow-sm ring-1 ring-stone-200 dark:bg-stone-800 dark:text-emerald-300 dark:ring-white/10' : 'text-stone-500 hover:text-stone-800 dark:text-stone-400 dark:hover:text-stone-200'" class="rounded-xl px-4 py-3 text-sm font-semibold transition-colors">
          收入分类
          <span class="ml-1.5 text-xs font-normal opacity-70">{{ categories.filter(c => c.type === 'INCOME').length }}</span>
        </button>
      </div>
    </section>

    <section class="card">
      <header class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-base font-semibold text-stone-900 dark:text-stone-100">{{ activeTab === 'EXPENSE' ? '支出' : '收入' }}分类目录</h2>
          <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">共 {{ filteredCategories().length }} 个分类，排序数字越小越靠前</p>
        </div>
        <button @click="openCreate" class="btn-secondary w-full sm:w-auto">新增{{ activeTab === 'EXPENSE' ? '支出' : '收入' }}分类</button>
      </header>

      <div v-if="loading" class="py-20 text-center text-sm text-stone-500 dark:text-stone-400">正在加载分类...</div>
      <div v-else-if="filteredCategories().length === 0" class="rounded-xl border border-dashed border-stone-300 px-6 py-20 text-center dark:border-white/15">
        <p class="text-sm font-medium text-stone-700 dark:text-stone-300">当前类型暂无分类</p>
        <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">新建分类后，可用于记账与预算管理。</p>
      </div>
      <div v-else class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <article v-for="c in filteredCategories()" :key="c.id" class="group flex min-h-[108px] flex-col justify-between rounded-xl border border-stone-200 bg-white p-4 transition-colors hover:border-stone-300 dark:border-white/10 dark:bg-white/[0.025] dark:hover:border-white/20">
          <div class="flex items-start gap-3">
            <span :style="{ backgroundColor: c.color + '18', color: c.color }" class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-current/10 text-xl">{{ c.icon }}</span>
            <div class="min-w-0 flex-1 pt-0.5">
              <div class="flex items-start justify-between gap-3">
                <h3 class="truncate text-sm font-semibold text-stone-900 dark:text-stone-100">{{ c.name }}</h3>
                <span :style="{ backgroundColor: c.color }" class="mt-1 h-2.5 w-2.5 shrink-0 rounded-full"></span>
              </div>
              <p class="mt-1 text-xs text-stone-500 dark:text-stone-400">显示顺序 {{ c.sortOrder }}</p>
            </div>
          </div>
          <div class="mt-4 flex items-center justify-end gap-1 border-t border-stone-100 pt-3 dark:border-white/[0.06] sm:opacity-60 sm:transition-opacity sm:group-hover:opacity-100">
            <button @click="openEdit(c)" class="btn-ghost !px-3 !py-1.5">编辑</button>
            <button @click="handleDelete(c.id)" class="btn-danger !px-3 !py-1.5">删除</button>
          </div>
        </article>
      </div>
    </section>

    <Teleport :to="modalRoot">
      <div v-if="showModal" @click.self="showModal = false" class="fixed inset-0 z-[100] flex items-end justify-center bg-stone-950/55 sm:items-center sm:px-4">
        <div class="w-full max-w-lg overflow-hidden rounded-t-2xl border border-stone-200 bg-white shadow-2xl dark:border-white/10 dark:bg-stone-900 sm:max-h-[90vh] sm:rounded-2xl">
          <header class="flex items-start justify-between border-b border-stone-200/80 px-5 py-5 dark:border-white/10 sm:px-6">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-emerald-700 dark:text-emerald-400">Category</p>
              <h3 class="mt-1 text-xl font-semibold text-stone-950 dark:text-white">{{ editingId ? '编辑分类' : '新增分类' }}</h3>
            </div>
            <button @click="showModal = false" class="btn-ghost !h-9 !w-9 !p-0" aria-label="关闭弹窗">×</button>
          </header>
          <div class="max-h-[calc(100vh-11rem)] space-y-5 overflow-y-auto px-5 py-5 sm:px-6">
            <label class="block">
              <span class="label-base">分类名称</span>
              <input v-model="form.name" type="text" placeholder="如：餐饮、交通、工资" class="input-base" />
            </label>
            <div>
              <label class="label-base">收支类型</label>
              <div class="grid grid-cols-2 rounded-xl bg-stone-100 p-1 dark:bg-white/[0.05]">
                <button @click="form.type = 'EXPENSE'" :class="form.type === 'EXPENSE' ? 'bg-white text-red-700 shadow-sm ring-1 ring-stone-200 dark:bg-stone-800 dark:text-red-300 dark:ring-white/10' : 'text-stone-500 dark:text-stone-400'" class="rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors">支出</button>
                <button @click="form.type = 'INCOME'" :class="form.type === 'INCOME' ? 'bg-white text-emerald-700 shadow-sm ring-1 ring-stone-200 dark:bg-stone-800 dark:text-emerald-300 dark:ring-white/10' : 'text-stone-500 dark:text-stone-400'" class="rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors">收入</button>
              </div>
            </div>
            <div>
              <label class="label-base">识别图标</label>
              <div class="grid grid-cols-6 gap-2 rounded-xl border border-stone-200 bg-stone-50 p-3 dark:border-white/10 dark:bg-white/[0.02] sm:grid-cols-8">
                <button v-for="icon in icons" :key="icon" @click="form.icon = icon" :class="form.icon === icon ? 'border-emerald-700 bg-white shadow-sm ring-1 ring-emerald-700 dark:border-emerald-500 dark:bg-stone-800 dark:ring-emerald-500' : 'border-transparent hover:border-stone-300 hover:bg-white dark:hover:border-white/15 dark:hover:bg-white/[0.05]'" class="flex aspect-square items-center justify-center rounded-lg border text-lg transition-colors">{{ icon }}</button>
              </div>
            </div>
            <div>
              <label class="label-base">标识颜色</label>
              <div class="flex flex-wrap gap-2.5 rounded-xl border border-stone-200 bg-stone-50 p-3 dark:border-white/10 dark:bg-white/[0.02]">
                <button v-for="color in colors" :key="color" @click="form.color = color" :class="form.color === color ? 'ring-2 ring-stone-900 ring-offset-2 dark:ring-white dark:ring-offset-stone-900' : ''" :style="{ backgroundColor: color }" class="h-8 w-8 rounded-lg border border-black/5 transition-transform hover:scale-105" :aria-label="`选择颜色 ${color}`"></button>
              </div>
            </div>
            <label class="block">
              <span class="label-base">显示顺序</span>
              <input v-model.number="form.sortOrder" type="number" class="input-base tabular" />
              <span class="mt-1.5 block text-xs text-stone-500 dark:text-stone-400">数字越小，在分类选择器中越靠前。</span>
            </label>
          </div>
          <footer class="flex gap-3 border-t border-stone-200/80 px-5 py-4 dark:border-white/10 sm:justify-end sm:px-6">
            <button @click="showModal = false" class="btn-secondary flex-1 sm:flex-none">取消</button>
            <button @click="handleSave" class="btn-primary flex-1 sm:flex-none">保存分类</button>
          </footer>
        </div>
      </div>
    </Teleport>
  </div>
</template>
