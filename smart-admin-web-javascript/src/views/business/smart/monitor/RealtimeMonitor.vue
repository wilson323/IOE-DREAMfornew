<template>
  <div class="realtime-monitor">
    <a-card title="实时数据监控" :bordered="false">
      <!-- 状态概览 -->
      <div class="status-overview">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic
              title="在线用户"
              :value="statistics.onlineUserCount"
              :value-style="{ color: '#3f8600' }"
              suffix="人"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="活跃连接"
              :value="statistics.activeConnectionCount"
              :value-style="{ color: '#1890ff' }"
              suffix="个"
            >
              <template #prefix>
                <WifiOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="实时告警"
              :value="statistics.alertCount"
              :value-style="{ color: '#cf1322' }"
              suffix="条"
            >
              <template #prefix>
                <AlertOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="消息处理"
              :value="statistics.messageProcessRate"
              suffix="/秒"
            >
              <template #prefix>
                <MessageOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
      </div>

      <!-- 监控选项卡 -->
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" class="monitor-tabs">
        <!-- 实时消息 -->
        <a-tab-pane key="messages" tab="实时消息">
          <div class="tab-content">
            <div class="action-bar">
              <a-space>
                <a-button type="primary" @click="connectWebSocket" :loading="connecting">
                  <template #icon><LinkOutlined /></template>
                  {{ wsConnected ? '断开连接' : '连接WebSocket' }}
                </a-button>
                <a-button @click="clearMessages">
                  <template #icon><DeleteOutlined /></template>
                  清空消息
                </a-button>
                <a-select
                  v-model:value="messageFilter"
                  placeholder="消息类型筛选"
                  style="width: 150px"
                  @change="filterMessages"
                >
                  <a-select-option value="ALL">全部</a-select-option>
                  <a-select-option value="LOCATION_UPDATE">位置更新</a-select-option>
                  <a-select-option value="DEVICE_STATUS">设备状态</a-select-option>
                  <a-select-option value="GEOFENCE_TRIGGER">围栏触发</a-select-option>
                  <a-select-option value="SYSTEM_NOTIFICATION">系统通知</a-select-option>
                  <a-select-option value="REALTIME_ALERT">实时告警</a-select-option>
                </a-select>
                <a-switch v-model:checked="autoScroll" checked-children="自动滚动" un-checked-children="手动滚动" />
              </a-space>
            </div>

            <div class="message-container" ref="messageContainer">
              <div
                v-for="(message, index) in filteredMessages"
                :key="index"
                :class="['message-item', getMessageClass(message.type)]"
              >
                <div class="message-header">
                  <span class="message-type">{{ message.type }}</span>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                  <span class="message-source">{{ message.sourceId || 'System' }}</span>
                </div>
                <div class="message-content">{{ message.content || JSON.stringify(message.data) }}</div>
                <div class="message-actions">
                  <a-button type="link" size="small" @click="showMessageDetail(message)">
                    详情
                  </a-button>
                  <a-button type="link" size="small" @click="copyMessage(message)">
                    复制
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </a-tab-pane>

        <!-- 队列监控 -->
        <a-tab-pane key="queues" tab="队列监控">
          <div class="tab-content">
            <div class="queue-stats">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-statistic title="高优先级队列" :value="queueStats.highPriorityQueueSize" />
                </a-col>
                <a-col :span="8">
                  <a-statistic title="普通队列" :value="queueStats.normalPriorityQueueSize" />
                </a-col>
                <a-col :span="8">
                  <a-statistic title="延迟队列" :value="queueStats.delayQueueSize" />
                </a-col>
              </a-row>
              <a-divider />
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-statistic title="高优先级处理" :value="queueStats.highPriorityProcessedCount" suffix="条" />
                </a-col>
                <a-col :span="8">
                  <a-statistic title="普通处理" :value="queueStats.normalPriorityProcessedCount" suffix="条" />
                </a-col>
                <a-col :span="8">
                  <a-statistic title="延迟处理" :value="queueStats.delayProcessedCount" suffix="条" />
                </a-col>
              </a-row>
            </div>

            <div class="queue-chart">
              <div ref="queueChart" style="width: 100%; height: 300px;"></div>
            </div>
          </div>
        </a-tab-pane>

        <!-- 告警管理 -->
        <a-tab-pane key="alerts" tab="告警管理">
          <div class="tab-content">
            <div class="alert-filters">
              <a-row :gutter="16">
                <a-col :span="6">
                  <a-select v-model:value="alertSeverity" placeholder="严重程度" style="width: 100%">
                    <a-select-option value="ALL">全部</a-select-option>
                    <a-select-option value="CRITICAL">紧急</a-select-option>
                    <a-select-option value="HIGH">高</a-select-option>
                    <a-select-option value="MEDIUM">中</a-select-option>
                    <a-select-option value="LOW">低</a-select-option>
                  </a-select>
                </a-col>
                <a-col :span="6">
                  <a-range-picker v-model:value="alertTimeRange" show-time />
                </a-col>
                <a-col :span="6">
                  <a-button type="primary" @click="loadAlerts">查询</a-button>
                  <a-button @click="refreshAlerts">刷新</a-button>
                </a-col>
              </a-row>
            </div>

            <a-table
              :columns="alertColumns"
              :data-source="alerts"
              :loading="alertsLoading"
              :pagination="alertPagination"
              row-key="id"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'severity'">
                  <a-tag :color="getSeverityColor(record.severity)">
                    {{ getSeverityLabel(record.severity) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="handleAlert(record)">
                      处理
                    </a-button>
                    <a-button type="link" size="small" @click="viewAlertDetail(record)">
                      详情
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>
        </a-tab-pane>

        <!-- 性能监控 -->
        <a-tab-pane key="performance" tab="性能监控">
          <div class="tab-content">
            <div class="performance-metrics">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-card title="消息处理速度" size="small">
                    <div ref="throughputChart" style="width: 100%; height: 250px;"></div>
                  </a-card>
                </a-col>
                <a-col :span="12">
                  <a-card title="响应时间" size="small">
                    <div ref="responseTimeChart" style="width: 100%; height: 250px;"></div>
                  </a-card>
                </a-col>
              </a-row>
              <a-row :gutter="16" style="margin-top: 16px;">
                <a-col :span="12">
                  <a-card title="内存使用" size="small">
                    <div ref="memoryChart" style="width: 100%; height: 250px;"></div>
                  </a-card>
                </a-col>
                <a-col :span="12">
                  <a-card title="连接数" size="small">
                    <div ref="connectionChart" style="width: 100%; height: 250px;"></div>
                  </a-card>
                </a-col>
              </a-row>
            </div>

            <div class="system-info">
              <a-descriptions title="系统信息" :column="2" size="small" bordered>
                <a-descriptions-item label="服务器时间">{{ systemInfo.serverTime }}</a-descriptions-item>
                <a-descriptions-item label="运行时长">{{ systemInfo.uptime }}</a-descriptions-item>
                <a-descriptions-item label="CPU使用率">{{ systemInfo.cpuUsage }}%</a-descriptions-item>
                <a-descriptions-item label="内存使用率">{{ systemInfo.memoryUsage }}%</a-descriptions-item>
                <a-descriptions-item label="线程数">{{ systemInfo.threadCount }}</a-descriptions-item>
                <a-descriptions-item label="队列大小">{{ systemInfo.queueSize }}</a-descriptions-item>
              </a-descriptions>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 消息详情弹窗 -->
    <a-modal
      v-model:open="messageDetailVisible"
      title="消息详情"
      width="800px"
      :footer="null"
    >
      <div class="message-detail">
        <pre><code>{{ JSON.stringify(selectedMessage, null, 2) }}</code></pre>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  UserOutlined,
  WifiOutlined,
  AlertOutlined,
  MessageOutlined,
  LinkOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue';
import RealtimeApi from '/@/api/realtime/RealtimeApi';
import dayjs from 'dayjs';

// 响应式数据
const activeTab = ref('messages');
const connecting = ref(false);
const wsConnected = ref(false);
const autoScroll = ref(true);
const messageFilter = ref('ALL');
const alertSeverity = ref('ALL');
const alertTimeRange = ref(null);
const alertsLoading = ref(false);

// 统计数据
const statistics = reactive({
  onlineUserCount: 0,
  activeConnectionCount: 0,
  alertCount: 0,
  messageProcessRate: 0
});

// 队列统计
const queueStats = reactive({
  highPriorityQueueSize: 0,
  normalPriorityQueueSize: 0,
  delayQueueSize: 0,
  highPriorityProcessedCount: 0,
  normalPriorityProcessedCount: 0,
  delayProcessedCount: 0
});

// 消息列表
const messages = ref([]);
const filteredMessages = computed(() => {
  if (messageFilter.value === 'ALL') {
    return messages.value;
  }
  return messages.value.filter(msg => msg.type === messageFilter.value);
});

// 告警数据
const alerts = ref([]);
const alertPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
});

