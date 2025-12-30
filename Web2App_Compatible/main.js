import { createSSRApp } from 'vue'
import App from './App.vue'
import store from './store/index.js'

// 导入uview-plus UI库
import uView from 'uview-plus'

// 导入工具函数
import * as util from '@/utils/util.js'

export function createApp() {
  const app = createSSRApp(App)
  
  // 使用状态管理
  app.use(store)
  
  // 使用uview-plus UI库
  app.use(uView)
  
  // 初始化用户状态
  store.dispatch('initUserState')
  
  // 将工具函数挂载到全局，方便使用
  app.config.globalProperties.$util = util
  
  // 全局错误处理
  app.config.errorHandler = (err, instance, info) => {
    console.error('Vue全局错误:', err)
    console.error('发生在组件:', instance)
    console.error('错误信息:', info)
    
    // 生产环境可以上报错误
    // if (!import.meta.env.DEV) {
    //   // 错误上报逻辑
    // }
  }
  
  return {
    app,
    store // 需要返回store以供SSR使用
  }
}