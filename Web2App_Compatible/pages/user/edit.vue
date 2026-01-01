<template>
  <view class="edit-page">
    <uni-nav-bar title="编辑信息" left-arrow @clickLeft="onBack"></uni-nav-bar>
    
    <uni-form ref="formRef" :model="form">
      <uni-form-item :label="getLabel()">
        <template v-if="field === 'gender'">
          <uni-radio-group v-model="form.value">
            <uni-radio label="男" value="MALE"></uni-radio>
            <uni-radio label="女" value="FEMALE"></uni-radio>
          </uni-radio-group>
        </template>
        
        <template v-if="field === 'campus'">
          <uni-select v-model="form.value" placeholder="选择校区">
            <uni-select-option label="良乡校区" value="LIANGXIANG"></uni-select-option>
            <uni-select-option label="中关村校区" value="ZHONGGUANCUN"></uni-select-option>
          </uni-select>
        </template>
        
        <template v-if="field === 'introduction'">
          <uni-easyinput 
            v-model="form.value" 
            type="textarea" 
            rows="4"
            placeholder="请输入个人简介"
          ></uni-easyinput>
        </template>
      </uni-form-item>
    </uni-form>
    
    <view class="btn-submit">
      <uni-button type="primary" @click="handleSubmit">保存</uni-button>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { useUserStore } from '@/stores/user'

const formRef = ref(null)
const userStore = useUserStore()
const field = ref('')
const form = ref({ value: '' })

// 接收参数
onLoad((options) => {
  field.value = options.field
  form.value.value = JSON.parse(options.value || '""')
})

// 根据字段获取标签
const getLabel = () => {
  const labels = {
    gender: '性别',
    campus: '校区',
    introduction: '个人简介'
  }
  return labels[field.value] || ''
}

// 提交修改
const handleSubmit = async () => {
  try {
    const updateData = { [field.value]: form.value.value }
    await userStore.updateUserInfo(updateData)
    uni.showToast({ title: '修改成功', icon: 'success' })
    // 返回上一页并刷新
    setTimeout(() => {
      uni.navigateBack({ delta: 1 })
    }, 1000)
  } catch (err) {
    uni.showToast({ title: err.message || '修改失败', icon: 'none' })
  }
}

// 返回上一页
const onBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.edit-page {
  background-color: #fff;
  min-height: 100vh;
}

.btn-submit {
  padding: 30rpx;
}
</style>