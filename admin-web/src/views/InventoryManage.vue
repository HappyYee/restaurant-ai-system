<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { fetchProducts, updateProductStock } from '../api/admin'

const loading = ref(false)
const products = ref([])

const warningProducts = computed(() => products.value.filter((item) => item.stock <= 10))

async function loadData() {
  loading.value = true
  products.value = await fetchProducts({})
  loading.value = false
}

async function changeStock(row, stock) {
  await updateProductStock(row.id, stock)
  ElMessage.success('库存已更新')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-stack">
    <section class="metric-grid compact">
      <article class="metric-card">
        <span>库存预警</span>
        <strong>{{ warningProducts.length }}</strong>
        <p>库存低于或等于 10 的菜品</p>
      </article>
      <article class="metric-card">
        <span>总库存</span>
        <strong>{{ products.reduce((total, item) => total + item.stock, 0) }}</strong>
        <p>所有上架与下架菜品合计</p>
      </article>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>库存列表</h2>
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      </div>
      <el-table v-loading="loading" :data="products" row-key="id">
        <el-table-column prop="name" label="菜品名称" min-width="160" />
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存" min-width="180">
          <template #default="{ row }">
            <el-input-number :model-value="row.stock" :min="0" :step="1" @change="(value) => changeStock(row, value)" />
          </template>
        </el-table-column>
        <el-table-column label="预警" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.stock <= 10" type="danger">需要补货</el-tag>
            <el-tag v-else type="success">充足</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tasteTags" label="口味标签" min-width="200" />
      </el-table>
    </section>
  </div>
</template>
