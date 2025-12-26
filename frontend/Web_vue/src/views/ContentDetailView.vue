<template>
  <div class="content-detail-view">
    <el-page-header @back="handleBack" content="动态详情" class="page-header" />

    <el-skeleton v-if="loading" :rows="6" animated />

    <template v-else>
      <el-card v-if="content" class="content-card" shadow="hover">
        <div class="card-header">
          <div class="user-info">
            <el-avatar
              :size="40"
              :src="resolveAvatarUrl(content.user?.avatarUrl)"
              class="user-avatar"
            >
              <span>{{ (content.user?.nickname || '用').slice(0, 1) }}</span>
            </el-avatar>
            <div class="user-meta">
              <div class="nickname">{{ content.user?.nickname || '用户' }}</div>
              <div class="time">{{ formatTime(content.createdAt) }}</div>
            </div>
          </div>
        </div>

        <div class="card-content">
          <p class="text">{{ content.content }}</p>

          <div
            v-if="content.mediaType === 1 && content.mediaUrls && content.mediaUrls.length"
            class="media-grid"
          >
            <el-image
              v-for="(url, index) in content.mediaUrls"
              :key="index"
              :src="resolveMediaUrl(url)"
              fit="cover"
              class="media-image"
              :preview-src-list="content.mediaUrls.map(resolveMediaUrl)"
              lazy
            />
          </div>

          <div
            v-if="content.mediaType === 2 && content.mediaUrls && content.mediaUrls.length"
            class="media-video"
          >
            <video
              v-for="(url, index) in content.mediaUrls"
              :key="index"
              :src="resolveMediaUrl(url)"
              controls
              class="video-player"
              preload="metadata"
            ></video>
          </div>
        </div>

        <div class="card-footer">
          <div class="left">
            <el-button
              text
              size="small"
              :type="content.liked ? 'primary' : 'default'"
              @click="handleLike"
            >
              <el-icon><Star /></el-icon>
              <span>{{ content.liked ? '已点赞' : '点赞' }}（{{ content.likeCount || 0 }}）</span>
            </el-button>
            <span class="stat">评论 {{ content.commentCount || 0 }}</span>
          </div>
        </div>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card" shadow="never">
        <template #header>
          <div class="comment-header">
            <span>评论</span>
            <span class="comment-count">{{ totalComments }} 条</span>
          </div>
        </template>

        <!-- 发表评论 -->
        <div class="comment-editor">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="友善发言，文明交流～ 支持换行和简单表情 😊"
          />
          <div class="comment-toolbar">
            <div class="emoji-bar">
              <span class="emoji" @click="appendEmoji('😊')">😊</span>
              <span class="emoji" @click="appendEmoji('😂')">😂</span>
              <span class="emoji" @click="appendEmoji('👍')">👍</span>
              <span class="emoji" @click="appendEmoji('❤️')">❤️</span>
            </div>
            <el-button
              type="primary"
              size="small"
              :loading="commentLoading"
              @click="handleSubmitComment"
            >
              发布评论
            </el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" v-if="comments.length">
          <comment-item
            v-for="item in comments"
            :key="item.id"
            :comment="item"
            :level="0"
            @reply="handleReply"
          />
        </div>
        <el-empty v-else description="快来抢沙发~" />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { useContentStore } from '../stores/content'
import { useAuthStore } from '../stores/auth'
import CommentItem from '../components/CommentItem.vue'

const route = useRoute()
const router = useRouter()
const contentStore = useContentStore()
const authStore = useAuthStore()

const contentId = Number(route.params.id)

const content = ref(null)
const loading = ref(false)

const comments = ref([])
const totalComments = ref(0)
const commentText = ref('')
const commentLoading = ref(false)
const replyParentId = ref(null)

const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'

const resolveMediaUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const resolveAvatarUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const resp = await contentStore.getContentDetail(contentId)
    content.value = resp.data?.data || resp.data || null
  } catch (error) {
    console.error('获取动态详情失败', error)
    ElMessage.error(error.response?.data?.message || '获取动态详情失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const resp = await contentStore.getComments(contentId, { page: 1, size: 200 })
    const data = resp.data?.data || resp.data || {}
    comments.value = data.list || []
    totalComments.value = data.total || comments.value.length
  } catch (error) {
    console.error('获取评论失败', error)
    ElMessage.error(error.response?.data?.message || '获取评论失败')
  }
}

const handleLike = async () => {
  if (!content.value) return
  try {
    const resp = await contentStore.likeContent(contentId)
    const likeData = resp.data?.data || {}
    if (Object.prototype.hasOwnProperty.call(likeData, 'liked')) {
      content.value.liked = likeData.liked
    }
    if (Object.prototype.hasOwnProperty.call(likeData, 'count')) {
      content.value.likeCount = likeData.count
    }
  } catch (error) {
    console.error('点赞失败', error)
    ElMessage.error(error.response?.data?.message || '点赞失败')
  }
}

const appendEmoji = (emoji) => {
  commentText.value += emoji
}

const handleSubmitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.info('请输入评论内容')
    return
  }
  if (!authStore.user?.id) {
    ElMessage.warning('请先登录后再评论')
    router.push('/login')
    return
  }

  commentLoading.value = true
  try {
    const payload = {
      content: commentText.value,
      parentId: replyParentId.value
    }
    await contentStore.createComment(contentId, payload)
    commentText.value = ''
    replyParentId.value = null
    ElMessage.success('评论发布成功')
    fetchComments()
  } catch (error) {
    console.error('发布评论失败', error)
    ElMessage.error(error.response?.data?.message || '发布评论失败')
  } finally {
    commentLoading.value = false
  }
}

const handleReply = (comment) => {
  replyParentId.value = comment.id
  commentText.value = `@${comment.user?.nickname || '用户'} `
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  fetchDetail()
  fetchComments()
})
</script>

<style scoped>
.content-detail-view {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.content-card {
  margin-bottom: 16px;
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
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
}

.media-image {
  width: 100%;
  height: 140px;
}

.media-video {
  margin-top: 8px;
}

.video-player {
  width: 100%;
  max-height: 400px;
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
  align-items: center;
}

.stat {
  font-size: 13px;
  color: #909399;
}

.comment-card {
  margin-top: 12px;
}

.comment-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.comment-count {
  font-size: 12px;
  color: #909399;
}

.comment-editor {
  margin-bottom: 16px;
}

.comment-toolbar {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.emoji-bar {
  display: flex;
  gap: 6px;
}

.emoji {
  cursor: pointer;
  font-size: 18px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #f2f3f5;
}

.comment-main {
  display: flex;
}

.comment-avatar {
  margin-right: 8px;
}

.comment-body {
  flex: 1;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.comment-meta .name {
  font-weight: 500;
}

.comment-meta .time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  margin-bottom: 4px;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-children {
  margin-left: 40px;
  margin-top: 6px;
}

@media (max-width: 768px) {
  .content-detail-view {
    padding: 12px;
  }

  .comment-children {
    margin-left: 28px;
  }
}
</style>
