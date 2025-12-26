<template>
  <div class="home-container">
    <!-- 顶部：当前用户信息 + 发布动态 -->
    <el-card class="create-content-card" shadow="hover">
      <div class="user-info">
        <el-avatar :src="userAvatar" size="large" class="user-avatar">
          <span>{{ userInitial }}</span>
        </el-avatar>
        <div class="user-meta">
          <div class="user-name">{{ userNickname }}</div>
          <div class="user-sub" v-if="userSubtitle">{{ userSubtitle }}</div>
        </div>
      </div>
      <el-input
        v-model="contentText"
        placeholder="分享你的动态..."
        type="textarea"
        :rows="2"
        maxlength="200"
        show-word-limit
      />
      <div class="content-actions">
        <div class="action-buttons">
          <el-button type="primary" @click="handleCreateContent">发布动态</el-button>
          <el-button @click="handleCreateOrder" type="success" plain>发布订单</el-button>
        </div>
      </div>
    </el-card>

    <!-- 中部：订单状态总览（我发布的 / 我申请的） -->
    <div class="order-status-section">
      <el-row :gutter="16">
        <!-- 我发布的订单 -->
        <el-col :xs="24" :md="12">
          <el-card class="order-panel" shadow="hover">
            <template #header>
              <div class="panel-header">
                <span>我发布的订单</span>
                <span class="panel-sub" v-if="currentUserId">共 {{ myPublishedOrders.length }} 条</span>
              </div>
            </template>
            <el-skeleton v-if="ordersLoading" :rows="3" animated />
            <el-empty
              v-else-if="!currentUserId || myPublishedOrders.length === 0"
              :description="currentUserId ? '暂未发布订单' : '请登录后查看'"
            />
            <div v-else class="order-list">
              <div
                v-for="order in myPublishedOrders"
                :key="order.id"
                class="order-item"
                @click="goOrderDetail(order.id)"
              >
                <div class="order-main">
                  <div class="order-title">
                    {{ getActivityTypeLabel(order.activityType) }} · {{ order.location }}
                  </div>
                  <div class="order-meta">
                    <span>{{ formatDateTime(order.startTime, false) }}</span>
                    <span>{{ order.currentPeople }}/{{ order.maxPeople }} 人</span>
                  </div>
                </div>
                <el-tag :type="getStatusType(order.status)" size="small">
                  {{ getStatusLabel(order.status) }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 我申请的订单 -->
        <el-col :xs="24" :md="12">
          <el-card class="order-panel" shadow="hover">
            <template #header>
              <div class="panel-header">
                <span>我申请的订单</span>
                <span class="panel-sub" v-if="currentUserId">共 {{ myAppliedOrders.length }} 条</span>
              </div>
            </template>
            <el-skeleton v-if="ordersLoading" :rows="3" animated />
            <el-empty
              v-else-if="!currentUserId || myAppliedOrders.length === 0"
              :description="currentUserId ? '暂未申请任何订单' : '请登录后查看'"
            />
            <div v-else class="order-list">
              <div
                v-for="item in myAppliedOrders"
                :key="item.order.id"
                class="order-item"
                @click="goOrderDetail(item.order.id)"
              >
                <div class="order-main">
                  <div class="order-title">
                    {{ getActivityTypeLabel(item.order.activityType) }} · {{ item.order.location }}
                  </div>
                  <div class="order-meta">
                    <span>{{ formatDateTime(item.order.startTime, false) }}</span>
                    <span>{{ item.order.currentPeople }}/{{ item.order.maxPeople }} 人</span>
                  </div>
                </div>
                <div class="order-tags">
                  <el-tag :type="getStatusType(item.order.status)" size="small">
                    {{ getStatusLabel(item.order.status) }}
                  </el-tag>
                  <el-tag :type="getApplyStatusType(item.applyStatus)" size="small" effect="plain">
                    {{ getApplyStatusLabel(item.applyStatus) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 底部：动态流 -->
    <div class="content-list">
      <el-card v-for="content in contents" :key="content.id" class="content-item">
        <div class="content-header">
          <div class="user-info">
            <el-avatar :src="content.user.avatarUrl" size="small">
              <span>{{ getUserInitial(content.user) }}</span>
            </el-avatar>
            <div class="user-name">{{ content.user.nickname }}</div>
          </div>
          <div class="content-time">{{ content.createdAt }}</div>
        </div>
        <div class="content-body">
          <div class="content-text">{{ content.content }}</div>
          <div class="content-media" v-if="content.media && content.media.length">
            <img v-for="(media, index) in content.media" :key="index" :src="media.url" class="media-item" />
          </div>
        </div>
        <div class="content-footer">
          <div class="action-item" @click="handleLike(content.id)">
            <el-icon v-if="content.liked"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
            <span>{{ content.likeCount }}</span>
          </div>
          <div class="action-item" @click="handleComment(content.id)">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ content.commentCount }}</span>
          </div>
        </div>
      </el-card>
    </div>

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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled, Star, ChatDotRound } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useContentStore } from '../stores/content'
import { useOrderStore } from '../stores/order'

const router = useRouter()
const authStore = useAuthStore()
const contentStore = useContentStore()
const orderStore = useOrderStore()

const contentText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const contents = ref([])

const userAvatar = ref('')
const userNickname = ref('')
const userInitial = ref('用')
const userSubtitle = ref('')

// 订单概览数据
const ordersLoading = ref(false)
const myPublishedOrders = ref([])
// { order, applyStatus, applyCreatedAt }
const myAppliedOrders = ref([])

