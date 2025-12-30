<template>
  <view class="create-order">
    <uni-nav-bar title="发布订单" left-arrow @clickLeft="onBack"></uni-nav-bar>
    
    <uni-form ref="formRef" :model="form" labelWidth="140rpx">
      <uni-form-item label="活动类型" required>
        <uni-select 
          v-model="form.activityType" 
          placeholder="请选择活动类型"
          @change="onSelectChange"
        >
          <uni-select-option label="篮球" value="BASKETBALL"></uni-select-option>
          <uni-select-option label="羽毛球" value="BADMINTON"></uni-select-option>
          <uni-select-option label="吃饭" value="MEAL"></uni-select-option>
          <uni-select-option label="自习" value="STUDY"></uni-select-option>
          <uni-select-option label="看电影" value="MOVIE"></uni-select-option>
          <uni-select-option label="跑步" value="RUNNING"></uni-select-option>
          <uni-select-option label="游戏" value="GAME"></uni-select-option>
          <uni-select-option label="其他" value="OTHER"></uni-select-option>
        </uni-select>
      </uni-form-item>
      
      <uni-form-item label="性别要求" required>
        <uni-radio-group v-model="form.genderRequire">
          <uni-radio label="不限" value="ANY"></uni-radio>
          <uni-radio label="仅男生" value="MALE"></uni-radio>
          <uni-radio label="仅女生" value="FEMALE"></uni-radio>
        </uni-radio-group>
      </uni-form-item>
      
      <uni-form-item label="校区" required>
        <uni-select v-model="form.campus" placeholder="请选择校区">
          <uni-select-option label="良乡校区" value="LIANGXIANG"></uni-select-option>
          <uni-select-option label="中关村校区" value="ZHONGGUANCUN"></uni-select-option>
        </uni-select>
      </uni-form-item>
      
      <uni-form-item label="活动地点" required>
        <uni-easyinput v-model="form.location" placeholder="请输入活动地点"></uni-easyinput>
      </uni-form-item>
      
      <uni-form-item label="开始时间" required>
        <uni-easyinput 
          v-model="form.startTime" 
          placeholder="选择开始时间"
          @click="showDatePicker = true"
          readonly
        ></uni-easyinput>
        <uni-datetime-picker
          type="datetime"
          v-model="form.startTime"
          :show="showDatePicker"
          @confirm="showDatePicker = false"
          @cancel="showDatePicker = false"
        ></uni-datetime-picker>
      </uni-form-item>
      
      <uni-form-item label="人数上限" required>
        <uni-easyinput 
          v-model="form.maxPeople" 
          type="number" 
          placeholder="请输入人数上限"
        ></uni-easyinput>
      </uni-form-item>
      
      <uni-form-item label="备注">
        <uni-easyinput 
          v-model="form.note" 
          type="textarea" 
          rows="3"
          placeholder="请输入备注信息"
        ></uni-easyinput>
      </uni-form-item>
      
      <view class="btn-submit">
        <uni-button type="primary" @click="handleSubmit">发布</uni-button>
      </view>
    </uni-form>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { createOrder } from '@/api/order'

const formRef = ref(null)
const showDatePicker = ref(false)
const form = reactive({
  activityType: '',
  genderRequire: 'ANY',
  campus: '',
  location: '',
  startTime: '',
  maxPeople: 2,
  note: ''
})

// 提交订单
const handleSubmit = async () => {
  if (!form.activityType) {
    return uni.showToast({ title: '请选择活动类型', icon: 'none' })
  }
  if (!form.campus) {
    return uni.showToast({ title: '请选择校区', icon: 'none' })
  }
  if (!form.location) {
    return uni.showToast({ title: '请输入活动地点', icon: 'none' })
  }
  if (!form.startTime) {
    return uni.showToast({ title: '请选择开始时间', icon: 'none' })
  }
  
  try {
    await createOrder(form)
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (err) {
    uni.showToast({ title: err.message || '发布失败', icon: 'none' })
  }
}

// 返回上一页
const onBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.btn-submit {
  padding: 30rpx;
}
</style>