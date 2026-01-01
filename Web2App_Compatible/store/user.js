/**
 * 用户模块 Vuex Store
 * 与主 store/index.js 集成
 */

export default {
  namespaced: true,
  
  state: {
    userInfo: null,
    isLogin: false
  },
  
  getters: {
    userInfo: state => state.userInfo,
    userId: state => state.userInfo ? state.userInfo.id : null,
    nickname: state => state.userInfo ? state.userInfo.nickname : '未登录',
    avatarUrl: state => state.userInfo ? state.userInfo.avatarUrl : '',
    userType: state => state.userInfo ? state.userInfo.userType : 0,
    isAdmin: state => state.userInfo ? state.userInfo.userType === 1 : false,
    isLogin: state => state.isLogin
  },
  
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      state.isLogin = !!userInfo
      
      if (userInfo) {
        uni.setStorageSync('user', JSON.stringify(userInfo))
      } else {
        uni.removeStorageSync('user')
      }
    },
    
    UPDATE_USER_INFO(state, updates) {
      if (state.userInfo) {
        state.userInfo = { ...state.userInfo, ...updates }
        uni.setStorageSync('user', JSON.stringify(state.userInfo))
      }
    },
    
    CLEAR_USER(state) {
      state.userInfo = null
      state.isLogin = false
      uni.removeStorageSync('user')
    }
  },
  
  actions: {
    // 登录
    async login({ commit }, loginResponse) {
      // loginResponse 可能是 { userInfo, token } 或直接是 userInfo
      let userInfo
      if (loginResponse.userInfo) {
        userInfo = loginResponse.userInfo
      } else if (loginResponse.id) {
        userInfo = loginResponse
      } else {
        userInfo = null
      }
      commit('SET_USER_INFO', userInfo)
      return userInfo
    },
    
    // 退出登录
    async logout({ commit }) {
      commit('CLEAR_USER')
    },
    
    // 更新用户信息
    async updateUserInfo({ commit }, updates) {
      commit('UPDATE_USER_INFO', updates)
    }
  }
}