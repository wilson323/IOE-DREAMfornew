<template>
  <div class="attendance-permission-config">
    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <a-card title="考勤权限配置" size="small">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索人员姓名或工号"
              @search="handleSearch"
              allow-clear
            />
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedDepartment"
              placeholder="选择部门"
              style="width: 100%"
              allow-clear
              @change="handleDepartmentChange"
            >
              <a-select-option value="admin">行政部</a-select-option>
              <a-select-option value="hr">人力资源部</a-select-option>
              <a-select-option value="finance">财务部</a-select-option>
              <a-select-option value="tech">技术部</a-select-option>
              <a-select-option value="operation">运营部</a-select-option>
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
              <a-select-option value="manage">管理权限</a-select-option>
              <a-select-option value="export">导出权限</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              placeholder="考勤日期范围"
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

    <!-- 考勤权限列表 -->
    <div class="attendance-permission-list">
      <a-table
        :columns="columns"
        :data-source="attendanceList"
        :loading="loading"
        :pagination="pagination"
        row-key="attendanceId"
        @change="handleTableChange"
        :scroll="{ x: 1500 }"
      >
        <!-- 人员信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'personnelInfo'">
            <div class="personnel-info">
              <div class="personnel-header">
                <div class="personnel-name">{{ record.personnelName }}</div>
                <div class="personnel-id">{{ record.personnelId }}</div>
                <a-tag :color="getDepartmentColor(record.department)">
                  {{ getDepartmentText(record.department) }}
                </a-tag>
              </div>
              <div class="personnel-position">{{ record.position }}</div>
              <div class="personnel-contact">{{ record.contactPhone }}</div>
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
                <span class="permission-label">管理:</span>
                <a-switch
                  :checked="record.hasManagePermission"
                  :loading="record.manageLoading"
                  @change="(checked) => handlePermissionChange(record, 'MANAGE', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">导出:</span>
                <a-switch
                  :checked="record.hasExportPermission"
                  :loading="record.exportLoading"
                  @change="(checked) => handlePermissionChange(record, 'EXPORT', checked)"
                />
              </div>
            </div>
          </template>

          <!-- 数据权限范围 -->
          <template v-else-if="column.key === 'dataScope'">
            <div class="data-scope">
              <a-select
                v-model:value="record.dataScope"
                style="width: 100%"
                size="small"
                @change="(value) => handleDataScopeChange(record, value)"
              >
                <a-select-option value="self">仅自己</a-select-option>
                <a-select-option value="department">本部门</a-select-option>
                <a-select-option value="sub_department">下属部门</a-select-option>
                <a-select-option value="all">全部</a-select-option>
              </a-select>
            </div>
          </template>

          <!-- 时间权限 -->
          <template v-else-if="column.key === 'timePermission'">
            <div class="time-permission">
              <div class="permission-item">
                <span class="permission-label">历史:</span>
                <a-switch
                  :checked="record.timePermission.history"
                  @change="(checked) => handleTimePermissionChange(record, 'history', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">实时:</span>
                <a-switch
                  :checked="record.timePermission.realtime"
                  @change="(checked) => handleTimePermissionChange(record, 'realtime', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">未来:</span>
                <a-switch
                  :checked="record.timePermission.future"
                  @change="(checked) => handleTimePermissionChange(record, 'future', checked)"
                />
              </div>
            </div>
          </template>

          <!-- 审批权限 -->
          <template v-else-if="column.key === 'approvalPermission'">
            <div class="approval-permission">
              <div class="permission-item">
                <span class="permission-label">请假:</span>
                <a-switch
                  :checked="record.approvalPermission.leave"
                  @change="(checked) => handleApprovalPermissionChange(record, 'leave', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">加班:</span>
                <a-switch
                  :checked="record.approvalPermission.overtime"
                  @change="(checked) => handleApprovalPermissionChange(record, 'overtime', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">补卡:</span>
                <a-switch
                  :checked="record.approvalPermission.repair"
                  @change="(checked) => handleApprovalPermissionChange(record, 'repair', checked)"
                />
              </div>
            </div>
          </template>

          <!-- 统计权限 -->
          <template v-else-if="column.key === 'statisticsPermission'">
            <div class="statistics-permission">
              <div class="permission-item">
                <span class="permission-label">日报:</span>
                <a-switch
                  :checked="record.statisticsPermission.daily"
                  @change="(checked) => handleStatisticsPermissionChange(record, 'daily', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">周报:</span>
                <a-switch
                  :checked="record.statisticsPermission.weekly"
                  @change="(checked) => handleStatisticsPermissionChange(record, 'weekly', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">月报:</span>
                <a-switch
                  :checked="record.statisticsPermission.monthly"
                  @change="(checked) => handleStatisticsPermissionChange(record, 'monthly', checked)"
                />
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
              <a-button type="link" size="small" @click="viewPersonnelDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="editAttendancePermission(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="showAttendanceHistoryModal(record)">
                      <CalendarOutlined />
                      考勤历史
                    </a-menu-item>
                    <a-menu-item @click="showAttendanceStatisticsModal(record)">
                      <BarChartOutlined />
                      统计分析
                    </a-menu-item>
                    <a-menu-item @click="syncAttendancePermission(record)">
                      <SyncOutlined />
                      同步权限
                    </a-menu-item>
                    <a-menu-item @click="exportAttendanceReport(record)">
                      <FileExcelOutlined />
                      导出报告
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="resetAttendancePermission(record)" class="danger-menu-item">
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

    <!-- 人员详情模态框 -->
    <PersonnelDetailModal
      v-model:visible="detailModalVisible"
      :personnel="selectedPersonnel"
      @close="handleDetailModalClose"
    />

    <!-- 编辑权限模态框 -->
    <EditAttendancePermissionModal
      v-model:visible="editModalVisible"
      :personnel="selectedPersonnel"
      @success="handleEditSuccess"
      @close="handleEditModalClose"
    />

    <!-- 考勤历史模态框 -->
    <AttendanceHistoryModal
      v-model:visible="historyModalVisible"
      :personnel="selectedPersonnel"
      @close="handleHistoryModalClose"
    />

    <!-- 统计分析模态框 -->
    <AttendanceStatisticsModal
      v-model:visible="statisticsModalVisible"
      :personnel="selectedPersonnel"
      @close="handleStatisticsModalClose"
    />

    <!-- 批量配置模态框 -->
    <BatchAttendanceConfigModal
      v-model:visible="batchConfigModalVisible"
      @success="handleBatchConfigSuccess"
      @close="handleBatchConfigModalClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined,
  SettingOutlined,
  ExportOutlined,
  EyeOutlined,
  EditOutlined,
  CalendarOutlined,
  BarChartOutlined,
  SyncOutlined,
  FileExcelOutlined,
  RollbackOutlined,
  DownOutlined
} from '@ant-design/icons-vue'

