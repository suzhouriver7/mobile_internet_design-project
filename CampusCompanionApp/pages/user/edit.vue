<template>
  <view class="edit-container">
    <view class="form">
      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="form.nickname" class="input" placeholder="请输入昵称" />
      </view>
      
      <view class="form-item">
        <text class="label">个性签名</text>
        <textarea
          v-model="form.signature"
          class="textarea"
          placeholder="请输入个性签名"
          maxlength="100"
        />
      </view>
      
      <button class="submit-btn" :loading="loading" @click="handleSubmit">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

const store = useStore()

const form = ref({
  nickname: '',
  signature: ''
})

const loading = ref(false)

onMounted(() => {
  const userInfo = store.getters['user/userInfo']
  if (userInfo) {
    form.value.nickname = userInfo.nickname || ''
    form.value.signature = userInfo.signature || ''
  }
})

const handleSubmit = async () => {
  if (!form.value.nickname.trim()) {
    showError('请输入昵称')
    return
  }
  
  loading.value = true
  showLoading('保存中...')
  
  try {
    await store.dispatch('user/updateUserInfo', {
      nickname: form.value.nickname,
      signature: form.value.signature
    })
    showSuccess('保存成功')
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error) {
    showError(error.message || '保存失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}
</script>

<style scoped>
.edit-container {
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

.textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
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

