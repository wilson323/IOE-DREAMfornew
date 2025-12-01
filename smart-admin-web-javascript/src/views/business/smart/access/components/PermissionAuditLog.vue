<template>
  <div class="permission-audit-log">
    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <a-card title="权限审计日志查询" size="small">
        <a-row :gutter="16">
          <a-col :span="5">
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索操作人、用户或资源"
              @search="handleSearch"
              allow-clear
            />
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedModule"
              placeholder="选择模块"
              style="width: 100%"
              allow-clear
              @change="handleModuleChange"
            >
              <a-select-option value="area">区域管理</a-select-option>
              <a-select-option value="device">设备管理</a-select-option>
              <a-select-option value="attendance">考勤管理</a-select-option>
              <a-select-option value="access">门禁管理</a-select-option>
              <a-select-option value="system">系统管理</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedOperationType"
              placeholder="操作类型"
              style="width: 100%"
              allow-clear
              @change="handleOperationTypeChange"
            >
              <a-select-option value="grant">授权</a-select-option>
              <a-select-option value="revoke">撤销</a-select-option>
              <a-select-option value="modify">修改</a-select-option>
              <a-select-option value="delete">删除</a-select-option>
              <a-select-option value="view">查看</a-select-option>
              <a-select-option value="export">导出</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedRiskLevel"
              placeholder="风险级别"
              style="width: 100%"
              allow-clear
              @change="handleRiskLevelChange"
            >
              <a-select-option :value="1">低风险</a-select-option>
              <a-select-option :value="2">中风险</a-select-option>
              <a-select-option :value="3">高风险</a-select-option>
              <a-select-option :value="4">严重风险</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="7">
            <a-range-picker
              v-model:value="dateTimeRange"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="操作时间范围"
              @change="handleDateTimeRangeChange"
              style="width: 100%"
            />
          </a-col>
        </a-row>
        <a-row :gutter="16" style="margin-top: 16px;">
          <a-col :span="24">
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a-button type="primary" @click="exportAuditLog">
                <template #icon><ExportOutlined /></template>
                导出日志
              </a-button>
              <a-button @click="showStatisticsModal">
                <template #icon><BarChartOutlined /></template>
                统计分析
              </a-button>
              <a-button @click="showRealTimeMonitor">
                <template #icon><MonitorOutlined /></template>
                实时监控
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards" style="margin-bottom: 16px;">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card size="small">
            <a-statistic
              title="今日操作总数"
              :value="statistics.todayTotal"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <FieldTimeOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small">
            <a-statistic
              title="高风险操作"
              :value="statistics.highRiskCount"
              :value-style="{ color: '#f5222d' }"
            >
              <template #prefix>
                <ExclamationCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small">
            <a-statistic
              title="异常操作"
              :value="statistics.abnormalCount"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <WarningOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small">
            <a-statistic
              title="权限变更"
              :value="statistics.permissionChangeCount"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <SwapOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 审计日志列表 -->
    <div class="audit-log-list">
      <a-table
        :columns="columns"
        :data-source="auditLogList"
        :loading="loading"
        :pagination="pagination"
        row-key="logId"
        @change="handleTableChange"
        :scroll="{ x: 1600 }"
        :row-class-name="getRowClassName"
      >
        <!-- 操作信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operationInfo'">
            <div class="operation-info">
              <div class="operation-header">
                <div class="operation-type">
                  <a-tag :color="getOperationTypeColor(record.operationType)">
                    {{ getOperationTypeText(record.operationType) }}
                  </a-tag>
                  <a-tag :color="getModuleColor(record.module)">
                    {{ getModuleText(record.module) }}
                  </a-tag>
                </div>
                <a-tag :color="getRiskLevelColor(record.riskLevel)" v-if="record.riskLevel">
                  {{ getRiskLevelText(record.riskLevel) }}
                </a-tag>
              </div>
              <div class="operation-desc">{{ record.operationDesc }}</div>
              <div class="operation-target" v-if="record.targetInfo">
                目标：{{ record.targetInfo }}
              </div>
            </div>
          </template>

          <!-- 操作人信息 -->
          <template v-else-if="column.key === 'operatorInfo'">
            <div class="operator-info">
              <div class="operator-name">{{ record.operatorName }}</div>
              <div class="operator-role">{{ record.operatorRole || '-' }}</div>
              <div class="operator-dept">{{ record.operatorDept || '-' }}</div>
              <div class="operator-ip">IP: {{ record.operatorIp || '-' }}</div>
            </div>
          </template>

          <!-- 操作结果 -->
          <template v-else-if="column.key === 'result'">
            <div class="operation-result">
              <a-tag :color="getResultColor(record.result)">
                {{ getResultText(record.result) }}
              </a-tag>
              <div class="result-detail" v-if="record.resultDetail">
                {{ record.resultDetail }}
              </div>
            </div>
          </template>

          <!-- 影响范围 -->
          <template v-else-if="column.key === 'impact'">
            <div class="impact-info">
              <div class="affected-users" v-if="record.affectedUsers">
                影响用户：{{ record.affectedUsers }}
              </div>
              <div class="affected-resources" v-if="record.affectedResources">
                影响资源：{{ record.affectedResources }}
              </div>
              <div class="impact-level">
                <a-progress
                  :percent="record.impactLevel"
                  size="small"
                  :stroke-color="getImpactLevelColor(record.impactLevel)"
                  :show-info="false"
                />
                <span class="impact-text">影响程度：{{ record.impactLevel }}%</span>
              </div>
            </div>
          </template>

          <!-- 操作时间 -->
          <template v-else-if="column.key === 'operationTime'">
            <div class="operation-time">
              <div>{{ formatDateTime(record.operationTime) }}</div>
              <div class="time-duration" v-if="record.duration">
                耗时：{{ record.duration }}ms
              </div>
            </div>
          </template>

          <!-- 操作详情 -->
          <template v-else-if="column.key === 'details'">
            <div class="operation-details">
              <div class="detail-item" v-if="record.beforeChange">
                <span class="detail-label">变更前:</span>
                <span class="detail-value">{{ record.beforeChange }}</span>
              </div>
              <div class="detail-item" v-if="record.afterChange">
                <span class="detail-label">变更后:</span>
                <span class="detail-value">{{ record.afterChange }}</span>
              </div>
              <div class="detail-item" v-if="record.requestParams">
                <span class="detail-label">请求参数:</span>
                <span class="detail-value">{{ record.requestParams }}</span>
              </div>
            </div>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewLogDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="showLogTrail(record)">
                <template #icon><ShareAltOutlined /></template>
                轨迹
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="exportSingleLog(record)">
                      <DownloadOutlined />
                      导出日志
                    </a-menu-item>
                    <a-menu-item @click="showRelatedLogs(record)">
                      <LinkOutlined />
                      关联日志
                    </a-menu-item>
                    <a-menu-item @click="reportAbnormal(record)" v-if="record.result === 'FAILED' || record.riskLevel >= 3">
                      <ExclamationCircleOutlined />
                      举报异常
                    </a-menu-item>
                    <a-menu-item @click="createAlert(record)" v-if="record.riskLevel >= 3">
                      <BellOutlined />
                      创建告警
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

    <!-- 日志详情模态框 -->
    <LogDetailModal
      v-model:visible="detailModalVisible"
      :log="selectedLog"
      @close="handleDetailModalClose"
    />

    <!-- 日志轨迹模态框 -->
    <LogTrailModal
      v-model:visible="trailModalVisible"
      :log="selectedLog"
      @close="handleTrailModalClose"
    />

    <!-- 关联日志模态框 -->
    <RelatedLogsModal
      v-model:visible="relatedLogsModalVisible"
      :log="selectedLog"
      @close="handleRelatedLogsModalClose"
    />

    <!-- 统计分析模态框 -->
    <StatisticsModal
      v-model:visible="statisticsModalVisible"
      @close="handleStatisticsModalClose"
    />

    <!-- 实时监控模态框 -->
    <RealTimeMonitorModal
      v-model:visible="realTimeMonitorModalVisible"
      @close="handleRealTimeMonitorModalClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  ReloadOutlined,
  ExportOutlined,
  BarChartOutlined,
  MonitorOutlined,
  EyeOutlined,
  ShareAltOutlined,
  DownloadOutlined,
  LinkOutlined,
  ExclamationCircleOutlined,
  BellOutlined,
  DownOutlined,
  FieldTimeOutlined,
  WarningOutlined,
  SwapOutlined
} from '@ant-design/icons-vue'

