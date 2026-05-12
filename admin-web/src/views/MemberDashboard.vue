<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Refresh, Search, UserFilled } from '@element-plus/icons-vue'
import { fetchMembers, fetchMemberStats } from '../api/admin'

const loading = ref(false)
const members = ref([])
const stats = ref(null)
const filters = reactive({
  keyword: '',
  level: '',
})

const levelOptions = ['普通会员', '银卡会员', '金卡会员']

const levelRows = computed(() => {
  const distribution = stats.value?.levelDistribution || {}
  const total = Number(stats.value?.memberCount || 0)
  return levelOptions.map((level) => {
    const count = Number(distribution[level] || 0)
    return {
      level,
      count,
      percentage: total ? Math.round((count / total) * 100) : 0,
    }
  })
})

const visibleMembers = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return members.value.filter((member) => {
    const matchKeyword =
      !keyword ||
      member.nickname?.toLowerCase().includes(keyword) ||
      String(member.userId).includes(keyword) ||
      member.memberLevel?.toLowerCase().includes(keyword)
    const matchLevel = !filters.level || member.memberLevel === filters.level
    return matchKeyword && matchLevel
  })
})

const topMembers = computed(() =>
  visibleMembers.value
    .slice()
    .sort((a, b) => Number(b.totalSpent || 0) - Number(a.totalSpent || 0))
    .slice(0, 6),
)

function money(value) {
  return Number(value || 0).toFixed(2)
}

function levelTag(level) {
  if (level === '金卡会员') return 'warning'
  if (level === '银卡会员') return 'success'
  return 'info'
}

async function loadData() {
  loading.value = true
  try {
    const [memberList, memberStats] = await Promise.all([fetchMembers(filters), fetchMemberStats()])
    members.value = memberList
    stats.value = memberStats
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-stack">
    <section class="panel member-hero">
      <div class="member-copy">
        <div class="panel-kicker">
          <el-icon><UserFilled /></el-icon>
          会员运营
        </div>
        <h2>把点餐数据变成复购资产</h2>
        <p>查看会员规模、等级分布、累计消费和可运营人群，后续可继续接优惠券、储值和精准推荐。</p>
      </div>
      <div class="member-metrics">
        <div>
          <span>会员总数</span>
          <strong>{{ stats?.memberCount || 0 }}</strong>
        </div>
        <div>
          <span>会员累计消费</span>
          <strong>¥{{ money(stats?.totalMemberSpent) }}</strong>
        </div>
        <div>
          <span>人均消费</span>
          <strong>¥{{ money(stats?.avgMemberSpent) }}</strong>
        </div>
        <div>
          <span>累计积分</span>
          <strong>{{ stats?.totalPoints || 0 }}</strong>
        </div>
      </div>
    </section>

    <section class="member-grid">
      <div class="panel">
        <div class="panel-header">
          <h2>等级构成</h2>
          <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
        </div>
        <div class="level-list">
          <div v-for="item in levelRows" :key="item.level" class="level-row">
            <div class="level-title">
              <span>{{ item.level }}</span>
              <strong>{{ item.count }} 人</strong>
            </div>
            <el-progress :percentage="item.percentage" :stroke-width="10" />
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h2>高价值会员</h2>
        </div>
        <div class="rank-list">
          <div v-for="member in topMembers" :key="member.userId" class="rank-row">
            <div>
              <strong>{{ member.nickname || '微信用户' }}</strong>
              <span>{{ member.memberLevel }} · {{ member.points || 0 }} 积分</span>
            </div>
            <b>¥{{ money(member.totalSpent) }}</b>
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header table-tools">
        <div>
          <h2>会员明细</h2>
          <p>支持按昵称、编号、等级查询，便于后续做会员营销和 AI 推荐画像。</p>
        </div>
        <div class="filter-row">
          <el-input v-model="filters.keyword" :prefix-icon="Search" clearable placeholder="搜索会员" />
          <el-select v-model="filters.level" clearable placeholder="等级">
            <el-option v-for="level in levelOptions" :key="level" :label="level" :value="level" />
          </el-select>
        </div>
      </div>

      <el-table :data="visibleMembers" v-loading="loading">
        <el-table-column prop="userId" label="编号" width="90" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column label="等级" width="120">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.memberLevel)">{{ row.memberLevel || '普通会员' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="140">
          <template #default="{ row }">¥{{ money(row.totalSpent) }}</template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column label="距下级目标" width="150">
          <template #default="{ row }">
            <span>{{ Number(row.nextLevelNeed || 0) === 0 ? '已达最高等级' : `还差 ¥${money(row.nextLevelNeed)}` }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberSince" label="入会时间" min-width="170" />
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.member-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 0.8fr);
  gap: 24px;
  align-items: center;
}

.member-copy h2 {
  margin: 10px 0;
  font-size: 26px;
}

.member-copy p,
.panel-header p {
  margin: 6px 0 0;
  color: var(--text-muted);
}

.panel-kicker {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--primary);
  font-weight: 700;
}

.member-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.member-metrics div {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #f8fafc;
}

.member-metrics span,
.rank-row span {
  display: block;
  color: var(--text-muted);
  font-size: 13px;
}

.member-metrics strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
}

.member-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 18px;
}

.level-list,
.rank-list {
  display: grid;
  gap: 14px;
}

.level-title,
.rank-row,
.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.rank-row {
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.rank-row:last-child {
  border-bottom: 0;
}

.rank-row b {
  color: var(--primary);
}

.table-tools {
  align-items: flex-end;
}

.filter-row {
  width: 420px;
}

@media (max-width: 1080px) {
  .member-hero,
  .member-grid {
    grid-template-columns: 1fr;
  }

  .filter-row {
    width: 100%;
  }
}
</style>
