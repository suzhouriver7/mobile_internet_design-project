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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '../stores/order'

const router = useRouter()
const orderStore = useOrderStore()

const formRef = ref(null)
const submitting = ref(false)

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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}
</style>
