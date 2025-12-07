<!--
  * 余额操作弹窗组件
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    v-model:visible="visible"
    :title="getOperationTitle()"
    width="500px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="submitLoading"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="账户ID">
        <a-input :value="account?.id" disabled />
      </a-form-item>

      <a-form-item label="当前余额">
        <a-input :value="formatAmount(account?.balance)" disabled />
      </a-form-item>

      <a-form-item label="操作金额" name="amount">
        <a-input-number
          v-model:value="formData.amount"
          placeholder="请输入操作金额（元）"
          style="width: 100%"
          :min="0.01"
          :precision="2"
        />
      </a-form-item>

      <a-form-item label="操作原因" name="reason">
        <a-textarea
          v-model:value="formData.reason"
          placeholder="请输入操作原因"
          :rows="3"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { reactive, ref, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { consumeApi } from '/@/api/business/consume/consume-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    account: {
      type: Object,
      default: () => ({}),
    },
    operationType: {
      type: String,
      default: 'add', // add, deduct, freeze, unfreeze
    },
  });

  const emit = defineEmits(['update:visible', 'success']);

  const formRef = ref();
  const submitLoading = ref(false);

  // 表单数据
  const formData = reactive({
    amount: null,
    reason: '',
  });

  // 表单验证规则
  const formRules = {
    amount: [{ required: true, message: '请输入操作金额', trigger: 'blur' }],
    reason: [{ required: true, message: '请输入操作原因', trigger: 'blur' }],
  };

  // 获取操作标题
  const getOperationTitle = () => {
    const titleMap = {
      add: '增加账户余额',
      deduct: '扣减账户余额',
      freeze: '冻结账户金额',
      unfreeze: '解冻账户金额',
    };
    return titleMap[props.operationType] || '余额操作';
  };

  // 监听props变化
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        resetForm();
      }
    }
  );

  // 提交表单
  const handleSubmit = async () => {
    try {
      await formRef.value.validateFields();
      submitLoading.value = true;

      const params = {
        accountId: props.account.id,
        amount: formData.amount,
        reason: formData.reason,
      };

      let result;
      switch (props.operationType) {
        case 'add':
          result = await consumeApi.addBalance(params);
          break;
        case 'deduct':
          result = await consumeApi.deductBalance(params);
          break;
        case 'freeze':
          result = await consumeApi.freezeAmount(params);
          break;
        case 'unfreeze':
          result = await consumeApi.unfreezeAmount(params);
          break;
      }

      if (result.code === 200) {
        message.success('操作成功');
        emit('success');
        handleCancel();
      } else {
        message.error(result.message || '操作失败');
      }
    } catch (error) {
      if (error.errorFields) {
        return;
      }
      smartSentry.captureError(error);
      message.error('操作失败');
    } finally {
      submitLoading.value = false;
    }
  };

  // 取消
  const handleCancel = () => {
    emit('update:visible', false);
    resetForm();
  };

  // 重置表单
  const resetForm = () => {
    Object.assign(formData, {
      amount: null,
      reason: '',
    });
    formRef.value?.resetFields();
  };

  // 格式化金额
  const formatAmount = (amount) => {
    if (!amount) return '0.00';
    if (amount > 1000) {
      return '¥' + (amount / 100).toFixed(2);
    }
    return '¥' + Number(amount).toFixed(2);
  };
</script>

<style lang="less" scoped>
</style>

