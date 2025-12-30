<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="background-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>

    <!-- 头部 -->
    <view class="login-header">
      <view class="back-btn" @click="goBack" v-if="showBack">
        <u-icon name="arrow-left" size="24" color="#333"></u-icon>
      </view>
      <text class="login-title">欢迎回来</text>
      <text class="login-subtitle">登录你的校园约伴账号</text>
    </view>

    <!-- 登录表单 -->
    <view class="login-form">
      <view class="form-group">
        <text class="form-label">邮箱/学号</text>
        <u-input
          v-model="form.identifier"
          placeholder="请输入邮箱或学号"
          border="bottom"
          clearable
          :custom-style="inputStyle"
          @confirm="onSubmit"
        >
          <template #prefix>
            <u-icon name="account" size="20" color="#999"></u-icon>
          </template>
        </u-input>
        <text class="error-text" v-if="errors.identifier">{{ errors.identifier }}</text>
      </view>

      <view class="form-group">
        <text class="form-label">密码</text>
        <u-input
          v-model="form.password"
          placeholder="请输入密码"
          border="bottom"
          clearable
          password
          :custom-style="inputStyle"
          @confirm="onSubmit"
        >
          <template #prefix>
            <u-icon name="lock" size="20" color="#999"></u-icon>
          </template>
        </u-input>
        <text class="error-text" v-if="errors.password">{{ errors.password }}</text>
      </view>

      <!-- 记住密码和忘记密码 -->
      <view class="form-options">
        <view class="remember-me" @click="rememberMe = !rememberMe">
          <u-checkbox
            v-model="rememberMe"
            shape="circle"
            label="记住密码"
            label-size="14"
            :label-color="rememberMe ? '#007aff' : '#666'"
          ></u-checkbox>
        </view>
        <text class="forgot-password" @click="toForgotPassword">忘记密码？</text>
      </view>

      <!-- 登录按钮 -->
      <view class="form-submit">
        <u-button
          type="primary"
          shape="circle"
          :loading="loading"
          :disabled="!form.identifier || !form.password"
          @click="onSubmit"
          :custom-style="{
            height: '88rpx',
            fontSize: '32rpx',
            fontWeight: '500'
          }"
        >
          {{ loading ? '登录中...' : '登录' }}
        </u-button>
      </view>

      <!-- 第三方登录 -->
      <view class="social-login" v-if="showSocialLogin">
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">其他方式登录</text>
          <view class="divider-line"></view>
        </view>

        <view class="social-buttons">
          <button class="social-btn wechat" @click="onWechatLogin">
            <u-icon name="weixin-fill" size="40" color="#07c160"></u-icon>
            <text>微信</text>
          </button>
          <button class="social-btn qq" @click="onQQLogin">
            <u-icon name="qq-fill" size="40" color="#12b7f5"></u-icon>
            <text>QQ</text>
          </button>
        </view>
      </view>

      <!-- 注册链接 -->
      <view class="register-link">
        <text>还没有账号？</text>
        <text class="link-text" @click="toRegister">立即注册</text>
      </view>
    </view>

    <!-- 协议提示 -->
    <view class="agreement-tip">
      <text>登录即代表你已同意</text>
      <text class="link-text" @click="toUserAgreement">《用户协议》</text>
      <text>和</text>
      <text class="link-text" @click="toPrivacyPolicy">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util'

// 使用Pinia store
const userStore = useUserStore()

// 响应式数据
const form = reactive({
  identifier: '',
  password: ''
})

const errors = reactive({
  identifier: '',
  password: ''
})

const rememberMe = ref(false)
const loading = ref(false)
const showBack = ref(false)
const showSocialLogin = ref(false)

// 计算属性
const inputStyle = computed(() => ({
  fontSize: '30rpx',
  padding: '20rpx 0'
}))

