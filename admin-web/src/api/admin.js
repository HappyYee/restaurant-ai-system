import {
  initialAnnualFinance,
  initialFinanceRecords,
  initialMonthlyFinance,
  initialOrders,
  initialProducts,
  initialStaff,
} from './mockData'

const PRODUCT_KEY = 'restaurant_mock_products'
const ORDER_KEY = 'restaurant_mock_orders'
const STAFF_KEY = 'restaurant_mock_staff'
const MONTHLY_FINANCE_KEY = 'restaurant_mock_monthly_finance'
const ANNUAL_FINANCE_KEY = 'restaurant_mock_annual_finance'
const FINANCE_RECORD_KEY = 'restaurant_mock_finance_records_v2'
const MEMBER_KEY = 'restaurant_mock_members_v1'
const TOKEN_KEY = 'restaurant_admin_token'
const USER_KEY = 'restaurant_admin_user'
const TOKEN_EXPIRES_AT_KEY = 'restaurant_admin_token_expires_at'
const DEFAULT_API_BASE_URL = import.meta.env.PROD ? 'https://api.yee.earth/api' : 'http://127.0.0.1:8080/api'
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL
const ENABLE_MOCK_FALLBACK = String(import.meta.env.VITE_ENABLE_MOCK_FALLBACK ?? (!import.meta.env.PROD)).toLowerCase() === 'true'

const clone = (value) => JSON.parse(JSON.stringify(value))
const sleep = (ms = 180) => new Promise((resolve) => setTimeout(resolve, ms))

function clearAdminSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(TOKEN_EXPIRES_AT_KEY)
  window.dispatchEvent(new Event('restaurant-admin-auth-expired'))
}

function getAdminToken() {
  const token = localStorage.getItem(TOKEN_KEY)
  const expiresAt = Number(localStorage.getItem(TOKEN_EXPIRES_AT_KEY) || 0)
  if (!token) {
    return ''
  }
  if (!expiresAt || Date.now() > expiresAt) {
    clearAdminSession()
    return ''
  }
  return token
}

async function backendRequest(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }
  const token = getAdminToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })
  const body = await response.json().catch(() => ({}))
  if (response.ok && body.code === 200) {
    return body.data
  }
  if (response.status === 401 || response.status === 403 || body.code === 401 || body.code === 403) {
    clearAdminSession()
  }
  const error = new Error(body.message || `后端请求失败：${response.status}`)
  error.status = response.status
  throw error
}

async function tryBackend(path, options) {
  try {
    return await backendRequest(path, options)
  } catch (error) {
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] fallback to local mock:', path, error.message)
    return null
  }
}

function queryString(params = {}) {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, value)
    }
  })
  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

function requireMockFallback(feature) {
  if (!ENABLE_MOCK_FALLBACK) {
    throw new Error(`${feature} 后端接口不可用，生产环境已禁用本地 mock fallback`)
  }
}

function read(key, fallback) {
  const value = localStorage.getItem(key)
  if (!value) {
    localStorage.setItem(key, JSON.stringify(fallback))
    return clone(fallback)
  }
  return JSON.parse(value)
}

function write(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
  return clone(value)
}

function getProducts() {
  return read(PRODUCT_KEY, initialProducts)
}

function getOrders() {
  return read(ORDER_KEY, initialOrders)
}

function getStaff() {
  return read(STAFF_KEY, initialStaff)
}

function getMonthlyFinance() {
  return read(MONTHLY_FINANCE_KEY, initialMonthlyFinance)
}

function getAnnualFinance() {
  return read(ANNUAL_FINANCE_KEY, initialAnnualFinance)
}

function getFinanceRecords() {
  return read(FINANCE_RECORD_KEY, initialFinanceRecords)
}

