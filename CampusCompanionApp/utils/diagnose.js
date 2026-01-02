/**
 * 诊断工具 - 用于检查App运行环境
 */
import config from './config.js'

/**
 * 诊断网络连接
 */
export async function diagnoseNetwork() {
  const results = {
    config: {
      baseURL: config.baseURL,
      fileBaseURL: config.fileBaseURL,
      debug: config.debug
    },
    network: {
      available: false,
      error: null
    },
    api: {
      reachable: false,
      error: null,
      response: null
    }
  }
  
  // 检查网络状态
  try {
    const networkInfo = await new Promise((resolve, reject) => {
      uni.getNetworkType({
        success: resolve,
        fail: reject
      })
    })
    results.network.available = networkInfo.networkType !== 'none'
    if (!results.network.available) {
      results.network.error = '无网络连接'
    }
  } catch (e) {
    results.network.error = e.message || '获取网络状态失败'
  }
  
  // 测试API连接
  if (results.network.available) {
    try {
      const response = await new Promise((resolve, reject) => {
        uni.request({
          url: config.baseURL.replace('/api/v1', '/health'),
          method: 'GET',
          timeout: 5000,
          success: (res) => {
            if (res.statusCode === 200) {
              resolve(res.data)
            } else {
              reject(new Error(`HTTP ${res.statusCode}`))
            }
          },
          fail: reject
        })
      })
      results.api.reachable = true
      results.api.response = response
    } catch (e) {
      results.api.error = e.message || e.errMsg || 'API连接失败'
      results.api.reachable = false
    }
  }
  
  return results
}

/**
 * 打印诊断信息
 */
export function printDiagnose(results) {
  console.log('========== 诊断信息 ==========')
  console.log('配置信息:', results.config)
  console.log('网络状态:', results.network.available ? '可用' : '不可用', results.network.error || '')
  console.log('API连接:', results.api.reachable ? '成功' : '失败', results.api.error || '')
  if (results.api.response) {
    console.log('API响应:', results.api.response)
  }
  console.log('============================')
  
  return results
}

/**
 * 获取系统信息
 */
export function getSystemInfo() {
  try {
    const systemInfo = uni.getSystemInfoSync()
    return {
      platform: systemInfo.platform,
      system: systemInfo.system,
      version: systemInfo.version,
      screenWidth: systemInfo.screenWidth,
      screenHeight: systemInfo.screenHeight,
      windowWidth: systemInfo.windowWidth,
      windowHeight: systemInfo.windowHeight,
      pixelRatio: systemInfo.pixelRatio
    }
  } catch (e) {
    console.error('获取系统信息失败:', e)
    return null
  }
}

