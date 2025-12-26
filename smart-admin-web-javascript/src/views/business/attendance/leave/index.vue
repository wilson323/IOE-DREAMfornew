<!--
  @fileoverview 请假管理主页面
  @author IOE-DREAM Team
  @description 请假申请查询、审批、统计管理
-->
<template>
  <div class="leave-management">
    <!-- 查询条件 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="员工姓名">
          <a-input v-model:value="queryForm.employeeName" placeholder="请输入员工姓名" allowClear />
        </a-form-item>
        <a-form-item label="请假类型">
          <a-select v-model:value="queryForm.leaveType" placeholder="请选择" allowClear style="width: 150px">
            <a-select-option value="ANNUAL">年假</a-select-option>
            <a-select-option value="SICK">病假</a-select-option>
            <a-select-option value="PERSONAL">事假</a-select-option>
            <a-select-option value="MARRIAGE">婚假</a-select-option>
            <a-select-option value="MATERNITY">产假</a-select-option>
            <a-select-option value="PATERNITY">陪产假</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="审批状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择" allowClear style="width: 120px">
            <a-select-option value="PENDING">待审批</a-select-option>
            <a-select-option value="APPROVED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="请假日期">
          <a-range-picker
            v-model:value="dateRange"
            :format="['YYYY-MM-DD', 'YYYY-MM-DD']"
            :placeholder="['开始日期', '结束日期']"
            @change="onDateRangeChange"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <SearchOutlined />
              查询
            </a-button>
            <a-button @click="handleReset">
              <ReloadOutlined />
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 统计概览 -->
    <a-card class="statistics-card" :bordered="false" style="margin-top: 16px">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-statistic
            title="总申请数"
            :value="statisticsData.totalCount"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <FileTextOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-statistic
            title="待审批"
            :value="statisticsData.pendingCount"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-statistic
            title="已通过"
            :value="statisticsData.approvedCount"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-statistic
            title="已驳回"
            :value="statisticsData.rejectedCount"
            :value-style="{ color: '#f5222d' }"
          >
            <template #prefix>
              <CloseCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-statistic
            title="已取消"
            :value="statisticsData.cancelledCount"
            :value-style="{ color: '#d9d9d9' }"
          >
            <template #prefix>
              <MinusCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-statistic
            title="总请假天数"
            :value="statisticsData.totalLeaveDays"
            suffix="天"
          >
            <template #prefix>
              <CalendarOutlined />
            </template>
          </a-statistic>
        </a-col>
      </a-row>
    </a-card>

    <!-- 请假记录列表 -->
    <a-card class="table-card" :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>请假记录列表</span>
          <a-button type="primary" size="small" @click="handleAdd">
            <PlusOutlined />
            新增请假
          </a-button>
          <a-button size="small" @click="handleExport">
            <DownloadOutlined />
            导出记录
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(record) => record.id"
        :scroll="{ x: 1500 }"
        @change="handleTableChange"
      >
        <!-- 请假类型 -->
        <template #leaveType="{ record }">
          <a-tag :color="getLeaveTypeColor(record.leaveType)">
            {{ getLeaveTypeName(record.leaveType) }}
          </a-tag>
        </template>

        <!-- 请假日期 -->
        <template #leaveDate="{ record }">
          <div>
            <div>{{ record.startDate }}</div>
            <div style="color: #999; font-size: 12px">至 {{ record.endDate }}</div>
          </div>
        </template>

        <!-- 请假天数 -->
        <template #leaveDays="{ record }">
          <a-statistic
            :value="record.leaveDays"
            :precision="1"
            suffix="天"
            :value-style="{ fontSize: '14px' }"
          />
        </template>

        <!-- 状态 -->
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusName(record.status) }}
          </a-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleView(record)">
              <EyeOutlined />
              查看
            </a-button>
            <a-button
              v-if="record.status === 'PENDING'"
              type="link"
              size="small"
              @click="handleApprove(record)"
            >
              <CheckOutlined />
              审批
            </a-button>
            <a-button
              v-if="record.status === 'PENDING'"
              type="link"
              size="small"
              danger
              @click="handleCancel(record)"
            >
              <StopOutlined />
              取消
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 请假申请对话框 -->
    <LeaveApplicationModal
      v-if="showApplicationModal"
      :visible="showApplicationModal"
      :leave-data="currentLeave"
      @cancel="handleApplicationCancel"
      @success="handleApplicationSuccess"
    />

    <!-- 请假审批对话框 -->
    <LeaveApprovalModal
      v-if="showApprovalModal"
      :visible="showApprovalModal"
      :leave-data="currentLeave"
      @cancel="handleApprovalCancel"
      @success="handleApprovalSuccess"
    />

    <!-- 请假详情抽屉 -->
    <a-drawer
      v-model:open="showDetailDrawer"
      title="请假详情"
      width="600"
      placement="right"
    >
      <LeaveDetail :leave-data="currentLeave" />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  MinusCircleOutlined,
  CalendarOutlined,
  PlusOutlined,
  DownloadOutlined,
  EyeOutlined,
  CheckOutlined,
  StopOutlined,
} from '@ant-design/icons-vue';
import { leaveApi, LeaveQueryForm, LeaveRecordVO, LeaveStatisticsVO, LeaveStatus, LeaveType } from '@/api/business/attendance/leave';
import LeaveApplicationModal from './components/LeaveApplicationModal.vue';
import LeaveApprovalModal from './components/LeaveApprovalModal.vue';
import LeaveDetail from './components/LeaveDetail.vue';

