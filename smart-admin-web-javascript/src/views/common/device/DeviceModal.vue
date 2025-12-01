<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    :title="modalTitle"
    :width="800"
    :confirm-loading="submitLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :destroyOnClose="true"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备编码" name="deviceCode">
            <a-input
              :value="formData.deviceCode" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备编码"
              :disabled="isEdit"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="设备名称" name="deviceName">
            <a-input
              :value="formData.deviceName" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备名称"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备类型" name="deviceType">
            <DeviceTypeSelect
              :value="formData.deviceType" @update:value="val => emit('update:value', val)"
              placeholder="请选择设备类型"
              :disabled="isEdit"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="设备品牌" name="deviceBrand">
            <a-input
              :value="formData.deviceBrand" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备品牌"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备型号" name="deviceModel">
            <a-input
              :value="formData.deviceModel" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备型号"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="设备序列号" name="deviceSerial">
            <a-input
              :value="formData.deviceSerial" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备序列号"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="IP地址" name="ipAddress">
            <a-input
              :value="formData.ipAddress" @update:value="val => emit('update:value', val)"
              placeholder="请输入IP地址"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="端口号" name="port">
            <a-input-number
              :value="formData.port" @update:value="val => emit('update:value', val)"
              placeholder="请输入端口号"
              :min="1"
              :max="65535"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="MAC地址" name="macAddress">
            <a-input
              :value="formData.macAddress" @update:value="val => emit('update:value', val)"
              placeholder="请输入MAC地址"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="通信协议" name="protocolType">
            <a-select
              :value="formData.protocolType" @update:value="val => emit('update:value', val)"
              placeholder="请选择通信协议"
              allow-clear
            >
              <a-select-option value="TCP">TCP</a-select-option>
              <a-select-option value="UDP">UDP</a-select-option>
              <a-select-option value="HTTP">HTTP</a-select-option>
              <a-select-option value="WEBSOCKET">WebSocket</a-select-option>
              <a-select-option value="MODBUS">MODBUS</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="所属区域ID" name="areaId">
            <a-input-number
              :value="formData.areaId" @update:value="val => emit('update:value', val)"
              placeholder="请输入所属区域ID"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="所属区域名称" name="areaName">
            <a-input
              :value="formData.areaName" @update:value="val => emit('update:value', val)"
              placeholder="请输入所属区域名称"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="位置描述" name="locationDesc">
        <a-textarea
          :value="formData.locationDesc" @update:value="val => emit('update:value', val)"
          placeholder="请输入位置描述"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="供应商信息" name="vendorInfo">
        <a-textarea
          :value="formData.vendorInfo" @update:value="val => emit('update:value', val)"
          placeholder="请输入供应商信息"
          :rows="2"
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="联系人" name="contactPerson">
            <a-input
              :value="formData.contactPerson" @update:value="val => emit('update:value', val)"
              placeholder="请输入联系人"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="联系电话" name="contactPhone">
            <a-input
              :value="formData.contactPhone" @update:value="val => emit('update:value', val)"
              placeholder="请输入联系电话"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="安装时间" name="installTime">
            <a-date-picker
              :value="formData.installTime" @update:value="val => emit('update:value', val)"
              placeholder="请选择安装时间"
              show-time
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="保修到期时间" name="warrantyEndTime">
            <a-date-picker
              :value="formData.warrantyEndTime" @update:value="val => emit('update:value', val)"
              placeholder="请选择保修到期时间"
              show-time
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="设备配置" name="configJson">
        <a-textarea
          :value="configJsonText" @update:value="val => emit('update:value', val)"
          placeholder="请输入设备配置（JSON格式）"
          :rows="4"
          @blur="handleConfigJsonBlur"
        />
        <div v-if="configJsonError" class="error-message">
          配置格式不正确：{{ configJsonError }}
        </div>
      </a-form-item>

      <a-form-item label="扩展信息" name="extendInfo">
        <a-textarea
          :value="extendInfoText" @update:value="val => emit('update:value', val)"
          placeholder="请输入扩展信息（JSON格式）"
          :rows="3"
          @blur="handleExtendInfoBlur"
        />
        <div v-if="extendInfoError" class="error-message">
          扩展信息格式不正确：{{ extendInfoError }}
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { deviceApi } from '/@/api/common/device/deviceApi'
import DeviceTypeSelect from '/@/components/common/device/DeviceTypeSelect.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  device: {
    type: Object,
    default: null
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref()
const submitLoading = ref(false)
const configJsonError = ref('')
const extendInfoError = ref('')