// 生命周期
onLoad((options) => {
  // 检查是否有返回按钮（从其他页面跳转过来）
  const pages = getCurrentPages()
  showBack.value = pages.length > 1
  
  // 检查是否支持第三方登录
  // #ifdef MP-WEIXIN
  showSocialLogin.value = true
  // #endif
  
  // 尝试读取记住的账号
  loadRememberedAccount()
})

onShow(() => {
  // 页面显示时清空密码（安全考虑）
  form.password = ''
})

// 方法
const loadRememberedAccount = () => {
  try {
    const remembered = uni.getStorageSync('remembered_account')
    if (remembered) {
      form.identifier = remembered.identifier || ''
      rememberMe.value = true
    }
  } catch (error) {
    console.error('读取记住的账号失败:', error)
  }
}

const saveRememberedAccount = () => {
  if (rememberMe.value && form.identifier) {
    try {
      uni.setStorageSync('remembered_account', {
        identifier: form.identifier,
        timestamp: Date.now()
      })
    } catch (error) {
      console.error('保存记住的账号失败:', error)
    }
  } else {
    uni.removeStorageSync('remembered_account')
  }
}

const validateForm = () => {
  let isValid = true
  
  // 清空错误信息
  errors.identifier = ''
  errors.password = ''
  
  // 验证邮箱/学号
  if (!form.identifier.trim()) {
    errors.identifier = '请输入邮箱或学号'
    isValid = false
  }
  
  // 验证密码
  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 6) {
    errors.password = '密码长度不能少于6位'
    isValid = false
  }
  
  return isValid
}

const onSubmit = async () => {
  // 表单验证
  if (!validateForm()) {
    return
  }
  
  // 开始登录
  loading.value = true
  showLoading('登录中...')
  
  try {
    // 调用登录API
    const response = await authApi.login({
      identifier: form.identifier.trim(),
      password: form.password
    })
    
    // 登录成功处理
    await handleLoginSuccess(response)
    
  } catch (error) {
    console.error('登录失败:', error)
    
    // 显示错误提示（已在request.js中统一处理，这里可补充特定逻辑）
    if (!error.message.includes('业务错误')) {
      showError('登录失败，请检查网络连接')
    }
    
  } finally {
    loading.value = false
    hideLoading()
  }
}

const handleLoginSuccess = async (response) => {
  // 保存记住的账号
  saveRememberedAccount()
  
  // 更新用户状态
  await userStore.login(response)
  
  // 显示成功提示
  showSuccess('登录成功')
  
  // 跳转到首页或来源页
  setTimeout(() => {
    navigateAfterLogin()
  }, 1500)
}

const navigateAfterLogin = () => {
  const pages = getCurrentPages()
  
  // 如果有来源页且不是登录/注册页，则返回
  if (pages.length > 1) {
    const prevPage = pages[pages.length - 2]
    const blacklist = ['/pages/auth/login', '/pages/auth/register']
    
    if (!blacklist.includes(prevPage.route)) {
      uni.navigateBack()
      return
    }
  }
  
  // 否则跳转到首页
  uni.switchTab({
    url: '/pages/index/index'
  })
}

// 页面跳转方法
const goBack = () => {
  uni.navigateBack()
}

const toRegister = () => {
  uni.navigateTo({
    url: '/pages/auth/register'
  })
}

const toForgotPassword = () => {
  uni.showModal({
    title: '找回密码',
    content: '请通过注册邮箱找回密码，或联系管理员重置',
    showCancel: false,
    confirmText: '知道了'
  })
}

const toUserAgreement = () => {
  uni.navigateTo({
    url: '/pages/webview/index?url=' + encodeURIComponent('https://example.com/user-agreement')
  })
}

const toPrivacyPolicy = () => {
  uni.navigateTo({
    url: '/pages/webview/index?url=' + encodeURIComponent('https://example.com/privacy-policy')
  })
}

