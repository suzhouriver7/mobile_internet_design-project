<template>
  <view class="comments-page">
    <uni-nav-bar title="评论列表" left-arrow @clickLeft="onBack"></uni-nav-bar>
    
    <!-- 评论输入框 -->
    <view class="comment-input">
      <uni-easyinput 
        v-model="commentContent" 
        placeholder="写下你的评论..."
        @confirm="submitComment"
      ></uni-easyinput>
      <uni-button type="primary" size="mini" @click="submitComment">发送</uni-button>
    </view>
    
    <!-- 评论列表 -->
    <view class="comments-list">
      <view v-for="comment in commentsList" :key="comment.id" class="comment-item">
        <view class="comment-header">
          <uni-image 
            :src="comment.user.avatarUrl || '/static/images/default-avatar.png'"
            class="comment-avatar"
          ></uni-image>
          <view class="comment-userinfo">
            <text class="comment-username">{{ comment.user.nickname }}</text>
            <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
          </view>
        </view>
        <view class="comment-content">{{ comment.content }}</view>
        
        <!-- 回复列表 -->
        <view class="replies-list" v-if="comment.replies && comment.replies.length">
          <view v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <view class="reply-header">
              <uni-image 
                :src="reply.user.avatarUrl || '/static/images/default-avatar.png'"
                class="reply-avatar"
              ></uni-image>
              <text class="reply-username">{{ reply.user.nickname }}</text>
            </view>
            <view class="reply-content">{{ reply.content }}</view>
          </view>
        </view>
        
        <view class="comment-actions">
          <text @click="replyToComment(comment.id)">回复</text>
        </view>
      </view>
    </view>
    
    <uni-load-more 
      :status="loadStatus" 
      @clickLoadMore="loadMore"
    ></uni-load-more>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { getComments, addComment } from '@/api/content'

const contentId = ref('')
const commentsList = ref([])
const commentContent = ref('')
const page = ref(1)
const size = ref(20)
const loadStatus = ref('more')

onLoad((options) => {
  contentId.value = options.contentId
  fetchComments(true)
})

// 获取评论列表
const fetchComments = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    loadStatus.value = 'loading'
  } else {
    loadStatus.value = 'loading'
  }
  
  try {
    const res = await getComments({
      contentId: contentId.value,
      page: page.value,
      size: size.value
    })
    
    if (isRefresh) {
      commentsList.value = res.data.list
    } else {
      commentsList.value = [...commentsList.value, ...res.data.list]
    }
    
    loadStatus.value = commentsList.value.length >= res.data.total ? 'noMore' : 'more'
  } catch (err) {
    uni.showToast({ title: '获取评论失败', icon: 'none' })
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) return
  
  try {
    await addComment({
      contentId: contentId.value,
      content: commentContent.value,
      parentId: null // 顶级评论
    })
    commentContent.value = ''
    fetchComments(true) // 刷新评论列表
    uni.showToast({ title: '评论成功', icon: 'success' })
  } catch (err) {
    uni.showToast({ title: '评论失败', icon: 'none' })
  }
}

// 回复评论
const replyToComment = (parentId) => {
  // 实际项目中可实现回复弹窗
  uni.showModal({
    title: '回复',
    inputPlaceholder: '请输入回复内容',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          await addComment({
            contentId: contentId.value,
            content: res.content,
            parentId
          })
          fetchComments(true)
          uni.showToast({ title: '回复成功', icon: 'success' })
        } catch (err) {
          uni.showToast({ title: '回复失败', icon: 'none' })
        }
      }
    }
  })
}

// 加载更多
const loadMore = () => {
  if (loadStatus.value !== 'more') return
  page.value++
  fetchComments()
}

// 格式化时间
const formatTime = (timeStr) => {
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 返回上一页
const onBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.comment-input {
  display: flex;
  padding: 15rpx;
  border-bottom: 1px solid #f5f5f5;
  background-color: #fff;
}

.comment-input uni-easyinput {
  flex: 1;
  margin-right: 15rpx;
}

.comments-list {
  padding: 15rpx;
}

.comment-item {
  padding: 20rpx 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.comment-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 15rpx;
}

.comment-username {
  font-size: 28rpx;
  font-weight: 500;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
  margin-left: 10rpx;
}

.comment-content {
  font-size: 28rpx;
  margin-bottom: 10rpx;
  line-height: 1.5;
}

.replies-list {
  margin-left: 75rpx;
  padding: 10rpx 0;
  background-color: #f9f9f9;
  border-radius: 8rpx;
  padding: 10rpx;
  margin-bottom: 10rpx;
}

.reply-item {
  margin-bottom: 10rpx;
}

.reply-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  margin-right: 10rpx;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5rpx;
}

.reply-username {
  font-size: 24rpx;
  color: #666;
}

.reply-content {
  font-size: 26rpx;
  margin-left: 50rpx;
}

.comment-actions {
  font-size: 26rpx;
  color: #409eff;
  margin-top: 10rpx;
}
</style>