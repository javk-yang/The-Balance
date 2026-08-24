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

const accountTypes: Record<string, { label: string; icon: string; color: string }> = {
  CASH: { label: '现金', icon: '💵', color: '#10b981' },
  BANK_CARD: { label: '银行卡', icon: '🏦', color: '#3b82f6' },
  ALIPAY: { label: '支付宝', icon: '💙', color: '#06b6d4' },
  WECHAT: { label: '微信', icon: '💚', color: '#22c55e' },
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
  <div class="space-y-5 animate-fade-up">
    <!-- 总余额 -->
    <div class="rounded-2xl bg-brand-gradient p-6 text-white shadow-glow">
      <p class="text-sm text-white/80">总资产</p>
      <p class="mt-1 tabular text-3xl font-bold">¥{{ totalBalance.toFixed(2) }}</p>
      <p class="mt-2 text-xs text-white/70">{{ accounts.length }} 个账户</p>
    </div>

    <!-- 账户列表 -->
    <div class="card">
      <div class="mb-5 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">账户列表</h3>
        <button @click="openCreate" class="btn-primary">+ 新增账户</button>
      </div>

      <div v-if="loading" class="py-10 text-center text-slate-400">加载中...</div>
      <div v-else-if="accounts.length === 0" class="py-10 text-center text-slate-400">
        暂无账户，点击「新增账户」添加
      </div>
      <div v-else class="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div
          v-for="a in accounts"
          :key="a.id"
          class="card-hover rounded-2xl border border-slate-100 p-4 transition-all dark:border-white/5"
        >
          <div class="mb-3 flex items-start justify-between">
            <div class="flex items-center gap-3">
              <span
                :style="{ backgroundColor: (accountTypes[a.type]?.color || '#6366f1') + '20' }"
                class="flex h-12 w-12 items-center justify-center rounded-full text-2xl"
              >
                {{ accountTypes[a.type]?.icon || '💳' }}
              </span>
              <div>
                <p class="text-sm font-medium text-slate-700 dark:text-slate-200">{{ a.name }}</p>
                <p class="text-xs text-slate-400">{{ accountTypes[a.type]?.label || a.type }}</p>
              </div>
            </div>
            <div class="flex gap-2">
              <button @click="openEdit(a)" class="text-xs font-medium text-primary-500 hover:text-primary-600 transition-colors">编辑</button>
              <button @click="handleDelete(a.id)" class="btn-danger !px-3 !py-1">删除</button>
            </div>
          </div>
          <p class="tabular text-xl font-bold text-slate-800 dark:text-white">
            ¥{{ Number(a.balance).toFixed(2) }}
          </p>
          <p v-if="a.remark" class="mt-1 text-xs text-slate-400">{{ a.remark }}</p>
        </div>
      </div>
    </div>

    <!-- 弹窗 -->
    <Teleport :to="modalRoot">
      <div v-if="showModal" @click.self="showModal = false" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-sm">
        <div class="card w-full max-w-md animate-scale-in max-h-[80vh] overflow-auto p-6">
        <h3 class="mb-5 text-lg font-semibold text-slate-800 dark:text-white">
          {{ editingId ? '编辑账户' : '新增账户' }}
        </h3>
        <div class="space-y-4">
          <div>
            <label class="label-base">账户名称</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="如：工资卡、零花钱"
              class="input-base"
            />
          </div>
          <div>
            <label class="label-base">类型</label>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="(info, key) in accountTypes"
                :key="key"
                @click="form.type = key"
                :class="form.type === key ? 'bg-brand-gradient text-white shadow-glow' : 'bg-slate-100 text-slate-600 dark:bg-white/5 dark:text-slate-300'"
                class="rounded-xl py-2 text-sm transition-all"
              >
                {{ info.icon }} {{ info.label }}
              </button>
            </div>
          </div>
          <div>
            <label class="label-base">余额</label>
            <input
              v-model="form.balance"
              type="number"
              step="0.01"
              placeholder="0.00"
              class="input-base tabular"
            />
          </div>
          <div>
            <label class="label-base">备注</label>
            <input
              v-model="form.remark"
              type="text"
              placeholder="选填"
              class="input-base"
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
