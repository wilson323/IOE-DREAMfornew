<template>
  <div class="device-manage-page">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="header-title">
        <h2>设备管理</h2>
        <p class="header-description">管理消费终端设备，支持设备配置、状态监控和远程控制</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="showAddDeviceModal">
          <template #icon><PlusOutlined /></template>
          添加设备
        </a-button>
        <a-button @click="batchOperate">
          <template #icon><SettingOutlined /></template>
          批量操作
        </a-button>
        <a-button @click="refreshData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="设备编号">
          <a-input
            v-model:value="searchForm.deviceCode"
            placeholder="请输入设备编号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="设备名称">
          <a-input
            v-model:value="searchForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="设备类型">
          <a-select
            v-model:value="searchForm.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="POS">POS机</a-select-option>
            <a-select-option value="CARD_READER">读卡器</a-select-option>
            <a-select-option value="QR_SCANNER">扫码器</a-select-option>
            <a-select-option value="FACE_RECOGNIZER">人脸识别</a-select-option>
            <a-select-option value="VENDING_MACHINE">售货机</a-select-option>
            <a-select-option value="SELF_SERVICE">自助终端</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="设备状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="ONLINE">在线</a-select-option>
            <a-select-option value="OFFLINE">离线</a-select-option>
            <a-select-option value="MAINTENANCE">维护中</a-select-option>
            <a-select-option value="ERROR">故障</a-select-option>
            <a-select-option value="DISABLED">已禁用</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="所在区域">
          <a-cascader
            v-model:value="searchForm.regionPath"
            :options="regionOptions"
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择区域"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch" :loading="tableLoading">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="resetSearch">
              <template #icon><ClearOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 统计信息卡片 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="设备总数"
              :value="statistics.totalCount"
              :precision="0"
              :value-style="{ color: '#3f8600' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="在线设备"
              :value="statistics.onlineCount"
              :precision="0"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="故障设备"
              :value="statistics.errorCount"
              :precision="0"
              :value-style="{ color: '#ff4d4f' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="使用率"
              :value="statistics.usageRate"
              suffix="%"
              :precision="1"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 数据表格 -->
    <a-card class="table-card" :bordered="false">
      <a-table
        :columns="tableColumns"
        :data-source="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        :row-key="record => record.deviceId"
        :scroll="{ x: 1600 }"
        @change="handleTableChange"
      >
        <!-- 设备信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'deviceInfo'">
            <div class="device-info">
              <div class="device-name">{{ record.deviceName }}</div>
              <div class="device-code">编号: {{ record.deviceCode }}</div>
              <a-tag :color="getDeviceTypeColor(record.deviceType)" size="small">
                {{ getDeviceTypeText(record.deviceType) }}
              </a-tag>
            </div>
          </template>

          <!-- 设备状态 -->
          <template v-else-if="column.key === 'status'">
            <div class="device-status">
              <a-tag :color="getStatusColor(record.status)">
                <template #icon>
                  <component :is="getStatusIcon(record.status)" />
                </template>
                {{ getStatusText(record.status) }}
              </a-tag>
              <div class="last-heartbeat" v-if="record.lastHeartbeat">
                最后心跳: {{ formatDateTime(record.lastHeartbeat) }}
              </div>
            </div>
          </template>

          <!-- 所在区域 -->
          <template v-else-if="column.key === 'region'">
            <div class="region-info">
              <div>{{ record.regionName }}</div>
              <div class="region-location" v-if="record.location">
                {{ record.location }}
              </div>
            </div>
          </template>

          <!-- 今日交易 -->
          <template v-else-if="column.key === 'todayTransactions'">
            <div class="transaction-info">
              <div class="transaction-count">
                {{ record.todayTransactionCount || 0 }}笔
              </div>
              <div class="transaction-amount">
                ¥{{ formatMoney(record.todayAmount || 0) }}
              </div>
            </div>
          </template>

          <!-- 连接状态 -->
          <template v-else-if="column.key === 'connection'">
            <a-tooltip :title="record.connectionInfo">
              <a-badge :status="record.connectionStatus === 'connected' ? 'success' : 'error'" />
            </a-tooltip>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewDeviceDetail(record)">
                详情
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleRemoteControl(record)"
                :disabled="record.status !== 'ONLINE'"
              >
                远控
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu @click="({ key }) => handleMenuClick(key, record)">
                    <a-menu-item key="configure">设备配置</a-menu-item>
                    <a-menu-item key="restart" v-if="record.status === 'ONLINE'">重启设备</a-menu-item>
                    <a-menu-item key="maintenance" v-if="record.status !== 'MAINTENANCE'">维护模式</a-menu-item>
                    <a-menu-item key="enable" v-if="record.status === 'DISABLED'">启用设备</a-menu-item>
                    <a-menu-item key="disable" v-if="record.status !== 'DISABLED'">禁用设备</a-menu-item>
                    <a-menu-item key="viewLog">查看日志</a-menu-item>
                    <a-menu-item key="exportData">导出数据</a-menu-item>
                    <a-menu-item key="deleteDevice" danger>删除设备</a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加设备弹窗 -->
    <DeviceFormModal
      v-model:visible="deviceFormModalVisible"
      :device-info="currentDevice"
      :mode="formMode"
      @success="handleFormSuccess"
    />

    <!-- 设备详情弹窗 -->
    <DeviceDetailModal
      v-model:visible="detailModalVisible"
      :device-info="currentDevice"
    />

    <!-- 远程控制弹窗 -->
    <RemoteControlModal
      v-model:visible="remoteControlModalVisible"
      :device-info="currentDevice"
      @success="handleRemoteControlSuccess"
    />

    <!-- 批量操作弹窗 -->
    <BatchOperateModal
      v-model:visible="batchOperateModalVisible"
      :selected-devices="selectedDevices"
      @success="handleBatchOperateSuccess"
    />

    <!-- 导出弹窗 -->
    <ExportModal
      v-model:visible="exportModalVisible"
      :export-type="'device'"
      @export="handleExport"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SettingOutlined,
  ReloadOutlined,
  SearchOutlined,
  ClearOutlined,
  DownOutlined,
  WifiOutlined,
  DisconnectOutlined,
  ExclamationCircleOutlined,
  ToolOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import DeviceFormModal from './components/DeviceFormModal.vue'
