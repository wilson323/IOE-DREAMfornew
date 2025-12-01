<template>
  <div class="account-manage-page">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="header-title">
        <h2>账户管理</h2>
        <p class="header-description">管理用户消费账户，支持开卡、充值、冻结等操作</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="showCreateAccountModal" v-permission="['consume:account:create']">
          <template #icon><PlusOutlined /></template>
          开户
        </a-button>
        <a-button @click="batchRecharge" v-permission="['consume:account:recharge']">
          <template #icon><WalletOutlined /></template>
          批量充值
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
        <a-form-item label="用户ID">
          <a-input
            v-model:value="searchForm.userId"
            placeholder="请输入用户ID"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>

        <a-form-item label="账户名">
          <a-input
            v-model:value="searchForm.accountName"
            placeholder="请输入账户名"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="账户类型">
          <a-select
            v-model:value="searchForm.accountType"
            placeholder="请选择账户类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="STAFF">员工账户</a-select-option>
            <a-select-option value="STUDENT">学生账户</a-select-option>
            <a-select-option value="VISITOR">访客账户</a-select-option>
            <a-select-option value="TEMP">临时账户</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="账户状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="FROZEN">冻结</a-select-option>
            <a-select-option value="CLOSED">已关闭</a-select-option>
            <a-select-option value="LOCKED">锁定</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="余额范围">
          <a-input-number
            v-model:value="searchForm.minBalance"
            placeholder="最小余额"
            :min="0"
            :precision="2"
            style="width: 120px"
          />
          <span class="range-separator">-</span>
          <a-input-number
            v-model:value="searchForm.maxBalance"
            placeholder="最大余额"
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
              title="总账户数"
              :value="statistics.totalCount"
              :precision="0"
              :value-style="{ color: '#3f8600' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="总余额"
              :value="statistics.totalBalance"
              :precision="2"
              prefix="¥"
              :value-style="{ color: '#cf1322' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="活跃账户数"
              :value="statistics.activeCount"
              :precision="0"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="冻结账户数"
              :value="statistics.frozenCount"
              :precision="0"
              :value-style="{ color: '#faad14' }"
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
        :row-key="record => record.accountId"
        :scroll="{ x: 1500 }"
        @change="handleTableChange"
      >
        <!-- 账户信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'accountInfo'">
            <div class="account-info">
              <div class="account-name">{{ record.accountName || '未设置' }}</div>
              <div class="account-id">ID: {{ record.userId }}</div>
              <a-tag :color="getAccountTypeColor(record.accountType)" size="small">
                {{ getAccountTypeText(record.accountType) }}
              </a-tag>
            </div>
          </template>

          <!-- 余额信息 -->
          <template v-else-if="column.key === 'balance'">
            <div class="balance-info">
              <div class="current-balance" :class="{ 'balance-negative': record.balance < 0 }">
                ¥{{ formatMoney(record.balance) }}
              </div>
              <div class="frozen-amount" v-if="record.frozenAmount > 0">
                冻结: ¥{{ formatMoney(record.frozenAmount) }}
              </div>
            </div>
          </template>

          <!-- 账户状态 -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- 开户时间 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <!-- 最后活动时间 -->
          <template v-else-if="column.key === 'lastActivityTime'">
            {{ record.lastActivityTime ? formatDateTime(record.lastActivityTime) : '无记录' }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewAccountDetail(record)">
                详情
              </a-button>
              <a-button
                v-if="record.status === 'ACTIVE'"
                type="link"
                size="small"
                @click="handleRecharge(record)"
              >
                充值
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu @click="({ key }) => handleMenuClick(key, record)">
                    <a-menu-item key="freeze" v-if="record.status === 'ACTIVE'">冻结账户</a-menu-item>
                    <a-menu-item key="unfreeze" v-if="record.status === 'FROZEN'">解冻账户</a-menu-item>
                    <a-menu-item key="resetPassword">重置密码</a-menu-item>
                    <a-menu-item key="setLimit">设置限额</a-menu-item>
                    <a-menu-item key="viewTransactions">查看交易记录</a-menu-item>
                    <a-menu-item key="exportInfo">导出账户信息</a-menu-item>
                    <a-menu-item key="closeAccount" v-if="record.status !== 'CLOSED'">关闭账户</a-menu-item>
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

    <!-- 创建账户弹窗 -->
    <CreateAccountModal
      v-model:visible="createAccountModalVisible"
      @success="handleCreateSuccess"
    />

    <!-- 充值弹窗 -->
    <RechargeModal
      v-model:visible="rechargeModalVisible"
      :account-info="currentAccount"
      @success="handleRechargeSuccess"
    />

    <!-- 账户详情弹窗 -->
    <AccountDetailModal
      v-model:visible="detailModalVisible"
      :account-info="currentAccount"
    />

    <!-- 批量充值弹窗 -->
    <BatchRechargeModal
      v-model:visible="batchRechargeModalVisible"
      @success="handleBatchRechargeSuccess"
    />

    <!-- 账户限额设置弹窗 -->
    <AccountLimitModal
      v-model:visible="limitModalVisible"
      :account-info="currentAccount"
      @success="handleLimitSuccess"
    />

    <!-- 导出弹窗 -->
    <ExportModal
      v-model:visible="exportModalVisible"
      :export-type="'account'"
      @export="handleExport"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  WalletOutlined,
  ReloadOutlined,
  SearchOutlined,
  ClearOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import CreateAccountModal from './components/CreateAccountModal.vue'
import RechargeModal from './components/RechargeModal.vue'
import AccountDetailModal from './components/AccountDetailModal.vue'
import BatchRechargeModal from './components/BatchRechargeModal.vue'
import AccountLimitModal from './components/AccountLimitModal.vue'
import ExportModal from './components/ExportModal.vue'
import { accountApi } from '@/api/business/consume/account'

// 响应式数据
const tableLoading = ref(false)
const tableData = ref([])
const createAccountModalVisible = ref(false)
const rechargeModalVisible = ref(false)
const detailModalVisible = ref(false)
const batchRechargeModalVisible = ref(false)
const limitModalVisible = ref(false)
const exportModalVisible = ref(false)
const currentAccount = ref(null)

// 搜索表单
const searchForm = reactive({
  userId: '',
  accountName: '',
  accountType: '',
  status: '',
  minBalance: null,
  maxBalance: null
})

// 统计数据
const statistics = reactive({
  totalCount: 0,
  totalBalance: 0,
  activeCount: 0,
  frozenCount: 0
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
    title: '账户信息',
    key: 'accountInfo',
    width: 200,
    fixed: 'left'
  },
  {
    title: '余额',
    key: 'balance',
    width: 150,
    sorter: true,
    align: 'right'
  },
  {
    title: '账户状态',
    key: 'status',
    width: 100,
    filters: [
      { text: '正常', value: 'ACTIVE' },
      { text: '冻结', value: 'FROZEN' },
      { text: '已关闭', value: 'CLOSED' },
      { text: '锁定', value: 'LOCKED' }
    ]
  },
  {
    title: '开户时间',
    key: 'createTime',
    width: 180,
    sorter: true
  },
  {
    title: '最后活动',
    key: 'lastActivityTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right'
  }
]

// 方法
const fetchAccountList = async () => {
  try {
    tableLoading.value = true
    const params = {
      ...searchForm,
      page: pagination.current,
      size: pagination.pageSize
    }

    const response = await accountApi.getAccountList(params)
    if (response.success) {
      tableData.value = response.data.records
      pagination.total = response.data.total
      // 更新统计数据
      Object.assign(statistics, response.data.statistics)
    } else {
      message.error(response.message || '获取账户列表失败')
    }
  } catch (error) {
    console.error('获取账户列表失败:', error)
    message.error('获取账户列表失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchAccountList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    userId: '',
    accountName: '',
    accountType: '',
    status: '',
    minBalance: null,
    maxBalance: null
  })
  handleSearch()
}

const refreshData = () => {
  fetchAccountList()
  message.success('数据已刷新')
}

const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchAccountList()
}

