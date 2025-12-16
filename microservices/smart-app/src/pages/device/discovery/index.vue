<template>
  <view class="device-discovery-page">
    <!-- 页面头部 -->
    <view class="page-header">
      <u-card :show-head="false" :body-style="{ padding: '15px' }">
        <view class="header-content">
          <view class="title">设备自动发现</view>
          <view class="stats-row">
            <view class="stat-item">
              <view class="stat-value">{{ statistics.totalTasks }}</view>
              <view class="stat-label">扫描任务</view>
            </view>
            <view class="stat-item">
              <view class="stat-value">{{ statistics.totalDiscoveredDevices }}</view>
              <view class="stat-label">发现设备</view>
            </view>
            <view class="stat-item">
              <view class="stat-value">{{ statistics.onlineDevices }}</view>
              <view class="stat-label">在线设备</view>
            </view>
          </view>
        </view>
      </u-card>
    </view>

    <!-- 快速操作 -->
    <view class="quick-actions">
      <u-grid :col="3" :border="false">
        <u-grid-item @click="quickScan">
          <view class="action-item">
            <u-icon name="search" size="40" color="#409EFF"></u-icon>
            <text class="action-text">快速扫描</text>
          </view>
        </u-grid-item>
        <u-grid-item @click="customScan">
          <view class="action-item">
            <u-icon name="setting" size="40" color="#67C23A"></u-icon>
            <text class="action-text">自定义扫描</text>
          </view>
        </u-grid-item>
        <u-grid-item @click="viewResults">
          <view class="action-item">
            <u-icon name="list" size="40" color="#E6A23C"></u-icon>
            <text class="action-text">查看结果</text>
          </view>
        </u-grid-item>
      </u-grid>
    </view>

    <!-- 扫描表单 -->
    <view class="scan-form" v-if="showForm">
      <u-card :show-head="false" :body-style="{ padding: '15px' }">
        <u-form :model="scanForm" :rules="formRules" ref="formRef">
          <u-form-item label="网络范围" prop="networkRange" required>
            <u-input
              v-model="scanForm.networkRange"
              placeholder="例如: 192.168.1.0/24"
              :disabled="scanning"
            ></u-input>
          </u-form-item>

          <u-form-item label="扫描类型">
            <u-checkbox-group v-model="scanForm.scanTypes" @change="onScanTypesChange">
              <u-checkbox label="ping" name="scanTypes">Ping检测</u-checkbox>
              <u-checkbox label="port" name="scanTypes">端口扫描</u-checkbox>
              <u-checkbox label="protocol" name="scanTypes">协议检测</u-checkbox>
            </u-checkbox-group>
          </u-form-item>

          <u-form-item label="超时时间(秒)" prop="timeout">
            <u-slider
              v-model="scanForm.timeout"
              :min="1"
              :max="30"
              :step="1"
              :show-value="true"
            ></u-slider>
          </u-form-item>

          <u-form-item>
            <u-button
              type="primary"
              @click="startScan"
              :loading="scanning"
              :disabled="scanning"
            >
              {{ scanning ? '停止扫描' : '开始扫描' }}
            </u-button>
            <u-button @click="resetForm">重置</u-button>
          </u-form-item>
        </u-form>
      </u-card>
    </view>

    <!-- 扫描进度 -->
    <view class="scan-progress" v-if="scanning">
      <u-card :show-head="false" :body-style="{ padding: '15px' }">
        <view class="progress-header">
          <text class="progress-title">正在扫描...</text>
          <u-button size="mini" @click="stopScan" type="error">停止</u-button>
        </view>

        <view class="progress-info">
          <u-line-progress
            :percentage="progress.percentage"
            :show-pivot="false"
            active-color="#409EFF"
            height="8"
          ></u-line-progress>
        </view>

        <view class="progress-stats">
          <text>已扫描: {{ progress.scannedCount }}/{{ progress.totalCount }}</text>
          <text>发现设备: {{ progress.discoveredCount }}</text>
          <text>耗时: {{ progress.elapsedTime }}s</text>
        </view>
      </u-card>
    </view>

    <!-- 发现结果 -->
    <view class="discovery-results" v-if="discoveryResults.length > 0">
      <u-card :show-head="false" :body-style="{ padding: '15px' }">
        <view class="results-header">
          <text class="results-title">发现结果 ({{ discoveryResults.length }})</text>
          <view class="filter-buttons">
            <u-button
              size="mini"
              :type="filter.protocol === null ? 'default' : 'primary'"
              @click="filterByProtocol(null)"
            >
              全部
            </u-button>
            <u-button
              size="mini"
              type="primary"
              v-for="protocol in protocols"
              :key="protocol"
              @click="filterByProtocol(protocol)"
            >
              {{ getProtocolName(protocol) }}
            </u-button>
          </view>
        </view>

        <view class="device-list">
          <view
            class="device-item"
            v-for="device in filteredResults"
            :key="device.ipAddress"
            @click="viewDeviceDetail(device)"
          >
            <view class="device-header">
              <view class="device-info">
                <view class="device-name">{{ device.hostname || '未知设备' }}</view>
                <view class="device-ip">{{ device.ipAddress }}</view>
              </view>
              <view class="device-status">
                <u-tag
                  :type="device.reachable ? 'success' : 'error'"
                  size="mini"
                >
                  {{ device.reachable ? '在线' : '离线' }}
                </u-tag>
              </view>
            </view>

            <view class="device-details">
              <text class="detail-item">协议: {{ getProtocolName(device.detectedProtocol) }}</text>
              <text class="detail-item" v-if="device.macAddress">MAC: {{ device.macAddress }}</text>
              <text class="detail-item">端口: {{ getPortCount(device) }}</text>
              <text class="detail-item">响应: {{ device.responseTime }}ms</text>
            </view>

            <view class="device-actions">
              <u-button size="mini" type="primary" @click.stop="registerDevice(device)">
                注册
              </u-button>
              <u-button size="mini" @click.stop="testConnection(device)">
                测试
              </u-button>
            </view>
          </view>
        </view>
      </u-card>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else-if="!scanning && discoveryResults.length === 0">
      <u-empty
        mode="data"
        icon="search"
        text="暂无发现结果"
        description="请启动设备扫描来发现网络中的设备"
      >
        <u-button type="primary" @click="quickScan">开始扫描</u-button>
      </u-empty>
    </view>

    <!-- 设备详情弹窗 -->
    <u-popup
      v-model="detailModalVisible"
      mode="bottom"
      :round="10"
      :close-on-click-overlay="false"
    >
      <view class="detail-modal">
        <view class="modal-header">
          <text class="modal-title">设备详情</text>
          <u-icon name="close" @click="closeDetailModal"></u-icon>
        </view>

        <view class="detail-content" v-if="selectedDevice">
          <view class="detail-section">
            <text class="section-title">基本信息</text>
            <u-cell-group>
              <u-cell title="IP地址" :value="selectedDevice.ipAddress"></u-cell>
              <u-cell title="MAC地址" :value="selectedDevice.macAddress || '未知'"></u-cell>
              <u-cell title="主机名" :value="selectedDevice.hostname || '未知'"></u-cell>
              <u-cell title="协议类型" :value="getProtocolName(selectedDevice.detectedProtocol)"></u-cell>
              <u-cell title="状态">
                <template #right-icon>
                  <u-tag :type="selectedDevice.reachable ? 'success' : 'error'" size="mini">
                    {{ selectedDevice.reachable ? '在线' : '离线' }}
                  </u-tag>
                </template>
              </u-cell>
            </u-cell-group>
          </view>

          <view class="detail-section" v-if="selectedDevice.openPorts">
            <text class="section-title">开放端口</text>
            <view class="port-list">
              <u-tag
                v-for="(service, port) in selectedDevice.openPorts"
                :key="port"
                type="primary"
                size="mini"
                style="margin: 4px"
              >
                {{ port }}/{{ service }}
              </u-tag>
            </view>
          </view>
        </view>

        <view class="modal-actions">
          <u-button type="primary" @click="registerDevice(selectedDevice)">注册设备</u-button>
          <u-button @click="closeDetailModal">关闭</u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceDiscoveryApi } from '/@/api/business/device/discovery'

