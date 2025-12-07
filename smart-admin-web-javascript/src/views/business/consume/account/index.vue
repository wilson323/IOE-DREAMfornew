<!--
  * 消费账户管理
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="account-management-page">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row">
          <a-form-item label="关键词" class="smart-query-form-item">
            <a-input
              style="width: 200px"
              v-model:value="queryForm.keyword"
              placeholder="账户ID、用户ID、用户姓名"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="账户类别" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.accountKindId"
              style="width: 150px"
              :show-search="true"
              :allow-clear="true"
              placeholder="请选择账户类别"
            >
              <a-select-option v-for="item in accountKindList" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="账户状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择状态"
            >
              <a-select-option :value="1">正常</a-select-option>
              <a-select-option :value="2">冻结</a-select-option>
              <a-select-option :value="3">注销</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryAccountList">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 表格区域 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>账户列表</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            创建账户
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'balance'">
            <span style="color: #52c41a; font-weight: 600">¥{{ formatAmount(record.balance) }}</span>
          </template>
          <template v-else-if="column.key === 'allowanceBalance'">
            <span style="color: #1890ff">¥{{ formatAmount(record.allowanceBalance) }}</span>
          </template>
          <template v-else-if="column.key === 'frozenBalance'">
            <span style="color: #faad14">¥{{ formatAmount(record.frozenBalance) }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-dropdown>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleBalanceOperation(record, 'add')">增加余额</a-menu-item>
                    <a-menu-item @click="handleBalanceOperation(record, 'deduct')">扣减余额</a-menu-item>
                    <a-menu-item @click="handleBalanceOperation(record, 'freeze')">冻结金额</a-menu-item>
                    <a-menu-item @click="handleBalanceOperation(record, 'unfreeze')">解冻金额</a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="handleStatusOperation(record, 'enable')" v-if="record.status !== 1">
                      启用账户
                    </a-menu-item>
                    <a-menu-item @click="handleStatusOperation(record, 'disable')" v-if="record.status === 1">
                      禁用账户
                    </a-menu-item>
                    <a-menu-item @click="handleStatusOperation(record, 'freeze')" v-if="record.status === 1">
                      冻结账户
                    </a-menu-item>
                    <a-menu-item @click="handleStatusOperation(record, 'unfreeze')" v-if="record.status === 2">
                      解冻账户
                    </a-menu-item>
                    <a-menu-item @click="handleStatusOperation(record, 'close')" danger>
                      关闭账户
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑账户弹窗 -->
    <AccountFormModal
      v-model:visible="formModalVisible"
      :form-data="currentAccount"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />

    <!-- 余额操作弹窗 -->
    <BalanceOperationModal
      v-model:visible="balanceModalVisible"
      :account="currentAccount"
      :operation-type="balanceOperationType"
      @success="handleBalanceSuccess"
    />

    <!-- 账户详情弹窗 -->
    <AccountDetailModal
      v-model:visible="detailModalVisible"
      :account-id="currentAccountId"
    />
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { SearchOutlined, ReloadOutlined, PlusOutlined, DownOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { consumeApi } from '/@/api/business/consume/consume-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import AccountFormModal from './components/AccountFormModal.vue';
  import BalanceOperationModal from './components/BalanceOperationModal.vue';
  import AccountDetailModal from './components/AccountDetailModal.vue';

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    keyword: null,
    accountKindId: null,
    status: null,
  };
  const queryForm = reactive({ ...queryFormState });

  // 表格数据
  const tableData = ref([]);
  const total = ref(0);
  const loading = ref(false);

  // 分页配置
  const pagination = reactive({
    current: 1,
    pageSize: PAGE_SIZE,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: PAGE_SIZE_OPTIONS,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 账户类别列表（TODO: 从后端获取）
  const accountKindList = ref([
    { label: '个人账户', value: 1 },
    { label: '企业账户', value: 2 },
    { label: '临时账户', value: 3 },
  ]);

  // 表格列定义
  const columns = ref([
    {
      title: '账户ID',
      dataIndex: 'id',
      key: 'id',
      width: 120,
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 120,
    },
    {
      title: '账户类别',
      dataIndex: 'accountKindId',
      key: 'accountKindId',
      width: 120,
    },
    {
      title: '账户余额',
      dataIndex: 'balance',
      key: 'balance',
      width: 120,
      align: 'right',
    },
    {
      title: '补贴余额',
      dataIndex: 'allowanceBalance',
      key: 'allowanceBalance',
      width: 120,
      align: 'right',
    },
    {
      title: '冻结余额',
      dataIndex: 'frozenBalance',
      key: 'frozenBalance',
      width: 120,
      align: 'right',
    },
    {
      title: '账户状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
    },
  ]);

  // 弹窗相关
  const formModalVisible = ref(false);
  const balanceModalVisible = ref(false);
  const detailModalVisible = ref(false);
  const isEdit = ref(false);
  const currentAccount = ref(null);
  const currentAccountId = ref(null);
  const balanceOperationType = ref('add');

  // 查询账户列表
  const queryAccountList = async () => {
    loading.value = true;
    try {
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        keyword: queryForm.keyword || undefined,
        accountKindId: queryForm.accountKindId || undefined,
        status: queryForm.status || undefined,
      };

      const result = await consumeApi.pageAccounts(params);
      if (result.code === 200 && result.data) {
        tableData.value = result.data.list || [];
        pagination.total = result.data.total || 0;
        pagination.current = result.data.pageNum || 1;
        pagination.pageSize = result.data.pageSize || PAGE_SIZE;
      } else {
        message.error(result.message || '查询账户列表失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('查询账户列表失败');
    } finally {
      loading.value = false;
    }
  };

  // 重置查询
  const resetQuery = () => {
    Object.assign(queryForm, queryFormState);
    queryAccountList();
  };

  // 表格变化
  const handleTableChange = (pag) => {
    queryForm.pageNum = pag.current;
    queryForm.pageSize = pag.pageSize;
    queryAccountList();
  };

  // 创建账户
  const handleAdd = () => {
    isEdit.value = false;
    currentAccount.value = null;
    formModalVisible.value = true;
  };

  // 编辑账户
  const handleEdit = (record) => {
    isEdit.value = true;
    currentAccount.value = { ...record };
    formModalVisible.value = true;
  };

  // 查看详情
  const handleView = (record) => {
    currentAccountId.value = record.id;
    detailModalVisible.value = true;
  };

  // 余额操作
  const handleBalanceOperation = (record, type) => {
    currentAccount.value = record;
    balanceOperationType.value = type;
    balanceModalVisible.value = true;
  };

  // 状态操作
  const handleStatusOperation = async (record, type) => {
    try {
      let result;
      switch (type) {
        case 'enable':
          result = await consumeApi.enableAccount(record.id);
          break;
        case 'disable':
          result = await consumeApi.disableAccount(record.id);
          break;
        case 'freeze':
          result = await consumeApi.freezeAccountStatus(record.id, '管理员操作');
          break;
        case 'unfreeze':
          result = await consumeApi.unfreezeAccountStatus(record.id);
          break;
        case 'close':
          result = await consumeApi.closeAccount(record.id, '管理员操作');
          break;
      }

      if (result.code === 200) {
        message.success('操作成功');
        queryAccountList();
      } else {
        message.error(result.message || '操作失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('操作失败');
    }
  };

  // 表单成功回调
  const handleFormSuccess = () => {
    queryAccountList();
  };

  // 余额操作成功回调
  const handleBalanceSuccess = () => {
    queryAccountList();
  };

  // 格式化金额
  const formatAmount = (amount) => {
    if (!amount) return '0.00';
    // 如果amount是分，需要转换为元
    if (amount > 1000) {
      return (amount / 100).toFixed(2);
    }
    return Number(amount).toFixed(2);
  };

  // 获取状态颜色
  const getStatusColor = (status) => {
    const colorMap = {
      1: 'green',
      2: 'orange',
      3: 'red',
    };
    return colorMap[status] || 'default';
  };

  // 获取状态文本
  const getStatusText = (status) => {
    const textMap = {
      1: '正常',
      2: '冻结',
      3: '注销',
    };
    return textMap[status] || '未知';
  };

  // 初始化
  onMounted(() => {
    queryAccountList();
  });
</script>

<style lang="less" scoped>
  .account-management-page {
    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

