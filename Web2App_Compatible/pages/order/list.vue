<template>
  <view class="orders-page">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <u-search
        v-model="searchKeyword"
        placeholder="搜索活动"
        :show-action="false"
        @search="onSearch"
        @click="onSearchClick"
        shape="square"
        bg-color="#f5f5f5"
      ></u-search>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item" @click="showActivityTypePicker = true">
        <text :class="{'filter-active': filterForm.activityType !== null}">
          {{ filterForm.activityType !== null ? ACTIVITY_TYPE_MAP[filterForm.activityType] : '活动类型' }}
        </text>
        <u-icon name="arrow-down" size="14" color="#666"></u-icon>
      </view>
      
      <view class="filter-item" @click="showCampusPicker = true">
        <text :class="{'filter-active': filterForm.campus !== null}">
          {{ filterForm.campus !== null ? CAMPUS_MAP[filterForm.campus] : '校区' }}
        </text>
        <u-icon name="arrow-down" size="14" color="#666"></u-icon>
      </view>
      
      <view class="filter-item" @click="showStatusPicker = true">
        <text :class="{'filter-active': filterForm.status !== null}">
          {{ filterForm.status !== null ? ORDER_STATUS_MAP[filterForm.status] : '状态' }}
        </text>
        <u-icon name="arrow-down" size="14" color="#666"></u-icon>
      </view>
      
      <view class="filter-item" @click="clearFilter" v-if="hasFilter">
        <u-icon name="close-circle" size="16" color="#999"></u-icon>
        <text>清除</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view 
      class="order-list" 
      scroll-y 
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-if="loading && orderList.length === 0" class="loading-container">
        <u-loading mode="circle" :show="true"></u-loading>
      </view>
      
      <view v-else-if="orderList.length === 0" class="empty-container">
        <u-empty
          mode="list"
          icon="http://cdn.uviewui.com/uview/empty/list.png"
          text="暂无活动"
        >
          <u-button 
            type="primary" 
            size="mini" 
            @click="toCreateOrder"
            v-if="isLogin"
          >去发布活动</u-button>
        </u-empty>
      </view>
      
      <view v-else>
        <view 
          class="order-card" 
          v-for="order in orderList" 
          :key="order.id"
          @click="toOrderDetail(order.id)"
        >
          <view class="order-header">
            <u-avatar 
              :src="resolveResourceUrl(order.user?.avatarUrl)" 
              size="32"
            ></u-avatar>
            <text class="order-user">{{ order.user?.nickname || '用户' }}</text>
            <view class="order-status" :class="getStatusClass(order.status)">
              {{ getOrderStatusText(order.status) }}
            </view>
          </view>
          
          <view class="order-content">
            <text class="order-title">
              {{ getActivityTypeText(order.activityType) }} · {{ getCampusText(order.campus) }}
            </text>
            <view class="order-info">
              <text class="order-location">
                <u-icon name="map" size="14" color="#666"></u-icon>
                {{ order.location }}
              </text>
              <text class="order-time">
                <u-icon name="clock" size="14" color="#666"></u-icon>
                {{ formatTime(order.startTime) }}
              </text>
            </view>
            <text class="order-note" v-if="order.note">{{ order.note }}</text>
          </view>
          
          <view class="order-footer">
            <text class="order-people">
              {{ order.currentPeople || 1 }}/{{ order.maxPeople }}人
            </text>
            <text class="order-time-ago">{{ formatRelativeTime(order.createdAt) }}</text>
          </view>
        </view>
      </view>
      
      <!-- 加载更多 -->
      <view class="load-more" v-if="orderList.length > 0">
        <u-loading 
          mode="circle" 
          :show="loadingMore"
          v-if="loadingMore"
        ></u-loading>
        <text v-else-if="noMore" class="no-more-text">没有更多了</text>
      </view>
    </scroll-view>

    <!-- 发布按钮 -->
    <view class="fab" @click="toCreateOrder" v-if="isLogin">
      <u-icon name="plus" size="24" color="#fff"></u-icon>
    </view>

    <!-- 活动类型选择器 -->
    <u-picker
      :show="showActivityTypePicker"
      :columns="activityTypeColumns"
      @confirm="onActivityTypeConfirm"
      @cancel="showActivityTypePicker = false"
    ></u-picker>

    <!-- 校区选择器 -->
    <u-picker
      :show="showCampusPicker"
      :columns="campusColumns"
      @confirm="onCampusConfirm"
      @cancel="showCampusPicker = false"
    ></u-picker>

    <!-- 状态选择器 -->
    <u-picker
      :show="showStatusPicker"
      :columns="statusColumns"
      @confirm="onStatusConfirm"
      @cancel="showStatusPicker = false"
    ></u-picker>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { orderApi } from '@/api'
