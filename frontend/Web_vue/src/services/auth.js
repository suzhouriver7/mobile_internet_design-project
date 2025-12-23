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
