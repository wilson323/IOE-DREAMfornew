<!--
 * 设备导入弹窗组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="modalVisible"
    title="设备导入"
    :width="600"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :okText="currentStep === 2 ? '完成' : '下一步'"
    :cancelText="currentStep === 0 ? '取消' : '上一步'"
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
        description="请先下载模板，按照模板格式填写设备信息后再上传。支持Excel格式，文件大小不超过10MB。"
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

        <!-- 模板格式说明 -->
        <a-collapse ghost class="mt-4">
          <a-collapse-panel key="1" header="模板格式说明">
            <div class="template-guide">
              <h4>必填字段：</h4>
              <ul>
                <li><strong>设备名称：</strong>设备的显示名称</li>
                <li><strong>设备编码：</strong>设备的唯一标识，只能包含大写字母、数字、下划线和横线</li>
                <li><strong>设备类型：</strong>FINGERPRINT(指纹)、FACE(人脸)、CARD(IC卡)、PASSWORD(密码)、MOBILE(移动端)</li>
                <li><strong>IP地址：</strong>设备的网络IP地址</li>
                <li><strong>端口号：</strong>设备通信端口号</li>
              </ul>

              <h4 class="mt-3">可选字段：</h4>
              <ul>
                <li><strong>厂商：</strong>设备制造商</li>
                <li><strong>型号：</strong>设备型号</li>
                <li><strong>所属区域：</strong>设备所属的区域名称</li>
                <li><strong>安装位置：</strong>设备的物理安装位置</li>
                <li><strong>描述：</strong>设备的详细描述</li>
              </ul>
            </div>
          </a-collapse-panel>
        </a-collapse>
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
              <span class="text-gray-600"> 条设备数据</span>
              <span v-if="previewData.errorCount > 0" class="ml-2">
                <span class="text-gray-600">，其中 </span>
                <span class="font-bold text-red-600">{{ previewData.errorCount }}</span>
                <span class="text-gray-600"> 条数据有错误</span>
              </span>
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
                  <a-tag v-else color="error">错误</a-tag>
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
              <a-checkbox v-model:checked="importOptions.updateExists" class="ml-4">更新已存在的设备</a-checkbox>
              <a-checkbox v-model:checked="importOptions.testConnection" class="ml-4">导入后测试设备连接</a-checkbox>
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
              <a-descriptions-item label="连接测试成功">
                <span class="text-blue-600 font-bold">{{ importResult.statistics.connected || 0 }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="连接测试失败">
                <span class="text-red-600 font-bold">{{ importResult.statistics.connectionFailed || 0 }}</span>
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- 连接测试结果 -->
          <div v-if="importResult.connectionResults && importResult.connectionResults.length > 0" class="mb-4">
            <h4 class="text-base font-medium mb-2">设备连接测试结果</h4>
            <a-table
              :columns="connectionColumns"
              :data-source="importResult.connectionResults"
              :pagination="false"
              size="small"
              :max-height="200"
            />
          </div>

          <!-- 失败数据详情 -->
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
            <a-button v-if="importResult.success && importResult.connectionFailed > 0" @click="retryFailedConnections">
              重试失败的连接
            </a-button>
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
  import { attendanceDeviceApi } from '/@/api/business/attendance/attendance-device-api';

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

  const currentStep = ref(0);
  const confirmLoading = ref(false);
  const previewLoading = ref(false);
  const fileList = ref([]);

  // 预览数据
  const previewData = reactive({
    totalCount: 0,
    errorCount: 0,
    data: [],
    errorMessage: null
  });

  // 导入选项
  const importOptions = reactive({
    skipErrors: false,
    updateExists: false,
    testConnection: false
  });

  // 导入结果
  const importResult = reactive({
    success: false,
    title: '',
    message: '',
    statistics: null,
    connectionResults: [],
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
      title: '设备名称',
      dataIndex: 'deviceName',
      width: 120
    },
    {
      title: '设备编码',
      dataIndex: 'deviceCode',
      width: 120
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      width: 100
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      width: 100
    },
    {
      title: '所属区域',
      dataIndex: 'areaName',
      width: 100
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

  // 连接测试结果表格列
  const connectionColumns = [
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      width: 120
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      width: 100
    },
    {
      title: '连接状态',
      dataIndex: 'connectionStatus',
      width: 100,
      customRender: ({ text }) => (
        <a-tag :color="text === 'success' ? 'success' : 'error'">
          {{ text === 'success' ? '连接成功' : '连接失败' }}
        </a-tag>
      )
    },
    {
      title: '响应时间',
      dataIndex: 'responseTime',
      width: 100,
      customRender: ({ text }) => text ? `${text}ms` : '-'
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
      title: '设备名称',
      dataIndex: 'deviceName',
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
    previewData.errorCount = 0;
    previewData.data = [];
    previewData.errorMessage = null;
    importOptions.skipErrors = false;
    importOptions.updateExists = false;
    importOptions.testConnection = false;
    importResult.success = false;
    importResult.title = '';
    importResult.message = '';
    importResult.statistics = null;
    importResult.connectionResults = [];
    importResult.errorRecords = [];
  };

  // 下载模板
  const downloadTemplate = async () => {
    try {
      const response = await attendanceDeviceApi.downloadDeviceTemplate();
      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `设备导入模板_${new Date().toISOString().slice(0, 10)}.xlsx`;
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

      const response = await attendanceDeviceApi.validateDeviceImportData(formData);

      if (response.success) {
        previewData.totalCount = response.data.totalCount;
        previewData.errorCount = response.data.errorCount || 0;
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

  // 重试失败的连接
  const retryFailedConnections = async () => {
    try {
      confirmLoading.value = true;
      // 实现重试逻辑
      message.info('连接重试功能开发中...');
    } catch (error) {
      console.error('重试连接失败:', error);
      message.error('重试连接失败');
    } finally {
      confirmLoading.value = false;
    }
  };

  // 确认操作
  const handleOk = async () => {
    if (currentStep.value === 0) {
      if (fileList.value.length === 0) {
        message.error('请选择要导入的文件');
        return;
      }
      currentStep.value = 1;
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
      formData.append('testConnection', importOptions.testConnection);

      const response = await attendanceDeviceApi.importDevices(formData);

      if (response.success) {
        importResult.success = true;
        importResult.title = '导入成功';
        importResult.message = `成功导入 ${response.data.successCount} 个设备`;
        importResult.statistics = {
          total: response.data.totalCount,
          success: response.data.successCount,
          failed: response.data.failedCount,
          skipped: response.data.skippedCount || 0,
          connected: response.data.connectedCount || 0,
          connectionFailed: response.data.connectionFailedCount || 0
        };
        importResult.connectionResults = response.data.connectionResults || [];
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
    if (currentStep.value === 1) {
      currentStep.value = 0;
      return;
    }
    modalVisible.value = false;
  };
</script>

<style lang="less" scoped>
.template-guide {
  h4 {
    margin-bottom: 8px;
    color: #262626;
  }

  ul {
    margin-bottom: 0;
    padding-left: 20px;

    li {
      margin-bottom: 4px;
      color: #595959;
    }
  }
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

.mt-2 {
  margin-top: 8px;
}

.mt-3 {
  margin-top: 12px;
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