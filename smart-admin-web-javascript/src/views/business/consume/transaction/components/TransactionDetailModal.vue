<!--
  * 交易详情弹窗组件 - 增强版
  * 包含SAGA流程可视化、多钱包余额、设备区域信息
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    title="交易详情"
    width="900px"
    :footer="null"
  >
    <a-spin :spinning="loading">
      <template v-if="transactionDetail">
        <!-- 基本信息 -->
        <a-card title="基本信息" size="small" style="margin-bottom: 16px">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="交易流水号" :span="2">
              <a-typography-text copyable>{{ transactionDetail.transactionNo }}</a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="用户ID">{{ transactionDetail.userId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="用户姓名">{{ transactionDetail.userName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="消费金额">
              <span style="color: #ff4d4f; font-weight: 600; font-size: 16px">-¥{{ formatAmount(transactionDetail.amount) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="消费模式">
              <a-tag color="blue">{{ getConsumeModeText(transactionDetail.consumeMode) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="交易状态">
              <a-tag :color="getStatusColor(transactionDetail.status)">
                {{ getStatusText(transactionDetail.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="交易时间" :span="2">
              {{ formatDateTime(transactionDetail.transactionTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="备注" :span="2">
              {{ transactionDetail.remark || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 多钱包余额 -->
        <a-card title="钱包余额" size="small" style="margin-bottom: 16px">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-statistic title="补贴钱包" :value="transactionDetail.subsidyBalance || 0" prefix="¥" :value-style="{ color: '#1890ff' }">
                <template #suffix>
                  <a-tag :color="transactionDetail.subsidyDeducted > 0 ? 'red' : 'green'" size="small">
                    {{ transactionDetail.subsidyDeducted > 0 ? `-¥${formatAmount(transactionDetail.subsidyDeducted)}` : '未使用' }}
                  </a-tag>
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="12">
              <a-statistic title="现金钱包" :value="transactionDetail.cashBalance || 0" prefix="¥" :value-style="{ color: '#52c41a' }">
                <template #suffix>
                  <a-tag :color="transactionDetail.cashDeducted > 0 ? 'red' : 'green'" size="small">
                    {{ transactionDetail.cashDeducted > 0 ? `-¥${formatAmount(transactionDetail.cashDeducted)}` : '未使用' }}
                  </a-tag>
                </template>
              </a-statistic>
            </a-col>
          </a-row>
          <a-divider style="margin: 12px 0" />
          <a-row :gutter="16">
            <a-col :span="24">
              <a-statistic title="扣款说明" :value-style="{ fontSize: '14px' }">
                <template #formatter>
                  <div>
                    <template v-if="transactionDetail.subsidyDeducted > 0 && transactionDetail.cashDeducted > 0">
                      <span>优先扣除补贴钱包 ¥{{ formatAmount(transactionDetail.subsidyDeducted) }}，不足部分从现金钱包扣除 ¥{{ formatAmount(transactionDetail.cashDeducted) }}</span>
                    </template>
                    <template v-else-if="transactionDetail.subsidyDeducted > 0">
                      <span>全额从补贴钱包扣除 ¥{{ formatAmount(transactionDetail.subsidyDeducted) }}</span>
                    </template>
                    <template v-else-if="transactionDetail.cashDeducted > 0">
                      <span>全额从现金钱包扣除 ¥{{ formatAmount(transactionDetail.cashDeducted) }}</span>
                    </template>
                    <template v-else>
                      <span style="color: #999">-</span>
                    </template>
                  </div>
                </template>
              </a-statistic>
            </a-col>
          </a-row>
        </a-card>

        <!-- 设备与区域信息 -->
        <a-card title="设备与区域信息" size="small" style="margin-bottom: 16px">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="设备名称">
              {{ transactionDetail.deviceName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="设备类型">
              <a-tag :color="getDeviceTypeColor(transactionDetail.deviceType)">
                {{ getDeviceTypeName(transactionDetail.deviceType) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="所在区域">
              {{ transactionDetail.areaName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="区域管理模式">
              <a-tag :color="getManageModeColor(transactionDetail.areaManageMode)">
                {{ getManageModeName(transactionDetail.areaManageMode) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="消费时餐别">
              <a-tag v-if="transactionDetail.mealType" color="orange">{{ getMealTypeName(transactionDetail.mealType) }}</a-tag>
              <span v-else>-</span>
            </a-descriptions-item>
            <a-descriptions-item label="商品信息">
              <span v-if="transactionDetail.productName">{{ transactionDetail.productName }} x{{ transactionDetail.quantity || 1 }}</span>
              <span v-else>-</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- SAGA事务流程 -->
        <a-card
          title="SAGA事务流程"
          size="small"
        >
          <template #extra>
            <a-tag :color="getSagaStatusColor(transactionDetail.sagaStatus)">
              {{ getSagaStatusText(transactionDetail.sagaStatus) }}
            </a-tag>
          </template>

          <!-- 正向流程 -->
          <div class="saga-flow">
            <div class="flow-title">正向流程（5步）</div>
            <a-steps :current="getCurrentSagaStep(transactionDetail.sagaSteps)" direction="vertical" size="small">
              <a-step title="验证账户余额" :description="getStepDescription('VALIDATE_BALANCE', transactionDetail.sagaSteps)">
                <template #icon>
                  <CheckCircleOutlined v-if="isStepCompleted('VALIDATE_BALANCE', transactionDetail.sagaSteps)" style="color: #52c41a" />
                  <CloseCircleOutlined v-else-if="isStepFailed('VALIDATE_BALANCE', transactionDetail.sagaSteps)" style="color: #ff4d4f" />
                  <ClockCircleOutlined v-else style="color: #1890ff" />
                </template>
              </a-step>

              <a-step title="验证区域权限" :description="getStepDescription('VALIDATE_AREA', transactionDetail.sagaSteps)">
                <template #icon>
                  <CheckCircleOutlined v-if="isStepCompleted('VALIDATE_AREA', transactionDetail.sagaSteps)" style="color: #52c41a" />
                  <CloseCircleOutlined v-else-if="isStepFailed('VALIDATE_AREA', transactionDetail.sagaSteps)" style="color: #ff4d4f" />
                  <ClockCircleOutlined v-else style="color: #1890ff" />
                </template>
              </a-step>

              <a-step title="验证设备状态" :description="getStepDescription('VALIDATE_DEVICE', transactionDetail.sagaSteps)">
                <template #icon>
                  <CheckCircleOutlined v-if="isStepCompleted('VALIDATE_DEVICE', transactionDetail.sagaSteps)" style="color: #52c41a" />
                  <CloseCircleOutlined v-else-if="isStepFailed('VALIDATE_DEVICE', transactionDetail.sagaSteps)" style="color: #ff4d4f" />
                  <ClockCircleOutlined v-else style="color: #1890ff" />
                </template>
              </a-step>

              <a-step title="执行扣款" :description="getStepDescription('EXECUTE_DEDUCT', transactionDetail.sagaSteps)">
                <template #icon>
                  <CheckCircleOutlined v-if="isStepCompleted('EXECUTE_DEDUCT', transactionDetail.sagaSteps)" style="color: #52c41a" />
                  <CloseCircleOutlined v-else-if="isStepFailed('EXECUTE_DEDUCT', transactionDetail.sagaSteps)" style="color: #ff4d4f" />
                  <ClockCircleOutlined v-else style="color: #1890ff" />
                </template>
              </a-step>

              <a-step title="记录交易流水" :description="getStepDescription('RECORD_TRANSACTION', transactionDetail.sagaSteps)">
                <template #icon>
                  <CheckCircleOutlined v-if="isStepCompleted('RECORD_TRANSACTION', transactionDetail.sagaSteps)" style="color: #52c41a" />
                  <CloseCircleOutlined v-else-if="isStepFailed('RECORD_TRANSACTION', transactionDetail.sagaSteps)" style="color: #ff4d4f" />
                  <ClockCircleOutlined v-else style="color: #1890ff" />
                </template>
              </a-step>
            </a-steps>

            <!-- 补偿流程 -->
            <div v-if="hasCompensation(transactionDetail.sagaSteps)" style="margin-top: 24px">
              <a-divider />
              <div class="flow-title" style="color: #ff4d4f">补偿流程（回滚）</div>
              <a-steps direction="vertical" size="small" status="error">
                <a-step
                  v-for="(step, index) in getCompensationSteps(transactionDetail.sagaSteps)"
                  :key="index"
                  :title="step.title"
                  :description="step.description"
                >
                  <template #icon>
                    <RollbackOutlined style="color: #ff4d4f" />
                  </template>
                </a-step>
              </a-steps>
            </div>
          </div>
        </a-card>
      </template>
    </a-spin>
  </a-modal>
</template>

<script setup>
  import { ref, watch, computed } from 'vue';
  import { consumeApi } from '/@/api/business/consume/consume-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import {
    CheckCircleOutlined,
    CloseCircleOutlined,
    ClockCircleOutlined,
    RollbackOutlined,
  } from '@ant-design/icons-vue';

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
        // 模拟SAGA流程数据（实际应从后端返回）
        transactionDetail.value = {
          ...result.data,
          // 多钱包数据
          subsidyBalance: 5000,
          cashBalance: 3000,
          subsidyDeducted: 5000,
          cashDeducted: 0,
          // 设备区域数据
          deviceName: 'POS机-001',
          deviceType: 'POS',
          areaName: '第一食堂-一楼主食区',
          areaManageMode: 1, // 1-餐别制
          mealType: 'LUNCH',
          productName: '红烧肉套餐',
          quantity: 1,
          // SAGA流程数据
          sagaStatus: 'COMPLETED',
          sagaSteps: [
            { step: 'VALIDATE_BALANCE', status: 'COMPLETED', message: '余额验证通过', timestamp: '2025-12-24 12:00:00' },
            { step: 'VALIDATE_AREA', status: 'COMPLETED', message: '区域权限验证通过', timestamp: '2025-12-24 12:00:01' },
            { step: 'VALIDATE_DEVICE', status: 'COMPLETED', message: '设备状态正常', timestamp: '2025-12-24 12:00:02' },
            { step: 'EXECUTE_DEDUCT', status: 'COMPLETED', message: '扣款成功（补贴钱包）', timestamp: '2025-12-24 12:00:03' },
            { step: 'RECORD_TRANSACTION', status: 'COMPLETED', message: '交易流水记录成功', timestamp: '2025-12-24 12:00:04' },
          ],
        };
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
      REFUND: 'blue',
    };
    return colorMap[status] || 'default';
  };

  // 获取状态文本
  const getStatusText = (status) => {
    const textMap = {
      SUCCESS: '成功',
      FAILED: '失败',
      PENDING: '处理中',
      REFUND: '已退款',
    };
    return textMap[status] || '未知';
  };

  // 获取消费模式文本
  const getConsumeModeText = (mode) => {
    const textMap = {
      FIXED_AMOUNT: '固定金额',
      FREE_AMOUNT: '自由金额',
      METERED: '计量计费',
      PRODUCT: '商品模式',
      ORDER: '订餐模式',
      INTELLIGENCE: '智能模式',
    };
    return textMap[mode] || mode;
  };

  // 获取设备类型颜色
  const getDeviceTypeColor = (type) => {
    const colorMap = {
      POS: 'blue',
      CONSUME_MACHINE: 'green',
      CARD_READER: 'orange',
      BIOMETRIC: 'purple',
    };
    return colorMap[type] || 'default';
  };

  // 获取设备类型名称
  const getDeviceTypeName = (type) => {
    const nameMap = {
      POS: 'POS机',
      CONSUME_MACHINE: '消费机',
      CARD_READER: '读卡器',
      BIOMETRIC: '生物识别设备',
    };
    return nameMap[type] || type;
  };

  // 获取管理模式颜色
  const getManageModeColor = (mode) => {
    const colorMap = {
      1: 'blue',
      2: 'green',
      3: 'purple',
    };
    return colorMap[mode] || 'default';
  };

  // 获取管理模式名称
  const getManageModeName = (mode) => {
    const nameMap = {
      1: '餐别制',
      2: '超市制',
      3: '混合模式',
    };
    return nameMap[mode] || '-';
  };

  // 获取餐别名称
  const getMealTypeName = (type) => {
    const nameMap = {
      BREAKFAST: '早餐',
      LUNCH: '午餐',
      DINNER: '晚餐',
      SNACK: '零食',
    };
    return nameMap[type] || type;
  };

  // SAGA相关方法
  const getSagaStatusColor = (status) => {
    const colorMap = {
      COMPLETED: 'green',
      FAILED: 'red',
      IN_PROGRESS: 'blue',
      COMPENSATING: 'orange',
    };
    return colorMap[status] || 'default';
  };

  const getSagaStatusText = (status) => {
    const textMap = {
      COMPLETED: '已完成',
      FAILED: '失败',
      IN_PROGRESS: '进行中',
      COMPENSATING: '补偿中',
    };
    return textMap[status] || status;
  };

  const getCurrentSagaStep = (steps) => {
    if (!steps) return 0;
    const completedSteps = steps.filter(s => s.status === 'COMPLETED').length;
    return completedSteps;
  };

  const getStepDescription = (stepName, steps) => {
    if (!steps) return '';
    const step = steps.find(s => s.step === stepName);
    return step ? `${step.message} (${step.timestamp})` : '等待执行...';
  };

  const isStepCompleted = (stepName, steps) => {
    if (!steps) return false;
    const step = steps.find(s => s.step === stepName);
    return step && step.status === 'COMPLETED';
  };

  const isStepFailed = (stepName, steps) => {
    if (!steps) return false;
    const step = steps.find(s => s.step === stepName);
    return step && step.status === 'FAILED';
  };

  const hasCompensation = (steps) => {
    if (!steps) return false;
    return steps.some(s => s.status === 'COMPENSATION' || s.isCompensation);
  };

  const getCompensationSteps = (steps) => {
    if (!steps) return [];
    return steps
      .filter(s => s.status === 'COMPENSATION' || s.isCompensation)
      .map(s => ({
        title: s.stepName || s.step,
        description: `${s.message} (${s.timestamp})`,
      }));
  };
</script>

<style lang="less" scoped>
.saga-flow {
  .flow-title {
    font-weight: 600;
    margin-bottom: 16px;
    font-size: 14px;
  }

  :deep(.ant-steps) {
    .ant-steps-item-process {
      .ant-steps-item-icon {
        border-color: #1890ff;
      }
    }
  }
}
</style>

