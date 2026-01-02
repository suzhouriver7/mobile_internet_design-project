<template>
  <view class="test-container">
    <text class="test-title">测试页面</text>
    <text class="test-desc">如果你能看到这个页面，说明项目运行正常！</text>
    <button class="test-btn" @click="handleClick">点击测试</button>
    <text class="test-info">当前时间: {{ currentTime }}</text>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentTime: new Date().toLocaleString()
    }
  },
  onLoad() {
    console.log('页面加载')
  },
  onShow() {
    console.log('页面显示')
    this.currentTime = new Date().toLocaleString()
  },
  methods: {
    handleClick() {
      uni.showToast({
        title: '点击成功！',
        icon: 'success'
      })
    }
  }
}
</script>

<style scoped>
.test-container {
  padding: 100rpx 50rpx;
  text-align: center;
  min-height: 100vh;
  background: #f8f8f8;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.test-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
  display: block;
}

.test-desc {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 50rpx;
  display: block;
}

.test-btn {
  width: 300rpx;
  height: 88rpx;
  background: #007AFF;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
  margin-bottom: 40rpx;
}

.test-info {
  font-size: 24rpx;
  color: #999999;
  display: block;
}
</style>

