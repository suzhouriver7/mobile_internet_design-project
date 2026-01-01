import request from '@/utils/request.js'

/**
 * 文件上传API服务
 * 对应后端 FileController
 */

const fileApi = {
  /**
   * 上传图片
   * @param {string} filePath - 图片文件路径
   * @returns {Promise} 图片URL
   */
  uploadImage(filePath) {
    return request.upload('/upload/image', filePath, {}, 'image')
  },
  
  /**
   * 上传视频
   * @param {string} filePath - 视频文件路径
   * @returns {Promise} 视频URL
   */
  uploadVideo(filePath) {
    return request.upload('/upload/video', filePath, {}, 'video')
  }
}

export default fileApi