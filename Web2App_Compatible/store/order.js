/**
 * 订单模块 Vuex Store
 * 与主 store/index.js 集成
 */

export default {
  namespaced: true,
  
  state: {
    orders: [],
    currentOrder: null,
    filter: {
      status: null,
      activityType: null,
      campus: null
    }
  },
  
  getters: {
    orderList: state => state.orders,
    currentOrderDetail: state => state.currentOrder,
    filter: state => state.filter
  },
  
  mutations: {
    SET_ORDERS(state, orders) {
      state.orders = orders
    },
    
    ADD_ORDERS(state, orders) {
      state.orders = [...state.orders, ...orders]
    },
    
    SET_CURRENT_ORDER(state, order) {
      state.currentOrder = order
    },
    
    UPDATE_ORDER(state, updatedOrder) {
      const index = state.orders.findIndex(o => o.id === updatedOrder.id)
      if (index !== -1) {
        state.orders[index] = updatedOrder
      }
      if (state.currentOrder && state.currentOrder.id === updatedOrder.id) {
        state.currentOrder = updatedOrder
      }
    },
    
    REMOVE_ORDER(state, orderId) {
      state.orders = state.orders.filter(o => o.id !== orderId)
      if (state.currentOrder && state.currentOrder.id === orderId) {
        state.currentOrder = null
      }
    },
    
    SET_FILTER(state, filter) {
      state.filter = { ...state.filter, ...filter }
    },
    
    CLEAR_FILTER(state) {
      state.filter = {
        status: null,
        activityType: null,
        campus: null
      }
    }
  },
  
  actions: {
    // 设置订单列表
    setOrders({ commit }, orders) {
      commit('SET_ORDERS', orders)
    },
    
    // 添加订单（用于分页加载）
    addOrders({ commit }, orders) {
      commit('ADD_ORDERS', orders)
    },
    
    // 设置当前订单详情
    setCurrentOrder({ commit }, order) {
      commit('SET_CURRENT_ORDER', order)
    },
    
    // 更新订单
    updateOrder({ commit }, order) {
      commit('UPDATE_ORDER', order)
    },
    
    // 删除订单
    removeOrder({ commit }, orderId) {
      commit('REMOVE_ORDER', orderId)
    },
    
    // 设置筛选条件
    setFilter({ commit }, filter) {
      commit('SET_FILTER', filter)
    },
    
    // 清除筛选条件
    clearFilter({ commit }) {
      commit('CLEAR_FILTER')
    }
  }
}