import { businessPermissionApi } from '@/api/smart-permission'

// 导入子组件
import PersonnelDetailModal from './modals/PersonnelDetailModal.vue'
import EditAttendancePermissionModal from './modals/EditAttendancePermissionModal.vue'
import AttendanceHistoryModal from './modals/AttendanceHistoryModal.vue'
import AttendanceStatisticsModal from './modals/AttendanceStatisticsModal.vue'
import BatchAttendanceConfigModal from './modals/BatchAttendanceConfigModal.vue'

// 响应式数据
const searchText = ref('')
const selectedDepartment = ref(null)
const selectedPermissionType = ref(null)
const dateRange = ref(null)
const loading = ref(false)
const attendanceList = ref([])
const selectedPersonnel = ref(null)

// 模态框状态
const detailModalVisible = ref(false)
const editModalVisible = ref(false)
const historyModalVisible = ref(false)
const statisticsModalVisible = ref(false)
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
    title: '人员信息',
    key: 'personnelInfo',
    width: 250,
    fixed: 'left'
  },
  {
    title: '权限配置',
    key: 'permissions',
    width: 180
  },
  {
    title: '数据权限范围',
    key: 'dataScope',
    width: 120
  },
  {
    title: '时间权限',
    key: 'timePermission',
    width: 160
  },
  {
    title: '审批权限',
    key: 'approvalPermission',
    width: 160
  },
  {
    title: '统计权限',
    key: 'statisticsPermission',
    width: 160
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
  fetchAttendanceList()
}

const handleReset = () => {
  searchText.value = ''
  selectedDepartment.value = null
  selectedPermissionType.value = null
  dateRange.value = null
  pagination.current = 1
  fetchAttendanceList()
}

const handleDepartmentChange = () => {
  pagination.current = 1
  fetchAttendanceList()
}

