import { createStore } from 'vuex'
import * as util from '@/utils/util.js'

// 初始状态
const state = {
  user: null,           // 当前登录用户信息 {id, nickname, avatarUrl, userType...}
  token: null,          // 认证token（当前使用X-User-Id方案，token为兼容保留）
  isLogin: false,       // 是否已登录
  unreadCount: 0,       // 未读消息数
  currentOrderFilter: { // 当前订单筛选条件
    status: null,
    activityType: null,
    campus: null
  }
}

// 获取器
const getters = {
  // 当前用户ID
  userId: state => state.user ? state.user.id : null,
  
  // 是否为管理员
  isAdmin: state => state.user ? state.user.userType === 1 : false,
  
  // 用户昵称
  nickname: state => state.user ? state.user.nickname : '未登录',
  
  // 用户头像URL
  avatarUrl: state => state.user ? util.resolveResourceUrl(state.user.avatarUrl) : '',
  
  // 完整的用户信息
  userInfo: state => state.user ? util.deepClone(state.user) : null
}

// 同步修改方法
const mutations = {
  // 设置用户信息（登录时调用）
  SET_USER(state, userData) {
    state.user = userData
    state.isLogin = !!userData
    
    // 同时存储到本地（同步方式）
    if (userData) {
      uni.setStorageSync('user', JSON.stringify(userData))
    } else {
      uni.removeStorageSync('user')
    }
  },
  
  // 设置认证token
  SET_TOKEN(state, token) {
    state.token = token
    if (token) {
      uni.setStorageSync('token', token)
    } else {
      uni.removeStorageSync('token')
    }
  },
  
  // 更新用户部分信息（如修改昵称、头像后）
  UPDATE_USER_INFO(state, userInfo) {
    if (state.user) {
      state.user = { ...state.user, ...userInfo }
      uni.setStorageSync('user', JSON.stringify(state.user))
    }
  },
  
  // 设置未读消息数
  SET_UNREAD_COUNT(state, count) {
    state.unreadCount = count
  },
  
  // 设置订单筛选条件
  SET_ORDER_FILTER(state, filter) {
    state.currentOrderFilter = { ...state.currentOrderFilter, ...filter }
  },
  
  // 清除所有状态（退出登录）
  CLEAR_ALL(state) {
    state.user = null
    state.token = null
    state.isLogin = false
    state.unreadCount = 0
    state.currentOrderFilter = {
      status: null,
      activityType: null,
      campus: null
    }
    
    // 清除本地存储
    uni.removeStorageSync('user')
    uni.removeStorageSync('token')
  }
}

// 异步操作方法
const actions = {
  /**
   * 初始化用户状态（从本地存储恢复）
   */
  initUserState({ commit }) {
    try {
      const userStr = uni.getStorageSync('user')
      const token = uni.getStorageSync('token')
      
      if (userStr) {
        const user = JSON.parse(userStr)
        commit('SET_USER', user)
      }
      
      if (token) {
        commit('SET_TOKEN', token)
      }
    } catch (error) {
      console.error('初始化用户状态失败:', error)
    }
  },
  
  /**
   * 用户登录
   */
  async login({ commit }, userData) {
    // 注意：实际登录逻辑在 auth.js API中，这里只更新状态
    commit('SET_USER', userData.userInfo)
    commit('SET_TOKEN', userData.token)
    
    // 登录成功提示
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
  },
  
  /**
   * 用户退出登录
   */
  async logout({ commit }) {
    commit('CLEAR_ALL')
    
    // 跳转到登录页
    uni.reLaunch({
      url: '/pages/auth/login'
    })
  },
  
  /**
   * 更新用户信息（如修改昵称、头像）
   */
  async updateUserInfo({ commit, state }, newInfo) {
    if (!state.user) return
    
    commit('UPDATE_USER_INFO', newInfo)
    
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
  
  // 开发环境开启严格模式
  strict: process.env.NODE_ENV !== 'production'
})

export default store