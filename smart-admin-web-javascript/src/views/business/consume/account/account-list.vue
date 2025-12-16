<template>
  <div class="account-management">
    <!-- 搜索筛选区域 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="账户编号">
          <a-input
            v-model:value="searchForm.accountNo"
            placeholder="请输入账户编号"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="用户姓名">
          <a-input
            v-model:value="searchForm.userName"
            placeholder="请输入用户姓名"
            allow-clear
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
            <a-select-option value="TEMPORARY">临时账户</a-select-option>
            <a-select-option value="GUEST">访客账户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
            <a-select-option :value="2">冻结</a-select-option>
          </a-select>
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

    <!-- 操作按钮区域 -->
    <a-card class="table-card" :bordered="false">
      <template #title>
        <span>账户列表</span>
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增账户
          </a-button>
          <a-button @click="handleBatchImport">
            <template #icon><ImportOutlined /></template>
            批量导入
          </a-button>
          <a-button @click="handleBatchExport" :loading="exportLoading">
            <template #icon><ExportOutlined /></template>
            导出
          </a-button>
          <a-button
            type="primary"
            danger
            @click="handleBatchDelete"
            :disabled="selectedRowKeys.length === 0"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
        </a-space>
      </template>

      <!-- 表格区域 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
        row-key="accountId"
        :scroll="{ x: 1200 }"
      >
        <!-- 账户头像 -->
        <template #avatar="{ record }">
          <a-avatar :src="record.avatar" :size="40">
            {{ record.userName?.charAt(0) }}
          </a-avatar>
        </template>

        <!-- 账户状态 -->
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <!-- 余额显示 -->
        <template #balance="{ record }">
          <span :class="{ 'negative-balance': record.balance < 0 }">
            ¥{{ formatMoney(record.balance) }}
          </span>
        </template>

        <!-- 操作按钮 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleView(record)">
              详情
            </a-button>
            <a-button type="link" size="small" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-button type="link" size="small" @click="handleRecharge(record)">
              充值
            </a-button>
            <a-dropdown>
              <a-button type="link" size="small">
                更多 <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleFreeze(record)" v-if="record.status === 1">
                    <StopOutlined /> 冻结
                  </a-menu-item>
                  <a-menu-item @click="handleUnfreeze(record)" v-if="record.status === 2">
                    <PlayCircleOutlined /> 解冻
                  </a-menu-item>
                  <a-menu-item @click="handleResetPassword(record)">
                    <KeyOutlined /> 重置密码
                  </a-menu-item>
                  <a-menu-item @click="handleViewHistory(record)">
                    <HistoryOutlined /> 消费记录
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item
                    @click="handleDelete(record)"
                    danger
                  >
                    <DeleteOutlined /> 删除
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 账户表单弹窗 -->
    <AccountFormModal
      v-model:visible="formVisible"
      :form-data="formData"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />

    <!-- 充值弹窗 -->
    <RechargeModal
      v-model:visible="rechargeVisible"
      :account-data="currentAccount"
      @success="handleRechargeSuccess"
    />

    <!-- 批量导入弹窗 -->
    <ImportModal
      v-model:visible="importVisible"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup>
	import { ref, reactive, onMounted, computed, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  ImportOutlined,
  ExportOutlined,
  DeleteOutlined,
  DownOutlined,
  StopOutlined,
  PlayCircleOutlined,
  KeyOutlined,
  HistoryOutlined
} from '@ant-design/icons-vue'
import { consumeApi } from '/@/api/business/consume/consume-api'
import AccountFormModal from './components/AccountFormModal.vue'
import RechargeModal from './components/RechargeModal.vue'
import ImportModal from './components/ImportModal.vue'

