<template>
  <div class="consume-record-page">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="header-title">
        <h2>消费记录管理</h2>
        <p class="header-description">查看和管理所有消费记录，支持多条件查询和数据导出</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="showExportModal">
          <template #icon><ExportOutlined /></template>
          导出数据
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
        <a-form-item label="消费单号">
          <a-input
            v-model:value="searchForm.orderNo"
            placeholder="请输入消费单号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="账户信息">
          <a-input
            v-model:value="searchForm.accountInfo"
            placeholder="用户ID或账户名"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="消费设备">
          <a-select
            v-model:value="searchForm.deviceId"
            placeholder="请选择消费设备"
            allow-clear
            style="width: 200px"
          >
            <a-select-option
              v-for="device in deviceOptions"
              :key="device.deviceId"
              :value="device.deviceId"
            >
              {{ device.deviceName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="消费模式">
          <a-select
            v-model:value="searchForm.consumeMode"
            placeholder="请选择消费模式"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="FIXED_AMOUNT">固定金额</a-select-option>
            <a-select-option value="FREE_AMOUNT">自由金额</a-select-option>
            <a-select-option value="METERING">计量计费</a-select-option>
            <a-select-option value="PRODUCT_SCAN">商品扫码</a-select-option>
            <a-select-option value="ORDERING">订餐模式</a-select-option>
            <a-select-option value="SMART">智能模式</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="消费状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="SUCCESS">成功</a-select-option>
            <a-select-option value="FAILED">失败</a-select-option>
            <a-select-option value="REFUNDED">已退款</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="消费时间">
          <a-range-picker
            v-model:value="searchForm.consumeTimeRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="['开始时间', '结束时间']"
            style="width: 350px"
          />
        </a-form-item>

        <a-form-item label="金额范围">
          <a-input-number
            v-model:value="searchForm.minAmount"
            placeholder="最小金额"
            :min="0"
            :precision="2"
            style="width: 120px"
          />
          <span class="range-separator">-</span>
          <a-input-number
            v-model:value="searchForm.maxAmount"
            placeholder="最大金额"
            :min="0"
            :precision="2"
            style="width: 120px"
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
              title="今日消费笔数"
              :value="statistics.todayCount"
              :precision="0"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="今日消费金额"
              :value="statistics.todayAmount"
              :precision="2"
              prefix="¥"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="本月消费笔数"
              :value="statistics.monthCount"
              :precision="0"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="本月消费金额"
              :value="statistics.monthAmount"
              :precision="2"
              prefix="¥"
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
        :row-key="record => record.recordId"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
        @resizeColumn="handleColumnResize"
      >
        <!-- 消费单号 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderNo'">
            <a @click="viewDetail(record)">{{ record.orderNo }}</a>
          </template>

          <!-- 账户信息 -->
          <template v-else-if="column.key === 'accountInfo'">
            <div class="account-info">
              <div>用户ID: {{ record.userId }}</div>
              <div class="account-name">{{ record.userName || '未设置' }}</div>
            </div>
          </template>

          <!-- 消费金额 -->
          <template v-else-if="column.key === 'consumeAmount'">
            <span class="amount" :class="{ 'amount-negative': record.consumeAmount < 0 }">
              ¥{{ formatMoney(record.consumeAmount) }}
            </span>
          </template>

          <!-- 消费模式 -->
          <template v-else-if="column.key === 'consumeMode'">
            <a-tag :color="getConsumeModeColor(record.consumeMode)">
              {{ getConsumeModeText(record.consumeMode) }}
            </a-tag>
          </template>

          <!-- 消费状态 -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewDetail(record)">
                详情
              </a-button>
              <a-button
                v-if="record.status === 'SUCCESS'"
                type="link"
                size="small"
                @click="handleRefund(record)"
                :disabled="!hasRefundPermission"
              >
                退款
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu @click="({ key }) => handleMenuClick(key, record)">
                    <a-menu-item key="viewLog">查看日志</a-menu-item>
                    <a-menu-item key="exportRecord">导出记录</a-menu-item>
                    <a-menu-item key="printReceipt" v-if="record.status === 'SUCCESS'">打印小票</a-menu-item>
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

    <!-- 详情弹窗 -->
    <ConsumeRecordDetail
      v-model:visible="detailVisible"
      :record="selectedRecord"
      @refresh="refreshData"
    />

    <!-- 导出弹窗 -->
    <ExportModal
      v-model:visible="exportVisible"
      :search-params="searchForm"
      @export="handleExport"
    />

    <!-- 退款弹窗 -->
    <RefundModal
      v-model:visible="refundVisible"
      :record="selectedRecord"
      @success="handleRefundSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  ExportOutlined,
  ReloadOutlined,
  SearchOutlined,
  ClearOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import { consumeRecordApi } from '@/api/business/consume/record-api'
import { consumeAccountApi } from '@/api/business/consume/account-api'
import { deviceApi } from '@/api/business/device/device-api'
import ConsumeRecordDetail from './components/ConsumeRecordDetail.vue'
import ExportModal from './components/ExportModal.vue'
import RefundModal from './components/RefundModal.vue'
import { useUserStore } from '@/store/modules/user'
import dayjs from 'dayjs'

// 权限检查
const userStore = useUserStore()
const hasRefundPermission = computed(() => {
  return userStore.hasPermission('consume:refund')
})

// 响应式数据
const tableLoading = ref(false)
const tableData = ref([])
const deviceOptions = ref([])
const detailVisible = ref(false)
const exportVisible = ref(false)
const refundVisible = ref(false)
const selectedRecord = ref(null)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  accountInfo: '',
  deviceId: undefined,
  consumeMode: undefined,
  status: undefined,
  consumeTimeRange: [],
  minAmount: undefined,
  maxAmount: undefined
})

