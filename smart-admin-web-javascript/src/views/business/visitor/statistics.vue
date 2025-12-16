<!--
 * 访客统计页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-statistics-page">
    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="今日预约"
            :value="statistics.todayAppointments || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <CalendarOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="在访访客"
            :value="statistics.activeVisitors || 0"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="本月访问"
            :value="statistics.monthlyVisitors || 0"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <TeamOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="平均停留时长"
            :value="statistics.averageDuration || 0"
            suffix="分钟"
            :value-style="{ color: '#13c2c2' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card :bordered="false" title="访客趋势" class="chart-card">
          <div ref="trendChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card :bordered="false" title="预约类型分布" class="chart-card">
          <div ref="typeChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细统计表 -->
    <a-card :bordered="false" title="详细统计" class="detail-card">
      <a-table
        :columns="detailColumns"
        :data-source="detailData"
        :loading="loadingDetail"
        :pagination="false"
        size="small"
      />
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  CalendarOutlined,
  UserOutlined,
  TeamOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue';
import { visitorApi } from '/@/api/business/visitor/visitor-api';

// 统计数据
const statistics = reactive({
  todayAppointments: 0,
  activeVisitors: 0,
  monthlyVisitors: 0,
  averageDuration: 0
});

// 详细数据
const detailData = ref([]);
const loadingDetail = ref(false);

// 详细列配置
const detailColumns = [
  { title: '日期', dataIndex: 'date', key: 'date' },
  { title: '预约数', dataIndex: 'appointments', key: 'appointments' },
  { title: '签到数', dataIndex: 'checkIns', key: 'checkIns' },
  { title: '签退数', dataIndex: 'checkOuts', key: 'checkOuts' },
  { title: '平均停留', dataIndex: 'avgDuration', key: 'avgDuration' }
];

// 图表引用
const trendChartRef = ref(null);
const typeChartRef = ref(null);

// 加载统计数据
const loadStatistics = async () => {
  try {
    const userId = 1; // 从用户状态中获取
    const result = await visitorApi.getPersonalStatistics(userId);
    if (result.code === 200 && result.data) {
      Object.assign(statistics, result.data);
    }
  } catch (error) {
    message.error('加载统计数据失败');
    console.error('加载统计数据失败:', error);
  }
};

// 初始化
onMounted(() => {
  loadStatistics();
  // 图表初始化由ECharts组件自动完成
});
</script>

<style lang="scss" scoped>
.visitor-statistics-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

.chart-card {
  margin-bottom: 16px;
}

.detail-card {
  :deep(.ant-table) {
    font-size: 14px;
  }
}
</style>

