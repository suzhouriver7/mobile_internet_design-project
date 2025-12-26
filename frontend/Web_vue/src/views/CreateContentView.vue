<template>
  <div class="create-content-view">
    <el-card class="editor-card" shadow="hover">
      <template #header>
        <div class="editor-header">
          <div class="editor-title">发布动态</div>
          <div class="editor-meta" v-if="userNickname">
            <el-avatar :src="userAvatar" size="small" class="editor-avatar">
              <span>{{ userInitial }}</span>
            </el-avatar>
            <span class="editor-name">{{ userNickname }}</span>
          </div>
        </div>
      </template>

      <!-- 文本输入 -->
      <el-input
        v-model="contentText"
        type="textarea"
        :rows="6"
        class="editor-textarea"
        maxlength="500"
        show-word-limit
        placeholder="今天想分享点什么？支持换行和多段落～"
      />

      <div class="editor-toolbar">
        <div class="editor-status">
          <el-tag v-if="draftSaved" size="small" type="success" effect="light">
            草稿已保存
          </el-tag>
          <el-tag v-else-if="hasDraft" size="small" type="info" effect="light">
            检测到未发布草稿
          </el-tag>
        </div>
      </div>

      <!-- 媒体上传：图片 + 视频 -->
      <div class="media-section">
        <div class="media-header">
          <span class="label">添加图片 / 视频（可选）：</span>
          <span class="tips">单次最多选择 9 个文件，图片与视频均可</span>
        </div>
        <el-upload
          class="media-uploader"
          drag
          multiple
          :limit="9"
          :auto-upload="false"
          :file-list="fileList"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          accept="image/*,video/*"
        >
          <el-icon class="media-icon"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或 <em>点击选择</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              图片将以九宫格形式展示，视频以播放器形式展示；文件实际上传在点击“发布”后进行。
            </div>
          </template>
        </el-upload>

        <!-- 选择预览 -->
        <div v-if="fileList.length" class="media-preview">
          <div
            v-for="file in fileList"
            :key="file.uid"
            class="media-preview-item"
          >
            <template v-if="isImage(file)">
              <el-image :src="file.url || file.rawUrl" fit="cover" lazy />
            </template>
            <template v-else>
              <div class="video-placeholder">
                <el-icon><VideoPlay /></el-icon>
                <span>{{ file.name }}</span>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- 操作区 -->
      <div class="editor-actions">
        <div class="left-actions">
          <el-button
            size="small"
            @click="handleSaveDraft"
            :loading="savingDraft"
          >
            保存草稿
          </el-button>
          <el-button
            size="small"
            type="info"
            plain
            class="attach-order-btn"
            @click="openOrderDialog"
          >
            {{ attachedOrder ? `已附加订单 #${attachedOrder.id}` : '附加订单' }}
          </el-button>
          <el-button
            v-if="attachedOrder"
            size="small"
            text
            class="detach-order-btn"
            @click="clearAttachedOrder"
          >
            移除
          </el-button>
        </div>
        <div class="right-actions">
          <el-button @click="handleCancel" :disabled="publishing">取消</el-button>
          <el-button
            type="primary"
            @click="handlePublish"
            :loading="publishing"
          >
            {{ publishing ? '发布中...' : '发布动态' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 选择附加订单弹窗 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="选择要附加的订单"
      width="680px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="order-dialog-body">
        <div class="order-dialog-toolbar">
          <el-space wrap>
            <el-radio-group v-model="onlyRelated" size="small">
              <el-radio-button :label="true">只看与我相关</el-radio-button>
              <el-radio-button :label="false">全部订单</el-radio-button>
            </el-radio-group>
            <el-input
              v-model="orderKeyword"
              placeholder="按订单编号 / 地点搜索"
              size="small"
              class="order-search-input"
              clearable
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-space>
        </div>

        <el-skeleton v-if="orderLoading && !ordersRaw.length" :rows="4" animated class="order-skeleton" />

        <template v-else>
          <el-empty
            v-if="displayOrders.length === 0"
            description="暂无可附加的订单"
          />
          <div
            v-else
            class="order-list"
            ref="orderListRef"
            @scroll.passive="handleOrderListScroll"
          >
            <transition-group name="list-fade" tag="div">
              <el-card
                v-for="order in displayOrders"
                :key="order.id"
                class="order-item-card"
                :class="{
                  selected: tempSelectedOrder && tempSelectedOrder.id === order.id
                }"
                shadow="hover"
                @click="selectOrder(order)"
              >
                <div class="order-item-main">
                  <div class="order-title">
                    <div class="order-title-main">
                      <span class="order-id">#{{ order.id }}</span>
                      <span class="order-activity">
                        {{ getActivityTypeLabel(order.activityType) }} · {{ order.location }}
                      </span>
                    </div>
                    <span class="order-select-flag">
                      {{ tempSelectedOrder && tempSelectedOrder.id === order.id ? '已选择' : '选择' }}
                    </span>
                  </div>
                  <div class="order-meta">
                    <el-tag size="small" :type="getStatusType(order.status)">
                      {{ getStatusLabel(order.status) }}
                    </el-tag>
                    <span class="order-time">{{ formatOrderTime(order.startTime) }}</span>
                  </div>
                </div>
              </el-card>
            </transition-group>
            <div v-if="orderLoading && ordersRaw.length" class="order-loading-more">
              加载中...
            </div>
            <div v-else-if="!orderHasMore && ordersRaw.length" class="order-no-more">
              没有更多订单了
            </div>
          </div>
        </template>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="orderDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmOrderSelection" :disabled="!tempSelectedOrder">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch, computed as vComputed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, VideoPlay, Search } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useContentStore } from '../stores/content'
