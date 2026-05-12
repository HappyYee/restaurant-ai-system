<script setup>
import { computed, onMounted, ref } from 'vue'
import { use } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { Box, Coin, DishDot, Money, Tickets, UserFilled, Wallet } from '@element-plus/icons-vue'
import { fetchDashboardStats } from '../api/admin'

use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const loading = ref(true)
const stats = ref(null)
const detailVisible = ref(false)
const activeDetail = ref('revenue')
const yuan = (value) => `¥${Number(value || 0).toFixed(2)}`
const wan = (value) => `${(Number(value || 0) / 10000).toFixed(1)}万`

const metricCards = computed(() => [
  {
    title: '今日营业额',
    value: yuan(stats.value?.revenueToday),
    note: `较昨日 ${stats.value?.revenueChange || 0}% · 客单价 ${yuan(stats.value?.avgTicket)}`,
    icon: Coin,
    detail: 'revenue',
  },
  {
    title: '今日餐品毛利',
    value: yuan(stats.value?.grossProfitToday),
    note: `食材成本率 ${stats.value?.foodCostRate || 0}%`,
    icon: DishDot,
    detail: 'profit',
  },
  {
    title: '今日净利润',
    value: yuan(stats.value?.netProfitToday),
    note: `净利率 ${stats.value?.profitMargin || 0}%`,
    icon: Money,
    detail: 'profit',
  },
  {
    title: '今日人力成本',
    value: yuan(stats.value?.laborCostToday),
    note: `在职 ${stats.value?.staffCount || 0} 人 · 本月 ${yuan(stats.value?.laborCostMonth)}`,
    icon: UserFilled,
    detail: 'labor',
  },
  {
    title: '本月收支',
    value: yuan(stats.value?.monthProfit),
    note: `收入 ${wan(stats.value?.monthRevenue)} · 支出 ${wan(stats.value?.monthExpense)}`,
    icon: Wallet,
    detail: 'monthly',
  },
  {
    title: '年度收支',
    value: yuan(stats.value?.yearProfit),
    note: `收入 ${wan(stats.value?.yearRevenue)} · 支出 ${wan(stats.value?.yearExpense)}`,
    icon: Coin,
    detail: 'annual',
  },
  {
    title: '今日订单',
    value: stats.value?.orderToday || 0,
    note: `待处理 ${stats.value?.pendingOrderCount || 0} 单`,
    icon: Tickets,
    detail: 'orders',
  },
  {
    title: '库存预警',
    value: stats.value?.lowStockCount || 0,
    note: '库存低于或等于 10',
    icon: Box,
    detail: 'inventory',
  },
])

const aiOrderRatio = computed(() => {
  const sources = stats.value?.sourceRevenue || []
  const total = sources.reduce((sum, item) => sum + Number(item.orderCount || 0), 0)
  const aiOrders = sources
    .filter((item) => String(item.name || '').toLowerCase().includes('ai'))
    .reduce((sum, item) => sum + Number(item.orderCount || 0), 0)
  return total ? Math.round((aiOrders / total) * 100) : 0
})

const heroStats = computed(() => [
  {
    label: 'AI 点餐占比',
    value: `${aiOrderRatio.value}%`,
    note: '订单来源结构',
  },
  {
    label: '净利率',
    value: `${stats.value?.profitMargin || 0}%`,
    note: '今日经营质量',
  },
  {
    label: '会员总数',
    value: stats.value?.memberCount || 0,
    note: '可运营资产',
  },
  {
    label: '成本率',
    value: `${stats.value?.foodCostRate || 0}%`,
    note: '食材成本控制',
  },
])

const detailTitles = {
  revenue: '营业额明细',
  profit: '餐品利润明细',
  labor: '人力成本明细',
  monthly: '月度收支明细',
  annual: '年度收支明细',
  orders: '订单明细',
  inventory: '库存预警明细',
}

