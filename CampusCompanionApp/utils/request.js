import config from './config.js'

export function request(options) {
  return new Promise((resolve, reject) => {
    const userId = uni.getStorageSync('userId')
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    if (userId && options.needAuth !== false) {
      header['X-User-Id'] = userId.toString()
    }
    
    uni.request({
      url: config.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        if (res.statusCode === 200) {
          const response = res.data
          if (response.code === 200) {
            resolve(response.data)
          } else {
            reject(new Error(response.message || '请求失败'))
          }
        } else {
          // 尝试解析响应体中的错误信息
          let errorMessage = `请求失败: ${res.statusCode}`
          try {
            if (res.data && typeof res.data === 'object') {
              if (res.data.message) {
                errorMessage = res.data.message
              } else if (res.data.error) {
                errorMessage = res.data.error
              }
            } else if (typeof res.data === 'string') {
              errorMessage = res.data
            }
          } catch (e) {
            // 解析失败，使用默认错误信息
          }
          reject(new Error(errorMessage))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export function get(url, params = {}, needAuth = true) {
  const queryString = Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  const fullUrl = queryString ? `${url}?${queryString}` : url
  
  return request({
    url: fullUrl,
    method: 'GET',
    needAuth
  })
}

export function post(url, data = {}, needAuth = true) {
  return request({
    url,
    method: 'POST',
    data,
    needAuth
  })
}

export function put(url, data = {}, needAuth = true) {
  return request({
    url,
    method: 'PUT',
    data,
    needAuth
  })
}

export function del(url, needAuth = true) {
  return request({
    url,
    method: 'DELETE',
    needAuth
  })
}

export function upload(url, filePath, name = 'file', needAuth = true) {
  return new Promise((resolve, reject) => {
    const userId = uni.getStorageSync('userId')
    const header = {}
    
    if (userId && needAuth) {
      header['X-User-Id'] = userId.toString()
    }
    
    uni.uploadFile({
      url: config.baseURL + url,
      filePath: filePath,
      name: name,
      header: header,
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        } catch (e) {
          reject(new Error('解析响应失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
