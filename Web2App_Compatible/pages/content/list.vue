<template>
  <view class="content-list-page">
    <!-- 发布按钮 -->
    <view class="fab" @click="toCreateContent" v-if="isLogin">
      <u-icon name="plus" size="24" color="#fff"></u-icon>
    </view>

    <!-- 动态列表 -->
    <scroll-view 
      class="content-list" 
      scroll-y 
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-if="loading && contentList.length === 0" class="loading-container">
        <u-loading mode="circle" :show="true"></u-loading>
      </view>
      
      <view v-else-if="contentList.length === 0" class="empty-container">
        <u-empty
          mode="list"
          icon="http://cdn.uviewui.com/uview/empty/list.png"
          text="暂无动态"
        >
          <u-button 
            type="primary" 
            size="mini" 
            @click="toCreateContent"
            v-if="isLogin"
          >去发布动态</u-button>
        </u-empty>
      </view>
      
      <view v-else>
        <view 
          class="content-card" 
          v-for="content in contentList" 
          :key="content.id"
          @click="toContentDetail(content.id)"
        >
          <view class="content-header">
            <u-avatar 
              :src="resolveResourceUrl(content.user?.avatarUrl)" 
              size="36"
            ></u-avatar>
            <view class="content-user-info">
              <text class="content-user">{{ content.user?.nickname || '用户' }}</text>
              <text class="content-time">{{ formatRelativeTime(content.createdAt) }}</text>
            </view>
            <view class="content-stats">
              <text class="content-stat">
                <u-icon name="thumb-up" size="14"></u-icon>
                {{ content.likeCount || 0 }}
              </text>
              <text class="content-stat">
                <u-icon name="chat" size="14"></u-icon>
                {{ content.commentCount || 0 }}
              </text>
            </view>
          </view>
          
          <view class="content-body">
            <text class="content-text">{{ content.content }}</text>
            
            <!-- 动态图片预览 -->
            <view 
              class="content-images" 
              v-if="content.media && content.media.length > 0"
            >
              <image 
                v-for="(media, index) in content.media.slice(0, 3)" 
                :key="index"
                :src="resolveResourceUrl(media.url)"
                mode="aspectFill"
                class="content-image"
                @click.stop="previewImage(content.media, index)"
              />
              <view 
                class="image-more" 
                v-if="content.media.length > 3"
                @click.stop="previewImage(content.media, 3)"
              >
                +{{ content.media.length - 3 }}
              </view>
            </view>
          </view>
          
          <!-- 关联的订单 -->
          <view 
            class="content-order" 
            v-if="content.order"
            @click.stop="toOrderDetail(content.order.id)"
          >
            <view class="order-tag">
              <u-icon name="calendar" size="12"></u-icon>
              关联活动：{{ getActivityTypeText(content.order.activityType) }}
            </view>
          </view>
        </view>
      </view>
      
      <!-- 加载更多 -->
      <view class="load-more" v-if="contentList.length > 0">
        <u-loading 
          mode="circle" 
          :show="loadingMore"
          v-if="loadingMore"
        ></u-loading>
        <text v-else-if="noMore" class="no-more-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { contentApi } from '@/api'
import { formatRelativeTime, resolveResourceUrl, showLoading, hideLoading } from '@/utils/util'
import { ACTIVITY_TYPE_MAP } from '@/utils/constants'

// 使用Vuex store
const store = useStore()
const isLogin = computed(() => store.getters.isLogin)

// 响应式数据
const contentList = ref([])
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const noMore = ref(false)

// 生命周期
onMounted(() => {
  loadContents(true)
})

onShow(() => {
  loadContents(true)
})

onPullDownRefresh(() => {
  onRefresh()
})

// 方法
const loadContents = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    loading.value = true
    noMore.value = false
  } else {
    if (noMore.value || loadingMore.value) return
    loadingMore.value = true
  }
  
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    
    const response = await contentApi.getContents(params)
    const list = response?.list || []
    
    if (isRefresh) {
      contentList.value = list
    } else {
      contentList.value = [...contentList.value, ...list]
    }
    
    // 判断是否还有更多
    const total = response?.total || 0
    if (contentList.value.length >= total) {
      noMore.value = true
    }
    
  } catch (error) {
    console.error('加载动态列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
    refreshing.value = false
    uni.stopPullDownRefresh()
  }
}

const onRefresh = () => {
  refreshing.value = true
  loadContents(true)
}

const loadMore = () => {
  if (noMore.value || loadingMore.value) return
  page.value++
  loadContents(false)
}

const getActivityTypeText = (type) => {
  return ACTIVITY_TYPE_MAP[type] || '未知活动'
}

const previewImage = (mediaList, current) => {
  const urls = mediaList.map(m => resolveResourceUrl(m.url))
  uni.previewImage({
    urls: urls,
    current: current
  })
}

const toContentDetail = (contentId) => {
  uni.navigateTo({
    url: `/pages/content/index?id=${contentId}`
  })
}

const toOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order/index?id=${orderId}`
  })
}

const toCreateContent = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.content-list-page {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 120rpx;
}

.content-list {
  height: calc(100vh - 100rpx);
  padding: 24rpx 32rpx;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
}

.content-card {
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  
  &:active {
    background: #f8f9fa;
  }
  
  .content-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 20rpx;
    
    .content-user-info {
      flex: 1;
      
      .content-user {
        display: block;
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 4rpx;
      }
      
      .content-time {
        font-size: 22rpx;
        color: #999;
      }
    }
    
    .content-stats {
      display: flex;
      gap: 20rpx;
      
      .content-stat {
        display: flex;
        align-items: center;
        gap: 4rpx;
        font-size: 22rpx;
        color: #666;
      }
    }
  }
  
  .content-body {
    .content-text {
      display: block;
      font-size: 26rpx;
      color: #333;
      line-height: 1.5;
      margin-bottom: 20rpx;
      word-break: break-word;
    }
    
    .content-images {
      display: flex;
      flex-wrap: wrap;
      gap: 8rpx;
      margin-bottom: 20rpx;
      
      .content-image {
        width: calc((100% - 16rpx) / 3);
        height: 180rpx;
        border-radius: 8rpx;
        overflow: hidden;
      }
      
      .image-more {
        width: calc((100% - 16rpx) / 3);
        height: 180rpx;
        border-radius: 8rpx;
        background: rgba(0, 0, 0, 0.5);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32rpx;
        font-weight: bold;
      }
    }
  }
  
  .content-order {
    padding-top: 16rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .order-tag {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 8rpx 16rpx;
      background: #e3f2fd;
      color: #1976d2;
      border-radius: 20rpx;
      font-size: 22rpx;
      font-weight: 500;
    }
  }
}

.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32rpx;
  
  .no-more-text {
    font-size: 24rpx;
    color: #999;
  }
}

.fab {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  z-index: 999;
  
  &:active {
    transform: scale(0.95);
  }
}
</style>