import { useOrderStore } from '../stores/order'

const router = useRouter()
const authStore = useAuthStore()
const contentStore = useContentStore()
const orderStore = useOrderStore()

const contentText = ref('')
const fileList = ref([])
const publishing = ref(false)
const savingDraft = ref(false)
const draftSaved = ref(false)

// 选中的订单（附加到动态）
const attachedOrder = ref(null)

const user = computed(() => authStore.user || {})
const userNickname = computed(() => user.value.nickname || '用户')
const userInitial = computed(() => (user.value.nickname ? user.value.nickname.slice(0, 1) : '用'))

const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'
const resolveAvatarUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const userAvatar = computed(() => {
  return user.value.avatarUrl ? resolveAvatarUrl(user.value.avatarUrl) : ''
})

// 草稿存储 key（按用户区分）
const draftKey = computed(() => {
  const id = user.value.id || 'guest'
  return `content_draft_${id}`
})

const hasDraft = computed(() => {
  return !!localStorage.getItem(draftKey.value)
})

const isImage = (file) => {
  const type = file.raw?.type || file.type || ''
  return type.startsWith('image/')
}

// 简单的前端体积校验，避免明显超过后端限制的文件
const MAX_FILE_SIZE = 20 * 1024 * 1024 // 20MB

const inferMediaType = () => {
  if (!fileList.value.length) return 0 // TEXT
  const hasVideo = fileList.value.some((f) => {
    const type = f.raw?.type || f.type || ''
    return type.startsWith('video/')
  })
  return hasVideo ? 2 : 1 // VIDEO:2, IMAGE:1，参照接口 MediaType
}

const handleFileChange = (file, files) => {
  // 超出大小限制的文件直接提示并过滤掉
  const raw = file.raw || file
  if (raw && raw.size && raw.size > MAX_FILE_SIZE) {
    ElMessage.warning('单个文件不能超过 20MB')
    // 从列表中移除当前文件
    fileList.value = files.filter((f) => f.uid !== file.uid)
    return
  }
  // el-upload 在未上传模式下不会自动赋 url，这里做个简单处理方便预览
  if (!file.url && file.raw) {
    file.rawUrl = URL.createObjectURL(file.raw)
  }
  fileList.value = files
  draftSaved.value = false
}

const handleFileRemove = (file, files) => {
  fileList.value = files
  draftSaved.value = false
}