// 当前登录用户 ID
const currentUserId = computed(() => {
  return authStore.user?.id || Number(localStorage.getItem('userId')) || null
})

// 静态资源基地址，与其他页面保持一致
const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'
const resolveAvatarUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

// 活动 / 校区 / 状态 映射，复用订单页的枚举展示
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

const applyStatusMap = {
  PENDING_REVIEW: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  CANCELLED_APPLY: '已取消'
}

const applyStatusTypeMap = {
  PENDING_REVIEW: 'info',
  APPROVED: 'success',
  REJECTED: 'danger',
  CANCELLED_APPLY: 'warning'
}

const getActivityTypeLabel = (type) => activityTypeMap[type] || '未知'
const getStatusLabel = (status) => statusMap[status] || '未知'
const getStatusType = (status) => statusTypeMap[status] || 'info'
const getApplyStatusLabel = (status) => applyStatusMap[status] || '未知'
const getApplyStatusType = (status) => applyStatusTypeMap[status] || 'info'

const formatDateTime = (value, withSeconds = false) => {
  if (!value) return ''
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

const fetchContents = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    const response = await contentStore.getContents(params)
    const serverData = response.data.data || {}
    let list = serverData.list || []

    // 解析头像与媒体地址
    list = list.map((item) => {
      if (item.user && item.user.avatarUrl) {
        item.user.avatarUrl = resolveAvatarUrl(item.user.avatarUrl)
      }
      if (Array.isArray(item.media)) {
        item.media = item.media.map((m) => ({
          ...m,
          url: resolveAvatarUrl(m.url)
        }))
      }
      return item
    })

    contents.value = list
    total.value = serverData.total || list.length
  } catch (error) {
    ElMessage.error(error.message || '获取动态列表失败')
  }
}

// 首页订单概览：从订单列表与申请列表中筛选当前用户相关信息
const fetchOrderOverview = async () => {
  if (!currentUserId.value) {
    myPublishedOrders.value = []
    myAppliedOrders.value = []
    return
  }

  ordersLoading.value = true
  try {
    const params = {
      page: 1,
      size: 30
    }
    const response = await orderStore.getOrders(params)
    const serverData = response.data?.data || {}
    let list = serverData.list || []

    // 解析发布者头像
    list = list.map((item) => {
      if (item.user && item.user.avatarUrl) {
        item.user.avatarUrl = resolveAvatarUrl(item.user.avatarUrl)
      }
      return item
    })

    // 我发布的订单（按创建时间倒序）
    const published = list
      .filter((item) => item.user && item.user.id === currentUserId.value)
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    myPublishedOrders.value = published

    // 我申请的订单：需要从每个订单的申请列表中查找自己
    const appliedTemp = []
    const tasks = list.map(async (item) => {
      try {
        const res = await orderStore.getApplications(item.id)
        const apps = res.data?.data || []
        const mine = apps.find((a) => a.user && a.user.id === currentUserId.value && a.status !== 'CANCELLED_APPLY')
        if (mine) {
          appliedTemp.push({
            order: item,
            applyStatus: mine.status,
            applyCreatedAt: mine.createdAt
          })
        }
      } catch (e) {
        // 单个订单申请列表拉取失败时忽略，避免影响整体
        console.error('获取订单申请列表失败', item.id, e)
      }
    })

    await Promise.all(tasks)

    // 按申请时间倒序
    myAppliedOrders.value = appliedTemp.sort(
      (a, b) => new Date(b.applyCreatedAt).getTime() - new Date(a.applyCreatedAt).getTime()
    )
  } catch (error) {
    console.error('获取订单概览失败', error)
    ElMessage.error(error.message || '获取订单概览失败')
  } finally {
    ordersLoading.value = false
  }
}

const handleCreateContent = async () => {
  if (!contentText.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  try {
    await contentStore.createContent({
      content: contentText.value
    })
    ElMessage.success('发布成功')
    contentText.value = ''
    fetchContents()
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  }
}

const handleCreateOrder = () => {
  router.push('/orders/create')
}

const handleLike = async (contentId) => {
  try {
    await contentStore.likeContent(contentId)
    fetchContents()
  } catch (error) {
    ElMessage.error(error.message || '点赞失败')
  }
}

const handleComment = (contentId) => {
  router.push(`/contents/${contentId}`)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchContents()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchContents()
}

const goOrderDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

onMounted(() => {
  const user = authStore.user || {}
  userAvatar.value = user.avatarUrl ? resolveAvatarUrl(user.avatarUrl) : ''
  userNickname.value = user.nickname || '用户'
  userInitial.value = user.nickname ? user.nickname.slice(0, 1) : '用'
  userSubtitle.value = user.email || ''

  fetchContents()
  fetchOrderOverview()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.create-content-card {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.user-name {
  font-weight: 600;
  font-size: 16px;
}

.user-subtitle,
.user-sub {
  font-size: 12px;
  color: #909399;
}

.content-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-item {
  transition: all 0.3s;
}

.content-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.order-status-section {
  margin-bottom: 20px;
}

.order-panel {
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}

.panel-sub {
  font-size: 12px;
  color: #909399;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s ease, transform 0.15s ease;
}

.order-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-1px);
}

.order-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.order-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.order-tags {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.content-time {
  color: #909399;
  font-size: 12px;
}

.content-body {
  margin-bottom: 10px;
}

.content-text {
  margin-bottom: 10px;
}

.content-media {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.media-item {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 4px;
}

.content-footer {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #606266;
}

.action-item:hover {
  color: #409eff;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .home-container {
    padding: 12px;
  }

  .order-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-tags {
    flex-direction: row;
  }
}
</style>
