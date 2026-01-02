<template>
  <div class="comment-item">
    <div class="comment-main">
      <el-avatar
        :size="32"
        :src="resolveAvatarUrl(comment.user?.avatarUrl)"
        class="comment-avatar"
      >
        <span>{{ (comment.user?.nickname || '用').slice(0, 1) }}</span>
      </el-avatar>
      <div class="comment-body">
        <div class="comment-meta">
          <span class="name">{{ comment.user?.nickname || '用户' }}</span>
          <span class="time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <div class="comment-text">{{ comment.content }}</div>
          <div
            v-if="comment.mediaUrls && comment.mediaUrls.length"
            class="comment-media"
          >
          <el-image
            v-for="(url, index) in comment.mediaUrls"
            :key="index"
            :src="resolveMediaUrl(url)"
            fit="cover"
            class="comment-media-image"
            :preview-src-list="comment.mediaUrls.map(resolveMediaUrl)"
            lazy
          />
        </div>
        <div class="comment-actions">
          <el-button text size="small" @click="onReply">
            <el-icon><ChatDotRound /></el-icon>
            <span>回复</span>
          </el-button>
          <el-button
            v-if="canDelete"
            text
            type="danger"
            size="small"
            @click="onDelete"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>
    <div
      class="comment-children"
      :class="{ 'comment-children-root': level === 0 }"
      v-if="comment.replies && comment.replies.length"
    >
      <CommentItem
        v-for="child in comment.replies"
        :key="child.id"
        :comment="child"
        :level="level + 1"
        @reply="$emit('reply', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  level: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['reply', 'delete'])

const authStore = useAuthStore()

const fileBaseUrl = import.meta.env.VITE_FILE_BASE_URL || 'http://localhost:8080'

const resolveAvatarUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const resolveMediaUrl = (url) => {
  if (!url) return url
  if (/^https?:\/\//.test(url)) return url
  return `${fileBaseUrl}${url}`
}

const isImageType = (mediaType) => mediaType === 1 || mediaType === 'IMAGE'

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

const onReply = () => {
  emit('reply', props.comment)
}

const canDelete = computed(() => {
  const user = authStore.user
  if (!user) return false
  const isAdmin = user.userType === 1 || user.userType === 'ADMIN'
  const isOwner = props.comment?.user?.id === user.id
  return isAdmin || isOwner
})

const onDelete = () => {
  emit('delete', props.comment)
}
</script>

<script>
// 给组件命名以支持自身递归调用
export default {
  name: 'CommentItem'
}
</script>

<style scoped>
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

.comment-media {
  margin: 4px 0;
  display: flex;
  gap: 6px;
}

.comment-media-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-children {
  margin-top: 6px;
}

.comment-children-root {
  margin-left: 40px;
}

@media (max-width: 768px) {
  .comment-children {
    margin-left: 0;
  }

  .comment-children-root {
    margin-left: 28px;
  }
}
</style>
