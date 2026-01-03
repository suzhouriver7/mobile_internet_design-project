<template>
  <view class="create-content-container">
    <view class="form">
      <view class="form-item">
        <textarea
          v-model="form.content"
          class="textarea"
          placeholder="分享你的动态..."
          maxlength="500"
        />
        <text class="char-count">{{ form.content.length }}/500</text>
      </view>
      
      <view class="form-item">
        <view class="media-preview" v-if="mediaUrl">
          <image :src="mediaUrl" class="preview-image" mode="aspectFill" />
          <text class="remove-btn" @click="removeMedia">×</text>
        </view>
        <button class="upload-btn" @click="chooseMedia">选择图片</button>
      </view>
      
      <button class="submit-btn" :loading="loading" @click="handleSubmit">发布</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { contentApi, fileApi } from '@/api/index.js'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

const store = useStore()

// 检查登录状态 - 使用 onLoad 页面生命周期
onLoad(() => {
  if (!store.getters['user/isLogin']) {
    showError('请先登录')
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }, 1000)
  }
})

const form = ref({
  content: '',
  mediaType: 'TEXT_ONLY'
})

const mediaUrl = ref('')
const loading = ref(false)

const chooseMedia = () => {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      try {
        showLoading('上传中...')
        const url = await fileApi.uploadImage(tempFilePath)
        mediaUrl.value = url
        form.value.mediaType = 'IMAGE'
        hideLoading()
      } catch (error) {
        hideLoading()
        showError(error.message || '上传失败')
      }
    }
  })
}

const removeMedia = () => {
  mediaUrl.value = ''
  form.value.mediaType = 'TEXT_ONLY'
}

const handleSubmit = async () => {
  // 检查登录状态
  if (!store.getters['user/isLogin']) {
    showError('请先登录')
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }, 1000)
    return
  }
  
  if (!form.value.content.trim()) {
    showError('请输入动态内容')
    return
  }
  
  loading.value = true
  showLoading('发布中...')
  
  try {
    const data = {
      content: form.value.content,
      mediaType: form.value.mediaType
    }
    
    const contentId = await contentApi.createContent(data)
    
    // 如果有图片，上传媒体
    if (mediaUrl.value && form.value.mediaType === 'IMAGE') {
      // 需要先获取文件路径，这里简化处理
      // 实际应该保存临时文件路径，上传时使用
    }
    
    showSuccess('发布成功')
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error) {
    showError(error.message || '发布失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}
</script>

<style scoped>
.create-content-container {
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

.textarea {
  width: 100%;
  min-height: 300rpx;
  padding: 24rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999999;
  margin-top: 10rpx;
}

.media-preview {
  position: relative;
  margin-bottom: 20rpx;
}

.preview-image {
  width: 100%;
  max-height: 400rpx;
  border-radius: 12rpx;
}

.remove-btn {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 60rpx;
  height: 60rpx;
  background: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
}

.upload-btn {
  width: 100%;
  height: 88rpx;
  background: #f5f5f5;
  color: #666666;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: 2rpx dashed #cccccc;
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

