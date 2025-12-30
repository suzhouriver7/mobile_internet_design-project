<template>
  <view class="home-container">
    <!-- 顶部导航栏 -->
    <view class="home-header">
      <view class="header-left">
        <text class="app-title">校园约伴</text>
      </view>
      <view class="header-right" @click="toUserProfile">
        <u-avatar 
          :src="userAvatar" 
          size="40"
          :show-level="false"
        ></u-avatar>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <u-search
        v-model="searchKeyword"
        placeholder="搜索活动、动态或用户"
        :show-action="false"
        @search="onSearch"
        @click="toSearchPage"
        shape="square"
        bg-color="#f5f5f5"
      ></u-search>
    </view>

    <!-- 轮播图 -->
    <view class="swiper-section" v-if="banners.length > 0">
      <swiper 
        class="swiper" 
        :indicator-dots="true" 
        :autoplay="true" 
        :interval="3000"
        :duration="500"
        circular
      >
        <swiper-item v-for="(banner, index) in banners" :key="index">
          <view class="swiper-item">
            <image 
              :src="banner.image" 
              mode="aspectFill" 
              class="banner-image"
              @click="onBannerClick(banner)"
            />
            <view class="banner-title" v-if="banner.title">{{ banner.title }}</view>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 功能入口 -->
    <view class="function-grid">
      <view class="function-item" @click="toOrdersList">
        <view class="function-icon">
          <u-icon name="calendar" size="28" color="#007aff"></u-icon>
        </view>
        <text class="function-text">活动广场</text>
      </view>
      
      <view class="function-item" @click="toCreateOrder">
        <view class="function-icon">
          <u-icon name="plus-circle" size="28" color="#4cd964"></u-icon>
        </view>
        <text class="function-text">发布活动</text>
      </view>
      
      <view class="function-item" @click="toContentsList">
        <view class="function-icon">
          <u-icon name="photo" size="28" color="#f0ad4e"></u-icon>
        </view>
        <text class="function-text">动态社区</text>
      </view>
      
      <view class="function-item" @click="toAI">
        <view class="function-icon">
          <u-icon name="chat" size="28" color="#dd524d"></u-icon>
        </view>
        <text class="function-text">AI问询</text>
      </view>
    </view>

    <!-- 推荐活动 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">推荐活动</text>
        <text class="section-more" @click="toOrdersList">查看更多</text>
      </view>
      
      <view class="recommend-orders" v-if="recommendOrders.length > 0">
        <view 
          class="order-card" 
          v-for="order in recommendOrders" 
          :key="order.id"
          @click="toOrderDetail(order.id)"
        >
          <view class="order-header">
            <u-avatar 
              :src="resolveResourceUrl(order.user?.avatarUrl)" 
              size="32"
            ></u-avatar>
            <text class="order-user">{{ order.user?.nickname || '用户' }}</text>
            <view class="order-status" :class="getStatusClass(order.status)">
              {{ getOrderStatusText(order.status) }}
            </view>
          </view>
          
          <view class="order-content">
            <text class="order-title">
              {{ getActivityTypeText(order.activityType) }} · {{ order.campus ? getCampusText(order.campus) : '未知校区' }}
            </text>
            <text class="order-location">
              <u-icon name="map" size="14" color="#666"></u-icon>
              {{ order.location }}
            </text>
            <text class="order-time">
              <u-icon name="clock" size="14" color="#666"></u-icon>
              {{ formatTime(order.startTime) }}
            </text>
          </view>
          
          <view class="order-footer">
            <text class="order-people">
              {{ order.currentPeople }}/{{ order.maxPeople }}人
            </text>
            <text class="order-note" v-if="order.note">
              {{ order.note }}
            </text>
          </view>
        </view>
      </view>
      
      <view class="empty-state" v-else>
        <u-empty
          mode="list"
          icon="http://cdn.uviewui.com/uview/empty/list.png"
          text="暂无推荐活动"
        >
          <u-button 
            type="primary" 
            size="mini" 
            @click="toCreateOrder"
          >去发布活动</u-button>
        </u-empty>
      </view>
    </view>

    <!-- 热门动态 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门动态</text>
        <text class="section-more" @click="toContentsList">查看更多</text>
      </view>
      
      <view class="hot-contents" v-if="hotContents.length > 0">
        <view 
          class="content-card" 
          v-for="content in hotContents" 
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
              v-if="content.mediaUrls && content.mediaUrls.length > 0"
            >
              <image 
                v-for="(url, index) in content.mediaUrls.slice(0, 3)" 
                :key="index"
                :src="resolveResourceUrl(url)"
                mode="aspectFill"
                class="content-image"
                @click.stop="previewImage(content.mediaUrls, index)"
              />
              <view 
                class="image-more" 
                v-if="content.mediaUrls.length > 3"
                @click.stop="previewImage(content.mediaUrls, 3)"
              >
                +{{ content.mediaUrls.length - 3 }}
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
      
      <view class="empty-state" v-else>
        <u-empty
          mode="list"
          icon="http://cdn.uviewui.com/uview/empty/list.png"
          text="暂无热门动态"
        >
          <u-button 
            type="primary" 
            size="mini" 
            @click="toCreateContent"
          >去发布动态</u-button>
        </u-empty>
      </view>
    </view>

    <!-- 底部安全区域 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/store/user'
