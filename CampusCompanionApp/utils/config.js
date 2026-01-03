function getLocalIP() {
  // #ifdef H5
  return 'localhost'
  // #endif
  
  // #ifndef H5
  const savedIP = uni.getStorageSync('apiServerIP')
  if (savedIP) {
    return savedIP
  }
  return '10.0.2.2'
  // #endif
}

const localIP = getLocalIP()

const devConfig = {
  baseURL: `http://${localIP}:8080/api/v1`,
  fileBaseURL: `http://${localIP}:8080`,
  debug: true
}

const prodConfig = {
  baseURL: 'http://117.72.218.137:8080/api/v1',
  fileBaseURL: 'http://117.72.218.137:8080',
  debug: false
}

// 环境判断：可以通过 uni.getStorageSync('env') 来切换环境
// 'dev' = 开发环境, 'prod' = 生产环境, 其他或不设置 = 自动判断
const envMode = uni.getStorageSync('env') || 'auto'

let isDev
// #ifdef H5
// H5环境：自动判断，localhost/127.0.0.1 为开发环境，其他为生产环境
isDev = envMode === 'dev' ? true : (envMode === 'prod' ? false : (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'))
// #endif

// #ifndef H5
// 非H5环境（App）：默认使用生产环境，可通过 env 配置切换
isDev = envMode === 'dev' ? true : false
// #endif

const config = isDev ? devConfig : prodConfig

export default config
