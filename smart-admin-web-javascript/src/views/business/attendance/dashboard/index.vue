<!--
  * Attendance Dashboard
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
-->
<template>
  <div class="attendance-dashboard-page">
    <!-- Today overview -->
    <div class="today-overview" style="margin-bottom: 24px;">
      <a-card :bordered="false" title="Today Overview">
        <template #extra>
          <a-space>
            <a-date-picker
              v-model:value="selectedDate"
              :disabled-date="disabledDate"
              @change="handleDateChange"
              placeholder="Select date"
            />
            <a-button @click="handleRefresh" :loading="refreshLoading">
              <template #icon><ReloadOutlined /></template>
              Refresh
            </a-button>
          </a-space>
        </template>

        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="6">
            <div class="stat-item normal">
              <div class="stat-icon">
                <UserOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ todayStats.normalCount }}</div>
                <div class="stat-label">Normal</div>
                <div class="stat-change" :class="{ positive: todayStats.normalChange >= 0 }">
                  {{ todayStats.normalChange >= 0 ? '+' : '' }}{{ todayStats.normalChange }}%
                </div>
              </div>
            </div>
          </a-col>

          <a-col :xs="24" :sm="12" :md="6">
            <div class="stat-item late">
              <div class="stat-icon">
                <ClockCircleOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ todayStats.lateCount }}</div>
                <div class="stat-label">Late</div>
                <div class="stat-change" :class="{ negative: todayStats.lateChange > 0 }">
                  {{ todayStats.lateChange > 0 ? '+' : '' }}{{ todayStats.lateChange }}%
                </div>
              </div>
            </div>
          </a-col>

          <a-col :xs="24" :sm="12" :md="6">
            <div class="stat-item early-leave">
              <div class="stat-icon">
                <MinusCircleOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ todayStats.earlyLeaveCount }}</div>
                <div class="stat-label">Early Leave</div>
                <div class="stat-change" :class="{ negative: todayStats.earlyLeaveChange > 0 }">
                  {{ todayStats.earlyLeaveChange > 0 ? '+' : '' }}{{ todayStats.earlyLeaveChange }}%
                </div>
              </div>
            </div>
          </a-col>

          <a-col :xs="24" :sm="12" :md="6">
            <div class="stat-item absent">
              <div class="stat-icon">
                <StopOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ todayStats.absentCount }}</div>
                <div class="stat-label">Absent</div>
                <div class="stat-change" :class="{ negative: todayStats.absentChange > 0 }">
                  {{ todayStats.absentChange > 0 ? '+' : '' }}{{ todayStats.absentChange }}%
                </div>
              </div>
            </div>
          </a-col>
        </a-row>

        <!-- Attendance rate -->
        <div class="attendance-rate" style="margin-top: 24px;">
          <div class="rate-label">
            <span>Attendance Rate</span>
            <a-statistic
              :value="todayStats.attendanceRate"
              suffix="%"
              :value-style="{ fontSize: '20px', fontWeight: '600', color: '#1890ff' }"
            />
          </div>
          <a-progress
            :percent="todayStats.attendanceRate"
            :stroke-color="getAttendanceRateColor(todayStats.attendanceRate)"
            :show-info="false"
            style="margin-top: 8px;"
          />
        </div>
      </a-card>
    </div>

    <!-- Realtime check-in and weekly trend -->
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <a-card :bordered="false" title="Realtime Check-ins" class="realtime-checkins">
          <template #extra>
            <a-badge :count="newCheckinsCount" :number-style="{ backgroundColor: '#52c41a' }" />
          </template>

          <div class="checkin-list" style="height: 400px; overflow-y: auto;">
            <div
              v-for="checkin in realtimeCheckins"
              :key="checkin.id"
              class="checkin-item"
              :class="{ 'checkin-success': checkin.type === 'SUCCESS', 'checkin-failed': checkin.type === 'FAILED' }"
            >
              <div class="checkin-avatar">
                <a-avatar :src="checkin.avatar" :size="40">
                  {{ checkin.employeeName?.charAt(0) }}
                </a-avatar>
              </div>
              <div class="checkin-content">
                <div class="checkin-header">
                  <span class="employee-name">{{ checkin.employeeName }}</span>
                  <span class="checkin-time">{{ checkin.time }}</span>
                </div>
                <div class="checkin-detail">
                  <span class="checkin-type">{{ checkin.checkType === 'IN' ? 'Check-in' : 'Check-out' }}</span>
                  <span class="checkin-location">{{ checkin.location }}</span>
                </div>
              </div>
              <div class="checkin-status">
                <a-badge
                  :status="checkin.type === 'SUCCESS' ? 'success' : 'error'"
                  :text="checkin.type === 'SUCCESS' ? 'Success' : 'Failed'"
                />
              </div>
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card :bordered="false" title="Weekly Trend" class="weekly-trend">
          <div style="height: 400px;">
            <v-chart class="chart" :option="weeklyTrendOption" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Alerts and department ranking -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" title="Alerts" class="abnormal-alerts">
          <template #extra>
            <a-space>
              <a-tag v-if="abnormalAlerts.length > 0" color="red">
                {{ abnormalAlerts.length }} items
              </a-tag>
              <a-button size="small" @click="handleViewAllAlerts">
                View all
              </a-button>
            </a-space>
          </template>

          <div class="alert-list" style="height: 300px; overflow-y: auto;">
            <div v-if="abnormalAlerts.length > 0">
              <div
                v-for="alert in abnormalAlerts"
                :key="alert.id"
                class="alert-item"
                :class="getAlertClass(alert.level)"
              >
                <div class="alert-icon">
                  <component :is="getAlertIcon(alert.level)" />
                </div>
                <div class="alert-content">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-desc">{{ alert.description }}</div>
                  <div class="alert-time">{{ alert.time }}</div>
                </div>
              </div>
            </div>
            <a-empty v-else description="No alerts" />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" title="Department Ranking" class="department-ranking">
          <a-list
            :data-source="departmentRanking"
            bordered
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.department"
                  :description="'Attendance Rate: ' + item.rate + '%'" />
                <a-badge :count="item.rate" :number-style="{ backgroundColor: getRateColor(item.rate) }" />
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" title="Leave Summary" class="leave-summary">
          <a-table
            :columns="leaveColumns"
            :data-source="leaveSummary"
            size="small"
            :pagination="false"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
  import { reactive, ref, computed, onMounted } from 'vue';
  import dayjs from 'dayjs';
  import {
    ReloadOutlined,
    UserOutlined,
    ClockCircleOutlined,
    MinusCircleOutlined,
    StopOutlined,
    WarningOutlined,
    ExclamationCircleOutlined,
  } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import VChart from 'vue-echarts';

  const selectedDate = ref(dayjs());
  const refreshLoading = ref(false);
  const newCheckinsCount = ref(0);

  const todayStats = reactive({
    normalCount: 128,
    normalChange: 5.3,
    lateCount: 12,
    lateChange: 1.2,
    earlyLeaveCount: 6,
    earlyLeaveChange: 0.5,
    absentCount: 3,
    absentChange: 0,
    attendanceRate: 96.5,
  });

  const realtimeCheckins = ref([]);

  const weeklyTrend = ref([
    { day: 'Mon', value: 320 },
    { day: 'Tue', value: 340 },
    { day: 'Wed', value: 360 },
    { day: 'Thu', value: 380 },
    { day: 'Fri', value: 420 },
    { day: 'Sat', value: 310 },
    { day: 'Sun', value: 290 },
  ]);

  const abnormalAlerts = ref([
    { id: 1, title: 'Late arrivals spike', description: 'Late count increased by 20%', level: 'HIGH', time: '09:10' },
    { id: 2, title: 'Missing check-ins', description: '3 employees missing morning punch', level: 'MEDIUM', time: '10:00' },
  ]);

  const departmentRanking = ref([
    { department: 'R&D', rate: 98 },
    { department: 'Operations', rate: 96 },
    { department: 'Sales', rate: 94 },
    { department: 'Support', rate: 92 },
  ]);

  const leaveColumns = [
    { title: 'Employee', dataIndex: 'name', key: 'name' },
    { title: 'Type', dataIndex: 'type', key: 'type' },
    { title: 'Duration', dataIndex: 'duration', key: 'duration' },
  ];

  const leaveSummary = ref([
    { key: 1, name: 'Alice', type: 'Annual', duration: '1 day' },
    { key: 2, name: 'Bob', type: 'Sick', duration: '0.5 day' },
  ]);

  const weeklyTrendOption = computed(() => ({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: weeklyTrend.value.map((item) => item.day) },
    yAxis: { type: 'value' },
    series: [
      {
        data: weeklyTrend.value.map((item) => item.value),
        type: 'bar',
        smooth: true,
        itemStyle: { color: '#1890ff' },
      },
    ],
  }));

  const disabledDate = (current) => {
    return current && current > dayjs().endOf('day');
  };

  const handleDateChange = () => {
    refreshData();
  };

  const handleRefresh = async () => {
    refreshLoading.value = true;
    await refreshData();
    refreshLoading.value = false;
  };

  const refreshData = () => {
    // Mock data update
    loadRealtimeCheckins();
    message.success('Data refreshed');
  };

  const loadRealtimeCheckins = () => {
    const now = dayjs();
    realtimeCheckins.value = Array.from({ length: 15 }).map((_, idx) => ({
      id: idx + 1,
      employeeName: `Employee ${idx + 1}`,
      time: now.subtract(idx * 5, 'minute').format('HH:mm'),
      location: `Building ${idx % 3 + 1} - Gate ${idx % 4 + 1}`,
      checkType: idx % 2 === 0 ? 'IN' : 'OUT',
      type: idx % 7 === 0 ? 'FAILED' : 'SUCCESS',
      avatar: '',
    }));
    newCheckinsCount.value = realtimeCheckins.value.filter((item) => item.type === 'FAILED').length;
  };

  const getAttendanceRateColor = (rate) => {
    if (rate >= 98) return '#52c41a';
    if (rate >= 95) return '#1890ff';
    return '#faad14';
  };

  const getAlertClass = (level) => {
    return {
      HIGH: 'alert-high',
      MEDIUM: 'alert-medium',
      LOW: 'alert-low',
    }[level] || '';
  };

  const getAlertIcon = (level) => {
    if (level === 'HIGH') return 'WarningOutlined';
    if (level === 'MEDIUM') return 'ExclamationCircleOutlined';
    return 'ClockCircleOutlined';
  };

  const handleViewAllAlerts = () => {
    message.info('Viewing all alerts');
  };

  const getRateColor = (rate) => {
    if (rate >= 97) return '#52c41a';
    if (rate >= 94) return '#1890ff';
    return '#faad14';
  };

  onMounted(() => {
    loadRealtimeCheckins();
  });