const handlePermissionTypeChange = () => {
  pagination.current = 1
  fetchAttendanceList()
}

const handleDateRangeChange = () => {
  pagination.current = 1
  fetchAttendanceList()
}

const handleTableChange = (paginationInfo) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  fetchAttendanceList()
}

const handlePermissionChange = async (personnel, permissionType, checked) => {
  const loadingKey = `${permissionType.toLowerCase()}Loading`
  personnel[loadingKey] = true

  try {
    const requestData = {
      personnelId: personnel.personnelId,
      permissionType,
      enabled: checked
    }

    // 调用API更新权限
    await businessPermissionApi.updateAttendancePermissionConfig(requestData)

    // 更新本地状态
    switch (permissionType) {
      case 'VIEW':
        personnel.hasViewPermission = checked
        break
      case 'MANAGE':
        personnel.hasManagePermission = checked
        break
      case 'EXPORT':
        personnel.hasExportPermission = checked
        break
    }

    message.success(`${getPermissionText(permissionType)}权限${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('权限配置更新失败')
    console.error('权限配置更新失败:', error)
  } finally {
    personnel[loadingKey] = false
  }
}

const handleDataScopeChange = async (personnel, value) => {
  try {
    const requestData = {
      personnelId: personnel.personnelId,
      dataScope: value
    }

    await businessPermissionApi.updateAttendancePermissionConfig(requestData)
    personnel.dataScope = value

    message.success('数据权限范围更新成功')
  } catch (error) {
    message.error('数据权限范围更新失败')
    console.error('数据权限范围更新失败:', error)
  }
}

const handleTimePermissionChange = async (personnel, permissionType, checked) => {
  try {
    const requestData = {
      personnelId: personnel.personnelId,
      timePermission: {
        ...personnel.timePermission,
        [permissionType]: checked
      }
    }

    await businessPermissionApi.updateAttendancePermissionConfig(requestData)
    personnel.timePermission[permissionType] = checked

    message.success(`时间权限${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('时间权限配置失败')
    console.error('时间权限配置失败:', error)
  }
}

const handleApprovalPermissionChange = async (personnel, permissionType, checked) => {
  try {
    const requestData = {
      personnelId: personnel.personnelId,
      approvalPermission: {
        ...personnel.approvalPermission,
        [permissionType]: checked
      }
    }

    await businessPermissionApi.updateAttendancePermissionConfig(requestData)
    personnel.approvalPermission[permissionType] = checked

    message.success(`审批权限${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('审批权限配置失败')
    console.error('审批权限配置失败:', error)
  }
}

const handleStatisticsPermissionChange = async (personnel, permissionType, checked) => {
  try {
    const requestData = {
      personnelId: personnel.personnelId,
      statisticsPermission: {
        ...personnel.statisticsPermission,
        [permissionType]: checked
      }
    }

    await businessPermissionApi.updateAttendancePermissionConfig(requestData)
    personnel.statisticsPermission[permissionType] = checked

    message.success(`统计权限${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('统计权限配置失败')
    console.error('统计权限配置失败:', error)
  }
}

const viewPersonnelDetail = (personnel) => {
  selectedPersonnel.value = personnel
  detailModalVisible.value = true
}

const editAttendancePermission = (personnel) => {
  selectedPersonnel.value = personnel
  editModalVisible.value = true
}

const showAttendanceHistoryModal = (personnel) => {
  selectedPersonnel.value = personnel
  historyModalVisible.value = true
}

const showAttendanceStatisticsModal = (personnel) => {
  selectedPersonnel.value = personnel
  statisticsModalVisible.value = true
}

const syncAttendancePermission = async (personnel) => {
  try {
    await businessPermissionApi.syncAttendancePermission(personnel.personnelId)
    message.success('考勤权限同步成功')
    fetchAttendanceList()
  } catch (error) {
    message.error('考勤权限同步失败')
    console.error('考勤权限同步失败:', error)
  }
}

const exportAttendanceReport = async (personnel) => {
  try {
    const response = await businessPermissionApi.exportAttendanceReport({
      personnelId: personnel.personnelId,
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD')
    })

    // 处理文件下载
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `attendance-report-${personnel.personnelName}-${dayjs().format('YYYYMMDD')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('考勤报告导出成功')
  } catch (error) {
    message.error('考勤报告导出失败')
    console.error('考勤报告导出失败:', error)
  }
}

const resetAttendancePermission = async (personnel) => {
  try {
    // 重置权限配置到默认状态
    const requestData = {
      personnelId: personnel.personnelId,
      hasViewPermission: false,
      hasManagePermission: false,
      hasExportPermission: false,
      dataScope: 'self',
      timePermission: {
        history: false,
        realtime: true,
        future: false
      },
      approvalPermission: {
        leave: false,
        overtime: false,
        repair: false
      },
      statisticsPermission: {
        daily: false,
        weekly: false,
        monthly: false
      }
    }

    await businessPermissionApi.updateAttendancePermissionConfig(requestData)

    // 更新本地状态
    Object.assign(personnel, requestData)
    message.success('考勤权限重置成功')
  } catch (error) {
    message.error('考勤权限重置失败')
    console.error('考勤权限重置失败:', error)
  }
}

const showBatchConfigModal = () => {
  batchConfigModalVisible.value = true
}

const exportConfig = async () => {
  try {
    const response = await businessPermissionApi.exportAttendancePermissionConfig({
      searchText: searchText.value,
      department: selectedDepartment.value,
      permissionType: selectedPermissionType.value,
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD')
    })

    // 处理文件下载
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `attendance-permission-config-${new Date().toISOString().split('T')[0]}.xlsx`
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
  selectedPersonnel.value = null
}

const handleEditModalClose = () => {
  editModalVisible.value = false
  selectedPersonnel.value = null
}

const handleHistoryModalClose = () => {
  historyModalVisible.value = false
  selectedPersonnel.value = null
}

const handleStatisticsModalClose = () => {
  statisticsModalVisible.value = false
  selectedPersonnel.value = null
}

const handleBatchConfigModalClose = () => {
  batchConfigModalVisible.value = false
}

const handleEditSuccess = () => {
  editModalVisible.value = false
  fetchAttendanceList()
  message.success('考勤权限编辑成功')
}

const handleBatchConfigSuccess = () => {
  batchConfigModalVisible.value = false
  fetchAttendanceList()
  message.success('批量配置成功')
}

const fetchAttendanceList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      searchText: searchText.value,
      department: selectedDepartment.value,
      permissionType: selectedPermissionType.value,
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD')
    }

    const response = await businessPermissionApi.getAttendancePermissionConfig(params)

    if (response.data) {
      attendanceList.value = response.data.records.map(personnel => ({
        ...personnel,
        viewLoading: false,
        manageLoading: false,
        exportLoading: false,
        timePermission: personnel.timePermission || {
          history: false,
          realtime: true,
          future: false
        },
        approvalPermission: personnel.approvalPermission || {
          leave: false,
          overtime: false,
          repair: false
        },
        statisticsPermission: personnel.statisticsPermission || {
          daily: false,
          weekly: false,
          monthly: false
        }
      }))
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    message.error('获取考勤权限配置失败')
    console.error('获取考勤权限配置失败:', error)
  } finally {
    loading.value = false
  }
}

const getDepartmentColor = (department) => {
  const colors = {
    'admin': 'blue',
    'hr': 'green',
    'finance': 'orange',
    'tech': 'purple',
    'operation': 'red'
  }
  return colors[department] || 'default'
}

const getDepartmentText = (department) => {
  const textMap = {
    'admin': '行政部',
    'hr': '人力资源部',
    'finance': '财务部',
    'tech': '技术部',
    'operation': '运营部'
  }
  return textMap[department] || department
}

const getPermissionText = (permissionType) => {
  const textMap = {
    'VIEW': '查看',
    'MANAGE': '管理',
    'EXPORT': '导出'
  }
  return textMap[permissionType] || permissionType
}

// 生命周期
onMounted(() => {
  fetchAttendanceList()
})
</script>

<style lang="less" scoped>
.attendance-permission-config {
  .search-bar {
    margin-bottom: 16px;
  }

  .attendance-permission-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .personnel-info {
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

        .personnel-id {
          font-size: 12px;
          color: #8c8c8c;
        }
      }

      .personnel-position {
        font-weight: 500;
        font-size: 13px;
        color: #262626;
        margin-bottom: 4px;
      }

      .personnel-contact {
        font-size: 12px;
        color: #8c8c8c;
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

    .time-permission {
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

    .approval-permission {
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

    .statistics-permission {
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