// 搜索表单
const searchForm = reactive({
  accountNo: '',
  userName: '',
  accountType: undefined,
  status: undefined
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const exportLoading = ref(false)
const selectedRowKeys = ref([])

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条数据`
})

// 表单相关
const formVisible = ref(false)
const isEdit = ref(false)
const formData = ref({})
const rechargeVisible = ref(false)
const importVisible = ref(false)
const currentAccount = ref({})

// 表格列定义
const columns = [
  {
    title: '账户信息',
    key: 'accountInfo',
    width: 200,
    customRender: ({ record }) => {
      return h('div', { class: 'account-info' }, [
        h('div', { class: 'avatar-wrapper' }, [
          h('template', { slot: 'avatar' }, { record })
        ]),
        h('div', { class: 'account-details' }, [
          h('div', { class: 'account-no' }, `编号: ${record.accountNo}`),
          h('div', { class: 'user-name' }, record.userName)
        ])
      ])
    }
  },
  {
    title: '账户类型',
    dataIndex: 'accountType',
    key: 'accountType',
    width: 100,
    customRender: ({ record }) => {
      const typeMap = {
        STAFF: { text: '员工账户', color: 'blue' },
        STUDENT: { text: '学生账户', color: 'green' },
        TEMPORARY: { text: '临时账户', color: 'orange' },
        GUEST: { text: '访客账户', color: 'purple' }
      }
      const type = typeMap[record.accountType] || { text: record.accountType, color: 'default' }
      return h('a-tag', { color: type.color }, type.text)
    }
  },
  {
    title: '用户信息',
    key: 'userInfo',
    width: 150,
    customRender: ({ record }) => {
      return h('div', [
        h('div', record.userPhone || '-'),
        h('div', { class: 'text-secondary', style: { fontSize: '12px' } }, record.userEmail || '-')
      ])
    }
  },
  {
    title: '所属部门',
    dataIndex: 'departmentName',
    key: 'departmentName',
    width: 120
  },
  {
    title: '当前余额',
    key: 'balance',
    width: 100,
    align: 'right',
    customRender: ({ record }) => {
      return h('template', { slot: 'balance' }, { record })
    }
  },
  {
    title: '累计消费',
    key: 'totalConsume',
    width: 100,
    align: 'right',
    customRender: ({ record }) => {
      return `¥${formatMoney(record.totalConsume || 0)}`
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    align: 'center',
    customRender: ({ record }) => {
      return h('template', { slot: 'status' }, { record })
    }
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150
  },
  {
    title: '最后活跃',
    dataIndex: 'lastActiveTime',
    key: 'lastActiveTime',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    customRender: ({ record }) => {
      return h('template', { slot: 'action' }, { record })
    }
  }
]

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    1: 'success',  // 正常
    0: 'default',  // 禁用
    2: 'error'     // 冻结
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    1: '正常',
    0: '禁用',
    2: '冻结'
  }
  return textMap[status] || '未知'
}

// 格式化金额
const formatMoney = (amount) => {
  return Number(amount || 0).toFixed(2)
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = undefined
  })
  pagination.current = 1
  fetchData()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

// 选择行
const onSelectChange = (keys) => {
  selectedRowKeys.value = keys
}

// 获取数据
	const fetchData = async () => {
	  loading.value = true
	  try {
	    const params = {
	      pageNum: pagination.current,
	      pageSize: pagination.pageSize,
	      ...searchForm
	    }
	    const res = await consumeApi.getAccountList(params)
	    if (res.code !== 200 || !res.data) {
	      tableData.value = []
	      pagination.total = 0
	      return
	    }

	    const records = res.data.records || res.data.list || []
	    tableData.value = records.map((item) => ({
	      ...item,
	      userPhone: item.mobile,
	      userEmail: item.email
	    }))

	    pagination.total = res.data.total || 0
	    pagination.current = res.data.current || pagination.current
	    pagination.pageSize = res.data.size || pagination.pageSize

	  } catch (error) {
	    message.error('获取账户列表失败')
	  } finally {
	    loading.value = false
  }
}

// 新增账户
const handleAdd = () => {
  isEdit.value = false
  formData.value = {}
  formVisible.value = true
}

// 编辑账户
const handleEdit = (record) => {
  isEdit.value = true
  formData.value = { ...record }
  formVisible.value = true
}

// 查看详情
const handleView = async (record) => {
  try {
    const res = await consumeApi.getAccountDetail(record.accountId)
    if (res.code === 0) {
      Modal.info({
        title: `账户详情 - ${record.accountNo}`,
        content: `用户名: ${res.data.userName || '-'}\n余额: ${res.data.balance || 0}\n状态: ${res.data.status || '-'}`,
        width: 500
      })
    }
  } catch (error) {
    message.error('获取账户详情失败')
  }
}

// 充值
const handleRecharge = (record) => {
  currentAccount.value = record
  rechargeVisible.value = true
}

// 冻结账户
const handleFreeze = (record) => {
  Modal.confirm({
    title: '确认冻结',
    content: `确定要冻结账户 ${record.accountNo} 吗？`,
    onOk: async () => {
      try {
        await consumeApi.freezeAccountStatus(record.accountId, '管理员冻结')
        message.success('账户冻结成功')
        fetchData()
      } catch (error) {
        message.error('账户冻结失败')
      }
    }
  })
}

// 解冻账户
const handleUnfreeze = (record) => {
  Modal.confirm({
    title: '确认解冻',
    content: `确定要解冻账户 ${record.accountNo} 吗？`,
    onOk: async () => {
      try {
        await consumeApi.unfreezeAccountStatus(record.accountId)
        message.success('账户解冻成功')
        fetchData()
      } catch (error) {
        message.error('账户解冻失败')
      }
    }
  })
}

// 重置密码
const handleResetPassword = (record) => {
  Modal.confirm({
    title: '确认重置密码',
    content: `确定要重置账户 ${record.accountNo} 的密码吗？重置后密码为默认密码`,
    onOk: async () => {
      try {
        // 重置密码功能待后端 API 实现
        message.warning('重置密码功能待实现')
      } catch (error) {
        message.error('密码重置失败')
      }
    }
  })
}

// 查看消费记录
const handleViewHistory = (record) => {
  // 跳转到消费记录页面，带上账户ID参数
  window.open(`/business/consume/record?accountId=${record.accountId}`, '_blank')
}

// 删除账户
const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除账户 ${record.accountNo} 吗？此操作不可恢复！`,
    onOk: async () => {
      try {
        await consumeApi.deleteAccount(record.accountId)
        message.success('账户删除成功')
        fetchData()
      } catch (error) {
        message.error('账户删除失败')
      }
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个账户吗？此操作不可恢复！`,
    onOk: async () => {
      try {
        // 逐个删除
        for (const accountId of selectedRowKeys.value) {
          await consumeApi.deleteAccount(accountId)
        }
        message.success('批量删除成功')
        selectedRowKeys.value = []
        fetchData()
      } catch (error) {
        message.error('批量删除失败')
      }
    }
  })
}

// 批量导入
const handleBatchImport = () => {
  importVisible.value = true
}

// 批量导出
const handleBatchExport = async () => {
  exportLoading.value = true
  try {
    // 导出功能待后端 API 实现
    message.warning('导出功能待实现')
  } catch (error) {
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 表单成功回调
const handleFormSuccess = () => {
  formVisible.value = false
  fetchData()
}

// 充值成功回调
const handleRechargeSuccess = () => {
  rechargeVisible.value = false
  fetchData()
}

// 导入成功回调
const handleImportSuccess = () => {
  importVisible.value = false
  fetchData()
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style lang="less" scoped>
.account-management {
  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .account-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .account-details {
        .account-no {
          font-weight: 500;
          margin-bottom: 2px;
        }

        .user-name {
          color: #666;
          font-size: 13px;
        }
      }
    }

    .negative-balance {
      color: #ff4d4f;
      font-weight: 500;
    }
  }
}
</style>
