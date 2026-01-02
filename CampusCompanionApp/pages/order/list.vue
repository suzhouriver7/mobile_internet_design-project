<template>
  <view class="page">
    <view class="filter-box">
      <picker mode="selector" :range="typeOptions" range-key="label" @change="onTypeChange">
        <view class="filter-item">
          <text>{{ selectedType ? selectedType.label : '活动类型' }}</text>
        </view>
      </picker>
      <picker mode="selector" :range="campusOptions" range-key="label" @change="onCampusChange">
        <view class="filter-item">
          <text>{{ selectedCampus ? selectedCampus.label : '校区' }}</text>
        </view>
      </picker>
    </view>
    
    <scroll-view class="scroll-view" scroll-y @scrolltolower="loadMore" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <view v-if="orderList.length > 0" class="list-box">
        <view v-for="order in orderList" :key="order.oid || order.id" class="item-box" @click="goDetail(order.oid || order.id)">
          <view class="item-header">
            <text class="item-title">{{ order.activityType || '活动' }}</text>
            <text class="item-status">{{ order.status || '进行中' }}</text>
          </view>
          <text class="item-location">📍 {{ order.location || '未设置' }}</text>
          <text class="item-time">⏰ {{ order.startTime || '未设置' }}</text>
          <view class="item-footer">
            <text class="item-people">{{ order.currentPeople || 0 }}/{{ order.maxPeople || 0 }}人</text>
            <text class="item-campus">{{ order.campus || '未设置' }}</text>
          </view>
        </view>
      </view>
      <view v-else-if="!loading" class="empty-box">
        <text class="empty-text">暂无活动</text>
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
import { orderApi } from '@/api/index.js'

export default {
  data() {
    return {
      orderList: [],
      loading: false,
      refreshing: false,
      page: 1,
      size: 10,
      hasMore: true,
      selectedType: null,
      selectedCampus: null,
      typeOptions: [
        { value: null, label: '全部' },
        { value: 1, label: '篮球' },
        { value: 2, label: '羽毛球' },
        { value: 3, label: '聚餐' },
        { value: 4, label: '学习' }
      ],
      campusOptions: [
        { value: null, label: '全部' },
        { value: 1, label: '主校区' },
        { value: 2, label: '分校区' }
      ]
    }
  },
  onLoad() {
    console.log('活动列表onLoad')
    this.loadOrders()
  },
  onShow() {
    console.log('活动列表onShow')
  },
  onPullDownRefresh() {
    this.onRefresh()
  },
  methods: {
    async loadOrders() {
      if (this.loading) return
      if (!this.hasMore) return
      
      this.loading = true
      try {
        const result = await orderApi.getOrders({ page: this.page, size: this.size })
        if (result && result.list) {
          if (this.page === 1) {
            this.orderList = result.list
          } else {
            this.orderList = [...this.orderList, ...result.list]
          }
          this.hasMore = result.list.length >= this.size
          this.page++
        }
      } catch (e) {
        console.error('加载活动失败:', e)
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
      this.loadOrders()
    },
    loadMore() {
      this.loadOrders()
    },
    onTypeChange(e) {
      this.selectedType = this.typeOptions[e.detail.value]
      this.page = 1
      this.hasMore = true
      this.loadOrders()
    },
    onCampusChange(e) {
      this.selectedCampus = this.campusOptions[e.detail.value]
      this.page = 1
      this.hasMore = true
      this.loadOrders()
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
    },
    goCreate() {
      uni.navigateTo({ url: '/pages/order/create' })
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

.filter-box {
  width: 100%;
  padding: 20rpx 30rpx;
  background: #ffffff;
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  flex-shrink: 0;
}

.filter-item {
  flex: 1;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  text-align: center;
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

.item-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.item-status {
  font-size: 24rpx;
  color: #666666;
}

.item-location {
  display: block;
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 10rpx;
}

.item-time {
  display: block;
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 20rpx;
}

.item-footer {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.item-people {
  font-size: 24rpx;
  color: #999999;
}

.item-campus {
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
