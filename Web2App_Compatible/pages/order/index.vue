<template>
  <view class="orders-container">
    <uni-card>
      <view slot="header" class="card-header">
        <text class="title">预约订单</text>
        <uni-button type="primary" @click="toCreateOrder">发布订单</uni-button>
      </view>

      <uni-forms :model="searchForm" ref="searchForm" inline>
        <uni-forms-item label="活动类型">
          <uni-select
            v-model="searchForm.activityType"
            placeholder="请选择"
            clearable
            @change="onSearch"
          >
            <uni-select-option value="" text="不限"></uni-select-option>
            <uni-select-option value="BASKETBALL" text="篮球"></uni-select-option>
            <uni-select-option value="BADMINTON" text="羽毛球"></uni-select-option>
            <uni-select-option value="MEAL" text="吃饭"></uni-select-option>
            <uni-select-option value="STUDY" text="自习"></uni-select-option>
            <!-- 其他活动类型 -->
          </uni-select>
        </uni-forms-item>

        <uni-forms-item label="校区">
          <uni-select
            v-model="searchForm.campus"
            placeholder="请选择"
            clearable
            @change="onSearch"
          >
            <uni-select-option value="" text="不限"></uni-select-option>
            <uni-select-option value="LIANGXIANG" text="良乡校区"></uni-select-option>
            <uni-select-option value="ZHONGGUANCUN" text="中关村校区"></uni-select-option>
            <!-- 其他校区 -->
          </uni-select>
        </uni-forms-item>

        <uni-button @click="resetForm">重置</uni-button>
      </uni-forms>

      <uni-list v-if="loading" class="order-list">
        <uni-list-item>
          <uni-loading :show="true"></uni-loading>
        </uni-list-item>
      </uni-list>

      <uni-list v-else class="order-list">
        <uni-list-item 
          v-for="order in ordersList" 
          :key="order.id"
          clickable
          @click="toDetail(order.id)"
        >
          <view class="order-main">
            <text class="order-title">{{ order.activityType | formatActivityType }}</text>
            <view class="order-meta">
              <text>{{ order.campus | formatCampus }}</text>
              <text>{{ order.startTime | formatTime }}</text>
              <text>剩余: {{ order.maxPeople - order.currentPeople }}人</text>
            </view>
          </view>
          <uni-tag :text="order.status | formatStatus" :type="getStatusType(order.status)"></uni-tag>
        </uni-list-item>
      </uni-list>

      <uni-pagination 
        v-if="total > 0"
        :current="currentPage"
        :pageSize="pageSize"
        :total="total"
        @change="onPageChange"
      ></uni-pagination>
    </uni-card>
  </view>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { formatActivityType, formatCampus, formatStatus, getStatusType } from '../../utils/formatter'

export default {
  filters: {
    formatActivityType,
    formatCampus,
    formatStatus
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const ordersList = ref([])
    
    const searchForm = reactive({
      activityType: '',
      campus: '',
      status: ''
    })

    const fetchOrders = async () => {
      loading.value = true
      try {
        const res = await request({
          url: '/orders',
          data: {
            page: currentPage.value,
            size: pageSize.value,
            activityType: searchForm.activityType,
            campus: searchForm.campus,
            status: searchForm.status
          }
        })
        ordersList.value = res.data.list
        total.value = res.data.total
      } catch (err) {
        console.error('获取订单失败', err)
      } finally {
        loading.value = false
      }
    }

    const onSearch = () => {
      currentPage.value = 1
      fetchOrders()
    }

    const resetForm = () => {
      searchForm.activityType = ''
      searchForm.campus = ''
      searchForm.status = ''
      currentPage.value = 1
      fetchOrders()
    }

    const onPageChange = (page) => {
      currentPage.value = page
      fetchOrders()
    }

    const toCreateOrder = () => {
      router.push('/pages/orders/create')
    }

    const toDetail = (orderId) => {
      router.push(`/pages/orders/detail?orderId=${orderId}`)
    }

    onMounted(() => {
      fetchOrders()
    })

    return {
      loading,
      currentPage,
      pageSize,
      total,
      ordersList,
      searchForm,
      fetchOrders,
      onSearch,
      resetForm,
      onPageChange,
      toCreateOrder,
      toDetail,
      getStatusType
    }
  }
}
</script>

<style scoped>
/* 样式适配移动端 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
}
.order-list {
  margin-top: 20rpx;
}
.order-main {
  flex: 1;
}
.order-title {
  font-size: 32rpx;
  color: #333;
}
.order-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}
</style>