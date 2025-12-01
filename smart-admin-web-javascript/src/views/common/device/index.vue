<template>
  <div class="device-management">
    <!-- 搜索区域 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="设备编码">
          <a-input
            v-model:value="searchForm.deviceCode"
            placeholder="请输入设备编码"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="设备名称">
          <a-input
            v-model:value="searchForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="设备类型">
          <DeviceTypeSelect
            v-model:value="searchForm.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">在线</a-select-option>
            <a-select-option :value="0">离线</a-select-option>
            <a-select-option :value="2">故障</a-select-option>
            <a-select-option :value="3">维护中</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="IP地址">
          <a-input
            v-model:value="searchForm.ipAddress"
            placeholder="请输入IP地址"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="statistics-row">
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="设备总数"
            :value="statistics.totalDevices"
            :value-style="{ color: '#3f8600' }"
          >
            <template #suffix>
              <DesktopOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="在线设备"
            :value="statistics.onlineDevices"
            :value-style="{ color: '#3f8600' }"
          >
            <template #suffix>
              <WifiOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="离线设备"
            :value="statistics.offlineDevices"
            :value-style="{ color: '#cf1322' }"
          >
            <template #suffix>
              <DisconnectOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="故障设备"
            :value="statistics.faultDevices"
            :value-style="{ color: '#fa8c16' }"
          >
            <template #suffix>
              <ExclamationCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 操作区域 -->
    <a-card class="table-card" :bordered="false">
      <div class="table-operations">
        <a-space>
          <a-button type="primary" @click="handleAdd" v-permission="['smart:device:add']">
            <template #icon><PlusOutlined /></template>
            新增设备
          </a-button>
          <a-button @click="handleRefresh" :loading="refreshLoading">
            <template #icon><ReloadOutlined /></template>
            刷新状态
          </a-button>
          <a-button
            type="primary"
            danger
            :disabled="!hasSelected"
            @click="handleBatchDelete"
            v-permission="['smart:device:delete']"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
          <a-button @click="handleExport" :loading="exportLoading">
            <template #icon><ExportOutlined /></template>
            导出数据
          </a-button>
        </a-space>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        :row-key="record => record.deviceId"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <!-- 状态列 -->
        <template #status="{ record }">
          <DeviceStatusTag :status="record.status" />
        </template>

        <!-- 是否启用列 -->
        <template #isEnabled="{ record }">
          <a-switch
            :checked="record.isEnabled === 1"
            :disabled="!hasPermission('smart:device:update')"
            @change="(checked) => handleToggleEnabled(record.deviceId, checked)"
          />
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleView(record.deviceId)">
              查看
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="handleEdit(record.deviceId)"
              v-permission="['smart:device:update']"
            >
              编辑
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu>
                  <a-menu-item
                    v-if="record.status === 1"
                    @click="handleOffline(record.deviceId)"
                    v-permission="['smart:device:control']"
                  >
                    <DisconnectOutlined />
                    设备离线
                  </a-menu-item>
                  <a-menu-item
                    v-else
                    @click="handleOnline(record.deviceId)"
                    v-permission="['smart:device:control']"
                  >
                    <WifiOutlined />
                    设备上线
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item
                    @click="handleEnable(record.deviceId)"
                    v-if="record.isEnabled === 0"
                    v-permission="['smart:device:update']"
                  >
                    <CheckCircleOutlined />
                    启用设备
                  </a-menu-item>
                  <a-menu-item
                    @click="handleDisable(record.deviceId)"
                    v-if="record.isEnabled === 1"
                    v-permission="['smart:device:update']"
                  >
                    <StopOutlined />
                    禁用设备
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item
                    @click="handleDelete(record.deviceId)"
                    v-permission="['smart:device:delete']"
                    class="menu-item-danger"
                  >
                    <DeleteOutlined />
                    删除设备
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button type="link" size="small">
                更多 <DownOutlined />
              </a-button>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <DeviceModal
      v-model:visible="modalVisible"
      :device="currentDevice"
      :is-edit="isEdit"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  ExportOutlined,
  DownOutlined,
  DesktopOutlined,
  WifiOutlined,
  DisconnectOutlined,
  ExclamationCircleOutlined,
  CheckCircleOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import { deviceApi } from '/@/api/common/device/deviceApi'
import DeviceStatusTag from '/@/components/common/device/DeviceStatusTag.vue'
import DeviceTypeSelect from '/@/components/common/device/DeviceTypeSelect.vue'
import DeviceModal from './DeviceModal.vue'
import { usePermission } from '/@/hooks/usePermission'
import { useRouter } from 'vue-router'

// 权限检查
const { hasPermission } = usePermission()

// 路由
const router = useRouter()

// 响应式数据
const loading = ref(false)
const refreshLoading = ref(false)
const exportLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const currentDevice = ref(null)
const selectedRowKeys = ref([])

