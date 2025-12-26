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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, VideoPlay } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useContentStore } from '../stores/content'

const router = useRouter()
const authStore = useAuthStore()
const contentStore = useContentStore()

const contentText = ref('')
const fileList = ref([])
const publishing = ref(false)
const savingDraft = ref(false)
const draftSaved = ref(false)

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
      mediaType
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
    ElMessage.success('动态发布成功')
    router.push('/contents')
  } catch (error) {
    console.error('发布动态失败', error)
    ElMessage.error(error.response?.data?.message || error.message || '发布动态失败')
  } finally {
    publishing.value = false
  }
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
  justify-content: flex-start;
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
