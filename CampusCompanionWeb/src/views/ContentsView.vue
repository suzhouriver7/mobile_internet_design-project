<template>
  <div class="contents-view">
    <div class="header">
      <div class="header-left">
        <h2>动态广场</h2>
        <span class="subtitle">浏览最新 / 最热的校园动态</span>
      </div>
      <el-button type="primary" @click="goToCreate">发布动态</el-button>
    </div>

    <el-form :inline="true" :model="filters" class="filter-form">
      <el-form-item label="排序">
        <el-select v-model="filters.sort" style="width: 160px">
          <el-option label="最新发布" value="latest" />
          <el-option label="热门优先" value="hot" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-skeleton v-if="loading" :rows="4" animated />

    <div v-else class="content-list">
      <el-empty v-if="contentsList.length === 0" description="暂无动态" />
      <transition-group name="list-fade" tag="div">
        <el-card
          v-for="item in sortedContents"
          :key="item.id"
          class="content-card"
          shadow="hover"
          @click="goToDetail(item)"
        >
          <div class="card-header">
            <div class="user-info">
              <el-avatar
                :size="40"
                :src="resolveAvatarUrl(item.user?.avatarUrl)"
                class="user-avatar"
              >
                <span>{{ (item.user?.nickname || '用').slice(0, 1) }}</span>
              </el-avatar>
              <div class="user-meta">
                <div class="nickname">{{ item.user?.nickname || '用户' }}</div>
                <div class="time">{{ formatTime(item.createdAt) }}</div>
              </div>
            </div>
            <el-dropdown v-if="canDelete(item)" @command="(command) => handleMore(command, item)">
              <span class="el-dropdown-link" @click.stop>
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="delete" type="danger">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
            <div class="card-content">
            <p class="text">{{ item.content }}</p>

            <div
              v-if="isImageType(item.mediaType) && item.mediaUrls && item.mediaUrls.length"
              class="media-grid"
            >
              <el-image
                v-for="(url, index) in item.mediaUrls"
                :key="index"
                :src="resolveMediaUrl(url)"
                fit="cover"
                class="media-image"
                :preview-src-list="item.mediaUrls.map(resolveMediaUrl)"
                lazy
              />
            </div>

            <div
              v-if="isVideoType(item.mediaType) && item.mediaUrls && item.mediaUrls.length"
              class="media-video"
            >
              <video
                v-for="(url, index) in item.mediaUrls"
                :key="index"
                :src="resolveMediaUrl(url)"
                controls
                class="video-player"
                preload="metadata"
              ></video>
            </div>

            <div v-if="item.order" class="order-info" @click.stop="goToOrder(item.order.id)">
              <div class="order-chip">
                <el-icon class="order-chip-icon"><Tickets /></el-icon>
                <span class="order-chip-text">订单 #{{ item.order.id }}</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <div class="left">
              <el-button
                text
                size="small"
                :type="item.liked ? 'primary' : 'default'"
                @click.stop="handleLike(item)"
              >
                <el-icon v-if="item.liked"><ThumbFilled /></el-icon>
                <el-icon v-else><ThumbOutline /></el-icon>
                <span>{{ item.liked ? '已点赞' : '点赞' }}（{{ item.likeCount || 0 }}）</span>
              </el-button>
              <el-button text size="small" @click.stop="handleComment(item)">
                <el-icon><ChatDotRound /></el-icon>
                <span>评论（{{ item.commentCount || 0 }}）</span>
              </el-button>
            </div>
          </div>
        </el-card>
      </transition-group>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ChatDotRound, Tickets } from '@element-plus/icons-vue'
import ThumbFilled from '../components/ThumbFilled.vue'
import ThumbOutline from '../components/ThumbOutline.vue'
import { useContentStore } from '../stores/content'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const contentStore = useContentStore()
const authStore = useAuthStore()

const contentsList = ref([])
const total = ref(0)
const loading = ref(false)

const filters = reactive({
  sort: 'latest'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'

const resolveMediaUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

// 兼容后端返回的媒体类型：既可能是数字 1/2，也可能是字符串 'IMAGE'/'VIDEO'
const isImageType = (mediaType) => mediaType === 1 || mediaType === 'IMAGE'
const isVideoType = (mediaType) => mediaType === 2 || mediaType === 'VIDEO'

const resolveAvatarUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const currentUser = computed(() => authStore.user)

const fetchContents = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }

    const response = await contentStore.getContents(params)
    const data = response.data?.data || response.data || {}
    contentsList.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取内容列表失败', error)
    ElMessage.error(error.response?.data?.message || '获取内容列表失败')
  } finally {
    loading.value = false
  }
}

const sortedContents = computed(() => {
  const list = [...contentsList.value]
  if (filters.sort === 'hot') {
    return list.sort((a, b) => {
      const la = a.likeCount || 0
      const lb = b.likeCount || 0
      if (lb !== la) return lb - la
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    })
  }
  return list.sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )
})

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  fetchContents()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  fetchContents()
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

const canDelete = (item) => {
  const user = currentUser.value
  if (!user) return false
  // 管理员可以删除任意动态
  if (user.userType === 1 || user.userType === 'ADMIN') return true
  // 否则仅作者本人可以删除
  return item.user && item.user.id === user.id
}

const handleMore = (command, item) => {
  if (command === 'delete') {
    handleDelete(item)
  }
}

const handleDelete = (item) => {
  ElMessageBox.confirm('确定要删除该动态吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await contentStore.deleteContent(item.id)
        ElMessage.success('删除成功')
        fetchContents()
      } catch (error) {
        console.error('删除失败', error)
        ElMessage.error(error.response?.data?.message || '删除失败')
      }
    })
    .catch(() => {})
}

const handleLike = async (item) => {
  try {
    const response = await contentStore.likeContent(item.id)
    const likeData = response.data?.data || {}
    if (Object.prototype.hasOwnProperty.call(likeData, 'liked')) {
      item.liked = likeData.liked
    }
    if (Object.prototype.hasOwnProperty.call(likeData, 'count')) {
      item.likeCount = likeData.count
    }
  } catch (error) {
    console.error('点赞失败', error)
    ElMessage.error(error.response?.data?.message || '点赞失败')
  }
}

const handleComment = (item) => {
  router.push(`/contents/${item.id}`)
}

const goToDetail = (item) => {
  router.push(`/contents/${item.id}`)
}

const goToOrder = (orderId) => {
  if (!orderId) return
  router.push(`/orders/${orderId}`)
}

const goToCreate = () => {
  router.push('/contents/create')
}

onMounted(() => {
  fetchContents()
})
</script>

<style scoped>
.contents-view {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.filter-form {
  margin-bottom: 16px;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-card {
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  cursor: pointer;
}

.content-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 10px;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: 600;
}

.time {
  font-size: 12px;
  color: #909399;
}

.card-content {
  margin-top: 12px;
}

.text {
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.media-image {
  width: 100%;
  height: 120px;
}

.media-video {
  margin-top: 8px;
}

.video-player {
  width: 100%;
  max-height: 360px;
}

.order-info {
  margin-top: 8px;
}

.order-chip {
  display: inline-flex;
  align-items: center;
  column-gap: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  background-color: var(--el-color-info-light-9, #ecf5ff);
  color: var(--el-color-info, #409eff);
  white-space: nowrap;
}

.order-chip-icon {
  font-size: 14px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.left {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.25s ease;
}

.list-fade-enter-from,
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@media (max-width: 768px) {
  .contents-view {
    padding: 12px;
  }
}
 </style>