import { businessPermissionApi } from '@/api/smart-permission'

// 导入子组件
import LogDetailModal from './modals/LogDetailModal.vue'
import LogTrailModal from './modals/LogTrailModal.vue'
import RelatedLogsModal from './modals/RelatedLogsModal.vue'
import StatisticsModal from './modals/StatisticsModal.vue'
import RealTimeMonitorModal from './modals/RealTimeMonitorModal.vue'

// 响应式数据
const searchText = ref('')
const selectedModule = ref(null)
const selectedOperationType = ref(null)
const selectedRiskLevel = ref(null)
const dateTimeRange = ref(null)
const loading = ref(false)
const auditLogList = ref([])
const selectedLog = ref(null)

// 统计数据
const statistics = ref({
  todayTotal: 0,
  highRiskCount: 0,
  abnormalCount: 0,
  permissionChangeCount: 0
})

// 模态框状态
const detailModalVisible = ref(false)
const trailModalVisible = ref(false)
const relatedLogsModalVisible = ref(false)
const statisticsModalVisible = ref(false)
const realTimeMonitorModalVisible = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列配置
const columns = [
  {
    title: '操作信息',
    key: 'operationInfo',
    width: 300,
    fixed: 'left'
  },
  {
    title: '操作人',
    key: 'operatorInfo',
    width: 150
  },
  {
    title: '操作结果',
    key: 'result',
    width: 120
  },
  {
    title: '影响范围',
    key: 'impact',
    width: 200
  },
  {
    title: '操作时间',
    key: 'operationTime',
    width: 160
  },
  {
    title: '操作详情',
    key: 'details',
    width: 250
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 实时更新定时器
let realTimeTimer = null

// 方法定义
const handleSearch = () => {
  pagination.current = 1
  fetchAuditLogList()
}

const handleReset = () => {
  searchText.value = ''
  selectedModule.value = null
  selectedOperationType.value = null
  selectedRiskLevel.value = null
  dateTimeRange.value = null
  pagination.current = 1
  fetchAuditLogList()
}

const handleModuleChange = () => {
  pagination.current = 1
  fetchAuditLogList()
}

const handleOperationTypeChange = () => {
  pagination.current = 1
  fetchAuditLogList()
}

const handleRiskLevelChange = () => {
  pagination.current = 1
  fetchAuditLogList()
}

const handleDateTimeRangeChange = () => {
  pagination.current = 1
  fetchAuditLogList()
}

const handleTableChange = (paginationInfo) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  fetchAuditLogList()
}

const viewLogDetail = (log) => {
  selectedLog.value = log
  detailModalVisible.value = true
}

const showLogTrail = (log) => {
  selectedLog.value = log
  trailModalVisible.value = true
}

const showRelatedLogs = (log) => {
  selectedLog.value = log
  relatedLogsModalVisible.value = true
}

const exportSingleLog = async (log) => {
  try {
    const response = await businessPermissionApi.exportSingleAuditLog(log.logId)

    const blob = new Blob([response.data], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `audit-log-${log.logId}-${dayjs().format('YYYYMMDDHHmmss')}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('日志导出成功')
  } catch (error) {
    message.error('日志导出失败')
    console.error('日志导出失败:', error)
  }
}

const reportAbnormal = (log) => {
  message.info('异常举报功能开发中...')
}

const createAlert = (log) => {
  message.info('创建告警功能开发中...')
}

const showStatisticsModal = () => {
  statisticsModalVisible.value = true
}

const showRealTimeMonitor = () => {
  realTimeMonitorModalVisible.value = true
}

const exportAuditLog = async () => {
  try {
    const response = await businessPermissionApi.exportAuditLog({
      searchText: searchText.value,
      module: selectedModule.value,
      operationType: selectedOperationType.value,
      riskLevel: selectedRiskLevel.value,
      startTime: dateTimeRange.value?.[0]?.toISOString(),
      endTime: dateTimeRange.value?.[1]?.toISOString()
    })

    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `audit-log-${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('审计日志导出成功')
  } catch (error) {
    message.error('审计日志导出失败')
    console.error('审计日志导出失败:', error)
  }
}

const handleDetailModalClose = () => {
  detailModalVisible.value = false
  selectedLog.value = null
}

const handleTrailModalClose = () => {
  trailModalVisible.value = false
  selectedLog.value = null
}

const handleRelatedLogsModalClose = () => {
  relatedLogsModalVisible.value = false
  selectedLog.value = null
}

const handleStatisticsModalClose = () => {
  statisticsModalVisible.value = false
}

const handleRealTimeMonitorModalClose = () => {
  realTimeMonitorModalVisible.value = false
}

const fetchAuditLogList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      searchText: searchText.value,
      module: selectedModule.value,
      operationType: selectedOperationType.value,
      riskLevel: selectedRiskLevel.value,
      startTime: dateTimeRange.value?.[0]?.toISOString(),
      endTime: dateTimeRange.value?.[1]?.toISOString()
    }

    const response = await businessPermissionApi.getAuditLogList(params)

    if (response.data) {
      auditLogList.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    message.error('获取审计日志列表失败')
    console.error('获取审计日志列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const response = await businessPermissionApi.getAuditLogStatistics()
    if (response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const getRowClassName = (record) => {
  if (record.riskLevel >= 4) {
    return 'high-risk-row'
  }
  if (record.result === 'FAILED') {
    return 'failed-row'
  }
  return ''
}

// 工具函数
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const getOperationTypeColor = (type) => {
  const colors = {
    'grant': 'green',
    'revoke': 'red',
    'modify': 'blue',
    'delete': 'orange',
    'view': 'default',
    'export': 'purple'
  }
  return colors[type] || 'default'
}

const getOperationTypeText = (type) => {
  const textMap = {
    'grant': '授权',
    'revoke': '撤销',
    'modify': '修改',
    'delete': '删除',
    'view': '查看',
    'export': '导出'
  }
  return textMap[type] || type
}

const getModuleColor = (module) => {
  const colors = {
    'area': 'blue',
    'device': 'green',
    'attendance': 'orange',
    'access': 'purple',
    'system': 'red'
  }
  return colors[module] || 'default'
}

const getModuleText = (module) => {
  const textMap = {
    'area': '区域管理',
    'device': '设备管理',
    'attendance': '考勤管理',
    'access': '门禁管理',
    'system': '系统管理'
  }
  return textMap[module] || module
}

const getRiskLevelColor = (level) => {
  const colors = {
    1: 'green',   // 低风险
    2: 'orange',  // 中风险
    3: 'red',     // 高风险
    4: 'purple'   // 严重风险
  }
  return colors[level] || 'default'
}

const getRiskLevelText = (level) => {
  const textMap = {
    1: '低风险',
    2: '中风险',
    3: '高风险',
    4: '严重风险'
  }
  return textMap[level] || '未知'
}

const getResultColor = (result) => {
  const colors = {
    'SUCCESS': 'green',
    'FAILED': 'red',
    'PENDING': 'orange',
    'PARTIAL': 'blue'
  }
  return colors[result] || 'default'
}

const getResultText = (result) => {
  const textMap = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'PENDING': '处理中',
    'PARTIAL': '部分成功'
  }
  return textMap[result] || result
}

const getImpactLevelColor = (level) => {
  if (level <= 30) return '#52c41a'
  if (level <= 60) return '#faad14'
  if (level <= 80) return '#fa8c16'
  return '#f5222d'
}

// 生命周期
onMounted(() => {
  fetchAuditLogList()
  fetchStatistics()

  // 启动实时更新定时器
  realTimeTimer = setInterval(() => {
    fetchStatistics()
  }, 30000) // 每30秒更新一次统计数据
})

onUnmounted(() => {
  if (realTimeTimer) {
    clearInterval(realTimeTimer)
  }
})
</script>

<style lang="less" scoped>
.permission-audit-log {
  .search-bar {
    margin-bottom: 16px;
  }

  .statistics-cards {
    .ant-statistic {
      text-align: center;
    }
  }

  .audit-log-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .operation-info {
      .operation-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .operation-type {
          display: flex;
          gap: 4px;
        }
      }

      .operation-desc {
        font-weight: 500;
        font-size: 14px;
        color: #262626;
        margin-bottom: 4px;
      }

      .operation-target {
        font-size: 12px;
        color: #8c8c8c;
      }
    }

    .operator-info {
      .operator-name {
        font-weight: 500;
        font-size: 14px;
        color: #262626;
        margin-bottom: 4px;
      }

      .operator-role, .operator-dept, .operator-ip {
        font-size: 12px;
        color: #595959;
        margin-bottom: 2px;
      }
    }

    .operation-result {
      .result-detail {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 4px;
      }
    }

    .impact-info {
      .affected-users, .affected-resources {
        font-size: 12px;
        color: #595959;
        margin-bottom: 4px;
      }

      .impact-level {
        .impact-text {
          font-size: 12px;
          color: #595959;
          margin-left: 8px;
        }
      }
    }

    .operation-time {
      .time-duration {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 4px;
      }
    }

    .operation-details {
      .detail-item {
        margin-bottom: 4px;

        .detail-label {
          font-size: 12px;
          color: #595959;
          margin-right: 4px;
        }

        .detail-value {
          font-size: 12px;
          color: #262626;
          word-break: break-all;
        }
      }
    }

    // 高风险行样式
    :deep(.high-risk-row) {
      background-color: #fff2f0;

      &:hover {
        background-color: #ffebe6 !important;
      }
    }

    // 失败行样式
    :deep(.failed-row) {
      background-color: #fff1f0;

      &:hover {
        background-color: #ffebe6 !important;
      }
    }

    :deep(.ant-table-tbody > tr:hover > td) {
      background-color: #f5f5f5;
    }
  }
}
</style>