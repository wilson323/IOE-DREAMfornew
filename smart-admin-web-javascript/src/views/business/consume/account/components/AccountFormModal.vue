<!--
 * 账户表单弹窗组件
 * 企业级消费系统账户管理功能
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    :title="isEdit ? '编辑账户' : '创建账户'"
    width="800px"
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
      <!-- 基本信息 -->
      <a-divider orientation="left">基本信息</a-divider>

      <a-form-item label="用户ID" name="userId" v-if="!isEdit">
        <a-input-number
          v-model:value="formData.userId"
          placeholder="请输入用户ID"
          style="width: 100%"
          :min="1"
        />
      </a-form-item>

      <a-form-item label="账户编号" name="accountNo">
        <a-input
          v-model:value="formData.accountNo"
          placeholder="系统自动生成，不可修改"
          :disabled="true"
        />
      </a-form-item>

      <a-form-item label="账户类别" name="accountKindId">
        <a-select
          v-model:value="formData.accountKindId"
          placeholder="请选择账户类别"
          :disabled="isEdit"
        >
          <a-select-option value="1">个人账户</a-select-option>
          <a-select-option value="2">企业账户</a-select-option>
          <a-select-option value="3">临时账户</a-select-option>
          <a-select-option value="4">补贴账户</a-select-option>
        </a-select>
      </a-form-item>

      <!-- 联系信息 -->
      <a-divider orientation="left">联系信息</a-divider>

      <a-form-item label="手机号码" name="userPhone">
        <a-input
          v-model:value="formData.userPhone"
          placeholder="请输入手机号码"
          :maxlength="11"
        />
      </a-form-item>

      <a-form-item label="邮箱地址" name="userEmail">
        <a-input
          v-model:value="formData.userEmail"
          placeholder="请输入邮箱地址"
        />
      </a-form-item>

      <!-- 财务信息 -->
      <a-divider orientation="left">财务信息</a-divider>

      <a-form-item label="初始余额" name="initialBalance" v-if="!isEdit">
        <a-input-number
          v-model:value="formData.initialBalance"
          placeholder="请输入初始余额（元）"
          style="width: 100%"
          :min="0"
          :precision="2"
          addon-before="¥"
        />
      </a-form-item>

      <a-form-item label="信用额度" name="creditLimit">
        <a-input-number
          v-model:value="formData.creditLimit"
          placeholder="请输入信用额度（元），0表示无限制"
          style="width: 100%"
          :min="0"
          :precision="2"
          addon-before="¥"
        />
      </a-form-item>

      <a-form-item label="允许透支" name="allowOverdraw">
        <a-switch
          v-model:checked="formData.allowOverdraw"
          :checked-children="'允许'"
          :un-checked-children="'禁止'"
        />
      </a-form-item>

      <!-- 权限设置 -->
      <a-divider orientation="left">权限设置</a-divider>

      <a-form-item label="消费区域" name="areaIds">
        <a-select
          v-model:value="formData.areaIds"
          mode="multiple"
          placeholder="请选择允许消费的区域"
          :options="areaOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="每日限额" name="dailyLimit">
        <a-input-number
          v-model:value="formData.dailyLimit"
          placeholder="请输入每日消费限额，0表示无限制"
          style="width: 100%"
          :min="0"
          :precision="2"
          addon-before="¥"
        />
      </a-form-item>

      <a-form-item label="单次限额" name="singleLimit">
        <a-input-number
          v-model:value="formData.singleLimit"
          placeholder="请输入单次消费限额，0表示无限制"
          style="width: 100%"
          :min="0"
          :precision="2"
          addon-before="¥"
        />
      </a-form-item>

      <!-- 状态设置 -->
      <a-divider orientation="left">状态设置</a-divider>

      <a-form-item label="账户状态" name="status" v-if="isEdit">
        <a-select v-model:value="formData.status" placeholder="请选择账户状态">
          <a-select-option :value="1">正常</a-select-option>
          <a-select-option :value="2">冻结</a-select-option>
          <a-select-option :value="3">注销</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="到期时间" name="expireTime">
        <a-date-picker
          v-model:value="formData.expireTime"
          placeholder="请选择到期时间，不选择表示永久有效"
          style="width: 100%"
          format="YYYY-MM-DD"
        />
      </a-form-item>

      <!-- 备注信息 -->
      <a-divider orientation="left">备注信息</a-divider>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="请输入备注信息"
          :rows="3"
          :maxlength="200"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { consumeApi } from '/@/api/business/consume/consume-api'