function getMembers() {
  return read(MEMBER_KEY, [
    {
      userId: 1,
      nickname: '微信用户',
      memberLevel: '金卡会员',
      points: 426,
      totalSpent: 426.8,
      memberSince: '2026-04-18 12:20:00',
      nextLevelNeed: 0,
      pointEarnRule: '实付1元得1.5积分',
      pointRedeemRule: '100积分抵1元，50积分起用',
    },
    {
      userId: 2,
      nickname: '午餐常客',
      memberLevel: '银卡会员',
      points: 168,
      totalSpent: 168.5,
      memberSince: '2026-04-29 09:35:00',
      nextLevelNeed: 131.5,
      pointEarnRule: '实付1元得1.2积分',
      pointRedeemRule: '100积分抵1元，50积分起用',
    },
    {
      userId: 3,
      nickname: '清淡饮食用户',
      memberLevel: '普通会员',
      points: 63,
      totalSpent: 63.2,
      memberSince: '2026-05-02 18:15:00',
      nextLevelNeed: 36.8,
      pointEarnRule: '实付1元得1积分',
      pointRedeemRule: '100积分抵1元，50积分起用',
    },
  ])
}

function isSameDay(dateText, offset = 0) {
  const target = new Date()
  target.setDate(target.getDate() + offset)
  const date = new Date(dateText.replace(' ', 'T'))
  return target.toDateString() === date.toDateString()
}

function sum(list, selector) {
  return list.reduce((total, item) => total + selector(item), 0)
}

function money(value) {
  return Math.round(value * 100) / 100
}

function productCostMap(products) {
  return products.reduce((map, product) => {
    map[product.id] = Number(product.costPrice || product.price * 0.42)
    return map
  }, {})
}

function calcFoodCost(orders, products) {
  const costs = productCostMap(products)
  return money(
    sum(orders, (order) =>
      sum(order.items, (item) => Number(costs[item.productId] || item.unitPrice * 0.42) * Number(item.quantity)),
    ),
  )
}

function staffMonthlyCost(staff = getStaff()) {
  return money(
    sum(
      staff.filter((item) => item.status === 1),
      (item) =>
        item.salaryType === '时薪'
          ? Number(item.hourlyWage || 0) * Number(item.workHoursThisMonth || 0)
          : Number(item.monthlySalary || 0),
    ),
  )
}

function expenseOf(record) {
  return money(record.foodCost + record.laborCost + record.fixedCost + record.marketingCost + record.otherCost)
}

function profitOf(record) {
  return money(record.revenue - expenseOf(record))
}

function monthKey() {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
}

const revenueLabels = {
  dineIn: '堂食收银',
  miniapp: '小程序点餐',
  aiOrder: 'AI 点餐',
  delivery: '外卖渠道',
}

const costLabels = {
  food: '食材成本',
  managerLabor: '店长人力',
  employeeLabor: '员工人力',
  partTimeLabor: '兼职人力',
  rent: '房租',
  utilities: '水电燃气',
  marketing: '营销费用',
  platformFee: '平台手续费',
  equipment: '设备耗材',
  other: '其他成本',
}

const laborCostKeys = ['managerLabor', 'employeeLabor', 'partTimeLabor']

function recordRevenue(record, revenueType = 'all') {
  if (revenueType && revenueType !== 'all') {
    return Number(record.revenue[revenueType] || 0)
  }
  return sum(Object.values(record.revenue), (value) => Number(value))
}

function recordTotalRevenue(record) {
  return recordRevenue(record, 'all')
}

function recordLaborCost(record) {
  return sum(laborCostKeys, (key) => Number(record.cost[key] || 0))
}

function recordTotalCost(record) {
  return sum(Object.values(record.cost), (value) => Number(value))
}

function normalizeFinanceRecord(record, revenueType = 'all') {
  const totalRevenue = recordTotalRevenue(record)
  const queriedRevenue = recordRevenue(record, revenueType)
  const totalCost = recordTotalCost(record)
  const laborCost = recordLaborCost(record)
  const profit = totalRevenue - totalCost
  return {
    ...record,
    totalRevenue: money(totalRevenue),
    queriedRevenue: money(queriedRevenue),
    foodCost: Number(record.cost.food || 0),
    laborCost: money(laborCost),
    operatingCost: money(totalCost - Number(record.cost.food || 0) - laborCost),
    totalCost: money(totalCost),
    profit: money(profit),
    profitRate: totalRevenue ? Math.round((profit / totalRevenue) * 100) : 0,
  }
}

function filterFinanceRecords(records, params = {}) {
  return records.filter((record) => {
    const matchYear = !params.year || params.year === 'all' || record.year === String(params.year)
    const [startMonth, endMonth] = params.monthRange || []
    const matchMonth = (!startMonth || record.month >= startMonth) && (!endMonth || record.month <= endMonth)
    return matchYear && matchMonth
  })
}

