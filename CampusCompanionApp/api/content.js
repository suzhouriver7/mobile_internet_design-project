import { get, post, put, del } from '../utils/request.js'

export default {
  createContent(data) {
    return post('/contents', data)
  },
  getContents(params = {}) {
    return get('/contents', params)
  },
  getContentDetail(contentId) {
    return get(`/contents/${contentId}`)
  },
  updateContent(contentId, data) {
    return put(`/contents/${contentId}`, data)
  },
  deleteContent(contentId) {
    return del(`/contents/${contentId}`)
  },
  createComment(contentId, data) {
    return post(`/contents/${contentId}/comments`, data)
  },
  getComments(contentId, page = 1, size = 20) {
    return get(`/contents/${contentId}/comments`, { page, size })
  },
  likeContent(contentId) {
    return post(`/contents/${contentId}/like`, {})
  },
  getLikes(contentId) {
    return get(`/contents/${contentId}/likes`)
  }
}
