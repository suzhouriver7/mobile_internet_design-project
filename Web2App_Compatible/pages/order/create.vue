<template>
  <view class="create-order-page">
    <scroll-view class="form-scroll" scroll-y>
      <view class="form-container">
        <!-- 活动类型 -->
        <view class="form-item">
          <text class="form-label">活动类型<text class="required">*</text></text>
          <view class="form-value" @click="showActivityTypePicker = true">
            <text :class="{'placeholder': !form.activityType}">
              {{ form.activityType !== null ? ACTIVITY_TYPE_MAP[form.activityType] : '请选择活动类型' }}
            </text>
            <u-icon name="arrow-right" size="16" color="#999"></u-icon>
          </view>
          <text class="error-text" v-if="errors.activityType">{{ errors.activityType }}</text>
        </view>

        <!-- 性别要求 -->
        <view class="form-item">
          <text class="form-label">性别要求<text class="required">*</text></text>
          <view class="radio-group">
            <view 
              class="radio-item" 
              v-for="(label, value) in GENDER_REQUIRE_MAP" 
              :key="value"
              @click="form.genderRequire = parseInt(value)"
            >
              <u-radio 
                :name="parseInt(value)" 
                v-model="form.genderRequire"
                :label="label"
                shape="circle"
              ></u-radio>
            </view>
          </view>
          <text class="error-text" v-if="errors.genderRequire">{{ errors.genderRequire }}</text>
        </view>

        <!-- 校区 -->
        <view class="form-item">
          <text class="form-label">校区<text class="required">*</text></text>
          <view class="form-value" @click="showCampusPicker = true">
            <text :class="{'placeholder': form.campus === null}">
              {{ form.campus !== null ? CAMPUS_MAP[form.campus] : '请选择校区' }}
            </text>
            <u-icon name="arrow-right" size="16" color="#999"></u-icon>
          </view>
          <text class="error-text" v-if="errors.campus">{{ errors.campus }}</text>
        </view>

        <!-- 活动地点 -->
        <view class="form-item">
          <text class="form-label">活动地点<text class="required">*</text></text>
          <u-input
            v-model="form.location"
            placeholder="请输入活动地点"
            border="bottom"
            clearable
          ></u-input>
          <text class="error-text" v-if="errors.location">{{ errors.location }}</text>
        </view>

        <!-- 开始时间 -->
        <view class="form-item">
          <text class="form-label">开始时间<text class="required">*</text></text>
          <view class="form-value" @click="showDateTimePicker = true">
            <text :class="{'placeholder': !form.startTime}">
              {{ form.startTime || '请选择开始时间' }}
            </text>
            <u-icon name="arrow-right" size="16" color="#999"></u-icon>
          </view>
          <text class="error-text" v-if="errors.startTime">{{ errors.startTime }}</text>
        </view>

        <!-- 人数上限 -->
        <view class="form-item">
          <text class="form-label">人数上限<text class="required">*</text></text>
          <u-input
            v-model="form.maxPeople"
            type="number"
            placeholder="请输入需要的人数上限"
            border="bottom"
            clearable
          ></u-input>
          <text class="hint-text">包括您自己在内，最多需要多少人</text>
          <text class="error-text" v-if="errors.maxPeople">{{ errors.maxPeople }}</text>
        </view>

        <!-- 备注 -->
        <view class="form-item">
          <text class="form-label">备注</text>
          <u-textarea
            v-model="form.note"
            placeholder="请输入备注信息（选填）"
            count
            :maxlength="200"
            height="120"
          ></u-textarea>
        </view>
      </view>
    </scroll-view>

    <!-- 提交按钮 -->
    <view class="submit-bar">
      <u-button
        type="primary"
        shape="circle"
        :loading="loading"
        :disabled="!canSubmit"
        @click="onSubmit"
        :custom-style="{
          height: '88rpx',
          fontSize: '32rpx',
          fontWeight: '500'
        }"
      >
        {{ loading ? '发布中...' : '发布活动' }}
      </u-button>
    </view>

    <!-- 活动类型选择器 -->
    <u-picker
      :show="showActivityTypePicker"
      :columns="activityTypeColumns"
      @confirm="onActivityTypeConfirm"
      @cancel="showActivityTypePicker = false"
    ></u-picker>

    <!-- 校区选择器 -->
    <u-picker
      :show="showCampusPicker"
      :columns="campusColumns"
      @confirm="onCampusConfirm"
      @cancel="showCampusPicker = false"
    ></u-picker>

    <!-- 日期时间选择器 -->
    <u-datetime-picker
      :show="showDateTimePicker"
      v-model="dateTimeValue"
      mode="datetime"
      @confirm="onDateTimeConfirm"
      @cancel="showDateTimePicker = false"
    ></u-datetime-picker>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { orderApi } from '@/api'
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/util'
import { 
  ACTIVITY_TYPE_MAP, 
  CAMPUS_MAP, 
  GENDER_REQUIRE_MAP,
  ACTIVITY_TYPE,
  CAMPUS,
  GENDER_REQUIRE
} from '@/utils/constants'
import dayjs from 'dayjs'

// 使用Vuex store
const store = useStore()
const isLogin = computed(() => store.getters.isLogin)

// 响应式数据
const form = reactive({
  activityType: null,
  genderRequire: GENDER_REQUIRE.ANY,
  campus: null,
  location: '',
  startTime: '',
  maxPeople: '',
  note: ''
})

