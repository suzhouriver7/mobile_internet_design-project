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
  baseURL: 'https://api.yourdomain.com/api/v1',
  fileBaseURL: 'https://api.yourdomain.com',
  debug: false
}

// #ifdef H5
const isDev = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
// #endif

// #ifndef H5
const isDev = true
// #endif

const config = isDev ? devConfig : prodConfig

export default config
