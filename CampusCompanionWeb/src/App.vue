<template>
  <div class="app-container">
    <!-- 导航栏 -->
    <el-header height="60px" class="header">
      <div class="header-content">
        <div class="logo">
          <router-link to="/" class="logo-text">校园 companion</router-link>
        </div>
        <!-- 桌面端导航菜单 -->
        <el-menu 
          :default-active="activeIndex" 
          class="nav-menu" 
          mode="horizontal"
          router
          @select="handleSelect"
        >
          <el-menu-item index="/">
            首页
          </el-menu-item>
          <el-menu-item index="/orders">
            订单
          </el-menu-item>
          <el-menu-item index="/contents">
            动态
          </el-menu-item>
          <el-menu-item index="/ai">
            AI问询
          </el-menu-item>
        </el-menu>
        <!-- 移动端下拉菜单 -->
        <div class="mobile-nav">
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-icon><MenuIcon /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="navigate('/')">首页</el-dropdown-item>
                <el-dropdown-item @click="navigate('/orders')">订单</el-dropdown-item>
                <el-dropdown-item @click="navigate('/contents')">动态</el-dropdown-item>
                <el-dropdown-item @click="navigate('/ai')">AI问询</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="user-info">
          <el-dropdown v-if="isAuthenticated">
            <span class="el-dropdown-link user-dropdown">
              <template v-if="navAvatarUrl">
                <img :src="navAvatarUrl" @error="onNavAvatarError" loading="lazy" alt="avatar" class="nav-avatar-img" width="32" height="32" />
              </template>
              <template v-else>
                <el-avatar :size="32" class="nav-avatar-fallback">{{ userInitial }}</el-avatar>
              </template>
              <span class="nav-nickname">{{ (userInfo && userInfo.nickname) || '用户' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item><router-link to="/user/profile" style="color:#222; text-decoration:none">个人中心</router-link></el-dropdown-item>
                <el-dropdown-item><router-link to="/orders/create" style="color:#222; text-decoration:none">发布订单</router-link></el-dropdown-item>
                <el-dropdown-item><router-link to="/contents/create" style="color:#222; text-decoration:none">发布动态</router-link></el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <div v-else class="login-register">
            <router-link to="/login" class="login-btn">登录</router-link>
            <router-link to="/register" class="register-btn">注册</router-link>
          </div>
        </div>
      </div>
    </el-header>
    
    <!-- 主要内容区域 -->
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
    
    <!-- 页脚 -->
    <el-footer height="80px" class="footer">
      <div class="footer-content">
        <p>© 2025 校园 companion. All rights reserved.</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { ArrowDown, Menu as MenuIcon } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// 计算用户认证状态（提前定义，供后续 avatar 处理使用）
const isAuthenticated = computed(() => authStore.isAuthenticated)
// 直接返回 store 中的用户对象，便于判断是否需要拉取用户信息
const userInfo = computed(() => authStore.user)

// navigation avatar handling
const navAvatarUrl = ref(null)
const navAvatarError = ref(false)
const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'

const resolveUrl = (url) => {
  if (!url) return null
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const userInitial = computed(() => {
  const u = userInfo.value
  if (u?.nickname) return u.nickname.slice(0, 1)
  if (u?.email) return u.email.slice(0, 1).toUpperCase()
  return '用'
})

watch(userInfo, (val) => {
  navAvatarError.value = false
  navAvatarUrl.value = val?.avatarUrl ? resolveUrl(val.avatarUrl) : null
}, { immediate: true })

const onNavAvatarError = () => {
  navAvatarError.value = true
  navAvatarUrl.value = null
}

// 计算当前激活的菜单索引（使用路径，配合 el-menu 的 router 模式）
const activeIndex = computed(() => {
  const currentPath = router.currentRoute.value.path
  if (currentPath.startsWith('/orders')) return '/orders'
  if (currentPath.startsWith('/contents')) return '/contents'
  if (currentPath.startsWith('/ai')) return '/ai'
  return '/'
})


// 处理菜单选择（此处仅做调试/埋点，实际导航由 el-menu 的 router 属性完成）
const handleSelect = (key, keyPath) => {
  console.log('Selected:', key, keyPath)
}

// 移动端菜单导航
const navigate = (path) => {
  router.push(path)
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('Logout error:', error)
  }
}

// 页面加载时检查用户登录状态
onMounted(async () => {
  if (isAuthenticated.value && !userInfo.value) {
    try {
      await authStore.getUserInfo()
    } catch (error) {
      console.error('Failed to get user info:', error)
      authStore.logout()
    }
  }
})
</script>

<style lang="scss">
// 全局样式重置
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
  color: #333;
  line-height: 1.6;
}

// 主容器
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// 头部样式
.header {
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 100%;
  }
  
  .logo {
    font-size: 20px;
    font-weight: bold;
    
    .logo-text {
      color: #409eff;
      text-decoration: none;
      transition: color 0.3s;
      
      &:hover {
        color: #66b1ff;
      }
      .ai-quick {
        margin-left: 12px;
        font-size: 14px;
        color: #606266;
        text-decoration: none;
        padding: 6px 8px;
        border-radius: 6px;
      }
    }
  }
  
  .nav-menu {
    flex: 1;
    justify-content: center;
    background-color: transparent;
    border-bottom: none;
    
    .el-menu-item {
      padding: 0 20px;
      height: 60px;
      line-height: 60px;
      
      a {
        color: #606266;
        text-decoration: none;
        transition: color 0.3s;
        
        &:hover {
          color: #409eff;
        }
      }
    }
    
    .el-menu-item.is-active {
      a {
        color: #409eff;
      }
    }
  }

  .mobile-nav {
    display: none;
    margin-left: 16px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    
    .el-dropdown-link {
      cursor: pointer;
      color: #606266;
      transition: color 0.3s;
      display: flex;
      align-items: center;
      gap: 8px;
      
      &:hover {
        color: #409eff;
      }
    }

    .user-dropdown {
      .nav-avatar-img {
        width: 32px;
        height: 32px;
        object-fit: cover;
        border-radius: 50%;
        display: inline-block;
        vertical-align: middle;
      }
      .nav-avatar-fallback {
        background-color: #f0f2f5;
        color: #606266;
        display: inline-flex;
        align-items: center;
        justify-content: center;
      }
      .nav-nickname {
        margin: 0 6px;
        font-size: 14px;
        color: #606266;
      }
    }
    
    .login-register {
      display: flex;
      gap: 16px;
      
      .login-btn, .register-btn {
        padding: 6px 16px;
        border-radius: 4px;
        text-decoration: none;
        font-size: 14px;
        transition: all 0.3s;
      }
      
      .login-btn {
        color: #409eff;
        
        &:hover {
          background-color: rgba(64, 158, 255, 0.1);
        }
      }
      
      .register-btn {
        background-color: #409eff;
        color: #fff;
        
        &:hover {
          background-color: #66b1ff;
        }
      }
    }
    // 覆盖下拉菜单项颜色为深色，避免默认蓝色链接样式
    .el-dropdown-menu {
      .el-dropdown-item {
        color: #222 !important;
      }
      a {
        color: inherit !important;
        text-decoration: none;
      }
    }
  }
}

// 主要内容区域
.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-top: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
}

// 页脚样式
.footer {
  background-color: #ffffff;
  color: #909399;
  text-align: center;
  padding: 20px 0;
  border-top: 1px solid #ebedf0;
  
  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式设计
@media (max-width: 768px) {
  .header {
    .header-content {
      padding: 0 10px;
    }
    
    .logo {
      font-size: 16px;
    }
    
    .nav-menu {
      display: none;
    }

    .mobile-nav {
      display: block;
    }
    
    .user-info {
      .login-register {
        gap: 8px;
        
        .login-btn, .register-btn {
          padding: 4px 12px;
          font-size: 12px;
        }
      }
    }
  }
  
  .main-content {
    padding: 10px;
    margin-top: 10px;
    margin-bottom: 10px;
  }
}
</style>
