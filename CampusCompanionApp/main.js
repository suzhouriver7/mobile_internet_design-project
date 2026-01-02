import { createSSRApp } from 'vue'
import App from './App.vue'
import store from './store/index.js'

export function createApp() {
  const app = createSSRApp(App)
  app.use(store)
  return { app }
}