const handleSaveDraft = () => {
  if (!contentText.value && fileList.value.length === 0) {
    ElMessage.info('暂无内容可保存')
    return
  }
  savingDraft.value = true
  try {
    const plainFiles = fileList.value.map((f) => ({
      name: f.name,
      type: f.raw?.type || f.type,
      size: f.size
    }))
    const data = {
      contentText: contentText.value,
      files: plainFiles
    }
    localStorage.setItem(draftKey.value, JSON.stringify(data))
    draftSaved.value = true
    ElMessage.success('草稿已保存到本地')
  } catch (e) {
    ElMessage.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

const restoreDraftIfAny = () => {
  const raw = localStorage.getItem(draftKey.value)
  if (!raw) return
  try {
    const data = JSON.parse(raw)
    contentText.value = data.contentText || ''
  } catch (e) {
    // ignore
  }
}

const clearDraft = () => {
  localStorage.removeItem(draftKey.value)
  draftSaved.value = false
}

const handlePublish = async () => {
  if (!contentText.value.trim() && fileList.value.length === 0) {
    ElMessage.warning('请输入内容或至少选择一个媒体文件')
    return
  }
  if (!authStore.user?.id) {
    ElMessage.warning('请先登录后再发布动态')
    router.push('/login')
    return
  }

  publishing.value = true
  try {
    const mediaType = inferMediaType()

    // 1. 先创建内容（文本 + 媒体类型）
    const createResp = await contentStore.createContent({
      content: contentText.value,
      mediaType,
      // 若选择了订单，则将订单 ID 一并提交给后端进行关联
      orderId: attachedOrder.value ? attachedOrder.value.id : null
    })

    // 兼容当前后端 ApiResponse<Long> 结构：
    // - 正常情况下 data 字段就是一个 Long 类型的内容 ID
    // - 若未来改成对象，则优先从 data.contentId / data.id 读取
    const respBody = createResp?.data || {}
    const rawData = respBody.data
    let contentId = null

    if (rawData != null) {
      if (typeof rawData === 'number' || typeof rawData === 'string') {
        contentId = Number(rawData)
      } else if (typeof rawData === 'object') {
        contentId = rawData.contentId ?? rawData.id ?? null
      }
    }

    // 2. 若有文件且拿到了内容 ID，依次上传到 /contents/{contentId}/media
    if (fileList.value.length && contentId) {
      for (const file of fileList.value) {
        const form = new FormData()
        form.append('media', file.raw)
        await contentStore.uploadMedia(contentId, form)
      }
    } else if (fileList.value.length && !contentId) {
      // 文本已成功发布，但未拿到内容 ID，无法上传媒体文件
      console.warn('发布成功但未获取到内容 ID，媒体文件未上传')
      ElMessage.warning('动态已发布，但暂未获取到内容ID，媒体文件未上传')
    }

    clearDraft()
    contentText.value = ''
    fileList.value = []
    attachedOrder.value = null
    ElMessage.success('动态发布成功')
    router.push('/contents')
  } catch (error) {
    console.error('发布动态失败', error)
    ElMessage.error(error.response?.data?.message || error.message || '发布动态失败')
  } finally {
    publishing.value = false
  }
}

// ====== 订单选择相关逻辑 ======
const orderDialogVisible = ref(false)
const orderLoading = ref(false)
const ordersRaw = ref([])
const orderTotal = ref(0)
const orderPage = ref(1)
const orderPageSize = ref(5)
const orderKeyword = ref('')
const onlyRelated = ref(true)
const tempSelectedOrder = ref(null)
const orderHasMore = ref(true)
const orderListRef = ref(null)

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

const getActivityTypeLabel = (type) => {
  if (!type) return '未知活动'
  if (typeof type === 'number') {
    // 兼容数字枚举，直接返回“活动”占位
    return '活动'
  }
  return activityTypeMap[type] || '活动'
}

const getStatusLabel = (status) => {
  if (!status) return '未知状态'
  if (typeof status === 'number') {
    return '状态'
  }
  return statusMap[status] || '状态'
}

const getStatusType = (status) => {
  if (!status || typeof status === 'number') return 'info'
  return statusTypeMap[status] || 'info'
}

const formatOrderTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time)
  return date.toLocaleString()
}

const currentUserId = vComputed(() => authStore.user?.id || null)

const fetchOrdersForDialog = async (append = false) => {
  if (orderLoading.value) return
  orderLoading.value = true
  try {
    const params = {
      page: orderPage.value,
      size: orderPageSize.value
    }
    const resp = await orderStore.getOrders(params)
    const data = resp.data?.data || {}
    const list = data.list || []
    if (append) {
      const existingIds = new Set(ordersRaw.value.map((o) => o.id))
      const merged = [...ordersRaw.value]
      list.forEach((item) => {
        if (!existingIds.has(item.id)) {
          merged.push(item)
        }
      })
      ordersRaw.value = merged
    } else {
      ordersRaw.value = list
    }
    orderTotal.value = data.total || list.length || 0
    // 是否还有更多数据可加载
    orderHasMore.value =
      ordersRaw.value.length < orderTotal.value && list.length >= orderPageSize.value
  } catch (error) {
    console.error('加载订单列表失败', error)
    ElMessage.error(error.response?.data?.message || '加载订单列表失败')
  } finally {
    orderLoading.value = false
  }
}

