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
            <el-checkbox v-model="onlyMine" :disabled="loading">
              我发布的
            </el-checkbox>
          </el-form-item>
        <el-form-item>
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
              <el-avatar :src="scope.row.user.avatarUrl" size="small">
                <span v-if="!scope.row.user.avatarUrl">
                  {{ (scope.row.user.nickname || '用').slice(0, 1) }}
                </span>
              </el-avatar>
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
        <el-table-column width="240">
          <template #header>
            <div class="status-header">
              <span>状态</span>
              <el-tooltip effect="light" placement="top" popper-class="status-tooltip-popper">
                <template #content>
                  <div class="status-tooltip-content">
                    <div><strong>待匹配：</strong>订单发布后，参与人数尚未达到上限，且当前时间未超过订单开始时间</div>
                    <div><strong>进行中：</strong>订单参与人数已达到上限，且当前时间未超过订单开始时间</div>
                    <div><strong>已完成：</strong>订单参与人数已达到上限，匹配成功，且当前时间已超过订单开始时间</div>
                    <div><strong>已过期：</strong>订单参与人数未达到上限，且当前时间已超过订单开始时间（即到时间未完成匹配）</div>
                    <div><strong>已取消：</strong>由订单发布者主动取消</div>
                  </div>
                </template>
                <InfoFilled class="status-info-icon" />
              </el-tooltip>
            </div>
          </template>
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
            <el-button
              type="success"
              size="small"
              :disabled="isApplyDisabled(scope.row)"
              @click="handleApplyOrder(scope.row)"
            >
              {{ getApplyButtonText(scope.row) }}
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { useOrderStore } from '../stores/order'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()
const authStore = useAuthStore()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const ordersList = ref([])
// 记录当前用户已申请的订单，key 为 orderId
const appliedOrderMap = ref({})

// 仅查看我发布的订单
const onlyMine = ref(false)

const searchForm = reactive({
  activityType: '',
  campus: '',
  status: ''
})

