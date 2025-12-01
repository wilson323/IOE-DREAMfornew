<template>
  <div class="device-permission-config">
    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <a-card title="设备权限配置" size="small">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索设备名称或编码"
              @search="handleSearch"
              allow-clear
            />
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedDeviceType"
              placeholder="选择设备类型"
              style="width: 100%"
              allow-clear
              @change="handleDeviceTypeChange"
            >
              <a-select-option value="CAMERA">摄像头</a-select-option>
              <a-select-option value="ACCESS_CONTROLLER">门禁控制器</a-select-option>
              <a-select-option value="CONSUMPTION_TERMINAL">消费终端</a-select-option>
              <a-select-option value="ATTENDANCE_MACHINE">考勤机</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedPermissionType"
              placeholder="选择权限类型"
              style="width: 100%"
              allow-clear
              @change="handlePermissionTypeChange"
            >
              <a-select-option value="view">查看权限</a-select-option>
              <a-select-option value="control">控制权限</a-select-option>
              <a-select-option value="config">配置权限</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedDeviceStatus"
              placeholder="设备状态"
              style="width: 100%"
              allow-clear
              @change="handleDeviceStatusChange"
            >
              <a-select-option :value="0">离线</a-select-option>
              <a-select-option :value="1">在线</a-select-option>
              <a-select-option :value="2">故障</a-select-option>
              <a-select-option :value="3">维护中</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a-button type="primary" @click="showBatchConfigModal">
                <template #icon><SettingOutlined /></template>
                批量配置
              </a-button>
              <a-button @click="exportConfig">
                <template #icon><ExportOutlined /></template>
                导出配置
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 设备权限列表 -->
    <div class="device-permission-list">
      <a-table
        :columns="columns"
        :data-source="deviceList"
        :loading="loading"
        :pagination="pagination"
        row-key="deviceId"
        @change="handleTableChange"
        :scroll="{ x: 1600 }"
      >
        <!-- 设备信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'deviceInfo'">
            <div class="device-info">
              <div class="device-header">
                <div class="device-code">{{ record.deviceCode }}</div>
                <a-tag :color="getDeviceTypeColor(record.deviceType)">
                  {{ getDeviceTypeText(record.deviceType) }}
                </a-tag>
                <a-tag :color="getDeviceStatusColor(record.status)">
                  {{ getDeviceStatusText(record.status) }}
                </a-tag>
              </div>
              <div class="device-name">{{ record.deviceName }}</div>
              <div class="device-location">位置：{{ record.locationDesc }}</div>
              <div class="device-detail">
                {{ record.deviceBrand }} {{ record.deviceModel }} | {{ record.ipAddress }}
              </div>
            </div>
          </template>

          <!-- 权限配置 -->
          <template v-else-if="column.key === 'permissions'">
            <div class="permission-config">
              <div class="permission-item">
                <span class="permission-label">查看:</span>
                <a-switch
                  :checked="record.hasViewPermission"
                  :loading="record.viewLoading"
                  @change="(checked) => handlePermissionChange(record, 'VIEW', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">控制:</span>
                <a-switch
                  :checked="record.hasControlPermission"
                  :loading="record.controlLoading"
                  @change="(checked) => handlePermissionChange(record, 'CONTROL', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">配置:</span>
                <a-switch
                  :checked="record.hasConfigPermission"
                  :loading="record.configLoading"
                  @change="(checked) => handlePermissionChange(record, 'CONFIG', checked)"
                />
              </div>
            </div>
          </template>

          <!-- 安全级别 -->
          <template v-else-if="column.key === 'securityLevel'">
            <div class="security-level">
              <a-progress
                :percent="record.securityLevel * 20"
                size="small"
                :stroke-color="getSecurityLevelColor(record.securityLevel)"
                :show-info="false"
              />
              <span class="level-text">{{ getSecurityLevelText(record.securityLevel) }}</span>
            </div>
          </template>

          <!-- 时间限制 -->
          <template v-else-if="column.key === 'timeRestriction'">
            <div class="time-restriction">
              <div class="restriction-item">
                <span class="restriction-label">工作时间:</span>
                <a-switch
                  :checked="record.timeRestriction.workHours"
                  @change="(checked) => handleTimeRestrictionChange(record, 'workHours', checked)"
                />
              </div>
              <div class="restriction-item">
                <span class="restriction-label">周末:</span>
                <a-switch
                  :checked="record.timeRestriction.weekend"
                  @change="(checked) => handleTimeRestrictionChange(record, 'weekend', checked)"
                />
              </div>
              <div class="restriction-item">
                <span class="restriction-label">节假日:</span>
                <a-switch
                  :checked="record.timeRestriction.holiday"
                  @change="(checked) => handleTimeRestrictionChange(record, 'holiday', checked)"
                />
              </div>
            </div>
          </template>

          <!-- 访问限制 -->
          <template v-else-if="column.key === 'accessRestriction'">
            <div class="access-restriction">
              <div class="restriction-item">
                <span class="restriction-label">IP限制:</span>
                <a-switch
                  :checked="record.accessRestriction.ipEnabled"
                  @change="(checked) => handleAccessRestrictionChange(record, 'ipEnabled', checked)"
                />
              </div>
              <div class="restriction-item">
                <span class="restriction-label">地域限制:</span>
                <a-switch
                  :checked="record.accessRestriction.areaEnabled"
                  @change="(checked) => handleAccessRestrictionChange(record, 'areaEnabled', checked)"
                />
              </div>
              <div class="ip-list" v-if="record.accessRestriction.ipEnabled && record.ipRestrictions && record.ipRestrictions.length">
                <a-tag
                  v-for="(ip, index) in record.ipRestrictions"
                  :key="index"
                  closable
                  size="small"
                  @close="removeIpRestriction(record, index)"
                >
                  {{ ip }}
                </a-tag>
              </div>
            </div>
          </template>

          <!-- 最后更新 -->
          <template v-else-if="column.key === 'updateTime'">
            <div class="update-info">
              <div>{{ record.updateTime }}</div>
              <div class="update-user">{{ record.updateUser || '-' }}</div>
            </div>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewDeviceDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="editDevicePermission(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="showDeviceUserPermissionModal(record)">
                      <UserOutlined />
                      用户权限
                    </a-menu-item>
                    <a-menu-item @click="showDeviceConfigModal(record)">
                      <ControlOutlined />
                      设备配置
                    </a-menu-item>
                    <a-menu-item @click="testDeviceConnection(record)">
                      <ApiOutlined />
                      测试连接
                    </a-menu-item>
                    <a-menu-item @click="syncDevicePermission(record)">
                      <SyncOutlined />
                      同步权限
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="resetDevicePermission(record)" class="danger-menu-item">
                      <RollbackOutlined />
                      重置权限
                    </a-menu-item>
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
    </div>

    <!-- 设备详情模态框 -->
    <DeviceDetailModal
      v-model:visible="detailModalVisible"
      :device="selectedDevice"
      @close="handleDetailModalClose"
    />

    <!-- 编辑权限模态框 -->
    <EditDevicePermissionModal
      v-model:visible="editModalVisible"
      :device="selectedDevice"
      @success="handleEditSuccess"
      @close="handleEditModalClose"
    />

    <!-- IP限制模态框 -->
    <IpRestrictionModal
      v-model:visible="ipModalVisible"
      :device="selectedDevice"
      @success="handleIpSuccess"
      @close="handleIpModalClose"
    />

    <!-- 用户权限模态框 -->
    <DeviceUserPermissionModal
      v-model:visible="userPermissionModalVisible"
      :device="selectedDevice"
      @close="handleUserPermissionModalClose"
    />

    <!-- 设备配置模态框 -->
    <DeviceConfigModal
      v-model:visible="configModalVisible"
      :device="selectedDevice"
      @success="handleConfigSuccess"
      @close="handleConfigModalClose"
    />

    <!-- 批量配置模态框 -->
    <BatchDeviceConfigModal
      v-model:visible="batchConfigModalVisible"
      @success="handleBatchConfigSuccess"
      @close="handleBatchConfigModalClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  SettingOutlined,
  ExportOutlined,
  EyeOutlined,
  EditOutlined,
  UserOutlined,
  ControlOutlined,
  ApiOutlined,
  SyncOutlined,
  RollbackOutlined,
  DownOutlined
} from '@ant-design/icons-vue'

