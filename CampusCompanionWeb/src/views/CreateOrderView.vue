<template>
  <div class="create-order-view">
    <el-card class="create-order-card">
      <template #header>
        <div class="card-header">
          <h2>发布预约订单</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="order-form"
      >
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="form.activityType" placeholder="请选择活动类型">
            <el-option label="篮球" :value="'BASKETBALL'" />
            <el-option label="羽毛球" :value="'BADMINTON'" />
            <el-option label="吃饭" :value="'MEAL'" />
            <el-option label="自习" :value="'STUDY'" />
            <el-option label="看电影" :value="'MOVIE'" />
            <el-option label="跑步" :value="'RUNNING'" />
            <el-option label="游戏" :value="'GAME'" />
            <el-option label="其他" :value="'OTHER'" />
          </el-select>
        </el-form-item>

        <el-form-item label="性别要求" prop="genderRequire">
          <el-radio-group v-model="form.genderRequire">
            <el-radio :label="'ANY'">不限</el-radio>
            <el-radio :label="'MALE'">仅限男生</el-radio>
            <el-radio :label="'FEMALE'">仅限女生</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="校区" prop="campus">
          <el-select v-model="form.campus" placeholder="请选择校区">
            <el-option label="良乡校区" :value="'LIANGXIANG'" />
            <el-option label="中关村校区" :value="'ZHONGGUANCUN'" />
            <el-option label="珠海校区" :value="'ZHUHAI'" />
            <el-option label="西山校区" :value="'XISHAN'" />
            <el-option label="其他校区" :value="'OTHER_CAMPUS'" />
          </el-select>
          <div
            v-if="showMatchingHint"
            class="matching-hint"
            @click="goToMatchedOrders"
          >
            已找到现有的 {{ matchingCount }} 个订单有同样的需求，点击查看
          </div>
        </el-form-item>

        <el-form-item label="活动地点" prop="location">
          <el-input
            v-model="form.location"
            placeholder="请输入活动地点，如：良乡体育馆 3 号场"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择开始时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            :disabled-time="disabledTime"
          />
        </el-form-item>

        <el-form-item label="人数上限" prop="maxPeople">
          <el-input-number
            v-model="form.maxPeople"
            :min="1"
            :max="20"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.note"
            type="textarea"
            :rows="3"
            placeholder="可填写一些补充说明（选填）"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            发布订单
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '../stores/order'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const formRef = ref(null)
const submitting = ref(false)
const matchingCount = ref(0)
const matchingLoading = ref(false)

// 与后端 CreateOrderRequest 完全对齐的字段
const form = reactive({
  activityType: '', // ActivityType 枚举：BASKETBALL, BADMINTON, ...
  genderRequire: 'ANY', // GenderRequire：MALE, FEMALE, ANY
  campus: '', // Campus：LIANGXIANG, ZHONGGUANCUN, ...
  location: '',
  startTime: '', // 字符串，格式 yyyy-MM-dd HH:mm:ss
  note: '',
  maxPeople: 2
})

// 如果是从 AI / MCP 跳转过来，支持通过 query 预填表单
const q = route.query || {}
if (q.activityType) form.activityType = String(q.activityType)
if (q.campus) form.campus = String(q.campus)
if (q.location) form.location = String(q.location)
if (q.startTime) form.startTime = String(q.startTime)
if (q.genderRequire) form.genderRequire = String(q.genderRequire)
if (q.maxPeople) {
  const n = Number(q.maxPeople)
  if (!Number.isNaN(n) && n > 0 && n <= 20) form.maxPeople = n
}
if (q.note) form.note = String(q.note)

const rules = {
  activityType: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
  ],
  genderRequire: [
    { required: true, message: '请选择性别要求', trigger: 'change' }
  ],
  campus: [
    { required: true, message: '请选择校区', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  maxPeople: [
    { required: true, message: '请输入人数上限', trigger: 'change' },
    { type: 'number', min: 1, message: '人数上限必须大于 0', trigger: 'change' }
  ]
}

// 根据已选择的活动类型和校区，实时统计当前「待匹配」且相同条件的订单数量
const fetchMatchingOrdersCount = async (activityType, campus) => {
  if (!activityType || !campus) {
    matchingCount.value = 0
    return
  }

  matchingLoading.value = true
  try {
    const params = {
      page: 1,
      size: 1,
      status: 'PENDING',
      activityType,
      campus
    }
    const res = await orderStore.getOrders(params)
    const data = res.data?.data || {}
    matchingCount.value = data.total || 0
  } catch (error) {
    console.error('获取匹配订单数量失败', error)
    matchingCount.value = 0
  } finally {
    matchingLoading.value = false
  }
}

// 监听活动类型和校区变化，触发统计
watch(
  () => [form.activityType, form.campus],
  ([activityType, campus]) => {
    if (!activityType || !campus) {
      matchingCount.value = 0
      return
    }
    fetchMatchingOrdersCount(activityType, campus)
  }
)

// 是否展示提示文案
const showMatchingHint = computed(() => {
  return (
    !!form.activityType &&
    !!form.campus &&
    !matchingLoading.value &&
    matchingCount.value > 0
  )
})

// 禁止选择早于当前时间的日期/时间
const disabledDate = (time) => {
  const now = new Date()
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  return time.getTime() < todayStart
}

const disabledTime = (date) => {
  if (!date) return {}
  const now = new Date()
  // 仅在选择的是当天时对小时/分钟进行限制
  if (
    date.getFullYear() !== now.getFullYear() ||
    date.getMonth() !== now.getMonth() ||
    date.getDate() !== now.getDate()
  ) {
    return {}
  }

  const curHour = now.getHours()
  const curMinute = now.getMinutes()
  const curSecond = now.getSeconds()

  return {
    disabledHours: () => {
      const arr = []
      for (let i = 0; i < 24; i++) {
        if (i < curHour) arr.push(i)
      }
      return arr
    },
    disabledMinutes: (hour) => {
      if (hour === curHour) {
        const arr = []
        for (let i = 0; i < 60; i++) {
          if (i < curMinute) arr.push(i)
        }
        return arr
      }
      return []
    },
    disabledSeconds: (hour, minute) => {
      if (hour === curHour && minute === curMinute) {
        const arr = []
        for (let i = 0; i < 60; i++) {
          if (i < curSecond) arr.push(i)
        }
        return arr
      }
      return []
    }
  }
}

// 跳转到带预设筛选条件的订单列表页
const goToMatchedOrders = () => {
  if (!form.activityType || !form.campus) return

  router.push({
    path: '/orders',
    query: {
      page: '1',
      size: '10',
      activityType: form.activityType,
      campus: form.campus,
      status: 'PENDING'
    }
  })
}

const handleCancel = () => {
  router.back()
}

const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 直接按 CreateOrderRequest 结构提交
      const payload = {
        activityType: form.activityType,
        genderRequire: form.genderRequire,
        campus: form.campus,
        location: form.location,
        startTime: form.startTime,
        note: form.note,
        maxPeople: form.maxPeople
      }

      const res = await orderStore.createOrder(payload)
      // 后端返回 ApiResponse<Long>，data 为订单 ID
      const orderId = res.data.data
      ElMessage.success('发布成功')
      // 跳转到订单详情或列表，这里跳到详情
      if (orderId) {
        router.push(`/orders/${orderId}`)
      } else {
        router.push('/orders')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || error.message || '发布失败')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.create-order-view {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.create-order-card {
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-form {
  margin-top: 10px;
}

.matching-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
}

.matching-hint:hover {
  text-decoration: underline;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}
</style>