function groupByYear(records, revenueType = 'all') {
  const yearMap = {}
  records.forEach((record) => {
    if (!yearMap[record.year]) {
      yearMap[record.year] = {
        year: record.year,
        revenue: 0,
        queriedRevenue: 0,
        foodCost: 0,
        laborCost: 0,
        operatingCost: 0,
        totalCost: 0,
        profit: 0,
        managerLabor: 0,
        employeeLabor: 0,
        partTimeLabor: 0,
      }
    }
    const normalized = normalizeFinanceRecord(record, revenueType)
    yearMap[record.year].revenue += normalized.totalRevenue
    yearMap[record.year].queriedRevenue += normalized.queriedRevenue
    yearMap[record.year].foodCost += normalized.foodCost
    yearMap[record.year].laborCost += normalized.laborCost
    yearMap[record.year].operatingCost += normalized.operatingCost
    yearMap[record.year].totalCost += normalized.totalCost
    yearMap[record.year].profit += normalized.profit
    yearMap[record.year].managerLabor += Number(record.cost.managerLabor || 0)
    yearMap[record.year].employeeLabor += Number(record.cost.employeeLabor || 0)
    yearMap[record.year].partTimeLabor += Number(record.cost.partTimeLabor || 0)
  })

  return Object.values(yearMap).map((item) => ({
    ...item,
    revenue: money(item.revenue),
    queriedRevenue: money(item.queriedRevenue),
    foodCost: money(item.foodCost),
    laborCost: money(item.laborCost),
    operatingCost: money(item.operatingCost),
    totalCost: money(item.totalCost),
    profit: money(item.profit),
    managerLabor: money(item.managerLabor),
    employeeLabor: money(item.employeeLabor),
    partTimeLabor: money(item.partTimeLabor),
    profitRate: item.revenue ? Math.round((item.profit / item.revenue) * 100) : 0,
  }))
}

function summarizeFinance(records, revenueType = 'all') {
  const selectedRevenue = sum(records, (record) => recordRevenue(record, revenueType))
  const totalRevenue = sum(records, recordTotalRevenue)
  const totalCost = sum(records, recordTotalCost)
  const foodCost = sum(records, (record) => Number(record.cost.food || 0))
  const laborCost = sum(records, recordLaborCost)
  const operatingCost = totalCost - foodCost - laborCost
  const profit = totalRevenue - totalCost
  const costStructure = Object.entries(costLabels).map(([key, label]) => ({
    key,
    name: label,
    value: money(sum(records, (record) => Number(record.cost[key] || 0))),
  }))
  const laborStructure = laborCostKeys.map((key) => ({
    key,
    name: costLabels[key],
    value: money(sum(records, (record) => Number(record.cost[key] || 0))),
  }))
  const revenueStructure = Object.entries(revenueLabels).map(([key, label]) => ({
    key,
    name: label,
    value: money(sum(records, (record) => Number(record.revenue[key] || 0))),
  }))

  return {
    selectedRevenue: money(selectedRevenue),
    totalRevenue: money(totalRevenue),
    totalCost: money(totalCost),
    foodCost: money(foodCost),
    laborCost: money(laborCost),
    operatingCost: money(operatingCost),
    profit: money(profit),
    profitRate: totalRevenue ? Math.round((profit / totalRevenue) * 100) : 0,
    costRate: totalRevenue ? Math.round((totalCost / totalRevenue) * 100) : 0,
    foodCostRate: totalRevenue ? Math.round((foodCost / totalRevenue) * 100) : 0,
    laborCostRate: totalRevenue ? Math.round((laborCost / totalRevenue) * 100) : 0,
    costStructure,
    laborStructure,
    revenueStructure,
  }
}

export async function login({ username, password }) {
  const backendResult = await tryBackend('/admin/auth/login', {
    method: 'POST',
    body: { username, password },
  })
  if (backendResult?.token) {
    return {
      token: backendResult.token,
      user: {
        id: backendResult.adminId,
        username: backendResult.username,
        nickname: '门店管理员',
      },
    }
  }
  requireMockFallback('管理员登录')
  await sleep()
  if (username === 'admin' && password === '123456') {
    return {
      token: 'mock-admin-token',
      user: {
        id: 1,
        username: 'admin',
        nickname: '门店管理员',
      },
    }
  }
  throw new Error('账号或密码错误，演示账号：admin / 123456')
}

