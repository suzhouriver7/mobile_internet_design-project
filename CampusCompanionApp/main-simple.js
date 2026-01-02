// 最简单的入口文件，用于测试
import { createSSRApp } from 'vue'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)
  console.log('Vue 应用已创建')
  return {
    app
  }
}

