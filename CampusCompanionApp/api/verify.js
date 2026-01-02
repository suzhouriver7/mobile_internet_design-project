import { post } from '../utils/request.js'

export default {
  sendCode(data) {
    return post('/verify/send-code', data, false)
  }
}