// 系统信息
const systemInfo = reactive({
  serverTime: '',
  uptime: '',
  cpuUsage: 0,
  memoryUsage: 0,
  threadCount: 0,
  queueSize: 0
});

// 弹窗状态
const messageDetailVisible = ref(false);
const selectedMessage = ref(null);

// WebSocket连接
let websocket = null;
let heartbeatTimer = null;

// 告警表格列定义
const alertColumns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 100
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 120
  },
  {
    title: '严重程度',
    dataIndex: 'severity',
    key: 'severity',
    width: 80
  },
  {
    title: '消息',
    dataIndex: 'message',
    key: 'message',
    width: 200
  },
  {
    title: '时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right'
  }
];

// DOM引用
const messageContainer = ref(null);
const queueChart = ref(null);
const throughputChart = ref(null);
const responseTimeChart = ref(null);
const memoryChart = ref(null);
const connectionChart = ref(null);

// 生命周期
onMounted(() => {
  loadStatistics();
  loadQueueStats();
  loadAlerts();
  initCharts();

  // 自动刷新数据
  startAutoRefresh();
});

onUnmounted(() => {
  disconnectWebSocket();
  stopAutoRefresh();
});

// 方法
const handleTabChange = (key) => {
  activeTab.value = key;
  if (key === 'queues') {
    updateQueueChart();
  } else if (key === 'performance') {
    updatePerformanceCharts();
  }
};

