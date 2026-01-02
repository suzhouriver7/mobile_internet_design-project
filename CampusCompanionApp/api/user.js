import { get, put } from '../utils/request.js'

export default {
  getUserInfo(userId) {
    return get(`/users/${userId}`)
  },
  updateUserInfo(userId, data) {
    return put(`/users/${userId}`, data)
  },
  changePassword(userId, data) {
    return put(`/users/${userId}/password`, data)
  }
}
