import { defineStore } from 'pinia'
import axios from '../utils/axios'

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
        const response = await axios.get('/users/current')
        this.currentUser = response.data.data
        return response.data
      } catch (error) {
        this.error = error.response.data.message
        throw error.response.data
      } finally {
        this.loading = false
      }
    },

    async updateUser(userData) {
      try {
        this.loading = true
        const response = await axios.put('/users/current', userData)
        this.currentUser = response.data.data
        return response.data
      } catch (error) {
        this.error = error.response.data.message
        throw error.response.data
      } finally {
        this.loading = false
      }
    },

    async updatePassword(passwordData) {
      try {
        this.loading = true
        const response = await axios.put('/users/password', passwordData)
        return response.data
      } catch (error) {
        this.error = error.response.data.message
        throw error.response.data
      } finally {
        this.loading = false
      }
    },

    async uploadAvatar(formData) {
      try {
        this.loading = true
        const response = await axios.post('/upload/avatar', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        this.currentUser.avatar = response.data.data.url
        return response.data
      } catch (error) {
        this.error = error.response.data.message
        throw error.response.data
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