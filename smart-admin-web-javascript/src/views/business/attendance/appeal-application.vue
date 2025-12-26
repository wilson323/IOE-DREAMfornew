<template>
  <div class="appeal-application">
    <!-- 页面头部 -->
    <a-card title="考勤申诉申请" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-button type="primary" @click="showCreateModal">
            <template #icon><PlusOutlined /></template>
            提交申诉
          </a-button>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-space>
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索申诉记录"
              style="width: 200px"
              @search="handleSearch"
            />
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 申诉记录列表 -->
    <a-card title="我的申诉记录" :bordered="false" style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="appealList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="appealId"
      >
        <!-- 申诉类型 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'appealType'">
            <a-tag :color="getTypeColor(record.appealType)">
              {{ getTypeLabel(record.appealType) }}
            </a-tag>
          </template>

          <!-- 申诉日期 -->
          <template v-else-if="column.key === 'appealDate'">
            {{ formatDate(record.appealDate) }}
          </template>

          <!-- 申诉状态 -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusLabel(record.status) }}
            </a-tag>
          </template>

          <!-- 审批进度 -->
          <template v-else-if="column.key === 'progress'">
            <a-steps :current="getProgressStep(record.status)" :size="'small'" direction="horizontal">
              <a-step title="提交" />
              <a-step title="主管审批" />
              <a-step title="HR确认" />
              <a-step title="完成" />
            </a-steps>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="viewDetail(record)"
              >
                查看详情
              </a-button>
              <a-button
                v-if="record.status === 0"
                type="link"
                size="small"
                danger
                @click="withdrawAppeal(record.appealId)"
              >
                撤销申诉
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 申诉表单模态框 -->
    <a-modal
      v-model:visible="createModalVisible"
      title="提交考勤申诉"
      width="800px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="appealFormRef"
        :model="appealForm"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="申诉类型" name="appealType">
          <a-select
            v-model:value="appealForm.appealType"
            placeholder="请选择申诉类型"
            @change="handleTypeChange"
          >
            <a-select-option value="LATE">迟到申诉</a-select-option>
            <a-select-option value="EARLY">早退申诉</a-select-option>
            <a-select-option value="ABSENCE">缺勤申诉</a-select-option>
            <a-select-option value="OVERTIME">加班申诉</a-select-option>
            <a-select-option value="FORGOT_CARD">忘打卡申诉</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="申诉日期" name="appealDate">
          <a-date-picker
            v-model:value="appealForm.appealDate"
            placeholder="请选择申诉日期"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="打卡时间" name="punchTime">
          <a-time-picker
            v-model:value="appealForm.punchTime"
            placeholder="请选择实际打卡时间"
            format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="申诉原因" name="appealReason">
          <a-textarea
            v-model:value="appealForm.appealReason"
            placeholder="请详细说明申诉原因（必填，至少10个字）"
            :rows="4"
            :max="500"
            show-count
          />
        </a-form-item>

        <a-form-item label="证明材料" name="attachments">
          <a-upload
            v-model:file-list="appealForm.attachments"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :max-count="5"
          >
            <div v-if="appealForm.attachments.length < 5">
              <PlusOutlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="appealForm.remark"
            placeholder="其他需要说明的信息（选填）"
            :rows="2"
            :max="200"
            show-count
          />
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
        <a-descriptions-item label="申诉类型">
          <a-tag :color="getTypeColor(currentAppeal.appealType)">
            {{ getTypeLabel(currentAppeal.appealType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="申诉日期">
          {{ formatDate(currentAppeal.appealDate) }}
        </a-descriptions-item>
        <a-descriptions-item label="打卡时间">
          {{ currentAppeal.punchTime }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉状态">
          <a-tag :color="getStatusColor(currentAppeal.status)">
            {{ getStatusLabel(currentAppeal.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="提交时间">
          {{ formatDateTime(currentAppeal.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="申诉原因" :span="2">
          {{ currentAppeal.appealReason }}
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">
          {{ currentAppeal.remark || '-' }}
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
import { ref, reactive, onMounted } from 'vue';
import {
  PlusOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 数据状态
const loading = ref(false);
const searchText = ref('');
const appealList = ref([]);
const currentAppeal = ref(null);

// 模态框状态
const createModalVisible = ref(false);
const detailModalVisible = ref(false);

// 表单引用
const appealFormRef = ref();

// 申诉表单
const appealForm = reactive({
  appealType: undefined,
  appealDate: null,
  punchTime: null,
  appealReason: '',
  attachments: [],
  remark: ''
});

// 表单验证规则
const rules = {
  appealType: [{ required: true, message: '请选择申诉类型', trigger: 'change' }],
  appealDate: [{ required: true, message: '请选择申诉日期', trigger: 'change' }],
  punchTime: [{ required: true, message: '请选择打卡时间', trigger: 'change' }],
  appealReason: [
    { required: true, message: '请输入申诉原因', trigger: 'blur' },
    { min: 10, message: '申诉原因至少10个字', trigger: 'blur' }
  ]
};

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
    width: 150
  },
  {
    title: '申诉类型',
    dataIndex: 'appealType',
    key: 'appealType',
    width: 120
  },
  {
    title: '申诉日期',
    dataIndex: 'appealDate',
    key: 'appealDate',
    width: 120
  },
  {
    title: '打卡时间',
    dataIndex: 'punchTime',
    key: 'punchTime',
    width: 100
  },
  {
    title: '申诉状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '审批进度',
    key: 'progress',
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

// 上传配置
const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/api/v1/attendance/appeal/upload';
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
};

// 生命周期
onMounted(() => {
  fetchAppealList();
});

// 获取申诉列表
const fetchAppealList = async () => {
  loading.value = true;
  try {
    // TODO: 调用实际API
    // const response = await attendanceApi.getAppealList({
    //   pageNum: pagination.current,
    //   pageSize: pagination.pageSize,
    //   searchText: searchText.value
    // });

    // 模拟数据
    const mockData = {
      records: [
        {
          appealId: '1',
          appealNo: 'AP202512260001',
          appealType: 'LATE',
          appealDate: '2025-12-26',
          punchTime: '09:15',
          status: 0,
          appealReason: '因交通拥堵导致迟到，请予以谅解',
          createTime: '2025-12-26 10:00:00',
          approvalRecords: []
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

// 显示创建模态框
const showCreateModal = () => {
  createModalVisible.value = true;
};

// 处理表单提交
const handleSubmit = async () => {
  try {
    await appealFormRef.value.validate();

    // TODO: 调用实际API
    // await attendanceApi.createAppeal(appealForm);

    message.success('申诉提交成功，请等待审批');
    createModalVisible.value = false;
    resetForm();
    fetchAppealList();
  } catch (error) {
    if (error.errorFields) {
      message.error('请检查表单填写是否正确');
    } else {
      message.error('提交失败：' + error.message);
    }
  }
};

// 重置表单
const resetForm = () => {
  appealFormRef.value.resetFields();
  appealForm.attachments = [];
};

// 取消创建
const handleCancel = () => {
  createModalVisible.value = false;
  resetForm();
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

// 撤销申诉
const withdrawAppeal = async (appealId) => {
  try {
    // TODO: 调用实际API
    // await attendanceApi.withdrawAppeal(appealId);

    message.success('申诉已撤销');
    fetchAppealList();
  } catch (error) {
    message.error('撤销失败：' + error.message);
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
    0: 'processing', // 待审批
    1: 'success', // 已通过
    2: 'error', // 已驳回
    3: 'default' // 已撤销
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

const getProgressStep = (status) => {
  const stepMap = {
    0: 0, // 提交
    1: 2, // 主管审批通过
    2: 2, // HR确认通过
    3: 3  // 完成
  };
  return stepMap[status] || 0;
};

const getTimelineColor = (status) => {
  const colorMap = {
    1: 'blue', // 待审批
    2: 'green', // 已通过
    3: 'red' // 已驳回
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

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return dateStr;
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

// 刷新列表
const refreshList = () => {
  fetchAppealList();
};

// 表格变化
const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchAppealList();
};

// 申诉类型变化
const handleTypeChange = (value) => {
  console.log('申诉类型变化:', value);
};
</script>

<style scoped lang="less">
.appeal-application {
  padding: 16px;

  :deep(.ant-card) {
    .ant-card-head {
      border-bottom: 1px solid #f0f0f0;
    }
  }

  :deep(.ant-steps) {
    .ant-steps-item-process {
      .ant-steps-item-icon {
        width: 24px;
        height: 24px;
        line-height: 24px;
        font-size: 12px;
      }
    }
  }

  :deep(.ant-upload-picture-card) {
    width: 104px;
    height: 104px;
  }
}
</style>
