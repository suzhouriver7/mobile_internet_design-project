import { defineStore } from 'pinia'
import api from '../utils/axios'
import logger from '../utils/logger'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [],
    currentOrder: null,
    loading: false
  }),

  actions: {
    async getOrders(params) {
      this.loading = true
      try {
        const response = await api.get('/orders', { params })
        logger.info('ORDER_LIST_LOADED', {
          params,
          code: response.data?.code
        })
        return response
      } catch (error) {
        logger.error('ORDER_LIST_FAILED', {
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async getOrderDetail(orderId) {
      this.loading = true
      try {
        const response = await api.get(`/orders/${orderId}`)
        this.currentOrder = response.data.data
        logger.info('ORDER_DETAIL_LOADED', { orderId })
        return response
      } catch (error) {
        logger.error('ORDER_DETAIL_FAILED', {
          orderId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async createOrder(orderData) {
      this.loading = true
      try {
        const response = await api.post('/orders', orderData)
        logger.event('ORDER_CREATED', { orderData })
        return response
      } catch (error) {
        logger.error('ORDER_CREATE_FAILED', {
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async applyOrder(orderId, message = '') {
      this.loading = true
      try {
        const response = await api.post(`/orders/${orderId}/apply`, { message })
        logger.event('ORDER_APPLY', { orderId })
        return response
      } catch (error) {
        logger.error('ORDER_APPLY_FAILED', {
          orderId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateOrder(orderId, orderData) {
      this.loading = true
      try {
        const response = await api.put(`/orders/${orderId}`, orderData)
        logger.event('ORDER_UPDATED', { orderId })
        return response
      } catch (error) {
        logger.error('ORDER_UPDATE_FAILED', {
          orderId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteOrder(orderId) {
      this.loading = true
      try {
        const response = await api.delete(`/orders/${orderId}`)
        logger.event('ORDER_DELETED', { orderId })
        return response
      } catch (error) {
        logger.error('ORDER_DELETE_FAILED', {
          orderId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async completeOrder(orderId) {
      this.loading = true
      try {
        const response = await api.put(`/orders/${orderId}/complete`)
        logger.event('ORDER_COMPLETED', { orderId })
        return response
      } catch (error) {
        logger.error('ORDER_COMPLETE_FAILED', {
          orderId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async getApplications(orderId) {
      this.loading = true
      try {
        const response = await api.get(`/orders/${orderId}/applications`)
        logger.info('ORDER_APPLICATIONS_LOADED', { orderId })
        return response
      } catch (error) {
        logger.error('ORDER_APPLICATIONS_FAILED', {
          orderId,
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async auditApplication(applyId, status) {
      this.loading = true
      try {
        const response = await api.put(`/orders/applications/${applyId}`, { status })
        logger.event('ORDER_APPLICATION_AUDIT', { applyId, status })
        return response
      } catch (error) {
        logger.error('ORDER_APPLICATION_AUDIT_FAILED', {
          applyId,
          status,
          message: error.response?.data?.message || error.message,
          statusCode: error.response?.status
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async acceptOrder(orderId, accepterId) {
      this.loading = true
      try {
        const response = await api.post(`/orders/${orderId}/accept`, { accepterId })
        logger.event('ORDER_ACCEPT', { orderId, accepterId })
        return response
      } catch (error) {
        logger.error('ORDER_ACCEPT_FAILED', {
          orderId,
          accepterId,
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
