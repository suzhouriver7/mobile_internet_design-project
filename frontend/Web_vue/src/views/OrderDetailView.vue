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
                <div v-if="canEditOrder" class="edit-actions">
                  <el-button
                    v-if="!isEditing"
                    type="primary"
                    link
                    size="small"
                    @click="startEdit"
                  >
                    编辑
                  </el-button>
                  <div v-else class="edit-actions-group">
                    <el-button
                      size="small"
                      link
                      @click="cancelEdit"
                    >
                      取消
                    </el-button>
                    <el-button
                      type="primary"
                      size="small"
                      :loading="savingEdit"
                      :disabled="!canSaveEdit"
                      @click="saveEdit"
                    >
                      保存
                    </el-button>
                  </div>
                </div>
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
                <span class="label">
                  地点
                  <el-tag
                    v-if="canEditOrder"
                    size="small"
                    type="warning"
                    effect="plain"
                    class="editable-badge"
                  >
                    可编辑
                  </el-tag>
                </span>
                <span class="value" v-if="!isEditing">{{ order.location }}</span>
                <div v-else class="edit-field">
                  <el-input
                    v-model="editForm.location"
                    size="small"
                    placeholder="请输入活动地点"
                    maxlength="100"
                    show-word-limit
                  />
                  <div v-if="locationError" class="field-error">{{ locationError }}</div>
                </div>
              </div>
              <div class="info-item">
                <span class="label">
                  开始时间
                  <el-tag
                    v-if="canEditOrder"
                    size="small"
                    type="warning"
                    effect="plain"
                    class="editable-badge"
                  >
                    可编辑
                  </el-tag>
                </span>
                <span class="value" v-if="!isEditing">{{ formatDateTime(order.startTime, true) }}</span>
                <div v-else class="edit-field">
                  <el-date-picker
                    v-model="editForm.startTime"
                    type="datetime"
                    placeholder="请选择开始时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :teleported="false"
                    style="width: 100%"
                  />
                  <div v-if="startTimeError" class="field-error">{{ startTimeError }}</div>
                </div>
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
                <span class="label">
                  人数
                  <el-tag
                    v-if="canEditOrder"
                    size="small"
                    type="warning"
                    effect="plain"
                    class="editable-badge"
                  >
                    可编辑
                  </el-tag>
                </span>
                <span class="value highlight" v-if="!isEditing">
                  {{ order.currentPeople }}/{{ order.maxPeople }}
                </span>
                <div v-else class="edit-field">
                  <div class="people-row">
                    <span class="current-people">当前：{{ order.currentPeople }}</span>
                    <span class="slash">/</span>
                    <el-input-number
                      v-model="editForm.maxPeople"
                      :min="1"
                      :step="1"
                      :precision="0"
                      size="small"
                    />
                  </div>
                  <div v-if="maxPeopleError" class="field-error">{{ maxPeopleError }}</div>
                </div>
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
            <div class="note-block">
              <div class="note-label">
                备注说明
                <el-tag
                  v-if="canEditOrder"
                  size="small"
                  type="warning"
                  effect="plain"
                  class="editable-badge"
                >
                  可编辑
                </el-tag>
              </div>
              <template v-if="!isEditing">
                <div v-if="order.note" class="note-content">{{ order.note }}</div>
                <div v-else class="note-content note-empty">暂无备注</div>
              </template>
              <template v-else>
                <el-input
                  v-model="editForm.note"
                  type="textarea"
                  :rows="3"
                  maxlength="200"
                  show-word-limit
                  placeholder="请输入备注（选填）"
                />
                <div v-if="noteError" class="field-error">{{ noteError }}</div>
              </template>
            </div>

            <div class="history-block" v-if="editHistory.length > 0">
              <div class="history-label">修改记录（本页会话）</div>
              <div class="history-list">
                <div
                  v-for="item in editHistory"
                  :key="item.id"
                  class="history-item"
                >
                  <div class="history-meta">
                    <span class="history-time">{{ formatDateTime(item.time, true) }}</span>
                  </div>
                  <div class="history-diff">
                    <div class="history-row">
                      <span class="history-field">地点</span>
                      <span class="history-value old">{{ item.before.location || '（空）' }}</span>
                      <span class="history-arrow">→</span>
                      <span class="history-value new">{{ item.after.location || '（空）' }}</span>
                    </div>
                    <div class="history-row">
                      <span class="history-field">开始</span>
                      <span class="history-value old">{{ item.before.startTime || '（空）' }}</span>
                      <span class="history-arrow">→</span>
                      <span class="history-value new">{{ item.after.startTime || '（空）' }}</span>
                    </div>
                    <div class="history-row">
                      <span class="history-field">人数</span>
                      <span class="history-value old">{{ item.before.maxPeople ?? '（空）' }}</span>
                      <span class="history-arrow">→</span>
                      <span class="history-value new">{{ item.after.maxPeople ?? '（空）' }}</span>
                    </div>
                    <div class="history-row">
                      <span class="history-field">备注</span>
                      <span class="history-value old">{{ item.before.note || '（空）' }}</span>
                      <span class="history-arrow">→</span>
                      <span class="history-value new">{{ item.after.note || '（空）' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：申请信息 -->
        <el-col :xs="24" :md="8">
          <el-card class="detail-card" shadow="always">
            <template #header>
              <div class="card-header">
                <span>申请信息</span>
                <div style="display:flex; align-items:center; gap:8px;">
                  <span class="apply-count">共 {{ applications.length }} 人申请</span>
                  <el-button
                    type="primary"
                    size="small"
                    :disabled="isApplyDisabled(order)"
                    @click="handleApplyOrder"
                  >
                    {{ getApplyButtonText(order) }}
                  </el-button>
                </div>
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
                    <div class="apply-actions">
                      <el-tag size="small" type="info">{{ getApplyStatusLabel(apply.status) }}</el-tag>
                      <!-- 发布者审核操作：仅对待审核的申请显示 -->
                      <template v-if="canAuditApply(apply)">
                        <el-button
                          type="success"
                          text
                          size="small"
                          :disabled="!canApproveApply(apply)"
                          @click.stop="handleAudit(apply, 'APPROVED')"
                        >
                          通过
                        </el-button>
                        <el-button
                          type="danger"
                          text
                          size="small"
                          @click.stop="handleAudit(apply, 'REJECTED')"
                        >
                          拒绝
                        </el-button>
                      </template>
                      <el-button
                        v-if="canCancelApply(apply)"
                        type="danger"
                        text
                        size="small"
                        @click.stop="handleCancelApply(apply)"
                      >
                        撤销申请
                      </el-button>
                    </div>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useOrderStore } from '../stores/order'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const authStore = useAuthStore()

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

// 与后端 ApplyStatus 枚举保持一致
const applyStatusMap = {
  PENDING_REVIEW: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  CANCELLED_APPLY: '已取消'
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
// 原始申请列表
const rawApplications = computed(() => detail.value?.applications || [])
// 过滤掉已撤销的申请，只展示有效记录
const applications = computed(() => rawApplications.value.filter(a => a.status !== 'CANCELLED_APPLY'))

// 当前登录用户 ID
const currentUserId = computed(() => {
  return authStore.user?.id || Number(localStorage.getItem('userId')) || null
})

const publisherAvatarUrl = computed(() => {
  if (!publisher.value?.avatarUrl) return ''
  return resolveAvatarUrl(publisher.value.avatarUrl)
})

// 编辑状态与表单
const isEditing = ref(false)
const savingEdit = ref(false)
const editForm = reactive({
  location: '',
  startTime: '',
  maxPeople: null,
  note: ''
})

// 本页会话内的修改记录
const editHistory = ref([])

// 订单是否已达到人数上限
const isOrderFull = computed(() => {
  if (!order.value) return false
  const current = Number(order.value.currentPeople || 0)
  const max = Number(order.value.maxPeople || 0)
  if (Number.isNaN(current) || Number.isNaN(max) || max <= 0) return false
  return current >= max
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

// 是否为当前订单的发布者
const isPublisher = computed(() => {
  if (!currentUserId.value || !order.value?.user) return false
  return order.value.user.id === currentUserId.value
})

// 是否可以编辑订单（仅发布者 + 待匹配状态）
const canEditOrder = computed(() => {
  if (!order.value) return false
  if (!isPublisher.value) return false
  return order.value.status === 'PENDING'
})

// 判断某条申请是否属于当前登录用户
const isMyApplication = (apply) => {
  if (!currentUserId.value || !apply?.user) return false
  return apply.user.id === currentUserId.value
}

// 仅待审核状态允许撤销
const isPendingApply = (apply) => {
  if (!apply?.status) return false
  return apply.status === 'PENDING_REVIEW'
}

// 是否可以显示“撤销申请”按钮
const canCancelApply = (apply) => {
  return isMyApplication(apply) && isPendingApply(apply) && order.value?.status === 'PENDING'
}

// ----- 与 OrdersView 复用的申请/操作逻辑 -----
const hasApplied = computed(() => {
  if (!Array.isArray(applications.value) || !currentUserId.value) return false
  return applications.value.some(a => a.user && a.user.id === currentUserId.value && a.status !== 'CANCELLED_APPLY')
})

const isApplyDisabled = (o) => {
  const ord = o || order.value
  if (!ord) return true
  // 发布者：仅在订单为待匹配时允许“取消订单”操作
  if (isPublisher.value) {
    if (ord.status === 'CANCELLED') return true
    return ord.status !== 'PENDING'
  }

  // 非发布者：已申请、已过期、人数已满或非待匹配状态都不允许申请
  if (hasApplied.value) return true
  if (ord.status === 'EXPIRED') return true
  if ((ord.currentPeople || 0) >= (ord.maxPeople || 0)) return true
  if (ord.status !== 'PENDING') return true
  return false
}

const getApplyButtonText = (o) => {
  const ord = o || order.value
  if (!ord) return '申请'

  // 发布者侧文案
  if (isPublisher.value) {
    if (ord.status === 'CANCELLED') return '已取消'
    if (ord.status !== 'PENDING') return '不可操作'
    return '取消订单'
  }

  // 已申请
  if (hasApplied.value) return '已申请'

  if (ord.status === 'EXPIRED') return '已过期'
  if (ord.status === 'IN_PROGRESS') return '进行中'
  if (ord.status === 'COMPLETED') return '已完成'
  if (ord.status === 'CANCELLED') return '已取消'

  if ((ord.currentPeople || 0) >= (ord.maxPeople || 0)) return '人数已满'

  if (ord.status === 'PENDING') return '申请'

  return '不可申请'
}

const handleApplyOrder = async () => {
  if (!order.value?.id) return

  // 发布者点击：执行“取消订单”逻辑
  if (isPublisher.value) {
    if (order.value.status === 'CANCELLED') return
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
      await orderStore.deleteOrder(order.value.id)
      ElMessage.success('订单已取消')
      router.back()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error?.message || '取消订单失败')
    }
    return
  }

  // 非发布者：申请逻辑
  if (isApplyDisabled(order.value)) return
  try {
    await orderStore.applyOrder(order.value.id)
    ElMessage.success('申请成功')
    // 重新加载详情，更新申请列表与人数
    await loadDetail()
  } catch (error) {
    ElMessage.error(error?.message || '申请失败')
  }
}

// 发布者是否可以对该申请进行审核
const canAuditApply = (apply) => {
  // 仅订单发布者、订单处于待匹配、且该申请为待审核状态时允许
  if (!isPublisher.value) return false
  if (!order.value || order.value.status !== 'PENDING') return false
  return isPendingApply(apply)
}

// 是否可以对该申请执行“通过”操作（已满时不允许）
const canApproveApply = (apply) => {
  if (!canAuditApply(apply)) return false
  if (isOrderFull.value) return false
  return true
}

// 实时校验
const locationError = computed(() => {
  if (!isEditing.value) return ''
  const value = (editForm.location || '').trim()
  if (!value) return '地点不能为空'
  if (value.length > 100) return '地点不能超过 100 个字符'
  return ''
})

const noteError = computed(() => {
  if (!isEditing.value) return ''
  const value = editForm.note || ''
  if (value.length > 200) return '备注不能超过 200 个字符'
  return ''
})

const startTimeError = computed(() => {
  if (!isEditing.value) return ''
  const value = editForm.startTime
  if (!value) return '开始时间不能为空'
  const d = new Date(value.replace(' ', 'T'))
  if (Number.isNaN(d.getTime())) return '开始时间格式不正确'
  return ''
})

const maxPeopleError = computed(() => {
  if (!isEditing.value) return ''
  const val = Number(editForm.maxPeople)
  if (!Number.isInteger(val) || val <= 0) {
    return '人数上限必须为正整数'
  }
  if (order.value) {
    const current = Number(order.value.currentPeople || 0)
    if (val < current) {
      return `人数上限不能小于当前人数（${current}）`
    }
  }
  return ''
})

const canSaveEdit = computed(() => {
  if (!isEditing.value) return false
  if (locationError.value || startTimeError.value || maxPeopleError.value || noteError.value) return false
  return true
})

const initEditFormFromOrder = () => {
  if (!order.value) return
  editForm.location = order.value.location || ''
   editForm.startTime = buildStartTimeString()
   editForm.maxPeople = order.value.maxPeople ?? null
  editForm.note = order.value.note || ''
}

const buildStartTimeString = () => {
  if (!order.value?.startTime) return ''
  const raw = order.value.startTime
  if (typeof raw === 'string') {
    return raw.replace('T', ' ').slice(0, 19)
  }
  return formatDateTime(raw, true)
}

const startEdit = () => {
  if (!canEditOrder.value) return
  initEditFormFromOrder()
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveEdit = async () => {
  if (!canEditOrder.value || !order.value) return
  if (!canSaveEdit.value) {
    ElMessage.error('请先修正表单中的错误')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要保存对订单文本信息的修改吗？',
      '确认修改',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        draggable: true
      }
    )

    savingEdit.value = true

    const beforeSnapshot = {
      location: order.value.location || '',
      startTime: buildStartTimeString(),
      maxPeople: order.value.maxPeople,
      note: order.value.note || ''
    }

    const payload = {
      activityType: order.value.activityType,
      genderRequire: order.value.genderRequire,
      campus: order.value.campus,
      location: (editForm.location || '').trim(),
      startTime: editForm.startTime,
      note: editForm.note || '',
      maxPeople: editForm.maxPeople
    }

    await orderStore.updateOrder(order.value.id, payload)
    await loadDetail()

    const afterSnapshot = {
      location: order.value.location || '',
      startTime: buildStartTimeString(),
      maxPeople: order.value.maxPeople,
      note: order.value.note || ''
    }

    editHistory.value.unshift({
      id: Date.now(),
      time: new Date().toISOString(),
      before: beforeSnapshot,
      after: afterSnapshot
    })

    isEditing.value = false
    ElMessage.success('订单信息已更新')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    const message = error?.response?.data?.message || error?.message || '修改订单失败'
    ElMessage.error(message)
  } finally {
    savingEdit.value = false
  }
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

const handleCancelApply = async (apply) => {
  if (!order.value?.id || !apply?.id) return
  if (!canCancelApply(apply)) return

  try {
    await ElMessageBox.confirm(
      '确定要撤销该申请吗？',
      '撤销申请确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        draggable: true
      }
    )

    await orderStore.cancelApply(order.value.id)
    // 本地更新并移除该条申请，避免整页刷新
    const d = orderStore.currentOrder
    if (d && Array.isArray(d.applications)) {
      d.applications = d.applications
        .map(a => {
          if (a.id === apply.id) {
            return { ...a, status: 'CANCELLED_APPLY' }
          }
          return a
        })
        .filter(a => a.status !== 'CANCELLED_APPLY')
    }

    ElMessage.success('已撤销申请')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error?.message || '撤销申请失败')
  }
}

