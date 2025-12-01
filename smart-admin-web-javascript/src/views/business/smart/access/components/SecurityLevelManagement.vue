<template>
  <div class="security-level-management">
    <!-- 概览统计 -->
    <div class="overview-section" style="margin-bottom: 24px;">
      <a-row :gutter="16">
        <a-col :span="6" v-for="(level, index) in securityLevels" :key="index">
          <a-card
            :class="['level-card', `level-${level.value}`]"
            hoverable
            @click="selectLevel(level)"
          >
            <div class="level-header">
              <div class="level-icon">
                <component :is="level.icon" />
              </div>
              <div class="level-info">
                <div class="level-name">{{ level.name }}</div>
                <div class="level-desc">{{ level.description }}</div>
              </div>
            </div>
            <div class="level-stats">
              <a-row>
                <a-col :span="12">
                  <a-statistic
                    title="用户数"
                    :value="level.userCount"
                    :value-style="{ color: '#1890ff', fontSize: '18px' }"
                  />
                </a-col>
                <a-col :span="12">
                  <a-statistic
                    title="权限数"
                    :value="level.permissionCount"
                    :value-style="{ color: '#52c41a', fontSize: '18px' }"
                  />
                </a-col>
              </a-row>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="search-bar" style="margin-bottom: 16px;">
      <a-card size="small">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索用户姓名、工号或部门"
              @search="handleSearch"
              allow-clear
            />
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedLevel"
              placeholder="安全级别"
              style="width: 100%"
              allow-clear
              @change="handleLevelChange"
            >
              <a-select-option :value="1">1级 - 公开级</a-select-option>
              <a-select-option :value="2">2级 - 内部级</a-select-option>
              <a-select-option :value="3">3级 - 秘密级</a-select-option>
              <a-select-option :value="4">4级 - 机密级</a-select-option>
              <a-select-option :value="5">5级 - 绝密级</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedDepartment"
              placeholder="部门"
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
              v-model:value="selectedStatus"
              placeholder="状态"
              style="width: 100%"
              allow-clear
              @change="handleStatusChange"
            >
              <a-select-option value="active">生效</a-select-option>
              <a-select-option value="pending">待审批</a-select-option>
              <a-select-option value="expired">已过期</a-select-option>
              <a-select-option value="suspended">暂停</a-select-option>
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
              <a-button type="primary" @click="showLevelUpgradeModal">
                <template #icon><UpOutlined /></template>
                级别调整
              </a-button>
              <a-button @click="exportReport">
                <template #icon><ExportOutlined /></template>
                导出报告
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 用户安全级别列表 -->
    <div class="level-user-list">
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        row-key="userId"
        @change="handleTableChange"
        :scroll="{ x: 1400 }"
        :row-selection="rowSelection"
      >
        <!-- 用户信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userInfo'">
            <div class="user-info">
              <div class="user-header">
                <a-avatar :src="record.avatar" :size="40">
                  {{ record.userName.charAt(0) }}
                </a-avatar>
                <div class="user-basic">
                  <div class="user-name">{{ record.userName }}</div>
                  <div class="user-id">工号：{{ record.userId }}</div>
                  <div class="user-dept">{{ record.department }}</div>
                </div>
              </div>
              <div class="user-contact">
                <div class="contact-item">
                  <PhoneOutlined />
                  {{ record.phone }}
                </div>
                <div class="contact-item">
                  <MailOutlined />
                  {{ record.email }}
                </div>
              </div>
            </div>
          </template>

          <!-- 当前安全级别 -->
          <template v-else-if="column.key === 'currentLevel'">
            <div class="current-level">
              <a-progress
                :percent="record.securityLevel * 20"
                size="small"
                :stroke-color="getSecurityLevelColor(record.securityLevel)"
                :show-info="false"
              />
              <div class="level-info">
                <a-tag :color="getSecurityLevelColor(record.securityLevel)">
                  {{ getSecurityLevelText(record.securityLevel) }}
                </a-tag>
                <div class="level-score">评分：{{ record.securityScore }}/100</div>
                <div class="effective-time">生效时间：{{ formatDate(record.levelEffectiveTime) }}</div>
              </div>
            </div>
          </template>

          <!-- 权限统计 -->
          <template v-else-if="column.key === 'permissionStats'">
            <div class="permission-stats">
              <div class="stat-row">
                <span class="stat-label">区域权限：</span>
                <a-progress
                  :percent="getPermissionPercent(record.areaPermissionCount, record.totalAreaCount)"
                  size="small"
                  :show-info="false"
                  style="width: 60px"
                />
                <span class="stat-value">{{ record.areaPermissionCount }}/{{ record.totalAreaCount }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">设备权限：</span>
                <a-progress
                  :percent="getPermissionPercent(record.devicePermissionCount, record.totalDeviceCount)"
                  size="small"
                  :show-info="false"
                  style="width: 60px"
                />
                <span class="stat-value">{{ record.devicePermissionCount }}/{{ record.totalDeviceCount }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">考勤权限：</span>
                <a-progress
                  :percent="getPermissionPercent(record.attendancePermissionCount, record.totalAttendanceCount)"
                  size="small"
                  :show-info="false"
                  style="width: 60px"
                />
                <span class="stat-value">{{ record.attendancePermissionCount }}/{{ record.totalAttendanceCount }}</span>
              </div>
            </div>
          </template>

          <!-- 访问限制 -->
          <template v-else-if="column.key === 'accessRestriction'">
            <div class="access-restriction">
              <div class="restriction-item">
                <span class="restriction-label">时间限制：</span>
                <a-tag :color="record.timeRestrictionEnabled ? 'green' : 'red'">
                  {{ record.timeRestrictionEnabled ? '已启用' : '未启用' }}
                </a-tag>
              </div>
              <div class="restriction-item">
                <span class="restriction-label">IP限制：</span>
                <a-tag :color="record.ipRestrictionEnabled ? 'green' : 'red'">
                  {{ record.ipRestrictionEnabled ? '已启用' : '未启用' }}
                </a-tag>
              </div>
              <div class="restriction-item">
                <span class="restriction-label">地域限制：</span>
                <a-tag :color="record.areaRestrictionEnabled ? 'green' : 'red'">
                  {{ record.areaRestrictionEnabled ? '已启用' : '未启用' }}
                </a-tag>
              </div>
            </div>
          </template>

          <!-- 最近活动 -->
          <template v-else-if="column.key === 'recentActivity'">
            <div class="recent-activity">
              <div class="activity-item" v-if="record.lastLoginTime">
                <ClockCircleOutlined />
                最后登录：{{ formatDateTime(record.lastLoginTime) }}
              </div>
              <div class="activity-item" v-if="record.lastPermissionChange">
                <SwapOutlined />
                权限变更：{{ formatDateTime(record.lastPermissionChange) }}
              </div>
              <div class="activity-item" v-if="record.lastSecurityEvent">
                <WarningOutlined />
                安全事件：{{ formatDateTime(record.lastSecurityEvent) }}
              </div>
            </div>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewUserDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="editUserLevel(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="showUserPermissionModal(record)">
                      <SafetyOutlined />
                      权限查看
                    </a-menu-item>
                    <a-menu-item @click="showAccessHistoryModal(record)">
                      <HistoryOutlined />
                      访问历史
                    </a-menu-item>
                    <a-menu-item @click="upgradeSecurityLevel(record)">
                      <UpOutlined />
                      提升级别
                    </a-menu-item>
                    <a-menu-item @click="downgradeSecurityLevel(record)">
                      <DownOutlined />
                      降低级别
                    </a-menu-item>
                    <a-menu-item @click="suspendUserLevel(record)">
                      <StopOutlined />
                      暂停权限
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="exportUserReport(record)">
                      <DownloadOutlined />
                      导出报告
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
          <span>已选择 {{ selectedRowKeys.length }} 个用户</span>
          <a-button type="primary" @click="batchUpgradeLevel">批量升级</a-button>
          <a-button @click="batchDowngradeLevel">批量降级</a-button>
          <a-button @click="batchSuspend">批量暂停</a-button>
          <a-button @click="batchExport">批量导出</a-button>
        </a-space>
      </a-card>
    </div>

    <!-- 用户详情模态框 -->
    <UserDetailModal
      v-model:visible="detailModalVisible"
      :user="selectedUser"
      @close="handleDetailModalClose"
    />

    <!-- 编辑级别模态框 -->
    <EditUserLevelModal
      v-model:visible="editModalVisible"
      :user="selectedUser"
      @success="handleEditSuccess"
      @close="handleEditModalClose"
    />

    <!-- 用户权限模态框 -->
    <UserPermissionModal
      v-model:visible="permissionModalVisible"
      :user="selectedUser"
      @close="handlePermissionModalClose"
    />

    <!-- 访问历史模态框 -->
    <AccessHistoryModal
      v-model:visible="historyModalVisible"
      :user="selectedUser"
      @close="handleHistoryModalClose"
    />

    <!-- 批量级别调整模态框 -->
    <BatchLevelAdjustModal
      v-model:visible="batchAdjustModalVisible"
      :users="selectedUsers"
      @success="handleBatchAdjustSuccess"
      @close="handleBatchAdjustModalClose"
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
  SafetyOutlined,
  HistoryOutlined,
  UpOutlined,
  DownOutlined,
  StopOutlined,
  DownloadOutlined,
  ClockCircleOutlined,
  SwapOutlined,
  WarningOutlined,
  PhoneOutlined,
  MailOutlined,
  SecurityScanOutlined,
  UserSwitchOutlined,
  FileProtectOutlined,
  LockOutlined
} from '@ant-design/icons-vue'

import { businessPermissionApi } from '@/api/smart-permission'

// 导入子组件
import UserDetailModal from './modals/UserDetailModal.vue'
import EditUserLevelModal from './modals/EditUserLevelModal.vue'
import UserPermissionModal from './modals/UserPermissionModal.vue'
import AccessHistoryModal from './modals/AccessHistoryModal.vue'
import BatchLevelAdjustModal from './modals/BatchLevelAdjustModal.vue'

// 响应式数据
const searchText = ref('')
const selectedLevel = ref(null)
const selectedDepartment = ref(null)
const selectedStatus = ref(null)
const loading = ref(false)
const userList = ref([])
const selectedUser = ref(null)
const selectedRowKeys = ref([])

// 安全级别定义
const securityLevels = ref([
  {
    value: 1,
    name: '公开级',
    description: '最低安全级别，基础访问权限',
    icon: UserSwitchOutlined,
    userCount: 0,
    permissionCount: 0
  },
  {
    value: 2,
    name: '内部级',
    description: '内部员工标准访问权限',
    icon: SecurityScanOutlined,
    userCount: 0,
    permissionCount: 0
  },
  {
    value: 3,
    name: '秘密级',
    description: '敏感信息访问权限',
    icon: FileProtectOutlined,
    userCount: 0,
    permissionCount: 0
  },
  {
    value: 4,
    name: '机密级',
    description: '高度敏感信息访问权限',
    icon: LockOutlined,
    userCount: 0,
    permissionCount: 0
  },
  {
    value: 5,
    name: '绝密级',
    description: '最高安全级别，全面访问权限',
    icon: LockOutlined,
    userCount: 0,
    permissionCount: 0
  }
])

// 模态框状态
const detailModalVisible = ref(false)
const editModalVisible = ref(false)
const permissionModalVisible = ref(false)
const historyModalVisible = ref(false)
const batchAdjustModalVisible = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列配置
const columns = [
  {
    title: '用户信息',
    key: 'userInfo',
    width: 280,
    fixed: 'left'
  },
  {
    title: '当前安全级别',
    key: 'currentLevel',
    width: 200
  },
  {
    title: '权限统计',
    key: 'permissionStats',
    width: 250
  },
  {
    title: '访问限制',
    key: 'accessRestriction',
    width: 180
  },
  {
    title: '最近活动',
    key: 'recentActivity',
    width: 200
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

const selectedUsers = computed(() => {
  return userList.value.filter(user => selectedRowKeys.value.includes(user.userId))
})

// 方法定义
const selectLevel = (level) => {
  selectedLevel.value = level.value
  handleSearch()
}

const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

const handleReset = () => {
  searchText.value = ''
  selectedLevel.value = null
  selectedDepartment.value = null
  selectedStatus.value = null
  selectedRowKeys.value = []
  pagination.current = 1
  fetchUserList()
}

const handleLevelChange = () => {
  pagination.current = 1
  fetchUserList()
}

const handleDepartmentChange = () => {
  pagination.current = 1
  fetchUserList()
}

const handleStatusChange = () => {
  pagination.current = 1
  fetchUserList()
}

const handleTableChange = (paginationInfo) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  fetchUserList()
}

const viewUserDetail = (user) => {
  selectedUser.value = user
  detailModalVisible.value = true
}

const editUserLevel = (user) => {
  selectedUser.value = user
  editModalVisible.value = true
}

const showUserPermissionModal = (user) => {
  selectedUser.value = user
  permissionModalVisible.value = true
}

const showAccessHistoryModal = (user) => {
  selectedUser.value = user
  historyModalVisible.value = true
}

const upgradeSecurityLevel = async (user) => {
  Modal.confirm({
    title: '确认升级安全级别',
    content: `确定要为 ${user.userName} 升级安全级别吗？`,
    onOk: async () => {
      try {
        await businessPermissionApi.upgradeSecurityLevel(user.userId)
        message.success('安全级别升级成功')
        fetchUserList()
        fetchSecurityLevelStats()
      } catch (error) {
        message.error('安全级别升级失败')
        console.error('安全级别升级失败:', error)
      }
    }
  })
}

const downgradeSecurityLevel = async (user) => {
  Modal.confirm({
    title: '确认降级安全级别',
    content: `确定要为 ${user.userName} 降级安全级别吗？这将影响其访问权限。`,
    okText: '确认降级',
    okType: 'danger',
    onOk: async () => {
      try {
        await businessPermissionApi.downgradeSecurityLevel(user.userId)
        message.success('安全级别降级成功')
        fetchUserList()
        fetchSecurityLevelStats()
      } catch (error) {
        message.error('安全级别降级失败')
        console.error('安全级别降级失败:', error)
      }
    }
  })
}

const suspendUserLevel = async (user) => {
  Modal.confirm({
    title: '确认暂停权限',
    content: `确定要暂停 ${user.userName} 的所有权限吗？`,
    onOk: async () => {
      try {
        await businessPermissionApi.suspendUserSecurityLevel(user.userId)
        message.success('权限暂停成功')
        fetchUserList()
        fetchSecurityLevelStats()
      } catch (error) {
        message.error('权限暂停失败')
        console.error('权限暂停失败:', error)
      }
    }
  })
}

const showLevelUpgradeModal = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要调整的用户')
    return
  }
  batchAdjustModalVisible.value = true
}