export default {
  name: 'DeviceDiscoveryIndex',
  setup() {
    // 响应式数据
    const formRef = ref()
    const showForm = ref(false)
    const scanning = ref(false)
    const detailModalVisible = ref(false)
    const selectedDevice = ref(null)

    const scanForm = reactive({
      networkRange: '',
      scanTypes: ['ping', 'port', 'protocol'],
      timeout: 5
    })

    const progress = reactive({
      percentage: 0,
      scannedCount: 0,
      totalCount: 0,
      discoveredCount: 0,
      elapsedTime: 0
    })

    const statistics = reactive({
      totalTasks: 0,
      totalDiscoveredDevices: 0,
      onlineDevices: 0,
      supportedProtocols: 0
    })

    const discoveryResults = ref([])
    const filter = reactive({
      protocol: null
    })

    // 表单验证规则
    const formRules = {
      networkRange: [
        { required: true, message: '请输入网络范围', trigger: 'blur' },
        { pattern: /^(?:\d{1,3}\.){3}\d{1,3}\/\d{1,2}$/, message: '请输入正确的CIDR格式', trigger: 'blur' }
      ]
    }

    // 协议列表
    const protocols = ref(['HIKVISION_VIDEO_V2_0', 'DAHUA_VIDEO_V2_0', 'UNIVIEW_VIDEO_V2_0', 'EZVIZ_VIDEO_V2_0', 'AXIS_CAMERA'])

    // 过滤后的结果
    const filteredResults = computed(() => {
      if (!filter.protocol) {
        return discoveryResults.value
      }
      return discoveryResults.value.filter(device => device.detectedProtocol === filter.protocol)
    })

    // 生命周期
    onMounted(() => {
      loadStatistics()
      loadRecentResults()
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
        uni.showToast({
          title: '加载统计失败',
          icon: 'none'
        })
      }
    }

    // 加载最近结果
    const loadRecentResults = async () => {
      try {
        const response = await deviceDiscoveryApi.getHistory(5)
        if (response.data && response.data.length > 0) {
          const latestResult = response.data[0]
          if (latestResult.discoveredDevices) {
            discoveryResults.value = Object.entries(latestResult.discoveredDevices).map(([ip, deviceInfo]) => ({
              ipAddress: ip,
              hostname: deviceInfo.hostname,
              macAddress: deviceInfo.macAddress,
              detectedProtocol: latestResult.detectedProtocol || 'UNKNOWN',
              reachable: deviceInfo.reachable,
              openPorts: deviceInfo.openPorts,
              responseTime: deviceInfo.responseTime
            }))
          }
        }
      } catch (error) {
        console.error('加载最近结果失败:', error)
      }
    }

    // 快速扫描
    const quickScan = () => {
      scanForm.networkRange = '192.168.1.0/24'
      scanForm.scanTypes = ['ping', 'port', 'protocol']
      scanForm.timeout = 5
      showForm.value = true
      startScan()
    }

    // 自定义扫描
    const customScan = () => {
      showForm.value = true
    }

    // 查看结果
    const viewResults = () => {
      showForm.value = false
      if (discoveryResults.value.length === 0) {
        uni.showToast({
          title: '暂无结果',
          icon: 'none'
        })
      }
    }

    // 开始扫描
    const startScan = async () => {
      try {
        if (!formRef.value) return

        // 验证表单
        const valid = await formRef.value.validate()
        if (!valid) return

        scanning.value = true
        resetProgress()

        const request = {
          networkRange: scanForm.networkRange,
          timeout: scanForm.timeout * 1000,
          enablePingScan: scanForm.scanTypes.includes('ping'),
          enablePortScan: scanForm.scanTypes.includes('port'),
          enableProtocolDetection: scanForm.scanTypes.includes('protocol')
        }

        // 模拟扫描过程
        simulateScanning()

        // 实际调用API（注释掉，使用模拟数据）
        /*
        const response = await deviceDiscoveryApi.startDiscovery(request)
        if (response.data) {
          scanProgressTimer = setInterval(updateProgress, 1000)
        }
        */

      } catch (error) {
        scanning.value = false
        uni.showToast({
          title: '扫描启动失败',
          icon: 'error'
        })
      }
    }

    // 停止扫描
    const stopScan = async () => {
      try {
        scanning.value = false
        if (scanProgressTimer) {
          clearInterval(scanProgressTimer)
        }

        // 实际调用API停止任务
        // await deviceDiscoveryApi.stopDiscovery(taskId)

        uni.showToast({
          title: '扫描已停止',
          icon: 'success'
        })
      } catch (error) {
        uni.showToast({
          title: '停止扫描失败',
          icon: 'error'
        })
      }
    }

    // 模拟扫描过程
    const simulateScanning = () => {
      const totalIPs = 254
      progress.totalCount = totalIPs
      progress.scannedCount = 0
      progress.discoveredCount = 0
      progress.percentage = 0

      const scanInterval = setInterval(() => {
        if (progress.scannedCount < totalIPs) {
          progress.scannedCount += Math.floor(Math.random() * 10) + 5
          progress.elapsedTime += 1
          progress.percentage = Math.floor((progress.scannedCount / totalIPs) * 100)

          // 模拟发现设备
          if (Math.random() > 0.8) {
            progress.discoveredCount += 1
            const mockDevice = {
              ipAddress: `192.168.1.${progress.scannedCount}`,
              hostname: `device-${progress.scannedCount}`,
              macAddress: generateRandomMAC(),
              detectedProtocol: protocols.value[Math.floor(Math.random() * protocols.value.length)],
              reachable: Math.random() > 0.2,
              openPorts: generateRandomPorts(),
              responseTime: Math.floor(Math.random() * 500) + 50
            }
            discoveryResults.value.unshift(mockDevice)
          }

          // 更新统计
          statistics.totalDiscoveredDevices = discoveryResults.value.length
          statistics.onlineDevices = discoveryResults.value.filter(d => d.reachable).length
        } else {
          clearInterval(scanInterval)
          scanning.value = false
          progress.percentage = 100

          uni.showToast({
            title: '扫描完成',
            icon: 'success'
          })
        }
      }, 1000)
    }

    // 重置进度
    const resetProgress = () => {
      progress.percentage = 0
      progress.scannedCount = 0
      progress.totalCount = 0
      progress.discoveredCount = 0
      progress.elapsedTime = 0
    }

    // 重置表单
    const resetForm = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
      scanForm.networkRange = ''
      scanForm.scanTypes = ['ping', 'port', 'protocol']
      scanForm.timeout = 5
    }

    // 扫描类型变化
    const onScanTypesChange = (value) => {
      scanForm.scanTypes = value
    }

    // 按协议筛选
    const filterByProtocol = (protocol) => {
      filter.protocol = protocol
    }

    // 查看设备详情
    const viewDeviceDetail = (device) => {
      selectedDevice.value = device
      detailModalVisible.value = true
    }

    // 注册设备
    const registerDevice = async (device) => {
      try {
        // 模拟注册
        uni.showLoading({
          title: '注册中...'
        })

        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '设备注册成功',
            icon: 'success'
          })
        }, 1000)

        // 实际调用API
        // const response = await deviceDiscoveryApi.registerDevice({
        //   deviceInfo: device.deviceInfo,
        //   detectedProtocol: device.detectedProtocol
        // }, true)
      } catch (error) {
        uni.showToast({
          title: '注册失败',
          icon: 'error'
        })
      }
    }

    // 测试连接
    const testConnection = async (device) => {
      try {
        uni.showLoading({
          title: '测试连接中...'
        })

        setTimeout(() => {
          uni.hideLoading()
          const success = Math.random() > 0.3
          uni.showToast({
            title: success ? '连接测试成功' : '连接测试失败',
            icon: success ? 'success' : 'error'
          })
        }, 2000)

        // 实际调用API
        // const response = await deviceDiscoveryApi.testConnection(device.ipAddress)
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '测试失败',
          icon: 'error'
        })
      }
    }

    // 关闭详情弹窗
    const closeDetailModal = () => {
      detailModalVisible.value = false
      selectedDevice.value = null
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

    // 获取端口数量
    const getPortCount = (device) => {
      return device.openPorts ? Object.keys(device.openPorts).length : 0
    }

    // 生成随机MAC地址
    const generateRandomMAC = () => {
      return '00:11:22:33:44:' +
             Math.floor(Math.random() * 256).toString(16).padStart(2, '0').toUpperCase() + ':' +
             Math.floor(Math.random() * 256).toString(16).padStart(2, '0').toUpperCase()
    }

    // 生成随机端口
    const generateRandomPorts = () => {
      const commonPorts = { 80: 'HTTP', 443: 'HTTPS', 554: 'RTSP', 161: 'SNMP' }
      const ports = {}

      Object.entries(commonPorts).forEach(([port, service]) => {
        if (Math.random() > 0.6) {
          ports[port] = service
        }
      })

      return ports
    }

    return {
      formRef,
      showForm,
      scanning,
      scanForm,
      formRules,
      progress,
      statistics,
      discoveryResults,
      filteredResults,
      filter,
      detailModalVisible,
      selectedDevice,
      protocols,
      quickScan,
      customScan,
      viewResults,
      startScan,
      stopScan,
      resetForm,
      onScanTypesChange,
      filterByProtocol,
      viewDeviceDetail,
      registerDevice,
      testConnection,
      closeDetailModal,
      getProtocolName,
      getPortCount
    }
  }
}
</script>

