<!--
  * 餐别分类导入模态框
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    title="导入餐别分类"
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
      <a-form-item label="上传文件" name="file">
        <a-upload
          v-model:file-list="fileList"
          :before-upload="beforeUpload"
          :remove="handleRemove"
          :max-count="1"
          accept=".xlsx,.xls,.csv"
        >
          <a-button>
            <template #icon><UploadOutlined /></template>
            选择文件
          </a-button>
        </a-upload>
        <div class="upload-tip">
          支持 Excel (.xlsx, .xls) 和 CSV (.csv) 文件格式
        </div>
      </a-form-item>

      <a-form-item label="导入选项">
        <a-checkbox v-model:checked="formData.overwrite">
          覆盖已存在的分类
        </a-checkbox>
      </a-form-item>
    </a-form>

    <template #footer>
      <div class="import-footer">
        <a-space>
          <a-button @click="handleCancel">取消</a-button>
          <a-button type="primary" @click="handleDownloadTemplate">
            <template #icon><DownloadOutlined /></template>
            下载模板
          </a-button>
          <a-button type="primary" @click="handleSubmit" :loading="submitLoading">
            开始导入
          </a-button>
        </a-space>
      </div>
    </template>
  </a-modal>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    UploadOutlined,
    DownloadOutlined,
  } from '@ant-design/icons-vue';

  // 定义 props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
  });

  // 定义 emits
  const emit = defineEmits(['update:visible', 'success']);

  // 表单引用
  const formRef = ref(null);
  const submitLoading = ref(false);
  const fileList = ref([]);

  // 表单数据
  const formData = reactive({
    file: null,
    overwrite: false,
  });

  // 表单验证规则
  const formRules = {
    file: [
      { required: true, message: '请选择要导入的文件', trigger: 'change' },
    ],
  };

  // 上传前处理
  const beforeUpload = (file) => {
    const isValidType = ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'text/csv'].includes(file.type);

    if (!isValidType) {
      message.error('请上传 Excel 或 CSV 文件');
      return false;
    }

    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('文件大小不能超过 10MB');
      return false;
    }

    formData.file = file;
    return false; // 阻止自动上传
  };

  // 移除文件
  const handleRemove = () => {
    formData.file = null;
  };

  // 下载模板
  const handleDownloadTemplate = () => {
    // 模板下载功能 - 待后端提供模板文件接口
    message.info('模板下载功能开发中');
  };

  // 提交导入
  const handleSubmit = async () => {
    try {
      await formRef.value.validate();

      if (!formData.file) {
        message.error('请选择要导入的文件');
        return;
      }

      submitLoading.value = true;

      const formDataToSend = new FormData();
      formDataToSend.append('file', formData.file);
      formDataToSend.append('overwrite', formData.overwrite);

      // 导入API调用 - 待后端接口完成后启用
      // const result = await mealCategoryApi.importCategories(formDataToSend);

      message.success('导入成功');
      emit('success');
      emit('update:visible', false);

      // 重置表单
      handleReset();

    } catch (error) {
      if (error.errorFields) {
        message.error('请检查表单填写是否正确');
      } else {
        message.error('导入失败');
      }
    } finally {
      submitLoading.value = false;
    }
  };

  // 取消
  const handleCancel = () => {
    emit('update:visible', false);
    handleReset();
  };

  // 重置表单
  const handleReset = () => {
    formRef.value?.resetFields();
    formData.file = null;
    formData.overwrite = false;
    fileList.value = [];
  };
</script>

<style lang="less" scoped>
  .upload-tip {
    color: #666;
    font-size: 12px;
    margin-top: 4px;
  }

  .import-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
</style>
