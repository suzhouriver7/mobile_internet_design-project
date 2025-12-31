<template>
  <div class="ai-view">
      <header class="ai-header">
        <h2>AI 问询</h2>
      </header>

      <section class="ai-chat-card">
        <div class="chat-messages" ref="chatContainer">
          <div v-for="message in messages" :key="message.id" :class="['message-row', message.role]">
            <div class="avatar">
              <el-avatar :src="message.role === 'user' ? userAvatar : aiAvatar" :size="32" />
            </div>
            <div class="bubble">
              <div class="bubble-content">
                <div v-if="message.role === 'assistant' && message.loading" class="loading-indicator">
                  <div class="three-body" aria-hidden="true" title="AI 正在生成回复">
                    <div class="three-body__dot"></div>
                    <div class="three-body__dot"></div>
                    <div class="three-body__dot"></div>
                  </div>
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

const escapeHtml = (unsafe) => {
  return (unsafe || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

const renderMarkdown = (md) => {
  if (!md) return ''
  // handle fenced code blocks first
  const codeBlocks = []
  md = md.replace(/```([\s\S]*?)```/g, (m, code) => {
    const idx = codeBlocks.length
    codeBlocks.push(code)
    return `@@CODE_BLOCK_${idx}@@`
  })

  // escape remaining text
  md = escapeHtml(md)

  // headings
  md = md.replace(/^######\s*(.*)$/gm, '<h6>$1</h6>')
  md = md.replace(/^#####\s*(.*)$/gm, '<h5>$1</h5>')
  md = md.replace(/^####\s*(.*)$/gm, '<h4>$1</h4>')
  md = md.replace(/^###\s*(.*)$/gm, '<h3>$1</h3>')
  md = md.replace(/^##\s*(.*)$/gm, '<h2>$1</h2>')
  md = md.replace(/^#\s*(.*)$/gm, '<h1>$1</h1>')

  // links
  md = md.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')

  // bold and italic (basic)
  md = md.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  md = md.replace(/\*(.+?)\*/g, '<em>$1</em>')

  // inline code
  md = md.replace(/`([^`]+)`/g, '<code>$1</code>')

  // unordered lists: convert lines starting with - or * into ul
  md = md.replace(/(^|\n)([ \t]*[-\*]\s+.+)(?=\n|$)/g, (m) => {
    // collect consecutive list items
    const items = m.split(/\n/).filter(Boolean).map(l => l.replace(/^[ \t]*[-\*]\s+/, ''))
    return '\n<ul>' + items.map(i => '<li>' + i + '</li>').join('') + '</ul>'
  })

  // paragraphs: split by blank lines
  const parts = md.split(/\n\s*\n/)
  md = parts.map(p => {
    // restore any inline newlines to <br>
    const s = p.replace(/\n/g, '<br/>')
    return /^<(h\d|ul|pre|blockquote)/.test(s) ? s : ('<p>' + s + '</p>')
  }).join('\n')

  // restore code blocks
  md = md.replace(/@@CODE_BLOCK_(\d+)@@/g, (m, idx) => {
    const code = codeBlocks[Number(idx)] || ''
    return '<pre><code>' + escapeHtml(code) + '</code></pre>'
  })

  return md
}

const formatContent = (text) => renderMarkdown(text)

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

  // add placeholder assistant message which will be filled incrementally
  const aiMsg = { id: messages.value.length + 1, role: 'assistant', content: '', loading: true }
  messages.value.push(aiMsg)
  scrollToBottom()

  try {
    const payload = { messages: history, stream: false }
    // 发起非流式请求，显示等待动画（通过 aiMsg.loading）直到收到完整回复
    const resp = await aiService.fullChat(payload)
    const body = resp.data || {}
    let reply = null
    if (body.choices && body.choices.length > 0 && body.choices[0].message) {
      reply = body.choices[0].message.content
    } else if (body.data && body.data.choices && body.data.choices.length > 0) {
      reply = body.data.choices[0].message?.content
    } else if (body.reply) {
      reply = body.reply
    }

    aiMsg.content = reply || '抱歉，AI 未返回有效内容。'
    aiMsg.loading = false
  } catch (e) {
    console.error('AI 请求失败', e)
    const errMsg = (e.response?.data?.message) || e.message || 'AI 请求失败'
    aiMsg.content = `错误：${errMsg}`
    aiMsg.loading = false
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
.ai-view { width: 100%; max-width: min(1600px, 95%); margin: 0 auto; padding: 24px 16px; box-sizing: border-box; }
.ai-header { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.ai-header h2 { margin: 0; font-size: 20px; }
.ai-sub { color: #666; margin: 0; }
.ai-chat-card { background: #fff; border-radius: 12px; box-shadow: 0 6px 18px rgba(29,31,33,0.04); padding: 16px; display: flex; flex-direction: column; height: 72vh; min-height: 560px; }
.chat-messages { flex: 1 1 auto; overflow: auto; padding: 12px; display: flex; flex-direction: column; gap: 12px; }
.message-row { display: flex; gap: 12px; align-items: flex-start; }
.message-row.user { flex-direction: row-reverse; }
.avatar { flex: 0 0 36px; }
.bubble { max-width: clamp(480px, 60%, 900px); }
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
/* loading spinner */
.loading-indicator { display: flex; align-items: center; gap: 12px; justify-content: center; }

/* From Uiverse.io by dovatgabriel - three-body animation */
.three-body {
 --uib-size: 35px;
 --uib-speed: 0.8s;
 --uib-color: #5D3FD3;
 position: relative;
 display: inline-block;
 height: var(--uib-size);
 width: var(--uib-size);
 animation: spin78236 calc(var(--uib-speed) * 2.5) infinite linear;
}

.three-body__dot {
 position: absolute;
 height: 100%;
 width: 30%;
}

.three-body__dot:after {
 content: '';
 position: absolute;
 height: 0%;
 width: 100%;
 padding-bottom: 100%;
 background-color: var(--uib-color);
 border-radius: 50%;
}

.three-body__dot:nth-child(1) {
 bottom: 5%;
 left: 0;
 transform: rotate(60deg);
 transform-origin: 50% 85%;
}

.three-body__dot:nth-child(1)::after {
 bottom: 0;
 left: 0;
 animation: wobble1 var(--uib-speed) infinite ease-in-out;
 animation-delay: calc(var(--uib-speed) * -0.3);
}

.three-body__dot:nth-child(2) {
 bottom: 5%;
 right: 0;
 transform: rotate(-60deg);
 transform-origin: 50% 85%;
}

.three-body__dot:nth-child(2)::after {
 bottom: 0;
 left: 0;
 animation: wobble1 var(--uib-speed) infinite
  calc(var(--uib-speed) * -0.15) ease-in-out;
}

.three-body__dot:nth-child(3) {
 bottom: -5%;
 left: 0;
 transform: translateX(116.666%);
}

.three-body__dot:nth-child(3)::after {
 top: 0;
 left: 0;
 animation: wobble2 var(--uib-speed) infinite ease-in-out;
}

@keyframes spin78236 {
 0% {
  transform: rotate(0deg);
 }

 100% {
  transform: rotate(360deg);
 }
}

@keyframes wobble1 {
 0%,
  100% {
  transform: translateY(0%) scale(1);
  opacity: 1;
 }

 50% {
  transform: translateY(-66%) scale(0.65);
  opacity: 0.8;
 }
}

@keyframes wobble2 {
 0%,
  100% {
  transform: translateY(0%) scale(1);
  opacity: 1;
 }

 50% {
  transform: translateY(66%) scale(0.65);
  opacity: 0.8;
 }
}
@media (max-width: 1200px) {
  .ai-view { max-width: 95%; padding: 16px; }
  .ai-chat-card { height: 78vh; min-height: 480px; }
  .bubble { max-width: 82%; }
}

@media (max-width: 600px) {
  .ai-view { padding: 10px; }
  .ai-chat-card { height: 72vh; min-height: 420px; }
  .bubble { max-width: 92%; }
  .btn.send { width: 40px; height: 40px; }
}
</style>