export async function fetchMembers(params = {}) {
  const backendResult = await tryBackend('/admin/members')
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('会员列表')
  await sleep()
  const keyword = params.keyword?.trim().toLowerCase()
  return getMembers().filter((member) => {
    const matchKeyword =
      !keyword ||
      member.nickname.toLowerCase().includes(keyword) ||
      String(member.userId).includes(keyword) ||
      member.memberLevel.toLowerCase().includes(keyword)
    const matchLevel = !params.level || member.memberLevel === params.level
    return matchKeyword && matchLevel
  })
}

export async function fetchMemberStats() {
  const backendResult = await tryBackend('/admin/members/stats')
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('会员统计')
  await sleep()
  const members = getMembers()
  const levelDistribution = members.reduce((map, member) => {
    map[member.memberLevel] = (map[member.memberLevel] || 0) + 1
    return map
  }, {})
  const totalMemberSpent = sum(members, (member) => Number(member.totalSpent || 0))
  return {
    memberCount: members.length,
    totalMemberSpent: money(totalMemberSpent),
    avgMemberSpent: members.length ? money(totalMemberSpent / members.length) : 0,
    totalPoints: sum(members, (member) => Number(member.points || 0)),
    levelDistribution,
    topMembers: members.slice().sort((a, b) => b.totalSpent - a.totalSpent).slice(0, 8),
  }
}

export async function fetchProducts(params = {}) {
  const backendResult = await tryBackend(`/admin/products${queryString(params)}`)
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('菜品管理')
  await sleep()
  const keyword = params.keyword?.trim().toLowerCase()
  const list = getProducts().filter((item) => {
    const matchKeyword =
      !keyword ||
      item.name.toLowerCase().includes(keyword) ||
      item.category.toLowerCase().includes(keyword) ||
      item.tasteTags.toLowerCase().includes(keyword)
    const matchCategory = !params.category || item.category === params.category
    const matchStatus = params.status === '' || params.status === undefined || item.status === Number(params.status)
    return matchKeyword && matchCategory && matchStatus
  })
  return list
}

export async function saveProduct(payload) {
  const backendPayload = { ...payload }
  const backendResult = await tryBackend(payload.id ? `/admin/products/${payload.id}` : '/admin/products', {
    method: payload.id ? 'PUT' : 'POST',
    body: backendPayload,
  })
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('菜品保存')
  await sleep()
  const products = getProducts()
  if (payload.id) {
    const index = products.findIndex((item) => item.id === payload.id)
    products[index] = { ...products[index], ...payload }
    return write(PRODUCT_KEY, products)[index]
  }
  const next = {
    ...payload,
    id: Math.max(...products.map((item) => item.id), 0) + 1,
    status: payload.status ?? 1,
    stock: Number(payload.stock || 0),
    price: Number(payload.price || 0),
    costPrice: Number(payload.costPrice || 0),
    cookTime: Number(payload.cookTime || 10),
  }
  products.unshift(next)
  write(PRODUCT_KEY, products)
  return next
}

export async function removeProduct(id) {
  try {
    await backendRequest(`/admin/products/${id}`, {
      method: 'DELETE',
    })
    return true
  } catch (error) {
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] fallback to local mock:', `/admin/products/${id}`, error.message)
  }
  requireMockFallback('菜品删除')
  await sleep()
  write(
    PRODUCT_KEY,
    getProducts().filter((item) => item.id !== id),
  )
  return true
}

export async function updateProductStatus(id, status) {
  try {
    await backendRequest(`/admin/products/${id}/status?status=${status}`, {
      method: 'PATCH',
    })
    return true
  } catch (error) {
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] fallback to local mock:', `/admin/products/${id}/status`, error.message)
  }
  requireMockFallback('菜品状态更新')
  await sleep()
  const products = getProducts().map((item) => (item.id === id ? { ...item, status } : item))
  write(PRODUCT_KEY, products)
  return products.find((item) => item.id === id)
}

