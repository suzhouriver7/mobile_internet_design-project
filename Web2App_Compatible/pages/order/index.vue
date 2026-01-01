<template>
  <view class="order-detail-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <u-loading mode="circle" :show="true"></u-loading>
    </view>

    <!-- 订单详情内容 -->
    <scroll-view v-else-if="orderDetail" class="detail-scroll" scroll-y>
      <!-- 发布者信息 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-title">发布者信息</text>
        </view>
        <view class="publisher-info">
          <u-avatar 
            :src="resolveResourceUrl(orderDetail.order?.user?.avatarUrl)" 
            size="64"
          ></u-avatar>
          <view class="publisher-meta">
            <text class="publisher-name">{{ orderDetail.order?.user?.nickname || '未知用户' }}</text>
            <text class="publisher-id">ID: {{ orderDetail.order?.user?.id }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="detail-card">
        <view class="card-header">
          <text class="card-title">订单信息</text>
          <view class="order-status-tag" :class="getStatusClass(orderDetail.order?.status)">
            {{ getOrderStatusText(orderDetail.order?.status) }}
          </view>
        </view>
        
        <view class="order-info-list">
          <view class="info-item">
            <text class="info-label">活动类型</text>
            <text class="info-value">{{ getActivityTypeText(orderDetail.order?.activityType) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">性别要求</text>
            <text class="info-value">{{ getGenderRequireText(orderDetail.order?.genderRequire) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">校区</text>
            <text class="info-value">{{ getCampusText(orderDetail.order?.campus) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">活动地点</text>
            <text class="info-value">{{ orderDetail.order?.location }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">开始时间</text>
            <text class="info-value">{{ formatTime(orderDetail.order?.startTime) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">人数</text>
            <text class="info-value highlight">
              {{ orderDetail.order?.currentPeople || 1 }}/{{ orderDetail.order?.maxPeople }}
            </text>
          </view>
          <view class="info-item" v-if="orderDetail.order?.note">
            <text class="info-label">备注</text>
            <text class="info-value note">{{ orderDetail.order?.note }}</text>
          </view>
        </view>
      </view>

      <!-- 申请列表（仅发布者可见） -->
      <view class="detail-card" v-if="isPublisher">
        <view class="card-header">
          <text class="card-title">申请列表 ({{ applicationList.length }})</text>
        </view>
        <view v-if="applicationList.length === 0" class="empty-applications">
          <u-empty mode="list" text="暂无申请" icon-size="60"></u-empty>
        </view>
        <view v-else class="application-list">
          <view 
            v-for="apply in applicationList" 
            :key="apply.id" 
            class="application-item"
          >
            <view class="application-header">
              <u-avatar 
                :src="resolveResourceUrl(apply.user?.avatarUrl)" 
                size="40"
              ></u-avatar>
              <view class="application-user-info">
                <text class="application-user-name">{{ apply.user?.nickname || '未知用户' }}</text>
                <text class="application-time">{{ formatRelativeTime(apply.createdAt) }}</text>
              </view>
              <view class="application-status" :class="getApplyStatusClass(apply.status)">
                {{ getApplyStatusText(apply.status) }}
              </view>
            </view>
            
            <!-- 审核按钮（仅待审核状态且是发布者可见） -->
            <view 
              v-if="(apply.status === APPLY_STATUS.PENDING_REVIEW || apply.status === 'PENDING_REVIEW') && isPublisher" 
              class="application-actions"
            >
              <u-button 
                type="error" 
                size="small" 
                shape="circle"
                @click="handleReject(apply)"
                :loading="auditing === apply.id"
              >
                拒绝
              </u-button>
              <u-button 
                type="primary" 
                size="small" 
                shape="circle"
                @click="handleApprove(apply)"
                :loading="auditing === apply.id"
              >
                同意
              </u-button>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作按钮区域 -->
      <view class="action-buttons">
        <!-- 发布者的操作 -->
        <template v-if="isPublisher">
          <u-button 
            v-if="orderDetail.order?.status === ORDER_STATUS.PENDING || orderDetail.order?.status === 'PENDING'" 
            type="primary"
            @click="handleCompleteOrder"
            :loading="completing"
          >
            完成订单
          </u-button>
          <u-button 
            type="error"
            @click="handleDeleteOrder"
            :loading="deleting"
          >
            删除订单
          </u-button>
        </template>
        
        <!-- 申请者的操作 -->
        <template v-else-if="isLogin && !isPublisher">
          <u-button 
            v-if="!myApplication" 
            type="primary"
            @click="handleApplyOrder"
            :loading="applying"
            :disabled="!canApply"
          >
            申请加入
          </u-button>
          <u-button 
            v-else-if="myApplication.status === APPLY_STATUS.PENDING_REVIEW"
            type="warning"
            @click="handleCancelApply"
            :loading="cancelling"
          >
            取消申请
          </u-button>
          <view v-else class="apply-status-hint">
            <text>申请状态：{{ getApplyStatusText(myApplication?.status) }}</text>
          </view>
        </template>

        <!-- 聊天按钮 -->
        <u-button 
          type="info"
          plain
          @click="toMessages"
          v-if="isLogin && (isPublisher || myApplication)"
        >
          订单聊天
        </u-button>
      </view>
    </scroll-view>

    <!-- 错误状态 -->
    <view v-else class="error-container">
      <u-empty mode="error" text="加载失败">
        <u-button type="primary" size="mini" @click="loadOrderDetail">重新加载</u-button>
      </u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { orderApi } from '@/api'
import { 
  formatTime, 
  formatRelativeTime, 
  resolveResourceUrl,
  showLoading,
  hideLoading,
  showSuccess,
  showError
} from '@/utils/util'
import { 
  ACTIVITY_TYPE_MAP,
  ACTIVITY_TYPE_STRING_MAP,
  CAMPUS_MAP,
  CAMPUS_STRING_MAP,
  GENDER_REQUIRE_MAP,
  GENDER_REQUIRE_STRING_MAP,
  ORDER_STATUS_MAP,
  ORDER_STATUS_STRING_MAP,
  APPLY_STATUS_MAP,
  APPLY_STATUS_STRING_MAP,
  ORDER_STATUS,
  APPLY_STATUS
} from '@/utils/constants'

// 使用Vuex store
const store = useStore()
const isLogin = computed(() => store.getters.isLogin)
const currentUserId = computed(() => store.getters.userId)

// 响应式数据
const loading = ref(false)
const orderDetail = ref(null)
const orderId = ref(null)
const auditing = ref(null)
const applying = ref(false)
const cancelling = ref(false)
const completing = ref(false)
const deleting = ref(false)

// 计算属性
const order = computed(() => orderDetail.value?.order)
const applicationList = computed(() => {
  if (!orderDetail.value?.applications) return []
  // 按创建时间倒序排列
  return [...orderDetail.value.applications].sort((a, b) => {
    return new Date(b.createdAt) - new Date(a.createdAt)
  })
})

const isPublisher = computed(() => {
  if (!isLogin.value || !order.value) return false
  return order.value.user?.id === currentUserId.value
})

const myApplication = computed(() => {
  if (!isLogin.value || !applicationList.value.length) return null
  return applicationList.value.find(app => app.user?.id === currentUserId.value) || null
})

const canApply = computed(() => {
  if (!order.value) return false
  // 订单必须是待匹配或进行中状态（支持数值和字符串）
  const status = order.value.status
  const validStatus = status === ORDER_STATUS.PENDING || status === 'PENDING' ||
                     status === ORDER_STATUS.IN_PROGRESS || status === 'IN_PROGRESS'
  // 人数未满
  const notFull = (order.value.currentPeople || 1) < order.value.maxPeople
  return validStatus && notFull && !myApplication.value
})

// 生命周期
onLoad((options) => {
  if (options.id) {
    orderId.value = parseInt(options.id)
    loadOrderDetail()
  } else {
    showError('订单ID不存在')
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }
})

onShow(() => {
  // 从其他页面返回时刷新数据
  if (orderId.value) {
    loadOrderDetail()
  }
})

// 方法
const loadOrderDetail = async () => {
  if (!orderId.value) return
  
  loading.value = true
  try {
    const detail = await orderApi.getOrderDetail(orderId.value)
    orderDetail.value = detail
    
    // 处理头像URL
    if (detail.order?.user?.avatarUrl) {
      detail.order.user.avatarUrl = resolveResourceUrl(detail.order.user.avatarUrl)
    }
    if (detail.applications) {
      detail.applications.forEach(app => {
        if (app.user?.avatarUrl) {
          app.user.avatarUrl = resolveResourceUrl(app.user.avatarUrl)
        }
      })
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    showError('加载订单详情失败')
    orderDetail.value = null
  } finally {
    loading.value = false
  }
}

// 审核申请 - 同意
const handleApprove = async (apply) => {
  if (!apply?.id || !order.value?.id) return
  
  try {
    const res = await uni.showModal({
      title: '确认审核',
      content: `确定要同意 ${apply.user?.nickname || '该用户'} 的申请吗？`,
      confirmText: '同意',
      cancelText: '取消'
    })
    
    if (!res.confirm) return
    
    auditing.value = apply.id
    showLoading('处理中...')
    
    // 第一步：调用auditApply改变申请状态为APPROVED
    // 使用字符串形式与后端枚举匹配
    await orderApi.auditApply(apply.id, { 
      status: 'APPROVED'
    })
    
    // 第二步：如果同意，再调用acceptOrder
    await orderApi.acceptOrder(order.value.id, { 
      accepterId: apply.user.id 
    })
    
    showSuccess('已同意申请')
    
    // 重新加载详情
    await loadOrderDetail()
    
  } catch (error) {
    console.error('审核申请失败:', error)
    showError(error.message || '审核失败，请稍后重试')
  } finally {
    auditing.value = null
    hideLoading()
  }
}

// 审核申请 - 拒绝
const handleReject = async (apply) => {
  if (!apply?.id) return
  
  try {
    const res = await uni.showModal({
      title: '确认审核',
      content: `确定要拒绝 ${apply.user?.nickname || '该用户'} 的申请吗？`,
      confirmText: '拒绝',
      cancelText: '取消',
      confirmColor: '#dd524d'
    })
    
    if (!res.confirm) return
    
    auditing.value = apply.id
    showLoading('处理中...')
    
    // 只调用auditApply改变申请状态为REJECTED
    // 使用字符串形式与后端枚举匹配
    await orderApi.auditApply(apply.id, { 
      status: 'REJECTED'
    })
    
    showSuccess('已拒绝申请')
    
    // 重新加载详情
    await loadOrderDetail()
    
  } catch (error) {
    console.error('审核申请失败:', error)
    showError(error.message || '审核失败，请稍后重试')
  } finally {
    auditing.value = null
    hideLoading()
  }
}

// 申请加入订单
const handleApplyOrder = async () => {
  if (!order.value?.id || !canApply.value) return
  
  try {
    applying.value = true
    showLoading('申请中...')
    
    await orderApi.applyOrder(order.value.id, {})
    
    showSuccess('申请成功，等待发布者审核')
    
    // 重新加载详情
    await loadOrderDetail()
    
  } catch (error) {
    console.error('申请加入失败:', error)
    showError(error.message || '申请失败，请稍后重试')
  } finally {
    applying.value = false
    hideLoading()
  }
}

// 取消申请
const handleCancelApply = async () => {
  if (!order.value?.id || !myApplication.value) return
  
  try {
    const res = await uni.showModal({
      title: '确认取消',
      content: '确定要取消申请吗？',
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (!res.confirm) return
    
    cancelling.value = true
    showLoading('处理中...')
    
    await orderApi.cancelApply(order.value.id)
    
    showSuccess('已取消申请')
    
    // 重新加载详情
    await loadOrderDetail()
    
  } catch (error) {
    console.error('取消申请失败:', error)
    showError(error.message || '取消失败，请稍后重试')
  } finally {
    cancelling.value = false
    hideLoading()
  }
}

// 完成订单
const handleCompleteOrder = async () => {
  if (!order.value?.id) return
  
  try {
    const res = await uni.showModal({
      title: '确认完成',
      content: '确定要完成该订单吗？',
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (!res.confirm) return
    
    completing.value = true
    showLoading('处理中...')
    
    await orderApi.completeOrder(order.value.id)
    
    showSuccess('订单已完成')
    
    // 重新加载详情
    await loadOrderDetail()
    
  } catch (error) {
    console.error('完成订单失败:', error)
    showError(error.message || '操作失败，请稍后重试')
  } finally {
    completing.value = false
    hideLoading()
  }
}

// 删除订单
const handleDeleteOrder = async () => {
  if (!order.value?.id) return
  
  try {
    const res = await uni.showModal({
      title: '确认删除',
      content: '确定要删除该订单吗？删除后无法恢复',
      confirmText: '确定',
      cancelText: '取消',
      confirmColor: '#dd524d'
    })
    
    if (!res.confirm) return
    
    deleting.value = true
    showLoading('处理中...')
    
    await orderApi.deleteOrder(order.value.id)
    
    showSuccess('订单已删除')
    
    // 返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
    
  } catch (error) {
    console.error('删除订单失败:', error)
    showError(error.message || '删除失败，请稍后重试')
  } finally {
    deleting.value = false
    hideLoading()
  }
}

// 跳转到聊天页面
const toMessages = () => {
  if (!order.value?.id) return
  uni.navigateTo({
    url: `/pages/order/messages?orderId=${order.value.id}`
  })
}

// 工具方法
const getActivityTypeText = (type) => {
  // 支持数值和字符串枚举值
  if (typeof type === 'number') {
    return ACTIVITY_TYPE_MAP[type] || '未知活动'
  } else if (typeof type === 'string') {
    return ACTIVITY_TYPE_STRING_MAP[type] || '未知活动'
  }
  return '未知活动'
}

const getCampusText = (campus) => {
  if (typeof campus === 'number') {
    return CAMPUS_MAP[campus] || '未知校区'
  } else if (typeof campus === 'string') {
    return CAMPUS_STRING_MAP[campus] || '未知校区'
  }
  return '未知校区'
}

const getGenderRequireText = (gender) => {
  if (typeof gender === 'number') {
    return GENDER_REQUIRE_MAP[gender] || '性别不限'
  } else if (typeof gender === 'string') {
    return GENDER_REQUIRE_STRING_MAP[gender] || '性别不限'
  }
  return '性别不限'
}

const getOrderStatusText = (status) => {
  if (typeof status === 'number') {
    return ORDER_STATUS_MAP[status] || '未知状态'
  } else if (typeof status === 'string') {
    return ORDER_STATUS_STRING_MAP[status] || '未知状态'
  }
  return '未知状态'
}

const getApplyStatusText = (status) => {
  if (typeof status === 'number') {
    return APPLY_STATUS_MAP[status] || '未知状态'
  } else if (typeof status === 'string') {
    return APPLY_STATUS_STRING_MAP[status] || '未知状态'
  }
  return '未知状态'
}

const getStatusClass = (status) => {
  // 支持数值和字符串
  const statusValue = typeof status === 'string' 
    ? { 'PENDING': 0, 'IN_PROGRESS': 1, 'COMPLETED': 2, 'CANCELLED': 3, 'EXPIRED': 4 }[status]
    : status
  
  const classMap = {
    0: 'status-pending',
    1: 'status-in-progress',
    2: 'status-completed',
    3: 'status-cancelled',
    4: 'status-expired'
  }
  return classMap[statusValue] || ''
}

const getApplyStatusClass = (status) => {
  // 支持数值和字符串
  const statusValue = typeof status === 'string'
    ? { 'PENDING_REVIEW': 0, 'APPROVED': 1, 'REJECTED': 2, 'CANCELLED_APPLY': 3 }[status]
    : status
    
  const classMap = {
    0: 'apply-pending',
    1: 'apply-approved',
    2: 'apply-rejected',
    3: 'apply-cancelled'
  }
  return classMap[statusValue] || ''
}
</script>

<style lang="scss" scoped>
.order-detail-page {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
}

.detail-scroll {
  height: calc(100vh - 0rpx);
  padding: 24rpx 32rpx;
  padding-bottom: 120rpx;
}

.detail-card {
  background: white;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    padding-bottom: 16rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .card-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .order-status-tag {
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      font-size: 24rpx;
      font-weight: 500;
      
      &.status-pending {
        background: #e3f2fd;
        color: #1976d2;
      }
      
      &.status-in-progress {
        background: #e8f5e9;
        color: #388e3c;
      }
      
      &.status-completed {
        background: #f5f5f5;
        color: #616161;
      }
      
      &.status-cancelled {
        background: #ffebee;
        color: #d32f2f;
      }
      
      &.status-expired {
        background: #fff8e1;
        color: #ff8f00;
      }
    }
  }
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
  
  .publisher-meta {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    
    .publisher-name {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;
    }
    
    .publisher-id {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.order-info-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .info-label {
      font-size: 28rpx;
      color: #666;
      min-width: 120rpx;
    }
    
    .info-value {
      flex: 1;
      text-align: right;
      font-size: 28rpx;
      color: #333;
      
      &.highlight {
        color: #007aff;
        font-weight: 500;
      }
      
      &.note {
        text-align: left;
        margin-top: 8rpx;
        line-height: 1.6;
        color: #666;
      }
    }
  }
}

.empty-applications {
  padding: 40rpx 0;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.application-item {
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  
  .application-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 16rpx;
    
    .application-user-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4rpx;
      
      .application-user-name {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
      
      .application-time {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .application-status {
      padding: 6rpx 12rpx;
      border-radius: 12rpx;
      font-size: 22rpx;
      font-weight: 500;
      
      &.apply-pending {
        background: #fff3cd;
        color: #856404;
      }
      
      &.apply-approved {
        background: #d4edda;
        color: #155724;
      }
      
      &.apply-rejected {
        background: #f8d7da;
        color: #721c24;
      }
      
      &.apply-cancelled {
        background: #e2e3e5;
        color: #383d41;
      }
    }
  }
  
  .application-actions {
    display: flex;
    gap: 16rpx;
    justify-content: flex-end;
    padding-top: 16rpx;
    border-top: 1rpx solid #e5e5e5;
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 24rpx 0;
  
  .apply-status-hint {
    text-align: center;
    padding: 24rpx;
    background: #f8f9fa;
    border-radius: 12rpx;
    font-size: 28rpx;
    color: #666;
  }
}
</style>