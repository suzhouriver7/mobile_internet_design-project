<template>
  <view class="login-container">
    <view class="login-header">
      <text class="app-title">校园约伴</text>
      <text class="app-subtitle">找到你的校园伙伴</text>
    </view>
    
    <view class="login-form">
      <view class="form-item">
        <input
          v-model="form.identifier"
          class="input"
          placeholder="请输入邮箱或学号"
          type="text"
        />
      </view>
      
      <view class="form-item">
        <input
          v-model="form.password"
          class="input"
          placeholder="请输入密码"
          password
          type="password"
        />
      </view>
      
      <view class="form-actions">
        <text class="link-text" @click="toForgotPassword">忘记密码？</text>
      </view>
      
      <button class="login-btn" :loading="loading" @click="handleLogin">登录</button>
      
      <view class="register-link">
        <text>还没有账号？</text>
        <text class="link-text" @click="toRegister">立即注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import { authApi } from '@/api/index.js'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

const store = useStore()

const form = ref({
  identifier: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.identifier || !form.value.password) {
    showError('请填写完整信息')
    return
  }
  
  loading.value = true
  showLoading('登录中...')
  
  try {
    const response = await authApi.login(form.value)
    await store.dispatch('user/login', response)
    showSuccess('登录成功')
    
    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }, 500)
  } catch (error) {
    showError(error.message || '登录失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}

const toRegister = () => {
  uni.navigateTo({
    url: '/pages/auth/register'
  })
}

const toForgotPassword = () => {
  uni.navigateTo({
    url: '/pages/auth/forgot-password'
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60rpx 40rpx;
}

.login-header {
  text-align: center;
  margin-bottom: 80rpx;
}

.app-title {
  display: block;
  font-size: 64rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 20rpx;
}

.app-subtitle {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-form {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
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

.form-actions {
  text-align: right;
  margin-bottom: 40rpx;
}

.link-text {
  color: #007AFF;
  font-size: 26rpx;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-bottom: 40rpx;
}

.register-link {
  text-align: center;
  font-size: 26rpx;
  color: #666666;
}

.register-link .link-text {
  color: #007AFF;
  margin-left: 10rpx;
}
</style>

