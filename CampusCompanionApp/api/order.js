import { get, post, put, del } from '../utils/request.js'

export default {
  createOrder(data) {
    return post('/orders', data)
  },
  getOrders(params = {}) {
    return get('/orders', params)
  },
  getOrderDetail(orderId) {
    return get(`/orders/${orderId}`)
  },
  updateOrder(orderId, data) {
    return put(`/orders/${orderId}`, data)
  },
  deleteOrder(orderId) {
    return del(`/orders/${orderId}`)
  },
  applyOrder(orderId, message = '') {
    return post(`/orders/${orderId}/apply`, { message })
  },
  cancelApply(orderId) {
    return del(`/orders/${orderId}/apply`)
  },
  getApplications(orderId) {
    return get(`/orders/${orderId}/applications`)
  },
  auditApply(applyId, status) {
    return put(`/orders/applications/${applyId}`, { status })
  },
  sendOrderMessage(orderId, content) {
    return post(`/orders/${orderId}/messages`, { content })
  },
  getOrderMessages(orderId, page = 1, size = 20) {
    return get(`/orders/${orderId}/messages`, { page, size })
  }
}
