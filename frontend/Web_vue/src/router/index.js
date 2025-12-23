import { createRouter, createWebHistory } from 'vue-router'
import logger from '../utils/logger'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/OrdersView.vue'),
    meta: { title: '订单列表', requiresAuth: true }
  },
  {
    path: '/orders/create',
    name: 'CreateOrder',
    component: () => import('../views/CreateOrderView.vue'),
    meta: { title: '发布订单', requiresAuth: true }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetailView.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
    path: '/contents',
    name: 'Contents',
    component: () => import('../views/ContentsView.vue'),
    meta: { title: '动态列表' }
  },
  {
    path: '/contents/create',
    name: 'CreateContent',
    component: () => import('../views/CreateContentView.vue'),
    meta: { title: '发布动态', requiresAuth: true }
  },
  {
    path: '/ai',
    name: 'AI',
    component: () => import('../views/AIView.vue'),
    meta: { title: 'AI问询' }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfileView.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园 Companion` : '校园 Companion'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (token) {
      next()
    } else {
      next({ name: 'Login' })
    }
  } else {
    next()
  }
})

// 路由跳转完成后记录导航日志
router.afterEach((to, from) => {
  logger.event('ROUTE_NAVIGATE', {
    from: from.fullPath,
    to: to.fullPath,
    requiresAuth: to.meta?.requiresAuth || false
  })
})

export default router
