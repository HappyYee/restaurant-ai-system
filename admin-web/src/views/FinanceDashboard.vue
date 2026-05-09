<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { use } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { Coin, DishDot, Money, Refresh, Search, TrendCharts, UserFilled, Wallet } from '@element-plus/icons-vue'
import { fetchFinanceOverview } from '../api/admin'

use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const loading = ref(true)
const overview = ref(null)
const query = reactive({
  year: 'all',
  monthRange: [],
  revenueType: 'all',
})

const revenueTypes = [
  { label: '全部收入', value: 'all' },
  { label: '堂食收银', value: 'dineIn' },
  { label: '小程序点餐', value: 'miniapp' },
  { label: 'AI 点餐', value: 'aiOrder' },
  { label: '外卖渠道', value: 'delivery' },
]

const yuan = (value) => `¥${Number(value || 0).toFixed(2)}`
const wan = (value) => `${(Number(value || 0) / 10000).toFixed(1)}万`
const percent = (value) => `${Number(value || 0)}%`
const moneyAxis = (value) => `${Number(value || 0) / 10000}万`

const selectedRevenueName = computed(
  () => revenueTypes.find((item) => item.value === query.revenueType)?.label || '全部收入',
)

const selectedPeriodText = computed(() => {
  if (query.monthRange?.length === 2) {
    return `${query.monthRange[0]} 至 ${query.monthRange[1]}`
  }
  if (query.year !== 'all') {
    return `${query.year} 年`
  }
  return '全部历史月份'
})

const financeCards = computed(() => {
  const summary = overview.value?.summary || {}
  return [
    {
      title: '查询收入',
      value: yuan(summary.selectedRevenue),
      note: `${selectedRevenueName.value} · ${selectedPeriodText.value}`,
      icon: Coin,
    },
    {
      title: '总营业收入',
      value: yuan(summary.totalRevenue),
      note: `所有收入渠道合计`,
      icon: TrendCharts,
    },
    {
      title: '总成本',
      value: yuan(summary.totalCost),
      note: `成本率 ${percent(summary.costRate)}`,
      icon: Wallet,
    },
    {
      title: '净利润',
      value: yuan(summary.profit),
      note: `净利率 ${percent(summary.profitRate)}`,
      icon: Money,
    },
    {
      title: '人力成本',
      value: yuan(summary.laborCost),
      note: `占收入 ${percent(summary.laborCostRate)}`,
      icon: UserFilled,
    },
    {
      title: '食材成本',
      value: yuan(summary.foodCost),
      note: `占收入 ${percent(summary.foodCostRate)}`,
      icon: DishDot,
    },
  ]
})

