import request from '@/utils/request.js'

/**
 * 用户相关API服务
 * 对应后端 UserController
 */

const userApi = {
  /**
   * 获取用户详情
   * @param {number} userId - 用户ID
   * @returns {Promise} 用户信息
   */
  getUserInfo(userId) {
    return request.get(`/users/${userId}`)
  },
  
  /**
   * 更新用户信息
   * @param {number} userId - 用户ID
   * @param {Object} userData - 用户数据
   * @returns {Promise} 更新后的用户信息
   */
  updateUserInfo(userId, userData) {
    return request.put(`/users/${userId}`, userData)
  },
  
  /**
   * 修改密码
   * @param {number} userId - 用户ID
   * @param {Object} passwordData - 密码数据
   * @returns {Promise}
   */
  changePassword(userId, passwordData) {
    return request.put(`/users/${userId}/password`, passwordData)
  },
  
  /**
   * 上传头像
   * @param {number} userId - 用户ID
   * @param {string} filePath - 头像文件路径
   * @returns {Promise} 头像URL
   */
  uploadAvatar(userId, filePath) {
    return request.upload(`/users/${userId}/avatar`, filePath, {}, 'avatar')
  },
  
  /**
   * 搜索用户
   * @param {Object} params - 搜索参数
   * @returns {Promise} 用户列表
   */
  searchUsers(params = {}) {
    const { keyword = '', page = 1, size = 10 } = params
    return request.get('/users/search', { keyword, page, size })
  }
}

export default userApi