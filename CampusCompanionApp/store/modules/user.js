import { authApi, userApi } from '@/api/index.js'

const state = {
  userId: null,
  userInfo: null,
  isLogin: false
}

const mutations = {
  SET_USER_ID(state, userId) {
    state.userId = userId
    if (userId) {
      uni.setStorageSync('userId', userId)
    } else {
      uni.removeStorageSync('userId')
    }
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    if (userInfo) {
      uni.setStorageSync('userInfo', JSON.stringify(userInfo))
    } else {
      uni.removeStorageSync('userInfo')
    }
  },
  SET_LOGIN_STATUS(state, isLogin) {
    state.isLogin = isLogin
  },
  CLEAR_ALL(state) {
    state.userId = null
    state.userInfo = null
    state.isLogin = false
    uni.removeStorageSync('userId')
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('token')
  }
}

const actions = {
  async login({ commit }, loginData) {
    try {
      const response = await authApi.login(loginData)
      if (response && response.userInfo) {
        commit('SET_USER_ID', response.userInfo.uid)
        commit('SET_USER_INFO', response.userInfo)
        commit('SET_LOGIN_STATUS', true)
        return response
      }
    } catch (error) {
      throw error
    }
  },
  async logout({ commit }) {
    try {
      await authApi.logout()
    } catch (e) {
      console.error('退出登录失败:', e)
    }
    commit('CLEAR_ALL')
  },
  initUserState({ commit }) {
    try {
      const userId = uni.getStorageSync('userId')
      const userInfoStr = uni.getStorageSync('userInfo')
      if (userId && userInfoStr) {
        const userInfo = JSON.parse(userInfoStr)
        commit('SET_USER_ID', userId)
        commit('SET_USER_INFO', userInfo)
        commit('SET_LOGIN_STATUS', true)
      }
    } catch (e) {
      console.error('初始化用户状态失败:', e)
    }
  },
  async updateUserInfo({ commit, state }, newInfo) {
    const userId = state.userId
    if (!userId) return
    const userInfo = await userApi.updateUserInfo(userId, newInfo)
    commit('SET_USER_INFO', userInfo)
    return userInfo
  }
}

const getters = {
  userId: state => state.userId,
  userInfo: state => state.userInfo,
  isLogin: state => state.isLogin,
  isAdmin: state => {
    return state.userInfo && state.userInfo.userType === 'ADMIN'
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
