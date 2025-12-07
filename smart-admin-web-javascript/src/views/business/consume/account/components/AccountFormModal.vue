<!--
  * 账户表单弹窗组件
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    v-model:visible="visible"
    :title="isEdit ? '编辑账户' : '创建账户'"
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
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="用户ID" name="userId" v-if="!isEdit">
        <a-input-number
          v-model:value="formData.userId"
          placeholder="请输入用户ID"
          style="width: 100%"
          :min="1"
        />
      </a-form-item>

      <a-form-item label="账户类别" name="accountKindId">
        <a-select
          v-model:value="formData.accountKindId"
          placeholder="请选择账户类别"
          :disabled="isEdit"
        >
          <a-select-option :value="1">个人账户</a-select-option>
          <a-select-option :value="2">企业账户</a-select-option>
          <a-select-option :value="3">临时账户</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="初始余额" name="initialBalance" v-if="!isEdit">
        <a-input-number
          v-model:value="formData.initialBalance"
          placeholder="请输入初始余额（元）"
          style="width: 100%"
          :min="0"
          :precision="2"
        />
      </a-form-item>

      <a-form-item label="账户状态" name="status" v-if="isEdit">
        <a-select v-model:value="formData.status" placeholder="请选择账户状态">
          <a-select-option :value="1">正常</a-select-option>
          <a-select-option :value="2">冻结</a-select-option>
          <a-select-option :value="3">注销</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="请输入备注"
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
    formData: {
      type: Object,
      default: () => ({}),
    },
    isEdit: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['update:visible', 'success']);

  const formRef = ref();
  const submitLoading = ref(false);

  // 表单数据
  const formData = reactive({
    userId: null,
    accountKindId: null,
    initialBalance: 0,
    status: 1,
    remark: '',
  });

  // 表单验证规则
  const formRules = {
    userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
    accountKindId: [{ required: true, message: '请选择账户类别', trigger: 'change' }],
    initialBalance: [{ type: 'number', min: 0, message: '初始余额不能小于0', trigger: 'blur' }],
    status: [{ required: true, message: '请选择账户状态', trigger: 'change' }],
  };

  // 监听props变化
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        if (props.isEdit && props.formData) {
          Object.assign(formData, {
            accountKindId: props.formData.accountKindId,
            status: props.formData.status,
            remark: props.formData.remark || '',
          });
        } else {
          resetForm();
        }
      }
    }
  );

  // 提交表单
  const handleSubmit = async () => {
    try {
      await formRef.value.validateFields();
      submitLoading.value = true;

      let result;
      if (props.isEdit) {
        // 编辑
        result = await consumeApi.updateAccount({
          accountId: props.formData.id,
          accountKindId: formData.accountKindId,
          status: formData.status,
          remark: formData.remark,
        });
      } else {
        // 创建
        result = await consumeApi.createAccount({
          userId: formData.userId,
          accountKindId: formData.accountKindId,
          initialBalance: formData.initialBalance,
          remark: formData.remark,
        });
      }

      if (result.code === 200) {
        message.success(props.isEdit ? '更新成功' : '创建成功');
        emit('success');
        handleCancel();
      } else {
        message.error(result.message || '操作失败');
      }
    } catch (error) {
      if (error.errorFields) {
        // 表单验证错误
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
      userId: null,
      accountKindId: null,
      initialBalance: 0,
      status: 1,
      remark: '',
    });
    formRef.value?.resetFields();
  };
</script>

<style lang="less" scoped>
</style>

