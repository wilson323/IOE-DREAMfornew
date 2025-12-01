<template>
  <div class="event-emitter">
    <!-- 事件发射器面板 -->
    <a-card title="事件发射器" size="small">
      <template #extra>
        <a-space>
          <a-button
            type="primary"
            size="small"
            @click="showEmitModal = true"
          >
            发送事件
          </a-button>
          <a-button
            size="small"
            @click="showListenersModal = true"
          >
            监听器 ({{ listenerCount }})
          </a-button>
          <a-button
            size="small"
            @click="showHistoryModal = true"
          >
            历史记录
          </a-button>
          <a-button
            size="small"
            @click="showConfigModal = true"
          >
            配置
          </a-button>
        </a-space>
      </template>

      <!-- 事件统计 -->
      <div class="event-statistics">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic
              title="总事件数"
              :value="statistics.totalEvents"
              :value-style="{ color: '#1890ff' }"
            >
              <template #suffix>
                <FireOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="监听器数量"
              :value="listenerCount"
              :value-style="{ color: '#52c41a' }"
            >
              <template #suffix>
                <SoundOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="中间件数量"
              :value="middlewareCount"
              :value-style="{ color: '#faad14' }"
            >
              <template #suffix>
                <AppstoreOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="错误次数"
              :value="errorCount"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #suffix>
                <ExclamationCircleOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
      </div>

      <!-- 快速事件触发 -->
      <div class="quick-events">
        <a-divider>快速事件</a-divider>
        <a-space wrap>
          <a-button
            v-for="event in quickEvents"
            :key="event.name"
            size="small"
            :type="event.type || 'default'"
            @click="handleQuickEvent(event)"
          >
            {{ event.label }}
          </a-button>
        </a-space>
      </div>

      <!-- 事件监控 -->
      <div class="event-monitor">
        <a-divider>事件监控</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <div class="monitor-section">
              <h4>最近事件</h4>
              <a-list
                size="small"
                :data-source="recentEvents"
                :locale="{ emptyText: '暂无事件' }"
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <div class="event-item">
                      <a-tag :color="getEventColor(item.type)">{{ item.event }}</a-tag>
                      <span class="event-time">{{ formatTime(item.timestamp) }}</span>
                      <a-button
                        type="link"
                        size="small"
                        @click="showEventDetail(item)"
                      >
                        详情
                      </a-button>
                    </div>
                  </a-list-item>
                </template>
              </a-list>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="monitor-section">
              <h4>事件频率</h4>
              <div class="event-frequency">
                <div
                  v-for="(count, event) in eventFrequency"
                  :key="event"
                  class="frequency-item"
                >
                  <span class="event-name">{{ event }}</span>
                  <a-progress
                    :percent="getFrequencyPercent(count)"
                    size="small"
                    :stroke-color="getFrequencyColor(count)"
                  />
                  <span class="event-count">{{ count }}</span>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-card>

    <!-- 发送事件弹窗 -->
    <a-modal
      v-model:open="showEmitModal"
      title="发送事件"
      :width="700"
      @ok="handleEmitEvent"
    >
      <a-form
        ref="emitForm"
        :model="emitFormData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="事件名称" name="event" :rules="[{ required: true, message: '请输入事件名称' }]">
          <a-input
            v-model:value="emitFormData.event"
            placeholder="例如: user.login"
          />
        </a-form-item>
        <a-form-item label="事件数据" name="data">
          <a-textarea
            v-model:value="emitFormData.dataString"
            :rows="6"
            placeholder="输入JSON格式的事件数据"
          />
        </a-form-item>
        <a-form-item label="选项">
          <a-space direction="vertical" style="width: 100%">
            <a-checkbox v-model:checked="emitFormData.async">异步执行</a-checkbox>
            <a-checkbox v-model:checked="emitFormData.cache">缓存事件</a-checkbox>
            <div v-if="emitFormData.cache">
              <span>缓存TTL(秒):</span>
              <a-input-number
                v-model:value="emitFormData.cacheTtl"
                :min="1"
                :max="3600"
                size="small"
                style="margin-left: 8px"
              />
            </div>
          </a-space>
        </a-form-item>
      </a-form>

      <template #footer>
        <a-space>
          <a-button @click="showEmitModal = false">取消</a-button>
          <a-button @click="handlePreviewEvent">预览</a-button>
          <a-button type="primary" @click="handleEmitEvent">发送</a-button>
        </a-space>
      </template>
    </a-modal>

    <!-- 监听器管理弹窗 -->
    <a-modal
      v-model:open="showListenersModal"
      title="监听器管理"
      :width="900"
      :footer="null"
    >
      <ListenersManager :event-bus="eventBus" />
    </a-modal>

    <!-- 历史记录弹窗 -->
    <a-modal
      v-model:open="showHistoryModal"
      title="事件历史记录"
      :width="1000"
      :footer="null"
    >
      <EventHistory :history="eventHistory" />
    </a-modal>

    <!-- 配置弹窗 -->
    <a-modal
      v-model:open="showConfigModal"
      title="事件发射器配置"
      :width="600"
      @ok="handleSaveConfig"
    >
      <EventConfig :config="eventConfig" @update="handleConfigUpdate" />
    </a-modal>

    <!-- 事件详情弹窗 -->
    <a-modal
      v-model:open="showEventDetailModal"
      title="事件详情"
      :width="600"
      :footer="null"
    >
      <EventDetail :event="selectedEvent" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { useEventBus } from '@/utils/event-bus';
