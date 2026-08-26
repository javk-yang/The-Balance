<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { assetApi } from '@/api/assets'
import type { Asset, AssetOverview, AssetPayload } from '@/types'

const modalRoot = document.body
const loading = ref(false)
const assets = ref<Asset[]>([])
const showModal = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<AssetPayload>({
  name: '', category: '房产', purchasePrice: 0, currentValue: 0, liquidatable: true,
  status: 'ACTIVE', purchaseDate: new Date().toISOString().slice(0, 10), remark: '',
})

const categories = ['房产', '车辆', '设备', '贵重物品', '电子产品', '其他']
const statuses = [
  { value: 'ACTIVE', label: '持有' },
  { value: 'SOLD', label: '已出售' },
  { value: 'DISPOSED', label: '已处置' },
]
const overview = ref<AssetOverview>({ totalPurchasePrice: 0, totalCurrentValue: 0, totalDepreciation: 0, totalAppreciation: 0, liquidatableValue: 0, assetCount: 0 })
const money = (value: unknown) => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
const statusLabel = (value: string) => statuses.find((item) => item.value === value)?.label || value || '未设置'
const statusClass = (value: string) => value === 'ACTIVE' ? 'badge-income' : value === 'SOLD' ? 'bg-primary-500/10 text-primary-700 dark:text-primary-300' : 'badge-expense'
const normalized = (asset: Asset) => ({ ...asset, purchasePrice: Number(asset.purchasePrice || 0), currentValue: Number(asset.currentValue || 0) })
const displayAssets = computed(() => assets.value.map(normalized))
const ownedCount = computed(() => displayAssets.value.filter((item) => item.status === 'ACTIVE').length)

const fetchData = async () => {
  loading.value = true
  try {
    const [list, summary] = await Promise.all([assetApi.list(), assetApi.overview().catch(() => null)])
    assets.value = Array.isArray(list) ? list : []
    const rows = displayAssets.value
    const data = (summary || {}) as any
    overview.value = {
      totalPurchasePrice: Number(data.totalPurchasePrice ?? data.purchaseTotal ?? rows.reduce((sum, item) => sum + item.purchasePrice, 0)),
      totalCurrentValue: Number(data.totalCurrentValue ?? data.currentTotal ?? rows.reduce((sum, item) => sum + item.currentValue, 0)),
      totalDepreciation: Math.max(0, Number(data.totalDepreciation ?? rows.reduce((sum, item) => sum + Math.max(0, item.purchasePrice - item.currentValue), 0))),
      totalAppreciation: Number(data.totalAppreciation ?? rows.reduce((sum, item) => sum + Math.max(0, item.currentValue - item.purchasePrice), 0)),
      liquidatableValue: Number(data.liquidatableValue ?? rows.filter((item) => item.liquidatable).reduce((sum, item) => sum + item.currentValue, 0)),
      assetCount: Number(data.assetCount ?? data.count ?? rows.length),
      activeCount: Number(data.activeCount ?? rows.filter((item) => item.status === 'ACTIVE').length),
    }
  } catch (error: any) {
    alert(error?.message || '资产数据加载失败')
  } finally { loading.value = false }
}

const resetForm = () => Object.assign(form, { name: '', category: '房产', purchasePrice: 0, currentValue: 0, liquidatable: true, status: 'ACTIVE', purchaseDate: new Date().toISOString().slice(0, 10), remark: '' })
const openCreate = () => { editingId.value = null; resetForm(); showModal.value = true }
const openEdit = (asset: Asset) => {
  editingId.value = asset.id
  Object.assign(form, { name: asset.name || '', category: asset.category || '其他', purchasePrice: Number(asset.purchasePrice || 0), currentValue: Number(asset.currentValue ?? asset.purchasePrice ?? 0), liquidatable: asset.liquidatable !== false, status: asset.status || 'ACTIVE', purchaseDate: asset.purchaseDate?.slice(0, 10) || '', remark: asset.remark || '' })
  showModal.value = true
}
const saveAsset = async () => {
  if (!form.name.trim()) return alert('请输入资产名称')
  if (Number(form.purchasePrice) < 0 || Number(form.currentValue ?? 0) < 0) return alert('价格不能为负数')
  const data: AssetPayload = { ...form, name: form.name.trim(), purchasePrice: Number(form.purchasePrice), currentValue: Number(form.currentValue ?? form.purchasePrice), remark: form.remark?.trim() }
  try {
    if (editingId.value) await assetApi.update(editingId.value, data)
    else await assetApi.create(data)
    showModal.value = false
    await fetchData()
  } catch (error: any) { alert(error?.message || '保存资产失败') }
}
const deleteAsset = async (id: number) => {
  if (!confirm('确定删除这项固定资产吗？')) return
  try { await assetApi.delete(id); await fetchData() } catch (error: any) { alert(error?.message || '删除资产失败') }
}
const formatDate = (value?: string) => value ? value.slice(0, 10) : '—'
onMounted(fetchData)
</script>

