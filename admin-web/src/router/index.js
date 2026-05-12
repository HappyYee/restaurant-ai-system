import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminLayout from '../layouts/AdminLayout.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import ProductManage from '../views/ProductManage.vue'
import OrderManage from '../views/OrderManage.vue'
import InventoryManage from '../views/InventoryManage.vue'
import StaffManage from '../views/StaffManage.vue'
import FinanceDashboard from '../views/FinanceDashboard.vue'
import MemberDashboard from '../views/MemberDashboard.vue'
import AiAnalysis from '../views/AiAnalysis.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { public: true, title: '管理员登录' },
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: Dashboard,
        meta: { title: '经营看板' },
      },
      {
        path: 'products',
        name: 'products',
        component: ProductManage,
        meta: { title: '菜品管理' },
      },
      {
        path: 'orders',
        name: 'orders',
        component: OrderManage,
        meta: { title: '订单管理' },
      },
      {
        path: 'inventory',
        name: 'inventory',
        component: InventoryManage,
        meta: { title: '库存管理' },
      },
      {
        path: 'staff',
        name: 'staff',
        component: StaffManage,
        meta: { title: '人员管理' },
      },
      {
        path: 'members',
        name: 'members',
        component: MemberDashboard,
        meta: { title: '会员运营' },
      },
      {
        path: 'finance',
        name: 'finance',
        component: FinanceDashboard,
        meta: { title: '财务看板' },
      },
      {
        path: 'ai-analysis',
        name: 'ai-analysis',
        component: AiAnalysis,
        meta: { title: 'AI 经营分析' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  document.title = `${to.meta.title || '管理后台'} - 餐饮门店管理系统`
  if (!to.meta.public && !authStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && authStore.isLoggedIn) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