const monthlyRevenueOption = computed(() => ({
  color: ['#2f6fed', '#1f7a5c', '#d97706', '#b42318'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { top: 44, right: 20, bottom: 34, left: 56 },
  xAxis: {
    type: 'category',
    data: overview.value?.monthlyTrend.map((item) => item.month) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', axisLabel: { formatter: moneyAxis } },
  series: [
    {
      name: selectedRevenueName.value,
      type: 'bar',
      barWidth: 14,
      data: overview.value?.monthlyTrend.map((item) => item.selectedRevenue) || [],
    },
    {
      name: '总营业收入',
      type: 'line',
      smooth: true,
      data: overview.value?.monthlyTrend.map((item) => item.revenue) || [],
    },
    {
      name: '总成本',
      type: 'line',
      smooth: true,
      data: overview.value?.monthlyTrend.map((item) => item.totalCost) || [],
    },
    {
      name: '净利润',
      type: 'line',
      smooth: true,
      data: overview.value?.monthlyTrend.map((item) => item.profit) || [],
    },
  ],
}))

const annualFinanceOption = computed(() => ({
  color: ['#1f7a5c', '#d97706', '#2f6fed'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { top: 44, right: 20, bottom: 34, left: 56 },
  xAxis: {
    type: 'category',
    data: overview.value?.annualTrend.map((item) => item.year) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', axisLabel: { formatter: moneyAxis } },
  series: [
    {
      name: '营业收入',
      type: 'line',
      smooth: true,
      data: overview.value?.annualTrend.map((item) => item.revenue) || [],
    },
    {
      name: '总成本',
      type: 'line',
      smooth: true,
      data: overview.value?.annualTrend.map((item) => item.totalCost) || [],
    },
    {
      name: '净利润',
      type: 'bar',
      barWidth: 24,
      data: overview.value?.annualTrend.map((item) => item.profit) || [],
    },
  ],
}))

const revenueStructureOption = computed(() => ({
  color: ['#1f7a5c', '#2f6fed', '#d97706', '#b42318'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '收入来源',
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['50%', '42%'],
      data: overview.value?.summary.revenueStructure || [],
    },
  ],
}))

const costStructureOption = computed(() => ({
  color: ['#1f7a5c', '#2f6fed', '#d97706', '#b42318', '#57534e', '#0f766e', '#7c3aed', '#c2410c', '#4b5563', '#0891b2'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '门店成本',
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['50%', '42%'],
      data: overview.value?.summary.costStructure || [],
    },
  ],
}))

const laborStructureOption = computed(() => ({
  color: ['#2f6fed', '#1f7a5c', '#d97706'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '人力成本',
      type: 'pie',
      radius: ['44%', '70%'],
      center: ['50%', '42%'],
      data: overview.value?.summary.laborStructure || [],
    },
  ],
}))

const laborHistoryOption = computed(() => ({
  color: ['#2f6fed', '#1f7a5c', '#d97706', '#b42318'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { top: 44, right: 20, bottom: 34, left: 56 },
  xAxis: {
    type: 'category',
    data: overview.value?.laborMonthlyTrend.map((item) => item.month) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', axisLabel: { formatter: moneyAxis } },
  series: [
    {
      name: '店长成本',
      type: 'line',
      smooth: true,
      data: overview.value?.laborMonthlyTrend.map((item) => item.managerLabor) || [],
    },
    {
      name: '员工成本',
      type: 'line',
      smooth: true,
      data: overview.value?.laborMonthlyTrend.map((item) => item.employeeLabor) || [],
    },
    {
      name: '兼职成本',
      type: 'line',
      smooth: true,
      data: overview.value?.laborMonthlyTrend.map((item) => item.partTimeLabor) || [],
    },
    {
      name: '人力合计',
      type: 'bar',
      barWidth: 12,
      data: overview.value?.laborMonthlyTrend.map((item) => item.laborCost) || [],
    },
  ],
}))

async function loadData() {
  loading.value = true
  overview.value = await fetchFinanceOverview({
    year: query.year,
    monthRange: query.monthRange || [],
    revenueType: query.revenueType,
  })
  loading.value = false
}

function resetQuery() {
  query.year = 'all'
  query.monthRange = []
  query.revenueType = 'all'
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading" class="page-stack">
    <section class="toolbar panel finance-query">
      <el-select v-model="query.year" placeholder="年份">
        <el-option label="全部年份" value="all" />
        <el-option v-for="year in overview?.yearOptions || []" :key="year" :label="`${year} 年`" :value="year" />
      </el-select>
      <el-date-picker
        v-model="query.monthRange"
        type="monthrange"
        range-separator="至"
        start-placeholder="开始月份"
        end-placeholder="结束月份"
        value-format="YYYY-MM"
      />
      <el-select v-model="query.revenueType" placeholder="收入渠道">
        <el-option v-for="item in revenueTypes" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </section>

    <section class="metric-grid finance-metrics">
      <article v-for="card in financeCards" :key="card.title" class="metric-card">
        <div class="metric-icon">
          <el-icon><component :is="card.icon" /></el-icon>
        </div>
        <div>
          <span>{{ card.title }}</span>
          <strong>{{ card.value }}</strong>
          <p>{{ card.note }}</p>
        </div>
      </article>
    </section>

    <section class="content-grid two-cols">
      <div class="panel">
        <div class="panel-header">
          <div>
            <h2>营业收入自定义查询</h2>
            <p>{{ selectedPeriodText }} · {{ selectedRevenueName }}</p>
          </div>
        </div>
        <VChart class="chart large-chart" :option="monthlyRevenueOption" autoresize />
      </div>
      <div class="panel">
        <div class="panel-header">
          <h2>收入来源构成</h2>
        </div>
        <VChart class="chart large-chart" :option="revenueStructureOption" autoresize />
      </div>
    </section>

    <section class="content-grid three-cols">
      <div class="panel">
        <div class="panel-header">
          <h2>门店成本构成</h2>
        </div>
        <VChart class="chart" :option="costStructureOption" autoresize />
      </div>
      <div class="panel">
        <div class="panel-header">
          <h2>人力成本构成</h2>
        </div>
        <VChart class="chart" :option="laborStructureOption" autoresize />
      </div>
      <div class="panel">
        <div class="panel-header">
          <h2>年度财务总览</h2>
        </div>
        <VChart class="chart" :option="annualFinanceOption" autoresize />
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h2>人力成本历史</h2>
          <p>按店长、正式员工、兼职人员拆分，可查看任意年份或月份范围。</p>
        </div>
      </div>
      <VChart class="chart large-chart" :option="laborHistoryOption" autoresize />
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>月度财务流水明细</h2>
      </div>
      <el-table :data="overview?.monthlyDetails || []" row-key="month">
        <el-table-column prop="month" label="月份" width="100" fixed />
        <el-table-column label="堂食收入"><template #default="{ row }">{{ yuan(row.revenue.dineIn) }}</template></el-table-column>
        <el-table-column label="小程序收入"><template #default="{ row }">{{ yuan(row.revenue.miniapp) }}</template></el-table-column>
        <el-table-column label="AI 点餐收入"><template #default="{ row }">{{ yuan(row.revenue.aiOrder) }}</template></el-table-column>
        <el-table-column label="外卖收入"><template #default="{ row }">{{ yuan(row.revenue.delivery) }}</template></el-table-column>
        <el-table-column label="营业收入"><template #default="{ row }">{{ yuan(row.totalRevenue) }}</template></el-table-column>
        <el-table-column label="食材成本"><template #default="{ row }">{{ yuan(row.foodCost) }}</template></el-table-column>
        <el-table-column label="人力成本"><template #default="{ row }">{{ yuan(row.laborCost) }}</template></el-table-column>
        <el-table-column label="运营成本"><template #default="{ row }">{{ yuan(row.operatingCost) }}</template></el-table-column>
        <el-table-column label="总成本"><template #default="{ row }">{{ yuan(row.totalCost) }}</template></el-table-column>
        <el-table-column label="净利润"><template #default="{ row }">{{ yuan(row.profit) }}</template></el-table-column>
        <el-table-column label="净利率"><template #default="{ row }">{{ percent(row.profitRate) }}</template></el-table-column>
      </el-table>
    </section>

    <section class="content-grid two-cols">
      <div class="panel">
        <div class="panel-header">
          <h2>年度财务明细</h2>
        </div>
        <el-table :data="overview?.allAnnualSummary || []" row-key="year">
          <el-table-column prop="year" label="年份" width="90" />
          <el-table-column label="营业收入"><template #default="{ row }">{{ yuan(row.revenue) }}</template></el-table-column>
          <el-table-column label="总成本"><template #default="{ row }">{{ yuan(row.totalCost) }}</template></el-table-column>
          <el-table-column label="净利润"><template #default="{ row }">{{ yuan(row.profit) }}</template></el-table-column>
          <el-table-column label="净利率"><template #default="{ row }">{{ percent(row.profitRate) }}</template></el-table-column>
        </el-table>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h2>年度人力成本明细</h2>
        </div>
        <el-table :data="overview?.laborAnnualTrend || []" row-key="year">
          <el-table-column prop="year" label="年份" width="90" />
          <el-table-column label="店长成本"><template #default="{ row }">{{ yuan(row.managerLabor) }}</template></el-table-column>
          <el-table-column label="员工成本"><template #default="{ row }">{{ yuan(row.employeeLabor) }}</template></el-table-column>
          <el-table-column label="兼职成本"><template #default="{ row }">{{ yuan(row.partTimeLabor) }}</template></el-table-column>
          <el-table-column label="人力合计"><template #default="{ row }">{{ yuan(row.laborCost) }}</template></el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>
