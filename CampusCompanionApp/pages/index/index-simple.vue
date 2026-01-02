<template>
  <view class="test-page">
    <text class="test-text">测试页面 - 如果你看到这个，说明项目运行正常！</text>
    <button @click="testClick">点击测试</button>
  </view>
</template>

<script setup>
const testClick = () => {
  uni.showToast({
    title: '点击成功！',
    icon: 'success'
  })
}
</script>

<style scoped>
.test-page {
  padding: 100rpx 50rpx;
  text-align: center;
}

.test-text {
  font-size: 32rpx;
  color: #333333;
  display: block;
  margin-bottom: 50rpx;
}
</style>