const connectWebSocket = () => {
  if (wsConnected.value) {
    disconnectWebSocket();
    return;
  }

  connecting.value = true;
  try {
    websocket = new WebSocket('ws://localhost:1024/ws');

    websocket.onopen = () => {
      wsConnected.value = true;
      connecting.value = false;
      message.success('WebSocket连接成功');
      startHeartbeat();

      // 添加连接消息
      messages.value.unshift({
        type: 'CONNECTION',
        content: 'WebSocket连接已建立',
        timestamp: Date.now(),
        sourceId: 'Monitor'
      });
    };

    websocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        handleMessage(data);
      } catch (error) {
        console.error('解析WebSocket消息失败:', error);
      }
    };

    websocket.onclose = () => {
      wsConnected.value = false;
      connecting.value = false;
      message.warning('WebSocket连接已关闭');
      stopHeartbeat();
    };

    websocket.onerror = (error) => {
      wsConnected.value = false;
      connecting.value = false;
      message.error('WebSocket连接错误');
      console.error('WebSocket错误:', error);
    };

  } catch (error) {
    connecting.value = false;
    message.error('创建WebSocket连接失败');
    console.error('WebSocket创建失败:', error);
  }
};

const disconnectWebSocket = () => {
  if (websocket) {
    websocket.close();
    websocket = null;
  }
  wsConnected.value = false;
  stopHeartbeat();
};

const startHeartbeat = () => {
  heartbeatTimer = setInterval(() => {
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      websocket.send(JSON.stringify({ type: 'PING' }));
    }
  }, 30000); // 30秒心跳
};

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
};

const handleMessage = (data) => {
  // 添加消息到列表
  messages.value.unshift({
    ...data,
    timestamp: Date.now()
  });

  // 限制消息数量
  if (messages.value.length > 1000) {
    messages.value = messages.value.slice(0, 1000);
  }

  // 自动滚动
  if (autoScroll.value) {
    nextTick(() => {
      scrollToBottom();
    });
  }

  // 更新统计
  if (data.type === 'USER_CONNECT' || data.type === 'USER_DISCONNECT') {
    loadStatistics();
  } else if (data.type === 'REALTIME_ALERT') {
    loadStatistics();
  }
};

