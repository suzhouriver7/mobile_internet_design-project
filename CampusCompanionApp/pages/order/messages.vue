<template>
  <view class="messages-container">
    <scroll-view class="messages-scroll" scroll-y :scroll-top="scrollTop">
      <view
        v-for="msg in messages"
        :key="msg.mid || msg.id"
        class="message-item"
        :class="{ 'own': isOwnMessage(msg) }"
      >
        <image
          v-if="msg.sender && msg.sender.avatarUrl"
          :src="msg.sender.avatarUrl"
          class="message-avatar"
          mode="aspectFill"
        />
        <view class="message-content">
          <text class="message-text">{{ msg.content }}</text>
          <text class="message-time">{{ formatTime(msg.createdAt, 'HH:mm') }}</text>
        </view>
      </view>
    </scroll-view>
    
    <view class="input-bar">
      <input
        v-model="inputText"
        class="input"
        placeholder="输入消息..."
        @confirm="sendMessage"
      />
      <button class="send-btn" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { orderApi } from '@/api/index.js'
import { formatTime, showError } from '@/utils/util.js'

const store = useStore()

const orderId = ref(null)
const messages = ref([])
const inputText = ref('')
const scrollTop = ref(0)

const isOwnMessage = (msg) => {
  const userId = store.getters['user/userId']
  return msg.sender && (msg.sender.uid === userId || msg.sender.id === userId)
}

const loadMessages = async () => {
  try {
    const result = await orderApi.getOrderMessages(orderId.value, 1, 50)
    messages.value = result.list || []
    scrollToBottom()
  } catch (error) {
    showError(error.message || '加载失败')
  }
}

const sendMessage = async () => {
  if (!inputText.value.trim()) return
  
  if (!store.getters['user/isLogin']) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  
  try {
    await orderApi.sendOrderMessage(orderId.value, inputText.value)
    inputText.value = ''
    loadMessages()
  } catch (error) {
    showError(error.message || '发送失败')
  }
}

const scrollToBottom = () => {
  setTimeout(() => {
    scrollTop.value = 99999
  }, 100)
}

import { onLoad } from '@dcloudio/uni-app'

onLoad((options) => {
  if (options.orderId) {
    orderId.value = options.orderId
    loadMessages()
  } else {
    showError('缺少订单ID')
  }
})
</script>

<style scoped>
.messages-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f8f8f8;
}

.messages-scroll {
  flex: 1;
  height: 0;
  padding: 20rpx;
}

.message-item {
  display: flex;
  gap: 16rpx;
  margin-bottom: 30rpx;
}

.message-item.own {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.message-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.message-item.own .message-content {
  align-items: flex-end;
}

.message-text {
  padding: 20rpx 24rpx;
  background: #ffffff;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333333;
  line-height: 1.5;
}

.message-item.own .message-text {
  background: #007AFF;
  color: #ffffff;
}

.message-time {
  font-size: 22rpx;
  color: #999999;
}

.input-bar {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  background: #ffffff;
  border-top: 2rpx solid #f0f0f0;
}

.input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.send-btn {
  width: 120rpx;
  height: 72rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
  padding: 0;
}
</style>

