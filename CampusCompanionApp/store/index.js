import { createStore } from 'vuex'
import userModule from './modules/user.js'

const store = createStore({
  modules: {
    user: userModule
  },
  strict: false
})

export default store