import { 
  formatTime, 
  formatRelativeTime, 
  resolveResourceUrl,
  showLoading,
  hideLoading 
} from '@/utils/util'
import { 
  ACTIVITY_TYPE_MAP, 
  CAMPUS_MAP, 
  ORDER_STATUS_MAP,
  ACTIVITY_TYPE,
  CAMPUS,
  ORDER_STATUS
} from '@/utils/constants'

// 使用Vuex store
const store = useStore()
const isLogin = computed(() => store.getters.isLogin)

// 响应式数据
const searchKeyword = ref('')
const filterForm = reactive({
  activityType: null,
  campus: null,
  status: null
})
const orderList = ref([])
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const noMore = ref(false)

// 选择器
const showActivityTypePicker = ref(false)
const showCampusPicker = ref(false)
const showStatusPicker = ref(false)

// 选择器数据
const activityTypeColumns = ref([[
  { text: '不限', value: null },
  { text: '篮球', value: ACTIVITY_TYPE.BASKETBALL },
  { text: '羽毛球', value: ACTIVITY_TYPE.BADMINTON },
  { text: '吃饭', value: ACTIVITY_TYPE.MEAL },
  { text: '自习', value: ACTIVITY_TYPE.STUDY },
  { text: '看电影', value: ACTIVITY_TYPE.MOVIE },
  { text: '跑步', value: ACTIVITY_TYPE.RUNNING },
  { text: '游戏', value: ACTIVITY_TYPE.GAME },
  { text: '其他', value: ACTIVITY_TYPE.OTHER }
]])

const campusColumns = ref([[
  { text: '不限', value: null },
  { text: '良乡校区', value: CAMPUS.LIANGXIANG },
  { text: '中关村校区', value: CAMPUS.ZHONGGUANCUN },
  { text: '珠海校区', value: CAMPUS.ZHUHAI },
  { text: '西山校区', value: CAMPUS.XISHAN },
  { text: '其他校区', value: CAMPUS.OTHER_CAMPUS }
]])

const statusColumns = ref([[
  { text: '不限', value: null },
  { text: '待匹配', value: ORDER_STATUS.PENDING },
  { text: '进行中', value: ORDER_STATUS.IN_PROGRESS },
  { text: '已完成', value: ORDER_STATUS.COMPLETED },
  { text: '已取消', value: ORDER_STATUS.CANCELLED },
  { text: '已过期', value: ORDER_STATUS.EXPIRED }
]])

// 计算属性
const hasFilter = computed(() => {
  return filterForm.activityType !== null || 
         filterForm.campus !== null || 
         filterForm.status !== null
})

// 生命周期
onLoad(() => {
  loadOrders(true)
})

onShow(() => {
  // 页面显示时刷新数据
  loadOrders(true)
})

onPullDownRefresh(() => {
  onRefresh()
})

