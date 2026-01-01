import request from '@/utils/request.js'

/**
 * 认证相关API服务
 * 对应后端 AuthController
 */

const authApi = {
  /**
   * 用户登录
   * @param {Object} credentials - 登录凭证 {identifier, password}
   * @returns {Promise} 包含token和用户信息的响应
   */
  login(credentials) {
    return request.post('/auth/login', credentials)
  },
  
  /**
   * 用户注册
   * @param {Object} userData - 注册数据 {email, password, nickname, studentId}
   * @returns {Promise} 注册成功的用户ID
   */
  register(userData) {
    return request.post('/auth/register', userData)
  },
  
  /**
   * 刷新Token
   * @param {string} refreshToken - 刷新令牌
   * @returns {Promise} 新的访问令牌
   */
  refreshToken(refreshToken) {
    return request.post('/auth/refresh', { refreshToken })
  },
  
  /**
   * 退出登录
   * @returns {Promise}
   */
  logout() {
    return request.post('/auth/logout')
  },
  
  /**
   * 获取用户信息（根据接口文档）
   * @param {number} userId - 用户ID
   * @returns {Promise} 用户详细信息
   */
  getUserInfo(userId) {
    return request.get(`/users/${userId}`)
  }
}

export default authApi