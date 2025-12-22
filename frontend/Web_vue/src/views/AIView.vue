<template>
  <div class="ai-view">
    <el-card shadow="hover" class="ai-card">
      <template #header>
        <div class="card-header">
          <h2>AI问询</h2>
          <span class="subtitle">在这里，你可以与AI进行对话，获取各种信息</span>
        </div>
      </template>
      <div class="ai-chat-container">
        <div class="chat-messages" ref="chatContainer">
          <div v-for="message in messages" :key="message.id" :class="['message', message.role]">
            <div class="message-avatar">
              <el-avatar :src="message.role === 'user' ? userAvatar : aiAvatar" size="small" />
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div v-if="message.role === 'assistant' && message.loading" class="loading-indicator">
                  <el-skeleton :rows="3" animated />
                </div>
                <div v-else>{{ message.content }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-input-container">
          <el-input
            v-model="inputMessage"
            :placeholder="'请输入你的问题...'"
            type="textarea"
            :rows="1"
            resize="none"
            @keyup.enter="handleSendMessage"
          >
            <template #append>
              <el-button type="primary" @click="handleSendMessage" :loading="sending">
                <el-icon><send /></el-icon>
                发送
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>
    <el-card shadow="hover" class="ai-functions" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <h3>AI功能</h3>
        </div>
      </template>
      <div class="function-grid">
        <div class="function-item" @click="handleImageEnhance">
          <div class="function-icon">
            <el-icon class="el-icon--big"><picture /></el-icon>
          </div>
          <div class="function-name">图片增强</div>
          <div class="function-desc">上传图片进行AI增强处理</div>
        </div>
        <div class="function-item" @click="handleVideoGenerate">
          <div class="function-icon">
            <el-icon class="el-icon--big"><video-camera /></el-icon>
          </div>
          <div class="function-name">视频生成</div>
          <div class="function-desc">根据文本生成视频内容</div>
        </div>
        <div class="function-item" @click="handleTextGenerate">
          <div class="function-icon">
            <el-icon class="el-icon--big"><edit /></el-icon>
          </div>
          <div class="function-name">文本生成</div>
          <div class="function-desc">根据提示生成文本内容</div>
        </div>
        <div class="function-item" @click="handleQnA">
          <div class="function-icon">
            <el-icon class="el-icon--big"><question-filled /></el-icon>
          </div>
          <div class="function-name">问答系统</div>
          <div class="function-desc">智能问答，获取信息</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Send, Picture, VideoCamera, Edit, QuestionFilled } from '@element-plus/icons-vue'

const router = useRouter()
const chatContainer = ref(null)
const messages = ref([
  {
    id: 1,
    role: 'assistant',
    content: '你好！我是校园约伴系统的AI助手，有什么可以帮助你的吗？',
    loading: false
  }
])
const inputMessage = ref('')
const sending = ref(false)
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const aiAvatar = ref('https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png')

const handleSendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userMessage = {
    id: messages.value.length + 1,
    role: 'user',
    content: inputMessage.value,
    loading: false
  }
  
  messages.value.push(userMessage)
  inputMessage.value = ''
  scrollToBottom()
  
  sending.value = true
  
  // 模拟AI响应
  setTimeout(() => {
    const aiMessage = {
      id: messages.value.length + 1,
      role: 'assistant',
      content: `这是对"${userMessage.content}"的AI回复`,
      loading: false
    }
    messages.value.push(aiMessage)
    sending.value = false
    scrollToBottom()
  }, 1000)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const handleImageEnhance = () => {
  // 图片增强功能
  alert('图片增强功能开发中')
}

const handleVideoGenerate = () => {
  // 视频生成功能
  alert('视频生成功能开发中')
}

const handleTextGenerate = () => {
  // 文本生成功能
  alert('文本生成功能开发中')
}

const handleQnA = () => {
  // 问答系统功能
  alert('问答系统功能开发中')
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.ai-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.ai-card {
  height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtitle {
  font-size: 14px;
  color: #606266;
}

.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100% - 60px);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  animation: slideIn 0.3s ease-out;
}

.message.user {
  flex-direction: row-reverse;
}

.message.assistant {
  flex-direction: row;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  word-break: break-word;
  line-height: 1.5;
}

.message.user .message-bubble {
  background-color: #409eff;
  color: white;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-bubble {
  background-color: #f0f2f5;
  color: #303133;
  align-self: flex-start;
  border-bottom-left-radius: 4px;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.loading-indicator {
  width: 100%;
}

.chat-input-container {
  margin-top: 16px;
}

.ai-functions {
  height: auto;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 10px;
}

.function-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.function-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.function-icon {
  margin-bottom: 12px;
  font-size: 36px;
  color: #409eff;
}

.function-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.function-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.el-icon--big {
  font-size: 36px;
}
</style>