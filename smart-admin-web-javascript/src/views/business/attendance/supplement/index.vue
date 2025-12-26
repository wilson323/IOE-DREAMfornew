<!--
  @fileoverview 补签管理主页面
  @author IOE-DREAM Team
  @description 补签申请查询、审批管理
-->
<template>
  <div class="supplement-management">
    <!-- 查询条件 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="员工姓名">
          <a-input v-model:value="queryForm.employeeName" placeholder="请输入员工姓名" allowClear />
        </a-form-item>
        <a-form-item label="打卡类型">
          <a-select v-model:value="queryForm.punchType" placeholder="请选择" allowClear style="width: 120px">
            <a-select-option value="CHECK_IN">上班打卡</a-select-option>
            <a-select-option value="CHECK_OUT">下班打卡</a-select-option>
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
        <a-form-item label="补签日期">
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
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
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
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
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
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
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
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
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
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="上班补签"
            :value="statisticsData.checkInCount"
            :value-style="{ color: '#722ed1' }"
          />
        </a-col>
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="下班补签"
            :value="statisticsData.checkOutCount"
            :value-style="{ color: '#13c2c2' }"
          />
        </a-col>
      </a-row>
    </a-card>

    <!-- 补签记录列表 -->
    <a-card class="table-card" :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>补签记录列表</span>
          <a-button type="primary" size="small" @click="handleAdd">
            <PlusOutlined />
            新增补签
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
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <!-- 补签日期 -->
        <template #supplementDate="{ record }">
          <div>
            <div>{{ record.supplementDate }}</div>
            <div style="color: #999; font-size: 12px">
              {{ record.punchTime }}
            </div>
          </div>
        </template>

        <!-- 打卡类型 -->
        <template #punchType="{ record }">
          <a-tag :color="record.punchType === 'CHECK_IN' ? 'blue' : 'green'">
            {{ record.punchType === 'CHECK_IN' ? '上班打卡' : '下班打卡' }}
          </a-tag>
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

    <!-- 补签申请对话框 -->
    <SupplementApplicationModal
      v-if="showApplicationModal"
      :visible="showApplicationModal"
      :supplement-data="currentSupplement"
      @cancel="handleApplicationCancel"
      @success="handleApplicationSuccess"
    />

    <!-- 补签审批对话框 -->
    <SupplementApprovalModal
      v-if="showApprovalModal"
      :visible="showApprovalModal"
      :supplement-data="currentSupplement"
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
  CloseCircleOutlined,
  PlusOutlined,
  DownloadOutlined,
  CheckOutlined,
  StopOutlined,
} from '@ant-design/icons-vue';
import {
  supplementApi,
  SupplementQueryForm,
  SupplementRecordVO,
  SupplementStatisticsVO,
  SupplementStatus,
} from '@/api/business/attendance/supplement';
import SupplementApplicationModal from './components/SupplementApplicationModal.vue';
import SupplementApprovalModal from './components/SupplementApprovalModal.vue';

/**
 * 查询表单
 */
const queryForm = reactive<SupplementQueryForm>({
  employeeName: '',
  punchType: undefined,
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
const tableData = ref<SupplementRecordVO[]>([]);
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
const statisticsData = ref<SupplementStatisticsVO>({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  cancelledCount: 0,
  checkInCount: 0,
  checkOutCount: 0,
});

/**
 * 对话框状态
 */
const showApplicationModal = ref(false);
const showApprovalModal = ref(false);
const currentSupplement = ref<SupplementRecordVO | null>(null);

/**
 * 表格列定义
 */
const columns = [
  {
    title: '申请编号',
    dataIndex: 'supplementNo',
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
    title: '补签日期',
    dataIndex: 'supplementDate',
    width: 140,
    slots: { customRender: 'supplementDate' },
  },
  {
    title: '打卡类型',
    dataIndex: 'punchType',
    width: 100,
    align: 'center',
    slots: { customRender: 'punchType' },
  },
  {
    title: '补签原因',
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
    width: 160,
    fixed: 'right',
    slots: { customRender: 'action' },
  },
];

/**
 * 查询补签记录列表
 */
const querySupplementList = async () => {
  loading.value = true;
  try {
    const result = await supplementApi.querySupplementList(queryForm);
    if (result.data) {
      tableData.value = result.data.list || [];
      pagination.total = result.data.total || 0;
    }

    // 同时加载统计数据
    await loadStatistics();
  } catch (error) {
    console.error('加载补签列表失败', error);
    message.error('加载补签列表失败');
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
    const result = await supplementApi.getSupplementStatistics(params);
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
  querySupplementList();
};

/**
 * 重置按钮
 */
const handleReset = () => {
  queryForm.employeeName = '';
  queryForm.punchType = undefined;
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
  querySupplementList();
};

/**
 * 新增补签
 */
const handleAdd = () => {
  currentSupplement.value = null;
  showApplicationModal.value = true;
};

/**
 * 审批
 */
const handleApprove = (record: SupplementRecordVO) => {
  currentSupplement.value = record;
  showApprovalModal.value = true;
};

/**
 * 取消补签
 */
const handleCancel = (record: SupplementRecordVO) => {
  Modal.confirm({
    title: '确认取消',
    content: `确定要取消员工【${record.employeeName}】的补签申请吗？`,
    onOk: async () => {
      try {
        await supplementApi.cancelSupplement(record.supplementNo);
        message.success('取消成功');
        querySupplementList();
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
    const blob = await supplementApi.exportSupplementRecords(queryForm);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `补签记录_${queryForm.startDate}_至_${queryForm.endDate}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (error) {
    console.error('导出失败', error);
    message.error('导出失败');
  }
};

/**
 * 补签申请对话框取消
 */
const handleApplicationCancel = () => {
  showApplicationModal.value = false;
  currentSupplement.value = null;
};

/**
 * 补签申请对话框成功
 */
const handleApplicationSuccess = () => {
  showApplicationModal.value = false;
  currentSupplement.value = null;
  querySupplementList();
};

/**
 * 审批对话框取消
 */
const handleApprovalCancel = () => {
  showApprovalModal.value = false;
  currentSupplement.value = null;
};

/**
 * 审批对话框成功
 */
const handleApprovalSuccess = () => {
  showApprovalModal.value = false;
  currentSupplement.value = null;
  querySupplementList();
};

/**
 * 获取状态名称
 */
const getStatusName = (status: SupplementStatus) => {
  const map: Record<SupplementStatus, string> = {
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
const getStatusColor = (status: SupplementStatus) => {
  const map: Record<SupplementStatus, string> = {
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
  querySupplementList();
});
</script>

<style scoped lang="less">
.supplement-management {
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
