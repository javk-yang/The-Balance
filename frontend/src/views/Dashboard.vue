<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
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

// 渲染近7天趋势图
const renderTrendChart = () => {
  const el = document.getElementById('trendChart')
  if (!el || !summary.value.weeklyTrend) return
  if (!trendChart) trendChart = echarts.init(el)
  const data = summary.value.weeklyTrend
  const axisColor = isDark() ? '#3f4643' : '#e7e2d8'
  const textColor = isDark() ? '#a8afa9' : '#7c817d'
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: isDark() ? '#202522' : '#ffffff',
      borderColor: isDark() ? '#3f4643' : '#e7e2d8',
      borderWidth: 1,
      textStyle: { color: isDark() ? '#f2f1ec' : '#26312c' },
      padding: [10, 14],
      borderRadius: 8,
      valueFormatter: (v: any) => '¥' + Number(v || 0).toFixed(2),
    },
    legend: { data: ['收入', '支出'], top: 0, textStyle: { color: textColor }, icon: 'roundRect', itemWidth: 14, itemHeight: 3, itemGap: 20 },
    grid: { left: '2%', right: '3%', bottom: '2%', top: 42, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map((d: any) => d.date.slice(5)),
      boundaryGap: false,
      axisLine: { lineStyle: { color: axisColor } },
      axisTick: { show: false },
      axisLabel: { color: textColor, margin: 12 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: axisColor, type: 'dashed' } },
      axisLabel: { color: textColor },
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: 0.25,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        data: data.map((d: any) => d.income),
        itemStyle: { color: '#2f6b4f' },
        lineStyle: { width: 2, color: '#2f6b4f' },
        areaStyle: { color: 'rgba(47,107,79,0.08)' },
      },
      {
        name: '支出',
        type: 'line',
        smooth: 0.25,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        data: data.map((d: any) => d.expense),
        itemStyle: { color: '#a15c45' },
        lineStyle: { width: 2, color: '#a15c45' },
        areaStyle: { color: 'rgba(161,92,69,0.07)' },
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
      backgroundColor: isDark() ? '#202522' : '#ffffff',
      borderColor: isDark() ? '#3f4643' : '#e7e2d8',
      borderWidth: 1,
      textStyle: { color: isDark() ? '#f2f1ec' : '#26312c' },
      padding: [10, 14],
      borderRadius: 8,
      formatter: '{b}<br/>¥{c} ({d}%)',
    },
    legend: { bottom: 0, left: 'center', textStyle: { color: isDark() ? '#a8afa9' : '#7c817d' }, icon: 'circle', itemWidth: 7, itemHeight: 7, itemGap: 16 },
    series: [
      {
        type: 'pie',
        radius: ['52%', '74%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 3, borderColor: isDark() ? '#202522' : '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 600, color: isDark() ? '#f2f1ec' : '#26312c', formatter: '{b}\n{d}%' } },
        data: data.map((d: any) => ({
          name: d.name,
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
            a: { color: isDark() ? '#a8afa9' : '#7c817d', fontSize: 12, padding: [0, 0, 6, 0] },
            b: { color: isDark() ? '#f2f1ec' : '#26312c', fontSize: 21, fontWeight: 600 },
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
  <div class="space-y-5 pb-4">
    <!-- 核心资金指标 -->
    <section class="overflow-hidden rounded-2xl border border-[#ded9cf] bg-white dark:border-white/10 dark:bg-[#202522]">
      <div class="grid grid-cols-1 divide-y divide-[#ebe7de] lg:grid-cols-4 lg:divide-x lg:divide-y-0 dark:divide-white/10">
        <div class="px-5 py-6 sm:px-6 lg:col-span-2 lg:py-7">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs font-medium uppercase tracking-[0.16em] text-[#7c817d] dark:text-[#a8afa9]">可用资金</p>
              <p class="mt-3 tabular text-3xl font-semibold tracking-tight text-[#26312c] sm:text-4xl dark:text-[#f2f1ec]">¥{{ Number(summary.availableCash ?? summary.totalAssets ?? 0).toFixed(2) }}</p>
              <p class="mt-2 text-xs text-[#92958f] dark:text-[#8f9691]">当前所有账户余额，可直接支配</p>
            </div>
            <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-[#dce6df] bg-[#f3f7f4] text-[#285a43] dark:border-[#3b5548] dark:bg-[#263b31] dark:text-[#91b7a2]">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4" aria-hidden="true"><path d="M3.75 8.25h16.5M5.25 8.25V18m4.5-9.75V18m4.5-9.75V18m4.5-9.75V18M3.75 18h16.5M12 3.75l8.25 4.5H3.75L12 3.75Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </span>
          </div>
        </div>
        <div class="px-5 py-5 sm:px-6 lg:py-7">
          <div class="flex items-center justify-between gap-3">
            <p class="text-xs font-medium text-[#7c817d] dark:text-[#a8afa9]">本月现金流</p>
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4 text-[#92958f]" aria-hidden="true"><path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <p :class="Number(summary.cashFlow || 0) >= 0 ? 'text-[#2f6b4f] dark:text-[#7eb495]' : 'text-[#9a543e] dark:text-[#d28a72]'" class="mt-3 tabular text-2xl font-semibold tracking-tight">¥{{ Number(summary.cashFlow || 0).toFixed(2) }}</p>
          <p class="mt-2 text-xs text-[#92958f]">收入 - 支出</p>
        </div>
        <div class="px-5 py-5 sm:px-6 lg:py-7">
          <div class="flex items-center justify-between gap-3">
            <p class="text-xs font-medium text-[#7c817d] dark:text-[#a8afa9]">净资产</p>
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4 text-[#92958f]" aria-hidden="true"><path d="M4 19.25V13m5.33 6.25V8.75m5.34 10.5V11m5.33 8.25V4.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </div>
          <p :class="Number(summary.netWorth || 0) >= 0 ? 'text-[#285a43] dark:text-[#91b7a2]' : 'text-[#9a543e] dark:text-[#d28a72]'" class="mt-3 tabular text-2xl font-semibold tracking-tight">¥{{ Number(summary.netWorth || 0).toFixed(2) }}</p>
          <p class="mt-2 text-xs text-[#92958f]">现金 + 固定资产 - 负债</p>
        </div>
      </div>
      <div class="grid grid-cols-1 border-t border-[#ebe7de] bg-[#faf9f6] sm:grid-cols-3 sm:divide-x sm:divide-[#ebe7de] dark:border-white/10 dark:bg-[#1c211e] dark:sm:divide-white/10">
        <div class="flex items-center justify-between gap-4 px-5 py-3.5 sm:block sm:px-6">
          <p class="text-xs text-[#7c817d] dark:text-[#a8afa9]">固定资产估值</p>
          <p class="tabular text-sm font-semibold text-[#39413d] sm:mt-1 dark:text-[#dedfd9]">¥{{ Number(summary.fixedAssetValue || 0).toFixed(2) }}</p>
        </div>
        <div class="flex items-center justify-between gap-4 border-t border-[#ebe7de] px-5 py-3.5 sm:block sm:border-t-0 sm:px-6 dark:border-white/10">
          <p class="text-xs text-[#7c817d] dark:text-[#a8afa9]">可变卖资产</p>
          <p class="tabular text-sm font-semibold text-[#2f6b4f] sm:mt-1 dark:text-[#7eb495]">¥{{ Number(summary.liquidatableAssetValue || 0).toFixed(2) }}</p>
        </div>
        <div class="flex items-center justify-between gap-4 border-t border-[#ebe7de] px-5 py-3.5 sm:block sm:border-t-0 sm:px-6 dark:border-white/10">
          <p class="text-xs text-[#7c817d] dark:text-[#a8afa9]">持有资产</p>
          <p class="tabular text-sm font-semibold text-[#39413d] sm:mt-1 dark:text-[#dedfd9]">{{ Number(summary.assetCount || 0) }} 项</p>
        </div>
      </div>
    </section>

    <!-- 本月经营收支 -->
    <section class="rounded-2xl border border-[#ded9cf] bg-white p-5 sm:p-6 dark:border-white/10 dark:bg-[#202522]">
      <div class="mb-5 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="text-xs font-medium uppercase tracking-[0.14em] text-[#92958f]">Monthly operations</p>
          <h2 class="mt-1 text-base font-semibold text-[#26312c] dark:text-[#f2f1ec]">本月经营收支</h2>
        </div>
        <span class="text-xs text-[#7c817d] dark:text-[#a8afa9]">月初可用资金 ¥{{ Number(summary.cashAtMonthStart || 0).toFixed(2) }}</span>
      </div>
      <div class="grid grid-cols-1 gap-px overflow-hidden rounded-xl border border-[#e7e2d8] bg-[#e7e2d8] sm:grid-cols-3 dark:border-white/10 dark:bg-white/10">
        <div class="bg-[#fbfcfa] px-5 py-4 dark:bg-[#1c211e]">
          <div class="flex items-center gap-2"><span class="h-1.5 w-1.5 rounded-full bg-[#2f6b4f]"></span><p class="text-xs text-[#6f7772] dark:text-[#a8afa9]">收入</p></div>
          <p class="mt-2 tabular text-xl font-semibold text-[#2f6b4f] dark:text-[#7eb495]">¥{{ Number(summary.monthIncome || 0).toFixed(2) }}</p>
        </div>
        <div class="bg-[#fdfaf8] px-5 py-4 dark:bg-[#211e1c]">
          <div class="flex items-center gap-2"><span class="h-1.5 w-1.5 rounded-full bg-[#a15c45]"></span><p class="text-xs text-[#6f7772] dark:text-[#a8afa9]">支出</p></div>
          <p class="mt-2 tabular text-xl font-semibold text-[#9a543e] dark:text-[#d28a72]">¥{{ Number(summary.monthExpense || 0).toFixed(2) }}</p>
        </div>
        <div class="bg-[#faf9f6] px-5 py-4 dark:bg-[#1f2421]">
          <div class="flex items-center gap-2"><span class="h-1.5 w-1.5 rounded-full bg-[#777d78]"></span><p class="text-xs text-[#6f7772] dark:text-[#a8afa9]">结余</p></div>
          <p :class="Number(summary.monthBalance || 0) >= 0 ? 'text-[#285a43] dark:text-[#91b7a2]' : 'text-[#9a543e] dark:text-[#d28a72]'" class="mt-2 tabular text-xl font-semibold">¥{{ Number(summary.monthBalance || 0).toFixed(2) }}</p>
        </div>
      </div>
    </section>

    <!-- 待收待还 -->
    <section class="rounded-2xl border border-[#ded9cf] bg-white p-5 sm:p-6 dark:border-white/10 dark:bg-[#202522]">
      <div class="mb-5 flex items-end justify-between gap-4">
        <div>
          <p class="text-xs font-medium uppercase tracking-[0.14em] text-[#92958f]">Cash commitments</p>
          <h2 class="mt-1 text-base font-semibold text-[#26312c] dark:text-[#f2f1ec]">待收待还</h2>
        </div>
        <span class="hidden text-xs text-[#7c817d] sm:block dark:text-[#a8afa9]">影响近期资金安排</span>
      </div>
      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div class="rounded-xl border border-[#dbe5de] bg-[#f8faf8] p-5 dark:border-[#385044] dark:bg-[#1d2923]">
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0">
              <p class="text-xs font-medium text-[#56705f] dark:text-[#91b7a2]">项目回款</p>
              <p class="mt-2 tabular text-2xl font-semibold tracking-tight text-[#2f6b4f] dark:text-[#7eb495]">¥{{ Number(summary.projectPending || 0).toFixed(2) }}</p>
              <p class="mt-1 text-xs text-[#7c817d] dark:text-[#a8afa9]">待收款 · 已收 ¥{{ Number(summary.projectReceived || 0).toFixed(2) }}</p>
            </div>
            <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-[#d5e1d9] text-[#2f6b4f] dark:border-[#456052] dark:text-[#91b7a2]">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4" aria-hidden="true"><path d="M3.75 7.75h6l1.5 2h9v8.5a2 2 0 0 1-2 2H5.75a2 2 0 0 1-2-2V7.75Zm0 0v-2h5l1.5 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </span>
          </div>
          <div class="mt-5 flex items-center justify-between gap-3 text-xs text-[#7c817d] dark:text-[#a8afa9]"><span>合同总额 ¥{{ Number(summary.projectContractValue || 0).toFixed(2) }}</span><span>{{ Number(summary.projectContractValue || 0) > 0 ? ((Number(summary.projectReceived || 0) / Number(summary.projectContractValue || 1)) * 100).toFixed(1) : '0.0' }}% 已回款</span></div>
          <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-[#e2e8e3] dark:bg-white/10"><div class="h-full rounded-full bg-[#2f6b4f] transition-all" :style="{ width: `${Math.min(100, Math.max(0, Number(summary.projectContractValue || 0) > 0 ? (Number(summary.projectReceived || 0) / Number(summary.projectContractValue)) * 100 : 0))}%` }"></div></div>
        </div>
        <div class="rounded-xl border border-[#eadcd6] bg-[#fcf9f7] p-5 dark:border-[#5a4037] dark:bg-[#29201d]">
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0">
              <p class="text-xs font-medium text-[#855b4c] dark:text-[#d2a08e]">贷款还款</p>
              <p class="mt-2 tabular text-2xl font-semibold tracking-tight text-[#9a543e] dark:text-[#d28a72]">¥{{ Number(summary.loanMonthlyDue || 0).toFixed(2) }}</p>
              <p class="mt-1 text-xs text-[#7c817d] dark:text-[#a8afa9]">本月应还 · 已还 ¥{{ Number(summary.loanMonthlyPaid || 0).toFixed(2) }}</p>
            </div>
            <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-[#e8d9d3] text-[#9a543e] dark:border-[#68493e] dark:text-[#d2a08e]">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4" aria-hidden="true"><path d="m3.75 10 8.25-6.25L20.25 10M5.5 9v10.25h13V9M9 19.25V14h6v5.25" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </span>
          </div>
          <div class="mt-5 flex items-center justify-between gap-3 text-xs text-[#7c817d] dark:text-[#a8afa9]"><span>剩余本金 ¥{{ Number(summary.loanRemainingPrincipal || 0).toFixed(2) }}</span><span>{{ summary.loanCount || 0 }} 笔贷款</span></div>
          <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-[#eee3de] dark:bg-white/10"><div class="h-full rounded-full bg-[#a15c45] transition-all" :style="{ width: `${Math.min(100, Math.max(0, Number(summary.loanMonthlyDue || 0) > 0 ? (Number(summary.loanMonthlyPaid || 0) / Number(summary.loanMonthlyDue)) * 100 : 0))}%` }"></div></div>
        </div>
      </div>
      <div class="mt-5 flex flex-wrap gap-x-6 gap-y-2 border-t border-[#ebe7de] pt-4 text-xs text-[#7c817d] dark:border-white/10 dark:text-[#a8afa9]"><span>总负债 ¥{{ Number(summary.totalDebt || 0).toFixed(2) }}</span><span>已还利息 ¥{{ Number(summary.loanPaidInterest || 0).toFixed(2) }}</span><span>本月项目回款 ¥{{ Number(summary.projectCurrentMonthReceived || 0).toFixed(2) }}</span></div>
    </section>

    <!-- 贷款明细 -->
    <section v-if="summary.loanSummary?.length" class="rounded-2xl border border-[#ded9cf] bg-white p-5 sm:p-6 dark:border-white/10 dark:bg-[#202522]">
      <div class="mb-5 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <h2 class="text-base font-semibold text-[#26312c] dark:text-[#f2f1ec]">贷款进度明细</h2>
        <span class="text-xs text-[#7c817d] dark:text-[#a8afa9]">{{ Number(summary.loanProgress || 0).toFixed(1) }}% 总体已偿还</span>
      </div>
      <div class="space-y-5">
        <div v-for="loan in summary.loanSummary" :key="loan.id">
          <div class="mb-2 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between sm:gap-3">
            <span class="truncate text-sm font-medium text-[#39413d] dark:text-[#dedfd9]">{{ loan.name }}</span>
            <span class="shrink-0 tabular text-xs text-[#7c817d] dark:text-[#a8afa9]">剩余 ¥{{ Number(loan.remainingPrincipal || 0).toFixed(2) }} · 月供 ¥{{ Number(loan.monthlyPayment || 0).toFixed(2) }}</span>
          </div>
          <div class="h-1.5 overflow-hidden rounded-full bg-[#ece9e2] dark:bg-white/10"><div class="h-full rounded-full bg-[#456b57] transition-all" :style="{ width: `${Math.min(100, Math.max(0, Number(loan.progress || 0)))}%` }"></div></div>
        </div>
      </div>
    </section>

    <!-- 分析图表 -->
    <div class="grid grid-cols-1 gap-5 xl:grid-cols-2">
      <section class="rounded-2xl border border-[#ded9cf] bg-white p-5 sm:p-6 dark:border-white/10 dark:bg-[#202522]">
        <div class="mb-2">
          <p class="text-xs font-medium uppercase tracking-[0.14em] text-[#92958f]">7-day movement</p>
          <h2 class="mt-1 text-base font-semibold text-[#26312c] dark:text-[#f2f1ec]">近7天收支趋势</h2>
        </div>
        <div id="trendChart" class="h-[290px] w-full sm:h-[320px]"></div>
      </section>
      <section class="rounded-2xl border border-[#ded9cf] bg-white p-5 sm:p-6 dark:border-white/10 dark:bg-[#202522]">
        <div class="mb-2">
          <p class="text-xs font-medium uppercase tracking-[0.14em] text-[#92958f]">Expense mix</p>
          <h2 class="mt-1 text-base font-semibold text-[#26312c] dark:text-[#f2f1ec]">支出分类占比</h2>
        </div>
        <div id="pieChart" class="h-[290px] w-full sm:h-[320px]"></div>
      </section>
    </div>

    <!-- 最近交易 -->
    <section class="overflow-hidden rounded-2xl border border-[#ded9cf] bg-white dark:border-white/10 dark:bg-[#202522]">
      <div class="flex items-center justify-between border-b border-[#ebe7de] px-5 py-4 sm:px-6 dark:border-white/10">
        <div>
          <p class="text-xs font-medium uppercase tracking-[0.14em] text-[#92958f]">Latest activity</p>
          <h2 class="mt-1 text-base font-semibold text-[#26312c] dark:text-[#f2f1ec]">最近交易</h2>
        </div>
        <button @click="handleExport" class="btn-ghost px-3 py-1.5 text-xs">导出 Excel</button>
      </div>
      <div v-if="summary.recentTransactions?.length === 0" class="px-5 py-12 text-center text-sm text-[#92958f]">暂无交易记录</div>
      <div v-else class="divide-y divide-[#efebe4] dark:divide-white/10">
        <div v-for="t in summary.recentTransactions" :key="t.id" class="flex items-center justify-between gap-4 px-5 py-4 transition-colors hover:bg-[#faf9f6] sm:px-6 dark:hover:bg-white/[0.03]">
          <div class="flex min-w-0 items-center gap-3">
            <span :class="t.type === 'INCOME' ? 'border-[#d7e4db] bg-[#f3f7f4] text-[#2f6b4f] dark:border-[#3b5548] dark:bg-[#263b31] dark:text-[#91b7a2]' : 'border-[#eadbd5] bg-[#fcf7f5] text-[#9a543e] dark:border-[#5a4037] dark:bg-[#382721] dark:text-[#d2a08e]'" class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border">
              <svg v-if="t.type === 'INCOME'" viewBox="0 0 24 24" fill="none" class="h-4 w-4" aria-hidden="true"><path d="M12 19V5m-5 5 5-5 5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" class="h-4 w-4" aria-hidden="true"><path d="M12 5v14m5-5-5 5-5-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </span>
            <div class="min-w-0">
              <p class="truncate text-sm font-medium text-[#39413d] dark:text-[#dedfd9]">{{ categories.find(c => c.id === t.categoryId)?.name || '未知' }}</p>
              <p class="mt-0.5 text-xs text-[#92958f]">{{ t.date }}</p>
            </div>
          </div>
          <span :class="t.type === 'INCOME' ? 'text-[#2f6b4f] dark:text-[#7eb495]' : 'text-[#9a543e] dark:text-[#d28a72]'" class="shrink-0 tabular text-sm font-semibold sm:text-base">{{ t.type === 'INCOME' ? '+' : '-' }}¥{{ Number(t.amount).toFixed(2) }}</span>
        </div>
      </div>
    </section>
  </div>
</template>
