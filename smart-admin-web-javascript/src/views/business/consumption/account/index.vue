<!--
  * 账户管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025/11/17
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="consume-account-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h1>账户管理</h1>
        <p>管理用户消费账户，包括余额、充值、退款等操作</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增账户
          </a-button>
          <a-button @click="handleBatchRecharge">
            <template #icon><WalletOutlined /></template>
            批量充值
          </a-button>
          <a-button @click="handleExport">
            <template #icon><ExportOutlined /></template>
            导出数据
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-cards">
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #1890ff, #096dd9)">
            <UserOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ stats.totalAccounts }}</div>
            <div class="stat-card-title">总账户数</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #52c41a, #389e0d)">
            <DollarOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">¥{{ formatAmount(stats.totalBalance) }}</div>
            <div class="stat-card-title">总余额</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #faad14, #d48806)">
            <CreditCardOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">¥{{ formatAmount(stats.todayRecharge) }}</div>
            <div class="stat-card-title">今日充值</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #ff4d4f, #cf1322)">
            <TransactionOutlined />
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">¥{{ formatAmount(stats.todayConsume) }}</div>
            <div class="stat-card-title">今日消费</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 搜索筛选 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="账户姓名">
          <a-input
            v-model:value="searchForm.userName"
            placeholder="请输入账户姓名"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="工号">
          <a-input
            v-model:value="searchForm.employeeNo"
            placeholder="请输入工号"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            @pressEnter="handleSearch"
          />
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
            <a-select-option value="DISABLED">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="账户类型">
          <a-select
            v-model:value="searchForm.accountType"
            placeholder="请选择账户类型"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="EMPLOYEE">员工</a-select-option>
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEMPORARY">临时</a-select-option>
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

    <!-- 数据表格 -->
    <a-card :bordered="false">
      <template #title>
        <div class="table-title-wrapper">
          <span>账户列表</span>
          <a-tag color="blue" v-if="tableData.length > 0">
            共 {{ pagination.total }} 个账户
          </a-tag>
        </div>
      </template>

      <template #extra>
        <a-space>
          <a-button
            type="text"
            :icon="h(resolveComponent, 'SyncOutlined')"
            @click="handleRefresh"
            :loading="loading"
          >
            刷新
          </a-button>
          <a-dropdown>
            <template #overlay>
              <a-menu @click="handleBatchAction">
                <a-menu-item key="recharge">批量充值</a-menu-item>
                <a-menu-item key="freeze">批量冻结</a-menu-item>
                <a-menu-item key="unfreeze">批量解冻</a-menu-item>
                <a-menu-item key="export">批量导出</a-menu-item>
              </a-menu>
            </template>
            <a-button type="text" :disabled="selectedRowKeys.length === 0">
              批量操作 <DownOutlined />
            </a-button>
          </a-dropdown>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1400 }"
      >
        <!-- 账户信息 -->
        <template #accountInfo="{ record }">
          <div class="account-info">
            <div class="account-name">{{ record.userName }}</div>
            <div class="account-details">
              <span class="employee-no">工号: {{ record.employeeNo }}</span>
              <a-divider type="vertical" />
              <span class="phone">{{ record.phone }}</span>
            </div>
          </div>
        </template>

        <!-- 账户类型 -->
        <template #accountType="{ record }">
          <a-tag :color="getAccountTypeColor(record.accountType)">
            {{ getAccountTypeText(record.accountType) }}
          </a-tag>
        </template>

        <!-- 账户状态 -->
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            <component :is="getStatusIcon(record.status)" />
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <!-- 余额信息 -->
        <template #balance="{ record }">
          <div class="balance-info">
            <div class="balance-amount">¥{{ formatAmount(record.balance) }}</div>
            <div class="freeze-amount" v-if="record.freezeAmount > 0">
              冻结: ¥{{ formatAmount(record.freezeAmount) }}
            </div>
          </div>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleView(record)">
              <template #icon><EyeOutlined /></template>
              详情
            </a-button>
            <a-button type="link" size="small" @click="handleRecharge(record)">
              <template #icon><WalletOutlined /></template>
              充值
            </a-button>
            <a-button type="link" size="small" @click="handleConsumeRecord(record)">
              <template #icon><HistoryOutlined /></template>
              消费记录
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu @click="(e) => handleMoreAction(e.key, record)">
                  <a-menu-item key="freeze" v-if="record.status === 'ACTIVE'">冻结账户</a-menu-item>
                  <a-menu-item key="unfreeze" v-if="record.status === 'FROZEN'">解冻账户</a-menu-item>
                  <a-menu-item key="resetPassword">重置密码</a-menu-item>
                  <a-menu-item key="edit">编辑信息</a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="exportRecords">导出记录</a-menu-item>
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

    <!-- 账户详情弹窗 -->
    <AccountDetailModal
      v-model:visible="detailModalVisible"
      :account="currentAccount"
    />

    <!-- 充值弹窗 -->
    <RechargeModal
      v-model:visible="rechargeModalVisible"
      :account="currentAccount"
      @success="handleRechargeSuccess"
    />

    <!-- 批量充值弹窗 -->
    <BatchRechargeModal
      v-model:visible="batchRechargeModalVisible"
      @success="handleBatchRechargeSuccess"
    />

    <!-- 账户表单弹窗 -->
    <AccountFormModal
      v-model:visible="formModalVisible"
      :account="currentAccount"
      :mode="formMode"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, h, resolveComponent, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    WalletOutlined,
    ExportOutlined,
    SearchOutlined,
    ReloadOutlined,
    DownOutlined,
    UserOutlined,
    DollarOutlined,
    CreditCardOutlined,
    TransactionOutlined,
    EyeOutlined,
    HistoryOutlined,
    LockOutlined,
    UnlockOutlined,
    KeyOutlined,
    EditOutlined,
    SyncOutlined,
  } from '@ant-design/icons-vue';
  import { consumeAccountApi } from '/@/api/business/consume/consume-account-api';
  import { formatAmount, formatDateTime } from '/@/lib/format';
  import AccountDetailModal from './components/AccountDetailModal.vue';
  import RechargeModal from './components/RechargeModal.vue';
  import BatchRechargeModal from './components/BatchRechargeModal.vue';
  import AccountFormModal from './components/AccountFormModal.vue';

  // ----------------------- 响应式数据 -----------------------
  const loading = ref(false);
  const tableData = ref([]);
  const selectedRowKeys = ref([]);

  // 统计数据
  const stats = reactive({
    totalAccounts: 0,
    totalBalance: 0,
    todayRecharge: 0,
    todayConsume: 0,
  });

  // 搜索表单
  const searchForm = reactive({
    userName: '',
    employeeNo: '',
    phone: '',
    status: undefined,
    accountType: undefined,
  });

  // 分页配置
  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条数据`,
  });

  // 弹窗状态
  const detailModalVisible = ref(false);
  const rechargeModalVisible = ref(false);
  const batchRechargeModalVisible = ref(false);
  const formModalVisible = ref(false);
  const currentAccount = ref(null);
  const formMode = ref('add'); // add | edit

  // ----------------------- 表格配置 -----------------------
  const columns = [
    {
      title: '账户信息',
      dataIndex: 'accountInfo',
      key: 'accountInfo',
      width: 200,
      slots: { customRender: 'accountInfo' },
    },
    {
      title: '账户类型',
      dataIndex: 'accountType',
      key: 'accountType',
      width: 100,
      slots: { customRender: 'accountType' },
      filters: [
        { text: '员工', value: 'EMPLOYEE' },
        { text: '学生', value: 'STUDENT' },
        { text: '临时', value: 'TEMPORARY' },
      ],
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      slots: { customRender: 'status' },
      filters: [
        { text: '正常', value: 'ACTIVE' },
        { text: '冻结', value: 'FROZEN' },
        { text: '禁用', value: 'DISABLED' },
      ],
    },
    {
      title: '余额',
      dataIndex: 'balance',
      key: 'balance',
      width: 150,
      slots: { customRender: 'balance' },
      sorter: true,
    },
    {
      title: '部门',
      dataIndex: 'department',
      key: 'department',
      width: 120,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
      customRender: ({ text }) => formatDateTime(text),
      sorter: true,
    },
    {
      title: '最后消费',
      dataIndex: 'lastConsumeTime',
      key: 'lastConsumeTime',
      width: 160,
      customRender: ({ text }) => text ? formatDateTime(text) : '从未消费',
      sorter: true,
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
      width: 200,
      fixed: 'right',
      slots: { customRender: 'action' },
    },
  ];

  // 行选择配置
  const rowSelection = computed(() => ({
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys) => {
      selectedRowKeys.value = keys;
    },
  }));

  // ----------------------- 生命周期 -----------------------
  onMounted(() => {
    queryTableData();
    loadStatsData();
  });

  // ----------------------- 数据查询 -----------------------
  const queryTableData = async () => {
    try {
      loading.value = true;

      const params = {
        ...searchForm,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };

      // TODO: 调用后端API
      // const res = await consumeAccountApi.queryAccountList(params);
      // if (res.data) {
      //   tableData.value = res.data.list;
      //   pagination.total = res.data.total;
      // }

      // 模拟数据
      setTimeout(() => {
        tableData.value = generateMockData();
        pagination.total = tableData.value.length;
      }, 500);

    } catch (error) {
      console.error('查询账户列表失败:', error);
      message.error('查询账户列表失败');
    } finally {
      loading.value = false;
    }
  };

  const loadStatsData = async () => {
    try {
      // TODO: 调用后端API
      // const res = await consumeAccountApi.getAccountStats();
      // if (res.data) {
      //   Object.assign(stats, res.data);
      // }

      // 模拟数据
      stats.totalAccounts = 2456;
      stats.totalBalance = 1245780.50;
      stats.todayRecharge = 25680.00;
      stats.todayConsume = 18450.30;
    } catch (error) {
      console.error('加载统计数据失败:', error);
    }
  };

  // 生成模拟数据
  const generateMockData = () => {
    const mockData = [];
    const accountTypes = ['EMPLOYEE', 'STUDENT', 'TEMPORARY'];
    const statuses = ['ACTIVE', 'FROZEN', 'DISABLED'];
    const departments = ['技术部', '人事部', '财务部', '市场部', '运营部'];
    const surnames = ['张', '李', '王', '刘', '陈', '杨', '赵', '黄', '周', '吴'];
    const names = ['伟', '芳', '娜', '敏', '静', '丽', '强', '磊', '洋', '艳'];

    for (let i = 1; i <= 100; i++) {
      const surname = surnames[Math.floor(Math.random() * surnames.length)];
      const name = names[Math.floor(Math.random() * names.length)];
      const accountType = accountTypes[Math.floor(Math.random() * accountTypes.length)];
      const status = statuses[Math.floor(Math.random() * statuses.length)];

      mockData.push({
        id: i,
        userName: surname + name,
        employeeNo: 'EMP' + i.toString().padStart(6, '0'),
        phone: '138' + Math.floor(Math.random() * 100000000).toString().padStart(8, '0'),
        accountType: accountType,
        status: status,
        balance: Math.random() * 5000 + 100,
        freezeAmount: status === 'FROZEN' ? Math.random() * 1000 : 0,
        department: departments[Math.floor(Math.random() * departments.length)],
        createTime: new Date(Date.now() - Math.random() * 31536000000),
        lastConsumeTime: Math.random() > 0.3 ? new Date(Date.now() - Math.random() * 604800000) : null,
      });
    }
    return mockData;
  };

  // ----------------------- 事件处理 -----------------------
  const handleSearch = () => {
    pagination.current = 1;
    queryTableData();
  };

  const handleReset = () => {
    Object.keys(searchForm).forEach(key => {
      searchForm[key] = undefined;
    });
    searchForm.userName = '';
    searchForm.employeeNo = '';
    searchForm.phone = '';
    handleSearch();
  };

  const handleRefresh = () => {
    queryTableData();
    loadStatsData();
  };

  const handleTableChange = (pag, filters, sorter) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    queryTableData();
  };

  const handleAdd = () => {
    currentAccount.value = null;
    formMode.value = 'add';
    formModalVisible.value = true;
  };

  const handleView = (record) => {
    currentAccount.value = record;
    detailModalVisible.value = true;
  };

  const handleRecharge = (record) => {
    currentAccount.value = record;
    rechargeModalVisible.value = true;
  };

  const handleConsumeRecord = (record) => {
    // 跳转到消费记录页面
    message.info(`查看消费记录: ${record.userName}`);
  };

  const handleBatchRecharge = () => {
    batchRechargeModalVisible.value = true;
  };

  const handleExport = () => {
    // TODO: 调用导出API
    message.info('导出功能开发中...');
  };

  const handleBatchAction = ({ key }) => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要操作的账户');
      return;
    }

    switch (key) {
      case 'recharge':
        batchRechargeModalVisible.value = true;
        break;
      case 'freeze':
        batchOperateAccounts('freeze', '冻结');
        break;
      case 'unfreeze':
        batchOperateAccounts('unfreeze', '解冻');
        break;
      case 'export':
        message.info('批量导出功能开发中...');
        break;
    }
  };

  const handleMoreAction = (action, record) => {
    currentAccount.value = record;

    switch (action) {
      case 'freeze':
        operateAccount('freeze', '冻结', record);
        break;
      case 'unfreeze':
        operateAccount('unfreeze', '解冻', record);
        break;
      case 'resetPassword':
        resetAccountPassword(record);
        break;
      case 'edit':
        formMode.value = 'edit';
        formModalVisible.value = true;
        break;
      case 'exportRecords':
        message.info(`导出 ${record.userName} 的消费记录`);
        break;
    }
  };

  const operateAccount = (action, actionText, record) => {
    Modal.confirm({
      title: `${actionText}账户确认`,
      content: `确定要${actionText}账户 ${record.userName} 吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          // TODO: 调用后端API
          message.success(`${actionText}成功`);
          queryTableData();
        } catch (error) {
          message.error(`${actionText}失败`);
        }
      },
    });
  };

  const batchOperateAccounts = (action, actionText) => {
    Modal.confirm({
      title: `批量${actionText}确认`,
      content: `确定要批量${actionText}选中的 ${selectedRowKeys.value.length} 个账户吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          // TODO: 调用后端API
          message.success(`批量${actionText}成功`);
          selectedRowKeys.value = [];
          queryTableData();
        } catch (error) {
          message.error(`批量${actionText}失败`);
        }
      },
    });
  };

  const resetAccountPassword = (record) => {
    Modal.confirm({
      title: '重置密码确认',
      content: `确定要重置账户 ${record.userName} 的密码吗？新密码将发送到用户手机。`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          // TODO: 调用后端API
          message.success('密码重置成功，新密码已发送');
        } catch (error) {
          message.error('密码重置失败');
        }
      },
    });
  };

  const handleFormSuccess = () => {
    formModalVisible.value = false;
    queryTableData();
    loadStatsData();
    message.success(formMode.value === 'add' ? '添加账户成功' : '编辑账户成功');
  };

  const handleRechargeSuccess = () => {
    rechargeModalVisible.value = false;
    queryTableData();
    loadStatsData();
    message.success('充值成功');
  };

  const handleBatchRechargeSuccess = () => {
    batchRechargeModalVisible.value = false;
    selectedRowKeys.value = [];
    queryTableData();
    loadStatsData();
    message.success('批量充值成功');
  };

  // ----------------------- 工具方法 -----------------------
  const getAccountTypeText = (type) => {
    const typeMap = {
      EMPLOYEE: '员工',
      STUDENT: '学生',
      TEMPORARY: '临时',
    };
    return typeMap[type] || type;
  };

  const getAccountTypeColor = (type) => {
    const colorMap = {
      EMPLOYEE: 'blue',
      STUDENT: 'green',
      TEMPORARY: 'orange',
    };
    return colorMap[type] || 'default';
  };

  const getStatusText = (status) => {
    const statusMap = {
      ACTIVE: '正常',
      FROZEN: '冻结',
      DISABLED: '禁用',
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status) => {
    const colorMap = {
      ACTIVE: 'success',
      FROZEN: 'warning',
      DISABLED: 'error',
    };
    return colorMap[status] || 'default';
  };

  const getStatusIcon = (status) => {
    const iconMap = {
      ACTIVE: UserOutlined,
      FROZEN: LockOutlined,
      DISABLED: UnlockOutlined,
    };
    return iconMap[status] || UserOutlined;
  };
</script>

<style lang="less" scoped>
  .consume-account-page {
    padding: 24px;
    background: #f5f5f5;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .page-title {
        h1 {
          margin: 0 0 8px 0;
          font-size: 28px;
          font-weight: 700;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 16px;
        }
      }
    }

    .stats-cards {
      margin-bottom: 24px;

      .stat-card {
        background: white;
        border-radius: 12px;
        padding: 24px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
        border: 1px solid #f0f0f0;
        display: flex;
        align-items: center;
        gap: 16px;
        height: 100%;

        .stat-card-icon {
          width: 64px;
          height: 64px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32px;
          color: white;
          flex-shrink: 0;
        }

        .stat-card-content {
          flex: 1;

          .stat-card-value {
            font-size: 28px;
            font-weight: 700;
            color: #262626;
            line-height: 1.2;
            margin-bottom: 4px;
          }

          .stat-card-title {
            font-size: 14px;
            color: #8c8c8c;
            font-weight: 500;
          }
        }
      }
    }

    .search-card {
      margin-bottom: 16px;
    }

    .table-title-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;
      font-weight: 600;
    }

    .account-info {
      .account-name {
        font-weight: 600;
        color: #262626;
        margin-bottom: 4px;
      }

      .account-details {
        font-size: 12px;
        color: #8c8c8c;

        .employee-no,
        .phone {
          color: #595959;
        }
      }
    }

    .balance-info {
      .balance-amount {
        font-weight: 600;
        color: #52c41a;
        font-size: 16px;
        margin-bottom: 4px;
      }

      .freeze-amount {
        font-size: 12px;
        color: #faad14;
      }
    }

    // 响应式布局
    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .stats-cards {
        .stat-card {
          padding: 16px;

          .stat-card-icon {
            width: 48px;
            height: 48px;
            font-size: 24px;
          }

          .stat-card-content {
            .stat-card-value {
              font-size: 24px;
            }
          }
        }
      }
    }
  }
</style>