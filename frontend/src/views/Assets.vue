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
const statusClass = (value: string) => value === 'ACTIVE' ? 'badge-income' : value === 'SOLD' ? 'bg-primary-500/10 text-primary-600 dark:text-primary-300' : 'badge-expense'
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
  <div class="space-y-5 animate-fade-up">
    <div class="flex items-center justify-between gap-3">
      <div><h2 class="text-2xl font-bold text-slate-800 dark:text-white">固定资产</h2><p class="mt-1 text-sm text-slate-400">记录房产、车辆及其他长期资产的价值变化</p></div>
      <button class="btn-primary" @click="openCreate">+ 新增资产</button>
    </div>

    <div class="grid grid-cols-2 gap-3 lg:grid-cols-5">
      <div class="card"><p class="text-xs text-slate-400">资产总额</p><p class="mt-2 tabular text-lg font-bold text-slate-800 dark:text-white">{{ money(overview.totalCurrentValue) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">购入总额</p><p class="mt-2 tabular text-lg font-bold text-slate-800 dark:text-white">{{ money(overview.totalPurchasePrice) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">累计折损</p><p class="mt-2 tabular text-lg font-bold text-expense-500">{{ money(overview.totalDepreciation) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">资产增值</p><p class="mt-2 tabular text-lg font-bold text-income-500">{{ money(overview.totalAppreciation) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">可变现价值</p><p class="mt-2 tabular text-lg font-bold text-income-500">{{ money(overview.liquidatableValue) }}</p></div>
      <div class="card"><p class="text-xs text-slate-400">资产数量</p><p class="mt-2 tabular text-lg font-bold text-primary-500">{{ overview.assetCount }} <span class="text-xs font-normal text-slate-400">项 · {{ ownedCount }} 项持有</span></p></div>
    </div>

    <div class="card">
      <div class="mb-5 flex items-center justify-between"><h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">资产列表</h3><span class="text-xs text-slate-400">共 {{ displayAssets.length }} 项</span></div>
      <div v-if="loading" class="py-12 text-center text-slate-400">加载中...</div>
      <div v-else-if="displayAssets.length === 0" class="py-12 text-center text-slate-400">暂无固定资产，点击右上角添加</div>
      <div v-else class="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <div v-for="asset in displayAssets" :key="asset.id" class="card-hover rounded-2xl border border-slate-100 p-5 dark:border-white/5">
          <div class="flex items-start justify-between gap-3"><div><h4 class="font-semibold text-slate-800 dark:text-white">{{ asset.name }}</h4><p class="mt-1 text-xs text-slate-400">{{ asset.category }} · 购入 {{ formatDate(asset.purchaseDate) }}</p></div><span class="badge" :class="statusClass(asset.status)">{{ statusLabel(asset.status) }}</span></div>
          <div class="mt-5 grid grid-cols-2 gap-3"><div><p class="text-xs text-slate-400">当前价值</p><p class="mt-1 tabular text-lg font-bold text-slate-800 dark:text-white">{{ money(asset.currentValue) }}</p></div><div><p class="text-xs text-slate-400">购入价格</p><p class="mt-1 tabular text-lg font-semibold text-slate-600 dark:text-slate-300">{{ money(asset.purchasePrice) }}</p></div></div>
          <div class="mt-4 flex items-center justify-between text-xs text-slate-400"><span>{{ asset.liquidatable ? '可变现' : '不可变现' }}</span><div class="flex gap-3"><button class="text-xs font-medium text-slate-500 hover:text-primary-500" @click="openEdit(asset)">编辑</button><button class="btn-danger !px-3 !py-1" @click="deleteAsset(asset.id)">删除</button></div></div>
          <p v-if="asset.remark" class="mt-3 truncate text-xs text-slate-400" :title="asset.remark">备注：{{ asset.remark }}</p>
        </div>
      </div>
    </div>

    <Teleport :to="modalRoot">
      <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 py-6 backdrop-blur-sm" @click.self="showModal = false">
        <div class="card max-h-[80vh] w-full max-w-2xl animate-scale-in overflow-auto p-6">
          <div class="mb-5 flex items-center justify-between"><h3 class="text-lg font-semibold text-slate-800 dark:text-white">{{ editingId ? '编辑资产' : '新增资产' }}</h3><button class="text-xl text-slate-400 hover:text-slate-700" @click="showModal = false">×</button></div>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div><label class="label-base">资产名称</label><input v-model="form.name" class="input-base" placeholder="如：自住房、笔记本电脑" /></div>
            <div><label class="label-base">分类</label><select v-model="form.category" class="input-base"><option v-for="item in categories" :key="item" :value="item">{{ item }}</option></select></div>
            <div><label class="label-base">购入价格</label><input v-model.number="form.purchasePrice" type="number" min="0" step="0.01" class="input-base" /></div>
            <div><label class="label-base">当前价值</label><input v-model.number="form.currentValue" type="number" min="0" step="0.01" class="input-base" /></div>
            <div><label class="label-base">资产状态</label><select v-model="form.status" class="input-base"><option v-for="item in statuses" :key="item.value" :value="item.value">{{ item.label }}</option></select></div>
            <div><label class="label-base">购入日期</label><input v-model="form.purchaseDate" type="date" class="input-base" /></div>
            <label class="flex items-center gap-2 self-end pb-2 text-sm text-slate-600 dark:text-slate-300"><input v-model="form.liquidatable" type="checkbox" class="h-4 w-4 rounded accent-primary-500" /> 可变现</label>
            <div class="sm:col-span-2"><label class="label-base">备注</label><textarea v-model="form.remark" class="input-base min-h-20 resize-y" placeholder="选填"></textarea></div>
          </div>
          <div class="mt-6 flex gap-3"><button class="btn-ghost flex-1" @click="showModal = false">取消</button><button class="btn-primary flex-1" @click="saveAsset">保存资产</button></div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
