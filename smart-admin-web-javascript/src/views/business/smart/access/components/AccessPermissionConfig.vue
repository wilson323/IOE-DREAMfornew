<template>
  <div class="access-permission-config">
    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <a-card title="门禁权限配置" size="small">
        <a-row :gutter="16">
          <a-col :span="5">
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索人员姓名或设备名称"
              @search="handleSearch"
              allow-clear
            />
          </a-col>
          <a-col :span="3">
            <a-select
              v-model:value="selectedPersonType"
              placeholder="人员类型"
              style="width: 100%"
              allow-clear
              @change="handlePersonTypeChange"
            >
              <a-select-option value="employee">员工</a-select-option>
              <a-select-option value="visitor">访客</a-select-option>
              <a-select-option value="contractor">承包商</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="3">
            <a-select
              v-model:value="selectedPermissionType"
              placeholder="权限类型"
              style="width: 100%"
              allow-clear
              @change="handlePermissionTypeChange"
            >
              <a-select-option value="permanent">永久权限</a-select-option>
              <a-select-option value="temporary">临时权限</a-select-option>
              <a-select-option value="scheduled">定时权限</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="3">
            <a-select
              v-model:value="selectedAccessStatus"
              placeholder="权限状态"
              style="width: 100%"
              allow-clear
              @change="handleAccessStatusChange"
            >
              <a-select-option :value="0">禁用</a-select-option>
              <a-select-option :value="1">启用</a-select-option>
              <a-select-option :value="2">待审批</a-select-option>
              <a-select-option :value="3">已过期</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              placeholder="生效日期范围"
              @change="handleDateRangeChange"
              style="width: 100%"
            />
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
              <a-button type="primary" @click="showBatchGrantModal">
                <template #icon><UserAddOutlined /></template>
                批量授权
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

    <!-- 门禁权限列表 -->
    <div class="access-permission-list">
      <a-table
        :columns="columns"
        :data-source="accessList"
        :loading="loading"
        :pagination="pagination"
        row-key="permissionId"
        @change="handleTableChange"
        :scroll="{ x: 1800 }"
        :row-selection="rowSelection"
      >
        <!-- 人员和设备信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'permissionInfo'">
            <div class="permission-info">
              <div class="personnel-section">
                <div class="personnel-header">
                  <div class="personnel-name">{{ record.personName }}</div>
                  <a-tag :color="getPersonTypeColor(record.personType)">
                    {{ getPersonTypeText(record.personType) }}
                  </a-tag>
                  <a-tag :color="getStatusColor(record.status)">
                    {{ getStatusText(record.status) }}
                  </a-tag>
                </div>
              </div>
              <div class="device-section">
                <div class="device-name">设备：{{ record.deviceName }}</div>
                <div class="area-name">区域：{{ record.areaName }}</div>
              </div>
              <div class="permission-detail">
                <div class="permission-type">{{ getPermissionTypeText(record.permissionType) }}</div>
                <div class="valid-period">
                  有效期：{{ formatDate(record.startTime) }} 至 {{ formatDate(record.endTime) }}
                </div>
              </div>
            </div>
          </template>

          <!-- 周通行权限 -->
          <template v-else-if="column.key === 'weekAccess'">
            <div class="week-access">
              <div class="week-row">
                <a-switch
                  :checked="record.mondayAccess === 1"
                  size="small"
                  @change="(checked) => handleWeekAccessChange(record, 'mondayAccess', checked)"
                />
                <span class="day-label">周一</span>
              </div>
              <div class="week-row">
                <a-switch
                  :checked="record.tuesdayAccess === 1"
                  size="small"
                  @change="(checked) => handleWeekAccessChange(record, 'tuesdayAccess', checked)"
                />
                <span class="day-label">周二</span>
              </div>
              <div class="week-row">
                <a-switch
                  :checked="record.wednesdayAccess === 1"
                  size="small"
                  @change="(checked) => handleWeekAccessChange(record, 'wednesdayAccess', checked)"
                />
                <span class="day-label">周三</span>
              </div>
              <div class="week-row">
                <a-switch
                  :checked="record.thursdayAccess === 1"
                  size="small"
                  @change="(checked) => handleWeekAccessChange(record, 'thursdayAccess', checked)"
                />
                <span class="day-label">周四</span>
              </div>
              <div class="week-row">
                <a-switch
                  :checked="record.fridayAccess === 1"
                  size="small"
                  @change="(checked) => handleWeekAccessChange(record, 'fridayAccess', checked)"
                />
                <span class="day-label">周五</span>
              </div>
              <div class="week-row">
                <a-switch
                  :checked="record.saturdayAccess === 1"
                  size="small"
                  @change="(checked) => handleWeekAccessChange(record, 'saturdayAccess', checked)"
                />
                <span class="day-label">周六</span>
              </div>
              <div class="week-row">
                <a-switch
                  :checked="record.sundayAccess === 1"
                  size="small"
                  @change="(checked) => handleWeekAccessChange(record, 'sundayAccess', checked)"
                />
                <span class="day-label">周日</span>
              </div>
            </div>
          </template>

          <!-- 时间段配置 -->
          <template v-else-if="column.key === 'timeConfig'">
            <div class="time-config">
              <div class="time-range" v-if="record.timeConfig">
                <a-tag color="blue" v-for="(time, index) in parseTimeConfig(record.timeConfig)" :key="index">
                  {{ time }}
                </a-tag>
              </div>
              <a-button
                type="dashed"
                size="small"
                @click="showTimeConfigModal(record)"
              >
                <template #icon><ClockCircleOutlined /></template>
                配置时间
              </a-button>
            </div>
          </template>

          <!-- 访问记录 -->
          <template v-else-if="column.key === 'accessRecords'">
            <div class="access-records">
              <div class="record-stats">
                <div class="stat-item">
                  <span class="stat-label">今日:</span>
                  <span class="stat-value">{{ record.todayAccessCount || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">本周:</span>
                  <span class="stat-value">{{ record.weekAccessCount || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">本月:</span>
                  <span class="stat-value">{{ record.monthAccessCount || 0 }}</span>
                </div>
              </div>
              <a-button
                type="link"
                size="small"
                @click="showAccessHistoryModal(record)"
              >
                查看详情
              </a-button>
            </div>
          </template>

          <!-- 审批信息 -->
          <template v-else-if="column.key === 'approvalInfo'">
            <div class="approval-info">
              <div class="approval-status">
                <a-tag :color="getApprovalStatusColor(record.approveUserId)">
                  {{ record.approveUserId ? '已审批' : '待审批' }}
                </a-tag>
              </div>
              <div class="approval-user" v-if="record.approveUserId">
                审批人：{{ record.approveUserName || '-' }}
              </div>
              <div class="approval-time" v-if="record.approveTime">
                {{ formatDateTime(record.approveTime) }}
              </div>
              <div class="approval-remark" v-if="record.approveRemark">
                {{ record.approveRemark }}
              </div>
            </div>
          </template>

          <!-- 最后更新 -->
          <template v-else-if="column.key === 'updateTime'">
            <div class="update-info">
              <div>{{ formatDateTime(record.updateTime) }}</div>
              <div class="update-user">{{ record.updateUserName || '-' }}</div>
            </div>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewPermissionDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="editAccessPermission(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="showAccessHistoryModal(record)">
                      <HistoryOutlined />
                      访问记录
                    </a-menu-item>
                    <a-menu-item @click="showPersonnelAccessModal(record)">
                      <UserOutlined />
                      人员权限
                    </a-menu-item>
                    <a-menu-item @click="approvePermission(record)" v-if="record.status === 2">
                      <CheckCircleOutlined />
                      审批权限
                    </a-menu-item>
                    <a-menu-item @click="revokePermission(record)" v-if="record.status === 1">
                      <StopOutlined />
                      撤销权限
                    </a-menu-item>
                    <a-menu-item @click="extendPermission(record)">
                      <FieldTimeOutlined />
                      延长权限
                    </a-menu-item>
                    <a-menu-item @click="syncAccessPermission(record)">
                      <SyncOutlined />
                      同步权限
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="deletePermission(record)" class="danger-menu-item">
                      <DeleteOutlined />
                      删除权限
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

    <!-- 批量操作工具栏 -->
    <div class="batch-toolbar" v-if="selectedRowKeys.length > 0">
      <a-card size="small">
        <a-space>
          <span>已选择 {{ selectedRowKeys.length }} 项</span>
          <a-button type="primary" @click="batchApprove">批量审批</a-button>
          <a-button @click="batchRevoke">批量撤销</a-button>
          <a-button @click="batchExtend">批量延期</a-button>
          <a-button danger @click="batchDelete">批量删除</a-button>
        </a-space>
      </a-card>
    </div>

    <!-- 权限详情模态框 -->
    <AccessPermissionDetailModal
      v-model:visible="detailModalVisible"
      :permission="selectedPermission"
      @close="handleDetailModalClose"
    />

    <!-- 编辑权限模态框 -->
    <EditAccessPermissionModal
      v-model:visible="editModalVisible"
      :permission="selectedPermission"
      @success="handleEditSuccess"
      @close="handleEditModalClose"
    />

    <!-- 时间配置模态框 -->
    <TimeConfigModal
      v-model:visible="timeConfigModalVisible"
      :permission="selectedPermission"
      @success="handleTimeConfigSuccess"
      @close="handleTimeConfigModalClose"
    />

    <!-- 访问历史模态框 -->
    <AccessHistoryModal
      v-model:visible="historyModalVisible"
      :permission="selectedPermission"
      @close="handleHistoryModalClose"
    />

    <!-- 人员权限模态框 -->
    <PersonnelAccessModal
      v-model:visible="personnelModalVisible"
      :personnel="selectedPersonnel"
      @close="handlePersonnelModalClose"
    />

    <!-- 批量授权模态框 -->
    <BatchGrantAccessModal
      v-model:visible="batchGrantModalVisible"
      @success="handleBatchGrantSuccess"
      @close="handleBatchGrantModalClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined,
  ExportOutlined,
  EyeOutlined,
  EditOutlined,
  UserAddOutlined,
  HistoryOutlined,
  UserOutlined,
  CheckCircleOutlined,
  StopOutlined,
  FieldTimeOutlined,
  SyncOutlined,
  DeleteOutlined,
  DownOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'

import { businessPermissionApi } from '@/api/smart-permission'

// 导入子组件
import AccessPermissionDetailModal from './modals/AccessPermissionDetailModal.vue'
import EditAccessPermissionModal from './modals/EditAccessPermissionModal.vue'
import TimeConfigModal from './modals/TimeConfigModal.vue'
import AccessHistoryModal from './modals/AccessHistoryModal.vue'
import PersonnelAccessModal from './modals/PersonnelAccessModal.vue'
import BatchGrantAccessModal from './modals/BatchGrantAccessModal.vue'

// 响应式数据
const searchText = ref('')
const selectedPersonType = ref(null)
const selectedPermissionType = ref(null)
const selectedAccessStatus = ref(null)
const dateRange = ref(null)
const loading = ref(false)
const accessList = ref([])
const selectedPermission = ref(null)
const selectedPersonnel = ref(null)
const selectedRowKeys = ref([])

// 模态框状态
const detailModalVisible = ref(false)
const editModalVisible = ref(false)
const timeConfigModalVisible = ref(false)
const historyModalVisible = ref(false)
const personnelModalVisible = ref(false)
const batchGrantModalVisible = ref(false)

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
    title: '权限信息',
    key: 'permissionInfo',
    width: 300,
    fixed: 'left'
  },
  {
    title: '周通行权限',
    key: 'weekAccess',
    width: 160
  },
  {
    title: '时间段配置',
    key: 'timeConfig',
    width: 200
  },
  {
    title: '访问记录',
    key: 'accessRecords',
    width: 150
  },
  {
    title: '审批信息',
    key: 'approvalInfo',
    width: 200
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

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  }
}))

