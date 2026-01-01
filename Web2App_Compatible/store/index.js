import { createStore } from 'vuex'
import * as util from '@/utils/util.js'
import userModule from './user.js'
import orderModule from './order.js'

// 初始状态
const state = {
  token: null,          // 认证token（当前使用X-User-Id方案，token为兼容保留）
  unreadCount: 0        // 未读消息数
}

// 获取器
const getters = {
  // 当前用户ID（从user模块获取）
  userId: (state, getters) => getters['user/userId'],
  
  // 是否为管理员（从user模块获取）
  isAdmin: (state, getters) => getters['user/isAdmin'],
  
  // 用户昵称（从user模块获取）
  nickname: (state, getters) => getters['user/nickname'],
  
  // 用户头像URL（从user模块获取）
  avatarUrl: (state, getters) => {
    const url = getters['user/avatarUrl']
    return url ? util.resolveResourceUrl(url) : ''
  },
  
  // 完整的用户信息（从user模块获取）
  userInfo: (state, getters) => {
    const user = getters['user/userInfo']
    return user ? util.deepClone(user) : null
  },
  
  // 是否已登录（从user模块获取）
  isLogin: (state, getters) => getters['user/isLogin']
}

// 同步修改方法
const mutations = {
  // 设置认证token
  SET_TOKEN(state, token) {
    state.token = token
    if (token) {
      uni.setStorageSync('token', token)
    } else {
      uni.removeStorageSync('token')
    }
  },
  
  // 设置未读消息数
  SET_UNREAD_COUNT(state, count) {
    state.unreadCount = count
  },
  
  // 清除所有状态（退出登录）
  CLEAR_ALL(state) {
    state.token = null
    state.unreadCount = 0
    
    // 清除本地存储
    uni.removeStorageSync('token')
  }
}

// 异步操作方法
const actions = {
  /**
   * 初始化用户状态（从本地存储恢复）
   */
  initUserState({ commit, dispatch }) {
    try {
      const userStr = uni.getStorageSync('user')
      const token = uni.getStorageSync('token')
      
      if (userStr) {
        const user = JSON.parse(userStr)
        dispatch('user/login', { userInfo: user })
      }
      
      if (token) {
        commit('SET_TOKEN', token)
      }
    } catch (error) {
      console.error('初始化用户状态失败:', error)
    }
  },
  
  /**
   * 用户登录（委托给user模块）
   */
  async login({ dispatch }, userData) {
    await dispatch('user/login', userData)
    // 登录成功提示
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
  },
  
  /**
   * 用户退出登录（委托给user模块）
   */
  async logout({ commit, dispatch }) {
    await dispatch('user/logout')
    commit('CLEAR_ALL')
    
    // 跳转到登录页
    uni.reLaunch({
      url: '/pages/auth/login'
    })
  },
  
  /**
   * 更新用户信息（委托给user模块）
   */
  async updateUserInfo({ dispatch }, newInfo) {
    await dispatch('user/updateUserInfo', newInfo)
    // 显示成功提示
    uni.showToast({
      title: '更新成功',
      icon: 'success'
    })
  }
}

// 创建Store实例
const store = createStore({
  state,
  getters,
  mutations,
  actions,
  
  // 模块
  modules: {
    user: userModule,
    order: orderModule
  },
  
  // 开发环境开启严格模式
  strict: process.env.NODE_ENV !== 'production'
})

export default store