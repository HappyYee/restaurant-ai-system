<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref()
const form = reactive({
  username: 'admin',
  password: '123456',
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/dashboard')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <p class="eyebrow">Restaurant AI Admin</p>
        <h1>餐饮门店智能经营后台</h1>
        <p>用于菜品维护、订单处理、库存预警和经营数据分析。</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="submit">
        <h2>管理员登录</h2>
        <el-form-item prop="username">
          <el-input v-model="form.username" :prefix-icon="User" size="large" placeholder="账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            size="large"
            type="password"
            show-password
            placeholder="密码"
          />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="submit">登录后台</el-button>
        <p class="login-hint">演示账号：admin / 123456</p>
      </el-form>
    </section>
  </main>
</template>
