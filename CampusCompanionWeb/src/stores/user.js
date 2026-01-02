import { defineStore } from 'pinia'
import axios from '../utils/axios'

// 将后端返回的头像相对路径（如 /uploads/xxx.png）转换为可在前端直接访问的完整 URL
const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'

const resolveAvatarUrl = (url) => {
  if (!url) return url
  // 已经是完整 URL 则直接返回
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    users: [],
    loading: false,
    error: null
  }),

  actions: {
    async fetchCurrentUser() {
      try {
        this.loading = true
        const storedId = localStorage.getItem('userId')
        if (!storedId) {
          this.loading = false
          return null
        }

        const response = await axios.get(`/users/${storedId}`)
        // 后端统一响应：{ code, message, data, timestamp }
        this.currentUser = response.data.data
        if (this.currentUser && this.currentUser.avatarUrl) {
          this.currentUser.avatarUrl = resolveAvatarUrl(this.currentUser.avatarUrl)
        }
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || '获取用户信息失败'
        throw error.response?.data || error
      } finally {
        this.loading = false
      }
    },

    async updateUser(userData) {
      try {
        this.loading = true
        const storedId = localStorage.getItem('userId')
        if (!storedId) {
          throw new Error('未找到用户ID，请重新登录')
        }

        const response = await axios.put(`/users/${storedId}`, userData)
        this.currentUser = response.data.data
        if (this.currentUser && this.currentUser.avatarUrl) {
          this.currentUser.avatarUrl = resolveAvatarUrl(this.currentUser.avatarUrl)
        }
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || '更新用户信息失败'
        throw error.response?.data || error
      } finally {
        this.loading = false
      }
    },

    async updatePassword(passwordData) {
      try {
        this.loading = true
        const storedId = localStorage.getItem('userId')
        if (!storedId) {
          throw new Error('未找到用户ID，请重新登录')
        }

        const response = await axios.put(`/users/${storedId}/password`, passwordData)
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || '修改密码失败'
        throw error.response?.data || error
      } finally {
        this.loading = false
      }
    },

    async uploadAvatar(file) {
      try {
        this.loading = true
        const storedId = localStorage.getItem('userId')
        if (!storedId) {
          throw new Error('未找到用户ID，请重新登录')
        }

        const formData = new FormData()
        formData.append('avatar', file)

        const response = await axios.post(`/users/${storedId}/avatar`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        const avatarUrl = resolveAvatarUrl(response.data.data)
        if (!this.currentUser) {
          this.currentUser = { avatarUrl }
        } else {
          this.currentUser.avatarUrl = avatarUrl
        }
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || '头像上传失败'
        throw error.response?.data || error
      } finally {
        this.loading = false
      }
    },

    async searchUsers(keyword, page = 1, size = 10) {
      try {
        this.loading = true
        const response = await axios.get('/users/search', {
          params: {
            keyword,
            page,
            size
          }
        })
        this.users = response.data.data
        return response.data
      } catch (error) {
        this.error = error.response.data.message
        throw error.response.data
      } finally {
        this.loading = false
      }
    }
  }
})