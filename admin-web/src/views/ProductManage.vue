<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { fetchProducts, removeProduct, saveProduct, updateProductStatus } from '../api/admin'

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const products = ref([])
const query = reactive({
  keyword: '',
  category: '',
  status: '',
})

const form = reactive({
  id: null,
  name: '',
  category: '主食',
  price: 0,
  costPrice: 0,
  stock: 0,
  status: 1,
  tasteTags: '',
  description: '',
  imageUrl: '',
  cookTime: 10,
})

const categories = computed(() => [...new Set(products.value.map((item) => item.category))])
const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  products.value = await fetchProducts(query)
  loading.value = false
}

function openCreate() {
  Object.assign(form, {
    id: null,
    name: '',
    category: '主食',
    price: 0,
    costPrice: 0,
    stock: 0,
    status: 1,
    tasteTags: '',
    description: '',
    imageUrl: '',
    cookTime: 10,
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  await saveProduct({
    ...form,
    price: Number(form.price),
    costPrice: Number(form.costPrice),
    stock: Number(form.stock),
    cookTime: Number(form.cookTime),
  })
  ElMessage.success(form.id ? '菜品已更新' : '菜品已新增')
  dialogVisible.value = false
  loadData()
}

async function toggleStatus(row) {
  await updateProductStatus(row.id, row.status ? 0 : 1)
  ElMessage.success(row.status ? '已下架' : '已上架')
  loadData()
}

async function handleRemove(row) {
  await ElMessageBox.confirm(`确认删除菜品“${row.name}”？`, '删除确认', { type: 'warning' })
  await removeProduct(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-stack">
    <section class="toolbar panel">
      <el-input v-model="query.keyword" :prefix-icon="Search" clearable placeholder="搜索菜品、分类或标签" />
      <el-select v-model="query.category" clearable placeholder="分类">
        <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button :icon="Search" @click="loadData">筛选</el-button>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增菜品</el-button>
    </section>

    <section class="panel">
      <el-table v-loading="loading" :data="products" row-key="id">
        <el-table-column prop="name" label="菜品名称" min-width="150" fixed />
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column label="价格" width="110">
          <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="成本价" width="110">
          <template #default="{ row }">¥{{ Number(row.costPrice || row.price * 0.42).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tasteTags" label="口味标签" min-width="180" />
        <el-table-column prop="cookTime" label="出餐时间" width="110">
          <template #default="{ row }">{{ row.cookTime }} 分钟</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="toggleStatus(row)">{{ row.status ? '下架' : '上架' }}</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜品' : '新增菜品'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="菜品名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" allow-create filterable>
                <el-option label="主食" value="主食" />
                <el-option label="饮品" value="饮品" />
                <el-option label="小吃" value="小吃" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" :step="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :step="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出餐时间">
              <el-input-number v-model="form.cookTime" :min="1" :step="1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="口味标签">
              <el-input v-model="form.tasteTags" placeholder="清淡,热销,饱腹" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜品描述">
              <el-input v-model="form.description" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
