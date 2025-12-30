<template>
  <view class="register-container">
    <!-- 返回按钮 -->
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="24" color="#333"></u-icon>
      </view>
      <text class="nav-title">注册账号</text>
    </view>

    <!-- 注册表单 -->
    <scroll-view class="form-scroll" scroll-y>
      <view class="register-form">
        <!-- 邮箱 -->
        <view class="form-group">
          <text class="form-label">邮箱<text class="required">*</text></text>
          <u-input
            v-model="form.email"
            placeholder="请输入邮箱"
            border="bottom"
            clearable
            :custom-style="inputStyle"
          >
            <template #prefix>
              <u-icon name="email" size="20" color="#999"></u-icon>
            </template>
          </u-input>
          <text class="error-text" v-if="errors.email">{{ errors.email }}</text>
        </view>

        <!-- 密码 -->
        <view class="form-group">
          <text class="form-label">密码<text class="required">*</text></text>
          <u-input
            v-model="form.password"
            placeholder="6-20位字符"
            border="bottom"
            clearable
            password
            :custom-style="inputStyle"
          >
            <template #prefix>
              <u-icon name="lock" size="20" color="#999"></u-icon>
            </template>
          </u-input>
          <text class="error-text" v-if="errors.password">{{ errors.password }}</text>
          <text class="hint-text" v-if="form.password && !errors.password">
            密码强度：<text :class="passwordStrengthClass">{{ passwordStrengthText }}</text>
          </text>
        </view>

        <!-- 确认密码 -->
        <view class="form-group">
          <text class="form-label">确认密码<text class="required">*</text></text>
          <u-input
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            border="bottom"
            clearable
            password
            :custom-style="inputStyle"
          >
            <template #prefix>
              <u-icon name="lock-fill" size="20" color="#999"></u-icon>
            </template>
          </u-input>
          <text class="error-text" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</text>
        </view>

        <!-- 昵称 -->
        <view class="form-group">
          <text class="form-label">昵称<text class="required">*</text></text>
          <u-input
            v-model="form.nickname"
            placeholder="2-10个字符"
            border="bottom"
            clearable
            :custom-style="inputStyle"
          >
            <template #prefix>
              <u-icon name="account" size="20" color="#999"></u-icon>
            </template>
          </u-input>
          <text class="error-text" v-if="errors.nickname">{{ errors.nickname }}</text>
        </view>

        <!-- 学号 -->
        <view class="form-group">
          <text class="form-label">学号</text>
          <u-input
            v-model="form.studentId"
            placeholder="请输入学号（选填）"
            border="bottom"
            clearable
            :custom-style="inputStyle"
          >
            <template #prefix>
              <u-icon name="list" size="20" color="#999"></u-icon>
            </template>
          </u-input>
        </view>

        <!-- 验证码 -->
        <view class="form-group" v-if="showVerificationCode">
          <text class="form-label">验证码<text class="required">*</text></text>
          <view class="verification-input">
            <u-input
              v-model="form.verificationCode"
              placeholder="请输入验证码"
              border="bottom"
              clearable
              :custom-style="inputStyle"
            >
              <template #prefix>
                <u-icon name="shield" size="20" color="#999"></u-icon>
              </template>
            </u-input>
            <u-button
              type="primary"
              size="mini"
              :disabled="countdown > 0"
              @click="sendVerificationCode"
              :custom-style="{
                width: '180rpx',
                marginLeft: '20rpx',
                height: '64rpx'
              }"
            >
              {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
            </u-button>
          </view>
          <text class="error-text" v-if="errors.verificationCode">{{ errors.verificationCode }}</text>
          <text class="hint-text" v-if="countdown === 0 && !form.verificationCode">
            验证码将发送到您的邮箱
          </text>
        </view>

        <!-- 注册协议 -->
        <view class="agreement-group">
          <view class="agreement-checkbox" @click="agreed = !agreed">
            <u-checkbox
              v-model="agreed"
              shape="circle"
              label-size="14"
              :label-color="agreed ? '#007aff' : '#666'"
            ></u-checkbox>
            <text class="agreement-text">
              我已阅读并同意
              <text class="link-text" @click.stop="toUserAgreement">《用户协议》</text>
              和
              <text class="link-text" @click.stop="toPrivacyPolicy">《隐私政策》</text>
            </text>
          </view>
          <text class="error-text" v-if="errors.agreement">{{ errors.agreement }}</text>
        </view>

        <!-- 注册按钮 -->
        <view class="form-submit">
          <u-button
            type="primary"
            shape="circle"
            :loading="loading"
            :disabled="!canSubmit"
            @click="onSubmit"
            :custom-style="{
              height: '88rpx',
              fontSize: '32rpx',
              fontWeight: '500'
            }"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </u-button>
        </view>

        <!-- 登录链接 -->
        <view class="login-link">
          <text>已有账号？</text>
          <text class="link-text" @click="toLogin">立即登录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { authApi } from '@/api'
