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
            <text class="item-title">{{ getActivityTypeText(order.activityType) }}</text>
            <text class="item-status">{{ getStatusText(order.status) }}</text>
          </view>
          <text class="item-location">📍 {{ order.location || '未设置' }}</text>
          <text class="item-time">⏰ {{ formatTime(order.startTime) }}</text>
          <view class="item-footer">
            <text class="item-people">{{ order.currentPeople || 0 }}/{{ order.maxPeople || 0 }}人</text>
            <text class="item-campus">{{ getCampusText(order.campus) }}</text>
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
import { ACTIVITY_TYPE, ACTIVITY_TYPE_MAP, CAMPUS, CAMPUS_MAP, ORDER_STATUS_MAP } from '@/utils/constants.js'

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
        { value: ACTIVITY_TYPE.BASKETBALL, label: ACTIVITY_TYPE_MAP.BASKETBALL },
        { value: ACTIVITY_TYPE.BADMINTON, label: ACTIVITY_TYPE_MAP.BADMINTON },
        { value: ACTIVITY_TYPE.MEAL, label: ACTIVITY_TYPE_MAP.MEAL },
        { value: ACTIVITY_TYPE.STUDY, label: ACTIVITY_TYPE_MAP.STUDY },
        { value: ACTIVITY_TYPE.MOVIE, label: ACTIVITY_TYPE_MAP.MOVIE },
        { value: ACTIVITY_TYPE.RUNNING, label: ACTIVITY_TYPE_MAP.RUNNING },
        { value: ACTIVITY_TYPE.GAME, label: ACTIVITY_TYPE_MAP.GAME },
        { value: ACTIVITY_TYPE.OTHER, label: ACTIVITY_TYPE_MAP.OTHER }
      ],
      campusOptions: [
        { value: null, label: '全部' },
        { value: CAMPUS.LIANGXIANG, label: CAMPUS_MAP.LIANGXIANG },
        { value: CAMPUS.ZHONGGUANCUN, label: CAMPUS_MAP.ZHONGGUANCUN },
        { value: CAMPUS.ZHUHAI, label: CAMPUS_MAP.ZHUHAI },
        { value: CAMPUS.XISHAN, label: CAMPUS_MAP.XISHAN },
        { value: CAMPUS.OTHER_CAMPUS, label: CAMPUS_MAP.OTHER_CAMPUS }
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
      if (!this.hasMore && this.page > 1) return
      
      this.loading = true
      try {
        // 构建请求参数，包含筛选条件
        const params = {
          page: this.page,
          size: this.size
        }
        
        // 添加活动类型筛选
        if (this.selectedType && this.selectedType.value) {
          params.activityType = this.selectedType.value
        }
        
        // 添加校区筛选
        if (this.selectedCampus && this.selectedCampus.value) {
          params.campus = this.selectedCampus.value
        }
        
        const result = await orderApi.getOrders(params)
        if (result && result.list) {
          if (this.page === 1) {
            this.orderList = result.list
          } else {
            this.orderList = [...this.orderList, ...result.list]
          }
          this.hasMore = result.list.length >= this.size
          this.page++
        } else {
          this.hasMore = false
        }
      } catch (e) {
        console.error('加载活动失败:', e)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
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
    },
    getActivityTypeText(type) {
      return ACTIVITY_TYPE_MAP[type] || '其他'
    },
    getCampusText(campus) {
      return CAMPUS_MAP[campus] || '其他'
    },
    getStatusText(status) {
      return ORDER_STATUS_MAP[status] || '未知'
    },
    formatTime(timeStr) {
      if (!timeStr) return '未设置'
      // 格式化时间显示，例如：2024-01-01 12:00:00 -> 01-01 12:00
      try {
        const date = new Date(timeStr)
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${month}-${day} ${hours}:${minutes}`
      } catch (e) {
        return timeStr
      }
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