<style lang="scss" scoped>
.device-discovery-page {
  min-height: 100vh;
  background-color: #f5f5f5;

  .page-header {
    .header-content {
      .title {
        font-size: 20px;
        font-weight: 600;
        color: #333;
        margin-bottom: 12px;
      }

      .stats-row {
        display: flex;
        justify-content: space-around;

        .stat-item {
          text-align: center;

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #409EFF;
          }

          .stat-label {
            font-size: 12px;
            color: #666;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .quick-actions {
    margin: 16px 0;

    .action-item {
      text-align: center;
      padding: 20px 0;

      .action-text {
        display: block;
        font-size: 14px;
        color: #333;
        margin-top: 8px;
      }
    }
  }

  .scan-form {
    margin: 16px;
  }

  .scan-progress {
    margin: 16px;

    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .progress-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
    }

    .progress-info {
      margin-bottom: 12px;
    }

    .progress-stats {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #666;
    }
  }

  .discovery-results {
    margin: 16px;

    .results-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .results-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }

      .filter-buttons {
        display: flex;
        gap: 8px;
        overflow-x: auto;
      }
    }

    .device-list {
      .device-item {
        background: #fff;
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 12px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);

        .device-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .device-info {
            .device-name {
              font-size: 14px;
              font-weight: 500;
              color: #333;
            }

            .device-ip {
              font-size: 12px;
              color: #666;
            }
          }
        }

        .device-details {
          margin-bottom: 8px;

          .detail-item {
            font-size: 12px;
            color: #666;
            margin-right: 12px;
            margin-bottom: 4px;
          }
        }

        .device-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }

  .empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
  }

  .detail-modal {
    max-height: 70vh;
    overflow-y: auto;

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 16px 8px;
      border-bottom: 1px solid #eee;

      .modal-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
    }

    .detail-content {
      padding: 16px;

      .detail-section {
        margin-bottom: 20px;

        .section-title {
          font-size: 16px;
          font-weight: 500;
          color: #333;
          margin-bottom: 12px;
        }
      }

      .port-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
    }

    .modal-actions {
      display: flex;
      gap: 12px;
      padding: 16px;
      border-top: 1px solid #eee;
      justify-content: flex-end;
    }
  }
}
</style>