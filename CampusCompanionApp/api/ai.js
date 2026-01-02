import { get, post } from '../utils/request.js'

export default {
  chat(message) {
    return get('/ai/chat', { message })
  },
  systemChat(data) {
    return post('/ai/chat/system', data)
  }
}
