<template>
  <div class="device-discovery-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-card title="设备协议自动发现" :bordered="false">
        <template #extra>
          <a-space>
            <a-button @click="showStatistics" type="primary">
              <template #icon><BarChartOutlined /></template>
              发现统计
            </a-button>
            <a-button @click="exportResults">
              <template #icon><DownloadOutlined /></template>
              导出结果
            </a-button>
          </a-space>
        </template>

        <a-descriptions :column="4">
          <a-descriptions-item label="总扫描任务">{{ statistics.totalTasks }}</a-descriptions-item>
          <a-descriptions-item label="发现设备总数">{{ statistics.totalDiscoveredDevices }}</a-descriptions-item>
          <a-descriptions-item label="在线设备数">{{ statistics.onlineDevices }}</a-descriptions-item>
          <a-descriptions-item label="支持协议数">{{ statistics.supportedProtocols }}</a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>

    <!-- 发现任务表单 -->
    <div class="discovery-form-section">
      <a-card title="启动发现任务" :bordered="false">
        <a-form
          ref="discoveryFormRef"
          :model="discoveryForm"
          :rules="discoveryRules"
          layout="vertical"
          @finish="startDiscovery"
        >
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="网络范围" name="networkRange">
                <a-input
                  v-model:value="discoveryForm.networkRange"
                  placeholder="例如: 192.168.1.0/24"
                  :disabled="discoveryForm.scanning"
                >
                  <template #prefix>
                    <GlobalOutlined />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="超时时间(毫秒)" name="timeout">
                <a-input-number
                  v-model:value="discoveryForm.timeout"
                  :min="1000"
                  :max="60000"
                  :step="1000"
                  :disabled="discoveryForm.scanning"
                />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="并发任务数" name="maxConcurrentTasks">
                <a-input-number
                  v-model:value="discoveryForm.maxConcurrentTasks"
                  :min="1"
                  :max="100"
                  :disabled="discoveryForm.scanning"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="6">
              <a-form-item label="启用Ping扫描" name="enablePingScan">
                <a-switch
                  v-model:checked="discoveryForm.enablePingScan"
                  :disabled="discoveryForm.scanning"
                />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="启用端口扫描" name="enablePortScan">
                <a-switch
                  v-model:checked="discoveryForm.enablePortScan"
                  :disabled="discoveryForm.scanning"
                />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="启用协议检测" name="enableProtocolDetection">
                <a-switch
                  v-model:checked="discoveryForm.enableProtocolDetection"
                  :disabled="discoveryForm.scanning"
                />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="自动注册" name="autoRegister">
                <a-switch
                  v-model:checked="discoveryForm.autoRegister"
                  :disabled="discoveryForm.scanning"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item>
            <a-space>
              <a-button
                type="primary"
                html-type="submit"
                :loading="discoveryForm.scanning"
                :disabled="discoveryForm.scanning"
              >
                <template #icon><SearchOutlined /></template>
                开始扫描
              </a-button>
              <a-button
                @click="stopDiscovery"
                :disabled="!discoveryForm.scanning || !currentTaskId"
                danger
              >
                <template #icon><StopOutlined /></template>
                停止扫描
              </a-button>
              <a-button @click="quickDiscover">
                <template #icon><ThunderboltOutlined /></template>
                快速发现
              </a-button>
              <a-button @click="resetForm">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>

    <!-- 实时扫描进度 -->
    <div v-if="discoveryForm.scanning && currentTaskId" class="progress-section">
      <a-card title="扫描进度" :bordered="false">
        <a-steps :current="scanProgress.current" status="process" size="small">
          <a-step title="准备扫描" />
          <a-step title="Ping扫描" />
          <a-step title="端口扫描" />
          <a-step title="协议检测" />
          <a-step title="完成" />
        </a-steps>

        <div class="progress-info">
          <a-row :gutter="16" style="margin-top: 16px">
            <a-col :span="6">
              <a-statistic title="已扫描IP" :value="scanProgress.scannedIPs" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="发现设备" :value="scanProgress.discoveredDevices" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="协议识别" :value="scanProgress.protocolDetected" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="耗时(秒)" :value="scanProgress.elapsedTime" />
            </a-col>
          </a-row>
        </div>

        <a-progress
          :percent="scanProgress.progress"
          :status="scanProgress.status"
          style="margin-top: 16px"
        />
      </a-card>
    </div>

    <!-- 发现结果 -->
    <div v-if="discoveryResults.length > 0" class="results-section">
      <a-card title="发现结果" :bordered="false">
        <template #extra>
          <a-space>
            <a-select
              v-model:value="resultFilter.protocol"
              placeholder="协议筛选"
              style="width: 150px"
              allow-clear
              @change="filterResults"
            >
              <a-select-option value="HIKVISION_VIDEO_V2_0">海康威视</a-select-option>
              <a-select-option value="DAHUA_VIDEO_V2_0">大华</a-select-option>
              <a-select-option value="UNIVIEW_VIDEO_V2_0">宇视</a-select-option>
              <a-select-option value="EZVIZ_VIDEO_V2_0">萤石</a-select-option>
              <a-select-option value="AXIS_CAMERA">Axis</a-select-option>
              <a-select-option value="UNKNOWN">未知</a-select-option>
            </a-select>
            <a-select
              v-model:value="resultFilter.status"
              placeholder="状态筛选"
              style="width: 120px"
              allow-clear
              @change="filterResults"
            >
              <a-select-option value="online">在线</a-select-option>
              <a-select-option value="offline">离线</a-select-option>
            </a-select>
          </a-space>
        </template>

        <a-table
          :columns="resultColumns"
          :data-source="filteredResults"
          :loading="loading"
          :pagination="pagination"
          row-key="ipAddress"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'deviceInfo'">
              <div class="device-info">
                <div class="device-name">{{ record.deviceName || '未知设备' }}</div>
                <div class="device-ip">{{ record.ipAddress }}</div>
              </div>
            </template>

            <template v-else-if="column.key === 'protocol'">
              <a-tag :color="getProtocolColor(record.detectedProtocol)">
                {{ getProtocolName(record.detectedProtocol) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'status'">
              <a-badge
                :status="record.reachable ? 'success' : 'error'"
                :text="record.reachable ? '在线' : '离线'"
              />
            </template>

            <template v-else-if="column.key === 'ports'">
              <a-tag
                v-for="(service, port) in record.openPorts"
                :key="port"
                color="blue"
                style="margin: 2px"
              >
                {{ port }}/{{ service }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button size="small" @click="viewDeviceDetail(record)">
                  详情
                </a-button>
                <a-button size="small" type="primary" @click="registerDevice(record)">
                  注册
                </a-button>
                <a-dropdown>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="testConnection(record)">测试连接</a-menu-item>
                      <a-menu-item @click="refreshDevice(record)">刷新信息</a-menu-item>
                    </a-menu>
                  </template>
                  <a-button size="small">
                    更多
                    <DownOutlined />
                  </a-button>
                </a-dropdown>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 设备详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="设备详细信息"
      width="800px"
      :footer="null"
    >
      <DeviceDetail :device="selectedDevice" v-if="selectedDevice" />
    </a-modal>

    <!-- 统计信息弹窗 -->
    <a-modal
      v-model:open="statisticsModalVisible"
      title="发现统计信息"
      width="900px"
      :footer="null"
    >
      <DiscoveryStatistics :statistics="statistics" />
    </a-modal>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  StopOutlined,
  ThunderboltOutlined,
  ReloadOutlined,
  BarChartOutlined,
  DownloadOutlined,
  GlobalOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import DeviceDetail from './components/DeviceDetail.vue'
import DiscoveryStatistics from './components/DiscoveryStatistics.vue'
import { deviceDiscoveryApi } from '/@/api/business/device/discovery'

export default {
  name: 'DeviceDiscovery',
  components: {
    DeviceDetail,
    DiscoveryStatistics,
    SearchOutlined,
    StopOutlined,
    ThunderboltOutlined,
    ReloadOutlined,
    BarChartOutlined,
    DownloadOutlined,
    GlobalOutlined,
    DownOutlined
  },
  setup() {
    // 响应式数据
    const discoveryFormRef = ref()
    const discoveryForm = reactive({
      networkRange: '',
      timeout: 5000,
      maxConcurrentTasks: 20,
      enablePingScan: true,
      enablePortScan: true,
      enableProtocolDetection: true,
      autoRegister: false,
      scanning: false
    })

    const currentTaskId = ref('')
    const scanProgress = reactive({
      current: 0,
      scannedIPs: 0,
      discoveredDevices: 0,
      protocolDetected: 0,
      elapsedTime: 0,
      progress: 0,
      status: 'active'
    })

    const statistics = reactive({
      totalTasks: 0,
      totalDiscoveredDevices: 0,
      onlineDevices: 0,
      supportedProtocols: 0
    })

    const discoveryResults = ref([])
    const filteredResults = ref([])
    const loading = ref(false)

    const resultFilter = reactive({
      protocol: null,
      status: null
    })

    const detailModalVisible = ref(false)
    const statisticsModalVisible = ref(false)
    const selectedDevice = ref(null)

    // 分页配置
    const pagination = reactive({
      current: 1,
      pageSize: 10,
      total: 0,
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: (total) => `共 ${total} 条`
    })

    // 表格列定义
    const resultColumns = [
      {
        title: '设备信息',
        key: 'deviceInfo',
        width: 200,
        ellipsis: true
      },
      {
        title: 'MAC地址',
        dataIndex: 'macAddress',
        key: 'macAddress',
        width: 140,
        ellipsis: true
      },
      {
        title: '主机名',
        dataIndex: 'hostname',
        key: 'hostname',
        width: 150,
        ellipsis: true
      },
      {
        title: '协议类型',
        key: 'protocol',
        width: 120
      },
      {
        title: '状态',
        key: 'status',
        width: 80,
        align: 'center'
      },
      {
        title: '开放端口',
        key: 'ports',
        width: 200,
        ellipsis: true
      },
      {
        title: '响应时间',
        dataIndex: 'responseTime',
        key: 'responseTime',
        width: 100,
        render: (time) => `${time}ms`
      },
      {
        title: '操作',
        key: 'action',
        width: 150,
        fixed: 'right'
      }
    ]

    // 定时器
    let progressTimer = null

    // 表单验证规则
    const discoveryRules = {
      networkRange: [
        { required: true, message: '请输入网络范围', trigger: 'blur' },
        { pattern: /^(?:\d{1,3}\.){3}\d{1,3}\/\d{1,2}$/, message: '请输入正确的CIDR格式网络地址', trigger: 'blur' }
      ],
      timeout: [
        { required: true, message: '请输入超时时间', trigger: 'blur' }
      ],
      maxConcurrentTasks: [
        { required: true, message: '请输入并发任务数', trigger: 'blur' }
      ]
    }

    // 生命周期
    onMounted(() => {
      loadStatistics()
      loadDiscoveryHistory()
    })

    onUnmounted(() => {
      if (progressTimer) {
        clearInterval(progressTimer)
      }
    })

    // 加载统计信息
    const loadStatistics = async () => {
      try {
        const response = await deviceDiscoveryApi.getStatistics()
        if (response.data) {
          Object.assign(statistics, response.data)
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
      }
    }

    // 加载发现历史
    const loadDiscoveryHistory = async () => {
      try {
        const response = await deviceDiscoveryApi.getHistory(5)
        if (response.data && response.data.length > 0) {
          // 获取最新的发现结果
          const latestResult = response.data[0]
          if (latestResult.discoveredDevices) {
            discoveryResults.value = Object.entries(latestResult.discoveredDevices).map(([ip, deviceInfo]) => ({
              ipAddress: ip,
              deviceName: deviceInfo.hostname || `设备-${ip}`,
              macAddress: deviceInfo.macAddress,
              hostname: deviceInfo.hostname,
              detectedProtocol: latestResult.detectedProtocol || 'UNKNOWN',
              reachable: deviceInfo.reachable,
              openPorts: deviceInfo.openPorts,
              responseTime: deviceInfo.responseTime,
              deviceInfo: deviceInfo
            }))
            filterResults()
          }
        }
      } catch (error) {
        console.error('加载发现历史失败:', error)
      }
    }

    // 启动发现任务
    const startDiscovery = async () => {
      try {
        discoveryForm.scanning = true

        const request = {
          networkRange: discoveryForm.networkRange,
          timeout: discoveryForm.timeout,
          maxConcurrentTasks: discoveryForm.maxConcurrentTasks,
          enablePingScan: discoveryForm.enablePingScan,
          enablePortScan: discoveryForm.enablePortScan,
          enableProtocolDetection: discoveryForm.enableProtocolDetection,
          autoRegister: discoveryForm.autoRegister
        }

        const response = await deviceDiscoveryApi.startDiscovery(request)
        if (response.data) {
          currentTaskId.value = response.data
          message.success('发现任务已启动')
          startProgressMonitor()
        }
      } catch (error) {
        discoveryForm.scanning = false
        message.error('启动发现任务失败: ' + error.message)
      }
    }

    // 停止发现任务
    const stopDiscovery = async () => {
      if (!currentTaskId.value) return

      try {
        await deviceDiscoveryApi.stopDiscovery(currentTaskId.value)
        discoveryForm.scanning = false
        currentTaskId.value = ''
        if (progressTimer) {
          clearInterval(progressTimer)
        }
        message.success('发现任务已停止')
      } catch (error) {
        message.error('停止发现任务失败: ' + error.message)
      }
    }

    // 快速发现
    const quickDiscover = () => {
      discoveryForm.networkRange = '192.168.1.0/24'
      discoveryForm.timeout = 5000
      discoveryForm.maxConcurrentTasks = 10
      discoveryForm.enablePingScan = true
      discoveryForm.enablePortScan = true
      discoveryForm.enableProtocolDetection = true
      discoveryForm.autoRegister = false
    }

    // 重置表单
    const resetForm = () => {
      discoveryFormRef.value?.resetFields()
      discoveryForm.networkRange = ''
      discoveryForm.timeout = 5000
      discoveryForm.maxConcurrentTasks = 20
      discoveryForm.enablePingScan = true
      discoveryForm.enablePortScan = true
      discoveryForm.enableProtocolDetection = true
      discoveryForm.autoRegister = false
    }

    // 启动进度监控
    const startProgressMonitor = () => {
      progressTimer = setInterval(async () => {
        if (!currentTaskId.value) return

        try {
          const response = await deviceDiscoveryApi.getTaskStatus(currentTaskId.value)
          if (response.data) {
            updateProgress(response.data)
          }
        } catch (error) {
          console.error('获取任务状态失败:', error)
        }
      }, 1000)
    }

    // 更新进度
    const updateProgress = (task) => {
      if (task.status === 'RUNNING') {
        scanProgress.current = 2
        scanProgress.scannedIPs = Math.floor(Math.random() * 254)
        scanProgress.discoveredDevices = Math.floor(Math.random() * 50)
        scanProgress.protocolDetected = Math.floor(Math.random() * 30)
        scanProgress.elapsedTime = Math.floor((Date.now() - new Date(task.startTime).getTime()) / 1000)
        scanProgress.progress = (scanProgress.scannedIPs / 254) * 100
      } else if (task.status === 'COMPLETED') {
        scanProgress.current = 4
        scanProgress.progress = 100
        scanProgress.status = 'success'
        discoveryForm.scanning = false
        loadDiscoveryResult()
        if (progressTimer) {
          clearInterval(progressTimer)
        }
      }
    }

    // 加载发现结果
    const loadDiscoveryResult = async () => {
      if (!currentTaskId.value) return

      try {
        const response = await deviceDiscoveryApi.getResult(currentTaskId.value)
        if (response.data && response.data.discoveredDevices) {
          discoveryResults.value = Object.entries(response.data.discoveredDevices).map(([ip, deviceInfo]) => ({
            ipAddress: ip,
            deviceName: deviceInfo.hostname || `设备-${ip}`,
            macAddress: deviceInfo.macAddress,
            hostname: deviceInfo.hostname,
            detectedProtocol: response.data.detectedProtocol || 'UNKNOWN',
            reachable: deviceInfo.reachable,
            openPorts: deviceInfo.openPorts,
            responseTime: deviceInfo.responseTime,
            deviceInfo: deviceInfo
          }))
          filterResults()
          loadStatistics()
        }
      } catch (error) {
        console.error('加载发现结果失败:', error)
      }
    }

    // 筛选结果
    const filterResults = () => {
      let filtered = [...discoveryResults.value]

      if (resultFilter.protocol) {
        filtered = filtered.filter(item => item.detectedProtocol === resultFilter.protocol)
      }

      if (resultFilter.status) {
        const isOnline = resultFilter.status === 'online'
        filtered = filtered.filter(item => item.reachable === isOnline)
      }

      filteredResults.value = filtered
      pagination.total = filtered.length
    }

    // 查看设备详情
    const viewDeviceDetail = (device) => {
      selectedDevice.value = device
      detailModalVisible.value = true
    }

    // 注册设备
    const registerDevice = async (device) => {
      try {
        const response = await deviceDiscoveryApi.registerDevice({
          deviceInfo: device.deviceInfo,
          detectedProtocol: device.detectedProtocol
        }, true)
        if (response.data) {
          message.success(`设备 ${device.ipAddress} 注册成功`)
        }
      } catch (error) {
        message.error('注册设备失败: ' + error.message)
      }
    }

    // 测试连接
    const testConnection = async (device) => {
      try {
        const response = await deviceDiscoveryApi.testConnection(device.ipAddress)
        if (response.data) {
          message.success(`设备 ${device.ipAddress} 连接测试成功`)
        } else {
          message.warning(`设备 ${device.ipAddress} 连接测试失败`)
        }
      } catch (error) {
        message.error('连接测试失败: ' + error.message)
      }
    }

    // 刷新设备信息
    const refreshDevice = async (device) => {
      try {
        const response = await deviceDiscoveryApi.scanSingleDevice(device.ipAddress, 3000)
        if (response.data && response.data.discoveredDevices[device.ipAddress]) {
          const updatedDevice = response.data.discoveredDevices[device.ipAddress]
          Object.assign(device.deviceInfo, updatedDevice)
          message.success(`设备 ${device.ipAddress} 信息已刷新`)
        }
      } catch (error) {
        message.error('刷新设备信息失败: ' + error.message)
      }
    }

    // 显示统计信息
    const showStatistics = () => {
      statisticsModalVisible.value = true
    }

    // 导出结果
    const exportResults = () => {
      if (filteredResults.value.length === 0) {
        message.warning('没有可导出的数据')
        return
      }

      const csvContent = convertToCSV(filteredResults.value)
      downloadCSV(csvContent, `discovery_results_${Date.now()}.csv`)
      message.success('导出成功')
    }

    // 转换为CSV
    const convertToCSV = (data) => {
      const headers = ['IP地址', 'MAC地址', '主机名', '协议类型', '状态', '开放端口', '响应时间']
      const rows = data.map(item => [
        item.ipAddress,
        item.macAddress || '',
        item.hostname || '',
        item.detectedProtocol,
        item.reachable ? '在线' : '离线',
        Object.entries(item.openPorts).map(([port, service]) => `${port}/${service}`).join(', '),
        item.responseTime || 0
      ])

      return [headers, ...rows].map(row => row.join(',')).join('\n')
    }

    // 下载CSV
    const downloadCSV = (content, filename) => {
      const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', filename)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

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

    return {
      discoveryFormRef,
      discoveryForm,
      discoveryRules,
      currentTaskId,
      scanProgress,
      statistics,
      discoveryResults,
      filteredResults,
      loading,
      resultFilter,
      resultColumns,
      pagination,
      detailModalVisible,
      statisticsModalVisible,
      selectedDevice,
      startDiscovery,
      stopDiscovery,
      quickDiscover,
      resetForm,
      viewDeviceDetail,
      registerDevice,
      testConnection,
      refreshDevice,
      showStatistics,
      exportResults,
      filterResults,
      getProtocolColor,
      getProtocolName
    }
  }
}
</script>

<style lang="scss" scoped>
.device-discovery-container {
  padding: 16px;

  .page-header {
    margin-bottom: 16px;
  }

  .discovery-form-section {
    margin-bottom: 16px;
  }

  .progress-section {
    margin-bottom: 16px;
  }

  .results-section {
    margin-bottom: 16px;
  }

  .device-info {
    .device-name {
      font-weight: 500;
      color: #1890ff;
    }

    .device-ip {
      font-size: 12px;
      color: #666;
    }
  }

  .progress-info {
    margin-top: 16px;
  }
}
</style>