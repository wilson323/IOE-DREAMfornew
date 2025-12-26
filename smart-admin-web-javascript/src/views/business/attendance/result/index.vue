<!--
  @fileoverview 考勤结果查询主页面
  @author IOE-DREAM Team
  @description 考勤记录查询、统计、导出功能
-->
<template>
  <div class="attendance-result-page">
    <!-- 查询表单 -->
    <a-card :bordered="false" class="query-card">
      <a-form layout="inline" class="smart-query-form">
        <a-row :gutter="[16, 16]" class="smart-query-form-row">
          <a-form-item label="姓名" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.userName"
              placeholder="请输入姓名"
              style="width: 160px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="部门" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.departmentId"
              placeholder="请选择部门"
              style="width: 160px"
              allow-clear
            >
              <a-select-option
                v-for="dept in departmentOptions"
                :key="dept.departmentId"
                :value="dept.departmentId"
              >
                {{ dept.departmentName }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="日期范围" class="smart-query-form-item">
            <a-range-picker
              v-model:value="dateRangeValue"
              format="YYYY-MM-DD"
              style="width: 280px"
              @change="handleDateRangeChange"
            />
          </a-form-item>

          <a-form-item label="考勤状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.attendanceStatus"
              placeholder="请选择状态"
              style="width: 140px"
              allow-clear
            >
              <a-select-option value="NORMAL">正常</a-select-option>
              <a-select-option value="LATE">迟到</a-select-option>
              <a-select-option value="EARLY_LEAVE">早退</a-select-option>
              <a-select-option value="ABSENT">缺勤</a-select-option>
              <a-select-option value="OVERTIME">加班</a-select-option>
              <a-select-option value="LEAVE">请假</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryRecords">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a-button @click="handleExportRecords">
                <template #icon><DownloadOutlined /></template>
                导出
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 统计概览 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>考勤统计</span>
          <a-tag color="blue">
            {{ statisticsData.startDate }} ~ {{ statisticsData.endDate }}
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-radio-group v-model:value="statisticsView" button-style="solid" size="small">
          <a-radio-button value="OVERVIEW">总览</a-radio-button>
          <a-radio-button value="DEPARTMENT">部门</a-radio-button>
          <a-radio-button value="TREND">趋势</a-radio-button>
        </a-radio-group>
      </template>

      <!-- 总览统计 -->
      <template v-if="statisticsView === 'OVERVIEW'">
        <a-row :gutter="16" class="statistics-overview">
          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-statistic
              title="总打卡次数"
              :value="statisticsData.totalCount"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-statistic>
          </a-col>

          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-statistic
              title="正常打卡"
              :value="statisticsData.normalCount"
              :value-style="{ color: '#52c41a' }"
              :suffix="`(${statisticsData.normalRate}%)`"
            >
              <template #prefix>
                <CheckCircleOutlined />
              </template>
            </a-statistic>
          </a-col>

          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-statistic
              title="迟到次数"
              :value="statisticsData.lateCount"
              :value-style="{ color: '#faad14' }"
              :suffix="`(${statisticsData.lateRate}%)`"
            >
              <template #prefix>
                <ClockCircleOutlined />
              </template>
            </a-statistic>
          </a-col>

          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-statistic
              title="早退次数"
              :value="statisticsData.earlyCount"
              :value-style="{ color: '#faad14' }"
              :suffix="`(${statisticsData.earlyRate}%)`"
            >
              <template #prefix>
                <MinusCircleOutlined />
              </template>
            </a-statistic>
          </a-col>

          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-statistic
              title="缺勤次数"
              :value="statisticsData.absentCount"
              :value-style="{ color: '#f5222d' }"
              :suffix="`(${statisticsData.absentRate}%)`"
            >
              <template #prefix>
                <CloseCircleOutlined />
              </template>
            </a-statistic>
          </a-col>

          <a-col :xs="12" :sm="8" :md="6" :lg="4">
            <a-statistic
              title="加班次数"
              :value="statisticsData.overtimeCount"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <PlusCircleOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
      </template>

      <!-- 部门统计 -->
      <template v-else-if="statisticsView === 'DEPARTMENT'">
        <a-table
          :columns="departmentColumns"
          :data-source="departmentStatistics"
          :pagination="false"
          size="small"
          :scroll="{ x: 800 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'attendanceRate'">
              <a-progress
                :percent="record.attendanceRate"
                :stroke-color="getProgressColor(record.attendanceRate)"
              />
            </template>
          </template>
        </a-table>
      </template>

      <!-- 趋势图 -->
      <template v-else-if="statisticsView === 'TREND'">
        <TrendChart :trend-data="trendData" />
      </template>
    </a-card>

    <!-- 考勤记录列表 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>考勤记录</span>
          <a-tag v-if="selectedRowKeys.length > 0" color="blue">
            已选 {{ selectedRowKeys.length }} 项
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleExportStatistics">
            <template #icon><FileExcelOutlined /></template>
            导出统计报表
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="recordId"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'attendanceStatus'">
            <a-tag :color="getStatusColor(record.attendanceStatus)">
              {{ getStatusText(record.attendanceStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'attendanceType'">
            <a-tag :color="record.attendanceType === 'CHECK_IN' ? 'green' : 'orange'">
              {{ record.attendanceType === 'CHECK_IN' ? '上班' : '下班' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewDetail(record)">详情</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:open="detailVisible"
      title="考勤记录详情"
      width="500"
    >
      <a-descriptions :column="1" bordered size="small" v-if="currentRecord">
        <a-descriptions-item label="姓名">
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
        <a-descriptions-item label="打卡时间">
          {{ currentRecord.punchTime }}
        </a-descriptions-item>
        <a-descriptions-item label="考勤状态">
          <a-tag :color="getStatusColor(currentRecord.attendanceStatus)">
            {{ getStatusText(currentRecord.attendanceStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="考勤类型">
          <a-tag :color="currentRecord.attendanceType === 'CHECK_IN' ? 'green' : 'orange'">
            {{ currentRecord.attendanceType === 'CHECK_IN' ? '上班' : '下班' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="打卡地址">
          {{ currentRecord.punchAddress || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="设备名称">
          {{ currentRecord.deviceName || '-' }}
        </a-descriptions-item>
      </a-descriptions>
      <a-empty v-else />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  DownloadOutlined,
  FileExcelOutlined,
  UserOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  MinusCircleOutlined,
  CloseCircleOutlined,
  PlusCircleOutlined
} from '@ant-design/icons-vue';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { smartSentry } from '/@/lib/smart-sentry';
import {
  resultApi,
  type ResultQueryForm,
  type StatisticsQueryForm,
  type AttendanceRecordVO,
  type AttendanceStatisticsVO,
  type DepartmentStatisticsVO,
  type AttendanceTrendVO
} from '@/api/business/attendance/result';
import TrendChart from './components/TrendChart.vue';

/**
 * 组件状态
 */
const queryFormState = {
  userName: null,
  departmentId: null,
  startDate: dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  endDate: dayjs().format('YYYY-MM-DD'),
  attendanceStatus: null,
  attendanceType: null,
  shiftId: null,
  pageNum: 1,
  pageSize: PAGE_SIZE,
};

const queryForm = reactive({ ...queryFormState });
const tableData = ref<AttendanceRecordVO[]>([]);
const loading = ref(false);
const selectedRowKeys = ref<number[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: PAGE_SIZE_OPTIONS,
  showTotal: (total: number) => `共 ${total} 条记录`,
});

const columns = [
  { title: '姓名', dataIndex: 'userName', key: 'userName', width: 100, fixed: 'left' },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 120 },
  { title: '考勤日期', dataIndex: 'attendanceDate', key: 'attendanceDate', width: 120 },
  { title: '打卡时间', dataIndex: 'punchTime', key: 'punchTime', width: 160 },
  { title: '班次', dataIndex: 'shiftName', key: 'shiftName', width: 100 },
  { title: '考勤状态', key: 'attendanceStatus', width: 100 },
  { title: '考勤类型', key: 'attendanceType', width: 100 },
  { title: '打卡地点', dataIndex: 'punchAddress', key: 'punchAddress', width: 200, ellipsis: true },
  { title: '设备', dataIndex: 'deviceName', key: 'deviceName', width: 120 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' },
];

const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys;
  },
};

// 部门选项
const departmentOptions = ref([
  { departmentId: 1, departmentName: '技术部' },
  { departmentId: 2, departmentName: '市场部' },
  { departmentId: 3, departmentName: '行政部' },
  { departmentId: 4, departmentName: '财务部' },
  { departmentId: 5, departmentName: '人事部' }
]);

// 日期范围选择器
const dateRangeValue = ref<[Dayjs, Dayjs]>([
  dayjs().subtract(7, 'day'),
  dayjs()
]);

// 统计数据
const statisticsView = ref<'OVERVIEW' | 'DEPARTMENT' | 'TREND'>('OVERVIEW');
const statisticsData = ref<AttendanceStatisticsVO>({
  totalCount: 0,
  normalCount: 0,
  lateCount: 0,
  earlyCount: 0,
  absentCount: 0,
  overtimeCount: 0,
  normalRate: 0,
  lateRate: 0,
  earlyRate: 0,
  absentRate: 0
});

// 部门统计数据
const departmentStatistics = ref<DepartmentStatisticsVO[]>([]);

const departmentColumns = [
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 150 },
  { title: '员工数', dataIndex: 'employeeCount', key: 'employeeCount', width: 100 },
  { title: '出勤人数', dataIndex: 'presentEmployees', key: 'presentEmployees', width: 100 },
  { title: '迟到人数', dataIndex: 'lateEmployees', key: 'lateEmployees', width: 100 },
  { title: '早退人数', dataIndex: 'earlyLeaveEmployees', key: 'earlyLeaveEmployees', width: 100 },
  { title: '缺勤人数', dataIndex: 'absentEmployees', key: 'absentEmployees', width: 100 },
  { title: '出勤率', key: 'attendanceRate', width: 200 },
];

// 趋势数据
const trendData = ref<AttendanceTrendVO[]>([]);

// 详情状态
const detailVisible = ref(false);
const currentRecord = ref<AttendanceRecordVO | null>(null);

/**
 * 获取状态颜色
 */
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    NORMAL: 'success',
    LATE: 'warning',
    EARLY_LEAVE: 'warning',
    ABSENT: 'error',
    OVERTIME: 'processing',
    LEAVE: 'default'
  };
  return colorMap[status] || 'default';
};

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    NORMAL: '正常',
    LATE: '迟到',
    EARLY_LEAVE: '早退',
    ABSENT: '缺勤',
    OVERTIME: '加班',
    LEAVE: '请假'
  };
  return textMap[status] || status;
};

/**
 * 获取进度条颜色
 */
const getProgressColor = (percent: number) => {
  if (percent >= 90) return '#52c41a';
  if (percent >= 80) return '#1890ff';
  if (percent >= 70) return '#faad14';
  return '#f5222d';
};

/**
 * 日期范围变化处理
 */
const handleDateRangeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates[0] && dates[1]) {
    queryForm.startDate = dates[0].format('YYYY-MM-DD');
    queryForm.endDate = dates[1].format('YYYY-MM-DD');
  } else {
    queryForm.startDate = dayjs().subtract(7, 'day').format('YYYY-MM-DD');
    queryForm.endDate = dayjs().format('YYYY-MM-DD');
  }
};

/**
 * 查询考勤记录
 */
const queryRecords = async () => {
  loading.value = true;
  try {
    const result = await resultApi.queryRecords(queryForm);
    if (result.data) {
      tableData.value = result.data.list || [];
      pagination.total = result.data.total || 0;
    }

    // 同时加载统计数据
    await loadStatistics();
  } catch (error) {
    smartSentry.captureError(error);
    message.error('加载考勤记录失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const params: StatisticsQueryForm = {
      startDate: queryForm.startDate,
      endDate: queryForm.endDate,
      departmentId: queryForm.departmentId
    };

    const result = await resultApi.getStatistics(params);
    if (result.data) {
      statisticsData.value = result.data;
      // 添加日期范围
      statisticsData.value.startDate = queryForm.startDate;
      statisticsData.value.endDate = queryForm.endDate;
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

/**
 * 加载部门统计
 */
const loadDepartmentStatistics = async () => {
  try {
    const params: StatisticsQueryForm = {
      startDate: queryForm.startDate,
      endDate: queryForm.endDate,
      groupBy: 'DEPARTMENT'
    };

    // TODO: 调用部门统计API
    // const result = await resultApi.getDepartmentStatistics(params);
    // departmentStatistics.value = result.data || [];

    // 模拟数据
    departmentStatistics.value = [
      {
        departmentId: 1,
        departmentName: '技术部',
        employeeCount: 30,
        presentEmployees: 28,
        lateEmployees: 3,
        earlyLeaveEmployees: 2,
        absentEmployees: 1,
        attendanceRate: 93.3
      },
      {
        departmentId: 2,
        departmentName: '市场部',
        employeeCount: 25,
        presentEmployees: 24,
        lateEmployees: 2,
        earlyLeaveEmployees: 1,
        absentEmployees: 0,
        attendanceRate: 96.0
      }
    ];
  } catch (error) {
    console.error('加载部门统计失败:', error);
  }
};

/**
 * 加载趋势数据
 */
const loadTrendData = async () => {
  try {
    // TODO: 调用趋势数据API
    // const result = await resultApi.getTrendData({
    //   startDate: queryForm.startDate,
    //   endDate: queryForm.endDate,
    //   dimension: 'DAILY'
    // });
    // trendData.value = result.data || [];

    // 模拟数据
    trendData.value = [
      {
        date: '2025-01-24',
        totalCount: 100,
        normalCount: 85,
        lateCount: 8,
        earlyCount: 5,
        absentCount: 2,
        normalRate: 85.0
      },
      {
        date: '2025-01-25',
        totalCount: 100,
        normalCount: 90,
        lateCount: 5,
        earlyCount: 3,
        absentCount: 2,
        normalRate: 90.0
      },
      {
        date: '2025-01-26',
        totalCount: 100,
        normalCount: 88,
        lateCount: 6,
        earlyCount: 4,
        absentCount: 2,
        normalRate: 88.0
      }
    ];
  } catch (error) {
    console.error('加载趋势数据失败:', error);
  }
};

/**
 * 重置查询
 */
const resetQuery = () => {
  Object.assign(queryForm, queryFormState);
  dateRangeValue.value = [dayjs().subtract(7, 'day'), dayjs()];
  selectedRowKeys.value = [];
  queryRecords();
};

/**
 * 表格变化处理
 */
const handleTableChange = (pag: any) => {
  queryForm.pageNum = pag.current;
  queryForm.pageSize = pag.pageSize;
  queryRecords();
};

/**
 * 查看详情
 */
const viewDetail = (record: AttendanceRecordVO) => {
  currentRecord.value = record;
  detailVisible.value = true;
};

/**
 * 导出考勤记录
 */
const handleExportRecords = async () => {
  try {
    const blob = await resultApi.exportRecords(queryForm);
    // TODO: 处理文件下载
    message.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    message.error('导出失败');
  }
};

/**
 * 导出统计报表
 */
const handleExportStatistics = async () => {
  try {
    const params: StatisticsQueryForm = {
      startDate: queryForm.startDate,
      endDate: queryForm.endDate,
      departmentId: queryForm.departmentId
    };

    const blob = await resultApi.exportStatistics(params);
    // TODO: 处理文件下载
    message.success('导出统计报表成功');
  } catch (error) {
    console.error('导出统计报表失败:', error);
    message.error('导出统计报表失败');
  }
};

/**
 * 监听统计视图变化
 */
async function handleStatisticsViewChange() {
  if (statisticsView.value === 'DEPARTMENT') {
    await loadDepartmentStatistics();
  } else if (statisticsView.value === 'TREND') {
    await loadTrendData();
  }
}

/**
 * 使用watch监听统计视图变化
 */
import { watch } from 'vue';
watch(statisticsView, () => {
  handleStatisticsViewChange();
});

onMounted(() => {
  queryRecords();
});
</script>

<style lang="less" scoped>
.attendance-result-page {
  .query-card {
    margin-bottom: 16px;
  }

  .statistics-overview {
    text-align: center;
    padding: 16px 0;
  }
}
</style>
