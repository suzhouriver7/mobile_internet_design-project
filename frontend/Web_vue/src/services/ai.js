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
    // 有时代理/浏览器对长请求会有较短的超时（例如 10s），在这里显式传入更大的超时以降低被客户端中止的风险
    return api.post('/ai/chat/full', payload, { timeout: 300000 })
  },
  // 注意：流式接口已移除，使用非流式 `fullChat` 获取完整回复
}