const batchUpgradeLevel = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要升级的用户')
    return
  }
  batchAdjustModalVisible.value = true
}

const batchDowngradeLevel = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要降级的用户')
    return
  }
  Modal.confirm({
    title: '批量降级确认',
    content: `确定要为选中的 ${selectedRowKeys.value.length} 个用户降级安全级别吗？`,
    okText: '确认降级',
    okType: 'danger',
    onOk: async () => {
      try {
        await businessPermissionApi.batchDowngradeSecurityLevel(selectedRowKeys.value)
        message.success('批量降级成功')
        selectedRowKeys.value = []
        fetchUserList()
        fetchSecurityLevelStats()
      } catch (error) {
        message.error('批量降级失败')
        console.error('批量降级失败:', error)
      }
    }
  })
}

const batchSuspend = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要暂停的用户')
    return
  }
  Modal.confirm({
    title: '批量暂停确认',
    content: `确定要暂停选中的 ${selectedRowKeys.value.length} 个用户的权限吗？`,
    onOk: async () => {
      try {
        await businessPermissionApi.batchSuspendSecurityLevel(selectedRowKeys.value)
        message.success('批量暂停成功')
        selectedRowKeys.value = []
        fetchUserList()
        fetchSecurityLevelStats()
      } catch (error) {
        message.error('批量暂停失败')
        console.error('批量暂停失败:', error)
      }
    }
  })
}

