import dayjs from 'dayjs'
import config from './config.js'

/**
 * 通用工具函数集
 */

/**
 * 格式化时间显示
 * 仿照 Web 端 formatTime 函数
 * @param {string|Date} time - 时间字符串或Date对象
 * @param {string} format - 输出格式，默认 'YYYY-MM-DD HH:mm'
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time, format = 'YYYY-MM-DD HH:mm') {
  if (!time) return ''
  return dayjs(time).format(format)
}

/**
 * 相对时间显示（如：刚刚，3分钟前，2小时前）
 * @param {string|Date} time - 时间
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(time) {
  if (!time) return ''
  
  const now = dayjs()
  const target = dayjs(time)
  const diffInMinutes = now.diff(target, 'minute')
  const diffInHours = now.diff(target, 'hour')
  const diffInDays = now.diff(target, 'day')
  
  if (diffInMinutes < 1) {
    return '刚刚'
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}分钟前`
  } else if (diffInHours < 24) {
    return `${diffInHours}小时前`
  } else if (diffInDays < 7) {
    return `${diffInDays}天前`
  } else {
    return formatTime(time, 'YYYY-MM-DD')
  }
}

/**
 * 解析资源URL（头像、媒体文件）
 * 仿照 Web 端 resolveAvatarUrl / resolveMediaUrl 函数
 * @param {string} url - 资源相对路径或完整URL
 * @returns {string} 完整的可访问URL
 */
export function resolveResourceUrl(url) {
  if (!url) return ''
  
  // 如果是完整的HTTP/HTTPS URL，直接返回
  if (/^https?:\/\//.test(url)) {
    return url
  }
  
  // 否则拼接基础文件URL
  return `${config.fileBaseURL}${url}`
}

/**
 * 深度克隆对象
 * @param {*} obj - 要克隆的对象
 * @returns {*} 克隆后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
  return obj
}

/**
 * 防抖函数
 * @param {Function} func - 要执行的函数
 * @param {number} wait - 等待时间(ms)
 * @returns {Function} 防抖处理后的函数
 */
export function debounce(func, wait = 300) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

/**
 * 节流函数
 * @param {Function} func - 要执行的函数
 * @param {number} limit - 时间限制(ms)
 * @returns {Function} 节流处理后的函数
 */
export function throttle(func, limit = 300) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}

/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 * @returns {boolean} 是否合法
 */
export function isValidEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

/**
 * 验证密码强度（6-20位）
 * @param {string} password - 密码
 * @returns {boolean} 是否合法
 */
export function isValidPassword(password) {
  return password && password.length >= 6 && password.length <= 20
}

/**
 * 获取屏幕安全区域（用于全面屏适配）
 * @returns {Object} 安全区域尺寸
 */
export function getSafeArea() {
  let safeArea = {}
  
  // #ifdef MP-WEIXIN
  const systemInfo = uni.getSystemInfoSync()
  safeArea = systemInfo.safeArea || {
    top: 0,
    bottom: systemInfo.windowHeight,
    left: 0,
    right: systemInfo.windowWidth,
    height: systemInfo.windowHeight,
    width: systemInfo.windowWidth
  }
  // #endif
  
  // #ifdef H5
  safeArea = {
    top: 0,
    bottom: window.innerHeight,
    left: 0,
    right: window.innerWidth,
    height: window.innerHeight,
    width: window.innerWidth
  }
  // #endif
  
  return safeArea
}

/**
 * 显示加载提示
 * @param {string} title - 提示文字
 */
export function showLoading(title = '加载中...') {
  uni.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
  uni.hideLoading()
}

/**
 * 显示成功提示
 * @param {string} title - 提示文字
 */
export function showSuccess(title = '操作成功') {
  uni.showToast({
    title,
    icon: 'success',
    duration: 2000
  })
}

/**
 * 显示错误提示
 * @param {string} title - 错误信息
 */
export function showError(title = '操作失败') {
  uni.showToast({
    title,
    icon: 'none',
    duration: 3000
  })
}