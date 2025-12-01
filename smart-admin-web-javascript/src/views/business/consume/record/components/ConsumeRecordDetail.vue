<template>
  <a-modal
    v-model:open="visible"
    title="消费记录详情"
    width="800px"
    :footer="null"
    @cancel="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <a-spin size="large" />
    </div>

    <div v-else-if="record" class="detail-container">
      <!-- 基本信息 -->
      <a-card class="detail-card" title="基本信息">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="消费单号">
            {{ record.orderNo }}
          </a-descriptions-item>
          <a-descriptions-item label="消费状态">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="用户ID">
            {{ record.userId }}
          </a-descriptions-item>
          <a-descriptions-item label="用户名">
            {{ record.userName || '未设置' }}
          </a-descriptions-item>
          <a-descriptions-item label="设备名称">
            {{ record.deviceName }}
          </a-descriptions-item>
          <a-descriptions-item label="消费模式">
            <a-tag :color="getConsumeModeColor(record.consumeMode)">
              {{ getConsumeModeText(record.consumeMode) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="消费时间">
            {{ formatDateTime(record.consumeTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatDateTime(record.createTime) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 金额信息 -->
      <a-card class="detail-card" title="金额信息">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="消费金额">
            <span class="amount">¥{{ formatMoney(record.consumeAmount) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="优惠金额">
            <span class="amount discount">
              {{ record.discountAmount ? '-¥' + formatMoney(record.discountAmount) : '¥0.00' }}
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="实付金额">
            <span class="amount actual">¥{{ formatMoney(record.actualAmount) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="消费前余额">
            <span class="amount">¥{{ formatMoney(record.balanceBefore) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="消费后余额">
            <span class="amount">¥{{ formatMoney(record.balanceAfter) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="支付方式">
            {{ getPaymentMethodText(record.paymentMethod) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 扩展信息（根据消费模式显示不同内容） -->
      <a-card
        v-if="hasExtendedInfo"
        class="detail-card"
        :title="getExtendedInfoTitle()"
      >
        <!-- 固定金额模式 -->
        <div v-if="record.consumeMode === 'FIXED_AMOUNT'" class="extended-info">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="固定档位">
              ¥{{ record.fixedAmount || '0.00' }}
            </a-descriptions-item>
            <a-descriptions-item label="档位描述">
              {{ record.fixedAmountDesc || '标准档位' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 自由金额模式 -->
        <div v-else-if="record.consumeMode === 'FREE_AMOUNT'" class="extended-info">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="输入金额">
              ¥{{ formatMoney(record.inputAmount) }}
            </a-descriptions-item>
            <a-descriptions-item label="金额来源">
              {{ record.amountSource || '手动输入' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 计量计费模式 -->
        <div v-else-if="record.consumeMode === 'METERING'" class="extended-info">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="计量单位">
              {{ record.meteringUnit }}
            </a-descriptions-item>
            <a-descriptions-item label="用量">
              {{ record.usageAmount }} {{ record.meteringUnit }}
            </a-descriptions-item>
            <a-descriptions-item label="单价">
              ¥{{ formatMoney(record.unitPrice) }}/{{ record.meteringUnit }}
            </a-descriptions-item>
            <a-descriptions-item label="上次读数">
              {{ record.lastReading }}
            </a-descriptions-item>
            <a-descriptions-item label="当前读数">
              {{ record.currentReading }}
            </a-descriptions-item>
            <a-descriptions-item label="计量类型">
              {{ getMeteringTypeText(record.meteringType) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 商品扫码模式 -->
        <div v-else-if="record.consumeMode === 'PRODUCT_SCAN'" class="extended-info">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="商品编码">
              {{ record.productCode }}
            </a-descriptions-item>
            <a-descriptions-item label="商品名称">
              {{ record.productName }}
            </a-descriptions-item>
            <a-descriptions-item label="商品单价">
              ¥{{ formatMoney(record.productPrice) }}
            </a-descriptions-item>
            <a-descriptions-item label="购买数量">
              {{ record.quantity }}
            </a-descriptions-item>
            <a-descriptions-item label="商品分类">
              {{ record.productCategory }}
            </a-descriptions-item>
            <a-descriptions-item label="库存状态">
              <a-tag :color="record.inStock ? 'success' : 'error'">
                {{ record.inStock ? '有库存' : '无库存' }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 订餐模式 -->
        <div v-else-if="record.consumeMode === 'ORDERING'" class="extended-info">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="餐别">
              {{ getMealTypeText(record.mealType) }}
            </a-descriptions-item>
            <a-descriptions-item label="套餐名称">
              {{ record.packageName }}
            </a-descriptions-item>
            <a-descriptions-item label="菜品清单">
              <div class="dish-list">
                <a-tag
                  v-for="dish in parseDishList(record.dishList)"
                  :key="dish.name"
                  color="blue"
                >
                  {{ dish.name }} x{{ dish.quantity }}
                </a-tag>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="取餐时间">
              {{ formatDateTime(record.pickupTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="取餐地点">
              {{ record.pickupLocation || '默认餐厅' }}
            </a-descriptions-item>
            <a-descriptions-item label="订餐状态">
              <a-tag :color="getOrderingStatusColor(record.orderingStatus)">
                {{ getOrderingStatusText(record.orderingStatus) }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 智能模式 -->
        <div v-else-if="record.consumeMode === 'SMART'" class="extended-info">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="智能算法">
              {{ getSmartAlgorithmText(record.smartAlgorithm) }}
            </a-descriptions-item>
            <a-descriptions-item label="推荐金额">
              ¥{{ formatMoney(record.recommendedAmount) }}
            </a-descriptions-item>
            <a-descriptions-item label="用户习惯">
              {{ record.userHabit || '首次消费' }}
            </a-descriptions-item>
            <a-descriptions-item label="置信度">
              {{ (record.confidence * 100).toFixed(1) }}%
            </a-descriptions-item>
            <a-descriptions-item label="AI决策">
              <a-tag :color="getAIDecisionColor(record.aiDecision)">
                {{ getAIDecisionText(record.aiDecision) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="风险等级">
              <a-tag :color="getRiskLevelColor(record.riskLevel)">
                {{ getRiskLevelText(record.riskLevel) }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </a-card>

      <!-- 备注信息 -->
      <a-card v-if="record.remark" class="detail-card" title="备注信息">
        <p class="remark-text">{{ record.remark }}</p>
      </a-card>

      <!-- 操作日志 -->
      <a-card class="detail-card" title="操作日志">
        <a-timeline>
          <a-timeline-item
            v-for="log in operationLogs"
            :key="log.logId"
            :color="getLogColor(log.operationType)"
          >
            <div class="log-item">
              <div class="log-header">
                <span class="log-operation">{{ getOperationText(log.operationType) }}</span>
                <span class="log-time">{{ formatDateTime(log.createTime) }}</span>
              </div>
              <div class="log-user">操作人：{{ log.operatorName || log.operatorId }}</div>
              <div v-if="log.remark" class="log-remark">备注：{{ log.remark }}</div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <a-space>
          <a-button @click="handlePrint">
            <template #icon><PrinterOutlined /></template>
            打印小票
          </a-button>
          <a-button @click="handleExport">
            <template #icon><ExportOutlined /></template>
            导出记录
          </a-button>
          <a-button
            v-if="record.status === 'SUCCESS' && hasRefundPermission"
            danger
            @click="handleRefund"
          >
            <template #icon><RollbackOutlined /></template>
            申请退款
          </a-button>
          <a-button @click="handleClose">关闭</a-button>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  PrinterOutlined,
  ExportOutlined,
  RollbackOutlined
} from '@ant-design/icons-vue'
import { consumeRecordApi } from '@/api/business/consume/record-api'
import { useUserStore } from '@/store/modules/user'
import dayjs from 'dayjs'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  record: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:visible', 'refresh'])

// 权限检查
const userStore = useUserStore()
const hasRefundPermission = computed(() => {
  return userStore.hasPermission('consume:refund')
})

// 响应式数据
const loading = ref(false)
const operationLogs = ref([])

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const hasExtendedInfo = computed(() => {
  return props.record && [
    'FIXED_AMOUNT',
    'FREE_AMOUNT',
    'METERING',
    'PRODUCT_SCAN',
    'ORDERING',
    'SMART'
  ].includes(props.record.consumeMode)
})

// 监听record变化
watch(() => props.record, (newRecord) => {
  if (newRecord && props.visible) {
    loadOperationLogs(newRecord.recordId)
  }
}, { immediate: true })

// 监听visible变化
watch(() => props.visible, (newVisible) => {
  if (newVisible && props.record) {
    loadOperationLogs(props.record.recordId)
  }
})

// 方法定义
const loadOperationLogs = async (recordId) => {
  if (!recordId) return

  loading.value = true
  try {
    const result = await consumeRecordApi.getOperationLogs(recordId)
    if (result.success) {
      operationLogs.value = result.data || []
    } else {
      console.error('加载操作日志失败:', result.message)
    }
  } catch (error) {
    console.error('加载操作日志异常:', error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  operationLogs.value = []
}

const handlePrint = () => {
  // TODO: 实现打印小票功能
  message.info('打印小票功能开发中')
}

const handleExport = async () => {
  try {
    const result = await consumeRecordApi.exportSingleRecord(props.record.recordId)
    if (result.success) {
      window.open(result.data?.downloadUrl, '_blank')
      message.success('导出成功')
    } else {
      message.error(result.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  }
}

const handleRefund = () => {
  // 触发退款事件，由父组件处理
  emit('refund', props.record)
}

// 工具方法
const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
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

const getExtendedInfoTitle = () => {
  if (!props.record) return ''

  const titleMap = {
    'FIXED_AMOUNT': '固定金额详情',
    'FREE_AMOUNT': '自由金额详情',
    'METERING': '计量计费详情',
    'PRODUCT_SCAN': '商品信息',
    'ORDERING': '订餐详情',
    'SMART': '智能分析'
  }
  return titleMap[props.record.consumeMode] || '详细信息'
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'BALANCE': '余额支付',
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'CARD': '银行卡',
    'CASH': '现金',
    'COMBO': '组合支付'
  }
  return methodMap[method] || method
}

const getMeteringTypeText = (type) => {
  const typeMap = {
    'ELECTRICITY': '电力',
    'WATER': '水费',
    'GAS': '燃气',
    'WEIGHT': '重量',
    'VOLUME': '容积',
    'TIME': '时间'
  }
  return typeMap[type] || type
}

const getMealTypeText = (type) => {
  const typeMap = {
    'BREAKFAST': '早餐',
    'LUNCH': '午餐',
    'DINNER': '晚餐',
    'SNACK': '夜宵'
  }
  return typeMap[type] || type
}

const getOrderingStatusText = (status) => {
  const statusMap = {
    'ORDERED': '已下单',
    'PREPARING': '制作中',
    'READY': '待取餐',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getOrderingStatusColor = (status) => {
  const colorMap = {
    'ORDERED': 'blue',
    'PREPARING': 'orange',
    'READY': 'green',
    'COMPLETED': 'success',
    'CANCELLED': 'error'
  }
  return colorMap[status] || 'default'
}

const getSmartAlgorithmText = (algorithm) => {
  const algorithmMap = {
    'RECOMMENDATION': '智能推荐',
    'AUTO_PAYMENT': '自动支付',
    'SMART_SUGGESTION': '智能建议',
    'PATTERN_MATCH': '模式匹配'
  }
  return algorithmMap[algorithm] || algorithm
}

const getAIDecisionText = (decision) => {
  const decisionMap = {
    'APPROVE': '批准',
    'REJECT': '拒绝',
    'MANUAL_REVIEW': '人工审核',
    'REQUIRE_ADDITIONAL_INFO': '需要补充信息'
  }
  return decisionMap[decision] || decision
}

const getAIDecisionColor = (decision) => {
  const colorMap = {
    'APPROVE': 'success',
    'REJECT': 'error',
    'MANUAL_REVIEW': 'warning',
    'REQUIRE_ADDITIONAL_INFO': 'blue'
  }
  return colorMap[decision] || 'default'
}

const getRiskLevelText = (level) => {
  const levelMap = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'CRITICAL': '严重风险'
  }
  return levelMap[level] || level
}

const getRiskLevelColor = (level) => {
  const colorMap = {
    'LOW': 'green',
    'MEDIUM': 'orange',
    'HIGH': 'red',
    'CRITICAL': 'error'
  }
  return colorMap[level] || 'default'
}

const getOperationText = (operationType) => {
  const operationMap = {
    'CREATE': '创建消费',
    'PAY': '支付成功',
    'REFUND': '退款处理',
    'CANCEL': '取消消费',
    'MODIFY': '修改记录',
    'EXPORT': '导出记录'
  }
  return operationMap[operationType] || operationType
}

const getLogColor = (operationType) => {
  const colorMap = {
    'CREATE': 'blue',
    'PAY': 'green',
    'REFUND': 'orange',
    'CANCEL': 'red',
    'MODIFY': 'purple',
    'EXPORT': 'cyan'
  }
  return colorMap[operationType] || 'default'
}

const parseDishList = (dishListStr) => {
  if (!dishListStr) return []

  try {
    return JSON.parse(dishListStr)
  } catch (error) {
    // 如果不是JSON格式，尝试解析为字符串格式
    return dishListStr.split(',').map(item => {
      const [name, quantity] = item.trim().split('x')
      return {
        name: name?.trim() || item.trim(),
        quantity: parseInt(quantity?.trim()) || 1
      }
    })
  }
}
</script>

<style lang="less" scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.detail-container {
  max-height: 70vh;
  overflow-y: auto;

  .detail-card {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .amount {
      font-weight: 600;
      font-size: 16px;

      &.discount {
        color: #ff4d4f;
      }

      &.actual {
        color: #52c41a;
      }
    }

    .extended-info {
      .dish-list {
        .ant-tag {
          margin: 2px 4px 2px 0;
        }
      }
    }

    .remark-text {
      margin: 0;
      line-height: 1.6;
      color: #595959;
    }
  }

  .log-item {
    .log-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .log-operation {
        font-weight: 600;
        color: #262626;
      }

      .log-time {
        color: #8c8c8c;
        font-size: 12px;
      }
    }

    .log-user {
      color: #595959;
      font-size: 12px;
      margin-bottom: 2px;
    }

    .log-remark {
      color: #8c8c8c;
      font-size: 12px;
    }
  }

  .action-buttons {
    margin-top: 24px;
    text-align: right;
  }
}

// 滚动条样式
.detail-container::-webkit-scrollbar {
  width: 6px;
}

.detail-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.detail-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;

  &:hover {
    background: #a8a8a8;
  }
}
</style>