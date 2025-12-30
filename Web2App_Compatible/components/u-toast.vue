<template>
  <view class="u-toast" v-show="visible" :style="{ backgroundColor: bgColor, color: textColor }">
    <text class="toast-text">{{ text }}</text>
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
    // 提示文本
    text: {
      type: String,
      default: ''
    },
    // 背景颜色
    bgColor: {
      type: String,
      default: 'rgba(0, 0, 0, 0.7)'
    },
    // 文本颜色
    textColor: {
      type: String,
      default: '#FFFFFF'
    },
    // 显示时长
    duration: {
      type: Number,
      default: 3000
    }
  },
  watch: {
    // 监听visible变化，自动隐藏
    visible(newVal) {
      if (newVal) {
        clearTimeout(this.timer)
        this.timer = setTimeout(() => {
          this.$emit('update:visible', false)
        }, this.duration)
      }
    }
  },
  data() {
    return {
      timer: null
    }
  },
  beforeDestroy() {
    // 销毁定时器，避免内存泄漏Bug
    clearTimeout(this.timer)
  }
}
</script>

<style scoped>
.u-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 20rpx 40rpx;
  border-radius: 10rpx;
  font-size: 28rpx;
  z-index: 9999;
  max-width: 80%;
  text-align: center;
}
</style>