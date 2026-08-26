<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { accountApi } from '@/api/account'
const modalRoot = document.body
import type { Account } from '@/types'

const loading = ref(false)
const accounts = ref<Account[]>([])
const showModal = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  name: '',
  type: 'CASH',
  balance: '',
  remark: '',
})

const accountTypes: Record<string, { label: string; mark: string; color: string }> = {
  CASH: { label: '现金', mark: '现', color: '#3b6a57' },
  BANK_CARD: { label: '银行卡', mark: '卡', color: '#305647' },
  ALIPAY: { label: '支付宝', mark: '支', color: '#4c7f6a' },
  WECHAT: { label: '微信', mark: '微', color: '#2e6b53' },
}

const fetchData = async () => {
  loading.value = true
  try {
    accounts.value = await accountApi.list()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  form.name = ''
  form.type = 'CASH'
  form.balance = '0'
  form.remark = ''
  showModal.value = true
}

const openEdit = (a: Account) => {
  editingId.value = a.id
  form.name = a.name
  form.type = a.type
  form.balance = String(a.balance)
  form.remark = a.remark
  showModal.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) {
    alert('请输入账户名称')
    return
  }
  const data = {
    name: form.name,
    type: form.type,
    balance: parseFloat(form.balance || '0'),
    remark: form.remark,
  }
  try {
    if (editingId.value) {
      await accountApi.update(editingId.value, data)
    } else {
      await accountApi.create(data)
    }
    showModal.value = false
    fetchData()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('删除账户不会影响已有记录，但已有记录的账户信息将丢失，确定删除？')) return
  try {
    await accountApi.delete(id)
    fetchData()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

const totalBalance = computed(() =>
  accounts.value.reduce((sum, a) => sum + Number(a.balance), 0)
)

onMounted(fetchData)
</script>

<template>
  <div class="space-y-6 animate-fade-up">
    <div class="page-header">
      <div>
        <h2 class="page-title">资金账户</h2>
        <p class="page-description">集中查看现金、银行卡与第三方支付账户余额</p>
      </div>
      <button class="btn-primary w-full sm:w-auto" @click="openCreate">新增账户</button>
    </div>

    <section class="card overflow-hidden !p-0">
      <div class="grid gap-0 lg:grid-cols-[1.5fr_1fr]">
        <div class="border-b border-[#e4e5e1] p-5 sm:p-7 lg:border-b-0 lg:border-r dark:border-white/[0.06]">
          <p class="metric-label">账户总资产</p>
          <p class="mt-3 tabular text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl dark:text-white">
            ¥{{ totalBalance.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
          </p>
          <p class="mt-3 text-xs text-slate-500 dark:text-slate-400">当前全部账户余额合计</p>
        </div>
        <div class="flex items-center justify-between gap-4 bg-[#fafaf8] p-5 sm:p-7 dark:bg-white/[0.02] lg:block">
          <div>
            <p class="metric-label">账户数量</p>
            <p class="mt-2 tabular text-2xl font-semibold text-ink-900 dark:text-white">{{ accounts.length }}</p>
          </div>
          <p class="text-xs text-slate-500 dark:text-slate-400 lg:mt-4">个已登记账户</p>
        </div>
      </div>
    </section>

    <section class="card !p-0">
      <div class="flex items-center justify-between border-b border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]">
        <h3 class="section-title">账户明细</h3>
        <span class="text-xs text-slate-500 dark:text-slate-400">按账户独立维护余额</span>
      </div>

      <div v-if="loading" class="py-14 text-center text-sm text-slate-500">加载中...</div>
      <div v-else-if="accounts.length === 0" class="px-5 py-14 text-center">
        <p class="text-sm font-medium text-ink-800 dark:text-slate-200">暂无账户</p>
        <p class="mt-1 text-xs text-slate-500">新增第一个账户后即可开始管理资金</p>
        <button class="btn-secondary mt-5" @click="openCreate">新增账户</button>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2">
        <article
          v-for="(a, index) in accounts"
          :key="a.id"
          class="group p-5 transition-colors hover:bg-[#fafaf8] sm:p-6 dark:hover:bg-white/[0.02]"
          :class="[
            index < accounts.length - 1 ? 'border-b border-[#e4e5e1] dark:border-white/[0.06]' : '',
            index % 2 === 0 ? 'md:border-r md:border-[#e4e5e1] md:dark:border-white/[0.06]' : '',
            index === accounts.length - 2 && accounts.length % 2 === 0 ? 'md:border-b-0' : '',
          ]"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex min-w-0 items-center gap-3">
              <span
                :style="{ color: accountTypes[a.type]?.color || '#3b6a57' }"
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-primary-100 bg-primary-50 text-sm font-semibold dark:border-primary-700/40 dark:bg-primary-900/30"
              >
                {{ accountTypes[a.type]?.mark || '账' }}
              </span>
              <div class="min-w-0">
                <h4 class="truncate text-sm font-semibold text-ink-900 dark:text-white">{{ a.name }}</h4>
                <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400">{{ accountTypes[a.type]?.label || a.type }}</p>
              </div>
            </div>
            <div class="flex shrink-0 items-center gap-1">
              <button class="rounded-lg px-2.5 py-1.5 text-xs font-medium text-slate-600 transition-colors hover:bg-primary-50 hover:text-primary-700 dark:text-slate-300 dark:hover:bg-white/5" @click="openEdit(a)">编辑</button>
              <button class="btn-danger !rounded-lg !px-2.5 !py-1.5 !text-xs" @click="handleDelete(a.id)">删除</button>
            </div>
          </div>
          <p class="mt-5 tabular text-2xl font-semibold tracking-tight text-ink-900 dark:text-white">
            ¥{{ Number(a.balance).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
          </p>
          <p v-if="a.remark" class="mt-2 truncate text-xs text-slate-500 dark:text-slate-400" :title="a.remark">{{ a.remark }}</p>
          <p v-else class="mt-2 text-xs text-slate-400">暂无备注</p>
        </article>
      </div>
    </section>

    <Teleport :to="modalRoot">
      <div v-if="showModal" class="fixed inset-0 z-[100] flex items-end justify-center bg-ink-950/45 sm:items-center sm:px-4 sm:py-8" @click.self="showModal = false">
        <div class="card max-h-[92vh] w-full max-w-md animate-scale-in overflow-auto !rounded-b-none !p-0 sm:!rounded-2xl">
          <div class="flex items-center justify-between border-b border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]">
            <div>
              <h3 class="text-lg font-semibold text-ink-900 dark:text-white">{{ editingId ? '编辑账户' : '新增账户' }}</h3>
              <p class="mt-0.5 text-xs text-slate-500">维护账户基础信息与当前余额</p>
            </div>
            <button class="rounded-lg px-2 py-1 text-lg leading-none text-slate-400 hover:bg-slate-100 hover:text-ink-900 dark:hover:bg-white/5 dark:hover:text-white" aria-label="关闭" @click="showModal = false">×</button>
          </div>
          <div class="space-y-5 px-5 py-5 sm:px-6">
            <div>
              <label class="label-base">账户名称</label>
              <input v-model="form.name" type="text" placeholder="如：工资卡、备用现金" class="input-base" />
            </div>
            <div>
              <label class="label-base">账户类型</label>
              <div class="grid grid-cols-2 gap-2 sm:grid-cols-4">
                <button
                  v-for="(info, key) in accountTypes"
                  :key="key"
                  type="button"
                  class="rounded-xl border px-3 py-2.5 text-sm font-medium transition-colors"
                  :class="form.type === key ? 'border-primary-700 bg-primary-700 text-white' : 'border-[#e4e5e1] bg-white text-slate-600 hover:border-primary-300 hover:text-primary-700 dark:border-white/10 dark:bg-white/5 dark:text-slate-300'"
                  @click="form.type = key"
                >
                  <span class="mr-1.5 text-xs">{{ info.mark }}</span>{{ info.label }}
                </button>
              </div>
            </div>
            <div>
              <label class="label-base">当前余额</label>
              <input v-model="form.balance" type="number" step="0.01" placeholder="0.00" class="input-base tabular" />
            </div>
            <div>
              <label class="label-base">备注</label>
              <input v-model="form.remark" type="text" placeholder="选填" class="input-base" />
            </div>
          </div>
          <div class="flex gap-3 border-t border-[#e4e5e1] px-5 py-4 sm:px-6 dark:border-white/[0.06]">
            <button class="btn-ghost flex-1" @click="showModal = false">取消</button>
            <button class="btn-primary flex-1" @click="handleSave">保存账户</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