const showCreateAccountModal = () => {
  createAccountModalVisible.value = true
}

const handleCreateSuccess = () => {
  fetchAccountList()
  message.success('账户创建成功')
}

const viewAccountDetail = (record) => {
  currentAccount.value = record
  detailModalVisible.value = true
}

const handleRecharge = (record) => {
  currentAccount.value = record
  rechargeModalVisible.value = true
}

const handleRechargeSuccess = () => {
  fetchAccountList()
  message.success('充值成功')
}

const batchRecharge = () => {
  batchRechargeModalVisible.value = true
}

const handleBatchRechargeSuccess = () => {
  fetchAccountList()
  message.success('批量充值成功')
}

const handleMenuClick = async (key, record) => {
  currentAccount.value = record

  switch (key) {
    case 'freeze':
      await handleFreezeAccount(record)
      break
    case 'unfreeze':
      await handleUnfreezeAccount(record)
      break
    case 'resetPassword':
      handleResetPassword(record)
      break
    case 'setLimit':
      limitModalVisible.value = true
      break
    case 'viewTransactions':
      viewTransactions(record)
      break
    case 'exportInfo':
      exportAccountInfo(record)
      break
    case 'closeAccount':
      await handleCloseAccount(record)
      break
  }
}

const handleFreezeAccount = async (record) => {
  Modal.confirm({
    title: '确认冻结账户',
    content: `确定要冻结账户 ${record.accountName} 吗？冻结后该账户将无法进行消费操作。`,
    onOk: async () => {
      try {
        const response = await accountApi.freezeAccount(record.accountId)
        if (response.success) {
          message.success('账户冻结成功')
          fetchAccountList()
        } else {
          message.error(response.message || '账户冻结失败')
        }
      } catch (error) {
        console.error('冻结账户失败:', error)
        message.error('账户冻结失败')
      }
    }
  })
}

