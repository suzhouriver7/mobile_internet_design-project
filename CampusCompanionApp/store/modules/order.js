/**
 * 订单模块
 */
const state = {
  orderList: [],
  currentOrder: null,
  filter: {
    status: null,
    activityType: null,
    campus: null
  }
}

const mutations = {
  SET_ORDER_LIST(state, list) {
    state.orderList = list
  },
  SET_CURRENT_ORDER(state, order) {
    state.currentOrder = order
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
}

const actions = {
  setOrderList({ commit }, list) {
    commit('SET_ORDER_LIST', list)
  },
  setCurrentOrder({ commit }, order) {
    commit('SET_CURRENT_ORDER', order)
  },
  setFilter({ commit }, filter) {
    commit('SET_FILTER', filter)
  },
  clearFilter({ commit }) {
    commit('CLEAR_FILTER')
  }
}

const getters = {
  orderList: state => state.orderList,
  currentOrder: state => state.currentOrder,
  filter: state => state.filter
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

