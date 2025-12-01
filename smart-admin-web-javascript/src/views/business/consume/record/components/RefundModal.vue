<template>
  <a-modal
    v-model:open="visible"
    title="申请退款"
    width="600px"
    @ok="handleRefund"
    @cancel="handleCancel"
    :confirm-loading="refunding"
    ok-text="确认退款"
    cancel-text="取消"
  >
    <div v-if="!record" class="no-record">
      <a-empty description="请选择要退款的消费记录" />
    </div>

    <div v-else class="refund-content">
      <!-- 原始消费信息 -->
      <a-card class="original-info" title="原始消费信息" size="small">
        <a-descriptions :column="2" size="small">
          <a-descriptions-item label="消费单号">
            {{ record.orderNo }}
          </a-descriptions-item>
          <a-descriptions-item label="消费时间">
            {{ formatDateTime(record.consumeTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="消费金额">
            <span class="amount">¥{{ formatMoney(record.consumeAmount) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="实付金额">
            <span class="amount actual">¥{{ formatMoney(record.actualAmount) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="消费模式">
            <a-tag :color="getConsumeModeColor(record.consumeMode)">
              {{ getConsumeModeText(record.consumeMode) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="当前余额">
            ¥{{ formatMoney(record.balanceAfter) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 退款表单 -->
      <a-form
        ref="formRef"
        :model="refundForm"
        :rules="formRules"
        layout="vertical"
        class="refund-form"
      >
        <!-- 退款类型 -->
        <a-form-item label="退款类型" name="refundType" required>
          <a-radio-group v-model:value="refundForm.refundType" @change="handleRefundTypeChange">
            <a-radio value="FULL">全额退款</a-radio>
            <a-radio value="PARTIAL">部分退款</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 退款金额 -->
        <a-form-item
          v-if="refundForm.refundType === 'PARTIAL'"
          label="退款金额"
          name="refundAmount"
          required
        >
          <a-input-number
            v-model:value="refundForm.refundAmount"
            :min="0.01"
            :max="record.actualAmount"
            :precision="2"
            placeholder="请输入退款金额"
            style="width: 100%"
            addon-before="¥"
          >
            <template #addonAfter>
              <a-button size="small" @click="setMaxAmount">最大金额</a-button>
            </template>
          </a-input-number>
          <div class="amount-hint">
            可退款金额：¥{{ formatMoney(record.actualAmount) }}
          </div>
        </a-form-item>

        <!-- 退款原因 -->
        <a-form-item label="退款原因" name="refundReason" required>
          <a-select
            v-model:value="refundForm.refundReason"
            placeholder="请选择退款原因"
            show-search
            allow-clear
          >
            <a-select-option value="DEVICE_ERROR">设备故障</a-select-option>
            <a-select-option value="SERVICE_ISSUE">服务问题</a-select-option>
            <a-select-option value="PRODUCT_QUALITY">商品质量问题</a-select-option>
            <a-select-option value="USER_REQUEST">用户主动申请</a-select-option>
            <a-select-option value="SYSTEM_ERROR">系统错误</a-select-option>
            <a-select-option value="OTHER">其他原因</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 详细说明 -->
        <a-form-item label="详细说明" name="refundDescription">
          <a-textarea
            v-model:value="refundForm.refundDescription"
            :rows="3"
            placeholder="请详细描述退款原因（选填）"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <!-- 退款方式 -->
        <a-form-item label="退款方式" name="refundMethod" required>
          <a-radio-group v-model:value="refundForm.refundMethod">
            <a-radio value="RETURN_BALANCE">退回余额</a-radio>
            <a-radio value="CASH">现金退款</a-radio>
            <a-radio value="BANK_TRANSFER">银行转账</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 银行转账信息 -->
        <div v-if="refundForm.refundMethod === 'BANK_TRANSFER'" class="bank-info">
          <a-form-item label="收款人姓名" name="bankAccountName" required>
            <a-input
              v-model:value="refundForm.bankAccountName"
              placeholder="请输入收款人姓名"
            />
          </a-form-item>
          <a-form-item label="银行卡号" name="bankAccountNumber" required>
            <a-input
              v-model:value="refundForm.bankAccountNumber"
              placeholder="请输入银行卡号"
              :maxlength="19"
            />
          </a-form-item>
          <a-form-item label="开户银行" name="bankName" required>
            <a-input
              v-model:value="refundForm.bankName"
              placeholder="请输入开户银行"
            />
          </a-form-item>
        </div>

        <!-- 紧急程度 -->
        <a-form-item label="紧急程度" name="urgencyLevel">
          <a-select v-model:value="refundForm.urgencyLevel" placeholder="请选择紧急程度">
            <a-select-option value="LOW">普通</a-select-option>
            <a-select-option value="MEDIUM">紧急</a-select-option>
            <a-select-option value="HIGH">特急</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 联系方式 -->
        <a-form-item label="联系电话" name="contactPhone">
          <a-input
            v-model:value="refundForm.contactPhone"
            placeholder="请输入联系电话（选填）"
            :maxlength="11"
          />
        </a-form-item>

        <!-- 附件上传 -->
        <a-form-item label="相关凭证">
          <a-upload
            v-model:file-list="refundForm.attachments"
            :before-upload="beforeUpload"
            @remove="handleRemove"
            multiple
            :max-count="5"
          >
            <a-button>
              <template #icon><UploadOutlined /></template>
              上传凭证
            </a-button>
          </a-upload>
          <div class="upload-hint">
            支持jpg、png、pdf格式，单个文件不超过5MB，最多上传5个文件
          </div>
        </a-form-item>

        <!-- 退款确认 -->
        <a-form-item>
          <a-checkbox v-model:checked="refundForm.confirmed">
            我已确认以上退款信息无误，并同意相关退款政策
          </a-checkbox>
        </a-form-item>
      </a-form>

      <!-- 退款预览 -->
      <div v-if="refundForm.refundType && refundForm.refundAmount" class="refund-preview">
        <a-alert
          type="info"
          show-icon
        >
          <template #message>
            退款预览
          </template>
          <template #description>
            <div class="preview-item">
              <span class="preview-label">退款类型：</span>
              <span class="preview-value">{{ refundForm.refundType === 'FULL' ? '全额退款' : '部分退款' }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">退款金额：</span>
              <span class="preview-value amount">¥{{ formatMoney(getRefundAmount()) }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">退款后余额：</span>
              <span class="preview-value">¥{{ formatMoney(getBalanceAfterRefund()) }}</span>
            </div>
          </template>
        </a-alert>
      </div>

      <!-- 退款政策 -->
      <a-collapse ghost class="refund-policy">
        <a-collapse-panel key="policy" header="退款政策说明">
          <div class="policy-content">
            <h4>退款条件</h4>
            <ul>
              <li>消费成功后24小时内可申请全额退款</li>
              <li>消费成功后7天内可申请部分退款</li>
              <li>设备故障或系统错误可全额退款，不受时间限制</li>
              <li>商品质量问题需提供相关凭证</li>
            </ul>

            <h4>退款处理</h4>
            <ul>
              <li>退回余额实时到账</li>
              <li>现金退款需到现场办理</li>
              <li>银行转账需1-3个工作日到账</li>
              <li>紧急退款申请将在2小时内处理</li>
            </ul>

            <h4>注意事项</h4>
            <ul>
              <li>每笔消费只能申请一次退款</li>
              <li>部分退款后不可再次申请退款</li>
              <li>恶意退款将被记录并影响信用等级</li>
            </ul>
          </div>
        </a-collapse-panel>
      </a-collapse>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { consumeRecordApi } from '@/api/business/consume/record-api'
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
const emit = defineEmits(['update:visible', 'success'])

// 表单引用
const formRef = ref()

// 响应式数据
const refunding = ref(false)

// 退款表单
const refundForm = reactive({
  refundType: 'FULL',
  refundAmount: undefined,
  refundReason: undefined,
  refundDescription: '',
  refundMethod: 'RETURN_BALANCE',
  bankAccountName: '',
  bankAccountNumber: '',
  bankName: '',
  urgencyLevel: 'LOW',
  contactPhone: '',
  attachments: [],
  confirmed: false
})

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 表单验证规则
const formRules = computed(() => {
  const rules = {
    refundType: [
      { required: true, message: '请选择退款类型' }
    ],
    refundReason: [
      { required: true, message: '请选择退款原因' }
    ],
    refundMethod: [
      { required: true, message: '请选择退款方式' }
    ],
    confirmed: [
      {
        validator: (rule, value) => {
          if (!value) {
            return Promise.reject(new Error('请确认退款信息'))
          }
          return Promise.resolve()
        }
      }
    ]
  }

  // 部分退款时需要验证退款金额
  if (refundForm.refundType === 'PARTIAL') {
    rules.refundAmount = [
      { required: true, message: '请输入退款金额' },
      {
        validator: (rule, value) => {
          if (!value || value <= 0) {
            return Promise.reject(new Error('退款金额必须大于0'))
          }
          if (value > props.record?.actualAmount) {
            return Promise.reject(new Error('退款金额不能超过实付金额'))
          }
          return Promise.resolve()
        }
      }
    ]
  }

  // 银行转账时需要验证银行信息
  if (refundForm.refundMethod === 'BANK_TRANSFER') {
    rules.bankAccountName = [
      { required: true, message: '请输入收款人姓名' }
    ]
    rules.bankAccountNumber = [
      { required: true, message: '请输入银行卡号' },
      {
        pattern: /^\d{16,19}$/,
        message: '请输入正确的银行卡号'
      }
    ]
    rules.bankName = [
      { required: true, message: '请输入开户银行' }
    ]
  }

  return rules
})

// 方法定义
const handleRefundTypeChange = () => {
  if (refundForm.refundType === 'FULL') {
    refundForm.refundAmount = props.record?.actualAmount
  } else {
    refundForm.refundAmount = undefined
  }
}

const setMaxAmount = () => {
  refundForm.refundAmount = props.record?.actualAmount
}

const getRefundAmount = () => {
  if (refundForm.refundType === 'FULL') {
    return props.record?.actualAmount || 0
  }
  return refundForm.refundAmount || 0
}

const getBalanceAfterRefund = () => {
  const currentBalance = props.record?.balanceAfter || 0
  const refundAmount = getRefundAmount()
  return currentBalance + refundAmount
}

const beforeUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'application/pdf'].includes(file.type)
  if (!isValidType) {
    message.error('只能上传 JPG、PNG、PDF 格式的文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('文件大小不能超过 5MB！')
    return false
  }
  return false // 阻止自动上传
}

const handleRemove = (file) => {
  const index = refundForm.attachments.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    refundForm.attachments.splice(index, 1)
  }
}

const handleRefund = async () => {
  try {
    await formRef.value.validate()

    refunding.value = true

    // 构建退款参数
    const refundParams = {
      recordId: props.record.recordId,
      refundType: refundForm.refundType,
      refundAmount: getRefundAmount(),
      refundReason: refundForm.refundReason,
      refundDescription: refundForm.refundDescription,
      refundMethod: refundForm.refundMethod,
      urgencyLevel: refundForm.urgencyLevel,
      contactPhone: refundForm.contactPhone
    }

    // 添加银行转账信息
    if (refundForm.refundMethod === 'BANK_TRANSFER') {
      refundParams.bankAccountName = refundForm.bankAccountName
      refundParams.bankAccountNumber = refundForm.bankAccountNumber
      refundParams.bankName = refundForm.bankName
    }

    // 添加附件信息
    if (refundForm.attachments.length > 0) {
      refundParams.attachments = refundForm.attachments.map(file => ({
        uid: file.uid,
        name: file.name,
        size: file.size,
        type: file.type,
        // 这里应该包含文件上传后的URL，暂时使用originFileObj
        file: file.originFileObj
      }))
    }

    // 调用退款API
    const result = await consumeRecordApi.applyRefund(refundParams)

    if (result.success) {
      message.success('退款申请提交成功')
      visible.value = false
      emit('success', result.data)
      resetForm()
    } else {
      message.error(result.message || '退款申请失败')
    }

  } catch (error) {
    console.error('退款申请失败:', error)
    message.error('退款申请失败')
  } finally {
    refunding.value = false
  }
}

const handleCancel = () => {
  visible.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(refundForm, {
    refundType: 'FULL',
    refundAmount: undefined,
    refundReason: undefined,
    refundDescription: '',
    refundMethod: 'RETURN_BALANCE',
    bankAccountName: '',
    bankAccountNumber: '',
    bankName: '',
    urgencyLevel: 'LOW',
    contactPhone: '',
    attachments: [],
    confirmed: false
  })

  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
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

// 监听record变化
watch(() => props.record, (newRecord) => {
  if (newRecord && props.visible) {
    // 重置表单
    resetForm()
    // 如果是全额退款，设置默认金额
    if (refundForm.refundType === 'FULL') {
      refundForm.refundAmount = newRecord.actualAmount
    }
  }
}, { immediate: true })
</script>

<style lang="less" scoped>
.no-record {
  text-align: center;
  padding: 40px 0;
}

.refund-content {
  .original-info {
    margin-bottom: 24px;

    .amount {
      font-weight: 600;
      color: #262626;

      &.actual {
        color: #1890ff;
      }
    }
  }

  .refund-form {
    margin-bottom: 24px;

    .amount-hint {
      color: #8c8c8c;
      font-size: 12px;
      margin-top: 4px;
    }

    .bank-info {
      padding: 16px;
      background-color: #fafafa;
      border-radius: 6px;
      margin: 16px 0;
    }

    .upload-hint {
      color: #8c8c8c;
      font-size: 12px;
      margin-top: 4px;
      line-height: 1.4;
    }
  }

  .refund-preview {
    margin-bottom: 24px;

    .preview-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .preview-label {
        color: #595959;
      }

      .preview-value {
        font-weight: 600;

        &.amount {
          color: #ff4d4f;
          font-size: 16px;
        }
      }
    }
  }

  .refund-policy {
    .policy-content {
      h4 {
        margin: 16px 0 8px 0;
        color: #262626;
        font-size: 14px;
        font-weight: 600;

        &:first-child {
          margin-top: 0;
        }
      }

      ul {
        margin: 0 0 16px 0;
        padding-left: 20px;

        li {
          margin-bottom: 4px;
          color: #595959;
          font-size: 13px;
          line-height: 1.5;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .refund-content {
    .refund-form {
      .bank-info {
        padding: 12px;
      }
    }
  }
}
</style>