<template>
  <view class="chat-container">
    <scroll-view class="messages-scroll" scroll-y :scroll-top="scrollTop">
      <view
        v-for="(msg, index) in messages"
        :key="index"
        class="message-item"
        :class="{ 'user': msg.role === 'user' }"
      >
        <text class="message-text">{{ msg.content }}</text>
      </view>
    </scroll-view>
    
    <view class="input-bar">
      <input
        v-model="inputText"
        class="input"
        placeholder="输入消息..."
        @confirm="sendMessage"
      />
      <button class="send-btn" :disabled="loading" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { aiApi } from '@/api/index.js'
import { showError } from '@/utils/util.js'

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const scrollTop = ref(0)

const sendMessage = async () => {
  if (!inputText.value.trim() || loading.value) return
  
  const userMessage = inputText.value.trim()
  inputText.value = ''
  
  messages.value.push({
    role: 'user',
    content: userMessage
  })
  
  scrollToBottom()
  
  loading.value = true
  
  try {
    const reply = await aiApi.simpleChat(userMessage)
    messages.value.push({
      role: 'assistant',
      content: reply
    })
    scrollToBottom()
  } catch (error) {
    showError(error.message || 'AI回复失败')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用'
    })
  } finally {
    loading.value = false
  }
}

const scrollToBottom = () => {
  setTimeout(() => {
    scrollTop.value = 99999
  }, 100)
}

onMounted(() => {
  messages.value.push({
    role: 'assistant',
    content: '你好！我是校园约伴AI助手，有什么可以帮助你的吗？'
  })
})
</script>

<style scoped>
.chat-container {
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
  margin-bottom: 30rpx;
  display: flex;
  justify-content: flex-start;
}

.message-item.user {
  justify-content: flex-end;
}

.message-text {
  max-width: 70%;
  padding: 20rpx 24rpx;
  background: #ffffff;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333333;
  line-height: 1.6;
}

.message-item.user .message-text {
  background: #007AFF;
  color: #ffffff;
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

.send-btn[disabled] {
  background: #cccccc;
}
</style>

