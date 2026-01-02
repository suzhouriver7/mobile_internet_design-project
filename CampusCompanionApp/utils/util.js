import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export function formatTime(time, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!time) return ''
  return dayjs(time).format(format)
}

export function formatRelativeTime(time) {
  if (!time) return ''
  return dayjs(time).fromNow()
}

export function showLoading(title = '加载中...') {
  uni.showLoading({ title, mask: true })
}

export function hideLoading() {
  uni.hideLoading()
}

export function showSuccess(title) {
  uni.showToast({ title, icon: 'success', duration: 2000 })
}

export function showError(title) {
  uni.showToast({ title, icon: 'none', duration: 2000 })
}
