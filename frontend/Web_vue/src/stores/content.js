import { defineStore } from 'pinia'
import api from '../utils/axios'
import logger from '../utils/logger'

export const useContentStore = defineStore('content', {
  state: () => ({
    contents: [],
    currentContent: null,
    comments: [],
    loading: false
  }),

  actions: {
    async getContents(params) {
      this.loading = true
      try {
        const response = await api.get('/contents', { params })
        logger.info('CONTENT_LIST_LOADED', {
          params,
          code: response.data?.code
        })
        return response
      } catch (error) {
        logger.error('CONTENT_LIST_FAILED', {
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async getContentDetail(contentId) {
      this.loading = true
      try {
        const response = await api.get(`/contents/${contentId}`)
        this.currentContent = response.data.data
        logger.info('CONTENT_DETAIL_LOADED', { contentId })
        return response
      } catch (error) {
        logger.error('CONTENT_DETAIL_FAILED', {
          contentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async createContent(contentData) {
      this.loading = true
      try {
        const response = await api.post('/contents', contentData)
        logger.event('CONTENT_CREATED', { contentData })
        return response
      } catch (error) {
        logger.error('CONTENT_CREATE_FAILED', {
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteContent(contentId) {
      this.loading = true
      try {
        const response = await api.delete(`/contents/${contentId}`)
        logger.event('CONTENT_DELETED', { contentId })
        return response
      } catch (error) {
        logger.error('CONTENT_DELETE_FAILED', {
          contentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async uploadMedia(contentId, formData) {
      this.loading = true
      try {
        const response = await api.post(`/contents/${contentId}/media`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        logger.event('CONTENT_MEDIA_UPLOADED', { contentId })
        return response
      } catch (error) {
        logger.error('CONTENT_MEDIA_UPLOAD_FAILED', {
          contentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async createComment(contentId, commentData) {
      this.loading = true
      try {
        const response = await api.post(`/contents/${contentId}/comments`, commentData)
        logger.event('CONTENT_COMMENT_CREATED', { contentId })
        return response
      } catch (error) {
        logger.error('CONTENT_COMMENT_CREATE_FAILED', {
          contentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteComment(commentId) {
      this.loading = true
      try {
        const response = await api.delete(`/contents/comments/${commentId}`)
        logger.event('CONTENT_COMMENT_DELETED', { commentId })
        return response
      } catch (error) {
        logger.error('CONTENT_COMMENT_DELETE_FAILED', {
          commentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async getComments(contentId, params) {
      this.loading = true
      try {
        const response = await api.get(`/contents/${contentId}/comments`, { params })
        this.comments = response.data.data.list
        logger.info('CONTENT_COMMENTS_LOADED', { contentId })
        return response
      } catch (error) {
        logger.error('CONTENT_COMMENTS_FAILED', {
          contentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async likeContent(contentId) {
      this.loading = true
      try {
        const response = await api.post(`/contents/${contentId}/like`)
        logger.event('CONTENT_LIKE_TOGGLED', { contentId })
        return response
      } catch (error) {
        logger.error('CONTENT_LIKE_FAILED', {
          contentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async getLikes(contentId) {
      this.loading = true
      try {
        const response = await api.get(`/contents/${contentId}/likes`)
        logger.info('CONTENT_LIKES_LOADED', { contentId })
        return response
      } catch (error) {
        logger.error('CONTENT_LIKES_FAILED', {
          contentId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})