const batchExport = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要导出的用户')
    return
  }
  message.info('批量导出功能开发中...')
}

const exportReport = async () => {
  try {
    const response = await businessPermissionApi.exportSecurityLevelReport({
      searchText: searchText.value,
      securityLevel: selectedLevel.value,
      department: selectedDepartment.value,
      status: selectedStatus.value
    })

    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `security-level-report-${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('报告导出成功')
  } catch (error) {
    message.error('报告导出失败')
    console.error('报告导出失败:', error)
  }
}

const exportUserReport = async (user) => {
  try {
    const response = await businessPermissionApi.exportUserSecurityReport(user.userId)

    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `user-security-report-${user.userName}-${dayjs().format('YYYYMMDD')}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('用户报告导出成功')
  } catch (error) {
    message.error('用户报告导出失败')
    console.error('用户报告导出失败:', error)
  }
}

const handleDetailModalClose = () => {
  detailModalVisible.value = false
  selectedUser.value = null
}

const handleEditModalClose = () => {
  editModalVisible.value = false
  selectedUser.value = null
}

const handlePermissionModalClose = () => {
  permissionModalVisible.value = false
  selectedUser.value = null
}

const handleHistoryModalClose = () => {
  historyModalVisible.value = false
  selectedUser.value = null
}

const handleBatchAdjustModalClose = () => {
  batchAdjustModalVisible.value = false
}

