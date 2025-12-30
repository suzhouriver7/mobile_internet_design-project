<template>
  <view class="content-list">
    <uni-nav-bar title="校园动态" left-arrow @clickLeft="onBack"></uni-nav-bar>
    
    <!-- 内容类型筛选 -->
    <uni-segmented-control 
      :current="currentType" 
      :values="typeList" 
      @clickItem="onTypeChange"
    ></uni-segmented-control>
    
    <!-- 动态列表 -->
    <uni-list v-if="loading">
      <uni-list-item>
        <uni-loading type="circle" color="#409eff"></uni-loading>
      </uni-list-item>
    </uni-list>
    
    <uni-list v-else>
      <uni-list-item 
        v-for="item in contentList" 
        :key="item.id" 
        class="content-item"
        clickable
        @click="navigateToDetail(item.id)"
      >
        <template #header>
          <view class="content-header">
            <uni-image 
              class="content-avatar" 
              :src="item.userAvatar || '/static/images/default-avatar.png'"
            ></uni-image>
            <view class="content-userinfo">
              <text class="content-username">{{ item.userNickname }}</text>
              <text class="content-time">{{ formatTime(item.createTime) }}</text>
            </view>
          </view>
        </template>
        <view class="content-text">{{ item.content }}</view>
        <template #footer>
          <view class="content-footer">
            <uni-icon type="heart" size="24" :color="item.liked ? '#ff4d4f' : '#999'"></uni-icon>
            <text class="like-count">{{ item.likeCount }}</text>
            <uni-icon type="chatbubble" size="24" class="comment-icon"></uni-icon>
            <text class="comment-count">{{ item.commentCount }}</text>
          </view>
        </template>
      </uni-list-item>
    </uni-list>
    
    <!-- 加载更多 -->
    <uni-load-more 
      :status="loadStatus" 
      @clickLoadMore="loadMore"
    ></uni-load-more>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { getContentList } from '@/api/content'

const currentType = ref(0) // 0-全部, 1-活动, 2-求助
const typeList = ['全部', '活动', '求助']
const contentList = ref([])
const page = ref(1)
const size = ref(10)
const loading = ref(true)
const loadStatus = ref('more') // more-可加载, noMore-无更多, loading-加载中

// 页面加载时获取数据
onLoad(async () => {
  await fetchContentList(true)
})

// 获取动态列表
const fetchContentList = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    loading.value = true
  } else {
    loadStatus.value = 'loading'
  }
  
  try {
    const typeParam = currentType.value === 0 ? null : currentType.value
    const res = await getContentList({
      page: page.value,
      size: size.value,
      type: typeParam
    })
    
    if (isRefresh) {
      contentList.value = res.data.records
    } else {
      contentList.value = [...contentList.value, ...res.data.records]
    }
    
    // 判断是否还有更多数据
    if (contentList.value.length >= res.data.total) {
      loadStatus.value = 'noMore'
    } else {
      loadStatus.value = 'more'
    }
  } catch (err) {
    uni.showToast({ title: '获取动态失败', icon: 'none' })
  } finally {
    loading.value = false
    if (!isRefresh) {
      loadStatus.value = contentList.value.length >= res.data.total ? 'noMore' : 'more'
    }
  }
}

// 切换内容类型
const onTypeChange = (e) => {
  currentType.value = e.currentIndex
  fetchContentList(true)
}

// 加载更多
const loadMore = () => {
  if (loadStatus.value !== 'more') return
  page.value++
  fetchContentList()
}

// 格式化时间
const formatTime = (timeStr) => {
  // 简单示例：实际项目建议使用dayjs等库
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes()}`
}

// 跳转到详情页
const navigateToDetail = (id) => {
  uni.navigateTo({ url: `/pages/content/detail?id=${id}` })
}

// 返回上一页
const onBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.content-item {
  padding: 20rpx 0;
  border-bottom: 1px solid #f5f5f5;
}

.content-header {
  display: flex;
  align-items: center;
  margin-bottom: 15rpx;
}

.content-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 15rpx;
}

.content-username {
  font-size: 28rpx;
  font-weight: 500;
}

.content-time {
  font-size: 22rpx;
  color: #999;
  margin-left: 10rpx;
}

.content-text {
  font-size: 30rpx;
  line-height: 1.5;
  margin-bottom: 15rpx;
}

.content-footer {
  display: flex;
  align-items: center;
  color: #999;
  font-size: 24rpx;
}

.like-count {
  margin: 0 20rpx 0 5rpx;
}

.comment-icon {
  margin-left: 30rpx;
}

.comment-count {
  margin-left: 5rpx;
}
</style>