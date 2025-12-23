<template>
  <div class="orders-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>预约订单</h2>
          <el-button type="primary" @click="handleCreateOrder">发布订单</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动类型">
          <el-select
            v-model="searchForm.activityType"
            placeholder="请选择"
            clearable
            :disabled="loading"
            class="filter-select"
          >
              <el-option label="不限" :value="''" />
              <el-option label="篮球" value="BASKETBALL" />
              <el-option label="羽毛球" value="BADMINTON" />
              <el-option label="吃饭" value="MEAL" />
              <el-option label="自习" value="STUDY" />
              <el-option label="看电影" value="MOVIE" />
              <el-option label="跑步" value="RUNNING" />
              <el-option label="游戏" value="GAME" />
              <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="校区">
          <el-select
            v-model="searchForm.campus"
            placeholder="请选择"
            clearable
            :disabled="loading"
            class="filter-select"
          >
              <el-option label="不限" :value="''" />
              <el-option label="良乡校区" value="LIANGXIANG" />
              <el-option label="中关村校区" value="ZHONGGUANCUN" />
              <el-option label="珠海校区" value="ZHUHAI" />
              <el-option label="西山校区" value="XISHAN" />
              <el-option label="其他校区" value="OTHER_CAMPUS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择"
            clearable
            :disabled="loading"
            class="filter-select"
          >
              <el-option label="不限" :value="''" />
              <el-option label="待匹配" value="PENDING" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="resetForm" :disabled="loading">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-if="ordersList.length > 0"
        v-loading="loading"
        :data="ordersList"
        style="width: 100%"
      >
        <el-table-column label="发布者" min-width="120">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :src="scope.row.user.avatarUrl" size="small" />
              <span>{{ scope.row.user.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动类型" width="100">
          <template #default="scope">
            {{ getActivityTypeLabel(scope.row.activityType) }}
          </template>
        </el-table-column>
        <el-table-column label="校区" width="100">
          <template #default="scope">
            {{ getCampusLabel(scope.row.campus) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="120" />
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column label="人数" width="100">
          <template #default="scope">
            {{ scope.row.currentPeople }}/{{ scope.row.maxPeople }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="150" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewDetail(scope.row.id)">
              详情
            </el-button>
            <el-button type="success" size="small" @click="handleApplyOrder(scope.row.id)">
              申请
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-else-if="!loading"
        description="没有找到符合条件的订单，试试调整筛选条件吧"
      />
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '../stores/order'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

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

// 活动类型映射
const activityTypeMap = {
  BASKETBALL: '篮球',
  BADMINTON: '羽毛球',
  MEAL: '吃饭',
  STUDY: '自习',
  MOVIE: '看电影',
  RUNNING: '跑步',
  GAME: '游戏',
  OTHER: '其他'
}

// 校区映射
const campusMap = {
  LIANGXIANG: '良乡校区',
  ZHONGGUANCUN: '中关村校区',
  ZHUHAI: '珠海校区',
  XISHAN: '西山校区',
  OTHER_CAMPUS: '其他校区'
}

// 订单状态映射
const statusMap = {
  PENDING: '待匹配',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  EXPIRED: '已过期'
}

// 订单状态标签类型
const statusTypeMap = {
  PENDING: 'info',
  IN_PROGRESS: 'success',
  COMPLETED: 'success',
  CANCELLED: 'warning',
  EXPIRED: 'danger'
}

const getActivityTypeLabel = (type) => {
  return activityTypeMap[type] || '未知'
}

const getCampusLabel = (campus) => {
  return campusMap[campus] || '未知'
}

const getStatusLabel = (status) => {
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  return statusTypeMap[status] || 'info'
}

const applyLocalFilter = (list) => {
  return list.filter((item) => {
    if (searchForm.activityType && item.activityType !== searchForm.activityType) {
      return false
    }
    if (searchForm.campus && item.campus !== searchForm.campus) {
      return false
    }
    if (searchForm.status && item.status !== searchForm.status) {
      return false
    }
    return true
  })
}

const buildQueryFromState = () => {
  const query = {
    page: currentPage.value,
    size: pageSize.value
  }

  if (searchForm.activityType) {
    query.activityType = searchForm.activityType
  }
  if (searchForm.campus) {
    query.campus = searchForm.campus
  }
  if (searchForm.status) {
    query.status = searchForm.status
  }

  return query
}

const applyStateFromRoute = () => {
  const q = route.query

  if (q.page) {
    const pageNum = Number(q.page)
    if (!Number.isNaN(pageNum) && pageNum > 0) {
      currentPage.value = pageNum
    }
  }

  if (q.size) {
    const sizeNum = Number(q.size)
    if (!Number.isNaN(sizeNum) && sizeNum > 0) {
      pageSize.value = sizeNum
    }
  }

  if (typeof q.activityType === 'string') {
    searchForm.activityType = q.activityType
  }
  if (typeof q.campus === 'string') {
    searchForm.campus = q.campus
  }
  if (typeof q.status === 'string') {
    searchForm.status = q.status
  }
}

const syncRouteWithState = () => {
  const query = buildQueryFromState()
  router.replace({ path: route.path, query })
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...(searchForm.activityType && { activityType: searchForm.activityType }),
      ...(searchForm.campus && { campus: searchForm.campus }),
      ...(searchForm.status && { status: searchForm.status })
    }
    const response = await orderStore.getOrders(params)
    // 后端返回 ApiResponse<Object>，其中 data 为分页结果
    const serverData = response.data?.data || {}
    let list = serverData.list || []

    const hasFilter = !!(searchForm.activityType || searchForm.campus || searchForm.status)
    if (hasFilter) {
      list = applyLocalFilter(list)
    }

    ordersList.value = list
    total.value = hasFilter ? list.length : serverData.total
  } catch (error) {
    ElMessage.error(error.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  syncRouteWithState()
  fetchOrders()
}

const resetForm = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  syncRouteWithState()
  fetchOrders()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  syncRouteWithState()
  fetchOrders()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchOrders()
}

const handleViewDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

const handleCreateOrder = () => {
  router.push('/orders/create')
}

const handleApplyOrder = async (orderId) => {
  try {
    await orderStore.applyOrder(orderId)
    ElMessage.success('申请成功')
    fetchOrders()
  } catch (error) {
    ElMessage.error(error.message || '申请失败')
  }
}

onMounted(() => {
  applyStateFromRoute()
  syncRouteWithState()
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.filter-select {
  width: 130px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
