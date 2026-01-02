<template>
  <view class="page">
    <scroll-view class="scroll-view" scroll-y @scrolltolower="loadMore" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <view v-if="contentList.length > 0" class="list-box">
        <view v-for="content in contentList" :key="content.pid || content.id" class="item-box" @click="goDetail(content.pid || content.id)">
          <view class="item-header">
            <text class="user-name">{{ content.user ? content.user.nickname : '匿名' }}</text>
            <text class="item-time">{{ content.createdAt || '刚刚' }}</text>
          </view>
          <text class="item-text">{{ content.content || '暂无内容' }}</text>
          <view class="item-footer">
            <text class="item-action">👍 {{ content.likeCount || 0 }}</text>
            <text class="item-action">💬 {{ content.commentCount || 0 }}</text>
          </view>
        </view>
      </view>
      <view v-else-if="!loading" class="empty-box">
        <text class="empty-text">暂无动态</text>
      </view>
      <view v-if="loading" class="loading-box">
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>
    
    <view class="fab" @click="goCreate">
      <text class="fab-text">+</text>
    </view>
  </view>
</template>

<script>
import { contentApi } from '@/api/index.js'

export default {
  data() {
    return {
      contentList: [],
      loading: false,
      refreshing: false,
      page: 1,
      size: 10,
      hasMore: true
    }
  },
  onLoad() {
    console.log('动态列表onLoad')
    this.loadContents()
  },
  onShow() {
    console.log('动态列表onShow')
  },
  onPullDownRefresh() {
    this.onRefresh()
  },
  methods: {
    async loadContents() {
      if (this.loading) return
      if (!this.hasMore) return
      
      this.loading = true
      try {
        const result = await contentApi.getContents({ page: this.page, size: this.size })
        if (result && result.list) {
          if (this.page === 1) {
            this.contentList = result.list
          } else {
            this.contentList = [...this.contentList, ...result.list]
          }
          this.hasMore = result.list.length >= this.size
          this.page++
        }
      } catch (e) {
        console.error('加载动态失败:', e)
      } finally {
        this.loading = false
        this.refreshing = false
        uni.stopPullDownRefresh()
      }
    },
    onRefresh() {
      this.page = 1
      this.hasMore = true
      this.refreshing = true
      this.loadContents()
    },
    loadMore() {
      this.loadContents()
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/content/detail?id=${id}` })
    },
    goCreate() {
      uni.navigateTo({ url: '/pages/content/create' })
    }
  }
}
</script>

<style>
.page {
  width: 100%;
  height: 100%;
  background: #f8f8f8;
  display: flex;
  flex-direction: column;
}

.scroll-view {
  width: 100%;
  flex: 1;
  height: 0;
}

.list-box {
  padding: 20rpx 30rpx;
}

.item-box {
  background: #ffffff;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.item-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.item-time {
  font-size: 24rpx;
  color: #999999;
}

.item-text {
  display: block;
  font-size: 28rpx;
  color: #666666;
  line-height: 1.6;
  margin-bottom: 20rpx;
}

.item-footer {
  display: flex;
  flex-direction: row;
  gap: 30rpx;
}

.item-action {
  font-size: 24rpx;
  color: #999999;
}

.empty-box {
  text-align: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
}

.loading-box {
  text-align: center;
  padding: 50rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999999;
}

.fab {
  position: fixed;
  right: 30rpx;
  bottom: 180rpx;
  width: 100rpx;
  height: 100rpx;
  background: #007AFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.3);
}

.fab-text {
  font-size: 60rpx;
  color: #ffffff;
  font-weight: bold;
}
</style>
