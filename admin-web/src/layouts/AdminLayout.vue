<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { fetchAiStatus } from '../api/admin'
import {
  Bowl,
  DataAnalysis,
  Goods,
  House,
  MagicStick,
  Menu as MenuIcon,
  Money,
  SwitchButton,
  Tickets,
  UserFilled,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const aiStatus = ref({
  provider: 'DeepSeek',
  model: 'deepseek-v4-pro',
  configured: false,
})

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '管理后台')
const aiStatusText = computed(() =>
  aiStatus.value.configured ? `${aiStatus.value.provider || 'DeepSeek'} ${aiStatus.value.model || '模型'} 已配置` : 'AI 接口待配置'
)
const todayText = new Intl.DateTimeFormat('zh-CN', {
  month: '2-digit',
  day: '2-digit',
  weekday: 'short',
}).format(new Date())

const menus = [
  { path: '/dashboard', title: '经营看板', icon: House },
  { path: '/products', title: '菜品管理', icon: Bowl },
  { path: '/orders', title: '订单管理', icon: Tickets },
  { path: '/inventory', title: '库存管理', icon: Goods },
  { path: '/staff', title: '人员管理', icon: UserFilled },
  { path: '/members', title: '会员运营', icon: UserFilled },
  { path: '/finance', title: '财务看板', icon: Money },
  { path: '/ai-analysis', title: 'AI 经营分析', icon: MagicStick },
]

function logout() {
  authStore.logout()
  router.push('/login')
}

onMounted(async () => {
  aiStatus.value = await fetchAiStatus()
})

watch(
  () => authStore.isLoggedIn,
  (isLoggedIn) => {
    if (!isLoggedIn) {
      router.push({
        name: 'login',
        query: {
          redirect: route.fullPath,
        },
      })
    }
  }
)
</script>

<template>
  <el-container class="admin-shell">
    <el-aside class="admin-sidebar" width="236px">
      <div class="brand">
        <div class="brand-mark">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div>
          <strong>星禾小馆</strong>
          <span>AI Restaurant Ops</span>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router class="side-menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-title">
          <el-icon><MenuIcon /></el-icon>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="header-actions">
          <div class="header-status" :class="{ ready: aiStatus.configured }">
            <i />
            <span>{{ aiStatusText }}</span>
          </div>
          <span class="header-date">{{ todayText }}</span>
          <span class="admin-name">{{ authStore.user?.nickname || '管理员' }}</span>
          <el-button :icon="SwitchButton" @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
