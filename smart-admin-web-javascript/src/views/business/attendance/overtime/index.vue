<!--
  @fileoverview 加班管理主页面
  @author IOE-DREAM Team
  @description 加班申请查询、审批、统计管理
-->
<template>
  <div class="overtime-management">
    <!-- 查询条件 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="员工姓名">
          <a-input v-model:value="queryForm.employeeName" placeholder="请输入员工姓名" allowClear />
        </a-form-item>
        <a-form-item label="审批状态">
          <a-select v-model:value="queryForm.status" placeholder="请选择" allowClear style="width: 120px">
            <a-select-option value="PENDING">待审批</a-select-option>
            <a-select-option value="APPROVED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="加班日期">
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
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
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
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
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
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
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
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-statistic
            title="总加班时长"
            :value="statisticsData.totalOvertimeHours"
            suffix="小时"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
      </a-row>
    </a-card>

    <!-- 加班记录列表 -->
    <a-card class="table-card" :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>加班记录列表</span>
          <a-button type="primary" size="small" @click="handleAdd">
            <PlusOutlined />
            新增加班
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
        :scroll="{ x: 1400 }"
        @change="handleTableChange"
      >
        <!-- 加班日期 -->
        <template #overtimeDate="{ record }">
          <div>
            <div>{{ record.overtimeDate }}</div>
            <div style="color: #999; font-size: 12px">
              {{ record.startTime }} - {{ record.endTime }}
            </div>
          </div>
        </template>

        <!-- 加班时长 -->
        <template #overtimeHours="{ record }">
          <a-statistic
            :value="record.overtimeHours"
            :precision="1"
            suffix="小时"
            :value-style="{ fontSize: '14px', color: '#1890ff' }"
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

    <!-- 加班申请对话框 -->
    <OvertimeApplicationModal
      v-if="showApplicationModal"
      :visible="showApplicationModal"
      :overtime-data="currentOvertime"
      @cancel="handleApplicationCancel"
      @success="handleApplicationSuccess"
    />

    <!-- 加班审批对话框 -->
    <OvertimeApprovalModal
      v-if="showApprovalModal"
      :visible="showApprovalModal"
      :overtime-data="currentOvertime"
      @cancel="handleApprovalCancel"
      @success="handleApprovalSuccess"
    />
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
  PlusOutlined,
  DownloadOutlined,
  EyeOutlined,
  CheckOutlined,
  StopOutlined,
} from '@ant-design/icons-vue';
import {
  overtimeApi,
  OvertimeQueryForm,
  OvertimeRecordVO,
  OvertimeStatisticsVO,
  OvertimeStatus,
} from '@/api/business/attendance/overtime';
import OvertimeApplicationModal from './components/OvertimeApplicationModal.vue';
import OvertimeApprovalModal from './components/OvertimeApprovalModal.vue';

/**
 * 查询表单
 */
const queryForm = reactive<OvertimeQueryForm>({
  employeeName: '',
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
const tableData = ref<OvertimeRecordVO[]>([]);
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
const statisticsData = ref<OvertimeStatisticsVO>({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  cancelledCount: 0,
  totalOvertimeHours: 0,
  averageOvertimeHours: 0,
});

/**
 * 对话框状态
 */
const showApplicationModal = ref(false);
const showApprovalModal = ref(false);
const currentOvertime = ref<OvertimeRecordVO | null>(null);

/**
 * 表格列定义
 */
const columns = [
  {
    title: '申请编号',
    dataIndex: 'overtimeNo',
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
    title: '加班日期',
    dataIndex: 'overtimeDate',
    width: 160,
    slots: { customRender: 'overtimeDate' },
  },
  {
    title: '加班时长',
    dataIndex: 'overtimeHours',
    width: 120,
    align: 'center',
    slots: { customRender: 'overtimeHours' },
  },
  {
    title: '加班原因',
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
 * 查询加班记录列表
 */
const queryOvertimeList = async () => {
  loading.value = true;
  try {
    const result = await overtimeApi.queryOvertimeList(queryForm);
    if (result.data) {
      tableData.value = result.data.list || [];
      pagination.total = result.data.total || 0;
    }

    // 同时加载统计数据
    await loadStatistics();
  } catch (error) {
    console.error('加载加班列表失败', error);
    message.error('加载加班列表失败');
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
    const result = await overtimeApi.getOvertimeStatistics(params);
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
  queryOvertimeList();
};

/**
 * 重置按钮
 */
const handleReset = () => {
  queryForm.employeeName = '';
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
  queryOvertimeList();
};

/**
 * 新增加班
 */
const handleAdd = () => {
  currentOvertime.value = null;
  showApplicationModal.value = true;
};

/**
 * 查看详情
 */
const handleView = (record: OvertimeRecordVO) => {
  // TODO: 显示详情对话框或抽屉
  message.info('查看详情功能开发中');
};

/**
 * 审批
 */
const handleApprove = (record: OvertimeRecordVO) => {
  currentOvertime.value = record;
  showApprovalModal.value = true;
};

/**
 * 取消加班
 */
const handleCancel = (record: OvertimeRecordVO) => {
  Modal.confirm({
    title: '确认取消',
    content: `确定要取消员工【${record.employeeName}】的加班申请吗？`,
    onOk: async () => {
      try {
        await overtimeApi.cancelOvertime(record.overtimeNo);
        message.success('取消成功');
        queryOvertimeList();
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
    const blob = await overtimeApi.exportOvertimeRecords(queryForm);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `加班记录_${queryForm.startDate}_至_${queryForm.endDate}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (error) {
    console.error('导出失败', error);
    message.error('导出失败');
  }
};

/**
 * 加班申请对话框取消
 */
const handleApplicationCancel = () => {
  showApplicationModal.value = false;
  currentOvertime.value = null;
};

/**
 * 加班申请对话框成功
 */
const handleApplicationSuccess = () => {
  showApplicationModal.value = false;
  currentOvertime.value = null;
  queryOvertimeList();
};

/**
 * 审批对话框取消
 */
const handleApprovalCancel = () => {
  showApprovalModal.value = false;
  currentOvertime.value = null;
};

/**
 * 审批对话框成功
 */
const handleApprovalSuccess = () => {
  showApprovalModal.value = false;
  currentOvertime.value = null;
  queryOvertimeList();
};

/**
 * 获取状态名称
 */
const getStatusName = (status: OvertimeStatus) => {
  const map: Record<OvertimeStatus, string> = {
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
const getStatusColor = (status: OvertimeStatus) => {
  const map: Record<OvertimeStatus, string> = {
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
  queryOvertimeList();
});
</script>

<style scoped lang="less">
.overtime-management {
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
