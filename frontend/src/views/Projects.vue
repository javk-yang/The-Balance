<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { projectApi } from '@/api/projects'
import type { Project, ProjectOverview, ProjectPayload } from '@/types'

const modalRoot = document.body
const loading = ref(false)
const projects = ref<Project[]>([])
const overview = ref<ProjectOverview>({
  totalContractValue: 0,
  totalReceived: 0,
  totalPending: 0,
  currentMonthReceived: 0,
  projectCount: 0,
})
const showModal = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  name: '',
  customer: '',
  price: '',
  depositAmount: '',
  depositStatus: 'UNPAID',
  balanceStatus: 'UNPAID',
  signedDate: '',
  deliveryDate: '',
  status: 'IN_PROGRESS',
  remark: '',
})

const statusOptions = [
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'PAUSED', label: '已暂停' },
  { value: 'CANCELLED', label: '已取消' },
]
const paymentOptions = [
  { value: 'UNPAID', label: '待收' },
  { value: 'PAID', label: '已收' },
]

const money = (value: unknown) => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
const statusLabel = (value: string) => statusOptions.find((item) => item.value === value)?.label || value || '未设置'
const paymentLabel = (value: string) => paymentOptions.find((item) => item.value === value)?.label || value || '待收'
const normalizedProject = (project: Project) => {
  const raw = project as any
  const price = Number(raw.price || 0)
  const depositAmount = Number(raw.depositAmount || 0)
  const received = Number(raw.totalReceived ?? raw.receivedAmount ?? (raw.depositStatus === 'PAID' ? depositAmount : 0))
  const receivable = Number(raw.pendingAmount ?? raw.receivableAmount ?? Math.max(0, price - received))
  return {
    ...raw,
    customer: raw.customer ?? raw.client ?? '',
    signedDate: raw.signedDate ?? raw.contractDate ?? '',
    deliveryDate: raw.deliveryDate ?? raw.dueDate ?? '',
    receivedAmount: received,
    receivableAmount: receivable,
    progress: Number(raw.paymentProgress ?? raw.progress ?? (price ? (received / price) * 100 : 0)),
  }
}
const displayProjects = computed(() => projects.value.map(normalizedProject))
const activeCount = computed(() => displayProjects.value.filter((project) => !['COMPLETED', 'CANCELLED'].includes(project.status)).length)