import { businessPermissionApi } from '@/api/smart-permission'

// 导入子组件
import DeviceDetailModal from './modals/DeviceDetailModal.vue'
import EditDevicePermissionModal from './modals/EditDevicePermissionModal.vue'
import IpRestrictionModal from './modals/IpRestrictionModal.vue'
import DeviceUserPermissionModal from './modals/DeviceUserPermissionModal.vue'
import DeviceConfigModal from './modals/DeviceConfigModal.vue'
import BatchDeviceConfigModal from './modals/BatchDeviceConfigModal.vue'

// 响应式数据
const searchText = ref('')
const selectedDeviceType = ref(null)
const selectedPermissionType = ref(null)
const selectedDeviceStatus = ref(null)
const loading = ref(false)
const deviceList = ref([])
const selectedDevice = ref(null)

// 模态框状态
const detailModalVisible = ref(false)
const editModalVisible = ref(false)
const ipModalVisible = ref(false)
const userPermissionModalVisible = ref(false)
const configModalVisible = ref(false)
const batchConfigModalVisible = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列配置
const columns = [
  {
    title: '设备信息',
    key: 'deviceInfo',
    width: 300,
    fixed: 'left'
  },
  {
    title: '权限配置',
    key: 'permissions',
    width: 180
  },
  {
    title: '安全级别',
    key: 'securityLevel',
    width: 120
  },
  {
    title: '时间限制',
    key: 'timeRestriction',
    width: 180
  },
  {
    title: '访问限制',
    key: 'accessRestriction',
    width: 220
  },
  {
    title: '最后更新',
    key: 'updateTime',
    width: 140
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 方法定义
const handleSearch = () => {
  pagination.current = 1
  fetchDeviceList()
}

const handleReset = () => {
  searchText.value = ''
  selectedDeviceType.value = null
  selectedPermissionType.value = null
  selectedDeviceStatus.value = null
  pagination.current = 1
  fetchDeviceList()
}

const handleDeviceTypeChange = () => {
  pagination.current = 1
  fetchDeviceList()
}

const handlePermissionTypeChange = () => {
  pagination.current = 1
  fetchDeviceList()
}

const handleDeviceStatusChange = () => {
  pagination.current = 1
  fetchDeviceList()
}

const handleTableChange = (paginationInfo) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  fetchDeviceList()
}

const handlePermissionChange = async (device, permissionType, checked) => {
  const loadingKey = `${permissionType.toLowerCase()}Loading`
  device[loadingKey] = true

  try {
    const requestData = {
      deviceId: device.deviceId,
      permissionType,
      enabled: checked
    }

    // 调用API更新权限
    await businessPermissionApi.updateDevicePermissionConfig(requestData)

    // 更新本地状态
    switch (permissionType) {
      case 'VIEW':
        device.hasViewPermission = checked
        break
      case 'CONTROL':
        device.hasControlPermission = checked
        break
      case 'CONFIG':
        device.hasConfigPermission = checked
        break
    }

    message.success(`${getPermissionText(permissionType)}权限${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('权限配置更新失败')
    console.error('权限配置更新失败:', error)
  } finally {
    device[loadingKey] = false
  }
}

const handleTimeRestrictionChange = async (device, restrictionType, checked) => {
  try {
    const requestData = {
      deviceId: device.deviceId,
      timeRestrictions: {
        ...device.timeRestriction,
        [restrictionType]: checked
      }
    }

    await businessPermissionApi.updateDevicePermissionConfig(requestData)
    device.timeRestriction[restrictionType] = checked

    message.success(`时间限制${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('时间限制配置失败')
    console.error('时间限制配置失败:', error)
  }
}

const handleAccessRestrictionChange = async (device, restrictionType, checked) => {
  try {
    const requestData = {
      deviceId: device.deviceId,
      accessRestrictions: {
        ...device.accessRestriction,
        [restrictionType]: checked
      }
    }

    await businessPermissionApi.updateDevicePermissionConfig(requestData)
    device.accessRestriction[restrictionType] = checked

    message.success(`访问限制${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('访问限制配置失败')
    console.error('访问限制配置失败:', error)
  }
}

const removeIpRestriction = async (device, index) => {
  try {
    const newIpRestrictions = [...device.ipRestrictions]
    newIpRestrictions.splice(index, 1)

    const requestData = {
      deviceId: device.deviceId,
      ipRestrictions: newIpRestrictions
    }

    await businessPermissionApi.updateDevicePermissionConfig(requestData)
    device.ipRestrictions = newIpRestrictions

    message.success('IP限制删除成功')
  } catch (error) {
    message.error('IP限制删除失败')
    console.error('IP限制删除失败:', error)
  }
}

const viewDeviceDetail = (device) => {
  selectedDevice.value = device
  detailModalVisible.value = true
}

const editDevicePermission = (device) => {
  selectedDevice.value = device
  editModalVisible.value = true
}

const showDeviceUserPermissionModal = (device) => {
  selectedDevice.value = device
  userPermissionModalVisible.value = true
}

const showDeviceConfigModal = (device) => {
  selectedDevice.value = device
  configModalVisible.value = true
}

const testDeviceConnection = async (device) => {
  try {
    message.loading('正在测试设备连接...', 0)
    const response = await businessPermissionApi.testDeviceConnection(device.deviceId)
    message.destroy()
    if (response.data) {
      message.success('设备连接测试成功')
    } else {
      message.warning('设备连接测试失败')
    }
  } catch (error) {
    message.destroy()
    message.error('设备连接测试失败')
    console.error('设备连接测试失败:', error)
  }
}

const syncDevicePermission = async (device) => {
  try {
    await businessPermissionApi.syncDevicePermission(device.deviceId)
    message.success('设备权限同步成功')
    fetchDeviceList()
  } catch (error) {
    message.error('设备权限同步失败')
    console.error('设备权限同步失败:', error)
  }
}

const resetDevicePermission = async (device) => {
  try {
    // 重置权限配置到默认状态
    const requestData = {
      deviceId: device.deviceId,
      hasViewPermission: false,
      hasControlPermission: false,
      hasConfigPermission: false,
      timeRestriction: {
        workHours: true,
        weekend: false,
        holiday: false
      },
      accessRestriction: {
        ipEnabled: false,
        areaEnabled: false
      },
      ipRestrictions: []
    }

    await businessPermissionApi.updateDevicePermissionConfig(requestData)

    // 更新本地状态
    Object.assign(device, requestData)
    message.success('设备权限重置成功')
  } catch (error) {
    message.error('设备权限重置失败')
    console.error('设备权限重置失败:', error)
  }
}

const showBatchConfigModal = () => {
  batchConfigModalVisible.value = true
}

const exportConfig = async () => {
  try {
    const response = await businessPermissionApi.exportDevicePermissionConfig({
      searchText: searchText.value,
      deviceType: selectedDeviceType.value,
      permissionType: selectedPermissionType.value,
      deviceStatus: selectedDeviceStatus.value
    })

    // 处理文件下载
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `device-permission-config-${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('配置导出成功')
  } catch (error) {
    message.error('配置导出失败')
    console.error('配置导出失败:', error)
  }
}

const handleDetailModalClose = () => {
  detailModalVisible.value = false
  selectedDevice.value = null
}

const handleEditModalClose = () => {
  editModalVisible.value = false
  selectedDevice.value = null
}

const handleIpModalClose = () => {
  ipModalVisible.value = false
  selectedDevice.value = null
}

const handleUserPermissionModalClose = () => {
  userPermissionModalVisible.value = false
  selectedDevice.value = null
}

const handleConfigModalClose = () => {
  configModalVisible.value = false
  selectedDevice.value = null
}

const handleBatchConfigModalClose = () => {
  batchConfigModalVisible.value = false
}

const handleEditSuccess = () => {
  editModalVisible.value = false
  fetchDeviceList()
  message.success('设备权限编辑成功')
}

const handleIpSuccess = () => {
  ipModalVisible.value = false
  fetchDeviceList()
  message.success('IP限制配置成功')
}

const handleConfigSuccess = () => {
  configModalVisible.value = false
  fetchDeviceList()
  message.success('设备配置成功')
}

const handleBatchConfigSuccess = () => {
  batchConfigModalVisible.value = false
  fetchDeviceList()
  message.success('批量配置成功')
}

const fetchDeviceList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      searchText: searchText.value,
      deviceType: selectedDeviceType.value,
      permissionType: selectedPermissionType.value,
      deviceStatus: selectedDeviceStatus.value
    }

    const response = await businessPermissionApi.getDevicePermissionConfig(params)

    if (response.data) {
      deviceList.value = response.data.records.map(device => ({
        ...device,
        viewLoading: false,
        controlLoading: false,
        configLoading: false,
        timeRestriction: device.timeRestriction || {
          workHours: true,
          weekend: false,
          holiday: false
        },
        accessRestriction: device.accessRestriction || {
          ipEnabled: false,
          areaEnabled: false
        },
        ipRestrictions: device.ipRestrictions || []
      }))
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    message.error('获取设备权限配置失败')
    console.error('获取设备权限配置失败:', error)
  } finally {
    loading.value = false
  }
}

const getDeviceTypeColor = (type) => {
  const colors = {
    'CAMERA': 'blue',
    'ACCESS_CONTROLLER': 'green',
    'CONSUMPTION_TERMINAL': 'orange',
    'ATTENDANCE_MACHINE': 'purple'
  }
  return colors[type] || 'default'
}

const getDeviceTypeText = (type) => {
  const textMap = {
    'CAMERA': '摄像头',
    'ACCESS_CONTROLLER': '门禁控制器',
    'CONSUMPTION_TERMINAL': '消费终端',
    'ATTENDANCE_MACHINE': '考勤机'
  }
  return textMap[type] || type
}

const getDeviceStatusColor = (status) => {
  const colors = {
    0: 'red',     // 离线
    1: 'green',   // 在线
    2: 'orange',  // 故障
    3: 'blue'     // 维护中
  }
  return colors[status] || 'default'
}

const getDeviceStatusText = (status) => {
  const textMap = {
    0: '离线',
    1: '在线',
    2: '故障',
    3: '维护中'
  }
  return textMap[status] || '未知'
}

const getSecurityLevelColor = (level) => {
  const colors = {
    1: 'blue',    // 公开级
    2: 'green',   // 内部级
    3: 'orange',  // 秘密级
    4: 'red',     // 机密级
    5: 'purple'   // 绝密级
  }
  return colors[level] || 'default'
}

const getSecurityLevelText = (level) => {
  const textMap = {
    1: '公开级',
    2: '内部级',
    3: '秘密级',
    4: '机密级',
    5: '绝密级'
  }
  return textMap[level] || '未知'
}

const getPermissionText = (permissionType) => {
  const textMap = {
    'VIEW': '查看',
    'CONTROL': '控制',
    'CONFIG': '配置'
  }
  return textMap[permissionType] || permissionType
}

// 生命周期
onMounted(() => {
  fetchDeviceList()
})
</script>

<style lang="less" scoped>
.device-permission-config {
  .search-bar {
    margin-bottom: 16px;
  }

  .device-permission-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .device-info {
      .device-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .device-code {
          font-weight: 500;
          font-size: 14px;
          color: #1890ff;
        }
      }

      .device-name {
        font-weight: 500;
        font-size: 16px;
        color: #262626;
        margin-bottom: 4px;
      }

      .device-location {
        font-size: 12px;
        color: #8c8c8c;
        margin-bottom: 4px;
      }

      .device-detail {
        font-size: 11px;
        color: #999;
      }
    }

    .permission-config {
      .permission-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .permission-label {
          width: 50px;
          font-size: 12px;
          color: #595959;
        }
      }
    }

    .security-level {
      .level-text {
        font-size: 12px;
        color: #595959;
        margin-left: 8px;
      }
    }

    .time-restriction {
      .restriction-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .restriction-label {
          width: 60px;
          font-size: 12px;
          color: #595959;
        }
      }
    }

    .access-restriction {
      .restriction-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .restriction-label {
          width: 60px;
          font-size: 12px;
          color: #595959;
        }
      }

      .ip-list {
        margin-top: 8px;

        .ant-tag {
          margin-right: 4px;
          margin-bottom: 4px;
        }
      }
    }

    .update-info {
      .update-user {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 4px;
      }
    }

    :deep(.ant-table-tbody > tr:hover > td) {
      background-color: #f5f5f5;
    }
  }
}

.danger-menu-item {
  color: #f5222d;
}
</style>