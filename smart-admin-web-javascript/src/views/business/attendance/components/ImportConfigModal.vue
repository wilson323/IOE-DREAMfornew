<!--
 * 区域配置导入弹窗组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="modalVisible"
    :title="modalTitle"
    :width="600"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-steps :current="currentStep" size="small" class="mb-4">
      <a-step title="选择文件" />
      <a-step title="数据预览" />
      <a-step title="导入结果" />
    </a-steps>

    <!-- 步骤1: 选择文件 -->
    <div v-if="currentStep === 0">
      <a-alert
        message="导入说明"
        description="请先下载模板，按照模板格式填写数据后再上传。支持Excel格式，文件大小不超过10MB。"
        type="info"
        show-icon
        class="mb-4"
      />

      <a-space direction="vertical" class="w-full">
        <a-button type="primary" @click="downloadTemplate">
          <template #icon><DownloadOutlined /></template>
          下载导入模板
        </a-button>

        <a-upload
          v-model:file-list="fileList"
          :max-count="1"
          :before-upload="beforeUpload"
          :customRequest="customUpload"
          accept=".xlsx,.xls"
        >
          <a-button>
            <template #icon><UploadOutlined /></template>
            选择Excel文件
          </a-button>
        </a-upload>

        <div v-if="fileList.length > 0" class="text-sm text-gray-500">
          已选择文件: {{ fileList[0].name }}
          <span class="ml-2">({{ formatFileSize(fileList[0].size) }})</span>
        </div>
      </a-space>
    </div>

    <!-- 步骤2: 数据预览 -->
    <div v-if="currentStep === 1">
      <a-spin :spinning="previewLoading">
        <div class="mb-4">
          <a-alert
            v-if="previewData.errorMessage"
            :message="previewData.errorMessage"
            type="error"
            show-icon
            class="mb-4"
          />

          <div v-else>
            <div class="mb-4 text-sm">
              <span class="text-gray-600">文件解析完成，共检测到 </span>
              <span class="font-bold text-blue-600">{{ previewData.totalCount }}</span>
              <span class="text-gray-600"> 条数据</span>
            </div>

            <!-- 数据预览表格 -->
            <a-table
              :columns="previewColumns"
              :data-source="previewData.data"
              :pagination="false"
              :scroll="{ x: 800 }"
              size="small"
              :max-height="300"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'status'">
                  <a-tag v-if="record.status === 'success'" color="success">正常</a-tag>
                  <a-tag v-else-if="record.status === 'warning'" color="warning">警告</a-tag>
                  <a-tag v-else color="error">{{ record.error || '错误' }}</a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'errors'">
                  <a-tooltip v-if="record.errors && record.errors.length > 0" :title="record.errors.join(', ')">
                    <a-tag color="error">{{ record.errors.length }}个错误</a-tag>
                  </a-tooltip>
                  <span v-else>-</span>
                </template>
              </template>
            </a-table>

            <!-- 导入选项 -->
            <div class="mt-4">
              <h4 class="text-base font-medium mb-2">导入选项</h4>
              <a-checkbox v-model:checked="importOptions.skipErrors">跳过错误数据</a-checkbox>
              <a-checkbox v-model:checked="importOptions.updateExists" class="ml-4">更新已存在的数据</a-checkbox>
            </div>
          </div>
        </div>
      </a-spin>
    </div>

    <!-- 步骤3: 导入结果 -->
    <div v-if="currentStep === 2">
      <a-result
        :status="importResult.success ? 'success' : 'error'"
        :title="importResult.title"
        :sub-title="importResult.message"
      >
        <template #extra>
          <div v-if="importResult.statistics" class="mb-4">
            <a-descriptions title="导入统计" :column="2" size="small">
              <a-descriptions-item label="总数据数">
                {{ importResult.statistics.total }}
              </a-descriptions-item>
              <a-descriptions-item label="成功导入">
                <span class="text-green-600 font-bold">{{ importResult.statistics.success }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="失败数据">
                <span class="text-red-600 font-bold">{{ importResult.statistics.failed }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="跳过数据">
                <span class="text-orange-600 font-bold">{{ importResult.statistics.skipped }}</span>
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <div v-if="importResult.errorRecords && importResult.errorRecords.length > 0">
            <h4 class="text-base font-medium mb-2">失败数据详情</h4>
            <a-table
              :columns="errorColumns"
              :data-source="importResult.errorRecords"
              :pagination="false"
              size="small"
              :max-height="200"
            />
          </div>

          <a-space>
            <a-button @click="handleCancel">关闭</a-button>
            <a-button v-if="!importResult.success" type="primary" @click="resetToStep1">重新导入</a-button>
          </a-space>
        </template>
      </a-result>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { DownloadOutlined, UploadOutlined } from '@ant-design/icons-vue';
  import { attendanceAreaApi } from '/@/api/business/attendance/attendance-area-api';

  // Props定义
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false
    }
  });

  // Emits定义
  const emit = defineEmits(['update:visible', 'success']);

  // 响应式数据
  const modalVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  });

  const modalTitle = computed(() => {
    const titles = ['选择文件', '数据预览', '导入结果'];
    return `区域配置导入 - ${titles[currentStep.value]}`;
  });

  const currentStep = ref(0);
  const confirmLoading = ref(false);
  const previewLoading = ref(false);
  const fileList = ref([]);

  // 预览数据
  const previewData = reactive({
    totalCount: 0,
    data: [],
    errorMessage: null
  });

  // 导入选项
  const importOptions = reactive({
    skipErrors: false,
    updateExists: false
  });

  // 导入结果
  const importResult = reactive({
    success: false,
    title: '',
    message: '',
    statistics: null,
    errorRecords: []
  });

  // 预览表格列
  const previewColumns = [
    {
      title: '行号',
      dataIndex: 'rowNumber',
      width: 60,
      fixed: 'left'
    },
    {
      title: '区域名称',
      dataIndex: 'areaName',
      width: 120
    },
    {
      title: '区域编码',
      dataIndex: 'areaCode',
      width: 100
    },
    {
      title: '区域类型',
      dataIndex: 'areaType',
      width: 80
    },
    {
      title: '上级区域',
      dataIndex: 'parentName',
      width: 100
    },
    {
      title: 'GPS验证',
      dataIndex: 'gpsRequired',
      width: 80,
      customRender: ({ text }) => text ? '是' : '否'
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80
    },
    {
      title: '错误信息',
      dataIndex: 'errors',
      width: 150
    }
  ];

  // 错误记录表格列
  const errorColumns = [
    {
      title: '行号',
      dataIndex: 'rowNumber',
      width: 60
    },
    {
      title: '区域名称',
      dataIndex: 'areaName',
      width: 120
    },
    {
      title: '错误原因',
      dataIndex: 'error',
      ellipsis: true
    }
  ];

  // 监听弹窗显示状态
  watch(modalVisible, (newVisible) => {
    if (newVisible) {
      resetImportData();
    }
  });

  // ============ 方法 ============

  // 重置导入数据
  const resetImportData = () => {
    currentStep.value = 0;
    fileList.value = [];
    previewData.totalCount = 0;
    previewData.data = [];
    previewData.errorMessage = null;
    importOptions.skipErrors = false;
    importOptions.updateExists = false;
    importResult.success = false;
    importResult.title = '';
    importResult.message = '';
    importResult.statistics = null;
    importResult.errorRecords = [];
  };

  // 下载模板
  const downloadTemplate = async () => {
    try {
      const response = await attendanceAreaApi.downloadAreaTemplate();
      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `区域配置导入模板_${new Date().toISOString().slice(0, 10)}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      message.success('模板下载成功');
    } catch (error) {
      console.error('下载模板失败:', error);
      message.error('下载模板失败');
    }
  };

  // 文件上传前检查
  const beforeUpload = (file) => {
    const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                   file.type === 'application/vnd.ms-excel';
    if (!isExcel) {
      message.error('只能上传Excel文件!');
      return false;
    }
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('文件大小不能超过10MB!');
      return false;
    }
    return false; // 阻止自动上传
  };

  // 自定义上传
  const customUpload = async ({ file }) => {
    fileList.value = [file];
    await parseFile(file);
  };

  // 解析文件
  const parseFile = async (file) => {
    try {
      previewLoading.value = true;
      previewData.errorMessage = null;

      const formData = new FormData();
      formData.append('file', file);

      const response = await attendanceAreaApi.validateImportData(formData);

      if (response.success) {
        previewData.totalCount = response.data.totalCount;
        previewData.data = response.data.previewData || [];
        currentStep.value = 1;
      } else {
        previewData.errorMessage = response.message || '文件解析失败';
      }
    } catch (error) {
      console.error('文件解析失败:', error);
      previewData.errorMessage = error.message || '文件解析失败';
    } finally {
      previewLoading.value = false;
    }
  };

  // 格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // 重置到步骤1
  const resetToStep1 = () => {
    resetImportData();
  };

  // 确认操作
  const handleOk = async () => {
    if (currentStep.value === 0) {
      if (fileList.value.length === 0) {
        message.error('请选择要导入的文件');
        return;
      }
      return;
    }

    if (currentStep.value === 1) {
      await executeImport();
      return;
    }

    if (currentStep.value === 2) {
      handleCancel();
    }
  };

  // 执行导入
  const executeImport = async () => {
    try {
      confirmLoading.value = true;

      const formData = new FormData();
      formData.append('file', fileList.value[0]);
      formData.append('skipErrors', importOptions.skipErrors);
      formData.append('updateExists', importOptions.updateExists);

      const response = await attendanceAreaApi.importAreas(formData);

      if (response.success) {
        importResult.success = true;
        importResult.title = '导入成功';
        importResult.message = `成功导入 ${response.data.successCount} 条数据`;
        importResult.statistics = {
          total: response.data.totalCount,
          success: response.data.successCount,
          failed: response.data.failedCount,
          skipped: response.data.skippedCount || 0
        };
      } else {
        importResult.success = false;
        importResult.title = '导入失败';
        importResult.message = response.message || '导入过程中发生错误';
        importResult.statistics = response.data?.statistics || null;
        importResult.errorRecords = response.data?.errorRecords || [];
      }

      currentStep.value = 2;

      if (importResult.success) {
        emit('success');
      }
    } catch (error) {
      console.error('导入失败:', error);
      importResult.success = false;
      importResult.title = '导入失败';
      importResult.message = error.message || '导入过程中发生异常';
      currentStep.value = 2;
    } finally {
      confirmLoading.value = false;
    }
  };

  // 取消操作
  const handleCancel = () => {
    modalVisible.value = false;
  };
</script>

<style lang="less" scoped>
.text-gray-500 {
  color: #8c8c8c;
}

.text-gray-600 {
  color: #595959;
}

.text-green-600 {
  color: #52c41a;
}

.text-red-600 {
  color: #ff4d4f;
}

.text-orange-600 {
  color: #fa8c16;
}

.text-blue-600 {
  color: #1890ff;
}

.font-bold {
  font-weight: 600;
}

.text-sm {
  font-size: 12px;
}

.text-base {
  font-size: 14px;
}

.font-medium {
  font-weight: 500;
}

.mb-2 {
  margin-bottom: 8px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.ml-2 {
  margin-left: 8px;
}

.ml-4 {
  margin-left: 16px;
}

.w-full {
  width: 100%;
}
</style>