import { formatDateTime } from '@/utils/format';
import ListenersManager from './modals/ListenersManager.vue';
import EventHistory from './modals/EventHistory.vue';
import EventConfig from './modals/EventConfig.vue';
import EventDetail from './modals/EventDetail.vue';

// Icons
import {
  FireOutlined,
  SoundOutlined,
  AppstoreOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue';

// Props
const props = defineProps({
  // 命名空间
  namespace: {
    type: String,
    default: null
  },
  // 快速事件配置
  quickEvents: {
    type: Array,
    default: () => [
      { name: 'user.login', label: '用户登录', type: 'primary' },
      { name: 'user.logout', label: '用户退出', type: 'default' },
      { name: 'system.error', label: '系统错误', type: 'danger' },
      { name: 'data.updated', label: '数据更新', type: 'default' },
      { name: 'notification', label: '发送通知', type: 'primary' }
    ]
  },
  // 最大历史记录数
  maxHistorySize: {
    type: Number,
    default: 1000
  }
});

// Emits
const emit = defineEmits([
  'event-emitted',
  'listener-added',
  'listener-removed'
]);

// 事件总线
const eventBus = useEventBus(props.namespace);

// 响应式数据
const showEmitModal = ref(false);
const showListenersModal = ref(false);
const showHistoryModal = ref(false);
const showConfigModal = ref(false);
const showEventDetailModal = ref(false);
const selectedEvent = ref(null);

const emitFormData = ref({
  event: '',
  dataString: '{}',
  async: false,
  cache: false,
  cacheTtl: 60
});

const eventHistory = ref([]);
const statistics = ref({
  totalEvents: 0,
  totalListeners: 0,
  totalErrors: 0,
  eventCounts: {}
});

const eventConfig = ref({
  debug: true,
  maxListeners: 100,
  enableCache: true,
  defaultCacheTtl: 60
});

const emitForm = ref();

// 计算属性
const listenerCount = computed(() => {
  let count = 0;
  const eventNames = eventBus.eventNames();
  eventNames.forEach(event => {
    count += eventBus.listenerCount(event);
  });
  return count;
});

const middlewareCount = computed(() => {
  return eventBus.middlewares?.length || 0;
});

const errorCount = computed(() => statistics.value.totalErrors);

const recentEvents = computed(() => {
  return eventHistory.value.slice(0, 10);
});

const eventFrequency = computed(() => {
  return statistics.value.eventCounts || {};
});

// 方法
/**
 * 格式化时间
 */
function formatTime(timestamp) {
  return formatDateTime(timestamp, 'HH:mm:ss');
}

/**
 * 获取事件颜色
 */
function getEventColor(type) {
  const colorMap = {
    'user.login': 'green',
    'user.logout': 'orange',
    'system.error': 'red',
    'data.updated': 'blue',
    'notification': 'purple'
  };
  return colorMap[type] || 'default';
}

/**
 * 获取频率百分比
 */
function getFrequencyPercent(count) {
  const maxCount = Math.max(...Object.values(eventFrequency.value));
  return maxCount > 0 ? Math.round((count / maxCount) * 100) : 0;
}

/**
 * 获取频率颜色
 */
function getFrequencyColor(count) {
  if (count >= 10) return '#ff4d4f';
  if (count >= 5) return '#faad14';
  return '#52c41a';
}

/**
 * 处理快速事件
 */
function handleQuickEvent(eventConfig) {
  emitFormData.value = {
    event: eventConfig.name,
    dataString: JSON.stringify({
      timestamp: Date.now(),
      source: 'quick-event',
      type: eventConfig.name
    }, null, 2),
    async: false,
    cache: false,
    cacheTtl: 60
  };
  showEmitModal.value = true;
}

/**
 * 处理发送事件
 */
async function handleEmitEvent() {
  try {
    await emitForm.value.validate();

    let eventData;
    try {
      eventData = JSON.parse(emitFormData.value.dataString);
    } catch (error) {
      throw new Error('事件数据格式不正确，请输入有效的JSON');
    }

    const options = {
      async: emitFormData.value.async,
      cache: emitFormData.value.cache
    };

    if (emitFormData.value.cache) {
      options.cacheTtl = emitFormData.value.cacheTtl * 1000;
    }

    const result = await eventBus.emit(emitFormData.value.event, eventData, options);

    // 添加到历史记录
    addToHistory({
      event: emitFormData.value.event,
      data: eventData,
      timestamp: Date.now(),
      type: 'manual',
      result
    });

    // 更新统计
    updateStatistics(emitFormData.value.event);

    showEmitModal.value = false;
    message.success('事件发送成功');
    emit('event-emitted', { event: emitFormData.value.event, data: eventData, result });

  } catch (error) {
    console.error('发送事件失败:', error);
    message.error('发送事件失败: ' + error.message);
  }
}

/**
 * 处理预览事件
 */
function handlePreviewEvent() {
  try {
    const data = JSON.parse(emitFormData.value.dataString);
    Modal.info({
      title: '事件预览',
      width: 600,
      content: () => {
        return h('div', [
          h('p', `事件名称: ${emitFormData.value.event}`),
          h('p', `事件数据: ${JSON.stringify(data, null, 2)}`),
          h('p', `异步执行: ${emitFormData.value.async ? '是' : '否'}`),
          h('p', `缓存事件: ${emitFormData.value.cache ? '是' : '否'}`)
        ]);
      }
    });
  } catch (error) {
    message.error('事件数据格式不正确');
  }
}

/**
 * 添加到历史记录
 */
function addToHistory(eventRecord) {
  eventHistory.value.unshift(eventRecord);

  // 限制历史记录数量
  if (eventHistory.value.length > props.maxHistorySize) {
    eventHistory.value = eventHistory.value.slice(0, props.maxHistorySize);
  }
}

/**
 * 更新统计信息
 */
function updateStatistics(eventName) {
  statistics.value.totalEvents++;

  if (!statistics.value.eventCounts[eventName]) {
    statistics.value.eventCounts[eventName] = 0;
  }
  statistics.value.eventCounts[eventName]++;
}

/**
 * 显示事件详情
 */
function showEventDetail(event) {
  selectedEvent.value = event;
  showEventDetailModal.value = true;
}

/**
 * 处理保存配置
 */
function handleSaveConfig() {
  // 应用配置
  if (eventConfig.value.debug !== undefined) {
    eventBus.debug = eventConfig.value.debug;
  }

  showConfigModal.value = false;
  message.success('配置已保存');
}

/**
 * 处理配置更新
 */
function handleConfigUpdate(newConfig) {
  Object.assign(eventConfig.value, newConfig);
}

/**
 * 监听事件总线事件
 */
function setupEventListeners() {
  // 监听所有事件
  const originalEmit = eventBus.emit;
  eventBus.emit = async function(event, data, options) {
    try {
      const result = await originalEmit.call(this, event, data, options);

      // 记录事件
      addToHistory({
        event,
        data,
        timestamp: Date.now(),
        type: 'emitted',
        result,
        options
      });

      updateStatistics(event);

      return result;
    } catch (error) {
      statistics.value.totalErrors++;
      console.error('事件发射失败:', error);
      throw error;
    }
  };
}

// 监听
watch(statistics, (newStats) => {
  // 可以在这里添加统计变化的处理逻辑
}, { deep: true });

// 生命周期
onMounted(() => {
  setupEventListeners();
});

onUnmounted(() => {
  // 清理资源
});

// 暴露方法
defineExpose({
  emit: eventBus.emit,
  on: eventBus.on,
  off: eventBus.off,
  once: eventBus.once,
  getStatistics: () => statistics.value,
  getHistory: () => eventHistory.value,
  clearHistory: () => { eventHistory.value = []; }
});
</script>

<style lang="less" scoped>
.event-emitter {
  .event-statistics {
    margin-bottom: 24px;
  }

  .quick-events {
    margin-bottom: 24px;
  }

  .event-monitor {
    .monitor-section {
      h4 {
        margin-bottom: 16px;
        color: #262626;
      }

      .event-item {
        display: flex;
        align-items: center;
        gap: 8px;
        width: 100%;

        .event-time {
          font-size: 12px;
          color: #666;
          margin-left: auto;
        }
      }

      .event-frequency {
        .frequency-item {
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          .event-name {
            min-width: 100px;
            font-size: 12px;
          }

          .event-count {
            min-width: 30px;
            text-align: right;
            font-size: 12px;
            margin-left: 8px;
          }
        }
      }
    }
  }
}
</style>