const scrollToBottom = () => {
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

const clearMessages = () => {
  messages.value = [];
  message.info('消息已清空');
};

const filterMessages = () => {
  // 消息过滤通过计算属性自动处理
};

const showMessageDetail = (msg) => {
  selectedMessage.value = msg;
  messageDetailVisible.value = true;
};

const copyMessage = (msg) => {
  navigator.clipboard.writeText(JSON.stringify(msg, null, 2))
    .then(() => message.success('消息已复制到剪贴板'))
    .catch(() => message.error('复制失败'));
};

const getMessageClass = (type) => {
  const classMap = {
    'LOCATION_UPDATE': 'message-location',
    'DEVICE_STATUS': 'message-device',
    'GEOFENCE_TRIGGER': 'message-geofence',
    'SYSTEM_NOTIFICATION': 'message-system',
    'REALTIME_ALERT': 'message-alert',
    'CONNECTION': 'message-connection'
  };
  return classMap[type] || 'message-default';
};

const formatTime = (timestamp) => {
  return dayjs(timestamp).format('HH:mm:ss.SSS');
};

const getSeverityColor = (severity) => {
  const colorMap = {
    'CRITICAL': 'red',
    'HIGH': 'orange',
    'MEDIUM': 'blue',
    'LOW': 'green'
  };
  return colorMap[severity] || 'default';
};

const getSeverityLabel = (severity) => {
  const labelMap = {
    'CRITICAL': '紧急',
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  };
  return labelMap[severity] || severity;
};

const loadStatistics = async () => {
  try {
    const response = await RealtimeApi.getStatistics();
    if (response.success) {
      Object.assign(statistics, response.data);
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

const loadQueueStats = async () => {
  try {
    const response = await RealtimeApi.getQueueStatistics();
    if (response.success) {
      Object.assign(queueStats, response.data);
    }
  } catch (error) {
    console.error('加载队列统计失败:', error);
  }
};

const loadAlerts = async () => {
  alertsLoading.value = true;
  try {
    const response = await RealtimeApi.getAlerts({
      severity: alertSeverity.value,
      startTime: alertTimeRange.value?.[0],
      endTime: alertTimeRange.value?.[1],
      page: alertPagination.current - 1,
      size: alertPagination.pageSize
    });

    if (response.success) {
      alerts.value = response.data.content || [];
      alertPagination.total = response.data.total || 0;
    }
  } catch (error) {
    console.error('加载告警数据失败:', error);
    message.error('加载告警数据失败');
  } finally {
    alertsLoading.value = false;
  }
};

const refreshAlerts = () => {
  loadAlerts();
};

const handleAlert = (alert) => {
  // 处理告警逻辑
  message.info(`处理告警: ${alert.id}`);
};

const viewAlertDetail = (alert) => {
  // 查看告警详情
  message.info(`查看告警详情: ${alert.id}`);
};

// 图表初始化（简化实现）
const initCharts = () => {
  // 这里可以使用ECharts或其他图表库来初始化图表
  console.log('初始化图表');
};

const updateQueueChart = () => {
  // 更新队列图表
  console.log('更新队列图表');
};

const updatePerformanceCharts = () => {
  // 更新性能图表
  console.log('更新性能图表');
};

// 自动刷新
let refreshTimer = null;
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadStatistics();
    loadQueueStats();
  }, 5000); // 5秒刷新一次
};

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
};
</script>

<style scoped>
.realtime-monitor {
  padding: 16px;
}

.status-overview {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 6px;
}

.monitor-tabs {
  margin-top: 16px;
}

.tab-content {
  padding-top: 16px;
}

.action-bar {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.message-container {
  height: 400px;
  overflow-y: auto;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 8px;
}

.message-item {
  margin-bottom: 8px;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid #d9d9d9;
}

.message-location {
  border-left-color: #52c41a;
}

.message-device {
  border-left-color: #1890ff;
}

.message-geofence {
  border-left-color: #722ed1;
}

.message-system {
  border-left-color: #fa8c16;
}

.message-alert {
  border-left-color: #f5222d;
}

.message-connection {
  border-left-color: #13c2c2;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
}

.message-type {
  font-weight: bold;
}

.message-time {
  color: #999;
}

.message-source {
  color: #666;
}

.message-content {
  margin-bottom: 4px;
  font-size: 14px;
  word-break: break-all;
}

.message-actions {
  display: flex;
  gap: 8px;
}

.queue-stats {
  margin-bottom: 24px;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 6px;
}

.alert-filters {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.performance-metrics {
  margin-bottom: 24px;
}

.system-info {
  margin-top: 24px;
}

.message-detail {
  max-height: 500px;
  overflow-y: auto;
}

.message-detail pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>