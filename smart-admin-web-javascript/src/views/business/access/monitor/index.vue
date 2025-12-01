<!--
  * 门禁实时监控页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-monitor-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h2>实时监控</h2>
        <p>实时监控门禁设备状态、通行数据和异常告警</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button type="primary" @click="toggleConnection">
            <template #icon>
              <WifiOutlined v-if="wsConnected" />
              <DisconnectOutlined v-else />
            </template>
            {{ wsConnected ? '断开连接' : '建立连接' }}
          </a-button>
          <a-button @click="handleFullscreen">
            <template #icon><FullscreenOutlined /></template>
            全屏监控
          </a-button>
          <a-button @click="handleExportReport">
            <template #icon><DownloadOutlined /></template>
            导出报告
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 系统状态概览 -->
    <div class="status-overview">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="status-card system-health">
            <div class="status-content">
              <div class="status-icon">
                <a-badge :status="systemHealthStatus" />
              </div>
              <div class="status-info">
                <div class="status-title">系统健康状态</div>
                <div class="status-value">{{ systemHealthText }}</div>
                <div class="status-time">更新时间: {{ lastUpdateTime }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="status-card device-status">
            <a-statistic
              title="设备在线率"
              :value="deviceOnlineRate"
              suffix="%"
              :precision="1"
              :value-style="{ color: getOnlineRateColor(deviceOnlineRate) }"
            >
              <template #prefix><DesktopOutlined /></template>
            </a-statistic>
            <div class="status-detail">
              在线: {{ onlineDevices }}/{{ totalDevices }}
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="status-card access-stats">
            <a-statistic
              title="今日通行"
              :value="todayAccessCount"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix><LoginOutlined /></template>
            </a-statistic>
            <div class="status-detail">
              成功率: {{ todaySuccessRate }}%
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="status-card alert-stats">
            <a-statistic
              title="未处理告警"
              :value="unprocessedAlerts"
              :value-style="{ color: getAlertColor(unprocessedAlerts) }"
            >
              <template #icon><BellOutlined /></template>
            </a-statistic>
            <div class="status-detail">
              严重: {{ criticalAlerts }}
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 监控面板 -->
    <div class="monitor-panels">
      <a-row :gutter="16">
        <!-- 左侧面板 -->
        <a-col :span="16">
          <!-- 实时设备状态 -->
          <a-card title="设备状态监控" class="monitor-panel" :bordered="false">
            <template #extra>
              <a-space>
                <a-select
                  v-model:value="deviceFilter"
                  placeholder="设备筛选"
                  style="width: 150px"
                  allow-clear
                >
                  <a-select-option value="all">全部设备</a-select-option>
                  <a-select-option value="online">在线设备</a-select-option>
                  <a-select-option value="offline">离线设备</a-select-option>
                  <a-select-option value="fault">故障设备</a-select-option>
                </a-select>
                <a-button @click="refreshDeviceStatus">
                  <template #icon><ReloadOutlined /></template>
                </a-button>
              </a-space>
            </template>

            <div class="device-status-grid">
              <div
                v-for="device in filteredDevices"
                :key="device.deviceId"
                class="device-card"
                :class="getDeviceCardClass(device)"
                @click="handleDeviceClick(device)"
              >
                <div class="device-header">
                  <div class="device-name">{{ device.deviceName }}</div>
                  <a-badge
                    :status="device.onlineStatus === 'online' ? 'success' : 'error'"
                  />
                </div>
                <div class="device-info">
                  <div class="device-location">
                    <EnvironmentOutlined />
                    {{ device.location }}
                  </div>
                  <div class="device-status">
                    <span :class="`status-${device.status}`">
                      {{ getStatusText(device.status) }}
                    </span>
                  </div>
                </div>
                <div v-if="device.statusData" class="device-metrics">
                  <div class="metric">
                    <span class="metric-label">CPU</span>
                    <a-progress
                      :percent="device.statusData.cpuUsage"
                      size="small"
                      :status="getCpuStatus(device.statusData.cpuUsage)"
                    />
                  </div>
                  <div class="metric">
                    <span class="metric-label">内存</span>
                    <a-progress
                      :percent="device.statusData.memoryUsage"
                      size="small"
                      :status="getMemoryStatus(device.statusData.memoryUsage)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </a-card>

          <!-- 实时通行数据 -->
          <a-card title="实时通行" class="monitor-panel" :bordered="false">
            <template #extra>
              <a-switch
                v-model:checked="autoScroll"
                checked-children="自动滚动"
                un-checked-children="停止滚动"
              />
            </template>

            <div class="realtime-access-list" ref="accessListRef">
              <div
                v-for="record in realtimeAccessData"
                :key="record.recordId"
                class="access-record"
                :class="{ 'access-failed': record.accessResult !== 'success' }"
              >
                <div class="record-avatar">
                  <a-avatar
                    v-if="record.photoUrl"
                    :src="record.photoUrl"
                    :size="40"
                  />
                  <a-avatar v-else :size="40" icon="user" />
                </div>
                <div class="record-content">
                  <div class="record-user">{{ record.userName }}</div>
                  <div class="record-device">{{ record.deviceName }}</div>
                </div>
                <div class="record-result">
                  <a-tag
                    :color="getAccessResultColor(record.accessResult)"
                    :icon="getAccessResultIcon(record.accessResult)"
                  >
                    {{ getAccessResultText(record.accessResult) }}
                  </a-tag>
                </div>
                <div class="record-time">
                  {{ formatTime(record.accessTime) }}
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧面板 -->
        <a-col :span="8">
          <!-- 告警面板 -->
          <a-card title="系统告警" class="monitor-panel alert-panel" :bordered="false">
            <template #extra>
              <a-badge
                :count="unprocessedAlerts"
                :overflow-count="99"
              >
                <a-button @click="handleShowAllAlerts">
                  查看全部
                </a-button>
              </a-badge>
            </template>

            <div class="alert-list">
              <div
                v-for="alert in recentAlerts"
                :key="alert.alertId"
                class="alert-item"
                :class="`alert-${alert.level}`"
                @click="handleAlertClick(alert)"
              >
                <div class="alert-icon">
                  <ExclamationCircleOutlined />
                </div>
                <div class="alert-content">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-message">{{ alert.content }}</div>
                  <div class="alert-time">{{ formatTime(alert.timestamp) }}</div>
                </div>
                <div class="alert-actions">
                  <a-button
                    v-if="alert.status === 'new'"
                    type="link"
                    size="small"
                    @click.stop="handleProcessAlert(alert)"
                  >
                    处理
                  </a-button>
                </div>
              </div>
            </div>
          </a-card>

          <!-- 通行趋势图 -->
          <a-card title="通行趋势" class="monitor-panel" :bordered="false">
            <template #extra>
              <a-select
                v-model:value="trendTimeRange"
                style="width: 120px"
                @change="handleTrendTimeChange"
              >
                <a-select-option value="1h">最近1小时</a-select-option>
                <a-select-option value="6h">最近6小时</a-select-option>
                <a-select-option value="24h">最近24小时</a-select-option>
                <a-select-option value="7d">最近7天</a-select-option>
              </a-select>
            </template>

            <div ref="trendChartRef" class="trend-chart"></div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 告警详情弹窗 -->
    <AlertDetailModal
      v-model:visible="alertDetailVisible"
      :alert="currentAlert"
      @processed="handleAlertProcessed"
    />

    <!-- 设备详情弹窗 -->
    <DeviceDetailModal
      v-model:visible="deviceDetailVisible"
      :device="currentDevice"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import dayjs from 'dayjs';
  import {
    WifiOutlined,
    DisconnectOutlined,
    FullscreenOutlined,
    DownloadOutlined,
    DesktopOutlined,
    LoginOutlined,
    BellOutlined,
    ReloadOutlined,
    EnvironmentOutlined,
    ExclamationCircleOutlined,
  } from '@ant-design/icons-vue';
  import { useAccessMonitorStore } from '/@/store/modules/business/access-monitor';
  import { formatDateTime, formatTime } from '/@/lib/format';
  import AlertDetailModal from './components/AlertDetailModal.vue';
  import DeviceDetailModal from './components/DeviceDetailModal.vue';

  // 状态管理
  const monitorStore = useAccessMonitorStore();

  // 响应式数据
  const accessListRef = ref();
  const trendChartRef = ref();
  const deviceFilter = ref('all');
  const autoScroll = ref(true);
  const trendTimeRange = ref('1h');
  const alertDetailVisible = ref(false);
  const deviceDetailVisible = ref(false);
  const currentAlert = ref(null);
  const currentDevice = ref(null);

  // 计算属性
  const wsConnected = computed(() => monitorStore.wsConnected);
  const systemHealthStatus = computed(() => {
    const health = monitorStore.systemHealth;
    switch (health) {
      case 'good': return 'success';
      case 'warning': return 'warning';
      case 'error': return 'error';
      default: return 'default';
    }
  });

  const systemHealthText = computed(() => {
    const health = monitorStore.systemHealth;
    switch (health) {
      case 'good': return '运行正常';
      case 'warning': return '性能警告';
      case 'error': return '系统异常';
      default: return '状态未知';
    }
  });

  const lastUpdateTime = computed(() => {
    const time = monitorStore.monitorStatus.dataUpdateTime;
    return time ? formatDateTime(time) : '从未更新';
  });

  const deviceOnlineRate = computed(() => {
    const stats = monitorStore.realTimeStats;
    if (!stats || stats.totalDevices === 0) return 0;
    return Math.round((stats.onlineDevices / stats.totalDevices) * 100 * 10) / 10;
  });

  const onlineDevices = computed(() => {
    return monitorStore.realTimeStats?.onlineDevices || 0;
  });

  const totalDevices = computed(() => {
    return monitorStore.realTimeStats?.totalDevices || 0;
  });

  const todayAccessCount = computed(() => {
    return monitorStore.todayAccessStats?.totalCount || 0;
  });

  const todaySuccessRate = computed(() => {
    const stats = monitorStore.todayAccessStats;
    if (!stats || stats.totalCount === 0) return 0;
    return Math.round((stats.successCount / stats.totalCount) * 100);
  });

  const unprocessedAlerts = computed(() => {
    return monitorStore.latestAlertCount;
  });

  const criticalAlerts = computed(() => {
    return monitorStore.criticalAlertCount;
  });

  const filteredDevices = computed(() => {
    const devices = monitorStore.realTimeDeviceStatus;
    if (!devices || deviceFilter.value === 'all') return devices;

    return devices.filter(device => {
      switch (deviceFilter.value) {
        case 'online':
          return device.onlineStatus === 'online';
        case 'offline':
          return device.onlineStatus === 'offline';
        case 'fault':
          return device.status === 'fault';
        default:
          return true;
      }
    });
  });

  const realtimeAccessData = computed(() => {
    return monitorStore.realTimeAccessData.slice(0, 50); // 只显示最近50条
  });

  const recentAlerts = computed(() => {
    return monitorStore.alerts.slice(0, 10); // 只显示最近10条告警
  });

  // 方法
  const toggleConnection = () => {
    if (wsConnected.value) {
      monitorStore.closeWebSocket();
    } else {
      monitorStore.initWebSocket();
    }
  };

  const handleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
  };

  const handleExportReport = () => {
    message.info('导出监控报告功能开发中...');
  };

  const refreshDeviceStatus = () => {
    monitorStore.fetchRealTimeDeviceStatus();
  };

  const handleDeviceClick = (device) => {
    currentDevice.value = device;
    deviceDetailVisible.value = true;
  };

  const handleAlertClick = (alert) => {
    currentAlert.value = alert;
    alertDetailVisible.value = true;
  };

  const handleProcessAlert = (alert) => {
    // 处理告警的逻辑
    monitorStore.handleAlert(alert.alertId, 'resolved');
  };

  const handleAlertProcessed = () => {
    alertDetailVisible.value = false;
    monitorStore.fetchAlerts();
  };

  const handleShowAllAlerts = () => {
    message.info('查看全部告警功能开发中...');
  };

  const handleTrendTimeChange = (value) => {
    // 根据时间范围更新趋势图
    updateTrendChart(value);
  };

  const updateTrendChart = (timeRange) => {
    // 更新趋势图的逻辑
    console.log('更新趋势图:', timeRange);
  };

  // 辅助方法
  const getOnlineRateColor = (rate) => {
    if (rate >= 95) return '#52c41a';
    if (rate >= 80) return '#faad14';
    return '#ff4d4f';
  };

  const getAlertColor = (count) => {
    if (count === 0) return '#52c41a';
    if (count <= 5) return '#faad14';
    return '#ff4d4f';
  };

  const getDeviceCardClass = (device) => {
    return {
      'device-online': device.onlineStatus === 'online',
      'device-offline': device.onlineStatus === 'offline',
      'device-fault': device.status === 'fault',
    };
  };

  const getStatusText = (status) => {
    const statusMap = {
      normal: '正常',
      maintenance: '维护中',
      fault: '故障',
      offline: '离线',
    };
    return statusMap[status] || status;
  };

  const getCpuStatus = (usage) => {
    if (usage >= 80) return 'exception';
    if (usage >= 60) return 'active';
    return 'normal';
  };

  const getMemoryStatus = (usage) => {
    if (usage >= 85) return 'exception';
    if (usage >= 70) return 'active';
    return 'normal';
  };

  const getAccessResultColor = (result) => {
    const colorMap = {
      success: 'green',
      failed: 'red',
      denied: 'orange',
      timeout: 'default',
    };
    return colorMap[result] || 'default';
  };

  const getAccessResultIcon = (result) => {
    const iconMap = {
      success: 'check-circle',
      failed: 'close-circle',
      denied: 'stop',
      timeout: 'clock-circle',
    };
    return iconMap[result] || 'question-circle';
  };

  const getAccessResultText = (result) => {
    const textMap = {
      success: '成功',
      failed: '失败',
      denied: '拒绝',
      timeout: '超时',
    };
    return textMap[result] || result;
  };

  // 自动滚动处理
  watch(() => realtimeAccessData.value, () => {
    if (autoScroll.value && accessListRef.value) {
      nextTick(() => {
        accessListRef.value.scrollTop = 0;
      });
    }
  });

  // 生命周期
  onMounted(async () => {
    await Promise.all([
      monitorStore.fetchRealTimeDeviceStatus(),
      monitorStore.fetchRealTimeAccessData(),
      monitorStore.fetchRealTimeStats(),
      monitorStore.fetchAlerts(),
    ]);

    // 初始化WebSocket连接
    monitorStore.initWebSocket();
  });

  onUnmounted(() => {
    monitorStore.closeWebSocket();
  });
