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
          <el-select v-model="searchForm.activityType" placeholder="请选择">
            <el-option label="篮球" value="0" />
            <el-option label="羽毛球" value="1" />
            <el-option label="吃饭" value="2" />
            <el-option label="自习" value="3" />
            <el-option label="看电影" value="4" />
            <el-option label="跑步" value="5" />
            <el-option label="游戏" value="6" />
            <el-option label="其他" value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="校区">
          <el-select v-model="searchForm.campus" placeholder="请选择">
            <el-option label="良乡校区" value="0" />
            <el-option label="中关村校区" value="1" />
            <el-option label="珠海校区" value="2" />
            <el-option label="西山校区" value="3" />
            <el-option label="其他校区" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择">
            <el-option label="待匹配" value="0" />
            <el-option label="进行中" value="1" />
            <el-option label="已完成" value="2" />
            <el-option label="已取消" value="3" />
            <el-option label="已过期" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table v-loading="loading" :data="ordersList" style="width: 100%">
        <el-table-column prop="id" label="订单ID" width="80" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '../stores/order'

const router = useRouter()
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
  0: '篮球',
  1: '羽毛球',
  2: '吃饭',
  3: '自习',
  4: '看电影',
  5: '跑步',
  6: '游戏',
  7: '其他'
}

// 校区映射
const campusMap = {
  0: '良乡校区',
  1: '中关村校区',
  2: '珠海校区',
  3: '西山校区',
  4: '其他校区'
}

// 订单状态映射
const statusMap = {
  0: '待匹配',
  1: '进行中',
  2: '已完成',
  3: '已取消',
  4: '已过期'
}

// 订单状态标签类型
const statusTypeMap = {
  0: 'info',
  1: 'success',
  2: 'success',
  3: 'warning',
  4: 'danger'
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
    ordersList.value = response.data.data.list
    total.value = response.data.data.total
  } catch (error) {
    ElMessage.error(error.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

const resetForm = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchOrders()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
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
