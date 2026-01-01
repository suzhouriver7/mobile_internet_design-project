/**
 * 项目配置文件
 * 可在开发/生产环境切换不同配置
 */

// 开发环境配置
const devConfig = {
  baseURL: 'http://localhost:8080/api/v1',
  fileBaseURL: 'http://localhost:8080',
  debug: true
}

// 生产环境配置
const prodConfig = {
  baseURL: 'https://api.yourdomain.com/api/v1',
  fileBaseURL: 'https://api.yourdomain.com',
  debug: false
}

// 根据编译环境选择配置
// #ifdef H5
const isDev = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
// #endif

// #ifndef H5
// 小程序/App端可通过其他方式判断环境，这里先默认开发环境
const isDev = true
// #endif

const config = isDev ? devConfig : prodConfig

export default config