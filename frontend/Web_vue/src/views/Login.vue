<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>校园约伴系统</h2>
      </template>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
      >
        <el-form-item label="学号/邮箱" prop="identifier">
          <el-input v-model="loginForm.identifier" placeholder="请输入学号或邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <div class="login-actions">
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <div class="login-links">
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
            <router-link to="/register" class="register-link">注册</router-link>
          </div>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref(null)

const loginForm = reactive({
  identifier: '',
  password: ''
})

const loginRules = {
  identifier: [
    { required: true, message: '请输入学号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await authStore.login(loginForm)
    await router.push('/')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.login-card {
  width: 300px;
  margin: 0;
}

.login-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.login-links {
  display: flex;
  gap: 12px;
  align-items: center;
}

.register-link,
.forgot-link {
  color: #409eff;
  text-decoration: none;
}
</style>