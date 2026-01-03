<template>
  <view class="container">
    <view class="header">
      <text class="title">校园约伴</text>
      <view class="avatar-box" @click="toUser">
        <image v-if="userInfo && userInfo.avatarUrl" :src="userInfo.avatarUrl" class="avatar" mode="aspectFill" />
        <view v-else class="avatar-placeholder">
          <text>{{ userInfo ? (userInfo.nickname?.[0] || '未') : '未' }}</text>
        </view>
      </view>
    </view>

    <view class="search-box" @click="toSearch">
      <text class="search-text">搜索活动、动态或用户</text>
    </view>

    <view class="menu-grid">
      <view class="menu-item" @click="toOrderList">
        <text class="menu-icon">🎯</text>
        <text class="menu-text">活动广场</text>
      </view>
      <view class="menu-item" @click="toContentList">
        <text class="menu-icon">📝</text>
        <text class="menu-text">动态社区</text>
      </view>
      <view class="menu-item" @click="toCreateOrder">
        <text class="menu-icon">➕</text>
        <text class="menu-text">发布活动</text>
      </view>
      <view class="menu-item" @click="toAIChat">
        <text class="menu-icon">🤖</text>
        <text class="menu-text">AI助手</text>
      </view>
    </view>

    <view class="section">
      <view class="section-title">
        <text>推荐活动</text>
        <text class="more" @click="toOrderList">更多 ></text>
      </view>
      <view v-if="orders.length > 0" class="order-list">
        <view v-for="order in orders" :key="order.id" class="order-item" @click="toOrderDetail(order.id)">
          <view class="order-header">
            <text class="order-type">{{ getActivityType(order.activityType) }}</text>
            <text class="order-status">{{ getStatus(order.status) }}</text>
          </view>
          <text class="order-location">📍 {{ order.location }}</text>
          <text class="order-time">⏰ {{ formatTime(order.startTime) }}</text>
          <view class="order-footer">
            <text>{{ order.currentPeople }}/{{ order.maxPeople }}人</text>
            <text>{{ getCampus(order.campus) }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无推荐活动</view>
    </view>

    <view class="section">
      <view class="section-title">
        <text>热门动态</text>
        <text class="more" @click="toContentList">更多 ></text>
      </view>
      <view v-if="contents.length > 0" class="content-list">
        <view v-for="content in contents" :key="content.pid" class="content-item" @click="toContentDetail(content.pid)">
          <view class="content-header">
            <image v-if="content.user && content.user.avatarUrl" :src="content.user.avatarUrl" class="user-avatar" mode="aspectFill" />
            <view class="user-info">
              <text class="user-name">{{ content.user ? content.user.nickname : '匿名' }}</text>
              <text class="content-time">{{ formatRelativeTime(content.createdAt) }}</text>
            </view>
          </view>
          <text class="content-text">{{ content.content }}</text>
          <view class="content-footer">
            <text>👍 {{ content.likeCount || 0 }}</text>
            <text>💬 {{ content.commentCount || 0 }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无热门动态</view>
    </view>
  </view>
</template>

<script>
import { orderApi, contentApi } from '@/api/index.js'
import { formatTime, formatRelativeTime } from '@/utils/util.js'
import { ACTIVITY_TYPE_MAP, ORDER_STATUS_MAP, CAMPUS_MAP } from '@/utils/constants.js'

export default {
  data() {
    return {
      orders: [],
      contents: []
    }
  },
  computed: {
    userInfo() {
      return this.$store.getters['user/userInfo']
    }
  },
  onLoad() {
    this.loadData()
    this.$store.dispatch('user/initUserState')
  },
  onShow() {
    this.loadData()
  },
  onPullDownRefresh() {
    this.loadData().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    async loadData() {
      try {
        const [ordersRes, contentsRes] = await Promise.all([
          orderApi.getOrders({ page: 1, size: 5 }).catch(() => ({ list: [] })),
          contentApi.getContents({ page: 1, size: 5 }).catch(() => ({ list: [] }))
        ])
        this.orders = ordersRes?.list || []
        this.contents = contentsRes?.list || []
      } catch (error) {
        console.error('加载数据失败:', error)
        this.orders = []
        this.contents = []
      }
    },
    getActivityType(type) {
      return ACTIVITY_TYPE_MAP[type] || '其他'
    },
    getStatus(status) {
      return ORDER_STATUS_MAP[status] || '未知'
    },
    getCampus(campus) {
      return CAMPUS_MAP[campus] || '其他'
    },
    formatTime,
    formatRelativeTime,
    toUser() {
      if (!this.$store.getters['user/isLogin']) {
        uni.navigateTo({ url: '/pages/auth/login' })
      } else {
        uni.switchTab({ url: '/pages/user/info' })
      }
    },
    toSearch() {
      uni.showToast({ title: '搜索功能开发中', icon: 'none' })
    },
    toOrderList() {
      uni.switchTab({ url: '/pages/order/list' })
    },
    toContentList() {
      uni.switchTab({ url: '/pages/content/list' })
    },
    toCreateOrder() {
      if (!this.$store.getters['user/isLogin']) {
        uni.navigateTo({ url: '/pages/auth/login' })
        return
      }
      uni.navigateTo({ url: '/pages/order/create' })
    },
    toAIChat() {
      uni.navigateTo({ url: '/pages/ai/chat' })
    },
    toOrderDetail(orderId) {
      uni.navigateTo({ url: `/pages/order/detail?id=${orderId}` })
    },
    toContentDetail(contentId) {
      uni.navigateTo({ url: `/pages/content/detail?id=${contentId}` })
    }
  }
}
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx 20rpx;
  background-color: #fff;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
}

.avatar-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background-color: #007AFF;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
}

.search-box {
  margin: 20rpx 30rpx;
  padding: 20rpx;
  background-color: #fff;
  border-radius: 10rpx;
}

.search-text {
  color: #999;
  font-size: 28rpx;
}

.menu-grid {
  display: flex;
  justify-content: space-around;
  padding: 30rpx;
  background-color: #fff;
  margin: 20rpx 30rpx;
  border-radius: 10rpx;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.menu-icon {
  font-size: 60rpx;
  margin-bottom: 10rpx;
}

.menu-text {
  font-size: 24rpx;
  color: #333;
}

.section {
  margin: 20rpx 30rpx;
  background-color: #fff;
  border-radius: 10rpx;
  padding: 30rpx;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.more {
  font-size: 24rpx;
  color: #007AFF;
  font-weight: normal;
}

.order-list, .content-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-item, .content-item {
  padding: 20rpx;
  background-color: #f9f9f9;
  border-radius: 10rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.order-type {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.order-status {
  font-size: 24rpx;
  color: #007AFF;
}

.order-location, .order-time {
  font-size: 24rpx;
  color: #666;
  margin: 5rpx 0;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #999;
}

.content-header {
  display: flex;
  align-items: center;
  margin-bottom: 15rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 15rpx;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.content-time {
  font-size: 22rpx;
  color: #999;
  margin-top: 5rpx;
}

.content-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.content-footer {
  display: flex;
  gap: 30rpx;
  font-size: 24rpx;
  color: #999;
}

.empty {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
