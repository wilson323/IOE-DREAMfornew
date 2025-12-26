<!--
 * Batch grant permissions
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM
-->
<template>
  <a-modal
    :open="visible"
    title="Batch Grant Permissions"
    width="700px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="submitLoading"
    destroy-on-close
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="Target Areas" name="areaIds">
        <a-select
          v-model:value="formData.areaIds"
          mode="multiple"
          placeholder="Select areas"
          :options="areaOptions"
          allow-clear
          show-search
          :filter-option="filterOption"
        />
      </a-form-item>

      <a-form-item label="Permission Type" name="permissionType">
        <a-radio-group v-model:value="formData.permissionType">
          <a-radio value="FULL">Full Access</a-radio>
          <a-radio value="LIMITED">Limited</a-radio>
          <a-radio value="CUSTOM">Custom Time</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="Valid Date" name="dateRange">
        <a-range-picker
          v-model:value="formData.dateRange"
          style="width: 100%"
          format="YYYY-MM-DD"
        />
      </a-form-item>

      <a-form-item label="Remark" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="Remark"
          :rows="3"
          :maxlength="200"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  selectedKeys: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref();
const submitLoading = ref(false);

const areaOptions = ref([
  { label: 'Building A', value: 1 },
  { label: 'Building B', value: 2 },
  { label: 'Parking', value: 3 },
  { label: 'Lobby', value: 4 },
]);

const formData = reactive({
  areaIds: [],
  permissionType: 'FULL',
  dateRange: [],
  remark: '',
});

const formRules = {
  areaIds: [
    { required: true, message: 'Please select areas', trigger: 'change' },
  ],
  permissionType: [
    { required: true, message: 'Please select permission type', trigger: 'change' },
  ],
};

const filterOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

watch(
  () => props.visible,
  (val) => {
    if (val) {
      formData.areaIds = [...props.selectedKeys];
      formData.permissionType = 'FULL';
      formData.dateRange = [];
      formData.remark = '';
      formRef.value?.resetFields();
    }
  }
);

const handleSubmit = async () => {
  try {
    await formRef.value.validateFields();
    submitLoading.value = true;

    // 构建批量授权请求参数
    const grantParams = {
      userIds: props.selectedKeys,
      areaIds: formData.areaIds,
      permissionType: formData.permissionType,
      startTime: formData.dateRange && formData.dateRange[0]
        ? dayjs(formData.dateRange[0]).format('YYYY-MM-DD 00:00:00')
        : null,
      endTime: formData.dateRange && formData.dateRange[1]
        ? dayjs(formData.dateRange[1]).format('YYYY-MM-DD 23:59:59')
        : null,
      remark: formData.remark,
    };

    // TODO: 调用批量授权API
    // const result = await accessApi.batchGrantPermissions(grantParams);
    // if (result.code === 200) {
    //   message.success(`成功为 ${props.selectedKeys.length} 个用户授权`);
    //   emit('success');
    //   handleCancel();
    // } else {
    //   message.error(result.message || '批量授权失败');
    // }

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    message.success(`成功为 ${props.selectedKeys.length} 个用户授权`);
    emit('success');
    handleCancel();
  } catch (error) {
    // validation errors are handled by form
  } finally {
    submitLoading.value = false;
  }
};

const handleCancel = () => {
  emit('update:visible', false);
};
</script>