// 表单数据
const formData = reactive({
  deviceId: null,
  deviceCode: '',
  deviceName: '',
  deviceType: '',
  deviceBrand: '',
  deviceModel: '',
  deviceSerial: '',
  areaId: null,
  areaName: '',
  locationDesc: '',
  ipAddress: '',
  port: null,
  macAddress: '',
  protocolType: '',
  isEnabled: 1,
  vendorInfo: '',
  contactPerson: '',
  contactPhone: '',
  installTime: null,
  warrantyEndTime: null,
  configJson: null,
  extendInfo: null
})

const configJsonText = computed({
  get: () => {
    return formData.configJson ? JSON.stringify(formData.configJson, null, 2) : ''
  },
  set: (value) => {
    try {
      if (value) {
        formData.configJson = JSON.parse(value)
        configJsonError.value = ''
      } else {
        formData.configJson = null
      }
    } catch (error) {
      configJsonText.value = ''
      configJsonError.value = 'JSON格式错误'
    }
  }
})

const extendInfoText = computed({
  get: () => {
    return formData.extendInfo ? JSON.stringify(formData.extendInfo, null, 2) : ''
  },
  set: (value) => {
    try {
      if (value) {
        formData.extendInfo = JSON.parse(value)
        extendInfoError.value = ''
      } else {
        formData.extendInfo = null
      }
    } catch (error) {
      extendInfoError.value = 'JSON格式错误'
    }
  }
})

// 计算属性
const modalTitle = computed(() => {
  return props.isEdit ? '编辑设备' : '新增设备'
})

// 表单验证规则
const formRules = {
  deviceCode: [
    { required: true, message: '请输入设备编码', trigger: 'blur' },
    { max: 100, message: '设备编码长度不能超过100个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '设备编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { max: 200, message: '设备名称长度不能超过200个字符', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  ipAddress: [
    { pattern: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/, message: 'IP地址格式不正确', trigger: 'blur' }
  ],
  port: [
    { type: 'number', min: 1, max: 65535, message: '端口号必须在1-65535之间', trigger: 'blur' }
  ],
  macAddress: [
    { pattern: /^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/, message: 'MAC地址格式不正确', trigger: 'blur' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// 监听设备数据变化
watch(() => props.device, (newDevice) => {
  if (newDevice) {
    Object.assign(formData, newDevice)

    // 转换JSON字段为文本
    if (newDevice.configJson) {
      try {
        configJsonText.value = JSON.stringify(newDevice.configJson, null, 2)
      } catch (error) {
        configJsonText.value = ''
      }
    }

    if (newDevice.extendInfo) {
      try {
        extendInfoText.value = JSON.stringify(newDevice.extendInfo, null, 2)
      } catch (error) {
        extendInfoText.value = ''
      }
    }
  } else {
    // 重置表单
    resetForm()
  }
}, { immediate: true, deep: true })

// 监听弹窗显示状态
watch(() => props.visible, (visible) => {
  if (!visible) {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    deviceId: null,
    deviceCode: '',
    deviceName: '',
    deviceType: '',
    deviceBrand: '',
    deviceModel: '',
    deviceSerial: '',
    areaId: null,
    areaName: '',
    locationDesc: '',
    ipAddress: '',
    port: null,
    macAddress: '',
    protocolType: '',
    isEnabled: 1,
    vendorInfo: '',
    contactPerson: '',
    contactPhone: '',
    installTime: null,
    warrantyEndTime: null,
    configJson: null,
    extendInfo: null
  })
  configJsonText.value = ''
  extendInfoText.value = ''
  configJsonError.value = ''
  extendInfoError.value = ''

  nextTick(() => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
  })
}

// 处理配置JSON失去焦点
const handleConfigJsonBlur = () => {
  try {
    if (configJsonText.value) {
      formData.configJson = JSON.parse(configJsonText.value)
      configJsonError.value = ''
    } else {
      formData.configJson = null
    }
  } catch (error) {
    configJsonError.value = 'JSON格式错误'
  }
}

// 处理扩展信息失去焦点
const handleExtendInfoBlur = () => {
  try {
    if (extendInfoText.value) {
      formData.extendInfo = JSON.parse(extendInfoText.value)
      extendInfoError.value = ''
    } else {
      formData.extendInfo = null
    }
  } catch (error) {
    extendInfoError.value = 'JSON格式错误'
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitLoading.value = true

    // 准备提交数据
    const submitData = { ...formData }

    if (props.isEdit) {
      await deviceApi.update(submitData)
      message.success('更新成功')
    } else {
      await deviceApi.add(submitData)
      message.success('新增成功')
    }

    emit('success')
    emit('update:visible', false)
  } catch (error) {
    console.error('提交失败:', error)
    message.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  emit('update:visible', false)
}
</script>

<style lang="less" scoped>
.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}
</style>