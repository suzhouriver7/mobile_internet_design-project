<template>
  <view class="create-order-container">
    <view class="form">
      <view class="form-item">
        <text class="label">活动类型 *</text>
        <picker mode="selector" :range="activityTypeOptions" range-key="label" @change="onActivityTypeChange">
          <view class="picker-view">
            <text :class="form.activityType ? '' : 'placeholder'">
              {{ form.activityType ? getActivityTypeText(form.activityType) : '请选择活动类型' }}
            </text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">性别要求 *</text>
        <picker mode="selector" :range="genderOptions" range-key="label" @change="onGenderChange">
          <view class="picker-view">
            <text :class="form.genderRequire ? '' : 'placeholder'">
              {{ form.genderRequire ? getGenderText(form.genderRequire) : '请选择性别要求' }}
            </text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">校区 *</text>
        <picker mode="selector" :range="campusOptions" range-key="label" @change="onCampusChange">
          <view class="picker-view">
            <text :class="form.campus ? '' : 'placeholder'">
              {{ form.campus ? getCampusText(form.campus) : '请选择校区' }}
            </text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">活动地点 *</text>
        <input v-model="form.location" class="input" placeholder="请输入活动地点" />
      </view>
      
      <view class="form-item">
        <text class="label">开始时间 *</text>
        <picker mode="date" :value="dateValue" :start="minDate" @change="onDateChange">
          <view class="picker-view">
            <text>{{ dateValue || '请选择日期' }}</text>
          </view>
        </picker>
        <picker mode="time" :value="timeValue" @change="onTimeChange">
          <view class="picker-view">
            <text>{{ timeValue || '请选择时间' }}</text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">人数上限 *</text>
        <input v-model.number="form.maxPeople" class="input" type="number" placeholder="请输入人数上限" />
      </view>
      
      <view class="form-item">
        <text class="label">备注</text>
        <textarea v-model="form.note" class="textarea" placeholder="请输入备注信息" maxlength="200" />
      </view>
      
      <button class="submit-btn" :loading="loading" @click="handleSubmit">发布活动</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { orderApi } from '@/api/index.js'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util.js'
import { ACTIVITY_TYPE, ACTIVITY_TYPE_MAP, GENDER_REQUIRE, GENDER_REQUIRE_MAP, CAMPUS, CAMPUS_MAP } from '@/utils/constants.js'

const store = useStore()

// 检查登录状态 - 使用 onLoad 页面生命周期
onLoad(() => {
  if (!store.getters['user/isLogin']) {
    showError('请先登录')
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }, 1000)
  }
})

const form = ref({
  activityType: null,
  genderRequire: null,
  campus: null,
  location: '',
  startTime: '',
  maxPeople: 4,
  note: ''
})

const dateValue = ref('')
const timeValue = ref('')
const loading = ref(false)

// 获取最小日期（今天）
const getMinDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const minDate = ref(getMinDate())

const activityTypeOptions = Object.keys(ACTIVITY_TYPE).map(key => ({
  value: ACTIVITY_TYPE[key],
  label: ACTIVITY_TYPE_MAP[ACTIVITY_TYPE[key]]
}))

const genderOptions = Object.keys(GENDER_REQUIRE).map(key => ({
  value: GENDER_REQUIRE[key],
  label: GENDER_REQUIRE_MAP[GENDER_REQUIRE[key]]
}))

const campusOptions = Object.keys(CAMPUS).map(key => ({
  value: CAMPUS[key],
  label: CAMPUS_MAP[CAMPUS[key]]
}))

const getActivityTypeText = (type) => {
  return ACTIVITY_TYPE_MAP[type] || ''
}

const getGenderText = (gender) => {
  return GENDER_REQUIRE_MAP[gender] || ''
}

const getCampusText = (campus) => {
  return CAMPUS_MAP[campus] || ''
}

const onActivityTypeChange = (e) => {
  form.value.activityType = activityTypeOptions[e.detail.value].value
}

const onGenderChange = (e) => {
  form.value.genderRequire = genderOptions[e.detail.value].value
}

const onCampusChange = (e) => {
  form.value.campus = campusOptions[e.detail.value].value
}

const onDateChange = (e) => {
  dateValue.value = e.detail.value
  updateStartTime()
}

const onTimeChange = (e) => {
  timeValue.value = e.detail.value
  updateStartTime()
}

const updateStartTime = () => {
  if (dateValue.value && timeValue.value) {
    form.value.startTime = `${dateValue.value} ${timeValue.value}:00`
  }
}

const handleSubmit = async () => {
  // 检查登录状态
  if (!store.getters['user/isLogin']) {
    showError('请先登录')
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }, 1000)
    return
  }
  
  // 验证必填项
  if (!form.value.activityType || !form.value.genderRequire || !form.value.campus ||
      !form.value.location || !form.value.startTime || !form.value.maxPeople) {
    showError('请填写完整信息')
    return
  }
  
  if (form.value.maxPeople < 2 || form.value.maxPeople > 20) {
    showError('人数上限应在2-20之间')
    return
  }
  
  // 验证开始时间必须大于当前时间
  if (form.value.startTime) {
    const startTime = new Date(form.value.startTime)
    const now = new Date()
    
    if (startTime <= now) {
      showError('开始时间必须大于当前时间')
      return
    }
  }
  
  loading.value = true
  showLoading('发布中...')
  
  try {
    await orderApi.createOrder(form.value)
    showSuccess('发布成功')
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (error) {
    showError(error.message || '发布失败')
  } finally {
    loading.value = false
    hideLoading()
  }
}
</script>

<style scoped>
.create-order-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 30rpx;
}

.form {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 40rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 16rpx;
}

.input,
.textarea {
  width: 100%;
  padding: 24rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.textarea {
  min-height: 200rpx;
}

.picker-view {
  padding: 24rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333333;
}

.placeholder {
  color: #999999;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-top: 40rpx;
}
</style>