import { showLoading, hideLoading, showSuccess, showError, isValidEmail, isValidPassword } from '@/utils/util'

// 响应式数据
const form = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  studentId: '',
  verificationCode: ''
})

const errors = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  verificationCode: '',
  agreement: ''
})

const agreed = ref(false)
const loading = ref(false)
const countdown = ref(0)
const showVerificationCode = ref(true) // 可以根据需要开启或关闭

// 计算属性
const inputStyle = computed(() => ({
  fontSize: '30rpx',
  padding: '20rpx 0'
}))

const passwordStrength = computed(() => {
  if (!form.password) return 0
  let strength = 0
  if (form.password.length >= 8) strength++
  if (/[A-Z]/.test(form.password)) strength++
  if (/[a-z]/.test(form.password)) strength++
  if (/[0-9]/.test(form.password)) strength++
  if (/[^A-Za-z0-9]/.test(form.password)) strength++
  return strength
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength === 0) return ''
  if (strength <= 2) return '弱'
  if (strength <= 4) return '中'
  return '强'
})

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 2) return 'strength-weak'
  if (strength <= 4) return 'strength-medium'
  return 'strength-strong'
})

const canSubmit = computed(() => {
  return form.email && form.password && form.confirmPassword && form.nickname && agreed.value
})

// 监听器
watch(() => form.email, (newVal) => {
  if (newVal && !isValidEmail(newVal)) {
    errors.email = '邮箱格式不正确'
  } else {
    errors.email = ''
  }
})

watch(() => form.password, (newVal) => {
  if (newVal && !isValidPassword(newVal)) {
    errors.password = '密码长度需6-20位'
  } else {
    errors.password = ''
  }
  
  // 检查确认密码
  if (form.confirmPassword && newVal !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
  } else {
    errors.confirmPassword = ''
  }
})

watch(() => form.confirmPassword, (newVal) => {
  if (newVal && form.password !== newVal) {
    errors.confirmPassword = '两次输入的密码不一致'
  } else {
    errors.confirmPassword = ''
  }
})

watch(() => form.nickname, (newVal) => {
  if (newVal && (newVal.length < 2 || newVal.length > 10)) {
    errors.nickname = '昵称长度需2-10个字符'
  } else {
    errors.nickname = ''
  }
})

// 方法
const validateForm = () => {
  let isValid = true
  
  // 邮箱验证
  if (!form.email.trim()) {
    errors.email = '请输入邮箱'
    isValid = false
  } else if (!isValidEmail(form.email)) {
    errors.email = '邮箱格式不正确'
    isValid = false
  }
  
  // 密码验证
  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (!isValidPassword(form.password)) {
    errors.password = '密码长度需6-20位'
    isValid = false
  }
  
  // 确认密码验证
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }
  
  // 昵称验证
  if (!form.nickname.trim()) {
    errors.nickname = '请输入昵称'
    isValid = false
  } else if (form.nickname.length < 2 || form.nickname.length > 10) {
    errors.nickname = '昵称长度需2-10个字符'
    isValid = false
  }
  
  // 验证码验证（如果开启）
  if (showVerificationCode.value && !form.verificationCode.trim()) {
    errors.verificationCode = '请输入验证码'
    isValid = false
  }
  
  // 协议验证
  if (!agreed.value) {
    errors.agreement = '请同意用户协议和隐私政策'
    isValid = false
  }
  
  return isValid
}

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const sendVerificationCode = async () => {
  if (!form.email) {
    showError('请先输入邮箱')
    return
  }
  
  if (!isValidEmail(form.email)) {
    showError('邮箱格式不正确')
    return
  }
  
  // 这里应该调用发送验证码的API
  // 由于后端可能没有验证码接口，这里模拟发送
  try {
    showLoading('发送中...')
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    showSuccess('验证码已发送到您的邮箱')
    startCountdown()
  } catch (error) {
    showError('发送验证码失败')
  } finally {
    hideLoading()
  }
}

