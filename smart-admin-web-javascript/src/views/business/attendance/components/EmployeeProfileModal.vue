<!--
 * 员工画像弹窗组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-25
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="modalVisible"
    title="员工考勤画像"
    :width="1200"
    :footer="null"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <div v-if="profileData">
        <!-- 员工基本信息 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">员工基本信息</span>
          </template>
          <a-descriptions :column="4" bordered>
            <a-descriptions-item label="员工姓名">
              {{ profileData.employeeName }}
            </a-descriptions-item>
            <a-descriptions-item label="员工编号">
              {{ profileData.employeeCode }}
            </a-descriptions-item>
            <a-descriptions-item label="所属部门">
              {{ profileData.departmentName }}
            </a-descriptions-item>
            <a-descriptions-item label="职位">
              {{ profileData.positionName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="入职日期">
              {{ formatDate(profileData.hireDate) }}
            </a-descriptions-item>
            <a-descriptions-item label="工作状态">
              <a-badge :status="getWorkStatusBadge(profileData.workStatus)" :text="getWorkStatusText(profileData.workStatus)" />
            </a-descriptions-item>
            <a-descriptions-item label="联系方式">
              {{ profileData.phone || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 考勤概览 -->
        <a-row :gutter="16" class="mb-4">
          <a-col :span="6">
            <a-card :bordered="false">
              <a-statistic
                title="本月出勤率"
                :value="profileData.monthlyAttendanceRate || 0"
                suffix="%"
                :value-style="{ color: getAttendanceRateColor(profileData.monthlyAttendanceRate) }"
                :precision="1"
              >
                <template #prefix>
                  <CalendarOutlined />
                </template>
              </a-statistic>
              <div class="mt-2">
                <span :class="getTrendClass(profileData.attendanceRateTrend)">
                  <ArrowUpOutlined v-if="profileData.attendanceRateTrend > 0" />
                  <ArrowDownOutlined v-else-if="profileData.attendanceRateTrend < 0" />
                  <MinusOutlined v-else />
                  {{ Math.abs(profileData.attendanceRateTrend || 0) }}%
                </span>
                <span class="text-gray-500 ml-1">较上月</span>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false">
              <a-statistic
                title="平均工作时长"
                :value="profileData.avgWorkHours || 0"
                suffix="小时"
                :value-style="{ color: '#1890ff' }"
                :precision="1"
              >
                <template #prefix>
                  <ClockCircleOutlined />
                </template>
              </a-statistic>
              <div class="mt-2">
                <span :class="getTrendClass(profileData.workHoursTrend)">
                  <ArrowUpOutlined v-if="profileData.workHoursTrend > 0" />
                  <ArrowDownOutlined v-else-if="profileData.workHoursTrend < 0" />
                  <MinusOutlined v-else />
                  {{ Math.abs(profileData.workHoursTrend || 0) }}h
                </span>
                <span class="text-gray-500 ml-1">较上月</span>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false">
              <a-statistic
                title="迟到次数"
                :value="profileData.lateCount || 0"
                suffix="次"
                :value-style="{ color: getLateCountColor(profileData.lateCount) }"
              />
              <div class="mt-2">
                <span :class="getTrendClass(-profileData.lateCountTrend)">
                  <ArrowUpOutlined v-if="profileData.lateCountTrend < 0" />
                  <ArrowDownOutlined v-else-if="profileData.lateCountTrend > 0" />
                  <MinusOutlined v-else />
                  {{ Math.abs(profileData.lateCountTrend || 0) }}次
                </span>
                <span class="text-gray-500 ml-1">较上月</span>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false">
              <a-statistic
                title="加班时长"
                :value="profileData.overtimeHours || 0"
                suffix="小时"
                :value-style="{ color: '#722ed1' }"
                :precision="1"
              >
                <template #prefix>
                  <ThunderboltOutlined />
                </template>
              </a-statistic>
              <div class="mt-2">
                <span :class="getTrendClass(profileData.overtimeTrend)">
                  <ArrowUpOutlined v-if="profileData.overtimeTrend > 0" />
                  <ArrowDownOutlined v-else-if="profileData.overtimeTrend < 0" />
                  <MinusOutlined v-else />
                  {{ Math.abs(profileData.overtimeTrend || 0) }}h
                </span>
                <span class="text-gray-500 ml-1">较上月</span>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 考勤行为分析 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">考勤行为分析</span>
          </template>
          <a-row :gutter="16">
            <a-col :span="12">
              <h4>出勤规律性</h4>
              <div class="behavior-analysis">
                <div class="behavior-item">
                  <span class="behavior-label">准点率：</span>
                  <a-progress
                    :percent="profileData.punctualityRate || 0"
                    size="small"
                    :stroke-color="getAttendanceRateColor(profileData.punctualityRate)"
                  />
                  <span class="behavior-value">{{ profileData.punctualityRate || 0 }}%</span>
                </div>
                <div class="behavior-item">
                  <span class="behavior-label">规律性评分：</span>
                  <a-rate
                    :value="profileData.regularityScore || 0"
                    disabled
                    :tooltips="['很差', '较差', '一般', '良好', '优秀']"
                  />
                </div>
                <div class="behavior-item">
                  <span class="behavior-label">工作时长稳定性：</span>
                  <a-tag :color="getWorkHoursStabilityColor(profileData.workHoursStability)">
                    {{ getWorkHoursStabilityText(profileData.workHoursStability) }}
                  </a-tag>
                </div>
              </div>
            </a-col>
            <a-col :span="12">
              <h4>异常倾向</h4>
              <div class="behavior-analysis">
                <div class="behavior-item">
                  <span class="behavior-label">迟到倾向：</span>
                  <a-tag :color="getTendencyColor(profileData.lateTendency)">
                    {{ getTendencyText(profileData.lateTendency) }}
                  </a-tag>
                </div>
                <div class="behavior-item">
                  <span class="behavior-label">早退倾向：</span>
                  <a-tag :color="getTendencyColor(profileData.earlyLeaveTendency)">
                    {{ getTendencyText(profileData.earlyLeaveTendency) }}
                  </a-tag>
                </div>
                <div class="behavior-item">
                  <span class="behavior-label">缺勤风险：</span>
                  <a-tag :color="getTendencyColor(profileData.absenceRisk)">
                    {{ getTendencyText(profileData.absenceRisk) }}
                  </a-tag>
                </div>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 月度趋势图表 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">月度趋势</span>
          </template>
          <div ref="trendChartRef" style="height: 300px;"></div>
        </a-card>

        <!-- 部门对比 -->
        <a-card :bordered="false">
          <template #title>
            <span class="text-lg font-medium">部门对比</span>
          </template>
          <a-table
            :columns="comparisonColumns"
            :data-source="departmentComparison"
            :pagination="false"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'attendanceRate'">
                <a-progress
                  :percent="record.attendanceRate"
                  size="small"
                  :status="getAttendanceRateStatus(record.attendanceRate)"
                />
              </template>
              <template v-if="column.dataIndex === 'ranking'">
                <span :class="getRankingClass(record.ranking, record.total)">
                  第{{ record.ranking }}名
                </span>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  CalendarOutlined,
  ClockCircleOutlined,
  ThunderboltOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  MinusOutlined
} from '@ant-design/icons-vue';
import * as echarts from 'echarts';

// Props定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  employeeId: {
    type: [Number, String],
    required: true
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
const profileData = ref(null);
const trendChartRef = ref();
const departmentComparison = ref([]);

// 部门对比表格列
const comparisonColumns = [
  {
    title: '部门',
    dataIndex: 'departmentName',
    width: 150
  },
  {
    title: '出勤率',
    dataIndex: 'attendanceRate',
    width: 150,
    align: 'center'
  },
  {
    title: '排名',
    dataIndex: 'ranking',
    width: 100,
    align: 'center'
  },
  {
    title: '部门平均',
    dataIndex: 'departmentAvg',
    width: 120,
    align: 'center'
  },
  {
    title: '差距',
    dataIndex: 'gap',
    width: 120,
    align: 'center'
  }
];

// 监听弹窗显示状态
watch(
  modalVisible,
  (newVisible) => {
    if (newVisible && props.employeeId) {
      loadEmployeeProfile();
    }
  }
);

// ============ 方法 ============

// 加载员工画像数据
const loadEmployeeProfile = async () => {
  try {
    loading.value = true;

    // 这里应该调用实际的API
    // const result = await attendanceApi.getEmployeeProfileAnalysis(props.employeeId);

    // 模拟数据
    profileData.value = {
      employeeName: '张三',
      employeeCode: 'EMP001',
      departmentName: '技术部',
      positionName: '高级开发工程师',
      hireDate: '2022-01-15',
      workStatus: 'ACTIVE',
      phone: '13800138000',
      monthlyAttendanceRate: 96.5,
      attendanceRateTrend: 2.3,
      avgWorkHours: 8.5,
      workHoursTrend: 0.2,
      lateCount: 2,
      lateCountTrend: -1,
      overtimeHours: 12.5,
      overtimeTrend: 3.8,
      punctualityRate: 95.2,
      regularityScore: 4.2,
      workHoursStability: 'HIGH',
      lateTendency: 'LOW',
      earlyLeaveTendency: 'LOW',
      absenceRisk: 'LOW'
    };

    // 加载部门对比数据
    loadDepartmentComparison();

    // 等待DOM更新后绘制图表
    await nextTick();
    renderTrendChart();
  } catch (err) {
    console.error('加载员工画像失败:', err);
    message.error('加载员工画像失败');
  } finally {
    loading.value = false;
  }
};

// 加载部门对比数据
const loadDepartmentComparison = async () => {
  // 模拟部门对比数据
  departmentComparison.value = [
    {
      departmentName: '技术部',
      attendanceRate: 96.5,
      ranking: 3,
      departmentAvg: 94.2,
      gap: 2.3
    },
    {
      departmentName: '人事部',
      attendanceRate: 98.1,
      ranking: 1,
      departmentAvg: 94.2,
      gap: 3.9
    },
    {
      departmentName: '财务部',
      attendanceRate: 97.2,
      ranking: 2,
      departmentAvg: 94.2,
      gap: 3.0
    },
    {
      departmentName: '市场部',
      attendanceRate: 92.8,
      ranking: 8,
      departmentAvg: 94.2,
      gap: -1.4
    }
  ];
};

// 渲染趋势图表
const renderTrendChart = () => {
  if (!trendChartRef.value) return;

  const chart = echarts.init(trendChartRef.value);

  // 模拟月度趋势数据
  const months = ['1月', '2月', '3月', '4月', '5月', '6月'];
  const attendanceRateData = [95.2, 94.8, 96.1, 97.2, 95.8, 96.5];
  const workHoursData = [8.2, 8.3, 8.5, 8.6, 8.4, 8.5];

  const option = {
    title: {
      text: '近6个月考勤趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['出勤率', '平均工作时长']
    },
    xAxis: {
      type: 'category',
      data: months
    },
    yAxis: [
      {
        type: 'value',
        name: '出勤率 (%)',
        min: 0,
        max: 100,
        position: 'left'
      },
      {
        type: 'value',
        name: '工作时长 (小时)',
        min: 0,
        max: 12,
        position: 'right'
      }
    ],
    series: [
      {
        name: '出勤率',
        type: 'line',
        data: attendanceRateData,
        yAxisIndex: 0,
        smooth: true,
        itemStyle: {
          color: '#52c41a'
        }
      },
      {
        name: '平均工作时长',
        type: 'bar',
        data: workHoursData,
        yAxisIndex: 1,
        itemStyle: {
          color: '#1890ff'
        }
      }
    ]
  };

  chart.setOption(option);
};

// 格式化日期
const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-';
};

// 获取工作状态徽标
const getWorkStatusBadge = (status) => {
  const statusMap = {
    'ACTIVE': 'success',
    'INACTIVE': 'default',
    'ON_LEAVE': 'warning',
    'RESIGNED': 'error'
  };
  return statusMap[status] || 'default';
};

// 获取工作状态文本
const getWorkStatusText = (status) => {
  const textMap = {
    'ACTIVE': '在职',
    'INACTIVE': '停职',
    'ON_LEAVE': '休假',
    'RESIGNED': '离职'
  };
  return textMap[status] || '未知';
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

// 获取趋势样式类
const getTrendClass = (trend) => {
  if (trend > 0) return 'text-success';
  if (trend < 0) return 'text-danger';
  return 'text-gray-500';
};

// 获取工作时长稳定性颜色
const getWorkHoursStabilityColor = (stability) => {
  const colorMap = {
    'HIGH': 'green',
    'MEDIUM': 'orange',
    'LOW': 'red'
  };
  return colorMap[stability] || 'default';
};

// 获取工作时长稳定性文本
const getWorkHoursStabilityText = (stability) => {
  const textMap = {
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  };
  return textMap[stability] || '未知';
};

// 获取倾向颜色
const getTendencyColor = (tendency) => {
  const colorMap = {
    'LOW': 'green',
    'MEDIUM': 'orange',
    'HIGH': 'red'
  };
  return colorMap[tendency] || 'default';
};

// 获取倾向文本
const getTendencyText = (tendency) => {
  const textMap = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  };
  return textMap[tendency] || '未知';
};

// 获取排名样式类
const getRankingClass = (ranking, total) => {
  const percentile = ranking / total;
  if (percentile <= 0.25) return 'text-success';
  if (percentile <= 0.5) return 'text-warning';
  return 'text-danger';
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

.mt-2 {
  margin-top: 8px;
}

.ml-1 {
  margin-left: 4px;
}

.text-lg {
  font-size: 16px;
}

.font-medium {
  font-weight: 500;
}

.text-gray-500 {
  color: #8c8c8c;
}

.text-success {
  color: #52c41a;
}

.text-warning {
  color: #fa8c16;
}

.text-danger {
  color: #f5222d;
}

.behavior-analysis {
  .behavior-item {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .behavior-label {
      width: 120px;
      flex-shrink: 0;
      color: #666;
    }

    .behavior-value {
      margin-left: 8px;
      font-weight: 500;
    }
  }
}
</style>