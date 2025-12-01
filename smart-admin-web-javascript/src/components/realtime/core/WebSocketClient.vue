<template>
  <div class="websocket-client">
    <!-- 连接状态指示器 -->
    <div class="connection-indicator">
      <a-badge
        :status="connectionStatusColor"
        :text="connectionStatusText"
        :title="connectionTooltip"
      />
      <a-button
        v-if="!isConnected"
        type="link"
        size="small"
        :loading="loading.connecting"
        @click="handleConnect"
      >
        重新连接
      </a-button>
      <a-button
        v-else
        type="link"
        size="small"
        @click="handleDisconnect"
      >
        断开连接
      </a-button>
    </div>

    <!-- 连接详情弹窗 -->
    <a-modal
      v-model:open="showDetailsModal"
      title="WebSocket连接详情"
      :width="800"
      :footer="null"
    >
      <div class="connection-details">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="连接状态">
            <a-tag :color="connectionStatusColor">
              {{ connectionStatusText }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="服务器地址">
            {{ config.websocket.url }}
          </a-descriptions-item>
          <a-descriptions-item label="连接时间">
            {{ formatTime(connectionStatus.connectedAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="最后活跃">
            {{ formatTime(connectionStatus.lastActiveAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="重连次数">
            {{ connectionStatus.reconnectCount }}
          </a-descriptions-item>
          <a-descriptions-item label="订阅数量">
            {{ activeSubscriptions.length }}
          </a-descriptions-item>
          <a-descriptions-item label="发送消息">
            {{ statistics.totalMessagesSent }}
          </a-descriptions-item>
          <a-descriptions-item label="接收消息">
            {{ statistics.totalMessagesReceived }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 连接日志 -->
        <div class="connection-logs">
          <h4>连接日志</h4>
          <a-timeline>
            <a-timeline-item
              v-for="log in connectionLogs"
              :key="log.id"
              :color="log.color"
            >
              <span class="log-time">{{ formatTime(log.timestamp) }}</span>
              <span class="log-message">{{ log.message }}</span>
            </a-timeline-item>
          </a-timeline>
        </div>

        <!-- 消息统计 -->
        <div class="message-statistics">
          <h4>消息统计</h4>
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic title="总消息数" :value="statistics.totalMessages" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="告警数量" :value="statistics.totalAlerts" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="连接次数" :value="statistics.totalConnections" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="平均响应时间" :value="statistics.averageResponseTime" suffix="ms" />
            </a-col>
          </a-row>
        </div>
      </div>
    </a-modal>

    <!-- 重连配置弹窗 -->
    <a-modal
      v-model:open="showReconnectModal"
      title="重连配置"
      :width="600"
      @ok="handleSaveReconnectConfig"
    >
      <a-form
        ref="reconnectForm"
        :model="reconnectConfig"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="自动重连" name="autoReconnect">
          <a-switch v-model:checked="reconnectConfig.autoReconnect" />
        </a-form-item>
        <a-form-item label="最大重连次数" name="maxReconnectAttempts">
          <a-input-number
            v-model:value="reconnectConfig.maxReconnectAttempts"
            :min="1"
            :max="20"
          />
        </a-form-item>
        <a-form-item label="重连间隔(ms)" name="reconnectInterval">
          <a-input-number
            v-model:value="reconnectConfig.reconnectInterval"
            :min="1000"
            :max="60000"
          />
        </a-form-item>
        <a-form-item label="心跳间隔(ms)" name="heartbeatInterval">
          <a-input-number
            v-model:value="reconnectConfig.heartbeatInterval"
            :min="5000"
            :max="300000"
          />
        </a-form-item>
        <a-form-item label="连接超时(ms)" name="timeout">
          <a-input-number
            v-model:value="reconnectConfig.timeout"
            :min="1000"
            :max="60000"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 消息发送器 -->
    <div class="message-sender">
      <a-space>
        <a-input
          v-model:value="messageInput"
          placeholder="输入要发送的消息(JSON格式)"
          :disabled="!isConnected"
          @press-enter="handleSendMessage"
        />
        <a-button
          type="primary"
          :disabled="!isConnected || !messageInput.trim()"
          @click="handleSendMessage"
        >
          发送
        </a-button>
        <a-button @click="showDetailsModal = true">
          详情
        </a-button>
        <a-button @click="showReconnectModal = true">
          配置
        </a-button>
      </a-space>
    </div>

    <!-- 订阅管理 -->
    <div class="subscription-manager">
      <div class="subscription-header">
        <h4>订阅管理</h4>
        <a-button type="primary" size="small" @click="showSubscribeModal = true">
          添加订阅
        </a-button>
      </div>

      <a-list
        size="small"
        :data-source="activeSubscriptions"
        :locale="{ emptyText: '暂无订阅' }"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <div class="subscription-item">
              <div class="subscription-info">
                <a-tag color="blue">{{ item.topic }}</a-tag>
                <span class="subscription-filters">
                  {{ item.filters?.length || 0 }} 个过滤器
                </span>
              </div>
              <div class="subscription-actions">
                <a-button type="link" size="small" @click="handleViewSubscription(item)">
                  查看
                </a-button>
                <a-button type="link" size="small" danger @click="handleUnsubscribe(item.id)">
                  取消
                </a-button>
              </div>
            </div>
          </a-list-item>
        </template>
      </a-list>
    </div>

    <!-- 添加订阅弹窗 -->
    <a-modal
      v-model:open="showSubscribeModal"
      title="添加订阅"
      :width="700"
      @ok="handleAddSubscription"
    >
      <a-form
        ref="subscribeForm"
        :model="newSubscription"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="主题" name="topic" :rules="[{ required: true, message: '请输入主题' }]">
          <a-input v-model:value="newSubscription.topic" placeholder="例如: device.monitor" />
        </a-form-item>
        <a-form-item label="过滤器">
          <div class="filters-section">
            <div v-for="(filter, index) in newSubscription.filters" :key="index" class="filter-item">
              <a-row :gutter="8">
                <a-col :span="6">
                  <a-input v-model:value="filter.field" placeholder="字段名" />
                </a-col>
                <a-col :span="5">
                  <a-select v-model:value="filter.operator" placeholder="操作符">
                    <a-select-option value="eq">等于</a-select-option>
                    <a-select-option value="ne">不等于</a-select-option>
                    <a-select-option value="gt">大于</a-select-option>
                    <a-select-option value="gte">大于等于</a-select-option>
                    <a-select-option value="lt">小于</a-select-option>
                    <a-select-option value="lte">小于等于</a-select-option>
                    <a-select-option value="in">包含</a-select-option>
                    <a-select-option value="nin">不包含</a-select-option>
                  </a-select>
                </a-col>
                <a-col :span="8">
                  <a-input v-model:value="filter.value" placeholder="值" />
                </a-col>
                <a-col :span="3">
                  <a-button type="text" danger @click="removeFilter(index)">
                    删除
                  </a-button>
                </a-col>
              </a-row>
            </div>
            <a-button type="dashed" @click="addFilter">
              添加过滤器
            </a-button>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { useRealtimeStore } from '@/store/modules/realtime';
import { useEventBus } from '@/utils/event-bus';
import { formatDateTime } from '@/utils/format';

// Props
const props = defineProps({
  // 是否显示连接详情
  showDetails: {
    type: Boolean,
    default: false
  },
  // 是否显示订阅管理
  showSubscriptionManager: {
    type: Boolean,
    default: true
  },
  // 是否显示消息发送器
  showMessageSender: {
    type: Boolean,
    default: true
  }
});

// Emits
const emit = defineEmits([
  'connection-changed',
  'message-received',
  'subscription-added',
  'subscription-removed'
]);

// Store
const realtimeStore = useRealtimeStore();
const eventBus = useEventBus('websocket-client');

// 响应式数据
const messageInput = ref('');
const showDetailsModal = ref(props.showDetails);
const showReconnectModal = ref(false);
const showSubscribeModal = ref(false);
const connectionLogs = ref([]);

// 重连配置
const reconnectConfig = ref({
  autoReconnect: true,
  maxReconnectAttempts: 5,
  reconnectInterval: 3000,
  heartbeatInterval: 30000,
  timeout: 10000
});

// 新订阅表单
const newSubscription = ref({
  topic: '',
  filters: []
});

// 表单引用
const reconnectForm = ref();
const subscribeForm = ref();

// 计算属性
const isConnected = computed(() => realtimeStore.isConnected);
const connectionStatus = computed(() => realtimeStore.connectionStatus);
const config = computed(() => realtimeStore.config);
const activeSubscriptions = computed(() => realtimeStore.activeSubscriptions);
const statistics = computed(() => realtimeStore.statistics);

const connectionStatusText = computed(() => realtimeStore.connectionStatusText);
const connectionStatusColor = computed(() => realtimeStore.connectionStatusColor);

const connectionTooltip = computed(() => {
  const status = connectionStatus.value;
  let tooltip = `状态: ${connectionStatusText.value}`;

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

// 方法
/**
 * 格式化时间
 */
function formatTime(timestamp) {
  if (!timestamp) return '-';
  return formatDateTime(timestamp);
}

/**
 * 添加连接日志
 */
function addConnectionLog(message, color = 'blue') {
  connectionLogs.value.unshift({
    id: Date.now().toString(),
    message,
    timestamp: Date.now(),
    color
  });

  // 限制日志数量
  if (connectionLogs.value.length > 50) {
    connectionLogs.value = connectionLogs.value.slice(0, 50);
  }
}

/**
 * 处理连接
 */
async function handleConnect() {
  try {
    await realtimeStore.connect();
    addConnectionLog('WebSocket连接成功', 'green');
  } catch (error) {
    addConnectionLog(`连接失败: ${error.message}`, 'red');
    message.error('连接失败: ' + error.message);
  }
}

/**
 * 处理断开连接
 */
function handleDisconnect() {
  Modal.confirm({
    title: '确认断开连接',
    content: '断开连接后将无法接收实时数据，是否继续？',
    onOk() {
      realtimeStore.disconnect();
      addConnectionLog('WebSocket连接已断开', 'orange');
    }
  });
}

/**
 * 处理发送消息
 */
async function handleSendMessage() {
  if (!messageInput.value.trim()) return;

  try {
    let messageData = messageInput.value;

    // 尝试解析JSON
    try {
      const parsed = JSON.parse(messageInput.value);
      messageData = parsed;
    } catch {
      // 如果不是JSON格式，作为文本消息发送
    }

    await realtimeStore.sendMessage(messageData);
    messageInput.value = '';
    addConnectionLog('消息发送成功', 'green');
    message.success('消息发送成功');
  } catch (error) {
    addConnectionLog(`发送失败: ${error.message}`, 'red');
    message.error('发送失败: ' + error.message);
  }
}

/**
 * 处理保存重连配置
 */
function handleSaveReconnectConfig() {
  reconnectForm.value.validate().then(() => {
    Object.assign(realtimeStore.config.websocket, reconnectConfig.value);
    showReconnectModal.value = false;
    message.success('配置已保存');
  });
}

/**
 * 处理添加订阅
 */
async function handleAddSubscription() {
  try {
    await subscribeForm.value.validate();

    const subscriptionId = await realtimeStore.subscribe(
      newSubscription.value.topic,
      newSubscription.value.filters
    );

    showSubscribeModal.value = false;
    newSubscription.value = { topic: '', filters: [] };

    addConnectionLog(`订阅主题: ${newSubscription.value.topic}`, 'blue');
    message.success('订阅添加成功');

    emit('subscription-added', subscriptionId);
  } catch (error) {
    console.error('添加订阅失败:', error);
    message.error('添加订阅失败: ' + error.message);
  }
}

/**
 * 处理取消订阅
 */
function handleUnsubscribe(subscriptionId) {
  Modal.confirm({
    title: '确认取消订阅',
    content: '确定要取消此订阅吗？',
    onOk() {
      if (realtimeStore.unsubscribe(subscriptionId)) {
        addConnectionLog('订阅已取消', 'orange');
        message.success('订阅已取消');
        emit('subscription-removed', subscriptionId);
      } else {
        message.error('取消订阅失败');
      }
    }
  });
}

/**
 * 处理查看订阅详情
 */
function handleViewSubscription(subscription) {
  Modal.info({
    title: '订阅详情',
    width: 600,
    content: () => {
      return h('div', [
        h('p', `主题: ${subscription.topic}`),
        h('p', `订阅时间: ${formatDateTime(subscription.subscribedAt)}`),
        h('p', `过滤器数量: ${subscription.filters?.length || 0}`),
        subscription.filters?.length > 0 && h('div', { class: 'filters-detail' }, [
          h('h5', '过滤器列表:'),
          subscription.filters.map(filter =>
            h('p', `${filter.field} ${filter.operator} ${filter.value}`)
          )
        ])
      ]);
    }
  });
}

/**
 * 添加过滤器
 */
function addFilter() {
  newSubscription.value.filters.push({
    field: '',
    operator: 'eq',
    value: ''
  });
}

/**
 * 移除过滤器
 */
function removeFilter(index) {
  newSubscription.value.filters.splice(index, 1);
}

// 监听连接状态变化
watch(connectionStatus, (newStatus) => {
  emit('connection-changed', newStatus);

  // 添加日志
  switch (newStatus.status) {
    case 'connected':
      addConnectionLog('连接已建立', 'green');
      break;
    case 'disconnected':
      addConnectionLog('连接已断开', 'orange');
      break;
    case 'reconnecting':
      addConnectionLog(`正在重连 (${newStatus.reconnectCount})`, 'blue');
      break;
    case 'error':
      addConnectionLog(`连接错误: ${newStatus.error}`, 'red');
      break;
  }
});

// 监听WebSocket消息
watch(() => realtimeStore.statistics.totalMessages, (newCount, oldCount) => {
  if (newCount > oldCount) {
    emit('message-received');
    addConnectionLog(`收到消息 (总计: ${newCount})`, 'blue');
  }
});

// 生命周期
onMounted(async () => {
  try {
    // 初始化重连配置
    Object.assign(reconnectConfig.value, config.value.websocket);

    // 尝试自动连接
    if (config.value.websocket.autoConnect !== false) {
      await handleConnect();
    }
  } catch (error) {
    console.error('初始化WebSocket客户端失败:', error);
  }
});

onUnmounted(() => {
  // 清理资源
  connectionLogs.value = [];
});

// 暴露方法给父组件
defineExpose({
  connect: handleConnect,
  disconnect: handleDisconnect,
  sendMessage: handleSendMessage,
  addSubscription: handleAddSubscription,
  unsubscribe: handleUnsubscribe
});
</script>

<style lang="less" scoped>
.websocket-client {
  .connection-indicator {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
  }

  .connection-details {
    .connection-logs {
      margin-top: 24px;

      h4 {
        margin-bottom: 16px;
      }

      .log-time {
        color: #666;
        font-size: 12px;
        margin-right: 8px;
      }

      .log-message {
        font-family: monospace;
      }
    }

    .message-statistics {
      margin-top: 24px;

      h4 {
        margin-bottom: 16px;
      }
    }
  }

  .message-sender {
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 6px;
  }

  .subscription-manager {
    .subscription-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h4 {
        margin: 0;
      }
    }

    .subscription-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;

      .subscription-info {
        display: flex;
        align-items: center;
        gap: 8px;

        .subscription-filters {
          font-size: 12px;
          color: #666;
        }
      }

      .subscription-actions {
        display: flex;
        gap: 4px;
      }
    }
  }

  .filters-section {
    width: 100%;

    .filter-item {
      margin-bottom: 8px;
    }
  }
}
</style>