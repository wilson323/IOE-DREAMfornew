<template>
  <div class="device-detail">
    <a-page-header
      :title="device?.deviceName || '设备详情'"
      @back="handleBack"
      :tags="[
        { color: getStatusColor(device?.status), text: getStatusText(device?.status) }
      ]"
    >
      <template #extra>
        <a-space>
          <a-button v-permission="['smart:device:update']" @click="handleEdit">
            <template #icon><EditOutlined /></template>
            编辑
          </a-button>
          <a-button
            type="primary"
            danger
            v-permission="['smart:device:delete']"
            @click="handleDelete"
          >
            <template #icon><DeleteOutlined /></template>
            删除
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <div class="device-content">
      <a-spin :spinning="loading">
        <a-row :gutter="24">
          <!-- 基本信息 -->
          <a-col :span="12">
            <a-card title="基本信息" :bordered="false">
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="设备编码">
                  {{ device?.deviceCode }}
                </a-descriptions-item>
                <a-descriptions-item label="设备名称">
                  {{ device?.deviceName }}
                </a-descriptions-item>
                <a-descriptions-item label="设备类型">
                  {{ device?.deviceTypeName }}
                </a-descriptions-item>
                <a-descriptions-item label="设备品牌">
                  {{ device?.deviceBrand || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="设备型号">
                  {{ device?.deviceModel || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="设备序列号">
                  {{ device?.deviceSerial || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="所属区域">
                  {{ device?.areaName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="位置描述">
                  {{ device?.locationDesc || '-' }}
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-col>

          <!-- 网络信息 -->
          <a-col :span="12">
            <a-card title="网络信息" :bordered="false">
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="IP地址">
                  {{ device?.ipAddress || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="端口号">
                  {{ device?.port || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="MAC地址">
                  {{ device?.macAddress || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="通信协议">
                  {{ device?.protocolType || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="设备状态">
                  <DeviceStatusTag :status="device?.status" />
                </a-descriptions-item>
                <a-descriptions-item label="是否启用">
                  <a-tag :color="device?.isEnabled === 1 ? 'green' : 'red'">
                    {{ device?.isEnabled === 1 ? '启用' : '禁用' }}
                  </a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="24" style="margin-top: 24px">
          <!-- 联系信息 -->
          <a-col :span="12">
            <a-card title="联系信息" :bordered="false">
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="联系人">
                  {{ device?.contactPerson || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="联系电话">
                  {{ device?.contactPhone || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="供应商信息">
                  {{ device?.vendorInfo || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="安装时间">
                  {{ device?.installTime || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="保修到期时间">
                  {{ device?.warrantyEndTime || '-' }}
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-col>

          <!-- 系统信息 -->
          <a-col :span="12">
            <a-card title="系统信息" :bordered="false">
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="创建时间">
                  {{ device?.createTime }}
                </a-descriptions-item>
                <a-descriptions-item label="更新时间">
                  {{ device?.updateTime }}
                </a-descriptions-item>
                <a-descriptions-item label="创建人">
                  {{ device?.createUserName }}
                </a-descriptions-item>
                <a-descriptions-item label="版本号">
                  {{ device?.version }}
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-col>
        </a-row>

        <!-- 配置信息 -->
        <a-row style="margin-top: 24px">
          <a-col :span="24">
            <a-card title="设备配置" :bordered="false">
              <pre class="config-content">{{ formatJson(device?.configJson) }}</pre>
            </a-card>
          </a-col>
        </a-row>

        <!-- 扩展信息 -->
        <a-row style="margin-top: 24px" v-if="device?.extendInfo">
          <a-col :span="24">
            <a-card title="扩展信息" :bordered="false">
              <pre class="config-content">{{ formatJson(device?.extendInfo) }}</pre>
            </a-card>
          </a-col>
        </a-row>
      </a-spin>
    </div>

    <!-- 编辑弹窗 -->
    <DeviceModal
      v-model:visible="modalVisible"
      :device="device"
      :is-edit="true"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { deviceApi } from '/@/api/common/device/deviceApi'
import DeviceStatusTag from '/@/components/common/device/DeviceStatusTag.vue'
import DeviceModal from './DeviceModal.vue'

// 路由
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const modalVisible = ref(false)
const device = ref(null)

// 方法
const loadDeviceDetail = async () => {
  try {
    loading.value = true
    const deviceId = route.params.deviceId
    const response = await deviceApi.getDetail(deviceId)
    device.value = response.data
  } catch (error) {
    console.error('加载设备详情失败:', error)
    message.error('加载设备详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.back()
}

const handleEdit = () => {
  modalVisible.value = true
}

const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个设备吗？删除后无法恢复。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deviceApi.delete(device.value.deviceId)
        message.success('删除成功')
        router.push('/common/device')
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleModalSuccess = () => {
  modalVisible.value = false
  loadDeviceDetail()
}

const getStatusColor = (status) => {
  const colorMap = {
    1: 'green',    // 在线
    0: 'red',      // 离线
    2: 'orange',   // 故障
    3: 'blue'      // 维护中
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    1: '在线',
    0: '离线',
    2: '故障',
    3: '维护中'
  }
  return textMap[status] || '未知'
}

const formatJson = (data) => {
  try {
    return JSON.stringify(data, null, 2)
  } catch (error) {
    return '-'
  }
}

// 生命周期
onMounted(() => {
  loadDeviceDetail()
})
</script>

<style lang="less" scoped>
.device-detail {
  padding: 24px;

  .device-content {
    .config-content {
      background: #f5f5f5;
      padding: 16px;
      border-radius: 6px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
      line-height: 1.5;
      max-height: 300px;
      overflow-y: auto;
    }
  }
}
</style>