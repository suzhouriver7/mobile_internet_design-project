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
          v-if="!isOwner && hasApplied && canCancelApply"
          class="action-btn"
          @click="handleCancelApply"
        >
          取消申请
        </button>
        <view
          v-if="!isOwner && hasApplied && !canCancelApply"
          class="apply-status-text"
        >
          <text>申请状态：{{ getApplyStatusText(myApplication.status) }}</text>
        </view>
        <button
          v-if="isOwner && canCancelOrder"
          class="action-btn cancel"
          @click="handleCancelOrder"
        >
          取消订单
        </button>
        <button
          v-if="isOwner"
          class="action-btn"
          @click="toMessages"
        >
          查看消息
        </button>
        <button
          v-if="isOwner && canCompleteOrder"
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
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
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
  return orderDetail.value.order && orderDetail.value.order.user && (orderDetail.value.order.user.uid === userId || orderDetail.value.order.user.id === userId)
})

const hasApplied = computed(() => {
  if (!orderDetail.value || !store.getters['user/isLogin']) return false
  const userId = store.getters['user/userId']
  return applications.value.some(app => 
    app.user && (app.user.uid === userId || app.user.id === userId)
  )
})

// 当前用户的申请状态
const myApplication = computed(() => {
  if (!store.getters['user/isLogin']) return null
  const userId = store.getters['user/userId']
  return applications.value.find(app => 
    app.user && (app.user.uid === userId || app.user.id === userId)
  )
})

// 是否可以取消申请（只有待审核状态可以取消）
const canCancelApply = computed(() => {
  const app = myApplication.value
  return app && app.status === 'PENDING_REVIEW'
})

// 是否可以取消订单（只有待匹配或进行中状态可以取消）
const canCancelOrder = computed(() => {
  if (!orderDetail.value || !orderDetail.value.order) return false
  const status = orderDetail.value.order.status
  return status === 'PENDING' || status === 'IN_PROGRESS'
})

// 是否可以完成订单（只有进行中状态可以完成）
const canCompleteOrder = computed(() => {
  if (!orderDetail.value || !orderDetail.value.order) return false
  return orderDetail.value.order.status === 'IN_PROGRESS'
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
  if (!orderId.value) {
    showError('缺少订单ID')
    return
  }
  
  loading.value = true
  showLoading('加载中...')
  
  try {
    const detail = await orderApi.getOrderDetail(orderId.value)
    orderDetail.value = detail
    
    // 加载申请列表（所有人都可以获取，用于判断是否已申请）
    try {
      const apps = await orderApi.getApplications(orderId.value)
      applications.value = apps || []
      
      // 如果是发布者，显示所有申请
      // 如果是非发布者，只保留自己的申请用于判断状态
      if (!isOwner.value && store.getters['user/isLogin']) {
        const userId = store.getters['user/userId']
        applications.value = applications.value.filter(app => 
          app.user && (app.user.uid === userId || app.user.id === userId)
        )
      }
    } catch (e) {
      // 如果获取申请列表失败，忽略错误
      console.error('获取申请列表失败:', e)
      applications.value = []
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
  
  // 如果已经有申请记录，提示用户
  if (myApplication.value) {
    const status = myApplication.value.status
    if (status === 'APPROVED') {
      showError('您的申请已通过，无需重复申请')
      return
    } else if (status === 'REJECTED') {
      showError('您的申请已被拒绝，无法再次申请')
      return
    } else if (status === 'PENDING_REVIEW') {
      showError('您已提交申请，请等待审核')
      return
    }
  }
  
  try {
    await orderApi.applyOrder(orderId.value, '')
    showSuccess('申请成功')
    loadOrderDetail()
  } catch (error) {
    // 改进错误提示
    let errorMsg = error.message || '申请失败'
    if (errorMsg.includes('已申请过')) {
      errorMsg = '您已申请过该订单，请勿重复申请'
    } else if (errorMsg.includes('人数已满')) {
      errorMsg = '该订单人数已满，无法申请'
    } else if (errorMsg.includes('已完成') || errorMsg.includes('已取消') || errorMsg.includes('已过期')) {
      errorMsg = '该订单状态不允许申请'
    }
    showError(errorMsg)
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

const handleCancelOrder = async () => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这个订单吗？此操作不可撤销。',
    success: async (res) => {
      if (res.confirm) {
        try {
          showLoading('取消中...')
          await orderApi.deleteOrder(orderId.value)
          showSuccess('订单已取消')
          hideLoading()
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } catch (error) {
          hideLoading()
          showError(error.message || '取消失败')
        }
      }
    }
  })
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

// uni-app 中页面参数通过 onLoad 获取
onLoad((options) => {
  if (options.id) {
    orderId.value = options.id
    loadOrderDetail()
  } else {
    showError('缺少订单ID')
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

.action-btn.cancel {
  background: #ff9500;
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

.apply-status-text {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666666;
}
</style>

