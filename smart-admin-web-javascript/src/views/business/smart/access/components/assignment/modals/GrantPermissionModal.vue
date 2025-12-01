<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    :title="`授予${operationName}权限`"
    :width="600"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="loading"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      layout="vertical"
    >
      <!-- 基本信息 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="用户姓名">
            <a-input :value="user.userName" disabled />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="权限类型">
            <a-input :value="operationName" disabled />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="当前安全级别">
            <a-tag :color="getSecurityLevelColor(user.securityLevelValue)">
              {{ user.securityLevelName }}
            </a-tag>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="权限风险级别">
            <a-progress
              :percent="operation.riskLevel"
              size="small"
              :stroke-color="getRiskColor(operation.riskLevel)"
              :show-info="false"
            />
            <span class="risk-text">{{ operation.riskLevel }}分</span>
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 权限配置 -->
      <a-divider>权限配置</a-divider>

      <a-form-item
        label="生效时间"
        name="effectiveTime"
        :rules="[{ required: true, message: '请选择生效时间' }]"
      >
        <a-date-picker
          :value="formData.effectiveTime" @update:value="val => emit('update:value', val)"
          show-time
          format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择生效时间"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="失效时间" name="expireTime">
        <a-date-picker
          :value="formData.expireTime" @update:value="val => emit('update:value', val)"
          show-time
          format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择失效时间（可选）"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="访问次数限制" name="accessCountLimit">
        <a-input-number
          :value="formData.accessCountLimit" @update:value="val => emit('update:value', val)"
          :min="1"
          :max="999999"
          placeholder="请输入访问次数限制（可选）"
          style="width: 100%"
          addon-after="次"
        />
      </a-form-item>

      <!-- 权限条件 -->
      <a-divider>权限条件</a-divider>

      <a-form-item label="IP地址限制" name="ipRestriction">
        <a-textarea
          :value="formData.ipRestriction" @update:value="val => emit('update:value', val)"
          placeholder="请输入允许的IP地址范围，多个IP用换行分隔（可选）"
          :rows="3"
        />
        <div class="help-text">
          支持：192.168.1.0/24 或 192.168.1.100-192.168.1.200 格式
        </div>
      </a-form-item>

      <a-form-item label="时间限制" name="timeRestriction">
        <a-textarea
          :value="formData.timeRestriction" @update:value="val => emit('update:value', val)"
          placeholder="请输入允许的时间段（可选）"
          :rows="3"
        />
        <div class="help-text">
          示例：工作日 09:00-18:00 或 周一至周五 全天
        </div>
      </a-form-item>

      <!-- 授权原因 -->
      <a-form-item
        label="授权原因"
        name="grantReason"
        :rules="[{ required: true, message: '请输入授权原因' }]"
      >
        <a-textarea
          :value="formData.grantReason" @update:value="val => emit('update:value', val)"
          placeholder="请输入授权原因"
          :rows="3"
          :maxlength="500"
          show-count
        />
      </a-form-item>

      <!-- 备注 -->
      <a-form-item label="备注" name="remark">
        <a-textarea
          :value="formData.remark" @update:value="val => emit('update:value', val)"
          placeholder="请输入备注信息（可选）"
          :rows="2"
          :maxlength="200"
          show-count
        />
      </a-form-item>
    </a-form>

    <!-- 权限风险提示 -->
    <a-alert
      v-if="operation.riskLevel > 60"
      type="warning"
      show-icon
      style="margin-bottom: 16px;"
    >
      <template #message>
        <strong>高权限风险提示</strong>
      </template>
      <template #description>
        此权限具有较高的安全风险，请谨慎授予。建议：
        1. 设置较短的有效期
        2. 限制访问IP和时间范围
        3. 定期审查权限使用情况
      </template>
    </a-alert>

    <template #footer>
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="loading">
          确认授予
        </a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

import { permissionApi } from '@/api/smart-permission'

// Props 定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  operation: {
    type: Object,
    required: true
  },
  user: {
    type: Object,
    required: true
  }
})

// 事件定义
const emit = defineEmits(['update:visible', 'success'])

// 响应式数据
const formRef = ref()
const loading = ref(false)

