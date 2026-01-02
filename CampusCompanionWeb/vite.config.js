import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 前端所有以 /api 开头的请求，转发到后端 http://localhost:8080
      // axios 的 baseURL 是 /api/v1，因此在开发环境下会由 Vite 代理到后端 /api/v1
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