// 搜索表单
const searchForm = reactive({
  deviceCode: '',
  deviceName: '',
  deviceType: '',
  status: undefined,
  ipAddress: '',
  current: 1,
  pageSize: 10
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
})

// 表格数据
const tableData = ref([])

// 统计数据
const statistics = reactive({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  faultDevices: 0
})

// 计算属性
const hasSelected = computed(() => selectedRowKeys.value.length > 0)

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'deviceId',
    key: 'deviceId',
    width: 80,
    fixed: 'left'
  },
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 120
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150
  },
  {
    title: '设备类型',
    dataIndex: 'deviceTypeName',
    key: 'deviceType',
    width: 120
  },
  {
    title: '设备品牌',
    dataIndex: 'deviceBrand',
    key: 'deviceBrand',
    width: 100
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 130
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' },
    width: 80
  },
  {
    title: '启用',
    dataIndex: 'isEnabled',
    key: 'isEnabled',
    slots: { customRender: 'isEnabled' },
    width: 80
  },
  {
    title: '所属区域',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 120
  },
  {
    title: '联系人',
    dataIndex: 'contactPerson',
    key: 'contactPerson',
    width: 100
  },
  {
    title: '联系电话',
    dataIndex: 'contactPhone',
    key: 'contactPhone',
    width: 120
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: 180,
    fixed: 'right'
  }
]

// 行选择配置
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  }
}

// 生命周期
onMounted(() => {
  loadData()
  loadStatistics()
})

// 方法
const loadData = async () => {
  try {
    loading.value = true
    const response = await deviceApi.queryPage(searchForm)
    tableData.value = response.data.records
    pagination.total = response.data.total
    pagination.current = response.data.current
    pagination.pageSize = response.data.size
  } catch (error) {
    console.error('加载设备数据失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await deviceApi.getStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  searchForm.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    deviceCode: '',
    deviceName: '',
    deviceType: '',
    status: undefined,
    ipAddress: '',
    current: 1,
    pageSize: 10
  })
  loadData()
}

const handleAdd = () => {
  currentDevice.value = null
  isEdit.value = false
  modalVisible.value = true
}

const handleEdit = (deviceId) => {
  currentDevice.value = { deviceId }
  isEdit.value = true
  modalVisible.value = true
}

const handleView = (deviceId) => {
  // 这里可以实现查看详情的逻辑
  console.log('查看设备详情:', deviceId)
}

const handleDelete = async (deviceId) => {
  try {
    await deviceApi.delete(deviceId)
    message.success('删除成功')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的数据')
    return
  }

  try {
    await deviceApi.batchDelete(selectedRowKeys.value)
    message.success(`批量删除成功，共删除 ${selectedRowKeys.value.length} 个设备`)
    selectedRowKeys.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('批量删除失败')
  }
}

const handleToggleEnabled = async (deviceId, enabled) => {
  try {
    if (enabled) {
      await deviceApi.enable(deviceId)
      message.success('启用成功')
    } else {
      await deviceApi.disable(deviceId)
      message.success('禁用成功')
    }
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('操作失败')
  }
}

const handleEnable = async (deviceId) => {
  try {
    await deviceApi.enable(deviceId)
    message.success('启用成功')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('启用失败')
  }
}

const handleDisable = async (deviceId) => {
  try {
    await deviceApi.disable(deviceId)
    message.success('禁用成功')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('禁用失败')
  }
}

const handleOnline = async (deviceId) => {
  try {
    await deviceApi.online(deviceId)
    message.success('设备上线成功')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('设备上线失败')
  }
}

const handleOffline = async (deviceId) => {
  try {
    await deviceApi.offline(deviceId)
    message.success('设备离线成功')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('设备离线失败')
  }
}

const handleRefresh = async () => {
  try {
    refreshLoading.value = true
    await deviceApi.refreshStatus()
    message.success('设备状态刷新完成')
    loadData()
    loadStatistics()
  } catch (error) {
    message.error('刷新失败')
  } finally {
    refreshLoading.value = false
  }
}

const handleExport = async () => {
  try {
    exportLoading.value = true
    // 这里可以实现导出逻辑
    message.info('导出功能开发中...')
  } catch (error) {
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const handleTableChange = (pag) => {
  searchForm.current = pag.current
  searchForm.pageSize = pag.pageSize
  loadData()
}

const handleModalSuccess = () => {
  modalVisible.value = false
  loadData()
  loadStatistics()
}
</script>

<style lang="less" scoped>
.device-management {
  padding: 24px;

  .search-card {
    margin-bottom: 16px;
  }

  .statistics-row {
    margin-bottom: 16px;

    .stat-card {
      text-align: center;

      :deep(.ant-statistic) {
        .ant-statistic-content {
          padding: 16px;
        }
      }
    }
  }

  .table-card {
    .table-operations {
      margin-bottom: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}

.menu-item-danger {
  color: #ff4d4f;
}
</style>