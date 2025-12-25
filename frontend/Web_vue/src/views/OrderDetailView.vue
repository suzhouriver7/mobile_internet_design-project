<template>
  <div class="order-detail-view">
    <!-- 顶部返回与标题 -->
    <div class="detail-header">
      <el-button type="primary" link @click="handleBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回订单列表</span>
      </el-button>
      <h2 class="detail-title">订单详情</h2>
    </div>

    <!-- 加载骨架屏 -->
    <el-skeleton v-if="loading" :rows="6" animated class="detail-skeleton" />

    <!-- 内容区域 -->
    <div v-else-if="detail" class="detail-content">
      <el-row :gutter="20" class="detail-main-row">
        <!-- 左侧：发布者信息 + 订单信息 -->
        <el-col :xs="24" :md="16">
          <!-- 发布者信息 -->
          <el-card class="detail-card" shadow="always">
            <template #header>
              <div class="card-header">
                <span>发布者信息</span>
              </div>
            </template>
            <div class="publisher-info">
              <div class="publisher-avatar">
                <img
                  v-if="publisherAvatarUrl"
                  :src="publisherAvatarUrl"
                  alt="avatar"
                  class="avatar-img avatar-80"
                  loading="lazy"
                />
                <div v-else class="avatar-placeholder avatar-80">
                  {{ publisherInitial }}
                </div>
              </div>
              <div class="publisher-meta">
                <div class="publisher-name-row">
                  <span class="publisher-name">{{ publisher?.nickname || '未知用户' }}</span>
                  <el-tag size="small" type="info" v-if="publisher">
                    {{ publisherTypeLabel }}
                  </el-tag>
                </div>
                <div class="publisher-extra" v-if="publisher">
                  <span>用户ID：{{ publisher.id }}</span>
                </div>
                <div class="publisher-extra" v-if="order">
                  <span>订单ID：{{ order.id }}</span>
                  <span>发布时间：{{ formatDateTime(order.createdAt, true) }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 订单要求信息 -->
          <el-card class="detail-card" shadow="always">
            <template #header>
              <div class="card-header">
                <span>订单信息</span>
              </div>
            </template>
            <div class="order-info-grid">
              <div class="info-item">
                <span class="label">活动类型</span>
                <span class="value">
                  <el-tag type="primary" effect="light">
                    {{ getActivityTypeLabel(order.activityType) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="label">性别要求</span>
                <span class="value">{{ getGenderLabel(order.genderRequire) }}</span>
              </div>
              <div class="info-item">
                <span class="label">校区</span>
                <span class="value">
                  <el-tag type="success" effect="light">
                    {{ getCampusLabel(order.campus) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="label">地点</span>
                <span class="value">{{ order.location }}</span>
              </div>
              <div class="info-item">
                <span class="label">开始时间</span>
                <span class="value">{{ formatDateTime(order.startTime, true) }}</span>
              </div>
              <div class="info-item">
                <span class="label">状态</span>
                <span class="value">
                  <el-tag :type="getStatusType(order.status)">
                    {{ getStatusLabel(order.status) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="label">人数</span>
                <span class="value highlight">{{ order.currentPeople }}/{{ order.maxPeople }}</span>
              </div>
              <div class="info-item">
                <span class="label">创建时间</span>
                <span class="value">{{ formatDateTime(order.createdAt, true) }}</span>
              </div>
              <div class="info-item">
                <span class="label">更新时间</span>
                <span class="value">{{ formatDateTime(order.updatedAt, true) }}</span>
              </div>
            </div>
            <div class="note-block" v-if="order.note">
              <div class="note-label">备注说明</div>
              <div class="note-content">{{ order.note }}</div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：申请信息 -->
        <el-col :xs="24" :md="8">
          <el-card class="detail-card" shadow="always">
            <template #header>
              <div class="card-header">
                <span>申请信息</span>
                <span class="apply-count">共 {{ applications.length }} 人申请</span>
              </div>
            </template>
            <div v-if="applications.length > 0" class="applications-list">
              <div
                v-for="apply in applications"
                :key="apply.id"
                class="apply-item"
              >
                <div class="apply-avatar">
                  <img
                    v-if="apply.user && apply.user.avatarUrl"
                    :src="apply.user.avatarUrl"
                    alt="avatar"
                    class="avatar-img avatar-40"
                    loading="lazy"
                  />
                  <div v-else class="avatar-placeholder avatar-40">
                    {{ getUserInitial(apply.user) }}
                  </div>
                </div>
                <div class="apply-meta">
                  <div class="apply-name-row">
                    <span class="apply-name">{{ apply.user?.nickname || '未知用户' }}</span>
                    <el-tag size="small" type="info">{{ getApplyStatusLabel(apply.status) }}</el-tag>
                  </div>
                  <div class="apply-time">
                    申请时间：{{ formatDateTime(apply.createdAt, false) }}
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂时还没有人申请" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else description="未找到该订单" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useOrderStore } from '../stores/order'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

const loading = ref(false)

// 映射表与列表页保持一致
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

const campusMap = {
  LIANGXIANG: '良乡校区',
  ZHONGGUANCUN: '中关村校区',
  ZHUHAI: '珠海校区',
  XISHAN: '西山校区',
  OTHER_CAMPUS: '其他校区'
}

const statusMap = {
  PENDING: '待匹配',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  EXPIRED: '已过期'
}

const statusTypeMap = {
  PENDING: 'info',
  IN_PROGRESS: 'success',
  COMPLETED: 'success',
  CANCELLED: 'warning',
  EXPIRED: 'danger'
}

const genderMap = {
  MALE: '男',
  FEMALE: '女',
  ANY: '不限'
}

const applyStatusMap = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  CANCELLED: '已取消'
}

// 静态资源基地址，与用户头像逻辑保持一致
const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'
const resolveAvatarUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const detail = computed(() => orderStore.currentOrder)
const order = computed(() => detail.value?.order || null)
const publisher = computed(() => order.value?.user || null)
const applications = computed(() => detail.value?.applications || [])

const publisherAvatarUrl = computed(() => {
  if (!publisher.value?.avatarUrl) return ''
  return resolveAvatarUrl(publisher.value.avatarUrl)
})

const publisherInitial = computed(() => {
  if (publisher.value?.nickname) {
    return publisher.value.nickname.slice(0, 1)
  }
  return '用'
})

const publisherTypeLabel = computed(() => {
  const user = publisher.value
  if (!user) return '普通用户'
  if (user.userType === 1 || user.userType === 'ADMIN') return '管理员'
  return '普通用户'
})

const getActivityTypeLabel = (type) => activityTypeMap[type] || '未知'
const getCampusLabel = (campus) => campusMap[campus] || '未知'
const getStatusLabel = (status) => statusMap[status] || '未知'
const getStatusType = (status) => statusTypeMap[status] || 'info'
const getGenderLabel = (gender) => genderMap[gender] || '不限'
const getApplyStatusLabel = (status) => applyStatusMap[status] || '待审核'

const formatDateTime = (value, withSeconds = false) => {
  if (!value) return ''
  // 后端格式 yyyy-MM-dd HH:mm:ss
  if (typeof value === 'string') {
    return withSeconds ? value.slice(0, 19) : value.slice(0, 16)
  }
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => (n < 10 ? `0${n}` : `${n}`)
  const year = d.getFullYear()
  const month = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const hour = pad(d.getHours())
  const minute = pad(d.getMinutes())
  const second = pad(d.getSeconds())
  return withSeconds
    ? `${year}-${month}-${day} ${hour}:${minute}:${second}`
    : `${year}-${month}-${day} ${hour}:${minute}`
}

const getUserInitial = (user) => {
  if (user?.nickname) return user.nickname.slice(0, 1)
  return '用'
}

const normalizeDetail = () => {
  const d = orderStore.currentOrder
  if (!d) return

  // 处理发布者头像
  if (d.order && d.order.user && d.order.user.avatarUrl) {
    d.order.user.avatarUrl = resolveAvatarUrl(d.order.user.avatarUrl)
  }

  // 处理申请人头像，并按申请时间倒序
  if (Array.isArray(d.applications)) {
    d.applications.forEach((a) => {
      if (a.user && a.user.avatarUrl) {
        a.user.avatarUrl = resolveAvatarUrl(a.user.avatarUrl)
      }
    })
    d.applications.sort((a, b) => {
      const ta = new Date(a.createdAt).getTime()
      const tb = new Date(b.createdAt).getTime()
      return Number.isNaN(tb - ta) ? 0 : tb - ta
    })
  }
}

const loadDetail = async () => {
  const id = route.params.id
  if (!id) return
  try {
    loading.value = true
    await orderStore.getOrderDetail(id)
    normalizeDetail()
  } catch (error) {
    console.error('加载订单详情失败', error)
    ElMessage.error(error?.message || '加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  // 使用浏览器历史返回，保留列表页面的筛选和滚动位置
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.order-detail-view {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.detail-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.detail-skeleton {
  margin-top: 8px;
}

.detail-content {
  margin-top: 4px;
}

.detail-main-row {
  margin-top: 4px;
}

.detail-card {
  margin-bottom: 16px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.detail-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 500;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.publisher-avatar {
  flex-shrink: 0;
}

.avatar-img {
  display: block;
  object-fit: cover;
}

.avatar-80 {
  width: 80px;
  height: 80px;
  border-radius: 50%;
}

.avatar-40 {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;
  border-radius: 50%;
}

.publisher-meta {
  flex: 1;
  min-width: 0;
}

.publisher-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.publisher-name {
  font-size: 16px;
  font-weight: 600;
}

.publisher-extra {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.order-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px 16px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.label {
  font-size: 12px;
  color: #909399;
}

.value {
  font-size: 14px;
  color: #303133;
}

.highlight {
  font-weight: 600;
  color: #409eff;
}

.note-block {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

.note-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.note-content {
  font-size: 14px;
  color: #303133;
  white-space: pre-wrap;
}

.apply-count {
  font-size: 12px;
  color: #909399;
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.apply-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.apply-avatar {
  flex-shrink: 0;
}

.apply-meta {
  flex: 1;
  min-width: 0;
}

.apply-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.apply-name {
  font-size: 14px;
  font-weight: 500;
}

.apply-time {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .publisher-info {
    align-items: flex-start;
  }

  .detail-card {
    margin-bottom: 12px;
  }
}
</style>
