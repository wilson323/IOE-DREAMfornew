<!--
  * 设备表单弹窗组件
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="props.visible"
    :title="isEdit ? '编辑设备' : '添加设备'"
    width="600px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="submitLoading"
  >
    <a-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="设备名称" name="deviceName">
        <a-input
          v-model:value="localFormData.deviceName"
          placeholder="请输入设备名称"
        />
      </a-form-item>

      <a-form-item label="设备编号" name="deviceCode">
        <a-input
          v-model:value="localFormData.deviceCode"
          placeholder="请输入设备编号"
        />
      </a-form-item>

      <a-form-item label="区域ID" name="areaId">
        <a-input
          v-model:value="localFormData.areaId"
          placeholder="请输入区域ID"
        />
      </a-form-item>

      <a-form-item label="设备类型" name="deviceType">
        <a-select v-model:value="localFormData.deviceType" placeholder="请选择设备类型">
          <a-select-option value="ACCESS_CONTROL">门禁设备</a-select-option>
          <a-select-option value="GATE">闸机设备</a-select-option>
          <a-select-option value="TURNSTILE">旋转门</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="设备状态" name="status" v-if="isEdit">
        <a-select v-model:value="localFormData.status" placeholder="请选择设备状态">
          <a-select-option :value="1">在线</a-select-option>
          <a-select-option :value="2">离线</a-select-option>
          <a-select-option :value="3">故障</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="localFormData.remark"
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
  import { accessApi } from '/@/api/business/access/access-api';
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

  // 表单数据（本地状态）
  const localFormData = reactive({
    deviceName: '',
    deviceCode: '',
    areaId: '',
    deviceType: '',
    status: 1,
    remark: '',
  });

  // 表单验证规则
  const formRules = {
    deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
    deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
    areaId: [{ required: true, message: '请输入区域ID', trigger: 'blur' }],
    deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
    status: [{ required: true, message: '请选择设备状态', trigger: 'change' }],
  };

  // 监听props变化
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        if (props.isEdit && props.formData) {
          Object.assign(localFormData, {
            deviceName: props.formData.deviceName || '',
            deviceCode: props.formData.deviceCode || '',
            areaId: props.formData.areaId || '',
            deviceType: props.formData.deviceType || '',
            status: props.formData.status || 1,
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
        result = await accessApi.updateDevice({
          deviceId: props.formData.deviceId,
          deviceName: localFormData.deviceName,
          deviceCode: localFormData.deviceCode,
          areaId: localFormData.areaId,
          deviceType: localFormData.deviceType,
          status: localFormData.status,
          remark: localFormData.remark,
        });
      } else {
        // 添加
        result = await accessApi.addDevice({
          deviceName: localFormData.deviceName,
          deviceCode: localFormData.deviceCode,
          areaId: localFormData.areaId,
          deviceType: localFormData.deviceType,
          remark: localFormData.remark,
        });
      }

      if (result.code === 200) {
        message.success(props.isEdit ? '更新成功' : '添加成功');
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
    Object.assign(localFormData, {
      deviceName: '',
      deviceCode: '',
      areaId: '',
      deviceType: '',
      status: 1,
      remark: '',
    });
    formRef.value?.resetFields();
  };
</script>

<style lang="less" scoped>
</style>

