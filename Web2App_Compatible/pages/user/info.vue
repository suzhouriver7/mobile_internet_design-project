<template>
  <view class="user-info-page">
    <!-- 头像与基本信息 -->
    <view class="user-header">
      <view class="avatar-container">
        <uni-image 
          class="avatar" 
          :src="userInfo.avatar || '/static/images/default-avatar.png'"
          mode="widthFix"
        ></uni-image>
        <uni-icon 
          class="edit-avatar" 
          type="camera" 
          size="24" 
          @click="chooseAvatar"
        ></uni-icon>
      </view>
      <view class="user-base">
        <text class="nickname">{{ userInfo.nickname || '未设置' }}</text>
        <text class="student-id">{{ userInfo.studentId || '未绑定学号' }}</text>
      </view>
    </view>

    <!-- 信息列表 -->
    <uni-list>
      <uni-list-item 
        title="性别" 
        :note="userInfo.gender === 'MALE' ? '男' : '女'" 
        clickable 
        @click="navigateToEdit('gender')"
      ></uni-list-item>
      <uni-list-item 
        title="校区" 
        :note="userInfo.campus || '未设置'" 
        clickable 
        @click="navigateToEdit('campus')"
      ></uni-list-item>
      <uni-list-item 
        title="简介" 
        :note="userInfo.introduction || '未填写'" 
        clickable 
        @click="navigateToEdit('introduction')"
      ></uni-list-item>
      <uni-list-item 
        title="修改密码" 
        clickable 
        @click="navigateToChangePwd"
      ></uni-list-item>
    </uni-list>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { useUserStore } from '@/stores/user'
import { uploadFile } from '@/utils/request'

const userStore = useUserStore()
const userInfo = ref({})

// 页面加载时获取用户信息
onLoad(async () => {
  try {
    const res = await userStore.getUserInfo()
    userInfo.value = res.data
  } catch (err) {
    uni.showToast({ title: '获取信息失败', icon: 'none' })
  }
})

// 选择头像并上传
const chooseAvatar = async () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      try {
        // 调用封装的文件上传接口
        const avatarUrl = await uploadFile({
          url: '/users/avatar',
          filePath: tempFilePath,
          name: 'avatar'
        })
        // 更新本地头像显示
        userInfo.value.avatar = avatarUrl
        uni.showToast({ title: '上传成功', icon: 'success' })
      } catch (err) {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    }
  })
}

// 跳转到编辑页
const navigateToEdit = (field) => {
  uni.navigateTo({
    url: `/pages/user/edit?field=${field}&value=${JSON.stringify(userInfo.value[field])}`
  })
}

// 跳转到修改密码页
const navigateToChangePwd = () => {
  uni.navigateTo({ url: '/pages/user/change-pwd' })
}
</script>

<style scoped>
.user-header {
  display: flex;
  padding: 30rpx;
  align-items: center;
  border-bottom: 1px solid #f5f5f5;
}

.avatar-container {
  position: relative;
  margin-right: 30rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
}

.edit-avatar {
  position: absolute;
  bottom: 0;
  right: 0;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  padding: 5rpx;
}

.user-base {
  flex: 1;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
}

.student-id {
  font-size: 28rpx;
  color: #666;
}
</style>