// 当前登录用户 ID
const currentUserId = computed(() => {
  return authStore.user?.id || Number(localStorage.getItem('userId')) || null
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

// 与用户 store 保持一致，将后端返回的相对头像路径转换为完整 URL
const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'
const resolveAvatarUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

// 判断是否是当前用户发布的订单
const isPublisher = (order) => {
  if (!currentUserId.value || !order?.user) return false
  return order.user.id === currentUserId.value
}

// 判断当前用户是否已申请过该订单
const hasApplied = (order) => {
  if (!order?.id) return false
  return !!appliedOrderMap.value[order.id]
}

// 申请按钮是否应禁用
const isApplyDisabled = (order) => {
  if (!order) return true

  // 发布者：仅在订单未取消且为待匹配状态时允许“取消订单”
  if (isPublisher(order)) {
    if (order.status === 'CANCELLED') return true
    return order.status !== 'PENDING' ? true : false
  }

  // 非发布者：已申请、非待匹配状态都不允许再申请
  if (hasApplied(order)) return true
  if (order.status !== 'PENDING') return true
  // 人数已满时不允许继续申请
  if (order.currentPeople >= order.maxPeople) return true
  return false
}

// 申请按钮文案
const getApplyButtonText = (order) => {
  if (!order) return '申请'

  if (isPublisher(order)) {
    if (order.status === 'CANCELLED') return '已取消'
    if (order.status !== 'PENDING') return '不可操作'
    return '取消订单'
  }

  if (hasApplied(order)) return '已申请'
  if (order.status !== 'PENDING') return '不可申请'
  if (order.currentPeople >= order.maxPeople) return '人数已满'
  return '申请'
}

// 加载某一页订单中，当前用户是否已申请
const loadAppliedInfoForOrders = async (list) => {
  const userId = currentUserId.value
  if (!userId || !Array.isArray(list) || list.length === 0) return

  try {
    const tasks = list.map(async (item) => {
      if (!item?.id) return
      try {
        const res = await orderStore.getApplications(item.id)
        const apps = res.data?.data || []
          // 只把仍然有效、未撤销的申请视为“已申请”
          const applied = apps.some(app => {
            if (!app.user || app.user.id !== userId) return false
            // 后端取消申请后状态为 CANCELLED_APPLY，不再算作已申请
            return app.status !== 'CANCELLED_APPLY'
          })
        if (applied) {
          appliedOrderMap.value[item.id] = true
        }
      } catch (e) {
        // 单个订单申请列表出错时忽略，避免影响整体
        console.error('获取申请列表失败', e)
      }
    })
    await Promise.all(tasks)
  } catch (e) {
    console.error('批量获取申请信息失败', e)
  }
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
    if (onlyMine.value && !isPublisher(item)) {
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

  if (onlyMine.value) {
    query.onlyMine = '1'
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

  if (q.onlyMine === '1' || q.onlyMine === 'true') {
    onlyMine.value = true
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
    // 每次查询前清空已申请信息
    appliedOrderMap.value = {}

    const response = await orderStore.getOrders(params)
    // 后端返回 ApiResponse<Object>，其中 data 为分页结果
    const serverData = response.data?.data || {}
    let list = serverData.list || []

    // 解析用户头像 URL（如果有）
    list = list.map((item) => {
      if (item.user && item.user.avatarUrl) {
        item.user.avatarUrl = resolveAvatarUrl(item.user.avatarUrl)
      }
      return item
    })

    const hasFilter = !!(searchForm.activityType || searchForm.campus || searchForm.status || onlyMine.value)
    if (hasFilter) {
      list = applyLocalFilter(list)
    }

    // 加载当前用户是否已申请过这些订单的信息
    await loadAppliedInfoForOrders(list)

    // 订单可见性控制：已取消订单仅发布者和曾申请者可见
    const visibleList = list.filter((item) => {
      if (item.status !== 'CANCELLED') return true
      return isPublisher(item) || hasApplied(item)
    })

    ordersList.value = visibleList
    total.value = hasFilter ? visibleList.length : serverData.total
  } catch (error) {
    ElMessage.error(error.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  onlyMine.value = false
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

const handleApplyOrder = async (order) => {
  if (!order?.id) return
  
  // 发布者点击：执行“取消订单”逻辑
  if (isPublisher(order)) {
    if (order.status === 'CANCELLED') return
    try {
      await ElMessageBox.confirm(
        '确定要取消此订单吗？此操作不可撤销。',
        '取消订单确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          draggable: true
        }
      )
      await orderStore.deleteOrder(order.id)
      ElMessage.success('订单已取消')
      // 重新加载列表，刷新状态与可见性
      fetchOrders()
    } catch (error) {
      if (error === 'cancel' || error === 'close') {
        return
      }
      ElMessage.error(error?.message || '取消订单失败')
    }
    return
  }

  // 非发布者：执行申请逻辑
  if (isApplyDisabled(order)) return

  try {
    await orderStore.applyOrder(order.id)
    ElMessage.success('申请成功')
    // 前端直接标记为已申请，避免再次点击
    appliedOrderMap.value[order.id] = true
    // 重新拉取列表数据，刷新人数等信息
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

// 监听筛选条件，自动刷新列表
watch(
  () => ({
    activityType: searchForm.activityType,
    campus: searchForm.campus,
    status: searchForm.status,
    onlyMine: onlyMine.value
  }),
  () => {
    currentPage.value = 1
    syncRouteWithState()
    fetchOrders()
  }
)
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

.status-header {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
.status-info-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  width: 16px;
  height: 16px;
  color: #909399;
  cursor: pointer;
}
/* ensure the svg inside the icon scales down */
.status-info-icon svg {
  width: 14px;
  height: 14px;
}
.status-tooltip-content {
  max-width: 360px;
  font-size: 13px;
  line-height: 1.5;
  color: #303133;
}
.status-tooltip-content div {
  margin-bottom: 6px;
}
</style>

/* Tooltip popper styling needs to be global because Element Plus mounts popper to body */
<style>
.status-tooltip-popper {
  background-color: #f5f7fa !important;
  color: #303133 !important;
  padding: 12px !important;
  border-radius: 8px !important;
  box-shadow: 0 6px 18px rgba(0,0,0,0.08) !important;
  max-width: 420px !important;
}
.status-tooltip-popper .status-tooltip-content {
  color: inherit !important;
}
</style>
