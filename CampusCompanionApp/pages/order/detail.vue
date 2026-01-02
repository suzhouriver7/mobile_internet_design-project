<template>
  <view class="order-detail-container">
    <view v-if="orderDetail" class="detail-content">
      <!-- 订单信息 -->
      <view class="info-section">
        <view class="section-title">活动信息</view>
        <view class="info-item">
          <text class="info-label">活动类型：</text>
          <text class="info-value">{{ getActivityTypeText(orderDetail.order.activityType) }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">活动地点：</text>
          <text class="info-value">{{ orderDetail.order.location }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">开始时间：</text>
          <text class="info-value">{{ formatTime(orderDetail.order.startTime) }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">校区：</text>
          <text class="info-value">{{ getCampusText(orderDetail.order.campus) }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">人数：</text>
          <text class="info-value">{{ orderDetail.order.currentPeople }}/{{ orderDetail.order.maxPeople }}人</text>
        </view>
        <view v-if="orderDetail.order.note" class="info-item">
          <text class="info-label">备注：</text>
          <text class="info-value">{{ orderDetail.order.note }}</text>
        </view>
      </view>
      
      <!-- 发布者信息 -->
      <view class="info-section">
        <view class="section-title">发布者</view>
        <view class="user-info">
          <image
            v-if="orderDetail.order.user && orderDetail.order.user.avatarUrl"
            :src="orderDetail.order.user.avatarUrl"
            class="user-avatar"
            mode="aspectFill"
          />
          <text class="user-nickname">{{ orderDetail.order.user ? orderDetail.order.user.nickname : '匿名' }}</text>
        </view>
      </view>
      
      <!-- 申请列表（仅发布者可见） -->
      <view v-if="isOwner" class="info-section">
        <view class="section-title">申请列表</view>
        <view v-if="applications.length > 0" class="application-list">
          <view
            v-for="app in applications"
            :key="app.apid || app.id"
            class="application-item"
          >
            <view class="application-user">
              <image
                v-if="app.user && app.user.avatarUrl"
                :src="app.user.avatarUrl"
                class="user-avatar"
                mode="aspectFill"
              />
              <text class="user-nickname">{{ app.user ? app.user.nickname : '匿名' }}</text>
            </view>
            <view class="application-actions">
              <text class="status-text">{{ getApplyStatusText(app.status) }}</text>
              <button
                v-if="app.status === 'PENDING_REVIEW'"
                class="action-btn approve"
                @click="handleAudit(app.apid || app.id, 'APPROVED')"
              >
                通过
              </button>
              <button
                v-if="app.status === 'PENDING_REVIEW'"
                class="action-btn reject"
                @click="handleAudit(app.apid || app.id, 'REJECTED')"
              >
                拒绝
              </button>
            </view>
          </view>
        </view>
        <view v-else class="empty-state">
          <text class="empty-text">暂无申请</text>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-bar">
        <button
          v-if="!isOwner && !hasApplied"
          class="action-btn primary"
          @click="handleApply"
        >
          申请加入
        </button>
        <button
          v-if="!isOwner && hasApplied"
          class="action-btn"
          @click="handleCancelApply"
        >
          取消申请
        </button>
        <button
          v-if="isOwner"
          class="action-btn"
          @click="toMessages"
        >
          查看消息
        </button>
        <button
          v-if="isOwner"
          class="action-btn"
          @click="handleComplete"
        >
          完成订单
        </button>
      </view>
    </view>
    
    <view v-else-if="loading" class="loading-state">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { orderApi } from '@/api/index.js'
import { formatTime, showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'
import { ACTIVITY_TYPE_MAP, CAMPUS_MAP, APPLY_STATUS_MAP } from '@/utils/constants.js'

const store = useStore()

const orderDetail = ref(null)
const applications = ref([])
const loading = ref(false)

const orderId = ref(null)

const isOwner = computed(() => {
  if (!orderDetail.value || !store.getters['user/isLogin']) return false
  const userId = store.getters['user/userId']
  return orderDetail.value.order.user && (orderDetail.value.order.user.uid === userId || orderDetail.value.order.user.id === userId)
})

const hasApplied = computed(() => {
  if (!orderDetail.value || !store.getters['user/isLogin']) return false
  const userId = store.getters['user/userId']
  return applications.value.some(app => 
    app.user && (app.user.uid === userId || app.user.id === userId)
  )
})

const getActivityTypeText = (type) => {
  return ACTIVITY_TYPE_MAP[type] || '其他'
}

const getCampusText = (campus) => {
  return CAMPUS_MAP[campus] || '其他'
}

const getApplyStatusText = (status) => {
  return APPLY_STATUS_MAP[status] || '未知'
}

const loadOrderDetail = async () => {
  loading.value = true
  showLoading('加载中...')
  
  try {
    const detail = await orderApi.getOrderDetail(orderId.value)
    orderDetail.value = detail
    
    // 如果是发布者，加载申请列表
    if (isOwner.value) {
      const apps = await orderApi.getApplications(orderId.value)
      applications.value = apps || []
    }
  } catch (error) {
    showError(error.message || '加载失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}

const handleApply = async () => {
  if (!store.getters['user/isLogin']) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  
  try {
    await orderApi.applyOrder(orderId.value, '')
    showSuccess('申请成功')
    loadOrderDetail()
  } catch (error) {
    showError(error.message || '申请失败')
  }
}

const handleCancelApply = async () => {
  try {
    await orderApi.cancelApply(orderId.value)
    showSuccess('取消成功')
    loadOrderDetail()
  } catch (error) {
    showError(error.message || '取消失败')
  }
}

const handleAudit = async (applyId, status) => {
  try {
    await orderApi.auditApply(applyId, status)
    showSuccess(status === 'APPROVED' ? '已通过' : '已拒绝')
    loadOrderDetail()
  } catch (error) {
    showError(error.message || '操作失败')
  }
}

const handleComplete = async () => {
  try {
    await orderApi.completeOrder(orderId.value)
    showSuccess('订单已完成')
    loadOrderDetail()
  } catch (error) {
    showError(error.message || '操作失败')
  }
}

const toMessages = () => {
  uni.navigateTo({
    url: `/pages/order/messages?orderId=${orderId.value}`
  })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  orderId.value = currentPage.options.id
  
  if (orderId.value) {
    loadOrderDetail()
  }
})
</script>

<style scoped>
.order-detail-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 120rpx;
}

.detail-content {
  padding: 30rpx;
}

.info-section {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.info-item {
  display: flex;
  margin-bottom: 16rpx;
  font-size: 28rpx;
}

.info-label {
  color: #666666;
  width: 160rpx;
}

.info-value {
  color: #333333;
  flex: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.user-nickname {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.application-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
}

.application-user {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.application-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.status-text {
  font-size: 24rpx;
  color: #666666;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  border: none;
}

.action-btn.primary {
  background: #007AFF;
  color: #ffffff;
}

.action-btn.approve {
  background: #4cd964;
  color: #ffffff;
}

.action-btn.reject {
  background: #ff3b30;
  color: #ffffff;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 20rpx 30rpx;
  border-top: 2rpx solid #f0f0f0;
  display: flex;
  gap: 20rpx;
}

.action-bar .action-btn {
  flex: 1;
  height: 80rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.empty-state,
.loading-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-text,
.loading-text {
  font-size: 28rpx;
  color: #999999;
}
</style>

