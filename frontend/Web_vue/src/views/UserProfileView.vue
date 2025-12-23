<template>
  <div class="user-profile-view">
    <!-- 顶部个人信息卡片 -->
    <el-card class="profile-header-card" shadow="hover">
      <div class="profile-header">
        <div class="avatar-area">
          <el-avatar
            :size="88"
            :src="currentUser?.avatarUrl"
            class="avatar"
          >
            {{ currentUserInitial }}
          </el-avatar>
          <div class="avatar-actions">
            <el-button size="small" @click="triggerAvatarSelect" :loading="avatarUploading">
              更换头像
            </el-button>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              class="hidden-file-input"
              @change="handleAvatarChange"
            />
            <p class="avatar-tip">支持 JPG/PNG，大小不超过 2MB</p>
          </div>
        </div>
        <div class="base-info">
          <div class="name-row">
            <h2 class="nickname">{{ currentUser?.nickname || '未命名用户' }}</h2>
            <el-tag size="small" type="info" v-if="currentUser">
              {{ userTypeLabel }}
            </el-tag>
          </div>
          <p class="email" v-if="currentUser?.email">{{ currentUser.email }}</p>
          <p class="signature" v-if="currentUser?.signature">
            {{ currentUser.signature }}
          </p>
          <div class="meta-row" v-if="currentUser">
            <span>用户ID：{{ currentUser.id }}</span>
            <span>注册时间：{{ currentUser.createdAt }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主要功能区域：标签页 -->
    <el-card class="profile-main-card" shadow="never">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            :model="editableUser"
            label-width="90px"
            class="profile-form"
          >
            <el-form-item label="昵称">
              <el-input
                v-model="editableUser.nickname"
                maxlength="50"
                show-word-limit
                placeholder="请输入昵称"
              />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="editableUser.email" disabled />
            </el-form-item>
            <el-form-item label="个性签名">
              <el-input
                v-model="editableUser.signature"
                type="textarea"
                :rows="3"
                maxlength="255"
                show-word-limit
                placeholder="写点什么，让大家更了解你吧"
              />
            </el-form-item>
            <el-form-item label="账号类型" v-if="currentUser">
              <el-tag type="info">{{ userTypeLabel }}</el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveProfile" :loading="savingProfile">
                保存修改
              </el-button>
              <el-button @click="resetProfile" :disabled="savingProfile">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 账户安全 -->
        <el-tab-pane label="账户安全" name="security">
          <div class="section-description">
            <p>建议定期更换密码，保证账户安全。</p>
          </div>
          <el-form :model="passwordForm" label-width="100px" class="security-form">
            <el-form-item label="当前密码">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                autocomplete="current-password"
                placeholder="请输入当前密码"
              />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                autocomplete="new-password"
                placeholder="6-20 位新密码"
              />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                autocomplete="new-password"
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
                修改密码
              </el-button>
              <span class="security-tip">密码长度需在 6-20 位之间，建议包含字母与数字。</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 隐私与权限 -->
        <el-tab-pane label="隐私设置" name="privacy">
          <div class="section-description">
            <p>控制哪些信息对其他用户可见，保护你的个人隐私。</p>
          </div>
          <div class="settings-group">
            <div class="settings-item">
              <div class="settings-text">
                <h4>对他人显示邮箱</h4>
                <p>关闭后，其他用户将无法在你的资料页看到邮箱信息。</p>
              </div>
              <el-switch v-model="privacySettings.showEmail" />
            </div>
            <div class="settings-item">
              <div class="settings-text">
                <h4>允许陌生人发起消息</h4>
                <p>关闭后，仅与你有订单关系的用户可以向你发消息。</p>
              </div>
              <el-switch v-model="privacySettings.allowMessages" />
            </div>
            <div class="settings-item">
              <div class="settings-text">
                <h4>允许在用户搜索中展示</h4>
                <p>关闭后，你的账号将不会出现在“搜索用户”的结果中。</p>
              </div>
              <el-switch v-model="privacySettings.showInSearch" />
            </div>
            <div class="settings-actions">
              <el-button type="primary" @click="savePrivacySettings">保存设置</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 消息通知 -->
        <el-tab-pane label="消息通知" name="notification">
          <div class="section-description">
            <p>配置你希望接收的通知类型，避免被无关消息打扰。</p>
          </div>
          <div class="settings-group">
            <div class="settings-item">
              <div class="settings-text">
                <h4>订单动态通知</h4>
                <p>包括订单申请、状态变更等提醒。</p>
              </div>
              <el-switch v-model="notificationSettings.orderUpdates" />
            </div>
            <div class="settings-item">
              <div class="settings-text">
                <h4>动态互动通知</h4>
                <p>包括有人点赞或评论你的动态时的提醒。</p>
              </div>
              <el-switch v-model="notificationSettings.contentUpdates" />
            </div>
            <div class="settings-item">
              <div class="settings-text">
                <h4>系统公告通知</h4>
                <p>包括重要更新、维护公告等消息。</p>
              </div>
              <el-switch v-model="notificationSettings.systemNotices" />
            </div>
            <div class="settings-actions">
              <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 收藏与历史 -->
        <el-tab-pane label="收藏与历史" name="collections">
          <el-row :gutter="20" class="collections-row">
            <el-col :xs="24" :sm="12">
              <el-card class="sub-card" shadow="hover">
                <div class="sub-card-header">
                  <h3>我的收藏</h3>
                  <span class="sub-card-desc">你关注的订单或动态会显示在这里</span>
                </div>
                <div v-if="favoriteItems.length === 0" class="empty-wrap">
                  <el-empty description="暂无收藏" />
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-card class="sub-card" shadow="hover">
                <div class="sub-card-header">
                  <h3>最近浏览</h3>
                  <span class="sub-card-desc">你最近查看过的订单或动态</span>
                </div>
                <div v-if="historyItems.length === 0" class="empty-wrap">
                  <el-empty description="暂无浏览记录" />
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 帮助与反馈 -->
        <el-tab-pane label="帮助与反馈" name="help">
          <el-row :gutter="20" class="help-row">
            <el-col :xs="24" :md="12">
              <el-card class="sub-card" shadow="never">
                <h3>常见问题</h3>
                <ul class="faq-list">
                  <li>· 如何创建和管理预约订单？</li>
                  <li>· 被别人申请订单后如何沟通？</li>
                  <li>· 如何修改个人资料和头像？</li>
                  <li>· 忘记密码了怎么办？</li>
                </ul>
                <p class="faq-tip">更多详细说明将陆续完善，敬请期待。</p>
              </el-card>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-card class="sub-card" shadow="never">
                <h3>意见反馈</h3>
                <el-form :model="feedbackForm" label-width="80px" class="feedback-form">
                  <el-form-item label="反馈内容">
                    <el-input
                      v-model="feedbackForm.content"
                      type="textarea"
                      :rows="4"
                      maxlength="500"
                      show-word-limit
                      placeholder="使用过程中遇到的问题或建议，都可以告诉我们"
                    />
                  </el-form-item>
                  <el-form-item label="联系方式">
                    <el-input
                      v-model="feedbackForm.contact"
                      placeholder="可选，方便我们与你取得联系（邮箱/微信等）"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleSubmitFeedback">提交反馈</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useAuthStore } from '../stores/auth'

const userStore = useUserStore()
const authStore = useAuthStore()

const activeTab = ref('basic')
const avatarInputRef = ref(null)
const avatarUploading = ref(false)
const savingProfile = ref(false)
const changingPassword = ref(false)

const currentUser = computed(() => userStore.currentUser)

const currentUserInitial = computed(() => {
  if (currentUser.value?.nickname) {
    return currentUser.value.nickname.slice(0, 1)
  }
  if (currentUser.value?.email) {
    return currentUser.value.email.slice(0, 1).toUpperCase()
  }
  return '用'
})

const userTypeLabel = computed(() => {
  if (!currentUser.value) return '普通用户'
  const userType = currentUser.value.userType
  if (userType === 1 || userType === 'ADMIN') return '管理员'
  return '普通用户'
})

const editableUser = reactive({
  nickname: '',
  email: '',
  signature: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const privacySettings = reactive({
  showEmail: true,
  allowMessages: true,
  showInSearch: true
})

const notificationSettings = reactive({
  orderUpdates: true,
  contentUpdates: true,
  systemNotices: true
})

const favoriteItems = ref([])
const historyItems = ref([])

const feedbackForm = reactive({
  content: '',
  contact: ''
})

const PRIVACY_KEY = 'cc_privacy_settings'
const NOTIFY_KEY = 'cc_notification_settings'

const loadLocalSettings = () => {
  try {
    const savedPrivacy = localStorage.getItem(PRIVACY_KEY)
    if (savedPrivacy) {
      Object.assign(privacySettings, JSON.parse(savedPrivacy))
    }
    const savedNotify = localStorage.getItem(NOTIFY_KEY)
    if (savedNotify) {
      Object.assign(notificationSettings, JSON.parse(savedNotify))
    }
  } catch (e) {
    console.error('加载本地设置失败', e)
  }
}

const savePrivacySettings = () => {
  localStorage.setItem(PRIVACY_KEY, JSON.stringify(privacySettings))
  ElMessage.success('隐私设置已保存')
}

const saveNotificationSettings = () => {
  localStorage.setItem(NOTIFY_KEY, JSON.stringify(notificationSettings))
  ElMessage.success('通知设置已保存')
}

const fillEditableFromUser = () => {
  if (!currentUser.value) return
  editableUser.nickname = currentUser.value.nickname || ''
  editableUser.email = currentUser.value.email || ''
  editableUser.signature = currentUser.value.signature || ''
}

const initUser = async () => {
  try {
    if (!currentUser.value) {
      await userStore.fetchCurrentUser()
    }
    fillEditableFromUser()
  } catch (error) {
    console.error('加载用户信息失败', error)
    ElMessage.error(error?.message || '加载用户信息失败')
  }
}

const triggerAvatarSelect = () => {
  if (avatarInputRef.value) {
    avatarInputRef.value.click()
  }
}

const handleAvatarChange = async (event) => {
  const file = event.target.files && event.target.files[0]
  if (!file) return

  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('仅支持图片文件作为头像')
    event.target.value = ''
    return
  }

  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    event.target.value = ''
    return
  }

  try {
    avatarUploading.value = true
    await userStore.uploadAvatar(file)

    if (userStore.currentUser) {
      authStore.setUser({
        ...(authStore.user || {}),
        ...userStore.currentUser
      })
    }

    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('头像上传失败', error)
    ElMessage.error(error?.message || '头像上传失败')
  } finally {
    avatarUploading.value = false
    event.target.value = ''
  }
}

const handleSaveProfile = async () => {
  if (!editableUser.nickname.trim()) {
    ElMessage.error('昵称不能为空')
    return
  }

  try {
    savingProfile.value = true
    const payload = {
      nickname: editableUser.nickname.trim(),
      signature: editableUser.signature?.trim() || '',
      avatarUrl: currentUser.value?.avatarUrl || ''
    }
    await userStore.updateUser(payload)

    if (userStore.currentUser) {
      authStore.setUser({
        ...(authStore.user || {}),
        ...userStore.currentUser
      })
    }

    ElMessage.success('个人信息已更新')
  } catch (error) {
    console.error('更新个人信息失败', error)
    ElMessage.error(error?.message || '更新个人信息失败')
  } finally {
    savingProfile.value = false
  }
}

const resetProfile = () => {
  fillEditableFromUser()
  ElMessage.info('已恢复为当前保存的信息')
}

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.error('请完整填写所有密码字段')
    return
  }

  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
    ElMessage.error('新密码长度需在 6-20 位之间')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }

  try {
    changingPassword.value = true
    await userStore.updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败', error)
    const message = error?.message || error?.msg || '修改密码失败'
    ElMessage.error(message)
  } finally {
    changingPassword.value = false
  }
}