// 第三方登录
const onWechatLogin = () => {
  // #ifdef MP-WEIXIN
  uni.login({
    provider: 'weixin',
    success: async (res) => {
      console.log('微信登录成功:', res)
      // 这里可以调用后端接口进行微信登录验证
      uni.showToast({
        title: '微信登录功能开发中',
        icon: 'none'
      })
    },
    fail: (err) => {
      console.error('微信登录失败:', err)
      showError('微信登录失败')
    }
  })
  // #endif
  
  // #ifndef MP-WEIXIN
  uni.showToast({
    title: '请在微信小程序中使用',
    icon: 'none'
  })
  // #endif
}

const onQQLogin = () => {
  uni.showToast({
    title: 'QQ登录功能开发中',
    icon: 'none'
  })
}
</script>

<script>
export default {
  name: 'LoginPage',
  data() {
    return {}
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 64rpx;
  position: relative;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    
    &.circle-1 {
      width: 400rpx;
      height: 400rpx;
      top: -200rpx;
      right: -100rpx;
    }
    
    &.circle-2 {
      width: 300rpx;
      height: 300rpx;
      bottom: 100rpx;
      left: -150rpx;
    }
    
    &.circle-3 {
      width: 200rpx;
      height: 200rpx;
      bottom: -50rpx;
      right: 100rpx;
    }
  }
}

.login-header {
  position: relative;
  margin-top: 80rpx;
  margin-bottom: 80rpx;
  
  .back-btn {
    position: absolute;
    top: 0;
    left: 0;
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:active {
      background: rgba(255, 255, 255, 0.3);
    }
  }
  
  .login-title {
    display: block;
    font-size: 56rpx;
    font-weight: bold;
    color: white;
    margin-bottom: 16rpx;
    text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  }
  
  .login-subtitle {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
  }
}

.login-form {
  background: white;
  border-radius: 32rpx;
  padding: 64rpx 48rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  
  .form-group {
    margin-bottom: 48rpx;
    
    .form-label {
      display: block;
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      margin-bottom: 20rpx;
    }
    
    .error-text {
      display: block;
      font-size: 24rpx;
      color: #dd524d;
      margin-top: 12rpx;
      min-height: 36rpx;
    }
  }
  
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 64rpx;
    
    .remember-me {
      :deep(.u-checkbox__icon) {
        border-radius: 50%;
      }
    }
    
    .forgot-password {
      font-size: 26rpx;
      color: #666;
      
      &:active {
        color: #007aff;
      }
    }
  }
  
  .form-submit {
    margin-bottom: 64rpx;
  }
  
  .social-login {
    margin-bottom: 48rpx;
    
    .divider {
      display: flex;
      align-items: center;
      margin-bottom: 48rpx;
      
      .divider-line {
        flex: 1;
        height: 1rpx;
        background: #e5e5e5;
      }
      
      .divider-text {
        padding: 0 32rpx;
        font-size: 26rpx;
        color: #999;
      }
    }
    
    .social-buttons {
      display: flex;
      justify-content: center;
      gap: 80rpx;
      
      .social-btn {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 16rpx;
        background: none;
        border: none;
        padding: 0;
        
        &::after {
          border: none;
        }
        
        text {
          font-size: 24rpx;
          color: #666;
        }
        
        &.wechat:active text {
          color: #07c160;
        }
        
        &.qq:active text {
          color: #12b7f5;
        }
      }
    }
  }
  
  .register-link {
    text-align: center;
    font-size: 26rpx;
    color: #666;
    
    .link-text {
      color: #007aff;
      margin-left: 8rpx;
      font-weight: 500;
      
      &:active {
        color: #0056cc;
      }
    }
  }
}

.agreement-tip {
  position: absolute;
  bottom: 40rpx;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  padding: 0 64rpx;
  line-height: 1.5;
  
  .link-text {
    color: white;
    text-decoration: underline;
    
    &:active {
      opacity: 0.8;
    }
  }
}

// 适配全面屏
.safe-area-inset-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
}
</style>