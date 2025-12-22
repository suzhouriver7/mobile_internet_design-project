<template>
  <div class="home-container">
    <el-card class="create-content-card">
      <div class="user-info">
        <el-avatar :src="userAvatar" size="large" />
        <div class="user-name">{{ userNickname }}</div>
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
          <el-button type="primary" @click="handleCreateContent">发布</el-button>
          <el-button @click="handleCreateOrder">发布订单</el-button>
        </div>
      </div>
    </el-card>

    <div class="content-list">
      <el-card v-for="content in contents" :key="content.id" class="content-item">
        <div class="content-header">
          <div class="user-info">
            <el-avatar :src="content.user.avatarUrl" size="small" />
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled, Star, ChatDotRound } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useContentStore } from '../stores/content'

const router = useRouter()
const authStore = useAuthStore()
const contentStore = useContentStore()

const contentText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const contents = ref([])

const userAvatar = ref('')
const userNickname = ref('')

const fetchContents = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    const response = await contentStore.getContents(params)
    contents.value = response.data.data.list
    total.value = response.data.data.total
  } catch (error) {
    ElMessage.error(error.message || '获取动态列表失败')
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

onMounted(() => {
  userAvatar.value = authStore.user?.avatarUrl || ''
  userNickname.value = authStore.user?.nickname || '用户'
  fetchContents()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.create-content-card {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.user-name {
  font-weight: bold;
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
</style>
