<template>
  <div class="device-batch-import-container">
    <!-- 页面标题和操作栏 -->
    <a-card class="header-card" :bordered="false">
      <a-row :gutter="16" align="middle">
        <a-col :span="12">
          <div class="page-title">
            <h3>设备批量导入</h3>
            <p class="subtitle">支持Excel文件批量导入设备数据</p>
          </div>
        </a-col>
        <a-col :span="12" class="text-right">
          <a-space>
            <a-button @click="downloadTemplate">
              <template #icon><DownloadOutlined /></template>
              下载模板
            </a-button>
            <a-button type="primary" @click="showUploadModal">
              <template #icon><UploadOutlined /></template>
              上传文件
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="总导入次数"
            :value="statistics.totalImportCount || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix><FileTextOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="今日导入"
            :value="statistics.todayImportCount || 0"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix><ClockCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="整体成功率"
            :value="statistics.overallSuccessRate || 0"
            suffix="%"
            :value-style="{ color: '#faad14' }"
            :precision="1"
          >
            <template #prefix><CheckCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="平均耗时"
            :value="statistics.averageDurationSeconds || 0"
            suffix="秒"
            :value-style="{ color: '#722ed1' }"
            :precision="1"
          >
            <template #prefix><TimerOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 查询表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="导入状态">
          <a-select v-model:value="queryForm.importStatus" placeholder="请选择" style="width: 150px" allowClear>
            <a-select-option :value="0">处理中</a-select-option>
            <a-select-option :value="1">成功</a-select-option>
            <a-select-option :value="2">部分失败</a-select-option>
            <a-select-option :value="3">全部失败</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="关键字">
          <a-input
            v-model:value="queryForm.keyword"
            placeholder="批次名称、文件名"
            allowClear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="queryForm.timeRange"
            format="YYYY-MM-DD"
            placeholder="['开始日期', '结束日期']"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 导入批次列表 -->
    <a-card class="table-card" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="batchList"
        :row-key="(record) => record.batchId"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <!-- 批次信息 -->
        <template #batchName="{ record }">
          <div>
            <div class="batch-name">{{ record.batchName }}</div>
            <div class="file-name">{{ record.fileName }}</div>
          </div>
        </template>

        <!-- 导入统计 -->
        <template #statistics="{ record }">
          <a-space direction="vertical" :size="0">
            <div>
              <a-tag color="green">成功: {{ record.successCount }}</a-tag>
              <a-tag color="red">失败: {{ record.failedCount }}</a-tag>
              <a-tag color="orange">跳过: {{ record.skippedCount }}</a-tag>
            </div>
            <div class="success-rate">成功率: {{ record.successRate }}%</div>
          </a-space>
        </template>

        <!-- 导入状态 -->
        <template #importStatus="{ record }">
          <a-tag :color="getStatusColor(record.importStatus)">
            {{ record.importStatusName }}
          </a-tag>
        </template>

        <!-- 耗时 -->
        <template #duration="{ record }">
          <span>{{ record.durationReadable || '-' }}</span>
        </template>

        <!-- 操作人 -->
        <template #operator="{ record }">
          <span>{{ record.operatorName }}</span>
        </template>

        <!-- 操作按钮 -->
        <template #action="{ record }">
          <a-space>
            <a-button v-if="record.importStatus === 0" type="link" size="small" @click="executeImport(record)">
              执行导入
            </a-button>
            <a-button type="link" size="small" @click="viewDetail(record)">
              详情
            </a-button>
            <a-button
              v-if="record.failedCount > 0"
              type="link"
              size="small"
              @click="exportErrors(record)"
            >
              导出错误
            </a-button>
            <a-popconfirm
              title="确定要删除这个批次吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="deleteBatch(record)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 上传文件对话框 -->
    <a-modal
      v-model:visible="uploadModalVisible"
      title="上传Excel文件"
      :confirm-loading="uploading"
      @ok="handleUpload"
      @cancel="uploadModalVisible = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="批次名称" required>
          <a-input
            v-model:value="uploadForm.batchName"
            placeholder="请输入批次名称，如：2025年1月设备导入"
          />
        </a-form-item>

        <a-form-item label="选择文件" required>
          <a-upload
            :file-list="fileList"
            :before-upload="beforeUpload"
            @remove="handleRemoveFile"
            accept=".xlsx"
          >
            <a-button :disabled="fileList.length >= 1">
              <UploadOutlined />
              选择Excel文件
            </a-button>
          </a-upload>
          <div class="upload-tip">
            只支持.xlsx格式，文件大小不超过10MB
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 批次详情抽屉 -->
    <a-drawer
      v-model:visible="detailDrawerVisible"
      title="导入批次详情"
      width="800"
      @close="detailDrawerVisible = false"
    >
      <div v-if="currentBatch">
        <!-- 批次基础信息 -->
        <a-descriptions title="批次信息" :column="2" bordered>
          <a-descriptions-item label="批次名称">
            {{ currentBatch.batchName }}
          </a-descriptions-item>
          <a-descriptions-item label="文件名">
            {{ currentBatch.fileName }}
          </a-descriptions-item>
          <a-descriptions-item label="文件大小">
            {{ currentBatch.fileSizeReadable }}
          </a-descriptions-item>
          <a-descriptions-item label="文件MD5">
            {{ currentBatch.fileMd5 }}
          </a-descriptions-item>
          <a-descriptions-item label="总记录数">
            {{ currentBatch.totalCount }}
          </a-descriptions-item>
          <a-descriptions-item label="成功数量">
            <a-tag color="green">{{ currentBatch.successCount }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="失败数量">
            <a-tag color="red">{{ currentBatch.failedCount }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="跳过数量">
            <a-tag color="orange">{{ currentBatch.skippedCount }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="成功率">
            {{ currentBatch.successRate }}%
          </a-descriptions-item>
          <a-descriptions-item label="导入状态">
            <a-tag :color="getStatusColor(currentBatch.importStatus)">
              {{ currentBatch.importStatusName }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="开始时间">
            {{ formatDateTime(currentBatch.startTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="结束时间">
            {{ formatDateTime(currentBatch.endTime) || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="耗时">
            {{ currentBatch.durationReadable || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="操作人">
            {{ currentBatch.operatorName }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 错误列表 -->
        <div v-if="errorList.length > 0" class="error-section">
          <h4>错误列表（{{ errorList.length }}条）</h4>
          <a-table
            :columns="errorColumns"
            :data-source="errorList"
            :row-key="(record) => record.errorId"
            :pagination="{ pageSize: 10 }"
            size="small"
          >
            <template #errorMessage="{ record }">
              <a-tag color="red">{{ record.errorMessage }}</a-tag>
            </template>
          </a-table>
        </div>

        <div v-else class="no-error">
          <a-empty description="暂无错误记录" />
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  DownloadOutlined,
  UploadOutlined,
  SearchOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  TimerOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import deviceImportApi from '/@/api/business/access/device-import-api';

// 响应式数据
const queryForm = reactive({
  importStatus: undefined,
  keyword: '',
  timeRange: [],
});

const statistics = reactive({
  totalImportCount: 0,
  todayImportCount: 0,
  overallSuccessRate: 0,
  averageDurationSeconds: 0,
});

const batchList = ref([]);
const loading = ref(false);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 上传相关
const uploadModalVisible = ref(false);
const uploading = ref(false);
const fileList = ref([]);
const uploadForm = reactive({
  batchName: '',
});

// 详情相关
const detailDrawerVisible = ref(false);
const currentBatch = ref(null);
const errorList = ref([]);

// 表格列定义
const columns = [
  {
    title: '批次信息',
    dataIndex: 'batchName',
    key: 'batchName',
    slots: { customRender: 'batchName' },
    width: 250,
  },
  {
    title: '导入统计',
    key: 'statistics',
    slots: { customRender: 'statistics' },
    width: 200,
  },
  {
    title: '状态',
    dataIndex: 'importStatus',
    key: 'importStatus',
    slots: { customRender: 'importStatus' },
    width: 100,
  },
  {
    title: '耗时',
    key: 'duration',
    slots: { customRender: 'duration' },
    width: 100,
  },
  {
    title: '操作人',
    key: 'operator',
    slots: { customRender: 'operator' },
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    customRender: ({ record }) => formatDateTime(record.createTime),
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    fixed: 'right',
    width: 200,
  },
];

// 错误列表列定义
const errorColumns = [
  {
    title: '行号',
    dataIndex: 'rowNumber',
    key: 'rowNumber',
    width: 80,
  },
  {
    title: '错误码',
    dataIndex: 'errorCode',
    key: 'errorCode',
    width: 150,
  },
  {
    title: '错误信息',
    key: 'errorMessage',
    slots: { customRender: 'errorMessage' },
  },
  {
    title: '错误字段',
    dataIndex: 'errorField',
    key: 'errorField',
    width: 120,
  },
];

// 生命周期
onMounted(() => {
  queryBatches();
  queryStatistics();
});

// 方法
const queryBatches = async () => {
  loading.value = true;
  try {
    const params = {
      importStatus: queryForm.importStatus,
      keyword: queryForm.keyword,
      startTime: queryForm.timeRange?.[0] ? dayjs(queryForm.timeRange[0]).format('YYYY-MM-DD') : undefined,
      endTime: queryForm.timeRange?.[1] ? dayjs(queryForm.timeRange[1]).format('YYYY-MM-DD') : undefined,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };

    const response = await deviceImportApi.queryImportBatches(params);
    if (response.code === 200) {
      batchList.value = response.data.list || [];
      pagination.total = response.data.total || 0;
    } else {
      message.error('查询失败：' + response.message);
    }
  } catch (error) {
    console.error('查询批次列表异常:', error);
    message.error('查询失败');
  } finally {
    loading.value = false;
  }
};

const queryStatistics = async () => {
  try {
    const response = await deviceImportApi.getImportStatistics();
    if (response.code === 200) {
      Object.assign(statistics, response.data);
    }
  } catch (error) {
    console.error('查询统计信息异常:', error);
  }
};

const handleQuery = () => {
  pagination.current = 1;
  queryBatches();
};

const handleReset = () => {
  queryForm.importStatus = undefined;
  queryForm.keyword = '';
  queryForm.timeRange = [];
  handleQuery();
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryBatches();
};

const showUploadModal = () => {
  uploadModalVisible.value = true;
  uploadForm.batchName = `设备导入_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}`;
  fileList.value = [];
};

const beforeUpload = (file) => {
  const isXlsx = file.name.endsWith('.xlsx');
  if (!isXlsx) {
    message.error('只支持.xlsx格式的Excel文件');
    return false;
  }

  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过10MB');
    return false;
  }

  fileList.value = [file];
  return false; // 阻止自动上传
};

const handleRemoveFile = () => {
  fileList.value = [];
};

const handleUpload = async () => {
  if (!uploadForm.batchName.trim()) {
    message.error('请输入批次名称');
    return;
  }

  if (fileList.length === 0) {
    message.error('请选择Excel文件');
    return;
  }

  uploading.value = true;
  try {
    const file = fileList[0];
    const response = await deviceImportApi.uploadFile(file, uploadForm.batchName);

    if (response.code === 200) {
      message.success('文件上传成功，正在解析...');
      uploadModalVisible.value = false;
      queryBatches();
    } else {
      message.error('上传失败：' + response.message);
    }
  } catch (error) {
    console.error('上传文件异常:', error);
    message.error('上传失败');
  } finally {
    uploading.value = false;
  }
};

const executeImport = async (record) => {
  try {
    const response = await deviceImportApi.executeImport(record.batchId);
    if (response.code === 200) {
      message.success('导入执行成功');
      queryBatches();
    } else {
      message.error('导入失败：' + response.message);
    }
  } catch (error) {
    console.error('执行导入异常:', error);
    message.error('导入失败');
  }
};

const viewDetail = async (record) => {
  try {
    const response = await deviceImportApi.getImportBatchDetail(record.batchId);
    if (response.code === 200) {
      currentBatch.value = response.data;

      // 查询错误列表
      const errorResponse = await deviceImportApi.queryBatchErrors(record.batchId);
      if (errorResponse.code === 200) {
        errorList.value = errorResponse.data || [];
      }

      detailDrawerVisible.value = true;
    } else {
      message.error('查询详情失败：' + response.message);
    }
  } catch (error) {
    console.error('查询详情异常:', error);
    message.error('查询详情失败');
  }
};

const exportErrors = async (record) => {
  try {
    const response = await deviceImportApi.exportErrors(record.batchId);
    // 下载文件
    const blob = new Blob([response.data]);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `导入错误_${record.batchName}_${record.batchId}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
    message.success('错误记录导出成功');
  } catch (error) {
    console.error('导出错误异常:', error);
    message.error('导出失败');
  }
};

const deleteBatch = async (record) => {
  try {
    const response = await deviceImportApi.deleteImportBatch(record.batchId);
    if (response.code === 200) {
      message.success('删除成功');
      queryBatches();
    } else {
      message.error('删除失败：' + response.message);
    }
  } catch (error) {
    console.error('删除批次异常:', error);
    message.error('删除失败');
  }
};

const downloadTemplate = async () => {
  try {
    const response = await deviceImportApi.downloadTemplate();
    // 下载文件
    const blob = new Blob([response.data]);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '设备导入模板.xlsx';
    a.click();
    window.URL.revokeObjectURL(url);
    message.success('模板下载成功');
  } catch (error) {
    console.error('下载模板异常:', error);
    message.error('下载失败');
  }
};

// 工具方法
const getStatusColor = (status) => {
  const colors = {
    0: 'blue',    // 处理中
    1: 'green',   // 成功
    2: 'orange',  // 部分失败
    3: 'red',     // 全部失败
  };
  return colors[status] || 'default';
};

const formatDateTime = (dateTime) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '';
};
</script>

<style scoped lang="less">
.device-batch-import-container {
  padding: 16px;

  .header-card {
    margin-bottom: 16px;

    .page-title {
      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 500;
      }

      .subtitle {
        margin: 4px 0 0 0;
        color: #999;
        font-size: 14px;
      }
    }

    .text-right {
      text-align: right;
    }
  }

  .stats-row {
    margin-bottom: 16px;
  }

  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .batch-name {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .file-name {
      font-size: 12px;
      color: #999;
    }

    .success-rate {
      font-size: 12px;
      color: #666;
    }
  }

  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
  }

  .error-section {
    margin-top: 24px;

    h4 {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 500;
    }
  }

  .no-error {
    margin-top: 24px;
  }
}
</style>