const errors = reactive({
  activityType: '',
  genderRequire: '',
  campus: '',
  location: '',
  startTime: '',
  maxPeople: ''
})

const loading = ref(false)
const showActivityTypePicker = ref(false)
const showCampusPicker = ref(false)
const showDateTimePicker = ref(false)
const dateTimeValue = ref(Date.now())

// 选择器数据
const activityTypeColumns = ref([[
  { text: '篮球', value: ACTIVITY_TYPE.BASKETBALL },
  { text: '羽毛球', value: ACTIVITY_TYPE.BADMINTON },
  { text: '吃饭', value: ACTIVITY_TYPE.MEAL },
  { text: '自习', value: ACTIVITY_TYPE.STUDY },
  { text: '看电影', value: ACTIVITY_TYPE.MOVIE },
  { text: '跑步', value: ACTIVITY_TYPE.RUNNING },
  { text: '游戏', value: ACTIVITY_TYPE.GAME },
  { text: '其他', value: ACTIVITY_TYPE.OTHER }
]])

const campusColumns = ref([[
  { text: '良乡校区', value: CAMPUS.LIANGXIANG },
  { text: '中关村校区', value: CAMPUS.ZHONGGUANCUN },
  { text: '珠海校区', value: CAMPUS.ZHUHAI },
  { text: '西山校区', value: CAMPUS.XISHAN },
  { text: '其他校区', value: CAMPUS.OTHER_CAMPUS }
]])

// 计算属性
const canSubmit = computed(() => {
  return form.activityType !== null &&
         form.genderRequire !== null &&
         form.campus !== null &&
         form.location.trim() &&
         form.startTime &&
         form.maxPeople
})

// 生命周期
onLoad(() => {
  if (!isLogin.value) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      showCancel: false,
      success: () => {
        uni.navigateTo({
          url: '/pages/auth/login'
        })
      }
    })
  }
})

// 方法
const validateForm = () => {
  let isValid = true
  
  // 清空错误
  Object.keys(errors).forEach(key => errors[key] = '')
  
  // 验证活动类型
  if (form.activityType === null) {
    errors.activityType = '请选择活动类型'
    isValid = false
  }
  
  // 验证性别要求
  if (form.genderRequire === null) {
    errors.genderRequire = '请选择性别要求'
    isValid = false
  }
  
  // 验证校区
  if (form.campus === null) {
    errors.campus = '请选择校区'
    isValid = false
  }
  
  // 验证活动地点
  if (!form.location.trim()) {
    errors.location = '请输入活动地点'
    isValid = false
  }
  
  // 验证开始时间
  if (!form.startTime) {
    errors.startTime = '请选择开始时间'
    isValid = false
  } else {
    // 验证时间不能是过去
    const selectedTime = dayjs(form.startTime)
    if (selectedTime.isBefore(dayjs())) {
      errors.startTime = '开始时间不能是过去'
      isValid = false
    }
  }
  
  // 验证人数上限
  if (!form.maxPeople) {
    errors.maxPeople = '请输入人数上限'
    isValid = false
  } else {
    const maxPeopleNum = parseInt(form.maxPeople)
    if (isNaN(maxPeopleNum) || maxPeopleNum < 2 || maxPeopleNum > 50) {
      errors.maxPeople = '人数上限需在2-50之间'
      isValid = false
    }
  }
  
  return isValid
}

const onActivityTypeConfirm = (e) => {
  form.activityType = e.value[0].value
  showActivityTypePicker.value = false
}

const onCampusConfirm = (e) => {
  form.campus = e.value[0].value
  showCampusPicker.value = false
}

const onDateTimeConfirm = (e) => {
  const date = dayjs(e.value)
  form.startTime = date.format('YYYY-MM-DD HH:mm:ss')
  showDateTimePicker.value = false
}

const onSubmit = async () => {
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  showLoading('发布中...')
  
  try {
    const orderData = {
      activityType: form.activityType,
      genderRequire: form.genderRequire,
      campus: form.campus,
      location: form.location.trim(),
      startTime: form.startTime,
      maxPeople: parseInt(form.maxPeople),
      note: form.note.trim() || undefined
    }
    
    const orderId = await orderApi.createOrder(orderData)
    
    showSuccess('发布成功')
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
    
  } catch (error) {
    console.error('发布订单失败:', error)
    if (!error.message.includes('业务错误')) {
      showError('发布失败，请稍后重试')
    }
  } finally {
    loading.value = false
    hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.create-order-page {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 120rpx;
}

.form-scroll {
  height: calc(100vh - 120rpx);
}

.form-container {
  padding: 32rpx;
}

.form-item {
  margin-bottom: 48rpx;
  
  .form-label {
    display: block;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 20rpx;
    
    .required {
      color: #dd524d;
      margin-left: 4rpx;
    }
  }
  
  .form-value {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #e5e5e5;
    
    .placeholder {
      color: #999;
    }
  }
  
  .radio-group {
    display: flex;
    gap: 32rpx;
    padding: 20rpx 0;
    
    .radio-item {
      display: flex;
      align-items: center;
    }
  }
  
  .error-text {
    display: block;
    font-size: 24rpx;
    color: #dd524d;
    margin-top: 12rpx;
  }
  
  .hint-text {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-top: 12rpx;
  }
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: white;
  border-top: 1rpx solid #f0f0f0;
  z-index: 999;
}
</style>
