<script setup>
import { onMounted, ref } from 'vue'
import { ChatDotRound, MagicStick, Position, Refresh } from '@element-plus/icons-vue'
import { chatBusinessAi, fetchDashboardStats } from '../api/admin'

const loading = ref(false)
const statsLoading = ref(false)
const stats = ref(null)
const sessionId = ref('')
const input = ref('今天经营情况怎么样？')
const messages = ref([])
const quickQuestions = [
  '今天营业额和利润有什么风险？',
  '会员运营下一步该做什么？',
  '哪些菜品适合做会员套餐？',
  '怎么降低人力成本但不影响出餐？',
]

async function loadStats() {
  statsLoading.value = true
  try {
    stats.value = await fetchDashboardStats()
  } finally {
    statsLoading.value = false
  }
}

async function sendQuestion(text = input.value) {
  const question = text.trim()
  if (!question || loading.value) {
    return
  }
  messages.value.push({
    role: 'user',
    content: question,
  })
  input.value = ''
  loading.value = true
  try {
    const response = await chatBusinessAi({
      message: question,
      sessionId: sessionId.value,
    })
    sessionId.value = response.sessionId
    messages.value.push({
      role: 'assistant',
      content: response.answer,
      thinking: response.thinking || [],
      actions: response.actions || [],
    })
  } finally {
    loading.value = false
  }
}

function useQuickQuestion(question) {
  input.value = question
  sendQuestion(question)
}

onMounted(() => {
  loadStats()
  sendQuestion()
})
</script>

<template>
  <div class="page-stack">
    <section class="panel ai-hero">
      <div>
        <div class="ai-kicker">
          <el-icon><MagicStick /></el-icon>
          DeepSeek 经营助手
        </div>
        <h2>随时询问门店数据，让 AI 给出经营判断</h2>
        <p>助手会读取营业额、订单、库存、会员和成本数据，返回可展示的分析依据与行动建议。</p>
      </div>
      <div class="ai-status">
        <span>当前模型</span>
        <strong>DeepSeek V4</strong>
        <small>后端未启动或 Key 未配置时自动使用本地规则兜底</small>
      </div>
    </section>

    <section class="ai-layout">
      <div class="panel chat-panel">
        <div class="panel-header">
          <div>
            <h2>经营对话</h2>
            <p>可以继续追问具体月份、成本项、菜品或会员等级。</p>
          </div>
          <el-button :icon="Refresh" :loading="loading" @click="sendQuestion('重新分析当前经营情况')">重新分析</el-button>
        </div>

        <div class="quick-row">
          <button v-for="question in quickQuestions" :key="question" type="button" @click="useQuickQuestion(question)">
            {{ question }}
          </button>
        </div>

        <div class="chat-list">
          <article v-for="(message, index) in messages" :key="index" class="chat-message" :class="message.role">
            <div class="bubble">
              <div v-if="message.role === 'assistant'" class="thinking">
                <el-icon><ChatDotRound /></el-icon>
                <span v-for="item in message.thinking" :key="item">{{ item }}</span>
              </div>
              <p>{{ message.content }}</p>
              <ul v-if="message.actions?.length" class="action-list">
                <li v-for="item in message.actions" :key="item">{{ item }}</li>
              </ul>
            </div>
          </article>
          <article v-if="loading" class="chat-message assistant">
            <div class="bubble">
              <div class="thinking active">
                <el-icon><MagicStick /></el-icon>
                <span>正在读取经营指标</span>
                <span>正在组织分析依据</span>
              </div>
              <el-skeleton :rows="2" animated />
            </div>
          </article>
        </div>

        <div class="chat-input">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            maxlength="180"
            show-word-limit
            placeholder="例如：这个月会员复购怎么提升？"
            @keyup.enter.exact.prevent="sendQuestion()"
          />
          <el-button type="primary" :icon="Position" :loading="loading" @click="sendQuestion()">发送</el-button>
        </div>
      </div>

      <aside class="panel stats-panel" v-loading="statsLoading">
        <div class="panel-header">
          <h2>实时数据快照</h2>
        </div>
        <div class="snapshot-grid">
          <div>
            <span>今日营业额</span>
            <strong>¥{{ Number(stats?.revenueToday || 0).toFixed(2) }}</strong>
          </div>
          <div>
            <span>今日订单</span>
            <strong>{{ stats?.orderToday || 0 }} 单</strong>
          </div>
          <div>
            <span>今日净利</span>
            <strong>¥{{ Number(stats?.netProfitToday || 0).toFixed(2) }}</strong>
          </div>
          <div>
            <span>本月利润</span>
            <strong>¥{{ Number(stats?.monthProfit || 0).toFixed(2) }}</strong>
          </div>
          <div>
            <span>人力成本率</span>
            <strong>{{ stats?.laborCostRate || 0 }}%</strong>
          </div>
          <div>
            <span>低库存菜品</span>
            <strong>{{ stats?.lowStockCount || 0 }} 个</strong>
          </div>
        </div>
      </aside>
    </section>
  </div>
</template>

<style scoped>
.ai-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
}

.ai-hero h2 {
  margin: 10px 0;
  font-size: 26px;
}

.ai-hero p {
  margin: 0;
  color: var(--text-muted);
}

.ai-kicker {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  color: var(--primary);
  font-weight: 700;
}

.ai-status {
  width: 260px;
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #f8fafc;
}

.ai-status span,
.ai-status small,
.snapshot-grid span {
  display: block;
  color: var(--text-muted);
  font-size: 13px;
}

.ai-status strong {
  display: block;
  margin: 8px 0;
  font-size: 22px;
}

.ai-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
  align-items: start;
}

.quick-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.quick-row button {
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 7px 12px;
  background: #ffffff;
  color: #344054;
  cursor: pointer;
}

.chat-list {
  display: grid;
  gap: 14px;
  max-height: 520px;
  overflow: auto;
  padding-right: 4px;
}

.chat-message {
  display: flex;
}

.chat-message.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 78%;
  padding: 14px 16px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: #ffffff;
}

.chat-message.user .bubble {
  color: #ffffff;
  background: var(--primary);
  border-color: var(--primary);
}

.bubble p {
  margin: 0;
  line-height: 1.7;
  white-space: pre-wrap;
}

.thinking {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
}

.thinking span {
  padding: 3px 7px;
  border-radius: 999px;
  background: #eef6f2;
}

.thinking.active span {
  background: #fff7ed;
}

.action-list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #344054;
  line-height: 1.7;
}

.chat-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 96px;
  gap: 10px;
  margin-top: 16px;
  align-items: stretch;
}

.snapshot-grid {
  display: grid;
  gap: 12px;
}

.snapshot-grid div {
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #f8fafc;
}

.snapshot-grid strong {
  display: block;
  margin-top: 8px;
  font-size: 20px;
}

@media (max-width: 1120px) {
  .ai-hero,
  .ai-layout {
    grid-template-columns: 1fr;
    display: grid;
  }

  .ai-status {
    width: 100%;
  }

  .bubble {
    max-width: 100%;
  }
}
</style>
