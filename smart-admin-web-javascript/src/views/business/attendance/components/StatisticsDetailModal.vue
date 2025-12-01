<!--
 * 考勤统计详情弹窗组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-25
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="modalVisible"
    title="考勤统计详情"
    :width="1000"
    :footer="null"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <div v-if="detailData">
        <!-- 基础信息 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">基础信息</span>
          </template>
          <a-descriptions :column="3" bordered>
            <a-descriptions-item label="员工姓名">
              {{ detailData.employeeName }}
            </a-descriptions-item>
            <a-descriptions-item label="员工编号">
              {{ detailData.employeeCode }}
            </a-descriptions-item>
            <a-descriptions-item label="所属部门">
              {{ detailData.departmentName }}
            </a-descriptions-item>
            <a-descriptions-item label="统计周期" :span="2">
              {{ queryForm?.startDate }} ~ {{ queryForm?.endDate }}
            </a-descriptions-item>
            <a-descriptions-item label="统计类型">
              {{ getStatisticsTypeText(queryForm?.statisticsType) }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 出勤统计 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">出勤统计</span>
          </template>
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic
                title="应出勤天数"
                :value="detailData.shouldWorkDays || 0"
                suffix="天"
                :value-style="{ color: '#1890ff' }"
              >
                <template #prefix>
                  <CalendarOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="实际出勤天数"
                :value="detailData.actualWorkDays || 0"
                suffix="天"
                :value-style="{ color: '#52c41a' }"
              >
                <template #prefix>
                  <CheckCircleOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="出勤率"
                :value="detailData.attendanceRate || 0"
                suffix="%"
                :value-style="{ color: getAttendanceRateColor(detailData.attendanceRate) }"
                :precision="1"
              >
                <template #prefix>
                  <PercentageOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="异常次数"
                :value="detailData.abnormalCount || 0"
                suffix="次"
                :value-style="{ color: '#f5222d' }"
              >
                <template #prefix>
                  <ExclamationCircleOutlined />
                </template>
              </a-statistic>
            </a-col>
          </a-row>

          <!-- 出勤率进度条 -->
          <div class="mt-4">
            <a-progress
              :percent="detailData.attendanceRate || 0"
              :status="getAttendanceRateStatus(detailData.attendanceRate)"
              :stroke-color="getAttendanceRateColor(detailData.attendanceRate)"
            />
          </div>
        </a-card>

        <!-- 异常统计 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">异常统计</span>
          </template>
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic
                title="迟到次数"
                :value="detailData.lateCount || 0"
                suffix="次"
                :value-style="{ color: getLateCountColor(detailData.lateCount) }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="早退次数"
                :value="detailData.earlyLeaveCount || 0"
                suffix="次"
                :value-style="{ color: getEarlyLeaveCountColor(detailData.earlyLeaveCount) }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="缺勤天数"
                :value="detailData.absenceCount || 0"
                suffix="天"
                :value-style="{ color: detailData.absenceCount > 0 ? '#f5222d' : '#52c41a' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="请假天数"
                :value="detailData.leaveCount || 0"
                suffix="天"
                :value-style="{ color: '#fa8c16' }"
              />
            </a-col>
          </a-row>
        </a-card>

        <!-- 工作时长统计 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">工作时长统计</span>
          </template>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-statistic
                title="标准工作时长"
                :value="detailData.standardWorkHours || 0"
                suffix="小时"
                :value-style="{ color: '#1890ff' }"
              />
            </a-col>
            <a-col :span="8">
              <a-statistic
                title="实际工作时长"
                :value="detailData.workHours || 0"
                suffix="小时"
                :value-style="{ color: getWorkHoursColor(detailData.workHours, detailData.standardWorkHours) }"
              />
            </a-col>
            <a-col :span="8">
              <a-statistic
                title="加班时长"
                :value="detailData.overtimeHours || 0"
                suffix="小时"
                :value-style="{ color: '#722ed1' }"
              />
            </a-col>
          </a-row>
          <div class="mt-4">
            <span :class="getWorkHoursClass(detailData.workHours, detailData.standardWorkHours)">
              工作时长达标情况：{{ getWorkHoursStatus(detailData.workHours, detailData.standardWorkHours) }}
            </span>
          </div>
        </a-card>

        <!-- 详细记录 -->
        <a-card :bordered="false" v-if="showDetailRecords">
          <template #title>
            <span class="text-lg font-medium">详细记录</span>
          </template>
          <a-table
            :columns="detailColumns"
            :data-source="detailRecords"
            :pagination="false"
            size="small"
            :scroll="{ x: 800 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'status'">
                <a-tag :color="getStatusColor(record.status)">
                  {{ getStatusText(record.status) }}
                </a-tag>
              </template>
              <template v-if="column.dataIndex === 'workHours'">
                <span :class="getWorkHoursClass(record.workHours, 8)">
                  {{ record.workHours }}h
                </span>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- 趋势分析 -->
        <a-card :bordered="false" v-if="showTrendAnalysis">
          <template #title>
            <span class="text-lg font-medium">趋势分析</span>
          </template>
          <div class="mb-4">
            <a-row :gutter="16">
              <a-col :span="8">
                <a-statistic
                  title="较上期出勤率变化"
                  :value="detailData.attendanceRateTrend || 0"
                  suffix="%"
                  :value-style="{ color: getTrendColor(detailData.attendanceRateTrend) }"
                  :precision="1"
                >
                  <template #prefix>
                    <ArrowUpOutlined v-if="detailData.attendanceRateTrend > 0" />
                    <ArrowDownOutlined v-else-if="detailData.attendanceRateTrend < 0" />
                    <MinusOutlined v-else />
                  </template>
                </a-statistic>
              </a-col>
              <a-col :span="8">
                <a-statistic
                  title="较上期工作时长变化"
                  :value="detailData.workHoursTrend || 0"
                  suffix="小时"
                  :value-style="{ color: getTrendColor(detailData.workHoursTrend) }"
                  :precision="1"
                >
                  <template #prefix>
                    <ArrowUpOutlined v-if="detailData.workHoursTrend > 0" />
                    <ArrowDownOutlined v-else-if="detailData.workHoursTrend < 0" />
                    <MinusOutlined v-else />
                  </template>
                </a-statistic>
              </a-col>
              <a-col :span="8">
                <a-statistic
                  title="较上期异常次数变化"
                  :value="detailData.abnormalCountTrend || 0"
                  suffix="次"
                  :value-style="{ color: getTrendColor(-detailData.abnormalCountTrend) }"
                >
                  <template #prefix>
                    <ArrowUpOutlined v-if="detailData.abnormalCountTrend < 0" />
                    <ArrowDownOutlined v-else-if="detailData.abnormalCountTrend > 0" />
                    <MinusOutlined v-else />
                  </template>
                </a-statistic>
              </a-col>
            </a-row>
          </div>
        </a-card>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import {
  CalendarOutlined,
  CheckCircleOutlined,
  PercentageOutlined,
  ExclamationCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  MinusOutlined
} from '@ant-design/icons-vue';

// Props定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  detailData: {
    type: Object,
    default: null
  },
  queryForm: {
    type: Object,
    default: null
  },
  showDetailRecords: {
    type: Boolean,
    default: false
  },
  showTrendAnalysis: {
    type: Boolean,
    default: false
  }
});

