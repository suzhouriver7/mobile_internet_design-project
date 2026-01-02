/**
 * 动态模块
 */
const state = {
  contentList: [],
  currentContent: null
}

const mutations = {
  SET_CONTENT_LIST(state, list) {
    state.contentList = list
  },
  SET_CURRENT_CONTENT(state, content) {
    state.currentContent = content
  }
}

const actions = {
  setContentList({ commit }, list) {
    commit('SET_CONTENT_LIST', list)
  },
  setCurrentContent({ commit }, content) {
    commit('SET_CURRENT_CONTENT', content)
  }
}

const getters = {
  contentList: state => state.contentList,
  currentContent: state => state.currentContent
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

