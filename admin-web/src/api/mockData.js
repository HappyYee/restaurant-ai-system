const now = new Date()
const pad = (value) => String(value).padStart(2, '0')
const dayString = (offset = 0) => {
  const date = new Date(now)
  date.setDate(date.getDate() + offset)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}
const at = (dayOffset, time) => `${dayString(dayOffset)} ${time}`

export const initialProducts = [
  {
    id: 1,
    name: '番茄鸡蛋饭',
    category: '主食',
    price: 22,
    costPrice: 9,
    stock: 20,
    status: 1,
    tasteTags: '清淡,不辣,实惠',
    description: '番茄鸡蛋搭配米饭，口味清淡，适合日常午餐。',
    imageUrl: '',
    cookTime: 8,
  },
  {
    id: 2,
    name: '牛肉饭',
    category: '主食',
    price: 28,
    costPrice: 13,
    stock: 5,
    status: 1,
    tasteTags: '微辣,热销,饱腹',
    description: '牛肉搭配米饭，微辣口味，门店热销菜品。',
    imageUrl: '',
    cookTime: 10,
  },
  {
    id: 3,
    name: '鸡腿饭',
    category: '主食',
    price: 26,
    costPrice: 12,
    stock: 30,
    status: 1,
    tasteTags: '咸香,饱腹,热销',
    description: '香煎鸡腿搭配米饭，适合想吃饱的顾客。',
    imageUrl: '',
    cookTime: 12,
  },
  {
    id: 4,
    name: '鸡胸肉轻食饭',
    category: '主食',
    price: 30,
    costPrice: 14,
    stock: 15,
    status: 1,
    tasteTags: '清淡,高蛋白,健康',
    description: '鸡胸肉搭配蔬菜和米饭，适合清淡健康需求。',
    imageUrl: '',
    cookTime: 10,
  },
  {
    id: 5,
    name: '柠檬茶',
    category: '饮品',
    price: 8,
    costPrice: 2.4,
    stock: 50,
    status: 1,
    tasteTags: '清爽,甜,饮品',
    description: '清爽柠檬茶，适合搭配主食。',
    imageUrl: '',
    cookTime: 2,
  },
  {
    id: 6,
    name: '无糖绿茶',
    category: '饮品',
    price: 6,
    costPrice: 1.8,
    stock: 40,
    status: 1,
    tasteTags: '清淡,无糖,饮品',
    description: '无糖绿茶，适合清淡低糖需求。',
    imageUrl: '',
    cookTime: 1,
  },
  {
    id: 7,
    name: '香辣鸡翅',
    category: '小吃',
    price: 16,
    costPrice: 7,
    stock: 8,
    status: 1,
    tasteTags: '香辣,小吃',
    description: '香辣口味鸡翅，适合加餐。',
    imageUrl: '',
    cookTime: 8,
  },
  {
    id: 8,
    name: '薯条',
    category: '小吃',
    price: 10,
    costPrice: 3.5,
    stock: 25,
    status: 1,
    tasteTags: '小吃,实惠',
    description: '经典薯条，可作为加餐。',
    imageUrl: '',
    cookTime: 5,
  },
]