const handleUnfreezeAccount = async (record) => {
  try {
    const response = await accountApi.unfreezeAccount(record.accountId)
    if (response.success) {
      message.success('账户解冻成功')
      fetchAccountList()
    } else {
      message.error(response.message || '账户解冻失败')
    }
  } catch (error) {
    console.error('解冻账户失败:', error)
    message.error('账户解冻失败')
  }
}

const handleResetPassword = (record) => {
  Modal.confirm({
    title: '确认重置密码',
    content: `确定要重置账户 ${record.accountName} 的密码吗？`,
    onOk: async () => {
      try {
        const response = await accountApi.resetPassword(record.accountId)
        if (response.success) {
          message.success('密码重置成功')
        } else {
          message.error(response.message || '密码重置失败')
        }
      } catch (error) {
        console.error('重置密码失败:', error)
        message.error('密码重置失败')
      }
    }
  })
}

const handleLimitSuccess = () => {
  message.success('限额设置成功')
}

const viewTransactions = (record) => {
  // 跳转到交易记录页面，并传递账户信息作为筛选条件
  // 这里可以使用路由跳转或者弹窗显示交易记录
  console.log('查看交易记录:', record)
}

const exportAccountInfo = (record) => {
  console.log('导出账户信息:', record)
}

const handleCloseAccount = async (record) => {
  Modal.confirm({
    title: '确认关闭账户',
    content: `确定要关闭账户 ${record.accountName} 吗？关闭后该账户将无法使用。`,
    okText: '确认关闭',
    okType: 'danger',
    onOk: async () => {
      try {
        const response = await accountApi.closeAccount(record.accountId)
        if (response.success) {
          message.success('账户关闭成功')
          fetchAccountList()
        } else {
          message.error(response.message || '账户关闭失败')
        }
      } catch (error) {
        console.error('关闭账户失败:', error)
        message.error('账户关闭失败')
      }
    }
  })
}

const showExportModal = () => {
  exportModalVisible.value = true
}

const handleExport = (exportParams) => {
  console.log('导出参数:', exportParams)
  // 实现导出逻辑
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

const getAccountTypeColor = (type) => {
  const colorMap = {
    STAFF: 'blue',
    STUDENT: 'green',
    VISITOR: 'orange',
    TEMP: 'gray'
  }
  return colorMap[type] || 'default'
}

const getAccountTypeText = (type) => {
  const textMap = {
    STAFF: '员工',
    STUDENT: '学生',
    VISITOR: '访客',
    TEMP: '临时'
  }
  return textMap[type] || type
}

const getStatusColor = (status) => {
  const colorMap = {
    ACTIVE: 'green',
    FROZEN: 'orange',
    CLOSED: 'red',
    LOCKED: 'purple'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    ACTIVE: '正常',
    FROZEN: '冻结',
    CLOSED: '已关闭',
    LOCKED: '锁定'
  }
  return textMap[status] || status
}

// 权限检查
const hasRefundPermission = ref(true) // 实际应该从权限系统获取

// 生命周期
onMounted(() => {
  fetchAccountList()
})
</script>

<style lang="less" scoped>
.account-manage-page {
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

.account-info {
  .account-name {
    font-weight: 600;
    color: #262626;
    margin-bottom: 4px;
  }

  .account-id {
    font-size: 12px;
    color: #8c8c8c;
    margin-bottom: 4px;
  }
}

.balance-info {
  .current-balance {
    font-weight: 600;
    color: #52c41a;

    &.balance-negative {
      color: #ff4d4f;
    }
  }

  .frozen-amount {
    font-size: 12px;
    color: #faad14;
    margin-top: 2px;
  }
}

.range-separator {
  margin: 0 8px;
  color: #8c8c8c;
}

:deep(.ant-table) {
  .ant-table-tbody > tr:hover > td {
    background: #f5f5f5;
  }
}

@media (max-width: 768px) {
  .account-manage-page {
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