// 统计信息
const statistics = reactive({
  todayCount: 0,
  todayAmount: 0,
  monthCount: 0,
  monthAmount: 0
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100']
})

// 表格列定义
const tableColumns = ref([
  {
    title: '消费单号',
    dataIndex: 'orderNo',
    key: 'orderNo',
    width: 180,
    fixed: 'left',
    resizable: true,
    sorter: true
  },
  {
    title: '账户信息',
    key: 'accountInfo',
    width: 200,
    resizable: true
  },
  {
    title: '消费设备',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150,
    resizable: true,
    sorter: true
  },
  {
    title: '消费模式',
    dataIndex: 'consumeMode',
    key: 'consumeMode',
    width: 120,
    resizable: true,
    filters: [
      { text: '固定金额', value: 'FIXED_AMOUNT' },
      { text: '自由金额', value: 'FREE_AMOUNT' },
      { text: '计量计费', value: 'METERING' },
      { text: '商品扫码', value: 'PRODUCT_SCAN' },
      { text: '订餐模式', value: 'ORDERING' },
      { text: '智能模式', value: 'SMART' }
    ]
  },
  {
    title: '消费金额',
    dataIndex: 'consumeAmount',
    key: 'consumeAmount',
    width: 120,
    align: 'right',
    resizable: true,
    sorter: true,
    customRender: ({ text }) => `¥${formatMoney(text)}`
  },
  {
    title: '优惠金额',
    dataIndex: 'discountAmount',
    key: 'discountAmount',
    width: 120,
    align: 'right',
    resizable: true,
    customRender: ({ text }) => text ? `¥${formatMoney(text)}` : '-'
  },
  {
    title: '实付金额',
    dataIndex: 'actualAmount',
    key: 'actualAmount',
    width: 120,
    align: 'right',
    resizable: true,
    sorter: true,
    customRender: ({ text }) => `¥${formatMoney(text)}`
  },
  {
    title: '余额状态',
    dataIndex: 'balanceAfter',
    key: 'balanceAfter',
    width: 120,
    align: 'right',
    resizable: true,
    customRender: ({ text }) => `¥${formatMoney(text)}`
  },
  {
    title: '消费状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    resizable: true,
    filters: [
      { text: '成功', value: 'SUCCESS' },
      { text: '失败', value: 'FAILED' },
      { text: '已退款', value: 'REFUNDED' },
      { text: '已取消', value: 'CANCELLED' }
    ]
  },
  {
    title: '消费时间',
    dataIndex: 'consumeTime',
    key: 'consumeTime',
    width: 160,
    resizable: true,
    sorter: true,
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    align: 'center'
  }
])

