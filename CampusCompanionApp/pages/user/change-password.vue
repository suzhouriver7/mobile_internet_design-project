<template>
  <view class="change-password-container">
    <view class="form">
      <view class="form-item">
        <text class="label">旧密码</text>
        <input
          v-model="form.oldPassword"
          class="input"
          placeholder="请输入旧密码"
          password
          type="password"
        />
      </view>
      
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
      
      <button class="submit-btn" :loading="loading" @click="handleSubmit">修改密码</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import { userApi } from '@/api/index.js'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

const store = useStore()

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!form.value.oldPassword || !form.value.newPassword || !form.value.confirmPassword) {
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
  showLoading('修改中...')
  
  try {
    const userId = store.getters['user/userId']
    await userApi.changePassword(userId, form.value.oldPassword, form.value.newPassword)
    showSuccess('修改成功')
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error) {
    showError(error.message || '修改失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}
</script>

<style scoped>
.change-password-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 30rpx;
}

.form {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 40rpx;
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

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-top: 40rpx;
}
</style>

