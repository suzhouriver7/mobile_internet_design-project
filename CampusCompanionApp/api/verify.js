import { post } from '../utils/request.js'

export default {
  sendCode(data) {
    return post('/verify/send-code', data, false)
  },
  // 发送注册验证码
  sendRegisterCode(email) {
    // 后端接口：POST /api/v1/verify/email/{email}
    const encodedEmail = encodeURIComponent(email)
    return post(`/verify/email/${encodedEmail}`, {}, false)
  }
}
