<template>
  <div class="rule-coverage-report-container">
    <!-- 页面头部 -->
    <a-card :bordered="false" class="header-card">
      <a-row :gutter="16" align="middle">
        <a-col :span="12">
          <div class="page-title">
            <UnorderedListOutlined style="font-size: 24px; margin-right: 8px;" />
            <span>规则覆盖率报告</span>
          </div>
        </a-col>
        <a-col :span="12" style="text-align: right;">
          <a-space>
            <a-button type="primary" @click="showGenerateModal">
              <template #icon><PlusOutlined /></template>
              生成报告
            </a-button>
            <a-button @click="loadRecentReports">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 概览卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px;">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="总规则数"
            :value="latestReport?.totalRules || 0"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <AppstoreOutlined style="font-size: 20px;" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="已测试规则"
            :value="latestReport?.testedRules || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <CheckCircleOutlined style="font-size: 20px;" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="覆盖率"
            :value="latestReport?.coverageRate || 0"
            suffix="%"
            :value-style="{ color: getCoverageRateColor(latestReport?.coverageRate) }"
          >
            <template #prefix>
              <DashboardOutlined style="font-size: 20px;" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="测试成功率"
            :value="latestReport?.successRate || 0"
            suffix="%"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <TrophyOutlined style="font-size: 20px;" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 趋势图表和详细列表 -->
    <a-row :gutter="16">
      <a-col :span="16">
        <!-- 覆盖率趋势图 -->
        <a-card title="覆盖率趋势" :bordered="false" style="margin-bottom: 16px;">
          <div ref="trendChartRef" style="height: 350px;"></div>
        </a-card>

        <!-- 规则覆盖详情 -->
        <a-card title="规则覆盖详情" :bordered="false">
          <a-table
            :columns="detailColumns"
            :data-source="ruleDetails"
            :loading="detailLoading"
            :pagination="{ pageSize: 10 }"
            row-key="ruleId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'ruleName'">
                <a-tag color="blue">{{ record.ruleType }}</a-tag>
                {{ record.ruleName }}
              </template>
              <template v-else-if="column.key === 'coverageLevel'">
                <a-tag :color="getCoverageLevelColor(record.coverageLevel)">
                  {{ getCoverageLevelText(record.coverageLevel) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'successRate'">
                <a-progress
                  :percent="record.successRate.toFixed(1)"
                  :stroke-color="getSuccessRateColor(record.successRate)"
                  size="small"
                />
              </template>
              <template v-else-if="column.key === 'testCount'">
                <a-statistic
                  :value="record.testCount"
                  :value-style="{ fontSize: '14px' }"
                >
                  <template #suffix>
                    <span style="font-size: 12px; color: #999;">次</span>
                  </template>
                </a-statistic>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :span="8">
        <!-- 最近报告列表 -->
        <a-card title="最近报告" :bordered="false">
          <a-list
            :data-source="recentReports"
            :loading="reportLoading"
            style="max-height: 700px; overflow-y: auto;"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a-space>
                      <span>{{ item.reportDate }}</span>
                      <a-tag :color="getReportTypeColor(item.reportType)">
                        {{ getReportTypeText(item.reportType) }}
                      </a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    <a-space direction="vertical" style="width: 100%;">
                      <div>
                        <span style="color: #666;">覆盖率：</span>
                        <span :style="{ color: getCoverageRateColor(item.coverageRate), fontWeight: 'bold' }">
                          {{ item.coverageRate }}%
                        </span>
                      </div>
                      <div>
                        <span style="color: #666;">测试次数：</span>
                        <span>{{ item.totalTests }}次</span>
                        <span style="margin: 0 8px; color: #d9d9d9;">|</span>
                        <span style="color: #666;">成功率：</span>
                        <span :style="{ color: getSuccessRateColor(item.successRate) }">
                          {{ item.successRate }}%
                        </span>
                      </div>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a @click="viewReportDetail(item)">查看</a>
                  <a @click="deleteReport(item.reportId)" style="color: #ff4d4f;">删除</a>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 生成报告弹窗 -->
    <a-modal
      v-model:open="generateVisible"
      title="生成覆盖率报告"
      :width="600"
      @ok="handleGenerate"
      :confirm-loading="generateLoading"
    >
      <a-form
        :model="generateForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="报告类型" required>
          <a-select v-model:value="generateForm.reportType" placeholder="请选择报告类型">
            <a-select-option value="DAILY">日报</a-select-option>
            <a-select-option value="WEEKLY">周报</a-select-option>
            <a-select-option value="MONTHLY">月报</a-select-option>
            <a-select-option value="CUSTOM">自定义</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item v-if="generateForm.reportType === 'DAILY'" label="报告日期">
          <a-date-picker
            v-model:value="generateForm.reportDate"
            style="width: 100%;"
          />
        </a-form-item>

        <a-form-item v-if="generateForm.reportType === 'WEEKLY' || generateForm.reportType === 'MONTHLY' || generateForm.reportType === 'CUSTOM'" label="开始日期">
          <a-date-picker
            v-model:value="generateForm.startDate"
            style="width: 100%;"
          />
        </a-form-item>

        <a-form-item v-if="generateForm.reportType === 'WEEKLY' || generateForm.reportType === 'MONTHLY' || generateForm.reportType === 'CUSTOM'" label="结束日期">
          <a-date-picker
            v-model:value="generateForm.endDate"
            style="width: 100%;"
          />
        </a-form-item>

        <a-form-item label="低覆盖率阈值">
          <a-input-number
            v-model:value="generateForm.lowCoverageThreshold"
            :min="1"
            :max="100"
            style="width: 100%;"
            placeholder="测试次数低于此值视为低覆盖率"
          />
          <template #extra>默认为5次</template>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import * as echarts from 'echarts';
import {
  UnorderedListOutlined,
  PlusOutlined,
  ReloadOutlined,
  AppstoreOutlined,
  CheckCircleOutlined,
  DashboardOutlined,
  TrophyOutlined
} from '@ant-design/icons-vue';
import ruleCoverageReportApi from '/@/api/business/attendance/rule-coverage-report-api';

// 图表引用
const trendChartRef = ref(null);
let trendChart = null;

// 数据状态
const latestReport = ref(null);
const recentReports = ref([]);
const ruleDetails = ref([]);
const reportLoading = ref(false);
const detailLoading = ref(false);
const generateLoading = ref(false);

// 生成报告弹窗
const generateVisible = ref(false);
const generateForm = reactive({
  reportType: 'DAILY',
  reportDate: dayjs(),
  startDate: dayjs().subtract(7, 'day'),
  endDate: dayjs(),
  lowCoverageThreshold: 5
});

// 表格列定义
const detailColumns = [
  {
    title: '规则名称',
    dataIndex: 'ruleName',
    key: 'ruleName',
    width: '30%'
  },
  {
    title: '覆盖等级',
    dataIndex: 'coverageLevel',
    key: 'coverageLevel',
    width: '15%',
    align: 'center'
  },
  {
    title: '测试次数',
    dataIndex: 'testCount',
    key: 'testCount',
    width: '15%',
    align: 'center'
  },
  {
    title: '成功率',
    dataIndex: 'successRate',
    key: 'successRate',
    width: '25%'
  },
  {
    title: '最后测试时间',
    dataIndex: 'lastTestTime',
    key: 'lastTestTime',
    width: '15%'
  }
];

// 计算属性
const currentReportId = computed(() => {
  return latestReport.value?.reportId || null;
});

// 加载最近报告
const loadRecentReports = async () => {
  reportLoading.value = true;
  try {
    const res = await ruleCoverageReportApi.getRecentReports(10);
    if (res.code === 200) {
      recentReports.value = res.data || [];
      if (recentReports.value.length > 0) {
        latestReport.value = recentReports.value[0];
        await loadTrendData();
        await loadRuleDetails(latestReport.value.reportId);
      }
    } else {
      message.error(res.message || '加载报告列表失败');
    }
  } catch (error) {
    console.error('加载报告列表失败:', error);
    message.error('加载报告列表失败');
  } finally {
    reportLoading.value = false;
  }
};

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const endDate = dayjs();
    const startDate = dayjs().subtract(30, 'day');

    const res = await ruleCoverageReportApi.getCoverageTrend(
      startDate.format('YYYY-MM-DD'),
      endDate.format('YYYY-MM-DD')
    );

    if (res.code === 200) {
      renderTrendChart(res.data || []);
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error);
  }
};