// 表单数据
const formData = reactive({
  effectiveTime: null,
  expireTime: null,
  accessCountLimit: null,
  ipRestriction: '',
  timeRestriction: '',
  grantReason: '',
  remark: ''
})

// 表单验证规则
const rules = {
  effectiveTime: [
    { required: true, message: '请选择生效时间', trigger: 'change' }
  ],
  grantReason: [
    { required: true, message: '请输入授权原因', trigger: 'blur' },
    { min: 5, max: 500, message: '授权原因长度应在5-500字符之间', trigger: 'blur' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const operationName = computed(() => {
  const nameMap = {
    'AREA_ACCESS': '区域访问',
    'AREA_MANAGE': '区域管理',
    'AREA_CONFIG': '区域配置',
    'DEVICE_VIEW': '设备查看',
    'DEVICE_CONTROL': '设备控制',
    'DEVICE_CONFIG': '设备配置',
    'ATTENDANCE_VIEW': '考勤查看',
    'ATTENDANCE_MANAGE': '考勤管理',
    'ATTENDANCE_EXPORT': '考勤导出',
    'ACCESS_ENTER': '门禁进入',
    'ACCESS_MANAGE': '门禁管理',
    'ACCESS_CONFIG': '门禁配置'
  }
  return nameMap[props.operation.operationCode] || props.operation.operationName
})

// 方法定义
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // 构建权限授予请求数据
    const requestData = {
      userId: props.user.userId,
      userName: props.user.userName,
      operationCode: props.operation.operationCode,
      effectiveTime: formData.effectiveTime?.toISOString(),
      expireTime: formData.expireTime?.toISOString(),
      accessCountLimit: formData.accessCountLimit,
      grantReason: formData.grantReason,
      conditions: {
        ipRestriction: formData.ipRestriction,
        timeRestriction: formData.timeRestriction
      },
      restrictions: {
        riskLevel: props.operation.riskLevel
      }
    }

    // 调用权限授予API
    let response
    switch (props.operation.operationCode) {
      case 'AREA_ACCESS':
      case 'AREA_MANAGE':
      case 'AREA_CONFIG':
        response = await permissionApi.grantAreaPermission(requestData)
        break
      case 'DEVICE_VIEW':
      case 'DEVICE_CONTROL':
      case 'DEVICE_CONFIG':
        response = await permissionApi.grantDevicePermission(requestData)
        break
      case 'ATTENDANCE_VIEW':
      case 'ATTENDANCE_MANAGE':
      case 'ATTENDANCE_EXPORT':
        response = await permissionApi.grantAttendancePermission(requestData)
        break
      case 'ACCESS_ENTER':
      case 'ACCESS_MANAGE':
      case 'ACCESS_CONFIG':
        response = await permissionApi.grantAccessPermission(requestData)
        break
      default:
        throw new Error('不支持的权限类型')
    }

    if (response.data) {
      message.success('权限授予成功')
      emit('success')
      handleCancel()
    }
  } catch (error) {
    message.error('权限授予失败: ' + (error.message || '未知错误'))
    console.error('权限授予失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  visible.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    effectiveTime: null,
    expireTime: null,
    accessCountLimit: null,
    ipRestriction: '',
    timeRestriction: '',
    grantReason: '',
    remark: ''
  })
  formRef.value?.resetFields()
}

const getSecurityLevelColor = (levelValue) => {
  const colors = {
    1: 'blue',    // 公开级
    2: 'green',   // 内部级
    3: 'orange',  // 秘密级
    4: 'red',     // 机密级
    5: 'purple'   // 绝密级
  }
  return colors[levelValue] || 'default'
}

const getRiskColor = (level) => {
  if (level <= 30) return '#52c41a'
  if (level <= 60) return '#faad14'
  return '#f5222d'
}

// 监听器
watch(visible, (newVisible) => {
  if (newVisible) {
    // 设置默认生效时间为当前时间
    formData.effectiveTime = dayjs()
  }
})
</script>

<style lang="less" scoped>
.help-text {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.risk-text {
  font-size: 12px;
  color: #8c8c8c;
  margin-left: 8px;
  vertical-align: middle;
}

:deep(.ant-divider) {
  margin: 16px 0;
}

:deep(.ant-alert) {
  margin-bottom: 16px;
}
</style>