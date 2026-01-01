/**
 * API统一导出文件
 * 方便在组件中一次性导入所有API
 */

import authApi from './auth.js'
import orderApi from './order.js'
import contentApi from './content.js'
import userApi from './user.js'
import fileApi from './file.js'

export {
  authApi,
  orderApi,
  contentApi,
  userApi,
  fileApi
}

// 默认导出所有API
export default {
  auth: authApi,
  order: orderApi,
  content: contentApi,
  user: userApi,
  file: fileApi
}