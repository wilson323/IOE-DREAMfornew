<!--
  @fileoverview 考勤仪表中心主页面（增强版）
  @author IOE-DREAM Team
  @description 展示考勤实时统计数据、打卡趋势图表和异常告警
  @version 2.0.0 - 企业级标准实现
-->
<template>
  <div class="attendance-dashboard">
    <!-- 统计卡片区域 -->
    <a-row :gutter="[16, 16]" class="attendance-dashboard__stats">
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard
          title="应出勤"
          :value="stats.totalAttendance"
          suffix="人"
          color="#1890ff"
          icon="UserOutlined"
          :change="statsChanges.totalAttendance"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard
          title="实出勤"
          :value="stats.actualAttendance"
          suffix="人"
          color="#52c41a"
          icon="CheckCircleOutlined"
          :change="statsChanges.actualAttendance"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard
          title="迟到"
          :value="stats.lateCount"
          suffix="人"
          color="#faad14"
          icon="ClockCircleOutlined"
          :change="statsChanges.lateCount"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard
          title="早退"
          :value="stats.earlyLeaveCount"
          suffix="人"
          color="#ff4d4f"
          icon="CloseCircleOutlined"
          :change="statsChanges.earlyLeaveCount"
        />
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="[16, 16]" class="attendance-dashboard__charts">
      <a-col :xs="24" :lg="16">
        <a-card title="今日打卡趋势" :bordered="false" class="chart-card">
          <template #extra>
            <a-button @click="fetchTrend" :loading="trendLoading">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </template>
          <PunchChart :data="trendData" :loading="trendLoading" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card title="部门出勤率" :bordered="false" class="chart-card">
          <template #extra>
            <a-button @click="fetchDepartmentRate" :loading="rateLoading">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </template>
          <DepartmentRateChart :data="departmentRate" :loading="rateLoading" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 异常告警区域 -->
    <a-row class="attendance-dashboard__alerts">
      <a-col :span="24">
        <a-card title="异常告警" :bordered="false">
          <template #extra>
            <a-space>
              <a-tag v-if="alertList.length > 0" :color="getHighestAlertColor()">
                {{ alertList.length }} 条告警
              </a-tag>
              <a-button size="small" @click="handleViewAllAlerts">
                查看全部
              </a-button>
              <a-button size="small" @click="fetchAlerts" :loading="alertLoading">
                <template #icon><ReloadOutlined /></template>
              </a-button>
            </a-space>
          </template>
          <AlertList
            :data="alertList"
            :loading="alertLoading"
            @view-detail="handleAlertDetail"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 快捷操作区域 -->
    <a-row class="attendance-dashboard__actions">
      <a-col :span="24">
        <a-card title="快捷操作" :bordered="false">
          <a-row :gutter="16">
            <a-col :xs="12" :sm="6">
              <a-button type="primary" block @click="goToSchedule">
                <template #icon><CalendarOutlined /></template>
                排班管理
              </a-button>
            </a-col>
            <a-col :xs="12" :sm="6">
              <a-button block @click="goToApproval">
                <template #icon><AuditOutlined /></template>
                审批中心
              </a-button>
            </a-col>
            <a-col :xs="12" :sm="6">
              <a-button block @click="goToReport">
                <template #icon><FileTextOutlined /></template>
                考勤报表
              </a-button>
            </a-col>
            <a-col :xs="12" :sm="6">
              <a-button block @click="refreshData" :loading="refreshing">
                <template #icon><ReloadOutlined /></template>
                刷新数据
              </a-button>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  CalendarOutlined,
  AuditOutlined,
  FileTextOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';
import StatCard from './components/StatCard.vue';
import PunchChart from './components/PunchChart.vue';
import DepartmentRateChart from './components/DepartmentRateChart.vue';
import AlertList from './components/AlertList.vue';
import {
  dashboardApi,
  type RealtimeStatsVO,
  type TrendDataPoint,
  type DepartmentRateVO,
  type AlertVO
} from '@/api/business/attendance/dashboard';

// 路由
const router = useRouter();

// 响应式数据
const stats = ref<RealtimeStatsVO>({
  totalAttendance: 0,
  actualAttendance: 0,
  lateCount: 0,
  earlyLeaveCount: 0
});

const statsChanges = ref<Record<string, number>>({});

