<template>
  <view class="u-empty" v-show="visible">
    <image class="empty-image" :src="image" mode="widthFix"></image>
    <text class="empty-text">{{ text }}</text>
    <button class="empty-btn" v-if="btnText" @click="handleBtnClick">{{ btnText }}</button>
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
    // 空数据图片
    image: {
      type: String,
      default: '/static/images/empty/default.png'
    },
    // 空数据提示文本
    text: {
      type: String,
      default: '暂无相关数据'
    },
    // 按钮文本
    btnText: {
      type: String,
      default: ''
    }
  },
  methods: {
    // 按钮点击事件（派发事件，避免组件耦合Bug）
    handleBtnClick() {
      this.$emit('btn-click')
    }
  }
}
</script>

<style scoped>
.u-empty {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.empty-image {
  width: 200rpx;
  height: auto;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
  margin-bottom: 40rpx;
}

.empty-btn {
  width: 200rpx;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #1890FF;
  color: #FFFFFF;
  font-size: 28rpx;
  border-radius: 40rpx;
}
</style>