export async function updateProductStock(id, stock) {
  try {
    await backendRequest(`/admin/products/${id}/stock?stock=${stock}`, {
      method: 'PATCH',
    })
    return true
  } catch (error) {
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] fallback to local mock:', `/admin/products/${id}/stock`, error.message)
  }
  requireMockFallback('库存更新')
  await sleep()
  const products = getProducts().map((item) => (item.id === id ? { ...item, stock } : item))
  write(PRODUCT_KEY, products)
  return products.find((item) => item.id === id)
}

export async function fetchOrders(params = {}) {
  const backendResult = await tryBackend(`/admin/orders${queryString(params)}`)
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('订单管理')
  await sleep()
  const keyword = params.keyword?.trim().toLowerCase()
  return getOrders().filter((item) => {
    const matchKeyword =
      !keyword ||
      item.orderNo.toLowerCase().includes(keyword) ||
      item.remark.toLowerCase().includes(keyword) ||
      item.items.some((detail) => detail.productName.toLowerCase().includes(keyword))
    const matchStatus = params.status === '' || params.status === undefined || item.status === Number(params.status)
    const matchSource = params.source === '' || params.source === undefined || item.source === Number(params.source)
    return matchKeyword && matchStatus && matchSource
  })
}

export async function updateOrderStatus(id, status) {
  try {
    await backendRequest(`/admin/orders/${id}/status`, {
      method: 'PATCH',
      body: { status },
    })
    return true
  } catch (error) {
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] fallback to local mock:', `/admin/orders/${id}/status`, error.message)
  }
  requireMockFallback('订单状态更新')
  await sleep()
  const orders = getOrders().map((item) => (item.id === id ? { ...item, status } : item))
  write(ORDER_KEY, orders)
  return orders.find((item) => item.id === id)
}

export async function fetchStaff(params = {}) {
  const backendResult = await tryBackend(`/admin/staff${queryString(params)}`)
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('人员管理')
  await sleep()
  const keyword = params.keyword?.trim().toLowerCase()
  return getStaff().filter((item) => {
    const matchKeyword =
      !keyword ||
      item.name.toLowerCase().includes(keyword) ||
      item.phone.includes(keyword) ||
      item.role.toLowerCase().includes(keyword) ||
      item.shift.toLowerCase().includes(keyword)
    const matchRole = !params.role || item.role === params.role
    const matchStatus = params.status === '' || params.status === undefined || item.status === Number(params.status)
    return matchKeyword && matchRole && matchStatus
  })
}

export async function saveStaff(payload) {
  const backendResult = await tryBackend(payload.id ? `/admin/staff/${payload.id}` : '/admin/staff', {
    method: payload.id ? 'PUT' : 'POST',
    body: payload,
  })
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('人员保存')
  await sleep()
  const staff = getStaff()
  const normalized = {
    ...payload,
    monthlySalary: Number(payload.monthlySalary || 0),
    hourlyWage: Number(payload.hourlyWage || 0),
    workHoursThisMonth: Number(payload.workHoursThisMonth || 0),
    status: payload.status ?? 1,
  }
  if (payload.id) {
    const index = staff.findIndex((item) => item.id === payload.id)
    staff[index] = { ...staff[index], ...normalized }
    return write(STAFF_KEY, staff)[index]
  }
  const next = {
    ...normalized,
    id: Math.max(...staff.map((item) => item.id), 0) + 1,
  }
  staff.unshift(next)
  write(STAFF_KEY, staff)
  return next
}

export async function removeStaff(id) {
  try {
    await backendRequest(`/admin/staff/${id}`, {
      method: 'DELETE',
    })
    return true
  } catch (error) {
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] fallback to local mock:', `/admin/staff/${id}`, error.message)
  }
  requireMockFallback('人员删除')
  await sleep()
  write(
    STAFF_KEY,
    getStaff().filter((item) => item.id !== id),
  )
  return true
}

export async function updateStaffStatus(id, status) {
  try {
    await backendRequest(`/admin/staff/${id}/status?status=${status}`, {
      method: 'PATCH',
    })
    return true
  } catch (error) {
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] fallback to local mock:', `/admin/staff/${id}/status`, error.message)
  }
  requireMockFallback('人员状态更新')
  await sleep()
  const staff = getStaff().map((item) => (item.id === id ? { ...item, status } : item))
  write(STAFF_KEY, staff)
  return staff.find((item) => item.id === id)
}

