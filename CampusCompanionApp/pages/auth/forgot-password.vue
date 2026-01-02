<template>
  <view class="forgot-password-container">
    <view class="form">
      <view class="step-indicator">
        <text class="step-text">步骤 {{ currentStep }}/3</text>
      </view>
      
      <!-- 步骤1: 验证邮箱 -->
      <view v-if="currentStep === 1" class="step-content">
        <view class="form-item">
          <text class="label">邮箱</text>
          <input
            v-model="form.email"
            class="input"
            placeholder="请输入注册邮箱"
            type="text"
          />
        </view>
        <button class="submit-btn" :loading="loading" @click="handleVerifyEmail">下一步</button>
      </view>
      
      <!-- 步骤2: 输入验证码 -->
      <view v-if="currentStep === 2" class="step-content">
        <view class="form-item">
          <text class="label">验证码</text>
          <view class="verify-code-row">
            <input
              v-model="form.verifyCode"
              class="input verify-input"
              placeholder="请输入验证码"
              type="number"
            />
            <button class="code-btn" :disabled="codeCountdown > 0" @click="sendCode">
              {{ codeCountdown > 0 ? `${codeCountdown}秒` : '重新发送' }}
            </button>
          </view>
        </view>
        <button class="submit-btn" :loading="loading" @click="handleVerifyCode">下一步</button>
      </view>
      
      <!-- 步骤3: 重置密码 -->
      <view v-if="currentStep === 3" class="step-content">
        <view class="form-item">
          <text class="label">新密码</text>
          <input
            v-model="form.newPassword"
            class="input"
            placeholder="请输入新密码（6-20位）"
            password
            type="password"
          />
        </view>
        <view class="form-item">
          <text class="label">确认新密码</text>
          <input
            v-model="form.confirmPassword"
            class="input"
            placeholder="请再次输入新密码"
            password
            type="password"
          />
        </view>
        <button class="submit-btn" :loading="loading" @click="handleResetPassword">完成</button>
      </view>
      
      <view class="back-link">
        <text class="link-text" @click="toLogin">返回登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api/index.js'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

const currentStep = ref(1)
const loading = ref(false)
const codeCountdown = ref(0)

const form = ref({
  email: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: ''
})

const handleVerifyEmail = async () => {
  if (!form.value.email) {
    showError('请输入邮箱')
    return
  }
  
  loading.value = true
  showLoading('验证中...')
  
  try {
    await authApi.verifyEmailForReset(form.value.email)
    showSuccess('邮箱验证通过')
    currentStep.value = 2
    sendCode()
  } catch (error) {
    showError(error.message || '验证失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}

const sendCode = async () => {
  try {
    await authApi.sendResetCode(form.value.email)
    showSuccess('验证码已发送')
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    showError(error.message || '发送失败')
  }
}

const handleVerifyCode = async () => {
  if (!form.value.verifyCode) {
    showError('请输入验证码')
    return
  }
  
  loading.value = true
  showLoading('验证中...')
  
  try {
    await authApi.verifyResetCode(form.value.email, parseInt(form.value.verifyCode))
    showSuccess('验证码正确')
    currentStep.value = 3
  } catch (error) {
    showError(error.message || '验证失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}

const handleResetPassword = async () => {
  if (!form.value.newPassword || !form.value.confirmPassword) {
    showError('请填写完整信息')
    return
  }
  
  if (form.value.newPassword !== form.value.confirmPassword) {
    showError('两次密码不一致')
    return
  }
  
  if (form.value.newPassword.length < 6 || form.value.newPassword.length > 20) {
    showError('密码长度应为6-20位')
    return
  }
  
  loading.value = true
  showLoading('重置中...')
  
  try {
    await authApi.resetPassword(
      form.value.email,
      parseInt(form.value.verifyCode),
      form.value.newPassword
    )
    showSuccess('密码重置成功')
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error) {
    showError(error.message || '重置失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}

const toLogin = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.forgot-password-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 40rpx;
}

.form {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.step-indicator {
  text-align: center;
  margin-bottom: 40rpx;
}

.step-text {
  font-size: 28rpx;
  color: #007AFF;
}

.step-content {
  display: flex;
  flex-direction: column;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 16rpx;
}

.input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.verify-code-row {
  display: flex;
  gap: 20rpx;
}

.verify-input {
  flex: 1;
}

.code-btn {
  width: 200rpx;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 26rpx;
  border: none;
  padding: 0;
}

.code-btn[disabled] {
  background: #cccccc;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-top: 20rpx;
}

.back-link {
  text-align: center;
  margin-top: 40rpx;
}

.link-text {
  color: #007AFF;
  font-size: 26rpx;
}
</style>

