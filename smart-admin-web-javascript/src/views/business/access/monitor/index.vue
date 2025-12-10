<!--
  * Access Monitoring
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM
-->
<template>
  <div class="access-monitor-page">
    <!-- Status overview -->
    <div class="status-overview" style="margin-bottom: 24px;">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="status-card online" :bordered="false">
            <a-statistic
              title="Online Devices"
              :value="monitorStatus.onlineDevices"
              :prefix="h(WifiOutlined)"
              :value-style="{ color: '#52c41a' }"
            />
            <div class="status-change" :class="{ positive: deviceChange >= 0, negative: deviceChange < 0 }">
              <template v-if="deviceChange !== 0">
                <component :is="deviceChange >= 0 ? 'ArrowUpOutlined' : 'ArrowDownOutlined'" />
                {{ Math.abs(deviceChange) }}
              </template>
              <span v-else>Stable</span>
            </div>
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="status-card today-access" :bordered="false">
            <a-statistic
              title="Today Access"
              :value="monitorStatus.todayAccess"
              :prefix="h(UserSwitchOutlined)"
              :value-style="{ color: '#1890ff' }"
            />
            <div class="status-change positive">
              Compared to yesterday +{{ monitorStatus.accessGrowth }}%
            </div>
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="status-card success-rate" :bordered="false">
            <a-statistic
              title="Success Rate"
              :value="monitorStatus.successRate"
              suffix="%"
              :prefix="h(SafetyOutlined)"
              :value-style="{ color: '#722ed1' }"
            />
            <div class="status-indicator" :class="getSuccessRateClass(monitorStatus.successRate)">
              {{ getSuccessRateText(monitorStatus.successRate) }}
            </div>
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="status-card alerts" :bordered="false">
            <a-statistic
              title="Alerts"
              :value="monitorStatus.alerts"
              :prefix="h(ExclamationCircleOutlined)"
              :value-style="{ color: monitorStatus.alerts > 0 ? '#f5222d' : '#52c41a' }"
            />
            <div class="status-change" :class="{ negative: monitorStatus.alerts > 0 }">
              {{ monitorStatus.alerts > 0 ? 'Need attention' : 'All clear' }}
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- Realtime records -->
    <a-card :bordered="false" style="margin-bottom: 16px;" class="realtime-records">
      <template #title>
        <a-space>
          <span>Realtime Access</span>
          <a-badge :count="newRecordsCount" :number-style="{ backgroundColor: '#52c41a' }" />
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-switch
            v-model:checked="autoRefresh"
            checked-children="Auto"
            un-checked-children="Manual"
            @change="handleAutoRefreshChange"
          />
          <a-button @click="handleRefreshRecords" :loading="recordsLoading">
            <template #icon><ReloadOutlined /></template>
            Refresh
          </a-button>
          <a-button @click="handleClearRecords">
            <template #icon><ClearOutlined /></template>
            Clear
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="recordColumns"
        :data-source="realtimeRecords"
        :pagination="{ pageSize: 10, showSizeChanger: false }"
        size="small"
        :loading="recordsLoading"
        :scroll="{ y: 400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userInfo'">
            <div class="user-info">
              <a-avatar
                :src="record.userAvatar"
                :size="24"
                style="margin-right: 8px;"
              >
                {{ record.userName?.charAt(0) }}
              </a-avatar>
              <div class="user-details">
                <div class="user-name">{{ record.userName }}</div>
                <div class="user-code">{{ record.userCode }}</div>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'deviceInfo'">
            <div class="device-info">
              <div class="device-name">{{ record.deviceName }}</div>
              <div class="area-name">{{ record.areaName }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'result'">
            <a-tag :color="record.result === 'SUCCESS' ? 'green' : 'red'">
              <template #icon>
                <component :is="record.result === 'SUCCESS' ? 'CheckCircleOutlined' : 'CloseCircleOutlined'" />
              </template>
              {{ record.result === 'SUCCESS' ? 'Success' : 'Failed' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'verifyMethod'">
            <a-tag :color="getVerifyMethodColor(record.verifyMethod)">
              {{ getVerifyMethodText(record.verifyMethod) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleViewRecord(record)">
              Details
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Charts -->
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <a-card :bordered="false" title="Device Status" class="device-status">
          <div style="height: 300px;">
            <v-chart class="chart" :option="deviceStatusOption" />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card :bordered="false" title="Access Trend" class="access-trend">
          <div style="height: 300px;">
            <v-chart class="chart" :option="accessTrendOption" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Alerts -->
    <a-card :bordered="false" style="margin-top: 16px;" title="Alerts" class="alerts-section">
      <template #extra>
        <a-space>
          <a-tag v-if="monitorStatus.alerts > 0" color="red">
            {{ monitorStatus.alerts }} pending
          </a-tag>
          <a-button size="small" @click="handleViewAllAlerts">
            View all
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="alertColumns"
        :data-source="alerts"
        :pagination="{ pageSize: 5, showSizeChanger: false }"
        size="small"
        v-if="alerts.length > 0"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'level'">
            <a-tag :color="getAlertColor(record.level)">
              {{ record.level }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 'OPEN' ? 'processing' : 'success'"
              :text="record.status === 'OPEN' ? 'Open' : 'Resolved'"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleAlertAction(record, 'view')">View</a-button>
              <a-button type="link" size="small" @click="handleAlertAction(record, 'resolve')">Resolve</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-empty v-else description="No alerts" />
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, computed, onMounted, onBeforeUnmount, h } from 'vue';
  import dayjs from 'dayjs';
  import {
    ReloadOutlined,
    ClearOutlined,
    WifiOutlined,
    UserSwitchOutlined,
    SafetyOutlined,
    ExclamationCircleOutlined,
    ArrowUpOutlined,
    ArrowDownOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
  } from '@ant-design/icons-vue';
  import { message, Modal } from 'ant-design-vue';
  import VChart from 'vue-echarts';
  import { smartSentry } from '/@/lib/smart-sentry';

  const monitorStatus = reactive({
    onlineDevices: 128,
    todayAccess: 560,
    accessGrowth: 8.6,
    successRate: 97.2,
    alerts: 2,
  });

  const deviceChange = ref(3);
  const autoRefresh = ref(true);
  const recordsLoading = ref(false);
  const newRecordsCount = ref(0);
  const realtimeRecords = ref([]);
  const alerts = ref([]);
  const refreshTimer = ref(null);

  const recordColumns = [
    { title: 'User', key: 'userInfo', width: 180 },
    { title: 'Device', key: 'deviceInfo', width: 180 },
    { title: 'Result', key: 'result', width: 100 },
    { title: 'Verify Method', key: 'verifyMethod', width: 120 },
    { title: 'Time', dataIndex: 'time', key: 'time', width: 180 },
    { title: 'Action', key: 'action', width: 100 },
  ];

  const alertColumns = [
    { title: 'Title', dataIndex: 'title', key: 'title', width: 200 },
    { title: 'Level', key: 'level', width: 100 },
    { title: 'Area', dataIndex: 'areaName', key: 'areaName', width: 120 },
    { title: 'Time', dataIndex: 'time', key: 'time', width: 160 },
    { title: 'Status', key: 'status', width: 120 },
    { title: 'Action', key: 'action', width: 140 },
  ];

  const deviceStatusOption = computed(() => ({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: 'Devices',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 120, name: 'Online' },
          { value: 15, name: 'Offline' },
          { value: 6, name: 'Fault' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }));

  const accessTrendOption = computed(() => ({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trendData.value.map((item) => item.date) },
    yAxis: { type: 'value' },
    series: [
      {
        data: trendData.value.map((item) => item.count),
        type: 'line',
        smooth: true,
        areaStyle: {},
      },
    ],
  }));

  const trendData = ref([
    { date: dayjs().subtract(6, 'day').format('MM-DD'), count: 320 },
    { date: dayjs().subtract(5, 'day').format('MM-DD'), count: 280 },
    { date: dayjs().subtract(4, 'day').format('MM-DD'), count: 300 },
    { date: dayjs().subtract(3, 'day').format('MM-DD'), count: 360 },
    { date: dayjs().subtract(2, 'day').format('MM-DD'), count: 410 },
    { date: dayjs().subtract(1, 'day').format('MM-DD'), count: 450 },
    { date: dayjs().format('MM-DD'), count: 560 },
  ]);

  const getVerifyMethodColor = (method) => {
    const map = { QR: 'blue', NFC: 'cyan', BIOMETRIC: 'purple', CARD: 'green', FACE: 'magenta' };
    return map[method] || 'default';
  };

  const getVerifyMethodText = (method) => {
    const map = { QR: 'QR Code', NFC: 'NFC', BIOMETRIC: 'Biometric', CARD: 'Card', FACE: 'Face' };
    return map[method] || method;
  };

  const getSuccessRateClass = (rate) => {
    if (rate >= 98) return 'good';
    if (rate >= 90) return 'medium';
    return 'bad';
  };

  const getSuccessRateText = (rate) => {
    if (rate >= 98) return 'Excellent';
    if (rate >= 90) return 'Good';
    return 'Low';
  };

  const getAlertColor = (level) => {
    const map = { High: 'red', Medium: 'orange', Low: 'blue' };
    return map[level] || 'default';
  };

  const loadMockRecords = () => {
    const now = dayjs();
    realtimeRecords.value = Array.from({ length: 12 }).map((_, idx) => ({
      id: idx + 1,
      userName: `User ${idx + 1}`,
      userCode: `EMP-${1200 + idx}`,
      userAvatar: '',
      deviceName: `Gate ${idx % 4 + 1}`,
      areaName: `Area ${idx % 3 + 1}`,
      result: idx % 7 === 0 ? 'FAILED' : 'SUCCESS',
      verifyMethod: ['QR', 'CARD', 'FACE', 'NFC'][idx % 4],
      time: now.subtract(idx * 3, 'minute').format('YYYY-MM-DD HH:mm:ss'),
    }));
    newRecordsCount.value = realtimeRecords.value.filter((item) => item.result === 'FAILED').length;
  };

  const loadMockAlerts = () => {
    const now = dayjs();
    alerts.value = [
      { id: 1, title: 'Door forced open', level: 'High', areaName: 'Building A', time: now.subtract(10, 'minute').format('YYYY-MM-DD HH:mm'), status: 'OPEN' },
      { id: 2, title: 'Multiple failures', level: 'Medium', areaName: 'Parking', time: now.subtract(1, 'hour').format('YYYY-MM-DD HH:mm'), status: 'OPEN' },
    ];
    monitorStatus.alerts = alerts.value.filter((i) => i.status === 'OPEN').length;
  };

  const handleRefreshRecords = async () => {
    recordsLoading.value = true;
    try {
      loadMockRecords();
    } catch (error) {
      smartSentry.captureError(error);
      message.error('Failed to refresh records');
    } finally {
      recordsLoading.value = false;
    }
  };

  const handleClearRecords = () => {
    realtimeRecords.value = [];
    newRecordsCount.value = 0;
  };

  const handleAutoRefreshChange = (checked) => {
    if (checked) {
      startAutoRefresh();
    } else {
      stopAutoRefresh();
    }
  };

  const handleViewRecord = (record) => {
    Modal.info({
      title: 'Record detail',
      content: `User: ${record.userName}\nDevice: ${record.deviceName}\nResult: ${record.result}\nTime: ${record.time}`,
    });
  };

  const handleViewAllAlerts = () => {
    message.info('Showing all alerts');
  };

  const handleAlertAction = (record, action) => {
    if (action === 'resolve') {
      record.status = 'RESOLVED';
      monitorStatus.alerts = alerts.value.filter((i) => i.status === 'OPEN').length;
      message.success('Alert resolved');
    } else {
      Modal.info({
        title: record.title,
        content: record.areaName,
      });
    }
  };

  const startAutoRefresh = () => {
    stopAutoRefresh();
    refreshTimer.value = setInterval(() => {
      handleRefreshRecords();
    }, 8000);
  };

  const stopAutoRefresh = () => {
    if (refreshTimer.value) {
      clearInterval(refreshTimer.value);
      refreshTimer.value = null;
    }
  };

  onMounted(() => {
    loadMockRecords();
    loadMockAlerts();
    if (autoRefresh.value) {
      startAutoRefresh();
    }
  });

  onBeforeUnmount(() => {
    stopAutoRefresh();
  });
</script>

<style lang="less" scoped>
  .access-monitor-page {
    .status-overview {
      .status-card {
        position: relative;
        overflow: hidden;

        .status-change {
          position: absolute;
          right: 16px;
          bottom: 12px;
          font-size: 12px;
          color: #8c8c8c;

          &.positive {
            color: #52c41a;
          }

          &.negative {
            color: #f5222d;
          }
        }

        .status-indicator {
          margin-top: 8px;
          font-size: 12px;

          &.good {
            color: #52c41a;
          }

          &.medium {
            color: #faad14;
          }

          &.bad {
            color: #f5222d;
          }
        }
      }
    }

    .realtime-records {
      .user-info {
        display: flex;
        align-items: center;

        .user-details {
          display: flex;
          flex-direction: column;
          line-height: 1.2;
        }
      }

      .device-info {
        display: flex;
        flex-direction: column;
        line-height: 1.2;
      }
    }

    .chart {
      height: 100%;
    }
  }
</style>