</script>

<style lang="less" scoped>
  .access-monitor-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: 100vh;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .page-title {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 14px;
        }
      }
    }

    .status-overview {
      margin-bottom: 24px;

      .status-card {
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &.system-health {
          border-left: 4px solid #1890ff;
        }

        .status-content {
          display: flex;
          align-items: center;
          gap: 16px;

          .status-icon {
            font-size: 24px;
          }

          .status-info {
            flex: 1;

            .status-title {
              font-size: 14px;
              color: #8c8c8c;
              margin-bottom: 4px;
            }

            .status-value {
              font-size: 20px;
              font-weight: 600;
              color: #262626;
              margin-bottom: 4px;
            }

            .status-time {
              font-size: 12px;
              color: #8c8c8c;
            }
          }
        }

        .status-detail {
          margin-top: 8px;
          font-size: 12px;
          color: #8c8c8c;
        }
      }
    }

    .monitor-panels {
      .monitor-panel {
        margin-bottom: 16px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &.alert-panel {
          max-height: 600px;
        }
      }

      .device-status-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 16px;
        max-height: 400px;
        overflow-y: auto;

        .device-card {
          border: 1px solid #f0f0f0;
          border-radius: 8px;
          padding: 16px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            border-color: #1890ff;
            box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
          }

          &.device-online {
            border-left: 4px solid #52c41a;
          }

          &.device-offline {
            border-left: 4px solid #ff4d4f;
          }

          &.device-fault {
            border-left: 4px solid #faad14;
          }

          .device-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;

            .device-name {
              font-weight: 600;
              color: #262626;
            }
          }

          .device-info {
            margin-bottom: 12px;

            .device-location {
              display: flex;
              align-items: center;
              gap: 4px;
              color: #8c8c8c;
              font-size: 12px;
              margin-bottom: 4px;
            }

            .device-status {
              .status-normal {
                color: #52c41a;
              }
              .status-maintenance {
                color: #faad14;
              }
              .status-fault {
                color: #ff4d4f;
              }
              .status-offline {
                color: #8c8c8c;
              }
            }
          }

          .device-metrics {
            .metric {
              margin-bottom: 8px;

              .metric-label {
                display: inline-block;
                width: 40px;
                font-size: 12px;
                color: #8c8c8c;
              }
            }
          }
        }
      }

      .realtime-access-list {
        height: 400px;
        overflow-y: auto;

        .access-record {
          display: flex;
          align-items: center;
          padding: 12px;
          border-bottom: 1px solid #f0f0f0;
          transition: background-color 0.3s;

          &:hover {
            background-color: #fafafa;
          }

          &.access-failed {
            background-color: #fff2f0;
            border-left: 3px solid #ff4d4f;
          }

          .record-avatar {
            margin-right: 12px;
          }

          .record-content {
            flex: 1;

            .record-user {
              font-weight: 500;
              color: #262626;
              margin-bottom: 2px;
            }

            .record-device {
              font-size: 12px;
              color: #8c8c8c;
            }
          }

          .record-result {
            margin-right: 12px;
          }

          .record-time {
            font-size: 12px;
            color: #8c8c8c;
            min-width: 80px;
            text-align: right;
          }
        }
      }

      .alert-list {
        max-height: 500px;
        overflow-y: auto;

        .alert-item {
          display: flex;
          align-items: flex-start;
          padding: 12px;
          border-bottom: 1px solid #f0f0f0;
          cursor: pointer;
          transition: background-color 0.3s;

          &:hover {
            background-color: #fafafa;
          }

          &.alert-critical {
            border-left: 3px solid #ff4d4f;
          }

          &.alert-high {
            border-left: 3px solid #faad14;
          }

          &.alert-medium {
            border-left: 3px solid #1890ff;
          }

          .alert-icon {
            margin-right: 12px;
            color: #faad14;
            font-size: 16px;
          }

          .alert-content {
            flex: 1;

            .alert-title {
              font-weight: 500;
              color: #262626;
              margin-bottom: 4px;
            }

            .alert-message {
              font-size: 12px;
              color: #8c8c8c;
              margin-bottom: 4px;
            }

            .alert-time {
              font-size: 12px;
              color: #8c8c8c;
            }
          }

          .alert-actions {
            margin-left: 12px;
          }
        }
      }

      .trend-chart {
        height: 200px;
        background: #fafafa;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #8c8c8c;
      }
    }

    // 响应式布局
    @media (max-width: 1200px) {
      .device-status-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)) !important;
      }
    }

    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .status-overview {
        .ant-col {
          margin-bottom: 16px;
        }
      }

      .monitor-panels {
        .ant-col {
          margin-bottom: 16px;
        }
      }
    }
  }
</style>