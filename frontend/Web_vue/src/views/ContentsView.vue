<template>
  <div class="contents-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>动态内容</h2>
          <el-button type="primary" @click="handleCreateContent">发布动态</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="内容类型">
          <el-select v-model="searchForm.type" placeholder="请选择">
            <el-option label="动态" value="0" />
            <el-option label="评论" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="媒体类型">
          <el-select v-model="searchForm.mediaType" placeholder="请选择">
            <el-option label="纯文本" value="0" />
            <el-option label="图片" value="1" />
            <el-option label="视频" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <div class="contents-list">
        <el-card v-for="content in contentsList" :key="content.id" class="content-item">
          <template #header>
            <div class="content-header">
              <div class="user-info">
                <el-avatar :src="content.user.avatarUrl" size="small" />
                <span class="nickname">{{ content.user.nickname }}</span>
                <span class="time">{{ content.createdAt }}</span>
              </div>
              <el-dropdown>
                <el-button type="text" class="more-btn">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleDeleteContent(content.id)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          
          <div class="content-body">
            <p class="content-text">{{ content.content }}</p>
            
            <div v-if="content.media && content.media.length > 0" class="media-list">
              <el-image
                v-for="media in content.media"
                :key="media.id"
                :src="media.url"
                fit="cover"
                class="media-item"
                :preview-src-list="content.media.map(m => m.url)"
              />
            </div>
            
            <div class="order-info" v-if="content.order">
              <el-tag size="small">关联订单</el-tag>
              <span>活动类型：{{ getActivityTypeLabel(content.order.activityType) }}</span>
            </div>
          </div>
          
          <div class="content-footer">
            <div class="interaction-bar">
              <div class="interaction-item" @click="handleLike(content.id, $event)">
                <el-icon><Star :filled="content.liked" /></el-icon>
                <span>{{ content.likeCount }}</span>
              </div>
              <div class="interaction-item" @click="handleComment(content.id)">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ content.commentCount }}</span>
              </div>
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useContentStore } from '../stores/content'
import { Star, ChatDotRound, More } from '@element-plus/icons-vue'

const router = useRouter()
const contentStore = useContentStore()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const contentsList = ref([])

const searchForm = reactive({
  type: '',
  mediaType: ''
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

const getActivityTypeLabel = (type) => {
  return activityTypeMap[type] || '未知'
}

const fetchContents = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...(searchForm.type && { type: searchForm.type }),
      ...(searchForm.mediaType && { mediaType: searchForm.mediaType })
    }
    const response = await contentStore.getContents(params)
    contentsList.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    ElMessage.error(error.message || '获取动态列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchContents()
}

const resetForm = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchContents()
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

const handleCreateContent = () => {
  router.push('/contents/create')
}

const handleDeleteContent = async (contentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await contentStore.deleteContent(contentId)
    ElMessage.success('删除成功')
    fetchContents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleLike = async (contentId, event) => {
  try {
    const response = await contentStore.likeContent(contentId)
    // 更新本地数据
    const content = contentsList.value.find(item => item.id === contentId)
    if (content) {
      content.liked = response.data.data.liked
      content.likeCount = response.data.data.count
    }
    ElMessage.success(response.data.message)
  } catch (error) {
    ElMessage.error(error.message || '点赞失败')
  }
}

const handleComment = (contentId) => {
  router.push(`/contents/${contentId}`)
}

onMounted(() => {
  fetchContents()
})
</script>

<style scoped>
.contents-container {
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

.contents-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-item {
  transition: all 0.3s ease;
}

.content-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nickname {
  font-weight: bold;
}

.time {
  font-size: 12px;
  color: #909399;
}

.more-btn {
  padding: 0;
}

.content-body {
  margin: 16px 0;
}

.content-text {
  margin-bottom: 16px;
  line-height: 1.6;
}

.media-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.media-item {
  width: 100%;
  height: 150px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.media-item:hover {
  transform: scale(1.05);
}

.order-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.content-footer {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.interaction-bar {
  display: flex;
  gap: 24px;
}

.interaction-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
  transition: all 0.3s ease;
}

.interaction-item:hover {
  color: #409eff;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
