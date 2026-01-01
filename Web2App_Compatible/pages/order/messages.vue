<template>
  <view class="message-page">
    <uni-nav-bar title="订单聊天" left-arrow @clickLeft="onBack"></uni-nav-bar>
    
    <!-- 消息列表 -->
    <view class="message-list" ref="messageList">
      <view 
        v-for="msg in messages" 
        :key="msg.id" 
        class="message-item"
        :class="{ 'sent': isSent(msg) }"
      >
        <uni-image 
          :src="isSent(msg) ? myAvatar : otherAvatar"
          class="message-avatar"
        ></uni-image>
        <view class="message-content">{{ msg.content }}</view>
        <text class="message-time">{{ formatTime(msg.createdAt) }}</text>
      </view>
    </view>
    
    <!-- 消息输入框 -->
    <view class="message-input">
      <uni-easyinput 
        v-model="messageContent" 
        placeholder="输入消息..."
        @confirm="sendMessage"
      ></uni-easyinput>
      <uni-button type="primary" size="mini" @click="sendMessage">发送</uni-button>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad, nextTick } from 'vue'
import { getOrderMessages, sendOrderMessage } from '@/api/order'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const orderId = ref('')
const messages = ref([])
const messageContent = ref('')
const myAvatar = ref('')
const otherAvatar = ref('')
const messageList = ref(null)

onLoad(async (options) => {
  orderId.value = options.orderId
  // 获取当前用户头像
  const userInfo = await userStore.getUserInfo()
  myAvatar.value = userInfo.data.avatar || '/static/images/default-avatar.png'
  // 获取消息列表
  await fetchMessages()
})

// 获取订单消息
const fetchMessages = async () => {
  try {
    const res = await getOrderMessages({
      orderId: orderId.value,
      page: 1,
      size: 100 // 加载更多历史消息
    })
    messages.value = res.data.list
    // 设置对方头像（取第一条消息的非当前用户头像）
    if (messages.value.length) {
      const firstMsg = messages.value[0]
      otherAvatar.value = firstMsg.sender.id === userStore.userId 
        ? firstMsg.receiver.avatarUrl 
        : firstMsg.sender.avatarUrl
    }
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (err) {
    uni.showToast({ title: '获取消息失败', icon: 'none' })
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageContent.value.trim()) return
  
  try {
    const res = await sendOrderMessage(orderId.value, {
      content: messageContent.value
    })
    // 本地添加消息（实际项目可通过WebSocket实时获取）
    messages.value.push(res.data)
    messageContent.value = ''
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (err) {
    uni.showToast({ title: '发送失败', icon: 'none' })
  }
}

// 判断是否为自己发送的消息
const isSent = (msg) => {
  return msg.sender.id === userStore.userId
}

// 滚动到消息底部
const scrollToBottom = () => {
  if (messageList.value) {
    const el = messageList.value.$el
    el.scrollTop = el.scrollHeight
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 返回上一页
const onBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.message-list {
  flex: 1;
  padding: 20rpx;
  height: calc(100vh - 200rpx);
  overflow-y: auto;
  background-color: #f5f5f5;
}

.message-item {
  display: flex;
  margin-bottom: 20rpx;
  align-items: flex-end;
}

.message-item.sent {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin: 0 10rpx;
}

.message-content {
  max-width: 60%;
  padding: 15rpx;
  border-radius: 10rpx;
  background-color: #fff;
  word-wrap: break-word;
}

.message-item.sent .message-content {
  background-color: #409eff;
  color: white;
}

.message-time {
  font-size: 20rpx;
  color: #999;
  margin: 0 10rpx;
}

.message-input {
  display: flex;
  padding: 15rpx;
  border-top: 1px solid #f5f5f5;
  background-color: #fff;
}

.message-input uni-easyinput {
  flex: 1;
  margin-right: 15rpx;
}
</style>