import { orderApi, contentApi } from '@/api'
import { 
  formatTime, 
  formatRelativeTime, 
  resolveResourceUrl,
  showLoading,
  hideLoading 
} from '@/utils/util'
import { 
  ACTIVITY_TYPE_MAP, 
  CAMPUS_MAP, 
  ORDER_STATUS_MAP 
} from '@/utils/constants'

// 使用Pinia store
const userStore = useUserStore()
const { userInfo, isLogin } = storeToRefs(userStore)

// 响应式数据
const searchKeyword = ref('')
const banners = ref([
  { image: '/static/banners/banner1.jpg', title: '找到你的校园伙伴', link: '/pages/orders/list' },
  { image: '/static/banners/banner2.jpg', title: '分享精彩时刻', link: '/pages/contents/list' },
  { image: '/static/banners/banner3.jpg', title: '智能AI助手', link: '/pages/ai/index' }
])
const recommendOrders = ref([])
const hotContents = ref([])
const loading = ref(false)

// 计算属性
const userAvatar = computed(() => {
  return isLogin.value && userInfo.value?.avatarUrl 
    ? resolveResourceUrl(userInfo.value.avatarUrl)
    : '/static/avatars/default.png'
})

// 生命周期
onMounted(() => {
  loadHomeData()
})

onShow(() => {
  // 页面显示时刷新数据
  loadHomeData()
})

// 方法
const loadHomeData = async () => {
  if (loading.value) return
  
  loading.value = true
  showLoading('加载中...')
  
  try {
    // 并行加载推荐活动和热门动态
    const [ordersRes, contentsRes] = await Promise.all([
      loadRecommendOrders(),
      loadHotContents()
    ])
    
    recommendOrders.value = ordersRes
    hotContents.value = contentsRes
  } catch (error) {
    console.error('加载首页数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    hideLoading()
    loading.value = false
  }
}

const loadRecommendOrders = async () => {
  try {
    const params = {
      page: 1,
      size: 3,
      status: 0 // 只显示待匹配的活动
    }
    const response = await orderApi.getOrders(params)
    return response?.list || []
  } catch (error) {
    console.error('加载推荐活动失败:', error)
    return []
  }
}

const loadHotContents = async () => {
  try {
    const params = {
      page: 1,
      size: 3
    }
    const response = await contentApi.getContents(params)
    return response?.list || []
  } catch (error) {
    console.error('加载热门动态失败:', error)
    return []
  }
}

// 工具方法
const getActivityTypeText = (type) => {
  return ACTIVITY_TYPE_MAP[type] || '未知活动'
}

const getCampusText = (campus) => {
  return CAMPUS_MAP[campus] || '未知校区'
}

const getOrderStatusText = (status) => {
  return ORDER_STATUS_MAP[status] || '未知状态'
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-in-progress',
    2: 'status-completed',
    3: 'status-cancelled',
    4: 'status-expired'
  }
  return classMap[status] || ''
}

// 页面跳转方法
const toUserProfile = () => {
  if (isLogin.value) {
    uni.navigateTo({
      url: '/pages/user/profile'
    })
  } else {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
  }
}

const toSearchPage = () => {
  // 这里可以跳转到专门的搜索页面
  uni.navigateTo({
    url: '/pages/search/index'
  })
}

const onSearch = () => {
  if (!searchKeyword.value.trim()) {
    uni.showToast({
      title: '请输入搜索关键词',
      icon: 'none'
    })
    return
  }
  
  // 跳转到搜索结果页
  uni.navigateTo({
    url: `/pages/search/result?keyword=${encodeURIComponent(searchKeyword.value)}`
  })
}

const toOrdersList = () => {
  uni.switchTab({
    url: '/pages/orders/list'
  })
}