<template>
  <div class="space-y-6 animate-fade-up">
    <div class="page-header">
      <div>
        <h2 class="page-title">固定资产</h2>
        <p class="page-description">记录房产、车辆及其他长期资产的价值变化</p>
      </div>
      <button class="btn-primary w-full sm:w-auto" @click="openCreate">新增资产</button>
    </div>

    <section class="grid grid-cols-2 gap-3 lg:grid-cols-3 xl:grid-cols-6">
      <div class="card col-span-2 !p-4 sm:!p-5 lg:col-span-1 xl:col-span-2"><p class="metric-label">资产总额</p><p class="mt-3 tabular text-2xl font-semibold tracking-tight text-ink-900 dark:text-white">{{ money(overview.totalCurrentValue) }}</p><p class="mt-2 text-xs text-slate-500">当前估值合计</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">购入总额</p><p class="mt-3 tabular text-lg font-semibold tracking-tight text-ink-900 sm:text-xl dark:text-white">{{ money(overview.totalPurchasePrice) }}</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">累计折损</p><p class="mt-3 tabular text-lg font-semibold tracking-tight text-expense-600 sm:text-xl dark:text-expense-300">{{ money(overview.totalDepreciation) }}</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">资产增值</p><p class="mt-3 tabular text-lg font-semibold tracking-tight text-income-700 sm:text-xl dark:text-income-300">{{ money(overview.totalAppreciation) }}</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">可变现价值</p><p class="mt-3 tabular text-lg font-semibold tracking-tight text-primary-700 sm:text-xl dark:text-primary-300">{{ money(overview.liquidatableValue) }}</p></div>
      <div class="card col-span-2 !p-4 sm:!p-5 lg:col-span-1"><p class="metric-label">资产数量</p><p class="mt-3 tabular text-xl font-semibold tracking-tight text-ink-900 dark:text-white">{{ overview.assetCount }} <span class="text-xs font-normal text-slate-500">项</span></p><p class="mt-2 text-xs text-slate-500">{{ ownedCount }} 项持有</p></div>
    </section>

    <section class="card !p-0">
      <div class="flex items-center justify-between border-b border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]"><h3 class="section-title">资产明细</h3><span class="text-xs text-slate-500 dark:text-slate-400">共 {{ displayAssets.length }} 项</span></div>
      <div v-if="loading" class="py-14 text-center text-sm text-slate-500">加载中...</div>
      <div v-else-if="displayAssets.length === 0" class="px-5 py-14 text-center"><p class="text-sm font-medium text-ink-800 dark:text-slate-200">暂无固定资产</p><p class="mt-1 text-xs text-slate-500">新增资产后即可跟踪持有价值</p><button class="btn-secondary mt-5" @click="openCreate">新增资产</button></div>
      <div v-else class="divide-y divide-[#e4e5e1] dark:divide-white/[0.06]">
        <article v-for="asset in displayAssets" :key="asset.id" class="p-5 transition-colors hover:bg-[#fafaf8] sm:p-6 dark:hover:bg-white/[0.02]">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-2"><h4 class="font-semibold text-ink-900 dark:text-white">{{ asset.name }}</h4><span class="badge" :class="statusClass(asset.status)">{{ statusLabel(asset.status) }}</span><span v-if="asset.liquidatable" class="badge bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300">可变现</span></div>
              <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">{{ asset.category }} · 购入于 {{ formatDate(asset.purchaseDate) }}</p>
            </div>
            <div class="flex w-full items-center justify-end gap-1 sm:w-auto"><button class="rounded-lg px-3 py-2 text-xs font-medium text-slate-600 hover:bg-primary-50 hover:text-primary-700 dark:text-slate-300 dark:hover:bg-white/5" @click="openEdit(asset)">编辑</button><button class="btn-danger !rounded-lg !px-3 !py-2 !text-xs" @click="deleteAsset(asset.id)">删除</button></div>
          </div>
          <div class="mt-5 grid grid-cols-2 gap-4 sm:max-w-lg">
            <div><p class="metric-label">当前价值</p><p class="mt-1.5 tabular text-xl font-semibold tracking-tight text-ink-900 dark:text-white">{{ money(asset.currentValue) }}</p></div>
            <div><p class="metric-label">购入价格</p><p class="mt-1.5 tabular text-xl font-semibold tracking-tight text-slate-600 dark:text-slate-300">{{ money(asset.purchasePrice) }}</p></div>
          </div>
          <p v-if="asset.remark" class="mt-4 border-t border-[#e4e5e1] pt-3 text-xs leading-relaxed text-slate-500 dark:border-white/[0.06] dark:text-slate-400">备注：{{ asset.remark }}</p>
        </article>
      </div>
    </section>

    <Teleport :to="modalRoot">
      <div v-if="showModal" class="fixed inset-0 z-[100] flex items-end justify-center bg-ink-950/45 sm:items-center sm:px-4 sm:py-8" @click.self="showModal = false">
        <div class="card max-h-[92vh] w-full max-w-2xl animate-scale-in overflow-auto !rounded-b-none !p-0 sm:!rounded-2xl">
          <div class="flex items-center justify-between border-b border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]"><div><h3 class="text-lg font-semibold text-ink-900 dark:text-white">{{ editingId ? '编辑资产' : '新增资产' }}</h3><p class="mt-0.5 text-xs text-slate-500">维护资产估值、状态与流动性</p></div><button class="rounded-lg px-2 py-1 text-lg leading-none text-slate-400 hover:bg-slate-100 hover:text-ink-900 dark:hover:bg-white/5" aria-label="关闭" @click="showModal = false">×</button></div>
          <div class="grid grid-cols-1 gap-4 px-5 py-5 sm:grid-cols-2 sm:px-6">
            <div><label class="label-base">资产名称</label><input v-model="form.name" class="input-base" placeholder="如：自住房、笔记本电脑" /></div>
            <div><label class="label-base">分类</label><select v-model="form.category" class="input-base"><option v-for="item in categories" :key="item" :value="item">{{ item }}</option></select></div>
            <div><label class="label-base">购入价格</label><input v-model.number="form.purchasePrice" type="number" min="0" step="0.01" class="input-base tabular" /></div>
            <div><label class="label-base">当前价值</label><input v-model.number="form.currentValue" type="number" min="0" step="0.01" class="input-base tabular" /></div>
            <div><label class="label-base">资产状态</label><select v-model="form.status" class="input-base"><option v-for="item in statuses" :key="item.value" :value="item.value">{{ item.label }}</option></select></div>
            <div><label class="label-base">购入日期</label><input v-model="form.purchaseDate" type="date" class="input-base" /></div>
            <label class="flex items-center gap-3 rounded-xl border border-[#e4e5e1] px-4 py-3 text-sm font-medium text-ink-800 dark:border-white/10 dark:text-slate-300"><input v-model="form.liquidatable" type="checkbox" class="h-4 w-4 rounded accent-primary-700" /> 可变现资产</label>
            <div class="sm:col-span-2"><label class="label-base">备注</label><textarea v-model="form.remark" class="input-base min-h-20 resize-y" placeholder="选填"></textarea></div>
          </div>
          <div class="flex gap-3 border-t border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]"><button class="btn-ghost flex-1" @click="showModal = false">取消</button><button class="btn-primary flex-1" @click="saveAsset">保存资产</button></div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
