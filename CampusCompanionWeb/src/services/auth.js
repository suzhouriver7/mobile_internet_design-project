import api from '../utils/axios'

// 认证相关接口与后端 /api/v1/auth 对齐

// 登录：返回后端统一的 ApiResponse<LoginResponse>
export const login = async (credentials) => {
  const response = await api.post('/auth/login', credentials)
  return response.data
}

// 注册
export const register = async (userData) => {
  const response = await api.post('/auth/register', userData)
  return response.data
}

// 注册：发送邮箱验证码（用于注册场景）
export const sendRegisterCode = async (email) => {
  const encodedEmail = encodeURIComponent(email)
  const response = await api.post(`/verify/email/${encodedEmail}`)
  return response.data
}

// 退出登录
export const logout = async () => {
  const response = await api.post('/auth/logout')
  return response.data
}

// 获取用户信息：根据接口文档使用 GET /users/{userId}
export const getUserInfo = async (userId) => {
  const response = await api.get(`/users/${userId}`)
  return response.data
}

// 刷新 Token
export const refreshToken = async (refreshToken) => {
  const response = await api.post('/auth/refresh', { refreshToken })
  return response.data
}

// 忘记密码：验证邮箱是否已注册
export const verifyEmailForReset = async (payload) => {
  const response = await api.post('/auth/forgot/verify-email', payload)
  return response.data
}

// 忘记密码：发送重置验证码
export const sendResetCode = async (payload) => {
  const response = await api.post('/auth/forgot/send-code', payload)
  return response.data
}

// 忘记密码：校验验证码
export const verifyResetCode = async (payload) => {
  const response = await api.post('/auth/forgot/verify-code', payload)
  return response.data
}

// 忘记密码：提交新密码完成重置
export const resetPassword = async (payload) => {
  const response = await api.post('/auth/forgot/reset-password', payload)
  return response.data
}
