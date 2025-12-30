import request from '@/utils/request.js'
import { ORDER_STATUS, ACTIVITY_TYPE, CAMPUS } from '@/utils/constants.js'

/**
 * 订单相关API服务
 * 对应后端 OrderController
 */

const orderApi = {
  /**
   * 发布预约订单
   * @param {Object} orderData - 订单数据
   * @returns {Promise} 订单ID
   */
  createOrder(orderData) {
    return request.post('/orders', orderData)
  },
  
  /**
   * 获取订单列表（支持多条件筛选）
   * @param {Object} params - 查询参数
   * @returns {Promise} 分页订单列表
   */
  getOrders(params = {}) {
    const {
      page = 1,
      size = 10,
      status = null,
      activityType = null,
      campus = null
    } = params
    
    const queryParams = {}
    if (page) queryParams.page = page
    if (size) queryParams.size = size
    if (status !== null && status !== undefined) queryParams.status = status
    if (activityType !== null && activityType !== undefined) queryParams.activityType = activityType
    if (campus !== null && campus !== undefined) queryParams.campus = campus
    
    return request.get('/orders', queryParams)
  },
  
  /**
   * 获取订单详情
   * @param {number} orderId - 订单ID
   * @returns {Promise} 订单详情（包含申请列表）
   */
  getOrderDetail(orderId) {
    return request.get(`/orders/${orderId}`)
  },
  
  /**
   * 修改订单信息
   * @param {number} orderId - 订单ID
   * @param {Object} orderData - 修改后的订单数据
   * @returns {Promise} 订单ID
   */
  updateOrder(orderId, orderData) {
    return request.put(`/orders/${orderId}`, orderData)
  },
  
  /**
   * 删除订单（逻辑删除）
   * @param {number} orderId - 订单ID
   * @returns {Promise}
   */
  deleteOrder(orderId) {
    return request.delete(`/orders/${orderId}`)
  },
  
  /**
   * 完成订单
   * @param {number} orderId - 订单ID
   * @returns {Promise}
   */
  completeOrder(orderId) {
    return request.put(`/orders/${orderId}/complete`)
  },
  
  /**
   * 申请加入订单
   * @param {number} orderId - 订单ID
   * @param {Object} applyData - 申请数据 {message}
   * @returns {Promise} 申请记录ID
   */
  applyOrder(orderId, applyData = {}) {
    return request.post(`/orders/${orderId}/apply`, applyData)
  },
  
  /**
   * 取消申请
   * @param {number} orderId - 订单ID
   * @returns {Promise}
   */
  cancelApply(orderId) {
    return request.delete(`/orders/${orderId}/apply`)
  },
  
  /**
   * 获取申请列表
   * @param {number} orderId - 订单ID
   * @returns {Promise} 申请列表
   */
  getApplications(orderId) {
    return request.get(`/orders/${orderId}/applications`)
  },
  
  /**
   * 审核申请
   * @param {number} applyId - 申请记录ID
   * @param {Object} auditData - 审核数据 {status}
   * @returns {Promise}
   */
  auditApply(applyId, auditData) {
    return request.put(`/orders/applications/${applyId}`, auditData)
  },
  
  /**
   * 接受订单
   * @param {number} orderId - 订单ID
   * @param {Object} acceptData - 接受数据 {accepterId}
   * @returns {Promise}
   */
  acceptOrder(orderId, acceptData) {
    return request.post(`/orders/${orderId}/accept`, acceptData)
  },
  
  /**
   * 发送订单消息
   * @param {number} orderId - 订单ID
   * @param {Object} messageData - 消息数据 {content}
   * @returns {Promise} 消息ID
   */
  sendOrderMessage(orderId, messageData) {
    return request.post(`/orders/${orderId}/messages`, messageData)
  },
  
  /**
   * 获取订单消息
   * @param {number} orderId - 订单ID
   * @param {Object} params - 分页参数 {page, size}
   * @returns {Promise} 分页消息列表
   */
  getOrderMessages(orderId, params = {}) {
    const { page = 1, size = 20 } = params
    return request.get(`/orders/${orderId}/messages`, { page, size })
  }
}

export default orderApi