<template>
  <view class="home-container">
    <!-- 顶部导航栏 -->
    <view class="home-header">
      <text class="app-title">校园约伴</text>
      <view class="header-right" @click="toUserProfile">
        <view class="avatar-placeholder">
          <text class="avatar-text">未</text>
        </view>
      </view>
    </view>
    
    <!-- 搜索栏 -->
    <view class="search-bar">
      <text class="search-placeholder">搜索活动、动态或用户</text>
    </view>
    
    <!-- 功能入口 -->
    <view class="function-grid">
      <view class="function-item" @click="toOrderList">
        <text class="function-icon">🎯</text>
        <text class="function-text">活动广场</text>
      </view>
      <view class="function-item" @click="toContentList">
        <text class="function-icon">📝</text>
        <text class="function-text">动态社区</text>
      </view>
      <view class="function-item" @click="toCreateOrder">
        <text class="function-icon">➕</text>
        <text class="function-text">发布活动</text>
      </view>
      <view class="function-item" @click="toAIChat">
        <text class="function-icon">🤖</text>
        <text class="function-text">AI助手</text>
      </view>
    </view>
    
    <!-- 推荐活动 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">推荐活动</text>
        <text class="section-more" @click="toOrderList">更多 ></text>
      </view>
      <view class="empty-state">
        <text class="empty-text">暂无推荐活动</text>
      </view>
    </view>
    
    <!-- 热门动态 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门动态</text>
        <text class="section-more" @click="toContentList">更多 ></text>
      </view>
      <view class="empty-state">
        <text class="empty-text">暂无热门动态</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  onLoad() {
    console.log('首页加载')
  },
  onShow() {
    console.log('首页显示')
  },
  methods: {
    toOrderList() {
      uni.switchTab({
        url: '/pages/order/list'
      })
    },
    toContentList() {
      uni.switchTab({
        url: '/pages/content/list'
      })
    },
    toCreateOrder() {
      uni.navigateTo({
        url: '/pages/order/create'
      })
    },
    toUserProfile() {
      uni.switchTab({
        url: '/pages/user/info'
      })
    },
    toAIChat() {
      uni.navigateTo({
        url: '/pages/ai/chat'
      })
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 40rpx;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx 20rpx;
  background: #ffffff;
}

.app-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
}

.header-right {
  width: 80rpx;
  height: 80rpx;
}

.avatar-placeholder {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #007AFF;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: #ffffff;
  font-size: 32rpx;
}

.search-bar {
  margin: 20rpx 30rpx;
  padding: 24rpx 30rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.search-placeholder {
  color: #999999;
  font-size: 28rpx;
}

.function-grid {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 30rpx;
  background: #ffffff;
  margin: 20rpx 30rpx;
  border-radius: 24rpx;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.function-icon {
  font-size: 60rpx;
}

.function-text {
  font-size: 24rpx;
  color: #666666;
}

.section {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.section-more {
  font-size: 26rpx;
  color: #007AFF;
}

.empty-state {
  text-align: center;
  padding: 60rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
}
</style>