import DeviceDetailModal from './components/DeviceDetailModal.vue'
import RemoteControlModal from './components/RemoteControlModal.vue'
import BatchOperateModal from './components/BatchOperateModal.vue'
import ExportModal from './components/ExportModal.vue'
import { deviceApi } from '@/api/business/consume/device'

// 响应式数据
const tableLoading = ref(false)
const tableData = ref([])
const deviceFormModalVisible = ref(false)
const detailModalVisible = ref(false)
const remoteControlModalVisible = ref(false)
const batchOperateModalVisible = ref(false)
const exportModalVisible = ref(false)
const currentDevice = ref(null)
const formMode = ref('add')
const selectedDevices = ref([])

// 搜索表单
const searchForm = reactive({
  deviceCode: '',
  deviceName: '',
  deviceType: '',
  status: '',
  regionPath: []
})

// 区域选项
const regionOptions = ref([])

// 统计数据
const statistics = reactive({
  totalCount: 0,
  onlineCount: 0,
  errorCount: 0,
  usageRate: 0
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列定义
const tableColumns = [
  {
    title: '设备信息',
    key: 'deviceInfo',
    width: 220,
    fixed: 'left'
  },
  {
    title: '设备状态',
    key: 'status',
    width: 150,
    filters: [
      { text: '在线', value: 'ONLINE' },
      { text: '离线', value: 'OFFLINE' },
      { text: '维护中', value: 'MAINTENANCE' },
      { text: '故障', value: 'ERROR' },
      { text: '已禁用', value: 'DISABLED' }
    ]
  },
  {
    title: '所在区域',
    key: 'region',
    width: 180
  },
  {
    title: '今日交易',
    key: 'todayTransactions',
    width: 150,
    align: 'right'
  },
  {
    title: '连接状态',
    key: 'connection',
    width: 100,
    align: 'center'
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right'
  }
]

// 行选择配置
const rowSelection = reactive({
  type: 'checkbox',
  onChange: (selectedRowKeys, selectedRows) => {
    selectedDevices.value = selectedRows
  }
})

// 方法
const fetchDeviceList = async () => {
  try {
    tableLoading.value = true
    const params = {
      ...searchForm,
      page: pagination.current,
      size: pagination.pageSize
    }

    const response = await deviceApi.getDeviceList(params)
    if (response.success) {
      tableData.value = response.data.records
      pagination.total = response.data.total
      // 更新统计数据
      Object.assign(statistics, response.data.statistics)
    } else {
      message.error(response.message || '获取设备列表失败')
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    message.error('获取设备列表失败')
  } finally {
    tableLoading.value = false
  }
}

const fetchRegionOptions = async () => {
  try {
    const response = await deviceApi.getRegionTree()
    if (response.success) {
      regionOptions.value = response.data
    }
  } catch (error) {
    console.error('获取区域选项失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchDeviceList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    deviceCode: '',
    deviceName: '',
    deviceType: '',
    status: '',
    regionPath: []
  })
  handleSearch()
}

const refreshData = () => {
  fetchDeviceList()
  message.success('数据已刷新')
}

const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchDeviceList()
}

const showAddDeviceModal = () => {
  currentDevice.value = null
  formMode.value = 'add'
  deviceFormModalVisible.value = true
}

const viewDeviceDetail = (record) => {
  currentDevice.value = record
  detailModalVisible.value = true
}

const handleRemoteControl = (record) => {
  currentDevice.value = record
  remoteControlModalVisible.value = true
}

const handleRemoteControlSuccess = () => {
  message.success('远程控制命令发送成功')
}

const batchOperate = () => {
  if (selectedDevices.value.length === 0) {
    message.warning('请先选择要操作的设备')
    return
  }
  batchOperateModalVisible.value = true
}

const handleBatchOperateSuccess = () => {
  selectedDevices.value = []
  fetchDeviceList()
  message.success('批量操作成功')
}

const handleFormSuccess = () => {
  fetchDeviceList()
  message.success(formMode.value === 'add' ? '设备添加成功' : '设备更新成功')
}

const handleMenuClick = async (key, record) => {
  currentDevice.value = record

  switch (key) {
    case 'configure':
      showConfigureModal(record)
      break
    case 'restart':
      await handleRestartDevice(record)
      break
    case 'maintenance':
      await handleMaintenanceMode(record)
      break
    case 'enable':
      await handleEnableDevice(record)
      break
    case 'disable':
      await handleDisableDevice(record)
      break
    case 'viewLog':
      viewDeviceLog(record)
      break
    case 'exportData':
      exportDeviceData(record)
      break
    case 'deleteDevice':
      await handleDeleteDevice(record)
      break
  }
}

const showConfigureModal = (record) => {
  currentDevice.value = record
  formMode.value = 'edit'
  deviceFormModalVisible.value = true
}

const handleRestartDevice = async (record) => {
  Modal.confirm({
    title: '确认重启设备',
    content: `确定要重启设备 ${record.deviceName} 吗？`,
    onOk: async () => {
      try {
        const response = await deviceApi.restartDevice(record.deviceId)
        if (response.success) {
          message.success('设备重启命令已发送')
        } else {
          message.error(response.message || '设备重启失败')
        }
      } catch (error) {
        console.error('重启设备失败:', error)
        message.error('设备重启失败')
      }
    }
  })
}

const handleMaintenanceMode = async (record) => {
  try {
    const response = await deviceApi.setMaintenanceMode(record.deviceId)
    if (response.success) {
      message.success('设备已进入维护模式')
      fetchDeviceList()
    } else {
      message.error(response.message || '设置维护模式失败')
    }
  } catch (error) {
    console.error('设置维护模式失败:', error)
    message.error('设置维护模式失败')
  }
}

const handleEnableDevice = async (record) => {
  try {
    const response = await deviceApi.enableDevice(record.deviceId)
    if (response.success) {
      message.success('设备启用成功')
      fetchDeviceList()
    } else {
      message.error(response.message || '设备启用失败')
    }
  } catch (error) {
    console.error('启用设备失败:', error)
    message.error('设备启用失败')
  }
}

const handleDisableDevice = async (record) => {
  Modal.confirm({
    title: '确认禁用设备',
    content: `确定要禁用设备 ${record.deviceName} 吗？禁用后将无法使用。`,
    onOk: async () => {
      try {
        const response = await deviceApi.disableDevice(record.deviceId)
        if (response.success) {
          message.success('设备禁用成功')
          fetchDeviceList()
        } else {
          message.error(response.message || '设备禁用失败')
        }
      } catch (error) {
        console.error('禁用设备失败:', error)
        message.error('设备禁用失败')
      }
    }
  })
}

const handleDeleteDevice = async (record) => {
  Modal.confirm({
    title: '确认删除设备',
    content: `确定要删除设备 ${record.deviceName} 吗？此操作不可恢复！`,
    okText: '确认删除',
    okType: 'danger',
    onOk: async () => {
      try {
        const response = await deviceApi.deleteDevice(record.deviceId)
        if (response.success) {
          message.success('设备删除成功')
          fetchDeviceList()
        } else {
          message.error(response.message || '设备删除失败')
        }
      } catch (error) {
        console.error('删除设备失败:', error)
        message.error('设备删除失败')
      }
    }
  })
}

const viewDeviceLog = (record) => {
  console.log('查看设备日志:', record)
  // 可以打开一个弹窗显示设备日志
}

const exportDeviceData = (record) => {
  console.log('导出设备数据:', record)
}

const showExportModal = () => {
  exportModalVisible.value = true
}

const handleExport = (exportParams) => {
  console.log('导出参数:', exportParams)
  message.success('导出成功')
}

// 工具方法
const formatMoney = (amount) => {
  return Number(amount || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getDeviceTypeColor = (type) => {
  const colorMap = {
    POS: 'blue',
    CARD_READER: 'green',
    QR_SCANNER: 'orange',
    FACE_RECOGNIZER: 'purple',
    VENDING_MACHINE: 'cyan',
    SELF_SERVICE: 'pink'
  }
  return colorMap[type] || 'default'
}

const getDeviceTypeText = (type) => {
  const textMap = {
    POS: 'POS机',
    CARD_READER: '读卡器',
    QR_SCANNER: '扫码器',
    FACE_RECOGNIZER: '人脸识别',
    VENDING_MACHINE: '售货机',
    SELF_SERVICE: '自助终端'
  }
  return textMap[type] || type
}

const getStatusColor = (status) => {
  const colorMap = {
    ONLINE: 'green',
    OFFLINE: 'default',
    MAINTENANCE: 'orange',
    ERROR: 'red',
    DISABLED: 'gray'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    ONLINE: '在线',
    OFFLINE: '离线',
    MAINTENANCE: '维护中',
    ERROR: '故障',
    DISABLED: '已禁用'
  }
  return textMap[status] || status
}

const getStatusIcon = (status) => {
  const iconMap = {
    ONLINE: WifiOutlined,
    OFFLINE: DisconnectOutlined,
    ERROR: ExclamationCircleOutlined,
    MAINTENANCE: ToolOutlined,
    DISABLED: StopOutlined
  }
  return iconMap[status] || WifiOutlined
}

// 生命周期
onMounted(() => {
  fetchDeviceList()
  fetchRegionOptions()
})
</script>

<style lang="less" scoped>
.device-manage-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);

  .header-title {
    h2 {
      margin: 0 0 4px 0;
      font-size: 20px;
      font-weight: 600;
      color: #262626;
    }

    .header-description {
      margin: 0;
      color: #8c8c8c;
      font-size: 14px;
    }
  }
}

.search-card,
.table-card {
  margin-bottom: 16px;
}

.stats-cards {
  margin-bottom: 16px;

  .stat-card {
    text-align: center;
    border-radius: 6px;

    :deep(.ant-statistic) {
      .ant-statistic-title {
        font-size: 14px;
        color: #8c8c8c;
      }

      .ant-statistic-content {
        font-size: 24px;
        font-weight: 600;
      }
    }
  }
}

.device-info {
  .device-name {
    font-weight: 600;
    color: #262626;
    margin-bottom: 4px;
  }

  .device-code {
    font-size: 12px;
    color: #8c8c8c;
    margin-bottom: 4px;
  }
}

.device-status {
  .last-heartbeat {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 4px;
  }
}

.region-info {
  .region-name {
    font-weight: 500;
    color: #262626;
  }

  .region-location {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 2px;
  }
}

.transaction-info {
  .transaction-count {
    font-weight: 600;
    color: #52c41a;
  }

  .transaction-amount {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 2px;
  }
}

:deep(.ant-table) {
  .ant-table-tbody > tr:hover > td {
    background: #f5f5f5;
  }

  .ant-table-selection-column {
    .ant-checkbox-wrapper {
      span {
        vertical-align: middle;
      }
    }
  }
}

@media (max-width: 768px) {
  .device-manage-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-cards {
    .ant-col {
      margin-bottom: 16px;
    }
  }
}
</style>