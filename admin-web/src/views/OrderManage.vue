<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { fetchOrders, updateOrderStatus } from '../api/admin'

const loading = ref(false)
const orders = ref([])
const query = reactive({
  keyword: '',
  status: '',
  source: '',
})

const statusMap = {
  0: { label: '待处理', type: 'warning' },
  1: { label: '制作中', type: 'primary' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已取消', type: 'info' },
}

async function loadData() {
  loading.value = true
  orders.value = await fetchOrders(query)
  loading.value = false
}

async function changeStatus(row, status) {
  await updateOrderStatus(row.id, status)
  ElMessage.success('订单状态已更新')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-stack">
    <section class="toolbar panel">
      <el-input v-model="query.keyword" :prefix-icon="Search" clearable placeholder="搜索订单号、菜品或备注" />
      <el-select v-model="query.status" clearable placeholder="订单状态">
        <el-option v-for="(item, key) in statusMap" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-select v-model="query.source" clearable placeholder="订单来源">
        <el-option label="普通点餐" :value="0" />
        <el-option label="AI 点餐" :value="1" />
      </el-select>
      <el-button :icon="Search" @click="loadData">筛选</el-button>
    </section>

    <section class="panel">
      <el-table v-loading="loading" :data="orders" row-key="id">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="order-detail">
              <el-table :data="row.items" size="small">
                <el-table-column prop="productName" label="菜品" min-width="160" />
                <el-table-column prop="quantity" label="数量" width="100" />
                <el-table-column label="单价" width="120">
                  <template #default="{ row: item }">¥{{ item.unitPrice.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column label="小计" width="120">
                  <template #default="{ row: item }">¥{{ item.subtotal.toFixed(2) }}</template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status].type">{{ statusMap[row.status].label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="110">
          <template #default="{ row }">
            <el-tag :type="row.source ? 'success' : 'info'">{{ row.source ? 'AI 点餐' : '普通点餐' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" />
        <el-table-column prop="createTime" label="下单时间" min-width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :disabled="row.status !== 0" @click="changeStatus(row, 1)">开始制作</el-button>
            <el-button size="small" type="success" :disabled="row.status !== 1" @click="changeStatus(row, 2)">完成</el-button>
            <el-button size="small" type="info" :disabled="row.status > 1" @click="changeStatus(row, 3)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>