const displayOrders = vComputed(() => {
  let list = [...ordersRaw.value]

  // 仅看与当前用户相关的订单：默认只看自己发布的订单
  if (onlyRelated.value && currentUserId.value) {
    list = list.filter((o) => o.user && o.user.id === currentUserId.value)
  }

  const keyword = orderKeyword.value.trim().toLowerCase()
  if (keyword) {
    list = list.filter((o) => {
      const idMatch = String(o.id || '').includes(keyword)
      const locationMatch = (o.location || '').toLowerCase().includes(keyword)
      return idMatch || locationMatch
    })
  }

  return list
})

const openOrderDialog = () => {
  if (!authStore.user?.id) {
    ElMessage.warning('请先登录后再选择订单')
    router.push('/login')
    return
  }
  orderDialogVisible.value = true
  if (!ordersRaw.value.length) {
    orderPage.value = 1
    orderHasMore.value = true
    fetchOrdersForDialog(false)
  }
  tempSelectedOrder.value = attachedOrder.value
}

const handleOrderListScroll = (event) => {
  if (!orderHasMore.value || orderLoading.value) return
  const el = event.target
  const threshold = 40
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - threshold) {
    orderPage.value += 1
    fetchOrdersForDialog(true)
  }
}

const selectOrder = (order) => {
  tempSelectedOrder.value = order
}

const confirmOrderSelection = () => {
  if (!tempSelectedOrder.value) return
  attachedOrder.value = tempSelectedOrder.value
  orderDialogVisible.value = false
  ElMessage.success(`已附加订单 #${attachedOrder.value.id}`)
}

const clearAttachedOrder = () => {
  attachedOrder.value = null
  tempSelectedOrder.value = null
}

const handleCancel = () => {
  if (!contentText.value && fileList.value.length === 0) {
    router.back()
    return
  }

  ElMessageBox.confirm(
    '确定要放弃本次编辑吗？未保存的内容将会丢失。',
    '放弃编辑',
    {
      confirmButtonText: '放弃',
      cancelButtonText: '继续编辑',
      type: 'warning'
    }
  )
    .then(() => {
      router.back()
    })
    .catch(() => {})
}

onMounted(() => {
  restoreDraftIfAny()
})
</script>

<style scoped>
.create-content-view {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.editor-card {
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.editor-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.editor-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.editor-avatar {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.editor-name {
  font-weight: 500;
}

.editor-textarea {
  margin-top: 12px;
}

.editor-toolbar {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.visibility-select {
  display: flex;
  align-items: center;
  gap: 8px;
}

.visibility-select .label {
  font-size: 13px;
  color: #606266;
}

.editor-status {
  display: flex;
  align-items: center;
}

.media-section {
  margin-top: 16px;
}

.media-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.media-header .label {
  font-size: 13px;
  color: #606266;
}

.media-header .tips {
  font-size: 12px;
  color: #909399;
}

.media-uploader {
  width: 100%;
}

.media-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 8px;
}

.media-preview {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.media-preview-item {
  border-radius: 6px;
  overflow: hidden;
}

.video-placeholder {
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #606266;
  font-size: 12px;
  gap: 6px;
}

.editor-actions {
  margin-top: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attach-order-btn {
  margin-left: 8px;
}

.detach-order-btn {
  padding: 0 4px;
}

.order-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-dialog-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.order-search-input {
  width: 220px;
}

.order-skeleton {
  margin-top: 8px;
}

.order-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 360px;
  overflow-y: auto;
  padding-right: 4px;
}

.order-item-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  cursor: pointer;
  border-radius: 8px;
  transition: box-shadow 0.18s ease, transform 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
}

.order-item-card :deep(.el-card__body) {
  padding: 10px 12px;
}

.order-item-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.order-item-card.selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.order-item-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.order-title-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-id {
  font-weight: 600;
}

.order-activity {
  font-size: 13px;
  color: #606266;
}

.order-select-flag {
  font-size: 12px;
  color: #409eff;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.order-time {
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 列表淡入淡出过渡效果 */
.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.18s ease;
}

.list-fade-enter-from,
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

.order-loading-more,
.order-no-more {
  margin-top: 8px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .order-dialog-body {
    gap: 10px;
  }

  .order-dialog-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .order-search-input {
    width: 100%;
  }

  .order-item-card {
    align-items: flex-start;
    gap: 8px;
  }
}

.left-actions {
  display: flex;
  gap: 10px;
}

.right-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .create-content-view {
    padding: 12px;
  }

  .editor-actions {
    flex-direction: column-reverse;
    align-items: stretch;
    gap: 8px;
  }

  .right-actions {
    justify-content: flex-end;
  }
}
</style>