// 方法
const loadOrders = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    loading.value = true
    noMore.value = false
  } else {
    if (noMore.value || loadingMore.value) return
    loadingMore.value = true
  }
  
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    
    if (filterForm.activityType !== null) {
      params.activityType = filterForm.activityType
    }
    if (filterForm.campus !== null) {
      params.campus = filterForm.campus
    }
    if (filterForm.status !== null) {
      params.status = filterForm.status
    }
    
    const response = await orderApi.getOrders(params)
    const list = response?.list || []
    
    if (isRefresh) {
      orderList.value = list
    } else {
      orderList.value = [...orderList.value, ...list]
    }
    
    // 判断是否还有更多
    const total = response?.total || 0
    if (orderList.value.length >= total) {
      noMore.value = true
    }
    
    // 更新store
    if (isRefresh) {
      store.dispatch('order/setOrders', list)
    } else {
      store.dispatch('order/addOrders', list)
    }
    
  } catch (error) {
    console.error('加载订单列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
    refreshing.value = false
    uni.stopPullDownRefresh()
  }
}

const onRefresh = () => {
  refreshing.value = true
  loadOrders(true)
}

const loadMore = () => {
  if (noMore.value || loadingMore.value) return
  page.value++
  loadOrders(false)
}

const onSearch = () => {
  // 搜索功能可以后续实现
  loadOrders(true)
}

const onSearchClick = () => {
  // 可以跳转到专门的搜索页面
}

const clearFilter = () => {
  filterForm.activityType = null
  filterForm.campus = null
  filterForm.status = null
  loadOrders(true)
}

const onActivityTypeConfirm = (e) => {
  filterForm.activityType = e.value[0].value
  showActivityTypePicker.value = false
  loadOrders(true)
}

const onCampusConfirm = (e) => {
  filterForm.campus = e.value[0].value
  showCampusPicker.value = false
  loadOrders(true)
}

const onStatusConfirm = (e) => {
  filterForm.status = e.value[0].value
  showStatusPicker.value = false
  loadOrders(true)
}

// 工具方法
const getActivityTypeText = (type) => {
  return ACTIVITY_TYPE_MAP[type] || '未知活动'
}

const getCampusText = (campus) => {
  return CAMPUS_MAP[campus] || '未知校区'
}

const getOrderStatusText = (status) => {
  return ORDER_STATUS_MAP[status] || '未知状态'
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-in-progress',
    2: 'status-completed',
    3: 'status-cancelled',
    4: 'status-expired'
  }
  return classMap[status] || ''
}

// 页面跳转
const toOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order/index?id=${orderId}`
  })
}

const toCreateOrder = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  
  uni.navigateTo({
    url: '/pages/order/create'
  })
}
</script>

<style lang="scss" scoped>
.orders-page {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 120rpx;
}

.search-bar {
  padding: 24rpx 32rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
}

.filter-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
  gap: 32rpx;
  
  .filter-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    background: #f5f5f5;
    font-size: 24rpx;
    color: #666;
    
    &:active {
      background: #e9ecef;
    }
    
    .filter-active {
      color: #007aff;
      font-weight: 500;
    }
  }
}

.order-list {
  height: calc(100vh - 200rpx);
  padding: 24rpx 32rpx;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
}

.order-card {
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  
  &:active {
    background: #f8f9fa;
  }
  
  .order-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 20rpx;
    
    .order-user {
      flex: 1;
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }
    
    .order-status {
      padding: 4rpx 12rpx;
      border-radius: 20rpx;
      font-size: 20rpx;
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
  
  .order-content {
    margin-bottom: 20rpx;
    
    .order-title {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }
    
    .order-info {
      display: flex;
      flex-direction: column;
      gap: 8rpx;
      margin-bottom: 12rpx;
      
      .order-location,
      .order-time {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 24rpx;
        color: #666;
      }
    }
    
    .order-note {
      display: block;
      font-size: 24rpx;
      color: #999;
      line-height: 1.5;
    }
  }
  
  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .order-people {
      font-size: 24rpx;
      color: #007aff;
      font-weight: 500;
    }
    
    .order-time-ago {
      font-size: 22rpx;
      color: #999;
    }
  }
}

.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32rpx;
  
  .no-more-text {
    font-size: 24rpx;
    color: #999;
  }
}

.fab {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  z-index: 999;
  
  &:active {
    transform: scale(0.95);
  }
}
</style>