const toCreateOrder = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  
  uni.navigateTo({
    url: '/pages/orders/create'
  })
}

const toContentsList = () => {
  uni.switchTab({
    url: '/pages/contents/list'
  })
}

const toCreateContent = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/auth/login'
    })
    return
  }
  
  uni.navigateTo({
    url: '/pages/contents/create'
  })
}

const toAI = () => {
  uni.navigateTo({
    url: '/pages/ai/index'
  })
}

const toOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/orders/detail?id=${orderId}`
  })
}

const toContentDetail = (contentId) => {
  uni.navigateTo({
    url: `/pages/contents/detail?id=${contentId}`
  })
}

const onBannerClick = (banner) => {
  if (banner.link) {
    uni.navigateTo({
      url: banner.link
    })
  }
}

const previewImage = (urls, current) => {
  const fullUrls = urls.map(url => resolveResourceUrl(url))
  uni.previewImage({
    urls: fullUrls,
    current
  })
}
</script>

<script>
// 配置选项
export default {
  name: 'HomePage',
  data() {
    return {}
  }
}
</script>

<style lang="scss" scoped>
.home-container {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .header-left {
    .app-title {
      font-size: 40rpx;
      font-weight: bold;
      color: white;
      text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
    }
  }
  
  .header-right {
    padding: 8rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
  }
}

.search-bar {
  padding: 24rpx 32rpx;
  background: white;
}

.swiper-section {
  padding: 0 32rpx 32rpx;
  
  .swiper {
    height: 300rpx;
    border-radius: 24rpx;
    overflow: hidden;
    
    .swiper-item {
      position: relative;
      height: 100%;
      
      .banner-image {
        width: 100%;
        height: 100%;
      }
      
      .banner-title {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 20rpx;
        background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
        color: white;
        font-size: 28rpx;
        font-weight: 500;
      }
    }
  }
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32rpx;
  padding: 32rpx;
  background: white;
  margin: 24rpx 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.08);
  
  .function-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    
    .function-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 20rpx;
      background: #f8f9fa;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;
      
      &:active {
        transform: scale(0.95);
        background: #e9ecef;
      }
    }
    
    .function-text {
      font-size: 24rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

.section {
  margin: 32rpx;
  background: white;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.08);
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 32rpx 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
    
    .section-more {
      font-size: 24rpx;
      color: #666;
      
      &:active {
        color: #007aff;
      }
    }
  }
}

.recommend-orders {
  padding: 0 32rpx 32rpx;
  
  .order-card {
    padding: 24rpx;
    margin-bottom: 24rpx;
    border-radius: 16rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    &:active {
      background: #e9ecef;
    }
    
    .order-header {
      display: flex;
      align-items: center;
      gap: 16rpx;
      margin-bottom: 20rpx;
      
      .order-user {
        flex: 1;
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
      
      .order-status {
        padding: 4rpx 12rpx;
        border-radius: 20rpx;
        font-size: 20rpx;
        font-weight: 500;
        
        &.status-pending {
          background: #e3f2fd;
          color: #1976d2;
        }
        
        &.status-in-progress {
          background: #e8f5e9;
          color: #388e3c;
        }
        
        &.status-completed {
          background: #f5f5f5;
          color: #616161;
        }
        
        &.status-cancelled {
          background: #ffebee;
          color: #d32f2f;
        }
        
        &.status-expired {
          background: #fff8e1;
          color: #ff8f00;
        }
      }
    }
    
    .order-content {
      margin-bottom: 20rpx;
      
      .order-title {
        display: block;
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 12rpx;
      }
      
      .order-location,
      .order-time {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 24rpx;
        color: #666;
        margin-bottom: 8rpx;
      }
    }
    
    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 16rpx;
      border-top: 1rpx solid #e9ecef;
      
      .order-people {
        font-size: 24rpx;
        color: #007aff;
        font-weight: 500;
      }
      
      .order-note {
        flex: 1;
        margin-left: 16rpx;
        font-size: 22rpx;
        color: #999;
        text-align: right;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.hot-contents {
  padding: 0 32rpx 32rpx;
  
  .content-card {
    padding: 24rpx;
    margin-bottom: 24rpx;
    border-radius: 16rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    &:active {
      background: #e9ecef;
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
          
          &:nth-child(3n) {
            margin-right: 0;
          }
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
      border-top: 1rpx solid #e9ecef;
      
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
}

.empty-state {
  padding: 64rpx 32rpx;
}

.safe-area-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  background: transparent;
}
</style>