// Emits定义
const emit = defineEmits(['update:visible']);

// 响应式数据
const modalVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

const loading = ref(false);
const detailRecords = ref([]);

// 详细记录表格列
const detailColumns = [
  {
    title: '日期',
    dataIndex: 'date',
    width: 100,
    fixed: 'left'
  },
  {
    title: '上班时间',
    dataIndex: 'clockInTime',
    width: 100
  },
  {
    title: '下班时间',
    dataIndex: 'clockOutTime',
    width: 100
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80
  },
  {
    title: '工作时长',
    dataIndex: 'workHours',
    width: 80,
    align: 'center'
  },
  {
    title: '备注',
    dataIndex: 'remark',
    ellipsis: true
  }
];

// 监听详情数据变化
watch(
  () => props.detailData,
  (newData) => {
    if (newData && props.showDetailRecords) {
      loadDetailRecords();
    }
  },
  { immediate: true }
);

// ============ 方法 ============

// 加载详细记录
const loadDetailRecords = async () => {
  // 这里应该调用API加载详细记录数据
  // detailRecords.value = await attendanceApi.getDetailRecords(props.detailData.employeeId, props.queryForm);
  detailRecords.value = [];
};

// 获取统计类型文本
const getStatisticsTypeText = (type) => {
  const typeMap = {
    'personal': '个人统计',
    'department': '部门统计',
    'company': '公司统计',
    'daily': '按日统计',
    'weekly': '按周统计',
    'monthly': '按月统计',
    'yearly': '按年统计'
  };
  return typeMap[type] || '未知';
};

// 获取出勤率颜色
const getAttendanceRateColor = (rate) => {
  if (rate >= 95) return '#52c41a';
  if (rate >= 80) return '#fa8c16';
  return '#f5222d';
};

// 获取出勤率状态
const getAttendanceRateStatus = (rate) => {
  if (rate >= 95) return 'success';
  if (rate >= 80) return 'normal';
  return 'exception';
};

// 获取迟到次数颜色
const getLateCountColor = (count) => {
  if (count > 3) return '#f5222d';
  if (count > 0) return '#fa8c16';
  return '#52c41a';
};

// 获取早退次数颜色
const getEarlyLeaveCountColor = (count) => {
  if (count > 3) return '#f5222d';
  if (count > 0) return '#fa8c16';
  return '#52c41a';
};

// 获取工作时长颜色
const getWorkHoursColor = (actual, standard) => {
  if (actual >= standard) return '#52c41a';
  if (actual >= standard * 0.9) return '#fa8c16';
  return '#f5222d';
};

// 获取工作时长样式类
const getWorkHoursClass = (actual, standard) => {
  if (actual >= standard) return 'text-success';
  if (actual >= standard * 0.9) return 'text-warning';
  return 'text-danger';
};

// 获取工作时长状态
const getWorkHoursStatus = (actual, standard) => {
  if (actual >= standard) return '已达标';
  if (actual >= standard * 0.9) return '接近达标';
  return '未达标';
};

// 获取趋势颜色
const getTrendColor = (trend) => {
  if (trend > 0) return '#52c41a';
  if (trend < 0) return '#f5222d';
  return '#8c8c8c';
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'NORMAL': 'green',
    'LATE': 'orange',
    'EARLY_LEAVE': 'orange',
    'ABSENCE': 'red',
    'LEAVE': 'blue',
    'WEEKEND': 'purple',
    'HOLIDAY': 'purple'
  };
  return colorMap[status] || 'default';
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'LATE': '迟到',
    'EARLY_LEAVE': '早退',
    'ABSENCE': '缺勤',
    'LEAVE': '请假',
    'WEEKEND': '周末',
    'HOLIDAY': '节假日'
  };
  return textMap[status] || '未知';
};

// 取消操作
const handleCancel = () => {
  modalVisible.value = false;
};
</script>

<style lang="less" scoped>
.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.text-success {
  color: #52c41a;
  font-weight: 500;
}

.text-warning {
  color: #fa8c16;
  font-weight: 500;
}

.text-danger {
  color: #f5222d;
  font-weight: 500;
}

.text-lg {
  font-size: 16px;
}

.font-medium {
  font-weight: 500;
}
</style>