const trendData = ref<TrendDataPoint[]>([]);
const trendLoading = ref(false);

const departmentRate = ref<DepartmentRateVO[]>([]);
const rateLoading = ref(false);

const alertList = ref<AlertVO[]>([]);
const alertLoading = ref(false);

const refreshing = ref(false);

// 定时刷新
let refreshInterval: NodeJS.Timeout | null = null;

// 计算属性
const totalAlerts = computed(() => alertList.value.length);

// 获取最高告警级别颜色
const getHighestAlertColor = () => {
  if (alertList.value.length === 0) return 'default';

  const highestLevel = Math.max(...alertList.value.map(a => a.level));
  if (highestLevel === 3) return 'error';
  if (highestLevel === 2) return 'warning';
  return 'processing';
};

// 方法
const fetchStats = async () => {
  try {
    const res = await dashboardApi.getRealtimeStats({});
    if (res.data) {
      // 保存旧数据用于计算变化
      const oldStats = { ...stats.value };
      stats.value = res.data;

      // 计算变化百分比
      statsChanges.value = {
        totalAttendance: calculateChange(oldStats.totalAttendance, res.data.totalAttendance),
        actualAttendance: calculateChange(oldStats.actualAttendance, res.data.actualAttendance),
        lateCount: calculateChange(oldStats.lateCount, res.data.lateCount),
        earlyLeaveCount: calculateChange(oldStats.earlyLeaveCount, res.data.earlyLeaveCount)
      };
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
  }
};

const calculateChange = (oldValue: number, newValue: number): number => {
  if (oldValue === 0) return 0;
  return Number(((newValue - oldValue) / oldValue * 100).toFixed(1));
};

const fetchTrend = async () => {
  trendLoading.value = true;
  try {
    const res = await dashboardApi.getTodayTrend();
    trendData.value = res.data || [];
  } catch (error) {
    console.error('获取趋势数据失败:', error);
    message.error('获取趋势数据失败');
  } finally {
    trendLoading.value = false;
  }
};

const fetchDepartmentRate = async () => {
  rateLoading.value = true;
  try {
    const res = await dashboardApi.getDepartmentRate({});
    departmentRate.value = res.data || [];
  } catch (error) {
    console.error('获取部门出勤率失败:', error);
    message.error('获取部门出勤率失败');
  } finally {
    rateLoading.value = false;
  }
};

const fetchAlerts = async () => {
  alertLoading.value = true;
  try {
    const res = await dashboardApi.getAlertList({ pageSize: 10 });
    alertList.value = res.data?.list || [];
  } catch (error) {
    console.error('获取异常告警失败:', error);
    message.error('获取异常告警失败');
  } finally {
    alertLoading.value = false;
  }
};

const refreshData = async () => {
  refreshing.value = true;
  try {
    await Promise.all([
      fetchStats(),
      fetchTrend(),
      fetchDepartmentRate(),
      fetchAlerts()
    ]);
    message.success('数据已刷新');
  } catch (error) {
    message.error('刷新数据失败');
  } finally {
    refreshing.value = false;
  }
};

const handleAlertDetail = (alert: AlertVO) => {
  // 跳转到详情页
  router.push({
    path: '/business/attendance/record',
    query: {
      employeeId: alert.employeeId.toString(),
      alertId: alert.alertId.toString()
    }
  });
};

const handleViewAllAlerts = () => {
  router.push('/business/attendance/exception');
};

const goToSchedule = () => {
  router.push('/business/attendance/schedule');
};

const goToApproval = () => {
  router.push('/business/attendance/approval');
};

const goToReport = () => {
  router.push('/business/attendance/report');
};

// 生命周期
onMounted(() => {
  refreshData();

  // 定时刷新（每5分钟）
  refreshInterval = setInterval(() => {
    fetchStats();
    fetchTrend();
    fetchAlerts();
  }, 5 * 60 * 1000);
});

// 清理定时器
onBeforeUnmount(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }
});
</script>

<style scoped lang="less">
.attendance-dashboard {
  padding: 24px;

  &__stats {
    margin-bottom: 16px;
  }

  &__charts {
    margin-bottom: 16px;
  }

  &__alerts {
    margin-bottom: 16px;
  }

  &__actions {
    // 快捷操作
  }
}

.chart-card {
  :deep(.ant-card-body) {
    min-height: 400px;
  }
}
</style>
