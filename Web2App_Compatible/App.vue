<script>
export default {
  onLaunch: function(options) {
    console.log('App Launch', options)
  },
  onShow: function(options) {
    console.log('App Show', options)
  },
  onHide: function() {
    console.log('App Hide')
  },
  onError: function(error) {
    console.error('App Error:', error)
  }
}
</script>

<script>
export default {
  onLaunch: function(options) {
    console.log('App Launch', options)
    
    // 应用启动时初始化
    this.initApp()
  },
  onShow: function(options) {
    console.log('App Show', options)
  },
  onHide: function() {
    console.log('App Hide')
  },
  onError: function(error) {
    console.error('App Error:', error)
    
    // 可以在这里进行错误上报
    // this.reportError(error)
  },
  methods: {
    initApp() {
      // 检查更新
      this.checkUpdate()
      
      // 获取系统信息
      this.getSystemInfo()
      
      // 初始化全局配置
      this.initGlobalConfig()
    },
    
    checkUpdate() {
      // #ifdef APP-PLUS
      const updateManager = uni.getUpdateManager()
      
      updateManager.onCheckForUpdate(function(res) {
        // 请求完新版本信息的回调
        console.log('是否有新版本：', res.hasUpdate)
      })
      
      updateManager.onUpdateReady(function(res) {
        uni.showModal({
          title: '更新提示',
          content: '新版本已经准备好，是否重启应用？',
          success: function(res) {
            if (res.confirm) {
              // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
              updateManager.applyUpdate()
            }
          }
        })
      })
      
      updateManager.onUpdateFailed(function(res) {
        // 新版本下载失败
        console.error('新版本下载失败')
      })
      // #endif
    },
    
    getSystemInfo() {
      try {
        const systemInfo = uni.getSystemInfoSync()
        console.log('系统信息:', systemInfo)
        
        // 存储到全局，方便使用
        this.$store.commit('SET_SYSTEM_INFO', systemInfo)
      } catch (error) {
        console.error('获取系统信息失败:', error)
      }
    },
    
    initGlobalConfig() {
      // 可以在这里初始化一些全局配置
      // 例如：设置请求超时时间、初始化统计SDK等
    },
    
    reportError(error) {
      // 错误上报逻辑
      console.log('上报错误:', error)
      // 实际项目中可以接入Sentry、Fundebug等错误监控平台
    }
  }
}
</script>

<style>
/* 全局样式 */
page {
  background-color: #f8f8f8;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto, 'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei', sans-serif;
}

/* 全局通用类 */
.text-primary {
  color: #007aff;
}

.text-success {
  color: #4cd964;
}

.text-warning {
  color: #f0ad4e;
}

.text-danger {
  color: #dd524d;
}

.text-muted {
  color: #999999;
}

.bg-primary {
  background-color: #007aff;
}

.bg-success {
  background-color: #4cd964;
}

.bg-warning {
  background-color: #f0ad4e;
}

.bg-danger {
  background-color: #dd524d;
}

/* 边距工具类 */
.mt-1 { margin-top: 8rpx; }
.mt-2 { margin-top: 16rpx; }
.mt-3 { margin-top: 24rpx; }
.mt-4 { margin-top: 32rpx; }

.mb-1 { margin-bottom: 8rpx; }
.mb-2 { margin-bottom: 16rpx; }
.mb-3 { margin-bottom: 24rpx; }
.mb-4 { margin-bottom: 32rpx; }

.ml-1 { margin-left: 8rpx; }
.ml-2 { margin-left: 16rpx; }
.ml-3 { margin-left: 24rpx; }
.ml-4 { margin-left: 32rpx; }

.mr-1 { margin-right: 8rpx; }
.mr-2 { margin-right: 16rpx; }
.mr-3 { margin-right: 24rpx; }
.mr-4 { margin-right: 32rpx; }

.pt-1 { padding-top: 8rpx; }
.pt-2 { padding-top: 16rpx; }
.pt-3 { padding-top: 24rpx; }
.pt-4 { padding-top: 32rpx; }

.pb-1 { padding-bottom: 8rpx; }
.pb-2 { padding-bottom: 16rpx; }
.pb-3 { padding-bottom: 24rpx; }
.pb-4 { padding-bottom: 32rpx; }

.pl-1 { padding-left: 8rpx; }
.pl-2 { padding-left: 16rpx; }
.pl-3 { padding-left: 24rpx; }
.pl-4 { padding-left: 32rpx; }

.pr-1 { padding-right: 8rpx; }
.pr-2 { padding-right: 16rpx; }
.pr-3 { padding-right: 24rpx; }
.pr-4 { padding-right: 32rpx; }

/* 布局类 */
.flex {
  display: flex;
}

.flex-column {
  flex-direction: column;
}

.flex-wrap {
  flex-wrap: wrap;
}

.justify-start {
  justify-content: flex-start;
}

.justify-center {
  justify-content: center;
}

.justify-end {
  justify-content: flex-end;
}

.justify-between {
  justify-content: space-between;
}

.justify-around {
  justify-content: space-around;
}

.items-start {
  align-items: flex-start;
}

.items-center {
  align-items: center;
}

.items-end {
  align-items: flex-end;
}

.flex-1 {
  flex: 1;
}

/* 文本类 */
.text-left {
  text-align: left;
}

.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-bold {
  font-weight: bold;
}

/* 边框类 */
.border {
  border: 1rpx solid #e5e5e5;
}

.border-top {
  border-top: 1rpx solid #e5e5e5;
}

.border-bottom {
  border-bottom: 1rpx solid #e5e5e5;
}

.border-left {
  border-left: 1rpx solid #e5e5e5;
}

.border-right {
  border-right: 1rpx solid #e5e5e5;
}

.rounded {
  border-radius: 8rpx;
}

.rounded-lg {
  border-radius: 16rpx;
}

.rounded-circle {
  border-radius: 50%;
}

/* 阴影类 */
.shadow-sm {
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.shadow {
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.shadow-lg {
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.2);
}

/* 其他实用类 */
.cursor-pointer {
  cursor: pointer;
}

.position-relative {
  position: relative;
}

.position-absolute {
  position: absolute;
}

.position-fixed {
  position: fixed;
}

.position-sticky {
  position: sticky;
}

.overflow-hidden {
  overflow: hidden;
}

.overflow-auto {
  overflow: auto;
}
</style>