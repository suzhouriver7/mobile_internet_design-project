<template>
  <view class="user-info-container">
    <view v-if="!isLogin" class="not-login">
      <text class="not-login-text">请先登录</text>
      <button class="login-btn" @click="toLogin">立即登录</button>
    </view>
    
    <view v-else class="user-content">
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <image
          v-if="userInfo && userInfo.avatarUrl"
          :src="userInfo.avatarUrl"
          class="avatar"
          mode="aspectFill"
          @click="changeAvatar"
        />
        <view v-else class="avatar-placeholder" @click="changeAvatar">
          <text class="avatar-text">{{ userInfo ? (userInfo.nickname?.[0] || '未') : '未' }}</text>
        </view>
        <text class="nickname">{{ userInfo ? userInfo.nickname : '未设置' }}</text>
        <text v-if="userInfo && userInfo.signature" class="signature">{{ userInfo.signature }}</text>
        <text v-else class="signature placeholder">点击编辑个性签名</text>
      </view>
      
      <!-- 功能列表 -->
      <view class="menu-list">
        <view class="menu-item" @click="toEdit">
          <text class="menu-icon">✏️</text>
          <text class="menu-text">编辑资料</text>
          <text class="menu-arrow">></text>
        </view>
        <view class="menu-item" @click="toChangePassword">
          <text class="menu-icon">🔒</text>
          <text class="menu-text">修改密码</text>
          <text class="menu-arrow">></text>
        </view>
        <view class="menu-item" @click="toMyOrders">
          <text class="menu-icon">📋</text>
          <text class="menu-text">我的活动</text>
          <text class="menu-arrow">></text>
        </view>
        <view class="menu-item" @click="toMyContents">
          <text class="menu-icon">📝</text>
          <text class="menu-text">我的动态</text>
          <text class="menu-arrow">></text>
        </view>
      </view>
      
      <!-- 退出登录 -->
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { userApi } from '@/api/index.js'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'

export default {
  data() {
    return {}
  },
  computed: {
    userInfo() {
      try {
        return this.$store.getters.userInfo || null
      } catch (e) {
        console.error('获取用户信息失败:', e)
        return null
      }
    },
    isLogin() {
      try {
        return this.$store.getters.isLogin || false
      } catch (e) {
        return false
      }
    }
  },
  onLoad() {
    console.log('用户页面加载')
  },
  onShow() {
    console.log('用户页面显示')
    if (this.isLogin) {
      this.$store.dispatch('user/refreshUserInfo').catch(() => {})
    }
  },
  methods: {
    changeAvatar() {
      if (!this.isLogin) return
      
      uni.chooseImage({
        count: 1,
        success: async (res) => {
          const tempFilePath = res.tempFilePaths[0]
          try {
            showLoading('上传中...')
            const userId = this.$store.getters.userId
            const url = await userApi.uploadAvatar(userId, tempFilePath)
            await this.$store.dispatch('user/updateUserInfo', { avatarUrl: url })
            showSuccess('上传成功')
            hideLoading()
          } catch (error) {
            hideLoading()
            showError(error.message || '上传失败')
          }
        }
      })
    },
    toEdit() {
      uni.navigateTo({
        url: '/pages/user/edit'
      })
    },
    toChangePassword() {
      uni.navigateTo({
        url: '/pages/user/change-password'
      })
    },
    toMyOrders() {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    },
    toMyContents() {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    },
    toLogin() {
      uni.navigateTo({
        url: '/pages/auth/login'
      })
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            this.$store.dispatch('user/logout')
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.user-info-container {
  min-height: 100vh;
  background: #f8f8f8;
}

.not-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 40rpx;
}

.not-login-text {
  font-size: 32rpx;
  color: #999999;
}

.login-btn {
  width: 300rpx;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
}

.user-content {
  padding: 40rpx 30rpx;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(255, 255, 255, 0.3);
  margin-bottom: 30rpx;
}

.avatar-placeholder {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.3);
}

.avatar-text {
  color: #ffffff;
  font-size: 64rpx;
  font-weight: bold;
}

.nickname {
  font-size: 40rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.signature {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

.signature.placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.menu-list {
  background: #ffffff;
  border-radius: 24rpx;
  margin-bottom: 40rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 24rpx;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
}

.menu-arrow {
  font-size: 28rpx;
  color: #cccccc;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background: #ff3b30;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
}
</style>
