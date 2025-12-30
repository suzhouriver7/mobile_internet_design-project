import { defineStore } from 'pinia'
import request from '../utils/request'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [],
    currentOrder: null
  }),
  actions: {
    // 获取订单列表
    async getOrders(params) {
      const res = await request({
        url: '/orders',
        data: params
      })
      this.orders = res.data.list
      return res
    },
    // 创建订单
    async createOrder(data) {
      return request({
        url: '/orders',
        method: 'POST',
        data
      })
    },
    // 获取订单详情
    async getOrderDetail(orderId) {
      const res = await request({
        url: `/orders/${orderId}`
      })
      this.currentOrder = res.data.data
      return res
    },
    // 申请订单
    async applyOrder(orderId) {
      return request({
        url: `/orders/${orderId}/apply`,
        method: 'POST'
      })
    }
  }
})