import api from '../utils/axios'

export default {
  // 简单对话（GET /api/v1/ai/chat?message=）
  simpleChat(message) {
    return api.get('/ai/chat', { params: { message } })
  },

  // 带系统提示的对话（POST /api/v1/ai/chat/system）
  chatWithSystem(system, user) {
    return api.post('/ai/chat/system', { system, user })
  },

  // 完整对话请求：POST /api/v1/ai/chat/full
  // body: { model?, messages: [{role, content}], stream?, temperature?, maxTokens? }
  fullChat(payload) {
    return api.post('/ai/chat/full', payload)
  }
}
