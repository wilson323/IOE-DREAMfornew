<template>
  <div class="device-detail">
    <!-- 基本信息 -->
    <a-descriptions title="基本信息" :column="2" bordered>
      <a-descriptions-item label="IP地址">{{ device.ipAddress }}</a-descriptions-item>
      <a-descriptions-item label="MAC地址">{{ device.macAddress || '未知' }}</a-descriptions-item>
      <a-descriptions-item label="主机名">{{ device.hostname || '未知' }}</a-descriptions-item>
      <a-descriptions-item label="设备名称">{{ device.deviceName || '未知设备' }}</a-descriptions-item>
      <a-descriptions-item label="厂商">{{ device.deviceInfo?.vendor || '未知' }}</a-descriptions-item>
      <a-descriptions-item label="设备类型">{{ device.deviceInfo?.deviceType || '未知' }}</a-descriptions-item>
      <a-descriptions-item label="协议类型">
        <a-tag :color="getProtocolColor(device.detectedProtocol)">
          {{ getProtocolName(device.detectedProtocol) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="状态">
        <a-badge
          :status="device.reachable ? 'success' : 'error'"
          :text="device.reachable ? '在线' : '离线'"
        />
      </a-descriptions-item>
    </a-descriptions>

    <!-- 网络信息 -->
    <a-descriptions title="网络信息" :column="2" bordered style="margin-top: 16px">
      <a-descriptions-item label="响应时间">{{ device.responseTime }}ms</a-descriptions-item>
      <a-descriptions-item label="置信度">{{ device.deviceInfo?.confidence || '未知' }}%</a-discriptions-item>
    </a-descriptions>

    <!-- 开放端口 -->
    <a-card title="开放端口" style="margin-top: 16px" size="small">
      <a-table
        :columns="portColumns"
        :data-source="portData"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'service'">
            <a-tag :color="getServiceColor(record.service)">
              {{ record.service }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag color="green">开放</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 设备详细信息 -->
    <a-descriptions title="设备详细信息" :column="1" bordered style="margin-top: 16px">
      <template v-if="deviceDetails.length > 0">
        <a-descriptions-item
          v-for="(value, key) in deviceDetails"
          :key="key"
          :label="formatDeviceKey(key)"
        >
          {{ formatDeviceValue(value) }}
        </a-descriptions-item>
      </template>
      <template v-else>
        <a-descriptions-item label="详细信息">暂无详细信息</a-descriptions-item>
      </template>
    </a-descriptions>

    <!-- 操作按钮 -->
    <div style="margin-top: 24px; text-align: center;">
      <a-space>
        <a-button type="primary" @click="registerDevice">
          <template #icon><PlusOutlined /></template>
          注册设备
        </a-button>
        <a-button @click="testConnection">
          <template #icon><ApiOutlined /></template>
          测试连接
        </a-button>
        <a-button @click="refreshDeviceInfo">
          <template #icon><ReloadOutlined /></template>
          刷新信息
        </a-button>
        <a-button @click="exportDeviceInfo">
          <template #icon><DownloadOutlined /></template>
          导出信息
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ApiOutlined,
  ReloadOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import { deviceDiscoveryApi } from '/@/api/business/device/discovery'

export default {
  name: 'DeviceDetail',
  components: {
    PlusOutlined,
    ApiOutlined,
    ReloadOutlined,
    DownloadOutlined
  },
  props: {
    device: {
      type: Object,
      required: true
    }
  },
  emits: ['registered', 'connection-tested', 'refreshed'],
  setup(props, { emit }) {
    // 端口表格列定义
    const portColumns = [
      {
        title: '端口',
        dataIndex: 'port',
        key: 'port',
        width: 80
      },
      {
        title: '服务',
        dataIndex: 'service',
        key: 'service',
        width: 120
      },
      {
        title: '状态',
        key: 'status',
        width: 80
      },
      {
        title: '描述',
        dataIndex: 'description',
        key: 'description',
        ellipsis: true
      }
    ]

    // 端口数据
    const portData = computed(() => {
      if (!props.device.openPorts) return []

      return Object.entries(props.device.openPorts).map(([port, service]) => ({
        port,
        service,
        status: 'open',
        description: getServiceDescription(port, service)
      }))
    })

    // 设备详细信息
    const deviceDetails = computed(() => {
      if (!props.device.deviceInfo) return []

      const details = {}

      // 添加设备信息中的所有字段
      Object.entries(props.device.deviceInfo).forEach(([key, value]) => {
        // 排除已经在基本信息中显示的字段
        if (!['openPorts', 'reachable', 'responseTime', 'hostname', 'macAddress', 'vendor', 'deviceType'].includes(key)) {
          details[key] = value
        }
      })

      return details
    })

    // 获取协议颜色
    const getProtocolColor = (protocol) => {
      const colorMap = {
        'HIKVISION_VIDEO_V2_0': 'red',
        'DAHUA_VIDEO_V2_0': 'orange',
        'UNIVIEW_VIDEO_V2_0': 'blue',
        'EZVIZ_VIDEO_V2_0': 'green',
        'AXIS_CAMERA': 'purple',
        'UNKNOWN': 'default'
      }
      return colorMap[protocol] || 'default'
    }

    // 获取协议名称
    const getProtocolName = (protocol) => {
      const nameMap = {
        'HIKVISION_VIDEO_V2_0': '海康威视',
        'DAHUA_VIDEO_V2_0': '大华',
        'UNIVIEW_VIDEO_V2_0': '宇视',
        'EZVIZ_VIDEO_V2_0': '萤石',
        'AXIS_CAMERA': 'Axis',
        'UNKNOWN': '未知'
      }
      return nameMap[protocol] || protocol
    }

    // 获取服务颜色
    const getServiceColor = (service) => {
      const colorMap = {
        'HTTP': 'blue',
        'HTTPS': 'green',
        'RTSP': 'red',
        'ONVIF': 'purple',
        'SNMP': 'orange',
        'MODBUS': 'cyan',
        'SSH': 'yellow',
        'TELNET': 'pink'
      }
      return colorMap[service] || 'default'
    }

    // 获取服务描述
    const getServiceDescription = (port, service) => {
      const descriptionMap = {
        '21': 'FTP文件传输协议',
        '22': 'SSH安全Shell协议',
        '23': 'Telnet远程登录协议',
        '25': 'SMTP邮件传输协议',
        '53': 'DNS域名解析协议',
        '80': 'HTTP超文本传输协议',
        '110': 'POP3邮局协议',
        '143': 'IMAP互联网消息访问协议',
        '443': 'HTTPS安全超文本传输协议',
        '554': 'RTSP实时流传输协议',
        '161': 'SNMP简单网络管理协议',
        '502': 'Modbus TCP工业控制协议'
      }

      return descriptionMap[port] || `${service}服务`
    }

    // 格式化设备键名
    const formatDeviceKey = (key) => {
      // 将下划线命名转换为可读的中文名称
      const keyMap = {
        'serialNumber': '序列号',
        'firmwareVersion': '固件版本',
        'hardwareVersion': '硬件版本',
        'model': '设备型号',
        'manufacturer': '制造商',
        'uptime': '运行时间',
        'location': '位置信息',
        'contact': '联系人',
        'systemName': '系统名称',
        'systemDescription': '系统描述',
        'bootTime': '启动时间',
        'loadAverage': '系统负载',
        'memoryUsage': '内存使用率',
        'cpuUsage': 'CPU使用率',
        'diskUsage': '磁盘使用率'
      }

      return keyMap[key] || key
    }

    // 格式化设备值
    const formatDeviceValue = (value) => {
      if (value === null || value === undefined) {
        return '未知'
      }

      if (typeof value === 'object') {
        return JSON.stringify(value, null, 2)
      }

      if (typeof value === 'boolean') {
        return value ? '是' : '否'
      }

      if (typeof value === 'number') {
        // 格式化数字，添加单位
        if (value > 1000000) {
          return (value / 1000000).toFixed(2) + 'M'
        } else if (value > 1000) {
          return (value / 1000).toFixed(2) + 'K'
        }
        return value.toString()
      }

      return String(value)
    }

    // 注册设备
    const registerDevice = async () => {
      try {
        const response = await deviceDiscoveryApi.registerDevice({
          deviceInfo: props.device.deviceInfo,
          detectedProtocol: props.device.detectedProtocol
        }, true)

        if (response.data && response.data.length > 0) {
          message.success(`设备 ${props.device.ipAddress} 注册成功`)
          emit('registered', response.data[0])
        } else {
          message.warning('设备注册完成，但未返回设备信息')
        }
      } catch (error) {
        message.error('注册设备失败: ' + error.message)
      }
    }

    // 测试连接
    const testConnection = async () => {
      try {
        const response = await deviceDiscoveryApi.testConnection(props.device.ipAddress)
        if (response.data) {
          message.success(`设备 ${props.device.ipAddress} 连接测试成功`)
          emit('connection-tested', { device: props.device, success: true })
        } else {
          message.warning(`设备 ${props.device.ipAddress} 连接测试失败`)
          emit('connection-tested', { device: props.device, success: false })
        }
      } catch (error) {
        message.error('连接测试失败: ' + error.message)
        emit('connection-tested', { device: props.device, success: false, error: error.message })
      }
    }

    // 刷新设备信息
    const refreshDeviceInfo = async () => {
      try {
        const response = await deviceDiscoveryApi.scanSingleDevice(props.device.ipAddress, 3000)
        if (response.data && response.data.discoveredDevices[props.device.ipAddress]) {
          const updatedDevice = response.data.discoveredDevices[props.device.ipAddress]
          // 这里应该通过父组件来更新设备信息
          message.success(`设备 ${props.device.ipAddress} 信息已刷新`)
          emit('refreshed', updatedDevice)
        } else {
          message.warning('未获取到设备信息')
        }
      } catch (error) {
        message.error('刷新设备信息失败: ' + error.message)
      }
    }

    // 导出设备信息
    const exportDeviceInfo = () => {
      const deviceInfo = {
        基本信息: {
          IP地址: props.device.ipAddress,
          MAC地址: props.device.macAddress,
          主机名: props.device.hostname,
          设备名称: props.device.deviceName,
          厂商: props.device.deviceInfo?.vendor,
          设备类型: props.device.deviceInfo?.deviceType,
          协议类型: getProtocolName(props.device.detectedProtocol),
          状态: props.device.reachable ? '在线' : '离线'
        },
        网络信息: {
          响应时间: `${props.device.responseTime}ms`,
          置信度: `${props.device.deviceInfo?.confidence || '未知'}%`
        },
        开放端口: portData.value.reduce((acc, item) => {
          acc[item.port] = `${item.service} (${item.description})`
          return acc
        }, {}),
        详细信息: deviceDetails.value
      }

      const jsonContent = JSON.stringify(deviceInfo, null, 2)
      const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', `device_${props.device.ipAddress}_${Date.now()}.json`)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)

      message.success('设备信息导出成功')
    }

    return {
      portColumns,
      portData,
      deviceDetails,
      getProtocolColor,
      getProtocolName,
      getServiceColor,
      getServiceDescription,
      formatDeviceKey,
      formatDeviceValue,
      registerDevice,
      testConnection,
      refreshDeviceInfo,
      exportDeviceInfo
    }
  }
}
</script>

<style lang="scss" scoped>
.device-detail {
  .ant-descriptions {
    margin-bottom: 0;
  }

  :deep(.ant-descriptions-item-label) {
    font-weight: 500;
  }
}
</style>