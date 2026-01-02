<template>
  <div class="forgot-container">
    <el-card class="forgot-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>找回密码</h2>
          <p class="subtitle">请输入注册时使用的邮箱，通过验证码重置密码。</p>
        </div>
      </template>

      <el-steps :active="currentStep" finish-status="success" align-center class="steps">
        <el-step title="验证邮箱" description="确认邮箱已注册" />
        <el-step title="输入验证码" description="查收邮件验证码" />
        <el-step title="重置密码" description="设置新登录密码" />
      </el-steps>

      <div v-if="currentStep === 0" class="step-body">
        <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-position="top">
          <el-form-item label="注册邮箱" prop="email">
            <el-input v-model="emailForm.email" placeholder="请输入注册时使用的邮箱" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleVerifyEmail">下一步</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else-if="currentStep === 1" class="step-body">
        <el-alert
          v-if="emailForm.email"
          type="info"
          show-icon
          :closable="false"
          :title="`验证码已发送至：${maskedEmail}`"
          description="请在 5 分钟内完成验证，若未收到可点击重新发送。"
          class="tip-alert"
        />
        <el-form :model="codeForm" :rules="codeRules" ref="codeFormRef" label-position="top">
          <el-form-item label="验证码" prop="code">
            <el-input v-model="codeForm.code" maxlength="6" placeholder="请输入邮件中的6位验证码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleVerifyCode">下一步</el-button>
            <el-button type="text" :disabled="resendCountdown > 0" @click="handleResendCode">
              <span v-if="resendCountdown > 0">重新发送（{{ resendCountdown }}s）</span>
              <span v-else>重新发送验证码</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else-if="currentStep === 2" class="step-body">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入6-20位新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleResetPassword">完成重置</el-button>
            <el-button @click="goLogin">返回登录</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else class="step-body success-body">
        <el-result icon="success" title="密码重置成功" sub-title="请使用新密码重新登录系统。">
          <template #extra>
            <el-button type="primary" @click="goLogin">去登录</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  verifyEmailForReset,
  sendResetCode,
  verifyResetCode,
  resetPassword
} from '../services/auth'

const router = useRouter()

const currentStep = ref(0)
const loading = ref(false)
const resendCountdown = ref(0)
let countdownTimer = null

const emailFormRef = ref(null)
const codeFormRef = ref(null)
const passwordFormRef = ref(null)

const emailForm = reactive({
  email: ''
})

const codeForm = reactive({
  code: ''
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ]
}

const codeRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '请输入6位验证码', trigger: 'blur' }
  ]
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请再次输入新密码'))
        } else if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const maskedEmail = computed(() => {
  if (!emailForm.email) return ''
  const [name, domain] = emailForm.email.split('@')
  if (!domain) return emailForm.email
  if (name.length <= 2) return `${name[0]}***@${domain}`
  return `${name.slice(0, 2)}***@${domain}`
})

const startCountdown = () => {
  resendCountdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    if (resendCountdown.value <= 1) {
      clearInterval(countdownTimer)
      resendCountdown.value = 0
      return
    }
    resendCountdown.value -= 1
  }, 1000)
}

const handleVerifyEmail = () => {
  emailFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await verifyEmailForReset({ email: emailForm.email })
      await sendResetCode({ email: emailForm.email })
      ElMessage.success('验证通过，验证码已发送到邮箱')
      currentStep.value = 1
      startCountdown()
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || '邮箱验证失败')
    } finally {
      loading.value = false
    }
  })
}

const handleResendCode = async () => {
  if (!emailForm.email || resendCountdown.value > 0) return
  loading.value = true
  try {
    await sendResetCode({ email: emailForm.email })
    ElMessage.success('验证码已重新发送')
    startCountdown()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '发送验证码失败')
  } finally {
    loading.value = false
  }
}

const handleVerifyCode = () => {
  codeFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await verifyResetCode({ email: emailForm.email, verifyCode: codeForm.code })
      ElMessage.success('验证码验证通过，请设置新密码')
      currentStep.value = 2
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || '验证码验证失败')
    } finally {
      loading.value = false
    }
  })
}

const handleResetPassword = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await resetPassword({
        email: emailForm.email,
        verifyCode: codeForm.code,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码重置成功，请使用新密码登录')
      currentStep.value = 3
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || '密码重置失败')
    } finally {
      loading.value = false
    }
  })
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.forgot-card {
  width: 420px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.steps {
  margin-bottom: 24px;
}

.step-body {
  margin-top: 12px;
}

.tip-alert {
  margin-bottom: 16px;
}

.success-body {
  padding: 12px 0;
}

@media (max-width: 480px) {
  .forgot-card {
    width: 100%;
    margin: 0 12px;
  }
}
</style>
