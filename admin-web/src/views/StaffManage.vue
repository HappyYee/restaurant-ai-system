<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { fetchStaff, removeStaff, saveStaff, updateStaffStatus } from '../api/admin'

const loading = ref(false)
const staff = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({
  keyword: '',
  role: '',
  status: '',
})

const form = reactive({
  id: null,
  name: '',
  phone: '',
  role: '前台',
  shift: '早班',
  salaryType: '月薪',
  monthlySalary: 0,
  hourlyWage: 0,
  workHoursThisMonth: 0,
  status: 1,
  hireDate: '',
  remark: '',
})

const roles = computed(() => [...new Set(staff.value.map((item) => item.role))])
const activeStaff = computed(() => staff.value.filter((item) => item.status === 1))
const monthlyLaborCost = computed(() =>
  activeStaff.value.reduce((total, item) => {
    if (item.salaryType === '时薪') {
      return total + Number(item.hourlyWage || 0) * Number(item.workHoursThisMonth || 0)
    }
    return total + Number(item.monthlySalary || 0)
  }, 0),
)

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  shift: [{ required: true, message: '请选择班次', trigger: 'change' }],
  salaryType: [{ required: true, message: '请选择薪资类型', trigger: 'change' }],
}

async function loadData() {
  loading.value = true
  staff.value = await fetchStaff(query)
  loading.value = false
}

function openCreate() {
  Object.assign(form, {
    id: null,
    name: '',
    phone: '',
    role: '前台',
    shift: '早班',
    salaryType: '月薪',
    monthlySalary: 0,
    hourlyWage: 0,
    workHoursThisMonth: 0,
    status: 1,
    hireDate: '',
    remark: '',
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  await saveStaff(form)
  ElMessage.success(form.id ? '人员信息已更新' : '人员已新增')
  dialogVisible.value = false
  loadData()
}

async function toggleStatus(row) {
  await updateStaffStatus(row.id, row.status ? 0 : 1)
  ElMessage.success(row.status ? '已停用' : '已启用')
  loadData()
}

async function handleRemove(row) {
  await ElMessageBox.confirm(`确认删除员工“${row.name}”？`, '删除确认', { type: 'warning' })
  await removeStaff(row.id)
  ElMessage.success('已删除')
  loadData()
}

function costText(row) {
  if (row.salaryType === '时薪') {
    return `¥${(Number(row.hourlyWage || 0) * Number(row.workHoursThisMonth || 0)).toFixed(2)}`
  }
  return `¥${Number(row.monthlySalary || 0).toFixed(2)}`
}

onMounted(loadData)
</script>

<template>
  <div class="page-stack">
    <section class="metric-grid compact">
      <article class="metric-card">
        <span>在职人员</span>
        <strong>{{ activeStaff.length }}</strong>
        <p>当前启用的门店人员</p>
      </article>
      <article class="metric-card">
        <span>本月人力成本</span>
        <strong>¥{{ monthlyLaborCost.toFixed(2) }}</strong>
        <p>月薪与兼职时薪合计</p>
      </article>
    </section>

    <section class="toolbar panel">
      <el-input v-model="query.keyword" :prefix-icon="Search" clearable placeholder="搜索姓名、手机号、岗位或班次" />
      <el-select v-model="query.role" clearable placeholder="岗位">
        <el-option v-for="item in roles" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="在职" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button :icon="Search" @click="loadData">筛选</el-button>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增人员</el-button>
    </section>

    <section class="panel">
      <el-table v-loading="loading" :data="staff" row-key="id">
        <el-table-column prop="name" label="姓名" min-width="120" fixed />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="role" label="岗位" width="100" />
        <el-table-column prop="shift" label="班次" width="110" />
        <el-table-column label="薪资类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.salaryType === '时薪' ? 'warning' : 'primary'">{{ row.salaryType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="本月成本" width="130">
          <template #default="{ row }">{{ costText(row) }}</template>
        </el-table-column>
        <el-table-column prop="workHoursThisMonth" label="本月工时" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '在职' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hireDate" label="入职日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="toggleStatus(row)">{{ row.status ? '停用' : '启用' }}</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑人员' : '新增人员'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="role">
              <el-select v-model="form.role" allow-create filterable>
                <el-option label="店长" value="店长" />
                <el-option label="后厨" value="后厨" />
                <el-option label="前台" value="前台" />
                <el-option label="兼职" value="兼职" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班次" prop="shift">
              <el-select v-model="form.shift" allow-create filterable>
                <el-option label="早班" value="早班" />
                <el-option label="午晚班" value="午晚班" />
                <el-option label="晚班" value="晚班" />
                <el-option label="全日班" value="全日班" />
                <el-option label="休假" value="休假" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资类型" prop="salaryType">
              <el-segmented v-model="form.salaryType" :options="['月薪', '时薪']" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="在职" inactive-text="停用" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月薪">
              <el-input-number v-model="form.monthlySalary" :min="0" :precision="2" :step="100" :disabled="form.salaryType !== '月薪'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时薪">
              <el-input-number v-model="form.hourlyWage" :min="0" :precision="2" :step="1" :disabled="form.salaryType !== '时薪'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="本月工时">
              <el-input-number v-model="form.workHoursThisMonth" :min="0" :precision="1" :step="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="form.hireDate" value-format="YYYY-MM-DD" type="date" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
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
