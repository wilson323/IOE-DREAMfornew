<!--
 * 账户充值弹窗组件
 * 企业级消费系统账户充值功能
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    title="账户充值"
    width="600px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="submitLoading"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <!-- 账户信息 -->
      <a-divider orientation="left">账户信息</a-divider>

      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="账户编号">{{ accountData.accountNo }}</a-descriptions-item>
        <a-descriptions-item label="用户姓名">{{ accountData.userName }}</a-descriptions-item>
        <a-descriptions-item label="当前余额">
          <span :class="{ 'negative-balance': accountData.balance < 0 }">
            ¥{{ formatMoney(accountData.balance) }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="账户类型">
          <a-tag :color="getAccountTypeColor(accountData.accountType)">
            {{ getAccountTypeText(accountData.accountType) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <!-- 充值信息 -->
      <a-divider orientation="left">充值信息</a-divider>

      <a-form-item label="充值方式" name="rechargeType">
        <a-select
          v-model:value="formData.rechargeType"
          placeholder="请选择充值方式"
          @change="handleRechargeTypeChange"
        >
          <a-select-option value="CASH">现金充值</a-select-option>
          <a-select-option value="BANK_TRANSFER">银行转账</a-select-option>
          <a-select-option value="WECHAT">微信支付</a-select-option>
          <a-select-option value="ALIPAY">支付宝</a-select-option>
          <a-select-option value="CARD">刷卡充值</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="充值金额" name="rechargeAmount">
        <a-input-number
          v-model:value="formData.rechargeAmount"
          placeholder="请输入充值金额"
          style="width: 100%"
          :min="0"
          :max="999999.99"
          :precision="2"
          addon-before="¥"
        />
      </a-form-item>

      <a-form-item label="赠送金额" name="giftAmount" v-if="showGiftAmount">
        <a-input-number
          v-model:value="formData.giftAmount"
          placeholder="请输入赠送金额"
          style="width: 100%"
          :min="0"
          :max="9999.99"
          :precision="2"
          addon-before="¥"
        />
      </a-form-item>

      <!-- 支付信息 -->
      <a-divider orientation="left" v-if="formData.rechargeType !== 'CASH'">支付信息</a-divider>

      <template v-if="formData.rechargeType === 'BANK_TRANSFER'">
        <a-form-item label="银行名称" name="bankName">
          <a-input
            v-model:value="formData.bankName"
            placeholder="请输入银行名称"
          />
        </a-form-item>

        <a-form-item label="转账账号" name="transferAccount">
          <a-input
            v-model:value="formData.transferAccount"
            placeholder="请输入转账账号"
          />
        </a-form-item>

        <a-form-item label="转账人姓名" name="transferorName">
          <a-input
            v-model:value="formData.transferorName"
            placeholder="请输入转账人姓名"
          />
        </a-form-item>
      </template>

      <template v-if="formData.rechargeType === 'WECHAT' || formData.rechargeType === 'ALIPAY'">
        <a-form-item label="交易流水号" name="transactionId">
          <a-input
            v-model:value="formData.transactionId"
            placeholder="请输入交易流水号"
          />
        </a-form-item>
      </template>

      <template v-if="formData.rechargeType === 'CARD'">
        <a-form-item label="刷卡设备" name="deviceNo">
          <a-input
            v-model:value="formData.deviceNo"
            placeholder="请输入刷卡设备编号"
          />
        </a-form-item>

        <a-form-item label="卡号" name="cardNo">
          <a-input
            v-model:value="formData.cardNo"
            placeholder="请输入卡号"
          />
        </a-form-item>
      </template>

      <!-- 备注信息 -->
      <a-divider orientation="left">备注信息</a-divider>

      <a-form-item label="充值备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="请输入充值备注信息"
          :rows="3"
          :maxlength="200"
          show-count
        />
      </a-form-item>

      <!-- 充值汇总 -->
      <a-divider orientation="left">充值汇总</a-divider>

      <a-row :gutter="16">
        <a-col :span="8">
          <a-statistic
            title="充值金额"
            :value="formatMoney(formData.rechargeAmount || 0)"
            :precision="2"
            prefix="¥"
            :value-style="{ color: '#1890ff' }"
          />
        </a-col>
        <a-col :span="8" v-if="showGiftAmount">
          <a-statistic
            title="赠送金额"
            :value="formatMoney(formData.giftAmount || 0)"
            :precision="2"
            prefix="¥"
            :value-style="{ color: '#52c41a' }"
          />
        </a-col>
        <a-col :span="8">
          <a-statistic
            title="到账金额"
            :value="formatMoney(totalAmount)"
            :precision="2"
            prefix="¥"
            :value-style="{ color: '#722ed1' }"
          />
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import { consumeApi } from '/@/api/business/consume/consume-api'
import { smartSentry } from '/@/lib/smart-sentry'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  accountData: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref()
const submitLoading = ref(false)
const showGiftAmount = ref(false)

// 表单数据
const formData = reactive({
  rechargeType: 'CASH',
  rechargeAmount: null,
  giftAmount: 0,
  bankName: '',
  transferAccount: '',
  transferorName: '',
  transactionId: '',
  deviceNo: '',
  cardNo: '',
  remark: '',
})

// 表单验证规则
const formRules = {
  rechargeType: [
    { required: true, message: '请选择充值方式', trigger: 'change' }
  ],
  rechargeAmount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '充值金额必须大于0', trigger: 'blur' }
  ],
  giftAmount: [
    { type: 'number', min: 0, message: '赠送金额不能为负数', trigger: 'blur' }
  ],
  bankName: [
    { required: true, message: '请输入银行名称', trigger: 'blur' }
  ],
  transferAccount: [
    { required: true, message: '请输入转账账号', trigger: 'blur' },
    { pattern: /^\d{16,19}$/, message: '请输入正确的银行账号', trigger: 'blur' }
  ],
  transferorName: [
    { required: true, message: '请输入转账人姓名', trigger: 'blur' }
  ],
  transactionId: [
    { required: true, message: '请输入交易流水号', trigger: 'blur' }
  ],
  deviceNo: [
    { required: true, message: '请输入刷卡设备编号', trigger: 'blur' }
  ],
  cardNo: [
    { required: true, message: '请输入卡号', trigger: 'blur' }
  ]
}

// 充值方式选项
const rechargeTypeOptions = [
  { label: '现金充值', value: 'CASH' },
  { label: '银行转账', value: 'BANK_TRANSFER' },
  { label: '微信支付', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '刷卡充值', value: 'CARD' }
]

// 总金额
const totalAmount = computed(() => {
  const recharge = Number(formData.rechargeAmount || 0)
  const gift = Number(formData.giftAmount || 0)
  return recharge + gift
})

// 获取账户类型文本
const getAccountTypeText = (accountType) => {
  const typeMap = {
    STAFF: '员工账户',
    STUDENT: '学生账户',
    TEMPORARY: '临时账户',
    GUEST: '访客账户'
  }
  return typeMap[accountType] || accountType
}

// 获取账户类型颜色
const getAccountTypeColor = (accountType) => {
  const colorMap = {
    STAFF: 'blue',
    STUDENT: 'green',
    TEMPORARY: 'orange',
    GUEST: 'purple'
  }
  return colorMap[accountType] || 'default'
}

// 格式化金额
const formatMoney = (amount) => {
  return Number(amount || 0).toFixed(2)
}

// 监听充值方式变化
const handleRechargeTypeChange = (value) => {
  // 重置支付信息
  formData.bankName = ''
  formData.transferAccount = ''
  formData.transferorName = ''
  formData.transactionId = ''
  formData.deviceNo = ''
  formData.cardNo = ''

  // 现金充值不显示赠送金额
  showGiftAmount.value = value !== 'CASH'
  if (value === 'CASH') {
    formData.giftAmount = 0
  }
}

// 监听props变化
watch(
  () => props.visible,
  (val) => {
    if (val) {
      // 重置表单数据
      Object.assign(formData, {
        rechargeType: 'CASH',
        rechargeAmount: null,
        giftAmount: 0,
        bankName: '',
        transferAccount: '',
        transferorName: '',
        transactionId: '',
        deviceNo: '',
        cardNo: '',
        remark: ''
      })

      showGiftAmount.value = false
      formRef.value?.resetFields()
    }
  }
)

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validateFields()
    submitLoading.value = true

    const submitData = {
      accountId: props.accountData.accountId,
      rechargeType: formData.rechargeType,
      rechargeAmount: formData.rechargeAmount,
      giftAmount: formData.giftAmount,
      bankName: formData.bankName,
      transferAccount: formData.transferAccount,
      transferorName: formData.transferorName,
      transactionId: formData.transactionId,
      deviceNo: formData.deviceNo,
      cardNo: formData.cardNo,
      remark: formData.remark
    }

    const result = await consumeApi.rechargeAccount(submitData)

    if (result.code === 200) {
      message.success('充值成功')
      emit('success')
      handleCancel()
    } else {
      message.error(result.message || '充值失败')
    }
  } catch (error) {
    if (error.errorFields) {
      // 表单验证错误
      return
    }
    smartSentry.captureError(error)
    message.error('充值失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消弹窗
const handleCancel = () => {
  emit('update:visible', false)
  formRef.value?.resetFields()
}
</script>

<style lang="less" scoped>
:deep(.ant-divider-horizontal.ant-divider-with-text-left) {
  margin: 16px 0;
  font-weight: 500;
}

.negative-balance {
  color: #ff4d4f;
  font-weight: 500;
}
</style>