// 渲染趋势图表
const renderTrendChart = (data) => {
  if (!trendChartRef.value) return;

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value);
  }

  const dates = data.map(item => dayjs(item.date).format('MM-DD'));
  const coverageRates = data.map(item => item.coverageRate);
  const successRates = data.map(item => item.successRate);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['覆盖率', '成功率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '覆盖率',
        type: 'line',
        smooth: true,
        data: coverageRates,
        itemStyle: {
          color: '#1890ff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
          ])
        }
      },
      {
        name: '成功率',
        type: 'line',
        smooth: true,
        data: successRates,
        itemStyle: {
          color: '#52c41a'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ])
        }
      }
    ]
  };

  trendChart.setOption(option);
};

// 加载规则详情
const loadRuleDetails = async (reportId) => {
  if (!reportId) return;

  detailLoading.value = true;
  try {
    const res = await ruleCoverageReportApi.getRuleCoverageDetails(reportId);
    if (res.code === 200) {
      ruleDetails.value = res.data || [];
    } else {
      message.error(res.message || '加载规则详情失败');
    }
  } catch (error) {
    console.error('加载规则详情失败:', error);
    message.error('加载规则详情失败');
  } finally {
    detailLoading.value = false;
  }
};

// 显示生成报告弹窗
const showGenerateModal = () => {
  generateVisible.value = true;
};