// 发布者审核申请：通过或拒绝
const handleAudit = async (apply, targetStatus) => {
  if (!apply?.id || !order.value?.id) return
  if (!canAuditApply(apply)) return

  const actionText = targetStatus === 'APPROVED' ? '通过' : '拒绝'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}该申请吗？`,
      '审核确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        draggable: true
      }
    )

    await orderStore.auditApplication(apply.id, targetStatus)
    // 审核成功后重新加载详情，以便人数和申请状态保持与后端一致
    await loadDetail()
    ElMessage.success(`已${actionText}申请`)
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error?.message || `${actionText}申请失败`)
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

.edit-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-actions-group {
  display: flex;
  align-items: center;
  gap: 4px;
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

.editable-badge {
  margin-left: 6px;
}

.edit-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.people-row {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.current-people {
  font-size: 13px;
  color: #606266;
}

.slash {
  color: #909399;
}

.field-error {
  font-size: 12px;
  color: #f56c6c;
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

.note-empty {
  color: #c0c4cc;
}

.history-block {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.history-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  padding: 8px 10px;
  border-radius: 6px;
  background-color: #f9fafc;
}

.history-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.history-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.history-field {
  width: 36px;
  color: #909399;
}

.history-value {
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-value.old {
  color: #909399;
}

.history-value.new {
  color: #409eff;
}

.history-arrow {
  color: #c0c4cc;
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

.apply-actions {
  display: flex;
  align-items: center;
  gap: 6px;
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
