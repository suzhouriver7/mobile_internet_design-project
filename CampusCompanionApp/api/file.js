import { upload } from '../utils/request.js'

export default {
  uploadAvatar(filePath) {
    return upload('/files/avatar', filePath, 'file')
  }
}