const fetchData = async () => {
  loading.value = true
  try {
    const [list, summary] = await Promise.all([projectApi.list(), projectApi.overview()])
    projects.value = Array.isArray(list) ? list : []
    const data = (summary || {}) as any
    overview.value = {
      totalContractValue: Number(data.totalContractValue ?? data.totalContractAmount ?? 0),
      totalReceived: Number(data.totalReceived ?? data.totalReceivedAmount ?? 0),
      totalPending: Number(data.totalPending ?? data.totalReceivableAmount ?? 0),
      currentMonthReceived: Number(data.currentMonthReceived ?? data.monthlyReceivedAmount ?? 0),
      projectCount: Number(data.projectCount ?? data.count ?? projects.value.length),
    }
  } catch (error: any) {
    alert(error?.message || '项目数据加载失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => Object.assign(form, {
  name: '', customer: '', price: '', depositAmount: '', depositStatus: 'UNPAID', balanceStatus: 'UNPAID',
  signedDate: new Date().toISOString().slice(0, 10), deliveryDate: '', status: 'IN_PROGRESS', remark: '',
})
const openCreate = () => { editingId.value = null; resetForm(); showModal.value = true }
const openEdit = (project: Project) => {
  editingId.value = project.id
  Object.assign(form, {
    name: project.name || '', customer: project.customer || '', price: String(project.price ?? ''), depositAmount: String(project.depositAmount ?? ''),
    depositStatus: project.depositStatus || 'PENDING', balanceStatus: project.balanceStatus || 'PENDING', signedDate: project.signedDate || '',
    deliveryDate: project.deliveryDate || '', status: project.status || 'IN_PROGRESS', remark: project.remark || '',
  })
  showModal.value = true
}
const saveProject = async () => {
  if (!form.name.trim() || !form.customer.trim() || !form.price || Number(form.price) <= 0 || !form.signedDate) {
    return alert('请填写项目名称、客户、项目价格和签约日期')
  }
  if (Number(form.depositAmount || 0) < 0 || Number(form.depositAmount || 0) > Number(form.price)) {
    return alert('定金金额不能小于0且不能超过项目价格')
  }
  const data: ProjectPayload = {
    name: form.name.trim(), client: form.customer.trim(), price: Number(form.price), depositAmount: Number(form.depositAmount || 0),
    depositStatus: form.depositStatus, balanceStatus: form.balanceStatus, contractDate: form.signedDate, dueDate: form.deliveryDate || undefined,
    status: form.status, remark: form.remark.trim(),
  }
  try {
    if (editingId.value) await projectApi.update(editingId.value, data)
    else await projectApi.create(data)
    showModal.value = false
    await fetchData()
  } catch (error: any) {
    alert(error?.message || '保存项目失败')
  }
}
const deleteProject = async (id: number) => {
  if (!confirm('确定删除这个项目吗？')) return
  try { await projectApi.delete(id); await fetchData() } catch (error: any) { alert(error?.message || '删除项目失败') }
}
const formatDate = (value?: string) => value ? value.slice(0, 10) : '—'

onMounted(fetchData)
</script>

<template>
  <div class="space-y-6 animate-fade-up">
    <div class="page-header">
      <div>
        <h2 class="page-title">已签约项目</h2>
        <p class="page-description">追踪合同金额、回款状态与项目交付进度</p>
      </div>
      <button class="btn-primary w-full sm:w-auto" @click="openCreate">新增项目</button>
    </div>

    <section class="grid grid-cols-2 gap-3 lg:grid-cols-5">
      <div class="card col-span-2 !p-4 sm:!p-5 lg:col-span-1"><p class="metric-label">合同总额</p><p class="mt-3 tabular text-xl font-semibold tracking-tight text-ink-900 lg:text-lg xl:text-xl dark:text-white">{{ money(overview.totalContractValue) }}</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">已收款</p><p class="mt-3 tabular text-lg font-semibold tracking-tight text-income-700 xl:text-xl dark:text-income-300">{{ money(overview.totalReceived) }}</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">待收款</p><p class="mt-3 tabular text-lg font-semibold tracking-tight text-expense-600 xl:text-xl dark:text-expense-300">{{ money(overview.totalPending) }}</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">本月回款</p><p class="mt-3 tabular text-lg font-semibold tracking-tight text-primary-700 xl:text-xl dark:text-primary-300">{{ money(overview.currentMonthReceived) }}</p></div>
      <div class="card !p-4 sm:!p-5"><p class="metric-label">项目数量</p><p class="mt-3 tabular text-xl font-semibold tracking-tight text-ink-900 dark:text-white">{{ overview.projectCount }} <span class="text-xs font-normal text-slate-500">个</span></p></div>
    </section>

    <section class="card !p-0">
      <div class="flex items-center justify-between border-b border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]"><h3 class="section-title">项目明细</h3><span class="text-xs text-slate-500 dark:text-slate-400">{{ activeCount }} 个进行中</span></div>
      <div v-if="loading" class="py-14 text-center text-sm text-slate-500">加载中...</div>
      <div v-else-if="displayProjects.length === 0" class="px-5 py-14 text-center"><p class="text-sm font-medium text-ink-800 dark:text-slate-200">暂无已签约项目</p><p class="mt-1 text-xs text-slate-500">新增项目后即可跟踪合同回款</p><button class="btn-secondary mt-5" @click="openCreate">新增项目</button></div>
      <div v-else class="divide-y divide-[#e4e5e1] dark:divide-white/[0.06]">
        <article v-for="project in displayProjects" :key="project.id" class="p-5 transition-colors hover:bg-[#fafaf8] sm:p-6 dark:hover:bg-white/[0.02]">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-2"><h4 class="font-semibold text-ink-900 dark:text-white">{{ project.name }}</h4><span class="badge" :class="project.status === 'COMPLETED' ? 'badge-income' : project.status === 'CANCELLED' ? 'badge-expense' : 'bg-primary-500/10 text-primary-700 dark:text-primary-300'">{{ statusLabel(project.status) }}</span></div>
              <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">{{ project.customer }} · 签约于 {{ formatDate(project.signedDate) }}</p>
            </div>
            <div class="flex w-full items-center justify-end gap-1 sm:w-auto"><button class="rounded-lg px-3 py-2 text-xs font-medium text-slate-600 hover:bg-primary-50 hover:text-primary-700 dark:text-slate-300 dark:hover:bg-white/5" @click="openEdit(project)">编辑</button><button class="btn-danger !rounded-lg !px-3 !py-2 !text-xs" @click="deleteProject(project.id)">删除</button></div>
          </div>
          <div class="mt-5 grid grid-cols-2 gap-x-4 gap-y-4 sm:grid-cols-4">
            <div><p class="metric-label">合同金额</p><p class="mt-1.5 tabular text-base font-semibold text-ink-900 dark:text-white">{{ money(project.price) }}</p></div>
            <div><p class="metric-label">已收款</p><p class="mt-1.5 tabular text-base font-semibold text-income-700 dark:text-income-300">{{ money(project.receivedAmount) }}</p></div>
            <div><p class="metric-label">待收款</p><p class="mt-1.5 tabular text-base font-semibold text-expense-600 dark:text-expense-300">{{ money(project.receivableAmount) }}</p></div>
            <div><p class="metric-label">交付日期</p><p class="mt-1.5 text-base font-semibold text-ink-800 dark:text-slate-200">{{ formatDate(project.deliveryDate) }}</p></div>
          </div>
          <div class="mt-5 grid grid-cols-1 gap-2 rounded-xl border border-[#e4e5e1] bg-[#fafaf8] px-4 py-3 text-xs text-slate-500 sm:grid-cols-2 dark:border-white/[0.06] dark:bg-white/[0.02] dark:text-slate-400"><span>定金 <b class="tabular font-medium text-ink-800 dark:text-slate-200">{{ money(project.depositAmount) }}</b> · {{ paymentLabel(project.depositStatus) }}</span><span class="sm:text-right">尾款 · {{ paymentLabel(project.balanceStatus) }}</span></div>
          <div class="mt-4"><div class="mb-2 flex justify-between text-xs text-slate-500 dark:text-slate-400"><span>回款进度</span><span class="tabular font-medium text-ink-800 dark:text-slate-200">{{ Math.min(100, Math.max(0, Number(project.progress || 0))).toFixed(1) }}%</span></div><div class="h-1.5 overflow-hidden rounded-full bg-slate-100 dark:bg-white/10"><div class="h-full rounded-full bg-primary-600 transition-all" :style="{ width: `${Math.min(100, Math.max(0, Number(project.progress || 0)))}%` }"></div></div></div>
          <p v-if="project.remark" class="mt-4 text-xs leading-relaxed text-slate-500 dark:text-slate-400">备注：{{ project.remark }}</p>
        </article>
      </div>
    </section>

    <Teleport :to="modalRoot">
      <div v-if="showModal" class="fixed inset-0 z-[100] flex items-end justify-center bg-ink-950/45 sm:items-center sm:px-4 sm:py-8" @click.self="showModal = false">
        <div class="card max-h-[92vh] w-full max-w-2xl animate-scale-in overflow-auto !rounded-b-none !p-0 sm:!rounded-2xl">
          <div class="flex items-center justify-between border-b border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]"><div><h3 class="text-lg font-semibold text-ink-900 dark:text-white">{{ editingId ? '编辑项目' : '新增项目' }}</h3><p class="mt-0.5 text-xs text-slate-500">维护项目合同、回款与交付信息</p></div><button class="rounded-lg px-2 py-1 text-lg leading-none text-slate-400 hover:bg-slate-100 hover:text-ink-900 dark:hover:bg-white/5" aria-label="关闭" @click="showModal = false">×</button></div>
          <div class="grid grid-cols-1 gap-4 px-5 py-5 sm:grid-cols-2 sm:px-6">
            <div><label class="label-base">项目名称</label><input v-model="form.name" class="input-base" placeholder="如：品牌官网建设" /></div>
            <div><label class="label-base">客户</label><input v-model="form.customer" class="input-base" placeholder="客户名称" /></div>
            <div><label class="label-base">项目价格</label><input v-model="form.price" type="number" min="0" step="0.01" class="input-base tabular" /></div>
            <div><label class="label-base">定金金额</label><input v-model="form.depositAmount" type="number" min="0" step="0.01" class="input-base tabular" /></div>
            <div><label class="label-base">定金状态</label><select v-model="form.depositStatus" class="input-base"><option v-for="item in paymentOptions" :key="item.value" :value="item.value">{{ item.label }}</option></select></div>
            <div><label class="label-base">尾款状态</label><select v-model="form.balanceStatus" class="input-base"><option v-for="item in paymentOptions" :key="item.value" :value="item.value">{{ item.label }}</option></select></div>
            <div><label class="label-base">签约日期</label><input v-model="form.signedDate" type="date" class="input-base" /></div>
            <div><label class="label-base">交付日期</label><input v-model="form.deliveryDate" type="date" class="input-base" /></div>
            <div><label class="label-base">项目状态</label><select v-model="form.status" class="input-base"><option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option></select></div>
            <div class="sm:col-span-2"><label class="label-base">备注</label><textarea v-model="form.remark" class="input-base min-h-20 resize-y" placeholder="选填"></textarea></div>
          </div>
          <div class="flex gap-3 border-t border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]"><button class="btn-ghost flex-1" @click="showModal = false">取消</button><button class="btn-primary flex-1" @click="saveProject">保存项目</button></div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
