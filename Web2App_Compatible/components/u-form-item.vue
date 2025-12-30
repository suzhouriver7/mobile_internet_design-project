<template>
  <view class="u-form-item">
    <view class="form-label" :style="{ width: labelWidth }">
      <text class="label-text" :class="{ required: required }">{{ label }}</text>
    </view>
    <view class="form-content" :style="{ flex: 1, marginLeft: labelMargin }">
      <slot></slot>
    </view>
  </view>
</template>

<script>
export default {
  props: {
    // 标签文本
    label: {
      type: String,
      default: ''
    },
    // 是否必填
    required: {
      type: Boolean,
      default: false
    },
    // 标签宽度
    labelWidth: {
      type: String,
      default: '160rpx'
    },
    // 标签右侧间距
    labelMargin: {
      type: String,
      default: '40rpx'
    }
  }
}
</script>

<style scoped>
.u-form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #EEEEEE;
}

.form-label {
  display: flex;
  align-items: center;
}

.label-text {
  font-size: 28rpx;
  color: #333333;
}

.required {
  color: #FF4D4F;
  position: relative;
}

.required::before {
  content: '*';
  position: absolute;
  left: -20rpx;
  top: 0;
}

.form-content {
  font-size: 28rpx;
  color: #333333;
}
</style>