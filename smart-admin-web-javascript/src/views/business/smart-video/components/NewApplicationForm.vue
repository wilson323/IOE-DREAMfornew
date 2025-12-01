<!--
  新建申请表单组件（简化版）

  @Author:    Claude Code
  @Date:      2025-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="new-application-form">
    <a-form layout="vertical">
      <a-form-item label="申请类型">
        <a-select v-model:value="formData.type" placeholder="请选择申请类型">
          <a-select-option value="leave">请假申请</a-select-option>
          <a-select-option value="overtime">加班申请</a-select-option>
          <a-select-option value="makeup">补签申请</a-select-option>
          <a-select-option value="shift">调班申请</a-select-option>
          <a-select-option value="cancel">销假申请</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="申请时间">
        <a-date-picker v-model:value="formData.date" style="width: 100%" />
      </a-form-item>

      <a-form-item label="申请原因">
        <a-textarea v-model:value="formData.reason" :rows="4" placeholder="请详细说明申请原因..." />
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" @click="handleSubmit">提交申请</a-button>
          <a-button @click="handleCancel">取消</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { message } from 'ant-design-vue';

const emit = defineEmits(['submit', 'cancel']);

const formData = reactive({
  type: '',
  date: null,
  reason: '',
});

const handleSubmit = () => {
  if (!formData.type) {
    message.warning('请选择申请类型');
    return;
  }
  if (!formData.reason) {
    message.warning('请输入申请原因');
    return;
  }

  message.success('申请提交成功');
  emit('submit', formData);
};

const handleCancel = () => {
  emit('cancel');
};
</script>

<style lang="less" scoped>
.new-application-form {
  padding: 16px;
}
</style>
