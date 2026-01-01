<template>
  <view class="user-info-page">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-header" @click="toEdit">
        <u-avatar 
          :src="avatarUrl || '/static/images/default-avatar.png'" 
          size="120"
        ></u-avatar>
        <view class="user-info">
          <text class="user-name">{{ nickname }}</text>
          <text class="user-email" v-if="userInfo?.email">{{ userInfo.email }}</text>
          <text class="user-student-id" v-if="userInfo?.studentId">学号：{{ userInfo.studentId }}</text>
        </view>
        <u-icon name="arrow-right" size="20" color="#999"></u-icon>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="toMyOrders">
        <u-icon name="calendar" size="20" color="#007aff"></u-icon>
        <text>我的活动</text>
        <u-icon name="arrow-right" size="16" color="#999"></u-icon>
      </view>
      
      <view class="menu-item" @click="toMyContents">
        <u-icon name="photo" size="20" color="#4cd964"></u-icon>
        <text>我的动态</text>
        <u-icon name="arrow-right" size="16" color="#999"></u-icon>
      </view>
      
      <view class="menu-item" @click="toEdit">
        <u-icon name="setting" size="20" color="#f0ad4e"></u-icon>
        <text>编辑资料</text>
        <u-icon name="arrow-right" size="16" color="#999"></u-icon>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section" v-if="isLogin">
      <u-button
        type="error"
        shape="circle"
        @click="handleLogout"
        :custom-style="{
          height: '88rpx',
          fontSize: '32rpx'
        }"
      >
        退出登录
      </u-button>
    </view>

    <!-- 未登录提示 -->
    <view class="login-prompt" v-else>
      <u-button
        type="primary"
        shape="circle"
        @click="toLogin"
        :custom-style="{
          height: '88rpx',
          fontSize: '32rpx'
        }"
      >
        立即登录
      </u-button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { userApi } from '@/api'
import { resolveResourceUrl } from '@/utils/util'

// 使用Vuex store
const store = useStore()
const userInfo = computed(() => store.getters.userInfo)
const isLogin = computed(() => store.getters.isLogin)
const nickname = computed(() => store.getters.nickname)
const avatarUrl = computed(() => {
  const url = store.getters.avatarUrl
  return url ? resolveResourceUrl(url) : ''
})

// 生命周期
onShow(() => {
  // 如果已登录，刷新用户信息
  if (isLogin.value && store.getters.userId) {
    loadUserInfo()
  }
})

// 方法
const loadUserInfo = async () => {
  try {
    const userId = store.getters.userId
    if (userId) {
      const info = await userApi.getUserInfo(userId)
      store.dispatch('user/updateUserInfo', info)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const toEdit = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  uni.navigateTo({
    url: '/pages/user/edit'
  })
}

const toMyOrders = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const toMyContents = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        await store.dispatch('logout')
      }
    }
  })
}

const toLogin = () => {
  uni.navigateTo({
    url: '/pages/auth/login'
  })
}
</script>

<style lang="scss" scoped>
.user-info-page {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 40rpx;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 32rpx 40rpx;
  margin-bottom: 24rpx;
  
  .user-header {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    .user-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;
      
      .user-name {
        font-size: 36rpx;
        font-weight: bold;
        color: white;
      }
      
      .user-email,
      .user-student-id {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.9);
      }
    }
  }
}

.menu-list {
  background: white;
  margin: 0 32rpx 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  
  .menu-item {
    display: flex;
    align-items: center;
    gap: 24rpx;
    padding: 32rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:active {
      background-color: #f8f9fa;
    }
    
    text {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }
  }
}

.logout-section,
.login-prompt {
  padding: 0 32rpx;
  margin-top: 40rpx;
}
</style>