const handleEditSuccess = () => {
  editModalVisible.value = false
  fetchUserList()
  fetchSecurityLevelStats()
  message.success('用户级别编辑成功')
}

const handleBatchAdjustSuccess = () => {
  batchAdjustModalVisible.value = false
  selectedRowKeys.value = []
  fetchUserList()
  fetchSecurityLevelStats()
  message.success('批量级别调整成功')
}

const fetchUserList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      searchText: searchText.value,
      securityLevel: selectedLevel.value,
      department: selectedDepartment.value,
      status: selectedStatus.value
    }

    const response = await businessPermissionApi.getSecurityLevelUserList(params)

    if (response.data) {
      userList.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    message.error('获取用户安全级别列表失败')
    console.error('获取用户安全级别列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchSecurityLevelStats = async () => {
  try {
    const response = await businessPermissionApi.getSecurityLevelStatistics()
    if (response.data) {
      securityLevels.value.forEach(level => {
        const stats = response.data[level.value] || {}
        level.userCount = stats.userCount || 0
        level.permissionCount = stats.permissionCount || 0
      })
    }
  } catch (error) {
    console.error('获取安全级别统计失败:', error)
  }
}

// 工具函数
const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
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

const getPermissionPercent = (current, total) => {
  if (!total || total === 0) return 0
  return Math.round((current / total) * 100)
}

// 生命周期
onMounted(() => {
  fetchUserList()
  fetchSecurityLevelStats()
})
</script>

<style lang="less" scoped>
.security-level-management {
  .overview-section {
    .level-card {
      cursor: pointer;
      transition: all 0.3s ease;
      margin-bottom: 16px;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &.level-1 { border-left: 4px solid #1890ff; }
      &.level-2 { border-left: 4px solid #52c41a; }
      &.level-3 { border-left: 4px solid #faad14; }
      &.level-4 { border-left: 4px solid #f5222d; }
      &.level-5 { border-left: 4px solid #722ed1; }

      .level-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;

        .level-icon {
          font-size: 24px;
          color: #1890ff;
        }

        .level-info {
          .level-name {
            font-weight: 500;
            font-size: 16px;
            color: #262626;
            margin-bottom: 4px;
          }

          .level-desc {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }

      .level-stats {
        :deep(.ant-statistic-title) {
          font-size: 12px;
        }
      }
    }
  }

  .level-user-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .user-info {
      .user-header {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 8px;

        .user-basic {
          .user-name {
            font-weight: 500;
            font-size: 14px;
            color: #262626;
            margin-bottom: 4px;
          }

          .user-id, .user-dept {
            font-size: 12px;
            color: #8c8c8c;
            margin-bottom: 2px;
          }
        }
      }

      .user-contact {
        .contact-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #595959;
          margin-bottom: 2px;
        }
      }
    }

    .current-level {
      .level-info {
        margin-top: 8px;

        .level-score, .effective-time {
          font-size: 11px;
          color: #8c8c8c;
          margin-top: 4px;
        }
      }
    }

    .permission-stats {
      .stat-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        .stat-label {
          font-size: 12px;
          color: #595959;
          width: 70px;
        }

        .stat-value {
          font-size: 12px;
          color: #262626;
        }
      }
    }

    .access-restriction {
      .restriction-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .restriction-label {
          font-size: 12px;
          color: #595959;
        }
      }
    }

    .recent-activity {
      .activity-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 11px;
        color: #595959;
        margin-bottom: 4px;
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
</style>