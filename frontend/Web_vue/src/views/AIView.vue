<template>
  <div class="ai-view">
    <div class="ai-wrapper">
      <header class="ai-header">
        <h2>AI 问询</h2>
      </header>

      <section class="ai-chat-card">
        <div class="chat-messages" ref="chatContainer">
          <div v-for="message in messages" :key="message.id" :class="['message-row', message.role]">
            <div class="avatar">
              <el-avatar :src="message.role === 'user' ? userAvatar : aiAvatar" size="32" />
            </div>
            <div class="bubble">
              <div class="bubble-content">
                <div v-if="message.role === 'assistant' && message.loading" class="loading-indicator">
                  <el-skeleton :rows="3" animated />
                </div>
                <div v-else v-html="formatContent(message.content)"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <div class="input-box">
            <el-input
              ref="inputRef"
              v-model="inputMessage"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 10 }"
              placeholder="请输入你的问题，Shift+Enter 换行，Enter 发送"
              @input="saveDraft"
              maxlength="4000"
            />
          </div>

          <div class="controls">
            <div class="meta">
              <span class="char-count">{{ inputLength }} / 4000</span>
            </div>
            <div class="actions">
              <button class="btn clear" @click="handleClear" title="清空对话">清空</button>
              <button
                class="btn send"
                :class="{ sending: sending }"
                @click="handleSendMessage"
                :disabled="sending || !inputMessage.trim()"
                title="发送 (Enter)"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" focusable="false" class="send-icon">
                  <path d="M12 2L3 21h7l2-6 2 6h7L12 2z" fill="currentColor" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import aiService from '../services/ai'

const chatContainer = ref(null)
const messages = ref([
  { id: 1, role: 'assistant', content: '你好！我是校园约伴系统的AI助手，有什么可以帮助你的吗？', loading: false }
])

const inputMessage = ref('')
try { inputMessage.value = localStorage.getItem('ai_draft') || '' } catch (e) {}
const sending = ref(false)
const inputRef = ref(null)
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const aiAvatar = ref('https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png')

const inputLength = computed(() => inputMessage.value.length)

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const formatContent = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br/>')
}

const saveDraft = () => {
  try { localStorage.setItem('ai_draft', inputMessage.value) } catch (e) {}
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSendMessage()
  }
}

const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return

  const userMessage = { id: messages.value.length + 1, role: 'user', content: inputMessage.value, loading: false }
  messages.value.push(userMessage)
  saveDraft()
  inputMessage.value = ''
  try { localStorage.removeItem('ai_draft') } catch (e) {}
  scrollToBottom()

  sending.value = true
  const history = messages.value.map((m) => ({ role: m.role === 'assistant' ? 'assistant' : 'user', content: m.content }))

  try {
    const payload = { messages: history, stream: false }
    const resp = await aiService.fullChat(payload)
    const body = resp.data || {}
    let reply = null
    if (body.choices && body.choices.length > 0 && body.choices[0].message) {
      reply = body.choices[0].message.content
    } else if (body.data && body.data.choices && body.data.choices.length > 0) {
      reply = body.data.choices[0].message?.content
    }

    const aiMessage = { id: messages.value.length + 1, role: 'assistant', content: reply || '抱歉，AI 未返回有效内容。', loading: false }
    messages.value.push(aiMessage)
  } catch (e) {
    console.error('AI 请求失败', e)
    const errMsg = (e.response?.data?.message) || e.message || 'AI 请求失败'
    messages.value.push({ id: messages.value.length + 1, role: 'assistant', content: `错误：${errMsg}`, loading: false })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

const handleClear = () => {
  messages.value = [ { id: 1, role: 'assistant', content: '已清空对话，欢迎继续提问。', loading: false } ]
  try { localStorage.removeItem('ai_draft') } catch (e) {}
}

onMounted(() => { scrollToBottom() })

// attach native keydown listener to the underlying textarea because
// Element Plus' el-input (type=textarea) doesn't forward native keydown to the component in Vue 3
onMounted(() => {
  nextTick(() => {
    const ta = inputRef.value?.$el?.querySelector('textarea')
    if (ta) ta.addEventListener('keydown', handleKeydown)
  })
})

onBeforeUnmount(() => {
  const ta = inputRef.value?.$el?.querySelector('textarea')
  if (ta) ta.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.ai-view { display: flex; justify-content: center; padding: 24px 16px; }
.ai-wrapper { width: 100%; max-width: 900px; }
.ai-header { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.ai-header h2 { margin: 0; font-size: 20px; }
.ai-sub { color: #666; margin: 0; }
.ai-chat-card { background: #fff; border-radius: 12px; box-shadow: 0 6px 18px rgba(29,31,33,0.04); padding: 16px; display: flex; flex-direction: column; height: 72vh; min-height: 560px; }
.chat-messages { flex: 1 1 auto; overflow: auto; padding: 12px; display: flex; flex-direction: column; gap: 12px; }
.message-row { display: flex; gap: 12px; align-items: flex-start; }
.message-row.user { flex-direction: row-reverse; }
.avatar { flex: 0 0 36px; }
.bubble { max-width: 78%; }
.bubble-content { padding: 12px 14px; border-radius: 12px; line-height: 1.6; }
.message-row.user .bubble-content { background: #409eff; color: #fff; border-bottom-right-radius: 6px; }
.message-row.assistant .bubble-content { background: #f5f7fa; color: #333; border-bottom-left-radius: 6px; }
.chat-input-area { margin-top: 12px; border-top: 1px solid #eef2f6; padding-top: 12px; }
.input-box { margin-bottom: 8px; }
.controls { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.meta { color: #8b96a6; font-size: 13px; }
.actions { display: flex; gap: 8px; }
.btn { border: none; background: transparent; padding: 8px 12px; border-radius: 8px; cursor: pointer; font-size: 14px; }
.btn.clear { color: #606266; }
.btn.clear:hover { background: #f5f7fa; }
.btn.send { background: #409eff; color: #fff; display: inline-flex; align-items: center; justify-content: center; width: 44px; height: 44px; padding: 0; }
.btn.send:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(64,158,255,0.18); }
.btn.send.sending { opacity: 0.8; pointer-events: none; }
.send-icon { display: block; }
@media (max-width: 768px) {
  .ai-wrapper { max-width: 100%; }
  .ai-chat-card { height: 78vh; min-height: 480px; }
  .bubble { max-width: 82%; }
  .btn.send { width: 40px; height: 40px; }
}
</style>
