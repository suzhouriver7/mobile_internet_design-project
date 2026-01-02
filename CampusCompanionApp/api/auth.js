import { post } from '../utils/request.js'

export default {
  login(data) {
    return post('/auth/login', data, false)
  },
  register(data) {
    return post('/auth/register', data, false)
  },
  logout() {
    return post('/auth/logout', {})
  },
  forgotPasswordStep1(data) {
    return post('/auth/forgot-password/verify-email', data, false)
  },
  forgotPasswordStep2(data) {
    return post('/auth/forgot-password/verify-code', data, false)
  },
  forgotPasswordStep3(data) {
    return post('/auth/forgot-password/reset-password', data, false)
  }
}
