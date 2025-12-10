<!--
  * 账户详情弹窗组件
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    title="账户详情"
    width="700px"
    :footer="null"
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered v-if="accountDetail">
        <a-descriptions-item label="账户ID">{{ accountDetail.id }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ accountDetail.userId }}</a-descriptions-item>
        <a-descriptions-item label="账户类别">{{ getAccountKindName(accountDetail.accountKindId) }}</a-descriptions-item>
        <a-descriptions-item label="账户状态">
          <a-tag :color="getStatusColor(accountDetail.status)">
            {{ getStatusText(accountDetail.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="账户余额">
          <span style="color: #52c41a; font-weight: 600">¥{{ formatAmount(accountDetail.balance) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="补贴余额">
          <span style="color: #1890ff">¥{{ formatAmount(accountDetail.allowanceBalance) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="冻结余额">
          <span style="color: #faad14">¥{{ formatAmount(accountDetail.frozenBalance) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="可用余额">
          <span style="color: #52c41a; font-weight: 600">
            ¥{{ formatAmount((accountDetail.balance || 0) - (accountDetail.frozenBalance || 0)) }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(accountDetail.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间" :span="2">
          {{ formatDateTime(accountDetail.updateTime) }}
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
    accountId: {
      type: Number,
      default: null,
    },
  });

  const emit = defineEmits(['update:visible']);

  const loading = ref(false);
  const accountDetail = ref(null);

  // 监听props变化
  watch(
    () => props.visible,
    (val) => {
      if (val && props.accountId) {
        loadAccountDetail();
      }
    }
  );

  // 加载账户详情
  const loadAccountDetail = async () => {
    if (!props.accountId) return;

    loading.value = true;
    try {
      const result = await consumeApi.getAccountDetail(props.accountId);
      if (result.code === 200 && result.data) {
        accountDetail.value = result.data;
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
    if (amount > 1000) {
      return (amount / 100).toFixed(2);
    }
    return Number(amount).toFixed(2);
  };

  // 格式化日期时间
  const formatDateTime = (datetime) => {
    if (!datetime) return '-';
    return datetime;
  };

  // 获取账户类别名称
  const getAccountKindName = (kindId) => {
    const nameMap = {
      1: '个人账户',
      2: '企业账户',
      3: '临时账户',
    };
    return nameMap[kindId] || '未知';
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
</script>

<style lang="less" scoped>
</style>

