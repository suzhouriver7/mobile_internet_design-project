import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import router from './router'
import logger from './utils/logger'

const app = createApp(App)
const pinia = createPinia()

app.use(ElementPlus)
app.use(pinia)
app.use(router)

// 全局错误处理，补充错误日志
app.config.errorHandler = (err, instance, info) => {
	logger.error('VUE_ERROR', {
		message: err?.message,
		stack: err?.stack,
		info
	})
	console.error(err)
}

window.addEventListener('error', (event) => {
	logger.error('WINDOW_ERROR', {
		message: event.error?.message,
		stack: event.error?.stack,
		source: event.filename,
		lineno: event.lineno,
		colno: event.colno
	})
})

window.addEventListener('unhandledrejection', (event) => {
	logger.error('UNHANDLED_REJECTION', {
		reason: event.reason
	})
})

app.mount('#app')
