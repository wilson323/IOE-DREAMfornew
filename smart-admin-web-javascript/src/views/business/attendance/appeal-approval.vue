<template>
  <div class="appeal-approval">
    <!-- 页面头部 -->
    <a-card title="考勤申诉审批" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="18">
          <a-space>
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索申诉记录"
              style="width: 250px"
              @search="handleSearch"
              allow-clear
            />
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              @change="handleDateChange"
            />
            <a-select
              v-model:value="statusFilter"
              placeholder="审批状态"
              style="width: 150px"
              @change="handleStatusFilter"
              allow-clear
            >
              <a-select-option :value="0">待审批</a-select-option>
              <a-select-option :value="1">已通过</a-select-option>
              <a-select-option :value="2">已驳回</a-select-option>
            </a-select>
          </a-space>
        </a-col>
        <a-col :span="6" style="text-align: right">
          <a-space>
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
            <a-button type="primary" @click="batchApprove" :disabled="!hasSelected">
              批量审批
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="待审批"
            :value="statistics.pending"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="本月已审批"
            :value="statistics.approved"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="本月已驳回"
            :value="statistics.rejected"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #prefix>
              <CloseCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="审批通过率"
            :value="statistics.approvalRate"
            suffix="%"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 申诉列表 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="pending" tab="待审批">
          <a-table
            :columns="columns"
            :data-source="appealList"
            :loading="loading"
            :pagination="pagination"
            :row-selection="rowSelection"
            @change="handleTableChange"
            row-key="appealId"
          >
            <template #bodyCell="{ column, record }">
              <!-- 申诉类型 -->
              <template v-if="column.key === 'appealType'">
                <a-tag :color="getTypeColor(record.appealType)">
                  {{ getTypeLabel(record.appealType) }}
                </a-tag>
              </template>

              <!-- 申诉人信息 -->
              <template v-else-if="column.key === 'applicant'">
                <a-space direction="vertical" :size="0">
                  <span>{{ record.applicantName }}</span>
                  <span style="color: #999; font-size: 12px">{{ record.department }}</span>
                </a-space>
              </template>

              <!-- 申诉日期和时间 -->
              <template v-else-if="column.key === 'appealDate'">
                <a-space direction="vertical" :size="0">
                  <span>{{ record.appealDate }}</span>
                  <span style="color: #999; font-size: 12px">打卡时间: {{ record.punchTime }}</span>
                </a-space>
              </template>

              <!-- 申诉原因 -->
              <template v-else-if="column.key === 'appealReason'">
                <a-tooltip :title="record.appealReason">
                  <span class="ellipsis-text">{{ record.appealReason }}</span>
                </a-tooltip>
              </template>

              <!-- 提交时间 -->
              <template v-else-if="column.key === 'createTime'">
                {{ formatDateTime(record.createTime) }}
              </template>

              <!-- 操作 -->
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button
                    type="primary"
                    size="small"
                    @click="showApprovalModal(record)"
                  >
                    审批
                  </a-button>
                  <a-button
                    size="small"
                    @click="viewDetail(record)"
                  >
                    详情
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="approved" tab="已通过">
          <a-table
            :columns="historyColumns"
            :data-source="appealList"
            :loading="loading"
            :pagination="pagination"
            @change="handleTableChange"
            row-key="appealId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'applicant'">
                <a-space direction="vertical" :size="0">
                  <span>{{ record.applicantName }}</span>
                  <span style="color: #999; font-size: 12px">{{ record.department }}</span>
                </a-space>
              </template>

              <template v-else-if="column.key === 'approvalResult'">
                <a-tag color="green">已通过</a-tag>
                <div style="margin-top: 4px; font-size: 12px; color: #999">
                  审批人：{{ record.approverName }}
                </div>
                <div style="font-size: 12px; color: #999">
                  审批时间：{{ formatDateTime(record.approvalTime) }}
                </div>
              </template>

              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" @click="viewDetail(record)">
                  查看详情
                </a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="rejected" tab="已驳回">
          <a-table
            :columns="historyColumns"
            :data-source="appealList"
            :loading="loading"
            :pagination="pagination"
            @change="handleTableChange"
            row-key="appealId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'applicant'">
                <a-space direction="vertical" :size="0">
                  <span>{{ record.applicantName }}</span>
                  <span style="color: #999; font-size: 12px">{{ record.department }}</span>
                </a-space>
              </template>

              <template v-else-if="column.key === 'approvalResult'">
                <a-tag color="red">已驳回</a-tag>
                <div style="margin-top: 4px; font-size: 12px; color: #999">
                  驳回原因：{{ record.rejectReason }}
                </div>
                <div style="font-size: 12px; color: #999">
                  审批人：{{ record.approverName }}
                </div>
              </template>

              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" @click="viewDetail(record)">
                  查看详情
                </a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 审批模态框 -->
    <a-modal
      v-model:visible="approvalModalVisible"
      title="申诉审批"
      width="700px"
      @ok="handleApprovalSubmit"
      @cancel="approvalModalVisible = false"
    >
      <a-descriptions
        v-if="currentAppeal"
        title="申诉信息"
        :column="2"
        bordered
      >
        <a-descriptions-item label="申诉编号">
          {{ currentAppeal.appealNo }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉人">
          {{ currentAppeal.applicantName }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门">
          {{ currentAppeal.department }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉类型">
          <a-tag :color="getTypeColor(currentAppeal.appealType)">
            {{ getTypeLabel(currentAppeal.appealType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="申诉日期">
          {{ currentAppeal.appealDate }}
        </a-descriptions-item>
        <a-descriptions-item label="打卡时间">
          {{ currentAppeal.punchTime }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉原因" :span="2">
          {{ currentAppeal.appealReason }}
        </a-descriptions-item>
        <a-descriptions-item label="证明材料" :span="2" v-if="currentAppeal.attachments && currentAppeal.attachments.length">
          <a-image-preview-group :preview="{ count: 5 }">
            <a-image
              v-for="(img, index) in currentAppeal.attachments"
              :key="index"
              :width="60"
              :src="img.url"
            />
          </a-image-preview-group>
        </a-descriptions-item>
      </a-descriptions>

      <!-- 审批操作 -->
      <a-divider>审批操作</a-divider>
      <a-form
        ref="approvalFormRef"
        :model="approvalForm"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="审批结果" name="approvalResult">
          <a-radio-group v-model:value="approvalForm.approvalResult">
            <a-radio :value="1">
              <a-tag color="green">通过</a-tag>
            </a-radio>
            <a-radio :value="2">
              <a-tag color="red">驳回</a-tag>
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item
          label="审批意见"
          name="approvalOpinion"
          :rules="[{ required: true, message: '请输入审批意见', trigger: 'blur' }]"
        >
          <a-textarea
            v-model:value="approvalForm.approvalOpinion"
            placeholder="请输入审批意见（必填）"
            :rows="4"
            :max="200"
            show-count
          />
        </a-form-item>

        <a-form-item label="后续处理" name="followUpAction" v-if="approvalForm.approvalResult === 1">
          <a-checkbox-group v-model:value="approvalForm.followUpActions">
            <a-checkbox value="AUTO_CORRECT">自动修正考勤记录</a-checkbox>
            <a-checkbox value="NOTIFY_HR">通知HR确认</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 申诉详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="申诉详情"
      width="900px"
      :footer="null"
    >
      <a-descriptions
        v-if="currentAppeal"
        title="申诉信息"
        :column="2"
        bordered
      >
        <a-descriptions-item label="申诉编号">
          {{ currentAppeal.appealNo }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉人">
          {{ currentAppeal.applicantName }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门">
          {{ currentAppeal.department }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉类型">
          <a-tag :color="getTypeColor(currentAppeal.appealType)">
            {{ getTypeLabel(currentAppeal.appealType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="申诉日期">
          {{ currentAppeal.appealDate }}
        </a-descriptions-item>
        <a-descriptions-item label="打卡时间">
          {{ currentAppeal.punchTime }}
        </a-descriptions-item>
        <a-descriptions-item label="提交时间">
          {{ formatDateTime(currentAppeal.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉状态">
          <a-tag :color="getStatusColor(currentAppeal.status)">
            {{ getStatusLabel(currentAppeal.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="申诉原因" :span="2">
          {{ currentAppeal.appealReason }}
        </a-descriptions-item>
        <a-descriptions-item label="证明材料" :span="2" v-if="currentAppeal.attachments && currentAppeal.attachments.length">
          <a-image-preview-group>
            <a-image
              v-for="(img, index) in currentAppeal.attachments"
              :key="index"
              :width="100"
              :src="img.url"
            />
          </a-image-preview-group>
        </a-descriptions-item>
      </a-descriptions>

      <!-- 审批流程 -->
      <a-timeline
        v-if="currentAppeal && currentAppeal.approvalRecords"
        style="margin-top: 24px"
        title="审批流程"
      >
        <a-timeline-item
          v-for="(record, index) in currentAppeal.approvalRecords"
          :key="index"
          :color="getTimelineColor(record.approvalStatus)"
        >
          <template #dot>
            <CheckCircleOutlined v-if="record.approvalStatus === 2" />
            <CloseCircleOutlined v-else-if="record.approvalStatus === 3" />
            <ClockCircleOutlined v-else />
          </template>
          <p>
            <strong>{{ record.approvalRole }}</strong>
            <a-tag :color="getApprovalStatusColor(record.approvalStatus)" style="margin-left: 8px">
              {{ getApprovalStatusLabel(record.approvalStatus) }}
            </a-tag>
          </p>
          <p>审批人：{{ record.approverName }}</p>
          <p v-if="record.approvalOpinion">
            审批意见：{{ record.approvalOpinion }}
          </p>
          <p v-if="record.approvalTime">
            审批时间：{{ formatDateTime(record.approvalTime) }}
          </p>
        </a-timeline-item>
      </a-timeline>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import {
  ReloadOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

// 数据状态
const loading = ref(false);
const searchText = ref('');
const dateRange = ref([]);
const statusFilter = ref();
const activeTab = ref('pending');
const appealList = ref([]);
const currentAppeal = ref(null);

// 模态框状态
const approvalModalVisible = ref(false);
const detailModalVisible = ref(false);

// 表单引用
const approvalFormRef = ref();

// 审批表单
const approvalForm = reactive({
  appealId: null,
  approvalResult: 1,
  approvalOpinion: '',
  followUpActions: []
});

// 行选择
const selectedRowKeys = ref([]);
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  }
};

const hasSelected = computed(() => selectedRowKeys.value.length > 0);

// 统计数据
const statistics = ref({
  pending: 0,
  approved: 0,
  rejected: 0,
  approvalRate: 0
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 表格列定义
const columns = [
  {
    title: '申诉编号',
    dataIndex: 'appealNo',
    key: 'appealNo',
    width: 140,
    fixed: 'left'
  },
  {
    title: '申诉类型',
    dataIndex: 'appealType',
    key: 'appealType',
    width: 100
  },
  {
    title: '申诉人',
    key: 'applicant',
    width: 120
  },
  {
    title: '申诉日期',
    key: 'appealDate',
    width: 140
  },
  {
    title: '申诉原因',
    key: 'appealReason',
    width: 200
  },
  {
    title: '提交时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
];

const historyColumns = [
  {
    title: '申诉编号',
    dataIndex: 'appealNo',
    key: 'appealNo',
    width: 140
  },
  {
    title: '申诉人',
    key: 'applicant',
    width: 120
  },
  {
    title: '申诉类型',
    dataIndex: 'appealType',
    key: 'appealType',
    width: 100
  },
  {
    title: '申诉日期',
    key: 'appealDate',
    width: 120
  },
  {
    title: '审批结果',
    key: 'approvalResult',
    width: 200
  },
  {
    title: '操作',
    key: 'action',
    width: 100
  }
];

// 生命周期
onMounted(() => {
  fetchAppealList();
  fetchStatistics();
});

// 获取申诉列表
const fetchAppealList = async () => {
  loading.value = true;
  try {
    // TODO: 调用实际API
    const mockData = {
      records: [
        {
          appealId: '1',
          appealNo: 'AP202512260001',
          appealType: 'LATE',
          applicantName: '张三',
          department: '研发部',
          appealDate: '2025-12-26',
          punchTime: '09:15',
          appealReason: '因交通拥堵导致迟到，请予以谅解',
          createTime: '2025-12-26 10:00:00',
          status: 0
        }
      ],
      total: 1
    };

    appealList.value = mockData.records;
    pagination.total = mockData.total;
  } catch (error) {
    message.error('获取申诉列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // TODO: 调用实际API
    statistics.value = {
      pending: 5,
      approved: 23,
      rejected: 3,
      approvalRate: 88.5
    };
  } catch (error) {
    console.error('获取统计数据失败：', error);
  }
};

// 显示审批模态框
const showApprovalModal = (record) => {
  currentAppeal.value = record;
  approvalForm.appealId = record.appealId;
  approvalForm.approvalResult = 1;
  approvalForm.approvalOpinion = '';
  approvalForm.followUpActions = [];
  approvalModalVisible.value = true;
};

// 处理审批提交
const handleApprovalSubmit = async () => {
  try {
    await approvalFormRef.value.validate();

    // TODO: 调用实际API
    // await attendanceApi.approveAppeal(approvalForm);

    message.success('审批成功');
    approvalModalVisible.value = false;
    fetchAppealList();
    fetchStatistics();
  } catch (error) {
    if (error.errorFields) {
      message.error('请检查表单填写是否正确');
    } else {
      message.error('审批失败：' + error.message);
    }
  }
};

// 批量审批
const batchApprove = () => {
  message.info('批量审批功能开发中');
};

// 查看详情
const viewDetail = async (record) => {
  try {
    // TODO: 调用实际API获取详情
    // const response = await attendanceApi.getAppealDetail(record.appealId);
    // currentAppeal.value = response.data;

    currentAppeal.value = record;
    detailModalVisible.value = true;
  } catch (error) {
    message.error('获取详情失败：' + error.message);
  }
};

// 工具方法
const getTypeColor = (type) => {
  const colorMap = {
    'LATE': 'orange',
    'EARLY': 'blue',
    'ABSENCE': 'red',
    'OVERTIME': 'green',
    'FORGOT_CARD': 'purple'
  };
  return colorMap[type] || 'default';
};

const getTypeLabel = (type) => {
  const labelMap = {
    'LATE': '迟到申诉',
    'EARLY': '早退申诉',
    'ABSENCE': '缺勤申诉',
    'OVERTIME': '加班申诉',
    'FORGOT_CARD': '忘打卡申诉'
  };
  return labelMap[type] || type;
};

const getStatusColor = (status) => {
  const colorMap = {
    0: 'processing',
    1: 'success',
    2: 'error',
    3: 'default'
  };
  return colorMap[status] || 'default';
};

const getStatusLabel = (status) => {
  const labelMap = {
    0: '待审批',
    1: '已通过',
    2: '已驳回',
    3: '已撤销'
  };
  return labelMap[status] || '未知';
};

const getTimelineColor = (status) => {
  const colorMap = {
    1: 'blue',
    2: 'green',
    3: 'red'
  };
  return colorMap[status] || 'gray';
};

const getApprovalStatusColor = (status) => {
  return getTimelineColor(status);
};

const getApprovalStatusLabel = (status) => {
  const labelMap = {
    1: '待审批',
    2: '已通过',
    3: '已驳回'
  };
  return labelMap[status] || '未知';
};

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-';
  return dateTimeStr;
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchAppealList();
};

// 日期范围变化
const handleDateChange = () => {
  pagination.current = 1;
  fetchAppealList();
};

// 状态筛选
const handleStatusFilter = () => {
  pagination.current = 1;
  fetchAppealList();
};

// Tab切换
const handleTabChange = () => {
  pagination.current = 1;
  fetchAppealList();
};

// 刷新列表
const refreshList = () => {
  fetchAppealList();
  fetchStatistics();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchAppealList();
};
</script>

<style scoped lang="less">
.appeal-approval {
  padding: 16px;

  .ellipsis-text {
    display: block;
    width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.ant-statistic) {
    .ant-statistic-title {
      font-size: 14px;
      color: #666;
    }

    .ant-statistic-content {
      font-size: 24px;
      font-weight: 500;
    }
  }
}
</style>
