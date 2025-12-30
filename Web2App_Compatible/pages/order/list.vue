<template>
  <view class="orders-page">
    <uni-nav-bar title="预约订单" left-arrow @clickLeft="onBack">
      <uni-button slot="right" type="primary" size="mini" @click="handleCreateOrder">发布订单</uni-button>
    </uni-nav-bar>

    <!-- 搜索筛选 -->
    <uni-form :model="searchForm" class="search-form">
      <uni-form-item label="活动类型">
        <uni-select 
          v-model="searchForm.activityType" 
          placeholder="请选择" 
          clearable
          @change="onSearch"
        >
          <uni-select-option label="不限" :value="''"></uni-select-option>
          <uni-select-option label="篮球" value="BASKETBALL"></uni-select-option>
          <uni-select-option label="羽毛球" value="BADMINTON"></uni-select-option>
          <uni-select-option label="吃饭" value="MEAL"></uni-select-option>
          <uni-select-option label="自习" value="STUDY"></uni-select-option>
          <uni-select-option label="看电影" value="MOVIE"></uni-select-option>
          <uni-select-option label="跑步" value="RUNNING"></uni-select-option>
          <uni-select-option label="游戏" value="GAME"></uni-select-option>
          <uni-select-option label="其他" value="OTHER"></uni-select-option>
        </uni-select>
      </uni-form-item>
      
      <uni-form-item label="校区">
        <uni-select 
          v-model="searchForm.campus" 
          placeholder="请选择" 
          clearable
          @change="onSearch"
        >
          <uni-select-option label="不限" :value="''"></uni-select-option>
          <uni-select-option label="良乡校区" value="LIANGXIANG"></uni-select-option>
          <uni-select-option label="中关村校区" value="ZHONGGUANCUN"></uni-select-option>
        </uni-select>
      </uni-form-item>
    </uni-form>

    <!-- 订单列表 -->
    <uni-list v-if="loading">
      <uni-list-item><uni-loading type="circle"></uni-loading></uni-list-item>
    </uni-list>
    
    <uni-list v-else>
      <uni-list-item 
        v-for="order in orderList" 
        :key="order.id" 
        clickable
        @click="navigateToDetail(order.id)"
      >
        <template #header>
          <view class="order-header">
            <text class="order-type">{{ formatActivityType(order.activityType) }}</text>
            <text class="order-status">{{ formatStatus(order.status) }}</text>
          </view>
        </template>
        <view class="order-info">
          <text>地点：{{ order.location }}</text>
          <text>时间：{{ order.startTime }}</text>
          <text>人数：{{ order.joinedPeople }}/{{ order.maxPeople }}</text>
        </view>
        <template #footer>
          <text class="order-creator">发布者：{{ order.creatorNickname }}</text>
        </template>
      </uni-list-item>
    </uni-list>

    <uni-load-more 
      :status="loadStatus" 
      @clickLoadMore="loadMore"
    ></uni-load-more>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { getOrderList } from '@/api/order'

const searchForm = ref({
  activityType: '',
  campus: ''
})
const orderList = ref([])
const page = ref(1)
const size = ref(10)
const loading = ref(true)
const loadStatus = ref('more')

onLoad(async () => {
  await fetchOrders(true)
})

// 获取订单列表
const fetchOrders = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    loading.value = true
  } else {
    loadStatus.value = 'loading'
  }

  try {
    const res = await getOrderList({
      page: page.value,
      size: size.value,
      activityType: searchForm.value.activityType,
      campus: searchForm.value.campus
    })
    
    if (isRefresh) {
      orderList.value = res.data.list
    } else {
      orderList.value = [...orderList.value, ...res.data.list]
    }
    
    loadStatus.value = orderList.value.length >= res.data.total ? 'noMore' : 'more'
  } catch (err) {
    uni.showToast({ title: '获取订单失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 搜索筛选
const onSearch = () => {
  fetchOrders(true)
}

// 加载更多
const loadMore = () => {
  if (loadStatus.value !== 'more') return
  page.value++
  fetchOrders()
}

// 格式化活动类型
const formatActivityType = (type) => {
  const map = {
    'BASKETBALL': '篮球',
    'BADMINTON': '羽毛球',
    'MEAL': '吃饭',
    'STUDY': '自习',
    'MOVIE': '看电影',
    'RUNNING': '跑步',
    'GAME': '游戏',
    'OTHER': '其他'
  }
  return map[type] || type
}

// 格式化订单状态
const formatStatus = (status) => {
  const map = {
    'PENDING': '待参与',
    'COMPLETED': '已完成',
    'CANCELED': '已取消'
  }
  return map[status] || status
}

// 跳转到订单详情
const navigateToDetail = (id) => {
  uni.navigateTo({ url: `/pages/orders/detail?id=${id}` })
}

// 跳转到创建订单页
const handleCreateOrder = () => {
  uni.navigateTo({ url: '/pages/orders/create' })
}

// 返回上一页
const onBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.search-form {
  padding: 20rpx;
  background-color: #fff;
  border-bottom: 1px solid #f5f5f5;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.order-type {
  font-size: 30rpx;
  font-weight: bold;
}

.order-status {
  font-size: 26rpx;
  padding: 5rpx 15rpx;
  border-radius: 20rpx;
  background-color: #e8f4fd;
  color: #409eff;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
  font-size: 26rpx;
  color: #666;
}

.order-creator {
  font-size: 24rpx;
  color: #999;
}
</style>