<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api/dashboard'
import { categoryApi } from '@/api/category'
import type { Category } from '@/types'

const loading = ref(true)
const summary = ref<any>({})
const categories = ref<Category[]>([])

// 图表实例
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const isDark = () => document.documentElement.classList.contains('dark')

const fetchSummary = async () => {
  loading.value = true
  try {
    summary.value = await dashboardApi.summary()
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  categories.value = await categoryApi.list()
}

// 渲染近7天趋势图（渐变面积）
const renderTrendChart = () => {
  const el = document.getElementById('trendChart')
  if (!el || !summary.value.weeklyTrend) return
  if (!trendChart) trendChart = echarts.init(el)
  const data = summary.value.weeklyTrend
  const axisColor = isDark() ? '#475569' : '#cbd5e1'
  const textColor = isDark() ? '#94a3b8' : '#64748b'
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: isDark() ? 'rgba(15,21,33,0.95)' : 'rgba(255,255,255,0.95)',
      borderColor: 'transparent',
      textStyle: { color: isDark() ? '#e2e8f0' : '#334155' },
      padding: [10, 14],
      borderRadius: 12,
      valueFormatter: (v: any) => '¥' + Number(v || 0).toFixed(2),
    },
    legend: { data: ['收入', '支出'], top: 0, textStyle: { color: textColor }, icon: 'roundRect', itemWidth: 14, itemHeight: 8 },
    grid: { left: '2%', right: '4%', bottom: '2%', top: 40, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map((d: any) => d.date.slice(5)),
      axisLine: { lineStyle: { color: axisColor } },
      axisTick: { show: false },
      axisLabel: { color: textColor },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: axisColor, type: 'dashed', opacity: 0.5 } },
      axisLabel: { color: textColor },
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: false,
        data: data.map((d: any) => d.income),
        itemStyle: { color: '#10b981' },
        lineStyle: { width: 3, color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16,185,129,0.35)' },
            { offset: 1, color: 'rgba(16,185,129,0.02)' },
          ]),
        },
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: false,
        data: data.map((d: any) => d.expense),
        itemStyle: { color: '#f43f5e' },
        lineStyle: { width: 3, color: '#f43f5e' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(244,63,94,0.35)' },
            { offset: 1, color: 'rgba(244,63,94,0.02)' },
          ]),
        },
      },
    ],
  })
}

// 渲染支出分类饼图（环形 + 中心总额）
const renderPieChart = () => {
  const el = document.getElementById('pieChart')
  if (!el || !summary.value.expenseByCategory) return
  if (!pieChart) pieChart = echarts.init(el)
  const data = summary.value.expenseByCategory
  const total = data.reduce((s: number, d: any) => s + Number(d.amount || 0), 0)
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: isDark() ? 'rgba(15,21,33,0.95)' : 'rgba(255,255,255,0.95)',
      borderColor: 'transparent',
      textStyle: { color: isDark() ? '#e2e8f0' : '#334155' },
      padding: [10, 14],
      borderRadius: 12,
      formatter: '{b}<br/>¥{c} ({d}%)',
    },
    legend: { bottom: 0, left: 'center', textStyle: { color: isDark() ? '#94a3b8' : '#64748b' }, icon: 'circle', itemWidth: 8, itemHeight: 8 },
    series: [
      {
        type: 'pie',
        radius: ['52%', '74%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 10, borderColor: isDark() ? '#0f1521' : '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 15, fontWeight: 'bold', color: isDark() ? '#e2e8f0' : '#334155', formatter: '{b}\n{d}%' } },
        data: data.map((d: any) => ({
          name: d.icon + ' ' + d.name,
          value: d.amount,
          itemStyle: { color: d.color },
        })),
      },
      {
        type: 'pie',
        radius: ['0%', '0%'],
        center: ['50%', '44%'],
        silent: true,
        label: {
          show: true,
          position: 'center',
          formatter: () => `{a|总支出}\n{b|¥${total.toFixed(0)}}`,
          rich: {
            a: { color: isDark() ? '#94a3b8' : '#94a3b8', fontSize: 12, padding: [0, 0, 6, 0] },
            b: { color: isDark() ? '#f1f5f9' : '#1e293b', fontSize: 22, fontWeight: 'bold' },
          },
        },
        data: [{ value: 1 }],
      },
    ],
  })
}