import { smartSentry } from '/@/lib/smart-sentry'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  formData: {
    type: Object,
    default: () => ({}),
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref()
const submitLoading = ref(false)

// 区域选项
const areaOptions = ref([])

// 表单数据
const formData = reactive({
  userId: null,
  accountNo: '',
  accountKindId: null,
  userPhone: '',
  userEmail: '',
  initialBalance: 0,
  creditLimit: 0,
  allowOverdraw: false,
  areaIds: [],
  dailyLimit: 0,
  singleLimit: 0,
  status: 1,
  expireTime: null,
  remark: '',
})

// 表单验证规则
const formRules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '用户ID必须大于0', trigger: 'blur' }
  ],
  accountKindId: [
    { required: true, message: '请选择账户类别', trigger: 'change' }
  ],
  userPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  userEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  initialBalance: [
    { required: true, message: '请输入初始余额', trigger: 'blur' },
    { type: 'number', min: 0, message: '初始余额不能小于0', trigger: 'blur' }
  ],
  creditLimit: [
    { type: 'number', min: 0, message: '信用额度不能小于0', trigger: 'blur' }
  ],
  dailyLimit: [
    { type: 'number', min: 0, message: '每日限额不能小于0', trigger: 'blur' }
  ],
  singleLimit: [
    { type: 'number', min: 0, message: '单次限额不能小于0', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择账户状态', trigger: 'change' }
  ]
}

// 获取区域选项
const fetchAreaOptions = async () => {
  try {
    // TODO: 对接后端接口获取区域列表
    // const res = await consumeApi.getAreaList()
    // areaOptions.value = res.data.map(item => ({
    //   label: item.areaName,
    //   value: item.areaId
    // }))

    // 模拟数据
    areaOptions.value = [
      { label: '食堂一楼', value: 1 },
      { label: '食堂二楼', value: 2 },
      { label: '咖啡厅', value: 3 },
      { label: '便利店', value: 4 },
      { label: '餐厅A区', value: 5 },
      { label: '餐厅B区', value: 6 }
    ]
  } catch (error) {
    smartSentry.captureError(error)
    message.error('获取区域列表失败')
  }
}

// 生成账户编号
const generateAccountNo = () => {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  return `ACC${timestamp}${random}`
}

// 监听props变化
watch(
  () => props.visible,
  (val) => {
    if (val) {
      fetchAreaOptions()

      if (props.isEdit && props.formData) {
        // 编辑模式，填充表单数据
        Object.assign(formData, {
          accountKindId: props.formData.accountKindId,
          userPhone: props.formData.userPhone || '',
          userEmail: props.formData.userEmail || '',
          creditLimit: props.formData.creditLimit || 0,
          allowOverdraw: props.formData.allowOverdraw || false,
          areaIds: props.formData.areaIds || [],
          dailyLimit: props.formData.dailyLimit || 0,
          singleLimit: props.formData.singleLimit || 0,
          status: props.formData.status,
          expireTime: props.formData.expireTime ? dayjs(props.formData.expireTime) : null,
          remark: props.formData.remark || '',
        })
      } else {
        // 创建模式，初始化表单数据
        Object.assign(formData, {
          userId: null,
          accountNo: generateAccountNo(),
          accountKindId: null,
          userPhone: '',
          userEmail: '',
          initialBalance: 0,
          creditLimit: 0,
          allowOverdraw: false,
          areaIds: [],
          dailyLimit: 0,
          singleLimit: 0,
          status: 1,
          expireTime: null,
          remark: '',
        })
      }
    }
  }
)

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validateFields()
    submitLoading.value = true

    const submitData = {
      ...formData,
      expireTime: formData.expireTime ? formData.expireTime.format('YYYY-MM-DD') : null
    }

    let result
    if (props.isEdit) {
      // 编辑
      result = await consumeApi.updateAccount({
        accountId: props.formData.id,
        accountKindId: formData.accountKindId,
        userPhone: formData.userPhone,
        userEmail: formData.userEmail,
        creditLimit: formData.creditLimit,
        allowOverdraw: formData.allowOverdraw,
        areaIds: formData.areaIds,
        dailyLimit: formData.dailyLimit,
        singleLimit: formData.singleLimit,
        status: formData.status,
        expireTime: submitData.expireTime,
        remark: formData.remark,
      })
    } else {
      // 创建
      result = await consumeApi.createAccount({
        userId: formData.userId,
        accountKindId: formData.accountKindId,
        userPhone: formData.userPhone,
        userEmail: formData.userEmail,
        initialBalance: formData.initialBalance,
        creditLimit: formData.creditLimit,
        allowOverdraw: formData.allowOverdraw,
        areaIds: formData.areaIds,
        dailyLimit: formData.dailyLimit,
        singleLimit: formData.singleLimit,
        expireTime: submitData.expireTime,
        remark: formData.remark,
      })
    }

    if (result.code === 200) {
      message.success(props.isEdit ? '更新成功' : '创建成功')
      emit('success')
      handleCancel()
    } else {
      message.error(result.message || '操作失败')
    }
  } catch (error) {
    if (error.errorFields) {
      // 表单验证错误
      return
    }
    smartSentry.captureError(error)
    message.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消
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
</style>