const onSubmit = async () => {
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  showLoading('注册中...')
  
  try {
    // 准备注册数据
    const registerData = {
      email: form.email.trim(),
      password: form.password,
      nickname: form.nickname.trim()
    }
    
    // 如果有学号，添加学号
    if (form.studentId.trim()) {
      registerData.studentId = form.studentId.trim()
    }
    
    // 调用注册API
    const userId = await authApi.register(registerData)
    
    // 注册成功处理
    await handleRegisterSuccess(userId)
    
  } catch (error) {
    console.error('注册失败:', error)
    
    // 处理特定的业务错误
    if (error.message.includes('用户已存在')) {
      errors.email = '该邮箱已被注册'
    } else if (!error.message.includes('业务错误')) {
      showError('注册失败，请稍后重试')
    }
    
  } finally {
    loading.value = false
    hideLoading()
  }
}

const handleRegisterSuccess = async (userId) => {
  // 显示成功提示
  showSuccess('注册成功')
  
  // 自动登录
  try {
    // 使用刚注册的账号自动登录
    const loginResponse = await authApi.login({
      identifier: form.email,
      password: form.password
    })
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }, 1500)
    
  } catch (loginError) {
    console.error('自动登录失败:', loginError)
    // 注册成功但自动登录失败，跳转到登录页
    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/auth/login'
      })
    }, 1500)
  }
}

// 页面跳转方法
const goBack = () => {
  uni.navigateBack()
}

const toLogin = () => {
  uni.navigateTo({
    url: '/pages/auth/login'
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
</script>

<script>
export default {
  name: 'RegisterPage',
  data() {
    return {}
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.nav-header {
  display: flex;
  align-items: center;
  padding: 32rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
  
  .back-btn {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:active {
      background-color: #f5f5f5;
    }
  }
  
  .nav-title {
    flex: 1;
    text-align: center;
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
    margin-right: 64rpx; // 平衡左侧按钮
  }
}

.form-scroll {
  height: calc(100vh - 130rpx);
}

.register-form {
  padding: 32rpx;
  max-width: 600rpx;
  margin: 0 auto;
  
  .form-group {
    margin-bottom: 40rpx;
    
    .form-label {
      display: block;
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      margin-bottom: 20rpx;
      
      .required {
        color: #dd524d;
        margin-left: 4rpx;
      }
    }
    
    .error-text {
      display: block;
      font-size: 24rpx;
      color: #dd524d;
      margin-top: 12rpx;
      min-height: 36rpx;
    }
    
    .hint-text {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 12rpx;
      
      .strength-weak {
        color: #dd524d;
      }
      
      .strength-medium {
        color: #f0ad4e;
      }
      
      .strength-strong {
        color: #4cd964;
      }
    }
    
    .verification-input {
      display: flex;
      align-items: center;
    }
  }
  
  .agreement-group {
    margin: 48rpx 0 64rpx;
    
    .agreement-checkbox {
      display: flex;
      align-items: flex-start;
      
      :deep(.u-checkbox__icon) {
        margin-top: 4rpx;
      }
      
      .agreement-text {
        flex: 1;
        margin-left: 16rpx;
        font-size: 26rpx;
        color: #666;
        line-height: 1.4;
        
        .link-text {
          color: #007aff;
          
          &:active {
            color: #0056cc;
          }
        }
      }
    }
    
    .error-text {
      display: block;
      font-size: 24rpx;
      color: #dd524d;
      margin-top: 12rpx;
    }
  }
  
  .form-submit {
    margin-bottom: 48rpx;
  }
  
  .login-link {
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
</style>