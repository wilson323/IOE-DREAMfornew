<template>
  <div class="anomaly-list-container">
    <!-- 页面标题 -->
    <a-page-header
      title="考勤异常管理"
      sub-title="查看和管理考勤异常记录"
    />

    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="员工姓名">
          <a-input v-model:value="searchForm.userName" placeholder="请输入员工姓名" allowClear />
        </a-form-item>

        <a-form-item label="部门">
          <a-select
            v-model:value="searchForm.departmentId"
            placeholder="请选择部门"
            allowClear
            style="width: 200px"
          >
            <a-select-option value="1">研发部</a-select-option>
            <a-select-option value="2">市场部</a-select-option>
            <a-select-option value="3">销售部</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="异常类型">
          <a-select
            v-model:value="searchForm.anomalyType"
            placeholder="请选择异常类型"
            allowClear
            style="width: 150px"
          >
            <a-select-option value="MISSING_CARD">缺卡</a-select-option>
            <a-select-option value="LATE">迟到</a-select-option>
            <a-select-option value="EARLY">早退</a-select-option>
            <a-select-option value="ABSENT">旷工</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="异常状态">
          <a-select
            v-model:value="searchForm.anomalyStatus"
            placeholder="请选择状态"
            allowClear
            style="width: 150px"
          >
            <a-select-option value="PENDING">待处理</a-select-option>
            <a-select-option value="APPLIED">已申请</a-select-option>
            <a-select-option value="APPROVED">已批准</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
            <a-select-option value="IGNORED">已忽略</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="考勤日期">
          <a-range-picker
            v-model:value="searchForm.dateRange"
            format="YYYY-MM-DD"
            :placeholder="['开始日期', '结束日期']"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作按钮栏 -->
    <div class="action-bar">
      <a-space>
        <a-button
          type="primary"
          @click="showTriggerDetectionModal"
        >
          <template #icon><ThunderboltOutlined /></template>
          手动检测异常
        </a-button>
        <a-button
          type="default"
          @click="handleExport"
        >
          <template #icon><ExportOutlined /></template>
          导出数据
        </a-button>
      </a-space>
    </div>

    <!-- 异常记录表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="anomalyId"
        :scroll="{ x: 1500 }"
      >
        <!-- 异常类型标签 -->
        <template #anomalyType="{ record }">
          <a-tag :color="getAnomalyTypeColor(record.anomalyType)">
            {{ getAnomalyTypeName(record.anomalyType) }}
          </a-tag>
        </template>

        <!-- 严重程度标签 -->
        <template #severityLevel="{ record }">
          <a-tag :color="getSeverityColor(record.severityLevel)">
            {{ getSeverityName(record.severityLevel) }}
          </a-tag>
        </template>

        <!-- 异常状态标签 -->
        <template #anomalyStatus="{ record }">
          <a-badge :status="getStatusBadge(record.anomalyStatus)" :text="getStatusName(record.anomalyStatus)" />
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              详情
            </a-button>
            <a-button
              v-if="record.anomalyStatus === 'PENDING'"
              type="link"
              size="small"
              @click="handleIgnore(record)"
            >
              忽略
            </a-button>
            <a-button
              v-if="record.anomalyStatus === 'PENDING'"
              type="link"
              size="small"
              danger
              @click="handleCorrect(record)"
            >
              修正
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 异常详情抽屉 -->
    <a-drawer
      v-model:visible="detailVisible"
      title="异常详情"
      width="600"
      placement="right"
    >
      <a-descriptions :column="1" bordered v-if="currentRecord">
        <a-descriptions-item label="员工姓名">
          {{ currentRecord.userName }}
        </a-descriptions-item>
        <a-descriptions-item label="部门">
          {{ currentRecord.departmentName }}
        </a-descriptions-item>
        <a-descriptions-item label="班次">
          {{ currentRecord.shiftName }}
        </a-descriptions-item>
        <a-descriptions-item label="考勤日期">
          {{ currentRecord.attendanceDate }}
        </a-descriptions-item>
        <a-descriptions-item label="异常类型">
          <a-tag :color="getAnomalyTypeColor(currentRecord.anomalyType)">
            {{ getAnomalyTypeName(currentRecord.anomalyType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="严重程度">
          <a-tag :color="getSeverityColor(currentRecord.severityLevel)">
            {{ getSeverityName(currentRecord.severityLevel) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="应打卡时间">
          {{ formatTime(currentRecord.expectedPunchTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="实际打卡时间">
          {{ formatTime(currentRecord.actualPunchTime) || '未打卡' }}
        </a-descriptions-item>
        <a-descriptions-item label="异常时长">
          {{ currentRecord.anomalyDuration ? currentRecord.anomalyDuration + '分钟' : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="异常原因">
          {{ currentRecord.anomalyReason }}
        </a-descriptions-item>
        <a-descriptions-item label="异常状态">
          <a-badge :status="getStatusBadge(currentRecord.anomalyStatus)" :text="getStatusName(currentRecord.anomalyStatus)" />
        </a-descriptions-item>
        <a-descriptions-item label="备注">
          {{ currentRecord.remark || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 操作按钮 -->
      <div class="detail-actions" v-if="currentRecord && currentRecord.anomalyStatus === 'PENDING'">
        <a-space>
          <a-button type="primary" @click="handleApplyFromDetail">
            提交申请
          </a-button>
          <a-button @click="handleIgnoreFromDetail">
            忽略异常
          </a-button>
        </a-space>
      </div>
    </a-drawer>

    <!-- 手动检测异常弹窗 -->
    <a-modal
      v-model:visible="triggerDetectionVisible"
      title="手动检测异常"
      @ok="handleTriggerDetection"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="检测日期" required>
          <a-date-picker
            v-model:value="detectionDate"
            format="YYYY-MM-DD"
            style="width: 100%"
          />
        </a-form-item>
        <a-alert
          message="提示"
          description="系统将自动检测指定日期的所有考勤异常，包括缺卡、迟到、早退、旷工等。"
          type="info"
          show-icon
        />
      </a-form>
    </a-modal>

    <!-- 忽略异常弹窗 -->
    <a-modal
      v-model:visible="ignoreVisible"
      title="忽略异常"
      @ok="handleConfirmIgnore"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="处理意见">
          <a-textarea
            v-model:value="ignoreForm.comment"
            placeholder="请输入处理意见（选填）"
            :rows="4"
          />
        </a-form-item>
        <a-alert
          message="确认忽略此异常吗？"
          description="忽略后，该异常将不会影响考勤统计。"
          type="warning"
          show-icon
        />
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  ThunderboltOutlined,
  ExportOutlined
} from '@ant-design/icons-vue';
import { anomalyApi } from '@/api/business/attendance/anomaly-api';
import dayjs from 'dayjs';

// 搜索表单
const searchForm = reactive({
  userName: '',
  departmentId: null,
  anomalyType: null,
  anomalyStatus: null,
  dateRange: null
});

// 数据源
const dataSource = ref([]);
const loading = ref(false);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 详情弹窗
const detailVisible = ref(false);
const currentRecord = ref(null);

// 手动检测弹窗
const triggerDetectionVisible = ref(false);
const detectionDate = ref(dayjs());

// 忽略异常弹窗
const ignoreVisible = ref(false);
const ignoreForm = reactive({
  anomalyId: null,
  comment: ''
});

// 表格列配置
const columns = [
  {
    title: '员工姓名',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
    fixed: 'left'
  },
  {
    title: '部门',
    dataIndex: 'departmentName',
    key: 'departmentName',
    width: 150
  },
  {
    title: '班次',
    dataIndex: 'shiftName',
    key: 'shiftName',
    width: 120
  },
  {
    title: '考勤日期',
    dataIndex: 'attendanceDate',
    key: 'attendanceDate',
    width: 120
  },
  {
    title: '异常类型',
    key: 'anomalyType',
    width: 100,
    slots: { customRender: 'anomalyType' }
  },
  {
    title: '严重程度',
    key: 'severityLevel',
    width: 100,
    slots: { customRender: 'severityLevel' }
  },
  {
    title: '应打卡时间',
    dataIndex: 'expectedPunchTime',
    key: 'expectedPunchTime',
    width: 160,
    customRender: ({ record }) => formatTime(record.expectedPunchTime)
  },
  {
    title: '实际打卡时间',
    dataIndex: 'actualPunchTime',
    key: 'actualPunchTime',
    width: 160,
    customRender: ({ record }) => formatTime(record.actualPunchTime) || '未打卡'
  },
  {
    title: '异常时长',
    dataIndex: 'anomalyDuration',
    key: 'anomalyDuration',
    width: 100,
    customRender: ({ record }) => record.anomalyDuration ? `${record.anomalyDuration}分钟` : '-'
  },
  {
    title: '异常原因',
    dataIndex: 'anomalyReason',
    key: 'anomalyReason',
    width: 200,
    ellipsis: true
  },
  {
    title: '状态',
    key: 'anomalyStatus',
    width: 120,
    slots: { customRender: 'anomalyStatus' }
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    slots: { customRender: 'action' }
  }
];

// 查询异常列表
const fetchAnomalies = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    };

    const response = await anomalyApi.getAnomalyPage(params);
    if (response.data) {
      dataSource.value = response.data.list || [];
      pagination.total = response.data.total || 0;
    }
  } catch (error) {
    message.error('查询异常记录失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchAnomalies();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    userName: '',
    departmentId: null,
    anomalyType: null,
    anomalyStatus: null,
    dateRange: null
  });
  handleSearch();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchAnomalies();
};

// 查看详情
const handleViewDetail = (record) => {
  currentRecord.value = record;
  detailVisible.value = true;
};

// 忽略异常
const handleIgnore = (record) => {
  ignoreForm.anomalyId = record.anomalyId;
  ignoreForm.comment = '';
  ignoreVisible.value = true;
};

// 确认忽略
const handleConfirmIgnore = async () => {
  try {
    await anomalyApi.ignoreAnomaly(
      ignoreForm.anomalyId,
      1, // TODO: 从用户信息获取
      '管理员',
      ignoreForm.comment
    );
    message.success('忽略成功');
    ignoreVisible.value = false;
    fetchAnomalies();
  } catch (error) {
    message.error('忽略失败');
  }
};

// 修正异常
const handleCorrect = (record) => {
  message.info('修正功能开发中');
};

// 手动检测异常
const showTriggerDetectionModal = () => {
  detectionDate.value = dayjs();
  triggerDetectionVisible.value = true;
};

const handleTriggerDetection = async () => {
  try {
    const date = detectionDate.value.format('YYYY-MM-DD');
    const response = await anomalyApi.triggerDetection(date);
    if (response.data) {
      message.success(`检测完成，共发现 ${response.data} 条异常`);
      triggerDetectionVisible.value = false;
      fetchAnomalies();
    }
  } catch (error) {
    message.error('检测失败');
  }
};

// 导出数据
const handleExport = () => {
  message.info('导出功能开发中');
};

// 辅助函数
const getAnomalyTypeName = (type) => {
  const names = {
    'MISSING_CARD': '缺卡',
    'LATE': '迟到',
    'EARLY': '早退',
    'ABSENT': '旷工'
  };
  return names[type] || type;
};

const getAnomalyTypeColor = (type) => {
  const colors = {
    'MISSING_CARD': 'orange',
    'LATE': 'red',
    'EARLY': 'volcano',
    'ABSENT': 'magenta'
  };
  return colors[type] || 'default';
};

const getSeverityName = (level) => {
  const names = {
    'NORMAL': '一般',
    'SERIOUS': '严重',
    'CRITICAL': '重大'
  };
  return names[level] || level;
};

const getSeverityColor = (level) => {
  const colors = {
    'NORMAL': 'green',
    'SERIOUS': 'orange',
    'CRITICAL': 'red'
  };
  return colors[level] || 'default';
};

const getStatusName = (status) => {
  const names = {
    'PENDING': '待处理',
    'APPLIED': '已申请',
    'APPROVED': '已批准',
    'REJECTED': '已驳回',
    'IGNORED': '已忽略'
  };
  return names[status] || status;
};

const getStatusBadge = (status) => {
  const badges = {
    'PENDING': 'warning',
    'APPLIED': 'processing',
    'APPROVED': 'success',
    'REJECTED': 'error',
    'IGNORED': 'default'
  };
  return badges[status] || 'default';
};

const formatTime = (time) => {
  if (!time) return '';
  return dayjs(time).format('YYYY-MM-DD HH:mm');
};

// 初始化
onMounted(() => {
  fetchAnomalies();
});
</script>

<style scoped lang="less">
.anomaly-list-container {
  padding: 24px;
}

.search-card {
  margin-bottom: 16px;
}

.action-bar {
  margin-bottom: 16px;
}

.table-card {
  .ant-table {
    font-size: 14px;
  }
}

.detail-actions {
  margin-top: 24px;
  text-align: center;
}
</style>
