<!--
  * 交易详情弹窗组件
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    v-model:visible="visible"
    title="交易详情"
    width="700px"
    :footer="null"
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered v-if="transactionDetail">
        <a-descriptions-item label="交易流水号" :span="2">
          {{ transactionDetail.transactionNo }}
        </a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ transactionDetail.userId }}</a-descriptions-item>
        <a-descriptions-item label="设备ID">{{ transactionDetail.deviceId }}</a-descriptions-item>
        <a-descriptions-item label="消费金额">
          <span style="color: #ff4d4f; font-weight: 600">¥{{ formatAmount(transactionDetail.amount) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="消费模式">
          {{ getConsumeModeText(transactionDetail.consumeMode) }}
        </a-descriptions-item>
        <a-descriptions-item label="交易状态">
          <a-tag :color="getStatusColor(transactionDetail.status)">
            {{ getStatusText(transactionDetail.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="账户余额（交易后）">
          <span style="color: #52c41a">¥{{ formatAmount(transactionDetail.balanceAfter) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="交易时间" :span="2">
          {{ formatDateTime(transactionDetail.transactionTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">
          {{ transactionDetail.remark || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-spin>
  </a-modal>
</template>

<script setup>
  import { ref, watch } from 'vue';
  import { consumeApi } from '/@/api/business/consume/consume-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    transactionNo: {
      type: String,
      default: null,
    },
  });

  const emit = defineEmits(['update:visible']);

  const loading = ref(false);
  const transactionDetail = ref(null);

  // 监听props变化
  watch(
    () => props.visible,
    (val) => {
      if (val && props.transactionNo) {
        loadTransactionDetail();
      }
    }
  );

  // 加载交易详情
  const loadTransactionDetail = async () => {
    if (!props.transactionNo) return;

    loading.value = true;
    try {
      const result = await consumeApi.getTransactionDetail(props.transactionNo);
      if (result.code === 200 && result.data) {
        transactionDetail.value = result.data;
      }
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      loading.value = false;
    }
  };

  // 格式化金额
  const formatAmount = (amount) => {
    if (!amount) return '0.00';
    return Number(amount).toFixed(2);
  };

  // 格式化日期时间
  const formatDateTime = (datetime) => {
    if (!datetime) return '-';
    return datetime;
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
</script>

<style lang="less" scoped>
</style>

