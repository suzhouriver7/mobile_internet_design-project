<template>
  <view class="register-container">
    <view class="register-header">
      <text class="title">注册账号</text>
    </view>
    
    <view class="register-form">
      <view class="form-item">
        <input
          v-model="form.email"
          class="input"
          placeholder="请输入邮箱"
          type="text"
        />
      </view>
      
      <view class="form-item">
        <input
          v-model="form.nickname"
          class="input"
          placeholder="请输入昵称"
          type="text"
        />
      </view>
      
      <view class="form-item">
        <input
          v-model="form.password"
          class="input"
          placeholder="请输入密码（6-20位）"
          password
          type="password"
        />
      </view>
      
      <view class="form-item">
        <input
          v-model="form.confirmPassword"
          class="input"
          placeholder="请确认密码"
          password
          type="password"
        />
      </view>
      
      <view class="form-item">
        <view class="verify-code-row">
          <input
            v-model="form.verifycode"
            class="input verify-input"
            placeholder="请输入验证码"
            type="number"
          />
          <button class="code-btn" :disabled="codeCountdown > 0" @click="sendVerifyCode">
            {{ codeCountdown > 0 ? `${codeCountdown}秒` : '获取验证码' }}
          </button>
        </view>
      </view>
      
      <button class="register-btn" :loading="loading" @click="handleRegister">注册</button>
      
      <view class="login-link">
        <text>已有账号？</text>
        <text class="link-text" @click="toLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api/index.js'
import verifyApi from '@/api/verify.js'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

const form = ref({
  email: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  verifycode: ''
})

const loading = ref(false)
const codeCountdown = ref(0)

const sendVerifyCode = async () => {
  if (!form.value.email) {
    showError('请先输入邮箱')
    return
  }
  
  try {
    await verifyApi.sendRegisterCode(form.value.email)
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

const handleRegister = async () => {
  if (!form.value.email || !form.value.nickname || !form.value.password || !form.value.verifycode) {
    showError('请填写完整信息')
    return
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    showError('两次密码不一致')
    return
  }
  
  if (form.value.password.length < 6 || form.value.password.length > 20) {
    showError('密码长度应为6-20位')
    return
  }
  
  loading.value = true
  showLoading('注册中...')
  
  try {
    await authApi.register({
      email: form.value.email,
      nickname: form.value.nickname,
      password: form.value.password,
      verifycode: parseInt(form.value.verifycode)
    })
    showSuccess('注册成功')
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error) {
    showError(error.message || '注册失败')
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
.register-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 40rpx;
}

.register-header {
  margin-bottom: 40rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
}

.register-form {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
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

.register-btn {
  width: 100%;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-top: 20rpx;
  margin-bottom: 40rpx;
}

.login-link {
  text-align: center;
  font-size: 26rpx;
  color: #666666;
}

.link-text {
  color: #007AFF;
  margin-left: 10rpx;
}
</style>