const handleSubmitFeedback = () => {
  if (!feedbackForm.content.trim()) {
    ElMessage.error('请先填写反馈内容')
    return
  }
  ElMessage.success('感谢你的反馈，我们会尽快查看')
  feedbackForm.content = ''
  feedbackForm.contact = ''
}

onMounted(async () => {
  loadLocalSettings()
  await initUser()
})
</script>

<style scoped lang="scss">
.user-profile-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-header-card {
  margin-bottom: 8px;
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar {
  border: 2px solid #e4e7ed;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
}

.hidden-file-input {
  display: none;
}

.base-info {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.email {
  font-size: 14px;
  color: #606266;
}

.signature {
  margin-top: 4px;
  font-size: 14px;
  color: #909399;
}

.meta-row {
  margin-top: 8px;
  font-size: 12px;
  color: #a0a3a6;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.profile-main-card {
  margin-top: 4px;
}

.profile-form,
.security-form,
.feedback-form {
  max-width: 520px;
}

.section-description {
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.security-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

.settings-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.settings-text {
  max-width: 80%;
}

.settings-text h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.settings-text p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.settings-actions {
  margin-top: 8px;
}

.collections-row,
.help-row {
  margin-top: 4px;
}

.sub-card {
  height: 100%;
}

.sub-card-header {
  margin-bottom: 8px;
}

.sub-card-header h3 {
  margin: 0;
  font-size: 16px;
}

.sub-card-desc {
  font-size: 12px;
  color: #909399;
}

.empty-wrap {
  padding: 12px 0;
}

.faq-list {
  list-style: none;
  padding: 0;
  margin: 8px 0;
  font-size: 13px;
  color: #606266;
}

.faq-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .avatar-area {
    flex-direction: row;
    align-items: center;
    gap: 16px;
  }

  .settings-item {
    align-items: flex-start;
  }

  .settings-text {
    max-width: 70%;
  }
}
</style>