export const initialOrders = [
  {
    id: 101,
    orderNo: 'RO202605090001',
    userId: 12,
    totalAmount: 58,
    status: 1,
    source: 0,
    remark: '少油',
    createTime: at(0, '11:24:18'),
    items: [
      { productId: 1, productName: '番茄鸡蛋饭', quantity: 1, unitPrice: 22, subtotal: 22 },
      { productId: 3, productName: '鸡腿饭', quantity: 1, unitPrice: 26, subtotal: 26 },
      { productId: 8, productName: '薯条', quantity: 1, unitPrice: 10, subtotal: 10 },
    ],
  },
  {
    id: 102,
    orderNo: 'RO202605090002',
    userId: 18,
    totalAmount: 44,
    status: 0,
    source: 1,
    remark: '两人份，清淡一点',
    createTime: at(0, '12:08:36'),
    items: [
      { productId: 4, productName: '鸡胸肉轻食饭', quantity: 1, unitPrice: 30, subtotal: 30 },
      { productId: 6, productName: '无糖绿茶', quantity: 1, unitPrice: 6, subtotal: 6 },
      { productId: 5, productName: '柠檬茶', quantity: 1, unitPrice: 8, subtotal: 8 },
    ],
  },
  {
    id: 103,
    orderNo: 'RO202605090003',
    userId: 21,
    totalAmount: 52,
    status: 2,
    source: 0,
    remark: '',
    createTime: at(0, '18:42:09'),
    items: [
      { productId: 2, productName: '牛肉饭', quantity: 1, unitPrice: 28, subtotal: 28 },
      { productId: 7, productName: '香辣鸡翅', quantity: 1, unitPrice: 16, subtotal: 16 },
      { productId: 5, productName: '柠檬茶', quantity: 1, unitPrice: 8, subtotal: 8 },
    ],
  },
  {
    id: 104,
    orderNo: 'RO202605080001',
    userId: 7,
    totalAmount: 34,
    status: 2,
    source: 0,
    remark: '',
    createTime: at(-1, '12:17:44'),
    items: [
      { productId: 3, productName: '鸡腿饭', quantity: 1, unitPrice: 26, subtotal: 26 },
      { productId: 5, productName: '柠檬茶', quantity: 1, unitPrice: 8, subtotal: 8 },
    ],
  },
  {
    id: 105,
    orderNo: 'RO202605080002',
    userId: 9,
    totalAmount: 60,
    status: 2,
    source: 1,
    remark: '预算 60 以内',
    createTime: at(-1, '19:01:12'),
    items: [
      { productId: 1, productName: '番茄鸡蛋饭', quantity: 1, unitPrice: 22, subtotal: 22 },
      { productId: 4, productName: '鸡胸肉轻食饭', quantity: 1, unitPrice: 30, subtotal: 30 },
      { productId: 5, productName: '柠檬茶', quantity: 1, unitPrice: 8, subtotal: 8 },
    ],
  },
]

export const initialStaff = [
  {
    id: 1,
    name: '李敏',
    phone: '13800010001',
    role: '店长',
    shift: '全日班',
    salaryType: '月薪',
    monthlySalary: 7200,
    hourlyWage: 0,
    workHoursThisMonth: 176,
    status: 1,
    hireDate: '2024-03-12',
    remark: '负责门店排班、库存和日结。',
  },
  {
    id: 2,
    name: '王磊',
    phone: '13800010002',
    role: '后厨',
    shift: '早班',
    salaryType: '月薪',
    monthlySalary: 5600,
    hourlyWage: 0,
    workHoursThisMonth: 168,
    status: 1,
    hireDate: '2024-06-01',
    remark: '主食出餐。',
  },
  {
    id: 3,
    name: '陈佳',
    phone: '13800010003',
    role: '前台',
    shift: '午晚班',
    salaryType: '月薪',
    monthlySalary: 4800,
    hourlyWage: 0,
    workHoursThisMonth: 160,
    status: 1,
    hireDate: '2025-02-18',
    remark: '收银、打包和小程序订单处理。',
  },
  {
    id: 4,
    name: '赵阳',
    phone: '13800010004',
    role: '兼职',
    shift: '晚班',
    salaryType: '时薪',
    monthlySalary: 0,
    hourlyWage: 28,
    workHoursThisMonth: 72,
    status: 1,
    hireDate: '2025-09-03',
    remark: '晚高峰支援。',
  },
  {
    id: 5,
    name: '周宁',
    phone: '13800010005',
    role: '后厨',
    shift: '休假',
    salaryType: '月薪',
    monthlySalary: 5200,
    hourlyWage: 0,
    workHoursThisMonth: 0,
    status: 0,
    hireDate: '2024-11-20',
    remark: '暂时停用账号。',
  },
]

export const initialMonthlyFinance = [
  { month: '2026-01', revenue: 86200, foodCost: 31800, laborCost: 23800, fixedCost: 9200, marketingCost: 3200, otherCost: 2600 },
  { month: '2026-02', revenue: 79400, foodCost: 29600, laborCost: 23600, fixedCost: 9200, marketingCost: 2800, otherCost: 2500 },
  { month: '2026-03', revenue: 93400, foodCost: 34500, laborCost: 24400, fixedCost: 9400, marketingCost: 3600, otherCost: 2900 },
  { month: '2026-04', revenue: 101600, foodCost: 37400, laborCost: 24800, fixedCost: 9400, marketingCost: 4200, otherCost: 3100 },
  { month: '2026-05', revenue: 108800, foodCost: 39800, laborCost: 25200, fixedCost: 9600, marketingCost: 4600, otherCost: 3300 },
  { month: '2026-06', revenue: 112300, foodCost: 41300, laborCost: 25600, fixedCost: 9600, marketingCost: 4800, otherCost: 3400 },
  { month: '2026-07', revenue: 119600, foodCost: 44100, laborCost: 25800, fixedCost: 9800, marketingCost: 5200, otherCost: 3600 },
  { month: '2026-08', revenue: 116500, foodCost: 42800, laborCost: 25800, fixedCost: 9800, marketingCost: 5000, otherCost: 3500 },
  { month: '2026-09', revenue: 121400, foodCost: 44600, laborCost: 26200, fixedCost: 9800, marketingCost: 5300, otherCost: 3700 },
  { month: '2026-10', revenue: 126800, foodCost: 46800, laborCost: 26600, fixedCost: 10000, marketingCost: 5700, otherCost: 3900 },
  { month: '2026-11', revenue: 132500, foodCost: 48900, laborCost: 26800, fixedCost: 10000, marketingCost: 6100, otherCost: 4100 },
  { month: '2026-12', revenue: 146200, foodCost: 53900, laborCost: 27200, fixedCost: 10200, marketingCost: 7600, otherCost: 4500 },
]