</script>

<style lang="less" scoped>
  .attendance-dashboard-page {
    .stat-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: 8px;
      background: #fafafa;

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        font-size: 20px;
        background: #e6f7ff;
      }

      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: 700;
        }

        .stat-label {
          color: rgba(0, 0, 0, 0.45);
        }

        .stat-change {
          font-size: 12px;
          margin-top: 4px;

          &.positive {
            color: #52c41a;
          }

          &.negative {
            color: #f5222d;
          }
        }
      }
    }

    .checkin-list {
      .checkin-item {
        display: flex;
        align-items: center;
        padding: 12px;
        border: 1px solid #f0f0f0;
        border-radius: 8px;
        margin-bottom: 10px;

        &.checkin-success {
          background: #f6ffed;
        }

        &.checkin-failed {
          background: #fff1f0;
        }

        .checkin-avatar {
          margin-right: 12px;
        }

        .checkin-content {
          flex: 1;

          .checkin-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 4px;
            font-weight: 600;
          }

          .checkin-detail {
            color: rgba(0, 0, 0, 0.45);
          }
        }
      }
    }

    .alert-item {
      display: flex;
      align-items: center;
      padding: 10px;
      border-radius: 8px;
      margin-bottom: 10px;
      background: #fafafa;

      &.alert-high {
        border-left: 4px solid #f5222d;
      }

      &.alert-medium {
        border-left: 4px solid #faad14;
      }

      &.alert-low {
        border-left: 4px solid #1890ff;
      }

      .alert-icon {
        margin-right: 10px;
        font-size: 18px;
      }

      .alert-content {
        .alert-title {
          font-weight: 600;
        }

        .alert-desc {
          color: rgba(0, 0, 0, 0.45);
        }

        .alert-time {
          font-size: 12px;
          color: rgba(0, 0, 0, 0.45);
        }
      }
    }

    .chart {
      height: 100%;
    }
  }
</style>
