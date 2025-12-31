import axios from 'axios'
import logger from './logger'

// 创建axios实例
const instance = axios.create({
  // 默认使用相对路径，配合 Vite 代理或同源部署，避免跨域和协议混用问题
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 300000, // 请求超时时间（5 分钟），增加以支撑较长的 AI 返回
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 记录请求开始时间用于统计耗时
    config.metadata = config.metadata || {}
    config.metadata.startTime = Date.now()

    // 在发送请求之前做些什么，例如添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

     // 透传当前登录用户 ID，后端用它识别当前用户身份
     const userId = localStorage.getItem('userId')
     if (userId) {
       config.headers['X-User-Id'] = userId
     }

    const fullUrl = config.baseURL ? `${config.baseURL}${config.url}` : config.url

    // 记录请求日志（不包含敏感信息）
    logger.info('API_REQUEST', {
      method: config.method,
      url: fullUrl,
      params: config.params,
      // 避免记录密码等敏感字段，简单输出 data 结构
      hasData: !!config.data
    })

    // 额外在浏览器控制台直接输出一行，方便你查看
    console.log('[HTTP REQUEST]', {
      method: config.method,
      url: fullUrl,
      params: config.params,
      data: config.data
    })
    return config
  },
  error => {
    logger.error('API_REQUEST_ERROR', {
      message: error.message,
      stack: error.stack
    })
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    const metadata = response.config.metadata || {}
    const duration = metadata.startTime ? Date.now() - metadata.startTime : undefined

    const fullUrl = response.config.baseURL
      ? `${response.config.baseURL}${response.config.url}`
      : response.config.url

    logger.info('API_RESPONSE', {
      method: response.config.method,
      url: fullUrl,
      status: response.status,
      duration,
      code: response.data?.code,
      message: response.data?.message
    })

    // 在控制台输出响应详情（包含返回体）
    console.log('[HTTP RESPONSE]', {
      method: response.config.method,
      url: fullUrl,
      status: response.status,
      data: response.data
    })
    return response
  },
  error => {
    // 对响应错误做点什么
    if (error.response) {
      // 服务器返回错误状态码
      logger.error('API_RESPONSE_ERROR', {
        status: error.response.status,
        url: error.response.config?.url,
        code: error.response.data?.code,
        message: error.response.data?.message
      })

      console.error('[HTTP ERROR RESPONSE]', {
        status: error.response.status,
        url: error.response.config?.url,
        data: error.response.data
      })
      switch (error.response.status) {
        case 401:
          // 未授权，清除token并重定向到登录页
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          // 禁止访问
          console.error('Forbidden:', error.response.data.message)
          break
        case 404:
          // 资源未找到
          console.error('Not Found:', error.response.data.message)
          break
        case 500:
          // 服务器错误
          console.error('Server Error:', error.response.data.message)
          break
        default:
          console.error('Request Error:', error.response.data.message)
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      logger.error('API_NETWORK_ERROR', {
        message: 'No response received',
        // 这里不打印整个 request 对象，避免信息过多
        hasRequest: !!error.request
      })
      console.error('[HTTP NETWORK ERROR] No response received', error.request)
    } else {
      // 请求配置出错
      logger.error('API_REQUEST_CONFIG_ERROR', {
        message: error.message
      })
      console.error('[HTTP CONFIG ERROR]', error.message)
    }
    return Promise.reject(error)
  }
)

export default instance
