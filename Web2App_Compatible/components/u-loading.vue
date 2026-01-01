<template>
  <view class="u-loading" v-show="visible">
    <view class="loading-spinner"></view>
    <text class="loading-text">{{ text }}</text>
  </view>
</template>

<script>
export default {
  props: {
    // 是否显示
    visible: {
      type: Boolean,
      default: false
    },
    // 加载提示文本
    text: {
      type: String,
      default: '加载中...'
    }
  }
}
</script>

<style scoped>
.u-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 28rpx;
  color: #FFFFFF;
  margin-top: 30rpx;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>