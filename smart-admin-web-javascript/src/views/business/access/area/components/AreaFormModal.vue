<!--
  * Area Form Modal Component
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    :title="isEdit ? 'Edit Area' : 'New Area'"
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
      <a-form-item label="Area Name" name="areaName" required>
        <a-input v-model:value="formData.areaName" placeholder="Enter area name" />
      </a-form-item>

      <a-form-item label="Area Code" name="areaCode" required>
        <a-input v-model:value="formData.areaCode" placeholder="Enter area code" />
      </a-form-item>

      <a-form-item label="Area Type" name="areaType" required>
        <a-select v-model:value="formData.areaType" placeholder="Select area type">
          <a-select-option value="BUILDING">Building</a-select-option>
          <a-select-option value="FLOOR">Floor</a-select-option>
          <a-select-option value="ROOM">Room</a-select-option>
          <a-select-option value="ENTRANCE">Entrance</a-select-option>
          <a-select-option value="PARKING">Parking</a-select-option>
          <a-select-option value="OTHER">Other</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Security Level" name="securityLevel" required>
        <a-select v-model:value="formData.securityLevel" placeholder="Select security level">
          <a-select-option value="LOW">Low</a-select-option>
          <a-select-option value="MEDIUM">Medium</a-select-option>
          <a-select-option value="HIGH">High</a-select-option>
          <a-select-option value="CRITICAL">Critical</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Description" name="description">
        <a-textarea v-model:value="formData.description" :rows="3" placeholder="Enter area description" />
      </a-form-item>

      <a-form-item label="Manager" name="managerId">
        <a-select v-model:value="formData.managerId" placeholder="Select manager" allow-clear>
          <a-select-option :value="1">John Doe</a-select-option>
          <a-select-option :value="2">Jane Smith</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Status" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">Enabled</a-radio>
          <a-radio :value="0">Disabled</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { reactive, ref, watch } from 'vue';
  import { message } from 'ant-design-vue';

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

  const formRef = ref(null);
  const submitLoading = ref(false);

  const formRules = {
    areaName: [
      { required: true, message: 'Please enter area name', trigger: 'blur' },
      { max: 100, message: 'Area name cannot exceed 100 characters', trigger: 'blur' },
    ],
    areaCode: [
      { required: true, message: 'Please enter area code', trigger: 'blur' },
      { max: 50, message: 'Area code cannot exceed 50 characters', trigger: 'blur' },
    ],
    areaType: [
      { required: true, message: 'Please select area type', trigger: 'change' },
    ],
    securityLevel: [
      { required: true, message: 'Please select security level', trigger: 'change' },
    ],
  };

  const handleSubmit = async () => {
    try {
      await formRef.value.validate();

      submitLoading.value = true;

      // TODO: 调用API保存区域信息
      // const result = await areaApi.saveArea(props.formData);

      message.success(props.isEdit ? 'Area updated successfully' : 'Area created successfully');
      emit('success');
      emit('update:visible', false);

      // 重置表单
      formRef.value.resetFields();

    } catch (error) {
      if (error.errorFields) {
        message.error('Please check the form inputs');
      } else {
        message.error('Save failed');
      }
    } finally {
      submitLoading.value = false;
    }
  };

  const handleCancel = () => {
    emit('update:visible', false);
    formRef.value?.resetFields();
  };
</script>

<style lang="less" scoped>
  // 表单样式
</style>