<!--
  @fileoverview 考勤报表管理主页面
  @author IOE-DREAM Team
  @description 月度汇总、部门报表、个人报表
-->
<template>
  <div class="report-management">
    <!-- 查询条件 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="报表类型">
          <a-select v-model:value="reportType" placeholder="请选择" style="width: 150px" @change="handleReportTypeChange">
            <a-select-option value="MONTHLY">月度汇总</a-select-option>
            <a-select-option value="DEPARTMENT">部门报表</a-select-option>
            <a-select-option value="PERSONAL">个人报表</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 部门选择（仅部门报表显示） -->
        <a-form-item v-if="reportType === 'DEPARTMENT'" label="部门">
          <a-select
            v-model:value="queryForm.departmentId"
            placeholder="请选择部门"
            allowClear
            showSearch
            :filter-option="filterOption"
            style="width: 200px"
          >
            <a-select-option v-for="dept in departmentList" :key="dept.id" :value="dept.id">
              {{ dept.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 员工选择（仅个人报表显示） -->
        <a-form-item v-if="reportType === 'PERSONAL'" label="员工">
          <a-select
            v-model:value="queryForm.employeeId"
            placeholder="请选择员工"
            allowClear
            showSearch
            :filter-option="filterOption"
            style="width: 200px"
          >
            <a-select-option v-for="emp in employeeList" :key="emp.id" :value="emp.id">
              {{ emp.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 月份选择 -->
        <a-form-item label="月份">
          <a-month-picker
            v-model:value="selectedMonth"
            placeholder="请选择月份"
            @change="handleMonthChange"
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
            <a-button @click="handleExport">
              <DownloadOutlined />
              导出
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 统计概览卡片 -->
    <a-card class="statistics-card" :bordered="false" style="margin-top: 16px">
      <a-row :gutter="16">
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="总人数"
            :value="statisticsData.totalCount"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <TeamOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="出勤人数"
            :value="statisticsData.attendanceCount"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="缺勤人数"
            :value="statisticsData.absenceCount"
            :value-style="{ color: '#f5222d' }"
          >
            <template #prefix>
              <CloseCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="出勤率"
            :value="statisticsData.attendanceRate"
            suffix="%"
            :value-style="{ color: '#722ed1' }"
            :precision="1"
          >
            <template #prefix>
              <RiseOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="迟到总次数"
            :value="statisticsData.lateCount"
            :value-style="{ color: '#faad14' }"
          />
        </a-col>
        <a-col :xs="12" :sm="8" :md="6" :lg="4">
          <a-statistic
            title="早退总次数"
            :value="statisticsData.earlyLeaveCount"
            :value-style="{ color: '#13c2c2' }"
          />
        </a-col>
      </a-row>
    </a-card>

    <!-- 图表展示 -->
    <a-card class="chart-card" :bordered="false" style="margin-top: 16px">
      <template #title>
        <span>考勤趋势分析</span>
      </template>
      <ReportTrendChart :chart-data="chartData" :loading="chartLoading" />
    </a-card>

    <!-- 报表数据表格 -->
    <a-card class="table-card" :bordered="false" style="margin-top: 16px">
      <template #title>
        <span>{{ getReportTitle() }}</span>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(record) => record.employeeId + '_' + record.reportDate"
        :scroll="{ x: 1400 }"
        @change="handleTableChange"
      >
        <!-- 员工信息 -->
        <template #employeeInfo="{ record }">
          <div>
            <div style="font-weight: 500">{{ record.employeeName }}</div>
            <div style="color: #999; font-size: 12px">{{ record.departmentName }}</div>
          </div>
        </template>

        <!-- 报表日期 -->
        <template #reportDate="{ record }">
          <a-tag color="blue">{{ record.reportDate }}</a-tag>
        </template>

        <!-- 出勤天数 -->
        <template #attendanceDays="{ record }">
          <a-statistic
            :value="record.attendanceDays"
            :value-style="{ color: record.attendanceDays >= 20 ? '#52c41a' : '#faad14', fontSize: '16px' }"
            :precision="0"
          />
        </template>

        <!-- 缺勤天数 -->
        <template #absenceDays="{ record }">
          <a-tag v-if="record.absenceDays > 0" color="red">{{ record.absenceDays }}天</a-tag>
          <span v-else style="color: #999">-</span>
        </template>

        <!-- 迟到早退 -->
        <template #abnormal="{ record }">
          <div>
            <div v-if="record.lateCount > 0" style="color: #faad14">
              <ClockCircleOutlined />
              迟到 {{ record.lateCount }}次
            </div>
            <div v-if="record.earlyLeaveCount > 0" style="color: #f5222d; margin-top: 4px">
              <LogoutOutlined />
              早退 {{ record.earlyLeaveCount }}次
            </div>
            <div v-if="record.lateCount === 0 && record.earlyLeaveCount === 0" style="color: #52c41a">
              <CheckCircleOutlined />
              正常
            </div>
          </div>
        </template>

        <!-- 加班时长 -->
        <template #overtimeHours="{ record }">
          <a-statistic
            v-if="record.overtimeHours > 0"
            :value="record.overtimeHours"
            suffix="小时"
            :value-style="{ color: '#722ed1', fontSize: '14px' }"
            :precision="1"
          />
          <span v-else style="color: #999">-</span>
        </template>

        <!-- 工作总时长 -->
        <template #totalWorkHours="{ record }">
          <a-progress
            :percent="Math.round((record.totalWorkHours / 160) * 100)"
            :format="() => `${record.totalWorkHours}小时`"
            :stroke-color="record.totalWorkHours >= 160 ? '#52c41a' : '#1890ff'"
          />
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  DownloadOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  RiseOutlined,
  ClockCircleOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue';
import {
  reportApi,
  ReportQueryForm,
  AttendanceReportVO,
  AttendanceStatisticsVO,
} from '@/api/business/attendance/report';
import ReportTrendChart from './components/ReportTrendChart.vue';

/**
 * 查询表单
 */
const queryForm = reactive<ReportQueryForm>({
  startDate: '',
  endDate: '',
  employeeId: undefined,
  departmentId: undefined,
  reportType: undefined,
});

/**
 * 报表类型
 */
const reportType = ref<'MONTHLY' | 'DEPARTMENT' | 'PERSONAL'>('MONTHLY');

/**
 * 选中的月份
 */
const selectedMonth = ref<Dayjs>(dayjs());

/**
 * 表格数据
 */
const tableData = ref<AttendanceReportVO[]>([]);
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
const statisticsData = ref<AttendanceStatisticsVO>({
  statisticsType: 'COMPANY',
  targetId: 0,
  targetName: '',
  totalCount: 0,
  attendanceCount: 0,
  absenceCount: 0,
  attendanceRate: 0,
  avgWorkHours: 0,
  lateCount: 0,
  earlyLeaveCount: 0,
  totalOvertimeHours: 0,
  avgOvertimeHours: 0,
  dailyStatistics: [],
  weeklyStatistics: [],
  monthlyStatistics: [],
});

/**
 * 图表数据
 */
const chartData = ref<Record<string, any>>({});
const chartLoading = ref(false);

/**
 * 部门列表（模拟数据）
 */
const departmentList = ref<Array<{ id: number; name: string }>>([
  { id: 1, name: '技术部' },
  { id: 2, name: '市场部' },
  { id: 3, name: '人事部' },
  { id: 4, name: '财务部' },
  { id: 5, name: '运营部' },
]);

/**
 * 员工列表（模拟数据）
 */
const employeeList = ref<Array<{ id: number; name: string }>>([
  { id: 1001, name: '张三' },
  { id: 1002, name: '李四' },
  { id: 1003, name: '王五' },
  { id: 1004, name: '赵六' },
  { id: 1005, name: '钱七' },
]);

/**
 * 表格列定义
 */
const columns = [
  {
    title: '员工信息',
    key: 'employeeInfo',
    width: 150,
    fixed: 'left',
    slots: { customRender: 'employeeInfo' },
  },
  {
    title: '报表日期',
    dataIndex: 'reportDate',
    key: 'reportDate',
    width: 120,
    align: 'center',
    slots: { customRender: 'reportDate' },
  },
  {
    title: '出勤天数',
    dataIndex: 'attendanceDays',
    key: 'attendanceDays',
    width: 120,
    align: 'center',
    slots: { customRender: 'attendanceDays' },
  },
  {
    title: '缺勤天数',
    dataIndex: 'absenceDays',
    key: 'absenceDays',
    width: 100,
    align: 'center',
    slots: { customRender: 'absenceDays' },
  },
  {
    title: '迟到早退',
    key: 'abnormal',
    width: 120,
    align: 'center',
    slots: { customRender: 'abnormal' },
  },
  {
    title: '加班时长',
    dataIndex: 'overtimeHours',
    key: 'overtimeHours',
    width: 120,
    align: 'center',
    slots: { customRender: 'overtimeHours' },
  },
  {
    title: '工作总时长',
    dataIndex: 'totalWorkHours',
    key: 'totalWorkHours',
    width: 200,
    slots: { customRender: 'totalWorkHours' },
  },
];

/**
 * 获取报表标题
 */
const getReportTitle = () => {
  const titleMap = {
    MONTHLY: '月度考勤汇总',
    DEPARTMENT: '部门考勤报表',
    PERSONAL: '个人考勤报表',
  };
  return titleMap[reportType.value];
};

/**
 * 报表类型变化
 */
const handleReportTypeChange = (value: 'MONTHLY' | 'DEPARTMENT' | 'PERSONAL') => {
  reportType.value = value;
  queryForm.employeeId = undefined;
  queryForm.departmentId = undefined;
  handleQuery();
};

/**
 * 月份变化
 */
const handleMonthChange = (date: Dayjs | null) => {
  selectedMonth.value = date as Dayjs;
  if (date) {
    const year = date.year();
    const month = date.month() + 1;
    const startDay = dayjs(`${year}-${month}-01`);
    const endDay = startDay.endOf('month');

    queryForm.startDate = startDay.format('YYYY-MM-DD');
    queryForm.endDate = endDay.format('YYYY-MM-DD');
  } else {
    queryForm.startDate = '';
    queryForm.endDate = '';
  }
};

/**
 * 查询报表数据
 */
const queryReportData = async () => {
  loading.value = true;
  try {
    const result = await reportApi.getMonthlyReport(queryForm);
    if (result.data) {
      tableData.value = result.data || [];
      pagination.total = result.data?.length || 0;
    }

    // 同时加载统计数据
    await loadStatistics();
  } catch (error) {
    console.error('加载报表数据失败', error);
    message.error('加载报表数据失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    let result;
    if (reportType.value === 'DEPARTMENT' && queryForm.departmentId) {
      result = await reportApi.getDepartmentStatistics(
        queryForm.departmentId,
        queryForm.startDate,
        queryForm.endDate
      );
    } else if (reportType.value === 'PERSONAL' && queryForm.employeeId) {
      result = await reportApi.getPersonalStatistics(
        queryForm.employeeId,
        queryForm.startDate,
        queryForm.endDate
      );
    } else {
      result = await reportApi.getCompanyStatistics(queryForm.startDate, queryForm.endDate);
    }

    if (result.data) {
      statisticsData.value = result.data;
    }

    // 加载图表数据
    await loadChartData();
  } catch (error) {
    console.error('加载统计数据失败', error);
  }
};

/**
 * 加载图表数据
 */
const loadChartData = async () => {
  chartLoading.value = true;
  try {
    const form = {
      employeeId: queryForm.employeeId,
      departmentId: queryForm.departmentId,
      startDate: queryForm.startDate,
      endDate: queryForm.endDate,
      statisticsType:
        reportType.value === 'DEPARTMENT'
          ? 'DEPARTMENT'
          : reportType.value === 'PERSONAL'
          ? 'PERSONAL'
          : 'COMPANY',
    };

    const result = await reportApi.getChartData(form);
    if (result.data) {
      chartData.value = result.data;
    }
  } catch (error) {
    console.error('加载图表数据失败', error);
  } finally {
    chartLoading.value = false;
  }
};

/**
 * 查询按钮
 */
const handleQuery = () => {
  pagination.current = 1;
  queryReportData();
};

/**
 * 重置按钮
 */
const handleReset = () => {
  queryForm.employeeId = undefined;
  queryForm.departmentId = undefined;
  queryForm.reportType = undefined;
  selectedMonth.value = dayjs();
  handleMonthChange(selectedMonth.value);
  handleQuery();
};

/**
 * 表格变化
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
};

/**
 * 导出
 */
const handleExport = async () => {
  try {
    await reportApi.exportReport(queryForm);
    message.success('导出成功');
  } catch (error) {
    console.error('导出失败', error);
    message.error('导出失败');
  }
};

/**
 * 搜索过滤
 */
const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

/**
 * 初始化
 */
onMounted(() => {
  handleMonthChange(selectedMonth.value);
  queryReportData();
});
</script>

<style scoped lang="less">
.report-management {
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

  .chart-card {
    min-height: 400px;
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
