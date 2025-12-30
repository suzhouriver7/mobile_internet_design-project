import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: uni.getStorageSync('userId') || '',
    userInfo: null
  }),
  actions: {
    // 获取用户信息
    async getUserInfo() {
      if (!this.userId) throw new Error('未登录')
      return request({
        url: `/users/${this.userId}/info`,
        method: 'GET'
      })
    },
    // 更新用户信息
    async updateUserInfo(data) {
      if (!this.userId) throw new Error('未登录')
      return request({
        url: `/users/${this.userId}/info`,
        method: 'PUT',
        data
      })
    },
    // 修改密码
    async changePassword(data) {
      if (!this.userId) throw new Error('未登录')
      return request({
        url: `/users/${this.userId}/password`,
        method: 'PUT',
        data
      })
    },
    // 搜索用户
    async searchUsers(keyword, page = 1, size = 10) {
      return request({
        url: '/users/search',
        method: 'GET',
        data: { keyword, page, size }
      })
    }
  }
})