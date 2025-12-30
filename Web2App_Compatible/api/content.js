import request from './request'

/**
 * 获取内容列表（与Web后端内容列表接口一致，支持分页、分类、搜索）
 * @param {Object} params - 分页/筛选参数（pageNum, pageSize, categoryId, keyword, userId）
 * @returns {Promise} 请求Promise
 */
export function getContentList(params) {
  return request({
    url: '/content/list',
    method: 'GET',
    data: params,
    loading: true
  })
}

/**
 * 获取内容详情（与Web后端内容详情接口一致）
 * @param {String} contentId - 内容ID
 * @returns {Promise} 请求Promise
 */
export function getContentDetail(contentId) {
  return request({
    url: `/content/detail/${contentId}`,
    method: 'GET',
    loading: true
  })
}

/**
 * 发布内容（与Web后端发布内容接口一致）
 * @param {Object} params - 内容参数（userId, title, content, categoryId, coverImage, tags）
 * @returns {Promise} 请求Promise
 */
export function publishContent(params) {
  return request({
    url: '/content/publish',
    method: 'POST',
    data: params,
    loading: true
  })
}

/**
 * 编辑内容（与Web后端编辑内容接口一致，仅本人发布的未下架内容可编辑）
 * @param {Object} params - 编辑参数（contentId, title, content, categoryId, coverImage, tags, status）
 * @returns {Promise} 请求Promise
 */
export function editContent(params) {
  return request({
    url: '/content/edit',
    method: 'PUT',
    data: params,
    loading: true
  })
}

/**
 * 删除内容（与Web后端删除内容接口一致，仅本人发布的内容可删除）
 * @param {String} contentId - 内容ID
 * @returns {Promise} 请求Promise
 */
export function deleteContent(contentId) {
  return request({
    url: `/content/delete/${contentId}`,
    method: 'DELETE',
    loading: true
  })
}

/**
 * 内容点赞/取消点赞（与Web后端内容点赞接口一致）
 * @param {String} contentId - 内容ID
 * @param {String} userId - 用户ID
 * @returns {Promise} 请求Promise
 */
export function likeContent(contentId, userId) {
  return request({
    url: '/content/like',
    method: 'POST',
    data: { contentId, userId },
    loading: false // 点赞操作不显示加载框，提升用户体验
  })
}

/**
 * 内容评论（与Web后端内容评论接口一致）
 * @param {Object} params - 评论参数（contentId, userId, commentContent, parentCommentId）
 * @returns {Promise} 请求Promise
 */
export function commentContent(params) {
  return request({
    url: '/content/comment',
    method: 'POST',
    data: params,
    loading: true
  })
}