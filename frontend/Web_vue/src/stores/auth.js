import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, logout as apiLogout, getUserInfo as apiGetUserInfo } from '../services/auth'
import logger from '../utils/logger'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)

  // 登录
  const login = async (credentials) => {
    loading.value = true
    error.value = null
    try {
      const response = await apiLogin(credentials)
      // 后端返回 ApiResponse<LoginResponse>
      // 其中 data = { token, userInfo }
      token.value = response.data.token
      user.value = response.data.userInfo
      localStorage.setItem('token', token.value)
      if (user.value?.id) {
        localStorage.setItem('userId', String(user.value.id))
      }

      logger.event('USER_LOGIN_SUCCESS', {
        userId: user.value?.id,
        nickname: user.value?.nickname
      })
      return response
    } catch (err) {
      error.value = err.response?.data?.message || '登录失败'
      logger.error('USER_LOGIN_FAILED', {
        message: error.value,
        status: err.response?.status
      })
      throw err
    } finally {
      loading.value = false
    }
  }

  // 注册
  const register = async (userData) => {
    loading.value = true
    error.value = null
    try {
      const response = await apiRegister(userData)
      logger.event('USER_REGISTER_SUCCESS', {
        email: userData.email,
        nickname: userData.nickname
      })
      return response
    } catch (err) {
      error.value = err.response?.data?.message || '注册失败'
      logger.error('USER_REGISTER_FAILED', {
        message: error.value,
        status: err.response?.status
      })
      throw err
    } finally {
      loading.value = false
    }
  }

  // 退出登录
  const logout = async () => {
    try {
      await apiLogout()
    } catch (err) {
      console.error('Logout error:', err)
    } finally {
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      logger.event('USER_LOGOUT', {})
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    if (!token.value) return
    
    loading.value = true
    error.value = null
    try {
      const storedId = localStorage.getItem('userId')
      const userId = user.value?.id || (storedId ? Number(storedId) : null)
      if (!userId) {
        loading.value = false
        return
      }

      const response = await apiGetUserInfo(userId)
      // ApiResponse<UserInfoResponse>，此处 data 即用户信息
      user.value = response.data
      if (user.value?.id) {
        localStorage.setItem('userId', String(user.value.id))
      }
      logger.info('USER_INFO_LOADED', {
        userId: user.value?.id,
        nickname: user.value?.nickname
      })
      return response
    } catch (err) {
      error.value = err.response?.data?.message || '获取用户信息失败'
      logger.error('USER_INFO_FAILED', {
        message: error.value,
        status: err.response?.status
      })
      logout()
      throw err
    } finally {
      loading.value = false
    }
  }

  // 设置用户信息
  const setUser = (userData) => {
    user.value = userData
  }

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    login,
    register,
    logout,
    getUserInfo,
    setUser
  }
})
