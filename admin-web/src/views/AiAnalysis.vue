<script setup>
import { onMounted, ref } from 'vue'
import { MagicStick, Refresh } from '@element-plus/icons-vue'
import { fetchAiAnalysis, fetchDashboardStats } from '../api/admin'

const loading = ref(false)
const report = ref(null)
const stats = ref(null)

async function generate() {
  loading.value = true
  const [analysis, dashboardStats] = await Promise.all([fetchAiAnalysis(), fetchDashboardStats()])
  report.value = analysis
  stats.value = dashboardStats
  loading.value = false
}

onMounted(generate)
</script>

<template>
  <div class="page-stack">
    <section class="panel ai-report">
      <div class="panel-header">
        <div>
          <h2>AI 经营分析报告</h2>
          <p>当前为本地模拟报告，后端完成后可切换到 DeepSeek API 生成。</p>
        </div>
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="generate">重新生成</el-button>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated />
      <template v-else>
        <div class="report-summary">
          <el-icon><MagicStick /></el-icon>
          <strong>{{ report?.summary }}</strong>
        </div>

        <div class="report-grid">
          <article v-for="section in report?.sections" :key="section.title" class="report-block">
            <h3>{{ section.title }}</h3>
            <p>{{ section.content }}</p>
          </article>
        </div>

        <div class="suggestions">
          <h3>经营建议</h3>
          <ol>
            <li v-for="item in report?.suggestions" :key="item">{{ item }}</li>
          </ol>
        </div>
      </template>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>后端统计数据快照</h2>
      </div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="今日营业额">¥{{ (stats?.revenueToday || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="昨日营业额">¥{{ (stats?.revenueYesterday || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="今日订单">{{ stats?.orderToday || 0 }} 单</el-descriptions-item>
        <el-descriptions-item label="待处理订单">{{ stats?.pendingOrderCount || 0 }} 单</el-descriptions-item>
        <el-descriptions-item label="低库存菜品">{{ stats?.lowStockCount || 0 }} 个</el-descriptions-item>
        <el-descriptions-item label="上架菜品">{{ stats?.activeProductCount || 0 }} 个</el-descriptions-item>
      </el-descriptions>
    </section>
  </div>
</template>