const monthlyOption = computed(() => ({
  color: ['#1f7a5c', '#d97706', '#2f6fed'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { top: 44, right: 20, bottom: 34, left: 56 },
  xAxis: {
    type: 'category',
    data: stats.value?.monthlyFinance.map((item) => item.month.slice(5)) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', axisLabel: { formatter: (value) => `${value / 10000}万` } },
  series: [
    {
      name: '收入',
      type: 'bar',
      data: stats.value?.monthlyFinance.map((item) => item.revenue) || [],
      barWidth: 14,
    },
    {
      name: '支出',
      type: 'bar',
      data: stats.value?.monthlyFinance.map((item) => item.expense) || [],
      barWidth: 14,
    },
    {
      name: '利润',
      type: 'line',
      smooth: true,
      data: stats.value?.monthlyFinance.map((item) => item.profit) || [],
    },
  ],
}))

const annualOption = computed(() => ({
  color: ['#1f7a5c', '#d97706', '#2f6fed'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { top: 44, right: 20, bottom: 34, left: 56 },
  xAxis: {
    type: 'category',
    data: stats.value?.annualFinance.map((item) => item.year) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', axisLabel: { formatter: (value) => `${value / 10000}万` } },
  series: [
    {
      name: '收入',
      type: 'line',
      smooth: true,
      data: stats.value?.annualFinance.map((item) => item.revenue) || [],
    },
    {
      name: '支出',
      type: 'line',
      smooth: true,
      data: stats.value?.annualFinance.map((item) => item.expense) || [],
    },
    {
      name: '利润',
      type: 'bar',
      barWidth: 24,
      data: stats.value?.annualFinance.map((item) => item.profit) || [],
    },
  ],
}))

const revenueTrendOption = computed(() => ({
  color: ['#1f7a5c', '#2f6fed'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { top: 44, right: 18, bottom: 32, left: 48 },
  xAxis: {
    type: 'category',
    data: stats.value?.dailyRevenueTrend.map((item) => item.date) || [],
    axisTick: { show: false },
  },
  yAxis: [
    { type: 'value', axisLabel: { formatter: (value) => `${value}` } },
    { type: 'value', minInterval: 1 },
  ],
  series: [
    {
      name: '营业额',
      type: 'line',
      smooth: true,
      data: stats.value?.dailyRevenueTrend.map((item) => item.revenue) || [],
    },
    {
      name: '订单数',
      type: 'bar',
      yAxisIndex: 1,
      barWidth: 18,
      data: stats.value?.dailyRevenueTrend.map((item) => item.orderCount) || [],
    },
  ],
}))

const laborTrendOption = computed(() => ({
  color: ['#2f6fed'],
  tooltip: { trigger: 'axis' },
  grid: { top: 24, right: 18, bottom: 32, left: 54 },
  xAxis: {
    type: 'category',
    data: stats.value?.monthlyFinance.map((item) => item.month.slice(5)) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', axisLabel: { formatter: (value) => `${value / 10000}万` } },
  series: [
    {
      name: '人力成本',
      type: 'line',
      smooth: true,
      data: stats.value?.monthlyFinance.map((item) => item.laborCost) || [],
    },
  ],
}))

const profitTrendOption = computed(() => ({
  color: ['#1f7a5c', '#d97706', '#2f6fed'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { top: 44, right: 18, bottom: 32, left: 54 },
  xAxis: {
    type: 'category',
    data: stats.value?.monthlyFinance.map((item) => item.month.slice(5)) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', axisLabel: { formatter: (value) => `${value / 10000}万` } },
  series: [
    {
      name: '营业额',
      type: 'line',
      smooth: true,
      data: stats.value?.monthlyFinance.map((item) => item.revenue) || [],
    },
    {
      name: '食材成本',
      type: 'line',
      smooth: true,
      data: stats.value?.monthlyFinance.map((item) => item.foodCost) || [],
    },
    {
      name: '利润',
      type: 'bar',
      barWidth: 18,
      data: stats.value?.monthlyFinance.map((item) => item.profit) || [],
    },
  ],
}))

const costOption = computed(() => ({
  color: ['#1f7a5c', '#2f6fed', '#d97706', '#b42318', '#57534e'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '成本',
      type: 'pie',
      radius: ['44%', '70%'],
      center: ['50%', '42%'],
      data: stats.value?.costBreakdown || [],
    },
  ],
}))

function openDetail(detail) {
  activeDetail.value = detail
  detailVisible.value = true
}

const hourlyOption = computed(() => ({
  color: ['#1f7a5c'],
  tooltip: { trigger: 'axis' },
  grid: { top: 24, right: 16, bottom: 28, left: 36 },
  xAxis: {
    type: 'category',
    data: stats.value?.hourlyOrders.map((item) => item.hour) || [],
    axisTick: { show: false },
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: '订单数',
      type: 'bar',
      barWidth: 22,
      data: stats.value?.hourlyOrders.map((item) => item.count) || [],
    },
  ],
}))

const topOption = computed(() => ({
  color: ['#2f6fed', '#1f7a5c', '#d97706', '#b42318', '#57534e', '#0f766e'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '销量',
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['50%', '43%'],
      data: (stats.value?.topProducts || []).map((item) => ({
        name: item.name,
        value: item.quantity,
      })),
    },
  ],
}))

onMounted(async () => {
  stats.value = await fetchDashboardStats()
  loading.value = false
})
</script>

<template>
  <div v-loading="loading" class="page-stack">
    <section class="dashboard-hero">
      <div class="dashboard-hero-main">
        <p class="eyebrow">Restaurant Intelligence Center</p>
        <h2>星禾小馆经营中枢</h2>
        <p>把营业额、利润、人力、库存、会员和 AI 点餐数据集中到一个实时面板里，适合店长每天开门前、午高峰后和收档前快速判断。</p>
        <div class="hero-chip-row">
          <span>今日营收 {{ yuan(stats?.revenueToday) }}</span>
          <span>本月利润 {{ yuan(stats?.monthProfit) }}</span>
          <span>低库存 {{ stats?.lowStockCount || 0 }} 项</span>
        </div>
      </div>
      <div class="hero-signal-grid">
        <div v-for="item in heroStats" :key="item.label">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.note }}</small>
        </div>
      </div>
    </section>

    <section class="metric-grid finance-metrics">
      <article v-for="card in metricCards" :key="card.title" class="metric-card metric-card-clickable" @click="openDetail(card.detail)">
        <div class="metric-icon">
          <el-icon><component :is="card.icon" /></el-icon>
        </div>
        <div>
          <span>{{ card.title }}</span>
          <strong>{{ card.value }}</strong>
          <p>{{ card.note }}</p>
          <em>查看明细</em>
        </div>
      </article>
    </section>

    <section class="content-grid two-cols">
      <div class="panel">
        <div class="panel-header">
          <h2>月收支情况</h2>
        </div>
        <VChart class="chart large-chart" :option="monthlyOption" autoresize />
      </div>

      <div class="panel">
        <div class="panel-header">
          <h2>本月成本结构</h2>
        </div>
        <VChart class="chart large-chart" :option="costOption" autoresize />
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>年收支情况</h2>
      </div>
      <VChart class="chart" :option="annualOption" autoresize />
    </section>

    <section class="content-grid two-cols">
      <div class="panel">
        <div class="panel-header">
          <h2>今日订单时段分布</h2>
        </div>
        <VChart class="chart" :option="hourlyOption" autoresize />
      </div>

      <div class="panel">
        <div class="panel-header">
          <h2>热销菜品</h2>
        </div>
        <VChart class="chart" :option="topOption" autoresize />
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>库存预警</h2>
      </div>
      <el-table :data="stats?.lowStock || []" empty-text="暂无库存预警">
        <el-table-column prop="name" label="菜品" min-width="160" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="stock" label="库存" width="120" />
        <el-table-column prop="tasteTags" label="标签" min-width="220" />
      </el-table>
    </section>

    <el-dialog v-model="detailVisible" :title="detailTitles[activeDetail]" width="1040px" class="dashboard-detail-dialog">
      <template v-if="stats">
        <div v-if="activeDetail === 'revenue'" class="detail-stack">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="今日营业额">{{ yuan(stats.revenueToday) }}</el-descriptions-item>
            <el-descriptions-item label="昨日营业额">{{ yuan(stats.revenueYesterday) }}</el-descriptions-item>
            <el-descriptions-item label="今日订单">{{ stats.orderToday }} 单</el-descriptions-item>
            <el-descriptions-item label="客单价">{{ yuan(stats.avgTicket) }}</el-descriptions-item>
          </el-descriptions>
          <VChart class="chart detail-chart" :option="revenueTrendOption" autoresize />
          <div class="detail-grid">
            <el-table :data="stats.sourceRevenue" size="small">
              <el-table-column prop="name" label="订单来源" />
              <el-table-column prop="orderCount" label="订单数" />
              <el-table-column label="营业额">
                <template #default="{ row }">{{ yuan(row.revenue) }}</template>
              </el-table-column>
            </el-table>
            <el-table :data="stats.categoryRevenue" size="small">
              <el-table-column prop="name" label="菜品分类" />
              <el-table-column label="营业额">
                <template #default="{ row }">{{ yuan(row.revenue) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <div v-else-if="activeDetail === 'labor'" class="detail-stack">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="今日人力成本">{{ yuan(stats.laborCostToday) }}</el-descriptions-item>
            <el-descriptions-item label="本月人力成本">{{ yuan(stats.laborCostMonth) }}</el-descriptions-item>
            <el-descriptions-item label="在职人员">{{ stats.staffCount }} 人</el-descriptions-item>
            <el-descriptions-item label="人力成本率">{{ stats.laborCostRate }}%</el-descriptions-item>
          </el-descriptions>
          <VChart class="chart detail-chart" :option="laborTrendOption" autoresize />
          <el-table :data="stats.staffCostDetails" size="small">
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="role" label="岗位" />
            <el-table-column prop="shift" label="班次" />
            <el-table-column prop="salaryType" label="薪资类型" />
            <el-table-column prop="workHoursThisMonth" label="本月工时" />
            <el-table-column label="本月成本">
              <template #default="{ row }">{{ yuan(row.monthlyCost) }}</template>
            </el-table-column>
            <el-table-column label="日均成本">
              <template #default="{ row }">{{ yuan(row.dailyCost) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else-if="activeDetail === 'profit'" class="detail-stack">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="今日营业额">{{ yuan(stats.revenueToday) }}</el-descriptions-item>
            <el-descriptions-item label="食材成本">{{ yuan(stats.foodCostToday) }}</el-descriptions-item>
            <el-descriptions-item label="餐品毛利">{{ yuan(stats.grossProfitToday) }}</el-descriptions-item>
            <el-descriptions-item label="净利润">{{ yuan(stats.netProfitToday) }}</el-descriptions-item>
            <el-descriptions-item label="食材成本率">{{ stats.foodCostRate }}%</el-descriptions-item>
            <el-descriptions-item label="人力成本率">{{ stats.laborCostRate }}%</el-descriptions-item>
            <el-descriptions-item label="净利率">{{ stats.profitMargin }}%</el-descriptions-item>
          </el-descriptions>
          <VChart class="chart detail-chart" :option="profitTrendOption" autoresize />
          <el-table :data="stats.costBreakdown" size="small">
            <el-table-column prop="name" label="成本项" />
            <el-table-column label="本月金额">
              <template #default="{ row }">{{ yuan(row.value) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else-if="activeDetail === 'monthly'" class="detail-stack">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="本月收入">{{ yuan(stats.monthRevenue) }}</el-descriptions-item>
            <el-descriptions-item label="本月支出">{{ yuan(stats.monthExpense) }}</el-descriptions-item>
            <el-descriptions-item label="本月利润">{{ yuan(stats.monthProfit) }}</el-descriptions-item>
          </el-descriptions>
          <VChart class="chart detail-chart" :option="monthlyOption" autoresize />
          <el-table :data="stats.monthlyFinance" size="small">
            <el-table-column prop="month" label="月份" width="100" />
            <el-table-column label="营业额"><template #default="{ row }">{{ yuan(row.revenue) }}</template></el-table-column>
            <el-table-column label="食材成本"><template #default="{ row }">{{ yuan(row.foodCost) }}</template></el-table-column>
            <el-table-column label="人力成本"><template #default="{ row }">{{ yuan(row.laborCost) }}</template></el-table-column>
            <el-table-column label="固定成本"><template #default="{ row }">{{ yuan(row.fixedCost) }}</template></el-table-column>
            <el-table-column label="营销费用"><template #default="{ row }">{{ yuan(row.marketingCost) }}</template></el-table-column>
            <el-table-column label="其他费用"><template #default="{ row }">{{ yuan(row.otherCost) }}</template></el-table-column>
            <el-table-column label="利润"><template #default="{ row }">{{ yuan(row.profit) }}</template></el-table-column>
          </el-table>
        </div>

        <div v-else-if="activeDetail === 'annual'" class="detail-stack">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="年度收入">{{ yuan(stats.yearRevenue) }}</el-descriptions-item>
            <el-descriptions-item label="年度支出">{{ yuan(stats.yearExpense) }}</el-descriptions-item>
            <el-descriptions-item label="年度利润">{{ yuan(stats.yearProfit) }}</el-descriptions-item>
          </el-descriptions>
          <VChart class="chart detail-chart" :option="annualOption" autoresize />
          <el-table :data="stats.annualFinance" size="small">
            <el-table-column prop="year" label="年份" width="90" />
            <el-table-column label="营业额"><template #default="{ row }">{{ yuan(row.revenue) }}</template></el-table-column>
            <el-table-column label="食材成本"><template #default="{ row }">{{ yuan(row.foodCost) }}</template></el-table-column>
            <el-table-column label="人力成本"><template #default="{ row }">{{ yuan(row.laborCost) }}</template></el-table-column>
            <el-table-column label="总支出"><template #default="{ row }">{{ yuan(row.expense) }}</template></el-table-column>
            <el-table-column label="利润"><template #default="{ row }">{{ yuan(row.profit) }}</template></el-table-column>
          </el-table>
        </div>

        <div v-else-if="activeDetail === 'orders'" class="detail-stack">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="今日订单">{{ stats.orderToday }} 单</el-descriptions-item>
            <el-descriptions-item label="待处理">{{ stats.pendingOrderCount }} 单</el-descriptions-item>
            <el-descriptions-item label="客单价">{{ yuan(stats.avgTicket) }}</el-descriptions-item>
            <el-descriptions-item label="较昨日">{{ stats.orderChange }}%</el-descriptions-item>
          </el-descriptions>
          <VChart class="chart detail-chart" :option="hourlyOption" autoresize />
          <el-table :data="stats.topProducts" size="small">
            <el-table-column prop="name" label="热销菜品" />
            <el-table-column prop="quantity" label="销量" />
          </el-table>
        </div>

        <div v-else class="detail-stack">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="库存预警">{{ stats.lowStockCount }} 个</el-descriptions-item>
            <el-descriptions-item label="上架菜品">{{ stats.activeProductCount }} 个</el-descriptions-item>
            <el-descriptions-item label="菜品总数">{{ stats.productCount }} 个</el-descriptions-item>
          </el-descriptions>
          <el-table :data="stats.lowStock" size="small" empty-text="暂无库存预警">
            <el-table-column prop="name" label="菜品" />
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="stock" label="库存" />
            <el-table-column prop="tasteTags" label="标签" />
          </el-table>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
