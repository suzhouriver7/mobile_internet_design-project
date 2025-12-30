import request from '@/utils/request.js'

/**
 * 动态相关API服务
 * 对应后端 ContentController
 */

const contentApi = {
  /**
   * 发布动态
   * @param {Object} contentData - 动态数据
   * @returns {Promise} 动态ID
   */
  createContent(contentData) {
    return request.post('/contents', contentData)
  },
  
  /**
   * 获取动态列表
   * @param {Object} params - 查询参数
   * @returns {Promise} 分页动态列表
   */
  getContents(params = {}) {
    const { page = 1, size = 10, type = null } = params
    const queryParams = { page, size }
    if (type !== null && type !== undefined) queryParams.type = type
    
    return request.get('/contents', queryParams)
  },
  
  /**
   * 获取动态详情
   * @param {number} contentId - 动态ID
   * @returns {Promise} 动态详情
   */
  getContentDetail(contentId) {
    return request.get(`/contents/${contentId}`)
  },
  
  /**
   * 删除动态
   * @param {number} contentId - 动态ID
   * @returns {Promise}
   */
  deleteContent(contentId) {
    return request.delete(`/contents/${contentId}`)
  },
  
  /**
   * 上传媒体文件
   * @param {number} contentId - 动态ID
   * @param {Object} fileData - 文件数据（FormData格式）
   * @returns {Promise} 媒体文件URL
   */
  uploadMedia(contentId, fileData) {
    // 注意：文件上传使用特殊的upload方法
    // 这里返回一个函数，由调用方传递文件路径
    return (filePath, fileName = 'media') => {
      return request.upload(`/contents/${contentId}/media`, filePath, {}, fileName)
    }
  },
  
  /**
   * 发布评论
   * @param {number} contentId - 动态ID
   * @param {Object} commentData - 评论数据
   * @returns {Promise} 评论ID
   */
  createComment(contentId, commentData) {
    return request.post(`/contents/${contentId}/comments`, commentData)
  },
  
  /**
   * 获取评论列表
   * @param {number} contentId - 动态ID
   * @param {Object} params - 分页参数
   * @returns {Promise} 分页评论列表（树形结构）
   */
  getComments(contentId, params = {}) {
    const { page = 1, size = 20 } = params
    return request.get(`/contents/${contentId}/comments`, { page, size })
  },
  
  /**
   * 点赞/取消点赞
   * @param {number} contentId - 内容ID
   * @returns {Promise} 点赞状态和数量
   */
  likeContent(contentId) {
    return request.post(`/contents/${contentId}/like`)
  },
  
  /**
   * 获取点赞列表
   * @param {number} contentId - 内容ID
   * @returns {Promise} 点赞用户列表
   */
  getLikes(contentId) {
    return request.get(`/contents/${contentId}/likes`)
  },
  
  /**
   * 搜索动态
   * @param {Object} params - 搜索参数
   * @returns {Promise} 搜索结果
   */
  searchByKeyword(params = {}) {
    const { keyword = '', page = 1, size = 10, type = null } = params
    const queryParams = { keyword, page, size }
    if (type !== null && type !== undefined) queryParams.type = type
    
    return request.get('/contents/search', queryParams)
  }
}

export default contentApi