// 生成报告
const handleGenerate = async () => {
  generateLoading.value = true;
  try {
    const params = {
      reportType: generateForm.reportType,
      reportDate: generateForm.reportDate ? generateForm.reportDate.format('YYYY-MM-DD') : null,
      startDate: generateForm.startDate ? generateForm.startDate.format('YYYY-MM-DD') : null,
      endDate: generateForm.endDate ? generateForm.endDate.format('YYYY-MM-DD') : null,
      lowCoverageThreshold: generateForm.lowCoverageThreshold
    };

    const res = await ruleCoverageReportApi.generateCoverageReport(params);
    if (res.code === 200) {
      message.success('报告生成成功');
      generateVisible.value = false;
      await loadRecentReports();
    } else {
      message.error(res.message || '报告生成失败');
    }
  } catch (error) {
    console.error('生成报告失败:', error);
    message.error('报告生成失败');
  } finally {
    generateLoading.value = false;
  }
};

// 查看报告详情
const viewReportDetail = async (report) => {
  latestReport.value = report;
  await loadRuleDetails(report.reportId);
};

// 删除报告
const deleteReport = (reportId) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除此报告吗？',
    onOk: async () => {
      try {
        const res = await ruleCoverageReportApi.deleteReport(reportId);
        if (res.code === 200) {
          message.success('删除成功');
          await loadRecentReports();
        } else {
          message.error(res.message || '删除失败');
        }
      } catch (error) {
        console.error('删除失败:', error);
        message.error('删除失败');
      }
    }
  });
};

// 辅助方法：获取覆盖率等级颜色
const getCoverageLevelColor = (level) => {
  const colors = {
    'HIGH': 'green',
    'MEDIUM': 'blue',
    'LOW': 'orange',
    'NONE': 'red'
  };
  return colors[level] || 'default';
};

// 辅助方法：获取覆盖率等级文本
const getCoverageLevelText = (level) => {
  const texts = {
    'HIGH': '高覆盖',
    'MEDIUM': '中覆盖',
    'LOW': '低覆盖',
    'NONE': '未覆盖'
  };
  return texts[level] || level;
};

// 辅助方法：获取报告类型颜色
const getReportTypeColor = (type) => {
  const colors = {
    'DAILY': 'blue',
    'WEEKLY': 'green',
    'MONTHLY': 'orange',
    'CUSTOM': 'purple'
  };
  return colors[type] || 'default';
};

// 辅助方法：获取报告类型文本
const getReportTypeText = (type) => {
  const texts = {
    'DAILY': '日报',
    'WEEKLY': '周报',
    'MONTHLY': '月报',
    'CUSTOM': '自定义'
  };
  return texts[type] || type;
};

// 辅助方法：获取覆盖率颜色
const getCoverageRateColor = (rate) => {
  if (rate >= 90) return '#52c41a';
  if (rate >= 70) return '#1890ff';
  if (rate >= 50) return '#faad14';
  return '#ff4d4f';
};

// 辅助方法：获取成功率颜色
const getSuccessRateColor = (rate) => {
  if (rate >= 95) return '#52c41a';
  if (rate >= 85) return '#1890ff';
  if (rate >= 70) return '#faad14';
  return '#ff4d4f';
};

// 生命周期
onMounted(() => {
  loadRecentReports();

  nextTick(() => {
    window.addEventListener('resize', () => {
      if (trendChart) {
        trendChart.resize();
      }
    });
  });
});
</script>

<style scoped lang="less">
.rule-coverage-report-container {
  padding: 16px;

  .header-card {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 20px;
    font-weight: 500;
    color: #333;
    display: flex;
    align-items: center;
  }

  .stat-card {
    :deep(.ant-statistic-title) {
      font-size: 14px;
      color: #666;
    }

    :deep(.ant-statistic-content) {
      font-size: 28px;
      font-weight: 500;
    }
  }
}
</style>