export async function fetchDashboardStats() {
  const backendResult = await tryBackend('/admin/stats/dashboard')
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('经营看板')
  await sleep()
  const products = getProducts()
  const orders = getOrders()
  const staff = getStaff()
  const monthlyFinance = getMonthlyFinance()
  const annualFinance = getAnnualFinance()
  const todayOrders = orders.filter((item) => isSameDay(item.createTime))
  const yesterdayOrders = orders.filter((item) => isSameDay(item.createTime, -1))
  const completedToday = todayOrders.filter((item) => item.status !== 3)
  const completedYesterday = yesterdayOrders.filter((item) => item.status !== 3)
  const productMap = products.reduce((map, product) => {
    map[product.id] = product
    return map
  }, {})
  const revenueToday = sum(completedToday, (item) => item.totalAmount)
  const revenueYesterday = sum(completedYesterday, (item) => item.totalAmount)
  const foodCostToday = calcFoodCost(completedToday, products)
  const laborCostMonth = staffMonthlyCost(staff)
  const laborCostToday = money(laborCostMonth / 30)
  const grossProfitToday = money(revenueToday - foodCostToday)
  const netProfitToday = money(grossProfitToday - laborCostToday)
  const orderToday = completedToday.length
  const orderYesterday = completedYesterday.length
  const lowStock = products.filter((item) => item.stock <= 10)
  const topItems = {}
  const categoryRevenueMap = {}
  const currentMonth = monthlyFinance.find((item) => item.month === monthKey()) || monthlyFinance.at(-1)
  const currentYear = annualFinance.find((item) => item.year === String(new Date().getFullYear())) || annualFinance.at(-1)

  completedToday.forEach((order) => {
    order.items.forEach((item) => {
      topItems[item.productName] = (topItems[item.productName] || 0) + item.quantity
      const category = productMap[item.productId]?.category || '其他'
      categoryRevenueMap[category] = (categoryRevenueMap[category] || 0) + item.subtotal
    })
  })

  const staffCostDetails = staff
    .filter((item) => item.status === 1)
    .map((item) => {
      const monthlyCost =
        item.salaryType === '时薪'
          ? Number(item.hourlyWage || 0) * Number(item.workHoursThisMonth || 0)
          : Number(item.monthlySalary || 0)
      return {
        id: item.id,
        name: item.name,
        role: item.role,
        shift: item.shift,
        salaryType: item.salaryType,
        workHoursThisMonth: item.workHoursThisMonth,
        monthlyCost: money(monthlyCost),
        dailyCost: money(monthlyCost / 30),
      }
    })

  const dailyRevenueTrend = Array.from({ length: 7 }, (_, index) => {
    const offset = index - 6
    const date = new Date()
    date.setDate(date.getDate() + offset)
    const label = `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    const dayOrders = orders.filter((item) => isSameDay(item.createTime, offset) && item.status !== 3)
    return {
      date: label,
      revenue: money(sum(dayOrders, (item) => item.totalAmount)),
      orderCount: dayOrders.length,
    }
  })

  return {
    revenueToday,
    revenueYesterday,
    revenueChange: revenueYesterday ? Math.round(((revenueToday - revenueYesterday) / revenueYesterday) * 100) : 100,
    foodCostToday,
    laborCostToday,
    grossProfitToday,
    netProfitToday,
    avgTicket: orderToday ? money(revenueToday / orderToday) : 0,
    foodCostRate: revenueToday ? Math.round((foodCostToday / revenueToday) * 100) : 0,
    laborCostRate: revenueToday ? Math.round((laborCostToday / revenueToday) * 100) : 0,
    profitMargin: revenueToday ? Math.round((netProfitToday / revenueToday) * 100) : 0,
    orderToday,
    orderYesterday,
    orderChange: orderYesterday ? Math.round(((orderToday - orderYesterday) / orderYesterday) * 100) : 100,
    productCount: products.length,
    lowStockCount: lowStock.length,
    pendingOrderCount: todayOrders.filter((item) => item.status === 0).length,
    activeProductCount: products.filter((item) => item.status === 1).length,
    staffCount: staff.filter((item) => item.status === 1).length,
    laborCostMonth,
    monthRevenue: currentMonth.revenue,
    monthExpense: expenseOf(currentMonth),
    monthProfit: profitOf(currentMonth),
    yearRevenue: currentYear.revenue,
    yearExpense: expenseOf(currentYear),
    yearProfit: profitOf(currentYear),
    staffCostDetails,
    sourceRevenue: [
      {
        name: '普通点餐',
        orderCount: completedToday.filter((item) => item.source === 0).length,
        revenue: money(sum(completedToday.filter((item) => item.source === 0), (item) => item.totalAmount)),
      },
      {
        name: 'AI 点餐',
        orderCount: completedToday.filter((item) => item.source === 1).length,
        revenue: money(sum(completedToday.filter((item) => item.source === 1), (item) => item.totalAmount)),
      },
    ],
    categoryRevenue: Object.entries(categoryRevenueMap).map(([name, revenue]) => ({
      name,
      revenue: money(revenue),
    })),
    dailyRevenueTrend,
    costBreakdown: [
      { name: '食材成本', value: currentMonth.foodCost },
      { name: '人力成本', value: currentMonth.laborCost },
      { name: '房租水电', value: currentMonth.fixedCost },
      { name: '营销费用', value: currentMonth.marketingCost },
      { name: '其他费用', value: currentMonth.otherCost },
    ],
    monthlyFinance: monthlyFinance.map((item) => ({
      ...item,
      expense: expenseOf(item),
      profit: profitOf(item),
    })),
    annualFinance: annualFinance.map((item) => ({
      ...item,
      expense: expenseOf(item),
      profit: profitOf(item),
    })),
    lowStock,
    topProducts: Object.entries(topItems)
      .map(([name, quantity]) => ({ name, quantity }))
      .sort((a, b) => b.quantity - a.quantity)
      .slice(0, 6),
    hourlyOrders: ['10', '11', '12', '13', '14', '17', '18', '19', '20'].map((hour) => ({
      hour: `${hour}:00`,
      count: todayOrders.filter((item) => item.createTime.includes(` ${hour}:`)).length,
    })),
  }
}

export async function fetchFinanceOverview(params = {}) {
  const backendResult = await tryBackend(
    `/admin/finance/overview${queryString({
      year: params.year,
      monthStart: params.monthRange?.[0],
      monthEnd: params.monthRange?.[1],
      revenueType: params.revenueType,
    })}`,
  )
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('财务看板')
  await sleep()
  const allRecords = getFinanceRecords().sort((a, b) => a.month.localeCompare(b.month))
  const revenueType = params.revenueType || 'all'
  const selectedRecords = filterFinanceRecords(allRecords, params)
  const normalizedRecords = selectedRecords.map((record) => normalizeFinanceRecord(record, revenueType))
  const allAnnualSummary = groupByYear(allRecords, revenueType)
  const selectedAnnualSummary = groupByYear(selectedRecords, revenueType)
  const summary = summarizeFinance(selectedRecords, revenueType)
  const yearOptions = [...new Set(allRecords.map((record) => record.year))]
  const monthOptions = allRecords.map((record) => record.month)

  return {
    query: {
      year: params.year || 'all',
      monthRange: params.monthRange || [],
      revenueType,
    },
    revenueLabels,
    costLabels,
    yearOptions,
    monthOptions,
    summary,
    monthlyDetails: normalizedRecords,
    annualDetails: selectedAnnualSummary,
    allAnnualSummary,
    monthlyTrend: normalizedRecords.map((record) => ({
      month: record.month,
      revenue: record.totalRevenue,
      selectedRevenue: record.queriedRevenue,
      foodCost: record.foodCost,
      laborCost: record.laborCost,
      operatingCost: record.operatingCost,
      totalCost: record.totalCost,
      profit: record.profit,
      profitRate: record.profitRate,
    })),
    annualTrend: allAnnualSummary.map((record) => ({
      year: record.year,
      revenue: record.revenue,
      selectedRevenue: record.queriedRevenue,
      foodCost: record.foodCost,
      laborCost: record.laborCost,
      operatingCost: record.operatingCost,
      totalCost: record.totalCost,
      profit: record.profit,
      profitRate: record.profitRate,
    })),
    laborMonthlyTrend: normalizedRecords.map((record) => ({
      month: record.month,
      managerLabor: Number(record.cost.managerLabor || 0),
      employeeLabor: Number(record.cost.employeeLabor || 0),
      partTimeLabor: Number(record.cost.partTimeLabor || 0),
      laborCost: record.laborCost,
    })),
    laborAnnualTrend: allAnnualSummary.map((record) => ({
      year: record.year,
      managerLabor: record.managerLabor,
      employeeLabor: record.employeeLabor,
      partTimeLabor: record.partTimeLabor,
      laborCost: record.laborCost,
    })),
  }
}

export async function fetchAiAnalysis() {
  const stats = await fetchDashboardStats()
  return {
    summary: `今日营业额 ${stats.revenueToday.toFixed(2)} 元，订单 ${stats.orderToday} 单，今日净利约 ${stats.netProfitToday.toFixed(2)} 元，本月利润 ${stats.monthProfit.toFixed(2)} 元。`,
    sections: [
      {
        title: '营业表现',
        content:
          stats.revenueChange >= 0
            ? '今日收入高于昨日，午餐和晚餐时段表现稳定，可以继续保持主食与饮品组合推荐。'
            : '今日收入低于昨日，建议观察午餐时段转化，适当推出套餐或满减活动。',
      },
      {
        title: '成本与利润',
        content: `今日食材成本率约 ${stats.foodCostRate}%，人力成本率约 ${stats.laborCostRate}%，净利率约 ${stats.profitMargin}%。建议持续关注低毛利菜品和排班效率。`,
      },
      {
        title: '热销菜品',
        content:
          stats.topProducts.length > 0
            ? `${stats.topProducts[0].name} 当前销量领先，可在小程序首页和收银推荐区优先展示。`
            : '今日暂未形成明显热销菜品，需要结合后续订单继续观察。',
      },
      {
        title: '库存提醒',
        content:
          stats.lowStock.length > 0
            ? `${stats.lowStock.map((item) => item.name).join('、')} 库存偏低，建议及时补货或临时下架。`
            : '当前重点菜品库存充足，短期内无明显缺货风险。',
      },
    ],
    suggestions: ['把热销主食与饮品组合成午餐套餐', '对库存低于 10 的菜品设置预警', '根据午晚高峰订单量调整兼职排班', '跟踪食材成本率，优先优化毛利偏低菜品'],
  }
}

export async function fetchAiStatus() {
  const backendResult = await tryBackend('/admin/ai/status')
  if (backendResult) {
    return backendResult
  }
  requireMockFallback('AI 状态')
  return {
    provider: 'DeepSeek',
    model: 'deepseek-v4-pro',
    configured: false,
  }
}

export async function chatBusinessAi({ message, sessionId }) {
  let fallbackReason = '后台服务未连接'
  try {
    return await backendRequest('/admin/ai/business-chat', {
      method: 'POST',
      body: {
        message,
        sessionId,
      },
    })
  } catch (error) {
    fallbackReason =
      error.status === 401
        ? '当前后台是离线登录或 token 已失效，请在后端启动后退出并重新登录'
        : error.message || fallbackReason
    if (!ENABLE_MOCK_FALLBACK) {
      throw error
    }
    console.warn('[admin-api] business AI fallback:', fallbackReason)
  }
  const stats = await fetchDashboardStats()
  return {
    sessionId: sessionId || `local-${Date.now()}`,
    provider: 'frontend-local',
    model: 'fallback',
    fallback: true,
    errorMessage: fallbackReason,
    thinking: ['读取经营看板指标', '结合会员消费、库存和成本结构', '后端 AI 暂不可用，使用本地规则生成'],
    answer: `当前今日营业额 ${stats.revenueToday.toFixed(2)} 元，今日订单 ${stats.orderToday} 单，净利约 ${stats.netProfitToday.toFixed(2)} 元。针对“${message}”，建议优先看热销菜品组合、会员复购转化和库存风险三件事。`,
    actions: ['把热销主食与饮品设置成会员套餐', '对低库存菜品设置小程序临时隐藏或预警', '追问具体月份、成本项或会员等级获得更细分析'],
  }
}
