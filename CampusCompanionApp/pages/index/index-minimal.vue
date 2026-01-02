<template>
  <view class="container">
    <text class="title">校园约伴</text>
    <text class="subtitle">首页</text>
    <button class="btn" @click="test">测试按钮</button>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  onLoad() {
    console.log('首页加载')
  },
  onShow() {
    console.log('首页显示')
  },
  methods: {
    test() {
      uni.showToast({
        title: '测试成功',
        icon: 'success'
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 100rpx 50rpx;
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
}

.subtitle {
  font-size: 32rpx;
  color: #666666;
  margin-bottom: 50rpx;
}

.btn {
  width: 300rpx;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
}
</style>