// 方法定义
const handleSearch = () => {
  pagination.current = 1
  fetchAccessList()
}

const handleReset = () => {
  searchText.value = ''
  selectedPersonType.value = null
  selectedPermissionType.value = null
  selectedAccessStatus.value = null
  dateRange.value = null
  selectedRowKeys.value = []
  pagination.current = 1
  fetchAccessList()
}

const handlePersonTypeChange = () => {
  pagination.current = 1
  fetchAccessList()
}

const handlePermissionTypeChange = () => {
  pagination.current = 1
  fetchAccessList()
}

const handleAccessStatusChange = () => {
  pagination.current = 1
  fetchAccessList()
}

const handleDateRangeChange = () => {
  pagination.current = 1
  fetchAccessList()
}

const handleTableChange = (paginationInfo) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  fetchAccessList()
}

const handleWeekAccessChange = async (record, day, checked) => {
  try {
    const requestData = {
      permissionId: record.permissionId,
      weekAccess: {
        [day]: checked ? 1 : 0
      }
    }

    await businessPermissionApi.updateAccessPermissionConfig(requestData)
    record[day] = checked ? 1 : 0

    message.success(`${getDayText(day)}通行权限${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('周通行权限配置失败')
    console.error('周通行权限配置失败:', error)
  }
}

const viewPermissionDetail = (permission) => {
  selectedPermission.value = permission
  detailModalVisible.value = true
}

const editAccessPermission = (permission) => {
  selectedPermission.value = permission
  editModalVisible.value = true
}

const showTimeConfigModal = (permission) => {
  selectedPermission.value = permission
  timeConfigModalVisible.value = true
}

const showAccessHistoryModal = (permission) => {
  selectedPermission.value = permission
  historyModalVisible.value = true
}

const showPersonnelAccessModal = (permission) => {
  selectedPersonnel.value = {
    personId: permission.personId,
    personName: permission.personName,
    personType: permission.personType
  }
  personnelModalVisible.value = true
}

const approvePermission = async (permission) => {
  Modal.confirm({
    title: '确认审批',
    content: `确定要审批 ${permission.personName} 的门禁权限吗？`,
    onOk: async () => {
      try {
        await businessPermissionApi.approveAccessPermission(permission.permissionId)
        message.success('权限审批成功')
        fetchAccessList()
      } catch (error) {
        message.error('权限审批失败')
        console.error('权限审批失败:', error)
      }
    }
  })
}

const revokePermission = async (permission) => {
  Modal.confirm({
    title: '确认撤销',
    content: `确定要撤销 ${permission.personName} 的门禁权限吗？`,
    onOk: async () => {
      try {
        await businessPermissionApi.revokeAccessPermission(permission.permissionId)
        message.success('权限撤销成功')
        fetchAccessList()
      } catch (error) {
        message.error('权限撤销失败')
        console.error('权限撤销失败:', error)
      }
    }
  })
}

const extendPermission = (permission) => {
  selectedPermission.value = permission
  // 这里可以打开延长时间模态框
  message.info('延长时间功能开发中...')
}

const syncAccessPermission = async (permission) => {
  try {
    await businessPermissionApi.syncAccessPermission(permission.permissionId)
    message.success('门禁权限同步成功')
    fetchAccessList()
  } catch (error) {
    message.error('门禁权限同步失败')
    console.error('门禁权限同步失败:', error)
  }
}

const deletePermission = async (permission) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除 ${permission.personName} 的门禁权限吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    onOk: async () => {
      try {
        await businessPermissionApi.deleteAccessPermission(permission.permissionId)
        message.success('权限删除成功')
        fetchAccessList()
      } catch (error) {
        message.error('权限删除失败')
        console.error('权限删除失败:', error)
      }
    }
  })
}

const batchApprove = async () => {
  Modal.confirm({
    title: '批量审批',
    content: `确定要审批选中的 ${selectedRowKeys.value.length} 个权限吗？`,
    onOk: async () => {
      try {
        await businessPermissionApi.batchApproveAccessPermission(selectedRowKeys.value)
        message.success('批量审批成功')
        selectedRowKeys.value = []
        fetchAccessList()
      } catch (error) {
        message.error('批量审批失败')
        console.error('批量审批失败:', error)
      }
    }
  })
}

const batchRevoke = async () => {
  Modal.confirm({
    title: '批量撤销',
    content: `确定要撤销选中的 ${selectedRowKeys.value.length} 个权限吗？`,
    onOk: async () => {
      try {
        await businessPermissionApi.batchRevokeAccessPermission(selectedRowKeys.value)
        message.success('批量撤销成功')
        selectedRowKeys.value = []
        fetchAccessList()
      } catch (error) {
        message.error('批量撤销失败')
        console.error('批量撤销失败:', error)
      }
    }
  })
}

const batchExtend = () => {
  message.info('批量延期功能开发中...')
}

const batchDelete = async () => {
  Modal.confirm({
    title: '批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个权限吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    onOk: async () => {
      try {
        await businessPermissionApi.batchDeleteAccessPermission(selectedRowKeys.value)
        message.success('批量删除成功')
        selectedRowKeys.value = []
        fetchAccessList()
      } catch (error) {
        message.error('批量删除失败')
        console.error('批量删除失败:', error)
      }
    }
  })
}

const showBatchGrantModal = () => {
  batchGrantModalVisible.value = true
}

const exportConfig = async () => {
  try {
    const response = await businessPermissionApi.exportAccessPermissionConfig({
      searchText: searchText.value,
      personType: selectedPersonType.value,
      permissionType: selectedPermissionType.value,
      status: selectedAccessStatus.value,
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD')
    })

    // 处理文件下载
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `access-permission-config-${new Date().toISOString().split('T')[0]}.xlsx`
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
  selectedPermission.value = null
}

const handleEditModalClose = () => {
  editModalVisible.value = false
  selectedPermission.value = null
}

const handleTimeConfigModalClose = () => {
  timeConfigModalVisible.value = false
  selectedPermission.value = null
}

const handleHistoryModalClose = () => {
  historyModalVisible.value = false
  selectedPermission.value = null
}

const handlePersonnelModalClose = () => {
  personnelModalVisible.value = false
  selectedPersonnel.value = null
}

const handleBatchGrantModalClose = () => {
  batchGrantModalVisible.value = false
}

const handleEditSuccess = () => {
  editModalVisible.value = false
  fetchAccessList()
  message.success('门禁权限编辑成功')
}

const handleTimeConfigSuccess = () => {
  timeConfigModalVisible.value = false
  fetchAccessList()
  message.success('时间段配置成功')
}

const handleBatchGrantSuccess = () => {
  batchGrantModalVisible.value = false
  fetchAccessList()
  message.success('批量授权成功')
}

const fetchAccessList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      searchText: searchText.value,
      personType: selectedPersonType.value,
      permissionType: selectedPermissionType.value,
      status: selectedAccessStatus.value,
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD')
    }

    const response = await businessPermissionApi.getAccessPermissionConfig(params)

    if (response.data) {
      accessList.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    message.error('获取门禁权限配置失败')
    console.error('获取门禁权限配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 工具函数
const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const parseTimeConfig = (timeConfig) => {
  if (!timeConfig) return []
  try {
    return JSON.parse(timeConfig)
  } catch {
    return [timeConfig]
  }
}

const getPersonTypeColor = (type) => {
  const colors = {
    'employee': 'blue',
    'visitor': 'green',
    'contractor': 'orange'
  }
  return colors[type] || 'default'
}

const getPersonTypeText = (type) => {
  const textMap = {
    'employee': '员工',
    'visitor': '访客',
    'contractor': '承包商'
  }
  return textMap[type] || type
}

const getStatusColor = (status) => {
  const colors = {
    0: 'red',     // 禁用
    1: 'green',   // 启用
    2: 'orange',  // 待审批
    3: 'gray'     // 已过期
  }
  return colors[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    0: '禁用',
    1: '启用',
    2: '待审批',
    3: '已过期'
  }
  return textMap[status] || '未知'
}

const getPermissionTypeText = (type) => {
  const textMap = {
    'permanent': '永久权限',
    'temporary': '临时权限',
    'scheduled': '定时权限'
  }
  return textMap[type] || type
}

const getApprovalStatusColor = (approveUserId) => {
  return approveUserId ? 'green' : 'orange'
}

const getDayText = (day) => {
  const dayMap = {
    'mondayAccess': '周一',
    'tuesdayAccess': '周二',
    'wednesdayAccess': '周三',
    'thursdayAccess': '周四',
    'fridayAccess': '周五',
    'saturdayAccess': '周六',
    'sundayAccess': '周日'
  }
  return dayMap[day] || day
}

// 生命周期
onMounted(() => {
  fetchAccessList()
})
</script>

<style lang="less" scoped>
.access-permission-config {
  .search-bar {
    margin-bottom: 16px;
  }

  .access-permission-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .permission-info {
      .personnel-section {
        .personnel-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .personnel-name {
            font-weight: 500;
            font-size: 14px;
            color: #1890ff;
          }
        }
      }

      .device-section {
        margin-bottom: 4px;

        .device-name, .area-name {
          font-size: 12px;
          color: #595959;
          margin-bottom: 2px;
        }
      }

      .permission-detail {
        .permission-type {
          font-size: 12px;
          color: #52c41a;
          margin-bottom: 4px;
        }

        .valid-period {
          font-size: 11px;
          color: #8c8c8c;
        }
      }
    }

    .week-access {
      .week-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;

        .day-label {
          font-size: 12px;
          color: #595959;
          width: 30px;
        }
      }
    }

    .time-config {
      .time-range {
        margin-bottom: 8px;

        .ant-tag {
          margin-right: 4px;
          margin-bottom: 4px;
        }
      }
    }

    .access-records {
      .record-stats {
        margin-bottom: 8px;

        .stat-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 4px;

          .stat-label {
            font-size: 12px;
            color: #595959;
          }

          .stat-value {
            font-size: 12px;
            font-weight: 500;
            color: #1890ff;
          }
        }
      }
    }

    .approval-info {
      .approval-status {
        margin-bottom: 8px;
      }

      .approval-user, .approval-time, .approval-remark {
        font-size: 12px;
        color: #595959;
        margin-bottom: 4px;
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

  .batch-toolbar {
    margin-top: 16px;
  }
}

.danger-menu-item {
  color: #f5222d;
}
</style>