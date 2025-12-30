import config from './config.js'
import store from '@/store/index.js'

/**
 * 统一的网络请求工具
 * 1. 自动携带认证信息 (X-User-Id)
 * 2. 统一处理响应格式 (ApiResponse)
 * 3. 统一错误处理 (BusinessException)
 */
class Request {
  constructor() {
    this.baseURL = config.baseURL
    this.timeout = 10000
  }

  /**
   * 发起请求
   * @param {string} url - 请求地址
   * @param {string} method - 请求方法
   * @param {object} data - 请求数据
   * @param {object} customHeader - 自定义请求头
   */
  async request(url, method = 'GET', data = {}, customHeader = {}) {
    // 构建请求头
    const header = {
      'Content-Type': 'application/json',
      ...customHeader
    }

    // 从Vuex状态中获取当前用户ID并添加到请求头
    const currentUser = store.state.user
    if (currentUser && currentUser.id) {
      header['X-User-Id'] = currentUser.id.toString()
    }

    // 构建完整请求URL
    const fullUrl = url.startsWith('http') ? url : `${this.baseURL}${url}`

    // 发起uni-app请求
    return new Promise((resolve, reject) => {
      uni.request({
        url: fullUrl,
        method,
        data,
        header,
        timeout: this.timeout,
        success: (res) => {
          // HTTP状态码成功
          if (res.statusCode >= 200 && res.statusCode < 300) {
            const response = res.data

            // 检查后端返回的统一响应格式
            if (response && typeof response.code !== 'undefined') {
              if (response.code === 200) {
                // 业务成功
                resolve(response.data)
              } else {
                // 业务异常 (BusinessException)
                this._handleBusinessError(response.code, response.message, reject)
              }
            } else {
              // 响应格式不符合ApiResponse
              reject(new Error(`响应格式错误: ${JSON.stringify(response).substring(0, 100)}`))
            }
          } else {
            // HTTP错误
            this._handleHttpError(res.statusCode, res.errMsg, reject)
          }
        },
        fail: (err) => {
          // 网络错误
          reject(new Error(`网络请求失败: ${err.errMsg || '未知错误'}`))
        }
      })
    })
  }

  /**
   * 处理业务错误 (BusinessException)
   */
  _handleBusinessError(code, message, reject) {
    // 特殊处理 token 失效或未登录的情况
    if (code === 1001 || code === 1012) {
      // 清除本地用户信息
      store.dispatch('logout')
      // 跳转到登录页
      uni.showToast({
        title: '登录已过期，请重新登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/auth/login'
        })
      }, 1500)
    }

    // 构建友好的错误提示
    const errorMap = {
      1001: '参数错误或登录信息无效',
      1002: '用户不存在',
      1003: '密码错误',
      1004: '无权进行此操作',
      1005: '订单不存在',
      1006: '动态不存在',
      1007: '文件上传失败',
      1008: '请勿重复操作',
      1009: '人数已满，无法加入',
      1010: '订单已过期',
      1011: '用户已存在',
      1014: '订单已完成',
      1015: '订单已取消',
    }

    const userMessage = errorMap[code] || message || '操作失败'
    
    // 显示错误提示
    uni.showToast({
      title: userMessage,
      icon: 'none',
      duration: 3000
    })

    reject(new Error(`业务错误 ${code}: ${userMessage}`))
  }

  /**
   * 处理HTTP错误
   */
  _handleHttpError(statusCode, errMsg, reject) {
    const errorMap = {
      400: '请求参数错误',
      401: '未授权，请登录',
      403: '禁止访问',
      404: '请求资源不存在',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务不可用'
    }

    const message = errorMap[statusCode] || `请求失败 (${statusCode})`
    
    uni.showToast({
      title: message,
      icon: 'none'
    })

    reject(new Error(`HTTP ${statusCode}: ${message}`))
  }

  // 便捷方法
  get(url, data = {}, header = {}) {
    return this.request(url, 'GET', data, header)
  }

  post(url, data = {}, header = {}) {
    return this.request(url, 'POST', data, header)
  }

  put(url, data = {}, header = {}) {
    return this.request(url, 'PUT', data, header)
  }

  delete(url, data = {}, header = {}) {
    return this.request(url, 'DELETE', data, header)
  }

  /**
   * 文件上传（FormData格式）
   */
  upload(url, filePath, formData = {}, name = 'file') {
    return new Promise((resolve, reject) => {
      // 获取当前用户ID
      const currentUser = store.state.user
      const header = {}
      if (currentUser && currentUser.id) {
        header['X-User-Id'] = currentUser.id.toString()
      }

      uni.uploadFile({
        url: `${this.baseURL}${url}`,
        filePath,
        name,
        formData,
        header,
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data)
              if (data.code === 200) {
                resolve(data.data)
              } else {
                this._handleBusinessError(data.code, data.message, reject)
              }
            } catch (e) {
              reject(new Error('解析响应数据失败'))
            }
          } else {
            this._handleHttpError(res.statusCode, res.errMsg, reject)
          }
        },
        fail: (err) => {
          reject(new Error(`上传失败: ${err.errMsg}`))
        }
      })
    })
  }
}

// 创建单例实例并导出
const request = new Request()
export default request