// 生命周期
onMounted(() => {
  loadDeviceOptions()
  loadStatistics()
  loadData()
})

// 方法定义
const loadDeviceOptions = async () => {
  try {
    const result = await deviceApi.getDeviceList({ pageSize: 1000 })
    deviceOptions.value = result.data?.records || []
  } catch (error) {
    console.error('加载设备选项失败:', error)
  }
}

const loadStatistics = async () => {
  try {
    const result = await consumeRecordApi.getStatistics()
    if (result.success) {
      Object.assign(statistics, result.data)
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      ...buildSearchParams(),
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }

    const result = await consumeRecordApi.getRecordList(params)
    if (result.success) {
      tableData.value = result.data?.records || []
      pagination.total = result.data?.total || 0
    } else {
      message.error(result.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    message.error('加载数据失败')
  } finally {
    tableLoading.value = false
  }
}

const buildSearchParams = () => {
  const params = {}

  // 基础搜索条件
  if (searchForm.orderNo) params.orderNo = searchForm.orderNo
  if (searchForm.accountInfo) {
    // 判断是用户ID还是用户名
    if (/^\d+$/.test(searchForm.accountInfo)) {
      params.userId = parseInt(searchForm.accountInfo)
    } else {
      params.userName = searchForm.accountInfo
    }
  }
  if (searchForm.deviceId) params.deviceId = searchForm.deviceId
  if (searchForm.consumeMode) params.consumeMode = searchForm.consumeMode
  if (searchForm.status) params.status = searchForm.status

  // 金额范围
  if (searchForm.minAmount !== undefined && searchForm.minAmount !== null) {
    params.minAmount = searchForm.minAmount
  }
  if (searchForm.maxAmount !== undefined && searchForm.maxAmount !== null) {
    params.maxAmount = searchForm.maxAmount
  }

  // 时间范围
  if (searchForm.consumeTimeRange && searchForm.consumeTimeRange.length === 2) {
    params.startTime = searchForm.consumeTimeRange[0].format('YYYY-MM-DD HH:mm:ss')
    params.endTime = searchForm.consumeTimeRange[1].format('YYYY-MM-DD HH:mm:ss')
  }

  return params
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    orderNo: '',
    accountInfo: '',
    deviceId: undefined,
    consumeMode: undefined,
    status: undefined,
    consumeTimeRange: [],
    minAmount: undefined,
    maxAmount: undefined
  })
  handleSearch()
}

const refreshData = () => {
  loadStatistics()
  loadData()
}

const handleTableChange = (paginationConfig, filters, sorter) => {
  // 更新分页
  pagination.current = paginationConfig.current
  pagination.pageSize = paginationConfig.pageSize

  // 更新排序
  if (sorter.field) {
    const sortField = sorter.field
    const sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
    // 这里可以添加排序参数到搜索条件中
  }

  loadData()
}

const handleColumnResize = (width, column) => {
  // 处理列宽调整
  console.log('列宽调整:', column.key, width)
}

const viewDetail = (record) => {
  selectedRecord.value = record
  detailVisible.value = true
}

const handleRefund = (record) => {
  selectedRecord.value = record
  refundVisible.value = true
}

const handleRefundSuccess = () => {
  message.success('退款处理成功')
  refreshData()
}

const showExportModal = () => {
  exportVisible.value = true
}