export const initialAnnualFinance = [
  { year: '2022', revenue: 684000, foodCost: 254000, laborCost: 186000, fixedCost: 102000, marketingCost: 28000, otherCost: 24000 },
  { year: '2023', revenue: 836000, foodCost: 309000, laborCost: 214000, fixedCost: 108000, marketingCost: 36000, otherCost: 31000 },
  { year: '2024', revenue: 982000, foodCost: 363000, laborCost: 246000, fixedCost: 112000, marketingCost: 47000, otherCost: 37000 },
  { year: '2025', revenue: 1186000, foodCost: 438000, laborCost: 288000, fixedCost: 118000, marketingCost: 62000, otherCost: 43000 },
  { year: '2026', revenue: 1334700, foodCost: 491500, laborCost: 304000, fixedCost: 116200, marketingCost: 58300, otherCost: 41000 },
]

const monthSeasonFactor = [0.86, 0.82, 0.94, 1.0, 1.08, 1.11, 1.18, 1.14, 1.2, 1.25, 1.3, 1.42]
const annualFinanceSeed = {
  2022: 684000,
  2023: 836000,
  2024: 982000,
  2025: 1186000,
  2026: 1334700,
}

const rounded = (value) => Math.round(value / 100) * 100

export const initialFinanceRecords = Object.entries(annualFinanceSeed).flatMap(([year, annualRevenue]) => {
  const factorTotal = monthSeasonFactor.reduce((total, item) => total + item, 0)
  const yearNumber = Number(year)

  return monthSeasonFactor.map((factor, index) => {
    const month = index + 1
    const revenue = rounded((annualRevenue * factor) / factorTotal)
    const aiGrowth = Math.min(0.18, 0.03 + (yearNumber - 2022) * 0.025 + index * 0.002)
    const deliveryRate = 0.1 + (index % 4) * 0.01
    const miniappRate = 0.28 + (yearNumber - 2022) * 0.018
    const aiOrder = rounded(revenue * aiGrowth)
    const delivery = rounded(revenue * deliveryRate)
    const miniapp = rounded(revenue * miniappRate)
    const dineIn = revenue - aiOrder - delivery - miniapp

    const foodCost = rounded(revenue * (0.355 + (index % 3) * 0.008))
    const managerLabor = rounded(6400 + (yearNumber - 2022) * 420 + (month >= 10 ? 300 : 0))
    const employeeLabor = rounded(9200 + (yearNumber - 2022) * 1300 + index * 120)
    const partTimeLabor = rounded(1800 + (month >= 6 && month <= 8 ? 1600 : 0) + (month >= 11 ? 1200 : 0))
    const rent = rounded(7600 + (yearNumber - 2022) * 240)
    const utilities = rounded(1400 + month * 45 + (month >= 6 && month <= 8 ? 650 : 0))
    const marketing = rounded(revenue * (0.035 + (month === 12 ? 0.018 : 0)))
    const platformFee = rounded((miniapp + aiOrder + delivery) * 0.022)
    const equipment = rounded(700 + (month === 3 || month === 9 ? 1800 : 0))
    const other = rounded(1200 + (index % 5) * 180)

    return {
      month: `${year}-${String(month).padStart(2, '0')}`,
      year,
      revenue: {
        dineIn,
        miniapp,
        aiOrder,
        delivery,
      },
      cost: {
        food: foodCost,
        managerLabor,
        employeeLabor,
        partTimeLabor,
        rent,
        utilities,
        marketing,
        platformFee,
        equipment,
        other,
      },
      remark: month === 12 ? '年末活动月份，营销费用和收入同步上升。' : '',
    }
  })
})
