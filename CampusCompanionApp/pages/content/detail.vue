<template>
  <view class="content-detail-container">
    <view v-if="contentDetail" class="detail-content">
      <!-- 用户信息 -->
      <view class="user-section">
        <image
          v-if="contentDetail.user && contentDetail.user.avatarUrl"
          :src="contentDetail.user.avatarUrl"
          class="user-avatar"
          mode="aspectFill"
        />
        <view class="user-info">
          <text class="user-nickname">{{ contentDetail.user ? contentDetail.user.nickname : '匿名' }}</text>
          <text class="content-time">{{ formatRelativeTime(contentDetail.createdAt) }}</text>
        </view>
      </view>
      
      <!-- 内容 -->
      <text class="content-text">{{ contentDetail.content }}</text>
      
      <!-- 媒体 -->
      <view v-if="contentDetail.media && contentDetail.media.length > 0" class="media-section">
        <image
          v-for="(media, index) in contentDetail.media"
          :key="index"
          :src="media.url"
          class="media-image"
          mode="widthFix"
          @click="previewImage(media.url, contentDetail.media)"
        />
      </view>
      
      <!-- 操作栏 -->
      <view class="action-bar">
        <view class="action-item" @click="handleLike">
          <text :class="contentDetail.liked ? 'liked' : ''">👍</text>
          <text>{{ contentDetail.likeCount || 0 }}</text>
        </view>
        <view class="action-item" @click="showCommentInput = true">
          <text>💬</text>
          <text>{{ contentDetail.commentCount || 0 }}</text>
        </view>
      </view>
      
      <!-- 评论输入 -->
      <view v-if="showCommentInput" class="comment-input-section">
        <textarea
          v-model="commentText"
          class="comment-input"
          placeholder="写评论..."
          maxlength="500"
        />
        <view class="comment-actions">
          <button class="cancel-btn" @click="showCommentInput = false">取消</button>
          <button class="submit-btn" @click="handleComment">发布</button>
        </view>
      </view>
      
      <!-- 评论列表 -->
      <view class="comments-section">
        <view class="section-title">评论</view>
        <view v-if="comments.length > 0" class="comments-list">
          <view
            v-for="comment in comments"
            :key="comment.pid || comment.id"
            class="comment-item"
          >
            <image
              v-if="comment.user && comment.user.avatarUrl"
              :src="comment.user.avatarUrl"
              class="comment-avatar"
              mode="aspectFill"
            />
            <view class="comment-content">
              <text class="comment-nickname">{{ comment.user ? comment.user.nickname : '匿名' }}</text>
              <text class="comment-text">{{ comment.content }}</text>
              <text class="comment-time">{{ formatRelativeTime(comment.createdAt) }}</text>
            </view>
          </view>
        </view>
        <view v-else class="empty-state">
          <text class="empty-text">暂无评论</text>
        </view>
      </view>
    </view>
    
    <view v-else-if="loading" class="loading-state">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { contentApi } from '@/api/index.js'
import { formatRelativeTime, showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

const store = useStore()

const contentDetail = ref(null)
const comments = ref([])
const loading = ref(false)
const showCommentInput = ref(false)
const commentText = ref('')

const contentId = ref(null)

const loadContentDetail = async () => {
  if (!contentId.value) {
    showError('缺少内容ID')
    return
  }
  
  loading.value = true
  showLoading('加载中...')
  
  try {
    const detail = await contentApi.getContentDetail(contentId.value)
    contentDetail.value = detail
    
    // 加载评论
    const result = await contentApi.getComments(contentId.value, 1, 50)
    comments.value = result.list || []
  } catch (error) {
    showError(error.message || '加载失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}

const handleLike = async () => {
  if (!store.getters['user/isLogin']) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  
  try {
    const result = await contentApi.likeContent(contentId.value)
    contentDetail.value.liked = result.liked
    contentDetail.value.likeCount = result.count
  } catch (error) {
    showError(error.message || '操作失败')
  }
}

const handleComment = async () => {
  if (!commentText.value.trim()) {
    showError('请输入评论内容')
    return
  }
  
  if (!store.getters['user/isLogin']) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  
  try {
    await contentApi.createComment(contentId.value, {
      content: commentText.value
    })
    showSuccess('评论成功')
    commentText.value = ''
    showCommentInput.value = false
    loadContentDetail()
  } catch (error) {
    showError(error.message || '评论失败')
  }
}

const previewImage = (current, urls) => {
  const urlList = urls.map(m => m.url)
  uni.previewImage({
    current: current,
    urls: urlList
  })
}

// uni-app 中页面参数通过 onLoad 获取
onLoad((options) => {
  if (options.id) {
    contentId.value = options.id
    loadContentDetail()
  } else {
    showError('缺少内容ID')
  }
})
</script>

<style scoped>
.content-detail-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 30rpx;
}

.detail-content {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-nickname {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.content-time {
  font-size: 24rpx;
  color: #999999;
}

.content-text {
  font-size: 30rpx;
  color: #333333;
  line-height: 1.8;
  margin-bottom: 24rpx;
}

.media-section {
  margin-bottom: 24rpx;
}

.media-image {
  width: 100%;
  border-radius: 12rpx;
  margin-bottom: 10rpx;
}

.action-bar {
  display: flex;
  gap: 40rpx;
  padding: 24rpx 0;
  border-top: 2rpx solid #f0f0f0;
  border-bottom: 2rpx solid #f0f0f0;
  margin-bottom: 24rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: #666666;
}

.action-item .liked {
  color: #ff3b30;
}

.comment-input-section {
  margin-bottom: 24rpx;
}

.comment-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 12rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.cancel-btn,
.submit-btn {
  padding: 16rpx 40rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666666;
}

.submit-btn {
  background: #007AFF;
  color: #ffffff;
}

.comments-section {
  margin-top: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.comment-item {
  display: flex;
  gap: 16rpx;
}

.comment-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.comment-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.comment-nickname {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
}

.comment-text {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.6;
}

.comment-time {
  font-size: 24rpx;
  color: #999999;
}

.empty-state,
.loading-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-text,
.loading-text {
  font-size: 28rpx;
  color: #999999;
}
</style>

