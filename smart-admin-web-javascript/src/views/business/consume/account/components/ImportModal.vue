<!--
  * Import Modal Component
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    title="Import Data"
    width="600px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="submitLoading"
  >
    <a-upload
      v-model:file-list="fileList"
      :before-upload="beforeUpload"
      :remove="handleRemove"
      :max-count="1"
      accept=".xlsx,.xls,.csv"
    >
      <a-button>
        <template #icon><UploadOutlined /></template>
        Select File
      </a-button>
    </a-upload>

    <div class="import-tip">
      <p>Support Excel (.xlsx, .xls) and CSV (.csv) files</p>
      <p>Max file size: 10MB</p>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { UploadOutlined } from '@ant-design/icons-vue';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['update:visible', 'success']);

  const fileList = ref([]);
  const submitLoading = ref(false);

  const beforeUpload = (file) => {
    const isValidType = ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'text/csv'].includes(file.type);
    if (!isValidType) {
      message.error('Please upload Excel or CSV file');
      return false;
    }

    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('File size cannot exceed 10MB');
      return false;
    }

    return false;
  };

  const handleRemove = () => {
    fileList.value = [];
  };

  const handleSubmit = () => {
    if (fileList.value.length === 0) {
      message.error('Please select a file');
      return;
    }

    submitLoading.value = true;
    setTimeout(() => {
      message.success('Import successful');
      emit('success');
      emit('update:visible', false);
      fileList.value = [];
      submitLoading.value = false;
    }, 1000);
  };

  const handleCancel = () => {
    emit('update:visible', false);
    fileList.value = [];
  };
</script>

<style lang="less" scoped>
  .import-tip {
    margin-top: 16px;
    padding: 12px;
    background: #f6f8fa;
    border-radius: 4px;
    font-size: 12px;
    color: #666;

    p {
      margin: 4px 0;
    }
  }
</style>