/**
 * 查询表单
 */
const queryForm = reactive<LeaveQueryForm>({
  employeeName: '',
  leaveType: undefined,
  status: undefined,
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 20,
});

/**
 * 日期范围
 */
const dateRange = ref<[Dayjs, Dayjs] | null>(null);

/**
 * 表格数据
 */
const tableData = ref<LeaveRecordVO[]>([]);
const loading = ref(false);

/**
 * 分页配置
 */
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

/**
 * 统计数据
 */
const statisticsData = ref<LeaveStatisticsVO>({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  cancelledCount: 0,
  annualLeaveCount: 0,
  sickLeaveCount: 0,
  personalLeaveCount: 0,
  totalLeaveDays: 0,
});

/**
 * 对话框状态
 */
const showApplicationModal = ref(false);
const showApprovalModal = ref(false);
const showDetailDrawer = ref(false);
const currentLeave = ref<LeaveRecordVO | null>(null);

/**
 * 表格列定义
 */
const columns = [
  {
    title: '申请编号',
    dataIndex: 'leaveNo',
    width: 150,
    fixed: 'left',
  },
  {
    title: '员工姓名',
    dataIndex: 'employeeName',
    width: 100,
    fixed: 'left',
  },
  {
    title: '部门',
    dataIndex: 'departmentName',
    width: 120,
  },
  {
    title: '请假类型',
    dataIndex: 'leaveType',
    width: 100,
    slots: { customRender: 'leaveType' },
  },
  {
    title: '请假日期',
    dataIndex: 'startDate',
    width: 180,
    slots: { customRender: 'leaveDate' },
  },
  {
    title: '请假天数',
    dataIndex: 'leaveDays',
    width: 100,
    align: 'center',
    slots: { customRender: 'leaveDays' },
  },
  {
    title: '请假原因',
    dataIndex: 'reason',
    width: 200,
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    align: 'center',
    slots: { customRender: 'status' },
  },
  {
    title: '审批意见',
    dataIndex: 'approvalComment',
    width: 150,
    ellipsis: true,
  },
  {
    title: '申请时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    slots: { customRender: 'action' },
  },
];

/**
 * 查询请假记录列表
 */
const queryLeaveList = async () => {
  loading.value = true;
  try {
    const result = await leaveApi.queryLeaveList(queryForm);
    if (result.data) {
      tableData.value = result.data.list || [];
      pagination.total = result.data.total || 0;
    }

    // 同时加载统计数据
    await loadStatistics();
  } catch (error) {
    console.error('加载请假列表失败', error);
    message.error('加载请假列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const params = {
      startDate: queryForm.startDate || undefined,
      endDate: queryForm.endDate || undefined,
    };
    const result = await leaveApi.getLeaveStatistics(params);
    if (result.data) {
      statisticsData.value = result.data;
    }
  } catch (error) {
    console.error('加载统计数据失败', error);
  }
};

/**
 * 查询按钮
 */
const handleQuery = () => {
  queryForm.pageNum = 1;
  pagination.current = 1;
  queryLeaveList();
};

/**
 * 重置按钮
 */
const handleReset = () => {
  queryForm.employeeName = '';
  queryForm.leaveType = undefined;
  queryForm.status = undefined;
  queryForm.startDate = '';
  queryForm.endDate = '';
  dateRange.value = null;
  handleQuery();
};

/**
 * 日期范围变化
 */
const onDateRangeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates.length === 2) {
    queryForm.startDate = dates[0].format('YYYY-MM-DD');
    queryForm.endDate = dates[1].format('YYYY-MM-DD');
  } else {
    queryForm.startDate = '';
    queryForm.endDate = '';
  }
};

/**
 * 表格变化
 */
const handleTableChange = (pag: any) => {
  queryForm.pageNum = pag.current;
  queryForm.pageSize = pag.pageSize;
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryLeaveList();
};

/**
 * 新增请假
 */
const handleAdd = () => {
  currentLeave.value = null;
  showApplicationModal.value = true;
};

/**
 * 查看详情
 */
const handleView = (record: LeaveRecordVO) => {
  currentLeave.value = record;
  showDetailDrawer.value = true;
};

/**
 * 审批
 */
const handleApprove = (record: LeaveRecordVO) => {
  currentLeave.value = record;
  showApprovalModal.value = true;
};

/**
 * 取消请假
 */
const handleCancel = (record: LeaveRecordVO) => {
  Modal.confirm({
    title: '确认取消',
    content: `确定要取消员工【${record.employeeName}】的请假申请吗？`,
    onOk: async () => {
      try {
        await leaveApi.cancelLeave(record.id);
        message.success('取消成功');
        queryLeaveList();
      } catch (error) {
        console.error('取消失败', error);
        message.error('取消失败');
      }
    },
  });
};

/**
 * 导出记录
 */
const handleExport = async () => {
  try {
    const blob = await leaveApi.exportLeaveRecords(queryForm);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `请假记录_${queryForm.startDate}_至_${queryForm.endDate}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (error) {
    console.error('导出失败', error);
    message.error('导出失败');
  }
};

/**
 * 请假申请对话框取消
 */
const handleApplicationCancel = () => {
  showApplicationModal.value = false;
  currentLeave.value = null;
};

/**
 * 请假申请对话框成功
 */
const handleApplicationSuccess = () => {
  showApplicationModal.value = false;
  currentLeave.value = null;
  queryLeaveList();
};

/**
 * 审批对话框取消
 */
const handleApprovalCancel = () => {
  showApprovalModal.value = false;
  currentLeave.value = null;
};

/**
 * 审批对话框成功
 */
const handleApprovalSuccess = () => {
  showApprovalModal.value = false;
  currentLeave.value = null;
  queryLeaveList();
};

/**
 * 获取请假类型名称
 */
const getLeaveTypeName = (type: LeaveType) => {
  const map: Record<LeaveType, string> = {
    ANNUAL: '年假',
    SICK: '病假',
    PERSONAL: '事假',
    MARRIAGE: '婚假',
    MATERNITY: '产假',
    PATERNITY: '陪产假',
    OTHER: '其他',
  };
  return map[type] || type;
};

/**
 * 获取请假类型颜色
 */
const getLeaveTypeColor = (type: LeaveType) => {
  const map: Record<LeaveType, string> = {
    ANNUAL: 'blue',
    SICK: 'orange',
    PERSONAL: 'red',
    MARRIAGE: 'pink',
    MATERNITY: 'purple',
    PATERNITY: 'cyan',
    OTHER: 'default',
  };
  return map[type] || 'default';
};

/**
 * 获取状态名称
 */
const getStatusName = (status: LeaveStatus) => {
  const map: Record<LeaveStatus, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已取消',
  };
  return map[status] || status;
};

/**
 * 获取状态颜色
 */
const getStatusColor = (status: LeaveStatus) => {
  const map: Record<LeaveStatus, string> = {
    PENDING: 'orange',
    APPROVED: 'green',
    REJECTED: 'red',
    CANCELLED: 'default',
  };
  return map[status] || 'default';
};

/**
 * 初始化
 */
onMounted(() => {
  queryLeaveList();
});
</script>

<style scoped lang="less">
.leave-management {
  padding: 16px;

  .search-card {
    .ant-form {
      .ant-form-item {
        margin-bottom: 16px;
      }
    }
  }

  .statistics-card {
    :deep(.ant-statistic) {
      text-align: center;
      padding: 16px 0;
      border-right: 1px solid #f0f0f0;

      &:last-child {
        border-right: none;
      }
    }

    @media (max-width: 768px) {
      :deep(.ant-statistic) {
        border-right: none;
        border-bottom: 1px solid #f0f0f0;
      }
    }
  }

  .table-card {
    :deep(.ant-table) {
      .ant-table-tbody > tr > td {
        padding: 12px 16px;
      }
    }
  }
}
</style>