const handleExport = async (exportParams) => {
  try {
    const params = {
      ...buildSearchParams(),
      ...exportParams
    }

    // 调用导出API
    const result = await consumeRecordApi.exportRecords(params)
    if (result.success) {
      // 下载文件
      const downloadUrl = result.data?.downloadUrl
      if (downloadUrl) {
        window.open(downloadUrl, '_blank')
        message.success('导出成功')
      } else {
        message.error('导出失败：未获取到下载链接')
      }
    } else {
      message.error(result.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  }
}

const handleMenuClick = (key, record) => {
  selectedRecord.value = record

  switch (key) {
    case 'viewLog':
      // 查看操作日志
      viewOperationLog(record)
      break
    case 'exportRecord':
      // 导出单条记录
      exportSingleRecord(record)
      break
    case 'printReceipt':
      // 打印小票
      printReceipt(record)
      break
  }
}

const viewOperationLog = (record) => {
  // TODO: 实现查看操作日志功能
  message.info('操作日志功能开发中')
}

const exportSingleRecord = async (record) => {
  try {
    const result = await consumeRecordApi.exportSingleRecord(record.recordId)
    if (result.success) {
      window.open(result.data?.downloadUrl, '_blank')
      message.success('导出成功')
    } else {
      message.error(result.message || '导出失败')
    }
  } catch (error) {
    console.error('导出单条记录失败:', error)
    message.error('导出失败')
  }
}

const printReceipt = (record) => {
  // TODO: 实现打印小票功能
  message.info('打印小票功能开发中')
}

// 工具方法
const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

const getConsumeModeText = (mode) => {
  const modeMap = {
    'FIXED_AMOUNT': '固定金额',
    'FREE_AMOUNT': '自由金额',
    'METERING': '计量计费',
    'PRODUCT_SCAN': '商品扫码',
    'ORDERING': '订餐模式',
    'SMART': '智能模式'
  }
  return modeMap[mode] || mode
}

const getConsumeModeColor = (mode) => {
  const colorMap = {
    'FIXED_AMOUNT': 'blue',
    'FREE_AMOUNT': 'green',
    'METERING': 'orange',
    'PRODUCT_SCAN': 'purple',
    'ORDERING': 'cyan',
    'SMART': 'red'
  }
  return colorMap[mode] || 'default'
}

const getStatusText = (status) => {
  const statusMap = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'REFUNDED': '已退款',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusColor = (status) => {
  const colorMap = {
    'SUCCESS': 'success',
    'FAILED': 'error',
    'REFUNDED': 'warning',
    'CANCELLED': 'default'
  }
  return colorMap[status] || 'default'
}
</script>

<style lang="less" scoped>
.consume-record-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background: white;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .header-title {
      h2 {
        margin: 0 0 8px 0;
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

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-card {
    margin-bottom: 16px;

    .ant-form {
      .ant-form-item {
        margin-bottom: 16px;
      }
    }
  }

  .stats-cards {
    margin-bottom: 16px;

    .stat-card {
      text-align: center;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      .ant-statistic-title {
        font-size: 14px;
        color: #8c8c8c;
      }

      .ant-statistic-content {
        font-size: 24px;
        font-weight: 600;
        color: #1890ff;
      }
    }
  }

  .table-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .account-info {
      line-height: 1.4;

      .account-name {
        color: #8c8c8c;
        font-size: 12px;
      }
    }

    .amount {
      font-weight: 600;

      &.amount-negative {
        color: #ff4d4f;
      }
    }

    .range-separator {
      margin: 0 8px;
      color: #8c8c8c;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .consume-record-page {
    padding: 16px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;

      .header-actions {
        width: 100%;
        justify-content: flex-end;
      }
    }

    .search-card {
      .ant-form {
        .ant-form-item {
          display: block;
          margin-right: 0;
        }
      }
    }

    .stats-cards {
      .ant-col {
        margin-bottom: 16px;
      }
    }
  }
}
</style>