const renderCharts = () => {
  renderTrendChart()
  renderPieChart()
}

const handleExport = async () => {
  try {
    const blob = await dashboardApi.exportExcel()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `transactions.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    alert('导出失败')
  }
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

// 跟随主题切换重绘图表
const observer = new MutationObserver(() => {
  if (!loading.value) renderCharts()
})
onMounted(() => {
  fetchSummary()
  fetchCategories()
  window.addEventListener('resize', handleResize)
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  observer.disconnect()
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 资金总览：只保留当前现金、净资产和现金流主指标 -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-4">
      <div class="relative overflow-hidden rounded-2xl bg-brand-gradient p-6 text-white shadow-glow lg:col-span-2 animate-fade-up">
        <div class="absolute -right-8 -top-8 h-32 w-32 rounded-full bg-white/15 blur-2xl"></div>
        <div class="relative flex items-center justify-between"><span class="text-sm text-white/80">可用资金</span><span class="text-2xl">🏦</span></div>
        <p class="relative mt-3 tabular text-4xl font-bold">¥{{ Number(summary.availableCash ?? summary.totalAssets ?? 0).toFixed(2) }}</p>
        <p class="relative mt-2 text-xs text-white/70">当前所有账户余额，可直接支配</p>
      </div>
      <div class="card card-hover animate-fade-up" style="animation-delay: 60ms">
        <div class="flex items-center justify-between"><span class="text-sm text-slate-500 dark:text-slate-400">本月现金流</span><span class="text-2xl">💵</span></div>
        <p :class="Number(summary.cashFlow || 0) >= 0 ? 'text-income-500' : 'text-expense-500'" class="mt-3 tabular text-2xl font-bold">¥{{ Number(summary.cashFlow || 0).toFixed(2) }}</p>
        <p class="mt-2 text-xs text-slate-400">收入 - 支出</p>
      </div>
      <div class="card card-hover animate-fade-up" style="animation-delay: 120ms">
        <div class="flex items-center justify-between"><span class="text-sm text-slate-500 dark:text-slate-400">净资产</span><span class="text-2xl">📊</span></div>
        <p :class="Number(summary.netWorth || 0) >= 0 ? 'text-primary-500' : 'text-expense-500'" class="mt-3 tabular text-2xl font-bold">¥{{ Number(summary.netWorth || 0).toFixed(2) }}</p>
        <p class="mt-2 text-xs text-slate-400">现金 + 固定资产 - 负债</p>
      </div>
    </div>
    <div class="grid grid-cols-1 gap-3 sm:grid-cols-3 animate-fade-up" style="animation-delay: 140ms">
      <div class="rounded-xl border border-primary-500/10 bg-primary-500/5 px-4 py-3"><p class="text-xs text-slate-400">固定资产估值</p><p class="mt-1 tabular font-semibold text-slate-700 dark:text-slate-200">¥{{ Number(summary.fixedAssetValue || 0).toFixed(2) }}</p></div>
      <div class="rounded-xl border border-income-500/10 bg-income-500/5 px-4 py-3"><p class="text-xs text-slate-400">可变卖资产</p><p class="mt-1 tabular font-semibold text-income-500">¥{{ Number(summary.liquidatableAssetValue || 0).toFixed(2) }}</p></div>
      <div class="rounded-xl border border-slate-200/60 bg-white/50 px-4 py-3 dark:border-white/5 dark:bg-white/5"><p class="text-xs text-slate-400">持有资产</p><p class="mt-1 tabular font-semibold text-slate-700 dark:text-slate-200">{{ Number(summary.assetCount || 0) }} 项</p></div>
    </div>

    <!-- 经营收支：收入、支出、结余放在同一张卡里 -->
    <div class="card animate-fade-up" style="animation-delay: 160ms">
      <div class="mb-4 flex items-center justify-between"><h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">本月经营收支</h3><span class="text-xs text-slate-400">月初可用资金 ¥{{ Number(summary.cashAtMonthStart || 0).toFixed(2) }}</span></div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div class="rounded-xl bg-income-500/10 p-4"><p class="text-xs text-slate-500 dark:text-slate-400">收入</p><p class="mt-2 tabular text-xl font-bold text-income-500">¥{{ Number(summary.monthIncome || 0).toFixed(2) }}</p></div>
        <div class="rounded-xl bg-expense-500/10 p-4"><p class="text-xs text-slate-500 dark:text-slate-400">支出</p><p class="mt-2 tabular text-xl font-bold text-expense-500">¥{{ Number(summary.monthExpense || 0).toFixed(2) }}</p></div>
        <div class="rounded-xl bg-primary-500/10 p-4"><p class="text-xs text-slate-500 dark:text-slate-400">结余</p><p :class="Number(summary.monthBalance || 0) >= 0 ? 'text-primary-500' : 'text-expense-500'" class="mt-2 tabular text-xl font-bold">¥{{ Number(summary.monthBalance || 0).toFixed(2) }}</p></div>
      </div>
    </div>

    <!-- 待收待还：把项目回款和贷款还款放在同一块 -->
    <div class="card animate-fade-up" style="animation-delay: 220ms">
      <div class="mb-4 flex items-center justify-between"><h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">待收待还</h3><span class="text-xs text-slate-400">影响近期资金安排</span></div>
      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div class="rounded-2xl border border-income-500/15 bg-income-500/5 p-5">
          <div class="flex items-center justify-between"><div><p class="text-xs text-slate-500 dark:text-slate-400">项目回款</p><p class="mt-2 tabular text-2xl font-bold text-income-500">¥{{ Number(summary.projectPending || 0).toFixed(2) }}</p><p class="mt-1 text-xs text-slate-400">待收款 · 已收 ¥{{ Number(summary.projectReceived || 0).toFixed(2) }}</p></div><span class="text-3xl">📁</span></div>
          <div class="mt-4 flex items-center justify-between text-xs text-slate-400"><span>合同总额 ¥{{ Number(summary.projectContractValue || 0).toFixed(2) }}</span><span>{{ Number(summary.projectContractValue || 0) > 0 ? ((Number(summary.projectReceived || 0) / Number(summary.projectContractValue || 1)) * 100).toFixed(1) : '0.0' }}% 已回款</span></div>
          <div class="mt-2 h-2 overflow-hidden rounded-full bg-white/70 dark:bg-white/10"><div class="h-full rounded-full bg-income-gradient" :style="{ width: `${Math.min(100, Math.max(0, Number(summary.projectContractValue || 0) > 0 ? (Number(summary.projectReceived || 0) / Number(summary.projectContractValue)) * 100 : 0))}%` }"></div></div>
        </div>
        <div class="rounded-2xl border border-expense-500/15 bg-expense-500/5 p-5">
          <div class="flex items-center justify-between"><div><p class="text-xs text-slate-500 dark:text-slate-400">贷款还款</p><p class="mt-2 tabular text-2xl font-bold text-expense-500">¥{{ Number(summary.loanMonthlyDue || 0) .toFixed(2) }}</p><p class="mt-1 text-xs text-slate-400">本月应还 · 已还 ¥{{ Number(summary.loanMonthlyPaid || 0).toFixed(2) }}</p></div><span class="text-3xl">🏠</span></div>
          <div class="mt-4 flex items-center justify-between text-xs text-slate-400"><span>剩余本金 ¥{{ Number(summary.loanRemainingPrincipal || 0).toFixed(2) }}</span><span>{{ summary.loanCount || 0 }} 笔贷款</span></div>
          <div class="mt-2 h-2 overflow-hidden rounded-full bg-white/70 dark:bg-white/10"><div class="h-full rounded-full bg-expense-gradient" :style="{ width: `${Math.min(100, Math.max(0, Number(summary.loanMonthlyDue || 0) > 0 ? (Number(summary.loanMonthlyPaid || 0) / Number(summary.loanMonthlyDue)) * 100 : 0))}%` }"></div></div>
        </div>
      </div>
      <div class="mt-4 flex flex-wrap gap-x-6 gap-y-2 border-t border-slate-100 pt-4 text-xs text-slate-400 dark:border-white/5"><span>总负债 ¥{{ Number(summary.totalDebt || 0).toFixed(2) }}</span><span>已还利息 ¥{{ Number(summary.loanPaidInterest || 0).toFixed(2) }}</span><span>本月项目回款 ¥{{ Number(summary.projectCurrentMonthReceived || 0).toFixed(2) }}</span></div>
    </div>

    <!-- 贷款明细：有贷款时才显示 -->
    <div v-if="summary.loanSummary?.length" class="card animate-fade-up" style="animation-delay: 260ms">
      <div class="mb-4 flex items-center justify-between"><h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">贷款进度明细</h3><span class="text-xs text-slate-400">{{ Number(summary.loanProgress || 0).toFixed(1) }}% 总体已偿还</span></div>
      <div class="space-y-4"><div v-for="loan in summary.loanSummary" :key="loan.id"><div class="mb-1.5 flex items-center justify-between gap-3 text-sm"><span class="truncate font-medium text-slate-700 dark:text-slate-200">{{ loan.name }}</span><span class="shrink-0 tabular text-xs text-slate-400">剩余 ¥{{ Number(loan.remainingPrincipal || 0).toFixed(2) }} · 月供 ¥{{ Number(loan.monthlyPayment || 0).toFixed(2) }}</span></div><div class="h-2 overflow-hidden rounded-full bg-slate-100 dark:bg-white/10"><div class="h-full rounded-full bg-brand-gradient transition-all" :style="{ width: `${Math.min(100, Math.max(0, Number(loan.progress || 0)))}%` }"></div></div></div></div>
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="card animate-fade-up" style="animation-delay: 280ms">
        <h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-4">近7天收支趋势</h3>
        <div id="trendChart" style="height: 300px"></div>
      </div>
      <div class="card animate-fade-up" style="animation-delay: 320ms">
        <h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-4">支出分类占比</h3>
        <div id="pieChart" style="height: 300px"></div>
      </div>
    </div>

    <!-- 最近交易 -->
    <div class="card animate-fade-up" style="animation-delay: 360ms">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-sm font-semibold text-slate-700 dark:text-slate-200">最近交易</h3>
        <button
          @click="handleExport"
          class="px-3 py-1.5 text-xs btn-ghost"
        >
          导出 Excel
        </button>
      </div>
      <div v-if="summary.recentTransactions?.length === 0" class="text-center py-10 text-slate-400">
        暂无交易记录
      </div>
      <div v-else class="space-y-2">
        <div
          v-for="t in summary.recentTransactions"
          :key="t.id"
          class="flex items-center justify-between py-3 px-3 rounded-xl hover:bg-slate-50 dark:hover:bg-white/5 transition-colors"
        >
          <div class="flex items-center gap-3">
            <span
              :class="t.type === 'INCOME' ? 'bg-income-500/12' : 'bg-expense-500/12'"
              class="w-11 h-11 rounded-full flex items-center justify-center text-lg"
            >
              {{ categories.find(c => c.id === t.categoryId)?.icon || '💰' }}
            </span>
            <div>
              <p class="text-sm font-medium text-slate-700 dark:text-slate-200">
                {{ categories.find(c => c.id === t.categoryId)?.name || '未知' }}
              </p>
              <p class="text-xs text-slate-400">{{ t.date }}</p>
            </div>
          </div>
          <span
            :class="t.type === 'INCOME' ? 'text-income-500' : 'text-expense-500'"
            class="font-semibold tabular"
          >
            {{ t.type === 'INCOME' ? '+' : '-' }}¥{{ Number(t.amount).toFixed(2) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
