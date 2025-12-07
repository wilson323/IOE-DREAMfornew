<!--
  * 消费交易管理
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="transaction-management-page">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row">
          <a-form-item label="交易流水号" class="smart-query-form-item">
            <a-input
              style="width: 200px"
              v-model:value="queryForm.transactionNo"
              placeholder="请输入交易流水号"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="用户ID" class="smart-query-form-item">
            <a-input-number
              v-model:value="queryForm.userId"
              placeholder="请输入用户ID"
              style="width: 150px"
              :min="1"
            />
          </a-form-item>

          <a-form-item label="设备ID" class="smart-query-form-item">
            <a-input-number
              v-model:value="queryForm.deviceId"
              placeholder="请输入设备ID"
              style="width: 150px"
              :min="1"
            />
          </a-form-item>

          <a-form-item label="交易时间" class="smart-query-form-item">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              style="width: 240px"
            />
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryTransactionList">
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

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="今日交易笔数"
            :value="statistics.todayCount || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <ShoppingCartOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="今日交易金额"
            :value="statistics.todayAmount || 0"
            prefix="¥"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <DollarOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="今日用户数"
            :value="statistics.todayUserCount || 0"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="平均客单价"
            :value="statistics.avgAmount || 0"
            prefix="¥"
            :value-style="{ color: '#13c2c2' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 表格区域 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>交易记录</span>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="transactionNo"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            <span style="color: #ff4d4f; font-weight: 600">-¥{{ formatAmount(record.amount) }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'consumeMode'">
            <a-tag>{{ getConsumeModeText(record.consumeMode) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 交易详情弹窗 -->
    <TransactionDetailModal
      v-model:visible="detailModalVisible"
      :transaction-no="currentTransactionNo"
    />
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    SearchOutlined,
    ReloadOutlined,
    ShoppingCartOutlined,
    DollarOutlined,
    UserOutlined,
  } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { consumeApi } from '/@/api/business/consume/consume-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import dayjs from 'dayjs';
  import TransactionDetailModal from './components/TransactionDetailModal.vue';

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    transactionNo: null,
    userId: null,
    deviceId: null,
    startTime: null,
    endTime: null,
  };
  const queryForm = reactive({ ...queryFormState });
  const dateRange = ref(null);

  // 统计数据
  const statistics = reactive({
    todayCount: 0,
    todayAmount: 0,
    todayUserCount: 0,
    avgAmount: 0,
  });

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

  // 表格列定义
  const columns = ref([
    {
      title: '交易流水号',
      dataIndex: 'transactionNo',
      key: 'transactionNo',
      width: 200,
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 120,
    },
    {
      title: '设备ID',
      dataIndex: 'deviceId',
      key: 'deviceId',
      width: 120,
    },
    {
      title: '消费金额',
      dataIndex: 'amount',
      key: 'amount',
      width: 120,
      align: 'right',
    },
    {
      title: '消费模式',
      dataIndex: 'consumeMode',
      key: 'consumeMode',
      width: 120,
    },
    {
      title: '交易状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
    },
    {
      title: '交易时间',
      dataIndex: 'transactionTime',
      key: 'transactionTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      fixed: 'right',
    },
  ]);

  // 弹窗相关
  const detailModalVisible = ref(false);
  const currentTransactionNo = ref(null);

  // 监听日期范围变化
  watch(dateRange, (val) => {
    if (val && val.length === 2) {
      queryForm.startTime = dayjs(val[0]).format('YYYY-MM-DD 00:00:00');
      queryForm.endTime = dayjs(val[1]).format('YYYY-MM-DD 23:59:59');
    } else {
      queryForm.startTime = null;
      queryForm.endTime = null;
    }
  });

  // 查询交易列表
  const queryTransactionList = async () => {
    loading.value = true;
    try {
      // TODO: 对接后端接口
      // const result = await consumeApi.queryTransactions(queryForm);
      // if (result.code === 200 && result.data) {
      //   tableData.value = result.data.list || [];
      //   pagination.total = result.data.total || 0;
      // }

      // 模拟数据
      setTimeout(() => {
        tableData.value = [
          {
            transactionNo: 'TXN20250130001',
            userId: 1001,
            deviceId: 2001,
            amount: 15.50,
            consumeMode: 'FIXED_AMOUNT',
            status: 'SUCCESS',
            transactionTime: '2025-01-30 12:30:00',
          },
        ];
        pagination.total = 1;
        loading.value = false;
      }, 300);
    } catch (error) {
      smartSentry.captureError(error);
      message.error('查询交易列表失败');
      loading.value = false;
    }
  };

  // 加载统计数据
  const loadStatistics = async () => {
    try {
      // TODO: 对接后端接口
      // const result = await consumeApi.getRealtimeStatistics();
      // if (result.code === 200 && result.data) {
      //   Object.assign(statistics, result.data);
      // }
    } catch (error) {
      smartSentry.captureError(error);
    }
  };

  // 重置查询
  const resetQuery = () => {
    Object.assign(queryForm, queryFormState);
    dateRange.value = null;
    queryTransactionList();
  };

  // 表格变化
  const handleTableChange = (pag) => {
    queryForm.pageNum = pag.current;
    queryForm.pageSize = pag.pageSize;
    queryTransactionList();
  };

  // 查看详情
  const handleView = (record) => {
    currentTransactionNo.value = record.transactionNo;
    detailModalVisible.value = true;
  };

  // 格式化金额
  const formatAmount = (amount) => {
    if (!amount) return '0.00';
    return Number(amount).toFixed(2);
  };

  // 获取状态颜色
  const getStatusColor = (status) => {
    const colorMap = {
      SUCCESS: 'green',
      FAILED: 'red',
      PENDING: 'orange',
    };
    return colorMap[status] || 'default';
  };

  // 获取状态文本
  const getStatusText = (status) => {
    const textMap = {
      SUCCESS: '成功',
      FAILED: '失败',
      PENDING: '处理中',
    };
    return textMap[status] || '未知';
  };

  // 获取消费模式文本
  const getConsumeModeText = (mode) => {
    const textMap = {
      FIXED_AMOUNT: '固定金额',
      AMOUNT: '金额模式',
      PRODUCT: '商品模式',
      COUNT: '计次模式',
    };
    return textMap[mode] || mode;
  };

  // 初始化
  onMounted(() => {
    queryTransactionList();
    loadStatistics();
  });
</script>

<style lang="less" scoped>
  .transaction-management-page {
    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

