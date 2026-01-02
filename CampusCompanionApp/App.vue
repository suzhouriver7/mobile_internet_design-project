<script>
import config from './utils/config.js'

export default {
  onLaunch() {
    console.log('App启动', config.baseURL)
  },
  onShow() {
    console.log('App显示')
  },
  onHide() {
    console.log('App隐藏')
  }
}
</script>

<style>
page {
  background-color: #f5f5f5;
  height: 100%;
}
</style>
