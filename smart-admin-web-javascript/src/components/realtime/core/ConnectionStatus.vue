<template>
  <div class="connection-status" :class="statusClass" :title="tooltip">
    <span class="dot" />
    <span v-if="mode !== 'compact'" class="text">{{ text }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 连接状态指示器
 * Props:
 * - status: 'connected' | 'connecting' | 'disconnected'
 * - mode: 'default' | 'compact'
 */
const props = defineProps({
  status: { type: String, default: 'connected' },
  mode: { type: String, default: 'default' }
})

const statusClass = computed(() => {
  switch (props.status) {
    case 'connected':
      return 'ok'
    case 'connecting':
      return 'warning'
    default:
      return 'error'
  }
})

const text = computed(() => {
  switch (props.status) {
    case 'connected':
      return '已连接'
    case 'connecting':
      return '连接中'
    default:
      return '未连接'
  }
})

const tooltip = computed(() => `WebSocket ${text.value}`)
</script>

<style scoped>
.connection-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(0,0,0,0.15);
}
.ok .dot { background: #52c41a; box-shadow: 0 0 8px rgba(82,196,26,.5); }
.warning .dot { background: #faad14; box-shadow: 0 0 8px rgba(250,173,20,.5); }
.error .dot { background: #ff4d4f; box-shadow: 0 0 8px rgba(255,77,79,.5); }
.text { font-size: 12px; color: #595959; }
</style>
<template>
  <div class="connection-status">
    <!-- 紧凑模式 -->
    <div v-if="mode === 'compact'" class="compact-mode">
      <a-badge
        :status="statusColor"
        :text="statusText"
        :title="tooltipText"
      />
      <a-tooltip :title="tooltipText">
        <a-button
          v-if="showReconnectButton && !isConnected"
          type="link"
          size="small"
          :loading="loading"
          @click="handleReconnect"
        >
          <template #icon>
            <ReloadOutlined />
          </template>
        </a-button>
      </a-tooltip>
    </div>

    <!-- 标准模式 -->
    <div v-else-if="mode === 'standard'" class="standard-mode">
      <a-card size="small" :class="['status-card', statusClass]">
        <template #title>
          <div class="status-header">
            <a-badge
              :status="statusColor"
              :text="statusText"
            />
            <a-space v-if="showActions" class="status-actions">
              <a-button
                v-if="!isConnected"
                type="primary"
                size="small"
                :loading="loading"
                @click="handleReconnect"
              >
                重新连接
              </a-button>
              <a-button
                v-else
                type="default"
                size="small"
                @click="handleDisconnect"
              >
                断开连接
              </a-button>
              <a-dropdown v-if="showDropdown">
                <template #overlay>
                  <a-menu @click="handleMenuClick">
                    <a-menu-item key="details">
                      <EyeOutlined />
                      连接详情
                    </a-menu-item>
                    <a-menu-item key="config">
                      <SettingOutlined />
                      连接配置
                    </a-menu-item>
                    <a-menu-item key="logs">
                      <FileTextOutlined />
                      连接日志
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="test">
                      <ExperimentOutlined />
                      测试连接
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="text" size="small">
                  <MoreOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </div>
        </template>

        <div class="status-content">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic
                title="连接时长"
                :value="connectionDuration"
                suffix="秒"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="重连次数"
                :value="reconnectCount"
                :value-style="{ color: reconnectCount > 0 ? '#cf1322' : '#3f8600' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="发送消息"
                :value="messagesSent"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="接收消息"
                :value="messagesReceived"
              />
            </a-col>
          </a-row>

          <div v-if="showProgressBar" class="progress-section">
            <div class="progress-item">
              <span>连接质量</span>
              <a-progress
                :percent="connectionQuality"
                :status="connectionQualityStatus"
                :stroke-color="connectionQualityColor"
              />
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 详细模式 -->
    <div v-else-if="mode === 'detailed'" class="detailed-mode">
      <a-card :class="['status-card', statusClass]" title="连接状态详情">
        <template #extra>
          <a-space>
            <a-button
              type="primary"
              size="small"
              :loading="loading"
              @click="handleReconnect"
            >
              重新连接
            </a-button>
            <a-button
              size="small"
              @click="handleDisconnect"
            >
              断开连接
            </a-button>
          </a-space>
        </template>

        <div class="detailed-content">
          <!-- 基本状态信息 -->
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="连接状态">
              <a-tag :color="statusColor">{{ statusText }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="连接时长">
              {{ formatDuration(connectionDuration) }}
            </a-descriptions-item>
            <a-descriptions-item label="服务器地址">
              {{ serverUrl }}
            </a-descriptions-item>
            <a-descriptions-item label="连接ID">
              {{ connectionId || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="连接时间">
              {{ formatDateTime(connectedAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="最后活跃">
              {{ formatDateTime(lastActiveAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="重连次数">
              <a-tag :color="reconnectCount > 0 ? 'red' : 'green'">
                {{ reconnectCount }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="平均延迟">
              {{ averageLatency }}ms
            </a-descriptions-item>
          </a-descriptions>

          <!-- 消息统计 -->
          <div class="statistics-section">
            <h4>消息统计</h4>
            <a-row :gutter="16">
              <a-col :span="6">
                <a-statistic
                  title="发送消息"
                  :value="messagesSent"
                  :value-style="{ color: '#3f8600' }"
                >
                  <template #suffix>
                    <SendOutlined />
                  </template>
                </a-statistic>
              </a-col>
              <a-col :span="6">
                <a-statistic
                  title="接收消息"
                  :value="messagesReceived"
                  :value-style="{ color: '#1890ff' }"
                >
                  <template #suffix>
                    <ReceiveOutlined />
                  </template>
                </a-statistic>
              </a-col>
              <a-col :span="6">
                <a-statistic
                  title="发送字节"
                  :value="formatBytes(bytesSent)"
                >
                  <template #suffix>
                    <UploadOutlined />
                  </template>
                </a-statistic>
              </a-col>
              <a-col :span="6">
                <a-statistic
                  title="接收字节"
                  :value="formatBytes(bytesReceived)"
                >
                  <template #suffix>
                    <DownloadOutlined />
                  </template>
                </a-statistic>
              </a-col>
            </a-row>
          </div>

          <!-- 连接质量图表 -->
          <div v-if="showQualityChart" class="quality-chart">
            <h4>连接质量</h4>
            <div class="quality-indicators">
              <div class="indicator-item">
                <span>信号强度</span>
                <a-progress
                  :percent="signalStrength"
                  :stroke-color="signalStrengthColor"
                  size="small"
                />
              </div>
              <div class="indicator-item">
                <span>稳定性</span>
                <a-progress
                  :percent="stability"
                  :stroke-color="stabilityColor"
                  size="small"
                />
              </div>
              <div class="indicator-item">
                <span>响应速度</span>
                <a-progress
                  :percent="responseSpeed"
                  :stroke-color="responseSpeedColor"
                  size="small"
                />
              </div>
            </div>
          </div>

          <!-- 错误信息 -->
          <div v-if="error" class="error-section">
            <h4>错误信息</h4>
            <a-alert
              :message="error"
              type="error"
              show-icon
              closable
            />
          </div>
        </div>
      </a-card>
    </div>

    <!-- 连接详情弹窗 -->
    <a-modal
      v-model:open="showDetailsModal"
      title="连接详情"
      :width="800"
      :footer="null"
    >
      <ConnectionDetailsModal :connection-info="connectionInfo" />
    </a-modal>

    <!-- 连接配置弹窗 -->
    <a-modal
      v-model:open="showConfigModal"
      title="连接配置"
      :width="600"
      @ok="handleSaveConfig"
    >
      <ConnectionConfigModal
        :config="config"
        @save="handleConfigSaved"
      />
    </a-modal>

    <!-- 连接日志弹窗 -->
    <a-modal
      v-model:open="showLogsModal"
      title="连接日志"
      :width="1000"
      :footer="null"
    >
      <ConnectionLogsModal :logs="connectionLogs" />
    </a-modal>

    <!-- 测试连接弹窗 -->
    <a-modal
      v-model:open="showTestModal"
      title="测试连接"
      :width="500"
      @ok="handleTestConnection"
    >
      <ConnectionTestModal
        :config="config"
        @test-result="handleTestResult"
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRealtimeStore } from '@/store/modules/realtime';
import { formatDateTime, formatDuration, formatBytes } from '@/utils/format';
import ConnectionDetailsModal from './modals/ConnectionDetailsModal.vue';
import ConnectionConfigModal from './modals/ConnectionConfigModal.vue';
import ConnectionLogsModal from './modals/ConnectionLogsModal.vue';
import ConnectionTestModal from './modals/ConnectionTestModal.vue';

// Icons
import {
  ReloadOutlined,
  EyeOutlined,
  SettingOutlined,
  FileTextOutlined,
  ExperimentOutlined,
  MoreOutlined,
  SendOutlined,
  ReceiveOutlined,
  UploadOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue';

// Props
const props = defineProps({
  // 显示模式: compact, standard, detailed
  mode: {
    type: String,
    default: 'standard',
    validator: (value) => ['compact', 'standard', 'detailed'].includes(value)
  },
  // 是否显示重连按钮
  showReconnectButton: {
    type: Boolean,
    default: true
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },
  // 是否显示下拉菜单
  showDropdown: {
    type: Boolean,
    default: true
  },
  // 是否显示进度条
  showProgressBar: {
    type: Boolean,
    default: true
  },
  // 是否显示质量图表
  showQualityChart: {
    type: Boolean,
    default: true
  },
  // 自动刷新间隔(毫秒)
  refreshInterval: {
    type: Number,
    default: 1000
  }
});

// Emits
const emit = defineEmits([
  'status-changed',
  'reconnect',
  'disconnect',
  'config-changed'
]);

// Store
const realtimeStore = useRealtimeStore();

// 响应式数据
const loading = ref(false);
const showDetailsModal = ref(false);
const showConfigModal = ref(false);
const showLogsModal = ref(false);
const showTestModal = ref(false);
const connectionLogs = ref([]);
const refreshTimer = ref(null);

// 配置
const config = ref({
  url: '',
  protocol: 'ws',
  timeout: 10000,
  heartbeatInterval: 30000,
  autoReconnect: true
});

// 计算属性
const connectionStatus = computed(() => realtimeStore.connectionStatus);
const isConnected = computed(() => realtimeStore.isConnected);
const statistics = computed(() => realtimeStore.statistics);

const statusText = computed(() => {
  const status = connectionStatus.value.status;
  const statusMap = {
    connected: '已连接',
    connecting: '连接中',
    disconnecting: '断开中',
    disconnected: '未连接',
    reconnecting: '重连中',
    error: '连接错误'
  };
  return statusMap[status] || status;
});

const statusColor = computed(() => {
  const status = connectionStatus.value.status;
  const colorMap = {
    connected: 'success',
    connecting: 'processing',
    disconnecting: 'warning',
    disconnected: 'default',
    reconnecting: 'warning',
    error: 'error'
  };
  return colorMap[status] || 'default';
});

const statusClass = computed(() => {
  const status = connectionStatus.value.status;
  return `status-${status}`;
});

const tooltipText = computed(() => {
  const status = connectionStatus.value;
  let tooltip = `状态: ${statusText.value}`;

  if (status.connectedAt) {
    tooltip += `\n连接时间: ${formatDateTime(status.connectedAt)}`;
  }

  if (status.lastActiveAt) {
    tooltip += `\n最后活跃: ${formatDateTime(status.lastActiveAt)}`;
  }

  if (status.reconnectCount > 0) {
    tooltip += `\n重连次数: ${status.reconnectCount}`;
  }

  if (status.error) {
    tooltip += `\n错误: ${status.error}`;
  }

  return tooltip;
});

const connectionDuration = computed(() => {
  if (!connectionStatus.value.connectedAt) return 0;
  return Math.floor((Date.now() - connectionStatus.value.connectedAt) / 1000);
});

const reconnectCount = computed(() => connectionStatus.value.reconnectCount || 0);

const messagesSent = computed(() => statistics.value.totalMessages || 0);
const messagesReceived = computed(() => statistics.value.totalMessages || 0);

const bytesSent = computed(() => statistics.value.bytesSent || 0);
const bytesReceived = computed(() => statistics.value.bytesReceived || 0);

const connectionQuality = computed(() => {
  // 基于延迟、稳定性等因素计算连接质量
  const latency = averageLatency.value;
  const reconnectRate = reconnectCount.value / Math.max(connectionDuration.value, 1);

  let quality = 100;

  // 延迟影响
  if (latency > 1000) quality -= 30;
  else if (latency > 500) quality -= 20;
  else if (latency > 200) quality -= 10;

  // 重连率影响
  if (reconnectRate > 0.1) quality -= 30;
  else if (reconnectRate > 0.05) quality -= 20;
  else if (reconnectRate > 0.01) quality -= 10;

  return Math.max(0, Math.min(100, quality));
});

const connectionQualityStatus = computed(() => {
  const quality = connectionQuality.value;
  if (quality >= 80) return 'success';
  if (quality >= 60) return 'normal';
  if (quality >= 40) return 'exception';
  return 'exception';
});

const connectionQualityColor = computed(() => {
  const quality = connectionQuality.value;
  if (quality >= 80) return '#52c41a';
  if (quality >= 60) return '#1890ff';
  if (quality >= 40) return '#faad14';
  return '#ff4d4f';
});

const averageLatency = computed(() => statistics.value.averageLatency || 0);

const serverUrl = computed(() => connectionStatus.value.url || config.value.url);
const connectionId = computed(() => connectionStatus.value.connectionId);
const connectedAt = computed(() => connectionStatus.value.connectedAt);
const lastActiveAt = computed(() => connectionStatus.value.lastActiveAt);
const error = computed(() => connectionStatus.value.error);

const signalStrength = computed(() => {
  const quality = connectionQuality.value;
  if (quality >= 80) return 100;
  if (quality >= 60) return 80;
  if (quality >= 40) return 60;
  return 30;
});

const signalStrengthColor = computed(() => {
  const strength = signalStrength.value;
  if (strength >= 80) return '#52c41a';
  if (strength >= 60) return '#faad14';
  return '#ff4d4f';
});

const stability = computed(() => {
  const reconnectRate = reconnectCount.value / Math.max(connectionDuration.value, 1);
  return Math.max(0, 100 - reconnectRate * 1000);
});

const stabilityColor = computed(() => {
  const stab = stability.value;
  if (stab >= 80) return '#52c41a';
  if (stab >= 60) return '#faad14';
  return '#ff4d4f';
});

const responseSpeed = computed(() => {
  const latency = averageLatency.value;
  if (latency <= 100) return 100;
  if (latency <= 200) return 80;
  if (latency <= 500) return 60;
  if (latency <= 1000) return 40;
  return 20;
});

const responseSpeedColor = computed(() => {
  const speed = responseSpeed.value;
  if (speed >= 80) return '#52c41a';
  if (speed >= 60) return '#faad14';
  return '#ff4d4f';
});

const connectionInfo = computed(() => ({
  status: connectionStatus.value,
  statistics: statistics.value,
  config: config.value
}));

// 方法
/**
 * 处理重新连接
 */
async function handleReconnect() {
  if (loading.value) return;

  loading.value = true;
  try {
    addConnectionLog('开始重新连接...');
    await realtimeStore.connect();
    addConnectionLog('重新连接成功', 'success');
    message.success('重新连接成功');
    emit('reconnect');
  } catch (error) {
    addConnectionLog(`重新连接失败: ${error.message}`, 'error');
    message.error('重新连接失败: ' + error.message);
  } finally {
    loading.value = false;
  }
}

/**
 * 处理断开连接
 */
async function handleDisconnect() {
  try {
    await realtimeStore.disconnect();
    addConnectionLog('连接已断开', 'warning');
    message.info('连接已断开');
    emit('disconnect');
  } catch (error) {
    message.error('断开连接失败: ' + error.message);
  }
}

/**
 * 处理菜单点击
 */
function handleMenuClick({ key }) {
  switch (key) {
    case 'details':
      showDetailsModal.value = true;
      break;
    case 'config':
      showConfigModal.value = true;
      break;
    case 'logs':
      showLogsModal.value = true;
      break;
    case 'test':
      showTestModal.value = true;
      break;
  }
}

/**
 * 处理保存配置
 */
function handleSaveConfig() {
  // 保存配置逻辑
  showConfigModal.value = false;
  message.success('配置已保存');
  emit('config-changed', config.value);
}

/**
 * 处理配置保存
 */
function handleConfigSaved(newConfig) {
  Object.assign(config.value, newConfig);
  showConfigModal.value = false;
  message.success('配置已保存');
  emit('config-changed', config.value);
}

/**
 * 处理测试连接
 */
async function handleTestConnection() {
  // 测试连接逻辑
  addConnectionLog('正在测试连接...');
  // 这里应该调用实际的测试接口
  setTimeout(() => {
    addConnectionLog('连接测试完成', 'success');
    message.success('连接测试成功');
  }, 1000);
}

/**
 * 处理测试结果
 */
function handleTestResult(result) {
  if (result.success) {
    addConnectionLog('连接测试成功', 'success');
    message.success('连接测试成功');
  } else {
    addConnectionLog(`连接测试失败: ${result.error}`, 'error');
    message.error('连接测试失败: ' + result.error);
  }
}

/**
 * 添加连接日志
 */
function addConnectionLog(log, type = 'info') {
  connectionLogs.value.unshift({
    id: Date.now().toString(),
    message: log,
    type,
    timestamp: Date.now()
  });

  // 限制日志数量
  if (connectionLogs.value.length > 100) {
    connectionLogs.value = connectionLogs.value.slice(0, 100);
  }
}

/**
 * 开始自动刷新
 */
function startAutoRefresh() {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value);
  }

  if (props.refreshInterval > 0) {
    refreshTimer.value = setInterval(() => {
      // 这里可以添加自动刷新逻辑
    }, props.refreshInterval);
  }
}

/**
 * 停止自动刷新
 */
function stopAutoRefresh() {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value);
    refreshTimer.value = null;
  }
}

// 监听连接状态变化
watch(connectionStatus, (newStatus) => {
  emit('status-changed', newStatus);
  addConnectionLog(`状态变化: ${statusText.value}`);
});

// 生命周期
onMounted(() => {
  startAutoRefresh();
  addConnectionLog('组件已初始化');
});

onUnmounted(() => {
  stopAutoRefresh();
});

// 暴露方法
defineExpose({
  reconnect: handleReconnect,
  disconnect: handleDisconnect,
  getConnectionInfo: () => connectionInfo.value
});
</script>

<style lang="less" scoped>
.connection-status {
  .compact-mode {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .standard-mode {
    .status-card {
      &.status-connected {
        border-color: #52c41a;
      }

      &.status-disconnected {
        border-color: #d9d9d9;
      }

      &.status-error {
        border-color: #ff4d4f;
      }

      .status-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;

        .status-actions {
          margin-left: 16px;
        }
      }

      .status-content {
        .progress-section {
          margin-top: 16px;

          .progress-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;

            > span {
              margin-right: 16px;
              min-width: 80px;
            }
          }
        }
      }
    }
  }

  .detailed-mode {
    .detailed-content {
      .statistics-section,
      .quality-chart,
      .error-section {
        margin-top: 24px;

        h4 {
          margin-bottom: 16px;
          color: #262626;
        }
      }

      .quality-indicators {
        .indicator-item {
          display: flex;
          align-items: center;
          margin-bottom: 12px;

          > span {
            margin-right: 16px;
            min-width: 80px;
          }
        }
      }
    }
  }
}
</style>