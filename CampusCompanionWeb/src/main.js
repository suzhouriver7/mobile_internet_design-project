import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import router from './router'
import logger from './utils/logger'
import { useAuthStore } from './stores/auth'

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

// 在挂载应用前进行一次同步的登录状态初始化，避免未授权内容闪现。
const initApp = async () => {
	// 在 app.use(pinia) 之后才能使用 store
	const authStore = useAuthStore()
	const token = localStorage.getItem('token')
	if (token) {
		try {
			// 尝试拉取用户信息校验 token
			await authStore.getUserInfo()
		} catch (e) {
			// 若获取用户信息失败（如 token 过期），清理本地状态并跳回登录
			try {
				await authStore.logout()
			} catch (err) {
				// 忽略 logout 中的错误
			}
		}
	}
	app.mount('#app')
}

initApp()
